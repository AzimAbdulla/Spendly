package com.example.spendly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
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
    val backStack = rememberNavBackStack(HomeDestination)
    var currentTab by rememberSaveable { mutableStateOf<Any>(HomeDestination) }

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
                        }
                    )
                }
                entry<ActivityDestination> {
                    ActivityScreen(
                        onTransactionClick = { id -> backStack.add(EditTransactionDestination(id)) }
                    )
                }
                entry<InsightsDestination> {
                    InsightsScreen()
                }
                entry<SettingsDestination> {
                    SettingsScreen()
                }
                entry<AddTransactionDestination> { key ->
                    AddTransactionScreen(
                        prefillAmount = key.prefillAmount,
                        onSaved = { backStack.removeLastOrNull() },
                        onCancel = { backStack.removeLastOrNull() }
                    )
                }
                entry<EditTransactionDestination> { key ->
                    EditTransactionScreen(
                        transactionId = key.transactionId,
                        onSaved = { backStack.removeLastOrNull() },
                        onCancel = { backStack.removeLastOrNull() }
                    )
                }
            }
        )
    }
}
