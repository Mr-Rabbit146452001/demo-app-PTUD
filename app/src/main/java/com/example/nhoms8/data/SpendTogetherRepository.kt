package com.example.nhoms8.data

import androidx.compose.ui.graphics.Color
import com.example.nhoms8.DebtItem
import com.example.nhoms8.ExpenseCategory
import com.example.nhoms8.GroupMember
import com.example.nhoms8.MemberBalanceStatus
import com.example.nhoms8.SplitMemberItem
import com.example.nhoms8.SplitMode
import com.example.nhoms8.TransactionDateGroup
import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.model.BudgetState
import com.example.nhoms8.model.DraftExpense
import com.example.nhoms8.model.TransactionFilter
import com.example.nhoms8.model.TransactionRecord
import com.example.nhoms8.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Kho dữ liệu trung tâm & Quản lý StateFlow (SpendTogetherRepository)
 * Thiết kế bởi Kỹ sư Dữ liệu & Persistence (data-architecture-engineer) - Nhóm 08
 */
class SpendTogetherRepository {

    // 1. Hồ sơ người dùng đang đăng nhập (Trưởng nhóm: Lê Văn Hùng)
    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "1",
            name = "Lê Văn Hùng",
            initials = "VH",
            phoneNumber = "0912 345 678",
            groupName = "Quỹ Chi Tiêu Nhóm 08",
            isLeader = true,
            totalBalanceFormatted = "0 đ"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // 2. Danh sách 4 thành viên chính thức của Nhóm 08
    private val _members = MutableStateFlow(
        listOf(
            GroupMember(
                id = "1",
                name = "Lê Văn Hùng",
                initials = "VH",
                phoneNumber = "0912 345 678",
                isLeader = true,
                avatarBgColor = Color(0xFFC4E2D3),
                balanceStatus = MemberBalanceStatus.Positive("+637.500 đ")
            ),
            GroupMember(
                id = "2",
                name = "Lý Việt Linh",
                initials = "VL",
                phoneNumber = "0908 224 510",
                isLeader = false,
                avatarBgColor = Color(0xFFDEE7A8),
                balanceStatus = MemberBalanceStatus.Negative("-47.500 đ")
            ),
            GroupMember(
                id = "3",
                name = "Nguyễn Phan Tấn Đạt",
                initials = "TĐ",
                phoneNumber = "0986 420 115",
                isLeader = false,
                avatarBgColor = Color(0xFFCBE3DC),
                balanceStatus = MemberBalanceStatus.Negative("-232.500 đ")
            ),
            GroupMember(
                id = "4",
                name = "Lê Trung Bình",
                initials = "TB",
                phoneNumber = "0934 881 902",
                isLeader = false,
                avatarBgColor = Color(0xFFF2D7C8),
                balanceStatus = MemberBalanceStatus.Negative("-192.500 đ")
            )
        )
    )
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    // Thành viên đang được chọn để chỉnh sửa
    private val _selectedMember = MutableStateFlow<GroupMember?>(null)
    val selectedMember: StateFlow<GroupMember?> = _selectedMember.asStateFlow()

    // 3. Danh sách giao dịch thực tế của Nhóm 08
    private val _transactions = MutableStateFlow(
        listOf(
            TransactionRecord(
                id = "tx_1",
                title = "Mua linh kiện & module đồ án",
                subtitle = "Hôm nay · Hùng đã trả",
                amount = 850000L,
                isIncome = false,
                category = ExpenseCategory.OTHER,
                payerId = "1",
                payerName = "Lê Văn Hùng",
                dateHeader = "Hôm nay"
            ),
            TransactionRecord(
                id = "tx_2",
                title = "Tiền phòng thảo luận & Internet",
                subtitle = "Hôm qua · Linh đã trả",
                amount = 330000L,
                isIncome = false,
                category = ExpenseCategory.HOUSING,
                payerId = "2",
                payerName = "Lý Việt Linh",
                dateHeader = "Hôm qua"
            ),
            TransactionRecord(
                id = "tx_3",
                title = "Đóng quỹ dự án đợt 1",
                subtitle = "01/11 · Hùng nạp quỹ",
                amount = 5000000L,
                isIncome = true,
                category = ExpenseCategory.OTHER,
                payerId = "1",
                payerName = "Lê Văn Hùng",
                dateHeader = "01/11/2026"
            ),
            TransactionRecord(
                id = "tx_4",
                title = "Taxi đi in ấn tài liệu đồ án",
                subtitle = "28/10 · Đạt đã trả",
                amount = 145000L,
                isIncome = false,
                category = ExpenseCategory.TRANSPORT,
                payerId = "3",
                payerName = "Nguyễn Phan Tấn Đạt",
                dateHeader = "28/10/2026"
            ),
            TransactionRecord(
                id = "tx_5",
                title = "Cà phê & nước họp nhóm đồ án",
                subtitle = "25/10 · Bình đã trả",
                amount = 185000L,
                isIncome = false,
                category = ExpenseCategory.FOOD,
                payerId = "4",
                payerName = "Lê Trung Bình",
                dateHeader = "25/10/2026"
            ),
            TransactionRecord(
                id = "tx_6",
                title = "Đóng quỹ dự án đợt 1",
                subtitle = "24/10 · Linh nạp quỹ",
                amount = 5000000L,
                isIncome = true,
                category = ExpenseCategory.OTHER,
                payerId = "2",
                payerName = "Lý Việt Linh",
                dateHeader = "24/10/2026"
            )
        )
    )
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    // 4. Danh sách các khoản nợ quyết toán thực tế
    private val _debts = MutableStateFlow(
        listOf(
            DebtItem(
                id = "debt_1",
                debtorName = "Nguyễn Phan Tấn Đạt",
                debtorInitials = "TĐ",
                debtorAvatarBg = Color(0xFFCBE3DC),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 232500L,
                amountFormatted = "232.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_2",
                debtorName = "Lê Trung Bình",
                debtorInitials = "TB",
                debtorAvatarBg = Color(0xFFF2D7C8),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 192500L,
                amountFormatted = "192.500 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_3",
                debtorName = "Lý Việt Linh",
                debtorInitials = "VL",
                debtorAvatarBg = Color(0xFFDEE7A8),
                creditorName = "Lê Văn Hùng",
                creditorInitials = "VH",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 47500L,
                amountFormatted = "47.500 đ",
                statusText = "Đã thanh toán",
                isPaid = true
            )
        )
    )
    val debts: StateFlow<List<DebtItem>> = _debts.asStateFlow()

    // 5. Ngân sách và thu chi
    private val _budget = MutableStateFlow(BudgetState())
    val budget: StateFlow<BudgetState> = _budget.asStateFlow()

    init {
        recalculateBudgetAndBalances()
    }

    /**
     * Tự động tính toán lại toàn bộ ngân sách, số dư quỹ và công nợ thành viên
     */
    private fun recalculateBudgetAndBalances() {
        val txList = _transactions.value
        val deposited = txList.filter { it.isIncome }.sumOf { it.amount }
        val spent = txList.filter { !it.isIncome }.sumOf { it.amount }
        val balance = deposited - spent

        _budget.value = BudgetState(
            totalBalance = balance,
            depositedAmount = deposited,
            spentAmount = spent,
            monthlyLimit = 10000000L,
            monthLabel = "Tháng 11"
        )

        _userProfile.update {
            it.copy(totalBalanceFormatted = FintechAlgorithm.formatVnd(balance))
        }
    }

    // 6. Khoản chi nháp đang chuẩn bị chia
    private val _draftExpense = MutableStateFlow(DraftExpense())
    val draftExpense: StateFlow<DraftExpense> = _draftExpense.asStateFlow()

    // 7. Bộ lọc lịch sử
    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    // 8. Tự động nhắc nợ
    private val _autoReminderEnabled = MutableStateFlow(true)
    val autoReminderEnabled: StateFlow<Boolean> = _autoReminderEnabled.asStateFlow()

    // ==================== HÀM NGHIỆP VỤ (BUSINESS ACTIONS) ====================

    fun selectMemberForEdit(member: GroupMember) {
        _selectedMember.value = member
    }

    fun updateMember(
        id: String,
        name: String,
        phoneNumber: String,
        isLeader: Boolean,
        canAddExpense: Boolean
    ) {
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()

        _members.update { list ->
            list.map { m ->
                if (m.id == id) {
                    m.copy(
                        name = name,
                        phoneNumber = phoneNumber,
                        isLeader = isLeader,
                        initials = if (initials.isNotEmpty()) initials else m.initials
                    )
                } else m
            }
        }

        // Nếu sửa chính user đang đăng nhập
        if (_userProfile.value.id == id) {
            _userProfile.update {
                it.copy(
                    name = name,
                    phoneNumber = phoneNumber,
                    isLeader = isLeader,
                    initials = if (initials.isNotEmpty()) initials else it.initials
                )
            }
        }
    }

    fun addMember(name: String, phoneNumber: String, isLeader: Boolean = false) {
        val newId = (System.currentTimeMillis() % 10000).toString()
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()

        val palette = listOf(
            Color(0xFFC4E2D3), Color(0xFFDEE7A8), Color(0xFFCBE3DC),
            Color(0xFFF2D7C8), Color(0xFFD2DEC8), Color(0xFFE5D4F1)
        )
        val color = palette[newId.hashCode().mod(palette.size)]

        val newMember = GroupMember(
            id = newId,
            name = name,
            initials = if (initials.isNotEmpty()) initials else "TV",
            phoneNumber = phoneNumber,
            isLeader = isLeader,
            avatarBgColor = color,
            balanceStatus = MemberBalanceStatus.Balanced
        )
        _members.update { it + newMember }
    }

    fun deleteMember(id: String) {
        _members.update { list -> list.filterNot { it.id == id } }
    }

    fun setDraftExpense(title: String, amount: Long, category: ExpenseCategory, note: String) {
        _draftExpense.value = DraftExpense(
            title = title,
            amount = amount,
            category = category,
            note = note
        )
    }

    fun addExpenseAndSplit(
        title: String,
        amount: Long,
        category: ExpenseCategory,
        note: String,
        payerId: String = "1",
        selectedMemberIds: List<String>,
        mode: SplitMode = SplitMode.EQUAL
    ) {
        val currentPayer = _members.value.find { it.id == payerId } ?: _members.value.first()
        val newTx = TransactionRecord(
            id = "tx_${System.currentTimeMillis()}",
            title = title,
            subtitle = "Hôm nay · ${currentPayer.name} đã trả",
            amount = amount,
            isIncome = false,
            category = category,
            payerId = currentPayer.id,
            payerName = currentPayer.name,
            dateHeader = "Hôm nay"
        )

        // Cập nhật danh sách giao dịch
        _transactions.update { listOf(newTx) + it }

        // Cập nhật ngân sách & số dư
        recalculateBudgetAndBalances()

        // Tính toán chia tiền & sinh các khoản nợ mới
        val participantCount = selectedMemberIds.size
        if (participantCount > 0) {
            val splitAmounts = FintechAlgorithm.calculateEqualSplit(amount, participantCount)
            val selectedMembers = _members.value.filter { it.id in selectedMemberIds }

            val newDebts = mutableListOf<DebtItem>()
            selectedMembers.forEachIndexed { index, debtor ->
                if (debtor.id != currentPayer.id) {
                    val share = splitAmounts.getOrElse(index) { amount / participantCount }
                    newDebts.add(
                        DebtItem(
                            id = "debt_${System.currentTimeMillis()}_$index",
                            debtorName = debtor.name,
                            debtorInitials = debtor.initials,
                            debtorAvatarBg = debtor.avatarBgColor,
                            creditorName = currentPayer.name,
                            creditorInitials = currentPayer.initials,
                            creditorAvatarBg = currentPayer.avatarBgColor,
                            rawAmount = share,
                            amountFormatted = FintechAlgorithm.formatVnd(share),
                            statusText = "Chờ chuyển khoản",
                            isPaid = false
                        )
                    )
                }
            }

            if (newDebts.isNotEmpty()) {
                _debts.update { newDebts + it }
            }
        }
    }

    fun topUpFund(amount: Long, note: String = "Nạp tiền quỹ") {
        val currentUser = _userProfile.value
        val newTx = TransactionRecord(
            id = "tx_topup_${System.currentTimeMillis()}",
            title = "Nạp quỹ thêm",
            subtitle = "Hôm nay · ${currentUser.name} nạp",
            amount = amount,
            isIncome = true,
            category = ExpenseCategory.OTHER,
            payerId = currentUser.id,
            payerName = currentUser.name,
            dateHeader = "Hôm nay"
        )

        _transactions.update { listOf(newTx) + it }
        recalculateBudgetAndBalances()
    }

    fun settleDebt(debtId: String) {
        _debts.update { list ->
            list.map { debt ->
                if (debt.id == debtId) {
                    debt.copy(
                        isPaid = true,
                        statusText = "Đã thanh toán"
                    )
                } else debt
            }
        }
    }

    fun toggleAutoReminder(enabled: Boolean) {
        _autoReminderEnabled.value = enabled
    }

    fun updateSearchQuery(query: String) {
        _filter.update { it.copy(searchQuery = query) }
    }

    fun setFilterCategory(category: ExpenseCategory?) {
        _filter.update { it.copy(selectedCategory = category) }
    }

    fun clearFilter() {
        _filter.value = TransactionFilter()
    }

    fun login(phoneNumber: String, name: String = "Lê Văn Hùng") {
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()

        _userProfile.update {
            it.copy(
                phoneNumber = phoneNumber,
                name = name,
                initials = if (initials.isNotEmpty()) initials else "VH"
            )
        }
    }
}
