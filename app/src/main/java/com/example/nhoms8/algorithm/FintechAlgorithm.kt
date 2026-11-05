package com.example.nhoms8.algorithm

import androidx.compose.ui.graphics.Color
import com.example.nhoms8.DebtItem
import com.example.nhoms8.GroupMember
import com.example.nhoms8.MemberBalanceStatus
import com.example.nhoms8.RecentTransaction
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.min

/**
 * Thuật toán Tài chính & Quyết toán Công nợ (Fintech Algorithm)
 * Được thiết kế bởi Kỹ sư Thuật toán Tài chính (fintech-algorithm-engineer) - Nhóm 08
 */
object FintechAlgorithm {

    private val numberFormatter = DecimalFormat("#,###")

    /**
     * Định dạng tiền tệ chuẩn Việt Nam (VND), ví dụ: 850.000 đ
     */
    fun formatVnd(amount: Long, withSymbol: Boolean = true): String {
        val formatted = numberFormatter.format(amount).replace(',', '.')
        return if (withSymbol) "$formatted đ" else formatted
    }

    /**
     * Thuật toán Chia đều tiền (Equal Split)
     * Đảm bảo tính toán chính xác tới từng đồng, xử lý trường hợp chia có số dư lẻ:
     * Ví dụ: 100.000 đ chia cho 3 người -> [33.334 đ, 33.333 đ, 33.333 đ]
     */
    fun calculateEqualSplit(totalAmount: Long, participantCount: Int): List<Long> {
        if (participantCount <= 0) return emptyList()
        val baseAmount = totalAmount / participantCount
        val remainder = (totalAmount % participantCount).toInt()

        return List(participantCount) { index ->
            if (index < remainder) baseAmount + 1L else baseAmount
        }
    }

    /**
     * Thuật toán Chia tiền theo Tỷ lệ % (Ratio / Percent Split)
     * Trả về danh sách số tiền đã làm tròn đảm bảo tổng luôn bằng totalAmount.
     */
    fun calculateRatioSplit(totalAmount: Long, ratios: List<Float>): List<Long> {
        if (ratios.isEmpty()) return emptyList()
        val totalRatio = ratios.sum()
        if (totalRatio <= 0f) return calculateEqualSplit(totalAmount, ratios.size)

        val splitAmounts = ratios.map { ratio ->
            (totalAmount * (ratio / totalRatio)).toLong()
        }.toMutableList()

        val diff = totalAmount - splitAmounts.sum()
        if (splitAmounts.isNotEmpty() && diff != 0L) {
            splitAmounts[0] += diff
        }
        return splitAmounts
    }

    /**
     * Thuật toán Tối ưu hóa Dòng tiền & Bù trừ Công nợ (Min Cash Flow Algorithm)
     * Ứng dụng đồ thị bù trừ nợ rút gọn:
     * - Tính số dư ròng (Net Balance) của từng thành viên:
     *   netBalance = Tổng tiền đã chi trả hộ - Tổng tiền phải gánh
     * - Phân loại 2 nhóm: Người nhận tiền (Creditor > 0) và Người nợ tiền (Debtor < 0)
     * - Khớp cặp bù trừ liên tục để giảm thiểu tối đa số lần chuyển khoản giữa các thành viên.
     */
    fun simplifyDebts(
        members: List<GroupMember>,
        netBalances: Map<String, Long>
    ): List<DebtItem> {
        val memberMap = members.associateBy { it.id }

        // Danh sách người cho vay (creditors) và người nợ (debtors)
        val creditors = mutableListOf<Pair<String, Long>>()
        val debtors = mutableListOf<Pair<String, Long>>()

        netBalances.forEach { (memberId, balance) ->
            if (balance > 0L) {
                creditors.add(memberId to balance)
            } else if (balance < 0L) {
                debtors.add(memberId to -balance) // lưu giá trị dương cho dễ tính
            }
        }

        // Sắp xếp giảm dần theo số tiền để ưu tiên thanh toán khoản lớn trước
        creditors.sortByDescending { it.second }
        debtors.sortByDescending { it.second }

        val simplifiedDebts = mutableListOf<DebtItem>()
        var debtCounter = 1

        var cIndex = 0
        var dIndex = 0

        val currentCreditors = creditors.map { it.first to it.second }.toMutableList()
        val currentDebtors = debtors.map { it.first to it.second }.toMutableList()

        while (cIndex < currentCreditors.size && dIndex < currentDebtors.size) {
            val (creditorId, creditAmt) = currentCreditors[cIndex]
            val (debtorId, debtAmt) = currentDebtors[dIndex]

            val settleAmount = min(creditAmt, debtAmt)

            if (settleAmount > 0L) {
                val creditorMember = memberMap[creditorId]
                val debtorMember = memberMap[debtorId]

                if (creditorMember != null && debtorMember != null) {
                    simplifiedDebts.add(
                        DebtItem(
                            id = "debt_auto_${debtCounter++}",
                            debtorName = debtorMember.name,
                            debtorInitials = debtorMember.initials,
                            debtorAvatarBg = debtorMember.avatarBgColor,
                            creditorName = creditorMember.name,
                            creditorInitials = creditorMember.initials,
                            creditorAvatarBg = creditorMember.avatarBgColor,
                            rawAmount = settleAmount,
                            amountFormatted = formatVnd(settleAmount),
                            statusText = "Chờ chuyển khoản",
                            isPaid = false
                        )
                    )
                }
            }

            val remainingCredit = creditAmt - settleAmount
            val remainingDebt = debtAmt - settleAmount

            if (remainingCredit <= 0L) {
                cIndex++
            } else {
                currentCreditors[cIndex] = creditorId to remainingCredit
            }

            if (remainingDebt <= 0L) {
                dIndex++
            } else {
                currentDebtors[dIndex] = debtorId to remainingDebt
            }
        }

        return simplifiedDebts
    }
}
