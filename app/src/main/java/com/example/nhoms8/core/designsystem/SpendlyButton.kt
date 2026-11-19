package com.example.nhoms8.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nhoms8.ui.theme.SpendlyIndigo
import com.example.nhoms8.ui.theme.SpendlyLavender
import com.example.nhoms8.ui.theme.SpendlyLavenderDeep
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState

/**
 * Button visual treatments for Spendly actions.
 */
enum class SpendlyButtonStyle {
    PRIMARY,
    SECONDARY_GLASS,
    OUTLINED
}

/**
 * Spendly primary gradient/lavender, secondary glass, or outlined pill button (48-52dp height).
 */
@Composable
fun SpendlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SpendlyButtonStyle = SpendlyButtonStyle.PRIMARY,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    hazeState: HazeState? = null,
    height: Dp = 50.dp,
    shape: Shape = SpendlyShapeTokens.Pill
) {
    val colors = MaterialTheme.spendlyColors
    val isInteractive = enabled && !isLoading

    val contentColor = when (style) {
        SpendlyButtonStyle.PRIMARY -> Color.White
        SpendlyButtonStyle.SECONDARY_GLASS -> colors.textPrimary
        SpendlyButtonStyle.OUTLINED -> colors.onLavenderContainer
    }

    val baseModifier = modifier
        .height(height)
        .then(if (!enabled) Modifier.alpha(0.5f) else Modifier)

    val surfaceModifier = when (style) {
        SpendlyButtonStyle.PRIMARY -> {
            val primaryGradient = Brush.horizontalGradient(
                colors = listOf(SpendlyLavender, SpendlyLavenderDeep, SpendlyIndigo)
            )
            baseModifier
                .then(
                    if (enabled) {
                        Modifier.shadow(
                            elevation = 8.dp,
                            shape = shape,
                            ambientColor = SpendlyLavenderDeep.copy(alpha = 0.35f),
                            spotColor = SpendlyLavenderDeep.copy(alpha = 0.35f)
                        )
                    } else {
                        Modifier
                    }
                )
                .clip(shape)
                .background(brush = primaryGradient, shape = shape)
        }

        SpendlyButtonStyle.SECONDARY_GLASS -> {
            baseModifier.spendlyGlassSurface(
                hazeState = hazeState,
                shape = shape,
                tintColor = colors.glassContainer
            )
        }

        SpendlyButtonStyle.OUTLINED -> {
            baseModifier
                .clip(shape)
                .background(
                    color = colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.25f else 0.45f),
                    shape = shape
                )
                .border(
                    width = 1.25.dp,
                    color = colors.primaryLavender.copy(alpha = 0.65f),
                    shape = shape
                )
        }
    }

    Box(
        modifier = surfaceModifier
            .clickable(
                enabled = isInteractive,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = contentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            } else if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (!isLoading && trailingIcon != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Convenience secondary frosted-glass pill button delegating to [SpendlyButton].
 */
@Composable
fun SpendlySecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    hazeState: HazeState? = null
) {
    SpendlyButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = SpendlyButtonStyle.SECONDARY_GLASS,
        enabled = enabled,
        leadingIcon = leadingIcon,
        hazeState = hazeState
    )
}

/**
 * Convenience outlined lavender pill button delegating to [SpendlyButton].
 */
@Composable
fun SpendlyOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    SpendlyButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        style = SpendlyButtonStyle.OUTLINED,
        enabled = enabled,
        leadingIcon = leadingIcon
    )
}
