# Spendly 

A native Android personal expense tracker built with Jetpack Compose, Material 3, and Room SQLite database.

## Overview
Spendly is an offline-first, private personal finance tracker designed for tracking expenses without any cloud dependency, accounts, or backend. It focuses strictly on spending insights and transaction management.

## Tech Stack
- **Language**: Kotlin 2.3
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: MVVM with Repository pattern
- **Local Storage**: Room 2.7 with SQLite + DataStore Preferences
- **Asynchrony**: Kotlin Coroutines & Flow
- **Navigation**: Navigation 3 Compose with typed destination keys
- **Build System**: Gradle 9 with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`)

## Features
- **Spending Dashboard**: Monthly total spending, spending by category (custom Canvas donut chart), weekly spending bar chart, and recent transaction list.
- **Review Inbox**: Visual indicators and filtering for transactions requiring user confirmation or categorization.
- **Activity Feed**: Chronologically organized transaction feed grouped by date (Today, Yesterday, etc.) with filter chips.
- **Insights**: Monthly and daily averages with linear progress indicators per category.
- **Multi-Currency Support**: Record transactions in multiple currencies (USD, EUR, GBP, INR, etc.) with a configurable default currency in Settings.
- **Transaction Management**: Add and edit transaction amounts, merchants, categories, dates, and notes.

## Design Philosophy
Spendly features a clean, minimal design:
- Neutral background (`#F8F8F6`) with dark ink text.
- Restrained accent colors: **Lavender** (`#8B7FC7`), **Sage** (`#6B9E7A`), and **Amber** (`#D4953A`).
- Flat, subtle card borders (`#E8E8E4`) without artificial heavy shadows.

## Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 17+
- Android SDK Platform 34+

### Building from Command Line
```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest
```
The generated APK is located at `app/build/outputs/apk/debug/app-debug.apk`.

### Running on Emulator
```bash
./gradlew installDebug
adb shell am start -n com.example.spendly/.MainActivity
```
