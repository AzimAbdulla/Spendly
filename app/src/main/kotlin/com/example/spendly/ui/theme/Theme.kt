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
// Spendly Clean Minimal Theme Architecture (Light & Dark)
// =========================================================================

data class SpendlyThemeColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val container: Color,
    val containerLow: Color,
    val containerHigh: Color,
    val border: Color,
    val divider: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val secondary: Color,
    val secondaryContainer: Color,
    val tertiary: Color,
    val tertiaryContainer: Color,
    val error: Color,
    val errorContainer: Color,
    val dockBackground: Color,
    val isDark: Boolean
)

val LightSpendlyColors = SpendlyThemeColors(
    background = Color(0xFFF9F9FB),
    surface = Color(0xFFFFFFFF),
    cardBackground = Color(0xFFFFFFFF),
    container = Color(0xFFF1F1F4),
    containerLow = Color(0xFFF6F6F9),
    containerHigh = Color(0xFFEAEAEF),
    border = Color(0xFFE8E8ED),
    divider = Color(0xFFEEEEF2),
    textPrimary = Color(0xFF141416),
    textSecondary = Color(0xFF6B6B78),
    textMuted = Color(0xFF9898A4),
    primary = Color(0xFF5E5297),
    primaryContainer = Color(0xFFEBE6FB),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF2E7D32),
    secondaryContainer = Color(0xFFE8F5E9),
    tertiary = Color(0xFFB45309),
    tertiaryContainer = Color(0xFFFEF3C7),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    dockBackground = Color(0xFF181920),
    isDark = false
)

val DarkSpendlyColors = SpendlyThemeColors(
    background = Color(0xFF0F1013),
    surface = Color(0xFF17181C),
    cardBackground = Color(0xFF17181C),
    container = Color(0xFF212228),
    containerLow = Color(0xFF1B1C21),
    containerHigh = Color(0xFF2A2B32),
    border = Color(0xFF282931),
    divider = Color(0xFF23242B),
    textPrimary = Color(0xFFF4F4F6),
    textSecondary = Color(0xFF9494A1),
    textMuted = Color(0xFF646470),
    primary = Color(0xFF9D8DF0),
    primaryContainer = Color(0xFF2B2648),
    onPrimary = Color(0xFF0F1013),
    secondary = Color(0xFF4ADE80),
    secondaryContainer = Color(0xFF163824),
    tertiary = Color(0xFFFBBF24),
    tertiaryContainer = Color(0xFF3B2C14),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF3F1919),
    dockBackground = Color(0xFF131418),
    isDark = true
)

val LocalSpendlyColors = staticCompositionLocalOf { LightSpendlyColors }

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
    onPrimaryContainer = Color(0xFF1C0C52),
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
    onPrimaryContainer = Color(0xFFEBE6FB),
    secondary = DarkSpendlyColors.secondary,
    onSecondary = Color(0xFF0F1013),
    secondaryContainer = DarkSpendlyColors.secondaryContainer,
    onSecondaryContainer = Color(0xFFB9EFC6),
    tertiary = DarkSpendlyColors.tertiary,
    onTertiary = Color(0xFF0F1013),
    tertiaryContainer = DarkSpendlyColors.tertiaryContainer,
    onTertiaryContainer = Color(0xFFFFDDB5),
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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
