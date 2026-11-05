package com.example.nhoms8.model

import androidx.compose.ui.graphics.Color
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.GroupMember
import com.example.nhoms8.HistoryItemType
import com.example.nhoms8.HistoryTransactionItem
import com.example.nhoms8.RecentTransaction
import com.example.nhoms8.TransactionIconType
import com.example.nhoms8.algorithm.FintechAlgorithm

/**
 * Thông tin tài khoản người dùng đang đăng nhập
 */
data class UserProfile(
    val id: String = "user_me",
    val name: String = "Nguyễn Minh Anh",
    val initials: String = "MA",
    val phoneNumber: String = "0912 345 678",
    val groupName: String = "Quỹ Nhà Mình",
    val isLeader: Boolean = true,
    val totalBalanceFormatted: String = "12.680.000 đ"
)

/**
 * Trạng thái ngân sách và quỹ nhóm
 */
data class BudgetState(
    val totalBalance: Long = 12680000L,
    val depositedAmount: Long = 8500000L,
    val spentAmount: Long = 5820000L,
    val monthlyLimit: Long = 10000000L,
    val monthLabel: String = "Tháng 10"
) {
    val totalBalanceFormatted: String get() = FintechAlgorithm.formatVnd(totalBalance)
    val depositedAmountFormatted: String get() = FintechAlgorithm.formatVnd(depositedAmount)
    val spentAmountFormatted: String get() = FintechAlgorithm.formatVnd(spentAmount)
    val monthlyLimitFormatted: String get() = FintechAlgorithm.formatVnd(monthlyLimit)
}

/**
 * Giao dịch chi tiết trong hệ thống
 */
data class TransactionRecord(
    val id: String,
    val title: String,
    val subtitle: String,
    val amount: Long,
    val isIncome: Boolean,
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val payerId: String = "1",
    val payerName: String = "Minh Anh",
    val dateHeader: String = "Hôm nay, 24/10",
    val timestamp: Long = System.currentTimeMillis()
) {
    fun toRecentTransaction(): RecentTransaction {
        val sign = if (isIncome) "+" else "-"
        return RecentTransaction(
            id = id,
            title = title,
            subtitle = subtitle,
            amountFormatted = "$sign${FintechAlgorithm.formatVnd(amount)}",
            isIncome = isIncome,
            iconType = when (category) {
                ExpenseCategory.FOOD -> TransactionIconType.GROCERY
                ExpenseCategory.HOUSING -> TransactionIconType.INTERNET
                ExpenseCategory.TRANSPORT -> TransactionIconType.GROCERY
                ExpenseCategory.OTHER -> if (isIncome) TransactionIconType.DEPOSIT else TransactionIconType.GROCERY
            }
        )
    }

    fun toHistoryTransactionItem(): HistoryTransactionItem {
        val sign = if (isIncome) "+" else "-"
        return HistoryTransactionItem(
            id = id,
            title = title,
            subtitle = subtitle,
            amountFormatted = "$sign${FintechAlgorithm.formatVnd(amount)}",
            isIncome = isIncome,
            type = when (category) {
                ExpenseCategory.FOOD -> HistoryItemType.GROCERY
                ExpenseCategory.HOUSING -> HistoryItemType.INTERNET
                ExpenseCategory.TRANSPORT -> HistoryItemType.TAXI
                ExpenseCategory.OTHER -> if (isIncome) HistoryItemType.DEPOSIT else HistoryItemType.COFFEE
            }
        )
    }
}

/**
 * Dữ liệu khoản chi nháp đang chuẩn bị chia tiền
 */
data class DraftExpense(
    val title: String = "Đi chợ cuối tuần",
    val amount: Long = 850000L,
    val category: ExpenseCategory = ExpenseCategory.FOOD,
    val note: String = "Thực phẩm và đồ dùng gia đình"
)

/**
 * Trạng thái lọc lịch sử giao dịch
 */
data class TransactionFilter(
    val searchQuery: String = "",
    val selectedCategory: ExpenseCategory? = null,
    val selectedMemberId: String? = null,
    val selectedMonth: String = "Tháng 10"
)
