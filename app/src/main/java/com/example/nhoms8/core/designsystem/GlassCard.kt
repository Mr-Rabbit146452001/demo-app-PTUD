package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.blur.materials.HazeMaterials

/**
 * Default visual tokens for Spendly glassmorphic surfaces.
 */
object SpendlyGlassDefaults {
    @Composable
    fun tintColor(): Color = MaterialTheme.spendlyColors.glassContainer

    @Composable
    fun borderBrush(isDark: Boolean = MaterialTheme.spendlyColors.isDark): Brush {
        val colors = MaterialTheme.spendlyColors
        val midStop = if (isDark) {
            Color.White.copy(alpha = 0.08f)
        } else {
            Color.White.copy(alpha = 0.42f)
        }
        return Brush.linearGradient(
            colors = listOf(
                colors.glassBorderStart,
                midStop,
                colors.glassBorderEnd
            )
        )
    }
}

/**
 * Applies real-time Haze 2.1.0 frosted glass blur when [hazeState] is provided,
 * or falls back cleanly to a translucent gradient surface for previews and older surfaces.
 */
@Composable
fun Modifier.spendlyGlassSurface(
    hazeState: HazeState?,
    shape: Shape = SpendlyShapeTokens.CardLarge,
    tintColor: Color = SpendlyGlassDefaults.tintColor(),
    borderBrush: Brush = SpendlyGlassDefaults.borderBrush(),
    borderWidth: Dp = 1.dp,
    blurStyle: HazeBlurStyle? = null
): Modifier {
    val clipped = this.clip(shape)
    return if (hazeState != null) {
        val resolvedStyle = blurStyle ?: HazeMaterials.thin(containerColor = tintColor)
        clipped
            .hazeBlur(
                input = HazeInput.Sources(hazeState),
                style = resolvedStyle
            )
            .border(width = borderWidth, brush = borderBrush, shape = shape)
    } else {
        val topTint = if (tintColor.alpha < 0.99f) tintColor else tintColor.copy(alpha = 0.84f)
        val bottomTint = if (tintColor.alpha < 0.99f) {
            tintColor.copy(alpha = (tintColor.alpha * 0.85f).coerceIn(0.1f, 1f))
        } else {
            tintColor.copy(alpha = 0.68f)
        }
        clipped
            .background(
                brush = Brush.verticalGradient(listOf(topTint, bottomTint)),
                shape = shape
            )
            .border(width = borderWidth, brush = borderBrush, shape = shape)
    }
}

/**
 * Frosted glass container backed by Haze 2.1.0 when [hazeState] is non-null,
 * with a crisp translucent gradient fallback when [hazeState] is null.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    hazeState: HazeState? = null,
    shape: Shape = SpendlyShapeTokens.CardLarge,
    tintColor: Color = SpendlyGlassDefaults.tintColor(),
    borderBrush: Brush = SpendlyGlassDefaults.borderBrush(),
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = modifier.spendlyGlassSurface(
            hazeState = hazeState,
            shape = shape,
            tintColor = tintColor,
            borderBrush = borderBrush
        ),
        propagateMinConstraints = true
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
