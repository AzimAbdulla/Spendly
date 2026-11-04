package com.example.spendly.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.TransactionItem
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.format.DateTimeFormatter

enum class ActivityFilter { ALL, REVIEW }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    transactions: List<Transaction> = SampleData.transactions
) {
    var filter by remember { mutableStateOf(ActivityFilter.ALL) }

    val filtered = when (filter) {
        ActivityFilter.ALL -> transactions
        ActivityFilter.REVIEW -> transactions.filter { it.status == TransactionStatus.NEEDS_REVIEW }
    }

    // Group by date
    val grouped = filtered.groupBy { it.date }.toSortedMap(compareByDescending { it })

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == ActivityFilter.ALL,
                    onClick = { filter = ActivityFilter.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filter == ActivityFilter.REVIEW,
                    onClick = { filter = ActivityFilter.REVIEW },
                    label = {
                        val reviewCount = transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
                        Text(if (reviewCount > 0) "Needs review · $reviewCount" else "Needs review")
                    }
                )
            }

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
                    grouped.forEach { (date, dayTransactions) ->
                        item(key = date.toString()) {
                            Text(
                                text = date.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        item(key = "${date}_card") {
                            SpendlyCard {
                              Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                                dayTransactions.forEachIndexed { index, transaction ->
                                    TransactionItem(
                                        transaction = transaction,
                                        onClick = { onTransactionClick(transaction.id) }
                                    )
                                    if (index < dayTransactions.lastIndex) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 56.dp)
                                                .fillMaxWidth()
                                                .height(1.dp)
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
                    }
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
