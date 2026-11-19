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
import androidx.compose.ui.graphics.Path
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

/**
 * 02 · Màn hình Hướng dẫn 1 (Onboarding Screen 1: Giới thiệu quản lý chi tiêu)
 * Ứng dụng Spendly
 */
@Composable
fun OnboardingScreen1(
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
                        TeamFinanceArtwork(
                            isDark = colors.isDark,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.92f)
                            .spendlyGlassSurface(
                                hazeState = hazeState,
                                shape = SpendlyShapeTokens.Pill,
                                tintColor = colors.surface.copy(alpha = if (colors.isDark) 0.85f else 0.92f)
                            )
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Một quỹ chung, không còn khoản chi thất lạc",
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
                    text = "QUẢN LÝ DỄ DÀNG",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.4.sp
                    ),
                    color = colors.deepLavender,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Quản lý chi tiêu nhóm thật nhẹ nhàng",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Ghi lại mọi khoản thu chi, biết quỹ còn bao\nnhiêu và ai đã đóng góp.",
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
private fun TeamFinanceArtwork(
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF28213B), Color(0xFF1E2235))
                } else {
                    listOf(Color(0xFFF3EEFC), Color(0xFFE7F2F9))
                }
            ),
            size = size
        )

        // Window frame
        val windowLeft = w * 0.20f
        val windowTop = h * 0.08f
        val windowWidth = w * 0.60f
        val windowHeight = h * 0.48f

        drawRect(
            brush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF332D52), Color(0xFF26404A))
                } else {
                    listOf(Color(0xFFE2DAF7), Color(0xFFD6EEF5))
                },
                startY = windowTop,
                endY = windowTop + windowHeight
            ),
            topLeft = Offset(windowLeft, windowTop),
            size = Size(windowWidth, windowHeight)
        )

        val ridgeColors = if (isDark) {
            listOf(Color(0xFF4B3D78), Color(0xFF3A675D), Color(0xFF2A4D46))
        } else {
            listOf(Color(0xFFC5B4EE), SpendlyMint.copy(alpha = 0.85f), SpendlyMintDeep.copy(alpha = 0.75f))
        }
        for (i in 0..2) {
            val terracePath = Path().apply {
                val startY = windowTop + windowHeight * (0.45f + i * 0.17f)
                moveTo(windowLeft, startY)
                cubicTo(
                    windowLeft + windowWidth * 0.35f, startY - 12.dp.toPx(),
                    windowLeft + windowWidth * 0.70f, startY + 10.dp.toPx(),
                    windowLeft + windowWidth, startY - 4.dp.toPx()
                )
                lineTo(windowLeft + windowWidth, windowTop + windowHeight)
                lineTo(windowLeft, windowTop + windowHeight)
                close()
            }
            drawPath(terracePath, color = ridgeColors[i])
        }

        drawRect(
            color = if (isDark) Color(0xFF6B58C4) else Color.White,
            topLeft = Offset(windowLeft, windowTop),
            size = Size(windowWidth, windowHeight),
            style = Stroke(width = 3.5.dp.toPx())
        )

        // Table
        val tableTopY = h * 0.56f
        drawRoundRect(
            color = if (isDark) Color(0xFF4C3D70) else Color(0xFFDCCFF6),
            topLeft = Offset(w * 0.14f, tableTopY),
            size = Size(w * 0.72f, 16.dp.toPx()),
            cornerRadius = CornerRadius(6.dp.toPx())
        )
        drawRoundRect(
            color = if (isDark) Color(0xFF372C53) else Color(0xFFC7B5EB),
            topLeft = Offset(w * 0.22f, tableTopY + 16.dp.toPx()),
            size = Size(10.dp.toPx(), h * 0.26f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )
        drawRoundRect(
            color = if (isDark) Color(0xFF372C53) else Color(0xFFC7B5EB),
            topLeft = Offset(w * 0.72f, tableTopY + 16.dp.toPx()),
            size = Size(10.dp.toPx(), h * 0.26f),
            cornerRadius = CornerRadius(3.dp.toPx())
        )

        // Three teammates
        // Member 1 (Lavender shirt)
        drawCircle(color = Color(0xFF3B271E), radius = 14.dp.toPx(), center = Offset(w * 0.30f, h * 0.37f))
        drawCircle(color = Color(0xFFF6D6BD), radius = 11.dp.toPx(), center = Offset(w * 0.31f, h * 0.385f))
        drawRoundRect(
            color = SpendlyLavender,
            topLeft = Offset(w * 0.21f, h * 0.44f),
            size = Size(w * 0.19f, h * 0.14f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Member 2 (Mint shirt)
        drawCircle(color = Color(0xFF4A3226), radius = 15.dp.toPx(), center = Offset(w * 0.51f, h * 0.35f))
        drawCircle(color = Color(0xFFFBE0CC), radius = 12.dp.toPx(), center = Offset(w * 0.51f, h * 0.365f))
        drawRoundRect(
            color = SpendlyMint,
            topLeft = Offset(w * 0.42f, h * 0.43f),
            size = Size(w * 0.18f, h * 0.15f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Member 3 (Sky shirt)
        drawCircle(color = Color(0xFF2E1F18), radius = 14.dp.toPx(), center = Offset(w * 0.70f, h * 0.37f))
        drawCircle(color = Color(0xFFF6D6BD), radius = 11.dp.toPx(), center = Offset(w * 0.69f, h * 0.385f))
        drawRoundRect(
            color = SpendlySky,
            topLeft = Offset(w * 0.61f, h * 0.44f),
            size = Size(w * 0.19f, h * 0.14f),
            cornerRadius = CornerRadius(8.dp.toPx())
        )

        // Tablet with pie chart on table
        drawRoundRect(
            color = SpendlyLavenderDeep,
            topLeft = Offset(w * 0.37f, tableTopY - 14.dp.toPx()),
            size = Size(38.dp.toPx(), 26.dp.toPx()),
            cornerRadius = CornerRadius(4.dp.toPx())
        )
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.37f + 2.dp.toPx(), tableTopY - 12.dp.toPx()),
            size = Size(34.dp.toPx(), 22.dp.toPx()),
            cornerRadius = CornerRadius(2.dp.toPx())
        )
        drawArc(
            color = SpendlyMintDeep,
            startAngle = 0f,
            sweepAngle = 210f,
            useCenter = true,
            topLeft = Offset(w * 0.40f, tableTopY - 9.dp.toPx()),
            size = Size(12.dp.toPx(), 12.dp.toPx())
        )
        drawArc(
            color = SpendlyLavender,
            startAngle = 210f,
            sweepAngle = 150f,
            useCenter = true,
            topLeft = Offset(w * 0.40f, tableTopY - 9.dp.toPx()),
            size = Size(12.dp.toPx(), 12.dp.toPx())
        )

        // Gold coins & notebook
        drawCircle(color = SpendlyAmber, radius = 4.dp.toPx(), center = Offset(w * 0.56f, tableTopY + 5.dp.toPx()))
        drawCircle(color = SpendlyIndigo, radius = 4.dp.toPx(), center = Offset(w * 0.60f, tableTopY + 5.dp.toPx()))
    }
}

@Preview(
    name = "02 · Onboarding 1 Spendly (Light)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen1Preview() {
    SpendlyTheme(darkTheme = false) {
        OnboardingScreen1()
    }
}

@Preview(
    name = "02 · Onboarding 1 Spendly (Dark)",
    showBackground = true,
    showSystemUi = true
)
@Composable
fun OnboardingScreen1DarkPreview() {
    SpendlyTheme(darkTheme = true) {
        OnboardingScreen1()
    }
}
