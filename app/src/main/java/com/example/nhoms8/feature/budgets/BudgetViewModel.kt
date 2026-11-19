package com.example.nhoms8.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.domain.model.BudgetHealthLevel
import com.example.nhoms8.domain.model.BudgetStatusSummary
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.repository.BudgetRepository
import com.example.nhoms8.domain.repository.GroupRepository
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
 * Trạng thái giao diện cho màn hình Quản lý Ngân sách nhóm & danh mục (4 cấp độ cảnh báo).
 */
data class BudgetUiState(
    val groupId: String = "",
    val groupName: String = "",
    val monthlyBudgetLimit: Long = 0L,
    val overallSummary: BudgetStatusSummary = BudgetStatusSummary(
        budget = SpendlyBudget(name = "Ngân sách tổng nhóm", limitAmount = 0L, category = null),
        spentAmount = 0L,
        remainingAmount = 0L,
        usagePercent = 0f,
        level = BudgetHealthLevel.NORMAL
    ),
    val budgetSummaries: List<BudgetStatusSummary> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

private data class BudgetOpStatus(
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel quản lý ngân sách tổng nhóm và ngân sách theo từng danh mục,
 * tính toán tiến độ & cảnh báo 4 cấp độ (`NORMAL`, `WATCH`, `WARNING`, `EXCEEDED`).
 */
class BudgetViewModel(
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _operationStatus = MutableStateFlow(BudgetOpStatus())
    private val budgetFlows = ConcurrentHashMap<String, StateFlow<BudgetUiState>>()

    /**
     * Quan sát tổng hợp ngân sách nhóm và danh sách ngân sách cấu hình theo thời gian thực.
     */
    fun observeGroupBudgets(groupId: String): StateFlow<BudgetUiState> {
        if (groupId.isBlank()) {
            return MutableStateFlow(BudgetUiState(groupId = groupId, isLoading = false))
        }

        return budgetFlows.getOrPut(groupId) {
            combine(
                groupRepository.observeGroup(groupId).catch { emit(null) },
                transactionRepository.observeExpenses(groupId).catch { emit(emptyList()) },
                budgetRepository.observeBudgets(groupId).catch { emit(emptyList()) },
                _operationStatus
            ) { group, expenses, budgets, opStatus ->
                buildBudgetUiState(
                    groupId = groupId,
                    group = group,
                    expenses = expenses,
                    budgets = budgets,
                    opStatus = opStatus
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = BudgetUiState(groupId = groupId, isLoading = true)
            )
        }
    }

    /**
     * Tạo mới hoặc cập nhật một ngân sách nhóm (`category == null`) hoặc theo danh mục.
     */
    fun upsertBudget(
        groupId: String,
        budgetId: String? = null,
        name: String,
        limitAmount: Long,
        category: SpendlyCategory? = null,
        createdBy: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (groupId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Chưa xác định quỹ nhóm") }
            return
        }
        if (limitAmount <= 0L) {
            _operationStatus.update { it.copy(errorMessage = "Hạn mức ngân sách phải lớn hơn 0 đ") }
            return
        }

        val resolvedName = name.trim().ifBlank {
            category?.let { "Ngân sách ${it.label}" } ?: "Ngân sách tổng tháng"
        }

        val budget = SpendlyBudget(
            budgetId = budgetId.orEmpty().trim(),
            groupId = groupId,
            name = resolvedName,
            limitAmount = limitAmount,
            period = "MONTHLY",
            category = category,
            isActive = true,
            createdBy = createdBy.trim()
        )

        viewModelScope.launch {
            _operationStatus.value = BudgetOpStatus(isSaving = true)
            budgetRepository.upsertBudget(groupId = groupId, budget = budget)
                .onSuccess { saved ->
                    _operationStatus.value = BudgetOpStatus(
                        isSaving = false,
                        successMessage = "Đã lưu ngân sách \"${saved.name}\""
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _operationStatus.value = BudgetOpStatus(
                        isSaving = false,
                        errorMessage = error.message ?: "Không thể lưu cấu hình ngân sách"
                    )
                }
        }
    }

    /**
     * Xóa một cấu hình ngân sách khỏi nhóm.
     */
    fun deleteBudget(
        groupId: String,
        budgetId: String,
        onSuccess: () -> Unit = {}
    ) {
        if (groupId.isBlank() || budgetId.isBlank()) {
            _operationStatus.update { it.copy(errorMessage = "Thiếu mã nhóm hoặc mã ngân sách") }
            return
        }

        viewModelScope.launch {
            _operationStatus.value = BudgetOpStatus(isSaving = true)
            budgetRepository.deleteBudget(groupId = groupId, budgetId = budgetId)
                .onSuccess {
                    _operationStatus.value = BudgetOpStatus(
                        isSaving = false,
                        successMessage = "Đã xóa ngân sách"
                    )
                    onSuccess()
                }
                .onFailure { error ->
                    _operationStatus.value = BudgetOpStatus(
                        isSaving = false,
                        errorMessage = error.message ?: "Không thể xóa ngân sách"
                    )
                }
        }
    }

    fun clearMessages() {
        _operationStatus.update { it.copy(errorMessage = null, successMessage = null) }
    }

    private fun buildBudgetUiState(
        groupId: String,
        group: SpendlyGroup?,
        expenses: List<SpendlyExpense>,
        budgets: List<SpendlyBudget>,
        opStatus: BudgetOpStatus
    ): BudgetUiState {
        val activeExpenses = expenses.filter { it.status == RecordStatus.ACTIVE }
        val activeBudgets = budgets.filter { it.isActive }
        val configuredOverallBudget = activeBudgets.firstOrNull { it.category == null }
        val monthlyLimit = configuredOverallBudget?.limitAmount ?: (group?.monthlyBudgetLimit ?: 0L)

        val effectiveOverallBudget = configuredOverallBudget ?: SpendlyBudget(
            budgetId = "",
            groupId = groupId,
            name = "Ngân sách tổng tháng",
            limitAmount = monthlyLimit,
            period = "MONTHLY",
            category = null,
            isActive = true,
            createdBy = group?.ownerId.orEmpty()
        )

        val overallSummary = SplitCalculationEngine.computeBudgetSummary(
            budget = effectiveOverallBudget,
            activeExpenses = activeExpenses
        )

        val budgetSummaries = activeBudgets
            .map { budget ->
                SplitCalculationEngine.computeBudgetSummary(
                    budget = budget,
                    activeExpenses = activeExpenses
                )
            }
            .sortedWith(
                compareBy<BudgetStatusSummary> { it.budget.category != null }
                    .thenByDescending { it.usagePercent }
                    .thenByDescending { it.budget.updatedAtMillis }
            )

        return BudgetUiState(
            groupId = groupId,
            groupName = group?.name.orEmpty(),
            monthlyBudgetLimit = monthlyLimit,
            overallSummary = overallSummary,
            budgetSummaries = budgetSummaries,
            isLoading = false,
            isSaving = opStatus.isSaving,
            errorMessage = opStatus.errorMessage,
            successMessage = opStatus.successMessage
        )
    }

    companion object {
        fun factory(
            groupRepository: GroupRepository,
            transactionRepository: TransactionRepository,
            budgetRepository: BudgetRepository
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                    return BudgetViewModel(
                        groupRepository = groupRepository,
                        transactionRepository = transactionRepository,
                        budgetRepository = budgetRepository
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
