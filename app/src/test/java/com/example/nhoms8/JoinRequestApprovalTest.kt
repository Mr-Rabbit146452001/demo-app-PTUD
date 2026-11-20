package com.example.nhoms8

import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.GroupRole
import com.example.nhoms8.domain.model.JoinRequestStatus
import com.example.nhoms8.domain.model.JoinRequestSubmissionResult
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.feature.groups.GroupViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class JoinRequestApprovalTest {

    private val testDispatcher = StandardTestDispatcher()

    private val treasurer = SpendlyUser(
        uid = "uid_treasurer",
        fullName = "Lê Văn Hùng",
        email = "hung@spendly.vn",
        phone = "0901000001"
    )
    private val requester = SpendlyUser(
        uid = "uid_linh",
        fullName = "Lý Việt Linh",
        email = "linh@spendly.vn",
        phone = "0902000002"
    )
    private val regularMember = SpendlyUser(
        uid = "uid_dat",
        fullName = "Nguyễn Phan Tấn Đạt",
        email = "dat@spendly.vn",
        phone = "0903000003"
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
    fun `requestJoinGroupByInviteCode queues pending request without adding member and notifies treasurer`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Du Lịch").getOrThrow()

        val submission = repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()
        assertTrue(submission is JoinRequestSubmissionResult.PendingApproval)
        val pending = submission as JoinRequestSubmissionResult.PendingApproval
        assertFalse(pending.alreadyPending)
        assertEquals(JoinRequestStatus.PENDING, pending.request.status)
        assertEquals(group.groupId, pending.request.groupId)

        // Người dùng chưa được thêm vào nhóm khi Thủ quỹ chưa duyệt
        assertTrue(repo.observeUserGroups(requester.uid).first().isEmpty())
        val members = repo.observeGroupMembers(group.groupId).first()
        assertFalse(members.any { it.userId == requester.uid })

        // Yêu cầu xuất hiện ở cả phía nhóm lẫn phía người gửi
        val groupRequests = repo.observeGroupJoinRequests(group.groupId).first()
        assertEquals(1, groupRequests.size)
        assertEquals(requester.uid, groupRequests.first().userId)

        val userRequests = repo.observeUserJoinRequests(requester.uid).first()
        assertEquals(1, userRequests.size)
        assertEquals(group.groupId, userRequests.first().groupId)

        // Thủ quỹ nhận thông báo JOIN_REQUEST trỏ tới nhóm
        val treasurerNotifs = repo.observeNotifications(treasurer.uid).first()
        val reqNotif = treasurerNotifs.firstOrNull { it.type == "JOIN_REQUEST" }
        assertEquals(group.groupId, reqNotif?.groupId)
    }

    @Test
    fun `duplicate pending request returns alreadyPending true without duplicate treasurer notification`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Ăn Uống").getOrThrow()

        val first = repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()
            as JoinRequestSubmissionResult.PendingApproval
        assertFalse(first.alreadyPending)

        val notifCountAfterFirst = repo.observeNotifications(treasurer.uid).first()
            .count { it.type == "JOIN_REQUEST" }
        assertEquals(1, notifCountAfterFirst)

        val second = repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()
            as JoinRequestSubmissionResult.PendingApproval
        assertTrue(second.alreadyPending)

        val notifCountAfterSecond = repo.observeNotifications(treasurer.uid).first()
            .count { it.type == "JOIN_REQUEST" }
        assertEquals(1, notifCountAfterSecond)
        assertEquals(1, repo.observeGroupJoinRequests(group.groupId).first().size)
    }

    @Test
    fun `regular member cannot approve or reject join request`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Lớp").getOrThrow()
        repo.joinGroupByInviteCode(regularMember, group.inviteCode).getOrThrow()
        repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()

        val approveResult = repo.approveJoinRequest(group.groupId, requester.uid, regularMember)
        assertTrue(approveResult.isFailure)

        val rejectResult = repo.rejectJoinRequest(group.groupId, requester.uid, regularMember)
        assertTrue(rejectResult.isFailure)

        // Yêu cầu vẫn còn ở trạng thái chờ duyệt
        assertEquals(1, repo.observeGroupJoinRequests(group.groupId).first().size)
    }

    @Test
    fun `approveJoinRequest activates member initializes balance and sends JOIN_APPROVED notification`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Nhóm 08").getOrThrow()
        repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()

        val approvedMember = repo.approveJoinRequest(
            groupId = group.groupId,
            requestUserId = requester.uid,
            reviewer = treasurer
        ).getOrThrow()

        assertEquals(requester.uid, approvedMember.userId)
        assertEquals(GroupRole.MEMBER, approvedMember.role)

        // Yêu cầu không còn trong hàng chờ PENDING
        assertTrue(repo.observeGroupJoinRequests(group.groupId).first().isEmpty())
        assertTrue(repo.observeUserJoinRequests(requester.uid).first().isEmpty())

        // Thành viên và memberCount của nhóm được cập nhật
        val members = repo.observeGroupMembers(group.groupId).first()
        assertEquals(2, members.size)
        assertTrue(members.any { it.userId == requester.uid })

        val requesterGroups = repo.observeUserGroups(requester.uid).first()
        assertEquals(1, requesterGroups.size)
        assertEquals(2, requesterGroups.first().memberCount)

        // Balance khởi tạo 0L
        val balance = repo.observeMemberBalances(group.groupId).first()
            .firstOrNull { it.userId == requester.uid }
        assertEquals(0L, balance?.netBalance)

        // Thông báo duyệt gửi tới người dùng
        val approvedNotif = repo.observeNotifications(requester.uid).first()
            .firstOrNull { it.type == "JOIN_APPROVED" }
        assertEquals(group.groupId, approvedNotif?.groupId)
    }

    @Test
    fun `rejectJoinRequest keeps user out notifies JOIN_REJECTED and allows re-requesting later`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Sinh Hoạt").getOrThrow()
        repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()

        repo.rejectJoinRequest(
            groupId = group.groupId,
            requestUserId = requester.uid,
            reviewer = treasurer
        ).getOrThrow()

        assertTrue(repo.observeUserGroups(requester.uid).first().isEmpty())
        assertFalse(repo.observeGroupMembers(group.groupId).first().any { it.userId == requester.uid })
        assertTrue(repo.observeGroupJoinRequests(group.groupId).first().isEmpty())

        val rejectedNotif = repo.observeNotifications(requester.uid).first()
            .firstOrNull { it.type == "JOIN_REJECTED" }
        assertEquals("", rejectedNotif?.groupId)

        // Có thể gửi lại yêu cầu mới sau khi bị từ chối
        val retry = repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()
            as JoinRequestSubmissionResult.PendingApproval
        assertFalse(retry.alreadyPending)
        assertEquals(1, repo.observeGroupJoinRequests(group.groupId).first().size)
    }

    @Test
    fun `cancelJoinRequest removes pending request from both sides and blocks approval afterwards`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Teambuilding").getOrThrow()
        repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()

        repo.cancelJoinRequest(group.groupId, requester.uid).getOrThrow()

        assertTrue(repo.observeGroupJoinRequests(group.groupId).first().isEmpty())
        assertTrue(repo.observeUserJoinRequests(requester.uid).first().isEmpty())

        val approveAfterCancel = repo.approveJoinRequest(group.groupId, requester.uid, treasurer)
        assertTrue(approveAfterCancel.isFailure)
    }

    @Test
    fun `already active member requesting join returns AlreadyMember without creating request`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val group = repo.createGroup(treasurer, "Quỹ Hiện Hữu").getOrThrow()
        repo.joinGroupByInviteCode(requester, group.inviteCode).getOrThrow()

        val result = repo.requestJoinGroupByInviteCode(requester, group.inviteCode).getOrThrow()
        assertTrue(result is JoinRequestSubmissionResult.AlreadyMember)
        assertEquals(group.groupId, (result as JoinRequestSubmissionResult.AlreadyMember).group.groupId)
        assertTrue(repo.observeGroupJoinRequests(group.groupId).first().isEmpty())
    }

    @Test
    fun `GroupViewModel orchestrates request approval rejection and cancellation flows`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val treasurerVm = GroupViewModel(repo, repo, repo, autoBindDefaultUser = false)
        val requesterVm = GroupViewModel(repo, repo, repo, autoBindDefaultUser = false)

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            treasurerVm.incomingJoinRequestsForManager.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            treasurerVm.groupsUiState.collect {}
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            requesterVm.myPendingJoinRequests.collect {}
        }

        treasurerVm.bindUser(treasurer)
        requesterVm.bindUser(requester)
        advanceUntilIdle()

        var createdGroupId = ""
        var inviteCode = ""
        treasurerVm.createGroup("Quỹ Đồ Án K23", "Test approval", 5_000_000L) { group ->
            createdGroupId = group.groupId
            inviteCode = group.inviteCode
        }
        advanceUntilIdle()

        val detailFlow = treasurerVm.observeGroupDetail(createdGroupId)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            detailFlow.collect {}
        }

        // Người dùng gửi yêu cầu -> thấy trong myPendingJoinRequests, Thủ quỹ thấy trong incomingJoinRequestsForManager & detailFlow
        var pendingCallbackInvoked = false
        requesterVm.requestJoinGroup(
            inviteCode = inviteCode,
            onPendingApproval = { req, already ->
                pendingCallbackInvoked = !already && req.groupId == createdGroupId
            }
        )
        advanceUntilIdle()

        assertTrue(pendingCallbackInvoked)
        assertEquals(1, requesterVm.myPendingJoinRequests.value.size)
        assertEquals(1, treasurerVm.incomingJoinRequestsForManager.value.size)
        assertEquals(1, detailFlow.value.pendingJoinRequests.size)
        assertEquals(1, treasurerVm.groupsUiState.value.pendingApprovalsCountByGroup[createdGroupId])

        // Người gửi hủy -> hàng đợi trống; gửi lại -> Thủ quỹ duyệt -> thành viên được thêm vào
        requesterVm.cancelJoinRequest(createdGroupId)
        advanceUntilIdle()
        assertTrue(requesterVm.myPendingJoinRequests.value.isEmpty())
        assertTrue(treasurerVm.incomingJoinRequestsForManager.value.isEmpty())

        requesterVm.requestJoinGroup(inviteCode = inviteCode)
        advanceUntilIdle()
        assertEquals(1, treasurerVm.incomingJoinRequestsForManager.value.size)

        treasurerVm.approveJoinRequest(createdGroupId, requester.uid)
        advanceUntilIdle()
        assertTrue(treasurerVm.incomingJoinRequestsForManager.value.isEmpty())
        assertTrue(detailFlow.value.members.any { it.userId == requester.uid })

        // Thử từ chối với người dùng thứ 3
        var rejectedSuccess = false
        requesterVm.bindUser(regularMember)
        advanceUntilIdle()
        requesterVm.requestJoinGroup(inviteCode = inviteCode)
        advanceUntilIdle()
        assertEquals(1, treasurerVm.incomingJoinRequestsForManager.value.size)

        treasurerVm.rejectJoinRequest(createdGroupId, regularMember.uid) {
            rejectedSuccess = true
        }
        advanceUntilIdle()
        assertTrue(rejectedSuccess)
        assertTrue(treasurerVm.incomingJoinRequestsForManager.value.isEmpty())
        assertFalse(detailFlow.value.members.any { it.userId == regularMember.uid })
    }
}
