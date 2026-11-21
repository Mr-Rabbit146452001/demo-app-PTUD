package com.example.nhoms8

import com.example.nhoms8.data.GeminiChatRepository
import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.usecase.SplitValidationResult
import com.example.nhoms8.feature.receipt.ReceiptConfidence
import com.example.nhoms8.feature.receipt.ReceiptLineItem
import com.example.nhoms8.feature.receipt.ReceiptScanStep
import com.example.nhoms8.feature.receipt.ReceiptScannerViewModel
import com.example.nhoms8.feature.receipt.ScannedReceiptResult
import com.example.nhoms8.feature.transactions.TransactionViewModel
import com.example.nhoms8.viewmodel.SpendTogetherViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TransactionViewModelReceiptTest {

    private val testDispatcher = StandardTestDispatcher()

    private val owner = SpendlyUser(
        uid = "uid_owner",
        fullName = "Lê Văn Hùng",
        email = "hung@spendly.vn",
        phone = "0901000001"
    )
    private val member2 = SpendlyUser(
        uid = "uid_linh",
        fullName = "Lý Việt Linh",
        email = "linh@spendly.vn",
        phone = "0902000002"
    )

    private val sampleReceipt = ScannedReceiptResult(
        isReceipt = true,
        title = "Cơm Tấm Cali",
        totalAmount = 300_000L,
        category = SpendlyCategory.FOOD,
        expenseDateMillis = 1_795_000_000_000L,
        items = listOf(ReceiptLineItem("Sườn bì chả", 3, 300_000L)),
        description = "Chi tiết hóa đơn: 3x Sườn bì chả (300.000 đ)",
        confidence = ReceiptConfidence.HIGH,
        savedReceiptPath = "/data/user/0/com.example.nhoms8/files/receipts/receipt_test.jpg"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startNewExpenseFromReceipt prefills draft and survives async member loading`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(owner, "Quỹ Ăn Trưa").getOrThrow()
        repo.addManualMember(group.groupId, owner.uid, member2.fullName, member2.phone).getOrThrow()

        val txViewModel = TransactionViewModel(repo, repo)
        txViewModel.startNewExpenseFromReceipt(
            groupId = group.groupId,
            currentUserId = owner.uid,
            receipt = sampleReceipt
        )
        advanceUntilIdle()

        val draft = txViewModel.draftState.value
        assertEquals("Cơm Tấm Cali", draft.title)
        assertEquals(300_000L, draft.amount)
        assertEquals(SpendlyCategory.FOOD, draft.category)
        assertEquals(sampleReceipt.savedReceiptPath, draft.receiptUrl)
        assertEquals(sampleReceipt.description, draft.description)
        assertEquals(sampleReceipt.expenseDateMillis, draft.expenseDateMillis)
        assertEquals(2, draft.members.size)
        assertTrue(draft.validationResult is SplitValidationResult.Valid)
    }

    @Test
    fun `applyScannedReceiptToDraft updates existing draft without dropping group members`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(owner, "Quỹ Nhóm 08").getOrThrow()
        repo.addManualMember(group.groupId, owner.uid, member2.fullName, member2.phone).getOrThrow()

        val txViewModel = TransactionViewModel(repo, repo)
        txViewModel.startNewExpense(group.groupId, owner.uid)
        advanceUntilIdle()
        assertEquals(2, txViewModel.draftState.value.members.size)

        txViewModel.applyScannedReceiptToDraft(sampleReceipt)
        val draft = txViewModel.draftState.value
        assertEquals("Cơm Tấm Cali", draft.title)
        assertEquals(300_000L, draft.amount)
        assertEquals(2, draft.members.size)
        assertTrue(draft.validationResult is SplitValidationResult.Valid)
    }

    @Test
    fun `ReceiptScannerViewModel saveQuickEqualSplit persists expense with receiptUrl`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(owner, "Quỹ Du Lịch").getOrThrow()
        repo.addManualMember(group.groupId, owner.uid, member2.fullName, member2.phone).getOrThrow()

        val scannerVm = ReceiptScannerViewModel(
            groupRepository = repo,
            transactionRepository = repo,
            geminiChatRepository = GeminiChatRepository(apiKey = "")
        )
        scannerVm.bindContext(group.groupId, owner.uid)
        advanceUntilIdle()

        scannerVm.updateEditableTitle("Grab Car Sân Bay")
        scannerVm.updateEditableAmount("200000")
        scannerVm.updateEditableCategory(SpendlyCategory.TRANSPORT)
        scannerVm.updateEditableDescription("Đi taxi ra sân bay")

        var successMsg: String? = null
        scannerVm.saveQuickEqualSplit(currentUserId = owner.uid) { msg ->
            successMsg = msg
        }
        advanceUntilIdle()

        assertNotNull(successMsg)
        val expenses = repo.observeExpenses(group.groupId).first()
        assertEquals(1, expenses.size)
        val created = expenses.first()
        assertEquals("Grab Car Sân Bay", created.title)
        assertEquals(200_000L, created.amount)
        assertEquals(SpendlyCategory.TRANSPORT, created.category)
        assertEquals(owner.uid, created.payerId)

        val detail = repo.observeExpenseDetail(group.groupId, created.expenseId).first()
        assertNotNull(detail)
        assertEquals(2, detail!!.second.size)
        assertTrue(detail.second.all { it.shareAmount == 100_000L })
    }

    @Test
    fun `ReceiptScannerViewModel blocks VIEWER role from saving quick expense`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(owner, "Quỹ Công Ty").getOrThrow()
        val addedMember = repo.addManualMember(
            groupId = group.groupId,
            actorId = owner.uid,
            displayName = "Khách Xem",
            role = GroupRole.VIEWER
        ).getOrThrow()

        val scannerVm = ReceiptScannerViewModel(
            groupRepository = repo,
            transactionRepository = repo,
            geminiChatRepository = GeminiChatRepository(apiKey = "")
        )
        scannerVm.bindContext(group.groupId, addedMember.userId)
        advanceUntilIdle()

        scannerVm.updateEditableTitle("Hóa đơn test")
        scannerVm.updateEditableAmount("150000")

        var callbackCalled = false
        scannerVm.saveQuickEqualSplit(currentUserId = addedMember.userId) {
            callbackCalled = true
        }
        advanceUntilIdle()

        assertEquals(false, callbackCalled)
        assertNotNull(scannerVm.uiState.value.errorMessage)
        assertTrue(scannerVm.uiState.value.errorMessage!!.contains("Người xem"))
        assertTrue(repo.observeExpenses(group.groupId).first().isEmpty())
    }

    @Test
    fun `SpendTogetherViewModel attachReceiptProposalToChat creates confirmable proposal`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(owner, "Quỹ Cafe").getOrThrow()

        val vm = SpendTogetherViewModel(
            geminiChatRepository = GeminiChatRepository(apiKey = ""),
            spendlyRepository = repo
        )
        vm.setActiveGroupId(group.groupId)
        vm.syncFirebaseUser(owner.uid, owner.fullName, owner.email)
        advanceUntilIdle()

        vm.attachReceiptProposalToChat(
            receipt = sampleReceipt,
            payerName = owner.fullName,
            groupName = group.name
        )
        advanceUntilIdle()

        val lastBotMsg = vm.uiState.value.chatMessages.last()
        val proposal = lastBotMsg.expenseProposal
        assertNotNull(proposal)
        assertEquals("Cơm Tấm Cali", proposal!!.title)
        assertEquals(300_000L, proposal.amount)
        assertEquals(sampleReceipt.savedReceiptPath, proposal.receiptUrl)

        vm.confirmChatExpense(lastBotMsg.id)
        advanceUntilIdle()

        val expenses = repo.observeExpenses(group.groupId).first()
        assertEquals(1, expenses.size)
        assertEquals(sampleReceipt.savedReceiptPath, expenses.first().receiptUrl)
        assertEquals(SpendlyCategory.FOOD, expenses.first().category)
        assertNull(vm.uiState.value.authErrorMessage)
    }

    @Test
    fun `ReceiptScannerViewModel error state exposes retry flag and resetToCamera restores CAMERA`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val scannerVm = ReceiptScannerViewModel(
            groupRepository = repo,
            transactionRepository = repo,
            geminiChatRepository = GeminiChatRepository(apiKey = "")
        )
        scannerVm.reportImagePreparationError("Ảnh bị hỏng")
        assertEquals(ReceiptScanStep.ERROR, scannerVm.uiState.value.step)
        assertEquals("Ảnh bị hỏng", scannerVm.uiState.value.errorMessage)

        scannerVm.resetToCamera()
        assertEquals(ReceiptScanStep.CAMERA, scannerVm.uiState.value.step)
        assertNull(scannerVm.uiState.value.errorMessage)
    }
}
