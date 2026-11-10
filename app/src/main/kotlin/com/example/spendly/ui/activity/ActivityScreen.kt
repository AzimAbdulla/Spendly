package com.example.spendly.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.format.DateTimeFormatter
import com.example.spendly.ui.components.TransactionRows
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.components.ScreenHeader
import com.example.spendly.ui.components.GlassSegmentedControl
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.theme.tint
import com.example.spendly.ui.theme.TintList

enum class ActivityFilter { ALL, REVIEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    transactions: List<Transaction> = SampleData.transactions,
    initialFilter: ActivityFilter = ActivityFilter.ALL
) {
    var filter by remember { mutableStateOf(initialFilter) }

    val filtered = when (filter) {
        ActivityFilter.ALL -> transactions
        ActivityFilter.REVIEW -> transactions.filter { it.status == TransactionStatus.NEEDS_REVIEW }
    }

    // Group by date
    val grouped = filtered.groupBy { it.date }.toSortedMap(compareByDescending { it })

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScreenHeader(
                title = "Activity",
                onBack = onBack,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Filter: the same glass toggle as Monthly / Weekly
            val reviewCount = transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
            GlassSegmentedControl(
                items = listOf(
                    ActivityFilter.ALL to "All",
                    ActivityFilter.REVIEW to if (reviewCount > 0) "Needs review · $reviewCount" else "Needs review"
                ),
                selected = filter,
                onSelect = { filter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (filter == ActivityFilter.REVIEW) "No transactions need review" else "No transactions yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (filter == ActivityFilter.REVIEW) "All transactions are confirmed" else "Tap + on Home to add your first expense",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Day groups glide to their new place when one is added or filtered out
                    grouped.forEach { (date, dayTransactions) ->
                        item(key = date.toString()) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .animateItem()
                                    .padding(vertical = 4.dp)
                            )
                        }
                        item(key = "${date}_card") {
                            TintedTile(tint = SpendlyTheme.colors.tint(TintList), modifier = Modifier.animateItem()) {
                                TransactionRows(
                                    transactions = dayTransactions,
                                    onTransactionClick = onTransactionClick,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    // Room to scroll the last day clear of the dock
                    item { Spacer(Modifier.navigationBarsPadding().height(DockClearance)) }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActivityScreenPreview() {
    SpendlyTheme { ActivityScreen(onTransactionClick = {}) }
}
