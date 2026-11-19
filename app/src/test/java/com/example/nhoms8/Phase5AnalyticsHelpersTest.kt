package com.example.nhoms8

import com.example.nhoms8.core.navigation.SpendlyAnalyticsHelpers
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.usecase.DebtTransfer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit Tests kiểm thử bộ hàm phân tích tài chính, gợi ý AI, bộ lọc lịch sử giao dịch
 * và xu hướng báo cáo theo tháng (Phase5AnalyticsHelpersTest).
 */
class Phase5AnalyticsHelpersTest {

    private val memberNames = mapOf(
        "user_hung" to "Lê Văn Hùng",
        "user_linh" to "Lý Việt Linh",
        "user_dat" to "Nguyễn Phan Tấn Đạt",
        "user_binh" to "Lê Trung Bình"
    )

    private fun timestampFor(year: Int, month: Int, day: Int, hour: Int = 10): Long {
        return Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    @Test
    fun computePersonalDebtSummary_calculatesYouOweAndYouAreOwedCorrectly() {
        val transfers = listOf(
            DebtTransfer(fromUserId = "user_dat", toUserId = "user_hung", amount = 250_000L),
            DebtTransfer(fromUserId = "user_binh", toUserId = "user_hung", amount = 150_000L),
            DebtTransfer(fromUserId = "user_hung", toUserId = "user_linh", amount = 80_000L)
        )

        val (hungOwes, hungIsOwed) = SpendlyAnalyticsHelpers.computePersonalDebtSummary(
            transfers = transfers,
            currentUserId = "user_hung"
        )
        assertEquals(80_000L, hungOwes)
        assertEquals(400_000L, hungIsOwed)

        val (datOwes, datIsOwed) = SpendlyAnalyticsHelpers.computePersonalDebtSummary(
            transfers = transfers,
            currentUserId = "user_dat"
        )
        assertEquals(250_000L, datOwes)
        assertEquals(0L, datIsOwed)

        val (emptyOwes, emptyIsOwed) = SpendlyAnalyticsHelpers.computePersonalDebtSummary(
            transfers = transfers,
            currentUserId = "   "
        )
        assertEquals(0L, emptyOwes)
        assertEquals(0L, emptyIsOwed)
    }

    @Test
    fun buildHomeAiSuggestion_coversAllFourBudgetZonesAndDebtReminders() {
        // 1. Vùng 0-50% (Bình thường / An toàn)
        val normalAdvice = SpendlyAnalyticsHelpers.buildHomeAiSuggestion(
            budgetRatio = 0.30f,
            youOwe = 0L,
            youAreOwed = 0L,
            groupName = "Quỹ Nhóm 08"
        )
        assertTrue(normalAdvice.contains("Bình thường"))
        assertTrue(normalAdvice.contains("30%"))
        assertTrue(normalAdvice.contains("Quỹ Nhóm 08"))

        // 2. Vùng 50-80% (Theo dõi) + Nhắc nợ cần trả
        val watchAdvice = SpendlyAnalyticsHelpers.buildHomeAiSuggestion(
            budgetRatio = 0.65f,
            youOwe = 120_000L,
            youAreOwed = 0L,
            groupName = "Quỹ Nhóm 08"
        )
        assertTrue(watchAdvice.contains("theo dõi", ignoreCase = true))
        assertTrue(watchAdvice.contains("65%"))
        assertTrue(watchAdvice.contains("120.000 đ"))

        // 3. Vùng 80-100% (Cảnh báo) + Nhắc khoản được hoàn trả
        val warningAdvice = SpendlyAnalyticsHelpers.buildHomeAiSuggestion(
            budgetRatio = 0.90f,
            youOwe = 0L,
            youAreOwed = 350_000L,
            groupName = "Quỹ Nhóm 08"
        )
        assertTrue(warningAdvice.contains("Cảnh báo"))
        assertTrue(warningAdvice.contains("90%"))
        assertTrue(warningAdvice.contains("350.000 đ"))

        // 4. Vùng >100% (Vượt ngân sách) + Cả hai chiều nợ
        val exceededAdvice = SpendlyAnalyticsHelpers.buildHomeAiSuggestion(
            budgetRatio = 1.25f,
            youOwe = 100_000L,
            youAreOwed = 200_000L,
            groupName = "Quỹ Nhóm 08"
        )
        assertTrue(exceededAdvice.contains("Vượt ngân sách"))
        assertTrue(exceededAdvice.contains("125%"))
        assertTrue(exceededAdvice.contains("100.000 đ"))
        assertTrue(exceededAdvice.contains("200.000 đ"))
    }

    @Test
    fun filterHistoryTransactions_filtersBySearchQueryAndScopePersonal() {
        val nov15 = timestampFor(2026, 11, 15)
        val nov14 = timestampFor(2026, 11, 14)

        val expenses = listOf(
            SpendlyExpense(
                expenseId = "exp_1",
                groupId = "grp_1",
                title = "Mua linh kiện cảm biến",
                amount = 500_000L,
                category = SpendlyCategory.EDUCATION,
                payerId = "user_hung",
                payerName = "Lê Văn Hùng",
                expenseDateMillis = nov15,
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "exp_2",
                groupId = "grp_1",
                title = "Cà phê thảo luận sprint",
                amount = 180_000L,
                category = SpendlyCategory.FOOD,
                payerId = "user_linh",
                payerName = "Lý Việt Linh",
                expenseDateMillis = nov14,
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "exp_cancelled",
                groupId = "grp_1",
                title = "Khoản chi đã hủy",
                amount = 999_000L,
                category = SpendlyCategory.OTHER,
                payerId = "user_hung",
                payerName = "Lê Văn Hùng",
                expenseDateMillis = nov15,
                status = RecordStatus.CANCELLED
            )
        )
        val incomes = listOf(
            SpendlyIncome(
                incomeId = "inc_1",
                groupId = "grp_1",
                title = "Nạp quỹ đợt 1",
                amount = 2_000_000L,
                payerId = "user_hung",
                payerName = "Lê Văn Hùng",
                incomeDateMillis = nov15,
                status = RecordStatus.ACTIVE
            )
        )

        // Lọc theo từ khóa tìm kiếm "cà phê"
        val byQuery = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "cà phê",
            selectedScope = "Tất cả",
            selectedMemberId = null,
            selectedCategoryLabel = null,
            selectedMonthLabel = "Tất cả"
        )
        val queryTx = byQuery.flatMap { it.transactions }
        assertEquals(1, queryTx.size)
        assertEquals("exp_2", queryTx.first().id)

        // Lọc theo phạm vi "Cá nhân" (chỉ giao dịch có payerId == currentUserId)
        val byPersonalScope = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "",
            selectedScope = "Cá nhân",
            selectedMemberId = null,
            selectedCategoryLabel = null,
            selectedMonthLabel = "Tất cả"
        )
        val personalTx = byPersonalScope.flatMap { it.transactions }
        assertEquals(2, personalTx.size)
        assertTrue(personalTx.map { it.id }.containsAll(listOf("exp_1", "inc_1")))
        assertTrue(personalTx.all { it.subtitle.contains("Cá nhân") })
    }

    @Test
    fun filterHistoryTransactions_filtersByMemberCategoryAndMonth() {
        val nov10 = timestampFor(2026, 11, 10)
        val oct20 = timestampFor(2026, 10, 20)

        val expenses = listOf(
            SpendlyExpense(
                expenseId = "exp_nov_food",
                groupId = "grp_1",
                title = "Ăn trưa nhóm",
                amount = 320_000L,
                category = SpendlyCategory.FOOD,
                payerId = "user_dat",
                payerName = "Nguyễn Phan Tấn Đạt",
                expenseDateMillis = nov10,
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "exp_oct_transport",
                groupId = "grp_1",
                title = "Taxi đi hội thảo",
                amount = 150_000L,
                category = SpendlyCategory.TRANSPORT,
                payerId = "user_binh",
                payerName = "Lê Trung Bình",
                expenseDateMillis = oct20,
                status = RecordStatus.ACTIVE
            )
        )
        val incomes = listOf(
            SpendlyIncome(
                incomeId = "inc_nov_fund",
                groupId = "grp_1",
                title = "Đóng quỹ tháng 11",
                amount = 1_500_000L,
                payerId = "user_dat",
                payerName = "Nguyễn Phan Tấn Đạt",
                incomeDateMillis = nov10,
                status = RecordStatus.ACTIVE
            )
        )

        // 1. Lọc theo thành viên "user_binh"
        val byMember = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "",
            selectedScope = "Nhóm",
            selectedMemberId = "user_binh",
            selectedCategoryLabel = null,
            selectedMonthLabel = "Tất cả"
        )
        val memberTx = byMember.flatMap { it.transactions }
        assertEquals(1, memberTx.size)
        assertEquals("exp_oct_transport", memberTx.first().id)

        // 2. Lọc theo danh mục chi tiêu "Ăn uống"
        val byFoodCategory = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "",
            selectedScope = "Nhóm",
            selectedMemberId = null,
            selectedCategoryLabel = "Ăn uống",
            selectedMonthLabel = "Tất cả"
        )
        val foodTx = byFoodCategory.flatMap { it.transactions }
        assertEquals(1, foodTx.size)
        assertEquals("exp_nov_food", foodTx.first().id)

        // 3. Lọc theo danh mục "Thu quỹ"
        val byFundCategory = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "",
            selectedScope = "Nhóm",
            selectedMemberId = null,
            selectedCategoryLabel = "Thu quỹ",
            selectedMonthLabel = "Tất cả"
        )
        val fundTx = byFundCategory.flatMap { it.transactions }
        assertEquals(1, fundTx.size)
        assertEquals("inc_nov_fund", fundTx.first().id)
        assertTrue(fundTx.first().isIncome)

        // 4. Lọc theo tháng "Tháng 11/2026"
        val byNovMonth = SpendlyAnalyticsHelpers.filterHistoryTransactions(
            expenses = expenses,
            incomes = incomes,
            memberNamesById = memberNames,
            currentUserId = "user_hung",
            searchQuery = "",
            selectedScope = "Nhóm",
            selectedMemberId = null,
            selectedCategoryLabel = null,
            selectedMonthLabel = "Tháng 11/2026"
        )
        val novTx = byNovMonth.flatMap { it.transactions }
        assertEquals(2, novTx.size)
        assertTrue(novTx.map { it.id }.containsAll(listOf("exp_nov_food", "inc_nov_fund")))
    }

    @Test
    fun computeMonthlyTrendPoints_aggregatesActiveExpensesAcrossLastNMonths() {
        val refNov2026 = timestampFor(2026, 11, 20)
        val expenses = listOf(
            SpendlyExpense(
                expenseId = "e_aug",
                amount = 400_000L,
                expenseDateMillis = timestampFor(2026, 8, 12),
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e_oct_1",
                amount = 600_000L,
                expenseDateMillis = timestampFor(2026, 10, 5),
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e_oct_2",
                amount = 400_000L,
                expenseDateMillis = timestampFor(2026, 10, 18),
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e_nov_1",
                amount = 1_500_000L,
                expenseDateMillis = timestampFor(2026, 11, 15),
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e_nov_cancelled",
                amount = 900_000L,
                expenseDateMillis = timestampFor(2026, 11, 16),
                status = RecordStatus.CANCELLED
            )
        )

        val trend = SpendlyAnalyticsHelpers.computeMonthlyTrendPoints(
            expenses = expenses,
            referenceTimeMillis = refNov2026,
            monthCount = 5
        )

        assertEquals(
            listOf(
                "T7" to 0L,
                "T8" to 400_000L,
                "T9" to 0L,
                "T10" to 1_000_000L,
                "T11" to 1_500_000L
            ),
            trend
        )
    }

    @Test
    fun computeMonthOverMonthPercent_computesDeltaOrReturnsNullWhenPrevZero() {
        val expenses = listOf(
            SpendlyExpense(
                expenseId = "e_oct",
                amount = 1_000_000L,
                expenseDateMillis = timestampFor(2026, 10, 10),
                status = RecordStatus.ACTIVE
            ),
            SpendlyExpense(
                expenseId = "e_nov",
                amount = 1_250_000L,
                expenseDateMillis = timestampFor(2026, 11, 12),
                status = RecordStatus.ACTIVE
            )
        )

        val novVsOct = SpendlyAnalyticsHelpers.computeMonthOverMonthPercent(
            expenses = expenses,
            targetMonthLabel = "Tháng 11/2026"
        )
        assertNotNull(novVsOct)
        assertEquals(25.0f, novVsOct!!, 0.01f)

        // Tháng 9/2026 không có chi tiêu (0 đ) -> so sánh Tháng 10/2026 với Tháng 9/2026 trả về null
        val octVsSep = SpendlyAnalyticsHelpers.computeMonthOverMonthPercent(
            expenses = expenses,
            targetMonthLabel = "Tháng 10/2026"
        )
        assertNull(octVsSep)
    }
}
