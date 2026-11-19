package com.example.nhoms8

import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.model.SplitMethod
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SpendlyRepositoryTest {

    private fun newRepo(startMillis: Long = 1_700_000_000_000L): FirestoreSpendlyRepository {
        var tick = startMillis
        return FirestoreSpendlyRepository(
            firestore = null,
            clock = {
                tick += 100L
                tick
            }
        )
    }

    private val ownerUser = SpendlyUser(
        uid = "uid_hung",
        fullName = "Lê Văn Hùng",
        email = "hung@spendly.vn",
        phone = "0912345678"
    )

    private val memberUser = SpendlyUser(
        uid = "uid_linh",
        fullName = "Lý Việt Linh",
        email = "linh@spendly.vn",
        phone = "0908224510"
    )

    private val thirdUser = SpendlyUser(
        uid = "uid_dat",
        fullName = "Nguyễn Phan Tấn Đạt",
        email = "dat@spendly.vn",
        phone = "0986420115"
    )

    @Test
    fun categoryAndRoleHelpers_matchPrdSpecification() {
        assertEquals(9, SpendlyCategory.entries.size)
        assertEquals("Ăn uống", SpendlyCategory.FOOD.label)
        assertEquals("Di chuyển", SpendlyCategory.TRANSPORT.label)
        assertEquals("Nhà ở", SpendlyCategory.HOUSING.label)
        assertEquals("Mua sắm", SpendlyCategory.SHOPPING.label)
        assertEquals("Giải trí", SpendlyCategory.ENTERTAINMENT.label)
        assertEquals("Y tế", SpendlyCategory.HEALTH.label)
        assertEquals("Giáo dục", SpendlyCategory.EDUCATION.label)
        assertEquals("Sinh hoạt", SpendlyCategory.UTILITIES.label)
        assertEquals("Khác", SpendlyCategory.OTHER.label)

        assertEquals(ExpenseCategory.FOOD, SpendlyCategory.FOOD.toLegacyExpenseCategory())
        assertEquals(ExpenseCategory.TRANSPORT, SpendlyCategory.TRANSPORT.toLegacyExpenseCategory())
        assertEquals(ExpenseCategory.HOUSING, SpendlyCategory.HOUSING.toLegacyExpenseCategory())
        assertEquals(ExpenseCategory.OTHER, SpendlyCategory.SHOPPING.toLegacyExpenseCategory())
        assertEquals(SpendlyCategory.FOOD, SpendlyCategory.fromLegacy(ExpenseCategory.FOOD))

        assertTrue(GroupRole.OWNER.canManageGroup())
        assertTrue(GroupRole.OWNER.canEditAnyExpense())
        assertTrue(GroupRole.ADMIN.canManageGroup())
        assertTrue(GroupRole.ADMIN.canEditAnyExpense())
        assertFalse(GroupRole.MEMBER.canManageGroup())
        assertFalse(GroupRole.MEMBER.canEditAnyExpense())
        assertFalse(GroupRole.VIEWER.canManageGroup())
        assertFalse(GroupRole.VIEWER.canEditAnyExpense())
    }

    @Test
    fun createMultipleGroups_generateInviteCodes_andJoinViaInviteCode() = runTest {
        val repo = newRepo()

        val g1Result = repo.createGroup(
            owner = ownerUser,
            name = "Quỹ Nhóm 08",
            description = "Chi tiêu đồ án",
            monthlyBudgetLimit = 10_000_000L
        )
        val g2Result = repo.createGroup(
            owner = ownerUser,
            name = "Du lịch Đà Lạt",
            description = "Chuyến đi cuối kỳ",
            monthlyBudgetLimit = 5_000_000L
        )

        assertTrue(g1Result.isSuccess)
        assertTrue(g2Result.isSuccess)
        val g1 = g1Result.getOrThrow()
        val g2 = g2Result.getOrThrow()

        assertTrue(g1.inviteCode.isNotBlank())
        assertTrue(g2.inviteCode.isNotBlank())
        assertNotEquals(g1.inviteCode, g2.inviteCode)

        val ownerGroups = repo.observeUserGroups(ownerUser.uid).first()
        assertEquals(2, ownerGroups.size)

        // Thành viên khác tham gia bằng mã mời (không phân biệt hoa thường/khoảng trắng)
        val joinResult = repo.joinGroupByInviteCode(
            user = memberUser,
            inviteCode = " ${g1.inviteCode.lowercase()} "
        )
        assertTrue(joinResult.isSuccess)
        val joinedGroup = joinResult.getOrThrow()
        assertEquals(2, joinedGroup.memberCount)
        assertTrue(memberUser.uid in joinedGroup.memberIds)

        // Tham gia lại cùng nhóm phải idempotent, không tăng memberCount lần nữa
        val duplicateJoin = repo.joinGroupByInviteCode(memberUser, g1.inviteCode)
        assertTrue(duplicateJoin.isSuccess)
        assertEquals(2, repo.observeGroup(g1.groupId).first()?.memberCount)

        // Mã mời sai trả về failure
        val badJoin = repo.joinGroupByInviteCode(memberUser, "INVALID99")
        assertTrue(badJoin.isFailure)

        // Trưởng nhóm nhận được thông báo tham gia và đánh dấu đã đọc được
        val ownerNotifs = repo.observeNotifications(ownerUser.uid).first()
        assertEquals(1, ownerNotifs.size)
        assertFalse(ownerNotifs.first().isRead)
        assertTrue(repo.markNotificationRead(ownerUser.uid, ownerNotifs.first().id).isSuccess)
        assertTrue(repo.observeNotifications(ownerUser.uid).first().first().isRead)
    }

    @Test
    fun rolePermissions_updateMemberRole_andRemoveOrLeaveGroup() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerUser, "Quỹ Phòng Trọ").getOrThrow()
        repo.joinGroupByInviteCode(memberUser, group.inviteCode).getOrThrow()

        // MEMBER không thể tự ý thêm thành viên thủ công
        val forbiddenAdd = repo.addManualMember(
            groupId = group.groupId,
            actorId = memberUser.uid,
            displayName = "Khách ngoài",
            phone = "0999888777",
            role = GroupRole.MEMBER
        )
        assertTrue(forbiddenAdd.isFailure)

        // OWNER thêm thành viên thủ công thành công
        val manualAdded = repo.addManualMember(
            groupId = group.groupId,
            actorId = ownerUser.uid,
            displayName = "Lê Trung Bình",
            phone = "0934881902",
            role = GroupRole.MEMBER
        )
        assertTrue(manualAdded.isSuccess)
        val manualMember = manualAdded.getOrThrow()
        assertEquals("TB", manualMember.initials)
        assertEquals(3, repo.observeGroupMembers(group.groupId).first().size)

        // Chuyển memberUser thành VIEWER -> canAddExpense tự động về false và bị chặn tạo chi tiêu
        assertTrue(
            repo.updateMemberRole(
                groupId = group.groupId,
                targetUserId = memberUser.uid,
                role = GroupRole.VIEWER,
                canAddExpense = true
            ).isSuccess
        )
        val updatedViewer = repo.observeGroupMembers(group.groupId).first()
            .first { it.userId == memberUser.uid }
        assertEquals(GroupRole.VIEWER, updatedViewer.role)
        assertFalse(updatedViewer.canAddExpense)

        val viewerExpenseAttempt = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Mua nước",
                amount = 50_000L,
                category = SpendlyCategory.FOOD,
                payerId = ownerUser.uid,
                createdBy = memberUser.uid,
                splitType = SplitMethod.EQUAL
            ),
            participants = listOf(
                ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 50_000L, 100.0)
            )
        )
        assertTrue(viewerExpenseAttempt.isFailure)

        // Nâng memberUser lên ADMIN -> có quyền quản trị nhóm
        assertTrue(
            repo.updateMemberRole(
                groupId = group.groupId,
                targetUserId = memberUser.uid,
                role = GroupRole.ADMIN,
                canAddExpense = true
            ).isSuccess
        )
        val promotedAdmin = repo.observeGroupMembers(group.groupId).first()
            .first { it.userId == memberUser.uid }
        assertTrue(promotedAdmin.role.canManageGroup())

        // Không thể hạ quyền OWNER hiện tại
        assertTrue(
            repo.updateMemberRole(
                groupId = group.groupId,
                targetUserId = ownerUser.uid,
                role = GroupRole.MEMBER,
                canAddExpense = true
            ).isFailure
        )

        // OWNER không thể rời nhóm khi vẫn còn thành viên khác
        assertTrue(repo.removeOrLeaveGroup(group.groupId, ownerUser.uid).isFailure)

        // Xóa thành viên thủ công -> memberCount giảm và không còn trong danh sách active
        assertTrue(repo.removeOrLeaveGroup(group.groupId, manualMember.userId).isSuccess)
        val remainingMembers = repo.observeGroupMembers(group.groupId).first()
        assertEquals(2, remainingMembers.size)
        assertFalse(remainingMembers.any { it.userId == manualMember.userId })
        assertEquals(2, repo.observeGroup(group.groupId).first()?.memberCount)
    }

    @Test
    fun createExpense_includingPayerNotInParticipants_updatesBalancesAccurately() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerUser, "Quỹ Ăn Trưa").getOrThrow()
        repo.joinGroupByInviteCode(memberUser, group.inviteCode).getOrThrow()
        repo.joinGroupByInviteCode(thirdUser, group.inviteCode).getOrThrow()

        // Case 1: Hùng trả 300.000 đ, chia đều cho cả 3 (Hùng, Linh, Đạt)
        val exp1 = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Cơm trưa văn phòng",
                amount = 300_000L,
                category = SpendlyCategory.FOOD,
                payerId = ownerUser.uid,
                payerName = ownerUser.fullName,
                createdBy = ownerUser.uid,
                splitType = SplitMethod.EQUAL
            ),
            participants = listOf(
                ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 100_000L, 33.33),
                ExpenseParticipant(memberUser.uid, memberUser.fullName, 100_000L, 33.33),
                ExpenseParticipant(thirdUser.uid, thirdUser.fullName, 100_000L, 33.34)
            )
        ).getOrThrow()

        // Case 2: Hùng trả hộ 600.000 đ CHỈ cho Linh và Đạt (Hùng KHÔNG nằm trong participants)
        val exp2 = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Vé xem phim hộ Linh & Đạt",
                amount = 600_000L,
                category = SpendlyCategory.ENTERTAINMENT,
                payerId = ownerUser.uid,
                payerName = ownerUser.fullName,
                createdBy = ownerUser.uid,
                splitType = SplitMethod.EQUAL
            ),
            participants = listOf(
                ExpenseParticipant(memberUser.uid, memberUser.fullName, 300_000L, 50.0),
                ExpenseParticipant(thirdUser.uid, thirdUser.fullName, 300_000L, 50.0)
            )
        ).getOrThrow()

        val balances = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        val hungBal = balances.getValue(ownerUser.uid)
        val linhBal = balances.getValue(memberUser.uid)
        val datBal = balances.getValue(thirdUser.uid)

        // Hùng: trả 300k + 600k = 900k; phần chịu = 100k + 0 = 100k -> net +800k
        assertEquals(900_000L, hungBal.totalPaid)
        assertEquals(100_000L, hungBal.totalShare)
        assertEquals(800_000L, hungBal.netBalance)

        // Linh & Đạt: trả 0; phần chịu = 100k + 300k = 400k -> net -400k
        assertEquals(0L, linhBal.totalPaid)
        assertEquals(400_000L, linhBal.totalShare)
        assertEquals(-400_000L, linhBal.netBalance)

        assertEquals(0L, datBal.totalPaid)
        assertEquals(400_000L, datBal.totalShare)
        assertEquals(-400_000L, datBal.netBalance)

        // Tổng cân bằng sổ cái bằng 0
        assertEquals(0L, balances.values.sumOf { it.netBalance })

        val detail = repo.observeExpenseDetail(group.groupId, exp2.expenseId).first()
        assertNotNull(detail)
        assertEquals(exp2.expenseId, detail!!.first.expenseId)
        assertEquals(2, detail.second.size)
        assertTrue(exp1.expenseId != exp2.expenseId)
    }

    @Test
    fun updateAndSoftCancelExpense_recalculatesBalancesAndRecordsActivities() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerUser, "Quỹ Sinh Hoạt").getOrThrow()
        repo.joinGroupByInviteCode(memberUser, group.inviteCode).getOrThrow()
        repo.joinGroupByInviteCode(thirdUser, group.inviteCode).getOrThrow()

        // 1. Tạo khoản chi 300k cho cả 3
        val created = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Tiền điện tháng 11",
                amount = 300_000L,
                category = SpendlyCategory.UTILITIES,
                payerId = ownerUser.uid,
                payerName = ownerUser.fullName,
                createdBy = ownerUser.uid,
                splitType = SplitMethod.EQUAL
            ),
            participants = listOf(
                ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 100_000L, 33.34),
                ExpenseParticipant(memberUser.uid, memberUser.fullName, 100_000L, 33.33),
                ExpenseParticipant(thirdUser.uid, thirdUser.fullName, 100_000L, 33.33)
            )
        ).getOrThrow()

        // 2. Cập nhật khoản chi thành 400k chỉ chia cho Hùng và Linh (loại Đạt)
        val updateResult = repo.updateExpenseWithParticipants(
            groupId = group.groupId,
            expense = created.copy(
                title = "Tiền điện nước tháng 11",
                amount = 400_000L,
                splitType = SplitMethod.CUSTOM
            ),
            participants = listOf(
                ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 150_000L, 37.5),
                ExpenseParticipant(memberUser.uid, memberUser.fullName, 250_000L, 62.5)
            )
        )
        assertTrue(updateResult.isSuccess)

        val balancesAfterUpdate = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(400_000L, balancesAfterUpdate.getValue(ownerUser.uid).totalPaid)
        assertEquals(150_000L, balancesAfterUpdate.getValue(ownerUser.uid).totalShare)
        assertEquals(250_000L, balancesAfterUpdate.getValue(ownerUser.uid).netBalance)
        assertEquals(-250_000L, balancesAfterUpdate.getValue(memberUser.uid).netBalance)
        assertEquals(0L, balancesAfterUpdate.getValue(thirdUser.uid).totalShare)
        assertEquals(0L, balancesAfterUpdate.getValue(thirdUser.uid).netBalance)

        val detailAfterUpdate = repo.observeExpenseDetail(group.groupId, created.expenseId).first()!!
        assertEquals(2, detailAfterUpdate.second.size)

        // 3. Soft-delete (CANCELLED) khoản chi -> sổ cái về 0 nhưng bản ghi vẫn tồn tại với trạng thái CANCELLED
        val cancelResult = repo.cancelExpense(
            groupId = group.groupId,
            expenseId = created.expenseId,
            actorId = ownerUser.uid,
            actorName = ownerUser.fullName
        )
        assertTrue(cancelResult.isSuccess)

        val expenses = repo.observeExpenses(group.groupId).first()
        assertEquals(1, expenses.size)
        assertEquals(RecordStatus.CANCELLED, expenses.first().status)

        val balancesAfterCancel = repo.observeMemberBalances(group.groupId).first()
        assertTrue(balancesAfterCancel.all { it.totalPaid == 0L && it.totalShare == 0L && it.netBalance == 0L })

        // Không thể sửa khoản chi đã CANCELLED
        assertTrue(
            repo.updateExpenseWithParticipants(
                groupId = group.groupId,
                expense = created.copy(amount = 100_000L),
                participants = listOf(ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 100_000L, 100.0))
            ).isFailure
        )

        val actions = repo.observeGroupActivities(group.groupId).first().map { it.action }
        assertTrue(actions.contains("CREATE_EXPENSE"))
        assertTrue(actions.contains("UPDATE_EXPENSE"))
        assertTrue(actions.contains("CANCEL_EXPENSE"))
        assertEquals(
            listOf("CANCEL_EXPENSE", "UPDATE_EXPENSE", "CREATE_EXPENSE"),
            actions.filter { it.endsWith("_EXPENSE") }
        )
    }

    @Test
    fun createAndCancelIncome_recordsActivityLog() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerUser, "Quỹ Lớp").getOrThrow()

        val incomeResult = repo.createIncome(
            groupId = group.groupId,
            income = SpendlyIncome(
                title = "Đóng quỹ đợt 1",
                amount = 2_000_000L,
                payerId = ownerUser.uid,
                payerName = ownerUser.fullName,
                category = SpendlyCategory.OTHER,
                description = "Đóng quỹ đầu tháng",
                createdBy = ownerUser.uid,
                isFundContribution = true
            )
        )
        assertTrue(incomeResult.isSuccess)
        val savedIncome = incomeResult.getOrThrow()
        assertEquals(RecordStatus.ACTIVE, savedIncome.status)
        assertTrue(savedIncome.isFundContribution)

        val incomes = repo.observeIncomes(group.groupId).first()
        assertEquals(1, incomes.size)
        assertEquals(2_000_000L, incomes.first().amount)

        val activitiesAfterCreate = repo.observeGroupActivities(group.groupId).first()
        assertEquals("CREATE_INCOME", activitiesAfterCreate.first().action)
        assertEquals(savedIncome.incomeId, activitiesAfterCreate.first().targetId)

        assertTrue(
            repo.cancelIncome(
                groupId = group.groupId,
                incomeId = savedIncome.incomeId,
                actorId = ownerUser.uid,
                actorName = ownerUser.fullName
            ).isSuccess
        )
        assertEquals(RecordStatus.CANCELLED, repo.observeIncomes(group.groupId).first().first().status)
        assertEquals("CANCEL_INCOME", repo.observeGroupActivities(group.groupId).first().first().action)
    }

    @Test
    fun updateGroupSettings_andInvalidExpenseSplit_behaveAsExpected() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerUser, "Quỹ Ban Đầu", "Mô tả cũ", 8_000_000L).getOrThrow()

        assertTrue(
            repo.updateGroupSettings(
                groupId = group.groupId,
                name = "Quỹ Mới Cập Nhật",
                description = "Mô tả mới",
                monthlyBudgetLimit = 15_000_000L
            ).isSuccess
        )
        val updated = repo.observeGroup(group.groupId).first()
        assertNotNull(updated)
        assertEquals("Quỹ Mới Cập Nhật", updated!!.name)
        assertEquals("Mô tả mới", updated.description)
        assertEquals(15_000_000L, updated.monthlyBudgetLimit)
        assertEquals("UPDATE_GROUP_SETTINGS", repo.observeGroupActivities(group.groupId).first().first().action)

        // Tổng phần chia không khớp tổng số tiền -> thất bại
        val mismatchedExpense = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Khoản chi sai tổng",
                amount = 200_000L,
                category = SpendlyCategory.FOOD,
                payerId = ownerUser.uid,
                createdBy = ownerUser.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerUser.uid, ownerUser.fullName, 150_000L, 75.0)
            )
        )
        assertTrue(mismatchedExpense.isFailure)
    }

    @Test
    fun seedStarterGroupAndUserProfile_areIdempotent() = runTest {
        val repo = newRepo()
        val ensured = repo.ensureUserProfile(
            uid = ownerUser.uid,
            fullName = ownerUser.fullName,
            email = ownerUser.email,
            phone = ownerUser.phone
        ).getOrThrow()
        assertEquals(ownerUser.fullName, ensured.fullName)

        repo.updateUserProfile(uid = ownerUser.uid, phone = "0999000111").getOrThrow()
        assertEquals("0999000111", repo.observeUserProfile(ownerUser.uid).first()?.phone)

        val firstSeed = repo.seedStarterGroupIfEmpty(ensured).getOrThrow()
        assertNotNull(firstSeed)

        val secondSeed = repo.seedStarterGroupIfEmpty(ensured).getOrThrow()
        assertNull(secondSeed)
        assertEquals(1, repo.observeUserGroups(ownerUser.uid).first().size)
    }
}
