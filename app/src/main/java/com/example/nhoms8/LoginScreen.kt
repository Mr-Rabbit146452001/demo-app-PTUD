package com.example.nhoms8

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * 05 · Màn hình Đăng nhập (Login Screen)
 * Ứng dụng Spendly - Quản lý chi tiêu cá nhân & nhóm
 */
@Composable
fun LoginScreen(
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onLoginSuccess: () -> Unit = {},
    onLoginSubmit: (emailOrPhone: String, password: String) -> Unit = { _, _ -> },
    onRegisterClicked: () -> Unit = {},
    onForgotPasswordClicked: (emailOrPhone: String) -> Unit = {},
    onGoogleLoginClicked: () -> Unit = {},
    onAppleLoginClicked: () -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var emailOrPhoneInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(true) }

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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 1. BRAND HEADER
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.spendly_app_logo),
                        contentDescription = "Spendly Logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(52.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Spendly",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Chi tiêu cá nhân & quỹ nhóm",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                }

                // 2. HEADLINE
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Chào mừng trở lại",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Đăng nhập để tiếp tục theo dõi số dư, chia hóa đơn và đồng bộ quỹ nhóm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted
                    )
                }

                // 3. FORM GLASS CARD
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = emailOrPhoneInput,
                            onValueChange = { emailOrPhoneInput = it },
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
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Mật khẩu") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = colors.primaryLavender
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) {
                                            Icons.Rounded.VisibilityOff
                                        } else {
                                            Icons.Rounded.Visibility
                                        },
                                        contentDescription = if (isPasswordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu",
                                        tint = colors.textMuted
                                    )
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
                                imeAction = ImeAction.Done
                            ),
                            shape = SpendlyShapeTokens.CardMedium,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { rememberMe = !rememberMe }
                                    .padding(vertical = 4.dp, horizontal = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (rememberMe) colors.deepLavender else Color.Transparent)
                                        .border(
                                            width = 1.25.dp,
                                            color = if (rememberMe) colors.deepLavender else colors.divider,
                                            shape = RoundedCornerShape(5.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rememberMe) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Ghi nhớ đăng nhập",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.textMuted
                                )
                            }

                            Text(
                                text = "Quên mật khẩu?",
                                style = MaterialTheme.typography.labelLarge,
                                color = colors.deepLavender,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(role = Role.Button) {
                                        onForgotPasswordClicked(emailOrPhoneInput)
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }

                        if (!errorMessage.isNullOrBlank()) {
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
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = colors.coralDeep
                                )
                            }
                        }

                        SpendlyButton(
                            text = if (isLoading) "Đang xác thực Firebase..." else "Đăng nhập",
                            onClick = {
                                if (!isLoading) {
                                    onLoginSubmit(emailOrPhoneInput, passwordInput)
                                    onLoginSuccess()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isLoading = isLoading,
                            trailingIcon = if (isLoading) null else Icons.AutoMirrored.Rounded.ArrowForward,
                            height = 52.dp
                        )
                    }
                }

                // 4. SOCIAL LOGIN DIVIDER & BUTTONS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = colors.divider,
                        thickness = 1.dp
                    )
                    Text(
                        text = "hoặc tiếp tục với",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = colors.divider,
                        thickness = 1.dp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SocialGlassButton(
                        label = "Google",
                        onClick = onGoogleLoginClicked,
                        hazeState = hazeState,
                        leadingContent = { GoogleIcon(modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    SocialGlassButton(
                        label = "Apple",
                        onClick = onAppleLoginClicked,
                        hazeState = hazeState,
                        leadingContent = {
                            AppleIcon(
                                color = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 5. FOOTER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted
                )
                Text(
                    text = "Đăng ký ngay",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = colors.deepLavender,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Button, onClick = onRegisterClicked)
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun SocialGlassButton(
    label: String,
    onClick: () -> Unit,
    hazeState: dev.chrisbanes.haze.HazeState,
    leadingContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    GlassCard(
        modifier = modifier
            .height(50.dp)
            .clip(SpendlyShapeTokens.Pill)
            .clickable(role = Role.Button, onClick = onClick),
        hazeState = hazeState,
        shape = SpendlyShapeTokens.Pill,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            leadingContent()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.44f
        val stroke = 2.8.dp.toPx()

        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = 315f,
            sweepAngle = 60f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        drawLine(
            color = Color(0xFF4285F4),
            start = Offset(cx, cy),
            end = Offset(cx + r, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun AppleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.56f

        val appleLeaf = Path().apply {
            moveTo(cx, cy - 7.dp.toPx())
            cubicTo(cx + 1.dp.toPx(), cy - 10.dp.toPx(), cx + 4.dp.toPx(), cy - 10.dp.toPx(), cx + 4.dp.toPx(), cy - 8.dp.toPx())
            cubicTo(cx + 3.dp.toPx(), cy - 6.dp.toPx(), cx + 1.dp.toPx(), cy - 6.dp.toPx(), cx, cy - 7.dp.toPx())
            close()
        }
        drawPath(appleLeaf, color = color)

        val appleBody = Path().apply {
            moveTo(cx, cy - 6.dp.toPx())
            cubicTo(cx + 5.5.dp.toPx(), cy - 6.dp.toPx(), cx + 7.5.dp.toPx(), cy - 1.5.dp.toPx(), cx + 7.5.dp.toPx(), cy + 3.dp.toPx())
            cubicTo(cx + 7.5.dp.toPx(), cy + 7.5.dp.toPx(), cx + 3.5.dp.toPx(), cy + 8.5.dp.toPx(), cx, cy + 8.5.dp.toPx())
            cubicTo(cx - 3.5.dp.toPx(), cy + 8.5.dp.toPx(), cx - 7.5.dp.toPx(), cy + 7.5.dp.toPx(), cx - 7.5.dp.toPx(), cy + 3.dp.toPx())
            cubicTo(cx - 7.5.dp.toPx(), cy - 1.5.dp.toPx(), cx - 5.5.dp.toPx(), cy - 6.dp.toPx(), cx, cy - 6.dp.toPx())
            close()
        }
        drawPath(appleBody, color = color)
    }
}

@Preview(
    name = "05 · Đăng nhập Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        LoginScreen()
    }
}

@Preview(
    name = "05 · Đăng nhập Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        LoginScreen(errorMessage = "Sai mật khẩu, vui lòng kiểm tra lại.")
    }
}
