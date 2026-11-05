package com.example.nhoms8

import com.example.nhoms8.data.GeminiChatRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm thử Bóc tách dữ liệu phản hồi từ Gemini 1.5 Flash (GeminiChatRepositoryTest)
 * Thực hiện bởi Kỹ sư Kiểm thử Tự động (qa-automation-tester) - Nhóm 08
 */
class GeminiChatRepositoryTest {

    @Test
    fun testApiKeyConfiguredCheck() {
        val repoEmpty = GeminiChatRepository(apiKey = "")
        assertFalse(repoEmpty.isApiKeyConfigured())

        val repoValid = GeminiChatRepository(apiKey = "AIzaSyFakeKeyForTesting123456789")
        assertTrue(repoValid.isApiKeyConfigured())
    }

    @Test
    fun testParseGeminiExpenseOutput_viaReflection() {
        val repo = GeminiChatRepository()
        val mockRawText = """
            Chào bạn! Mình đã nắm được thông tin bữa ăn rồi nha. Bạn xác nhận giúp mình nhé! 🌱
            <<<ACTION_EXPENSE>>>
            {"title":"Lẩu bò Haidilao","amount":650000,"category":"FOOD","categoryName":"Ăn uống","payerName":"Lê Văn Hùng","splitSummary":"Chia đều 5 người"}
            <<<END_ACTION_EXPENSE>>>
        """.trimIndent()

        val method = GeminiChatRepository::class.java.getDeclaredMethod("parseGeminiOutput", String::class.java)
        method.isAccessible = true
        val result = method.invoke(repo, mockRawText) as com.example.nhoms8.data.GeminiResult

        assertNotNull(result.expenseProposal)
        val proposal = result.expenseProposal!!
        assertEquals("Lẩu bò Haidilao", proposal.title)
        assertEquals(650000L, proposal.amount)
        assertEquals("Lê Văn Hùng", proposal.payerName)
        assertTrue("Văn bản hiển thị cho người dùng không được chứa thẻ tag", !result.cleanText.contains("<<<ACTION_EXPENSE>>>"))
    }

    @Test
    fun testParseGeminiReminderOutput_viaReflection() {
        val repo = GeminiChatRepository()
        val mockRawText = """
            FinSprout đã soạn xong mẫu tin nhắn nhắc nợ đáng yêu cho bạn nè: 💌
            <<<ACTION_REMINDER>>>
            {"recipientName":"Linh","amountFormatted":"120.000 đ","suggestedMessage":"Ting ting! Linh ơi trả 120k tiền lẩu nhé!"}
            <<<END_ACTION_REMINDER>>>
        """.trimIndent()

        val method = GeminiChatRepository::class.java.getDeclaredMethod("parseGeminiOutput", String::class.java)
        method.isAccessible = true
        val result = method.invoke(repo, mockRawText) as com.example.nhoms8.data.GeminiResult

        assertNotNull(result.reminderProposal)
        val reminder = result.reminderProposal!!
        assertEquals("Linh", reminder.recipientName)
        assertEquals("120.000 đ", reminder.amountFormatted)
        assertEquals("Ting ting! Linh ơi trả 120k tiền lẩu nhé!", reminder.suggestedMessage)
        assertTrue("Không chứa thẻ tag trong cleanText", !result.cleanText.contains("<<<ACTION_REMINDER>>>"))
    }
}
