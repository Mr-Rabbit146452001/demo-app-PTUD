package com.example.nhoms8

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhoms8.core.designsystem.GlassCard
import com.example.nhoms8.core.designsystem.OrganicBackground
import com.example.nhoms8.core.designsystem.SpendlyButton
import com.example.nhoms8.core.designsystem.spendlyGlassSurface
import com.example.nhoms8.ui.theme.SpendlyAmber
import com.example.nhoms8.ui.theme.SpendlyCoral
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyMint
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState

/**
 * 04 · Màn hình Hướng dẫn 3 (Onboarding Screen 3: Báo cáo trực quan)
 * Ứng dụng Spendly
 */
@Composable
fun OnboardingScreen3(
    onSkipClicked: () -> Unit = {},
    onGetStartedClicked: () -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val hazeState = rememberHazeState()

    OrganicBackground(
        modifier = Modifier.fillMaxSize(),
        hazeState = hazeState
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .background(colors.lavenderContainer)
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "Spendly",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = colors.onLavenderContainer
                    )
                }

                Text(
                    text = "Bỏ qua",
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.deepLavender,
                    modifier = Modifier
                        .clip(SpendlyShapeTokens.Pill)
                        .clickable(role = Role.Button, onClick = onSkipClicked)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            // ARTWORK GLASS CARD
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(355.dp),
                hazeState = hazeState,
                shape = SpendlyShapeTokens.HeroCard,
                contentPadding = PaddingValues(14.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        VisualAnalyticsArtwork(
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nhìn là hiểu quỹ đang đi về đâu",
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // TEXT BLOCK
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 6.dp)
            ) {
                Text(
                    text = "BÁO CÁO TRỰC QUAN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = colors.deepLavender,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Thói quen chi tiêu trong một ánh nhìn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Biểu đồ rõ ràng giúp bạn và cả nhóm điều chỉnh\nngân sách đúng lúc.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center
                )
            }

            // FOOTER: DOTS + PRIMARY BUTTON
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.divider)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.divider)
                    )
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(6.dp)
                            .clip(SpendlyShapeTokens.Pill)
                            .background(colors.deepLavender)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                SpendlyButton(
                    text = "Bắt đầu ngay",
                    onClick = onGetStartedClicked,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = Icons.Rounded.AutoAwesome,
                    height = 52.dp
                )
            }
        }
    }
}

@Composable
private fun VisualAnalyticsArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF28203B), Color(0xFF1D2436))
                } else {
                    listOf(Color(0xFFF3EEFC), Color(0xFFE7F5F1))
                }
            ),
            size = size
        )

        // Sparkle stars
        val sparkles = listOf(
            Offset(w * 0.14f, h * 0.16f),
            Offset(w * 0.85f, h * 0.18f),
            Offset(w * 0.13f, h * 0.48f),
            Offset(w * 0.87f, h * 0.45f),
            Offset(w * 0.17f, h * 0.74f),
            Offset(w * 0.84f, h * 0.72f)
        )
        for (pt in sparkles) {
            drawCircle(color = SpendlyLavender.copy(alpha = 0.28f), radius = 8.dp.toPx(), center = pt)
            drawCircle(color = Color.White.copy(alpha = 0.90f), radius = 3.5.dp.toPx(), center = pt)
        }

        // Center analytics plinth card
        val plinthWidth = w * 0.58f
        val plinthHeight = h * 0.76f
        val plinthLeft = (w - plinthWidth) / 2f
        val plinthTop = h * 0.11f

        drawRoundRect(
            color = if (isDark) Color(0xFF15121E).copy(alpha = 0.5f) else SpendlyLavender.copy(alpha = 0.16f),
            topLeft = Offset(plinthLeft, plinthTop + 6.dp.toPx()),
            size = Size(plinthWidth, plinthHeight),
            cornerRadius = CornerRadius(26.dp.toPx())
        )
        drawRoundRect(
            color = if (isDark) Color(0xFF2B243B) else Color.White,
            topLeft = Offset(plinthLeft, plinthTop),
            size = Size(plinthWidth, plinthHeight),
            cornerRadius = CornerRadius(26.dp.toPx())
        )

        // Donut chart on top half
        val pieCenter = Offset(w * 0.50f, h * 0.32f)
        val pieRadius = w * 0.17f
        val ringStroke = 14.dp.toPx()

        drawArc(
            color = SpendlyLavenderDeep,
            startAngle = -90f,
            sweepAngle = 165f,
            useCenter = false,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2),
            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = SpendlyMintDeep,
            startAngle = 90f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2),
            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )
        drawArc(
            color = SpendlySky,
            startAngle = 210f,
            sweepAngle = 50f,
            useCenter = false,
            topLeft = Offset(pieCenter.x - pieRadius, pieCenter.y - pieRadius),
            size = Size(pieRadius * 2, pieRadius * 2),
            style = Stroke(width = ringStroke, cap = StrokeCap.Round)
        )

        // Bar columns + smooth trend line on bottom half
        val barBaseY = h * 0.76f
        val barWidths = w * 0.08f
        val bars = listOf(
            Triple(w * 0.31f, h * 0.11f, SpendlySky),
            Triple(w * 0.42f, h * 0.17f, SpendlyMint),
            Triple(w * 0.53f, h * 0.14f, SpendlyAmber),
            Triple(w * 0.64f, h * 0.22f, SpendlyLavender)
        )
        bars.forEach { (x, barH, clr) ->
            drawRoundRect(
                color = clr,
                topLeft = Offset(x, barBaseY - barH),
                size = Size(barWidths, barH),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
        }

        val trendPath = Path().apply {
            moveTo(w * 0.35f, barBaseY - h * 0.13f)
            cubicTo(
                w * 0.44f, barBaseY - h * 0.21f,
                w * 0.54f, barBaseY - h * 0.15f,
                w * 0.68f, barBaseY - h * 0.25f
            )
        }
        drawPath(
            path = trendPath,
            color = SpendlyCoral,
            style = Stroke(width = 2.8.dp.toPx(), cap = StrokeCap.Round)
        )

        // Outer glowing frame
        val frameW = w * 0.66f
        val frameH = h * 0.74f
        drawRoundRect(
            color = if (isDark) SpendlyLavender.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.85f),
            topLeft = Offset((w - frameW) / 2f, h * 0.12f),
            size = Size(frameW, frameH),
            cornerRadius = CornerRadius(28.dp.toPx()),
            style = Stroke(width = 3.dp.toPx())
        )
    }
}

@Preview(
    name = "04 · Onboarding 3 Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen3Preview() {
    SpendlyTheme(darkTheme = false) {
        OnboardingScreen3()
    }
}

@Preview(
    name = "04 · Onboarding 3 Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen3DarkPreview() {
    SpendlyTheme(darkTheme = true) {
        OnboardingScreen3()
    }
}
