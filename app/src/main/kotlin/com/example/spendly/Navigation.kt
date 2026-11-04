package com.example.spendly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.activity.ActivityScreen
import com.example.spendly.ui.calendar.CalendarScreen
import com.example.spendly.ui.categories.CategoriesScreen
import com.example.spendly.ui.categories.CategoryDetailScreen
import com.example.spendly.data.model.Category
import java.time.YearMonth
import com.example.spendly.ui.components.GlassWallpaper
import com.example.spendly.ui.components.LocalGlassBackdrop
import com.example.spendly.ui.components.StitchBottomDock
import com.example.spendly.ui.components.glassBackdropSource
import com.example.spendly.ui.components.rememberGlassBackdrop
import com.example.spendly.ui.components.StitchNavDestination
import com.example.spendly.ui.home.HomeScreen
import com.example.spendly.ui.insights.InsightsScreen
import com.example.spendly.ui.settings.SettingsScreen
import com.example.spendly.ui.transaction.AddTransactionScreen
import com.example.spendly.ui.transaction.EditTransactionScreen

@Composable
fun MainNavigation(viewModel: SpendlyViewModel? = null) {
    val context = LocalContext.current.applicationContext as SpendlyApplication
    val vm: SpendlyViewModel = viewModel ?: viewModel(
        factory = SpendlyViewModel.provideFactory(context.repository, context.preferences)
    )

    val transactions by vm.transactions.collectAsStateWithLifecycle()
    val recentTransactions by vm.recentTransactions.collectAsStateWithLifecycle()
    val monthlyTotal by vm.monthlyTotal.collectAsStateWithLifecycle()
    val reviewCount by vm.reviewCount.collectAsStateWithLifecycle()
    val categoryTotals by vm.categoryTotals.collectAsStateWithLifecycle()
    val weeklySpending by vm.weeklySpending.collectAsStateWithLifecycle()
    val defaultCurrency by vm.defaultCurrency.collectAsStateWithLifecycle()
    val smsAutoSave by vm.smsAutoSave.collectAsStateWithLifecycle()
    val isScanningSms by vm.isScanningSms.collectAsStateWithLifecycle()
    val themeMode by vm.themeMode.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(HomeDestination)

    val currencySymbol = when (defaultCurrency) {
        "INR" -> "₹"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }

    val currentKey = backStack.lastOrNull()
    val showDock = currentKey !is EditTransactionDestination && currentKey !is AddTransactionDestination

    val currentDest = when (currentKey) {
        CalendarDestination -> StitchNavDestination.CALENDAR
        InsightsDestination -> StitchNavDestination.INSIGHTS
        SettingsDestination -> StitchNavDestination.SETTINGS
        else -> StitchNavDestination.HOME
    }

    val backdrop = rememberGlassBackdrop()

    CompositionLocalProvider(LocalGlassBackdrop provides backdrop) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper + screens are recorded so the floating dock can blur them
            Box(modifier = Modifier.fillMaxSize().glassBackdropSource(backdrop)) {
                GlassWallpaper(modifier = Modifier.fillMaxSize())
                NavDisplay(
                    backStack = backStack,
                    onBack = {
                        if (backStack.size > 1) {
                            backStack.removeLastOrNull()
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    entryProvider = entryProvider {
                        entry<HomeDestination> {
                            HomeScreen(
                                onAddTransaction = { backStack.add(AddTransactionDestination()) },
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onReviewClick = {
                                    backStack.add(ActivityDestination)
                                },
                                onViewAllClick = {
                                    backStack.add(ActivityDestination)
                                },
                                onCategoriesClick = {
                                    backStack.add(CategoriesDestination)
                                },
                                monthlyTotal = monthlyTotal,
                                transactionCount = transactions.size,
                                recentTransactions = recentTransactions,
                                reviewCount = reviewCount,
                                weeklySpending = weeklySpending,
                                weekDayLabels = vm.weekDayLabels,
                                categoryTotals = categoryTotals,
                                defaultCurrency = defaultCurrency
                            )
                        }
                        entry<CalendarDestination> {
                            CalendarScreen(
                                transactions = transactions,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<InsightsDestination> {
                            InsightsScreen(
                                transactions = transactions,
                                currencySymbol = currencySymbol,
                                defaultCurrency = defaultCurrency
                            )
                        }
                        entry<SettingsDestination> {
                            SettingsScreen(
                                defaultCurrency = defaultCurrency,
                                onCurrencySelected = { vm.setDefaultCurrency(it) },
                                smsAutoSave = smsAutoSave,
                                onSmsAutoSaveChanged = { vm.setSmsAutoSave(it) },
                                isScanningSms = isScanningSms,
                                onScanSms = { onResult -> vm.scanSmsInbox(context, onResult) },
                                onClearAllData = { vm.clearAllData() },
                                onLoadSampleData = { vm.loadSampleData() },
                                themeMode = themeMode,
                                onThemeModeSelected = { vm.setThemeMode(it) }
                            )
                        }
                        entry<CategoriesDestination> {
                            CategoriesScreen(
                                transactions = transactions,
                                onCategoryClick = { cat, month ->
                                    backStack.add(CategoryDetailDestination(cat.name, month.toString()))
                                },
                                onBack = { backStack.removeLastOrNull() },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<CategoryDetailDestination> { key ->
                            CategoryDetailScreen(
                                category = Category.valueOf(key.categoryName),
                                initialMonth = YearMonth.parse(key.yearMonth),
                                transactions = transactions,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onBack = { backStack.removeLastOrNull() },
                                currencySymbol = currencySymbol
                            )
                        }
                        entry<ActivityDestination> {
                            ActivityScreen(
                                transactions = transactions,
                                onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<AddTransactionDestination> { key ->
                            AddTransactionScreen(
                                prefillAmount = key.prefillAmount,
                                defaultCurrency = defaultCurrency,
                                onSaved = { amt, curr, merch, cat, notes, item ->
                                    vm.addTransaction(amt, curr, merch, cat, notes, item = item)
                                    backStack.removeLastOrNull()
                                },
                                onCancel = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<EditTransactionDestination> { key ->
                            val tx = transactions.find { it.id == key.transactionId }
                            EditTransactionScreen(
                                transactionId = key.transactionId,
                                transaction = tx,
                                onSaved = { updated ->
                                    vm.updateTransaction(updated)
                                    backStack.removeLastOrNull()
                                },
                                onDelete = { id ->
                                    vm.deleteTransaction(id)
                                    backStack.removeLastOrNull()
                                },
                                onCancel = { backStack.removeLastOrNull() }
                            )
                        }
                    }
                )
            }

            // Floating Bottom Dock (matching Stitch 1:1)
            if (showDock) {
                StitchBottomDock(
                    currentDestination = currentDest,
                    onNavigate = { dest ->
                        val targetKey: NavKey = when (dest) {
                            StitchNavDestination.HOME -> HomeDestination
                            StitchNavDestination.CALENDAR -> CalendarDestination
                            StitchNavDestination.INSIGHTS -> InsightsDestination
                            StitchNavDestination.SETTINGS -> SettingsDestination
                        }
                        while (backStack.size > 1) backStack.removeLastOrNull()
                        if (backStack.lastOrNull() != targetKey) {
                            backStack.removeLastOrNull()
                            backStack.add(targetKey)
                        }
                    },
                    onFabClick = { backStack.add(AddTransactionDestination()) },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
