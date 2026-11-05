package com.example.nhoms8

import androidx.compose.ui.graphics.Color
import com.example.nhoms8.algorithm.FintechAlgorithm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm thử Thuật toán Tài chính & Quyết toán Nợ (FintechAlgorithmTest)
 * Thực hiện bởi Kỹ sư Kiểm thử Tự động (qa-automation-tester) - Nhóm 08
 */
class FintechAlgorithmTest {

    @Test
    fun testEqualSplit_exactDivision() {
        val totalAmount = 900000L
        val count = 3
        val result = FintechAlgorithm.calculateEqualSplit(totalAmount, count)

        assertEquals(3, result.size)
        assertEquals(300000L, result[0])
        assertEquals(300000L, result[1])
        assertEquals(300000L, result[2])
        assertEquals(totalAmount, result.sum())
    }

    @Test
    fun testEqualSplit_withRemainderRounding() {
        val totalAmount = 100000L
        val count = 3
        val result = FintechAlgorithm.calculateEqualSplit(totalAmount, count)

        assertEquals(3, result.size)
        // Số lẻ 1 đồng được cộng vào người đầu tiên đảm bảo tổng chính xác
        assertEquals(33334L, result[0])
        assertEquals(33333L, result[1])
        assertEquals(33333L, result[2])
        assertEquals(totalAmount, result.sum())
    }

    @Test
    fun testRatioSplit() {
        val totalAmount = 1000000L
        val ratios = listOf(50f, 30f, 20f)
        val result = FintechAlgorithm.calculateRatioSplit(totalAmount, ratios)

        assertEquals(3, result.size)
        assertEquals(500000L, result[0])
        assertEquals(300000L, result[1])
        assertEquals(200000L, result[2])
        assertEquals(totalAmount, result.sum())
    }

    @Test
    fun testFormatCurrencyVnd() {
        val formatted = FintechAlgorithm.formatVnd(12680000L)
        assertEquals("12.680.000 đ", formatted)
    }

    @Test
    fun testSimplifyDebts_minCashFlow() {
        val m1 = GroupMember("1", "Minh Anh", "MA", "0912345678", true, Color.Green, MemberBalanceStatus.Balanced)
        val m2 = GroupMember("2", "Lan Anh", "LA", "0908224510", false, Color.Yellow, MemberBalanceStatus.Balanced)
        val m3 = GroupMember("3", "Tuấn Nam", "TN", "0986420115", false, Color.Cyan, MemberBalanceStatus.Balanced)

        // Minh Anh cho vay ròng: +400.000 đ
        // Lan Anh cho vay ròng: +200.000 đ
        // Tuấn Nam nợ ròng: -600.000 đ
        val netBalances = mapOf(
            "1" to 400000L,
            "2" to 200000L,
            "3" to -600000L
        )

        val simplifiedDebts = FintechAlgorithm.simplifyDebts(listOf(m1, m2, m3), netBalances)

        // Thuật toán Min Cash Flow ghép cặp tối ưu: Tuấn Nam trả Minh Anh 400k, Tuấn Nam trả Lan Anh 200k (chỉ 2 giao dịch)
        assertEquals(2, simplifiedDebts.size)
        val totalSettled = simplifiedDebts.sumOf { it.rawAmount }
        assertEquals(600000L, totalSettled)
    }
}
