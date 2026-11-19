package com.example.nhoms8.domain.usecase

import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.domain.model.BudgetHealthLevel
import com.example.nhoms8.domain.model.BudgetStatusSummary
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyExpense
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.PriorityQueue

/**
 * Chế độ chia tiền trong bộ máy tính toán chia bill.
 */
enum class EngineSplitMode {
    EQUAL,
    PERCENTAGE,
    CUSTOM
}

/**
 * Đầu vào của từng thành viên khi cấu hình chia một khoản chi.
 */
data class ParticipantInput(
    val userId: String,
    val displayName: String,
    val isIncluded: Boolean,
    val percentage: Double = 0.0,
    val customAmount: Long = 0L
)

/**
 * Kết quả phần tiền đã tính toán và làm tròn cho từng thành viên tham gia.
 */
data class ComputedShare(
    val userId: String,
    val displayName: String,
    val shareAmount: Long,
    val percentage: Double
)

/**
 * Kết quả kiểm tra tính hợp lệ và phân bổ chia tiền.
 */
sealed class SplitValidationResult {
    data class Valid(val shares: List<ComputedShare>) : SplitValidationResult()
    data class Invalid(val errorMessage: String) : SplitValidationResult()
}

/**
 * Bản ghi khoản chi chuẩn hóa cho sổ cái (Ledger) thuần JVM, không phụ thuộc UI/Android.
 */
data class LedgerExpense(
    val expenseId: String,
    val payerId: String,
    val amount: Long,
    val isActive: Boolean,
    val shares: Map<String, Long>
)

/**
 * Tổng hợp sổ cái cho từng thành viên: tổng đã trả hộ, tổng phần phải chịu và số dư ròng.
 */
data class LedgerMemberSummary(
    val userId: String,
    val totalPaid: Long,
    val totalShare: Long,
    val netBalance: Long
)

/**
 * Bản ghi giao dịch quyết toán công nợ chuẩn hóa cho sổ cái (Ledger) thuần JVM.
 */
data class LedgerSettlement(
    val settlementId: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Long,
    val isConfirmed: Boolean
)

/**
 * Đề xuất chuyển khoản bù trừ nợ giữa hai thành viên.
 */
data class DebtTransfer(
    val fromUserId: String,
    val toUserId: String,
    val amount: Long
)

/**
 * Bộ máy Tính toán Chia tiền & Cân bằng Sổ cái Thuần JVM (SplitCalculationEngine).
 * Đảm bảo bất biến tổng phần chia bằng tổng hóa đơn đến từng đồng và hỗ trợ tất toán tham lam.
 */
class SplitCalculationEngine {

    fun calculateAndValidateSplit(
        totalAmount: Long,
        mode: EngineSplitMode,
        participants: List<ParticipantInput>,
        validMemberIds: Set<String>? = null
    ): SplitValidationResult = Companion.calculateAndValidateSplit(
        totalAmount = totalAmount,
        mode = mode,
        participants = participants,
        validMemberIds = validMemberIds
    )

    fun computeMemberLedgerBalances(
        memberIds: Collection<String>,
        expenses: List<LedgerExpense>
    ): Map<String, LedgerMemberSummary> = Companion.computeMemberLedgerBalances(
        memberIds = memberIds,
        expenses = expenses
    )

    fun computeMemberLedgerBalances(
        memberIds: Collection<String>,
        expenses: List<LedgerExpense>,
        confirmedSettlements: List<LedgerSettlement>
    ): Map<String, LedgerMemberSummary> = Companion.computeMemberLedgerBalances(
        memberIds = memberIds,
        expenses = expenses,
        confirmedSettlements = confirmedSettlements
    )

    fun computeGreedySettlements(
        netBalances: Map<String, Long>
    ): List<DebtTransfer> = Companion.computeGreedySettlements(
        netBalances = netBalances
    )

    fun evaluateBudgetHealth(
        limitAmount: Long,
        spentAmount: Long
    ): BudgetHealthLevel = Companion.evaluateBudgetHealth(
        limitAmount = limitAmount,
        spentAmount = spentAmount
    )

    fun computeBudgetSummary(
        budget: SpendlyBudget,
        activeExpenses: List<SpendlyExpense>
    ): BudgetStatusSummary = Companion.computeBudgetSummary(
        budget = budget,
        activeExpenses = activeExpenses
    )

    companion object {
        private val MIN_PERCENT_SUM = BigDecimal("99.99")
        private val MAX_PERCENT_SUM = BigDecimal("100.01")
        private val FIFTY = BigDecimal(50)
        private val EIGHTY = BigDecimal(80)
        private val HUNDRED = BigDecimal(100)

        private data class BalanceNode(
            val userId: String,
            val amount: Long
        )

        /**
         * Kiểm tra tính hợp lệ và tính toán phần tiền phải trả cho từng thành viên tham gia.
         */
        fun calculateAndValidateSplit(
            totalAmount: Long,
            mode: EngineSplitMode,
            participants: List<ParticipantInput>,
            validMemberIds: Set<String>? = null
        ): SplitValidationResult {
            if (totalAmount <= 0L) {
                return SplitValidationResult.Invalid("Tổng số tiền phải lớn hơn 0 đ")
            }

            val included = participants.filter { it.isIncluded }
            if (included.isEmpty()) {
                return SplitValidationResult.Invalid("Vui lòng chọn ít nhất 1 thành viên tham gia chia tiền")
            }

            if (included.any { it.userId.isBlank() }) {
                return SplitValidationResult.Invalid("Mã thành viên tham gia không hợp lệ")
            }

            if (included.distinctBy { it.userId }.size != included.size) {
                return SplitValidationResult.Invalid("Danh sách thành viên tham gia bị trùng lặp")
            }

            if (validMemberIds != null) {
                val invalidMembers = included.filter { it.userId !in validMemberIds }
                if (invalidMembers.isNotEmpty()) {
                    val names = invalidMembers.joinToString(", ") {
                        it.displayName.ifBlank { it.userId }
                    }
                    return SplitValidationResult.Invalid("Thành viên không thuộc nhóm hiện tại: $names")
                }
            }

            if (included.any { !it.percentage.isFinite() || it.percentage < 0.0 }) {
                return SplitValidationResult.Invalid("Tỷ lệ phần trăm không được âm hoặc không hợp lệ")
            }

            if (included.any { it.customAmount < 0L }) {
                return SplitValidationResult.Invalid("Số tiền chia chi tiết không được âm")
            }

            return when (mode) {
                EngineSplitMode.EQUAL -> computeEqualSplit(totalAmount, included)
                EngineSplitMode.PERCENTAGE -> computePercentageSplit(totalAmount, included)
                EngineSplitMode.CUSTOM -> computeCustomSplit(totalAmount, included)
            }
        }

        /**
         * Tính tổng đã chi, tổng phần phải chịu và số dư ròng cho toàn bộ thành viên từ sổ cái.
         * - Bỏ qua các khoản chi đã hủy (isActive == false).
         * - Hỗ trợ người trả hộ không nằm trong danh sách chia tiền (totalShare = 0).
         * - Giữ nguyên lịch sử công nợ của thành viên đã rời nhóm nếu vẫn còn trong khoản chi active.
         */
        fun computeMemberLedgerBalances(
            memberIds: Collection<String>,
            expenses: List<LedgerExpense>
        ): Map<String, LedgerMemberSummary> = computeMemberLedgerBalances(
            memberIds = memberIds,
            expenses = expenses,
            confirmedSettlements = emptyList()
        )

        /**
         * Tính số dư sổ cái có xét thêm các giao dịch quyết toán đã được xác nhận ([LedgerSettlement.isConfirmed] == true).
         * Bất biến: với mọi giao dịch xác nhận hợp lệ, `fromUserId` cộng [amount] vào `totalPaid`
         * và `toUserId` cộng [amount] vào `totalShare`, giữ `sum(netBalance) == 0`.
         */
        fun computeMemberLedgerBalances(
            memberIds: Collection<String>,
            expenses: List<LedgerExpense>,
            confirmedSettlements: List<LedgerSettlement>
        ): Map<String, LedgerMemberSummary> {
            val paidMap = linkedMapOf<String, Long>()
            val shareMap = linkedMapOf<String, Long>()

            memberIds.forEach { memberId ->
                paidMap.putIfAbsent(memberId, 0L)
                shareMap.putIfAbsent(memberId, 0L)
            }

            expenses.forEach { expense ->
                if (!expense.isActive) return@forEach

                paidMap[expense.payerId] = (paidMap[expense.payerId] ?: 0L) + expense.amount
                shareMap.putIfAbsent(expense.payerId, 0L)

                expense.shares.forEach { (participantId, shareAmount) ->
                    shareMap[participantId] = (shareMap[participantId] ?: 0L) + shareAmount
                    paidMap.putIfAbsent(participantId, 0L)
                }
            }

            confirmedSettlements.forEach { settlement ->
                if (!settlement.isConfirmed || settlement.amount <= 0L) return@forEach
                if (settlement.fromUserId.isBlank() || settlement.toUserId.isBlank()) return@forEach
                if (settlement.fromUserId == settlement.toUserId) return@forEach

                paidMap[settlement.fromUserId] =
                    (paidMap[settlement.fromUserId] ?: 0L) + settlement.amount
                shareMap.putIfAbsent(settlement.fromUserId, 0L)

                shareMap[settlement.toUserId] =
                    (shareMap[settlement.toUserId] ?: 0L) + settlement.amount
                paidMap.putIfAbsent(settlement.toUserId, 0L)
            }

            val summaries = linkedMapOf<String, LedgerMemberSummary>()
            paidMap.keys.forEach { userId ->
                val totalPaid = paidMap[userId] ?: 0L
                val totalShare = shareMap[userId] ?: 0L
                summaries[userId] = LedgerMemberSummary(
                    userId = userId,
                    totalPaid = totalPaid,
                    totalShare = totalShare,
                    netBalance = totalPaid - totalShare
                )
            }
            return summaries
        }

        /**
         * Đánh giá mức độ sức khỏe ngân sách 4 cấp độ:
         * - <= 50% -> NORMAL
         * - > 50% && <= 80% -> WATCH
         * - > 80% && <= 100% -> WARNING
         * - > 100% -> EXCEEDED
         */
        fun evaluateBudgetHealth(
            limitAmount: Long,
            spentAmount: Long
        ): BudgetHealthLevel {
            if (spentAmount <= 0L) return BudgetHealthLevel.NORMAL
            if (limitAmount <= 0L) return BudgetHealthLevel.EXCEEDED

            val scaledSpent = BigDecimal.valueOf(spentAmount).multiply(HUNDRED)
            val limitBd = BigDecimal.valueOf(limitAmount)
            return when {
                scaledSpent <= limitBd.multiply(FIFTY) -> BudgetHealthLevel.NORMAL
                scaledSpent <= limitBd.multiply(EIGHTY) -> BudgetHealthLevel.WATCH
                scaledSpent <= limitBd.multiply(HUNDRED) -> BudgetHealthLevel.WARNING
                else -> BudgetHealthLevel.EXCEEDED
            }
        }

        /**
         * Tổng hợp tiến độ sử dụng và mức cảnh báo của một ngân sách dựa trên các khoản chi active.
         * Nếu [SpendlyBudget.category] != null thì chỉ tính các khoản chi thuộc danh mục đó.
         */
        fun computeBudgetSummary(
            budget: SpendlyBudget,
            activeExpenses: List<SpendlyExpense>
        ): BudgetStatusSummary {
            val spentAmount = activeExpenses
                .asSequence()
                .filter { it.status == RecordStatus.ACTIVE }
                .filter { budget.groupId.isBlank() || it.groupId.isBlank() || it.groupId == budget.groupId }
                .filter { budget.category == null || it.category == budget.category }
                .filter { expense ->
                    if (budget.startDateMillis <= 0L && budget.endDateMillis <= 0L) {
                        true
                    } else {
                        val effectiveDate = if (expense.expenseDateMillis > 0L) {
                            expense.expenseDateMillis
                        } else {
                            expense.createdAtMillis
                        }
                        val afterStart = budget.startDateMillis <= 0L || effectiveDate >= budget.startDateMillis
                        val beforeEnd = budget.endDateMillis <= 0L || effectiveDate <= budget.endDateMillis
                        afterStart && beforeEnd
                    }
                }
                .sumOf { it.amount }

            val remainingAmount = budget.limitAmount - spentAmount
            val usagePercent = when {
                budget.limitAmount > 0L ->
                    (spentAmount.toDouble() * 100.0 / budget.limitAmount.toDouble()).toFloat()
                spentAmount > 0L -> 100f
                else -> 0f
            }
            val level = evaluateBudgetHealth(
                limitAmount = budget.limitAmount,
                spentAmount = spentAmount
            )

            return BudgetStatusSummary(
                budget = budget,
                spentAmount = spentAmount,
                remainingAmount = remainingAmount,
                usagePercent = usagePercent,
                level = level
            )
        }

        /**
         * Tối ưu hóa dòng tiền theo thuật toán tham lam:
         * Ghép người nợ nhiều nhất với người được nhận nhiều nhất cho đến khi tất toán hết.
         */
        fun computeGreedySettlements(netBalances: Map<String, Long>): List<DebtTransfer> {
            val nodeComparator = compareByDescending<BalanceNode> { it.amount }
                .thenBy { it.userId }
            val creditors = PriorityQueue(nodeComparator)
            val debtors = PriorityQueue(nodeComparator)

            netBalances.forEach { (userId, balance) ->
                when {
                    balance > 0L -> creditors.add(BalanceNode(userId = userId, amount = balance))
                    balance < 0L -> debtors.add(BalanceNode(userId = userId, amount = -balance))
                }
            }

            val transfers = mutableListOf<DebtTransfer>()
            while (creditors.isNotEmpty() && debtors.isNotEmpty()) {
                val creditor = creditors.poll()!!
                val debtor = debtors.poll()!!
                val settleAmount = minOf(creditor.amount, debtor.amount)

                if (settleAmount > 0L) {
                    transfers.add(
                        DebtTransfer(
                            fromUserId = debtor.userId,
                            toUserId = creditor.userId,
                            amount = settleAmount
                        )
                    )
                }

                val remainingCredit = creditor.amount - settleAmount
                val remainingDebt = debtor.amount - settleAmount

                if (remainingCredit > 0L) {
                    creditors.add(BalanceNode(userId = creditor.userId, amount = remainingCredit))
                }
                if (remainingDebt > 0L) {
                    debtors.add(BalanceNode(userId = debtor.userId, amount = remainingDebt))
                }
            }

            return transfers
        }

        private fun computeEqualSplit(
            totalAmount: Long,
            included: List<ParticipantInput>
        ): SplitValidationResult {
            val amounts = FintechAlgorithm.calculateEqualSplit(totalAmount, included.size)
            val shares = included.mapIndexed { index, participant ->
                val shareAmount = amounts[index]
                ComputedShare(
                    userId = participant.userId,
                    displayName = participant.displayName,
                    shareAmount = shareAmount,
                    percentage = roundPercentage(shareAmount, totalAmount)
                )
            }
            return SplitValidationResult.Valid(shares)
        }

        private fun computePercentageSplit(
            totalAmount: Long,
            included: List<ParticipantInput>
        ): SplitValidationResult {
            val pctSum = included.fold(BigDecimal.ZERO) { acc, item ->
                acc.add(BigDecimal.valueOf(item.percentage))
            }
            if (pctSum < MIN_PERCENT_SUM || pctSum > MAX_PERCENT_SUM) {
                val displaySum = pctSum.stripTrailingZeros().toPlainString()
                return SplitValidationResult.Invalid(
                    "Tổng tỷ lệ phần trăm phải nằm trong khoảng 99.99% - 100.01% (hiện tại: $displaySum%)"
                )
            }

            val amounts = FintechAlgorithm.calculatePercentageSplit(
                totalAmount = totalAmount,
                percentages = included.map { it.percentage }
            )
            val shares = included.mapIndexed { index, participant ->
                ComputedShare(
                    userId = participant.userId,
                    displayName = participant.displayName,
                    shareAmount = amounts[index],
                    percentage = participant.percentage
                )
            }
            return SplitValidationResult.Valid(shares)
        }

        private fun computeCustomSplit(
            totalAmount: Long,
            included: List<ParticipantInput>
        ): SplitValidationResult {
            val customSum = included.fold(BigDecimal.ZERO) { acc, item ->
                acc.add(BigDecimal.valueOf(item.customAmount))
            }
            val expectedTotal = BigDecimal.valueOf(totalAmount)
            if (customSum.compareTo(expectedTotal) != 0) {
                return SplitValidationResult.Invalid(
                    "Tổng số tiền chia chi tiết (${customSum.toPlainString()} đ) phải bằng tổng hóa đơn ($totalAmount đ)"
                )
            }

            val shares = included.map { participant ->
                ComputedShare(
                    userId = participant.userId,
                    displayName = participant.displayName,
                    shareAmount = participant.customAmount,
                    percentage = roundPercentage(participant.customAmount, totalAmount)
                )
            }
            return SplitValidationResult.Valid(shares)
        }

        private fun roundPercentage(shareAmount: Long, totalAmount: Long): Double {
            if (totalAmount <= 0L) return 0.0
            return BigDecimal.valueOf(shareAmount)
                .multiply(HUNDRED)
                .divide(BigDecimal.valueOf(totalAmount), 2, RoundingMode.HALF_UP)
                .toDouble()
        }
    }
}
