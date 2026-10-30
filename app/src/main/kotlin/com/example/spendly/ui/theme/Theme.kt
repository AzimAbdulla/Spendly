package com.example.spendly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// =========================================================================
// Spendly Stitch Exact Theme Architecture (Light & Dark)
// =========================================================================

data class SpendlyThemeColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val container: Color,
    val containerLow: Color,
    val containerHigh: Color,
    val containerLowest: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val primaryGlow: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryGlow: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryGlow: Color,
    val tertiaryContainer: Color,
    val error: Color,
    val errorGlow: Color,
    val errorContainer: Color,
    val dockBackground: Color,
    val isDark: Boolean
)

val LightSpendlyColors = SpendlyThemeColors(
    background = StitchLightBackground,
    surface = StitchLightSurface,
    cardBackground = StitchLightCardSurface,
    container = StitchLightContainer,
    containerLow = StitchLightContainerLow,
    containerHigh = StitchLightContainerHigh,
    containerLowest = StitchLightContainerLowest,
    border = StitchLightBorder,
    divider = StitchLightDivider,
    textPrimary = StitchLightOnSurface,
    textSecondary = StitchLightOnSurfaceVariant,
    textMuted = Color(0xFF737373),
    primary = StitchLightPrimary,
    primaryGlow = StitchLightPrimary,
    primaryContainer = StitchLightPrimaryContainer,
    onPrimary = StitchLightOnPrimary,
    secondary = StitchLightSecondary,
    secondaryGlow = StitchLightSecondary,
    secondaryContainer = StitchLightSecondaryContainer,
    tertiary = StitchLightTertiary,
    tertiaryGlow = StitchLightTertiary,
    tertiaryContainer = StitchLightTertiaryContainer,
    error = StitchLightError,
    errorGlow = StitchLightError,
    errorContainer = StitchLightErrorContainer,
    dockBackground = StitchLightDockBackground,
    isDark = false
)

val DarkSpendlyColors = SpendlyThemeColors(
    background = StitchDarkBackground,
    surface = StitchDarkSurface,
    cardBackground = StitchDarkContainer,
    container = StitchDarkContainer,
    containerLow = StitchDarkContainerLow,
    containerHigh = StitchDarkContainerHigh,
    containerLowest = StitchDarkContainerLowest,
    border = StitchDarkBorder,
    divider = StitchDarkDivider,
    textPrimary = StitchDarkOnSurface,
    textSecondary = StitchDarkOnSurfaceVariant,
    textMuted = StitchDarkMuted,
    primary = StitchDarkPrimary,
    primaryGlow = StitchDarkPrimaryGlow,
    primaryContainer = StitchDarkPrimaryContainer,
    onPrimary = StitchDarkOnPrimary,
    secondary = StitchDarkSecondary,
    secondaryGlow = StitchDarkSecondaryGlow,
    secondaryContainer = StitchDarkSecondaryContainer,
    tertiary = StitchDarkTertiary,
    tertiaryGlow = StitchDarkTertiaryGlow,
    tertiaryContainer = StitchDarkTertiaryContainer,
    error = StitchDarkError,
    errorGlow = StitchDarkErrorGlow,
    errorContainer = StitchDarkErrorContainer,
    dockBackground = StitchDarkDockBackground,
    isDark = true
)

val LocalSpendlyColors = staticCompositionLocalOf { DarkSpendlyColors }

object SpendlyTheme {
    val colors: SpendlyThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalSpendlyColors.current
}

private val LightColorScheme = lightColorScheme(
    primary = LightSpendlyColors.primary,
    onPrimary = LightSpendlyColors.onPrimary,
    primaryContainer = LightSpendlyColors.primaryContainer,
    onPrimaryContainer = Color(0xFF0B1630),
    secondary = LightSpendlyColors.secondary,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LightSpendlyColors.secondaryContainer,
    onSecondaryContainer = Color(0xFF00210F),
    tertiary = LightSpendlyColors.tertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = LightSpendlyColors.tertiaryContainer,
    onTertiaryContainer = Color(0xFF2A1800),
    background = LightSpendlyColors.background,
    onBackground = LightSpendlyColors.textPrimary,
    surface = LightSpendlyColors.surface,
    onSurface = LightSpendlyColors.textPrimary,
    surfaceVariant = LightSpendlyColors.container,
    onSurfaceVariant = LightSpendlyColors.textSecondary,
    outline = LightSpendlyColors.border,
    error = LightSpendlyColors.error,
    errorContainer = LightSpendlyColors.errorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSpendlyColors.primary,
    onPrimary = DarkSpendlyColors.onPrimary,
    primaryContainer = DarkSpendlyColors.primaryContainer,
    onPrimaryContainer = Color(0xFFDCE6FB),
    secondary = DarkSpendlyColors.secondary,
    onSecondary = DarkSpendlyColors.onPrimary,
    secondaryContainer = DarkSpendlyColors.secondaryContainer,
    onSecondaryContainer = Color(0xFF96F4BC),
    tertiary = DarkSpendlyColors.tertiary,
    onTertiary = Color(0xFF291800),
    tertiaryContainer = DarkSpendlyColors.tertiaryContainer,
    onTertiaryContainer = Color(0xFFFFDDB4),
    background = DarkSpendlyColors.background,
    onBackground = DarkSpendlyColors.textPrimary,
    surface = DarkSpendlyColors.surface,
    onSurface = DarkSpendlyColors.textPrimary,
    surfaceVariant = DarkSpendlyColors.container,
    onSurfaceVariant = DarkSpendlyColors.textSecondary,
    outline = DarkSpendlyColors.border,
    error = DarkSpendlyColors.error,
    errorContainer = DarkSpendlyColors.errorContainer,
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = true, // Default to true matching Stitch Dark Design
    content: @Composable () -> Unit
) {
    val spendlyColors = if (darkTheme) DarkSpendlyColors else LightSpendlyColors
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalSpendlyColors provides spendlyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
