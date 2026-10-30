package com.example.spendly.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Google Stitch Exact Design Tokens (Dark OLED & Light)
// =========================================================================

// --- Stitch Dark Palette (Matches Stitch Tailwind config & inline OLED styles) ---
val StitchDarkBackground = Color(0xFF0B0B0C)
val StitchDarkSurface = Color(0xFF0B0B0C)
val StitchDarkContainerLowest = Color(0xFF08080A)
val StitchDarkContainerLow = Color(0xFF101012)
val StitchDarkContainer = Color(0xFF141416)
val StitchDarkContainerHigh = Color(0xFF1A1A1D)
val StitchDarkContainerHighest = Color(0xFF202024)
val StitchDarkBorder = Color(0xFF232327)
val StitchDarkOutline = Color(0xFF323237)
val StitchDarkDivider = Color(0xFF1E1E21)

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
val StitchLightBackground = Color(0xFFFCF9F5)
val StitchLightSurface = Color(0xFFFCF9F5)
val StitchLightCardSurface = Color(0xFFFFFFFF)
val StitchLightContainerLowest = Color(0xFFFFFFFF)
val StitchLightContainerLow = Color(0xFFF6F3EF)
val StitchLightContainer = Color(0xFFF0EDEA)
val StitchLightContainerHigh = Color(0xFFEBE8E4)
val StitchLightBorder = Color(0xFFE8E8E4)
val StitchLightDivider = Color(0xFFEEEEEA)
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
