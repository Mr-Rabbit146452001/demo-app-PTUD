package com.example.nhoms8.feature.receipt

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.data.GeminiChatRepository
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.usecase.EngineSplitMode
import com.example.nhoms8.domain.usecase.ParticipantInput
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import com.example.nhoms8.domain.usecase.SplitValidationResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException

enum class ReceiptScanStep {
    CAMERA,
    ANALYZING,
    REVIEW,
    ERROR
}

data class ReceiptScannerUiState(
    val step: ReceiptScanStep = ReceiptScanStep.CAMERA,
    val groupId: String = "",
    val groupName: String = "",
    val members: List<SpendlyGroupMember> = emptyList(),
    val capturedImagePath: String = "",
    val scannedResult: ScannedReceiptResult? = null,
    val editableTitle: String = "",
    val editableAmountText: String = "",
    val editableCategory: SpendlyCategory = SpendlyCategory.FOOD,
    val editablePayerId: String = "",
    val editableDescription: String = "",
    val editableDateMillis: Long = System.currentTimeMillis(),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val canRetry: Boolean = false
) {
    val parsedAmount: Long
        get() = editableAmountText.filter { it.isDigit() }.toLongOrNull() ?: 0L
}

/**
 * ViewModel điều phối toàn bộ luồng Quét hóa đơn AI:
 * Tiền xử lý ảnh -> Gemini 2.5 Flash Vision OCR -> Review & chỉnh sửa -> Lưu nhanh / Chia tiền chi tiết.
 */
class ReceiptScannerViewModel(
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val geminiChatRepository: GeminiChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReceiptScannerUiState())
    val uiState: StateFlow<ReceiptScannerUiState> = _uiState.asStateFlow()

    private var contextObserverJob: Job? = null
    private var analyzeJob: Job? = null

    fun bindContext(groupId: String, currentUserId: String) {
        if (groupId.isBlank()) return
        if (_uiState.value.groupId == groupId && contextObserverJob?.isActive == true) return

        _uiState.update {
            it.copy(
                groupId = groupId,
                editablePayerId = it.editablePayerId.ifBlank { currentUserId }
            )
        }

        contextObserverJob?.cancel()
        contextObserverJob = viewModelScope.launch {
            combine(
                groupRepository.observeGroup(groupId),
                groupRepository.observeGroupMembers(groupId)
            ) { group, members ->
                group to members
            }.catch {
                // Giữ nguyên danh sách hiện tại nếu gặp lỗi luồng tạm thời
            }.collect { (group, members) ->
                val activeMembers = members.filter { it.status == "ACTIVE" }
                _uiState.update { current ->
                    val resolvedPayerId = when {
                        current.editablePayerId.isNotBlank() &&
                            activeMembers.any { it.userId == current.editablePayerId } -> current.editablePayerId
                        currentUserId.isNotBlank() &&
                            activeMembers.any { it.userId == currentUserId } -> currentUserId
                        else -> activeMembers.firstOrNull()?.userId ?: current.editablePayerId
                    }
                    current.copy(
                        groupName = group?.name.orEmpty().ifBlank { current.groupName },
                        members = activeMembers,
                        editablePayerId = resolvedPayerId
                    )
                }
            }
        }
    }

    fun analyzePreparedImage(prepared: PreparedReceiptImage) {
        analyzeJob?.cancel()
        _uiState.update {
            it.copy(
                step = ReceiptScanStep.ANALYZING,
                capturedImagePath = prepared.savedFilePath,
                errorMessage = null,
                canRetry = true
            )
        }

        analyzeJob = viewModelScope.launch {
            val current = _uiState.value
            val result = geminiChatRepository.scanReceipt(
                bitmap = prepared.bitmap,
                savedReceiptPath = prepared.savedFilePath,
                groupName = current.groupName,
                memberNames = current.members.map { it.displayName }
            )

            result.onSuccess { scanned ->
                if (!scanned.isReceipt) {
                    _uiState.update { state ->
                        state.copy(
                            step = ReceiptScanStep.ERROR,
                            scannedResult = scanned,
                            errorMessage = scanned.warningMessage
                                ?: "Ảnh này chưa giống hóa đơn thanh toán. Vui lòng chụp lại rõ nét hơn.",
                            canRetry = state.capturedImagePath.isNotBlank()
                        )
                    }
                } else {
                    _uiState.update { state ->
                        state.copy(
                            step = ReceiptScanStep.REVIEW,
                            scannedResult = scanned,
                            editableTitle = scanned.title,
                            editableAmountText = if (scanned.totalAmount > 0L) scanned.totalAmount.toString() else "",
                            editableCategory = scanned.category,
                            editableDescription = scanned.description,
                            editableDateMillis = scanned.expenseDateMillis ?: System.currentTimeMillis(),
                            errorMessage = null,
                            canRetry = true
                        )
                    }
                }
            }.onFailure { throwable ->
                runCatching { Log.w("ReceiptScanner", "scanReceipt failed", throwable) }
                _uiState.update { state ->
                    state.copy(
                        step = ReceiptScanStep.ERROR,
                        errorMessage = mapFriendlyError(throwable),
                        canRetry = state.capturedImagePath.isNotBlank()
                    )
                }
            }
        }
    }

    fun reportImagePreparationError(message: String) {
        _uiState.update {
            it.copy(
                step = ReceiptScanStep.ERROR,
                errorMessage = message.ifBlank { "Không thể đọc ảnh hóa đơn đã chọn" },
                canRetry = it.capturedImagePath.isNotBlank()
            )
        }
    }

    fun retryLastImage(context: Context) {
        val savedPath = _uiState.value.capturedImagePath
        if (savedPath.isBlank()) {
            resetToCamera()
            return
        }
        analyzeJob?.cancel()
        _uiState.update {
            it.copy(
                step = ReceiptScanStep.ANALYZING,
                errorMessage = null
            )
        }
        analyzeJob = viewModelScope.launch {
            val bitmap = ReceiptImageProcessor.loadSavedBitmap(savedPath)
            if (bitmap == null) {
                _uiState.update {
                    it.copy(
                        step = ReceiptScanStep.ERROR,
                        errorMessage = "Ảnh tạm không còn khả dụng, vui lòng chụp lại hóa đơn",
                        canRetry = false
                    )
                }
                return@launch
            }
            analyzePreparedImage(PreparedReceiptImage(bitmap = bitmap, savedFilePath = savedPath))
        }
    }

    fun resetToCamera() {
        analyzeJob?.cancel()
        _uiState.update {
            it.copy(
                step = ReceiptScanStep.CAMERA,
                errorMessage = null,
                isSaving = false
            )
        }
    }

    fun updateEditableTitle(title: String) {
        _uiState.update { it.copy(editableTitle = title, errorMessage = null) }
    }

    fun updateEditableAmount(amountDigits: String) {
        val digitsOnly = amountDigits.filter { it.isDigit() }.take(12)
        _uiState.update { it.copy(editableAmountText = digitsOnly, errorMessage = null) }
    }

    fun updateEditableCategory(category: SpendlyCategory) {
        _uiState.update { it.copy(editableCategory = category, errorMessage = null) }
    }

    fun updateEditablePayer(userId: String) {
        _uiState.update { it.copy(editablePayerId = userId, errorMessage = null) }
    }

    fun updateEditableDescription(description: String) {
        _uiState.update { it.copy(editableDescription = description, errorMessage = null) }
    }

    fun updateEditableDateMillis(dateMillis: Long) {
        _uiState.update { it.copy(editableDateMillis = dateMillis, errorMessage = null) }
    }

    fun selectedPayerName(fallbackName: String = "Thành viên"): String {
        val state = _uiState.value
        return state.members.firstOrNull { it.userId == state.editablePayerId }?.displayName
            ?: fallbackName
    }

    fun buildReviewedResult(): ScannedReceiptResult {
        val state = _uiState.value
        val base = state.scannedResult ?: ScannedReceiptResult(savedReceiptPath = state.capturedImagePath)
        return base.copy(
            isReceipt = true,
            title = state.editableTitle.trim().ifBlank { "Chi tiêu từ hóa đơn" },
            totalAmount = state.parsedAmount,
            category = state.editableCategory,
            expenseDateMillis = state.editableDateMillis,
            description = state.editableDescription.trim(),
            savedReceiptPath = state.capturedImagePath.ifBlank { base.savedReceiptPath }
        )
    }

    /**
     * Lưu thẳng khoản chi theo chế độ chia đều cho toàn bộ thành viên hoạt động của nhóm.
     */
    fun saveQuickEqualSplit(
        currentUserId: String,
        onSuccess: (String) -> Unit
    ) {
        val state = _uiState.value
        if (state.isSaving) return

        val groupId = state.groupId
        val activeMembers = state.members.filter { it.status == "ACTIVE" }
        val actorMember = activeMembers.firstOrNull { it.userId == currentUserId }
        if (actorMember != null && (actorMember.role == GroupRole.VIEWER || !actorMember.canAddExpense)) {
            _uiState.update {
                it.copy(errorMessage = "Tài khoản Người xem không có quyền thêm khoản chi vào nhóm này")
            }
            return
        }

        val title = state.editableTitle.trim()
        val amount = state.parsedAmount
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập tên khoản chi") }
            return
        }
        if (amount <= 0L) {
            _uiState.update { it.copy(errorMessage = "Số tiền hóa đơn phải lớn hơn 0 đ") }
            return
        }
        if (groupId.isBlank() || activeMembers.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Chưa có thành viên hoạt động trong nhóm để chia tiền") }
            return
        }

        val payer = activeMembers.firstOrNull { it.userId == state.editablePayerId }
            ?: actorMember
            ?: activeMembers.first()
        val creatorId = currentUserId.ifBlank { payer.userId }

        val splitValidation = SplitCalculationEngine.calculateAndValidateSplit(
            totalAmount = amount,
            mode = EngineSplitMode.EQUAL,
            participants = activeMembers.map { member ->
                ParticipantInput(
                    userId = member.userId,
                    displayName = member.displayName,
                    isIncluded = true
                )
            }
        )

        val participants = when (splitValidation) {
            is SplitValidationResult.Valid -> splitValidation.shares.map { share ->
                ExpenseParticipant(
                    userId = share.userId,
                    displayName = share.displayName,
                    shareAmount = share.shareAmount,
                    percentage = share.percentage
                )
            }
            is SplitValidationResult.Invalid -> {
                _uiState.update { it.copy(errorMessage = splitValidation.errorMessage) }
                return
            }
        }

        val now = System.currentTimeMillis()
        val expense = SpendlyExpense(
            groupId = groupId,
            title = title,
            amount = amount,
            category = state.editableCategory,
            payerId = payer.userId,
            payerName = payer.displayName,
            createdBy = creatorId,
            splitType = SplitMethod.EQUAL,
            description = state.editableDescription.trim().ifBlank { "Quét hóa đơn bằng FinSprout AI" },
            receiptUrl = state.capturedImagePath,
            expenseDateMillis = state.editableDateMillis,
            createdAtMillis = now,
            updatedAtMillis = now,
            status = RecordStatus.ACTIVE
        )

        _uiState.update { it.copy(isSaving = true, errorMessage = null) }
        viewModelScope.launch {
            val result = transactionRepository.createExpenseWithParticipants(
                groupId = groupId,
                expense = expense,
                participants = participants
            )
            result.onSuccess {
                _uiState.update { it.copy(isSaving = false) }
                onSuccess("Đã lưu \"$title\" (${FintechAlgorithm.formatVnd(amount)}) vào nhóm!")
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = error.message ?: "Không thể lưu khoản chi từ hóa đơn"
                    )
                }
            }
        }
    }

    private fun mapFriendlyError(throwable: Throwable): String {
        val rawMsg = throwable.message.orEmpty()
        return when {
            throwable is TimeoutCancellationException ->
                "Kết nối tới Gemini AI quá thời gian chờ. Vui lòng thử lại."
            throwable is IOException ->
                "Không có kết nối mạng để gửi ảnh lên FinSprout AI. Bạn vẫn có thể nhập thủ công kèm ảnh."
            rawMsg.contains("GEMINI_API_KEY", ignoreCase = true) ->
                "Chưa cấu hình GEMINI_API_KEY trong local.properties. Vui lòng nhập thủ công hoặc thêm key để dùng AI."
            else -> rawMsg.ifBlank { "AI chưa thể đọc rõ hóa đơn này. Bạn có thể chụp lại hoặc nhập thủ công." }
        }
    }

    companion object {
        fun factory(
            groupRepository: GroupRepository,
            transactionRepository: TransactionRepository,
            geminiChatRepository: GeminiChatRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ReceiptScannerViewModel::class.java)) {
                    return ReceiptScannerViewModel(
                        groupRepository = groupRepository,
                        transactionRepository = transactionRepository,
                        geminiChatRepository = geminiChatRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
