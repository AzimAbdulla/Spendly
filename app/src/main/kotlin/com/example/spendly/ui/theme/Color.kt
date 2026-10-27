package com.example.spendly.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =========================================================================
// Google Stitch Exact Design Tokens (Dark OLED & Light)
// =========================================================================

// --- Stitch Dark Palette (Matches Stitch Tailwind config & inline OLED styles) ---
val StitchDarkBackground = Color(0xFF0B0B0E)
val StitchDarkSurface = Color(0xFF0B0B0E)
val StitchDarkContainerLowest = Color(0xFF08080B)
val StitchDarkContainerLow = Color(0xFF111116)
val StitchDarkContainer = Color(0xFF14141A)
val StitchDarkContainerHigh = Color(0xFF1A1A22)
val StitchDarkContainerHighest = Color(0xFF1F1F2A)
val StitchDarkBorder = Color(0xFF23232F)
val StitchDarkOutline = Color(0xFF323242)
val StitchDarkDivider = Color(0xFF23232F)

val StitchDarkOnSurface = Color(0xFFF3F3F7)
val StitchDarkOnSurfaceVariant = Color(0xFF8C8C9C)
val StitchDarkMuted = Color(0xFF636270)

val StitchDarkPrimary = Color(0xFFCABEFE)
val StitchDarkPrimaryGlow = Color(0xFF9E8CFC)
val StitchDarkPrimaryContainer = Color(0xFF2F2858)
val StitchDarkOnPrimary = Color(0xFF1E1346)
val StitchDarkOnPrimaryFixed = Color(0xFF1C0C52)

val StitchDarkSecondary = Color(0xFF7DD9A3)
val StitchDarkSecondaryGlow = Color(0xFF2FE575)
val StitchDarkSecondaryContainer = Color(0xFF113824)
val StitchDarkOnSecondary = Color(0xFF003920)

val StitchDarkTertiary = Color(0xFFFEB956)
val StitchDarkTertiaryGlow = Color(0xFFFFB03A)
val StitchDarkTertiaryContainer = Color(0xFFC18525)
val StitchDarkOnTertiary = Color(0xFF452B00)

val StitchDarkError = Color(0xFFFF7A70)
val StitchDarkErrorGlow = Color(0xFFFF5A5A)
val StitchDarkErrorContainer = Color(0xFF93000A)
val StitchDarkOnError = Color(0xFF690005)

val StitchDarkDockBackground = Color(0xFF07070A)

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
val StitchLightOnSurfaceVariant = Color(0xFF484550)
val StitchLightPrimary = Color(0xFF5E5297)
val StitchLightPrimaryContainer = Color(0xFFE6DEFF)
val StitchLightOnPrimary = Color(0xFFFFFFFF)
val StitchLightSecondary = Color(0xFF376848)
val StitchLightSecondaryContainer = Color(0xFFB6ECC4)
val StitchLightTertiary = Color(0xFF805200)
val StitchLightTertiaryContainer = Color(0xFFFFDDB5)
val StitchLightError = Color(0xFFBA1A1A)
val StitchLightErrorContainer = Color(0xFFFFDAD6)
val StitchLightDockBackground = Color(0xFF1A1B22)

// --- Category Colors (from Stitch Donut Chart & Legend) ---
val StitchCatFood = Color(0xFF9E8CFC)
val StitchCatGroceries = Color(0xFF2FE575)
val StitchCatTransport = Color(0xFFFF5A5A)
val StitchCatEntertainment = Color(0xFFFFB03A)
val StitchCatUtilities = Color(0xFF7058FF)
val StitchCatShopping = Color(0xFFD4953A)
val StitchCatHealth = Color(0xFFFF7A70)
val StitchCatSubscriptions = Color(0xFF8A8A96)
val StitchCatTravel = Color(0xFF5CB884)
val StitchCatOther = Color(0xFF8A8A96)

// --- Signature Gradients from Stitch ---
val HeroCardGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF372B78),
        Color(0xFF4F34A8),
        Color(0xFF2A1B63)
    )
)

val FabGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFDCD4FF),
        Color(0xFFCABEFE),
        Color(0xFF9E8CFC)
    )
)

val ActiveBarGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF9E8CFC),
        Color(0xFF6850D9)
    )
)

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
