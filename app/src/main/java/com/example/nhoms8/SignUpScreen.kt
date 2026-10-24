package com.example.nhoms8

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 06 · Màn hình Đăng ký tài khoản (Sign Up Screen)
 * Ứng dụng SpendTogether - Quản lý chi tiêu cá nhân & nhóm
 */
@Composable
fun SignUpScreen(
    onBackClicked: () -> Unit = {},
    onSignUpSuccess: () -> Unit = {},
    onLoginClicked: () -> Unit = {},
    onPickAvatarClicked: () -> Unit = {}
) {
    // Trạng thái Form nhập liệu
    var fullName by remember { mutableStateOf("Nguyễn Minh Anh") }
    var phoneNumber by remember { mutableStateOf("0912 345 678") }
    var password by remember { mutableStateOf("12345678") }
    var confirmPassword by remember { mutableStateOf("12345678") }
    var agreeToTerms by remember { mutableStateOf(true) }

    // Bảng màu thiết kế ấm áp & chuẩn phong cách
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp (Beige)
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu sẫm sang trọng
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

        // 2. Nội dung cuộn dọc toàn màn hình
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // ====================== 1. TOP NAVIGATION BAR ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút tròn quay lại (Back Button)
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .background(Color.White, CircleShape)
                        .clickable { onBackClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    LeftArrowIcon(
                        color = forestGreenDark,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Logo thương hiệu thu nhỏ bên phải
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SproutMiniIcon(
                        color = forestGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "SpendTogether",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ====================== 2. TIÊU ĐỀ MÀN HÌNH ======================
            Text(
                text = "Tạo tài khoản mới",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = forestGreenDark,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Bắt đầu một quỹ chung minh bạch chỉ trong vài\nbước.",
                fontSize = 14.5.sp,
                color = textMuted,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ====================== 3. KHỐI THÊM ẢNH ĐẠI DIỆN ======================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Khung avatar xem trước bên trái
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFDCEDE3), CircleShape)
                        .border(1.5.dp, Color(0xFFB9DDC7), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    UserOutlineIcon(
                        color = Color(0xFF4C755E),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Dòng mô tả ở giữa
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Thêm ảnh đại diện",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = forestGreenDark
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "JPG hoặc PNG, tối đa 5 MB",
                        fontSize = 12.5.sp,
                        color = Color(0xFF82948A)
                    )
                }

                // Nút camera bên phải
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(14.dp),
                            ambientColor = Color.Black.copy(alpha = 0.06f),
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .background(Color.White, RoundedCornerShape(14.dp))
                        .clickable { onPickAvatarClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    CameraOutlineIcon(
                        color = Color(0xFF4A6557),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            // ====================== 4. BIỂU MẪU THÔNG TIN (4 FIELDS) ======================
            // --- Trường 1: Họ và tên ---
            Text(
                text = "Họ và tên",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF42564C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.07f)
                    )
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    UserOutlineIcon(
                        color = Color(0xFF7D9488),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = forestGreenDark,
                            fontWeight = FontWeight.Normal
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(forestGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Trường 2: Số điện thoại ---
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
                    .height(52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.07f)
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
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
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

            Spacer(modifier = Modifier.height(14.dp))

            // --- Trường 3: Mật khẩu ---
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
                    .height(52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.07f)
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
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = password,
                        onValueChange = { password = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = forestGreenDark,
                            fontWeight = FontWeight.Normal
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        cursorBrush = SolidColor(forestGreenDark),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Mạnh",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF4C8764)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Trường 4: Nhập lại mật khẩu ---
            Text(
                text = "Nhập lại mật khẩu",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF42564C)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 3.dp,
                        shape = CircleShape,
                        ambientColor = Color.Black.copy(alpha = 0.05f),
                        spotColor = Color.Black.copy(alpha = 0.07f)
                    )
                    .background(Color.White, CircleShape)
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ShieldCheckOutlineIcon(
                        color = Color(0xFF7D9488),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    BasicTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = forestGreenDark,
                            fontWeight = FontWeight.Normal
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        cursorBrush = SolidColor(forestGreenDark),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // ====================== 5. CHECKBOX ĐIỀU KHOẢN ======================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { agreeToTerms = !agreeToTerms }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox hình vuông bo góc nhỏ
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            color = if (agreeToTerms) forestGreenDark else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(
                            width = 1.2.dp,
                            color = if (agreeToTerms) forestGreenDark else Color(0xFF94A79D),
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (agreeToTerms) {
                        Text(
                            text = "✓",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Tôi đồng ý với Điều khoản sử dụng và Chính sách bảo mật.",
                    fontSize = 12.5.sp,
                    color = textMuted,
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ====================== 6. NÚT TẠO TÀI KHOẢN ======================
            Button(
                onClick = { onSignUpSuccess() },
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
                    contentColor = Color.White
                ),
                shape = CircleShape
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    SparkleIcon(
                        color = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Tạo tài khoản",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ====================== 7. CHÂN TRANG: ĐÃ CÓ TÀI KHOẢN? ĐĂNG NHẬP ======================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản? ",
                    fontSize = 14.sp,
                    color = textMuted
                )
                Text(
                    text = "Đăng nhập",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = forestGreenDark,
                    modifier = Modifier
                        .clickable { onLoginClicked() }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Biểu tượng mũi tên sang trái (Back Arrow)
 */
@Composable
private fun LeftArrowIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 2.dp.toPx()

        // Thân ngang
        drawLine(
            color = color,
            start = Offset(w * 0.85f, h * 0.50f),
            end = Offset(w * 0.18f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh trên
        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.22f),
            end = Offset(w * 0.18f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh dưới
        drawLine(
            color = color,
            start = Offset(w * 0.45f, h * 0.78f),
            end = Offset(w * 0.18f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Biểu tượng người dùng (User outline)
 */
@Composable
private fun UserOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Đầu người
        drawCircle(
            color = color,
            radius = w * 0.22f,
            center = Offset(w * 0.50f, h * 0.30f),
            style = stroke
        )

        // Vai áo uốn cong
        val body = Path().apply {
            moveTo(w * 0.15f, h * 0.86f)
            cubicTo(w * 0.18f, h * 0.62f, w * 0.35f, h * 0.56f, w * 0.50f, h * 0.56f)
            cubicTo(w * 0.65f, h * 0.56f, w * 0.82f, h * 0.62f, w * 0.85f, h * 0.86f)
        }
        drawPath(body, color = color, style = stroke)
    }
}

/**
 * Biểu tượng máy ảnh (Camera outline)
 */
@Composable
private fun CameraOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Thân máy ảnh
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.14f, h * 0.30f),
            size = Size(w * 0.72f, h * 0.54f),
            cornerRadius = CornerRadius(4.dp.toPx()),
            style = stroke
        )

        // Ống kính tròn
        drawCircle(
            color = color,
            radius = w * 0.15f,
            center = Offset(w * 0.50f, h * 0.57f),
            style = stroke
        )

        // Đỉnh máy ảnh (gù flash)
        val flash = Path().apply {
            moveTo(w * 0.36f, h * 0.30f)
            lineTo(w * 0.42f, h * 0.20f)
            lineTo(w * 0.58f, h * 0.20f)
            lineTo(w * 0.64f, h * 0.30f)
        }
        drawPath(flash, color = color, style = stroke)
    }
}

/**
 * Biểu tượng khiên bảo vệ có dấu tích (Shield-Check outline)
 */
@Composable
private fun ShieldCheckOutlineIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)

        // Thân khiên
        val shield = Path().apply {
            moveTo(w * 0.50f, h * 0.14f)
            lineTo(w * 0.82f, h * 0.26f)
            lineTo(w * 0.82f, h * 0.56f)
            cubicTo(w * 0.82f, h * 0.74f, w * 0.66f, h * 0.86f, w * 0.50f, h * 0.92f)
            cubicTo(w * 0.34f, h * 0.86f, w * 0.18f, h * 0.74f, w * 0.18f, h * 0.56f)
            lineTo(w * 0.18f, h * 0.26f)
            close()
        }
        drawPath(shield, color = color, style = stroke)

        // Dấu tích bên trong
        val check = Path().apply {
            moveTo(w * 0.36f, h * 0.50f)
            lineTo(w * 0.46f, h * 0.62f)
            lineTo(w * 0.66f, h * 0.40f)
        }
        drawPath(check, color = color, style = stroke)
    }
}

/**
 * Biểu tượng ống nghe điện thoại
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
 * Biểu tượng ổ khóa
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
        drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(w * 0.50f, h * 0.64f))
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
 * Biểu tượng mầm cây mini
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

        drawLine(
            color = color,
            start = Offset(w * 0.30f, h * 0.82f),
            end = Offset(w * 0.70f, h * 0.82f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        val stem = Path().apply {
            moveTo(w * 0.50f, h * 0.82f)
            cubicTo(w * 0.50f, h * 0.55f, w * 0.49f, h * 0.42f, w * 0.49f, h * 0.28f)
        }
        drawPath(stem, color = color, style = stroke)

        val leftLeaf = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            cubicTo(w * 0.28f, h * 0.52f, w * 0.26f, h * 0.38f, w * 0.46f, h * 0.38f)
            cubicTo(w * 0.50f, h * 0.44f, w * 0.50f, h * 0.50f, w * 0.50f, h * 0.55f)
        }
        drawPath(leftLeaf, color = color, style = stroke)

        val rightLeaf = Path().apply {
            moveTo(w * 0.49f, h * 0.38f)
            cubicTo(w * 0.58f, h * 0.35f, w * 0.74f, h * 0.20f, w * 0.64f, h * 0.16f)
            cubicTo(w * 0.52f, h * 0.14f, w * 0.46f, h * 0.26f, w * 0.49f, h * 0.38f)
        }
        drawPath(rightLeaf, color = color, style = stroke)
    }
}

/**
 * Biểu tượng ngôi sao 4 cánh lấp lánh (Sparkle)
 */
@Composable
private fun SparkleIcon(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val path = Path().apply {
            moveTo(cx, 0f)
            cubicTo(cx, cy * 0.45f, cx * 0.55f, cy, 0f, cy)
            cubicTo(cx * 0.55f, cy, cx, cy * 1.55f, cx, h)
            cubicTo(cx, cy * 1.55f, cx * 1.45f, cy, w, cy)
            cubicTo(cx * 1.45f, cy, cx, cy * 0.45f, cx, 0f)
            close()
        }
        drawPath(path = path, color = color)
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "06 · Đăng ký (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun SignUpScreenPreview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            SignUpScreen()
        }
    }
}
