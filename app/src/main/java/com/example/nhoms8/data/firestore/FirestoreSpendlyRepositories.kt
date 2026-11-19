package com.example.nhoms8.data.firestore

import com.example.nhoms8.algorithm.FintechAlgorithm
import com.example.nhoms8.domain.model.ExpenseParticipant
import com.example.nhoms8.domain.model.GroupActivity
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.MemberBalance
import com.example.nhoms8.domain.model.RecordStatus
import com.example.nhoms8.domain.model.SettlementStatus
import com.example.nhoms8.domain.model.SpendlyBudget
import com.example.nhoms8.domain.model.SpendlyCategory
import com.example.nhoms8.domain.model.SpendlyExpense
import com.example.nhoms8.domain.model.SpendlyGroup
import com.example.nhoms8.domain.model.SpendlyGroupMember
import com.example.nhoms8.domain.model.SpendlyIncome
import com.example.nhoms8.domain.model.SpendlyNotification
import com.example.nhoms8.domain.model.SpendlySettlement
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.domain.model.SplitMethod
import com.example.nhoms8.domain.repository.BudgetRepository
import com.example.nhoms8.domain.repository.GroupRepository
import com.example.nhoms8.domain.repository.SettlementRepository
import com.example.nhoms8.domain.repository.TransactionRepository
import com.example.nhoms8.domain.repository.UserRepository
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.security.SecureRandom
import java.util.UUID

/**
 * Triển khai hợp nhất UserRepository, GroupRepository, TransactionRepository,
 * SettlementRepository và BudgetRepository cho Spendly.
 *
 * - Khi [firestore] != null: đọc/ghi thời gian thực qua Cloud Firestore với WriteBatch/Transaction
 *   nguyên tử cho toàn bộ các thao tác sổ cái (expense + participants + settlements + balances + budgets + activities).
 * - Khi [firestore] == null: sử dụng kho lưu trữ trạng thái trong bộ nhớ (In-Memory StateFlow)
 *   với cùng logic nghiệp vụ, phù hợp cho Unit Test, Compose Preview và chế độ offline fallback.
 */
class FirestoreSpendlyRepository(
    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Throwable) {
        null
    },
    private val clock: () -> Long = { System.currentTimeMillis() }
) : UserRepository, GroupRepository, TransactionRepository, SettlementRepository, BudgetRepository {

    private data class InvitationRecord(
        val inviteCode: String,
        val groupId: String,
        val groupName: String,
        val createdAt: Long
    )

    private data class InMemoryDatabase(
        val users: Map<String, SpendlyUser> = emptyMap(),
        val notifications: Map<String, List<SpendlyNotification>> = emptyMap(),
        val groups: Map<String, SpendlyGroup> = emptyMap(),
        val members: Map<String, Map<String, SpendlyGroupMember>> = emptyMap(),
        val expenses: Map<String, List<SpendlyExpense>> = emptyMap(),
        val participants: Map<String, Map<String, List<ExpenseParticipant>>> = emptyMap(),
        val incomes: Map<String, List<SpendlyIncome>> = emptyMap(),
        val settlements: Map<String, List<SpendlySettlement>> = emptyMap(),
        val budgets: Map<String, List<SpendlyBudget>> = emptyMap(),
        val balances: Map<String, Map<String, MemberBalance>> = emptyMap(),
        val activities: Map<String, List<GroupActivity>> = emptyMap(),
        val invitations: Map<String, InvitationRecord> = emptyMap()
    )

    private val memoryMutex = Mutex()
    private val memoryDb = MutableStateFlow(InMemoryDatabase())
    private val random = SecureRandom()

    // ==================== USER REPOSITORY ====================

    override suspend fun ensureUserProfile(
        uid: String,
        fullName: String,
        email: String,
        phone: String
    ): Result<SpendlyUser> = runCatchingSuspend {
        require(uid.isNotBlank()) { "UID người dùng không được để trống" }
        val now = clock()
        val trimmedEmail = email.trim()
        val trimmedPhone = phone.trim()
        val fallbackName = fullName.trim()
            .ifBlank { trimmedEmail.substringBefore('@').trim() }
            .ifBlank { "Thành viên Spendly" }

        val db = firestore
        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val existing = state.users[uid]
                val resolved = if (existing == null) {
                    SpendlyUser(
                        uid = uid,
                        fullName = fallbackName,
                        email = trimmedEmail,
                        phone = trimmedPhone,
                        avatarUrl = "",
                        createdAtMillis = now,
                        updatedAtMillis = now,
                        isActive = true
                    )
                } else {
                    existing.copy(
                        fullName = existing.fullName.ifBlank { fallbackName },
                        email = existing.email.ifBlank { trimmedEmail },
                        phone = existing.phone.ifBlank { trimmedPhone },
                        updatedAtMillis = now
                    )
                }
                memoryDb.value = state.copy(users = state.users + (uid to resolved))
                resolved
            }
        } else {
            val userRef = db.collection(COLLECTION_USERS).document(uid)
            val snapshot = userRef.get().await()
            val resolved = if (!snapshot.exists()) {
                SpendlyUser(
                    uid = uid,
                    fullName = fallbackName,
                    email = trimmedEmail,
                    phone = trimmedPhone,
                    avatarUrl = "",
                    createdAtMillis = now,
                    updatedAtMillis = now,
                    isActive = true
                )
            } else {
                val existing = snapshot.toSpendlyUser()
                existing.copy(
                    fullName = existing.fullName.ifBlank { fallbackName },
                    email = existing.email.ifBlank { trimmedEmail },
                    phone = existing.phone.ifBlank { trimmedPhone },
                    updatedAtMillis = now
                )
            }
            userRef.set(resolved.toFirestoreMap(), SetOptions.merge()).await()
            resolved
        }
    }

    override fun observeUserProfile(uid: String): Flow<SpendlyUser?> {
        val db = firestore ?: return memoryDb
            .map { it.users[uid] }
            .distinctUntilChanged()

        return callbackFlow {
            if (uid.isBlank()) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }
            val registration = db.collection(COLLECTION_USERS)
                .document(uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val user = if (snapshot != null && snapshot.exists()) {
                        snapshot.toSpendlyUser()
                    } else {
                        null
                    }
                    trySend(user)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun updateUserProfile(
        uid: String,
        fullName: String?,
        phone: String?,
        avatarUrl: String?
    ): Result<Unit> = runCatchingSuspend {
        require(uid.isNotBlank()) { "UID người dùng không được để trống" }
        val now = clock()

        val db = firestore
        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val existing = state.users[uid]
                    ?: throw NoSuchElementException("Không tìm thấy hồ sơ người dùng")
                val updated = existing.copy(
                    fullName = fullName?.trim()?.ifBlank { existing.fullName } ?: existing.fullName,
                    phone = phone?.trim() ?: existing.phone,
                    avatarUrl = avatarUrl?.trim() ?: existing.avatarUrl,
                    updatedAtMillis = now
                )
                memoryDb.value = state.copy(users = state.users + (uid to updated))
            }
        } else {
            val userRef = db.collection(COLLECTION_USERS).document(uid)
            val snapshot = userRef.get().await()
            if (!snapshot.exists()) {
                throw NoSuchElementException("Không tìm thấy hồ sơ người dùng")
            }
            val updates = mutableMapOf<String, Any>(
                "updatedAtMillis" to now
            )
            fullName?.trim()?.takeIf { it.isNotBlank() }?.let { updates["fullName"] = it }
            phone?.trim()?.let { updates["phone"] = it }
            avatarUrl?.trim()?.let { updates["avatarUrl"] = it }
            userRef.update(updates).await()
        }
    }

    override fun observeNotifications(uid: String): Flow<List<SpendlyNotification>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.notifications[uid]
                    .orEmpty()
                    .sortedByDescending { it.createdAtMillis }
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (uid.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toSpendlyNotification() }
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun markNotificationRead(
        uid: String,
        notificationId: String
    ): Result<Unit> = runCatchingSuspend {
        require(uid.isNotBlank() && notificationId.isNotBlank()) {
            "UID và mã thông báo không được để trống"
        }

        val db = firestore
        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val currentList = state.notifications[uid].orEmpty()
                val updatedList = currentList.map { notif ->
                    if (notif.id == notificationId) notif.copy(isRead = true) else notif
                }
                memoryDb.value = state.copy(
                    notifications = state.notifications + (uid to updatedList)
                )
            }
        } else {
            db.collection(COLLECTION_USERS)
                .document(uid)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .document(notificationId)
                .update("isRead", true)
                .await()
        }
    }

    // ==================== GROUP REPOSITORY ====================

    override fun observeUserGroups(uid: String): Flow<List<SpendlyGroup>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.groups.values
                    .filter { it.status == STATUS_ACTIVE && uid in it.memberIds }
                    .sortedByDescending { it.createdAtMillis }
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (uid.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            // Chỉ query đơn trường array-contains để không cần Composite Index trên Firestore
            val registration = db.collection(COLLECTION_GROUPS)
                .whereArrayContains("memberIds", uid)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val groups = snapshot?.documents
                        ?.map { it.toSpendlyGroup() }
                        ?.filter { it.status == STATUS_ACTIVE }
                        ?.sortedByDescending { it.createdAtMillis }
                        .orEmpty()
                    trySend(groups)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeGroup(groupId: String): Flow<SpendlyGroup?> {
        val db = firestore ?: return memoryDb
            .map { it.groups[groupId] }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(null)
                awaitClose { }
                return@callbackFlow
            }
            val registration = db.collection(COLLECTION_GROUPS)
                .document(groupId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val group = if (snapshot != null && snapshot.exists()) {
                        snapshot.toSpendlyGroup()
                    } else {
                        null
                    }
                    trySend(group)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeGroupMembers(groupId: String): Flow<List<SpendlyGroupMember>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.members[groupId]
                    ?.values
                    ?.filter { it.status == STATUS_ACTIVE }
                    ?.sortedBy { it.joinedAtMillis }
                    .orEmpty()
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val members = snapshot?.documents
                        ?.map { it.toSpendlyGroupMember() }
                        ?.filter { it.status == STATUS_ACTIVE }
                        ?.sortedBy { it.joinedAtMillis }
                        .orEmpty()
                    trySend(members)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeGroupActivities(groupId: String): Flow<List<GroupActivity>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.activities[groupId]
                    .orEmpty()
                    .sortedByDescending { it.createdAtMillis }
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES)
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toGroupActivity() }
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun createGroup(
        owner: SpendlyUser,
        name: String,
        description: String,
        monthlyBudgetLimit: Long
    ): Result<SpendlyGroup> = runCatchingSuspend {
        require(owner.uid.isNotBlank()) { "Mã người tạo quỹ không được để trống" }
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Tên quỹ nhóm không được để trống" }
        require(monthlyBudgetLimit > 0L) { "Hạn mức ngân sách hàng tháng phải lớn hơn 0" }

        val now = clock()
        val ownerName = owner.fullName.trim().ifBlank {
            owner.email.substringBefore('@').ifBlank { "Trưởng nhóm" }
        }
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val groupId = newId("grp")
                val inviteCode = generateUniqueInviteCodeInMemory(state.invitations.keys)
                val group = SpendlyGroup(
                    groupId = groupId,
                    name = trimmedName,
                    description = description.trim(),
                    avatarUrl = "",
                    ownerId = owner.uid,
                    inviteCode = inviteCode,
                    memberCount = 1,
                    currency = "VND",
                    monthlyBudgetLimit = monthlyBudgetLimit,
                    createdAtMillis = now,
                    status = STATUS_ACTIVE,
                    memberIds = listOf(owner.uid)
                )
                val ownerMember = SpendlyGroupMember(
                    userId = owner.uid,
                    displayName = ownerName,
                    initials = computeInitials(ownerName),
                    phone = owner.phone,
                    role = GroupRole.OWNER,
                    joinedAtMillis = now,
                    status = STATUS_ACTIVE,
                    nickname = "",
                    canAddExpense = true
                )
                val ownerBalance = MemberBalance(
                    userId = owner.uid,
                    totalPaid = 0L,
                    totalShare = 0L,
                    netBalance = 0L,
                    updatedAtMillis = now
                )
                val invitation = InvitationRecord(
                    inviteCode = inviteCode,
                    groupId = groupId,
                    groupName = trimmedName,
                    createdAt = now
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = owner.uid,
                    actorName = ownerName,
                    action = ACTION_CREATE_GROUP,
                    targetId = groupId,
                    description = "$ownerName đã tạo quỹ $trimmedName",
                    createdAtMillis = now
                )

                val updatedUsers = if (state.users.containsKey(owner.uid)) {
                    state.users
                } else {
                    state.users + (owner.uid to owner.copy(
                        fullName = ownerName,
                        createdAtMillis = if (owner.createdAtMillis > 0L) owner.createdAtMillis else now,
                        updatedAtMillis = now
                    ))
                }

                memoryDb.value = state.copy(
                    users = updatedUsers,
                    groups = state.groups + (groupId to group),
                    members = state.members + (groupId to mapOf(owner.uid to ownerMember)),
                    balances = state.balances + (groupId to mapOf(owner.uid to ownerBalance)),
                    activities = state.activities + (groupId to listOf(activity)),
                    invitations = state.invitations + (inviteCode to invitation)
                )
                group
            }
        } else {
            val groupRef = db.collection(COLLECTION_GROUPS).document()
            val groupId = groupRef.id
            val inviteCode = generateUniqueInviteCodeFirestore(db)

            val group = SpendlyGroup(
                groupId = groupId,
                name = trimmedName,
                description = description.trim(),
                avatarUrl = "",
                ownerId = owner.uid,
                inviteCode = inviteCode,
                memberCount = 1,
                currency = "VND",
                monthlyBudgetLimit = monthlyBudgetLimit,
                createdAtMillis = now,
                status = STATUS_ACTIVE,
                memberIds = listOf(owner.uid)
            )
            val ownerMember = SpendlyGroupMember(
                userId = owner.uid,
                displayName = ownerName,
                initials = computeInitials(ownerName),
                phone = owner.phone,
                role = GroupRole.OWNER,
                joinedAtMillis = now,
                status = STATUS_ACTIVE,
                nickname = "",
                canAddExpense = true
            )
            val ownerBalance = MemberBalance(
                userId = owner.uid,
                totalPaid = 0L,
                totalShare = 0L,
                netBalance = 0L,
                updatedAtMillis = now
            )
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = owner.uid,
                actorName = ownerName,
                action = ACTION_CREATE_GROUP,
                targetId = groupId,
                description = "$ownerName đã tạo quỹ $trimmedName",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(groupRef, group.toFirestoreMap())
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).document(owner.uid),
                ownerMember.toFirestoreMap()
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_BALANCES).document(owner.uid),
                ownerBalance.toFirestoreMap()
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.set(
                db.collection(COLLECTION_INVITATIONS).document(inviteCode),
                mapOf(
                    "inviteCode" to inviteCode,
                    "groupId" to groupId,
                    "groupName" to trimmedName,
                    "createdAt" to now
                )
            )
            batch.commit().await()
            group
        }
    }

    override suspend fun joinGroupByInviteCode(
        user: SpendlyUser,
        inviteCode: String
    ): Result<SpendlyGroup> = runCatchingSuspend {
        require(user.uid.isNotBlank()) { "UID người dùng không được để trống" }
        val normalizedCode = inviteCode.trim().uppercase()
        require(normalizedCode.isNotBlank()) { "Mã mời không được để trống" }

        val now = clock()
        val displayName = user.fullName.trim().ifBlank {
            user.email.substringBefore('@').ifBlank { "Thành viên" }
        }
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val invitation = state.invitations[normalizedCode]
                    ?: throw NoSuchElementException("Mã mời $normalizedCode không tồn tại hoặc đã hết hạn")
                val group = state.groups[invitation.groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm tương ứng với mã mời")
                if (group.status != STATUS_ACTIVE) {
                    throw IllegalStateException("Nhóm này không còn hoạt động")
                }

                val groupMembers = state.members[group.groupId].orEmpty()
                val existingMember = groupMembers[user.uid]
                if (existingMember != null && existingMember.status == STATUS_ACTIVE) {
                    return@withLock group
                }

                val joinedMember = (existingMember ?: SpendlyGroupMember(userId = user.uid)).copy(
                    displayName = displayName,
                    initials = computeInitials(displayName),
                    phone = user.phone,
                    role = existingMember?.role?.takeIf { it == GroupRole.OWNER } ?: GroupRole.MEMBER,
                    joinedAtMillis = now,
                    status = STATUS_ACTIVE,
                    canAddExpense = existingMember?.role != GroupRole.VIEWER
                )
                val updatedMembers = groupMembers + (user.uid to joinedMember)
                val activeMemberIds = updatedMembers.values
                    .filter { it.status == STATUS_ACTIVE }
                    .map { it.userId }
                    .distinct()
                val updatedGroup = group.copy(
                    memberIds = activeMemberIds,
                    memberCount = activeMemberIds.size
                )

                val groupBalances = state.balances[group.groupId].orEmpty()
                val updatedBalances = if (groupBalances.containsKey(user.uid)) {
                    groupBalances
                } else {
                    groupBalances + (user.uid to MemberBalance(
                        userId = user.uid,
                        totalPaid = 0L,
                        totalShare = 0L,
                        netBalance = 0L,
                        updatedAtMillis = now
                    ))
                }

                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = group.groupId,
                    actorId = user.uid,
                    actorName = displayName,
                    action = ACTION_JOIN_GROUP,
                    targetId = user.uid,
                    description = "$displayName đã tham gia nhóm qua mã mời $normalizedCode",
                    createdAtMillis = now
                )
                val currentActivities = state.activities[group.groupId].orEmpty()

                val updatedNotifications = if (group.ownerId != user.uid) {
                    val ownerNotifs = state.notifications[group.ownerId].orEmpty()
                    val notif = SpendlyNotification(
                        id = newId("notif"),
                        title = "Thành viên mới tham gia ${group.name}",
                        body = "$displayName vừa tham gia quỹ nhóm bằng mã mời $normalizedCode",
                        type = "GROUP_JOIN",
                        groupId = group.groupId,
                        isRead = false,
                        createdAtMillis = now
                    )
                    state.notifications + (group.ownerId to (listOf(notif) + ownerNotifs))
                } else {
                    state.notifications
                }

                memoryDb.value = state.copy(
                    groups = state.groups + (group.groupId to updatedGroup),
                    members = state.members + (group.groupId to updatedMembers),
                    balances = state.balances + (group.groupId to updatedBalances),
                    activities = state.activities + (group.groupId to (listOf(activity) + currentActivities)),
                    notifications = updatedNotifications
                )
                updatedGroup
            }
        } else {
            val inviteSnap = db.collection(COLLECTION_INVITATIONS).document(normalizedCode).get().await()
            if (!inviteSnap.exists()) {
                throw NoSuchElementException("Mã mời $normalizedCode không tồn tại hoặc đã hết hạn")
            }
            val groupId = inviteSnap.getString("groupId").orEmpty()
            val groupRef = db.collection(COLLECTION_GROUPS).document(groupId)
            val groupSnap = groupRef.get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm tương ứng với mã mời")
            }
            val group = groupSnap.toSpendlyGroup()
            if (group.status != STATUS_ACTIVE) {
                throw IllegalStateException("Nhóm này không còn hoạt động")
            }

            val membersCol = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS)
            val existingMembersSnap = membersCol.get().await()
            val existingMap = existingMembersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val existingMember = existingMap[user.uid]
            if (existingMember != null && existingMember.status == STATUS_ACTIVE) {
                return@runCatchingSuspend group
            }

            val joinedMember = (existingMember ?: SpendlyGroupMember(userId = user.uid)).copy(
                displayName = displayName,
                initials = computeInitials(displayName),
                phone = user.phone,
                role = existingMember?.role?.takeIf { it == GroupRole.OWNER } ?: GroupRole.MEMBER,
                joinedAtMillis = now,
                status = STATUS_ACTIVE,
                canAddExpense = existingMember?.role != GroupRole.VIEWER
            )
            val activeIds = (existingMap + (user.uid to joinedMember)).values
                .filter { it.status == STATUS_ACTIVE }
                .map { it.userId }
                .distinct()
            val updatedGroup = group.copy(
                memberIds = activeIds,
                memberCount = activeIds.size
            )

            val balanceRef = groupCollection(db, groupId, SUBCOLLECTION_BALANCES).document(user.uid)
            val balanceSnap = balanceRef.get().await()

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = user.uid,
                actorName = displayName,
                action = ACTION_JOIN_GROUP,
                targetId = user.uid,
                description = "$displayName đã tham gia nhóm qua mã mời $normalizedCode",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(membersCol.document(user.uid), joinedMember.toFirestoreMap())
            batch.update(
                groupRef,
                mapOf(
                    "memberIds" to updatedGroup.memberIds,
                    "memberCount" to updatedGroup.memberCount
                )
            )
            if (!balanceSnap.exists()) {
                batch.set(
                    balanceRef,
                    MemberBalance(
                        userId = user.uid,
                        totalPaid = 0L,
                        totalShare = 0L,
                        netBalance = 0L,
                        updatedAtMillis = now
                    ).toFirestoreMap()
                )
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            if (group.ownerId.isNotBlank() && group.ownerId != user.uid) {
                val notifRef = db.collection(COLLECTION_USERS)
                    .document(group.ownerId)
                    .collection(SUBCOLLECTION_NOTIFICATIONS)
                    .document()
                val notif = SpendlyNotification(
                    id = notifRef.id,
                    title = "Thành viên mới tham gia ${group.name}",
                    body = "$displayName vừa tham gia quỹ nhóm bằng mã mời $normalizedCode",
                    type = "GROUP_JOIN",
                    groupId = groupId,
                    isRead = false,
                    createdAtMillis = now
                )
                batch.set(notifRef, notif.toFirestoreMap())
            }
            batch.commit().await()
            updatedGroup
        }
    }

    override suspend fun addManualMember(
        groupId: String,
        actorId: String,
        displayName: String,
        phone: String,
        role: GroupRole
    ): Result<SpendlyGroupMember> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        val trimmedName = displayName.trim()
        require(trimmedName.isNotBlank()) { "Tên thành viên không được để trống" }
        if (role == GroupRole.OWNER) {
            throw IllegalArgumentException("Nhóm chỉ có một Trưởng nhóm (OWNER)")
        }

        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm")
                val groupMembers = state.members[groupId].orEmpty()
                val actor = groupMembers[actorId]?.takeIf { it.status == STATUS_ACTIVE }
                    ?: throw SecurityException("Bạn không phải thành viên của nhóm này")
                if (!actor.role.canManageGroup()) {
                    throw SecurityException("Chỉ Trưởng nhóm hoặc Quản trị viên mới có quyền thêm thành viên")
                }

                val memberId = newId("mem")
                val newMember = SpendlyGroupMember(
                    userId = memberId,
                    displayName = trimmedName,
                    initials = computeInitials(trimmedName),
                    phone = phone.trim(),
                    role = role,
                    joinedAtMillis = now,
                    status = STATUS_ACTIVE,
                    nickname = "",
                    canAddExpense = role != GroupRole.VIEWER
                )
                val updatedMembers = groupMembers + (memberId to newMember)
                val activeIds = updatedMembers.values
                    .filter { it.status == STATUS_ACTIVE }
                    .map { it.userId }
                    .distinct()
                val updatedGroup = group.copy(
                    memberIds = activeIds,
                    memberCount = activeIds.size
                )
                val balance = MemberBalance(
                    userId = memberId,
                    totalPaid = 0L,
                    totalShare = 0L,
                    netBalance = 0L,
                    updatedAtMillis = now
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = actor.displayName,
                    action = ACTION_ADD_MEMBER,
                    targetId = memberId,
                    description = "${actor.displayName} đã thêm thành viên $trimmedName (${role.label})",
                    createdAtMillis = now
                )

                memoryDb.value = state.copy(
                    groups = state.groups + (groupId to updatedGroup),
                    members = state.members + (groupId to updatedMembers),
                    balances = state.balances + (groupId to (state.balances[groupId].orEmpty() + (memberId to balance))),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
                newMember
            }
        } else {
            val groupRef = db.collection(COLLECTION_GROUPS).document(groupId)
            val groupSnap = groupRef.get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm")
            }
            val group = groupSnap.toSpendlyGroup()

            val membersCol = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS)
            val membersSnap = membersCol.get().await()
            val existingMembers = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val actor = existingMembers[actorId]?.takeIf { it.status == STATUS_ACTIVE }
                ?: throw SecurityException("Bạn không phải thành viên của nhóm này")
            if (!actor.role.canManageGroup()) {
                throw SecurityException("Chỉ Trưởng nhóm hoặc Quản trị viên mới có quyền thêm thành viên")
            }

            val memberRef = membersCol.document()
            val memberId = memberRef.id
            val newMember = SpendlyGroupMember(
                userId = memberId,
                displayName = trimmedName,
                initials = computeInitials(trimmedName),
                phone = phone.trim(),
                role = role,
                joinedAtMillis = now,
                status = STATUS_ACTIVE,
                nickname = "",
                canAddExpense = role != GroupRole.VIEWER
            )
            val activeIds = (existingMembers + (memberId to newMember)).values
                .filter { it.status == STATUS_ACTIVE }
                .map { it.userId }
                .distinct()
            val balance = MemberBalance(
                userId = memberId,
                totalPaid = 0L,
                totalShare = 0L,
                netBalance = 0L,
                updatedAtMillis = now
            )
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = actor.displayName,
                action = ACTION_ADD_MEMBER,
                targetId = memberId,
                description = "${actor.displayName} đã thêm thành viên $trimmedName (${role.label})",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(memberRef, newMember.toFirestoreMap())
            batch.update(
                groupRef,
                mapOf(
                    "memberIds" to activeIds,
                    "memberCount" to activeIds.size
                )
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_BALANCES).document(memberId),
                balance.toFirestoreMap()
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
            newMember
        }
    }

    override suspend fun updateMemberRole(
        groupId: String,
        targetUserId: String,
        role: GroupRole,
        canAddExpense: Boolean
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && targetUserId.isNotBlank()) {
            "Mã nhóm và mã thành viên không được để trống"
        }
        val now = clock()
        val effectiveCanAddExpense = if (role == GroupRole.VIEWER) false else canAddExpense
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm")
                val groupMembers = state.members[groupId].orEmpty()
                val target = groupMembers[targetUserId]?.takeIf { it.status == STATUS_ACTIVE }
                    ?: throw NoSuchElementException("Không tìm thấy thành viên trong nhóm")

                validateRoleChange(group, targetUserId, role)

                val updatedMember = target.copy(
                    role = role,
                    canAddExpense = effectiveCanAddExpense
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = group.ownerId,
                    actorName = groupMembers[group.ownerId]?.displayName.orEmpty().ifBlank { "Hệ thống" },
                    action = ACTION_UPDATE_MEMBER_ROLE,
                    targetId = targetUserId,
                    description = "Cập nhật quyền của ${target.displayName} thành ${role.label}",
                    createdAtMillis = now
                )
                memoryDb.value = state.copy(
                    members = state.members + (groupId to (groupMembers + (targetUserId to updatedMember))),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm")
            }
            val group = groupSnap.toSpendlyGroup()
            val memberRef = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).document(targetUserId)
            val memberSnap = memberRef.get().await()
            if (!memberSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy thành viên trong nhóm")
            }
            val target = memberSnap.toSpendlyGroupMember()
            if (target.status != STATUS_ACTIVE) {
                throw IllegalStateException("Thành viên không còn hoạt động trong nhóm")
            }

            validateRoleChange(group, targetUserId, role)

            val ownerSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS)
                .document(group.ownerId)
                .get()
                .await()
            val actorName = if (ownerSnap.exists()) {
                ownerSnap.toSpendlyGroupMember().displayName.ifBlank { "Hệ thống" }
            } else {
                "Hệ thống"
            }

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = group.ownerId,
                actorName = actorName,
                action = ACTION_UPDATE_MEMBER_ROLE,
                targetId = targetUserId,
                description = "Cập nhật quyền của ${target.displayName} thành ${role.label}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.update(
                memberRef,
                mapOf(
                    "role" to role.name,
                    "canAddExpense" to effectiveCanAddExpense
                )
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    override suspend fun removeOrLeaveGroup(
        groupId: String,
        targetUserId: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && targetUserId.isNotBlank()) {
            "Mã nhóm và mã thành viên không được để trống"
        }
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm")
                val groupMembers = state.members[groupId].orEmpty()
                val target = groupMembers[targetUserId]?.takeIf { it.status == STATUS_ACTIVE }
                    ?: throw NoSuchElementException("Thành viên không còn trong nhóm")

                val otherActiveIds = groupMembers.values
                    .filter { it.status == STATUS_ACTIVE && it.userId != targetUserId }
                    .map { it.userId }

                if (targetUserId == group.ownerId && otherActiveIds.isNotEmpty()) {
                    throw IllegalStateException("Trưởng nhóm không thể rời nhóm khi vẫn còn thành viên khác")
                }

                val updatedMembers = groupMembers + (targetUserId to target.copy(status = STATUS_LEFT))
                val newStatus = if (otherActiveIds.isEmpty()) STATUS_ARCHIVED else group.status
                val updatedGroup = group.copy(
                    memberIds = otherActiveIds,
                    memberCount = otherActiveIds.size,
                    status = newStatus
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = targetUserId,
                    actorName = target.displayName,
                    action = ACTION_REMOVE_MEMBER,
                    targetId = targetUserId,
                    description = "${target.displayName} đã rời khỏi nhóm",
                    createdAtMillis = now
                )

                memoryDb.value = state.copy(
                    groups = state.groups + (groupId to updatedGroup),
                    members = state.members + (groupId to updatedMembers),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupRef = db.collection(COLLECTION_GROUPS).document(groupId)
            val groupSnap = groupRef.get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm")
            }
            val group = groupSnap.toSpendlyGroup()

            val membersCol = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS)
            val membersSnap = membersCol.get().await()
            val memberMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val target = memberMap[targetUserId]?.takeIf { it.status == STATUS_ACTIVE }
                ?: throw NoSuchElementException("Thành viên không còn trong nhóm")

            val otherActiveIds = memberMap.values
                .filter { it.status == STATUS_ACTIVE && it.userId != targetUserId }
                .map { it.userId }

            if (targetUserId == group.ownerId && otherActiveIds.isNotEmpty()) {
                throw IllegalStateException("Trưởng nhóm không thể rời nhóm khi vẫn còn thành viên khác")
            }

            val newStatus = if (otherActiveIds.isEmpty()) STATUS_ARCHIVED else group.status
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = targetUserId,
                actorName = target.displayName,
                action = ACTION_REMOVE_MEMBER,
                targetId = targetUserId,
                description = "${target.displayName} đã rời khỏi nhóm",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.update(membersCol.document(targetUserId), "status", STATUS_LEFT)
            batch.update(
                groupRef,
                mapOf(
                    "memberIds" to otherActiveIds,
                    "memberCount" to otherActiveIds.size,
                    "status" to newStatus
                )
            )
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    override suspend fun updateGroupSettings(
        groupId: String,
        name: String,
        description: String,
        monthlyBudgetLimit: Long
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        val trimmedName = name.trim()
        require(trimmedName.isNotBlank()) { "Tên quỹ nhóm không được để trống" }
        require(monthlyBudgetLimit > 0L) { "Hạn mức ngân sách hàng tháng phải lớn hơn 0" }
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm")
                val updatedGroup = group.copy(
                    name = trimmedName,
                    description = description.trim(),
                    monthlyBudgetLimit = monthlyBudgetLimit
                )
                val updatedInvitations = if (group.inviteCode.isNotBlank() && state.invitations.containsKey(group.inviteCode)) {
                    val existingInvite = state.invitations.getValue(group.inviteCode)
                    state.invitations + (group.inviteCode to existingInvite.copy(groupName = trimmedName))
                } else {
                    state.invitations
                }
                val ownerName = state.members[groupId]?.get(group.ownerId)?.displayName.orEmpty().ifBlank { "Trưởng nhóm" }
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = group.ownerId,
                    actorName = ownerName,
                    action = ACTION_UPDATE_GROUP_SETTINGS,
                    targetId = groupId,
                    description = "Cập nhật thông tin quỹ $trimmedName",
                    createdAtMillis = now
                )
                memoryDb.value = state.copy(
                    groups = state.groups + (groupId to updatedGroup),
                    invitations = updatedInvitations,
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupRef = db.collection(COLLECTION_GROUPS).document(groupId)
            val groupSnap = groupRef.get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm")
            }
            val group = groupSnap.toSpendlyGroup()
            val ownerSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).document(group.ownerId).get().await()
            val ownerName = if (ownerSnap.exists()) {
                ownerSnap.toSpendlyGroupMember().displayName.ifBlank { "Trưởng nhóm" }
            } else {
                "Trưởng nhóm"
            }
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = group.ownerId,
                actorName = ownerName,
                action = ACTION_UPDATE_GROUP_SETTINGS,
                targetId = groupId,
                description = "Cập nhật thông tin quỹ $trimmedName",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.update(
                groupRef,
                mapOf(
                    "name" to trimmedName,
                    "description" to description.trim(),
                    "monthlyBudgetLimit" to monthlyBudgetLimit
                )
            )
            if (group.inviteCode.isNotBlank()) {
                batch.set(
                    db.collection(COLLECTION_INVITATIONS).document(group.inviteCode),
                    mapOf("groupName" to trimmedName),
                    SetOptions.merge()
                )
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    override suspend fun seedStarterGroupIfEmpty(user: SpendlyUser): Result<SpendlyGroup?> {
        if (user.uid.isBlank()) {
            return Result.failure(IllegalArgumentException("UID người dùng không được để trống"))
        }

        val db = firestore
        val hasActiveGroup = if (db == null) {
            memoryDb.value.groups.values.any { it.status == STATUS_ACTIVE && user.uid in it.memberIds }
        } else {
            try {
                val snap = db.collection(COLLECTION_GROUPS)
                    .whereArrayContains("memberIds", user.uid)
                    .get()
                    .await()
                snap.documents.any { it.toSpendlyGroup().status == STATUS_ACTIVE }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        if (hasActiveGroup) {
            return Result.success(null)
        }

        return createGroup(
            owner = user,
            name = "Quỹ Chi Tiêu Nhóm 08",
            description = "Quỹ chi tiêu chung khởi tạo cho nhóm của bạn",
            monthlyBudgetLimit = 10_000_000L
        ).map { it }
    }

    // ==================== TRANSACTION REPOSITORY ====================

    override fun observeExpenses(groupId: String): Flow<List<SpendlyExpense>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.expenses[groupId]
                    .orEmpty()
                    .sortedWith(
                        compareByDescending<SpendlyExpense> { it.expenseDateMillis }
                            .thenByDescending { it.createdAtMillis }
                    )
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES)
                .orderBy("expenseDateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val expenses = snapshot?.documents
                        ?.map { it.toSpendlyExpense(groupId) }
                        ?.sortedWith(
                            compareByDescending<SpendlyExpense> { it.expenseDateMillis }
                                .thenByDescending { it.createdAtMillis }
                        )
                        .orEmpty()
                    trySend(expenses)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeExpenseDetail(
        groupId: String,
        expenseId: String
    ): Flow<Pair<SpendlyExpense, List<ExpenseParticipant>>?> {
        val db = firestore ?: return memoryDb
            .map { state ->
                val exp = state.expenses[groupId]?.firstOrNull { it.expenseId == expenseId }
                    ?: return@map null
                val parts = state.participants[groupId]?.get(expenseId).orEmpty()
                exp to parts
            }
            .distinctUntilChanged()

        if (groupId.isBlank() || expenseId.isBlank()) {
            return kotlinx.coroutines.flow.flowOf(null)
        }

        val expenseDocRef = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES).document(expenseId)
        val expenseFlow = callbackFlow<SpendlyExpense?> {
            val registration = expenseDocRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val exp = if (snapshot != null && snapshot.exists()) {
                    snapshot.toSpendlyExpense(groupId)
                } else {
                    null
                }
                trySend(exp)
            }
            awaitClose { registration.remove() }
        }

        val participantsFlow = callbackFlow<List<ExpenseParticipant>> {
            val registration = expenseDocRef
                .collection(SUBCOLLECTION_PARTICIPANTS)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val list = snapshot?.documents
                        ?.map { it.toExpenseParticipant() }
                        .orEmpty()
                    trySend(list)
                }
            awaitClose { registration.remove() }
        }

        return combine(expenseFlow, participantsFlow) { exp, parts ->
            exp?.let { it to parts }
        }
    }

    override fun observeIncomes(groupId: String): Flow<List<SpendlyIncome>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.incomes[groupId]
                    .orEmpty()
                    .sortedWith(
                        compareByDescending<SpendlyIncome> { it.incomeDateMillis }
                            .thenByDescending { it.createdAtMillis }
                    )
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_INCOMES)
                .orderBy("incomeDateMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toSpendlyIncome(groupId) }
                        ?.sortedWith(
                            compareByDescending<SpendlyIncome> { it.incomeDateMillis }
                                .thenByDescending { it.createdAtMillis }
                        )
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override fun observeMemberBalances(groupId: String): Flow<List<MemberBalance>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.balances[groupId]
                    ?.values
                    ?.sortedBy { it.userId }
                    .orEmpty()
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toMemberBalance() }
                        ?.sortedBy { it.userId }
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun createExpenseWithParticipants(
        groupId: String,
        expense: SpendlyExpense,
        participants: List<ExpenseParticipant>
    ): Result<SpendlyExpense> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        validateExpenseInput(expense, participants)
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                checkCreatorPermission(membersMap, expense.createdBy)

                val expenseId = expense.expenseId.ifBlank { newId("exp") }
                val actorId = expense.createdBy.ifBlank { expense.payerId }
                val payerName = expense.payerName.ifBlank {
                    membersMap[expense.payerId]?.displayName ?: expense.payerId
                }
                val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

                val savedExpense = expense.copy(
                    expenseId = expenseId,
                    groupId = groupId,
                    payerName = payerName,
                    createdBy = actorId,
                    expenseDateMillis = if (expense.expenseDateMillis > 0L) expense.expenseDateMillis else now,
                    createdAtMillis = if (expense.createdAtMillis > 0L) expense.createdAtMillis else now,
                    updatedAtMillis = now,
                    status = RecordStatus.ACTIVE
                )

                val currentExpenses = state.expenses[groupId].orEmpty()
                    .filterNot { it.expenseId == expenseId }
                val updatedExpenses = listOf(savedExpense) + currentExpenses
                val currentParticipants = state.participants[groupId].orEmpty() + (expenseId to participants)

                val activeMemberIds = group.memberIds.toSet()
                val existingBalanceIds = state.balances[groupId]?.keys.orEmpty()
                val confirmedSettlements = state.settlements[groupId]
                    .orEmpty()
                    .filter { it.status == SettlementStatus.CONFIRMED }
                val newBalances = computeBalances(
                    memberIds = activeMemberIds,
                    existingBalanceUserIds = existingBalanceIds,
                    expensesWithParticipants = updatedExpenses.map { exp ->
                        exp to currentParticipants[exp.expenseId].orEmpty()
                    },
                    confirmedSettlements = confirmedSettlements,
                    now = now
                )

                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = actorName,
                    action = ACTION_CREATE_EXPENSE,
                    targetId = expenseId,
                    description = "$actorName đã thêm khoản chi ${savedExpense.title}",
                    createdAtMillis = now
                )

                memoryDb.value = state.copy(
                    expenses = state.expenses + (groupId to updatedExpenses),
                    participants = state.participants + (groupId to currentParticipants),
                    balances = state.balances + (groupId to newBalances),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
                savedExpense
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()

            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            checkCreatorPermission(membersMap, expense.createdBy)

            val expensesCol = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES)
            val expenseRef = if (expense.expenseId.isBlank()) {
                expensesCol.document()
            } else {
                expensesCol.document(expense.expenseId)
            }
            val expenseId = expenseRef.id
            val actorId = expense.createdBy.ifBlank { expense.payerId }
            val payerName = expense.payerName.ifBlank {
                membersMap[expense.payerId]?.displayName ?: expense.payerId
            }
            val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

            val savedExpense = expense.copy(
                expenseId = expenseId,
                groupId = groupId,
                payerName = payerName,
                createdBy = actorId,
                expenseDateMillis = if (expense.expenseDateMillis > 0L) expense.expenseDateMillis else now,
                createdAtMillis = if (expense.createdAtMillis > 0L) expense.createdAtMillis else now,
                updatedAtMillis = now,
                status = RecordStatus.ACTIVE
            )

            val existingActiveWithParts = fetchActiveExpensesWithParticipants(db, groupId)
                .filterNot { it.first.expenseId == expenseId }
            val confirmedSettlements = fetchConfirmedSettlements(db, groupId)
            val existingBalanceIds = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()

            val newBalances = computeBalances(
                memberIds = group.memberIds.toSet(),
                existingBalanceUserIds = existingBalanceIds,
                expensesWithParticipants = existingActiveWithParts + (savedExpense to participants),
                confirmedSettlements = confirmedSettlements,
                now = now
            )

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = actorName,
                action = ACTION_CREATE_EXPENSE,
                targetId = expenseId,
                description = "$actorName đã thêm khoản chi ${savedExpense.title}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(expenseRef, savedExpense.toFirestoreMap())
            val participantsCol = expenseRef.collection(SUBCOLLECTION_PARTICIPANTS)
            participants.forEach { participant ->
                batch.set(participantsCol.document(participant.userId), participant.toFirestoreMap())
            }
            val balancesCol = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
            newBalances.forEach { (userId, balance) ->
                batch.set(balancesCol.document(userId), balance.toFirestoreMap())
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
            savedExpense
        }
    }

    override suspend fun updateExpenseWithParticipants(
        groupId: String,
        expense: SpendlyExpense,
        participants: List<ExpenseParticipant>
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        require(expense.expenseId.isNotBlank()) { "Mã khoản chi không được để trống" }
        validateExpenseInput(expense, participants)
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val groupExpenses = state.expenses[groupId].orEmpty()
                val existing = groupExpenses.firstOrNull { it.expenseId == expense.expenseId }
                    ?: throw NoSuchElementException("Không tìm thấy khoản chi để cập nhật")
                if (existing.status == RecordStatus.CANCELLED) {
                    throw IllegalStateException("Không thể chỉnh sửa khoản chi đã bị hủy")
                }

                val membersMap = state.members[groupId].orEmpty()
                val actorId = expense.createdBy.ifBlank { existing.createdBy.ifBlank { expense.payerId } }
                val payerName = expense.payerName.ifBlank {
                    membersMap[expense.payerId]?.displayName ?: expense.payerName
                }
                val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

                val updatedExpense = expense.copy(
                    groupId = groupId,
                    payerName = payerName,
                    createdBy = existing.createdBy.ifBlank { actorId },
                    expenseDateMillis = if (expense.expenseDateMillis > 0L) expense.expenseDateMillis else existing.expenseDateMillis,
                    createdAtMillis = existing.createdAtMillis,
                    updatedAtMillis = now,
                    status = RecordStatus.ACTIVE
                )

                val updatedExpenses = groupExpenses.map {
                    if (it.expenseId == expense.expenseId) updatedExpense else it
                }
                val updatedParticipants = state.participants[groupId].orEmpty() + (expense.expenseId to participants)

                val confirmedSettlements = state.settlements[groupId]
                    .orEmpty()
                    .filter { it.status == SettlementStatus.CONFIRMED }
                val newBalances = computeBalances(
                    memberIds = group.memberIds.toSet(),
                    existingBalanceUserIds = state.balances[groupId]?.keys.orEmpty(),
                    expensesWithParticipants = updatedExpenses.map { exp ->
                        exp to updatedParticipants[exp.expenseId].orEmpty()
                    },
                    confirmedSettlements = confirmedSettlements,
                    now = now
                )

                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = actorName,
                    action = ACTION_UPDATE_EXPENSE,
                    targetId = expense.expenseId,
                    description = "$actorName đã cập nhật khoản chi ${updatedExpense.title}",
                    createdAtMillis = now
                )

                memoryDb.value = state.copy(
                    expenses = state.expenses + (groupId to updatedExpenses),
                    participants = state.participants + (groupId to updatedParticipants),
                    balances = state.balances + (groupId to newBalances),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()

            val expenseRef = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES).document(expense.expenseId)
            val existingSnap = expenseRef.get().await()
            if (!existingSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy khoản chi để cập nhật")
            }
            val existing = existingSnap.toSpendlyExpense(groupId)
            if (existing.status == RecordStatus.CANCELLED) {
                throw IllegalStateException("Không thể chỉnh sửa khoản chi đã bị hủy")
            }

            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val actorId = expense.createdBy.ifBlank { existing.createdBy.ifBlank { expense.payerId } }
            val payerName = expense.payerName.ifBlank {
                membersMap[expense.payerId]?.displayName ?: expense.payerName
            }
            val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

            val updatedExpense = expense.copy(
                groupId = groupId,
                payerName = payerName,
                createdBy = existing.createdBy.ifBlank { actorId },
                expenseDateMillis = if (expense.expenseDateMillis > 0L) expense.expenseDateMillis else existing.expenseDateMillis,
                createdAtMillis = existing.createdAtMillis,
                updatedAtMillis = now,
                status = RecordStatus.ACTIVE
            )

            val oldParticipantSnap = expenseRef.collection(SUBCOLLECTION_PARTICIPANTS).get().await()
            val oldParticipantIds = oldParticipantSnap.documents.map { it.id }.toSet()
            val newParticipantIds = participants.map { it.userId }.toSet()
            val removedParticipantIds = oldParticipantIds - newParticipantIds

            val activeOthers = fetchActiveExpensesWithParticipants(db, groupId)
                .filterNot { it.first.expenseId == expense.expenseId }
            val confirmedSettlements = fetchConfirmedSettlements(db, groupId)
            val existingBalanceIds = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()

            val newBalances = computeBalances(
                memberIds = group.memberIds.toSet(),
                existingBalanceUserIds = existingBalanceIds,
                expensesWithParticipants = activeOthers + (updatedExpense to participants),
                confirmedSettlements = confirmedSettlements,
                now = now
            )

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = actorName,
                action = ACTION_UPDATE_EXPENSE,
                targetId = expense.expenseId,
                description = "$actorName đã cập nhật khoản chi ${updatedExpense.title}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(expenseRef, updatedExpense.toFirestoreMap())
            val participantsCol = expenseRef.collection(SUBCOLLECTION_PARTICIPANTS)
            removedParticipantIds.forEach { removedId ->
                batch.delete(participantsCol.document(removedId))
            }
            participants.forEach { participant ->
                batch.set(participantsCol.document(participant.userId), participant.toFirestoreMap())
            }
            val balancesCol = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
            newBalances.forEach { (userId, balance) ->
                batch.set(balancesCol.document(userId), balance.toFirestoreMap())
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    override suspend fun cancelExpense(
        groupId: String,
        expenseId: String,
        actorId: String,
        actorName: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && expenseId.isNotBlank()) {
            "Mã nhóm và mã khoản chi không được để trống"
        }
        val now = clock()
        val resolvedActorName = actorName.trim().ifBlank { actorId }
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val groupExpenses = state.expenses[groupId].orEmpty()
                val existing = groupExpenses.firstOrNull { it.expenseId == expenseId }
                    ?: throw NoSuchElementException("Không tìm thấy khoản chi")
                if (existing.status == RecordStatus.CANCELLED) {
                    return@withLock
                }

                val cancelled = existing.copy(
                    status = RecordStatus.CANCELLED,
                    updatedAtMillis = now
                )
                val updatedExpenses = groupExpenses.map {
                    if (it.expenseId == expenseId) cancelled else it
                }
                val groupParticipants = state.participants[groupId].orEmpty()
                val confirmedSettlements = state.settlements[groupId]
                    .orEmpty()
                    .filter { it.status == SettlementStatus.CONFIRMED }
                val newBalances = computeBalances(
                    memberIds = group.memberIds.toSet(),
                    existingBalanceUserIds = state.balances[groupId]?.keys.orEmpty(),
                    expensesWithParticipants = updatedExpenses.map { exp ->
                        exp to groupParticipants[exp.expenseId].orEmpty()
                    },
                    confirmedSettlements = confirmedSettlements,
                    now = now
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    action = ACTION_CANCEL_EXPENSE,
                    targetId = expenseId,
                    description = "$resolvedActorName đã hủy khoản chi ${existing.title}",
                    createdAtMillis = now
                )

                memoryDb.value = state.copy(
                    expenses = state.expenses + (groupId to updatedExpenses),
                    balances = state.balances + (groupId to newBalances),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()

            val expenseRef = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES).document(expenseId)
            val existingSnap = expenseRef.get().await()
            if (!existingSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy khoản chi")
            }
            val existing = existingSnap.toSpendlyExpense(groupId)
            if (existing.status == RecordStatus.CANCELLED) {
                return@runCatchingSuspend
            }

            val remainingActive = fetchActiveExpensesWithParticipants(db, groupId)
                .filterNot { it.first.expenseId == expenseId }
            val confirmedSettlements = fetchConfirmedSettlements(db, groupId)
            val existingBalanceIds = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
                .get()
                .await()
                .documents
                .map { it.id }
                .toSet()

            val newBalances = computeBalances(
                memberIds = group.memberIds.toSet(),
                existingBalanceUserIds = existingBalanceIds,
                expensesWithParticipants = remainingActive,
                confirmedSettlements = confirmedSettlements,
                now = now
            )

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = resolvedActorName,
                action = ACTION_CANCEL_EXPENSE,
                targetId = expenseId,
                description = "$resolvedActorName đã hủy khoản chi ${existing.title}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.update(
                expenseRef,
                mapOf(
                    "status" to RecordStatus.CANCELLED.name,
                    "updatedAtMillis" to now
                )
            )
            val balancesCol = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)
            newBalances.forEach { (userId, balance) ->
                batch.set(balancesCol.document(userId), balance.toFirestoreMap())
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    override suspend fun createIncome(
        groupId: String,
        income: SpendlyIncome
    ): Result<SpendlyIncome> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        require(income.title.isNotBlank()) { "Tên khoản thu không được để trống" }
        require(income.amount > 0L) { "Số tiền khoản thu phải lớn hơn 0" }
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                if (!state.groups.containsKey(groupId)) {
                    throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                }
                val membersMap = state.members[groupId].orEmpty()
                checkCreatorPermission(membersMap, income.createdBy)

                val incomeId = income.incomeId.ifBlank { newId("inc") }
                val actorId = income.createdBy.ifBlank { income.payerId }
                val payerName = income.payerName.ifBlank {
                    membersMap[income.payerId]?.displayName ?: income.payerId
                }
                val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

                val savedIncome = income.copy(
                    incomeId = incomeId,
                    groupId = groupId,
                    payerName = payerName,
                    createdBy = actorId,
                    incomeDateMillis = if (income.incomeDateMillis > 0L) income.incomeDateMillis else now,
                    createdAtMillis = if (income.createdAtMillis > 0L) income.createdAtMillis else now,
                    status = RecordStatus.ACTIVE
                )
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = actorName,
                    action = ACTION_CREATE_INCOME,
                    targetId = incomeId,
                    description = if (savedIncome.isFundContribution) {
                        "$actorName đã đóng quỹ: ${savedIncome.title}"
                    } else {
                        "$actorName đã ghi nhận khoản thu: ${savedIncome.title}"
                    },
                    createdAtMillis = now
                )
                val currentIncomes = state.incomes[groupId].orEmpty()
                    .filterNot { it.incomeId == incomeId }
                memoryDb.value = state.copy(
                    incomes = state.incomes + (groupId to (listOf(savedIncome) + currentIncomes)),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
                savedIncome
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            checkCreatorPermission(membersMap, income.createdBy)

            val incomesCol = groupCollection(db, groupId, SUBCOLLECTION_INCOMES)
            val incomeRef = if (income.incomeId.isBlank()) {
                incomesCol.document()
            } else {
                incomesCol.document(income.incomeId)
            }
            val incomeId = incomeRef.id
            val actorId = income.createdBy.ifBlank { income.payerId }
            val payerName = income.payerName.ifBlank {
                membersMap[income.payerId]?.displayName ?: income.payerId
            }
            val actorName = membersMap[actorId]?.displayName?.ifBlank { payerName } ?: payerName

            val savedIncome = income.copy(
                incomeId = incomeId,
                groupId = groupId,
                payerName = payerName,
                createdBy = actorId,
                incomeDateMillis = if (income.incomeDateMillis > 0L) income.incomeDateMillis else now,
                createdAtMillis = if (income.createdAtMillis > 0L) income.createdAtMillis else now,
                status = RecordStatus.ACTIVE
            )
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = actorName,
                action = ACTION_CREATE_INCOME,
                targetId = incomeId,
                description = if (savedIncome.isFundContribution) {
                    "$actorName đã đóng quỹ: ${savedIncome.title}"
                } else {
                    "$actorName đã ghi nhận khoản thu: ${savedIncome.title}"
                },
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.set(incomeRef, savedIncome.toFirestoreMap())
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
            savedIncome
        }
    }

    override suspend fun cancelIncome(
        groupId: String,
        incomeId: String,
        actorId: String,
        actorName: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && incomeId.isNotBlank()) {
            "Mã nhóm và mã khoản thu không được để trống"
        }
        val now = clock()
        val resolvedActorName = actorName.trim().ifBlank { actorId }
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                if (!state.groups.containsKey(groupId)) {
                    throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                }
                val currentIncomes = state.incomes[groupId].orEmpty()
                val existing = currentIncomes.firstOrNull { it.incomeId == incomeId }
                    ?: throw NoSuchElementException("Không tìm thấy khoản thu")
                if (existing.status == RecordStatus.CANCELLED) {
                    return@withLock
                }
                val updatedIncomes = currentIncomes.map {
                    if (it.incomeId == incomeId) it.copy(status = RecordStatus.CANCELLED) else it
                }
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    action = ACTION_CANCEL_INCOME,
                    targetId = incomeId,
                    description = "$resolvedActorName đã hủy khoản thu ${existing.title}",
                    createdAtMillis = now
                )
                memoryDb.value = state.copy(
                    incomes = state.incomes + (groupId to updatedIncomes),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val incomeRef = groupCollection(db, groupId, SUBCOLLECTION_INCOMES).document(incomeId)
            val snap = incomeRef.get().await()
            if (!snap.exists()) {
                throw NoSuchElementException("Không tìm thấy khoản thu")
            }
            val existing = snap.toSpendlyIncome(groupId)
            if (existing.status == RecordStatus.CANCELLED) {
                return@runCatchingSuspend
            }

            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = resolvedActorName,
                action = ACTION_CANCEL_INCOME,
                targetId = incomeId,
                description = "$resolvedActorName đã hủy khoản thu ${existing.title}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.update(incomeRef, "status", RecordStatus.CANCELLED.name)
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    // ==================== SETTLEMENT REPOSITORY ====================

    override fun observeSettlements(groupId: String): Flow<List<SpendlySettlement>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.settlements[groupId]
                    .orEmpty()
                    .sortedByDescending { it.createdAtMillis }
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_SETTLEMENTS)
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toSpendlySettlement(groupId) }
                        ?.sortedByDescending { it.createdAtMillis }
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun createSettlementRequest(
        groupId: String,
        settlement: SpendlySettlement
    ): Result<SpendlySettlement> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        validateSettlementRequest(settlement)
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                val settlementId = settlement.settlementId.ifBlank { newId("stl") }
                val savedSettlement = buildPendingSettlement(
                    groupId = groupId,
                    settlementId = settlementId,
                    settlement = settlement,
                    membersMap = membersMap,
                    now = now
                )
                val actorName = resolveActorName(
                    actorId = savedSettlement.createdBy,
                    fallbackName = savedSettlement.fromUserName,
                    membersMap = membersMap
                )
                val activity = buildSettlementRequestActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorName = actorName,
                    settlement = savedSettlement,
                    now = now
                )
                val notification = buildSettlementRequestNotification(
                    notificationId = newId("notif"),
                    group = group,
                    settlement = savedSettlement,
                    now = now
                )

                val currentSettlements = state.settlements[groupId].orEmpty()
                    .filterNot { it.settlementId == settlementId }
                val targetNotifs = state.notifications[savedSettlement.toUserId].orEmpty()

                memoryDb.value = state.copy(
                    settlements = state.settlements + (groupId to (listOf(savedSettlement) + currentSettlements)),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty())),
                    notifications = state.notifications + (savedSettlement.toUserId to (listOf(notification) + targetNotifs))
                )
                savedSettlement
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()
            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }

            val settlementsCol = groupCollection(db, groupId, SUBCOLLECTION_SETTLEMENTS)
            val settlementRef = if (settlement.settlementId.isBlank()) {
                settlementsCol.document()
            } else {
                settlementsCol.document(settlement.settlementId)
            }
            val savedSettlement = buildPendingSettlement(
                groupId = groupId,
                settlementId = settlementRef.id,
                settlement = settlement,
                membersMap = membersMap,
                now = now
            )
            val actorName = resolveActorName(
                actorId = savedSettlement.createdBy,
                fallbackName = savedSettlement.fromUserName,
                membersMap = membersMap
            )
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = buildSettlementRequestActivity(
                activityId = activityId,
                groupId = groupId,
                actorName = actorName,
                settlement = savedSettlement,
                now = now
            )
            val notifRef = db.collection(COLLECTION_USERS)
                .document(savedSettlement.toUserId)
                .collection(SUBCOLLECTION_NOTIFICATIONS)
                .document()
            val notification = buildSettlementRequestNotification(
                notificationId = notifRef.id,
                group = group,
                settlement = savedSettlement,
                now = now
            )

            val batch = db.batch()
            batch.set(settlementRef, savedSettlement.toFirestoreMap())
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.set(notifRef, notification.toFirestoreMap())
            batch.commit().await()
            savedSettlement
        }
    }

    override suspend fun confirmSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && settlementId.isNotBlank()) {
            "Mã nhóm và mã thanh toán không được để trống"
        }
        require(actorId.isNotBlank()) { "Mã người xác nhận không được để trống" }
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                val groupSettlements = state.settlements[groupId].orEmpty()
                val existing = groupSettlements.firstOrNull { it.settlementId == settlementId }
                    ?: throw NoSuchElementException("Không tìm thấy giao dịch thanh toán")

                ensurePendingForConfirmation(existing.status)

                val confirmed = existing.copy(
                    status = SettlementStatus.CONFIRMED,
                    confirmedAtMillis = now,
                    confirmedBy = actorId
                )
                val updatedSettlements = groupSettlements.map {
                    if (it.settlementId == settlementId) confirmed else it
                }
                val groupParticipants = state.participants[groupId].orEmpty()
                val newBalances = computeBalances(
                    memberIds = group.memberIds.toSet(),
                    existingBalanceUserIds = state.balances[groupId]?.keys.orEmpty(),
                    expensesWithParticipants = state.expenses[groupId].orEmpty().map { exp ->
                        exp to groupParticipants[exp.expenseId].orEmpty()
                    },
                    confirmedSettlements = updatedSettlements.filter { it.status == SettlementStatus.CONFIRMED },
                    now = now
                )

                val resolvedActorName = resolveActorName(actorId, actorName, membersMap)
                val activity = buildSettlementConfirmActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    settlement = confirmed,
                    now = now
                )
                val updatedNotifications = if (confirmed.fromUserId.isNotBlank()) {
                    val notification = buildSettlementConfirmNotification(
                        notificationId = newId("notif"),
                        group = group,
                        actorName = resolvedActorName,
                        settlement = confirmed,
                        now = now
                    )
                    val fromNotifs = state.notifications[confirmed.fromUserId].orEmpty()
                    state.notifications + (confirmed.fromUserId to (listOf(notification) + fromNotifs))
                } else {
                    state.notifications
                }

                memoryDb.value = state.copy(
                    settlements = state.settlements + (groupId to updatedSettlements),
                    balances = state.balances + (groupId to newBalances),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty())),
                    notifications = updatedNotifications
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()

            lateinit var membersMap: Map<String, SpendlyGroupMember>
            lateinit var activeExpenses: List<Pair<SpendlyExpense, List<ExpenseParticipant>>>
            lateinit var existingBalanceIds: Set<String>
            lateinit var otherConfirmedSettlements: List<SpendlySettlement>
            coroutineScope {
                val membersDef = async { groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await() }
                val expensesDef = async { fetchActiveExpensesWithParticipants(db, groupId) }
                val balancesDef = async { groupCollection(db, groupId, SUBCOLLECTION_BALANCES).get().await() }
                val settlementsDef = async { fetchConfirmedSettlements(db, groupId) }
                membersMap = membersDef.await().documents.associate { it.id to it.toSpendlyGroupMember() }
                activeExpenses = expensesDef.await()
                existingBalanceIds = balancesDef.await().documents.map { it.id }.toSet()
                otherConfirmedSettlements = settlementsDef.await().filter { it.settlementId != settlementId }
            }

            val settlementRef = groupCollection(db, groupId, SUBCOLLECTION_SETTLEMENTS).document(settlementId)
            val resolvedActorName = resolveActorName(actorId, actorName, membersMap)
            val activityRef = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document()
            val balancesCol = groupCollection(db, groupId, SUBCOLLECTION_BALANCES)

            db.runTransaction { transaction ->
                val snap = transaction.get(settlementRef)
                if (!snap.exists()) {
                    throw NoSuchElementException("Không tìm thấy giao dịch thanh toán")
                }
                val existing = snap.toSpendlySettlement(groupId)
                ensurePendingForConfirmation(existing.status)

                val confirmed = existing.copy(
                    status = SettlementStatus.CONFIRMED,
                    confirmedAtMillis = now,
                    confirmedBy = actorId
                )
                val newBalances = computeBalances(
                    memberIds = group.memberIds.toSet(),
                    existingBalanceUserIds = existingBalanceIds,
                    expensesWithParticipants = activeExpenses,
                    confirmedSettlements = otherConfirmedSettlements + confirmed,
                    now = now
                )
                val activity = buildSettlementConfirmActivity(
                    activityId = activityRef.id,
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    settlement = confirmed,
                    now = now
                )

                transaction.update(
                    settlementRef,
                    mapOf(
                        "status" to SettlementStatus.CONFIRMED.name,
                        "confirmedAtMillis" to now,
                        "confirmedBy" to actorId
                    )
                )
                newBalances.forEach { (userId, balance) ->
                    transaction.set(balancesCol.document(userId), balance.toFirestoreMap())
                }
                transaction.set(activityRef, activity.toFirestoreMap())
                if (confirmed.fromUserId.isNotBlank()) {
                    val notifRef = db.collection(COLLECTION_USERS)
                        .document(confirmed.fromUserId)
                        .collection(SUBCOLLECTION_NOTIFICATIONS)
                        .document()
                    val notification = buildSettlementConfirmNotification(
                        notificationId = notifRef.id,
                        group = group,
                        actorName = resolvedActorName,
                        settlement = confirmed,
                        now = now
                    )
                    transaction.set(notifRef, notification.toFirestoreMap())
                }
            }.await()
        }
    }

    override suspend fun rejectSettlement(
        groupId: String,
        settlementId: String,
        actorId: String,
        actorName: String,
        reason: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && settlementId.isNotBlank()) {
            "Mã nhóm và mã thanh toán không được để trống"
        }
        require(actorId.isNotBlank()) { "Mã người từ chối không được để trống" }
        val now = clock()
        val trimmedReason = reason.trim()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                val groupSettlements = state.settlements[groupId].orEmpty()
                val existing = groupSettlements.firstOrNull { it.settlementId == settlementId }
                    ?: throw NoSuchElementException("Không tìm thấy giao dịch thanh toán")

                ensurePendingForRejection(existing.status)

                val rejected = existing.copy(status = SettlementStatus.REJECTED)
                val updatedSettlements = groupSettlements.map {
                    if (it.settlementId == settlementId) rejected else it
                }
                val resolvedActorName = resolveActorName(actorId, actorName, membersMap)
                val activity = buildSettlementRejectActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    settlement = rejected,
                    reason = trimmedReason,
                    now = now
                )
                val updatedNotifications = if (rejected.fromUserId.isNotBlank()) {
                    val notification = buildSettlementRejectNotification(
                        notificationId = newId("notif"),
                        group = group,
                        actorName = resolvedActorName,
                        settlement = rejected,
                        reason = trimmedReason,
                        now = now
                    )
                    val fromNotifs = state.notifications[rejected.fromUserId].orEmpty()
                    state.notifications + (rejected.fromUserId to (listOf(notification) + fromNotifs))
                } else {
                    state.notifications
                }

                memoryDb.value = state.copy(
                    settlements = state.settlements + (groupId to updatedSettlements),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty())),
                    notifications = updatedNotifications
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()
            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val resolvedActorName = resolveActorName(actorId, actorName, membersMap)

            val settlementRef = groupCollection(db, groupId, SUBCOLLECTION_SETTLEMENTS).document(settlementId)
            val activityRef = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document()

            db.runTransaction { transaction ->
                val snap = transaction.get(settlementRef)
                if (!snap.exists()) {
                    throw NoSuchElementException("Không tìm thấy giao dịch thanh toán")
                }
                val existing = snap.toSpendlySettlement(groupId)
                ensurePendingForRejection(existing.status)

                val activity = buildSettlementRejectActivity(
                    activityId = activityRef.id,
                    groupId = groupId,
                    actorId = actorId,
                    actorName = resolvedActorName,
                    settlement = existing,
                    reason = trimmedReason,
                    now = now
                )
                transaction.update(settlementRef, "status", SettlementStatus.REJECTED.name)
                transaction.set(activityRef, activity.toFirestoreMap())
                if (existing.fromUserId.isNotBlank()) {
                    val notifRef = db.collection(COLLECTION_USERS)
                        .document(existing.fromUserId)
                        .collection(SUBCOLLECTION_NOTIFICATIONS)
                        .document()
                    val notification = buildSettlementRejectNotification(
                        notificationId = notifRef.id,
                        group = group,
                        actorName = resolvedActorName,
                        settlement = existing,
                        reason = trimmedReason,
                        now = now
                    )
                    transaction.set(notifRef, notification.toFirestoreMap())
                }
            }.await()
        }
    }

    // ==================== BUDGET REPOSITORY ====================

    override fun observeBudgets(groupId: String): Flow<List<SpendlyBudget>> {
        val db = firestore ?: return memoryDb
            .map { state ->
                state.budgets[groupId]
                    .orEmpty()
                    .sortedByDescending { it.updatedAtMillis }
            }
            .distinctUntilChanged()

        return callbackFlow {
            if (groupId.isBlank()) {
                trySend(emptyList())
                awaitClose { }
                return@callbackFlow
            }
            val registration = groupCollection(db, groupId, SUBCOLLECTION_BUDGETS)
                .orderBy("updatedAtMillis", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val items = snapshot?.documents
                        ?.map { it.toSpendlyBudget(groupId) }
                        ?.sortedByDescending { it.updatedAtMillis }
                        .orEmpty()
                    trySend(items)
                }
            awaitClose { registration.remove() }
        }
    }

    override suspend fun upsertBudget(
        groupId: String,
        budget: SpendlyBudget
    ): Result<SpendlyBudget> = runCatchingSuspend {
        require(groupId.isNotBlank()) { "Mã nhóm không được để trống" }
        validateBudgetInput(budget)
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                val budgetId = budget.budgetId.ifBlank { newId("bdg") }
                val savedBudget = normalizeBudget(
                    groupId = groupId,
                    budgetId = budgetId,
                    budget = budget,
                    fallbackOwnerId = group.ownerId,
                    now = now
                )
                val actorName = resolveActorName(savedBudget.createdBy, "Thành viên", membersMap)
                val activity = buildUpsertBudgetActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorName = actorName,
                    budget = savedBudget,
                    now = now
                )

                val currentBudgets = state.budgets[groupId].orEmpty()
                    .filterNot { it.budgetId == budgetId }
                val updatedGroups = if (savedBudget.category == null && savedBudget.isActive) {
                    state.groups + (groupId to group.copy(monthlyBudgetLimit = savedBudget.limitAmount))
                } else {
                    state.groups
                }

                memoryDb.value = state.copy(
                    groups = updatedGroups,
                    budgets = state.budgets + (groupId to (listOf(savedBudget) + currentBudgets)),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
                savedBudget
            }
        } else {
            val groupRef = db.collection(COLLECTION_GROUPS).document(groupId)
            val groupSnap = groupRef.get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()
            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }

            val budgetsCol = groupCollection(db, groupId, SUBCOLLECTION_BUDGETS)
            val budgetRef = if (budget.budgetId.isBlank()) {
                budgetsCol.document()
            } else {
                budgetsCol.document(budget.budgetId)
            }
            val savedBudget = normalizeBudget(
                groupId = groupId,
                budgetId = budgetRef.id,
                budget = budget,
                fallbackOwnerId = group.ownerId,
                now = now
            )
            val actorName = resolveActorName(savedBudget.createdBy, "Thành viên", membersMap)
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = buildUpsertBudgetActivity(
                activityId = activityId,
                groupId = groupId,
                actorName = actorName,
                budget = savedBudget,
                now = now
            )

            val batch = db.batch()
            batch.set(budgetRef, savedBudget.toFirestoreMap())
            if (savedBudget.category == null && savedBudget.isActive) {
                batch.update(groupRef, "monthlyBudgetLimit", savedBudget.limitAmount)
            }
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
            savedBudget
        }
    }

    override suspend fun deleteBudget(
        groupId: String,
        budgetId: String
    ): Result<Unit> = runCatchingSuspend {
        require(groupId.isNotBlank() && budgetId.isNotBlank()) {
            "Mã nhóm và mã ngân sách không được để trống"
        }
        val now = clock()
        val db = firestore

        if (db == null) {
            memoryMutex.withLock {
                val state = memoryDb.value
                val group = state.groups[groupId]
                    ?: throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
                val membersMap = state.members[groupId].orEmpty()
                val currentBudgets = state.budgets[groupId].orEmpty()
                val existing = currentBudgets.firstOrNull { it.budgetId == budgetId }
                    ?: throw NoSuchElementException("Không tìm thấy ngân sách")
                val actorId = existing.createdBy.ifBlank { group.ownerId }
                val actorName = resolveActorName(actorId, "Thành viên", membersMap)
                val activity = GroupActivity(
                    activityId = newId("act"),
                    groupId = groupId,
                    actorId = actorId,
                    actorName = actorName,
                    action = ACTION_DELETE_BUDGET,
                    targetId = budgetId,
                    description = "$actorName đã xóa ngân sách ${existing.name}",
                    createdAtMillis = now
                )
                memoryDb.value = state.copy(
                    budgets = state.budgets + (groupId to currentBudgets.filterNot { it.budgetId == budgetId }),
                    activities = state.activities + (groupId to (listOf(activity) + state.activities[groupId].orEmpty()))
                )
            }
        } else {
            val groupSnap = db.collection(COLLECTION_GROUPS).document(groupId).get().await()
            if (!groupSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy nhóm chi tiêu")
            }
            val group = groupSnap.toSpendlyGroup()
            val budgetRef = groupCollection(db, groupId, SUBCOLLECTION_BUDGETS).document(budgetId)
            val budgetSnap = budgetRef.get().await()
            if (!budgetSnap.exists()) {
                throw NoSuchElementException("Không tìm thấy ngân sách")
            }
            val existing = budgetSnap.toSpendlyBudget(groupId)
            val membersSnap = groupCollection(db, groupId, SUBCOLLECTION_MEMBERS).get().await()
            val membersMap = membersSnap.documents.associate { it.id to it.toSpendlyGroupMember() }
            val actorId = existing.createdBy.ifBlank { group.ownerId }
            val actorName = resolveActorName(actorId, "Thành viên", membersMap)
            val activityId = groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document().id
            val activity = GroupActivity(
                activityId = activityId,
                groupId = groupId,
                actorId = actorId,
                actorName = actorName,
                action = ACTION_DELETE_BUDGET,
                targetId = budgetId,
                description = "$actorName đã xóa ngân sách ${existing.name}",
                createdAtMillis = now
            )

            val batch = db.batch()
            batch.delete(budgetRef)
            batch.set(
                groupCollection(db, groupId, SUBCOLLECTION_ACTIVITIES).document(activityId),
                activity.toFirestoreMap()
            )
            batch.commit().await()
        }
    }

    // ==================== PRIVATE HELPERS ====================

    private suspend inline fun <T> runCatchingSuspend(crossinline block: suspend () -> T): Result<T> {
        return try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun groupCollection(
        db: FirebaseFirestore,
        groupId: String,
        subcollection: String
    ): CollectionReference {
        return db.collection(COLLECTION_GROUPS)
            .document(groupId)
            .collection(subcollection)
    }

    private suspend fun fetchActiveExpensesWithParticipants(
        db: FirebaseFirestore,
        groupId: String
    ): List<Pair<SpendlyExpense, List<ExpenseParticipant>>> = coroutineScope {
        val expensesSnap = groupCollection(db, groupId, SUBCOLLECTION_EXPENSES)
            .whereEqualTo("status", RecordStatus.ACTIVE.name)
            .get()
            .await()

        expensesSnap.documents.map { doc ->
            async {
                val expense = doc.toSpendlyExpense(groupId)
                val partSnap = doc.reference.collection(SUBCOLLECTION_PARTICIPANTS).get().await()
                val participants = partSnap.documents.map { it.toExpenseParticipant() }
                expense to participants
            }
        }.awaitAll()
    }

    private suspend fun fetchConfirmedSettlements(
        db: FirebaseFirestore,
        groupId: String
    ): List<SpendlySettlement> {
        val snap = groupCollection(db, groupId, SUBCOLLECTION_SETTLEMENTS)
            .whereEqualTo("status", SettlementStatus.CONFIRMED.name)
            .get()
            .await()
        return snap.documents.map { it.toSpendlySettlement(groupId) }
    }

    private fun validateSettlementRequest(settlement: SpendlySettlement) {
        require(settlement.fromUserId.isNotBlank()) { "Người trả nợ không được để trống" }
        require(settlement.toUserId.isNotBlank()) { "Người nhận thanh toán không được để trống" }
        require(settlement.fromUserId != settlement.toUserId) {
            "Người trả và người nhận không thể trùng nhau"
        }
        require(settlement.amount > 0L) { "Số tiền thanh toán phải lớn hơn 0" }
    }

    private fun ensurePendingForConfirmation(status: SettlementStatus) {
        when (status) {
            SettlementStatus.CONFIRMED ->
                throw IllegalStateException("Giao dịch thanh toán này đã được xác nhận trước đó")
            SettlementStatus.REJECTED ->
                throw IllegalStateException("Giao dịch thanh toán này đã bị từ chối")
            SettlementStatus.PENDING -> Unit
        }
    }

    private fun ensurePendingForRejection(status: SettlementStatus) {
        when (status) {
            SettlementStatus.CONFIRMED ->
                throw IllegalStateException("Không thể từ chối giao dịch đã được xác nhận")
            SettlementStatus.REJECTED ->
                throw IllegalStateException("Giao dịch thanh toán này đã bị từ chối trước đó")
            SettlementStatus.PENDING -> Unit
        }
    }

    private fun validateBudgetInput(budget: SpendlyBudget) {
        require(budget.limitAmount > 0L) { "Hạn mức ngân sách phải lớn hơn 0" }
        if (budget.startDateMillis > 0L && budget.endDateMillis > 0L) {
            require(budget.endDateMillis >= budget.startDateMillis) {
                "Ngày kết thúc phải sau ngày bắt đầu"
            }
        }
    }

    private fun resolveActorName(
        actorId: String,
        fallbackName: String,
        membersMap: Map<String, SpendlyGroupMember>
    ): String {
        return fallbackName.trim()
            .ifBlank { membersMap[actorId]?.displayName.orEmpty() }
            .ifBlank { actorId }
    }

    private fun buildPendingSettlement(
        groupId: String,
        settlementId: String,
        settlement: SpendlySettlement,
        membersMap: Map<String, SpendlyGroupMember>,
        now: Long
    ): SpendlySettlement {
        val fromName = settlement.fromUserName.trim().ifBlank {
            membersMap[settlement.fromUserId]?.displayName ?: settlement.fromUserId
        }
        val toName = settlement.toUserName.trim().ifBlank {
            membersMap[settlement.toUserId]?.displayName ?: settlement.toUserId
        }
        val createdBy = settlement.createdBy.trim().ifBlank { settlement.fromUserId }
        return settlement.copy(
            settlementId = settlementId,
            groupId = groupId,
            fromUserName = fromName,
            toUserName = toName,
            status = SettlementStatus.PENDING,
            note = settlement.note.trim(),
            bankInfo = settlement.bankInfo.trim(),
            createdBy = createdBy,
            createdAtMillis = if (settlement.createdAtMillis > 0L) settlement.createdAtMillis else now,
            confirmedAtMillis = null,
            confirmedBy = null
        )
    }

    private fun buildSettlementRequestActivity(
        activityId: String,
        groupId: String,
        actorName: String,
        settlement: SpendlySettlement,
        now: Long
    ): GroupActivity = GroupActivity(
        activityId = activityId,
        groupId = groupId,
        actorId = settlement.createdBy,
        actorName = actorName,
        action = ACTION_REQUEST_SETTLEMENT,
        targetId = settlement.settlementId,
        description = "${settlement.fromUserName} đã gửi yêu cầu thanh toán ${FintechAlgorithm.formatVnd(settlement.amount)} cho ${settlement.toUserName}",
        createdAtMillis = now
    )

    private fun buildSettlementRequestNotification(
        notificationId: String,
        group: SpendlyGroup,
        settlement: SpendlySettlement,
        now: Long
    ): SpendlyNotification = SpendlyNotification(
        id = notificationId,
        title = "Yêu cầu xác nhận thanh toán",
        body = "${settlement.fromUserName} đã gửi yêu cầu thanh toán ${FintechAlgorithm.formatVnd(settlement.amount)} trong nhóm ${group.name}",
        type = "SETTLEMENT_REQUEST",
        groupId = group.groupId,
        isRead = false,
        createdAtMillis = now
    )

    private fun buildSettlementConfirmActivity(
        activityId: String,
        groupId: String,
        actorId: String,
        actorName: String,
        settlement: SpendlySettlement,
        now: Long
    ): GroupActivity = GroupActivity(
        activityId = activityId,
        groupId = groupId,
        actorId = actorId,
        actorName = actorName,
        action = ACTION_CONFIRM_SETTLEMENT,
        targetId = settlement.settlementId,
        description = "$actorName đã xác nhận khoản thanh toán ${FintechAlgorithm.formatVnd(settlement.amount)} từ ${settlement.fromUserName}",
        createdAtMillis = now
    )

    private fun buildSettlementConfirmNotification(
        notificationId: String,
        group: SpendlyGroup,
        actorName: String,
        settlement: SpendlySettlement,
        now: Long
    ): SpendlyNotification = SpendlyNotification(
        id = notificationId,
        title = "Thanh toán đã được xác nhận",
        body = "$actorName đã xác nhận nhận ${FintechAlgorithm.formatVnd(settlement.amount)} trong nhóm ${group.name}",
        type = "SETTLEMENT_CONFIRMED",
        groupId = group.groupId,
        isRead = false,
        createdAtMillis = now
    )

    private fun buildSettlementRejectActivity(
        activityId: String,
        groupId: String,
        actorId: String,
        actorName: String,
        settlement: SpendlySettlement,
        reason: String,
        now: Long
    ): GroupActivity {
        val reasonSuffix = if (reason.isNotBlank()) " ($reason)" else ""
        return GroupActivity(
            activityId = activityId,
            groupId = groupId,
            actorId = actorId,
            actorName = actorName,
            action = ACTION_REJECT_SETTLEMENT,
            targetId = settlement.settlementId,
            description = "$actorName đã từ chối khoản thanh toán ${FintechAlgorithm.formatVnd(settlement.amount)}$reasonSuffix",
            createdAtMillis = now
        )
    }

    private fun buildSettlementRejectNotification(
        notificationId: String,
        group: SpendlyGroup,
        actorName: String,
        settlement: SpendlySettlement,
        reason: String,
        now: Long
    ): SpendlyNotification {
        val reasonSuffix = if (reason.isNotBlank()) ": $reason" else ""
        return SpendlyNotification(
            id = notificationId,
            title = "Thanh toán bị từ chối",
            body = "$actorName đã từ chối khoản thanh toán ${FintechAlgorithm.formatVnd(settlement.amount)} trong nhóm ${group.name}$reasonSuffix",
            type = "SETTLEMENT_REJECTED",
            groupId = group.groupId,
            isRead = false,
            createdAtMillis = now
        )
    }

    private fun normalizeBudget(
        groupId: String,
        budgetId: String,
        budget: SpendlyBudget,
        fallbackOwnerId: String,
        now: Long
    ): SpendlyBudget {
        val resolvedName = budget.name.trim().ifBlank {
            budget.category?.let { "Ngân sách ${it.label}" } ?: "Ngân sách tháng nhóm"
        }
        val resolvedPeriod = budget.period.trim().ifBlank { "MONTHLY" }
        val resolvedCreator = budget.createdBy.trim().ifBlank { fallbackOwnerId }
        return budget.copy(
            budgetId = budgetId,
            groupId = groupId,
            name = resolvedName,
            period = resolvedPeriod,
            createdBy = resolvedCreator,
            updatedAtMillis = now
        )
    }

    private fun buildUpsertBudgetActivity(
        activityId: String,
        groupId: String,
        actorName: String,
        budget: SpendlyBudget,
        now: Long
    ): GroupActivity = GroupActivity(
        activityId = activityId,
        groupId = groupId,
        actorId = budget.createdBy,
        actorName = actorName,
        action = ACTION_UPSERT_BUDGET,
        targetId = budget.budgetId,
        description = "$actorName đã thiết lập ngân sách ${budget.name} (${FintechAlgorithm.formatVnd(budget.limitAmount)})",
        createdAtMillis = now
    )

    private fun validateRoleChange(
        group: SpendlyGroup,
        targetUserId: String,
        newRole: GroupRole
    ) {
        if (targetUserId == group.ownerId && newRole != GroupRole.OWNER) {
            throw IllegalStateException("Không thể thay đổi vai trò của Trưởng nhóm")
        }
        if (targetUserId != group.ownerId && newRole == GroupRole.OWNER) {
            throw IllegalStateException("Không thể gán vai trò Trưởng nhóm cho thành viên khác")
        }
    }

    private fun checkCreatorPermission(
        membersMap: Map<String, SpendlyGroupMember>,
        createdBy: String
    ) {
        if (createdBy.isBlank()) return
        val creator = membersMap[createdBy] ?: return
        if (creator.status != STATUS_ACTIVE || !creator.canAddExpense || creator.role == GroupRole.VIEWER) {
            throw SecurityException("Thành viên ${creator.displayName} không có quyền tạo giao dịch")
        }
    }

    private fun validateExpenseInput(
        expense: SpendlyExpense,
        participants: List<ExpenseParticipant>
    ) {
        require(expense.title.isNotBlank()) { "Tên khoản chi không được để trống" }
        require(expense.amount > 0L) { "Số tiền khoản chi phải lớn hơn 0" }
        require(expense.payerId.isNotBlank()) { "Người thanh toán không được để trống" }
        require(participants.isNotEmpty()) { "Khoản chi phải có ít nhất một người tham gia" }
        require(participants.all { it.userId.isNotBlank() }) { "Mã người tham gia không hợp lệ" }
        require(participants.map { it.userId }.distinct().size == participants.size) {
            "Danh sách người tham gia bị trùng lặp"
        }
        require(participants.all { it.shareAmount >= 0L && it.paidAmount >= 0L }) {
            "Số tiền chia không được âm"
        }
        val shareSum = participants.sumOf { it.shareAmount }
        require(shareSum == expense.amount) {
            "Tổng phần chia ($shareSum) phải bằng tổng số tiền khoản chi (${expense.amount})"
        }
    }

    private fun computeBalances(
        memberIds: Set<String>,
        existingBalanceUserIds: Set<String>,
        expensesWithParticipants: List<Pair<SpendlyExpense, List<ExpenseParticipant>>>,
        confirmedSettlements: List<SpendlySettlement> = emptyList(),
        now: Long
    ): Map<String, MemberBalance> {
        val allUserIds = linkedSetOf<String>()
        allUserIds.addAll(memberIds)
        allUserIds.addAll(existingBalanceUserIds)

        val paidByUser = mutableMapOf<String, Long>()
        val shareByUser = mutableMapOf<String, Long>()

        expensesWithParticipants.forEach { (expense, participants) ->
            if (expense.status != RecordStatus.ACTIVE) return@forEach

            val explicitPaidSum = participants.sumOf { it.paidAmount }
            if (explicitPaidSum == expense.amount && participants.any { it.paidAmount > 0L }) {
                participants.forEach { participant ->
                    if (participant.paidAmount > 0L) {
                        allUserIds.add(participant.userId)
                        paidByUser[participant.userId] =
                            (paidByUser[participant.userId] ?: 0L) + participant.paidAmount
                    }
                }
            } else if (expense.payerId.isNotBlank()) {
                allUserIds.add(expense.payerId)
                paidByUser[expense.payerId] =
                    (paidByUser[expense.payerId] ?: 0L) + expense.amount
            }

            participants.forEach { participant ->
                allUserIds.add(participant.userId)
                shareByUser[participant.userId] =
                    (shareByUser[participant.userId] ?: 0L) + participant.shareAmount
            }
        }

        confirmedSettlements.forEach { settlement ->
            if (settlement.status != SettlementStatus.CONFIRMED || settlement.amount <= 0L) return@forEach
            if (settlement.fromUserId.isBlank() || settlement.toUserId.isBlank()) return@forEach
            if (settlement.fromUserId == settlement.toUserId) return@forEach

            allUserIds.add(settlement.fromUserId)
            allUserIds.add(settlement.toUserId)

            paidByUser[settlement.fromUserId] =
                (paidByUser[settlement.fromUserId] ?: 0L) + settlement.amount
            shareByUser[settlement.toUserId] =
                (shareByUser[settlement.toUserId] ?: 0L) + settlement.amount
        }

        return allUserIds.associateWith { userId ->
            val totalPaid = paidByUser[userId] ?: 0L
            val totalShare = shareByUser[userId] ?: 0L
            MemberBalance(
                userId = userId,
                totalPaid = totalPaid,
                totalShare = totalShare,
                netBalance = totalPaid - totalShare,
                updatedAtMillis = now
            )
        }
    }

    private fun computeInitials(name: String): String {
        val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        val initials = parts.mapNotNull { it.firstOrNull()?.toString() }
            .takeLast(2)
            .joinToString("")
            .uppercase()
        return initials.ifBlank { "TV" }
    }

    private fun newId(prefix: String): String {
        return "${prefix}_${UUID.randomUUID().toString().replace("-", "").take(12)}"
    }

    private fun randomInviteCode(): String {
        return buildString(INVITE_CODE_LENGTH) {
            repeat(INVITE_CODE_LENGTH) {
                append(INVITE_ALPHABET[random.nextInt(INVITE_ALPHABET.length)])
            }
        }
    }

    private fun generateUniqueInviteCodeInMemory(existingCodes: Set<String>): String {
        repeat(20) {
            val candidate = randomInviteCode()
            if (candidate !in existingCodes) return candidate
        }
        return randomInviteCode()
    }

    private suspend fun generateUniqueInviteCodeFirestore(db: FirebaseFirestore): String {
        repeat(10) {
            val candidate = randomInviteCode()
            val exists = db.collection(COLLECTION_INVITATIONS)
                .document(candidate)
                .get()
                .await()
                .exists()
            if (!exists) return candidate
        }
        return randomInviteCode()
    }

    // ==================== FIRESTORE MAPPING ====================

    private fun SpendlyUser.toFirestoreMap(): Map<String, Any> = mapOf(
        "uid" to uid,
        "fullName" to fullName,
        "email" to email,
        "phone" to phone,
        "avatarUrl" to avatarUrl,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "isActive" to isActive
    )

    private fun DocumentSnapshot.toSpendlyUser(): SpendlyUser = SpendlyUser(
        uid = getString("uid").orEmpty().ifBlank { id },
        fullName = getString("fullName").orEmpty(),
        email = getString("email").orEmpty(),
        phone = getString("phone").orEmpty(),
        avatarUrl = getString("avatarUrl").orEmpty(),
        createdAtMillis = longValue("createdAtMillis"),
        updatedAtMillis = longValue("updatedAtMillis"),
        isActive = getBoolean("isActive") ?: true
    )

    private fun SpendlyNotification.toFirestoreMap(): Map<String, Any> = mapOf(
        "id" to id,
        "title" to title,
        "body" to body,
        "type" to type,
        "groupId" to groupId,
        "isRead" to isRead,
        "createdAtMillis" to createdAtMillis
    )

    private fun DocumentSnapshot.toSpendlyNotification(): SpendlyNotification = SpendlyNotification(
        id = getString("id").orEmpty().ifBlank { id },
        title = getString("title").orEmpty(),
        body = getString("body").orEmpty(),
        type = getString("type").orEmpty(),
        groupId = getString("groupId").orEmpty(),
        isRead = getBoolean("isRead") ?: false,
        createdAtMillis = longValue("createdAtMillis")
    )

    private fun SpendlyGroup.toFirestoreMap(): Map<String, Any> = mapOf(
        "groupId" to groupId,
        "name" to name,
        "description" to description,
        "avatarUrl" to avatarUrl,
        "ownerId" to ownerId,
        "inviteCode" to inviteCode,
        "memberCount" to memberCount,
        "currency" to currency,
        "monthlyBudgetLimit" to monthlyBudgetLimit,
        "createdAtMillis" to createdAtMillis,
        "status" to status,
        "memberIds" to memberIds
    )

    private fun DocumentSnapshot.toSpendlyGroup(): SpendlyGroup {
        val rawMemberIds = (get("memberIds") as? List<*>)
            ?.mapNotNull { it as? String }
            .orEmpty()
        return SpendlyGroup(
            groupId = getString("groupId").orEmpty().ifBlank { id },
            name = getString("name").orEmpty(),
            description = getString("description").orEmpty(),
            avatarUrl = getString("avatarUrl").orEmpty(),
            ownerId = getString("ownerId").orEmpty(),
            inviteCode = getString("inviteCode").orEmpty(),
            memberCount = intValue("memberCount", rawMemberIds.size),
            currency = getString("currency").orEmpty().ifBlank { "VND" },
            monthlyBudgetLimit = longValue("monthlyBudgetLimit", 10_000_000L),
            createdAtMillis = longValue("createdAtMillis"),
            status = getString("status").orEmpty().ifBlank { STATUS_ACTIVE },
            memberIds = rawMemberIds
        )
    }

    private fun SpendlyGroupMember.toFirestoreMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName,
        "initials" to initials,
        "phone" to phone,
        "role" to role.name,
        "joinedAtMillis" to joinedAtMillis,
        "status" to status,
        "nickname" to nickname,
        "canAddExpense" to canAddExpense
    )

    private fun DocumentSnapshot.toSpendlyGroupMember(): SpendlyGroupMember = SpendlyGroupMember(
        userId = getString("userId").orEmpty().ifBlank { id },
        displayName = getString("displayName").orEmpty(),
        initials = getString("initials").orEmpty(),
        phone = getString("phone").orEmpty(),
        role = GroupRole.fromRaw(getString("role")),
        joinedAtMillis = longValue("joinedAtMillis"),
        status = getString("status").orEmpty().ifBlank { STATUS_ACTIVE },
        nickname = getString("nickname").orEmpty(),
        canAddExpense = getBoolean("canAddExpense") ?: true
    )

    private fun SpendlyExpense.toFirestoreMap(): Map<String, Any> = mapOf(
        "expenseId" to expenseId,
        "groupId" to groupId,
        "title" to title,
        "amount" to amount,
        "category" to category.name,
        "payerId" to payerId,
        "payerName" to payerName,
        "createdBy" to createdBy,
        "splitType" to splitType.name,
        "description" to description,
        "receiptUrl" to receiptUrl,
        "expenseDateMillis" to expenseDateMillis,
        "createdAtMillis" to createdAtMillis,
        "updatedAtMillis" to updatedAtMillis,
        "status" to status.name
    )

    private fun DocumentSnapshot.toSpendlyExpense(fallbackGroupId: String): SpendlyExpense = SpendlyExpense(
        expenseId = getString("expenseId").orEmpty().ifBlank { id },
        groupId = getString("groupId").orEmpty().ifBlank { fallbackGroupId },
        title = getString("title").orEmpty(),
        amount = longValue("amount"),
        category = SpendlyCategory.fromRaw(getString("category")),
        payerId = getString("payerId").orEmpty(),
        payerName = getString("payerName").orEmpty(),
        createdBy = getString("createdBy").orEmpty(),
        splitType = SplitMethod.fromRaw(getString("splitType")),
        description = getString("description").orEmpty(),
        receiptUrl = getString("receiptUrl").orEmpty(),
        expenseDateMillis = longValue("expenseDateMillis"),
        createdAtMillis = longValue("createdAtMillis"),
        updatedAtMillis = longValue("updatedAtMillis"),
        status = RecordStatus.fromRaw(getString("status"))
    )

    private fun ExpenseParticipant.toFirestoreMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to displayName,
        "shareAmount" to shareAmount,
        "percentage" to percentage,
        "paidAmount" to paidAmount
    )

    private fun DocumentSnapshot.toExpenseParticipant(): ExpenseParticipant = ExpenseParticipant(
        userId = getString("userId").orEmpty().ifBlank { id },
        displayName = getString("displayName").orEmpty(),
        shareAmount = longValue("shareAmount"),
        percentage = doubleValue("percentage"),
        paidAmount = longValue("paidAmount")
    )

    private fun SpendlyIncome.toFirestoreMap(): Map<String, Any> = mapOf(
        "incomeId" to incomeId,
        "groupId" to groupId,
        "title" to title,
        "amount" to amount,
        "payerId" to payerId,
        "payerName" to payerName,
        "category" to category.name,
        "description" to description,
        "incomeDateMillis" to incomeDateMillis,
        "createdBy" to createdBy,
        "createdAtMillis" to createdAtMillis,
        "status" to status.name,
        "isFundContribution" to isFundContribution
    )

    private fun DocumentSnapshot.toSpendlyIncome(fallbackGroupId: String): SpendlyIncome = SpendlyIncome(
        incomeId = getString("incomeId").orEmpty().ifBlank { id },
        groupId = getString("groupId").orEmpty().ifBlank { fallbackGroupId },
        title = getString("title").orEmpty(),
        amount = longValue("amount"),
        payerId = getString("payerId").orEmpty(),
        payerName = getString("payerName").orEmpty(),
        category = SpendlyCategory.fromRaw(getString("category")),
        description = getString("description").orEmpty(),
        incomeDateMillis = longValue("incomeDateMillis"),
        createdBy = getString("createdBy").orEmpty(),
        createdAtMillis = longValue("createdAtMillis"),
        status = RecordStatus.fromRaw(getString("status")),
        isFundContribution = getBoolean("isFundContribution") ?: true
    )

    private fun MemberBalance.toFirestoreMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "totalPaid" to totalPaid,
        "totalShare" to totalShare,
        "netBalance" to netBalance,
        "updatedAtMillis" to updatedAtMillis
    )

    private fun DocumentSnapshot.toMemberBalance(): MemberBalance = MemberBalance(
        userId = getString("userId").orEmpty().ifBlank { id },
        totalPaid = longValue("totalPaid"),
        totalShare = longValue("totalShare"),
        netBalance = longValue("netBalance"),
        updatedAtMillis = longValue("updatedAtMillis")
    )

    private fun GroupActivity.toFirestoreMap(): Map<String, Any> = mapOf(
        "activityId" to activityId,
        "groupId" to groupId,
        "actorId" to actorId,
        "actorName" to actorName,
        "action" to action,
        "targetId" to targetId,
        "description" to description,
        "createdAtMillis" to createdAtMillis
    )

    private fun DocumentSnapshot.toGroupActivity(): GroupActivity = GroupActivity(
        activityId = getString("activityId").orEmpty().ifBlank { id },
        groupId = getString("groupId").orEmpty(),
        actorId = getString("actorId").orEmpty(),
        actorName = getString("actorName").orEmpty(),
        action = getString("action").orEmpty(),
        targetId = getString("targetId").orEmpty(),
        description = getString("description").orEmpty(),
        createdAtMillis = longValue("createdAtMillis")
    )

    private fun SpendlySettlement.toFirestoreMap(): Map<String, Any?> = mapOf(
        "settlementId" to settlementId,
        "groupId" to groupId,
        "fromUserId" to fromUserId,
        "fromUserName" to fromUserName,
        "toUserId" to toUserId,
        "toUserName" to toUserName,
        "amount" to amount,
        "status" to status.name,
        "note" to note,
        "bankInfo" to bankInfo,
        "createdBy" to createdBy,
        "createdAtMillis" to createdAtMillis,
        "confirmedAtMillis" to confirmedAtMillis,
        "confirmedBy" to confirmedBy
    )

    private fun DocumentSnapshot.toSpendlySettlement(fallbackGroupId: String): SpendlySettlement = SpendlySettlement(
        settlementId = getString("settlementId").orEmpty().ifBlank { id },
        groupId = getString("groupId").orEmpty().ifBlank { fallbackGroupId },
        fromUserId = getString("fromUserId").orEmpty(),
        fromUserName = getString("fromUserName").orEmpty(),
        toUserId = getString("toUserId").orEmpty(),
        toUserName = getString("toUserName").orEmpty(),
        amount = longValue("amount"),
        status = SettlementStatus.fromRaw(getString("status")),
        note = getString("note").orEmpty(),
        bankInfo = getString("bankInfo").orEmpty(),
        createdBy = getString("createdBy").orEmpty(),
        createdAtMillis = longValue("createdAtMillis"),
        confirmedAtMillis = (get("confirmedAtMillis") as? Number)?.toLong(),
        confirmedBy = getString("confirmedBy")
    )

    private fun SpendlyBudget.toFirestoreMap(): Map<String, Any?> = mapOf(
        "budgetId" to budgetId,
        "groupId" to groupId,
        "name" to name,
        "limitAmount" to limitAmount,
        "period" to period,
        "startDateMillis" to startDateMillis,
        "endDateMillis" to endDateMillis,
        "category" to category?.name,
        "isActive" to isActive,
        "createdBy" to createdBy,
        "updatedAtMillis" to updatedAtMillis
    )

    private fun DocumentSnapshot.toSpendlyBudget(fallbackGroupId: String): SpendlyBudget {
        val rawCategory = getString("category")?.takeIf { it.isNotBlank() }
        return SpendlyBudget(
            budgetId = getString("budgetId").orEmpty().ifBlank { id },
            groupId = getString("groupId").orEmpty().ifBlank { fallbackGroupId },
            name = getString("name").orEmpty(),
            limitAmount = longValue("limitAmount"),
            period = getString("period").orEmpty().ifBlank { "MONTHLY" },
            startDateMillis = longValue("startDateMillis"),
            endDateMillis = longValue("endDateMillis"),
            category = rawCategory?.let { SpendlyCategory.fromRaw(it) },
            isActive = getBoolean("isActive") ?: true,
            createdBy = getString("createdBy").orEmpty(),
            updatedAtMillis = longValue("updatedAtMillis")
        )
    }

    private fun DocumentSnapshot.longValue(key: String, default: Long = 0L): Long {
        return (get(key) as? Number)?.toLong() ?: default
    }

    private fun DocumentSnapshot.intValue(key: String, default: Int = 0): Int {
        return (get(key) as? Number)?.toInt() ?: default
    }

    private fun DocumentSnapshot.doubleValue(key: String, default: Double = 0.0): Double {
        return (get(key) as? Number)?.toDouble() ?: default
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_GROUPS = "groups"
        private const val COLLECTION_INVITATIONS = "invitations"

        private const val SUBCOLLECTION_NOTIFICATIONS = "notifications"
        private const val SUBCOLLECTION_MEMBERS = "members"
        private const val SUBCOLLECTION_EXPENSES = "expenses"
        private const val SUBCOLLECTION_PARTICIPANTS = "participants"
        private const val SUBCOLLECTION_INCOMES = "incomes"
        private const val SUBCOLLECTION_SETTLEMENTS = "settlements"
        private const val SUBCOLLECTION_BUDGETS = "budgets"
        private const val SUBCOLLECTION_ACTIVITIES = "activities"
        private const val SUBCOLLECTION_BALANCES = "balances"

        private const val STATUS_ACTIVE = "ACTIVE"
        private const val STATUS_LEFT = "LEFT"
        private const val STATUS_ARCHIVED = "ARCHIVED"

        private const val INVITE_CODE_LENGTH = 6
        private const val INVITE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

        const val ACTION_CREATE_GROUP = "CREATE_GROUP"
        const val ACTION_JOIN_GROUP = "JOIN_GROUP"
        const val ACTION_ADD_MEMBER = "ADD_MEMBER"
        const val ACTION_UPDATE_MEMBER_ROLE = "UPDATE_MEMBER_ROLE"
        const val ACTION_REMOVE_MEMBER = "REMOVE_MEMBER"
        const val ACTION_UPDATE_GROUP_SETTINGS = "UPDATE_GROUP_SETTINGS"
        const val ACTION_CREATE_EXPENSE = "CREATE_EXPENSE"
        const val ACTION_UPDATE_EXPENSE = "UPDATE_EXPENSE"
        const val ACTION_CANCEL_EXPENSE = "CANCEL_EXPENSE"
        const val ACTION_CREATE_INCOME = "CREATE_INCOME"
        const val ACTION_CANCEL_INCOME = "CANCEL_INCOME"
        const val ACTION_REQUEST_SETTLEMENT = "REQUEST_SETTLEMENT"
        const val ACTION_CONFIRM_SETTLEMENT = "CONFIRM_SETTLEMENT"
        const val ACTION_REJECT_SETTLEMENT = "REJECT_SETTLEMENT"
        const val ACTION_UPSERT_BUDGET = "UPSERT_BUDGET"
        const val ACTION_DELETE_BUDGET = "DELETE_BUDGET"
    }
}

class FirestoreUserRepository(
    private val delegate: FirestoreSpendlyRepository = FirestoreSpendlyRepository()
) : UserRepository by delegate

class FirestoreGroupRepository(
    private val delegate: FirestoreSpendlyRepository = FirestoreSpendlyRepository()
) : GroupRepository by delegate

class FirestoreTransactionRepository(
    private val delegate: FirestoreSpendlyRepository = FirestoreSpendlyRepository()
) : TransactionRepository by delegate

class FirestoreSettlementRepository(
    private val delegate: FirestoreSpendlyRepository = FirestoreSpendlyRepository()
) : SettlementRepository by delegate

class FirestoreBudgetRepository(
    private val delegate: FirestoreSpendlyRepository = FirestoreSpendlyRepository()
) : BudgetRepository by delegate
