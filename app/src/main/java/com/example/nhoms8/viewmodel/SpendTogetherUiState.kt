package com.example.nhoms8.viewmodel

import com.example.nhoms8.DebtItem
import com.example.nhoms8.GroupMember
import com.example.nhoms8.HistoryTransactionItem
import com.example.nhoms8.RecentTransaction
import com.example.nhoms8.SplitMemberItem
import com.example.nhoms8.TransactionDateGroup
import com.example.nhoms8.model.BudgetState
import com.example.nhoms8.model.DraftExpense
import com.example.nhoms8.model.TransactionFilter
import com.example.nhoms8.model.UserProfile

/**
 * Trạng thái UI tổng hợp (SpendTogetherUiState)
 * Cung cấp luồng dữ liệu phản ứng tức thì (StateFlow) cho 15 màn hình Compose
 */
data class SpendTogetherUiState(
    val userProfile: UserProfile = UserProfile(),
    val members: List<GroupMember> = emptyList(),
    val selectedMember: GroupMember? = null,
    val recentTransactions: List<RecentTransaction> = emptyList(),
    val historyGroups: List<TransactionDateGroup> = emptyList(),
    val debts: List<DebtItem> = emptyList(),
    val budget: BudgetState = BudgetState(),
    val draftExpense: DraftExpense = DraftExpense(),
    val filter: TransactionFilter = TransactionFilter(),
    val autoReminderEnabled: Boolean = true,
    val splitMembers: List<SplitMemberItem> = emptyList(),
    val totalContributedAmount: String = "21.500.000 đ",
    val unpaidDebtsCount: Int = 0,
    val totalUnpaidDebtFormatted: String = "0 đ"
)
