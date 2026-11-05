package com.example.nhoms8.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.GroupMember
import com.example.nhoms8.SplitMemberItem
import com.example.nhoms8.SplitMode
import com.example.nhoms8.TransactionDateGroup
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.data.AuthResult
import com.example.nhoms8.data.FirebaseAuthRepository
import com.example.nhoms8.data.SpendTogetherRepository
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatMessage
import com.example.nhoms8.model.ChatReminderProposal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel trung tâm cho toàn bộ 15 màn hình Jetpack Compose (SpendTogetherViewModel)
 * Tích hợp Firebase Authentication & StateFlow
 */
class SpendTogetherViewModel(
    private val repository: SpendTogetherRepository = SpendTogetherRepository(),
    private val firebaseAuthRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    // Danh sách thành viên tham gia chia tiền tại màn hình SplitBill
    private val _splitSelection = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Trạng thái Firebase Authentication
    private val _authLoading = MutableStateFlow(false)
    private val _authErrorMessage = MutableStateFlow<String?>(null)
    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    private val _firebaseUserEmail = MutableStateFlow<String?>(firebaseAuthRepository.getCurrentUser()?.email)

    // Trạng thái Trợ lý AI FinSprout Chatbot
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                text = "Chào bạn! Mình là FinSprout 🌱 - Trợ lý AI của SpendTogether. Mình có thể giúp bạn ghi nhanh chi tiêu bằng lời nói, kiểm tra quỹ nhóm hoặc gợi ý tin nhắn đòi nợ khéo léo. Bạn cần hỗ trợ gì hôm nay?",
                isFromUser = false,
                timestampFormatted = "Vừa xong"
            )
        )
    )
    private val _isChatbotTyping = MutableStateFlow(false)

    init {
        // Tự động đồng bộ tài khoản Firebase nếu đã đăng nhập từ trước
        firebaseAuthRepository.getCurrentUser()?.let { user ->
            val email = user.email ?: ""
            val name = user.displayName ?: "Nguyễn Minh Anh"
            repository.login(email, name)
        }
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<SpendTogetherUiState> = combine(
        repository.userProfile,
        repository.members,
        repository.selectedMember,
        repository.transactions,
        repository.debts,
        repository.budget,
        repository.draftExpense,
        repository.filter,
        repository.autoReminderEnabled,
        _splitSelection,
        _authLoading,
        _authErrorMessage,
        _authSuccessMessage,
        _firebaseUserEmail,
        _chatMessages,
        _isChatbotTyping
    ) { params ->
        val userProfile = params[0] as com.example.nhoms8.model.UserProfile
        val members = params[1] as List<GroupMember>
        val selectedMember = params[2] as GroupMember?
        val transactions = params[3] as List<com.example.nhoms8.model.TransactionRecord>
        val debts = params[4] as List<com.example.nhoms8.DebtItem>
        val budget = params[5] as com.example.nhoms8.model.BudgetState
        val draftExpense = params[6] as com.example.nhoms8.model.DraftExpense
        val filter = params[7] as com.example.nhoms8.model.TransactionFilter
        val autoReminder = params[8] as Boolean
        val splitSelectionMap = params[9] as Map<String, Boolean>
        val authLoading = params[10] as Boolean
        val authErrorMessage = params[11] as String?
        val authSuccessMessage = params[12] as String?
        val firebaseUserEmail = params[13] as String?
        val chatMessages = params[14] as List<ChatMessage>
        val isChatbotTyping = params[15] as Boolean

        // 1. Chuyển đổi giao dịch gần đây cho HomeScreen
        val recentTransactions = transactions.map { it.toRecentTransaction() }

        // 2. Lọc và nhóm giao dịch theo ngày cho TransactionHistoryScreen
        val filteredTx = transactions.filter { tx ->
            val matchQuery = filter.searchQuery.isEmpty() ||
                    tx.title.contains(filter.searchQuery, ignoreCase = true) ||
                    tx.payerName.contains(filter.searchQuery, ignoreCase = true)
            val matchCategory = filter.selectedCategory == null || tx.category == filter.selectedCategory
            val matchMember = filter.selectedMemberId == null || tx.payerId == filter.selectedMemberId
            matchQuery && matchCategory && matchMember
        }

        val groupedByDate = filteredTx.groupBy { it.dateHeader }.map { (header, items) ->
            val netTotal = items.sumOf { if (it.isIncome) it.amount else -it.amount }
            val sign = if (netTotal >= 0) "+" else ""
            TransactionDateGroup(
                dateHeader = header,
                netTotalFormatted = "$sign${FintechAlgorithm.formatVnd(netTotal)}",
                transactions = items.map { it.toHistoryTransactionItem() }
            )
        }

        // 3. Chuẩn bị danh sách thành viên chia tiền cho SplitBillScreen
        val splitMembers = members.map { member ->
            val isSelected = splitSelectionMap[member.id] ?: true
            SplitMemberItem(
                id = member.id,
                name = member.name,
                initials = member.initials,
                avatarBgColor = member.avatarBgColor,
                isSelected = isSelected,
                assignedAmount = 0L
            )
        }

        // 4. Tính toán tổng nợ chưa thanh toán
        val unpaidDebts = debts.filterNot { it.isPaid }
        val totalUnpaidAmount = unpaidDebts.sumOf { it.rawAmount }

        SpendTogetherUiState(
            userProfile = userProfile,
            members = members,
            selectedMember = selectedMember,
            recentTransactions = recentTransactions,
            historyGroups = groupedByDate,
            debts = debts,
            budget = budget,
            draftExpense = draftExpense,
            filter = filter,
            autoReminderEnabled = autoReminder,
            splitMembers = splitMembers,
            totalContributedAmount = "21.500.000 đ",
            unpaidDebtsCount = unpaidDebts.size,
            totalUnpaidDebtFormatted = FintechAlgorithm.formatVnd(totalUnpaidAmount),
            authLoading = authLoading,
            authErrorMessage = authErrorMessage,
            authSuccessMessage = authSuccessMessage,
            firebaseUserEmail = firebaseUserEmail,
            chatMessages = chatMessages,
            isChatbotTyping = isChatbotTyping
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SpendTogetherUiState()
    )

    // ==================== XÁC THỰC FIREBASE AUTH ====================

    fun signInWithFirebase(
        emailOrPhone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            when (val result = firebaseAuthRepository.signIn(emailOrPhone, password)) {
                is AuthResult.Success -> {
                    _authLoading.value = false
                    _firebaseUserEmail.value = result.user.email
                    _authSuccessMessage.value = result.message
                    val displayName = result.user.displayName ?: "Nguyễn Minh Anh"
                    repository.login(emailOrPhone, displayName)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authLoading.value = false
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun signUpWithFirebase(
        name: String,
        emailOrPhone: String,
        password: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            when (val result = firebaseAuthRepository.signUp(emailOrPhone, password, name)) {
                is AuthResult.Success -> {
                    _authLoading.value = false
                    _firebaseUserEmail.value = result.user.email
                    _authSuccessMessage.value = result.message
                    repository.login(emailOrPhone, name)
                    repository.addMember(name, emailOrPhone, isLeader = false)
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authLoading.value = false
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun sendPasswordReset(emailOrPhone: String, onDone: (String) -> Unit) {
        viewModelScope.launch {
            when (val result = firebaseAuthRepository.sendPasswordReset(emailOrPhone)) {
                is AuthResult.Success -> onDone(result.message)
                is AuthResult.Error -> onDone(result.message)
            }
        }
    }

    fun signOutFromFirebase() {
        firebaseAuthRepository.signOut()
        _firebaseUserEmail.value = null
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    // ==================== HÀNH ĐỘNG TƯƠNG TÁC (USER INTENTS) ====================

    fun onLogin(phoneNumber: String, name: String = "Nguyễn Minh Anh") {
        repository.login(phoneNumber, name)
    }

    fun onSignUp(name: String, phoneNumber: String) {
        repository.login(phoneNumber, name)
        repository.addMember(name, phoneNumber, isLeader = false)
    }

    fun onSelectMemberForEdit(member: GroupMember) {
        repository.selectMemberForEdit(member)
    }

    fun onSaveMember(name: String, phone: String, isLeader: Boolean, canAddExpense: Boolean) {
        val selected = uiState.value.selectedMember
        if (selected != null) {
            repository.updateMember(selected.id, name, phone, isLeader, canAddExpense)
        }
    }

    fun onDeleteMember(memberId: String) {
        repository.deleteMember(memberId)
    }

    fun onAddMember(name: String, phone: String, isLeader: Boolean = false) {
        repository.addMember(name, phone, isLeader)
    }

    fun onCreateExpenseDraft(amountStr: String, categoryStr: String, note: String) {
        val cleanAmount = amountStr.replace(".", "").replace(",", "").trim().toLongOrNull() ?: 850000L
        val category = when (categoryStr) {
            "Ăn uống" -> ExpenseCategory.FOOD
            "Nhà ở" -> ExpenseCategory.HOUSING
            "Đi lại" -> ExpenseCategory.TRANSPORT
            else -> ExpenseCategory.OTHER
        }
        val title = if (note.isNotBlank()) note else "Khoản chi mới"
        repository.setDraftExpense(title, cleanAmount, category, note)
    }

    fun onToggleSplitMember(memberId: String) {
        val current = _splitSelection.value.toMutableMap()
        val currentStatus = current[memberId] ?: true
        current[memberId] = !currentStatus
        _splitSelection.value = current
    }

    fun onConfirmSplitBill(selectedCount: Int, perPersonAmount: Long) {
        val draft = uiState.value.draftExpense
        val selectedIds = uiState.value.splitMembers.filter { it.isSelected }.map { it.id }

        repository.addExpenseAndSplit(
            title = draft.title,
            amount = draft.amount,
            category = draft.category,
            note = draft.note,
            payerId = "1",
            selectedMemberIds = selectedIds,
            mode = SplitMode.EQUAL
        )
    }

    fun onSettleDebt(debtId: String) {
        repository.settleDebt(debtId)
    }

    fun onToggleAutoReminder(enabled: Boolean) {
        repository.toggleAutoReminder(enabled)
    }

    fun onTopUpFund(amount: Long) {
        repository.topUpFund(amount)
    }

    fun onSearchHistory(query: String) {
        repository.updateSearchQuery(query)
    }

    fun onFilterCategory(category: ExpenseCategory?) {
        repository.setFilterCategory(category)
    }

    fun onClearFilter() {
        repository.clearFilter()
    }

    // ==================== TRỢ LÝ ẢO FINSPROUT CHATBOT ====================

    fun sendMessageToChatbot(userText: String) {
        if (userText.isBlank()) return
        val trimmed = userText.trim()
        val userMsg = ChatMessage(text = trimmed, isFromUser = true)
        _chatMessages.update { it + userMsg }

        viewModelScope.launch {
            _isChatbotTyping.value = true
            delay(500) // Tạo độ trễ tự nhiên giả lập phản hồi AI

            val currentBudget = repository.budget.value
            val lower = trimmed.lowercase()

            // 1. Phân tích tạo khoản chi ("ăn", "uống", "chi", "trả", "k", "đ", "tiền", "mua", "lẩu", "cà phê")
            val amountRegex = Regex("(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|tr|triệu|đ|vnd)?", RegexOption.IGNORE_CASE)
            val match = amountRegex.findAll(trimmed).firstOrNull()
            val detectedAmount: Long? = match?.let {
                val numPart = it.groupValues[1].replace(",", "").replace(".", "").toDoubleOrNull() ?: return@let null
                val unit = it.groupValues[2].lowercase()
                when {
                    unit == "k" || unit == "nghìn" || unit == "ngàn" -> (numPart * 1000).toLong()
                    unit == "tr" || unit == "triệu" -> (numPart * 1000000).toLong()
                    numPart < 1000 -> (numPart * 1000).toLong()
                    else -> numPart.toLong()
                }
            }

            val isExpenseIntent = (detectedAmount != null && detectedAmount > 0) &&
                    (lower.contains("ăn") || lower.contains("uống") || lower.contains("chi") || lower.contains("trả") ||
                            lower.contains("mua") || lower.contains("hết") || lower.contains("tiền") || lower.contains("lẩu") ||
                            lower.contains("phở") || lower.contains("cơm") || lower.contains("cafe") || lower.contains("trà sữa") ||
                            lower.contains("vé") || lower.contains("xe") || lower.contains("nhà") || lower.contains("đi chợ"))

            val botResponse: ChatMessage = when {
                isExpenseIntent -> {
                    val category = when {
                        lower.contains("nhà") || lower.contains("điện") || lower.contains("nước") || lower.contains("mạng") -> ExpenseCategory.HOUSING
                        lower.contains("xe") || lower.contains("taxi") || lower.contains("xăng") || lower.contains("grab") -> ExpenseCategory.TRANSPORT
                        lower.contains("mua") || lower.contains("quần") || lower.contains("áo") -> ExpenseCategory.OTHER
                        else -> ExpenseCategory.FOOD
                    }
                    val categoryName = when (category) {
                        ExpenseCategory.FOOD -> "Ăn uống"
                        ExpenseCategory.HOUSING -> "Nhà ở & Tiện ích"
                        ExpenseCategory.TRANSPORT -> "Đi lại & Di chuyển"
                        ExpenseCategory.OTHER -> "Chi tiêu khác"
                    }

                    val payer = when {
                        lower.contains("hùng") -> "Lê Văn Hùng"
                        lower.contains("linh") -> "Lý Việt Linh"
                        lower.contains("đạt") -> "Nguyễn Phan Tấn Đạt"
                        lower.contains("bình") -> "Lê Trung Bình"
                        lower.contains("nam") -> "Tuấn Nam"
                        lower.contains("my") -> "Hà My"
                        else -> uiState.value.userProfile.name
                    }

                    val title = when {
                        lower.contains("lẩu") -> "Ăn lẩu nhóm"
                        lower.contains("cà phê") || lower.contains("cafe") -> "Cà phê gặp mặt"
                        lower.contains("trà sữa") -> "Trà sữa chiều"
                        lower.contains("cơm") -> "Cơm trưa nhóm"
                        lower.contains("bún bò") -> "Bún bò Huế trưa"
                        lower.contains("đi chợ") || lower.contains("siêu thị") -> "Đi siêu thị đồ dùng"
                        lower.contains("xăng") || lower.contains("xe") -> "Tiền xăng xe chung"
                        else -> "Khoản chi mới (${categoryName})"
                    }

                    ChatMessage(
                        text = "FinSprout đã nhận diện thông tin khoản chi! 🌿 Hãy kiểm tra lại và bấm Xác nhận lưu bên dưới nhé:",
                        isFromUser = false,
                        expenseProposal = ChatExpenseProposal(
                            title = title,
                            amount = detectedAmount,
                            amountFormatted = FintechAlgorithm.formatVnd(detectedAmount),
                            category = category,
                            categoryName = categoryName,
                            payerName = payer,
                            splitSummary = "Chia đều cho cả nhóm",
                            isConfirmed = false
                        )
                    )
                }

                // 2. Hỏi về tình hình quỹ / số dư
                lower.contains("quỹ") || lower.contains("tiền còn") || lower.contains("ngân sách") || lower.contains("tình hình") || lower.contains("sống sót") || lower.contains("báo cáo") -> {
                    val balanceStr = currentBudget.totalBalanceFormatted
                    val spentStr = currentBudget.spentAmountFormatted
                    val limitStr = currentBudget.monthlyLimitFormatted
                    val percent = if (currentBudget.monthlyLimit > 0) (currentBudget.spentAmount * 100 / currentBudget.monthlyLimit) else 0

                    ChatMessage(
                        text = "📊 Báo cáo tài chính nhanh từ FinSprout:\n\n" +
                                "• Số dư quỹ hiện tại: $balanceStr\n" +
                                "• Đã chi trong tháng: $spentStr / $limitStr ($percent% hạn mức)\n" +
                                "• Đánh giá: ${if (percent < 80) "Quỹ nhóm đang ở mức an toàn! Cả nhóm chi tiêu rất có kế hoạch. 🎉" else "Cảnh báo nhẹ: Đã chạm 80% hạn mức tháng, nhóm nên cân nhắc trước các buổi tiệc lớn nhé! ⚠️"}",
                        isFromUser = false
                    )
                }

                // 3. Nhắc nợ / đòi nợ
                lower.contains("nhắc nợ") || lower.contains("đòi nợ") || lower.contains("nhắc tiền") || lower.contains("nợ") -> {
                    val targetName = when {
                        lower.contains("linh") -> "Linh"
                        lower.contains("hùng") -> "Hùng"
                        lower.contains("đạt") -> "Đạt"
                        lower.contains("bình") -> "Bình"
                        lower.contains("nam") -> "Nam"
                        else -> "bạn"
                    }

                    val reminderText = "Ting ting! 🧋 $targetName ơi, hôm trước nhóm mình đi ăn chung vui quá. Phần chi phí của bạn là 120.000 đ nè. Khi nào tiện bạn chuyển khoản vào quỹ nhóm để thủ quỹ chốt sổ tháng nhé! Cảm ơn bạn nhiều ❤️"

                    ChatMessage(
                        text = "FinSprout đã soạn sẵn một mẫu nhắc nợ vừa dễ thương vừa tế nhị cho bạn này! Bạn có thể bấm sao chép để gửi ngay qua Zalo/Messenger nhé: 💌",
                        isFromUser = false,
                        reminderProposal = ChatReminderProposal(
                            recipientName = targetName,
                            amountFormatted = "120.000 đ",
                            suggestedMessage = reminderText,
                            isCopied = false
                        )
                    )
                }

                // 4. Mẹo tiết kiệm
                lower.contains("mẹo") || lower.contains("tiết kiệm") || lower.contains("lời khuyên") -> {
                    ChatMessage(
                        text = "💡 3 Mẹo vàng tiết kiệm chi tiêu nhóm từ FinSprout:\n\n" +
                                "1. Quy tắc 48h: Chờ 2 ngày trước khi quyết định mua sắm đồ dùng chung đắt tiền.\n" +
                                "2. Chốt sổ định kỳ hàng tuần: Dùng tính năng Quyết toán nợ để giảm thiểu số lần chuyển tiền lòng vòng.\n" +
                                "3. Đặt hạn mức cảnh báo: Theo dõi ngưỡng 80% ngân sách tại màn hình Báo cáo để tránh bị động cuối tháng!",
                        isFromUser = false
                    )
                }

                // 5. Mặc định
                else -> {
                    ChatMessage(
                        text = "FinSprout luôn sẵn sàng hỗ trợ! 🌱 Bạn có thể thử các gợi ý bên dưới như:\n" +
                                "• Nhắn 'Trưa nay ăn bún bò 180k Hùng trả' để ghi sổ tự động\n" +
                                "• Hỏi 'Tình hình quỹ tháng này' để xem số dư tức thì\n" +
                                "• Nhờ 'Viết tin nhắn nhắc nợ bạn Linh' để đòi tiền khéo léo!",
                        isFromUser = false
                    )
                }
            }

            _chatMessages.update { it + botResponse }
            _isChatbotTyping.value = false
        }
    }

    fun confirmChatExpense(messageId: String) {
        val message = _chatMessages.value.find { it.id == messageId } ?: return
        val proposal = message.expenseProposal ?: return
        if (proposal.isConfirmed) return

        // Cập nhật trạng thái tin nhắn thành đã xác nhận
        _chatMessages.update { list ->
            list.map { msg ->
                if (msg.id == messageId) {
                    msg.copy(expenseProposal = proposal.copy(isConfirmed = true))
                } else msg
            }
        }

        // Ghi trực tiếp vào Repository
        val allMemberIds = repository.members.value.map { it.id }
        val payerMember = repository.members.value.find { it.name.contains(proposal.payerName, ignoreCase = true) }
        val payerId = payerMember?.id ?: "1"

        repository.addExpenseAndSplit(
            title = proposal.title,
            amount = proposal.amount,
            category = proposal.category,
            note = "Ghi nhanh qua FinSprout AI",
            payerId = payerId,
            selectedMemberIds = allMemberIds,
            mode = SplitMode.EQUAL
        )

        // Bot gửi thông báo thành công
        _chatMessages.update { list ->
            list + ChatMessage(
                text = "🎉 Đã lưu thành công! Khoản chi ${proposal.title} (${proposal.amountFormatted}) đã được trừ vào quỹ và chia đều cho cả nhóm.",
                isFromUser = false,
                timestampFormatted = "Vừa xong"
            )
        }
    }

    fun clearChatHistory() {
        _chatMessages.value = listOf(
            ChatMessage(
                text = "Chào bạn! Mình là FinSprout 🌱 - Trợ lý AI của SpendTogether. Mình có thể giúp bạn ghi nhanh chi tiêu bằng lời nói, kiểm tra quỹ nhóm hoặc gợi ý tin nhắn đòi nợ khéo léo. Bạn cần hỗ trợ gì hôm nay?",
                isFromUser = false,
                timestampFormatted = "Vừa xong"
            )
        )
    }
}
