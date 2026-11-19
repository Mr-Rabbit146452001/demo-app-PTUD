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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyMint
import com.example.nhoms8.ui.theme.SpendlyMintDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky
import com.example.nhoms8.ui.theme.SpendlyTheme
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.rememberHazeState
import kotlin.math.cos
import kotlin.math.sin

/**
 * 03 · Màn hình Hướng dẫn 2 (Onboarding Screen 2: Giới thiệu chia hóa đơn)
 * Ứng dụng Spendly
 */
@Composable
fun OnboardingScreen2(
    onSkipClicked: () -> Unit = {},
    onContinueClicked: () -> Unit = {}
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
                        BillSplitArtwork(
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
                            text = "Chia đúng phần, không còn tính nhầm",
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
                    text = "CHIA HÓA ĐƠN",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = colors.deepLavender,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Chia tiền công bằng theo cách của bạn",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Chia đều, theo tỷ lệ hoặc nhập chính xác số tiền\ncho từng người.",
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
                            .width(24.dp)
                            .height(6.dp)
                            .clip(SpendlyShapeTokens.Pill)
                            .background(colors.deepLavender)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(colors.divider)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                SpendlyButton(
                    text = "Tiếp tục",
                    onClick = onContinueClicked,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward,
                    height = 52.dp
                )
            }
        }
    }
}

@Composable
private fun BillSplitArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF251F38), Color(0xFF1A2535))
                } else {
                    listOf(Color(0xFFEDE6FA), Color(0xFFE3F4F0))
                }
            ),
            size = size
        )

        val ringColors = if (isDark) {
            listOf(Color(0xFF534085), Color(0xFF336A5C), Color(0xFF2E5474), Color(0xFF6B58C4))
        } else {
            listOf(SpendlyLavender.copy(alpha = 0.45f), SpendlyMint.copy(alpha = 0.65f), SpendlySky.copy(alpha = 0.55f), Color(0xFFD6C8F8))
        }
        for (i in 0..15) {
            val angle = Math.toRadians(i * (360.0 / 16.0))
            val leafX = (w * 0.50f + cos(angle) * (w * 0.43f)).toFloat()
            val leafY = (h * 0.50f + sin(angle) * (h * 0.43f)).toFloat()
            drawCircle(
                color = ringColors[i % ringColors.size],
                radius = (12 + (i % 4) * 3).dp.toPx(),
                center = Offset(leafX, leafY)
            )
        }

        // Round table in center
        val tableCenter = Offset(w * 0.50f, h * 0.50f)
        val tableRadius = w * 0.36f
        drawCircle(
            color = if (isDark) Color(0xFF423663) else Color(0xFFE2D8F7),
            radius = tableRadius + 6.dp.toPx(),
            center = tableCenter
        )
        drawCircle(
            color = if (isDark) Color(0xFF2C2542) else Color.White,
            radius = tableRadius,
            center = tableCenter
        )

        // Bill slip in center
        val billLeft = w * 0.40f
        val billTop = h * 0.34f
        val billWidth = w * 0.20f
        val billHeight = h * 0.28f
        drawRoundRect(
            color = if (isDark) Color(0xFF3C335A) else Color(0xFFF6F1FD),
            topLeft = Offset(billLeft, billTop),
            size = Size(billWidth, billHeight),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawLine(
            color = SpendlyLavenderDeep,
            start = Offset(billLeft + 8.dp.toPx(), billTop + 10.dp.toPx()),
            end = Offset(billLeft + billWidth - 8.dp.toPx(), billTop + 10.dp.toPx()),
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        for (k in 0..3) {
            val lineY = billTop + 20.dp.toPx() + k * 9.dp.toPx()
            drawLine(
                color = if (isDark) Color(0xFF9C8CE0) else SpendlyLavender.copy(alpha = 0.65f),
                start = Offset(billLeft + 8.dp.toPx(), lineY),
                end = Offset(billLeft + billWidth - 8.dp.toPx(), lineY),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Split nodes around the bill (Equal / % / Custom)
        val nodes = listOf(
            Triple(Offset(w * 0.26f, h * 0.34f), SpendlyLavender, 14.dp.toPx()),
            Triple(Offset(w * 0.74f, h * 0.34f), SpendlyMintDeep, 14.dp.toPx()),
            Triple(Offset(w * 0.26f, h * 0.66f), SpendlySky, 14.dp.toPx()),
            Triple(Offset(w * 0.74f, h * 0.66f), SpendlyCoral, 14.dp.toPx())
        )
        nodes.forEach { (pt, clr, rad) ->
            drawLine(
                color = clr.copy(alpha = 0.55f),
                start = tableCenter,
                end = pt,
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(color = clr, radius = rad, center = pt)
            drawCircle(color = Color.White.copy(alpha = 0.85f), radius = rad * 0.45f, center = pt)
        }

        // Scanner frame highlight
        val scannerW = w * 0.54f
        val scannerH = h * 0.72f
        drawRoundRect(
            color = if (isDark) Color.White.copy(alpha = 0.55f) else Color.White,
            topLeft = Offset((w - scannerW) / 2f, h * 0.14f),
            size = Size(scannerW, scannerH),
            cornerRadius = CornerRadius(28.dp.toPx()),
            style = Stroke(width = 3.5.dp.toPx())
        )

        // Accent coins
        drawCircle(color = SpendlyAmber, radius = 5.dp.toPx(), center = Offset(w * 0.40f, h * 0.25f))
        drawCircle(color = SpendlyIndigo, radius = 4.dp.toPx(), center = Offset(w * 0.62f, h * 0.25f))
    }
}

@Preview(
    name = "03 · Onboarding 2 Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen2Preview() {
    SpendlyTheme(darkTheme = false) {
        OnboardingScreen2()
    }
}

@Preview(
    name = "03 · Onboarding 2 Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen2DarkPreview() {
    SpendlyTheme(darkTheme = true) {
        OnboardingScreen2()
    }
}
