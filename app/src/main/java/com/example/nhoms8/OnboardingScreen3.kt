package com.example.nhoms8

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 04 · Màn hình Hướng dẫn 3 (Onboarding Screen 3: Báo cáo trực quan)
 * Ứng dụng SpendTogether
 */
@Composable
fun OnboardingScreen3(
    onSkipClicked: () -> Unit = {},
    onGetStartedClicked: () -> Unit = {}
) {
    // Bảng màu thiết kế ấm áp & chuẩn phong cách
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp (Beige)
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt cho mảng nền cong
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu sẫm sang trọng
    val sageAccent = Color(0xFF4A725E)           // Xanh sage nhãn phụ & nút bỏ qua
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

            // Mảng cong góc dưới bên trái
            drawCircle(
                color = ambientSage,
                radius = w * 0.52f,
                center = Offset(w * 0.06f, h * 0.88f)
            )
        }

        // 2. Nội dung chính toàn màn hình
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ====================== TOP BAR: NÚT BỎ QUA ======================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bỏ qua",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = sageAccent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSkipClicked() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // ====================== PHẦN THẺ HÌNH ẢNH MINH HỌA ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                // Khung tranh minh họa 3D bo góc lớn
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(26.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.12f)
                        )
                        .clip(RoundedCornerShape(26.dp))
                        .background(Color(0xFFEBE6DD))
                ) {
                    VisualAnalyticsArtwork(modifier = Modifier.fillMaxSize())
                }

                // Hộp thoại nổi (Floating Pill Badge) đè lên mép dưới ảnh
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-6).dp)
                        .fillMaxWidth(0.86f)
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.15f)
                        )
                        .background(
                            color = Color.White.copy(alpha = 0.88f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.75f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nhìn là hiểu quỹ đang đi về đâu",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF173427),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ====================== PHẦN KHỐI THÔNG TIN VĂN BẢN ======================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Nhãn phụ (Overline)
                Text(
                    text = "BÁO CÁO TRỰC QUAN",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF507865),
                    letterSpacing = 1.6.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tiêu đề chính (Main Heading)
                Text(
                    text = "Thói quen chi tiêu trong một ánh\nnhìn",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 33.sp,
                    color = forestGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Đoạn mô tả (Body text)
                Text(
                    text = "Biểu đồ rõ ràng giúp cả nhóm điều chỉnh ngân\nsách đúng lúc.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center
                )
            }

            // ====================== FOOTER: CHỈ BÁO & NÚT BẮT ĐẦU NGAY ======================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Chỉ báo phân trang (Dots) - Chấm thứ 3 Active
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chấm 1 (Inactive Dot)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFCAD8CE), CircleShape)
                    )
                    // Chấm 2 (Inactive Dot)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFCAD8CE), CircleShape)
                    )
                    // Chấm 3 (Active Pill - Trang hiện tại)
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .background(forestGreenDark, RoundedCornerShape(3.dp))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút hành động chính (Primary CTA Button: Bắt đầu ngay)
                Button(
                    onClick = { onGetStartedClicked() },
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
                        // Icon ngôi sao / lấp lánh (Sparkle)
                        SparkleIcon(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Bắt đầu ngay",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Biểu tượng ngôi sao 4 cánh lấp lánh (Sparkle / ✦)
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
 * Tranh minh họa phong cách 3D / Paper-cut nghệ thuật:
 * Biểu đồ tròn và biểu đồ cột kết hợp mầm cây, hoa lá vươn lên tươi tốt,
 * bao quanh bởi khung viền sáng bo tròn và các hạt đốm sáng lung linh.
 */
@Composable
private fun VisualAnalyticsArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Nền ấm áp với các lá rơi và tinh thể lấp lánh
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF7F2E9), Color(0xFFE9E2D7))
            ),
            size = size
        )

        // Các đốm sáng vàng và lá bay xung quanh
        val sparkles = listOf(
            Offset(w * 0.15f, h * 0.14f),
            Offset(w * 0.84f, h * 0.18f),
            Offset(w * 0.12f, h * 0.46f),
            Offset(w * 0.88f, h * 0.44f),
            Offset(w * 0.16f, h * 0.72f),
            Offset(w * 0.85f, h * 0.70f)
        )
        for (pt in sparkles) {
            drawCircle(color = Color(0xFFFFF9E6).copy(alpha = 0.85f), radius = 3.5.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFFFE082).copy(alpha = 0.30f), radius = 8.dp.toPx(), center = pt)
        }

        // 2. Tấm thẻ đế trung tâm (Center Plinth) màu kem sáng bo tròn
        val plinthWidth = w * 0.56f
        val plinthHeight = h * 0.76f
        val plinthLeft = (w - plinthWidth) / 2f
        val plinthTop = h * 0.10f

        // Đổ bóng cho tấm thẻ
        drawRoundRect(
            color = Color(0xFFD6C8B5).copy(alpha = 0.45f),
            topLeft = Offset(plinthLeft, plinthTop + 6.dp.toPx()),
            size = Size(plinthWidth, plinthHeight),
            cornerRadius = CornerRadius(28.dp.toPx())
        )
        // Mặt thẻ chính
        drawRoundRect(
            color = Color(0xFFF8F5EE),
            topLeft = Offset(plinthLeft, plinthTop),
            size = Size(plinthWidth, plinthHeight),
            cornerRadius = CornerRadius(28.dp.toPx())
        )

        // 3. BIỂU ĐỒ TRÒN 3D (Layered 3D Pie Chart) ở nửa trên tấm thẻ
        val pieCenter = Offset(w * 0.50f, h * 0.31f)
        val pieRadius = w * 0.19f

        // Đổ bóng 3D dày bên dưới biểu đồ tròn
        drawCircle(
            color = Color(0xFFBFD2C5),
            radius = pieRadius,
            center = Offset(pieCenter.x, pieCenter.y + 7.dp.toPx())
        )
        // Lát cắt 1: Xanh rêu đậm (55%)
        drawArc(
            color = Color(0xFF6B937C),
            startAngle = 140f,
            sweepAngle = 190f,
            useCenter = true,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2)
        )
        // Lát cắt 2: Xanh mint nhạt (30%)
        drawArc(
            color = Color(0xFF9EC2AA),
            startAngle = 330f,
            sweepAngle = 110f,
            useCenter = true,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2)
        )
        // Lát cắt 3: Màu kem bơ ấm (15%)
        drawArc(
            color = Color(0xFFEFE6D1),
            startAngle = 80f,
            sweepAngle = 60f,
            useCenter = true,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2)
        )
        // Khía rãnh phân tách các lát bánh pie
        drawLine(
            color = Color(0xFFF8F5EE),
            start = pieCenter,
            end = Offset(pieCenter.x + pieRadius * 0.7f, pieCenter.y - pieRadius * 0.7f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFFF8F5EE),
            start = pieCenter,
            end = Offset(pieCenter.x + pieRadius * 0.2f, pieCenter.y + pieRadius * 0.95f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFFF8F5EE),
            start = pieCenter,
            end = Offset(pieCenter.x - pieRadius * 0.9f, pieCenter.y - pieRadius * 0.4f),
            strokeWidth = 2.dp.toPx()
        )

        // Cây mầm xanh vươn lên từ tâm biểu đồ tròn
        drawLine(
            color = Color(0xFF436B54),
            start = pieCenter,
            end = Offset(pieCenter.x, pieCenter.y - pieRadius * 1.35f),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Các lá non vươn ra
        drawCircle(color = Color(0xFF5D886E), radius = 5.dp.toPx(), center = Offset(pieCenter.x - 7.dp.toPx(), pieCenter.y - pieRadius * 1.1f))
        drawCircle(color = Color(0xFF5D886E), radius = 5.dp.toPx(), center = Offset(pieCenter.x + 8.dp.toPx(), pieCenter.y - pieRadius * 1.25f))
        drawCircle(color = Color(0xFF7FA890), radius = 6.dp.toPx(), center = Offset(pieCenter.x, pieCenter.y - pieRadius * 1.40f))

        // 4. BIỂU ĐỒ CỘT 3D (3D Bar Chart Columns) kết hợp mầm cây ở nửa dưới tấm thẻ
        val barBaseY = h * 0.74f

        // Đất màu mỡ ở chân biểu đồ cột
        val soilPath = Path().apply {
            moveTo(plinthLeft + 16.dp.toPx(), barBaseY)
            cubicTo(
                w * 0.38f, barBaseY - 6.dp.toPx(),
                w * 0.62f, barBaseY + 4.dp.toPx(),
                plinthLeft + plinthWidth - 16.dp.toPx(), barBaseY
            )
            lineTo(plinthLeft + plinthWidth - 16.dp.toPx(), barBaseY + 12.dp.toPx())
            lineTo(plinthLeft + 16.dp.toPx(), barBaseY + 12.dp.toPx())
            close()
        }
        drawPath(soilPath, color = Color(0xFF74553A))

        // Cột 1 (Bên trái - Chiều cao thấp nhất, màu xanh bơ)
        val col1Width = w * 0.09f
        val col1Height = h * 0.12f
        drawRoundRect(
            color = Color(0xFF98BFA6),
            topLeft = Offset(w * 0.35f, barBaseY - col1Height),
            size = Size(col1Width, col1Height),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Cột 2 (Ở giữa - Chiều cao trung bình, màu xanh ngọc mint)
        val col2Width = w * 0.09f
        val col2Height = h * 0.19f
        drawRoundRect(
            color = Color(0xFF71A384),
            topLeft = Offset(w * 0.46f, barBaseY - col2Height),
            size = Size(col2Width, col2Height),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Cột 3 (Bên phải - Chiều cao cao nhất, màu xanh rừng đậm)
        val col3Width = w * 0.09f
        val col3Height = h * 0.25f
        drawRoundRect(
            color = Color(0xFF426E54),
            topLeft = Offset(w * 0.57f, barBaseY - col3Height),
            size = Size(col3Width, col3Height),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Dây leo & hoa nhỏ leo quanh các cột
        for (m in 0..2) {
            val stemX = w * (0.39f + m * 0.11f)
            val stemHeight = when (m) {
                0 -> col1Height
                1 -> col2Height
                else -> col3Height
            }
            // Thân dây leo
            drawLine(
                color = Color(0xFF385E47),
                start = Offset(stemX, barBaseY),
                end = Offset(stemX, barBaseY - stemHeight - 12.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            // Những chiếc lá nhỏ
            drawCircle(color = Color(0xFF5D886E), radius = 3.5.dp.toPx(), center = Offset(stemX - 4.dp.toPx(), barBaseY - stemHeight * 0.5f))
            drawCircle(color = Color(0xFF7DA88F), radius = 3.5.dp.toPx(), center = Offset(stemX + 4.dp.toPx(), barBaseY - stemHeight * 0.8f))
            // Hoa nhỏ màu kem ở đỉnh
            drawCircle(color = Color(0xFFFFF9ED), radius = 3.5.dp.toPx(), center = Offset(stemX, barBaseY - stemHeight - 12.dp.toPx()))
            drawCircle(color = Color(0xFFF59E0B), radius = 1.5.dp.toPx(), center = Offset(stemX, barBaseY - stemHeight - 12.dp.toPx()))
        }

        // 5. KHUNG VIỀN SÁNG BO TRÒN (Glowing White Frame) bao quanh khu vực minh họa
        val frameWidth = w * 0.64f
        val frameHeight = h * 0.72f
        val frameLeft = (w - frameWidth) / 2f
        val frameTop = h * 0.12f

        drawRoundRect(
            color = Color.White.copy(alpha = 0.85f),
            topLeft = Offset(frameLeft, frameTop),
            size = Size(frameWidth, frameHeight),
            cornerRadius = CornerRadius(30.dp.toPx()),
            style = Stroke(width = 3.5.dp.toPx())
        )
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "04 · Giới thiệu báo cáo trực quan (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen3Preview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen3()
        }
    }
}
