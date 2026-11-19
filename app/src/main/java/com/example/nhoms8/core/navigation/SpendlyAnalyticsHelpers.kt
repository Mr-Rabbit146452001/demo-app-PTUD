package com.example.nhoms8.core.navigation

import com.example.nhoms8.HistoryItemType
import com.example.nhoms8.HistoryTransactionItem
import com.example.nhoms8.TransactionDateGroup
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.usecase.DebtTransfer
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Bộ hàm tiện ích phân tích tài chính, gợi ý AI, bộ lọc lịch sử giao dịch và xu hướng báo cáo
 * thuần JVM (Pure JVM testable) cho Spendly Phase 4-5.
 */
object SpendlyAnalyticsHelpers {

    private data class FilteredLedgerEntry(
        val dateHeader: String,
        val sortTimestamp: Long,
        val signedAmount: Long,
        val item: HistoryTransactionItem
    )

    /**
     * Tính tổng số tiền người dùng hiện tại đang nợ (`youOweTotal`) và tổng số tiền
     * người dùng hiện tại được hoàn trả (`youAreOwedTotal`) từ danh sách chuyển khoản tối ưu.
     */
    fun computePersonalDebtSummary(
        transfers: List<DebtTransfer>,
        currentUserId: String
    ): Pair<Long, Long> {
        val uid = currentUserId.trim()
        if (uid.isEmpty()) return 0L to 0L

        val youOweTotal = transfers
            .asSequence()
            .filter { it.fromUserId == uid && it.amount > 0L }
            .sumOf { it.amount }

        val youAreOwedTotal = transfers
            .asSequence()
            .filter { it.toUserId == uid && it.amount > 0L }
            .sumOf { it.amount }

        return youOweTotal to youAreOwedTotal
    }

    /**
     * Tạo lời khuyên tiếng Việt theo ngữ cảnh 4 vùng ngân sách (0-50%, 50-80%, 80-100%, >100%)
     * kết hợp nhắc nợ cá nhân trên màn hình Trang chủ V2.
     */
    fun buildHomeAiSuggestion(
        budgetRatio: Float,
        youOwe: Long,
        youAreOwed: Long,
        groupName: String
    ): String {
        val safeGroupName = groupName.trim().ifBlank { "Quỹ nhóm" }
        val percent = (budgetRatio.coerceAtLeast(0f) * 100f).roundToInt()

        val budgetAdvice = when {
            budgetRatio > 1.0f ->
                "Vượt ngân sách ($percent%) tại $safeGroupName! Hãy ưu tiên chi tiêu thiết yếu hoặc điều chỉnh hạn mức tháng."
            budgetRatio >= 0.80f ->
                "Cảnh báo ngân sách $safeGroupName đã chạm mức $percent% (80-100%). Nên cân nhắc kỹ các khoản chi mới."
            budgetRatio >= 0.50f ->
                "Mức theo dõi ($percent%): $safeGroupName đã dùng hơn nửa ngân sách tháng (50-80%), hãy giữ nhịp chi tiêu hợp lý."
            else ->
                "Bình thường ($percent%): Ngân sách $safeGroupName đang ở vùng an toàn (0-50%). Tiếp tục duy trì thói quen tốt nhé!"
        }

        val debtReminder = when {
            youOwe > 0L && youAreOwed > 0L ->
                " Bạn đang nợ ${FintechAlgorithm.formatVnd(youOwe)} và được trả ${FintechAlgorithm.formatVnd(youAreOwed)} — mở Đối soát để bù trừ."
            youOwe > 0L ->
                " Nhắc nợ: Bạn đang cần thanh toán ${FintechAlgorithm.formatVnd(youOwe)} trong mục Đối soát."
            youAreOwed > 0L ->
                " Bạn đang được hoàn trả ${FintechAlgorithm.formatVnd(youAreOwed)} từ các thành viên."
            else -> ""
        }

        return budgetAdvice + debtReminder
    }

    /**
     * Lọc và gom nhóm lịch sử giao dịch (khoản chi + khoản thu đang hoạt động) theo:
     * - [selectedScope]: `"Tất cả"`, `"Nhóm"`, hoặc `"Cá nhân"` (lọc `payerId == currentUserId`)
     * - [selectedMemberId]: ID hoặc tên hiển thị của thành viên
     * - [selectedCategoryLabel]: 9 nhãn `SpendlyCategory.label` hoặc `"Thu quỹ"`
     * - [selectedMonthLabel]: `"Tháng MM/yyyy"` hoặc `"Tất cả"`
     * - [searchQuery]: khớp theo tiêu đề hoặc tên người chi/nạp
     */
    fun filterHistoryTransactions(
        expenses: List<SpendlyExpense>,
        incomes: List<SpendlyIncome>,
        memberNamesById: Map<String, String>,
        currentUserId: String,
        searchQuery: String,
        selectedScope: String,
        selectedMemberId: String?,
        selectedCategoryLabel: String?,
        selectedMonthLabel: String?
    ): List<TransactionDateGroup> {
        val trimmedQuery = searchQuery.trim()
        val normalizedScope = selectedScope.trim()
        val isPersonalScope = normalizedScope.equals("Cá nhân", ignoreCase = true)
        val normalizedUid = currentUserId.trim()
        val normalizedMemberFilter = selectedMemberId?.trim()?.takeIf {
            it.isNotEmpty() && !it.equals("Tất cả", ignoreCase = true)
        }
        val normalizedCategoryFilter = selectedCategoryLabel?.trim()?.takeIf {
            it.isNotEmpty() && !it.equals("Tất cả", ignoreCase = true)
        }
        val monthYearFilter = parseMonthYearLabel(selectedMonthLabel)
        val isIncomeCategoryFilter = normalizedCategoryFilter != null && (
            normalizedCategoryFilter.equals("Thu quỹ", ignoreCase = true) ||
                normalizedCategoryFilter.equals("Nạp quỹ", ignoreCase = true) ||
                normalizedCategoryFilter.equals("Khoản thu", ignoreCase = true)
            )
        val scopeTag = if (isPersonalScope) " · Cá nhân" else ""

        val filteredExpenses = expenses
            .asSequence()
            .filter { it.status == RecordStatus.ACTIVE }
            .filter { exp ->
                !isPersonalScope || (normalizedUid.isNotEmpty() && exp.payerId == normalizedUid)
            }
            .filter { exp ->
                val resolvedPayer = resolvePayerName(exp.payerId, exp.payerName, memberNamesById)
                matchesMemberFilter(exp.payerId, resolvedPayer, normalizedMemberFilter, memberNamesById)
            }
            .filter { exp ->
                when {
                    normalizedCategoryFilter == null -> true
                    isIncomeCategoryFilter -> false
                    normalizedCategoryFilter.equals("Đi lại", ignoreCase = true) ->
                        exp.category == SpendlyCategory.TRANSPORT
                    else -> exp.category.label.equals(normalizedCategoryFilter, ignoreCase = true)
                }
            }
            .filter { exp ->
                val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
                matchesMonthYear(ts, monthYearFilter)
            }
            .filter { exp ->
                if (trimmedQuery.isEmpty()) {
                    true
                } else {
                    val resolvedPayer = resolvePayerName(exp.payerId, exp.payerName, memberNamesById)
                    exp.title.contains(trimmedQuery, ignoreCase = true) ||
                        resolvedPayer.contains(trimmedQuery, ignoreCase = true)
                }
            }
            .map { exp ->
                val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
                val payer = resolvePayerName(exp.payerId, exp.payerName, memberNamesById)
                val header = formatFullRelativeDate(ts)
                FilteredLedgerEntry(
                    dateHeader = header,
                    sortTimestamp = ts,
                    signedAmount = -exp.amount,
                    item = HistoryTransactionItem(
                        id = exp.expenseId,
                        title = exp.title,
                        subtitle = "${formatShortRelativeDate(ts)} · ${exp.category.label} · $payer đã trả$scopeTag",
                        amountFormatted = "-${FintechAlgorithm.formatVnd(exp.amount)}",
                        isIncome = false,
                        type = when (exp.category) {
                            SpendlyCategory.HOUSING,
                            SpendlyCategory.UTILITIES -> HistoryItemType.INTERNET
                            SpendlyCategory.TRANSPORT -> HistoryItemType.TAXI
                            SpendlyCategory.FOOD -> HistoryItemType.COFFEE
                            else -> HistoryItemType.GROCERY
                        }
                    )
                )
            }
            .toList()

        val filteredIncomes = incomes
            .asSequence()
            .filter { it.status == RecordStatus.ACTIVE }
            .filter { inc ->
                !isPersonalScope || (normalizedUid.isNotEmpty() && inc.payerId == normalizedUid)
            }
            .filter { inc ->
                val resolvedPayer = resolvePayerName(inc.payerId, inc.payerName, memberNamesById)
                matchesMemberFilter(inc.payerId, resolvedPayer, normalizedMemberFilter, memberNamesById)
            }
            .filter {
                normalizedCategoryFilter == null || isIncomeCategoryFilter
            }
            .filter { inc ->
                val ts = inc.incomeDateMillis.takeIf { it > 0L } ?: inc.createdAtMillis
                matchesMonthYear(ts, monthYearFilter)
            }
            .filter { inc ->
                if (trimmedQuery.isEmpty()) {
                    true
                } else {
                    val resolvedPayer = resolvePayerName(inc.payerId, inc.payerName, memberNamesById)
                    inc.title.contains(trimmedQuery, ignoreCase = true) ||
                        resolvedPayer.contains(trimmedQuery, ignoreCase = true)
                }
            }
            .map { inc ->
                val ts = inc.incomeDateMillis.takeIf { it > 0L } ?: inc.createdAtMillis
                val payer = resolvePayerName(inc.payerId, inc.payerName, memberNamesById)
                val header = formatFullRelativeDate(ts)
                FilteredLedgerEntry(
                    dateHeader = header,
                    sortTimestamp = ts,
                    signedAmount = inc.amount,
                    item = HistoryTransactionItem(
                        id = inc.incomeId,
                        title = inc.title,
                        subtitle = "${formatShortRelativeDate(ts)} · Thu quỹ · $payer nạp quỹ$scopeTag",
                        amountFormatted = "+${FintechAlgorithm.formatVnd(inc.amount)}",
                        isIncome = true,
                        type = HistoryItemType.DEPOSIT
                    )
                )
            }
            .toList()

        return (filteredExpenses + filteredIncomes)
            .sortedByDescending { it.sortTimestamp }
            .groupBy { it.dateHeader }
            .map { (header, entries) ->
                val netTotal = entries.sumOf { it.signedAmount }
                val sign = if (netTotal >= 0L) "+" else ""
                TransactionDateGroup(
                    dateHeader = header,
                    netTotalFormatted = "$sign${FintechAlgorithm.formatVnd(netTotal)}",
                    transactions = entries.map { it.item }
                )
            }
    }

    /**
     * Gom nhóm các khoản chi đang hoạt động trong [monthCount] tháng gần nhất tính tới [referenceTimeMillis]
     * thành danh sách điểm xu hướng `("T$month" to totalAmount)` theo thứ tự thời gian tăng dần.
     */
    fun computeMonthlyTrendPoints(
        expenses: List<SpendlyExpense>,
        referenceTimeMillis: Long = System.currentTimeMillis(),
        monthCount: Int = 5
    ): List<Pair<String, Long>> {
        if (monthCount <= 0) return emptyList()
        val effectiveRef = if (referenceTimeMillis > 0L) referenceTimeMillis else System.currentTimeMillis()
        val activeExpenses = expenses.filter { it.status == RecordStatus.ACTIVE }

        val amountByYearMonth = mutableMapOf<Pair<Int, Int>, Long>()
        activeExpenses.forEach { exp ->
            val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
            val effectiveTs = if (ts > 0L) ts else effectiveRef
            val cal = Calendar.getInstance().apply { timeInMillis = effectiveTs }
            val key = cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1)
            amountByYearMonth[key] = (amountByYearMonth[key] ?: 0L) + exp.amount
        }

        val points = ArrayList<Pair<String, Long>>(monthCount)
        for (offset in -(monthCount - 1)..0) {
            val cal = Calendar.getInstance().apply {
                timeInMillis = effectiveRef
                set(Calendar.DAY_OF_MONTH, 1)
                add(Calendar.MONTH, offset)
            }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val total = amountByYearMonth[year to month] ?: 0L
            points.add("T$month" to total)
        }
        return points
    }

    /**
     * Tính tỷ lệ phần trăm thay đổi chi tiêu của tháng [targetMonthLabel] so với tháng liền trước.
     * Trả về `null` nếu tổng chi tiêu của tháng liền trước bằng `0L`.
     */
    fun computeMonthOverMonthPercent(
        expenses: List<SpendlyExpense>,
        targetMonthLabel: String?
    ): Float? {
        val activeExpenses = expenses.filter { it.status == RecordStatus.ACTIVE }
        val parsed = parseMonthYearLabel(targetMonthLabel)

        val latestExpenseTs = activeExpenses
            .maxOfOrNull { exp -> exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis }
            ?.takeIf { it > 0L }
            ?: System.currentTimeMillis()
        val fallbackCal = Calendar.getInstance().apply { timeInMillis = latestExpenseTs }

        val targetMonth = parsed?.first ?: (fallbackCal.get(Calendar.MONTH) + 1)
        val targetYear = parsed?.second ?: fallbackCal.get(Calendar.YEAR)

        val (prevMonth, prevYear) = if (targetMonth == 1) {
            12 to (targetYear - 1)
        } else {
            (targetMonth - 1) to targetYear
        }

        var targetTotal = 0L
        var prevTotal = 0L
        activeExpenses.forEach { exp ->
            val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
            if (ts > 0L) {
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                val m = cal.get(Calendar.MONTH) + 1
                val y = cal.get(Calendar.YEAR)
                if (m == targetMonth && y == targetYear) {
                    targetTotal += exp.amount
                } else if (m == prevMonth && y == prevYear) {
                    prevTotal += exp.amount
                }
            }
        }

        if (prevTotal <= 0L) return null
        return ((targetTotal - prevTotal).toFloat() / prevTotal.toFloat()) * 100f
    }

    /**
     * Trích xuất danh sách nhãn tháng khả dụng (`"Tháng MM/yyyy"`) từ dữ liệu sổ cái kèm `"Tất cả"`.
     */
    fun extractAvailableMonths(
        expenses: List<SpendlyExpense>,
        incomes: List<SpendlyIncome>,
        currentMonthYearLabel: String
    ): List<String> {
        val yearMonths = sortedSetOf(
            compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second }
        )

        parseMonthYearLabel(currentMonthYearLabel)?.let { (m, y) ->
            if (y != null) yearMonths.add(y to m)
        }

        val nowCal = Calendar.getInstance()
        val nowYear = nowCal.get(Calendar.YEAR)
        val nowMonth = nowCal.get(Calendar.MONTH) + 1
        yearMonths.add(nowYear to nowMonth)
        val prevPair = if (nowMonth == 1) (nowYear - 1) to 12 else nowYear to (nowMonth - 1)
        yearMonths.add(prevPair)

        expenses.filter { it.status == RecordStatus.ACTIVE }.forEach { exp ->
            val ts = exp.expenseDateMillis.takeIf { it > 0L } ?: exp.createdAtMillis
            if (ts > 0L) {
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                yearMonths.add(cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1))
            }
        }
        incomes.filter { it.status == RecordStatus.ACTIVE }.forEach { inc ->
            val ts = inc.incomeDateMillis.takeIf { it > 0L } ?: inc.createdAtMillis
            if (ts > 0L) {
                val cal = Calendar.getInstance().apply { timeInMillis = ts }
                yearMonths.add(cal.get(Calendar.YEAR) to (cal.get(Calendar.MONTH) + 1))
            }
        }

        val labels = yearMonths.map { (year, month) -> "Tháng $month/$year" }.toMutableList()
        if ("Tất cả" !in labels) {
            labels.add("Tất cả")
        }
        return labels
    }

    /**
     * Lọc danh sách `TransactionDateGroup` ở chế độ offline / legacy fallback.
     */
    fun filterLegacyHistoryGroups(
        groups: List<TransactionDateGroup>,
        currentUserName: String,
        searchQuery: String,
        selectedScope: String,
        selectedMemberName: String?,
        selectedCategoryLabel: String?,
        selectedMonthLabel: String?,
        currentMonthYearLabel: String
    ): List<TransactionDateGroup> {
        val trimmedQuery = searchQuery.trim()
        val isPersonalScope = selectedScope.trim().equals("Cá nhân", ignoreCase = true)
        val scopeTag = if (isPersonalScope) " · Cá nhân" else ""

        return groups.mapNotNull { group ->
            val monthMatches = if (selectedMonthLabel.isNullOrBlank() ||
                selectedMonthLabel.equals("Tất cả", ignoreCase = true)
            ) {
                true
            } else if (group.dateHeader.equals("Hôm nay", ignoreCase = true) ||
                group.dateHeader.equals("Hôm qua", ignoreCase = true)
            ) {
                selectedMonthLabel.equals(currentMonthYearLabel, ignoreCase = true)
            } else {
                val parsed = parseMonthYearLabel(selectedMonthLabel)
                val monthToken = parsed?.first?.toString()?.padStart(2, '0').orEmpty()
                monthToken.isEmpty() || group.dateHeader.contains("/$monthToken")
            }

            if (!monthMatches) return@mapNotNull null

            val filteredItems = group.transactions.filter { item ->
                val matchesScope = !isPersonalScope ||
                    (currentUserName.isNotBlank() && item.subtitle.contains(currentUserName, ignoreCase = true))
                val matchesMember = selectedMemberName.isNullOrBlank() ||
                    item.subtitle.contains(selectedMemberName, ignoreCase = true)
                val matchesCategory = selectedCategoryLabel.isNullOrBlank() ||
                    selectedCategoryLabel.equals("Tất cả", ignoreCase = true) ||
                    item.subtitle.contains(selectedCategoryLabel, ignoreCase = true) ||
                    (selectedCategoryLabel.equals("Thu quỹ", ignoreCase = true) && item.isIncome)
                val matchesQuery = trimmedQuery.isEmpty() ||
                    item.title.contains(trimmedQuery, ignoreCase = true) ||
                    item.subtitle.contains(trimmedQuery, ignoreCase = true)
                matchesScope && matchesMember && matchesCategory && matchesQuery
            }.map { item ->
                if (isPersonalScope && !item.subtitle.contains("Cá nhân", ignoreCase = true)) {
                    item.copy(subtitle = item.subtitle + scopeTag)
                } else {
                    item
                }
            }

            if (filteredItems.isEmpty()) {
                null
            } else {
                val netTotal = filteredItems.sumOf { tx ->
                    val digits = tx.amountFormatted.filter { it.isDigit() }.toLongOrNull() ?: 0L
                    if (tx.isIncome) digits else -digits
                }
                val sign = if (netTotal >= 0L) "+" else "-"
                group.copy(
                    netTotalFormatted = "$sign${FintechAlgorithm.formatVnd(abs(netTotal))}",
                    transactions = filteredItems
                )
            }
        }
    }

    private fun resolvePayerName(
        payerId: String,
        payerName: String,
        memberNamesById: Map<String, String>
    ): String {
        return payerName.trim().ifBlank {
            memberNamesById[payerId]?.trim().orEmpty()
        }.ifBlank { "Thành viên" }
    }

    private fun matchesMemberFilter(
        payerId: String,
        resolvedPayerName: String,
        selectedMemberId: String?,
        memberNamesById: Map<String, String>
    ): Boolean {
        if (selectedMemberId.isNullOrBlank()) return true
        if (payerId.equals(selectedMemberId, ignoreCase = true)) return true
        if (resolvedPayerName.equals(selectedMemberId, ignoreCase = true)) return true
        if (resolvedPayerName.contains(selectedMemberId, ignoreCase = true)) return true
        val mappedName = memberNamesById[selectedMemberId]?.trim()
        return !mappedName.isNullOrBlank() && resolvedPayerName.equals(mappedName, ignoreCase = true)
    }

    internal fun parseMonthYearLabel(label: String?): Pair<Int, Int?>? {
        val raw = label?.trim().orEmpty()
        if (raw.isEmpty() || raw.equals("Tất cả", ignoreCase = true)) return null
        val numbers = Regex("\\d+").findAll(raw).mapNotNull { it.value.toIntOrNull() }.toList()
        if (numbers.isEmpty()) return null
        val month = numbers.firstOrNull { it in 1..12 } ?: return null
        val year = numbers.firstOrNull { it >= 1970 }
        return month to year
    }

    private fun matchesMonthYear(timestampMillis: Long, filter: Pair<Int, Int?>?): Boolean {
        if (filter == null) return true
        val (targetMonth, targetYear) = filter
        val effectiveTs = if (timestampMillis > 0L) timestampMillis else System.currentTimeMillis()
        val cal = Calendar.getInstance().apply { timeInMillis = effectiveTs }
        val txMonth = cal.get(Calendar.MONTH) + 1
        val txYear = cal.get(Calendar.YEAR)
        return txMonth == targetMonth && (targetYear == null || txYear == targetYear)
    }

    private fun formatShortRelativeDate(millis: Long): String {
        if (millis <= 0L) return "Hôm nay"
        return when {
            isSameDay(millis, 0) -> "Hôm nay"
            isSameDay(millis, -1) -> "Hôm qua"
            else -> SimpleDateFormat("dd/MM", Locale.forLanguageTag("vi-VN")).format(Date(millis))
        }
    }

    private fun formatFullRelativeDate(millis: Long): String {
        if (millis <= 0L) return "Hôm nay"
        return when {
            isSameDay(millis, 0) -> "Hôm nay"
            isSameDay(millis, -1) -> "Hôm qua"
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("vi-VN")).format(Date(millis))
        }
    }

    private fun isSameDay(millis: Long, offsetDaysFromToday: Int): Boolean {
        val target = Calendar.getInstance().apply { timeInMillis = millis }
        val reference = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, offsetDaysFromToday) }
        return target.get(Calendar.YEAR) == reference.get(Calendar.YEAR) &&
            target.get(Calendar.DAY_OF_YEAR) == reference.get(Calendar.DAY_OF_YEAR)
    }
}
