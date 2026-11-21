package com.example.nhoms8

import com.example.nhoms8.data.firestore.FirestoreSpendlyRepository
import com.example.nhoms8.domain.model.SpendlyUser
import com.example.nhoms8.feature.groups.GroupViewModel
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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileEditTest {

    private val testDispatcher = StandardTestDispatcher()

    private val owner = SpendlyUser(
        uid = "uid_owner_01",
        fullName = "Nguyễn Văn A",
        email = "owner@spendly.vn",
        phone = "0901112223"
    )

    private val otherOwner = SpendlyUser(
        uid = "uid_other_02",
        fullName = "Trần Thị B",
        email = "other@spendly.vn",
        phone = "0909888777"
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
    fun `updateUserProfile updates owner users record and propagates displayName initials phone to all joined groups`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        repo.ensureUserProfile(owner.uid, owner.fullName, owner.email, owner.phone).getOrThrow()
        repo.ensureUserProfile(otherOwner.uid, otherOwner.fullName, otherOwner.email, otherOwner.phone).getOrThrow()

        val ownedGroup = repo.createGroup(owner, "Quỹ Nhà Trọ").getOrThrow()
        val secondGroup = repo.createGroup(otherOwner, "Quỹ Du Lịch").getOrThrow()
        repo.joinGroupByInviteCode(owner, secondGroup.inviteCode).getOrThrow()

        val updateResult = repo.updateUserProfile(
            uid = owner.uid,
            fullName = "Nguyễn Minh An",
            phone = "0988777666",
            avatarUrl = "/data/user/0/app/files/avatars/an.jpg"
        )
        assertTrue(updateResult.isSuccess)

        val savedProfile = repo.observeUserProfile(owner.uid).first()
        assertNotNull(savedProfile)
        assertEquals("Nguyễn Minh An", savedProfile?.fullName)
        assertEquals("0988777666", savedProfile?.phone)
        assertEquals("/data/user/0/app/files/avatars/an.jpg", savedProfile?.avatarUrl)
        assertEquals(owner.email, savedProfile?.email)

        val ownedGroupMember = repo.observeGroupMembers(ownedGroup.groupId)
            .first()
            .first { it.userId == owner.uid }
        assertEquals("Nguyễn Minh An", ownedGroupMember.displayName)
        assertEquals("MA", ownedGroupMember.initials)
        assertEquals("0988777666", ownedGroupMember.phone)

        val secondGroupMembers = repo.observeGroupMembers(secondGroup.groupId).first()
        val joinedOwnerMember = secondGroupMembers.first { it.userId == owner.uid }
        assertEquals("Nguyễn Minh An", joinedOwnerMember.displayName)
        assertEquals("MA", joinedOwnerMember.initials)
        assertEquals("0988777666", joinedOwnerMember.phone)

        // Thành viên khác trong nhóm không bị ảnh hưởng
        val untouchedOtherMember = secondGroupMembers.first { it.userId == otherOwner.uid }
        assertEquals("Trần Thị B", untouchedOtherMember.displayName)
        assertEquals("0909888777", untouchedOtherMember.phone)
    }

    @Test
    fun `updateUserProfile creates user record safely when profile document does not exist yet`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)

        val result = repo.updateUserProfile(
            uid = "uid_brand_new",
            fullName = "Trần Thu Hà",
            phone = "0901122334",
            avatarUrl = ""
        )
        assertTrue(result.isSuccess)

        val created = repo.observeUserProfile("uid_brand_new").first()
        assertNotNull(created)
        assertEquals("uid_brand_new", created?.uid)
        assertEquals("Trần Thu Hà", created?.fullName)
        assertEquals("0901122334", created?.phone)
        assertTrue(created?.isActive == true)
    }

    @Test
    fun `GroupViewModel updateCurrentUserProfile updates currentUser immediately on valid input`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val viewModel = GroupViewModel(
            userRepository = repo,
            groupRepository = repo,
            transactionRepository = repo,
            autoBindDefaultUser = false
        )

        viewModel.bindUser(owner)
        advanceUntilIdle()

        var callbackUser: SpendlyUser? = null
        var callbackError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "Phạm Hải Đăng",
            phone = "0912 345 678",
            avatarUrl = "/local/avatar.jpg",
            onSuccess = { callbackUser = it },
            onError = { callbackError = it }
        )
        advanceUntilIdle()

        assertNull(callbackError)
        assertNotNull(callbackUser)
        assertEquals("Phạm Hải Đăng", viewModel.currentUser.value.fullName)
        assertEquals("0912 345 678", viewModel.currentUser.value.phone)
        assertEquals("/local/avatar.jpg", viewModel.currentUser.value.avatarUrl)
        assertEquals(owner.email, viewModel.currentUser.value.email)

        val persisted = repo.observeUserProfile(owner.uid).first()
        assertEquals("Phạm Hải Đăng", persisted?.fullName)
        assertEquals("0912 345 678", persisted?.phone)
        assertEquals("/local/avatar.jpg", persisted?.avatarUrl)
    }

    @Test
    fun `GroupViewModel updateCurrentUserProfile rejects blank name invalid phone and unauthenticated user`() = runTest(testDispatcher) {
        val repo = FirestoreSpendlyRepository(firestore = null)
        val viewModel = GroupViewModel(
            userRepository = repo,
            groupRepository = repo,
            transactionRepository = repo,
            autoBindDefaultUser = false
        )

        // 1. Chưa đăng nhập (uid trống)
        var unauthSuccess = false
        var unauthError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "Nguyễn Minh An",
            phone = "0912345678",
            onSuccess = { unauthSuccess = true },
            onError = { unauthError = it }
        )
        advanceUntilIdle()
        assertFalse(unauthSuccess)
        assertNotNull(unauthError)

        // Gắn tài khoản hợp lệ
        viewModel.bindUser(owner)
        advanceUntilIdle()

        // 2. Họ tên trống hoặc quá ngắn
        var blankNameError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "   ",
            phone = "0912345678",
            onError = { blankNameError = it }
        )
        assertNotNull(blankNameError)

        var shortNameError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "A",
            phone = "0912345678",
            onError = { shortNameError = it }
        )
        assertNotNull(shortNameError)

        // 3. Số điện thoại sai định dạng
        var badPhoneError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "Nguyễn Văn A",
            phone = "12ab3",
            onError = { badPhoneError = it }
        )
        assertNotNull(badPhoneError)

        var tooShortPhoneError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "Nguyễn Văn A",
            phone = "12345",
            onError = { tooShortPhoneError = it }
        )
        assertNotNull(tooShortPhoneError)
        assertEquals("Nguyễn Văn A", viewModel.currentUser.value.fullName)
        assertEquals("0901112223", viewModel.currentUser.value.phone)

        // 4. Số điện thoại bỏ trống (tùy chọn) vẫn lưu hợp lệ
        var optionalPhoneSuccess = false
        var optionalPhoneError: String? = null
        viewModel.updateCurrentUserProfile(
            fullName = "Nguyễn Văn An",
            phone = "",
            onSuccess = { optionalPhoneSuccess = true },
            onError = { optionalPhoneError = it }
        )
        advanceUntilIdle()
        assertTrue(optionalPhoneSuccess)
        assertNull(optionalPhoneError)
        assertEquals("Nguyễn Văn An", viewModel.currentUser.value.fullName)
        assertEquals("", viewModel.currentUser.value.phone)
    }
}
