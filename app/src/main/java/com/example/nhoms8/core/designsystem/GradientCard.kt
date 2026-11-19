package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlySky

/**
 * Hero balance card with Spendly's Lavender-to-Indigo-to-Sky diagonal gradient
 * (`#8B70C9` -> `#6B58C4` -> `#5FA8D3`) and subtle organic surface highlights.
 */
@Composable
fun GradientCard(
    modifier: Modifier = Modifier,
    shape: Shape = SpendlyShapeTokens.CardLarge,
    gradientColors: List<Color> = listOf(SpendlyLavender, SpendlyIndigo, SpendlySky),
    contentPadding: PaddingValues = PaddingValues(20.dp),
    showDecorations: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val rimBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.36f),
            Color.White.copy(alpha = 0.12f)
        )
    )

    Box(
        modifier = modifier
            .shadow(
                elevation = 14.dp,
                shape = shape,
                ambientColor = SpendlyLavenderDeep.copy(alpha = 0.45f),
                spotColor = SpendlyIndigo.copy(alpha = 0.45f)
            )
            .clip(shape)
            .background(
                brush = Brush.linearGradient(colors = gradientColors),
                shape = shape
            )
            .then(
                if (showDecorations) {
                    Modifier.drawBehind {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.10f),
                            radius = size.maxDimension * 0.34f,
                            center = Offset(size.width * 0.90f, size.height * 0.18f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.maxDimension * 0.24f,
                            center = Offset(size.width * 0.12f, size.height * 0.95f)
                        )
                    }
                } else {
                    Modifier
                }
            )
            .border(width = 1.dp, brush = rimBrush, shape = shape),
        propagateMinConstraints = true
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(
                modifier = Modifier.padding(contentPadding),
                content = content
            )
        }
    }
}
