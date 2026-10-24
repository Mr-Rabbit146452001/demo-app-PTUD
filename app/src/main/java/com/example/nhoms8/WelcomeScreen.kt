package com.example.nhoms8

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
 * 01 · Màn hình Chào mừng (Splash / Welcome Screen)
 * Ứng dụng SpendTogether - Quản lý chi tiêu cá nhân & nhóm
 */
@Composable
fun WelcomeScreen() {
    // Hiệu ứng pulse mờ ảo cho dòng loading phía dưới
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_loading")
    val loadingAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_anim"
    )

    // Màu sắc chủ đạo theo thiết kế
    val primaryBgColor = Color(0xFF14382C)       // Xanh rêu/rừng đậm
    val ambientShapeTop = Color(0xFF224E3E)      // Mảng cong góc trên
    val ambientShapeBottom = Color(0xFF1C4537)   // Mảng cong góc dưới

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(primaryBgColor)
    ) {
        // 1. Họa tiết nền: Các mảng cong mềm mại tạo chiều sâu
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Mảng cong lớn góc trên bên phải
            drawCircle(
                color = ambientShapeTop,
                radius = width * 0.52f,
                center = Offset(x = width * 0.95f, y = height * 0.08f)
            )

            // Mảng cong mềm mại góc dưới bên trái
            drawCircle(
                color = ambientShapeBottom,
                radius = width * 0.60f,
                center = Offset(x = width * 0.15f, y = height * 0.88f)
            )
        }

        // 2. Nội dung chính toàn màn hình
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Khoảng đệm phía trên
            Spacer(modifier = Modifier.height(20.dp))

            // ====================== PHẦN 1: HEADER & THƯƠNG HIỆU ======================
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Biểu tượng ứng dụng (Squircle với mầm cây vươn lên)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = Color.Black.copy(alpha = 0.25f)
                        )
                        .background(
                            color = Color(0xFF9FB2A9),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    SproutIcon(modifier = Modifier.size(38.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tên ứng dụng
                Text(
                    text = "SpendTogether",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Slogan / Phụ đề
                Text(
                    text = "Cùng góp quỹ, cùng chia sẻ — rõ ràng trong\ntừng khoản chi.",
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFFCBDCD3),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====================== PHẦN 2: THẺ NGHỆ THUẬT TRUNG TÂM ======================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(390.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(32.dp),
                        ambientColor = Color.Black.copy(alpha = 0.25f),
                        spotColor = Color.Black.copy(alpha = 0.35f)
                    )
                    .background(
                        color = Color(0xFFBACAC3), // Màu xám bạc/sage pastel
                        shape = RoundedCornerShape(32.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Bức tranh nghệ thuật 3D Paper-cut bên trong
                Box(
                    modifier = Modifier
                        .width(210.dp)
                        .height(310.dp)
                        .offset(y = (-14).dp)
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(18.dp),
                            spotColor = Color(0xFF2C4A3E).copy(alpha = 0.35f)
                        )
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFE4EEE8))
                ) {
                    PaperCutArtwork(modifier = Modifier.fillMaxSize())
                }

                // Hộp thoại con nhộng nổi (Floating Capsule Pill)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-20).dp)
                        .fillMaxWidth(0.86f)
                        .shadow(
                            elevation = 10.dp,
                            shape = CircleShape,
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .background(
                            color = Color(0xFFEDF3EF).copy(alpha = 0.94f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.85f),
                            shape = CircleShape
                        )
                        .padding(horizontal = 18.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mọi người đều thấy. Mọi khoản đều\nrõ.",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF14382C),
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ====================== PHẦN 3: FOOTER DÒNG TRẠNG THÁI ======================
            Box(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .alpha(loadingAlpha),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Đang chuẩn bị không gian chung...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF8CA599),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Biểu tượng chiếc lá / mầm cây vươn lên bằng nét vẽ Canvas vector mảnh, thanh lịch
 */
@Composable
private fun SproutIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeColor = Color.White
        val strokeStyle = Stroke(
            width = 2.4.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )

        // Đường gạch ngang nhỏ ở chân mầm cây
        drawLine(
            color = strokeColor,
            start = Offset(w * 0.32f, h * 0.80f),
            end = Offset(w * 0.68f, h * 0.80f),
            strokeWidth = 2.4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Thân cây vươn thẳng lên
        val stemPath = Path().apply {
            moveTo(w * 0.50f, h * 0.80f)
            cubicTo(
                w * 0.50f, h * 0.55f,
                w * 0.49f, h * 0.40f,
                w * 0.49f, h * 0.28f
            )
        }
        drawPath(path = stemPath, color = strokeColor, style = strokeStyle)

        // Nhánh lá bên trái (vươn cong mềm mại)
        val leftLeafPath = Path().apply {
            moveTo(w * 0.50f, h * 0.55f)
            cubicTo(
                w * 0.30f, h * 0.52f,
                w * 0.28f, h * 0.38f,
                w * 0.46f, h * 0.38f
            )
            cubicTo(
                w * 0.50f, h * 0.44f,
                w * 0.50f, h * 0.50f,
                w * 0.50f, h * 0.55f
            )
        }
        drawPath(path = leftLeafPath, color = strokeColor, style = strokeStyle)

        // Nhánh lá bên phải cao hơn (tạo thành vòng tròn lá khép nhẹ)
        val rightLeafPath = Path().apply {
            moveTo(w * 0.49f, h * 0.38f)
            cubicTo(
                w * 0.58f, h * 0.35f,
                w * 0.72f, h * 0.20f,
                w * 0.64f, h * 0.16f
            )
            cubicTo(
                w * 0.52f, h * 0.14f,
                w * 0.46f, h * 0.26f,
                w * 0.49f, h * 0.38f
            )
        }
        drawPath(path = rightLeafPath, color = strokeColor, style = strokeStyle)
    }
}

/**
 * Tranh minh họa nghệ thuật Paper-cut 3D thiên nhiên
 * Gồm mặt trời nổi, đồi núi uốn lượn, bóng cây và người ngồi dã ngoại
 */
@Composable
private fun PaperCutArtwork(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Nền trời xanh bạc dịu mát
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE5EDE8), Color(0xFFD6E3DC))
            ),
            size = size
        )

        // 1. Mặt trời 3D nổi với hiệu ứng bóng mềm
        drawCircle(
            color = Color(0xFFD2DFD8).copy(alpha = 0.5f),
            radius = w * 0.17f,
            center = Offset(w * 0.52f, h * 0.23f + 4.dp.toPx())
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF7E6), Color(0xFFEADCC6)),
                center = Offset(w * 0.48f, h * 0.20f),
                radius = w * 0.17f
            ),
            radius = w * 0.16f,
            center = Offset(w * 0.52f, h * 0.23f)
        )

        // 2. Dãy đồi phía xa (lớp xa nhất - màu pastel nhạt)
        val distantHill = Path().apply {
            moveTo(0f, h * 0.42f)
            cubicTo(w * 0.25f, h * 0.36f, w * 0.75f, h * 0.40f, w, h * 0.33f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = distantHill, color = Color(0xFFB5C9BD))

        // 3. Rặng thông phía xa bên phải
        for (i in 0..4) {
            val pineX = w * (0.68f + i * 0.07f)
            val pineY = h * 0.36f
            val pinePath = Path().apply {
                moveTo(pineX, pineY - (18 + i * 4).dp.toPx())
                lineTo(pineX - 7.dp.toPx(), pineY + 12.dp.toPx())
                lineTo(pineX + 7.dp.toPx(), pineY + 12.dp.toPx())
                close()
            }
            drawPath(path = pinePath, color = Color(0xFF4C6D5B))
        }

        // 4. Lớp đồi trung tâm (uốn lượn từ trái sang phải)
        val midHill = Path().apply {
            moveTo(0f, h * 0.54f)
            cubicTo(w * 0.35f, h * 0.46f, w * 0.65f, h * 0.58f, w, h * 0.48f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = midHill, color = Color(0xFF8BA999))

        // 5. Cây lớn bên trái phong cách Paper-cut
        drawCircle(
            color = Color(0xFF5A7D6B),
            radius = w * 0.14f,
            center = Offset(w * 0.28f, h * 0.46f)
        )
        drawCircle(
            color = Color(0xFF759784),
            radius = w * 0.10f,
            center = Offset(w * 0.26f, h * 0.44f)
        )
        // Thân cây
        drawLine(
            color = Color(0xFF466354),
            start = Offset(w * 0.28f, h * 0.48f),
            end = Offset(w * 0.28f, h * 0.62f),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 6. Nhánh lá nhỏ trang trí lượn ở góc trái
        for (j in 0..3) {
            val leafY = h * (0.24f + j * 0.08f)
            drawCircle(
                color = Color(0xFF547866),
                radius = 5.dp.toPx(),
                center = Offset(w * 0.12f, leafY)
            )
        }

        // 7. Con đường mòn uốn lượn về phía trước
        val pathRoad = Path().apply {
            moveTo(w * 0.48f, h * 0.54f)
            cubicTo(w * 0.42f, h * 0.64f, w * 0.58f, h * 0.76f, w * 0.50f, h)
            lineTo(w * 0.62f, h)
            cubicTo(w * 0.70f, h * 0.76f, w * 0.50f, h * 0.64f, w * 0.54f, h * 0.54f)
            close()
        }
        drawPath(path = pathRoad, color = Color(0xFFD3E0D6))

        // 8. Đồi tiền cảnh phía trước (Foreground Hill)
        val foreHill = Path().apply {
            moveTo(0f, h * 0.64f)
            cubicTo(w * 0.40f, h * 0.74f, w * 0.70f, h * 0.68f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = foreHill, color = Color(0xFF6B8F7D))

        // 9. Bàn dã ngoại bằng gỗ và hình người ngồi dưới gốc cây
        // Mặt bàn
        drawRoundRect(
            color = Color(0xFFC79E72),
            topLeft = Offset(w * 0.32f, h * 0.61f),
            size = Size(w * 0.18f, 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx())
        )
        // Ghế/chân bàn
        drawLine(
            color = Color(0xFFA67B52),
            start = Offset(w * 0.35f, h * 0.61f),
            end = Offset(w * 0.33f, h * 0.65f),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color(0xFFA67B52),
            start = Offset(w * 0.47f, h * 0.61f),
            end = Offset(w * 0.49f, h * 0.65f),
            strokeWidth = 2.dp.toPx()
        )
        // Người ngồi (chấm tròn đầu & thân áo trắng dã ngoại)
        drawCircle(
            color = Color(0xFFEFE8DB),
            radius = 3.5.dp.toPx(),
            center = Offset(w * 0.44f, h * 0.585f)
        )
        drawRoundRect(
            color = Color(0xFF3B5949),
            topLeft = Offset(w * 0.42f, h * 0.60f),
            size = Size(6.dp.toPx(), 8.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.dp.toPx())
        )

        // 10. Những đóa hoa nhỏ li ti trên thảm cỏ
        val flowerPoints = listOf(
            Offset(w * 0.72f, h * 0.68f),
            Offset(w * 0.78f, h * 0.72f),
            Offset(w * 0.85f, h * 0.70f),
            Offset(w * 0.20f, h * 0.78f),
            Offset(w * 0.28f, h * 0.82f)
        )
        for (pt in flowerPoints) {
            drawCircle(color = Color(0xFFFFF7ED), radius = 2.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFFE2B679), radius = 1.dp.toPx(), center = pt)
        }
    }
}

/**
 * Preview hiển thị chuẩn xác giao diện điện thoại trong Android Studio
 */
@Preview(
    name = "01 · Màn hình chào (SpendTogether)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun WelcomeScreenPreview() {
    Nhoms8Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WelcomeScreen()
        }
    }
}
