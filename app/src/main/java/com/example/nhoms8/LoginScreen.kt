package com.example.nhoms8

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 05 · Màn hình Đăng nhập (Login Screen)
 * Ứng dụng SpendTogether - Quản lý chi tiêu cá nhân & nhóm
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
    // Trạng thái Form đăng nhập
    var phoneInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(true) }

    // Bảng màu thiết kế ấm áp & chuẩn phong cách
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp (Beige)
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm sang trọng
    val sageGreen = Color(0xFF4A725E)            // Xanh sage điểm nhấn
    val textMuted = Color(0xFF6B7E74)            // Màu xám trung tính

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Họa tiết nền cong mềm mại ở góc trên phải & dưới trái
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Mảng cong góc trên bên phải
            drawCircle(
                color = ambientSage,
                radius = w * 0.44f,
                center = Offset(w * 0.96f, h * 0.05f)
            )

            // Mảng cong lớn góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.58f,
                center = Offset(w * 0.04f, h * 0.88f)
            )
        }

        // 2. Toàn bộ nội dung màn hình Đăng nhập
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(12.dp))

                // ====================== 1. BRAND HEADER (LOGO & TÊN APP) ======================
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Squircle App Icon
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(14.dp),
                                spotColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .background(
                                color = forestGreenDark,
                                shape = RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        SproutMiniIcon(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "SpendTogether",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark,
                        letterSpacing = (-0.3).sp
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // ====================== 2. TIÊU ĐỀ & PHỤ ĐỀ ======================

                Spacer(modifier = Modifier.height(8.dp))



                Spacer(modifier = Modifier.height(24.dp))

                // ====================== 3. BIỂU MẪU ĐĂNG NHẬP (FORM) ======================
                // --- Trường Số điện thoại ---
                Text(
                    text = "Số điện thoại",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF42564C)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PhoneOutlineIcon(
                            color = Color(0xFF7D9488),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        BasicTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = forestGreenDark,
                                fontWeight = FontWeight.Normal
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            cursorBrush = SolidColor(forestGreenDark),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- Trường Mật khẩu ---
                Text(
                    text = "Mật khẩu",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF42564C)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .background(Color.White, CircleShape)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LockOutlineIcon(
                            color = Color(0xFF7D9488),
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        BasicTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                color = forestGreenDark,
                                fontWeight = FontWeight.Normal
                            ),
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            cursorBrush = SolidColor(forestGreenDark),
                            modifier = Modifier.weight(1f)
                        )

                        // Nút Hiện / Ẩn mật khẩu
                        Text(
                            text = if (isPasswordVisible) "Ẩn" else "Hiện",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = sageGreen,
                            modifier = Modifier
                                .clickable { isPasswordVisible = !isPasswordVisible }
                                .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // --- Tùy chọn Ghi nhớ & Quên mật khẩu ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkbox Ghi nhớ đăng nhập
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { rememberMe = !rememberMe }
                            .padding(vertical = 4.dp)
                    ) {

                        Spacer(modifier = Modifier.width(6.dp))

                    }

                    // Nút Quên mật khẩu
                    Text(
                        text = "Quên mật khẩu?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = sageGreen,
                        modifier = Modifier
                            .clickable { onForgotPasswordClicked(phoneInput) }
                            .padding(vertical = 4.dp)
                    )
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFDE8E8), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            fontSize = 13.sp,
                            color = Color(0xFFC81E1E),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ====================== 4. NÚT ĐĂNG NHẬP CHÍNH ======================
                Button(
                    onClick = {
                        if (!isLoading) {
                            onLoginSubmit(phoneInput, passwordInput)
                            onLoginSuccess()
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(
                            elevation = 6.dp,
                            shape = CircleShape,
                            spotColor = forestGreenDark.copy(alpha = 0.35f)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = forestGreenDark,
                        contentColor = Color.White,
                        disabledContainerColor = forestGreenDark.copy(alpha = 0.6f),
                        disabledContentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    shape = CircleShape
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            Text(
                                text = "Đang xác thực Firebase...",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        } else {
                            RightArrowIcon(
                                color = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Đăng nhập",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ====================== 5. PHÂN CÁCH HOẶC TIẾP TỤC VỚI ======================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDCE4DE),
                        thickness = 1.dp
                    )
                    Text(
                        text = "hoặc tiếp tục với",
                        fontSize = 12.5.sp,
                        color = Color(0xFF8B9B92),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f),
                        color = Color(0xFFDCE4DE),
                        thickness = 1.dp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // ====================== 6. NÚT MẠNG XÃ HỘI (GOOGLE & APPLE) ======================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Nút Google
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.06f),
                                spotColor = Color.Black.copy(alpha = 0.08f)
                            )
                            .background(Color.White, CircleShape)
                            .clickable { onGoogleLoginClicked() },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            GoogleIcon(modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Google",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                }
            }

            // ====================== 7. CHÂN TRANG: ĐĂNG KÝ NGAY ======================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Chưa có tài khoản? ",
                    fontSize = 14.sp,
                    color = textMuted
                )
                Text(
                    text = "Đăng ký ngay",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark,
                    modifier = Modifier
                        .clickable { onRegisterClicked() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Biểu tượng mầm cây mini màu trắng sắc nét
 */
@Composable
private fun SproutMiniIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Đường gạch đáy mầm
        drawLine(
            color = color,
            start = Offset(w * 0.30f, h * 0.82f),
            end = Offset(w * 0.70f, h * 0.82f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Thân cây
        val stem = Path().apply {
            moveTo(w * 0.50f, h * 0.82f)
            cubicTo(w * 0.50f, h * 0.55f, w * 0.49f, h * 0.42f, w * 0.49f, h * 0.28f)
        }
        drawPath(stem, color = color, style = stroke)

        // Lá trái
        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            cubicTo(w * 0.28f, h * 0.52f, w * 0.26f, h * 0.38f, w * 0.46f, h * 0.38f)
            cubicTo(w * 0.50f, h * 0.44f, w * 0.50f, h * 0.50f, w * 0.50f, h * 0.55f)
        }
        drawPath(leftLeaf, color = color, style = stroke)

        // Lá phải
        val rightLeaf = Path().apply {
            moveTo(w * 0.49f, h * 0.38f)
            cubicTo(w * 0.58f, h * 0.35f, w * 0.74f, h * 0.20f, w * 0.64f, h * 0.16f)
            cubicTo(w * 0.52f, h * 0.14f, w * 0.46f, h * 0.26f, w * 0.49f, h * 0.38f)
        }
        drawPath(rightLeaf, color = color, style = stroke)
    }
}

/**
 * Biểu tượng ống nghe điện thoại dạng outline
 */
@Composable
private fun PhoneOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        val phonePath = Path().apply {
            moveTo(w * 0.28f, h * 0.22f)
            cubicTo(w * 0.22f, h * 0.28f, w * 0.20f, h * 0.36f, w * 0.28f, h * 0.50f)
            cubicTo(w * 0.38f, h * 0.68f, w * 0.54f, h * 0.82f, w * 0.70f, h * 0.86f)
            cubicTo(w * 0.78f, h * 0.88f, w * 0.86f, h * 0.82f, w * 0.88f, h * 0.74f)
            lineTo(w * 0.78f, h * 0.60f)
            cubicTo(w * 0.74f, h * 0.56f, w * 0.68f, h * 0.58f, w * 0.64f, h * 0.62f)
            lineTo(w * 0.60f, h * 0.66f)
            cubicTo(w * 0.48f, h * 0.60f, w * 0.40f, h * 0.52f, w * 0.34f, h * 0.40f)
            lineTo(w * 0.38f, h * 0.36f)
            cubicTo(w * 0.42f, h * 0.32f, w * 0.44f, h * 0.26f, w * 0.40f, h * 0.22f)
            close()
        }
        drawPath(phonePath, color = color, style = stroke)
    }
}

/**
 * Biểu tượng ổ khóa dạng outline
 */
@Composable
private fun LockOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Quai khóa
        val shackle = Path().apply {
            moveTo(w * 0.32f, h * 0.48f)
            lineTo(w * 0.32f, h * 0.32f)
            cubicTo(w * 0.32f, h * 0.16f, w * 0.68f, h * 0.16f, w * 0.68f, h * 0.32f)
            lineTo(w * 0.68f, h * 0.48f)
        }
        drawPath(shackle, color = color, style = stroke)

        // Thân khóa
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.22f, h * 0.46f),
            size = Size(w * 0.56f, h * 0.44f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = stroke
        )

        // Lỗ khóa nhỏ
        drawCircle(
            color = color,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.50f, h * 0.64f)
        )
        drawLine(
            color = color,
            start = Offset(w * 0.50f, h * 0.66f),
            end = Offset(w * 0.50f, h * 0.74f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

/**
 * Logo Google nhiều màu chuẩn xác
 */
@Composable
private fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.44f
        val stroke = 3.dp.toPx()

        // Đường vòng cung Google (Đỏ, Vàng, Xanh lục, Xanh lam)
        // Cung xanh lục (dưới)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Cung vàng (trái dưới)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 135f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Cung đỏ (trên)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 225f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
        // Cung & thanh ngang xanh lam (phải)
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

/**
 * Logo Apple quả táo cắn dở thanh lịch
 */
@Composable
private fun AppleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.50f
        val cy = h * 0.55f

        // Lá quả táo


        // Thân quả táo
        val appleBody = Path().apply {
            moveTo(cx, cy - 7.dp.toPx())
            cubicTo(cx + 6.dp.toPx(), cy - 7.dp.toPx(), cx + 8.dp.toPx(), cy - 2.dp.toPx(), cx + 8.dp.toPx(), cy + 3.dp.toPx())
            cubicTo(cx + 8.dp.toPx(), cy + 8.dp.toPx(), cx + 4.dp.toPx(), cy + 9.dp.toPx(), cx, cy + 9.dp.toPx())
            cubicTo(cx - 4.dp.toPx(), cy + 9.dp.toPx(), cx - 8.dp.toPx(), cy + 8.dp.toPx(), cx - 8.dp.toPx(), cy + 3.dp.toPx())
            cubicTo(cx - 8.dp.toPx(), cy - 2.dp.toPx(), cx - 6.dp.toPx(), cy - 7.dp.toPx(), cx, cy - 7.dp.toPx())
            close()
        }
        drawPath(appleBody, color = color)
    }
}

/**
 * Biểu tượng mũi tên chỉ sang phải
 */
@Composable
private fun RightArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.50f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.55f, h * 0.22f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.55f, h * 0.78f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "05 · Đăng nhập (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun LoginScreenPreview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LoginScreen()
        }
    }
}
