package com.example.nhoms8

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * 06 · Màn hình Đăng ký tài khoản (Sign Up Screen)
 * Ứng dụng Spendly - Quản lý chi tiêu cá nhân & nhóm
 */
@Composable
fun SignUpScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onBackClicked: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onSignUpSubmit: (name: String, emailOrPhone: String, password: String) -> Unit = { _, _, _ -> },
    onLoginClicked: () -> Unit = {},
    onPickAvatarClicked: () -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var fullName by rememberSaveable { mutableStateOf("") }
    var emailOrPhone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmVisible by rememberSaveable { mutableStateOf(false) }
    var agreeToTerms by rememberSaveable { mutableStateOf(true) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val activeError = errorMessage ?: localError

    val passwordStrengthLabel = when {
        password.isEmpty() -> null
        password.length >= 8 -> "Mạnh" to colors.mintDeep
        password.length >= 6 -> "Vừa" to colors.amberDeep
        else -> "Yếu" to colors.coralDeep
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = colors.primaryLavender,
        unfocusedBorderColor = colors.divider,
        focusedLabelColor = colors.deepLavender,
        unfocusedLabelColor = colors.textMuted,
        cursorColor = colors.deepLavender,
        focusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.45f else 0.72f),
        unfocusedContainerColor = colors.surface.copy(alpha = if (colors.isDark) 0.30f else 0.55f),
        focusedTextColor = colors.textPrimary,
        unfocusedTextColor = colors.textPrimary
    )

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. TOP BAR
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .spendlyGlassSurface(
                            hazeState = hazeState,
                            shape = CircleShape,
                            tintColor = colors.glassContainer
                        )
                        .clickable(role = Role.Button, onClick = onBackClicked),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.spendly_app_logo),
                        contentDescription = "Spendly Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Spendly",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = colors.onLavenderContainer
                    )
                }
            }

            // 2. TITLE & SUBTITLE
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Tạo tài khoản mới",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary
                )
                Text(
                    text = "Bắt đầu quản lý chi tiêu cá nhân và quỹ chung minh bạch cùng Spendly.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
            }

            // 3. AVATAR PICKER GLASS CARD
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                contentPadding = PaddingValues(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(colors.lavenderContainer)
                                .border(1.5.dp, colors.primaryLavender.copy(alpha = 0.45f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = colors.onLavenderContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Thêm ảnh đại diện",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "JPG hoặc PNG, tối đa 5 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textMuted
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(colors.lavenderContainer)
                            .clickable(role = Role.Button, onClick = onPickAvatarClicked),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CameraAlt,
                            contentDescription = "Chọn ảnh",
                            tint = colors.onLavenderContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // 4. REGISTRATION FORM GLASS CARD
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                hazeState = hazeState,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Họ và tên") },
                        placeholder = { Text("vd: Lê Văn Hùng") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emailOrPhone,
                        onValueChange = { emailOrPhone = it },
                        label = { Text("Email hoặc số điện thoại") },
                        placeholder = { Text("vd: hung.le@spendly.vn") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.AlternateEmail,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Mật khẩu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        trailingIcon = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                if (passwordStrengthLabel != null) {
                                    Text(
                                        text = passwordStrengthLabel.first,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = passwordStrengthLabel.second
                                    )
                                }
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = "Hiện hoặc ẩn mật khẩu",
                                        tint = colors.textMuted
                                    )
                                }
                            }
                        },
                        visualTransformation = if (isPasswordVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Nhập lại mật khẩu") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.VerifiedUser,
                                contentDescription = null,
                                tint = colors.primaryLavender
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isConfirmVisible = !isConfirmVisible }) {
                                Icon(
                                    imageVector = if (isConfirmVisible) {
                                        Icons.Rounded.VisibilityOff
                                    } else {
                                        Icons.Rounded.Visibility
                                    },
                                    contentDescription = "Hiện hoặc ẩn mật khẩu xác nhận",
                                    tint = colors.textMuted
                                )
                            }
                        },
                        visualTransformation = if (isConfirmVisible) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = SpendlyShapeTokens.CardMedium,
                        colors = fieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { agreeToTerms = !agreeToTerms }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (agreeToTerms) colors.deepLavender else Color.Transparent)
                                .border(
                                    width = 1.25.dp,
                                    color = if (agreeToTerms) colors.deepLavender else colors.divider,
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (agreeToTerms) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }

                    if (!activeError.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SpendlyShapeTokens.CardSmall)
                                .background(colors.coralContainer)
                                .border(
                                    width = 1.dp,
                                    color = colors.coralDeep.copy(alpha = 0.35f),
                                    shape = SpendlyShapeTokens.CardSmall
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = colors.coralDeep,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = activeError,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = colors.coralDeep
                            )
                        }
                    }

                    SpendlyButton(
                        text = if (isLoading) "Đang tạo tài khoản Firebase..." else "Tạo tài khoản",
                        onClick = {
                            if (password != confirmPassword) {
                                localError = "Mật khẩu xác nhận không khớp!"
                                return@SpendlyButton
                            }
                            if (!agreeToTerms) {
                                localError = "Vui lòng đồng ý với Điều khoản dịch vụ"
                                return@SpendlyButton
                            }
                            localError = null
                            onSignUpSubmit(fullName, emailOrPhone, password)
                            onSignUpSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isLoading = isLoading,
                        leadingIcon = if (isLoading) null else Icons.Rounded.AutoAwesome,
                        height = 52.dp
                    )
                }
            }

            // 5. FOOTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
                Text(
                    text = "Đăng nhập",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.deepLavender,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onLoginClicked)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Preview(
    name = "06 · Đăng ký Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SignUpScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        SignUpScreen()
    }
}

@Preview(
    name = "06 · Đăng ký Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SignUpScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        SignUpScreen()
    }
}
