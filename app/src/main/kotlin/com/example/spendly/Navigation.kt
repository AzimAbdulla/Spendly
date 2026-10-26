package com.example.spendly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import com.example.spendly.ui.theme.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.spendly.ui.SpendlyViewModel
import com.example.spendly.ui.activity.ActivityScreen
import com.example.spendly.ui.home.HomeScreen
import com.example.spendly.ui.insights.InsightsScreen
import com.example.spendly.ui.settings.SettingsScreen
import com.example.spendly.ui.transaction.AddTransactionScreen
import com.example.spendly.ui.transaction.EditTransactionScreen

private data class NavItem(
    val label: String,
    val icon: ImageVector,
    val destination: Any
)

private val navItems = listOf(
    NavItem("Home", Icons.Default.Home, HomeDestination),
    NavItem("Activity", Icons.AutoMirrored.Filled.List, ActivityDestination),
    NavItem("Insights", Icons.Default.PieChart, InsightsDestination),
    NavItem("Settings", Icons.Default.Settings, SettingsDestination),
)

@Composable
fun MainNavigation() {
    val context = LocalContext.current.applicationContext as SpendlyApplication
    val viewModel: SpendlyViewModel = viewModel(
        factory = SpendlyViewModel.provideFactory(context.repository)
    )

    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val recentTransactions by viewModel.recentTransactions.collectAsStateWithLifecycle()
    val monthlyTotal by viewModel.monthlyTotal.collectAsStateWithLifecycle()
    val reviewCount by viewModel.reviewCount.collectAsStateWithLifecycle()
    val categoryTotals by viewModel.categoryTotals.collectAsStateWithLifecycle()
    val weeklySpending by viewModel.weeklySpending.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack(HomeDestination)
    var currentTab by remember { mutableStateOf<Any>(HomeDestination) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                navItems.forEach { item ->
                    val isSelected = currentTab == item.destination
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = isSelected,
                        onClick = {
                            currentTab = item.destination
                            // Clear back stack to tab root
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            if (backStack.lastOrNull() != item.destination) {
                                backStack.removeLastOrNull()
                                backStack.add(item.destination as NavKey)
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            entryProvider = entryProvider {
                entry<HomeDestination> {
                    HomeScreen(
                        onAddTransaction = { backStack.add(AddTransactionDestination()) },
                        onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) },
                        onReviewClick = {
                            currentTab = ActivityDestination
                            while (backStack.size > 1) backStack.removeLastOrNull()
                            backStack.removeLastOrNull()
                            backStack.add(ActivityDestination)
                        },
                        monthlyTotal = monthlyTotal,
                        transactionCount = transactions.size,
                        recentTransactions = recentTransactions,
                        reviewCount = reviewCount,
                        weeklySpending = weeklySpending,
                        weekDayLabels = viewModel.weekDayLabels,
                        categoryTotals = categoryTotals
                    )
                }
                entry<ActivityDestination> {
                    ActivityScreen(
                        transactions = transactions,
                        onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) }
                    )
                }
                entry<InsightsDestination> {
                    InsightsScreen(
                        categoryTotals = categoryTotals,
                        monthlyTotal = monthlyTotal
                    )
                }
                entry<SettingsDestination> {
                    SettingsScreen(
                        onClearAllData = { viewModel.clearAllData() },
                        onLoadSampleData = { viewModel.loadSampleData() }
                    )
                }
                entry<AddTransactionDestination> { key ->
                    AddTransactionScreen(
                        prefillAmount = key.prefillAmount,
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
    }
}
