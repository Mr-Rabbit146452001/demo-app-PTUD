package com.example.nhoms8.data

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Kết quả trả về của các thao tác xác thực Firebase
 */
sealed class AuthResult {
    data class Success(val user: FirebaseUser, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

/**
 * Kho xử lý Xác thực Firebase Authentication (FirebaseAuthRepository)
 * Phụ trách bởi Kỹ sư Dữ liệu & Trưởng nhóm (Lê Văn Hùng) - Nhóm 08
 */
class FirebaseAuthRepository(
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (_: Throwable) {
        null
    }
) {

    /**
     * Chuẩn hóa email: Nếu người dùng nhập số điện thoại hoặc username,
     * tự động chuyển đổi thành email hợp lệ theo định dạng domain SpendTogether.
     */
    private fun normalizeEmail(input: String): String {
        val trimmed = input.trim().replace(" ", "")
        return if (trimmed.contains("@")) {
            trimmed
        } else {
            // Chuyển số điện thoại thành email nội bộ SpendTogether
            "$trimmed@spendtogether.com"
        }
    }

    /**
     * Lấy người dùng Firebase hiện tại đã đăng nhập
     */
    fun getCurrentUser(): FirebaseUser? = auth?.currentUser

    /**
     * Kiểm tra trạng thái đã đăng nhập
     */
    fun isUserLoggedIn(): Boolean = auth?.currentUser != null

    /**
     * Đăng ký tài khoản mới bằng Email/Mật khẩu
     */
    suspend fun signUp(
        emailOrPhone: String,
        password: String,
        displayName: String
    ): AuthResult = suspendCancellableCoroutine { continuation ->
        val currentAuth = auth ?: run {
            continuation.resume(AuthResult.Error("Firebase Auth chưa khởi tạo trong môi trường Preview"))
            return@suspendCancellableCoroutine
        }

        val email = normalizeEmail(emailOrPhone)

        if (password.length < 6) {
            continuation.resume(AuthResult.Error("Mật khẩu phải có tối thiểu 6 ký tự"))
            return@suspendCancellableCoroutine
        }

        currentAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    // Cập nhật họ tên hiển thị cho User Profile
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()

                    user.updateProfile(profileUpdate).addOnCompleteListener {
                        continuation.resume(
                            AuthResult.Success(user, "Đăng ký tài khoản Firebase thành công!")
                        )
                    }
                } else {
                    continuation.resume(AuthResult.Error("Không tìm thấy thông tin tài khoản vừa tạo"))
                }
            }
            .addOnFailureListener { exception ->
                val friendlyMessage = mapFirebaseException(exception)
                continuation.resume(AuthResult.Error(friendlyMessage))
            }
    }

    /**
     * Đăng nhập bằng Email/Mật khẩu
     */
    suspend fun signIn(
        emailOrPhone: String,
        password: String
    ): AuthResult = suspendCancellableCoroutine { continuation ->
        val currentAuth = auth ?: run {
            continuation.resume(AuthResult.Error("Firebase Auth chưa khởi tạo trong môi trường Preview"))
            return@suspendCancellableCoroutine
        }

        val email = normalizeEmail(emailOrPhone)

        if (email.isBlank() || password.isBlank()) {
            continuation.resume(AuthResult.Error("Vui lòng nhập đầy đủ tài khoản và mật khẩu"))
            return@suspendCancellableCoroutine
        }

        currentAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    continuation.resume(
                        AuthResult.Success(user, "Đăng nhập thành công qua Firebase!")
                    )
                } else {
                    continuation.resume(AuthResult.Error("Không tìm thấy phiên đăng nhập"))
                }
            }
            .addOnFailureListener { exception ->
                val friendlyMessage = mapFirebaseException(exception)
                continuation.resume(AuthResult.Error(friendlyMessage))
            }
    }

    /**
     * Quên mật khẩu: Gửi email khôi phục mật khẩu
     */
    suspend fun sendPasswordReset(emailOrPhone: String): AuthResult =
        suspendCancellableCoroutine { continuation ->
            val currentAuth = auth ?: run {
                continuation.resume(AuthResult.Error("Firebase Auth chưa khởi tạo trong môi trường Preview"))
                return@suspendCancellableCoroutine
            }

            val email = normalizeEmail(emailOrPhone)
            currentAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    continuation.resume(
                        AuthResult.Success(
                            currentAuth.currentUser ?: userMock(),
                            "Đã gửi liên kết khôi phục mật khẩu đến $email"
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    val friendlyMessage = mapFirebaseException(exception)
                    continuation.resume(AuthResult.Error(friendlyMessage))
                }
        }

    /**
     * Đăng xuất khỏi Firebase
     */
    fun signOut() {
        auth?.signOut()
    }

    /**
     * Chuyển đổi mã lỗi Firebase thành thông báo tiếng Việt dễ hiểu
     */
    private fun mapFirebaseException(e: Exception): String {
        return when (e) {
            is FirebaseAuthWeakPasswordException ->
                "Mật khẩu quá yếu! Vui lòng chọn mật khẩu từ 6 ký tự trở lên."
            is FirebaseAuthInvalidCredentialsException ->
                "Thông tin đăng nhập không chính xác hoặc email sai định dạng."
            is FirebaseAuthUserCollisionException ->
                "Tài khoản email này đã được đăng ký trước đó. Vui lòng đăng nhập."
            is FirebaseAuthInvalidUserException ->
                "Tài khoản không tồn tại hoặc đã bị khóa."
            is FirebaseNetworkException ->
                "Không có kết nối mạng. Vui lòng kiểm tra Wi-Fi / 4G của bạn."
            else ->
                e.localizedMessage ?: "Đã xảy ra lỗi trong quá trình xác thực."
        }
    }

    private fun userMock(): FirebaseUser {
        return auth?.currentUser ?: throw IllegalStateException("No user")
    }
}
