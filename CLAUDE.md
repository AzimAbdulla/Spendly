# CLAUDE.md

## Project Overview
**Spendly** is a native Android personal finance & expense tracker built with Jetpack Compose, Material 3, and Room SQLite.
- **Privacy First & Offline Only**: No internet permissions (`android.permission.INTERNET` is intentionally omitted), no analytics, no cloud accounts, and zero remote network dependencies. All data is kept strictly on-device in a local Room database.
- **Design Language**: Dark bento UI: flat near-black with opaque charcoal tiles, a monochrome accent with category hues for data, Space Grotesk for figures, Geist for body copy, an inverted floating pill dock, crisp vector icons.
- **Core Capabilities**: Dashboard with custom Canvas charts, Calendar view, Insights & spending breakdown, on-device bank SMS parser/ingestion, multi-currency support, and quick manual transaction logging.

---

## Technical Stack & Toolchain
- **Language**: Kotlin `2.3.20` (`jvmToolchain(17)`)
- **UI Toolkit**: Jetpack Compose (BOM `2026.03.01`) + Material 3 + Material Icons Extended
- **Architecture**: MVVM with Repository Pattern & Kotlin Coroutines / Flow
- **Local Persistence**: Room `2.7.1` + KSP `2.3.12` + DataStore Preferences `1.1.4`
- **Navigation**: AndroidX Navigation 3 (`androidx.navigation3`) with typed `NavKey` serializable objects
- **Build System**: Gradle 9 with Kotlin DSL (`build.gradle.kts`) and Version Catalog (`gradle/libs.versions.toml`)
- **Android Target**: `compileSdk = 36`, `targetSdk = 35`, `minSdk = 26`

---

## Common Development Commands

### Building & Packaging
```bash
# Build debug APK (emulator testing)
./gradlew assembleDebug

# Build the APK for a phone: release, signed with the debug key so it installs over a debug
# build and keeps its data; it starts about 40% faster and animates more smoothly than debug
./gradlew assembleRelease

# Output APK paths:
# app/build/outputs/apk/debug/app-debug.apk
# app/build/outputs/apk/release/app-release.apk (copied to root as Spendly.apk)

# Clean project
./gradlew clean
```

### Running Unit Tests
```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.example.spendly.sms.SmsParserTest"
./gradlew testDebugUnitTest --tests "com.example.spendly.data.SampleDataTest"
```

### Deploying & Testing on Android Device / Emulator
```bash
# Install debug build to running emulator/device
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch main activity
adb shell am start -n com.example.spendly/.MainActivity

# Check runtime logs for Compose errors or crashes
adb logcat -s AndroidRuntime:E ComposeInternal:E

# Take a screenshot via ADB
adb exec-out screencap -p > screenshot.png
```

---

## Codebase Architecture & Structure

```
app/src/main/kotlin/com/example/spendly/
├── SpendlyApplication.kt           # Application class initializing Room DB, Repository, DataStore
├── MainActivity.kt                 # Single Activity with edge-to-edge setup & Navigation entry
├── NavigationKeys.kt               # Serializable NavKey definitions (Home, Calendar, Insights, etc.)
├── Navigation.kt                   # NavDisplay / screen routing & state hoisting
├── data/
│   ├── model/Transaction.kt        # Room Entity: Transaction, Category enum, Source & Status
│   ├── db/
│   │   ├── SpendlyDatabase.kt      # Room database with type converters & pre-population
│   │   ├── TransactionDao.kt       # Room DAO with Flow-based reactive queries
│   │   └── Converters.kt           # Room TypeConverters (LocalDate, Category, Enums)
│   ├── TransactionRepository.kt    # Data layer abstraction exposing reactive transaction streams
│   ├── SpendlyPreferences.kt       # DataStore preferences (currency, dark theme, review counts)
│   └── SampleData.kt               # Seed data for previews and initial state
├── sms/
│   ├── SmsParser.kt                # Regex-based parser for bank SMS; records only debits that name an account (a/c, acct, account), so card, wallet and FASTag alerts are skipped
│   ├── SmsReader.kt                # On-device telephony content provider scanner
│   └── SmsReceiver.kt              # BroadcastReceiver for incoming SMS debits
├── ui/
│   ├── SpendlyViewModel.kt         # Central ViewModel managing state flows, metrics, & mutations
│   ├── home/HomeScreen.kt          # Spending cards, Donut chart, Weekly chart, recent items
│   ├── calendar/CalendarScreen.kt  # Monthly grid, daily spending breakdown, month total
│   ├── insights/InsightsScreen.kt  # Category spending, daily averages, and a trend chart (6 months in Monthly mode, the week's 7 days in Weekly); tap or drag across its bars to see one's spend, starting on the latest
│   ├── settings/SettingsScreen.kt  # Currency picker, SMS sync triggers, data export/reset
│   ├── activity/ActivityScreen.kt  # Chronological transaction log with filters
│   ├── transaction/
│   │   ├── AddTransactionScreen.kt # Full-screen Add Expense page with interactive numpad
│   │   └── EditTransactionScreen.kt# Form for editing/deleting existing expenses
│   ├── components/
│   │   ├── SpendlyCard.kt          # Standard styled surface card matching Stitch design
│   │   ├── StitchBottomDock.kt     # Floating pill bottom navigation bar with center FAB
│   │   ├── TransactionItem.kt      # List row with category badge, relative date & amount
│   │   ├── DonutChart.kt           # Custom Compose Canvas donut chart with touch selection
│   │   ├── CategoryCharts.kt       # Home's by-category chart in 5 styles (donut, stacked bar, tiles, rings, half donut), picked in Settings
│   │   └── WeeklyBarChart.kt       # Custom Compose Canvas 7-day spending bar chart
│   └── theme/
│       ├── Color.kt                # Dark bento tokens (`#050506` base, `#141416` tiles, monochrome accent)
│       ├── Theme.kt                # Custom SpendlyTheme with SpendlyThemeColors LocalComposition
│       ├── Type.kt                 # Typography definition with Space Grotesk & Geist
│       └── SpendlyIcons.kt         # Vector icon helpers & category color mapping
```

---

## Coding Guidelines & Style Conventions

### 1. Minimal & Surgical Changes
- Touch only the lines directly related to the user request or bug.
- Do not add speculative abstractions, unrequested config options, or dead code.
- Match existing formatting, imports, and naming patterns.

### 2. Jetpack Compose & UI Best Practices
- **Edge-to-Edge & Status Bar Insets**: To prevent double padding / top gaps across screens, `Scaffold` must use:
  ```kotlin
  Scaffold(
      contentWindowInsets = WindowInsets(0.dp)
  ) { innerPadding ->
      LazyColumn(
          modifier = Modifier
              .fillMaxSize()
              .statusBarsPadding()
      ) { ... }
  }
  ```
  Never stack both `innerPadding.calculateTopPadding()` and `statusBarsPadding()`.
- **Custom Typography Pairing**:
  - `SpaceGroteskFontFamily`: Used for tabular numeric values, currency amounts (`-$₹450.00`), headlines, and stat callouts.
  - `GeistFontFamily`: Used for body copy, category labels, button text, and captions.
- **Dark bento design** (direction E, picked 2026-11-04; the Stitch "Liquid Luminescence" folder is now only a reference):
  - Flat backgrounds with no wallpaper glow: near-black `#050506` (Dark), `#000000` (Black), `#ECEEF3` (Light). Screen `Scaffold`s keep `containerColor = Color.Transparent`
  - Content lives in opaque tiles: `cardBackground` / `container` `#141416` dark and white in light, 24dp corners, no borders or rims (`glassRimBrush()` is transparent now)
  - Home is a bento grid: a wide total tile (smaller decimals, expenses and daily average), a top-category ring tile beside a "this week" bars tile (it starts on today; tapping a day, or dragging sideways across the bars, shows that day's spend in its header with the week's total underneath), the By category card (style from Settings), then recent expenses
  - Accent is monochrome: `colors.primary` is near-white in dark and near-black in light, with `onPrimary` the opposite, for buttons, the dock and selection
  - Tinted tiles (picked 2026-11-04): each kind of tile mixes its own shade from one hue with `colors.tint(hue)` (deep in the dark themes, pastel in light) and is drawn with `TintedTile`, whose content gets tinted secondary text, tracks, dividers and accent from `SpendlyTheme.colors`. Navy `TintMoney` for totals and each screen's main tile (Home total, Calendar grid and month total, Insights "Spent"), amber `TintTime` for trends (teal `TintTimeAlt` when an amber category tile sits beside it), plum `TintList` for expense lists, green `TintSafe` for Add expense (a deep green screen, mint in light, with fields and keys a step lighter, the amount a step lighter again and a green Save), and a category's own hue for category tiles (top-category ring, category detail list, the selected Add tile, Categories row icons). On Add expense the chosen category's colour also marks its name and the Item icon. Charts, settings groups, fields and keys stay on neutral charcoal
  - The dock matches the tiles: a charcoal pill with a hairline edge in the dark themes, white in light (`dockBackground`, `dockBorder`, `dockContent`, `dockContentSelected`, `dockPill`); the green + disc (`dockAction` = `TintSafe`, `onDockAction` = `OnTintSafe`) is its one bright control. It's drawn with `GlassSurface` and a `tint` override
  - A background-coloured fade rises behind the dock, so content fades out instead of sliding under it. Every scrolling screen the dock shows on ends with `Spacer(Modifier.navigationBarsPadding().height(DockClearance))`, so its last item clears both
  - Popups, menus and dialogs use the opaque `colors.popup`. Selected pills use `colors.glassSelected`. Use `GlassSegmentedControl` for small option sets
  - Big figures use Space Grotesk Bold; labels and body use the default body font in sentence case
- **Themes**: Auto, Light, Dark and Black (`themeMode` "SYSTEM" / "LIGHT" / "DARK" / "BLACK"). Black is `BlackSpendlyColors`: Dark's tokens on true #000000 with no wallpaper glow and neutral glass, for OLED screens; Auto follows the system between Light and Dark
- **Motion** (`ui/theme/Motion.kt`): 150 / 250 / 400ms; arrivals decelerate, exits accelerate, only touch feedback springs
  - Screens are transparent over one wallpaper, so they fade through (`Motion.fadeOutFirst`, then `Motion.fadeInAfter`) and never crossfade at full opacity
  - Add expense grows out of the + button and shrinks back into it: `CircleReveal` clips the entry to a circle driven by its NavDisplay transition (so the back gesture scrubs it), starting in the button's colour; the entry's `RevealMotion` keeps the screen beneath in place. The dock reports where the + was tapped
  - Home's total counts up or down to its new value (`animateAmountAsState`; a per-digit rolling version broke baseline alignment on a real phone and was removed). Saving on Add expense folds the Save button into a tick, then the screen closes 480ms later (the expense is saved on the tap). A new row in `TransactionRows` grows in with a green wash that fades
  - Data animations (`animateAmountAsState`, `rememberAnimatedValues`, `TransactionRows`) wait `Motion.AfterScreen` and keep what they last showed in the screen's `ShownMemory` (one per back-stack entry, set up in `Navigation.kt`), so coming back animates only what changed
  - Opening the app: Home's four tiles fade in and lift 12dp into place, 50ms apart, once per app start (`rememberSoftRise` in `HomeScreen.kt`), while its totals count up and charts grow from zero. The rise and those first animations wait on `LaunchGate` until the splash has gone; `MainActivity` replaces the system's slow splash exit with a quick fade and opens the gate (it opens by itself after 3s)
  - Animate opacity, offset and scale only. The emulator renders in software, so judge smoothness on a real phone
  - Playful goo effects (`ui/components/Goo.kt`, after Skiper UI's Apple squircle effect: blur a layer, then cut it back to a hard edge, so corners come out squircle and shapes that come close melt together). Android 12+ only; earlier phones keep the motion without the goo
    - The dock's pill moves like liquid, with a tail that pulls out behind it (`LiquidPill` in `StitchBottomDock.kt`, in its own layer under the tabs)
    - Home's total, ring and week tiles squish under a finger (`rememberSquish`, `squish`): a touch response only, with no click action
    - Keep goo away from text on the bare background, since it smears; text inside opaque tiles is fine because the tile keeps its alpha solid
    - To test the motion on the emulator, slow it with `adb shell settings put global animator_duration_scale 10`, and remove that with `adb shell settings delete global animator_duration_scale` afterwards
- **Weights & case**: Regular and Medium for text; Bold only for big figures (totals, ring percentages, the month title). Sentence case everywhere
- **Spacing**: 4dp grid; list rows 56dp; card padding 16 to 20dp
- **Signature detail**: transactions read from bank SMS carry a small muted SMS mark in `TransactionItem`; don't add other decorative markers
- **No Emojis for UI Elements**: Use clean Material vector icons (e.g., via `SpendlyIcons.kt` or `transaction.category.getIcon()`) rather than emoji strings.

### 3. Date & Time Formats (Important Gotcha)
- In `Transaction.kt`, the date field is `java.time.LocalDate` (no time/clock component).
- **Never** format `transaction.date` with hour/minute patterns such as `h:mm a` or `HH:mm` (this causes `java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: ClockHourOfAmPm`).
- Format `LocalDate` with date patterns only: `d MMM`, `d MMM yyyy`, `EEEE, MMM d`, or relative labels (`Today`, `Yesterday`).
- Time of day lives in its own field, `transaction.time` (`LocalTime?`, added in DB v3; null for rows saved earlier, which show the date alone). Clock patterns (`h:mm a`, `HH:mm`) go on `transaction.time` only.
- Transaction lists are newest first (`ORDER BY date DESC, time DESC, id DESC` in `TransactionDao`); screens rely on that order, so filter or group the list rather than re-sorting it.

### 4. Home Screen Widgets (Jetpack Glance)
- Five widgets in `widget/`: Overview (category amounts under the split), This week (amounts over each bar, today green, the peak amber, a badge against last week), Recent (amount over time, a review pill, payees from SMS and View all), Quick add, and a 3x2 Today (spend against the daily average with a pace bar, green under 80% of it, amber up to it and red once over, and today's expenses). The 6x2 ones are declared (`targetCellWidth/Height`, `minWidth` 390dp) so One UI can stack them in one spot, and resizable for 4 and 5 column grids. Layouts adapt using `LocalSize` (`SizeMode.Exact`).
- One UI's 6x2 cell is about 350x190dp, taller than the 150dp the compact layouts need, so type, the ring, bars and buttons grow with `roominess()` (0 at 150dp, 1 by 200dp) and spacers push content to the edges. Check text against `textWidth()`, which measures in the system font the launcher uses, rather than guessing character widths.
- Colours come from the app's tile tints on the dark or light base, and every widget has a hairline edge (`widget_outline_dark` / `_light`, colour baked in: a Glance colour filter would tint the transparent middle too) so it shows at 100% transparency; settings live in `SpendlyPreferences` (`widget*`) and apply to every widget.
- Widget settings (`WidgetSettingsActivity`) save as they change, including mid-drag, and the widget colour style is one of `WidgetColour` (Tinted: a wash per widget that stays at 45% or more even at full transparency; Data: plain, colour only in the data; Figures: plain, coloured big figures). `SpendlyApplication.refreshWidgets` folds a burst of changes into one refresh after 100ms, and `updateSpendlyWidgets` redraws all widgets in parallel
- Widgets reload their data inside `provideContent` whenever `widgetTick` changes (`provide` in `SpendlyWidgets.kt`): Glance keeps a session alive for about 45s and an update to a live session doesn't run `provideGlance` again, so data read only there goes stale.
- `SpendlyApplication` updates all widgets whenever transactions or preferences change, including SMS imports; `updatePeriodMillis` is hourly for the date rolling over.
- Taps open the app through `WidgetTarget` extras on `MainActivity` (Home, Insights weekly, Needs review, Add with a category) or `EXTRA_TRANSACTION_ID` for an expense.
- Glance rows and columns hold at most 10 children: put gaps inside items (padding on a wrapper) rather than as spacers. Widgets can't draw freely, so the ring is a bitmap and bars are sized boxes.
- Widget vector drawables: write arc flags out in full (`0 0 0 20 4`, not `0020 4`); RemoteViews leaves the icon blank otherwise.

### 5. Privacy & Permissions
- Do not request `android.permission.INTERNET`.
- Only SMS permissions (`android.permission.RECEIVE_SMS`, `android.permission.READ_SMS`) are declared for local regex parsing of bank notifications.
- `android.permission.POST_NOTIFICATIONS` is declared only for the local alert `SmsNotifier` posts when a bank SMS expense is recorded; tapping it opens that expense.
