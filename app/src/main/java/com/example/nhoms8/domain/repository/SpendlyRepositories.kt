package com.example.nhoms8.domain.repository

import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupActivity
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SpendlyNotification
import com.example.nhoms8.domain.model.SpendlySettlement
import com.example.nhoms8.domain.model.SpendlyUser
import kotlinx.coroutines.flow.Flow

/**
 * Giao diện kho dữ liệu hồ sơ người dùng và thông báo cá nhân.
 */
interface UserRepository {
    suspend fun ensureUserProfile(
        uid: String,
        fullName: String,
        email: String,
        phone: String = ""
    ): Result<SpendlyUser>

    fun observeUserProfile(uid: String): Flow<SpendlyUser?>

    suspend fun updateUserProfile(
        uid: String,
        fullName: String? = null,
        phone: String? = null,
        avatarUrl: String? = null
    ): Result<Unit>

    fun observeNotifications(uid: String): Flow<List<SpendlyNotification>>

    suspend fun markNotificationRead(uid: String, notificationId: String): Result<Unit>
}

/**
 * Giao diện kho dữ liệu quỹ nhóm, thành viên, lời mời và nhật ký hoạt động.
 */
interface GroupRepository {
    fun observeUserGroups(uid: String): Flow<List<SpendlyGroup>>

    fun observeGroup(groupId: String): Flow<SpendlyGroup?>

    fun observeGroupMembers(groupId: String): Flow<List<SpendlyGroupMember>>

    fun observeGroupActivities(groupId: String): Flow<List<GroupActivity>>

    suspend fun createGroup(
        owner: SpendlyUser,
        name: String,
        description: String = "",
        monthlyBudgetLimit: Long = 10_000_000L
    ): Result<SpendlyGroup>

    suspend fun joinGroupByInviteCode(
        user: SpendlyUser,
        inviteCode: String
    ): Result<SpendlyGroup>

    suspend fun addManualMember(
        groupId: String,
        actorId: String,
        displayName: String,
        phone: String = "",
        role: GroupRole = GroupRole.MEMBER
    ): Result<SpendlyGroupMember>

    suspend fun updateMemberRole(
        groupId: String,
        targetUserId: String,
        role: GroupRole,
        canAddExpense: Boolean = role != GroupRole.VIEWER
    ): Result<Unit>

    suspend fun removeOrLeaveGroup(
        groupId: String,
        targetUserId: String
    ): Result<Unit>

    suspend fun updateGroupSettings(
        groupId: String,
        name: String,
        description: String,
        monthlyBudgetLimit: Long
    ): Result<Unit>

    suspend fun seedStarterGroupIfEmpty(user: SpendlyUser): Result<SpendlyGroup?>
}

/**
 * Giao diện kho dữ liệu sổ cái thu chi, phần chia thành viên và số dư công nợ.
 */
interface TransactionRepository {
    fun observeExpenses(groupId: String): Flow<List<SpendlyExpense>>

    fun observeExpenseDetail(
        groupId: String,
        expenseId: String
    ): Flow<Pair<SpendlyExpense, List<ExpenseParticipant>>?>

    fun observeIncomes(groupId: String): Flow<List<SpendlyIncome>>

    fun observeMemberBalances(groupId: String): Flow<List<MemberBalance>>

    suspend fun createExpenseWithParticipants(
        groupId: String,
        expense: SpendlyExpense,
        participants: List<ExpenseParticipant>
    ): Result<SpendlyExpense>

    suspend fun updateExpenseWithParticipants(
        groupId: String,
        expense: SpendlyExpense,
        participants: List<ExpenseParticipant>
    ): Result<Unit>

    suspend fun cancelExpense(
        groupId: String,
        expenseId: String,
        actorId: String,
        actorName: String
    ): Result<Unit>

    suspend fun createIncome(
        groupId: String,
        income: SpendlyIncome
    ): Result<SpendlyIncome>

    suspend fun cancelIncome(
        groupId: String,
        incomeId: String,
        actorId: String,
        actorName: String
    ): Result<Unit>
}

/**
 * Giao diện kho dữ liệu quy trình quyết toán công nợ 2 bước (Đề xuất -> Xác nhận / Từ chối).
 */
interface SettlementRepository {
    fun observeSettlements(groupId: String): Flow<List<SpendlySettlement>>

    suspend fun createSettlementRequest(
        groupId: String,
        settlement: SpendlySettlement
    ): Result<SpendlySettlement>

    suspend fun confirmSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String
    ): Result<Unit>

    suspend fun rejectSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String,
        reason: String = ""
    ): Result<Unit>
}

/**
 * Giao diện kho dữ liệu quản lý ngân sách nhóm và ngân sách theo danh mục.
 */
interface BudgetRepository {
    fun observeBudgets(groupId: String): Flow<List<SpendlyBudget>>

    suspend fun upsertBudget(
        groupId: String,
        budget: SpendlyBudget
    ): Result<SpendlyBudget>

    suspend fun deleteBudget(
        groupId: String,
        budgetId: String
    ): Result<Unit>
}
