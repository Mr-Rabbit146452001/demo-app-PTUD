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
 * 03 · Màn hình Hướng dẫn 2 (Onboarding Screen 2: Giới thiệu chia hóa đơn)
 * Ứng dụng SpendTogether
 */
@Composable
fun OnboardingScreen2(
    onSkipClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {}
) {
    // Bảng màu thiết kế ấm áp & chuẩn phong cách
    val backgroundColor = Color(0xFFF5F3EC)      // Màu kem nhạt ấm áp (Beige)
    val ambientSage = Color(0xFFDDEAE0)          // Màu xanh xô thơm nhạt cho mảng nền cong
    val forestGreenDark = Color(0xFF14382C)      // Xanh rêu đậm sang trọng
    val sageAccent = Color(0xFF4A725E)           // Xanh sage nhãn phụ & nút bỏ qua
    val textMuted = Color(0xFF6B7E74)            // Màu xám xanh trung tính

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
                // Khung tranh minh họa 3D bo tròn lớn
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
                        .background(Color(0xFFE8E5DC))
                ) {
                    BillSplitArtwork(modifier = Modifier.fillMaxSize())
                }

                // Hộp thoại nổi (Floating Pill Badge) đè lên mép dưới ảnh
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-6).dp)
                        .fillMaxWidth(0.88f)
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
                        text = "Chia đúng phần, không còn tính nhầm",
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
                    text = "CHIA HÓA ĐƠN",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF507865),
                    letterSpacing = 1.6.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tiêu đề chính (Main Heading)
                Text(
                    text = "Chia tiền công bằng theo cách\ncủa bạn",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 33.sp,
                    color = forestGreenDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Đoạn mô tả (Body text)
                Text(
                    text = "Chia đều, theo tỷ lệ hoặc nhập chính xác số tiền\ncho từng người.",
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
                // Chỉ báo phân trang (Dots) - Chấm thứ 2 Active
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
                    // Chấm 2 (Active Pill - Trang hiện tại)
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(6.dp)
                            .background(forestGreenDark, RoundedCornerShape(3.dp))
                    )
                    // Chấm 3 (Inactive Dot)
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFFCAD8CE), CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Nút hành động chính (Primary CTA Button)
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
 * Mũi tên chỉ sang phải thanh mảnh, sắc nét
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

        // Thân ngang
        drawLine(
            color = color,
            start = Offset(w * 0.15f, h * 0.50f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh trên
        drawLine(
            color = color,
            start = Offset(w * 0.55f, h * 0.22f),
            end = Offset(w * 0.82f, h * 0.50f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Cánh dưới
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
 * Tranh minh họa phong cách Paper-cut / 3D nghệ thuật:
 * Bàn tròn với hóa đơn "BILL DINNER", nhiều bàn tay cùng đặt quanh bàn,
 * tiền xu, cây xanh và khung quét chụp hóa đơn điện thoại màu trắng.
 */
@Composable
private fun BillSplitArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Nền hoa lá cỏ cây bao quanh viền
        drawRect(color = Color(0xFFE5EDE6), size = size)

        // Các chùm lá xanh & hoa cỏ trang trí xung quanh viền tranh
        val foliageColors = listOf(Color(0xFF8FB89F), Color(0xFFB1D2BE), Color(0xFF6F9A80), Color(0xFF4A725B))
        for (i in 0..15) {
            val angle = i * (360f / 16f)
            val rad = Math.toRadians(angle.toDouble())
            val leafX = (w * 0.50f + Math.cos(rad) * (w * 0.44f)).toFloat()
            val leafY = (h * 0.50f + Math.sin(rad) * (h * 0.44f)).toFloat()
            drawCircle(
                color = foliageColors[i % foliageColors.size],
                radius = (14 + (i % 5) * 3).dp.toPx(),
                center = Offset(leafX, leafY)
            )
        }

        // Hạt đốm sáng lung linh (bokeh / fairy lights)
        val sparklePoints = listOf(
            Offset(w * 0.20f, h * 0.15f),
            Offset(w * 0.82f, h * 0.16f),
            Offset(w * 0.88f, h * 0.42f),
            Offset(w * 0.14f, h * 0.48f),
            Offset(w * 0.18f, h * 0.72f),
            Offset(w * 0.84f, h * 0.75f)
        )
        for (pt in sparklePoints) {
            drawCircle(color = Color(0xFFFFF7DB).copy(alpha = 0.8f), radius = 3.5.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFFFE082).copy(alpha = 0.35f), radius = 7.dp.toPx(), center = pt)
        }

        // 2. Mặt bàn tròn phủ khăn vải dệt màu be có tua rua macrame
        val tableCenter = Offset(w * 0.50f, h * 0.50f)
        val tableRadius = w * 0.38f

        // Vòng tua rua viền bàn
        drawCircle(
            color = Color(0xFFDACBB8),
            radius = tableRadius + 6.dp.toPx(),
            center = tableCenter
        )
        // Mặt khăn bàn tròn dệt kem sáng
        drawCircle(
            color = Color(0xFFF7F2EA),
            radius = tableRadius,
            center = tableCenter
        )

        // 3. Cây lá xanh paper-cut trung tâm vươn lên từ hóa đơn
        val plantCenter = Offset(w * 0.50f, h * 0.34f)
        // Thân cây xanh rêu
        drawLine(
            color = Color(0xFF587D67),
            start = Offset(w * 0.50f, h * 0.45f),
            end = Offset(w * 0.50f, h * 0.22f),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Lá cây trung tâm (hình lá elip thanh mảnh phong cách paper cut)
        // Lá đỉnh
        drawOval(
            color = Color(0xFF9ECAA9),
            topLeft = Offset(w * 0.44f, h * 0.18f),
            size = Size(w * 0.12f, h * 0.10f)
        )
        // Cặp lá trên
        drawOval(
            color = Color(0xFF86B795),
            topLeft = Offset(w * 0.34f, h * 0.25f),
            size = Size(w * 0.14f, h * 0.08f)
        )
        drawOval(
            color = Color(0xFF86B795),
            topLeft = Offset(w * 0.52f, h * 0.25f),
            size = Size(w * 0.14f, h * 0.08f)
        )
        // Cặp lá dưới
        drawOval(
            color = Color(0xFF72A482),
            topLeft = Offset(w * 0.36f, h * 0.32f),
            size = Size(w * 0.13f, h * 0.07f)
        )
        drawOval(
            color = Color(0xFF72A482),
            topLeft = Offset(w * 0.51f, h * 0.32f),
            size = Size(w * 0.13f, h * 0.07f)
        )

        // 4. Tờ hóa đơn "BILL DINNER" ở giữa bàn
        val billLeft = w * 0.41f
        val billTop = h * 0.40f
        val billWidth = w * 0.18f
        val billHeight = h * 0.22f

        // Giấy hóa đơn giấy vintage màu kem nâu viền răng cưa
        drawRoundRect(
            color = Color(0xFFE8DAC3),
            topLeft = Offset(billLeft, billTop),
            size = Size(billWidth, billHeight),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        // Tiêu đề "BILL" & "DINNER" & các dòng kẻ hóa đơn
        drawLine(
            color = Color(0xFF785E43),
            start = Offset(billLeft + 6.dp.toPx(), billTop + 6.dp.toPx()),
            end = Offset(billLeft + billWidth - 6.dp.toPx(), billTop + 6.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFF997D61),
            start = Offset(billLeft + 8.dp.toPx(), billTop + 14.dp.toPx()),
            end = Offset(billLeft + billWidth - 8.dp.toPx(), billTop + 14.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )
        for (k in 0..4) {
            val lineY = billTop + 22.dp.toPx() + k * 8.dp.toPx()
            drawLine(
                color = Color(0xFFB59D82),
                start = Offset(billLeft + 6.dp.toPx(), lineY),
                end = Offset(billLeft + billWidth - 6.dp.toPx(), lineY),
                strokeWidth = 1.dp.toPx()
            )
        }
        // Dòng chữ DINNER ở đáy bill
        drawLine(
            color = Color(0xFF785E43),
            start = Offset(billLeft + 8.dp.toPx(), billTop + billHeight - 8.dp.toPx()),
            end = Offset(billLeft + billWidth - 8.dp.toPx(), billTop + billHeight - 8.dp.toPx()),
            strokeWidth = 2.dp.toPx()
        )

        // Bộ dao nĩa bạc bên phải hóa đơn
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(w * 0.61f, h * 0.46f),
            end = Offset(w * 0.61f, h * 0.58f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(w * 0.635f, h * 0.46f),
            end = Offset(w * 0.635f, h * 0.58f),
            strokeWidth = 1.5.dp.toPx()
        )
        // Ly nước thủy tinh phía trên bên phải
        drawCircle(
            color = Color(0xFFCBD5E1).copy(alpha = 0.5f),
            radius = 8.dp.toPx(),
            center = Offset(w * 0.68f, h * 0.42f)
        )

        // Các đồng tiền xu vàng, nút áo scattered xung quanh bill
        val coins = listOf(
            Offset(w * 0.35f, h * 0.39f),
            Offset(w * 0.38f, h * 0.42f),
            Offset(w * 0.33f, h * 0.45f),
            Offset(w * 0.56f, h * 0.38f),
            Offset(w * 0.60f, h * 0.41f),
            Offset(w * 0.34f, h * 0.52f),
            Offset(w * 0.36f, h * 0.56f)
        )
        for ((idx, coin) in coins.withIndex()) {
            val coinColor = if (idx % 2 == 0) Color(0xFFEAB308) else Color(0xFFD4D4D8)
            drawCircle(color = coinColor, radius = 4.dp.toPx(), center = coin)
        }

        // 5. Bốn cánh tay/bàn tay vươn vào quanh bàn:
        // Cánh tay TRÁI (áo kẻ sọc xanh trắng)
        drawRoundRect(
            color = Color(0xFF7FA899),
            topLeft = Offset(0f, h * 0.34f),
            size = Size(w * 0.22f, 26.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawCircle(color = Color(0xFFF7D5BE), radius = 10.dp.toPx(), center = Offset(w * 0.24f, h * 0.37f))
        drawRoundRect(
            color = Color(0xFFF7D5BE),
            topLeft = Offset(w * 0.22f, h * 0.35f),
            size = Size(w * 0.12f, 12.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Cánh tay PHẢI (áo hoa văn xanh lá xô thơm)
        drawRoundRect(
            color = Color(0xFF86A893),
            topLeft = Offset(w * 0.78f, h * 0.34f),
            size = Size(w * 0.22f, 26.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawCircle(color = Color(0xFFF7D5BE), radius = 10.dp.toPx(), center = Offset(w * 0.76f, h * 0.37f))
        drawRoundRect(
            color = Color(0xFFF7D5BE),
            topLeft = Offset(w * 0.66f, h * 0.35f),
            size = Size(w * 0.12f, 12.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Cánh tay DƯỚI TRÁI (tay đeo vòng ngọc lam/da, chỉ ngón tay vào bill)
        drawRoundRect(
            color = Color(0xFFE5D5C3),
            topLeft = Offset(w * 0.15f, h * 0.58f),
            size = Size(w * 0.18f, 24.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        // Vòng tay turquoise & nâu
        drawCircle(color = Color(0xFF06B6D4), radius = 3.dp.toPx(), center = Offset(w * 0.30f, h * 0.57f))
        drawCircle(color = Color(0xFF854D0E), radius = 3.dp.toPx(), center = Offset(w * 0.32f, h * 0.57f))
        // Bàn tay & ngón trỏ vươn đến bill
        drawRoundRect(
            color = Color(0xFFF7D5BE),
            topLeft = Offset(w * 0.31f, h * 0.47f),
            size = Size(w * 0.09f, 22.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // Cánh tay DƯỚI PHẢI (tay đeo đồng hồ da vàng, chỉ ngón tay vào bill)
        drawRoundRect(
            color = Color(0xFF86A893),
            topLeft = Offset(w * 0.68f, h * 0.58f),
            size = Size(w * 0.18f, 24.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        // Đồng hồ đeo tay vàng mặt tròn
        drawCircle(color = Color(0xFFFDE68A), radius = 5.dp.toPx(), center = Offset(w * 0.69f, h * 0.57f))
        drawCircle(color = Color(0xFF78350F), radius = 2.dp.toPx(), center = Offset(w * 0.69f, h * 0.57f))
        // Bàn tay & ngón trỏ vươn đến bill
        drawRoundRect(
            color = Color(0xFFF7D5BE),
            topLeft = Offset(w * 0.59f, h * 0.47f),
            size = Size(w * 0.09f, 22.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )

        // 6. KHUNG QUÉT CHỤP HÓA ĐƠN ĐIỆN THOẠI (Trắng nổi bật)
        val scannerWidth = w * 0.52f
        val scannerHeight = h * 0.72f
        val scannerLeft = (w - scannerWidth) / 2f
        val scannerTop = h * 0.14f

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(scannerLeft, scannerTop),
            size = Size(scannerWidth, scannerHeight),
            cornerRadius = CornerRadius(30.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "03 · Giới thiệu chia hóa đơn (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen2Preview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            OnboardingScreen2()
        }
    }
}
