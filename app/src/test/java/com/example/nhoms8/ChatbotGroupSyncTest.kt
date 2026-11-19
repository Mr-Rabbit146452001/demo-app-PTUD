package com.example.nhoms8

import com.example.nhoms8.data.AuthResult
import com.example.nhoms8.data.GeminiChatRepository
import com.example.nhoms8.data.SpendlyServiceLocator
import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.viewmodel.SpendTogetherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit Test kiểm thử cầu nối FinSprout AI Chatbot + SpendTogetherViewModel với nhóm Firestore đang hoạt động
 * và kiểm tra SpendlyServiceLocator + AuthResult nullable user.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ChatbotGroupSyncTest {

    private val testDispatcher = StandardTestDispatcher()

    private val ownerUser = SpendlyUser(
        uid = "uid_an",
        fullName = "Nguyễn An",
        email = "an@spendly.vn",
        phone = "0901111111"
    )
    private val memberBao = SpendlyUser(
        uid = "uid_bao",
        fullName = "Trần Bảo",
        email = "bao@spendly.vn",
        phone = "0902222222"
    )
    private val memberChi = SpendlyUser(
        uid = "uid_chi",
        fullName = "Lê Chi",
        email = "chi@spendly.vn",
        phone = "0903333333"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        SpendlyServiceLocator.setActiveGroup(null)
    }

    private fun newInMemorySpendlyRepo(): FirestoreSpendlyRepository {
        var tick = 1_700_000_000_000L
        return FirestoreSpendlyRepository(
            firestore = null,
            clock = {
                tick += 100L
                tick
            }
        )
    }

    @Test
    fun activeFirestoreGroup_chatbotReadsBudgetAndRecordsConfirmedExpense() = runTest {
        val spendlyRepo = newInMemorySpendlyRepo()

        // 1. Tạo nhóm "Du lịch Đà Lạt" với 3 thành viên và ngân sách 5.000.000 đ
        val dalatGroup = spendlyRepo.createGroup(
            owner = ownerUser,
            name = "Du lịch Đà Lạt",
            description = "Quỹ đi chơi 3 ngày 2 đêm",
            monthlyBudgetLimit = 5_000_000L
        ).getOrThrow()
        spendlyRepo.joinGroupByInviteCode(memberBao, dalatGroup.inviteCode).getOrThrow()
        spendlyRepo.joinGroupByInviteCode(memberChi, dalatGroup.inviteCode).getOrThrow()

        val viewModel = SpendTogetherViewModel(
            geminiChatRepository = GeminiChatRepository(apiKey = ""),
            spendlyRepository = spendlyRepo
        )
        viewModel.syncFirebaseUser(ownerUser.uid, ownerUser.fullName, ownerUser.email)
        viewModel.setActiveGroupId(dalatGroup.groupId)
        advanceUntilIdle()

        assertEquals(dalatGroup.groupId, viewModel.uiState.value.activeGroupId)
        assertEquals("Du lịch Đà Lạt", viewModel.uiState.value.activeGroupName)

        // 2. Hỏi FinSprout "tình hình quỹ" -> phản hồi phản ánh nhóm "Du lịch Đà Lạt" và ngân sách 5.000.000 đ
        viewModel.sendMessageToChatbot("tình hình quỹ")
        advanceUntilIdle()

        val statusResponse = viewModel.uiState.value.chatMessages.last()
        assertTrue(!statusResponse.isFromUser)
        assertTrue(
            "Phản hồi phải nêu đúng tên quỹ Du lịch Đà Lạt: ${statusResponse.text}",
            statusResponse.text.contains("Du lịch Đà Lạt")
        )
        assertTrue(
            "Phản hồi phải nêu đúng hạn mức/số dư 5.000.000 đ: ${statusResponse.text}",
            statusResponse.text.contains("5.000.000 đ")
        )
        assertTrue(
            "Phản hồi phải nêu đúng số lượng 3 thành viên: ${statusResponse.text}",
            statusResponse.text.contains("3 thành viên")
        )

        // 3. Nhắn "Ăn tối 300k" -> FinSprout đề xuất Action Card -> confirmChatExpense -> ghi vào FirestoreSpendlyRepository
        viewModel.sendMessageToChatbot("Ăn tối 300k")
        advanceUntilIdle()

        val proposalMessage = viewModel.uiState.value.chatMessages.lastOrNull { it.expenseProposal != null }
        assertNotNull("FinSprout phải tạo Action Card cho khoản chi Ăn tối 300k", proposalMessage)
        val proposal = proposalMessage!!.expenseProposal!!
        assertEquals(300_000L, proposal.amount)
        assertEquals(ExpenseCategory.FOOD, proposal.category)
        assertEquals(ownerUser.fullName, proposal.payerName)

        viewModel.confirmChatExpense(proposalMessage.id)
        advanceUntilIdle()

        val recordedExpenses = spendlyRepo.observeExpenses(dalatGroup.groupId).first()
        assertEquals("Khoản chi phải được lưu vào FirestoreSpendlyRepository dưới nhóm Đà Lạt", 1, recordedExpenses.size)
        val savedExpense = recordedExpenses.first()
        assertEquals(dalatGroup.groupId, savedExpense.groupId)
        assertEquals(300_000L, savedExpense.amount)
        assertEquals(SpendlyCategory.FOOD, savedExpense.category)
        assertEquals(SplitMethod.EQUAL, savedExpense.splitType)
        assertEquals(RecordStatus.ACTIVE, savedExpense.status)
        assertEquals(ownerUser.uid, savedExpense.payerId)

        val detail = spendlyRepo.observeExpenseDetail(dalatGroup.groupId, savedExpense.expenseId).first()
        assertNotNull(detail)
        assertEquals(3, detail!!.second.size)
        assertEquals(300_000L, detail.second.sumOf { it.shareAmount })
        assertTrue(detail.second.all { it.shareAmount == 100_000L })

        val balances = spendlyRepo.observeMemberBalances(dalatGroup.groupId).first().associateBy { it.userId }
        assertEquals(200_000L, balances.getValue(ownerUser.uid).netBalance)
        assertEquals(-100_000L, balances.getValue(memberBao.uid).netBalance)
        assertEquals(-100_000L, balances.getValue(memberChi.uid).netBalance)

        // Hỏi lại tình hình quỹ sau khi chi 300k -> số dư khả dụng còn 4.700.000 đ
        viewModel.sendMessageToChatbot("tình hình quỹ")
        advanceUntilIdle()
        val updatedStatusResponse = viewModel.uiState.value.chatMessages.last()
        assertTrue(updatedStatusResponse.text.contains("300.000 đ"))
        assertTrue(updatedStatusResponse.text.contains("4.700.000 đ"))
    }

    @Test
    fun bindToSpendlyRepository_buildsRealContextPrompt_andTracksNotificationsAndDebts() = runTest {
        val spendlyRepo = newInMemorySpendlyRepo()
        val groupA = spendlyRepo.createGroup(
            owner = ownerUser,
            name = "Quỹ Trọ Hà Nội",
            monthlyBudgetLimit = 3_000_000L
        ).getOrThrow()

        val groupDalat = spendlyRepo.createGroup(
            owner = ownerUser,
            name = "Du lịch Đà Lạt",
            description = "Chuyến đi mùa thu",
            monthlyBudgetLimit = 5_000_000L
        ).getOrThrow()
        spendlyRepo.joinGroupByInviteCode(memberBao, groupDalat.inviteCode).getOrThrow()
        spendlyRepo.joinGroupByInviteCode(memberChi, groupDalat.inviteCode).getOrThrow()

        val activeGroupFlow = MutableStateFlow<String?>(groupA.groupId)
        val viewModel = SpendTogetherViewModel(
            geminiChatRepository = GeminiChatRepository(apiKey = "")
        )
        viewModel.bindToSpendlyRepository(spendlyRepo, activeGroupFlow)
        viewModel.syncFirebaseUser(ownerUser.uid, ownerUser.fullName, ownerUser.email)
        viewModel.observeUnreadNotificationsCount(ownerUser.uid)
        advanceUntilIdle()

        // Trưởng nhóm có 2 thông báo chưa đọc do Bảo và Chi tham gia groupDalat
        assertEquals(2, viewModel.uiState.value.unreadNotificationsCount)
        assertEquals("Quỹ Trọ Hà Nội", viewModel.uiState.value.activeGroupName)

        // Chuyển activeGroupIdFlow sang "Du lịch Đà Lạt"
        activeGroupFlow.value = groupDalat.groupId
        advanceUntilIdle()
        assertEquals("Du lịch Đà Lạt", viewModel.uiState.value.activeGroupName)

        // Tạo khoản chi do Bảo trả hộ 300k -> An nợ Bảo 100k, Chi nợ Bảo 100k
        viewModel.sendMessageToChatbot("Ăn tối 300k Bảo trả")
        advanceUntilIdle()
        val proposalMsg = viewModel.uiState.value.chatMessages.last { it.expenseProposal != null }
        assertEquals(memberBao.fullName, proposalMsg.expenseProposal!!.payerName)

        viewModel.confirmChatExpense(proposalMsg.id)
        advanceUntilIdle()

        val contextPrompt = viewModel.buildChatbotContextPrompt()
        assertTrue(contextPrompt.contains("Du lịch Đà Lạt"))
        assertTrue(contextPrompt.contains("5.000.000 đ"))
        assertTrue(contextPrompt.contains("300.000 đ"))
        assertTrue(contextPrompt.contains("Nguyễn An"))
        assertTrue(contextPrompt.contains("Trần Bảo"))
        assertTrue(contextPrompt.contains("Lê Chi"))
        assertTrue(contextPrompt.contains("nợ Trần Bảo 100.000 đ"))

        // Hỏi nhắc nợ Chi -> trích xuất đúng khoản nợ 100.000 đ từ dữ liệu Firestore thật
        viewModel.sendMessageToChatbot("Viết tin nhắn nhắc nợ Chi")
        advanceUntilIdle()
        val reminderMsg = viewModel.uiState.value.chatMessages.last()
        val reminder = reminderMsg.reminderProposal
        assertNotNull(reminder)
        assertEquals("Lê Chi", reminder!!.recipientName)
        assertEquals("100.000 đ", reminder.amountFormatted)
        assertTrue(reminder.suggestedMessage.contains("Du lịch Đà Lạt"))
    }

    @Test
    fun serviceLocatorAndAuthResultNullUser_behaveSafely() {
        val authSuccess = AuthResult.Success(user = null, message = "Đã gửi liên kết khôi phục")
        assertNull(authSuccess.user)
        assertEquals("Đã gửi liên kết khôi phục", authSuccess.message)

        assertSame(SpendlyServiceLocator.authRepository, SpendlyServiceLocator.authRepository)
        assertSame(SpendlyServiceLocator.spendlyRepository, SpendlyServiceLocator.spendlyRepository)
        assertSame(SpendlyServiceLocator.geminiChatRepository, SpendlyServiceLocator.geminiChatRepository)

        SpendlyServiceLocator.setActiveGroup("grp_dalat")
        assertEquals("grp_dalat", SpendlyServiceLocator.activeGroupId.value)
        SpendlyServiceLocator.setActiveGroup("   ")
        assertNull(SpendlyServiceLocator.activeGroupId.value)
    }
}
