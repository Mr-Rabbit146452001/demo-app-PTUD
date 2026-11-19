package com.example.nhoms8

import com.example.nhoms8.domain.usecase.EngineSplitMode
import com.example.nhoms8.domain.usecase.LedgerExpense
import com.example.nhoms8.domain.usecase.ParticipantInput
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import com.example.nhoms8.domain.usecase.SplitValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm thử Bộ máy Chia tiền & Cân bằng Sổ cái (SplitCalculationEngineTest)
 */
class SplitCalculationEngineTest {

    private val engine = SplitCalculationEngine()

    @Test
    fun equalSplit_exactDivision_assignsExactSharesAndPercentages() {
        val participants = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true),
            ParticipantInput("u2", "Linh", isIncluded = true),
            ParticipantInput("u3", "Đạt", isIncluded = true),
            ParticipantInput("u4", "Bình", isIncluded = false)
        )

        val result = engine.calculateAndValidateSplit(
            totalAmount = 900_000L,
            mode = EngineSplitMode.EQUAL,
            participants = participants
        )

        assertTrue(result is SplitValidationResult.Valid)
        val shares = (result as SplitValidationResult.Valid).shares
        assertEquals(3, shares.size)
        assertEquals(listOf("u1", "u2", "u3"), shares.map { it.userId })
        assertEquals(listOf(300_000L, 300_000L, 300_000L), shares.map { it.shareAmount })
        assertEquals(900_000L, shares.sumOf { it.shareAmount })
        shares.forEach { assertEquals(33.33, it.percentage, 0.01) }
    }

    @Test
    fun equalSplit_withRemainder_100kDividedBy3_preservesStrictTotal() {
        val participants = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true),
            ParticipantInput("u2", "Linh", isIncluded = true),
            ParticipantInput("u3", "Đạt", isIncluded = true)
        )

        val result = SplitCalculationEngine.calculateAndValidateSplit(
            totalAmount = 100_000L,
            mode = EngineSplitMode.EQUAL,
            participants = participants
        )

        assertTrue(result is SplitValidationResult.Valid)
        val shares = (result as SplitValidationResult.Valid).shares
        assertEquals(3, shares.size)
        assertEquals(33_334L, shares[0].shareAmount)
        assertEquals(33_333L, shares[1].shareAmount)
        assertEquals(33_333L, shares[2].shareAmount)
        assertEquals(100_000L, shares.sumOf { it.shareAmount })
    }

    @Test
    fun percentageSplit_valid50_30_20_computesExactAmounts() {
        val participants = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, percentage = 50.0),
            ParticipantInput("u2", "Linh", isIncluded = true, percentage = 30.0),
            ParticipantInput("u3", "Đạt", isIncluded = true, percentage = 20.0)
        )

        val result = engine.calculateAndValidateSplit(
            totalAmount = 1_000_000L,
            mode = EngineSplitMode.PERCENTAGE,
            participants = participants
        )

        assertTrue(result is SplitValidationResult.Valid)
        val shares = (result as SplitValidationResult.Valid).shares
        assertEquals(listOf(500_000L, 300_000L, 200_000L), shares.map { it.shareAmount })
        assertEquals(listOf(50.0, 30.0, 20.0), shares.map { it.percentage })
        assertEquals(1_000_000L, shares.sumOf { it.shareAmount })
    }

    @Test
    fun percentageSplit_roundingRemainder_3333_3333_3334_adjustsFirstNonZeroParticipant() {
        val standardParticipants = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, percentage = 33.33),
            ParticipantInput("u2", "Linh", isIncluded = true, percentage = 33.33),
            ParticipantInput("u3", "Đạt", isIncluded = true, percentage = 33.34)
        )

        val exact100k = engine.calculateAndValidateSplit(
            totalAmount = 100_000L,
            mode = EngineSplitMode.PERCENTAGE,
            participants = standardParticipants
        ) as SplitValidationResult.Valid
        assertEquals(listOf(33_330L, 33_330L, 33_340L), exact100k.shares.map { it.shareAmount })
        assertEquals(100_000L, exact100k.shares.sumOf { it.shareAmount })

        // Với tổng có số dư lẻ (100.001 đ) và người đầu tiên 0%, phần dư cộng vào người đầu tiên có tỷ lệ > 0
        val zeroFirstParticipants = listOf(
            ParticipantInput("u0", "Bình", isIncluded = true, percentage = 0.0),
            ParticipantInput("u1", "Hùng", isIncluded = true, percentage = 33.33),
            ParticipantInput("u2", "Linh", isIncluded = true, percentage = 33.33),
            ParticipantInput("u3", "Đạt", isIncluded = true, percentage = 33.34)
        )
        val oddResult = engine.calculateAndValidateSplit(
            totalAmount = 100_001L,
            mode = EngineSplitMode.PERCENTAGE,
            participants = zeroFirstParticipants
        ) as SplitValidationResult.Valid
        assertEquals(0L, oddResult.shares[0].shareAmount)
        assertEquals(33_331L, oddResult.shares[1].shareAmount)
        assertEquals(33_330L, oddResult.shares[2].shareAmount)
        assertEquals(33_340L, oddResult.shares[3].shareAmount)
        assertEquals(100_001L, oddResult.shares.sumOf { it.shareAmount })
    }

    @Test
    fun percentageSplit_boundaryTolerance_9999And10001AreValid_outsideIsInvalid() {
        val sum9999 = listOf(
            ParticipantInput("u1", "A", isIncluded = true, percentage = 33.33),
            ParticipantInput("u2", "B", isIncluded = true, percentage = 33.33),
            ParticipantInput("u3", "C", isIncluded = true, percentage = 33.33)
        )
        val validLow = engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, sum9999)
        assertTrue(validLow is SplitValidationResult.Valid)
        assertEquals(100_000L, (validLow as SplitValidationResult.Valid).shares.sumOf { it.shareAmount })

        val sum10001 = listOf(
            ParticipantInput("u1", "A", isIncluded = true, percentage = 33.34),
            ParticipantInput("u2", "B", isIncluded = true, percentage = 33.34),
            ParticipantInput("u3", "C", isIncluded = true, percentage = 33.33)
        )
        val validHigh = engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, sum10001)
        assertTrue(validHigh is SplitValidationResult.Valid)
        assertEquals(100_000L, (validHigh as SplitValidationResult.Valid).shares.sumOf { it.shareAmount })

        val sum9998 = listOf(
            ParticipantInput("u1", "A", isIncluded = true, percentage = 33.33),
            ParticipantInput("u2", "B", isIncluded = true, percentage = 33.33),
            ParticipantInput("u3", "C", isIncluded = true, percentage = 33.32)
        )
        assertTrue(engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, sum9998) is SplitValidationResult.Invalid)

        val sum10002 = listOf(
            ParticipantInput("u1", "A", isIncluded = true, percentage = 33.34),
            ParticipantInput("u2", "B", isIncluded = true, percentage = 33.34),
            ParticipantInput("u3", "C", isIncluded = true, percentage = 33.34)
        )
        assertTrue(engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, sum10002) is SplitValidationResult.Invalid)
    }

    @Test
    fun percentageSplit_negativeOrInvalidSum_returnsInvalid() {
        val negativePct = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, percentage = -10.0),
            ParticipantInput("u2", "Linh", isIncluded = true, percentage = 110.0)
        )
        assertTrue(
            engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, negativePct)
                is SplitValidationResult.Invalid
        )

        val not100Pct = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, percentage = 50.0),
            ParticipantInput("u2", "Linh", isIncluded = true, percentage = 40.0)
        )
        assertTrue(
            engine.calculateAndValidateSplit(100_000L, EngineSplitMode.PERCENTAGE, not100Pct)
                is SplitValidationResult.Invalid
        )
    }

    @Test
    fun customSplit_validSumEqualsTotal_andInvalidCases() {
        val validCustom = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, customAmount = 100_000L),
            ParticipantInput("u2", "Linh", isIncluded = true, customAmount = 200_000L),
            ParticipantInput("u3", "Đạt", isIncluded = false, customAmount = 999_999L)
        )
        val validResult = engine.calculateAndValidateSplit(
            totalAmount = 300_000L,
            mode = EngineSplitMode.CUSTOM,
            participants = validCustom
        )
        assertTrue(validResult is SplitValidationResult.Valid)
        val shares = (validResult as SplitValidationResult.Valid).shares
        assertEquals(2, shares.size)
        assertEquals(100_000L, shares[0].shareAmount)
        assertEquals(33.33, shares[0].percentage, 0.01)
        assertEquals(200_000L, shares[1].shareAmount)
        assertEquals(66.67, shares[1].percentage, 0.01)

        val mismatchSum = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, customAmount = 100_000L),
            ParticipantInput("u2", "Linh", isIncluded = true, customAmount = 150_000L)
        )
        assertTrue(
            engine.calculateAndValidateSplit(300_000L, EngineSplitMode.CUSTOM, mismatchSum)
                is SplitValidationResult.Invalid
        )

        val negativeAmount = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true, customAmount = -50_000L),
            ParticipantInput("u2", "Linh", isIncluded = true, customAmount = 350_000L)
        )
        assertTrue(
            engine.calculateAndValidateSplit(300_000L, EngineSplitMode.CUSTOM, negativeAmount)
                is SplitValidationResult.Invalid
        )
    }

    @Test
    fun validation_rejectsNonPositiveTotal_noParticipants_orNonMember() {
        val participants = listOf(
            ParticipantInput("u1", "Hùng", isIncluded = true),
            ParticipantInput("u_outsider", "Khách", isIncluded = false)
        )

        assertTrue(
            engine.calculateAndValidateSplit(0L, EngineSplitMode.EQUAL, participants)
                is SplitValidationResult.Invalid
        )
        assertTrue(
            engine.calculateAndValidateSplit(-100_000L, EngineSplitMode.EQUAL, participants)
                is SplitValidationResult.Invalid
        )
        assertTrue(
            engine.calculateAndValidateSplit(
                100_000L,
                EngineSplitMode.EQUAL,
                listOf(ParticipantInput("u1", "Hùng", isIncluded = false))
            ) is SplitValidationResult.Invalid
        )

        // Excluded outsider should NOT cause failure when validMemberIds is checked
        assertTrue(
            engine.calculateAndValidateSplit(
                totalAmount = 100_000L,
                mode = EngineSplitMode.EQUAL,
                participants = participants,
                validMemberIds = setOf("u1", "u2")
            ) is SplitValidationResult.Valid
        )

        // Included outsider MUST fail validation
        assertTrue(
            engine.calculateAndValidateSplit(
                totalAmount = 100_000L,
                mode = EngineSplitMode.EQUAL,
                participants = listOf(
                    ParticipantInput("u1", "Hùng", isIncluded = true),
                    ParticipantInput("u_outsider", "Khách", isIncluded = true)
                ),
                validMemberIds = setOf("u1", "u2")
            ) is SplitValidationResult.Invalid
        )
    }

    @Test
    fun ledgerBalances_payerNotParticipatingInSplit_generatesThreeTransfersToPayer() {
        val members = listOf("A", "B", "C", "D")
        val expenses = listOf(
            LedgerExpense(
                expenseId = "exp_1",
                payerId = "A",
                amount = 600_000L,
                isActive = true,
                shares = mapOf(
                    "B" to 200_000L,
                    "C" to 200_000L,
                    "D" to 200_000L
                )
            )
        )

        val balances = engine.computeMemberLedgerBalances(members, expenses)

        assertEquals(600_000L, balances["A"]?.totalPaid)
        assertEquals(0L, balances["A"]?.totalShare)
        assertEquals(600_000L, balances["A"]?.netBalance)

        listOf("B", "C", "D").forEach { debtor ->
            assertEquals(0L, balances[debtor]?.totalPaid)
            assertEquals(200_000L, balances[debtor]?.totalShare)
            assertEquals(-200_000L, balances[debtor]?.netBalance)
        }

        val settlements = engine.computeGreedySettlements(balances.mapValues { it.value.netBalance })
        assertEquals(3, settlements.size)
        assertTrue(settlements.all { it.toUserId == "A" && it.amount == 200_000L })
        assertEquals(setOf("B", "C", "D"), settlements.map { it.fromUserId }.toSet())
    }

    @Test
    fun ledgerBalances_editedAndCancelledExpenses_recalculatesAccurately() {
        val members = listOf("A", "B", "C")
        val initialExpenses = listOf(
            LedgerExpense(
                expenseId = "exp_1",
                payerId = "A",
                amount = 300_000L,
                isActive = true,
                shares = mapOf("A" to 100_000L, "B" to 100_000L, "C" to 100_000L)
            ),
            LedgerExpense(
                expenseId = "exp_2",
                payerId = "B",
                amount = 150_000L,
                isActive = true,
                shares = mapOf("A" to 50_000L, "B" to 50_000L, "C" to 50_000L)
            )
        )

        val initialBalances = engine.computeMemberLedgerBalances(members, initialExpenses)
        assertEquals(150_000L, initialBalances["A"]?.netBalance) // paid 300k - share 150k
        assertEquals(0L, initialBalances["B"]?.netBalance)       // paid 150k - share 150k
        assertEquals(-150_000L, initialBalances["C"]?.netBalance) // paid 0 - share 150k

        // Sửa exp_1 (tăng lên 450k) và hủy mềm exp_2 (isActive = false)
        val updatedExpenses = listOf(
            LedgerExpense(
                expenseId = "exp_1",
                payerId = "A",
                amount = 450_000L,
                isActive = true,
                shares = mapOf("A" to 150_000L, "B" to 150_000L, "C" to 150_000L)
            ),
            LedgerExpense(
                expenseId = "exp_2",
                payerId = "B",
                amount = 150_000L,
                isActive = false,
                shares = mapOf("A" to 50_000L, "B" to 50_000L, "C" to 50_000L)
            )
        )

        val recalculated = engine.computeMemberLedgerBalances(members, updatedExpenses)
        assertEquals(450_000L, recalculated["A"]?.totalPaid)
        assertEquals(150_000L, recalculated["A"]?.totalShare)
        assertEquals(300_000L, recalculated["A"]?.netBalance)

        assertEquals(0L, recalculated["B"]?.totalPaid)
        assertEquals(150_000L, recalculated["B"]?.totalShare)
        assertEquals(-150_000L, recalculated["B"]?.netBalance)

        assertEquals(0L, recalculated["C"]?.totalPaid)
        assertEquals(150_000L, recalculated["C"]?.totalShare)
        assertEquals(-150_000L, recalculated["C"]?.netBalance)
    }

    @Test
    fun ledgerBalances_memberLeftGroup_retainsActiveExpenseDebts() {
        // Thành viên D đã rời nhóm nên không còn trong currentMembers, nhưng vẫn có khoản chi active
        val currentMembers = listOf("A", "B", "C")
        val expenses = listOf(
            LedgerExpense(
                expenseId = "exp_active",
                payerId = "A",
                amount = 400_000L,
                isActive = true,
                shares = mapOf("A" to 100_000L, "B" to 100_000L, "C" to 100_000L, "D" to 100_000L)
            ),
            LedgerExpense(
                expenseId = "exp_cancelled_former",
                payerId = "E",
                amount = 200_000L,
                isActive = false,
                shares = mapOf("E" to 100_000L, "A" to 100_000L)
            )
        )

        val balances = engine.computeMemberLedgerBalances(currentMembers, expenses)

        assertTrue(balances.containsKey("D"))
        assertEquals(0L, balances["D"]?.totalPaid)
        assertEquals(100_000L, balances["D"]?.totalShare)
        assertEquals(-100_000L, balances["D"]?.netBalance)

        // E chỉ xuất hiện trong khoản chi đã hủy nên không bị đưa vào sổ cái
        assertTrue(!balances.containsKey("E"))

        val settlements = engine.computeGreedySettlements(balances.mapValues { it.value.netBalance })
        assertEquals(3, settlements.size)
        assertTrue(settlements.any { it.fromUserId == "D" && it.toUserId == "A" && it.amount == 100_000L })
    }

    @Test
    fun greedySettlement_reducesFourPersonCyclicDebtCleanly() {
        // Chuỗi nợ vòng tròn 4 người:
        // A trả hộ B 400k, B trả hộ C 300k, C trả hộ D 200k, D trả hộ A 100k
        // -> A: +300k, B: -100k, C: -100k, D: -100k
        val members = listOf("A", "B", "C", "D")
        val cyclicExpenses = listOf(
            LedgerExpense("e1", payerId = "A", amount = 400_000L, isActive = true, shares = mapOf("B" to 400_000L)),
            LedgerExpense("e2", payerId = "B", amount = 300_000L, isActive = true, shares = mapOf("C" to 300_000L)),
            LedgerExpense("e3", payerId = "C", amount = 200_000L, isActive = true, shares = mapOf("D" to 200_000L)),
            LedgerExpense("e4", payerId = "D", amount = 100_000L, isActive = true, shares = mapOf("A" to 100_000L))
        )

        val balances = engine.computeMemberLedgerBalances(members, cyclicExpenses)
        assertEquals(300_000L, balances["A"]?.netBalance)
        assertEquals(-100_000L, balances["B"]?.netBalance)
        assertEquals(-100_000L, balances["C"]?.netBalance)
        assertEquals(-100_000L, balances["D"]?.netBalance)

        val transfers = engine.computeGreedySettlements(balances.mapValues { it.value.netBalance })
        assertEquals(3, transfers.size)
        assertEquals(300_000L, transfers.sumOf { it.amount })
        assertTrue(transfers.all { it.toUserId == "A" && it.amount == 100_000L })
        assertEquals(setOf("B", "C", "D"), transfers.map { it.fromUserId }.toSet())

        // Vòng nợ triệt tiêu hoàn toàn (A->B->C->D->A đều 200k) -> 0 giao dịch chuyển khoản
        val zeroCycleTransfers = engine.computeGreedySettlements(
            mapOf("A" to 0L, "B" to 0L, "C" to 0L, "D" to 0L)
        )
        assertEquals(0, zeroCycleTransfers.size)
    }

    @Test
    fun greedySettlement_asymmetricBalances_matchesLargestDebtorWithLargestCreditorAtEachStep() {
        // Creditors: A (+100k), B (+50k); Debtors: C (-70k), D (-50k), E (-30k)
        // Step 1: C(70k) -> A(100k) => A còn +30k, B +50k
        // Step 2: D(50k) -> B(50k) => khớp hết 100%, chỉ 3 giao dịch thay vì 4!
        // Step 3: E(30k) -> A(30k)
        val transfers = engine.computeGreedySettlements(
            mapOf(
                "A" to 100_000L,
                "B" to 50_000L,
                "C" to -70_000L,
                "D" to -50_000L,
                "E" to -30_000L
            )
        )

        assertEquals(3, transfers.size)
        assertEquals(150_000L, transfers.sumOf { it.amount })
        assertEquals("C", transfers[0].fromUserId)
        assertEquals("A", transfers[0].toUserId)
        assertEquals(70_000L, transfers[0].amount)
    }
}
