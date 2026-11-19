package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource

/**
 * Spendly organic pastel backdrop with soft Lavender, Mint, and Sky cubic-bezier blobs.
 *
 * When [hazeState] is provided, [Modifier.hazeSource] is attached exclusively to the backdrop
 * canvas layer so foreground [GlassCard] and [SpendlyBottomBar] siblings can blur it cleanly
 * without self-inclusion.
 */
@Composable
fun OrganicBackground(
    modifier: Modifier = Modifier.fillMaxSize(),
    hazeState: HazeState? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = MaterialTheme.spendlyColors
    val backdropBase = colors.background
    val washBottom = if (colors.isDark) {
        Color(0xFF191626)
    } else {
        Color(0xFFEDF4FA)
    }
    val blobLavender = colors.blobLavender
    val blobMint = colors.blobMint
    val blobSky = colors.blobSky

    Box(modifier = modifier) {
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .then(if (hazeState != null) Modifier.hazeSource(state = hazeState) else Modifier)
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    val baseBrush = Brush.verticalGradient(
                        colors = listOf(backdropBase, backdropBase, washBottom)
                    )

                    val topLavenderPath = Path().apply {
                        moveTo(w * 0.25f, 0f)
                        lineTo(w, 0f)
                        lineTo(w, h * 0.34f)
                        cubicTo(
                            w * 0.80f, h * 0.42f,
                            w * 0.56f, h * 0.24f,
                            w * 0.34f, h * 0.17f
                        )
                        cubicTo(
                            w * 0.20f, h * 0.13f,
                            w * 0.18f, h * 0.04f,
                            w * 0.25f, 0f
                        )
                        close()
                    }

                    val midMintPath = Path().apply {
                        moveTo(0f, h * 0.26f)
                        cubicTo(
                            w * 0.24f, h * 0.22f,
                            w * 0.42f, h * 0.36f,
                            w * 0.34f, h * 0.52f
                        )
                        cubicTo(
                            w * 0.26f, h * 0.66f,
                            w * 0.08f, h * 0.64f,
                            0f, h * 0.68f
                        )
                        close()
                    }

                    val bottomSkyPath = Path().apply {
                        moveTo(w * 0.32f, h)
                        cubicTo(
                            w * 0.40f, h * 0.78f,
                            w * 0.68f, h * 0.68f,
                            w, h * 0.72f
                        )
                        lineTo(w, h)
                        close()
                    }

                    val topLavenderGlow = Brush.radialGradient(
                        colors = listOf(blobLavender, blobLavender.copy(alpha = 0f)),
                        center = Offset(w * 0.82f, h * 0.14f),
                        radius = w * 0.72f
                    )

                    val midMintGlow = Brush.radialGradient(
                        colors = listOf(blobMint, blobMint.copy(alpha = 0f)),
                        center = Offset(w * 0.16f, h * 0.48f),
                        radius = w * 0.62f
                    )

                    val bottomSkyGlow = Brush.radialGradient(
                        colors = listOf(blobSky, blobSky.copy(alpha = 0f)),
                        center = Offset(w * 0.78f, h * 0.86f),
                        radius = w * 0.68f
                    )

                    onDrawBehind {
                        drawRect(brush = baseBrush)
                        drawPath(path = topLavenderPath, color = blobLavender.copy(alpha = blobLavender.alpha * 0.65f))
                        drawPath(path = midMintPath, color = blobMint.copy(alpha = blobMint.alpha * 0.60f))
                        drawPath(path = bottomSkyPath, color = blobSky.copy(alpha = blobSky.alpha * 0.65f))

                        drawCircle(
                            brush = topLavenderGlow,
                            radius = w * 0.72f,
                            center = Offset(w * 0.82f, h * 0.14f)
                        )
                        drawCircle(
                            brush = midMintGlow,
                            radius = w * 0.62f,
                            center = Offset(w * 0.16f, h * 0.48f)
                        )
                        drawCircle(
                            brush = bottomSkyGlow,
                            radius = w * 0.68f,
                            center = Offset(w * 0.78f, h * 0.86f)
                        )
                    }
                }
        )
        content()
    }
}
