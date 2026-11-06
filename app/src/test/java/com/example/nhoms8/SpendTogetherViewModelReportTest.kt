package com.example.nhoms8

import com.example.nhoms8.viewmodel.SpendTogetherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Test kiểm thử Tính toán Phân tích Báo cáo Tài chính Động (Donut & Bar Chart Analytics)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpendTogetherViewModelReportTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testCategoryReportCalculations() = runTest {
        val viewModel = SpendTogetherViewModel(
            geminiChatRepository = com.example.nhoms8.data.GeminiChatRepository(apiKey = "")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        val reportItems = uiState.categoryReportItems

        assertEquals(4, reportItems.size) // 4 danh mục: FOOD, HOUSING, TRANSPORT, OTHER

        val totalPercentage = reportItems.sumOf { it.percentage.toDouble() }
        assertTrue("Tổng phần trăm các danh mục phải xấp xỉ 100%", totalPercentage in 99.0..101.0)

        val totalSweep = reportItems.sumOf { it.sweepAngle.toDouble() }
        assertTrue("Tổng góc quét Donut chart phải xấp xỉ 360 độ", totalSweep in 359.0..361.0)
    }

    @Test
    fun testMemberReportCalculations() = runTest {
        val viewModel = SpendTogetherViewModel(
            geminiChatRepository = com.example.nhoms8.data.GeminiChatRepository(apiKey = "")
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        val memberReports = uiState.memberReportItems

        assertEquals(4, memberReports.size) // 4 thành viên Nhóm 08: Hùng, Linh, Đạt, Bình

        val maxRatio = memberReports.maxOf { it.heightRatio }
        assertEquals("Thành viên chi nhiều nhất phải có tỷ lệ cột cao nhất là 1.0f", 1.0f, maxRatio, 0.01f)

        assertNotNull(uiState.highestSpenderNote)
        assertTrue("Ghi chú người chi nhiều nhất phải có tên thành viên", uiState.highestSpenderNote.contains("Lê Văn Hùng"))
    }
}
