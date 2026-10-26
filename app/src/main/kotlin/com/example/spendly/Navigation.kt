package com.example.spendly

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
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
import com.example.spendly.ui.components.StitchBottomDock
import com.example.spendly.ui.components.StitchNavDestination
import com.example.spendly.ui.home.HomeScreen
import com.example.spendly.ui.insights.InsightsScreen
import com.example.spendly.ui.settings.SettingsScreen
import com.example.spendly.ui.transaction.AddTransactionScreen
import com.example.spendly.ui.transaction.EditTransactionScreen
import com.example.spendly.ui.transaction.QuickAddExpenseSheet

@Composable
fun MainNavigation() {
    val context = LocalContext.current.applicationContext as SpendlyApplication
    val viewModel: SpendlyViewModel = viewModel(
        factory = SpendlyViewModel.provideFactory(context.repository, context.preferences)
    )

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val reviewCount by viewModel.reviewCount.collectAsStateWithLifecycle()
    val categoryTotals by viewModel.categoryTotals.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()
    val defaultCurrency by viewModel.defaultCurrency.collectAsStateWithLifecycle()
    val smsAutoSave by viewModel.smsAutoSave.collectAsStateWithLifecycle()
    val isScanningSms by viewModel.isScanningSms.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(HomeDestination)
    var showQuickAddSheet by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.fillMaxSize(),
            entryProvider = entryProvider {
                entry<HomeDestination> {
                    HomeScreen(
                        onAddTransaction = { showQuickAddSheet = true },
                        onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                        onReviewClick = {
                            backStack.add(ActivityDestination)
                        },
                        onViewAllClick = {
                            backStack.add(ActivityDestination)
                        },
                        monthlyTotal = monthlyTotal,
                        transactionCount = transactions.size,
                        recentTransactions = recentTransactions,
                        reviewCount = reviewCount,
                        weeklySpending = weeklySpending,
                        weekDayLabels = viewModel.weekDayLabels,
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
                        categoryTotals = categoryTotals,
                        monthlyTotal = monthlyTotal,
                        currencySymbol = currencySymbol,
                        defaultCurrency = defaultCurrency
                    )
                }
                entry<SettingsDestination> {
                    SettingsScreen(
                        defaultCurrency = defaultCurrency,
                        onCurrencySelected = { viewModel.setDefaultCurrency(it) },
                        smsAutoSave = smsAutoSave,
                        onSmsAutoSaveChanged = { viewModel.setSmsAutoSave(it) },
                        isScanningSms = isScanningSms,
                        onScanSms = { onResult -> viewModel.scanSmsInbox(context, onResult) },
                        onClearAllData = { viewModel.clearAllData() },
                        onLoadSampleData = { viewModel.loadSampleData() }
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
                        onSaved = { amt, curr, merch, cat, notes ->
                            viewModel.addTransaction(amt, curr, merch, cat, notes)
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
                            viewModel.updateTransaction(updated)
                            backStack.removeLastOrNull()
                        },
                        onDelete = { id ->
                            viewModel.deleteTransaction(id)
                            backStack.removeLastOrNull()
                        },
                        onCancel = { backStack.removeLastOrNull() }
                    )
                }
            }
        )

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
                onFabClick = { showQuickAddSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }

        // Quick Add Expense Bottom Sheet Modal (matching Stitch 1:1)
        if (showQuickAddSheet) {
            QuickAddExpenseSheet(
                onDismiss = { showQuickAddSheet = false },
                onSave = { amount, currency, merchant, category, notes ->
                    viewModel.addTransaction(amount, currency, merchant, category, notes)
                    showQuickAddSheet = false
                },
                defaultCurrency = defaultCurrency
            )
        }
    }
}
