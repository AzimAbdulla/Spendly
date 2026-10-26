package com.example.spendly.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Category
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.categoryColors
import com.example.spendly.ui.theme.SpendlyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    modifier: Modifier = Modifier,
    categoryTotals: Map<Category, Double> = SampleData.categoryTotals,
    monthlyTotal: Double = SampleData.monthlyTotal
) {
    val sorted = categoryTotals.entries.sortedByDescending { it.value }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.W600) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top stat cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$${String.format("%.0f", monthlyTotal)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.W700
                            )
                            Text("spent", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Daily avg", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "$${String.format("%.0f", monthlyTotal / 30)}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.W700
                            )
                            Text("per day", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Category breakdown
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Category Breakdown",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.W600
                        )
                        Spacer(Modifier.height(16.dp))
                        if (sorted.isEmpty()) {
                            Text(
                                text = "No category data recorded this month yet.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            sorted.forEach { (category, amount) ->
                            val fraction = if (monthlyTotal > 0) (amount / monthlyTotal).toFloat() else 0f
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Surface(
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = (categoryColors[category] ?: MaterialTheme.colorScheme.primary).copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(category.emoji, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(category.displayName, style = MaterialTheme.typography.bodySmall)
                                        Text(
                                            "$${String.format("%.0f", amount)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.W600
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp),
                                        color = categoryColors[category] ?: MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }
                }
            }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun InsightsScreenPreview() {
    SpendlyTheme { InsightsScreen() }
}
