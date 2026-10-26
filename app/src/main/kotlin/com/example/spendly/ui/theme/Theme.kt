package com.example.spendly.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = SpendlyLavender,
    onPrimary = SpendlySurface,
    primaryContainer = SpendlyLavenderContainer,
    onPrimaryContainer = SpendlyLavenderDark,
    secondary = SpendlySage,
    onSecondary = SpendlySurface,
    secondaryContainer = SpendlySageContainer,
    onSecondaryContainer = SpendlySageDark,
    tertiary = SpendlyAmber,
    onTertiary = SpendlySurface,
    tertiaryContainer = SpendlyAmberContainer,
    onTertiaryContainer = SpendlyAmberDark,
    background = SpendlyBackground,
    onBackground = SpendlyOnBackground,
    surface = SpendlySurface,
    onSurface = SpendlyOnSurface,
    surfaceVariant = SpendlyCardBorder,
    onSurfaceVariant = SpendlySubtle,
    outline = SpendlyCardBorder,
    error = SpendlyExpense,
    errorContainer = SpendlyExpenseContainer,
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
