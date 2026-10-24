package com.example.nhoms8

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.ui.theme.Nhoms8Theme

/**
 * 02 · Màn hình Hướng dẫn 1 (Onboarding Screen 1: Giới thiệu quản lý chi tiêu)
 * Ứng dụng SpendTogether
 */
@Composable
fun OnboardingScreen1(
    onSkipClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {}
) {
    // Bảng màu ấm áp & thanh lịch theo thiết kế
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp (Beige)
    val ambientSage = Color(0xFFDCE9DF)          // Màu xanh xô thơm nhạt cho họa tiết nền
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm sang trọng
    val sageAccent = Color(0xFF4A725E)           // Xanh sage cho nhãn tag phụ & nút bỏ qua
    val textMuted = Color(0xFF6B7E74)            // Màu chữ mô tả xám xanh dịu mắt

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Họa tiết trang trí nền: Mảng cong bán nguyệt ở góc trên phải & góc dưới trái
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
                // Khung chứa tranh vẽ 3D ấm cúng
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
                        .background(Color(0xFFEDE4D8))
                ) {
                    TeamFinanceArtwork(modifier = Modifier.fillMaxSize())
                }

                // Hộp thoại nổi (Floating Pill Badge) đè lên mép dưới bức tranh
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-6).dp)
                        .fillMaxWidth(0.92f)
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
                        text = "Một quỹ chung, không còn khoản chi thất lạc",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF173427),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ====================== PHẦN KHỐI THÔNG TIN GIỚI THIỆU ======================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                // Nhãn phụ (Overline Tag)
                Text(
                    text = "QUẢN LÝ DỄ DÀNG",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF507865),
                    letterSpacing = 1.6.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tiêu đề chính (Main Heading)
                Text(
                    text = "Quản lý chi tiêu nhóm thật nhẹ\nnhàng",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 33.sp,
                    color = forestGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Đoạn mô tả (Body text)
                Text(
                    text = "Ghi lại mọi khoản thu chi, biết quỹ còn bao\nnhiêu và ai đã đóng góp.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center
                )
            }

            // ====================== FOOTER: CHỈ BÁO & NÚT TIẾP TỤC ======================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                // Chỉ báo phân trang (Page Indicator / Dots)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Chấm 1 (Active Pill)
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .background(forestGreenDark, RoundedCornerShape(3.dp))
                    )
                    // Chấm 2 (Inactive Dot)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFCAD8CE), CircleShape)
                    )
                    // Chấm 3 (Inactive Dot)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFCAD8CE), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút hành động chính (Primary CTA Button: Tiếp tục)
                Button(
                    onClick = { onContinueClicked() },
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
                        // Mũi tên hướng sang phải
                        RightArrowIcon(
                            color = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Tiếp tục",
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
 * Biểu tượng mũi tên sang phải (Right Arrow) vẽ bằng Canvas chuẩn nét
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

        // Đường thân mũi tên ngang
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.50f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh mũi tên trên
        drawLine(
            color = color,
            start = Offset(w * 0.55f, h * 0.22f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh mũi tên dưới
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
 * Bức tranh minh họa 3 bạn trẻ cùng nhau quản lý tài chính nhóm bên khung cửa sổ nhìn ra đồi núi
 */
@Composable
private fun TeamFinanceArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Tường phòng nền ấm áp
        drawRect(color = Color(0xFFF3ECE2), size = size)

        // 2. Cửa sổ lớn ở giữa nhìn ra thiên nhiên ruộng bậc thang
        val windowLeft = w * 0.20f
        val windowTop = h * 0.08f
        val windowWidth = w * 0.60f
        val windowHeight = h * 0.50f

        // Bầu trời ngoài cửa sổ
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE5EFE8), Color(0xFFD6E7DC)),
                startY = windowTop,
                endY = windowTop + windowHeight
            ),
            topLeft = Offset(windowLeft, windowTop),
            size = Size(windowWidth, windowHeight)
        )

        // Đồi núi & ruộng bậc thang ngoài cửa sổ
        for (i in 0..3) {
            val terracePath = Path().apply {
                val startY = windowTop + windowHeight * (0.42f + i * 0.15f)
                moveTo(windowLeft, startY)
                cubicTo(
                    windowLeft + windowWidth * 0.35f, startY - 14.dp.toPx(),
                    windowLeft + windowWidth * 0.70f, startY + 12.dp.toPx(),
                    windowLeft + windowWidth, startY - 4.dp.toPx()
                )
                lineTo(windowLeft + windowWidth, windowTop + windowHeight)
                lineTo(windowLeft, windowTop + windowHeight)
                close()
            }
            val terraceColor = when (i) {
                0 -> Color(0xFFB0CCA8)
                1 -> Color(0xFF90B584)
                2 -> Color(0xFF7CA670)
                else -> Color(0xFF67945A)
            }
            drawPath(terracePath, color = terraceColor)
        }

        // Khung cửa sổ gỗ màu vàng nâu nhạt
        val frameStroke = 4.dp.toPx()
        val frameColor = Color(0xFFCFAB84)
        drawRect(
            color = frameColor,
            topLeft = Offset(windowLeft, windowTop),
            size = Size(windowWidth, windowHeight),
            style = Stroke(width = frameStroke)
        )
        // Cánh cửa sổ mở nghiêng 2 bên
        drawLine(
            color = frameColor,
            start = Offset(windowLeft - 10.dp.toPx(), windowTop),
            end = Offset(windowLeft, windowTop + windowHeight),
            strokeWidth = frameStroke
        )
        drawLine(
            color = frameColor,
            start = Offset(windowLeft + windowWidth + 10.dp.toPx(), windowTop),
            end = Offset(windowLeft + windowWidth, windowTop + windowHeight),
            strokeWidth = frameStroke
        )

        // 3. Kệ sách & cây cảnh bên góc trái phòng
        drawRoundRect(
            color = Color(0xFFD2B28E),
            topLeft = Offset(w * 0.05f, h * 0.14f),
            size = Size(w * 0.14f, 4.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        // Vài quyển sách đứng
        drawRoundRect(
            color = Color(0xFF5A7E6C),
            topLeft = Offset(w * 0.06f, h * 0.09f),
            size = Size(8.dp.toPx(), 16.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFD97746),
            topLeft = Offset(w * 0.09f, h * 0.10f),
            size = Size(7.dp.toPx(), 13.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        // Cây cảnh lá xanh rủ xuống bên trái
        drawCircle(
            color = Color(0xFF4C7558),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.11f, h * 0.28f)
        )
        drawCircle(
            color = Color(0xFF649472),
            radius = 12.dp.toPx(),
            center = Offset(w * 0.08f, h * 0.32f)
        )

        // Đèn mây tre treo góc trên bên phải
        drawLine(
            color = Color(0xFF7A6B5D),
            start = Offset(w * 0.88f, 0f),
            end = Offset(w * 0.88f, h * 0.10f),
            strokeWidth = 1.5.dp.toPx()
        )
        drawRoundRect(
            color = Color(0xFFE2C296),
            topLeft = Offset(w * 0.84f, h * 0.10f),
            size = Size(18.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Cây Monstera (trầu bà lá xẻ) lớn bên phải
        drawCircle(
            color = Color(0xFF38644A),
            radius = 24.dp.toPx(),
            center = Offset(w * 0.90f, h * 0.30f)
        )
        drawCircle(
            color = Color(0xFF4D8262),
            radius = 18.dp.toPx(),
            center = Offset(w * 0.86f, h * 0.36f)
        )

        // 4. Bàn gỗ trung tâm
        val tableTopY = h * 0.52f
        drawRoundRect(
            color = Color(0xFF9F7348),
            topLeft = Offset(w * 0.14f, tableTopY),
            size = Size(w * 0.72f, 16.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        // Chân bàn
        drawRect(
            color = Color(0xFF7D5531),
            topLeft = Offset(w * 0.20f, tableTopY + 16.dp.toPx()),
            size = Size(10.dp.toPx(), h * 0.32f)
        )
        drawRect(
            color = Color(0xFF7D5531),
            topLeft = Offset(w * 0.74f, tableTopY + 16.dp.toPx()),
            size = Size(10.dp.toPx(), h * 0.32f)
        )

        // 5. Ba bạn trẻ ngồi quanh bàn
        // --- BẠN 1 (Bên trái - Nam, áo sơ mi xanh rêu) ---
        // Đầu & tóc
        drawCircle(
            color = Color(0xFF3B271E), // Tóc nâu đậm
            radius = 15.dp.toPx(),
            center = Offset(w * 0.30f, h * 0.34f)
        )
        drawCircle(
            color = Color(0xFFF6D6BD), // Khuôn mặt
            radius = 12.dp.toPx(),
            center = Offset(w * 0.31f, h * 0.355f)
        )
        // Thân áo sơ mi xanh rêu
        drawRoundRect(
            color = Color(0xFF5D7B69),
            topLeft = Offset(w * 0.21f, h * 0.40f),
            size = Size(w * 0.19f, h * 0.18f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // --- BẠN 2 (Ở giữa - Nữ, áo kem, hoa cài tóc) ---
        // Đầu & tóc dài
        drawCircle(
            color = Color(0xFF4A3226),
            radius = 16.dp.toPx(),
            center = Offset(w * 0.51f, h * 0.33f)
        )
        drawCircle(
            color = Color(0xFFFBE0CC),
            radius = 12.5.dp.toPx(),
            center = Offset(w * 0.51f, h * 0.345f)
        )
        // Bông hoa vàng nhỏ cài tóc
        drawCircle(
            color = Color(0xFFF4C444),
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.56f, h * 0.325f)
        )
        // Thân áo màu kem
        drawRoundRect(
            color = Color(0xFFF1EADB),
            topLeft = Offset(w * 0.42f, h * 0.39f),
            size = Size(w * 0.18f, h * 0.19f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // --- BẠN 3 (Bên phải - Nữ, áo blazer xanh mint, băng đô) ---
        // Đầu & tóc & băng đô
        drawCircle(
            color = Color(0xFF2E1F18),
            radius = 15.dp.toPx(),
            center = Offset(w * 0.70f, h * 0.35f)
        )
        drawCircle(
            color = Color(0xFFF6D6BD),
            radius = 12.dp.toPx(),
            center = Offset(w * 0.69f, h * 0.365f)
        )
        // Băng đô màu xanh mint
        drawArc(
            color = Color(0xFF6DA38C),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.655f, h * 0.325f),
            size = Size(18.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
        // Thân áo blazer xanh pastel
        drawRoundRect(
            color = Color(0xFF75A692),
            topLeft = Offset(w * 0.61f, h * 0.41f),
            size = Size(w * 0.19f, h * 0.17f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // 6. Các đồ vật trên bàn (Máy tính bảng, biểu đồ, laptop, tiền xu, sổ tay)
        // Máy tính bảng hiển thị biểu đồ tròn tài chính (ở giữa bạn 1 và bạn 2)
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(w * 0.37f, tableTopY - 14.dp.toPx()),
            size = Size(36.dp.toPx(), 26.dp.toPx()),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFFF8FAFC),
            topLeft = Offset(w * 0.37f + 2.dp.toPx(), tableTopY - 12.dp.toPx()),
            size = Size(32.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        // Biểu đồ tròn trên màn hình tablet
        drawArc(
            color = Color(0xFF10B981),
            startAngle = 0f,
            sweepAngle = 210f,
            useCenter = true,
            topLeft = Offset(w * 0.39f, tableTopY - 10.dp.toPx()),
            size = Size(11.dp.toPx(), 11.dp.toPx())
        )
        drawArc(
            color = Color(0xFFF59E0B),
            startAngle = 210f,
            sweepAngle = 150f,
            useCenter = true,
            topLeft = Offset(w * 0.39f, tableTopY - 10.dp.toPx()),
            size = Size(11.dp.toPx(), 11.dp.toPx())
        )

        // Sổ tay mở & bút ghi chép (trước mặt bạn 2)
        drawRoundRect(
            color = Color(0xFFFFFBEB),
            topLeft = Offset(w * 0.51f, tableTopY - 8.dp.toPx()),
            size = Size(26.dp.toPx(), 18.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        // Máy tính cầm tay nhỏ
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(w * 0.58f, tableTopY - 4.dp.toPx()),
            size = Size(12.dp.toPx(), 14.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        // Các đồng tiền xu vàng xếp chồng
        drawCircle(color = Color(0xFFEAB308), radius = 3.dp.toPx(), center = Offset(w * 0.56f, tableTopY + 5.dp.toPx()))
        drawCircle(color = Color(0xFFCA8A04), radius = 3.dp.toPx(), center = Offset(w * 0.58f, tableTopY + 6.dp.toPx()))

        // Laptop mở nắp (trước mặt bạn 3)
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(w * 0.69f, tableTopY - 18.dp.toPx()),
            size = Size(34.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(w * 0.68f, tableTopY + 2.dp.toPx()),
            size = Size(38.dp.toPx(), 4.dp.toPx()),
            cornerRadius = CornerRadius(1.dp.toPx())
        )
        // Tách cà phê ấm cúng
        drawCircle(
            color = Color(0xFFFFFFFF),
            radius = 4.dp.toPx(),
            center = Offset(w * 0.66f, tableTopY + 6.dp.toPx())
        )
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "02 · Giới thiệu quản lý chi tiêu (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen1Preview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen1()
        }
    }
}
