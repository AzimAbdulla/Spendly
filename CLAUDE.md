# CLAUDE.md

## Project Overview
**Spendly** is a native Android personal finance & expense tracker built with Jetpack Compose, Material 3, and Room SQLite.
- **Privacy First & Offline First**: No analytics and no cloud accounts. `android.permission.INTERNET` exists for one thing only, downloading a public exchange-rates file (`data/Rates.kt`); no request may ever carry user data, and every feature must work the same with no connection. All data is kept strictly on-device in a local Room database.
- **Design Language**: Dark bento UI: flat near-black with opaque charcoal tiles, a monochrome accent with category hues for data, Space Grotesk for figures, Geist for body copy, an inverted floating pill dock, crisp vector icons.
- **Core Capabilities**: Home dashboard with a monthly budget, a searchable Activity list, a shaded Calendar, Insights, on-device bank SMS import with one-tap review, CSV export and import, and quick manual entry. Every expense keeps its own currency; totals are shown in one display currency.

---

## Technical Stack & Toolchain
- **Language**: Kotlin `2.3.20` (`jvmToolchain(17)`)
- **UI Toolkit**: Jetpack Compose (BOM `2026.03.01`) + Material 3 + Material Icons Extended
- **Architecture**: MVVM with Repository Pattern & Kotlin Coroutines / Flow
- **Local Persistence**: Room `2.7.1` + KSP `2.3.12`; settings in `SharedPreferences` (`SpendlyPreferences`)
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
# build and keeps its data; it starts about 40% faster and animates more smoothly than debug.
# R8 stays off: shrinking to 3 MB merged the five Glance widget classes into one (every widget
# showed the same thing) and broke WorkManager, and the size wasn't worth that risk
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
├── SpendlyApplication.kt           # Application class initializing Room DB, Repository, preferences
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
│   ├── SpendlyPreferences.kt       # SharedPreferences: currency, theme, monthly budget, widget options, and the category learned for each SMS payee
│   ├── Currency.kt                 # `currencySymbol(code)`: the only place a currency code becomes a symbol
│   ├── Rates.kt                    # Exchange rates: `Fx` (display currency + latest table, converts an expense for display), `Rates` (cache, daily refresh, a past day's rate), `RateSource`, `supportedCurrencies`
│   ├── CsvBackup.kt                # CSV export and import (Settings > Data); ids are not written, and an import skips expenses already here
│   └── SampleData.kt               # Seed data for previews and initial state
├── sms/
│   ├── SmsParser.kt                # Regex-based parser for bank SMS; records only debits that name an account (a/c, acct, account), so card, wallet and FASTag alerts are skipped. Also `inferCategory` (whole-word keywords, the payee first), `isBankSender` (ordinary phone numbers are ignored) and `smsKey` (sent time + text, stored on the expense so one SMS is never recorded twice)
│   ├── SmsReader.kt                # "Scan past SMS": reads the last 90 days of the inbox
│   └── SmsReceiver.kt              # BroadcastReceiver for incoming SMS debits; does nothing when "Read bank SMS" is off, and never logs a message
├── ui/
│   ├── SpendlyViewModel.kt         # Central ViewModel managing state flows, metrics, & mutations
│   ├── home/HomeScreen.kt          # Month header with a Settings button, total tile with the budget bar and reminders at its foot (SMS expenses to check, money owed to you), top-category ring, week tile, top three categories, the month's recent expenses
│   ├── calendar/CalendarScreen.kt  # Monthly grid with each day shaded by its spend (four steps, `heatLevels` / `heatRamp` from Insights), the selected day's expenses with "Add an expense for this day", month total
│   ├── insights/InsightsScreen.kt  # One "Spent" tile (total, daily average, top category), category spending, and a trend chart (6 months in Monthly mode, the week's 7 days in Weekly); tap or drag across its bars to see one's spend, starting on the latest. Weekly mode adds "When you spend" (`WeekHeatmap`): a column per day and a row per part of the day, expenses without a time left out; squares take four steps of one hue by quartile, tap one to show it in the header, press and hold for a popup (`HeatPopup`) that follows the finger. The grid and the trend chart share teal `timeHue`
│   ├── settings/SettingsScreen.kt  # Opened from Home's header: currency, theme, monthly budget, widgets, SMS, export / import / delete. "Load sample data" shows in debuggable builds only
│   ├── activity/ActivityScreen.kt  # A dock tab: every expense by day with day totals, search, filter pills (needs review, each category), "Confirm" on SMS rows and "Confirm all"
│   ├── transaction/
│   │   └── AddTransactionScreen.kt # Add expense (item, date, amount keypad, category); the same screen edits an expense when given `editing`, with a delete button. It must fit one screen without scrolling: on a short phone it measures its overflow and takes it back from the keys, gaps and padding (`squeeze`, up to `MaxSqueeze`)
│   ├── components/
│   │   ├── SpendlyCard.kt          # Standard styled surface card matching Stitch design
│   │   ├── StitchBottomDock.kt     # Floating pill dock: Home, Activity, the + button, Calendar, Insights
│   │   ├── TransactionItem.kt      # List row with category badge, relative date & amount
│   │   ├── CategoryColors.kt       # `stitchCategoryColors`: each category's hue
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
  - Home's header is "Month Year" between round arrows that step a month (next stops at the current month), as on Calendar; tapping the name opens `MonthPickerMenu` to jump further. The month is one value in the view model, shared by Home, Calendar, Insights and Categories
  - Home is a bento grid: a wide total tile ("Spent", the total in whole rupees, expenses and daily average muted, then with a budget set `BudgetBar`: the bar, a thin white pace mark with "Day N" above it in the current month, "₹X left of ₹Y" and "₹Z over pace" in amber or "under pace" in green; the reminders are chips at its foot), a top-category ring tile beside a "this week" bars tile (it starts on today; tapping a day, or dragging sideways across the bars, shows that day's spend in its header with the week's total underneath), a By category card with the three biggest, then the month's recent expenses
  - Accent is monochrome: `colors.primary` is near-white in dark and near-black in light, with `onPrimary` the opposite, for buttons, the dock and selection
  - Tile colours (one emerald, picked 2026-11-14 after navy tiles in many colours read as too busy): each screen has one emerald main tile, `TintedTile(colors.tint(Emerald))` (Home total, Calendar grid, Insights "Spent"); `Emerald` is `TintSafe`, the colour of the + button and Add expense. Every other tile on Home, Activity, Calendar and Insights is plain charcoal, `colors.quiet`. Emerald also marks the ring, the selected week bar, Calendar's heat steps and the Insights trend. Category bars on Home and Insights are shades of emerald by rank (`rankShade`), while row icons and dots keep each category's own colour. Owed stays amber, and the "Confirm" and "Paid" pills are green. Add expense keeps its deep green screen (mint in light), where the chosen category's colour marks its name and the Item icon. The category detail screen keeps the category's own hue. Settings stays charcoal. Widgets keep their own tints (`WidgetStyle`: `TintMoney`, `TintTime`, `TintList`)
  - The dock matches the tiles: a charcoal pill with a hairline edge in the dark themes, white in light (`dockBackground`, `dockBorder`, `dockContent`, `dockContentSelected`, `dockPill`); the green + disc (`dockAction` = `TintSafe`, `onDockAction` = `OnTintSafe`) is its one bright control. It's drawn with `GlassSurface` and a `tint` override
  - A background-coloured fade rises behind the dock, so content fades out instead of sliding under it. Every scrolling screen the dock shows on ends with `Spacer(Modifier.navigationBarsPadding().height(DockClearance))`, so its last item clears both
  - Popups, menus and dialogs use the opaque `colors.popup`. Selected pills use `colors.glassSelected`. Use `GlassSegmentedControl` for small option sets
  - Big figures use Space Grotesk Bold; labels and body use the default body font in sentence case
- **Themes**: Auto, Light, Dark and Black (`themeMode` "SYSTEM" / "LIGHT" / "DARK" / "BLACK"). Black is `BlackSpendlyColors`: Dark's tokens on true #000000 with no wallpaper glow and neutral glass, for OLED screens; Auto follows the system between Light and Dark
- **Motion** (`ui/theme/Motion.kt`): 150 / 250 / 400ms; arrivals decelerate, exits accelerate, only touch feedback springs
  - Screens are transparent over one wallpaper, so they fade through (`Motion.fadeOutFirst`, then `Motion.fadeInAfter`) and never crossfade at full opacity
  - Add expense grows out of the + button and shrinks back into it: `CircleReveal` clips the entry to a circle driven by its NavDisplay transition (so the back gesture scrubs it), starting in the button's colour; the entry's `RevealMotion` keeps the screen beneath in place. The dock reports where the + was tapped
  - Home's total counts up or down to its new value (`animateAmountAsState`; a per-digit rolling version broke baseline alignment on a real phone and was removed). Saving on Add expense folds the Save button into a tick, then the screen closes 480ms later (the expense is saved on the tap). A new row in `TransactionRows` grows in with a green wash that fades; the wash runs to the tile's edges (`flashBleed` covers the tile's padding above the first row and below the last)
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
- Add and edit both set the date with a date picker (never in the future). An expense added for today gets the time; a backdated one has none. "Today" comes from `SpendlyViewModel.today`, refreshed when the app returns to the front.
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
- `android.permission.INTERNET` is only for `Rates.kt`'s GET of a public rates file. Never send anything about the user or their expenses, and never make a feature wait on the network.
- **Currencies** (DB v7): an expense keeps `amount` and `currency` as paid. The display currency is `SpendlyPreferences.defaultCurrency`; a new expense starts in `spendingCurrency`, the last one used (the symbol in Add expense's amount opens `CurrencySheet`). An expense in another currency stores `rate` (what one of its currency was worth in `rateCurrency`, the display currency when saved) and `rateSource`: `ESTIMATED` (the latest rate on the phone, used when saving so nothing waits on the network), `FETCHED` (that day's rate, set once by `TransactionRepository.settleRates` and never changed again) or `MANUAL` (typed in the sheet, never overwritten). `Rates` keeps the latest table (per US dollar, with `Fx.Bundled` for a first run offline), refreshes it at most daily from the fawazahmed0 currency API on jsDelivr (open.er-api.com as a fallback) and fetches a past day's rate by date. `SpendlyApplication.syncRates()` runs both on start and after saves; failures are silent. `TransactionRepository.getAllTransactions()` passes every row through `Fx.inDisplay`, so in memory `rate` always means "display currency per one of its own" and `spend`, `owedAmount` and `converted` are in the display currency; `ownSpend` and `ownOwed` are in the expense's currency (row captions). Changing the display currency rewrites nothing: a rate saved for another display currency is carried across at today's rate. Stored rows (`getStored`, `getAllStored`) are what edits, Undo and CSV export use. Rows show the amount as paid, with "≈ ₹3,435" under it when it's in another currency. Home's total tile leads with what was paid in the display currency itself (count and daily average too), then a line per other currency ("AED 150.00 ≈ ₹3,844", `SpendlyViewModel.monthAbroad`); the budget bar, charts, Calendar and widgets count everything converted. A foreign dollar amount shows its code (`foreignSymbol`). The Overview widget does the same in short: rupees as the big figure, then one line "+ AED 150 · USD 20 ≈ ₹5,692" (`WidgetData.abroad`); the category names drop off below 170dp of height to make room. Recent shows a foreign expense as paid; Today, This week and the category split are converted. Nothing is rounded before totals are summed. The monthly budget is a plain figure in the display currency and isn't converted when that changes
- `allowBackup` is off and `data_extraction_rules.xml` excludes everything, so nothing reaches cloud backup or a device transfer; CSV export is how data moves.
- **Who an expense is for** (`Sharing` in `Transaction.kt`, DB v6): the chip beside the date on Add / Edit opens `WhoIsThisForSheet` with four answers. Just me (default); Split the bill (`myShare` set, only the share counts, the rest is owed; "Split between − N people +" fills in the equal share, and typing over it keeps the typed share, marked "edited" with the equal share beside it. The count isn't stored: an existing split reopens on the count its share is an equal part of, else 2); They'll pay me back (`owed`, counts in full until paid); Already paid back (`paidBack`, counts nothing). Every total sums `Transaction.spend`, never `amount`; `owedAmount` is what is still to come back. Marking paid (`markPaid`, or "They've paid me back" at the top of the sheet) clears `owed` and sets `paidBack` unless it is a split. Rows keep the full amount with one caption ("₹1,100 owed", "Owed back" in amber; "My share ₹400", "Paid back" muted), Home's total tile ends with "₹X owed to you" while anything is owed (beside "N to check" for SMS expenses, each opening its Activity filter), and it opens Activity on the "Owed" filter, where each row has a "Paid" pill. Counts use `spend > 0`
- Amounts are spending, shown without a minus sign. Percentages round with `roundToInt()` everywhere.
- Deleting an expense shows an Undo pill for 5 seconds (`Navigation.kt`); saving an edit confirms the expense and remembers the payee's category for later SMS.
- Only SMS permissions (`android.permission.RECEIVE_SMS`, `android.permission.READ_SMS`) are declared for local regex parsing of bank notifications.
- `android.permission.POST_NOTIFICATIONS` is declared only for the local alert `SmsNotifier` posts when a bank SMS expense is recorded; tapping it opens that expense.
