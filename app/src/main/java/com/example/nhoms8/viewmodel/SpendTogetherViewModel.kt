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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
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
        _firebaseUserEmail
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
            firebaseUserEmail = firebaseUserEmail
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
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
}
