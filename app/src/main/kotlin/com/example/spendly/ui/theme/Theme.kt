package com.example.spendly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = StitchPrimary,
    onPrimary = StitchOnPrimary,
    primaryContainer = StitchPrimaryFixed,
    onPrimaryContainer = StitchOnPrimaryFixed,
    secondary = StitchSecondary,
    onSecondary = StitchOnSecondary,
    secondaryContainer = StitchSecondaryContainer,
    onSecondaryContainer = StitchOnSecondaryFixed,
    tertiary = StitchTertiary,
    onTertiary = StitchOnTertiary,
    tertiaryContainer = StitchTertiaryContainer,
    onTertiaryContainer = StitchOnTertiaryContainer,
    background = StitchBackground,
    onBackground = StitchOnSurface,
    surface = StitchCardSurface,
    onSurface = StitchOnSurface,
    surfaceVariant = StitchContainer,
    onSurfaceVariant = StitchOnSurfaceVariant,
    outline = StitchBorder,
    error = StitchError,
    errorContainer = StitchErrorContainer,
)

private val DarkColorScheme = darkColorScheme(
    primary = SpendlyLavenderDarkMode,
    onPrimary = SpendlyBackgroundDark,
    primaryContainer = SpendlyLavenderDark,
    onPrimaryContainer = SpendlyLavenderDarkMode,
    secondary = SpendlySage,
    onSecondary = SpendlyBackgroundDark,
    secondaryContainer = SpendlySageDark,
    onSecondaryContainer = SpendlySageContainer,
    tertiary = SpendlyAmber,
    onTertiary = SpendlyBackgroundDark,
    tertiaryContainer = SpendlyAmberDark,
    onTertiaryContainer = SpendlyAmberContainer,
    background = SpendlyBackgroundDark,
    onBackground = SpendlyOnBackgroundDark,
    surface = SpendlySurfaceDark,
    onSurface = SpendlyOnBackgroundDark,
    surfaceVariant = SpendlyCardBorderDark,
    onSurfaceVariant = SpendlySubtleDark,
    outline = SpendlyCardBorderDark,
    error = SpendlyExpense,
    errorContainer = SpendlyExpenseContainer,
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
