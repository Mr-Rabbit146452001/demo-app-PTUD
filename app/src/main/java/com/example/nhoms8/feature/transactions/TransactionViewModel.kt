package com.example.nhoms8.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.usecase.EngineSplitMode
import com.example.nhoms8.domain.usecase.ParticipantInput
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import com.example.nhoms8.domain.usecase.SplitValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Trạng thái soạn thảo / chỉnh sửa khoản chi và cấu hình chia tiền 3 chế độ.
 */
data class ExpenseDraftUiState(
    val editingExpenseId: String? = null,
    val groupId: String = "",
    val groupName: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val category: SpendlyCategory = SpendlyCategory.FOOD,
    val payerId: String = "",
    val payerName: String = "",
    val description: String = "",
    val receiptUrl: String = "",
    val expenseDateMillis: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val splitMode: EngineSplitMode = EngineSplitMode.EQUAL,
    val payerParticipates: Boolean = true,
    val participantInputs: List<ParticipantInput> = emptyList(),
    val validationResult: SplitValidationResult = SplitValidationResult.Invalid("Tổng số tiền phải lớn hơn 0 đ"),
    val members: List<SpendlyGroupMember> = emptyList(),
    val percentageTexts: Map<String, String> = emptyMap(),
    val customAmountTexts: Map<String, String> = emptyMap(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isEditable: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val isEditMode: Boolean
        get() = editingExpenseId != null
}

/**
 * Trạng thái màn hình Thêm khoản thu / Nạp quỹ nhóm.
 */
data class IncomeUiState(
    val groupId: String = "",
    val groupName: String = "",
    val members: List<SpendlyGroupMember> = emptyList(),
    val defaultPayerId: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Trạng thái màn hình Chi tiết khoản chi.
 */
data class ExpenseDetailUiState(
    val isLoading: Boolean = true,
    val expense: SpendlyExpense? = null,
    val participants: List<ExpenseParticipant> = emptyList(),
    val isCancelling: Boolean = false,
    val errorMessage: String? = null
)

private data class DetailOpState(
    val isCancelling: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel điều phối luồng Tạo/Sửa khoản chi, Chia tiền V2 (Equal/Percentage/Custom),
 * Nạp quỹ nhóm (Income) và Chi tiết/Hủy mềm khoản chi theo nguyên tắc Sổ cái.
 */
class TransactionViewModel(
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _draftState = MutableStateFlow(ExpenseDraftUiState())
    val draftState: StateFlow<ExpenseDraftUiState> = _draftState.asStateFlow()

    private val _incomeState = MutableStateFlow(IncomeUiState())
    val incomeState: StateFlow<IncomeUiState> = _incomeState.asStateFlow()

    private val _detailOpState = MutableStateFlow(DetailOpState())
    private val detailFlows = mutableMapOf<String, StateFlow<ExpenseDetailUiState>>()

    private var draftObserverJob: Job? = null
    private var incomeObserverJob: Job? = null

    /**
     * Khởi tạo bản nháp khoản chi mới cho [groupId], tự động nạp danh sách thành viên.
     */
    fun startNewExpense(groupId: String, currentUserId: String) {
        draftObserverJob?.cancel()
        val now = System.currentTimeMillis()
        _draftState.value = ExpenseDraftUiState(
            editingExpenseId = null,
            groupId = groupId,
            createdBy = currentUserId,
            payerId = currentUserId,
            expenseDateMillis = now,
            splitMode = EngineSplitMode.EQUAL,
            payerParticipates = true,
            isLoading = groupId.isNotBlank(),
            isEditable = true
        )

        if (groupId.isBlank()) {
            _draftState.update {
                recomputeValidation(it.copy(isLoading = false, errorMessage = "Chưa chọn quỹ nhóm"))
            }
            return
        }

        draftObserverJob = viewModelScope.launch {
            combine(
                groupRepository.observeGroup(groupId),
                groupRepository.observeGroupMembers(groupId)
            ) { group, members ->
                group to members
            }.catch { throwable ->
                _draftState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải thông tin nhóm"
                    )
                }
            }.collect { (group, members) ->
                _draftState.update { current ->
                    mergeMembersIntoNewDraft(
                        current = current,
                        groupName = group?.name.orEmpty(),
                        members = members,
                        preferredUserId = currentUserId
                    )
                }
            }
        }
    }

    /**
     * Nạp khoản chi hiện có để chỉnh sửa đầy đủ số tiền, danh mục, người trả và cấu hình chia.
     */
    fun loadExpenseForEdit(groupId: String, expenseId: String) {
        draftObserverJob?.cancel()
        _draftState.value = ExpenseDraftUiState(
            editingExpenseId = expenseId,
            groupId = groupId,
            isLoading = true,
            isEditable = true
        )

        if (groupId.isBlank() || expenseId.isBlank()) {
            _draftState.update {
                recomputeValidation(
                    it.copy(
                        isLoading = false,
                        isEditable = false,
                        errorMessage = "Thiếu mã nhóm hoặc mã khoản chi"
                    )
                )
            }
            return
        }

        draftObserverJob = viewModelScope.launch {
            var seededExpense = false
            var seededParticipantsById = emptyMap<String, ExpenseParticipant>()
            var seededWithFullMembers = false

            combine(
                groupRepository.observeGroup(groupId),
                groupRepository.observeGroupMembers(groupId),
                transactionRepository.observeExpenseDetail(groupId, expenseId)
            ) { group, members, detail ->
                Triple(group, members, detail)
            }.catch { throwable ->
                _draftState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải dữ liệu khoản chi"
                    )
                }
            }.collect { (group, members, detail) ->
                if (!seededExpense) {
                    if (detail == null) {
                        _draftState.update {
                            recomputeValidation(
                                it.copy(
                                    groupName = group?.name.orEmpty(),
                                    members = members,
                                    isLoading = false,
                                    isEditable = false,
                                    errorMessage = "Không tìm thấy khoản chi cần chỉnh sửa"
                                )
                            )
                        }
                        return@collect
                    }

                    val (expense, participants) = detail
                    seededExpense = true
                    seededParticipantsById = participants.associateBy { it.userId }
                    seededWithFullMembers = members.isNotEmpty()

                    val splitMode = expense.splitType.toEngineMode()
                    val payerParticipates = seededParticipantsById.containsKey(expense.payerId)

                    val inputs = if (members.isNotEmpty()) {
                        buildInputsFromMembers(members, seededParticipantsById)
                    } else {
                        participants.map { p ->
                            ParticipantInput(
                                userId = p.userId,
                                displayName = p.displayName,
                                isIncluded = true,
                                percentage = p.percentage,
                                customAmount = p.shareAmount
                            )
                        }
                    }

                    val percentageTexts = inputs.associate { input ->
                        input.userId to formatEditablePercent(input.percentage)
                    }
                    val customAmountTexts = inputs.associate { input ->
                        input.userId to formatEditableAmount(input.customAmount)
                    }
                    val editable = expense.status == RecordStatus.ACTIVE

                    _draftState.update {
                        recomputeValidation(
                            it.copy(
                                editingExpenseId = expense.expenseId,
                                groupId = expense.groupId.ifBlank { groupId },
                                groupName = group?.name.orEmpty(),
                                title = expense.title,
                                amount = expense.amount,
                                category = expense.category,
                                payerId = expense.payerId,
                                payerName = expense.payerName.ifBlank {
                                    members.firstOrNull { m -> m.userId == expense.payerId }?.displayName.orEmpty()
                                },
                                description = expense.description,
                                receiptUrl = expense.receiptUrl,
                                expenseDateMillis = expense.expenseDateMillis.takeIf { ts -> ts > 0L }
                                    ?: System.currentTimeMillis(),
                                createdBy = expense.createdBy,
                                splitMode = splitMode,
                                payerParticipates = payerParticipates,
                                participantInputs = inputs,
                                members = members,
                                percentageTexts = percentageTexts,
                                customAmountTexts = customAmountTexts,
                                isLoading = false,
                                isEditable = editable,
                                errorMessage = if (editable) null else "Khoản chi đã bị hủy không thể chỉnh sửa"
                            )
                        )
                    }
                } else {
                    _draftState.update { current ->
                        val nextGroupName = group?.name.orEmpty().ifBlank { current.groupName }
                        if (!seededWithFullMembers && members.isNotEmpty()) {
                            seededWithFullMembers = true
                            val fullInputs = buildInputsFromMembers(members, seededParticipantsById)
                            recomputeValidation(
                                current.copy(
                                    groupName = nextGroupName,
                                    members = members,
                                    participantInputs = fullInputs,
                                    percentageTexts = fullInputs.associate {
                                        it.userId to (current.percentageTexts[it.userId]
                                            ?: formatEditablePercent(it.percentage))
                                    },
                                    customAmountTexts = fullInputs.associate {
                                        it.userId to (current.customAmountTexts[it.userId]
                                            ?: formatEditableAmount(it.customAmount))
                                    }
                                )
                            )
                        } else {
                            recomputeValidation(
                                current.copy(
                                    groupName = nextGroupName,
                                    members = if (members.isNotEmpty()) members else current.members
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Cập nhật từng phần thông tin cơ bản của khoản chi và tự động tính lại validation chia tiền.
     */
    fun updateDraft(
        title: String? = null,
        amount: Long? = null,
        category: SpendlyCategory? = null,
        payerId: String? = null,
        payerName: String? = null,
        description: String? = null,
        receiptUrl: String? = null,
        expenseDateMillis: Long? = null
    ) {
        _draftState.update { current ->
            val nextPayerId = payerId ?: current.payerId
            val resolvedPayerName = when {
                payerName != null -> payerName
                payerId != null -> current.members.firstOrNull { it.userId == payerId }?.displayName
                    ?: current.payerName
                else -> current.payerName
            }

            // Nếu người dùng đổi người trả trong khi chế độ "người trả KHÔNG tham gia" đang bật,
            // tự động đồng bộ trạng thái checkbox của người trả cũ/mới.
            val updatedParticipants = if (
                payerId != null &&
                payerId != current.payerId &&
                !current.payerParticipates
            ) {
                current.participantInputs.map { input ->
                    when (input.userId) {
                        current.payerId -> input.copy(isIncluded = true)
                        payerId -> input.copy(isIncluded = false)
                        else -> input
                    }
                }
            } else {
                current.participantInputs
            }

            recomputeValidation(
                current.copy(
                    title = title ?: current.title,
                    amount = amount ?: current.amount,
                    category = category ?: current.category,
                    payerId = nextPayerId,
                    payerName = resolvedPayerName,
                    description = description ?: current.description,
                    receiptUrl = receiptUrl ?: current.receiptUrl,
                    expenseDateMillis = expenseDateMillis ?: current.expenseDateMillis,
                    participantInputs = updatedParticipants,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Chuyển chế độ chia tiền (EQUAL / PERCENTAGE / CUSTOM) và validate lại tức thì.
     */
    fun setSplitMode(mode: EngineSplitMode) {
        _draftState.update { current ->
            recomputeValidation(
                current.copy(
                    splitMode = mode,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Bật/tắt thành viên tham gia chia khoản chi; nếu là người trả thì đồng bộ cờ [payerParticipates].
     */
    fun toggleMemberIncluded(userId: String) {
        _draftState.update { current ->
            val updatedInputs = current.participantInputs.map { input ->
                if (input.userId == userId) {
                    input.copy(isIncluded = !input.isIncluded)
                } else {
                    input
                }
            }
            val toggledPayerState = updatedInputs
                .firstOrNull { it.userId == current.payerId }
                ?.isIncluded
                ?: current.payerParticipates

            recomputeValidation(
                current.copy(
                    payerParticipates = if (userId == current.payerId) {
                        toggledPayerState
                    } else {
                        current.payerParticipates
                    },
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Xử lý edge case PRD #1: người trả hộ có thể không tham gia chia tiền (tắt checkbox của payer
     * trong danh sách chia nhưng vẫn giữ nguyên tư cách người trả tiền).
     */
    fun setPayerParticipates(participates: Boolean) {
        _draftState.update { current ->
            val updatedInputs = current.participantInputs.map { input ->
                if (input.userId == current.payerId) {
                    input.copy(isIncluded = participates)
                } else {
                    input
                }
            }
            recomputeValidation(
                current.copy(
                    payerParticipates = participates,
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Cập nhật tỷ lệ % cho một thành viên khi ở chế độ PERCENTAGE.
     */
    fun updateMemberPercentage(userId: String, pctText: String) {
        val normalized = pctText.trim().replace(',', '.')
        val parsed = if (normalized.isEmpty()) 0.0 else normalized.toDoubleOrNull()

        _draftState.update { current ->
            val updatedTexts = current.percentageTexts + (userId to normalized)
            val updatedInputs = current.participantInputs.map { input ->
                if (input.userId == userId) {
                    input.copy(percentage = parsed ?: 0.0)
                } else {
                    input
                }
            }
            recomputeValidation(
                current.copy(
                    percentageTexts = updatedTexts,
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Cập nhật số tiền cụ thể cho một thành viên khi ở chế độ CUSTOM.
     */
    fun updateMemberCustomAmount(userId: String, amtText: String) {
        val trimmed = amtText.trim()
        val isNegativeAttempt = trimmed.startsWith("-")
        val digitsOnly = trimmed.replace(".", "").replace(",", "").let { raw ->
            if (isNegativeAttempt) {
                "-" + raw.drop(1).filter { it.isDigit() }
            } else {
                raw.filter { it.isDigit() }
            }
        }
        val parsed = if (digitsOnly.isEmpty() || digitsOnly == "-") 0L else digitsOnly.toLongOrNull() ?: 0L

        _draftState.update { current ->
            val updatedTexts = current.customAmountTexts + (userId to digitsOnly)
            val updatedInputs = current.participantInputs.map { input ->
                if (input.userId == userId) {
                    input.copy(customAmount = parsed)
                } else {
                    input
                }
            }
            recomputeValidation(
                current.copy(
                    customAmountTexts = updatedTexts,
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Tiện ích chia đều 100% cho các thành viên đang được chọn trong chế độ PERCENTAGE.
     */
    fun distributePercentagesEqually() {
        _draftState.update { current ->
            val includedIds = current.participantInputs.filter { it.isIncluded }.map { it.userId }
            if (includedIds.isEmpty()) return@update current

            val countBd = BigDecimal(includedIds.size)
            val hundred = BigDecimal("100.00")
            val base = hundred.divide(countBd, 2, RoundingMode.DOWN)
            val remainder = hundred.subtract(base.multiply(countBd))

            val pctMap = mutableMapOf<String, Double>()
            includedIds.forEachIndexed { index, uid ->
                val value = if (index == 0) base.add(remainder) else base
                pctMap[uid] = value.toDouble()
            }

            val updatedTexts = current.percentageTexts.toMutableMap()
            val updatedInputs = current.participantInputs.map { input ->
                val assigned = pctMap[input.userId] ?: 0.0
                if (input.isIncluded) {
                    updatedTexts[input.userId] = formatEditablePercent(assigned)
                    input.copy(percentage = assigned)
                } else {
                    updatedTexts[input.userId] = ""
                    input.copy(percentage = 0.0)
                }
            }

            recomputeValidation(
                current.copy(
                    percentageTexts = updatedTexts,
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Tiện ích điền sẵn số tiền chia đều vào các ô CUSTOM để người dùng tinh chỉnh nhanh.
     */
    fun prefillCustomFromEqualSplit() {
        _draftState.update { current ->
            if (current.amount <= 0L) return@update current
            val equalResult = SplitCalculationEngine.calculateAndValidateSplit(
                totalAmount = current.amount,
                mode = EngineSplitMode.EQUAL,
                participants = current.participantInputs
            )
            if (equalResult !is SplitValidationResult.Valid) return@update current

            val shareMap = equalResult.shares.associate { it.userId to it.shareAmount }
            val updatedTexts = current.customAmountTexts.toMutableMap()
            val updatedInputs = current.participantInputs.map { input ->
                val share = shareMap[input.userId] ?: 0L
                updatedTexts[input.userId] = formatEditableAmount(share)
                input.copy(customAmount = share)
            }
            recomputeValidation(
                current.copy(
                    customAmountTexts = updatedTexts,
                    participantInputs = updatedInputs,
                    errorMessage = null
                )
            )
        }
    }

    /**
     * Xác thực toàn bộ khoản chi + phần chia và ghi nguyên tử xuống Firestore WriteBatch.
     */
    fun commitExpense(onSuccess: () -> Unit = {}) {
        val state = recomputeValidation(_draftState.value)
        if (!state.isEditable) {
            _draftState.update {
                it.copy(errorMessage = "Khoản chi đã bị hủy không thể chỉnh sửa")
            }
            return
        }
        if (state.groupId.isBlank()) {
            _draftState.update { it.copy(errorMessage = "Chưa xác định quỹ nhóm cho khoản chi") }
            return
        }
        if (state.title.trim().isBlank()) {
            _draftState.update { it.copy(errorMessage = "Vui lòng nhập tên khoản chi") }
            return
        }
        if (state.payerId.isBlank()) {
            _draftState.update { it.copy(errorMessage = "Vui lòng chọn người thanh toán") }
            return
        }

        val validation = state.validationResult
        if (validation !is SplitValidationResult.Valid) {
            _draftState.update {
                it.copy(
                    validationResult = validation,
                    errorMessage = (validation as SplitValidationResult.Invalid).errorMessage
                )
            }
            return
        }

        val resolvedPayerName = state.payerName.ifBlank {
            state.members.firstOrNull { it.userId == state.payerId }?.displayName ?: state.payerId
        }
        val participants = validation.shares.map { share ->
            ExpenseParticipant(
                userId = share.userId,
                displayName = share.displayName,
                shareAmount = share.shareAmount,
                percentage = share.percentage,
                paidAmount = 0L
            )
        }
        val expense = SpendlyExpense(
            expenseId = state.editingExpenseId.orEmpty(),
            groupId = state.groupId,
            title = state.title.trim(),
            amount = state.amount,
            category = state.category,
            payerId = state.payerId,
            payerName = resolvedPayerName,
            createdBy = state.createdBy.ifBlank { state.payerId },
            splitType = state.splitMode.toDomainMethod(),
            description = state.description.trim(),
            receiptUrl = state.receiptUrl.trim(),
            expenseDateMillis = state.expenseDateMillis,
            status = RecordStatus.ACTIVE
        )

        viewModelScope.launch {
            _draftState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            val result = if (state.editingExpenseId == null) {
                transactionRepository.createExpenseWithParticipants(
                    groupId = state.groupId,
                    expense = expense,
                    participants = participants
                ).map { Unit }
            } else {
                transactionRepository.updateExpenseWithParticipants(
                    groupId = state.groupId,
                    expense = expense,
                    participants = participants
                )
            }

            result.onSuccess {
                _draftState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = null,
                        successMessage = if (state.isEditMode) {
                            "Đã cập nhật khoản chi thành công"
                        } else {
                            "Đã lưu khoản chi thành công"
                        }
                    )
                }
                onSuccess()
            }.onFailure { throwable ->
                _draftState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = throwable.message ?: "Không thể lưu khoản chi"
                    )
                }
            }
        }
    }

    /**
     * Nạp thông tin quỹ và thành viên cho màn hình Thêm khoản thu / Nạp quỹ.
     */
    fun loadGroupForIncome(groupId: String, currentUserId: String = "") {
        incomeObserverJob?.cancel()
        _incomeState.value = IncomeUiState(
            groupId = groupId,
            defaultPayerId = currentUserId,
            isLoading = groupId.isNotBlank()
        )
        if (groupId.isBlank()) return

        incomeObserverJob = viewModelScope.launch {
            combine(
                groupRepository.observeGroup(groupId),
                groupRepository.observeGroupMembers(groupId)
            ) { group, members ->
                group to members
            }.catch { throwable ->
                _incomeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải thành viên quỹ"
                    )
                }
            }.collect { (group, members) ->
                val resolvedDefaultPayer = when {
                    currentUserId.isNotBlank() && members.any { it.userId == currentUserId } -> currentUserId
                    else -> members.firstOrNull { it.role != GroupRole.VIEWER }?.userId
                        ?: members.firstOrNull()?.userId
                        ?: currentUserId
                }
                _incomeState.update {
                    it.copy(
                        groupName = group?.name.orEmpty(),
                        members = members,
                        defaultPayerId = resolvedDefaultPayer,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Ghi nhận khoản thu / nạp quỹ nhóm (`isFundContribution = true`), tách biệt hoàn toàn với quyết toán nợ.
     */
    fun createIncome(
        groupId: String,
        title: String,
        amount: Long,
        payerId: String,
        category: SpendlyCategory = SpendlyCategory.OTHER,
        description: String = "",
        currentUserId: String,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedTitle = title.trim()
        if (groupId.isBlank()) {
            _incomeState.update { it.copy(errorMessage = "Chưa xác định quỹ nhóm") }
            return
        }
        if (trimmedTitle.isBlank()) {
            _incomeState.update { it.copy(errorMessage = "Vui lòng nhập tên khoản thu / nạp quỹ") }
            return
        }
        if (amount <= 0L) {
            _incomeState.update { it.copy(errorMessage = "Số tiền nạp quỹ phải lớn hơn 0 đ") }
            return
        }
        if (payerId.isBlank()) {
            _incomeState.update { it.copy(errorMessage = "Vui lòng chọn thành viên đóng quỹ") }
            return
        }

        val payerName = _incomeState.value.members.firstOrNull { it.userId == payerId }?.displayName
            ?: _draftState.value.members.firstOrNull { it.userId == payerId }?.displayName
            ?: ""

        val income = SpendlyIncome(
            groupId = groupId,
            title = trimmedTitle,
            amount = amount,
            payerId = payerId,
            payerName = payerName,
            category = category,
            description = description.trim(),
            incomeDateMillis = System.currentTimeMillis(),
            createdBy = currentUserId.ifBlank { payerId },
            status = RecordStatus.ACTIVE,
            isFundContribution = true
        )

        viewModelScope.launch {
            _incomeState.update { it.copy(isSaving = true, errorMessage = null, successMessage = null) }
            transactionRepository.createIncome(groupId, income)
                .onSuccess {
                    _incomeState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = null,
                            successMessage = "Đã ghi nhận khoản nạp quỹ thành công"
                        )
                    }
                    onSuccess()
                }
                .onFailure { throwable ->
                    _incomeState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = throwable.message ?: "Không thể ghi nhận khoản nạp quỹ"
                        )
                    }
                }
        }
    }

    /**
     * Hủy mềm khoản chi (soft-delete Ledger rule: chuyển trạng thái sang CANCELLED và tính lại số dư).
     */
    fun cancelExpense(
        groupId: String,
        expenseId: String,
        actorId: String,
        actorName: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _detailOpState.value = DetailOpState(isCancelling = true, errorMessage = null)
            transactionRepository.cancelExpense(
                groupId = groupId,
                expenseId = expenseId,
                actorId = actorId,
                actorName = actorName
            ).onSuccess {
                _detailOpState.value = DetailOpState(isCancelling = false, errorMessage = null)
                onSuccess()
            }.onFailure { throwable ->
                _detailOpState.value = DetailOpState(
                    isCancelling = false,
                    errorMessage = throwable.message ?: "Không thể hủy khoản chi"
                )
            }
        }
    }

    /**
     * Quan sát chi tiết khoản chi kèm danh sách phần chia thành viên theo thời gian thực.
     */
    fun observeExpenseDetail(groupId: String, expenseId: String): StateFlow<ExpenseDetailUiState> {
        val key = "$groupId::$expenseId"
        return detailFlows.getOrPut(key) {
            combine(
                transactionRepository.observeExpenseDetail(groupId, expenseId),
                _detailOpState
            ) { detail, opState ->
                if (detail == null) {
                    ExpenseDetailUiState(
                        isLoading = false,
                        expense = null,
                        participants = emptyList(),
                        isCancelling = opState.isCancelling,
                        errorMessage = opState.errorMessage
                    )
                } else {
                    ExpenseDetailUiState(
                        isLoading = false,
                        expense = detail.first,
                        participants = detail.second,
                        isCancelling = opState.isCancelling,
                        errorMessage = opState.errorMessage
                    )
                }
            }.catch { throwable ->
                emit(
                    ExpenseDetailUiState(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải chi tiết khoản chi"
                    )
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = ExpenseDetailUiState(isLoading = true)
            )
        }
    }

    fun clearDraftError() {
        _draftState.update { it.copy(errorMessage = null) }
    }

    fun clearIncomeError() {
        _incomeState.update { it.copy(errorMessage = null) }
    }

    fun clearDetailError() {
        _detailOpState.update { it.copy(errorMessage = null) }
    }

    private fun mergeMembersIntoNewDraft(
        current: ExpenseDraftUiState,
        groupName: String,
        members: List<SpendlyGroupMember>,
        preferredUserId: String
    ): ExpenseDraftUiState {
        val existingByUser = current.participantInputs.associateBy { it.userId }
        val resolvedPayer = when {
            current.payerId.isNotBlank() && members.any { it.userId == current.payerId } ->
                members.first { it.userId == current.payerId }
            preferredUserId.isNotBlank() && members.any { it.userId == preferredUserId } ->
                members.first { it.userId == preferredUserId }
            else -> members.firstOrNull()
        }
        val payerId = resolvedPayer?.userId ?: current.payerId
        val payerName = resolvedPayer?.displayName ?: current.payerName

        val mergedInputs = members.map { member ->
            val previous = existingByUser[member.userId]
            if (previous != null) {
                previous.copy(displayName = member.displayName)
            } else {
                ParticipantInput(
                    userId = member.userId,
                    displayName = member.displayName,
                    isIncluded = if (member.userId == payerId) current.payerParticipates else true,
                    percentage = 0.0,
                    customAmount = 0L
                )
            }
        }

        return recomputeValidation(
            current.copy(
                groupName = groupName.ifBlank { current.groupName },
                members = members,
                payerId = payerId,
                payerName = payerName,
                participantInputs = mergedInputs,
                isLoading = false
            )
        )
    }

    private fun buildInputsFromMembers(
        members: List<SpendlyGroupMember>,
        participantById: Map<String, ExpenseParticipant>
    ): List<ParticipantInput> {
        return members.map { member ->
            val existing = participantById[member.userId]
            ParticipantInput(
                userId = member.userId,
                displayName = member.displayName,
                isIncluded = existing != null,
                percentage = existing?.percentage ?: 0.0,
                customAmount = existing?.shareAmount ?: 0L
            )
        }
    }

    private fun recomputeValidation(state: ExpenseDraftUiState): ExpenseDraftUiState {
        val validMemberIds = state.members
            .map { it.userId }
            .toSet()
            .takeIf { it.isNotEmpty() }
        val validation = SplitCalculationEngine.calculateAndValidateSplit(
            totalAmount = state.amount,
            mode = state.splitMode,
            participants = state.participantInputs,
            validMemberIds = validMemberIds
        )
        return state.copy(validationResult = validation)
    }

    private fun SplitMethod.toEngineMode(): EngineSplitMode = when (this) {
        SplitMethod.EQUAL -> EngineSplitMode.EQUAL
        SplitMethod.PERCENTAGE -> EngineSplitMode.PERCENTAGE
        SplitMethod.CUSTOM -> EngineSplitMode.CUSTOM
    }

    private fun EngineSplitMode.toDomainMethod(): SplitMethod = when (this) {
        EngineSplitMode.EQUAL -> SplitMethod.EQUAL
        EngineSplitMode.PERCENTAGE -> SplitMethod.PERCENTAGE
        EngineSplitMode.CUSTOM -> SplitMethod.CUSTOM
    }

    private fun formatEditablePercent(percentage: Double): String {
        if (percentage <= 0.0) return ""
        val bd = BigDecimal.valueOf(percentage).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros()
        return bd.toPlainString()
    }

    private fun formatEditableAmount(amount: Long): String {
        return if (amount <= 0L) "" else amount.toString()
    }

    companion object {
        fun factory(
            groupRepository: GroupRepository,
            transactionRepository: TransactionRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
                    return TransactionViewModel(
                        groupRepository = groupRepository,
                        transactionRepository = transactionRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
