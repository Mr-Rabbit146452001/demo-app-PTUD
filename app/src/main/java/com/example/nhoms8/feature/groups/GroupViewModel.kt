package com.example.nhoms8.feature.groups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.GroupActivity
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SpendlyNotification
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.repository.UserRepository
import com.example.nhoms8.domain.usecase.DebtTransfer
import com.example.nhoms8.domain.usecase.SplitCalculationEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

/**
 * Thống kê tài chính tổng hợp cho một quỹ nhóm trên danh sách quỹ.
 */
data class GroupStats(
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val balance: Long = 0L,
    val transactionCount: Int = 0,
    val budgetRatio: Float = 0f
)

/**
 * Trạng thái giao diện cho màn hình danh sách các quỹ nhóm.
 */
data class GroupListUiState(
    val groups: List<SpendlyGroup> = emptyList(),
    val groupStats: Map<String, GroupStats> = emptyMap(),
    val activeGroupId: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
) {
    val filteredGroups: List<SpendlyGroup>
        get() = if (searchQuery.isBlank()) {
            groups
        } else {
            val query = searchQuery.trim()
            groups.filter { group ->
                group.name.contains(query, ignoreCase = true) ||
                    group.description.contains(query, ignoreCase = true) ||
                    group.inviteCode.contains(query, ignoreCase = true)
            }
        }

    fun statsFor(groupId: String): GroupStats = groupStats[groupId] ?: GroupStats()
}

/**
 * Thống kê chi tiêu theo danh mục cho tab Thống kê của nhóm.
 */
data class CategoryBreakdownItem(
    val category: SpendlyCategory,
    val totalAmount: Long,
    val ratio: Float,
    val transactionCount: Int
)

/**
 * Trạng thái chi tiết 6 tab của một quỹ nhóm.
 */
data class GroupDetailUiState(
    val groupId: String = "",
    val group: SpendlyGroup? = null,
    val members: List<SpendlyGroupMember> = emptyList(),
    val expenses: List<SpendlyExpense> = emptyList(),
    val incomes: List<SpendlyIncome> = emptyList(),
    val balances: List<MemberBalance> = emptyList(),
    val activities: List<GroupActivity> = emptyList(),
    val simplifiedTransfers: List<DebtTransfer> = emptyList(),
    val totalIncome: Long = 0L,
    val totalExpense: Long = 0L,
    val groupBalance: Long = 0L,
    val budgetRatio: Float = 0f,
    val categoryBreakdown: List<CategoryBreakdownItem> = emptyList(),
    val memberNamesById: Map<String, String> = emptyMap(),
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Mục hoạt động nhóm kèm tên quỹ phục vụ hiển thị trong màn hình Thông báo.
 */
data class NotificationActivityItem(
    val activity: GroupActivity,
    val groupName: String
)

private data class OperationStatus(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

private data class GroupCoreSnapshot(
    val group: SpendlyGroup?,
    val members: List<SpendlyGroupMember>,
    val activities: List<GroupActivity>
)

private data class GroupLedgerSnapshot(
    val expenses: List<SpendlyExpense>,
    val incomes: List<SpendlyIncome>,
    val balances: List<MemberBalance>
)

private object DefaultSpendlyRepositoryHolder {
    val instance: FirestoreSpendlyRepository by lazy { FirestoreSpendlyRepository() }
}

/**
 * ViewModel quản lý đa quỹ nhóm, thành viên, chi tiết 6 tab và thông báo cho Spendly.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GroupViewModel @JvmOverloads constructor(
    private val userRepository: UserRepository = DefaultSpendlyRepositoryHolder.instance,
    private val groupRepository: GroupRepository = DefaultSpendlyRepositoryHolder.instance,
    private val transactionRepository: TransactionRepository = DefaultSpendlyRepositoryHolder.instance,
    autoBindDefaultUser: Boolean = true
) : ViewModel() {

    private val _currentUser = MutableStateFlow(
        if (autoBindDefaultUser) DEFAULT_OFFLINE_USER else SpendlyUser()
    )
    val currentUser: StateFlow<SpendlyUser> = _currentUser.asStateFlow()

    private val _activeGroupId = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _isBootstrapping = MutableStateFlow(autoBindDefaultUser)
    private val _hasLoadedGroups = MutableStateFlow(!autoBindDefaultUser)
    private val _operationStatus = MutableStateFlow(OperationStatus())

    private val groupDetailFlows = ConcurrentHashMap<String, StateFlow<GroupDetailUiState>>()
    private var bootstrapJob: Job? = null

    private val userProfileSyncFlow: Flow<SpendlyUser?> = _currentUser
        .map { it.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(null)
            } else {
                userRepository.observeUserProfile(uid).catch { emit(null) }
            }
        }

    private val groupsWithStatsFlow: Flow<Pair<List<SpendlyGroup>, Map<String, GroupStats>>> =
        _currentUser
            .map { it.uid }
            .distinctUntilChanged()
            .flatMapLatest { uid ->
                if (uid.isBlank()) {
                    _hasLoadedGroups.value = true
                    flowOf(emptyList<SpendlyGroup>() to emptyMap())
                } else {
                    groupRepository.observeUserGroups(uid)
                        .catch { error ->
                            _operationStatus.update {
                                it.copy(errorMessage = error.message ?: "Không thể tải danh sách quỹ")
                            }
                            emit(emptyList())
                        }
                        .flatMapLatest { groups ->
                            _hasLoadedGroups.value = true
                            syncActiveGroupSelection(groups)
                            if (groups.isEmpty()) {
                                flowOf(emptyList<SpendlyGroup>() to emptyMap())
                            } else {
                                val perGroupFlows = groups.map { group ->
                                    combine(
                                        transactionRepository.observeExpenses(group.groupId)
                                            .catch { emit(emptyList()) },
                                        transactionRepository.observeIncomes(group.groupId)
                                            .catch { emit(emptyList()) }
                                    ) { expenses, incomes ->
                                        group.groupId to computeGroupStats(group, expenses, incomes)
                                    }
                                }
                                combine(perGroupFlows) { statsEntries ->
                                    groups to statsEntries.toMap()
                                }
                            }
                        }
                }
            }

    private val listMetaFlow = combine(
        _activeGroupId,
        _searchQuery,
        _isBootstrapping,
        _hasLoadedGroups,
        _operationStatus
    ) { activeId, query, bootstrapping, hasLoaded, opStatus ->
        ListMetaState(
            activeGroupId = activeId,
            searchQuery = query,
            isBootstrapping = bootstrapping,
            hasLoadedGroups = hasLoaded,
            operationStatus = opStatus
        )
    }

    val groupsUiState: StateFlow<GroupListUiState> = combine(
        groupsWithStatsFlow,
        listMetaFlow
    ) { (groups, statsMap), meta ->
        GroupListUiState(
            groups = groups,
            groupStats = statsMap,
            activeGroupId = meta.activeGroupId ?: groups.firstOrNull()?.groupId,
            searchQuery = meta.searchQuery,
            isLoading = !meta.hasLoadedGroups || (meta.isBootstrapping && groups.isEmpty()),
            isSubmitting = meta.operationStatus.isSubmitting,
            errorMessage = meta.operationStatus.errorMessage,
            successMessage = meta.operationStatus.successMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = GroupListUiState()
    )

    val notifications: StateFlow<List<SpendlyNotification>> = _currentUser
        .map { it.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(emptyList())
            } else {
                userRepository.observeNotifications(uid).catch { emit(emptyList()) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    val unreadNotificationsCount: StateFlow<Int> = notifications
        .map { list -> list.count { !it.isRead } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = 0
        )

    val recentActivities: StateFlow<List<NotificationActivityItem>> = _currentUser
        .map { it.uid }
        .distinctUntilChanged()
        .flatMapLatest { uid ->
            if (uid.isBlank()) {
                flowOf(emptyList())
            } else {
                groupRepository.observeUserGroups(uid)
                    .catch { emit(emptyList()) }
                    .flatMapLatest { groups ->
                        if (groups.isEmpty()) {
                            flowOf(emptyList())
                        } else {
                            val activityFlows = groups.map { group ->
                                groupRepository.observeGroupActivities(group.groupId)
                                    .catch { emit(emptyList()) }
                                    .map { items ->
                                        items.map { activity ->
                                            NotificationActivityItem(
                                                activity = activity,
                                                groupName = group.name
                                            )
                                        }
                                    }
                            }
                            combine(activityFlows) { chunks ->
                                chunks.asSequence()
                                    .flatMap { it.asSequence() }
                                    .sortedByDescending { it.activity.createdAtMillis }
                                    .take(30)
                                    .toList()
                            }
                        }
                    }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            userProfileSyncFlow.collect { profile ->
                if (profile != null && profile.uid == _currentUser.value.uid) {
                    _currentUser.value = profile
                }
            }
        }
        if (autoBindDefaultUser) {
            bindUser(DEFAULT_OFFLINE_USER)
        }
    }

    /**
     * Xóa sạch trạng thái phiên nhóm khi người dùng đăng xuất.
     */
    fun resetSession() {
        bootstrapJob?.cancel()
        _currentUser.value = SpendlyUser()
        _activeGroupId.value = null
        _searchQuery.value = ""
        _hasLoadedGroups.value = true
        _isBootstrapping.value = false
        _operationStatus.value = OperationStatus()
        groupDetailFlows.clear()
    }

    /**
     * Gắn người dùng hiện tại, đồng bộ hồ sơ lên Firestore và tự khởi tạo quỹ mẫu nếu chưa có nhóm nào.
     */
    fun bindUser(user: SpendlyUser) {
        val sanitized = if (user.uid.isBlank()) DEFAULT_OFFLINE_USER else user
        val isUserChanged = _currentUser.value.uid != sanitized.uid
        _currentUser.value = sanitized
        if (isUserChanged) {
            _activeGroupId.value = null
            _hasLoadedGroups.value = false
        }

        bootstrapJob?.cancel()
        bootstrapJob = viewModelScope.launch {
            _isBootstrapping.value = true
            val ensuredResult = userRepository.ensureUserProfile(
                uid = sanitized.uid,
                fullName = sanitized.fullName,
                email = sanitized.email,
                phone = sanitized.phone
            )
            val resolvedUser = ensuredResult.getOrNull() ?: sanitized
            if (_currentUser.value.uid == resolvedUser.uid) {
                _currentUser.value = resolvedUser
            }
            groupRepository.seedStarterGroupIfEmpty(resolvedUser)
                .onSuccess { seeded ->
                    if (seeded != null && _activeGroupId.value == null) {
                        _activeGroupId.value = seeded.groupId
                    }
                }
                .onFailure { error ->
                    _operationStatus.update {
                        it.copy(errorMessage = error.message)
                    }
                }
            _isBootstrapping.value = false
        }
    }

    /**
     * Theo dõi trạng thái chi tiết 6 tab của một quỹ nhóm theo thời gian thực.
     */
    fun observeGroupDetail(groupId: String): StateFlow<GroupDetailUiState> {
        if (groupId.isBlank()) {
            return MutableStateFlow(GroupDetailUiState(isLoading = false))
        }
        return groupDetailFlows.getOrPut(groupId) {
            val coreFlow = combine(
                groupRepository.observeGroup(groupId).catch { emit(null) },
                groupRepository.observeGroupMembers(groupId).catch { emit(emptyList()) },
                groupRepository.observeGroupActivities(groupId).catch { emit(emptyList()) }
            ) { group, members, activities ->
                GroupCoreSnapshot(group = group, members = members, activities = activities)
            }

            val ledgerFlow = combine(
                transactionRepository.observeExpenses(groupId).catch { emit(emptyList()) },
                transactionRepository.observeIncomes(groupId).catch { emit(emptyList()) },
                transactionRepository.observeMemberBalances(groupId).catch { emit(emptyList()) }
            ) { expenses, incomes, balances ->
                GroupLedgerSnapshot(expenses = expenses, incomes = incomes, balances = balances)
            }

            combine(coreFlow, ledgerFlow, _operationStatus) { core, ledger, opStatus ->
                buildGroupDetailUiState(
                    groupId = groupId,
                    core = core,
                    ledger = ledger,
                    opStatus = opStatus
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000L),
                initialValue = GroupDetailUiState(groupId = groupId, isLoading = true)
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearMessages() {
        _operationStatus.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun setActiveGroup(groupId: String) {
        if (groupId.isNotBlank()) {
            _activeGroupId.value = groupId
        }
    }

    fun markNotificationRead(notificationId: String) {
        val uid = _currentUser.value.uid
        if (uid.isBlank() || notificationId.isBlank()) return
        viewModelScope.launch {
            userRepository.markNotificationRead(uid, notificationId)
        }
    }

    fun markAllNotificationsRead() {
        val uid = _currentUser.value.uid
        val unreadIds = notifications.value.filter { !it.isRead }.map { it.id }
        if (uid.isBlank() || unreadIds.isEmpty()) return
        viewModelScope.launch {
            unreadIds.forEach { id ->
                userRepository.markNotificationRead(uid, id)
            }
        }
    }

    fun createGroup(
        name: String,
        description: String,
        monthlyBudgetLimit: Long,
        onSuccess: (SpendlyGroup) -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.createGroup(
                owner = _currentUser.value,
                name = name,
                description = description,
                monthlyBudgetLimit = monthlyBudgetLimit
            ).onSuccess { created ->
                _activeGroupId.value = created.groupId
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = "Đã tạo quỹ \"${created.name}\" (Mã: ${created.inviteCode})"
                )
                onSuccess(created)
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể tạo quỹ nhóm mới"
                )
            }
        }
    }

    fun joinGroup(
        inviteCode: String,
        onSuccess: (SpendlyGroup) -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.joinGroupByInviteCode(
                user = _currentUser.value,
                inviteCode = inviteCode
            ).onSuccess { joined ->
                _activeGroupId.value = joined.groupId
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = "Đã tham gia quỹ \"${joined.name}\""
                )
                onSuccess(joined)
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể tham gia quỹ bằng mã mời"
                )
            }
        }
    }

    fun addManualMember(
        groupId: String,
        name: String,
        phone: String = "",
        role: GroupRole = GroupRole.MEMBER,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.addManualMember(
                groupId = groupId,
                actorId = _currentUser.value.uid,
                displayName = name,
                phone = phone,
                role = role
            ).onSuccess { member ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = "Đã thêm thành viên ${member.displayName}"
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể thêm thành viên"
                )
            }
        }
    }

    fun updateMemberRole(
        groupId: String,
        userId: String,
        role: GroupRole,
        canAddExpense: Boolean = role != GroupRole.VIEWER,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.updateMemberRole(
                groupId = groupId,
                targetUserId = userId,
                role = role,
                canAddExpense = canAddExpense
            ).onSuccess {
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = "Đã cập nhật vai trò thành ${role.label}"
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể cập nhật quyền thành viên"
                )
            }
        }
    }

    fun removeOrLeaveGroup(
        groupId: String,
        userId: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.removeOrLeaveGroup(
                groupId = groupId,
                targetUserId = userId
            ).onSuccess {
                val isSelfLeave = userId == _currentUser.value.uid
                if (isSelfLeave && _activeGroupId.value == groupId) {
                    _activeGroupId.value = null
                }
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = if (isSelfLeave) {
                        "Bạn đã rời khỏi quỹ nhóm"
                    } else {
                        "Đã xóa thành viên khỏi nhóm"
                    }
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể cập nhật thành viên nhóm"
                )
            }
        }
    }

    fun updateGroupSettings(
        groupId: String,
        name: String,
        description: String,
        monthlyBudgetLimit: Long,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _operationStatus.value = OperationStatus(isSubmitting = true)
            groupRepository.updateGroupSettings(
                groupId = groupId,
                name = name,
                description = description,
                monthlyBudgetLimit = monthlyBudgetLimit
            ).onSuccess {
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    successMessage = "Đã lưu cài đặt quỹ nhóm"
                )
                onSuccess()
            }.onFailure { error ->
                _operationStatus.value = OperationStatus(
                    isSubmitting = false,
                    errorMessage = error.message ?: "Không thể cập nhật cài đặt quỹ"
                )
            }
        }
    }

    private fun syncActiveGroupSelection(groups: List<SpendlyGroup>) {
        val currentActive = _activeGroupId.value
        val validIds = groups.map { it.groupId }
        if (currentActive == null || currentActive !in validIds) {
            _activeGroupId.value = validIds.firstOrNull()
        }
    }

    private fun computeGroupStats(
        group: SpendlyGroup,
        expenses: List<SpendlyExpense>,
        incomes: List<SpendlyIncome>
    ): GroupStats {
        val activeExpenses = expenses.filter { it.status == RecordStatus.ACTIVE }
        val activeIncomes = incomes.filter { it.status == RecordStatus.ACTIVE }
        val totalExpense = activeExpenses.sumOf { it.amount }
        val totalIncome = activeIncomes.sumOf { it.amount }
        val balance = totalIncome - totalExpense
        val txCount = activeExpenses.size + activeIncomes.size
        val budgetRatio = if (group.monthlyBudgetLimit > 0L) {
            (totalExpense.toFloat() / group.monthlyBudgetLimit.toFloat()).coerceAtLeast(0f)
        } else {
            0f
        }
        return GroupStats(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            transactionCount = txCount,
            budgetRatio = budgetRatio
        )
    }

    private fun buildGroupDetailUiState(
        groupId: String,
        core: GroupCoreSnapshot,
        ledger: GroupLedgerSnapshot,
        opStatus: OperationStatus
    ): GroupDetailUiState {
        val activeExpenses = ledger.expenses.filter { it.status == RecordStatus.ACTIVE }
        val activeIncomes = ledger.incomes.filter { it.status == RecordStatus.ACTIVE }
        val totalExpense = activeExpenses.sumOf { it.amount }
        val totalIncome = activeIncomes.sumOf { it.amount }
        val groupBalance = totalIncome - totalExpense
        val monthlyLimit = core.group?.monthlyBudgetLimit ?: 0L
        val budgetRatio = if (monthlyLimit > 0L) {
            (totalExpense.toFloat() / monthlyLimit.toFloat()).coerceAtLeast(0f)
        } else {
            0f
        }

        val netBalanceMap = ledger.balances.associate { it.userId to it.netBalance }
        val simplifiedTransfers = SplitCalculationEngine.computeGreedySettlements(netBalanceMap)

        val categoryBreakdown = if (totalExpense <= 0L) {
            emptyList()
        } else {
            activeExpenses
                .groupBy { it.category }
                .map { (category, items) ->
                    val sum = items.sumOf { it.amount }
                    CategoryBreakdownItem(
                        category = category,
                        totalAmount = sum,
                        ratio = (sum.toFloat() / totalExpense.toFloat()).coerceIn(0f, 1f),
                        transactionCount = items.size
                    )
                }
                .sortedByDescending { it.totalAmount }
        }

        val nameMap = linkedMapOf<String, String>()
        ledger.expenses.forEach { expense ->
            if (expense.payerId.isNotBlank() && expense.payerName.isNotBlank()) {
                nameMap[expense.payerId] = expense.payerName
            }
        }
        ledger.incomes.forEach { income ->
            if (income.payerId.isNotBlank() && income.payerName.isNotBlank()) {
                nameMap[income.payerId] = income.payerName
            }
        }
        core.activities.forEach { activity ->
            if (activity.actorId.isNotBlank() && activity.actorName.isNotBlank()) {
                nameMap[activity.actorId] = activity.actorName
            }
        }
        core.members.forEach { member ->
            if (member.userId.isNotBlank() && member.displayName.isNotBlank()) {
                nameMap[member.userId] = member.displayName
            }
        }

        return GroupDetailUiState(
            groupId = groupId,
            group = core.group,
            members = core.members,
            expenses = ledger.expenses,
            incomes = ledger.incomes,
            balances = ledger.balances,
            activities = core.activities,
            simplifiedTransfers = simplifiedTransfers,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            groupBalance = groupBalance,
            budgetRatio = budgetRatio,
            categoryBreakdown = categoryBreakdown,
            memberNamesById = nameMap,
            isLoading = false,
            isProcessing = opStatus.isSubmitting,
            errorMessage = opStatus.errorMessage,
            successMessage = opStatus.successMessage
        )
    }

    private data class ListMetaState(
        val activeGroupId: String?,
        val searchQuery: String,
        val isBootstrapping: Boolean,
        val hasLoadedGroups: Boolean,
        val operationStatus: OperationStatus
    )

    companion object {
        val DEFAULT_OFFLINE_USER = SpendlyUser(
            uid = "user_hung",
            fullName = "Lê Văn Hùng",
            email = "hung.le@spendly.vn",
            phone = "0912 345 678",
            isActive = true
        )

        fun factory(
            userRepository: UserRepository,
            groupRepository: GroupRepository,
            transactionRepository: TransactionRepository,
            autoBindDefaultUser: Boolean = true
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return GroupViewModel(
                    userRepository = userRepository,
                    groupRepository = groupRepository,
                    transactionRepository = transactionRepository,
                    autoBindDefaultUser = autoBindDefaultUser
                ) as T
            }
        }
    }
}
