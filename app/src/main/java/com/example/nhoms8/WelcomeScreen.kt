package com.example.nhoms8

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * 01 · Màn hình Chào mừng (Splash / Welcome Screen)
 * Ứng dụng Spendly - Quản lý chi tiêu cá nhân & nhóm
 */
@Composable
fun WelcomeScreen() {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

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

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. BRAND HEADER
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.spendly_app_logo),
                    contentDescription = "Spendly Logo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(92.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Spendly",
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Cùng góp quỹ, cùng chia sẻ — rõ ràng trong\ntừng khoản chi.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }

            // 2. CENTER ARTWORK GLASS CARD
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
                hazeState = hazeState,
                shape = SpendlyShapeTokens.HeroCard,
                contentPadding = PaddingValues(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 26.dp)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        LavenderPaperCutArtwork(
                            isDark = colors.isDark,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.90f)
                            .spendlyGlassSurface(
                                hazeState = hazeState,
                                shape = SpendlyShapeTokens.Pill,
                                tintColor = colors.surface.copy(alpha = if (colors.isDark) 0.85f else 0.92f)
                            )
                            .padding(horizontal = 18.dp, vertical = 11.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Mọi người đều thấy. Mọi khoản đều rõ.",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 3. FOOTER STATUS
            Column(
                modifier = Modifier
                    .padding(bottom = 24.dp)
                    .alpha(loadingAlpha),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Đang chuẩn bị không gian chung...",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Chạm để bắt đầu",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.deepLavender,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LavenderPaperCutArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val skyTop = if (isDark) Color(0xFF28223D) else Color(0xFFEDE6FA)
        val skyBottom = if (isDark) Color(0xFF1C2B3B) else Color(0xFFDCEEF8)
        drawRect(
            brush = Brush.verticalGradient(colors = listOf(skyTop, skyBottom)),
            size = size
        )

        // Glowing sun / moon orb
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0xFFFFF8E8),
                    if (isDark) Color(0xFFB39DF0).copy(alpha = 0.65f) else Color(0xFFF3D5FF)
                ),
                center = Offset(w * 0.50f, h * 0.22f),
                radius = w * 0.16f
            ),
            radius = w * 0.14f,
            center = Offset(w * 0.50f, h * 0.23f)
        )

        // Distant lavender ridge
        val distantHill = Path().apply {
            moveTo(0f, h * 0.45f)
            cubicTo(w * 0.28f, h * 0.36f, w * 0.70f, h * 0.43f, w, h * 0.35f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = distantHill,
            color = if (isDark) Color(0xFF433668) else Color(0xFFCBBBF2)
        )

        // Mid ridge in indigo-lavender
        val midHill = Path().apply {
            moveTo(0f, h * 0.56f)
            cubicTo(w * 0.34f, h * 0.47f, w * 0.66f, h * 0.60f, w, h * 0.50f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = midHill,
            color = if (isDark) Color(0xFF5A498C) else SpendlyLavender.copy(alpha = 0.72f)
        )

        // Winding sky-mint path
        val pathRoad = Path().apply {
            moveTo(w * 0.48f, h * 0.55f)
            cubicTo(w * 0.42f, h * 0.66f, w * 0.58f, h * 0.78f, w * 0.49f, h)
            lineTo(w * 0.61f, h)
            cubicTo(w * 0.70f, h * 0.78f, w * 0.50f, h * 0.66f, w * 0.54f, h * 0.55f)
            close()
        }
        drawPath(
            path = pathRoad,
            color = if (isDark) Color(0xFF3E6A60) else Color(0xFFD6F2E8)
        )

        // Foreground mint hill
        val foreHill = Path().apply {
            moveTo(0f, h * 0.66f)
            cubicTo(w * 0.38f, h * 0.74f, w * 0.72f, h * 0.68f, w, h * 0.78f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(
            path = foreHill,
            color = if (isDark) Color(0xFF285549) else Color(0xFF8BCDB5)
        )

        // Stylised lavender tree on left
        drawLine(
            color = if (isDark) Color(0xFF9E8BD6) else SpendlyLavenderDeep,
            start = Offset(w * 0.25f, h * 0.48f),
            end = Offset(w * 0.25f, h * 0.66f),
            strokeWidth = 3.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(
            color = if (isDark) Color(0xFF7B65BA) else SpendlyIndigo,
            radius = w * 0.11f,
            center = Offset(w * 0.25f, h * 0.45f)
        )
        drawCircle(
            color = if (isDark) Color(0xFF9C86DF) else SpendlySky,
            radius = w * 0.08f,
            center = Offset(w * 0.23f, h * 0.43f)
        )

        // Floating wallet / coin tokens on meadow
        drawRoundRect(
            color = Color.White.copy(alpha = if (isDark) 0.20f else 0.72f),
            topLeft = Offset(w * 0.62f, h * 0.58f),
            size = Size(44.dp.toPx(), 28.dp.toPx()),
            cornerRadius = CornerRadius(10.dp.toPx())
        )
        drawCircle(
            color = Color(0xFFF3B75E),
            radius = 7.dp.toPx(),
            center = Offset(w * 0.74f, h * 0.62f)
        )
    }
}

@Preview(
    name = "01 · Màn hình chào Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun WelcomeScreenPreview() {
    SpendlyTheme(darkTheme = false) {
        WelcomeScreen()
    }
}

@Preview(
    name = "01 · Màn hình chào Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun WelcomeScreenDarkPreview() {
    SpendlyTheme(darkTheme = true) {
        WelcomeScreen()
    }
}
