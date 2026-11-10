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
import androidx.compose.ui.graphics.lerp

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
    // The dock matches the tiles, with a hairline edge; the green + disc is its one bright control
    val dockBackground: Color,
    val dockBorder: Color,
    val dockContent: Color,
    val dockContentSelected: Color,
    val dockPill: Color,
    val dockAction: Color,
    val onDockAction: Color,
    // Liquid glass
    val glassTint: Color,
    val glassFallback: Color,
    val glassRimTop: Color,
    val glassRimBottom: Color,
    val glassSelected: Color,
    val popup: Color,
    val wallpaperBlobs: List<Color>,
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
    textMuted = Color(0xFF8A909C),
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
    dockBorder = Color(0xFFE2E4EA),
    dockContent = Color(0xFF7D828B),
    dockContentSelected = Color(0xFF14161A),
    dockPill = Color(0xFFECEEF3),
    dockAction = TintSafe,
    onDockAction = OnTintSafe,
    glassTint = GlassLightTint,
    glassFallback = GlassLightFallback,
    glassRimTop = GlassLightRimTop,
    glassRimBottom = GlassLightRimBottom,
    glassSelected = GlassLightSelected,
    popup = GlassLightPopup,
    wallpaperBlobs = GlassLightWallpaperBlobs,
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
    dockBorder = Color(0xFF2A2A2F),
    dockContent = Color(0xFF8A8F98),
    dockContentSelected = Color(0xFFF4F4F5),
    dockPill = Color(0xFF2E2E33),
    dockAction = TintSafe,
    onDockAction = OnTintSafe,
    glassTint = GlassDarkTint,
    glassFallback = GlassDarkFallback,
    glassRimTop = GlassDarkRimTop,
    glassRimBottom = GlassDarkRimBottom,
    glassSelected = GlassDarkSelected,
    popup = GlassDarkPopup,
    wallpaperBlobs = GlassDarkWallpaperBlobs,
    isDark = true
)

// Pure black for OLED: Dark's tokens on a true #000000 canvas with no wallpaper glow and
// neutral glass, so unlit pixels stay fully off
val BlackSpendlyColors = DarkSpendlyColors.copy(
    background = Color.Black,
    surface = Color.Black,
    glassTint = Color(0x8C000000),
    glassFallback = Color(0xF0000000),
    popup = Color(0xFF111111),
    wallpaperBlobs = emptyList()
)

/** A tile's own shade, mixed from one hue: the fill plus matching text, track and accent for its content. */
data class TileTint(val background: Color, val secondary: Color, val track: Color, val accent: Color)

/** Deep shades on the dark themes, pastels on light; the same hue reads as the same tile in both. */
fun SpendlyThemeColors.tint(hue: Color): TileTint {
    val fill = if (isDark) lerp(background, hue, 0.2f) else lerp(Color.White, hue, 0.14f)
    return TileTint(
        background = fill,
        secondary = if (isDark) lerp(hue, Color.White, 0.55f) else lerp(hue, Color.Black, 0.45f),
        track = lerp(fill, hue, 0.22f),
        accent = if (isDark) hue else lerp(hue, Color.Black, 0.1f)
    )
}

/** These colours as seen inside a tinted tile: secondary text, tracks, dividers and the accent follow the tint. */
fun SpendlyThemeColors.inTile(tint: TileTint) = copy(
    cardBackground = tint.background,
    container = tint.track,
    containerLow = tint.track,
    containerHigh = tint.track,
    divider = lerp(tint.background, tint.track, 0.6f),
    textSecondary = tint.secondary,
    textMuted = tint.secondary.copy(alpha = 0.7f),
    primary = tint.accent,
    primaryContainer = tint.track
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
    surfaceVariant = LightSpendlyColors.popup,
    surfaceContainer = LightSpendlyColors.popup,
    surfaceContainerHigh = LightSpendlyColors.popup,
    surfaceContainerHighest = LightSpendlyColors.popup,
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
    surfaceVariant = DarkSpendlyColors.popup,
    surfaceContainer = DarkSpendlyColors.popup,
    surfaceContainerHigh = DarkSpendlyColors.popup,
    surfaceContainerHighest = DarkSpendlyColors.popup,
    onSurfaceVariant = DarkSpendlyColors.textSecondary,
    outline = DarkSpendlyColors.border,
    error = DarkSpendlyColors.error,
    errorContainer = DarkSpendlyColors.errorContainer,
)

private val BlackColorScheme = DarkColorScheme.copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceVariant = BlackSpendlyColors.popup,
    surfaceContainer = BlackSpendlyColors.popup,
    surfaceContainerHigh = BlackSpendlyColors.popup,
    surfaceContainerHighest = BlackSpendlyColors.popup,
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = true, // Default to true matching Stitch Dark Design
    pureBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val spendlyColors = when {
        !darkTheme -> LightSpendlyColors
        pureBlack -> BlackSpendlyColors
        else -> DarkSpendlyColors
    }
    val colorScheme = when {
        !darkTheme -> LightColorScheme
        pureBlack -> BlackColorScheme
        else -> DarkColorScheme
    }

    CompositionLocalProvider(LocalSpendlyColors provides spendlyColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
