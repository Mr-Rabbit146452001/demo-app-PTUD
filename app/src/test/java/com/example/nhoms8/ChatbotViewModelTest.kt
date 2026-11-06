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
 * Unit Test kiểm thử luồng hoạt động của Trợ lý AI FinSprout (ChatbotViewModelTest)
 * Thực hiện bởi Kỹ sư Kiểm thử Tự động (qa-automation-tester) - Nhóm 08
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createTestViewModel() = SpendTogetherViewModel(
        geminiChatRepository = com.example.nhoms8.data.GeminiChatRepository(apiKey = "")
    )

    @Test
    fun testInitialChatbotGreeting() = runTest {
        val viewModel = createTestViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.chatMessages
        assertTrue("Hội thoại ban đầu phải có ít nhất 1 tin nhắn chào mừng", messages.isNotEmpty())
        assertTrue("Tin nhắn ban đầu phải đến từ FinSprout bot", !messages[0].isFromUser)
        assertTrue("Tin nhắn ban đầu phải có chữ FinSprout", messages[0].text.contains("FinSprout"))
    }

    @Test
    fun testSendMessage_NaturalLanguageExpenseLogging() = runTest {
        val viewModel = createTestViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        // Gửi câu thoại nhập chi tiêu
        viewModel.sendMessageToChatbot("Trưa nay ăn bún bò 180k Hùng trả, chia đều")
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.chatMessages
        assertTrue("Sau khi chat phải có thêm tin nhắn", messages.size >= 3)

        val lastBotMessage = messages.last()
        assertTrue("Tin nhắn cuối phải đến từ Bot", !lastBotMessage.isFromUser)
        assertNotNull("Bot phải trích xuất được thẻ Action Card chi tiêu", lastBotMessage.expenseProposal)

        val proposal = lastBotMessage.expenseProposal!!
        assertEquals("Số tiền phải bóc tách đúng là 180.000 đ", 180000L, proposal.amount)
        assertEquals("Ăn uống", proposal.categoryName)
        assertEquals("Lê Văn Hùng", proposal.payerName)
    }

    @Test
    fun testConfirmExpense_UpdatesBudgetAndAddsTransaction() = runTest {
        val viewModel = createTestViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        val initialBalance = viewModel.uiState.value.budget.totalBalance

        // 1. Gửi tin nhắn tạo khoản chi
        viewModel.sendMessageToChatbot("Ăn lẩu 200k Hùng trả")
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.chatMessages
        val expenseMessage = messages.find { it.expenseProposal != null }
        assertNotNull(expenseMessage)

        // 2. Xác nhận lưu khoản chi
        viewModel.confirmChatExpense(expenseMessage!!.id)
        testDispatcher.scheduler.advanceUntilIdle()

        val updatedBalance = viewModel.uiState.value.budget.totalBalance
        assertEquals("Số dư quỹ phải giảm đúng 200.000 đ sau khi xác nhận", initialBalance - 200000L, updatedBalance)
    }

    @Test
    fun testSendMessage_DebtReminder() = runTest {
        val viewModel = createTestViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendMessageToChatbot("Viết giúp mình tin nhắn nhắc nợ bạn Linh")
        testDispatcher.scheduler.advanceUntilIdle()

        val messages = viewModel.uiState.value.chatMessages
        val reminderMessage = messages.last()
        assertNotNull("Bot phải trích xuất được thẻ nhắc nợ tế nhị", reminderMessage.reminderProposal)
        assertTrue("Gợi ý nhắc nợ phải gửi cho Linh", reminderMessage.reminderProposal!!.recipientName.contains("Linh"))
    }

    @Test
    fun testClearChatHistory() = runTest {
        val viewModel = createTestViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.sendMessageToChatbot("Hỏi tình hình quỹ")
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.chatMessages.size > 1)

        viewModel.clearChatHistory()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Sau khi xóa hội thoại chỉ còn 1 tin nhắn chào mừng", 1, viewModel.uiState.value.chatMessages.size)
    }
}
