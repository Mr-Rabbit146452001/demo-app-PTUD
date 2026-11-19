package com.example.nhoms8

import com.example.nhoms8.data.firestore.FirestoreBudgetRepository
import com.example.nhoms8.data.firestore.FirestoreSettlementRepository
import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.BudgetHealthLevel
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SettlementStatus
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlySettlement
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.domain.usecase.LedgerExpense
import com.example.nhoms8.domain.usecase.LedgerSettlement
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class Phase4SettlementBudgetTest {

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

    private val ownerHung = SpendlyUser(
        uid = "uid_hung",
        fullName = "Lê Văn Hùng",
        email = "hung@spendly.vn",
        phone = "0912345678"
    )

    private val memberLinh = SpendlyUser(
        uid = "uid_linh",
        fullName = "Lý Việt Linh",
        email = "linh@spendly.vn",
        phone = "0908224510"
    )

    private val memberDat = SpendlyUser(
        uid = "uid_dat",
        fullName = "Nguyễn Phan Tấn Đạt",
        email = "dat@spendly.vn",
        phone = "0986420115"
    )

    @Test
    fun pendingSettlementRequest_doesNotChangeBalances_andNotifiesCreditor() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerHung, "Quỹ Ăn Uống", monthlyBudgetLimit = 6_000_000L).getOrThrow()
        repo.joinGroupByInviteCode(memberLinh, group.inviteCode).getOrThrow()

        // Hùng trả 200k chia đều cho Hùng & Linh -> Hùng +100k, Linh -100k
        repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Bún chả",
                amount = 200_000L,
                category = SpendlyCategory.FOOD,
                payerId = ownerHung.uid,
                payerName = ownerHung.fullName,
                createdBy = ownerHung.uid,
                splitType = SplitMethod.EQUAL
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 100_000L, 50.0),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 100_000L, 50.0)
            )
        ).getOrThrow()

        // Linh gửi yêu cầu thanh toán 100k cho Hùng (PENDING)
        val requestResult = repo.createSettlementRequest(
            groupId = group.groupId,
            settlement = SpendlySettlement(
                fromUserId = memberLinh.uid,
                fromUserName = memberLinh.fullName,
                toUserId = ownerHung.uid,
                toUserName = ownerHung.fullName,
                amount = 100_000L,
                note = "Chuyển khoản VCB",
                bankInfo = "VCB 0123456789",
                createdBy = memberLinh.uid
            )
        )
        assertTrue(requestResult.isSuccess)
        val pending = requestResult.getOrThrow()
        assertEquals(SettlementStatus.PENDING, pending.status)

        // Số dư KHÔNG đổi khi mới chỉ gửi yêu cầu PENDING
        val balances = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(100_000L, balances.getValue(ownerHung.uid).netBalance)
        assertEquals(-100_000L, balances.getValue(memberLinh.uid).netBalance)

        // Ghi nhật ký REQUEST_SETTLEMENT và gửi thông báo cho Hùng
        val activities = repo.observeGroupActivities(group.groupId).first()
        assertEquals(FirestoreSpendlyRepository.ACTION_REQUEST_SETTLEMENT, activities.first().action)
        assertEquals(pending.settlementId, activities.first().targetId)

        val hungNotifications = repo.observeNotifications(ownerHung.uid).first()
        assertTrue(hungNotifications.any { it.type == "SETTLEMENT_REQUEST" && it.groupId == group.groupId })

        // Kiểm tra validate đầu vào sai (số tiền <= 0 hoặc tự trả cho chính mình)
        assertTrue(
            repo.createSettlementRequest(
                groupId = group.groupId,
                settlement = SpendlySettlement(
                    fromUserId = memberLinh.uid,
                    toUserId = ownerHung.uid,
                    amount = 0L
                )
            ).isFailure
        )
        assertTrue(
            repo.createSettlementRequest(
                groupId = group.groupId,
                settlement = SpendlySettlement(
                    fromUserId = memberLinh.uid,
                    toUserId = memberLinh.uid,
                    amount = 50_000L
                )
            ).isFailure
        )
    }

    @Test
    fun confirmSettlement_reducesBalancesToZero_writesActivityAndNotification() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerHung, "Quỹ Phòng Trọ").getOrThrow()
        repo.joinGroupByInviteCode(memberLinh, group.inviteCode).getOrThrow()

        repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Tiền Internet",
                amount = 300_000L,
                category = SpendlyCategory.UTILITIES,
                payerId = ownerHung.uid,
                payerName = ownerHung.fullName,
                createdBy = ownerHung.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 150_000L, 50.0),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 150_000L, 50.0)
            )
        ).getOrThrow()

        val settlement = repo.createSettlementRequest(
            groupId = group.groupId,
            settlement = SpendlySettlement(
                fromUserId = memberLinh.uid,
                toUserId = ownerHung.uid,
                amount = 150_000L,
                createdBy = memberLinh.uid
            )
        ).getOrThrow()

        val confirmResult = repo.confirmSettlement(
            groupId = group.groupId,
            settlementId = settlement.settlementId,
            actorId = ownerHung.uid,
            actorName = ownerHung.fullName
        )
        assertTrue(confirmResult.isSuccess)

        val updatedSettlement = repo.observeSettlements(group.groupId).first().first()
        assertEquals(SettlementStatus.CONFIRMED, updatedSettlement.status)
        assertEquals(ownerHung.uid, updatedSettlement.confirmedBy)
        assertTrue((updatedSettlement.confirmedAtMillis ?: 0L) > 0L)

        // Cả 2 thành viên về 0 đ ròng
        val balances = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(0L, balances.getValue(ownerHung.uid).netBalance)
        assertEquals(0L, balances.getValue(memberLinh.uid).netBalance)
        assertEquals(0L, balances.values.sumOf { it.netBalance })

        // Hoạt động & thông báo xác nhận cho người gửi (Linh)
        val latestActivity = repo.observeGroupActivities(group.groupId).first().first()
        assertEquals(FirestoreSpendlyRepository.ACTION_CONFIRM_SETTLEMENT, latestActivity.action)
        assertEquals(settlement.settlementId, latestActivity.targetId)

        val linhNotifications = repo.observeNotifications(memberLinh.uid).first()
        assertTrue(linhNotifications.any { it.type == "SETTLEMENT_CONFIRMED" && it.groupId == group.groupId })
    }

    @Test
    fun doubleConfirmSettlement_failsCleanlyWithoutDoubleCounting() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerHung, "Quỹ Du Lịch").getOrThrow()
        repo.joinGroupByInviteCode(memberLinh, group.inviteCode).getOrThrow()

        repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Đặt phòng",
                amount = 400_000L,
                category = SpendlyCategory.HOUSING,
                payerId = ownerHung.uid,
                createdBy = ownerHung.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 200_000L, 50.0),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 200_000L, 50.0)
            )
        ).getOrThrow()

        val settlement = repo.createSettlementRequest(
            groupId = group.groupId,
            settlement = SpendlySettlement(
                fromUserId = memberLinh.uid,
                toUserId = ownerHung.uid,
                amount = 200_000L,
                createdBy = memberLinh.uid
            )
        ).getOrThrow()

        assertTrue(
            repo.confirmSettlement(group.groupId, settlement.settlementId, ownerHung.uid, ownerHung.fullName).isSuccess
        )

        val secondConfirm = repo.confirmSettlement(
            groupId = group.groupId,
            settlementId = settlement.settlementId,
            actorId = ownerHung.uid,
            actorName = ownerHung.fullName
        )
        assertTrue(secondConfirm.isFailure)
        assertEquals(
            "Giao dịch thanh toán này đã được xác nhận trước đó",
            secondConfirm.exceptionOrNull()?.message
        )

        // Không bị trừ/cộng kép sau lần confirm thứ 2
        val balances = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(0L, balances.getValue(ownerHung.uid).netBalance)
        assertEquals(0L, balances.getValue(memberLinh.uid).netBalance)
    }

    @Test
    fun expenseLifecycleAfterConfirmedSettlement_preservesSettledOffset() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerHung, "Quỹ Chung").getOrThrow()
        repo.joinGroupByInviteCode(memberLinh, group.inviteCode).getOrThrow()
        repo.joinGroupByInviteCode(memberDat, group.inviteCode).getOrThrow()

        // Khoản chi 1: Hùng trả 300k chia đều 3 người -> Hùng +200k, Linh -100k, Đạt -100k
        repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Ăn tối",
                amount = 300_000L,
                category = SpendlyCategory.FOOD,
                payerId = ownerHung.uid,
                createdBy = ownerHung.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 100_000L, 33.34),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 100_000L, 33.33),
                ExpenseParticipant(memberDat.uid, memberDat.fullName, 100_000L, 33.33)
            )
        ).getOrThrow()

        // Linh thanh toán xong 100k cho Hùng và được xác nhận
        val settlement = repo.createSettlementRequest(
            groupId = group.groupId,
            settlement = SpendlySettlement(
                fromUserId = memberLinh.uid,
                toUserId = ownerHung.uid,
                amount = 100_000L,
                createdBy = memberLinh.uid
            )
        ).getOrThrow()
        repo.confirmSettlement(group.groupId, settlement.settlementId, ownerHung.uid, ownerHung.fullName).getOrThrow()

        // Khoản chi 2 SAU khi đã quyết toán: Linh trả 200k cho Hùng & Linh -> mỗi người chịu 100k
        val exp2 = repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Cà phê sáng",
                amount = 200_000L,
                category = SpendlyCategory.FOOD,
                payerId = memberLinh.uid,
                createdBy = memberLinh.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 100_000L, 50.0),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 100_000L, 50.0)
            )
        ).getOrThrow()

        // Hùng: +200k (exp1) - 100k (đã nhận từ Linh) - 100k (exp2) = 0
        // Linh: -100k (exp1) + 100k (đã trả Hùng) + 100k (exp2) = +100k
        // Đạt: -100k (exp1) = -100k
        val afterExp2 = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(0L, afterExp2.getValue(ownerHung.uid).netBalance)
        assertEquals(100_000L, afterExp2.getValue(memberLinh.uid).netBalance)
        assertEquals(-100_000L, afterExp2.getValue(memberDat.uid).netBalance)
        assertEquals(0L, afterExp2.values.sumOf { it.netBalance })

        // Cập nhật exp2 thành 300k cho cả 3 người (mỗi người 100k) -> Linh net +200k, Hùng -0k, Đạt -200k?
        // Kiểm tra lại: Hùng paid 300k, share 100k(exp1)+100k(exp2)+100k(settlement) = 300k -> 0
        // Linh paid 100k(settlement)+300k(exp2)=400k, share 100k(exp1)+100k(exp2)=200k -> +200k
        // Đạt paid 0, share 100k(exp1)+100k(exp2)=200k -> -200k
        repo.updateExpenseWithParticipants(
            groupId = group.groupId,
            expense = exp2.copy(amount = 300_000L),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 100_000L, 33.34),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 100_000L, 33.33),
                ExpenseParticipant(memberDat.uid, memberDat.fullName, 100_000L, 33.33)
            )
        ).getOrThrow()

        val afterUpdate = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(0L, afterUpdate.getValue(ownerHung.uid).netBalance)
        assertEquals(200_000L, afterUpdate.getValue(memberLinh.uid).netBalance)
        assertEquals(-200_000L, afterUpdate.getValue(memberDat.uid).netBalance)

        // Hủy exp2 -> quay lại đúng trạng thái sau settlement (+100k Hùng, 0 Linh, -100k Đạt)
        repo.cancelExpense(group.groupId, exp2.expenseId, memberLinh.uid, memberLinh.fullName).getOrThrow()
        val afterCancel = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(100_000L, afterCancel.getValue(ownerHung.uid).netBalance)
        assertEquals(0L, afterCancel.getValue(memberLinh.uid).netBalance)
        assertEquals(-100_000L, afterCancel.getValue(memberDat.uid).netBalance)
    }

    @Test
    fun rejectedSettlement_leavesBalancesUntouched_andNotifiesSender() = runTest {
        val repo = newRepo()
        val group = repo.createGroup(ownerHung, "Quỹ Nhóm").getOrThrow()
        repo.joinGroupByInviteCode(memberLinh, group.inviteCode).getOrThrow()

        repo.createExpenseWithParticipants(
            groupId = group.groupId,
            expense = SpendlyExpense(
                title = "Mua đồ",
                amount = 200_000L,
                category = SpendlyCategory.SHOPPING,
                payerId = ownerHung.uid,
                createdBy = ownerHung.uid
            ),
            participants = listOf(
                ExpenseParticipant(ownerHung.uid, ownerHung.fullName, 100_000L, 50.0),
                ExpenseParticipant(memberLinh.uid, memberLinh.fullName, 100_000L, 50.0)
            )
        ).getOrThrow()

        val pending = repo.createSettlementRequest(
            groupId = group.groupId,
            settlement = SpendlySettlement(
                fromUserId = memberLinh.uid,
                toUserId = ownerHung.uid,
                amount = 100_000L,
                createdBy = memberLinh.uid
            )
        ).getOrThrow()

        val rejectResult = repo.rejectSettlement(
            groupId = group.groupId,
            settlementId = pending.settlementId,
            actorId = ownerHung.uid,
            actorName = ownerHung.fullName,
            reason = "Chưa nhận được tiền chuyển khoản"
        )
        assertTrue(rejectResult.isSuccess)

        val rejected = repo.observeSettlements(group.groupId).first().first()
        assertEquals(SettlementStatus.REJECTED, rejected.status)

        // Số dư giữ nguyên
        val balances = repo.observeMemberBalances(group.groupId).first().associateBy { it.userId }
        assertEquals(100_000L, balances.getValue(ownerHung.uid).netBalance)
        assertEquals(-100_000L, balances.getValue(memberLinh.uid).netBalance)

        // Hoạt động và thông báo từ chối
        val latestActivity = repo.observeGroupActivities(group.groupId).first().first()
        assertEquals(FirestoreSpendlyRepository.ACTION_REJECT_SETTLEMENT, latestActivity.action)
        assertTrue(latestActivity.description.contains("Chưa nhận được tiền chuyển khoản"))

        val linhNotifications = repo.observeNotifications(memberLinh.uid).first()
        assertTrue(linhNotifications.any { it.type == "SETTLEMENT_REJECTED" && it.body.contains("Chưa nhận được tiền") })

        // Không thể confirm một giao dịch đã bị từ chối
        assertTrue(
            repo.confirmSettlement(group.groupId, pending.settlementId, ownerHung.uid, ownerHung.fullName).isFailure
        )
    }

    @Test
    fun budgetRepositoryCrud_healthLevels_categoryFilter_andMonthlyLimitSync() = runTest {
        val repo = newRepo()
        val settlementDelegate = FirestoreSettlementRepository(repo)
        val budgetDelegate = FirestoreBudgetRepository(repo)

        val group = repo.createGroup(ownerHung, "Quỹ Tháng 11", monthlyBudgetLimit = 10_000_000L).getOrThrow()
        assertTrue(settlementDelegate.observeSettlements(group.groupId).first().isEmpty())

        // 1. Tạo ngân sách tổng nhóm (category == null) -> đồng bộ monthlyBudgetLimit của SpendlyGroup
        val groupBudget = budgetDelegate.upsertBudget(
            groupId = group.groupId,
            budget = SpendlyBudget(
                name = "Ngân sách tổng tháng 11",
                limitAmount = 2_000_000L,
                period = "MONTHLY",
                category = null,
                createdBy = ownerHung.uid
            )
        ).getOrThrow()
        assertEquals(2_000_000L, repo.observeGroup(group.groupId).first()?.monthlyBudgetLimit)

        // 2. Tạo ngân sách theo danh mục Ăn uống
        val foodBudget = budgetDelegate.upsertBudget(
            groupId = group.groupId,
            budget = SpendlyBudget(
                name = "Ngân sách Ăn uống",
                limitAmount = 1_000_000L,
                period = "MONTHLY",
                category = SpendlyCategory.FOOD,
                createdBy = ownerHung.uid
            )
        ).getOrThrow()
        assertEquals(2, budgetDelegate.observeBudgets(group.groupId).first().size)

        // Cập nhật ngân sách tổng nhóm -> kiểm tra cập nhật in-place & đồng bộ lại monthlyBudgetLimit
        val updatedGroupBudget = budgetDelegate.upsertBudget(
            groupId = group.groupId,
            budget = groupBudget.copy(limitAmount = 5_000_000L)
        ).getOrThrow()
        assertEquals(groupBudget.budgetId, updatedGroupBudget.budgetId)
        assertEquals(5_000_000L, repo.observeGroup(group.groupId).first()?.monthlyBudgetLimit)
        assertEquals(2, budgetDelegate.observeBudgets(group.groupId).first().size)

        // 3. Kiểm tra 4 vùng evaluateBudgetHealth (40% NORMAL, 65% WATCH, 90% WARNING, 110% EXCEEDED)
        val engine = SplitCalculationEngine()
        assertEquals(BudgetHealthLevel.NORMAL, engine.evaluateBudgetHealth(1_000_000L, 400_000L))
        assertEquals(BudgetHealthLevel.NORMAL, engine.evaluateBudgetHealth(1_000_000L, 500_000L))
        assertEquals(BudgetHealthLevel.WATCH, engine.evaluateBudgetHealth(1_000_000L, 650_000L))
        assertEquals(BudgetHealthLevel.WATCH, engine.evaluateBudgetHealth(1_000_000L, 800_000L))
        assertEquals(BudgetHealthLevel.WARNING, engine.evaluateBudgetHealth(1_000_000L, 900_000L))
        assertEquals(BudgetHealthLevel.WARNING, engine.evaluateBudgetHealth(1_000_000L, 1_000_000L))
        assertEquals(BudgetHealthLevel.EXCEEDED, engine.evaluateBudgetHealth(1_000_000L, 1_100_000L))
        assertEquals(BudgetHealthLevel.EXCEEDED, engine.evaluateBudgetHealth(0L, 100_000L))
        assertEquals(BudgetHealthLevel.NORMAL, engine.evaluateBudgetHealth(0L, 0L))

        // 4. Kiểm tra lọc theo danh mục & loại bỏ khoản chi CANCELLED trong computeBudgetSummary
        val expenses = listOf(
            SpendlyExpense(
                expenseId = "e1",
                groupId = group.groupId,
                title = "Cơm trưa",
                amount = 650_000L,
                category = SpendlyCategory.FOOD,
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e2",
                groupId = group.groupId,
                title = "Grab",
                amount = 500_000L,
                category = SpendlyCategory.TRANSPORT,
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e3",
                groupId = group.groupId,
                title = "Trà sữa đã hủy",
                amount = 300_000L,
                category = SpendlyCategory.FOOD,
                status = RecordStatus.CANCELLED
            )
        )

        val foodSummary = engine.computeBudgetSummary(foodBudget, expenses)
        assertEquals(650_000L, foodSummary.spentAmount)
        assertEquals(350_000L, foodSummary.remainingAmount)
        assertEquals(65f, foodSummary.usagePercent, 0.01f)
        assertEquals(BudgetHealthLevel.WATCH, foodSummary.level)

        val overallSummary = engine.computeBudgetSummary(updatedGroupBudget, expenses)
        assertEquals(1_150_000L, overallSummary.spentAmount)
        assertEquals(3_850_000L, overallSummary.remainingAmount)
        assertEquals(23f, overallSummary.usagePercent, 0.01f)
        assertEquals(BudgetHealthLevel.NORMAL, overallSummary.level)

        // 5. Xóa ngân sách & kiểm tra nhật ký UPSERT_BUDGET / DELETE_BUDGET
        assertTrue(budgetDelegate.deleteBudget(group.groupId, foodBudget.budgetId).isSuccess)
        val remainingBudgets = budgetDelegate.observeBudgets(group.groupId).first()
        assertEquals(1, remainingBudgets.size)
        assertFalse(remainingBudgets.any { it.budgetId == foodBudget.budgetId })

        val actions = repo.observeGroupActivities(group.groupId).first().map { it.action }
        assertTrue(actions.contains(FirestoreSpendlyRepository.ACTION_UPSERT_BUDGET))
        assertTrue(actions.contains(FirestoreSpendlyRepository.ACTION_DELETE_BUDGET))
    }

    @Test
    fun splitCalculationEngine_confirmedSettlementsOverload_maintainsZeroSumInvariant() {
        val engine = SplitCalculationEngine()
        val members = listOf("A", "B", "C")
        val expenses = listOf(
            LedgerExpense(
                expenseId = "e1",
                payerId = "A",
                amount = 300_000L,
                isActive = true,
                shares = mapOf("A" to 100_000L, "B" to 100_000L, "C" to 100_000L)
            )
        )
        val settlements = listOf(
            LedgerSettlement("s1", fromUserId = "B", toUserId = "A", amount = 100_000L, isConfirmed = true),
            LedgerSettlement("s2", fromUserId = "C", toUserId = "A", amount = 100_000L, isConfirmed = false)
        )

        val summaryMap = engine.computeMemberLedgerBalances(members, expenses, settlements)
        assertEquals(100_000L, summaryMap.getValue("A").netBalance)
        assertEquals(0L, summaryMap.getValue("B").netBalance)
        assertEquals(-100_000L, summaryMap.getValue("C").netBalance)
        assertEquals(0L, summaryMap.values.sumOf { it.netBalance })

        val transfers = engine.computeGreedySettlements(summaryMap.mapValues { it.value.netBalance })
        assertEquals(1, transfers.size)
        assertEquals("C", transfers.first().fromUserId)
        assertEquals("A", transfers.first().toUserId)
        assertEquals(100_000L, transfers.first().amount)
    }

    @Test
    fun enumFromRawAndDateRangeBudgetSummary_behaveCorrectly() {
        assertEquals(SettlementStatus.PENDING, SettlementStatus.fromRaw(null))
        assertEquals(SettlementStatus.CONFIRMED, SettlementStatus.fromRaw("confirmed"))
        assertEquals(SettlementStatus.REJECTED, SettlementStatus.fromRaw(" REJECTED "))
        assertEquals(BudgetHealthLevel.NORMAL, BudgetHealthLevel.fromRaw(null))
        assertEquals(BudgetHealthLevel.EXCEEDED, BudgetHealthLevel.fromRaw("exceeded"))

        val budget = SpendlyBudget(
            budgetId = "b_period",
            groupId = "g1",
            name = "Ngân sách tuần",
            limitAmount = 500_000L,
            startDateMillis = 1_000L,
            endDateMillis = 2_000L,
            category = null
        )
        val expenses = listOf(
            SpendlyExpense(expenseId = "in", groupId = "g1", amount = 450_000L, expenseDateMillis = 1_500L),
            SpendlyExpense(expenseId = "out", groupId = "g1", amount = 400_000L, expenseDateMillis = 2_500L)
        )

        val summary = SplitCalculationEngine.computeBudgetSummary(budget, expenses)
        assertEquals(450_000L, summary.spentAmount)
        assertEquals(50_000L, summary.remainingAmount)
        assertEquals(90f, summary.usagePercent, 0.01f)
        assertEquals(BudgetHealthLevel.WARNING, summary.level)
    }
}
