package com.example.nhoms8.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.MarkEmailRead
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.SpendlyButtonStyle
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * Màn hình Quên mật khẩu phong cách Lavender / Organic Glassmorphism.
 */
@Composable
fun ForgotPasswordScreen(
    initialEmailOrPhone: String = "",
    isLoading: Boolean = false,
    statusMessage: String? = null,
    isError: Boolean = false,
    onSendResetClicked: (emailOrPhone: String) -> Unit = {},
    onBackToLoginClicked: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    var emailOrPhone by rememberSaveable(initialEmailOrPhone) { mutableStateOf(initialEmailOrPhone) }
    var localError by rememberSaveable { mutableStateOf<String?>(null) }

    val activeMessage = localError ?: statusMessage
    val activeIsError = localError != null || isError

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
        modifier = modifier.fillMaxSize(),
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
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(colors.glassContainer.copy(alpha = 0.85f))
                            .border(1.dp, colors.divider, CircleShape)
                            .clickable(onClick = onBackToLoginClicked),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại đăng nhập",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(SpendlyShapeTokens.Pill)
                            .background(colors.lavenderContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Spendly · Khôi phục tài khoản",
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.onLavenderContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(SpendlyShapeTokens.CardLarge)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockReset,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Quên mật khẩu?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = colors.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Nhập email hoặc số điện thoại bạn đã đăng ký. Hệ thống sẽ gửi liên kết đặt lại mật khẩu ngay lập tức.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textMuted,
                        textAlign = TextAlign.Center
                    )
                }

                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    hazeState = hazeState
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        OutlinedTextField(
                            value = emailOrPhone,
                            onValueChange = {
                                emailOrPhone = it
                                localError = null
                            },
                            label = { Text("Email hoặc số điện thoại") },
                            placeholder = { Text("VD: hung.le@spendly.vn hoặc 0912345678") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.AlternateEmail,
                                    contentDescription = null,
                                    tint = colors.deepLavender
                                )
                            },
                            trailingIcon = {
                                if (emailOrPhone.isNotEmpty()) {
                                    IconButton(onClick = {
                                        emailOrPhone = ""
                                        localError = null
                                    }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Xóa nội dung",
                                            tint = colors.textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Done
                            ),
                            shape = SpendlyShapeTokens.CardSmall,
                            colors = fieldColors,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "Lưu ý: Với số điện thoại nội bộ, liên kết sẽ được gửi tới hộp thư liên kết trên Firebase Auth.",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textMuted
                        )

                        if (!activeMessage.isNullOrBlank()) {
                            val bannerBg = if (activeIsError) colors.coralContainer else colors.mintContainer
                            val bannerFg = if (activeIsError) colors.coralDeep else colors.mintDeep
                            val bannerIcon = if (activeIsError) {
                                Icons.Rounded.ErrorOutline
                            } else {
                                Icons.Rounded.CheckCircleOutline
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(SpendlyShapeTokens.CardSmall)
                                    .background(bannerBg)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = bannerIcon,
                                    contentDescription = null,
                                    tint = bannerFg,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = activeMessage,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = bannerFg,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        SpendlyButton(
                            text = "Gửi liên kết khôi phục",
                            onClick = {
                                val trimmed = emailOrPhone.trim()
                                val validationError = validateEmailOrPhone(trimmed)
                                if (validationError != null) {
                                    localError = validationError
                                } else {
                                    localError = null
                                    onSendResetClicked(trimmed)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            isLoading = isLoading,
                            leadingIcon = Icons.Rounded.MarkEmailRead
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpendlyButton(
                    text = "Quay lại đăng nhập",
                    onClick = onBackToLoginClicked,
                    modifier = Modifier.fillMaxWidth(),
                    style = SpendlyButtonStyle.SECONDARY_GLASS,
                    hazeState = hazeState,
                    leadingIcon = Icons.AutoMirrored.Rounded.ArrowBack
                )
            }
        }
    }
}

private fun validateEmailOrPhone(value: String): String? {
    if (value.isBlank()) {
        return "Vui lòng nhập email hoặc số điện thoại đã đăng ký"
    }
    if (value.contains("@")) {
        val parts = value.split("@")
        if (parts.size != 2 || parts[0].isBlank() || !parts[1].contains(".") || value.contains(" ")) {
            return "Địa chỉ email không đúng định dạng"
        }
        return null
    }
    val digits = value.filter { it.isDigit() }
    if (digits.length < 9) {
        return "Số điện thoại phải có ít nhất 9 chữ số hoặc nhập đầy đủ email"
    }
    return null
}

@Preview(showBackground = true, widthDp = 412, heightDp = 780)
@Composable
private fun ForgotPasswordScreenPreview() {
    SpendlyTheme {
        ForgotPasswordScreen(
            initialEmailOrPhone = "hung.le@spendly.vn",
            statusMessage = "Đã gửi liên kết khôi phục mật khẩu đến hung.le@spendly.vn",
            isError = false
        )
    }
}
