package com.example.nhoms8.core.designsystem

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nhoms8.ui.theme.SpendlyShapeTokens
import com.example.nhoms8.ui.theme.SpendlyTextStyles
import com.example.nhoms8.ui.theme.spendlyColors
import dev.chrisbanes.haze.HazeState
import kotlin.math.roundToInt

/**
 * Budget consumption health thresholds used across Spendly fund rings.
 */
enum class BudgetHealthZone {
    HEALTHY,
    CAUTION,
    CRITICAL
}

/**
 * Classifies [ratio] (0f..1f+) into Mint (`<= 0.50`), Amber (`<= 0.80`), or Coral (`> 0.80`).
 */
fun budgetHealthZone(ratio: Float): BudgetHealthZone = when {
    ratio <= 0.50f -> BudgetHealthZone.HEALTHY
    ratio <= 0.80f -> BudgetHealthZone.CAUTION
    else -> BudgetHealthZone.CRITICAL
}

/**
 * Returns the foreground accent color for a budget progress [ratio] using Mint/Amber/Coral.
 */
@Composable
fun budgetProgressColor(ratio: Float): Color {
    val colors = MaterialTheme.spendlyColors
    return when (budgetHealthZone(ratio)) {
        BudgetHealthZone.HEALTHY -> colors.mintDeep
        BudgetHealthZone.CAUTION -> colors.amberDeep
        BudgetHealthZone.CRITICAL -> colors.coralDeep
    }
}

/**
 * Circular Canvas budget progress indicator colored dynamically by [ratio] (0-50-80-100%).
 */
@Composable
fun BudgetProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 66.dp,
    strokeWidth: Dp = 6.5.dp,
    showPercentageText: Boolean = true
) {
    val colors = MaterialTheme.spendlyColors
    val clampedSweepRatio = progress.coerceIn(0f, 1f)
    val animatedRatio by animateFloatAsState(
        targetValue = clampedSweepRatio,
        animationSpec = tween(durationMillis = 550),
        label = "budget_ring_progress"
    )

    val zone = budgetHealthZone(progress)
    val ringStartColor = when (zone) {
        BudgetHealthZone.HEALTHY -> colors.mint
        BudgetHealthZone.CAUTION -> colors.amber
        BudgetHealthZone.CRITICAL -> colors.coral
    }
    val ringEndColor = when (zone) {
        BudgetHealthZone.HEALTHY -> colors.mintDeep
        BudgetHealthZone.CAUTION -> colors.amberDeep
        BudgetHealthZone.CRITICAL -> colors.coralDeep
    }
    val trackColor = colors.divider.copy(alpha = 0.65f)
    val percentLabel = "${(progress.coerceAtLeast(0f) * 100f).roundToInt()}%"

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcTopLeft = Offset(inset, inset)
            val arcSize = Size(
                width = (this.size.width - strokePx).coerceAtLeast(1f),
                height = (this.size.height - strokePx).coerceAtLeast(1f)
            )
            val ringBrush = Brush.sweepGradient(
                colors = listOf(ringStartColor, ringEndColor, ringStartColor)
            )
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animatedRatio > 0f) {
                drawArc(
                    brush = ringBrush,
                    startAngle = -90f,
                    sweepAngle = animatedRatio * 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        if (showPercentageText) {
            Text(
                text = percentLabel,
                style = SpendlyTextStyles.moneySmall,
                color = ringEndColor,
                maxLines = 1
            )
        }
    }
}

/**
 * Group/Fund summary card displaying fund title, formatted balance, member & transaction counts,
 * and a Canvas circular budget progress ring colored Mint/Amber/Coral by ratio (0-50-80-100%).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FundCard(
    fundName: String,
    balanceFormatted: String,
    memberCount: Int,
    transactionCount: Int,
    budgetRatio: Float,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    budgetCaption: String = "Ngân sách",
    hazeState: HazeState? = null,
    onClick: (() -> Unit)? = null
) {
    val colors = MaterialTheme.spendlyColors
    val cardShape = SpendlyShapeTokens.CardLarge
    val clickModifier = if (onClick != null) {
        Modifier
            .clip(cardShape)
            .clickable(onClick = onClick)
    } else {
        Modifier
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .then(clickModifier),
        hazeState = hazeState,
        shape = cardShape,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primaryLavender,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = fundName,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = balanceFormatted,
                    style = SpendlyTextStyles.moneyLarge,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FundMetaChip(
                        icon = Icons.Outlined.Group,
                        label = "$memberCount thành viên"
                    )
                    FundMetaChip(
                        icon = Icons.AutoMirrored.Outlined.ReceiptLong,
                        label = "$transactionCount giao dịch"
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                BudgetProgressRing(progress = budgetRatio)
                Text(
                    text = budgetCaption,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = colors.textMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun FundMetaChip(
    icon: ImageVector,
    label: String
) {
    val colors = MaterialTheme.spendlyColors
    Row(
        modifier = Modifier
            .clip(SpendlyShapeTokens.Pill)
            .background(colors.lavenderContainer.copy(alpha = if (colors.isDark) 0.45f else 0.75f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = colors.onLavenderContainer,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = colors.onLavenderContainer,
            maxLines = 1
        )
    }
}
