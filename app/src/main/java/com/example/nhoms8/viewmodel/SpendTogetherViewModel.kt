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
import com.example.nhoms8.data.GeminiChatRepository
import com.example.nhoms8.data.SpendTogetherRepository
import com.example.nhoms8.model.BudgetState
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
    private val firebaseAuthRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val geminiChatRepository: GeminiChatRepository = GeminiChatRepository()
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
                text = "Chào bạn! Mình là FinSprout 🌱 - Trợ lý AI của SpendTogether dành riêng cho Quỹ Chi Tiêu Nhóm 08. Mình có thể giúp bạn ghi nhanh chi tiêu bằng lời nói, kiểm tra quỹ nhóm hoặc gợi ý tin nhắn đòi nợ khéo léo. Bạn cần hỗ trợ gì hôm nay?",
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
            val name = user.displayName ?: "Lê Văn Hùng"
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

        // 5. Tính toán phân bổ chi tiêu theo danh mục (Donut Chart Analytics)
        val expenseTx = transactions.filter { !it.isIncome }
        val totalSpent = expenseTx.sumOf { it.amount }

        val categoryReportItems = ExpenseCategory.entries.map { category ->
            val catAmount = expenseTx.filter { it.category == category }.sumOf { it.amount }
            val percentage = if (totalSpent > 0L) (catAmount.toFloat() / totalSpent) * 100f else 0f
            val sweepAngle = percentage * 3.6f
            val color = when (category) {
                ExpenseCategory.HOUSING -> androidx.compose.ui.graphics.Color(0xFF14382C)
                ExpenseCategory.FOOD -> androidx.compose.ui.graphics.Color(0xFF6E9882)
                ExpenseCategory.TRANSPORT -> androidx.compose.ui.graphics.Color(0xFFDCE8AC)
                ExpenseCategory.OTHER -> androidx.compose.ui.graphics.Color(0xFFD6B98C)
            }
            com.example.nhoms8.model.CategoryReportItem(
                category = category,
                categoryName = category.label,
                amount = catAmount,
                amountFormatted = FintechAlgorithm.formatShortVnd(catAmount),
                percentage = percentage,
                sweepAngle = sweepAngle,
                color = color
            )
        }

        // 6. Tính toán phân bổ chi tiêu theo thành viên (Member Bar Chart)
        val memberSpendingMap = members.associate { member ->
            val spent = expenseTx.filter { it.payerId == member.id }.sumOf { it.amount }
            member.id to spent
        }
        val maxMemberSpent = memberSpendingMap.values.maxOrNull()?.takeIf { it > 0L } ?: 1L

        val memberReportItems = members.map { member ->
            val spent = memberSpendingMap[member.id] ?: 0L
            val ratio = if (maxMemberSpent > 0L) spent.toFloat() / maxMemberSpent else 0f
            val heightRatio = if (spent == 0L) 0.08f else ratio.coerceIn(0.18f, 1.0f)
            val shortName = member.name.trim().split(" ").lastOrNull() ?: member.name
            com.example.nhoms8.model.MemberReportItem(
                memberId = member.id,
                memberName = member.name,
                shortName = shortName,
                amount = spent,
                amountFormatted = FintechAlgorithm.formatShortVnd(spent),
                heightRatio = heightRatio
            )
        }

        val topSpender = members.maxByOrNull { memberSpendingMap[it.id] ?: 0L }
        val topSpenderAmount = topSpender?.let { memberSpendingMap[it.id] } ?: 0L
        val highestSpenderNote = if (topSpender != null && topSpenderAmount > 0L) {
            "${topSpender.name} chi nhiều nhất"
        } else {
            "Chưa có khoản chi"
        }

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
            totalContributedAmount = FintechAlgorithm.formatVnd(budget.depositedAmount),
            unpaidDebtsCount = unpaidDebts.size,
            totalUnpaidDebtFormatted = FintechAlgorithm.formatVnd(totalUnpaidAmount),
            categoryReportItems = categoryReportItems,
            memberReportItems = memberReportItems,
            highestSpenderNote = highestSpenderNote,
            totalSpentFormatted = FintechAlgorithm.formatVnd(totalSpent),
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
                    val displayName = result.user.displayName ?: "Lê Văn Hùng"
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

    fun onLogin(phoneNumber: String, name: String = "Lê Văn Hùng") {
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
            val currentBudget = repository.budget.value

            // 1. Nếu đã cấu hình Google Gemini API Key trong local.properties: Gọi Gemini 1.5 Flash
            if (geminiChatRepository.isApiKeyConfigured()) {
                try {
                    val currentMembers = repository.members.value.joinToString { "${it.name} (${if (it.isLeader) "Trưởng nhóm" else "Thành viên"})" }
                    val contextPrompt = """
                        • Nhóm: ${uiState.value.userProfile.groupName}
                        • Số dư quỹ hiện tại: ${currentBudget.totalBalanceFormatted}
                        • Đã chi tháng này: ${currentBudget.spentAmountFormatted} / ${currentBudget.monthlyLimitFormatted}
                        • Danh sách thành viên: $currentMembers
                        • Người dùng: ${uiState.value.userProfile.name}
                    """.trimIndent()

                    val geminiResult = geminiChatRepository.sendMessage(trimmed, contextPrompt)
                    val botMessage = ChatMessage(
                        text = geminiResult.cleanText,
                        isFromUser = false,
                        expenseProposal = geminiResult.expenseProposal,
                        reminderProposal = geminiResult.reminderProposal
                    )
                    _chatMessages.update { it + botMessage }
                    _isChatbotTyping.value = false
                    return@launch
                } catch (_: Exception) {
                    // Nếu mạng chập chờn hoặc hết quota, tự động fallback sang bộ xử lý thông minh nội bộ
                }
            }

            // 2. Chế độ dự phòng thông minh (Offline / Mock Intelligent Fallback)
            delay(500)
            val botResponse = processLocalSmartChat(trimmed, currentBudget)
            _chatMessages.update { it + botResponse }
            _isChatbotTyping.value = false
        }
    }

    /**
     * Bộ xử lý ngôn ngữ tự nhiên thông minh nội bộ (dự phòng khi không có mạng hoặc chưa nhập Gemini API Key)
     */
    private fun processLocalSmartChat(trimmed: String, currentBudget: BudgetState): ChatMessage {
        val lower = trimmed.lowercase()

        // Phân tích tạo khoản chi ("ăn", "uống", "chi", "trả", "k", "đ", "tiền", "mua", "lẩu", "cà phê")
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

        return when {
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

            // Hỏi về tình hình quỹ / số dư
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

            // Nhắc nợ / đòi nợ
            lower.contains("nhắc nợ") || lower.contains("đòi nợ") || lower.contains("nhắc tiền") || lower.contains("nợ") -> {
                val targetName = when {
                    lower.contains("linh") -> "Linh"
                    lower.contains("hùng") -> "Hùng"
                    lower.contains("đạt") -> "Đạt"
                    lower.contains("bình") -> "Bình"
                    else -> "bạn"
                }

                val matchedDebt = repository.debts.value.find { !it.isPaid && it.debtorName.contains(targetName, ignoreCase = true) }
                val debtAmountStr = matchedDebt?.amountFormatted ?: "khoản chi gần nhất"

                val reminderText = "Ting ting! 🧋 $targetName ơi, hôm trước nhóm mình chi chung hoạt động đồ án. Phần chi phí của bạn là $debtAmountStr nè. Khi nào tiện bạn chuyển khoản vào quỹ nhóm để thủ quỹ chốt sổ nhé! Cảm ơn bạn nhiều ❤️"

                ChatMessage(
                    text = "FinSprout đã soạn sẵn một mẫu nhắc nợ vừa dễ thương vừa tế nhị cho bạn này! Bạn có thể bấm sao chép để gửi ngay qua Zalo/Messenger nhé: 💌",
                    isFromUser = false,
                    reminderProposal = ChatReminderProposal(
                        recipientName = targetName,
                        amountFormatted = matchedDebt?.amountFormatted ?: "200.000 đ",
                        suggestedMessage = reminderText,
                        isCopied = false
                    )
                )
            }

            // Mẹo tiết kiệm
            lower.contains("mẹo") || lower.contains("tiết kiệm") || lower.contains("lời khuyên") -> {
                ChatMessage(
                    text = "💡 3 Mẹo vàng tiết kiệm chi tiêu nhóm từ FinSprout:\n\n" +
                            "1. Quy tắc 48h: Chờ 2 ngày trước khi quyết định mua sắm đồ dùng chung đắt tiền.\n" +
                            "2. Chốt sổ định kỳ hàng tuần: Dùng tính năng Quyết toán nợ để giảm thiểu số lần chuyển tiền lòng vòng.\n" +
                            "3. Đặt hạn mức cảnh báo: Theo dõi ngưỡng 80% ngân sách tại màn hình Báo cáo để tránh bị động cuối tháng!",
                    isFromUser = false
                )
            }

            // Mặc định
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
