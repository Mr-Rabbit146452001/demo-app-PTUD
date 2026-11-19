package com.example.nhoms8.domain.model

import com.example.nhoms8.ExpenseCategory

/**
 * 9 danh mục chi tiêu chuẩn của Spendly theo PRD.
 */
enum class SpendlyCategory(val label: String) {
    FOOD("Ăn uống"),
    TRANSPORT("Di chuyển"),
    HOUSING("Nhà ở"),
    SHOPPING("Mua sắm"),
    ENTERTAINMENT("Giải trí"),
    HEALTH("Y tế"),
    EDUCATION("Giáo dục"),
    UTILITIES("Sinh hoạt"),
    OTHER("Khác");

    fun toLegacyExpenseCategory(): ExpenseCategory = when (this) {
        FOOD -> ExpenseCategory.FOOD
        TRANSPORT -> ExpenseCategory.TRANSPORT
        HOUSING -> ExpenseCategory.HOUSING
        SHOPPING,
        ENTERTAINMENT,
        HEALTH,
        EDUCATION,
        UTILITIES,
        OTHER -> ExpenseCategory.OTHER
    }

    companion object {
        fun fromLegacy(category: ExpenseCategory): SpendlyCategory = when (category) {
            ExpenseCategory.FOOD -> FOOD
            ExpenseCategory.HOUSING -> HOUSING
            ExpenseCategory.TRANSPORT -> TRANSPORT
            ExpenseCategory.OTHER -> OTHER
        }

        fun fromRaw(raw: String?): SpendlyCategory =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: OTHER
    }
}

/**
 * Vai trò phân quyền thành viên trong nhóm/quỹ Spendly.
 */
enum class GroupRole(val label: String) {
    OWNER("Trưởng nhóm"),
    ADMIN("Quản trị viên"),
    MEMBER("Thành viên"),
    VIEWER("Người xem");

    fun canManageGroup(): Boolean = this == OWNER || this == ADMIN

    fun canEditAnyExpense(): Boolean = this == OWNER || this == ADMIN

    fun canCreateTransaction(): Boolean = this != VIEWER

    companion object {
        fun fromRaw(raw: String?): GroupRole =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: MEMBER
    }
}

/**
 * Phương thức chia tiền cho một khoản chi.
 */
enum class SplitMethod(val label: String) {
    EQUAL("Chia đều"),
    PERCENTAGE("Theo %"),
    CUSTOM("Tùy chỉnh");

    companion object {
        fun fromRaw(raw: String?): SplitMethod =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: EQUAL
    }
}

/**
 * Trạng thái bản ghi sổ cái (hỗ trợ soft-delete bằng CANCELLED thay vì xóa cứng).
 */
enum class RecordStatus {
    ACTIVE,
    CANCELLED;

    companion object {
        fun fromRaw(raw: String?): RecordStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: ACTIVE
    }
}

/**
 * Hồ sơ người dùng được lưu tại `users/{userId}`.
 */
data class SpendlyUser(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val avatarUrl: String = "",
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val isActive: Boolean = true
)

/**
 * Quỹ/Nhóm chi tiêu được lưu tại `groups/{groupId}`.
 */
data class SpendlyGroup(
    val groupId: String = "",
    val name: String = "",
    val description: String = "",
    val avatarUrl: String = "",
    val ownerId: String = "",
    val inviteCode: String = "",
    val memberCount: Int = 0,
    val currency: String = "VND",
    val monthlyBudgetLimit: Long = 10_000_000L,
    val createdAtMillis: Long = 0L,
    val status: String = "ACTIVE",
    val memberIds: List<String> = emptyList()
)

/**
 * Thành viên trong một nhóm tại `groups/{groupId}/members/{userId}`.
 */
data class SpendlyGroupMember(
    val userId: String = "",
    val displayName: String = "",
    val initials: String = "",
    val phone: String = "",
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAtMillis: Long = 0L,
    val status: String = "ACTIVE",
    val nickname: String = "",
    val canAddExpense: Boolean = true
)

/**
 * Khoản chi tại `groups/{groupId}/expenses/{expenseId}`.
 */
data class SpendlyExpense(
    val expenseId: String = "",
    val groupId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val category: SpendlyCategory = SpendlyCategory.OTHER,
    val payerId: String = "",
    val payerName: String = "",
    val createdBy: String = "",
    val splitType: SplitMethod = SplitMethod.EQUAL,
    val description: String = "",
    val receiptUrl: String = "",
    val expenseDateMillis: Long = 0L,
    val createdAtMillis: Long = 0L,
    val updatedAtMillis: Long = 0L,
    val status: RecordStatus = RecordStatus.ACTIVE
)

/**
 * Chi tiết phần chia của một thành viên tại `groups/{groupId}/expenses/{expenseId}/participants/{userId}`.
 */
data class ExpenseParticipant(
    val userId: String = "",
    val displayName: String = "",
    val shareAmount: Long = 0L,
    val percentage: Double = 0.0,
    val paidAmount: Long = 0L
)

/**
 * Khoản thu / nạp quỹ tại `groups/{groupId}/incomes/{incomeId}`.
 */
data class SpendlyIncome(
    val incomeId: String = "",
    val groupId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val payerId: String = "",
    val payerName: String = "",
    val category: SpendlyCategory = SpendlyCategory.OTHER,
    val description: String = "",
    val incomeDateMillis: Long = 0L,
    val createdBy: String = "",
    val createdAtMillis: Long = 0L,
    val status: RecordStatus = RecordStatus.ACTIVE,
    val isFundContribution: Boolean = true
)

/**
 * Số dư công nợ lũy kế của thành viên tại `groups/{groupId}/balances/{userId}`.
 */
data class MemberBalance(
    val userId: String = "",
    val totalPaid: Long = 0L,
    val totalShare: Long = 0L,
    val netBalance: Long = 0L,
    val updatedAtMillis: Long = 0L
)

/**
 * Nhật ký hoạt động nhóm tại `groups/{groupId}/activities/{activityId}`.
 */
data class GroupActivity(
    val activityId: String = "",
    val groupId: String = "",
    val actorId: String = "",
    val actorName: String = "",
    val action: String = "",
    val targetId: String = "",
    val description: String = "",
    val createdAtMillis: Long = 0L
)

/**
 * Thông báo người dùng tại `users/{userId}/notifications/{notificationId}`.
 */
data class SpendlyNotification(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val groupId: String = "",
    val isRead: Boolean = false,
    val createdAtMillis: Long = 0L
)

/**
 * Trạng thái quy trình quyết toán công nợ 2 bước (Đề xuất -> Xác nhận / Từ chối).
 */
enum class SettlementStatus(val label: String) {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    REJECTED("Từ chối");

    companion object {
        fun fromRaw(raw: String?): SettlementStatus =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: PENDING
    }
}

/**
 * Giao dịch đề xuất quyết toán công nợ tại `groups/{groupId}/settlements/{settlementId}`.
 */
data class SpendlySettlement(
    val settlementId: String = "",
    val groupId: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val toUserName: String = "",
    val amount: Long = 0L,
    val status: SettlementStatus = SettlementStatus.PENDING,
    val note: String = "",
    val bankInfo: String = "",
    val createdBy: String = "",
    val createdAtMillis: Long = 0L,
    val confirmedAtMillis: Long? = null,
    val confirmedBy: String? = null
)

/**
 * Mức độ sức khỏe ngân sách 4 cấp độ (Bình thường, Theo dõi, Cảnh báo, Vượt ngân sách).
 */
enum class BudgetHealthLevel(val label: String) {
    NORMAL("Bình thường"),
    WATCH("Theo dõi"),
    WARNING("Cảnh báo"),
    EXCEEDED("Vượt ngân sách");

    companion object {
        fun fromRaw(raw: String?): BudgetHealthLevel =
            entries.firstOrNull { it.name.equals(raw?.trim(), ignoreCase = true) } ?: NORMAL
    }
}

/**
 * Cấu hình ngân sách nhóm hoặc theo danh mục tại `groups/{groupId}/budgets/{budgetId}`.
 */
data class SpendlyBudget(
    val budgetId: String = "",
    val groupId: String = "",
    val name: String = "",
    val limitAmount: Long = 0L,
    val period: String = "MONTHLY",
    val startDateMillis: Long = 0L,
    val endDateMillis: Long = 0L,
    val category: SpendlyCategory? = null,
    val isActive: Boolean = true,
    val createdBy: String = "",
    val updatedAtMillis: Long = 0L
)

/**
 * Tổng hợp tiến độ sử dụng và trạng thái cảnh báo của một ngân sách.
 */
data class BudgetStatusSummary(
    val budget: SpendlyBudget,
    val spentAmount: Long,
    val remainingAmount: Long,
    val usagePercent: Float,
    val level: BudgetHealthLevel
)
