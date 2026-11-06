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
     * Định dạng tiền tệ rút gọn cho biểu đồ & KPI (ví dụ: 1.800.000 -> 1,8 tr, 450.000 -> 450k)
     */
    fun formatShortVnd(amount: Long): String {
        return when {
            amount >= 1_000_000_000L -> {
                val b = amount.toDouble() / 1_000_000_000.0
                String.format(java.util.Locale.US, "%.1f tỷ", b).replace('.', ',')
            }
            amount >= 1_000_000L -> {
                val m = amount.toDouble() / 1_000_000.0
                if (amount % 1_000_000L == 0L) "${amount / 1_000_000L} tr"
                else String.format(java.util.Locale.US, "%.1f tr", m).replace('.', ',')
            }
            amount >= 1_000L -> {
                val k = amount.toDouble() / 1_000.0
                if (amount % 1_000L == 0L) "${amount / 1_000L}k"
                else String.format(java.util.Locale.US, "%.0fk", k)
            }
            else -> "$amount đ"
        }
    }

    /**
     * Lấy ngày hôm nay định dạng tiếng Việt chuẩn: "dd tháng MM, yyyy"
     */
    fun getTodayFormatted(): String {
        val sdf = java.text.SimpleDateFormat("dd 'tháng' MM, yyyy", java.util.Locale.forLanguageTag("vi-VN"))
        return sdf.format(java.util.Date())
    }

    /**
     * Đọc số tiền bằng chữ tiếng Việt (Vietnamese Number to Words)
     * Ví dụ: 850000 -> "Tám trăm năm mươi nghìn đồng"
     */
    fun convertNumberToWords(amount: Long): String {
        if (amount <= 0L) return "Không đồng"

        val units = arrayOf("", "nghìn", "triệu", "tỷ")
        val digits = arrayOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")

        fun readThreeDigits(n: Int, isHighest: Boolean): String {
            val h = n / 100
            val t = (n % 100) / 10
            val u = n % 10
            if (h == 0 && t == 0 && u == 0) return ""

            val sb = StringBuilder()
            if (!isHighest || h > 0) {
                sb.append("${digits[h]} trăm ")
            }

            when (t) {
                0 -> {
                    if (u > 0) {
                        if (!isHighest || h > 0) sb.append("lẻ ")
                        sb.append("${digits[u]} ")
                    }
                }
                1 -> {
                    sb.append("mười ")
                    when (u) {
                        0 -> {}
                        5 -> sb.append("lăm ")
                        else -> sb.append("${digits[u]} ")
                    }
                }
                else -> {
                    sb.append("${digits[t]} mươi ")
                    when (u) {
                        0 -> {}
                        1 -> sb.append("mốt ")
                        4 -> sb.append("tư ")
                        5 -> sb.append("lăm ")
                        else -> sb.append("${digits[u]} ")
                    }
                }
            }
            return sb.toString().trim()
        }

        var temp = amount
        val groups = mutableListOf<Int>()
        while (temp > 0) {
            groups.add((temp % 1000).toInt())
            temp /= 1000
        }

        val resultWords = mutableListOf<String>()
        for (i in groups.indices.reversed()) {
            val n = groups[i]
            if (n > 0) {
                val groupText = readThreeDigits(n, i == groups.size - 1)
                val unit = units.getOrElse(i) { "" }
                val part = if (unit.isNotEmpty()) "$groupText $unit" else groupText
                resultWords.add(part)
            }
        }

        val fullText = resultWords.joinToString(" ").trim()
        val capitalized = fullText.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        return "$capitalized đồng"
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
