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
import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.domain.usecase.DebtTransfer
import com.example.nhoms8.domain.usecase.EngineSplitMode
import com.example.nhoms8.domain.usecase.ParticipantInput
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import com.example.nhoms8.domain.usecase.SplitValidationResult
import com.example.nhoms8.model.BudgetState
import com.example.nhoms8.model.ChatExpenseProposal
import com.example.nhoms8.model.ChatMessage
import com.example.nhoms8.model.ChatReminderProposal
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * ViewModel trung tâm cho toàn bộ 15 màn hình Jetpack Compose (SpendTogetherViewModel)
 * Tích hợp Firebase Authentication, StateFlow và cầu nối Cloud Firestore cho Trợ lý FinSprout AI
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SpendTogetherViewModel(
    private val repository: SpendTogetherRepository = SpendTogetherRepository(),
    private val firebaseAuthRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val geminiChatRepository: GeminiChatRepository = GeminiChatRepository(),
    spendlyRepository: FirestoreSpendlyRepository? = null
) : ViewModel() {

    private data class ActiveGroupSnapshot(
        val group: SpendlyGroup? = null,
        val members: List<SpendlyGroupMember> = emptyList(),
        val expenses: List<SpendlyExpense> = emptyList(),
        val incomes: List<SpendlyIncome> = emptyList(),
        val balances: List<MemberBalance> = emptyList()
    )

    private data class GroupFinancialSummary(
        val group: SpendlyGroup,
        val members: List<SpendlyGroupMember>,
        val activeExpenses: List<SpendlyExpense>,
        val activeIncomes: List<SpendlyIncome>,
        val totalIncome: Long,
        val totalSpent: Long,
        val availableBalance: Long,
        val monthlyLimit: Long,
        val transfers: List<DebtTransfer>,
        val memberNamesById: Map<String, String>
    )

    private data class BridgeUiSlice(
        val activeGroupId: String? = null,
        val snapshot: ActiveGroupSnapshot = ActiveGroupSnapshot(),
        val unreadNotificationsCount: Int = 0
    )

    // Danh sách thành viên tham gia chia tiền tại màn hình SplitBill
    private val _splitSelection = MutableStateFlow<Map<String, Boolean>>(emptyMap())

    // Trạng thái Firebase Authentication
    private val _authLoading = MutableStateFlow(false)
    private val _authErrorMessage = MutableStateFlow<String?>(null)
    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    private val _firebaseUserEmail = MutableStateFlow<String?>(firebaseAuthRepository.getCurrentUser()?.email)

    // Trạng thái cầu nối Firestore Group & Thông báo
    private val _spendlyRepo = MutableStateFlow<FirestoreSpendlyRepository?>(spendlyRepository)
    private val _activeGroupId = MutableStateFlow<String?>(null)
    private val _activeGroupSnapshot = MutableStateFlow(ActiveGroupSnapshot())
    private val _activeUserUid = MutableStateFlow<String?>(firebaseAuthRepository.getCurrentUser()?.uid)
    private val _unreadNotificationsCount = MutableStateFlow(0)
    private var externalGroupIdSyncJob: Job? = null

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

    private val _bridgeSlice = combine(
        _activeGroupId,
        _activeGroupSnapshot,
        _unreadNotificationsCount
    ) { activeGroupId, snapshot, unreadCount ->
        BridgeUiSlice(
            activeGroupId = activeGroupId,
            snapshot = snapshot,
            unreadNotificationsCount = unreadCount
        )
    }

    init {
        // Tự động đồng bộ tài khoản Firebase nếu đã đăng nhập từ trước
        firebaseAuthRepository.getCurrentUser()?.let { user ->
            val email = user.email ?: ""
            val name = user.displayName?.takeIf { it.isNotBlank() } ?: "Lê Văn Hùng"
            repository.login(email, name)
            syncFirebaseUser(user.uid, name, email)
        }

        // Lắng nghe thời gian thực dữ liệu nhóm Firestore đang hoạt động
        viewModelScope.launch {
            combine(_spendlyRepo, _activeGroupId) { repo, groupId ->
                repo to groupId
            }.flatMapLatest { (repo, groupId) ->
                if (repo == null || groupId.isNullOrBlank()) {
                    flowOf(ActiveGroupSnapshot())
                } else {
                    combine(
                        repo.observeGroup(groupId).catch { emit(null) },
                        repo.observeGroupMembers(groupId).catch { emit(emptyList()) },
                        repo.observeExpenses(groupId).catch { emit(emptyList()) },
                        repo.observeIncomes(groupId).catch { emit(emptyList()) },
                        repo.observeMemberBalances(groupId).catch { emit(emptyList()) }
                    ) { group, members, expenses, incomes, balances ->
                        ActiveGroupSnapshot(
                            group = group,
                            members = members,
                            expenses = expenses,
                            incomes = incomes,
                            balances = balances
                        )
                    }
                }
            }.collect { snapshot ->
                _activeGroupSnapshot.value = snapshot
            }
        }

        // Lắng nghe số lượng thông báo chưa đọc của người dùng hiện tại
        viewModelScope.launch {
            combine(_spendlyRepo, _activeUserUid) { repo, uid ->
                repo to uid
            }.flatMapLatest { (repo, uid) ->
                if (repo == null || uid.isNullOrBlank()) {
                    flowOf(0)
                } else {
                    repo.observeNotifications(uid)
                        .map { notifications -> notifications.count { !it.isRead } }
                        .catch { emit(0) }
                }
            }.collect { count ->
                _unreadNotificationsCount.value = count
            }
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
        _isChatbotTyping,
        _bridgeSlice
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
        val bridgeSlice = params[16] as BridgeUiSlice

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

        val activeGroupName = bridgeSlice.snapshot.group?.name?.takeIf { it.isNotBlank() }
            ?: userProfile.groupName

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
            isChatbotTyping = isChatbotTyping,
            activeGroupId = bridgeSlice.activeGroupId,
            activeGroupName = activeGroupName,
            unreadNotificationsCount = bridgeSlice.unreadNotificationsCount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = SpendTogetherUiState()
    )

    // ==================== CẦU NỐI FIRESTORE GROUP & SERVICE LOCATOR ====================

    /**
     * Gắn ViewModel với kho dữ liệu FirestoreSpendlyRepository và luồng activeGroupId dùng chung.
     */
    fun bindToSpendlyRepository(
        spendlyRepo: FirestoreSpendlyRepository,
        activeGroupIdFlow: StateFlow<String?>
    ) {
        _spendlyRepo.value = spendlyRepo
        _activeGroupId.value = activeGroupIdFlow.value?.takeIf { it.isNotBlank() }

        externalGroupIdSyncJob?.cancel()
        externalGroupIdSyncJob = viewModelScope.launch {
            activeGroupIdFlow.collect { groupId ->
                _activeGroupId.value = groupId?.takeIf { it.isNotBlank() }
            }
        }

        _activeUserUid.value?.takeIf { it.isNotBlank() }?.let { uid ->
            val profile = repository.userProfile.value
            val email = _firebaseUserEmail.value ?: profile.phoneNumber
            syncFirebaseUser(uid, profile.name, email)
        }
    }

    /**
     * Cập nhật trực tiếp nhóm/quỹ đang hoạt động cho ViewModel.
     */
    fun setActiveGroupId(groupId: String?) {
        _activeGroupId.value = groupId?.takeIf { it.isNotBlank() }
    }

    /**
     * Đồng bộ hồ sơ người dùng Firebase lên collection `users/{uid}` và kích hoạt đếm thông báo chưa đọc.
     */
    fun syncFirebaseUser(uid: String, name: String, email: String) {
        if (uid.isBlank()) return
        _activeUserUid.value = uid
        val repo = _spendlyRepo.value ?: return
        viewModelScope.launch {
            repo.ensureUserProfile(
                uid = uid,
                fullName = name,
                email = email
            )
        }
    }

    /**
     * Theo dõi số lượng thông báo chưa đọc theo thời gian thực cho người dùng [uid].
     */
    fun observeUnreadNotificationsCount(uid: String) {
        if (uid.isBlank()) {
            _activeUserUid.value = null
            _unreadNotificationsCount.value = 0
            return
        }
        _activeUserUid.value = uid
    }

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
                    _firebaseUserEmail.value = result.user?.email
                    _authSuccessMessage.value = result.message
                    val displayName = result.user?.displayName?.takeIf { it.isNotBlank() } ?: "Lê Văn Hùng"
                    repository.login(emailOrPhone, displayName)
                    result.user?.let { fbUser ->
                        syncFirebaseUser(
                            uid = fbUser.uid,
                            name = displayName,
                            email = fbUser.email ?: emailOrPhone
                        )
                    }
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
                    _firebaseUserEmail.value = result.user?.email
                    _authSuccessMessage.value = result.message
                    repository.login(emailOrPhone, name)
                    repository.addMember(name, emailOrPhone, isLeader = false)
                    result.user?.let { fbUser ->
                        syncFirebaseUser(
                            uid = fbUser.uid,
                            name = name,
                            email = fbUser.email ?: emailOrPhone
                        )
                    }
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
        _activeUserUid.value = null
        _unreadNotificationsCount.value = 0
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
            val activeSnapshot = resolveActiveSnapshot()

            // 1. Nếu đã cấu hình Google Gemini API Key trong local.properties: Gọi Gemini 2.5 Flash
            if (geminiChatRepository.isApiKeyConfigured()) {
                try {
                    val contextPrompt = buildContextPrompt(activeSnapshot, currentBudget)
                    val recentHistory = _chatMessages.value
                        .dropLast(1) // Bỏ tin nhắn hiện tại vừa thêm
                        .takeLast(6)
                        .map { it.isFromUser to it.text }
                    val geminiResult = geminiChatRepository.sendMessage(
                        userMessage = trimmed,
                        groupContextPrompt = contextPrompt,
                        recentHistory = recentHistory
                    )
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
            val botResponse = processLocalSmartChat(trimmed, currentBudget, activeSnapshot)
            _chatMessages.update { it + botResponse }
            _isChatbotTyping.value = false
        }
    }

    internal suspend fun buildChatbotContextPrompt(): String {
        val snapshot = resolveActiveSnapshot()
        return buildContextPrompt(snapshot, repository.budget.value)
    }

    private suspend fun resolveActiveSnapshot(): ActiveGroupSnapshot {
        val repo = _spendlyRepo.value ?: return ActiveGroupSnapshot()
        val groupId = _activeGroupId.value?.takeIf { it.isNotBlank() } ?: return ActiveGroupSnapshot()
        val cached = _activeGroupSnapshot.value
        if (cached.group?.groupId == groupId && cached.members.isNotEmpty()) {
            return cached
        }
        val fetched = withTimeoutOrNull(1_500L) {
            val group = repo.observeGroup(groupId).first()
            if (group == null) {
                ActiveGroupSnapshot()
            } else {
                ActiveGroupSnapshot(
                    group = group,
                    members = repo.observeGroupMembers(groupId).first(),
                    expenses = repo.observeExpenses(groupId).first(),
                    incomes = repo.observeIncomes(groupId).first(),
                    balances = repo.observeMemberBalances(groupId).first()
                )
            }
        } ?: cached
        if (fetched.group != null) {
            _activeGroupSnapshot.value = fetched
        }
        return fetched
    }

    private fun summarizeGroup(snapshot: ActiveGroupSnapshot): GroupFinancialSummary? {
        val group = snapshot.group ?: return null
        val activeMembers = snapshot.members.filter { it.status == "ACTIVE" }
        val activeExpenses = snapshot.expenses.filter { it.status == RecordStatus.ACTIVE }
        val activeIncomes = snapshot.incomes.filter { it.status == RecordStatus.ACTIVE }
        val totalIncome = activeIncomes.sumOf { it.amount }
        val totalSpent = activeExpenses.sumOf { it.amount }
        val availableBalance = if (totalIncome > 0L) {
            totalIncome - totalSpent
        } else {
            group.monthlyBudgetLimit - totalSpent
        }
        val netBalanceMap = snapshot.balances.associate { it.userId to it.netBalance }
        val transfers = SplitCalculationEngine.computeGreedySettlements(netBalanceMap)
        val memberNamesById = activeMembers.associate { it.userId to it.displayName }

        return GroupFinancialSummary(
            group = group,
            members = activeMembers,
            activeExpenses = activeExpenses,
            activeIncomes = activeIncomes,
            totalIncome = totalIncome,
            totalSpent = totalSpent,
            availableBalance = availableBalance,
            monthlyLimit = group.monthlyBudgetLimit,
            transfers = transfers,
            memberNamesById = memberNamesById
        )
    }

    private fun buildContextPrompt(
        snapshot: ActiveGroupSnapshot,
        fallbackBudget: BudgetState
    ): String {
        val summary = summarizeGroup(snapshot) ?: return buildLegacyContextPrompt(fallbackBudget)

        val membersText = if (summary.members.isNotEmpty()) {
            summary.members.joinToString { "${it.displayName} (${it.role.label})" }
        } else {
            "Chưa có thành viên"
        }
        val recentExpensesText = if (summary.activeExpenses.isNotEmpty()) {
            summary.activeExpenses.take(5).joinToString("; ") { exp ->
                "${exp.title}: ${FintechAlgorithm.formatVnd(exp.amount)} (${exp.payerName} trả, ${exp.category.label})"
            }
        } else {
            "Chưa có khoản chi nào"
        }
        val debtsText = if (summary.transfers.isNotEmpty()) {
            summary.transfers.joinToString("; ") { transfer ->
                val fromName = summary.memberNamesById[transfer.fromUserId] ?: transfer.fromUserId
                val toName = summary.memberNamesById[transfer.toUserId] ?: transfer.toUserId
                "$fromName nợ $toName ${FintechAlgorithm.formatVnd(transfer.amount)}"
            }
        } else {
            "Không có công nợ chưa thanh toán"
        }
        val spentPercent = if (summary.monthlyLimit > 0L) {
            summary.totalSpent * 100L / summary.monthlyLimit
        } else 0L
        val currentUserName = resolveActiveUserName(summary.members)

        return """
            • Nhóm: ${summary.group.name}
            • Mô tả quỹ: ${summary.group.description.ifBlank { "Quỹ chi tiêu nhóm" }}
            • Số dư quỹ hiện tại: ${FintechAlgorithm.formatVnd(summary.availableBalance)}
            • Tổng thu / nạp quỹ: ${FintechAlgorithm.formatVnd(summary.totalIncome)}
            • Đã chi tháng này: ${FintechAlgorithm.formatVnd(summary.totalSpent)} / ${FintechAlgorithm.formatVnd(summary.monthlyLimit)} ($spentPercent%)
            • Danh sách thành viên (${summary.members.size}): $membersText
            • Các khoản chi gần đây: $recentExpensesText
            • Công nợ cần quyết toán: $debtsText
            • Người dùng: $currentUserName
        """.trimIndent()
    }

    private fun buildLegacyContextPrompt(currentBudget: BudgetState): String {
        val currentMembers = repository.members.value.joinToString {
            "${it.name} (${if (it.isLeader) "Trưởng nhóm" else "Thành viên"})"
        }
        return """
            • Nhóm: ${uiState.value.userProfile.groupName}
            • Số dư quỹ hiện tại: ${currentBudget.totalBalanceFormatted}
            • Đã chi tháng này: ${currentBudget.spentAmountFormatted} / ${currentBudget.monthlyLimitFormatted}
            • Danh sách thành viên: $currentMembers
            • Người dùng: ${uiState.value.userProfile.name}
        """.trimIndent()
    }

    /**
     * Bộ xử lý ngôn ngữ tự nhiên thông minh nội bộ (dự phòng khi không có mạng hoặc chưa nhập Gemini API Key)
     */
    private fun processLocalSmartChat(
        trimmed: String,
        currentBudget: BudgetState,
        activeSnapshot: ActiveGroupSnapshot = ActiveGroupSnapshot()
    ): ChatMessage {
        val lower = trimmed.lowercase()
        val groupSummary = summarizeGroup(activeSnapshot)

        // Phân tích tạo khoản chi ("ăn", "uống", "chi", "trả", "k", "đ", "tiền", "mua", "lẩu", "cà phê")
        val amountRegex = Regex("(\\d+[.,]?\\d*)\\s*(k|nghìn|ngàn|tr|triệu|đ|vnd|đồng)?", RegexOption.IGNORE_CASE)
        val match = amountRegex.findAll(trimmed).firstOrNull { m ->
            val unit = m.groupValues[2].lowercase()
            val numVal = m.groupValues[1].replace(",", "").replace(".", "").toDoubleOrNull() ?: 0.0
            unit.isNotEmpty() || numVal >= 1000.0
        }
        val detectedAmount: Long? = match?.let {
            val numPart = it.groupValues[1].replace(",", "").replace(".", "").toDoubleOrNull() ?: return@let null
            val unit = it.groupValues[2].lowercase()
            when {
                unit == "k" || unit == "nghìn" || unit == "ngàn" -> (numPart * 1000).toLong()
                unit == "tr" || unit == "triệu" -> (numPart * 1000000).toLong()
                numPart < 1000 && unit.isNotEmpty() -> (numPart * 1000).toLong()
                else -> numPart.toLong()
            }
        }

        val isReminderRequest = lower.contains("nhắc nợ") ||
            lower.contains("đòi nợ") ||
            lower.contains("nhắc tiền") ||
            lower.contains("soạn tin")
        val isDebtInquiry = !isReminderRequest && (
            lower.contains("ai nợ") ||
                lower.contains("ai đang nợ") ||
                lower.contains("tôi nợ") ||
                lower.contains("tôi đang nợ") ||
                lower.contains("công nợ") ||
                lower.contains("nợ nhiều nhất") ||
                lower.contains("nợ ai")
            )
        val isTopSpenderOrCategoryInquiry = lower.contains("chi nhiều nhất") ||
            lower.contains("tiêu nhiều nhất") ||
            lower.contains("khoản chi lớn nhất") ||
            lower.contains("danh mục") ||
            lower.contains("thành viên")

        val isExpenseIntent = !isReminderRequest && !isDebtInquiry && (detectedAmount != null && detectedAmount > 0) &&
                (lower.contains("ăn") || lower.contains("uống") || lower.contains("chi") || lower.contains("trả") ||
                        lower.contains("mua") || lower.contains("hết") || lower.contains("tiền") || lower.contains("lẩu") ||
                        lower.contains("phở") || lower.contains("cơm") || lower.contains("cafe") || lower.contains("cà phê") ||
                        lower.contains("trà sữa") || lower.contains("vé") || lower.contains("xe") || lower.contains("nhà") ||
                        lower.contains("đi chợ") || lower.contains("khách sạn") || lower.contains("phòng"))

        return when {
            isExpenseIntent -> {
                val category = when {
                    lower.contains("nhà") || lower.contains("điện") || lower.contains("nước") || lower.contains("mạng") || lower.contains("phòng") || lower.contains("khách sạn") -> ExpenseCategory.HOUSING
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

                val payer = if (groupSummary != null && groupSummary.members.isNotEmpty()) {
                    findMentionedMember(trimmed, groupSummary.members)?.displayName
                        ?: resolveActiveUserName(groupSummary.members)
                } else {
                    when {
                        lower.contains("hùng") -> "Lê Văn Hùng"
                        lower.contains("linh") -> "Lý Việt Linh"
                        lower.contains("đạt") -> "Nguyễn Phan Tấn Đạt"
                        lower.contains("bình") -> "Lê Trung Bình"
                        else -> uiState.value.userProfile.name
                    }
                }

                val title = when {
                    lower.contains("lẩu") -> "Ăn lẩu nhóm"
                    lower.contains("cà phê") || lower.contains("cafe") -> "Cà phê gặp mặt"
                    lower.contains("trà sữa") -> "Trà sữa chiều"
                    lower.contains("cơm") -> "Cơm trưa nhóm"
                    lower.contains("bún bò") -> "Bún bò Huế trưa"
                    lower.contains("ăn tối") -> "Ăn tối nhóm"
                    lower.contains("ăn trưa") -> "Ăn trưa nhóm"
                    lower.contains("ăn sáng") -> "Ăn sáng nhóm"
                    lower.contains("đi chợ") || lower.contains("siêu thị") -> "Đi siêu thị đồ dùng"
                    lower.contains("xăng") || lower.contains("xe") -> "Tiền xăng xe chung"
                    else -> "Khoản chi mới (${categoryName})"
                }

                val splitSummary = if (groupSummary != null && groupSummary.members.isNotEmpty()) {
                    "Chia đều cho ${groupSummary.members.size} thành viên (${groupSummary.group.name})"
                } else {
                    "Chia đều cho cả nhóm"
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
                        splitSummary = splitSummary,
                        isConfirmed = false
                    )
                )
            }

            // Hỏi về tình hình quỹ / số dư
            lower.contains("quỹ") || lower.contains("tiền còn") || lower.contains("ngân sách") || lower.contains("tình hình") || lower.contains("sống sót") || lower.contains("báo cáo") -> {
                if (groupSummary != null) {
                    val balanceStr = FintechAlgorithm.formatVnd(groupSummary.availableBalance)
                    val spentStr = FintechAlgorithm.formatVnd(groupSummary.totalSpent)
                    val limitStr = FintechAlgorithm.formatVnd(groupSummary.monthlyLimit)
                    val percent = if (groupSummary.monthlyLimit > 0L) {
                        groupSummary.totalSpent * 100L / groupSummary.monthlyLimit
                    } else 0L

                    ChatMessage(
                        text = "📊 Báo cáo tài chính nhanh từ FinSprout — ${groupSummary.group.name}:\n\n" +
                                "• Quỹ đang hoạt động: ${groupSummary.group.name} (${groupSummary.members.size} thành viên)\n" +
                                "• Số dư quỹ hiện tại: $balanceStr\n" +
                                (if (groupSummary.totalIncome > 0L) "• Tổng thu / nạp quỹ: ${FintechAlgorithm.formatVnd(groupSummary.totalIncome)}\n" else "") +
                                "• Đã chi trong tháng: $spentStr / $limitStr ($percent% hạn mức)\n" +
                                "• Đánh giá: ${if (percent < 80) "Quỹ ${groupSummary.group.name} đang ở mức an toàn! Cả nhóm chi tiêu rất có kế hoạch. 🎉" else "Cảnh báo nhẹ: Quỹ ${groupSummary.group.name} đã chạm $percent% hạn mức tháng, nhóm nên cân nhắc trước các khoản chi lớn nhé! ⚠️"}",
                        isFromUser = false
                    )
                } else {
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
            }

            // Hỏi về công nợ hiện tại (Ai nợ ai?)
            isDebtInquiry -> {
                if (groupSummary != null) {
                    val text = if (groupSummary.transfers.isEmpty()) {
                        "✅ Hiện tại trong quỹ ${groupSummary.group.name} không có khoản nợ nào cần thanh toán! Mọi người đã cân bằng."
                    } else {
                        val lines = groupSummary.transfers.joinToString("\n") { t ->
                            val from = groupSummary.memberNamesById[t.fromUserId] ?: t.fromUserId
                            val to = groupSummary.memberNamesById[t.toUserId] ?: t.toUserId
                            "• $from cần trả $to: ${FintechAlgorithm.formatVnd(t.amount)}"
                        }
                        "⚖️ Tổng hợp công nợ tối ưu của ${groupSummary.group.name}:\n\n$lines"
                    }
                    ChatMessage(text = text, isFromUser = false)
                } else {
                    val unpaid = repository.debts.value.filterNot { it.isPaid }
                    val text = if (unpaid.isEmpty()) {
                        "✅ Hiện tại không còn khoản nợ nào chưa thanh toán!"
                    } else {
                        val lines = unpaid.joinToString("\n") {
                            "• ${it.debtorName} nợ ${it.creditorName}: ${it.amountFormatted}"
                        }
                        "⚖️ Các khoản nợ đang chờ quyết toán:\n\n$lines"
                    }
                    ChatMessage(text = text, isFromUser = false)
                }
            }

            // Hỏi về người chi nhiều nhất / danh mục / thành viên
            isTopSpenderOrCategoryInquiry -> {
                if (groupSummary != null) {
                    val topExp = groupSummary.activeExpenses.maxByOrNull { it.amount }
                    val topCat = groupSummary.activeExpenses.groupBy { it.category }
                        .mapValues { (_, list) -> list.sumOf { it.amount } }
                        .maxByOrNull { it.value }
                    val membersText = groupSummary.members.joinToString { it.displayName }
                    ChatMessage(
                        text = buildString {
                            append("📈 Phân tích nhanh quỹ ${groupSummary.group.name}:\n\n")
                            append("• Thành viên (${groupSummary.members.size}): $membersText\n")
                            if (topCat != null) {
                                append("• Danh mục chi nhiều nhất: ${topCat.key.label} (${FintechAlgorithm.formatVnd(topCat.value)})\n")
                            }
                            if (topExp != null) {
                                append("• Khoản chi lớn nhất: ${topExp.title} — ${FintechAlgorithm.formatVnd(topExp.amount)} (${topExp.payerName} trả)")
                            }
                        }.trim(),
                        isFromUser = false
                    )
                } else {
                    val state = uiState.value
                    val topCat = state.categoryReportItems.maxByOrNull { it.amount }
                    ChatMessage(
                        text = "📈 Phân tích nhanh:\n\n" +
                            "• ${state.highestSpenderNote}\n" +
                            (if (topCat != null && topCat.amount > 0L) "• Danh mục chi nhiều nhất: ${topCat.categoryName} (${topCat.amountFormatted})" else ""),
                        isFromUser = false
                    )
                }
            }

            // Nhắc nợ / đòi nợ
            isReminderRequest || lower.contains("nợ") -> {
                if (groupSummary != null) {
                    val mentionedMember = findMentionedMember(trimmed, groupSummary.members)
                    val matchedTransfer = if (mentionedMember != null) {
                        groupSummary.transfers.firstOrNull { it.fromUserId == mentionedMember.userId }
                    } else {
                        groupSummary.transfers.firstOrNull()
                    }
                    val targetName = mentionedMember?.displayName
                        ?: matchedTransfer?.let { groupSummary.memberNamesById[it.fromUserId] }
                        ?: "bạn"
                    val debtAmountStr = matchedTransfer?.let { FintechAlgorithm.formatVnd(it.amount) }
                        ?: "khoản chi gần nhất"
                    val creditorHint = matchedTransfer?.let {
                        groupSummary.memberNamesById[it.toUserId]
                    }
                    val recipientPhrase = if (creditorHint != null) "cho $creditorHint" else "vào quỹ ${groupSummary.group.name}"

                    val reminderText = "Ting ting! 🧋 $targetName ơi, hôm trước nhóm ${groupSummary.group.name} có chi chung hoạt động. Phần chi phí của bạn là $debtAmountStr nè. Khi nào tiện bạn chuyển khoản $recipientPhrase để chốt sổ nhé! Cảm ơn bạn nhiều ❤️"

                    ChatMessage(
                        text = "FinSprout đã soạn sẵn một mẫu nhắc nợ vừa dễ thương vừa tế nhị cho quỹ ${groupSummary.group.name}! Bạn có thể bấm sao chép để gửi ngay qua Zalo/Messenger nhé: 💌",
                        isFromUser = false,
                        reminderProposal = ChatReminderProposal(
                            recipientName = targetName,
                            amountFormatted = matchedTransfer?.let { FintechAlgorithm.formatVnd(it.amount) } ?: "0 đ",
                            suggestedMessage = reminderText,
                            isCopied = false
                        )
                    )
                } else {
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
                val scopeLabel = groupSummary?.group?.name ?: "nhóm"
                ChatMessage(
                    text = "FinSprout luôn sẵn sàng hỗ trợ $scopeLabel! 🌱 Bạn có thể thử các gợi ý bên dưới như:\n" +
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

        // Ghi vào Legacy Repository để bảo toàn 100% tương thích với các màn hình & unit test hiện tại
        val allMemberIds = repository.members.value.map { it.id }
        val legacyPayerMember = repository.members.value.find {
            it.name.contains(proposal.payerName, ignoreCase = true)
        }
        val legacyPayerId = legacyPayerMember?.id ?: "1"

        repository.addExpenseAndSplit(
            title = proposal.title,
            amount = proposal.amount,
            category = proposal.category,
            note = "Ghi nhanh qua FinSprout AI",
            payerId = legacyPayerId,
            selectedMemberIds = allMemberIds,
            mode = SplitMode.EQUAL
        )

        val repo = _spendlyRepo.value
        val activeGroupId = _activeGroupId.value?.takeIf { it.isNotBlank() }

        if (repo != null && activeGroupId != null) {
            viewModelScope.launch {
                val snapshot = resolveActiveSnapshot()
                val activeMembers = snapshot.members.filter { it.status == "ACTIVE" }
                val groupName = snapshot.group?.name ?: uiState.value.activeGroupName

                if (activeMembers.isEmpty()) {
                    _chatMessages.update { list ->
                        list + ChatMessage(
                            text = "🎉 Đã lưu thành công! Khoản chi ${proposal.title} (${proposal.amountFormatted}) đã được trừ vào quỹ và chia đều cho cả nhóm.",
                            isFromUser = false,
                            timestampFormatted = "Vừa xong"
                        )
                    }
                    return@launch
                }

                val payerMember = findMemberByName(proposal.payerName, activeMembers)
                    ?: activeMembers.firstOrNull { it.userId == _activeUserUid.value }
                    ?: activeMembers.firstOrNull { it.role == GroupRole.OWNER }
                    ?: activeMembers.first()

                val creatorMember = activeMembers.firstOrNull {
                    it.userId == _activeUserUid.value && it.canAddExpense && it.role != GroupRole.VIEWER
                } ?: activeMembers.firstOrNull {
                    it.userId == payerMember.userId && it.canAddExpense && it.role != GroupRole.VIEWER
                } ?: activeMembers.firstOrNull {
                    it.canAddExpense && it.role != GroupRole.VIEWER
                } ?: payerMember

                val splitValidation = SplitCalculationEngine.calculateAndValidateSplit(
                    totalAmount = proposal.amount,
                    mode = EngineSplitMode.EQUAL,
                    participants = activeMembers.map { member ->
                        ParticipantInput(
                            userId = member.userId,
                            displayName = member.displayName,
                            isIncluded = true
                        )
                    }
                )

                val participants = when (splitValidation) {
                    is SplitValidationResult.Valid -> splitValidation.shares.map { share ->
                        ExpenseParticipant(
                            userId = share.userId,
                            displayName = share.displayName,
                            shareAmount = share.shareAmount,
                            percentage = share.percentage
                        )
                    }
                    is SplitValidationResult.Invalid -> {
                        _chatMessages.update { list ->
                            list.map { msg ->
                                if (msg.id == messageId) {
                                    msg.copy(expenseProposal = proposal.copy(isConfirmed = false))
                                } else msg
                            } + ChatMessage(
                                text = "Không thể lưu khoản chi vào quỹ $groupName: ${splitValidation.errorMessage}",
                                isFromUser = false,
                                timestampFormatted = "Vừa xong"
                            )
                        }
                        return@launch
                    }
                }

                val now = System.currentTimeMillis()
                val spendlyExpense = SpendlyExpense(
                    groupId = activeGroupId,
                    title = proposal.title,
                    amount = proposal.amount,
                    category = SpendlyCategory.fromLegacy(proposal.category),
                    payerId = payerMember.userId,
                    payerName = payerMember.displayName,
                    createdBy = creatorMember.userId,
                    splitType = SplitMethod.EQUAL,
                    description = "Ghi nhanh qua FinSprout AI",
                    expenseDateMillis = now,
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    status = RecordStatus.ACTIVE
                )

                val writeResult = repo.createExpenseWithParticipants(
                    groupId = activeGroupId,
                    expense = spendlyExpense,
                    participants = participants
                )

                if (writeResult.isSuccess) {
                    _chatMessages.update { list ->
                        list + ChatMessage(
                            text = "🎉 Đã lưu thành công vào quỹ $groupName! Khoản chi ${proposal.title} (${proposal.amountFormatted}) đã được trừ vào quỹ và chia đều cho ${participants.size} thành viên.",
                            isFromUser = false,
                            timestampFormatted = "Vừa xong"
                        )
                    }
                } else {
                    val errMsg = writeResult.exceptionOrNull()?.localizedMessage ?: "Lỗi không xác định"
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == messageId) {
                                msg.copy(expenseProposal = proposal.copy(isConfirmed = false))
                            } else msg
                        } + ChatMessage(
                            text = "Không thể lưu khoản chi lên Firestore cho quỹ $groupName: $errMsg",
                            isFromUser = false,
                            timestampFormatted = "Vừa xong"
                        )
                    }
                }
            }
        } else {
            // Bot gửi thông báo thành công
            _chatMessages.update { list ->
                list + ChatMessage(
                    text = "🎉 Đã lưu thành công! Khoản chi ${proposal.title} (${proposal.amountFormatted}) đã được trừ vào quỹ và chia đều cho cả nhóm.",
                    isFromUser = false,
                    timestampFormatted = "Vừa xong"
                )
            }
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

    // ==================== HELPER MATCHING THÀNH VIÊN ====================

    private fun resolveActiveUserName(members: List<SpendlyGroupMember>): String {
        val uid = _activeUserUid.value
        if (!uid.isNullOrBlank()) {
            members.firstOrNull { it.userId == uid }?.let { return it.displayName }
        }
        val profileName = uiState.value.userProfile.name
        findMemberByName(profileName, members)?.let { return it.displayName }
        return members.firstOrNull { it.role == GroupRole.OWNER }?.displayName
            ?: members.firstOrNull()?.displayName
            ?: profileName
    }

    private fun findMentionedMember(
        text: String,
        members: List<SpendlyGroupMember>
    ): SpendlyGroupMember? {
        if (members.isEmpty()) return null
        val lower = text.lowercase()
        val fullNameMatch = members
            .filter { it.displayName.isNotBlank() }
            .sortedByDescending { it.displayName.length }
            .firstOrNull { lower.contains(it.displayName.lowercase()) }
        if (fullNameMatch != null) return fullNameMatch

        val wordTokens = lower.split(Regex("[^\\p{L}\\p{N}]+")).filter { it.isNotBlank() }.toSet()
        return members.firstOrNull { member ->
            val shortName = member.displayName.trim()
                .split(Regex("\\s+"))
                .lastOrNull()
                ?.lowercase()
            !shortName.isNullOrBlank() && shortName in wordTokens
        }
    }

    private fun findMemberByName(
        name: String,
        members: List<SpendlyGroupMember>
    ): SpendlyGroupMember? {
        if (name.isBlank() || members.isEmpty()) return null
        val trimmed = name.trim()
        return members.firstOrNull { it.displayName.equals(trimmed, ignoreCase = true) }
            ?: members.firstOrNull {
                it.displayName.contains(trimmed, ignoreCase = true) ||
                    trimmed.contains(it.displayName, ignoreCase = true)
            }
            ?: findMentionedMember(trimmed, members)
    }
}
