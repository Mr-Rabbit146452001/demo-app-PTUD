package com.example.nhoms8.feature.settlements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.SettlementStatus
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlySettlement
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.SettlementRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Một khoản gợi ý chuyển khoản tối ưu đã được gắn tên và chữ viết tắt thành viên.
 */
data class SuggestedTransferItem(
    val fromUserId: String,
    val fromUserName: String,
    val fromInitials: String,
    val toUserId: String,
    val toUserName: String,
    val toInitials: String,
    val amount: Long
)

/**
 * Trạng thái giao diện cho màn hình Đối soát & Thanh toán công nợ 2 bước.
 */
data class SettlementUiState(
    val groupId: String = "",
    val groupName: String = "",
    val currentUserId: String = "",
    val currentUserRole: GroupRole = GroupRole.MEMBER,
    val members: List<SpendlyGroupMember> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val myBalance: Long = 0L,
    val iOweTotal: Long = 0L,
    val owedToMeTotal: Long = 0L,
    val suggestedTransfers: List<SuggestedTransferItem> = emptyList(),
    val pendingSettlements: List<SpendlySettlement> = emptyList(),
    val historySettlements: List<SpendlySettlement> = emptyList(),
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val canManageSettlements: Boolean
        get() = currentUserRole.canManageGroup()
}

private data class SettlementGroupMeta(
    val group: SpendlyGroup?,
    val members: List<SpendlyGroupMember>,
    val expenses: List<SpendlyExpense>
)

private data class SettlementLedgerMeta(
    val balances: List<MemberBalance>,
    val settlements: List<SpendlySettlement>,
    val opStatus: SettlementOpStatus
)

private data class SettlementOpStatus(
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel điều phối quy trình quyết toán công nợ 2 bước:
 * Gợi ý bù trừ tối ưu -> Đề xuất chuyển khoản (PENDING) -> Người nhận xác nhận / từ chối.
 */
class SettlementViewModel(
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val settlementRepository: SettlementRepository
) : ViewModel() {

    private val _operationStatus = MutableStateFlow(SettlementOpStatus())
    private val settlementFlows = ConcurrentHashMap<String, StateFlow<SettlementUiState>>()

    /**
     * Quan sát trạng thái công nợ, gợi ý chuyển khoản tối ưu, yêu cầu chờ duyệt và lịch sử thanh toán.
     */
    fun observeGroupSettlement(
        groupId: String,
        currentUserId: String
    ): StateFlow<SettlementUiState> {
        if (groupId.isBlank()) {
            return MutableStateFlow(
                SettlementUiState(
                    groupId = groupId,
                    currentUserId = currentUserId,
                    isLoading = false
                )
            )
        }

        val cacheKey = "$groupId::$currentUserId"
        return settlementFlows.getOrPut(cacheKey) {
            val groupMetaFlow = combine(
                groupRepository.observeGroup(groupId).catch { emit(null) },
                groupRepository.observeGroupMembers(groupId).catch { emit(emptyList()) },
                transactionRepository.observeExpenses(groupId).catch { emit(emptyList()) }
            ) { group, members, expenses ->
                SettlementGroupMeta(group = group, members = members, expenses = expenses)
            }

            val ledgerMetaFlow = combine(
                transactionRepository.observeMemberBalances(groupId).catch { emit(emptyList()) },
                settlementRepository.observeSettlements(groupId).catch { emit(emptyList()) },
                _operationStatus
            ) { balances, settlements, opStatus ->
                SettlementLedgerMeta(
                    balances = balances,
                    settlements = settlements,
                    opStatus = opStatus
                )
            }

            combine(groupMetaFlow, ledgerMetaFlow) { groupMeta, ledgerMeta ->
                buildSettlementUiState(
                    groupId = groupId,
                    currentUserId = currentUserId,
                    groupMeta = groupMeta,
                    ledgerMeta = ledgerMeta
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = SettlementUiState(
                    groupId = groupId,
                    currentUserId = currentUserId,
                    isLoading = true
                )
            )
        }
    }

    /**
     * Gửi yêu cầu xác nhận đã chuyển khoản (tạo bản ghi PENDING, chưa thay đổi số dư).
     */
    fun requestSettlement(
        groupId: String,
        fromUserId: String,
        fromUserName: String,
        toUserId: String,
        toUserName: String,
        amount: Long,
        note: String = "",
        bankInfo: String = "",
        createdBy: String = fromUserId,
        onSuccess: () -> Unit = {}
    ) {
        if (groupId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Chưa xác định quỹ nhóm") }
            return
        }
        if (fromUserId.isBlank() || toUserId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Thiếu thông tin người trả hoặc người nhận") }
            return
        }
        if (fromUserId == toUserId) {
            _operationStatus.update { it.copy(errorMessage = "Người trả và người nhận không thể trùng nhau") }
            return
        }
        if (amount <= 0L) {
            _operationStatus.update { it.copy(errorMessage = "Số tiền thanh toán phải lớn hơn 0 đ") }
            return
        }

        val settlement = SpendlySettlement(
            groupId = groupId,
            fromUserId = fromUserId,
            fromUserName = fromUserName.trim(),
            toUserId = toUserId,
            toUserName = toUserName.trim(),
            amount = amount,
            status = SettlementStatus.PENDING,
            note = note.trim(),
            bankInfo = bankInfo.trim(),
            createdBy = createdBy.ifBlank { fromUserId }
        )

        viewModelScope.launch {
            _operationStatus.value = SettlementOpStatus(isProcessing = true)
            settlementRepository.createSettlementRequest(groupId = groupId, settlement = settlement)
                .onSuccess {
                    _operationStatus.value = SettlementOpStatus(
                        isProcessing = false,
                        successMessage = "Đã gửi yêu cầu xác nhận thanh toán ${FintechAlgorithm.formatVnd(amount)}"
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _operationStatus.value = SettlementOpStatus(
                        isProcessing = false,
                        errorMessage = error.message ?: "Không thể gửi yêu cầu thanh toán"
                    )
                }
        }
    }

    /**
     * Người nhận (hoặc quản trị viên) xác nhận đã nhận tiền thanh toán -> cập nhật số dư nhóm.
     */
    fun confirmSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String,
        onSuccess: () -> Unit = {}
    ) {
        if (groupId.isBlank() || settlementId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Thiếu mã nhóm hoặc mã thanh toán") }
            return
        }
        if (actorId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Không xác định được người xác nhận") }
            return
        }

        viewModelScope.launch {
            _operationStatus.value = SettlementOpStatus(isProcessing = true)
            settlementRepository.confirmSettlement(
                groupId = groupId,
                settlementId = settlementId,
                actorId = actorId,
                actorName = actorName
            ).onSuccess {
                _operationStatus.value = SettlementOpStatus(
                    isProcessing = false,
                    successMessage = "Đã xác nhận thanh toán và cập nhật công nợ"
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = SettlementOpStatus(
                    isProcessing = false,
                    errorMessage = error.message ?: "Không thể xác nhận thanh toán"
                )
            }
        }
    }

    /**
     * Người nhận (hoặc quản trị viên) từ chối yêu cầu thanh toán -> giữ nguyên công nợ.
     */
    fun rejectSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String,
        reason: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (groupId.isBlank() || settlementId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Thiếu mã nhóm hoặc mã thanh toán") }
            return
        }
        if (actorId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Không xác định được người từ chối") }
            return
        }

        viewModelScope.launch {
            _operationStatus.value = SettlementOpStatus(isProcessing = true)
            settlementRepository.rejectSettlement(
                groupId = groupId,
                settlementId = settlementId,
                actorId = actorId,
                actorName = actorName,
                reason = reason
            ).onSuccess {
                _operationStatus.value = SettlementOpStatus(
                    isProcessing = false,
                    successMessage = "Đã từ chối yêu cầu thanh toán"
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = SettlementOpStatus(
                    isProcessing = false,
                    errorMessage = error.message ?: "Không thể từ chối yêu cầu thanh toán"
                )
            }
        }
    }

    fun clearMessages() {
        _operationStatus.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun buildSettlementUiState(
        groupId: String,
        currentUserId: String,
        groupMeta: SettlementGroupMeta,
        ledgerMeta: SettlementLedgerMeta
    ): SettlementUiState {
        val group = groupMeta.group
        val members = groupMeta.members
        val expenses = groupMeta.expenses
        val balances = ledgerMeta.balances
        val settlements = ledgerMeta.settlements

        val namesById = linkedMapOf<String, String>()
        val initialsById = linkedMapOf<String, String>()

        expenses.forEach { expense ->
            if (expense.payerId.isNotBlank() && expense.payerName.isNotBlank()) {
                namesById[expense.payerId] = expense.payerName
            }
        }
        settlements.forEach { stl ->
            if (stl.fromUserId.isNotBlank() && stl.fromUserName.isNotBlank()) {
                namesById[stl.fromUserId] = stl.fromUserName
            }
            if (stl.toUserId.isNotBlank() && stl.toUserName.isNotBlank()) {
                namesById[stl.toUserId] = stl.toUserName
            }
        }
        members.forEach { member ->
            if (member.userId.isNotBlank()) {
                if (member.displayName.isNotBlank()) {
                    namesById[member.userId] = member.displayName
                }
                if (member.initials.isNotBlank()) {
                    initialsById[member.userId] = member.initials.take(2).uppercase()
                }
            }
        }

        val netBalancesMap = balances.associate { it.userId to it.netBalance }
        val rawTransfers = SplitCalculationEngine.computeGreedySettlements(netBalancesMap)
        val suggestedTransfers = rawTransfers.map { transfer ->
            val fromName = namesById[transfer.fromUserId]?.takeIf { it.isNotBlank() } ?: transfer.fromUserId
            val toName = namesById[transfer.toUserId]?.takeIf { it.isNotBlank() } ?: transfer.toUserId
            SuggestedTransferItem(
                fromUserId = transfer.fromUserId,
                fromUserName = fromName,
                fromInitials = initialsById[transfer.fromUserId] ?: computeInitials(fromName),
                toUserId = transfer.toUserId,
                toUserName = toName,
                toInitials = initialsById[transfer.toUserId] ?: computeInitials(toName),
                amount = transfer.amount
            )
        }

        val myBalance = if (currentUserId.isNotBlank()) {
            balances.firstOrNull { it.userId == currentUserId }?.netBalance ?: 0L
        } else {
            0L
        }
        val iOweTotal = if (myBalance < 0L) -myBalance else 0L
        val owedToMeTotal = if (myBalance > 0L) myBalance else 0L

        val currentUserRole = when {
            currentUserId.isBlank() -> GroupRole.MEMBER
            group != null && group.ownerId == currentUserId -> GroupRole.OWNER
            else -> members.firstOrNull { it.userId == currentUserId }?.role ?: GroupRole.MEMBER
        }

        val pendingSettlements = settlements
            .filter { it.status == SettlementStatus.PENDING }
            .sortedByDescending { it.createdAtMillis }

        val historySettlements = settlements
            .filter { it.status == SettlementStatus.CONFIRMED || it.status == SettlementStatus.REJECTED }
            .sortedByDescending { it.confirmedAtMillis ?: it.createdAtMillis }

        return SettlementUiState(
            groupId = groupId,
            groupName = group?.name.orEmpty(),
            currentUserId = currentUserId,
            currentUserRole = currentUserRole,
            members = members,
            balances = balances,
            myBalance = myBalance,
            iOweTotal = iOweTotal,
            owedToMeTotal = owedToMeTotal,
            suggestedTransfers = suggestedTransfers,
            pendingSettlements = pendingSettlements,
            historySettlements = historySettlements,
            isLoading = false,
            isProcessing = ledgerMeta.opStatus.isProcessing,
            errorMessage = ledgerMeta.opStatus.errorMessage,
            successMessage = ledgerMeta.opStatus.successMessage
        )
    }

    private fun computeInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return parts.mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()
            .ifBlank { "TV" }
    }

    companion object {
        fun factory(
            groupRepository: GroupRepository,
            transactionRepository: TransactionRepository,
            settlementRepository: SettlementRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(SettlementViewModel::class.java)) {
                    return SettlementViewModel(
                        groupRepository = groupRepository,
                        transactionRepository = transactionRepository,
                        settlementRepository = settlementRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
