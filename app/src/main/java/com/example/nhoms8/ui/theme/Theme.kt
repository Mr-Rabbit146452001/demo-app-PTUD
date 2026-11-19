package com.example.nhoms8.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SpendlyLightColorScheme = lightColorScheme(
    primary = SpendlyLavender,
    onPrimary = Color.White,
    primaryContainer = SpendlyLavenderContainer,
    onPrimaryContainer = SpendlyLavenderDeep,
    secondary = SpendlyMintDeep,
    onSecondary = Color.White,
    secondaryContainer = SpendlyMintContainer,
    onSecondaryContainer = SpendlyMintDeep,
    tertiary = SpendlySky,
    onTertiary = Color.White,
    tertiaryContainer = SpendlySkyTint,
    onTertiaryContainer = Color(0xFF1B577B),
    background = SpendlyLightBackground,
    onBackground = SpendlyTextDark,
    surface = SpendlyLightSurface,
    onSurface = SpendlyTextDark,
    surfaceVariant = SpendlyLightSurfaceVariant,
    onSurfaceVariant = SpendlyTextMuted,
    error = SpendlyCoralDeep,
    onError = Color.White,
    errorContainer = SpendlyCoralContainer,
    onErrorContainer = SpendlyCoralDeep,
    outline = Color(0xFFD7CEE9),
    outlineVariant = SpendlyLightDivider
)

private val SpendlyDarkColorScheme = darkColorScheme(
    primary = SpendlyLavenderLight,
    onPrimary = Color(0xFF231347),
    primaryContainer = Color(0xFF392B5D),
    onPrimaryContainer = SpendlyLavenderContainer,
    secondary = SpendlyMint,
    onSecondary = Color(0xFF0E3327),
    secondaryContainer = Color(0xFF1A3A30),
    onSecondaryContainer = SpendlyMint,
    tertiary = SpendlySky,
    onTertiary = Color(0xFF0C2E42),
    tertiaryContainer = Color(0xFF184159),
    onTertiaryContainer = SpendlySkyTint,
    background = SpendlyDarkBackground,
    onBackground = SpendlyDarkText,
    surface = SpendlyDarkSurface,
    onSurface = SpendlyDarkText,
    surfaceVariant = SpendlyDarkSurfaceVariant,
    onSurfaceVariant = SpendlyDarkTextMuted,
    error = SpendlyCoral,
    onError = Color(0xFF4B1618),
    errorContainer = Color(0xFF472226),
    onErrorContainer = SpendlyCoral,
    outline = Color(0xFF473F5E),
    outlineVariant = SpendlyDarkDivider
)

/**
 * Convenience accessor for the active [SpendlyColors] inside [MaterialTheme].
 */
val MaterialTheme.spendlyColors: SpendlyColors
    @Composable
    @ReadOnlyComposable
    get() = LocalSpendlyColors.current

/**
 * Root theme for Spendly combining Material 3 color/shape/typography tokens with [LocalSpendlyColors].
 */
@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SpendlyDarkColorScheme
        else -> SpendlyLightColorScheme
    }

    val spendlyColors = if (darkTheme) DarkSpendlyColors else LightSpendlyColors

    CompositionLocalProvider(LocalSpendlyColors provides spendlyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            shapes = SpendlyShapes,
            typography = SpendlyTypography,
            content = content
        )
    }
}

/**
 * Backward-compatible theme entry point delegating to [SpendlyTheme].
 * Preserves the exact signature used by existing screens and previews while applying
 * Spendly's branded Lavender/Organic palette consistently across Android versions.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun Nhoms8Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    SpendlyTheme(
        darkTheme = darkTheme,
        dynamicColor = false,
        content = content
    )
}
