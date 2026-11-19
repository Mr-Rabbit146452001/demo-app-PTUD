package com.example.nhoms8.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 shape scale tuned for Spendly's soft organic cards and pill controls.
 */
val SpendlyShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Named Spendly shape tokens for cards, pills, bottom bars, and bottom sheets.
 */
object SpendlyShapeTokens {
    val CardSmall = RoundedCornerShape(16.dp)
    val CardMedium = RoundedCornerShape(20.dp)
    val CardLarge = RoundedCornerShape(24.dp)
    val HeroCard = RoundedCornerShape(28.dp)
    val Pill = RoundedCornerShape(50)
    val BottomBar = RoundedCornerShape(28.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}
