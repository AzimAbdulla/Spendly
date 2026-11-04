package com.example.spendly.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Google Stitch Exact Design Tokens (Dark OLED & Light)
// =========================================================================

// --- Stitch Dark Palette (Matches Stitch Tailwind config & inline OLED styles) ---
val StitchDarkBackground = Color(0xFF0A0A0D)
val StitchDarkSurface = Color(0xFF0A0A0D)
val StitchDarkContainerLowest = Color(0x08FFFFFF)
val StitchDarkContainerLow = Color(0x0DFFFFFF)
val StitchDarkContainer = Color(0x12FFFFFF)
val StitchDarkContainerHigh = Color(0x1CFFFFFF)
val StitchDarkContainerHighest = Color(0x26FFFFFF)
val StitchDarkBorder = Color(0x1FFFFFFF)
val StitchDarkOutline = Color(0x33FFFFFF)
val StitchDarkDivider = Color(0x14FFFFFF)

val StitchDarkOnSurface = Color(0xFFEDEDEF)
val StitchDarkOnSurfaceVariant = Color(0xFFA1A1A6)
val StitchDarkMuted = Color(0xFF85858B)

val StitchDarkPrimary = Color(0xFF8EB1F5)
val StitchDarkPrimaryGlow = StitchDarkPrimary
val StitchDarkPrimaryContainer = Color(0xFF1A2438)
val StitchDarkOnPrimary = Color(0xFF0B1630)
val StitchDarkOnPrimaryFixed = Color(0xFF0B1630)

val StitchDarkSecondary = Color(0xFF6CC79A)
val StitchDarkSecondaryGlow = StitchDarkSecondary
val StitchDarkSecondaryContainer = Color(0xFF15261D)
val StitchDarkOnSecondary = Color(0xFF003920)

val StitchDarkTertiary = Color(0xFFE8B061)
val StitchDarkTertiaryGlow = StitchDarkTertiary
val StitchDarkTertiaryContainer = Color(0xFF2A2116)
val StitchDarkOnTertiary = Color(0xFF452B00)

val StitchDarkError = Color(0xFFE5686B)
val StitchDarkErrorGlow = StitchDarkError
val StitchDarkErrorContainer = Color(0xFF2E1617)
val StitchDarkOnError = Color(0xFF690005)

val StitchDarkDockBackground = Color(0xFF08080A)

// --- Stitch Light Palette ---
val StitchLightBackground = Color(0xFFF2F3F7)
val StitchLightSurface = Color(0xFFF2F3F7)
val StitchLightCardSurface = Color(0x9EFFFFFF)
val StitchLightContainerLowest = Color(0x52FFFFFF)
val StitchLightContainerLow = Color(0x80FFFFFF)
val StitchLightContainer = Color(0x9EFFFFFF)
val StitchLightContainerHigh = Color(0xD9FFFFFF)
val StitchLightBorder = Color(0x17000000)
val StitchLightDivider = Color(0x0F000000)
val StitchLightOnSurface = Color(0xFF1C1C1A)
val StitchLightOnSurfaceVariant = Color(0xFF55524C)
val StitchLightPrimary = Color(0xFF2F55A4)
val StitchLightPrimaryContainer = Color(0xFFDFE7F8)
val StitchLightOnPrimary = Color(0xFFFFFFFF)
val StitchLightSecondary = Color(0xFF376848)
val StitchLightSecondaryContainer = Color(0xFFB6ECC4)
val StitchLightTertiary = Color(0xFF805200)
val StitchLightTertiaryContainer = Color(0xFFFFDDB5)
val StitchLightError = Color(0xFFBA1A1A)
val StitchLightErrorContainer = Color(0xFFFFDAD6)
val StitchLightDockBackground = Color(0xFF1A1B22)

// --- Liquid Glass (floating glass, rims, wallpaper) ---
val GlassDarkTint = Color(0x6117171C)
val GlassDarkFallback = Color(0xF017171C)
val GlassDarkRimTop = Color(0x47FFFFFF)
val GlassDarkRimBottom = Color(0x0DFFFFFF)
val GlassDarkSelected = Color(0x29FFFFFF)
val GlassDarkPopup = Color(0xFF1B1B21)
val GlassDarkWallpaperBlobs = listOf(Color(0x852F55A4), Color(0x664A33B8), Color(0x4D1E6A9C))

val GlassLightTint = Color(0xADF8F8FB)
val GlassLightFallback = Color(0xF5F8F8FB)
val GlassLightRimTop = Color(0xF2FFFFFF)
val GlassLightRimBottom = Color(0x14000000)
val GlassLightSelected = Color(0xF2FFFFFF)
val GlassLightPopup = Color(0xFFFFFFFF)
val GlassLightWallpaperBlobs = listOf(Color(0x8C9DB8F5), Color(0x66C3B4F2), Color(0x66A6D6EE))

// --- Category Colors (muted, one hue per category; used only in charts and dots) ---
val StitchCatFood = Color(0xFFE0A86B)
val StitchCatGroceries = Color(0xFF6FBF8E)
val StitchCatTransport = Color(0xFF6FA8DC)
val StitchCatEntertainment = Color(0xFFC48BD6)
val StitchCatUtilities = Color(0xFFD9C46A)
val StitchCatShopping = Color(0xFFD98F7A)
val StitchCatHealth = Color(0xFFE5848A)
val StitchCatSubscriptions = Color(0xFF9A9AA3)
val StitchCatTravel = Color(0xFF5FB3B0)
val StitchCatOther = Color(0xFF85858B)

// Aliases for compatibility
val SpendlyBackground = StitchLightBackground
val SpendlySurface = StitchLightCardSurface
val SpendlyCardBorder = StitchLightBorder
val SpendlyLavender = StitchLightPrimary
val SpendlySage = StitchLightSecondary
val SpendlyAmber = StitchLightTertiaryContainer
val SpendlyOnBackground = StitchLightOnSurface
val SpendlyOnSurface = StitchLightOnSurface
val SpendlySubtle = StitchLightOnSurfaceVariant
val SpendlyDivider = StitchLightDivider
val SpendlyExpense = StitchLightError
val SpendlyExpenseContainer = StitchLightErrorContainer
val SpendlyIncome = StitchLightSecondary

val SpendlyBackgroundDark = StitchDarkBackground
val SpendlySurfaceDark = StitchDarkContainer
val SpendlyCardBorderDark = StitchDarkBorder
val SpendlyLavenderDarkMode = StitchDarkPrimary
val SpendlyOnBackgroundDark = StitchDarkOnSurface
val SpendlySubtleDark = StitchDarkOnSurfaceVariant
