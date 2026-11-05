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

    // 1. Hồ sơ người dùng
    private val _userProfile = MutableStateFlow(
        UserProfile(
            id = "1",
            name = "Nguyễn Minh Anh",
            initials = "MA",
            phoneNumber = "0912 345 678",
            groupName = "Quỹ Nhà Mình",
            isLeader = true,
            totalBalanceFormatted = "12.680.000 đ"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    // 2. Danh sách thành viên nhóm
    private val _members = MutableStateFlow(
        listOf(
            GroupMember(
                id = "1",
                name = "Minh Anh",
                initials = "MA",
                phoneNumber = "0912 345 678",
                isLeader = true,
                avatarBgColor = Color(0xFFC4E2D3),
                balanceStatus = MemberBalanceStatus.Positive("+1.180.000 đ")
            ),
            GroupMember(
                id = "2",
                name = "Lan Anh",
                initials = "LA",
                phoneNumber = "0908 224 510",
                isLeader = false,
                avatarBgColor = Color(0xFFDEE7A8),
                balanceStatus = MemberBalanceStatus.Positive("+680.000 đ")
            ),
            GroupMember(
                id = "3",
                name = "Tuấn Nam",
                initials = "TN",
                phoneNumber = "0986 420 115",
                isLeader = false,
                avatarBgColor = Color(0xFFCBE3DC),
                balanceStatus = MemberBalanceStatus.Negative("-450.000 đ")
            ),
            GroupMember(
                id = "4",
                name = "Hà My",
                initials = "HM",
                phoneNumber = "0934 881 902",
                isLeader = false,
                avatarBgColor = Color(0xFFF2D7C8),
                balanceStatus = MemberBalanceStatus.Negative("-320.000 đ")
            ),
            GroupMember(
                id = "5",
                name = "Quang Phúc",
                initials = "QP",
                phoneNumber = "0971 639 204",
                isLeader = false,
                avatarBgColor = Color(0xFFD2DEC8),
                balanceStatus = MemberBalanceStatus.Balanced
            )
        )
    )
    val members: StateFlow<List<GroupMember>> = _members.asStateFlow()

    // Thành viên đang được chọn để chỉnh sửa
    private val _selectedMember = MutableStateFlow<GroupMember?>(null)
    val selectedMember: StateFlow<GroupMember?> = _selectedMember.asStateFlow()

    // 3. Danh sách giao dịch
    private val _transactions = MutableStateFlow(
        listOf(
            TransactionRecord(
                id = "tx_1",
                title = "Đi chợ cuối tuần",
                subtitle = "Hôm nay · Lan đã trả",
                amount = 850000L,
                isIncome = false,
                category = ExpenseCategory.FOOD,
                payerId = "2",
                payerName = "Lan Anh",
                dateHeader = "Hôm nay, 24/10"
            ),
            TransactionRecord(
                id = "tx_2",
                title = "Tiền mạng Internet FPT",
                subtitle = "Hôm qua · Nam đã trả",
                amount = 330000L,
                isIncome = false,
                category = ExpenseCategory.HOUSING,
                payerId = "3",
                payerName = "Tuấn Nam",
                dateHeader = "Hôm qua, 23/10"
            ),
            TransactionRecord(
                id = "tx_3",
                title = "Nạp quỹ tháng 10",
                subtitle = "20/10 · Minh Anh nạp quỹ",
                amount = 2000000L,
                isIncome = true,
                category = ExpenseCategory.OTHER,
                payerId = "1",
                payerName = "Minh Anh",
                dateHeader = "20/10/2026"
            ),
            TransactionRecord(
                id = "tx_4",
                title = "Taxi đi làm chung",
                subtitle = "18/10 · Hà My đã trả",
                amount = 145000L,
                isIncome = false,
                category = ExpenseCategory.TRANSPORT,
                payerId = "4",
                payerName = "Hà My",
                dateHeader = "18/10/2026"
            ),
            TransactionRecord(
                id = "tx_5",
                title = "Cà phê họp nhóm đồ án",
                subtitle = "15/10 · Phúc đã trả",
                amount = 185000L,
                isIncome = false,
                category = ExpenseCategory.FOOD,
                payerId = "5",
                payerName = "Quang Phúc",
                dateHeader = "15/10/2026"
            )
        )
    )
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    // 4. Danh sách các khoản nợ quyết toán
    private val _debts = MutableStateFlow(
        listOf(
            DebtItem(
                id = "debt_1",
                debtorName = "Tuấn Nam",
                debtorInitials = "TN",
                debtorAvatarBg = Color(0xFFCBE3DC),
                creditorName = "Lan Anh",
                creditorInitials = "LA",
                creditorAvatarBg = Color(0xFFDEE7A8),
                rawAmount = 450000L,
                amountFormatted = "450.000 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_2",
                debtorName = "Hà My",
                debtorInitials = "HM",
                debtorAvatarBg = Color(0xFFF2D7C8),
                creditorName = "Minh Anh",
                creditorInitials = "MA",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 320000L,
                amountFormatted = "320.000 đ",
                statusText = "Chờ chuyển khoản",
                isPaid = false
            ),
            DebtItem(
                id = "debt_3",
                debtorName = "Quang Phúc",
                debtorInitials = "QP",
                debtorAvatarBg = Color(0xFFD2DEC8),
                creditorName = "Minh Anh",
                creditorInitials = "MA",
                creditorAvatarBg = Color(0xFFC4E2D3),
                rawAmount = 210000L,
                amountFormatted = "210.000 đ",
                statusText = "Đã thanh toán",
                isPaid = true
            )
        )
    )
    val debts: StateFlow<List<DebtItem>> = _debts.asStateFlow()

    // 5. Ngân sách và thu chi
    private val _budget = MutableStateFlow(BudgetState())
    val budget: StateFlow<BudgetState> = _budget.asStateFlow()

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

        // Cập nhật ngân sách
        _budget.update { current ->
            current.copy(
                totalBalance = current.totalBalance - amount,
                spentAmount = current.spentAmount + amount
            )
        }

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
        _budget.update { current ->
            current.copy(
                totalBalance = current.totalBalance + amount,
                depositedAmount = current.depositedAmount + amount
            )
        }
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

    fun login(phoneNumber: String, name: String = "Nguyễn Minh Anh") {
        val initials = name.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()

        _userProfile.update {
            it.copy(
                phoneNumber = phoneNumber,
                name = name,
                initials = if (initials.isNotEmpty()) initials else "MA"
            )
        }
    }
}
