package com.example.spendly.ui.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// Google Stitch Exact Design Tokens (Dark OLED & Light)
// =========================================================================

// --- Dark bento palette: flat near-black, opaque charcoal tiles, white accent ---
val StitchDarkBackground = Color(0xFF050506)
val StitchDarkSurface = Color(0xFF050506)
val StitchDarkContainerLowest = Color(0xFF0C0C0E)
val StitchDarkContainerLow = Color(0xFF101012)
val StitchDarkContainer = Color(0xFF141416)
val StitchDarkContainerHigh = Color(0xFF1E1F22)
val StitchDarkContainerHighest = Color(0xFF27282C)
val StitchDarkBorder = Color(0x00000000) // bento: no outlines
val StitchDarkOutline = Color(0xFF2E3034)
val StitchDarkDivider = Color(0xFF1C1D20)

val StitchDarkOnSurface = Color(0xFFF4F4F5)
val StitchDarkOnSurfaceVariant = Color(0xFF9A9EA6)
val StitchDarkMuted = Color(0xFF6E737C)

val StitchDarkPrimary = Color(0xFFF4F4F5)
val StitchDarkPrimaryGlow = StitchDarkPrimary
val StitchDarkPrimaryContainer = Color(0xFF2A2B2F)
val StitchDarkOnPrimary = Color(0xFF050506)
val StitchDarkOnPrimaryFixed = Color(0xFF050506)

val StitchDarkSecondary = Color(0xFF3FBF85)
val StitchDarkSecondaryGlow = StitchDarkSecondary
val StitchDarkSecondaryContainer = Color(0xFF10291D)
val StitchDarkOnSecondary = Color(0xFF003920)

val StitchDarkTertiary = Color(0xFFE8B061)
val StitchDarkTertiaryGlow = StitchDarkTertiary
val StitchDarkTertiaryContainer = Color(0xFF2A2116)
val StitchDarkOnTertiary = Color(0xFF452B00)

val StitchDarkError = Color(0xFFE5686B)
val StitchDarkErrorGlow = StitchDarkError
val StitchDarkErrorContainer = Color(0xFF2E1617)
val StitchDarkOnError = Color(0xFF690005)

val StitchDarkDockBackground = Color(0xFF1B1B1E)

// --- Stitch Light Palette ---
val StitchLightBackground = Color(0xFFECEEF3)
val StitchLightSurface = Color(0xFFECEEF3)
val StitchLightCardSurface = Color(0xFFFFFFFF)
val StitchLightContainerLowest = Color(0xFFFFFFFF)
val StitchLightContainerLow = Color(0xFFF4F5F8)
val StitchLightContainer = Color(0xFFFFFFFF)
val StitchLightContainerHigh = Color(0xFFE7E9EE)
val StitchLightBorder = Color(0x00000000) // bento: no outlines
val StitchLightDivider = Color(0xFFECEEF1)
val StitchLightOnSurface = Color(0xFF14161A)
val StitchLightOnSurfaceVariant = Color(0xFF5B6270)
val StitchLightPrimary = Color(0xFF14161A)
val StitchLightPrimaryContainer = Color(0xFFDCDDE2)
val StitchLightOnPrimary = Color(0xFFFFFFFF)
val StitchLightSecondary = Color(0xFF2F9E6B)
val StitchLightSecondaryContainer = Color(0xFFB6ECC4)
val StitchLightTertiary = Color(0xFF805200)
val StitchLightTertiaryContainer = Color(0xFFFFDDB5)
val StitchLightError = Color(0xFFBA1A1A)
val StitchLightErrorContainer = Color(0xFFFFDAD6)
val StitchLightDockBackground = Color(0xFFFFFFFF)

// --- Glass helpers: blur stays for the dock; rims and wallpaper glow are off in the bento look ---
val GlassDarkTint = Color(0x610C0E10)
val GlassDarkFallback = Color(0xF00C0E10)
val GlassDarkRimTop = Color(0x00FFFFFF)
val GlassDarkRimBottom = Color(0x00FFFFFF)
val GlassDarkSelected = Color(0x24FFFFFF)
val GlassDarkPopup = Color(0xFF1A1B1E)
val GlassDarkWallpaperBlobs = emptyList<Color>()

val GlassLightTint = Color(0xADF8F8FB)
val GlassLightFallback = Color(0xF5F8F8FB)
val GlassLightRimTop = Color(0x00FFFFFF)
val GlassLightRimBottom = Color(0x00000000)
val GlassLightSelected = Color(0x14000000)
val GlassLightPopup = Color(0xFFFFFFFF)
val GlassLightWallpaperBlobs = emptyList<Color>()

// --- Tile tints (bento): each kind of tile mixes its own shade from one hue ---
val TintMoney = Color(0xFF5B7BE6)   // navy: totals and each screen's main tile
val TintTime = Color(0xFFEF9F27)    // amber: week and month trends
val TintTimeAlt = Color(0xFF5FB3B0) // teal: a trend tile that sits beside an amber category tile
val TintList = Color(0xFF4FA3D9)    // sky blue: expense lists
val TintSafe = Color(0xFF3FBF85)    // green: privacy, and Add expense with the + that opens it
val OnTintSafe = Color(0xFF06140D)  // text and icons on a TintSafe fill (the + button, Save)

// --- Category Colors (muted, one hue per category; used only in charts and dots) ---
val StitchCatFood = Color(0xFFEF9F27)
val StitchCatGroceries = Color(0xFF3FBF85)
val StitchCatTransport = Color(0xFF5B9BEA)
val StitchCatEntertainment = Color(0xFFC48BD6)
val StitchCatUtilities = Color(0xFFD9C46A)
val StitchCatShopping = Color(0xFFD98F7A)
val StitchCatHealth = Color(0xFFE5848A)
val StitchCatSubscriptions = Color(0xFFA1A1AA)
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
