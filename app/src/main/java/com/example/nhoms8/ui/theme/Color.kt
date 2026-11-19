package com.example.nhoms8.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ============================================================================
// Legacy M3 template colors (preserved for backward compatibility)
// ============================================================================
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// ============================================================================
// Spendly Design System Palette Tokens (Lavender / Organic / Glassmorphism)
// ============================================================================
val SpendlyLavender = Color(0xFF8B70C9)
val SpendlyLavenderDeep = Color(0xFF6B4FB3)
val SpendlyLavenderContainer = Color(0xFFEDE6FA)
val SpendlyLavenderLight = Color(0xFFB39DF0)

val SpendlyIndigo = Color(0xFF6B58C4)
val SpendlySky = Color(0xFF5FA8D3)
val SpendlySkyTint = Color(0xFFD6ECF9)

val SpendlyMint = Color(0xFF8BCDB5)
val SpendlyMintDeep = Color(0xFF2E8B6E)
val SpendlyMintContainer = Color(0xFFE1F5EE)

val SpendlyAmber = Color(0xFFF3B75E)
val SpendlyAmberDeep = Color(0xFFC8801C)
val SpendlyAmberContainer = Color(0xFFFDF1DC)

val SpendlyCoral = Color(0xFFF2A6A6)
val SpendlyCoralDeep = Color(0xFFD95B5B)
val SpendlyCoralContainer = Color(0xFFFCE8E8)

// Light mode neutrals
val SpendlyLightBackground = Color(0xFFF5F1FA)
val SpendlyLightSurface = Color(0xFFFFFFFF)
val SpendlyLightSurfaceElevated = Color(0xFFFAF8FE)
val SpendlyLightSurfaceVariant = Color(0xFFEDE8F6)
val SpendlyTextDark = Color(0xFF302B3D)
val SpendlyTextMuted = Color(0xFF6D677E)
val SpendlyLightDivider = Color(0xFFE4DDF2)

// Dark mode neutrals
val SpendlyDarkBackground = Color(0xFF15121E)
val SpendlyDarkSurface = Color(0xFF221D2E)
val SpendlyDarkSurfaceElevated = Color(0xFF2B243B)
val SpendlyDarkSurfaceVariant = Color(0xFF2D273D)
val SpendlyDarkText = Color(0xFFEAE4F7)
val SpendlyDarkTextMuted = Color(0xFFAFA6C7)
val SpendlyDarkDivider = Color(0xFF362F4A)

/**
 * Semantic design tokens for Spendly Lavender / Organic UI components.
 */
@Immutable
data class SpendlyColors(
    val isDark: Boolean,
    val primaryLavender: Color,
    val deepLavender: Color,
    val lavenderContainer: Color,
    val onLavenderContainer: Color,
    val indigo: Color,
    val sky: Color,
    val mint: Color,
    val mintDeep: Color,
    val mintContainer: Color,
    val amber: Color,
    val amberDeep: Color,
    val amberContainer: Color,
    val coral: Color,
    val coralDeep: Color,
    val coralContainer: Color,
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val glassContainer: Color,
    val glassBorderStart: Color,
    val glassBorderEnd: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val divider: Color,
    val blobLavender: Color,
    val blobMint: Color,
    val blobSky: Color
)

val LightSpendlyColors = SpendlyColors(
    isDark = false,
    primaryLavender = SpendlyLavender,
    deepLavender = SpendlyLavenderDeep,
    lavenderContainer = SpendlyLavenderContainer,
    onLavenderContainer = SpendlyLavenderDeep,
    indigo = SpendlyIndigo,
    sky = SpendlySky,
    mint = SpendlyMint,
    mintDeep = SpendlyMintDeep,
    mintContainer = SpendlyMintContainer,
    amber = SpendlyAmber,
    amberDeep = SpendlyAmberDeep,
    amberContainer = SpendlyAmberContainer,
    coral = SpendlyCoral,
    coralDeep = SpendlyCoralDeep,
    coralContainer = SpendlyCoralContainer,
    background = SpendlyLightBackground,
    surface = SpendlyLightSurface,
    surfaceElevated = SpendlyLightSurfaceElevated,
    glassContainer = SpendlyLightSurface,
    glassBorderStart = Color.White.copy(alpha = 0.88f),
    glassBorderEnd = SpendlyLavender.copy(alpha = 0.24f),
    textPrimary = SpendlyTextDark,
    textMuted = SpendlyTextMuted,
    divider = SpendlyLightDivider,
    blobLavender = SpendlyLavender.copy(alpha = 0.20f),
    blobMint = SpendlyMint.copy(alpha = 0.22f),
    blobSky = SpendlySky.copy(alpha = 0.18f)
)

val DarkSpendlyColors = SpendlyColors(
    isDark = true,
    primaryLavender = SpendlyLavenderLight,
    deepLavender = SpendlyLavender,
    lavenderContainer = Color(0xFF392B5D),
    onLavenderContainer = SpendlyLavenderContainer,
    indigo = SpendlyIndigo,
    sky = SpendlySky,
    mint = SpendlyMint,
    mintDeep = Color(0xFF56B898),
    mintContainer = Color(0xFF1A3A30),
    amber = SpendlyAmber,
    amberDeep = Color(0xFFF6C677),
    amberContainer = Color(0xFF3B2D17),
    coral = SpendlyCoral,
    coralDeep = Color(0xFFEC7A7A),
    coralContainer = Color(0xFF472226),
    background = SpendlyDarkBackground,
    surface = SpendlyDarkSurface,
    surfaceElevated = SpendlyDarkSurfaceElevated,
    glassContainer = SpendlyDarkSurface,
    glassBorderStart = Color.White.copy(alpha = 0.24f),
    glassBorderEnd = SpendlyLavenderLight.copy(alpha = 0.20f),
    textPrimary = SpendlyDarkText,
    textMuted = SpendlyDarkTextMuted,
    divider = SpendlyDarkDivider,
    blobLavender = SpendlyLavender.copy(alpha = 0.24f),
    blobMint = SpendlyMint.copy(alpha = 0.16f),
    blobSky = SpendlySky.copy(alpha = 0.16f)
)

val LocalSpendlyColors = staticCompositionLocalOf { LightSpendlyColors }
