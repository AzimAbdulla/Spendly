package com.example.spendly.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.DonutChart
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.TransactionItem
import com.example.spendly.ui.components.WeeklyBarChart
import com.example.spendly.ui.theme.SpendlyAmber
import com.example.spendly.ui.theme.SpendlyAmberContainer
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onReviewClick: () -> Unit,
    modifier: Modifier = Modifier,
    // In a real app these would come from a ViewModel
    monthlyTotal: Double = SampleData.monthlyTotal,
    transactionCount: Int = SampleData.transactions.size,
    recentTransactions: List<Transaction> = SampleData.transactions.take(5),
    reviewCount: Int = SampleData.reviewCount,
    weeklySpending: List<Double> = SampleData.weeklySpending,
    weekDayLabels: List<String> = SampleData.weekDayLabels,
    categoryTotals: Map<Category, Double> = SampleData.categoryTotals,
    defaultCurrency: String = "USD",
) {
    val monthLabel = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val currencySymbol = when (defaultCurrency) {
        "USD", "CAD", "AUD", "SGD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        "JPY" -> "¥"
        "AED" -> "AED "
        "CHF" -> "CHF "
        else -> "$defaultCurrency "
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Spendly",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.W600
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    if (reviewCount > 0) {
                        IconButton(onClick = onReviewClick) {
                            BadgedBox(badge = {
                                Badge { Text(reviewCount.toString()) }
                            }) {
                                Icon(Icons.Default.Notifications, contentDescription = "Review needed")
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add expense", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly total hero card
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Spent in $monthLabel",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${String.format("%.2f", monthlyTotal)}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.W700,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$transactionCount transactions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Review banner (if any)
            if (reviewCount > 0) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        color = SpendlyAmberContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$reviewCount transactions need review",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.W500,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tap to confirm or edit them",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = onReviewClick) {
                                Text("Review", color = SpendlyAmber, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                }
            }

            // Category donut chart
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Spending by category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        DonutChart(categoryTotals = categoryTotals)
                    }
                }
            }

            // Weekly bar chart
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "This week",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.W600,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(16.dp))
                        WeeklyBarChart(
                            values = weeklySpending,
                            labels = weekDayLabels
                        )
                    }
                }
            }

            // Recent transactions header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.W600
                    )
                    TextButton(onClick = { /* navigate to activity */ }) {
                        Text("See all", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                SpendlyCard {
                    if (recentTransactions.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No recent transactions",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.W500,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Tap the + button to record an expense",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        recentTransactions.forEachIndexed { index, transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick = { onTransactionClick(transaction.id) }
                            )
                            if (index < recentTransactions.lastIndex) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxSize(),
                                        color = MaterialTheme.colorScheme.outline
                                    ) {}
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    SpendlyTheme { HomeScreen(onAddTransaction = {}, onTransactionClick = {}, onReviewClick = {}) }
}
