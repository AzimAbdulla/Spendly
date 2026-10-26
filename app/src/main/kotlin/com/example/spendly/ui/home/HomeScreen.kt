package com.example.spendly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import com.example.spendly.ui.theme.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.SampleData
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.DonutChart
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.TransactionItem
import com.example.spendly.ui.components.WeeklyBarChart
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.StitchBackground
import com.example.spendly.ui.theme.StitchCatEntertainment
import com.example.spendly.ui.theme.StitchCatFood
import com.example.spendly.ui.theme.StitchContainer
import com.example.spendly.ui.theme.StitchContainerLow
import com.example.spendly.ui.theme.StitchOnPrimary
import com.example.spendly.ui.theme.StitchOnPrimaryFixed
import com.example.spendly.ui.theme.StitchOnSurface
import com.example.spendly.ui.theme.StitchOnSurfaceVariant
import com.example.spendly.ui.theme.StitchOnTertiary
import com.example.spendly.ui.theme.StitchPrimary
import com.example.spendly.ui.theme.StitchPrimaryFixed
import com.example.spendly.ui.theme.StitchSecondary
import com.example.spendly.ui.theme.StitchTertiary
import com.example.spendly.ui.theme.StitchTertiaryContainer
import com.example.spendly.ui.theme.StitchTertiaryFixed
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onReviewClick: () -> Unit,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    monthlyTotal: Double = SampleData.monthlyTotal,
    transactionCount: Int = SampleData.transactions.size,
    recentTransactions: List<Transaction> = SampleData.transactions.take(5),
    reviewCount: Int = SampleData.reviewCount,
    weeklySpending: List<Double> = SampleData.weeklySpending,
    weekDayLabels: List<String> = SampleData.weekDayLabels,
    categoryTotals: Map<Category, Double> = SampleData.categoryTotals,
    defaultCurrency: String = "INR",
) {
    val now = LocalDate.now()
    val monthLabel = now.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val dateRangeLabel = "${now.format(DateTimeFormatter.ofPattern("MMM"))} 1 – ${now.lengthOfMonth()}"
    val currencySymbol = when (defaultCurrency) {
        "INR" -> "₹"
        "EUR" -> "€"
        "GBP" -> "£"
        else -> "$"
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(StitchPrimary, CircleShape)
                        )
                        Text(
                            text = "Overview",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = StitchOnSurfaceVariant
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .background(StitchPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = StitchOnPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchBackground)
            )
        },
        containerColor = StitchBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sub-header Row: Spendly Brand + Month Pill + Review Bell
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Spendly",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StitchSecondary, CircleShape)
                        )
                        // Month switcher pill
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StitchContainer,
                            modifier = Modifier.clickable {}
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W600,
                                    color = StitchOnSurfaceVariant
                                )
                                Text("▼", fontSize = 9.sp, color = StitchOnSurfaceVariant)
                            }
                        }
                    }

                    // Notification bell with badge
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(StitchContainer)
                            .clickable(onClick = onReviewClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Reviews",
                            tint = StitchOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        if (reviewCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .size(16.dp)
                                    .background(StitchTertiaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = reviewCount.toString(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnTertiary
                                )
                            }
                        }
                    }
                }
            }

            // Primary Monthly Spending Card (Hero)
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarToday,
                                    contentDescription = null,
                                    tint = StitchPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "SPENT THIS MONTH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurfaceVariant,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StitchContainer)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = dateRangeLabel,
                                    fontSize = 11.sp,
                                    color = StitchOnSurfaceVariant,
                                    fontWeight = FontWeight.W500
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Large Outflow Metric
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", monthlyTotal)}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface,
                            letterSpacing = (-1).sp
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = "$transactionCount recorded expenses  •  +4.2% vs last month",
                            fontSize = 12.sp,
                            color = StitchOnSurfaceVariant,
                            fontWeight = FontWeight.W500
                        )

                        Spacer(Modifier.height(16.dp))

                        // Tactile Action Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable(onClick = onAddTransaction),
                                shape = RoundedCornerShape(22.dp),
                                color = StitchPrimaryFixed
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = StitchOnPrimaryFixed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Add Expense",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W700,
                                        color = StitchOnPrimaryFixed
                                    )
                                }
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clickable(onClick = onViewAllClick),
                                shape = RoundedCornerShape(22.dp),
                                color = StitchContainer
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏷",
                                        fontSize = 14.sp
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Categories",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W600,
                                        color = StitchOnSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Review Pending Alert Banner
            if (reviewCount > 0) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onReviewClick),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFDF1DF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StitchTertiaryFixed)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .background(StitchTertiaryFixed, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📝", fontSize = 16.sp)
                                }
                                Column {
                                    Text(
                                        text = "$reviewCount uncertain expenses",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.W700,
                                        color = StitchOnSurface
                                    )
                                    Text(
                                        text = "Auto-parsed via SMS alerts",
                                        fontSize = 11.sp,
                                        color = StitchOnSurfaceVariant
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(StitchTertiaryContainer)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Review",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.W700,
                                        color = StitchOnTertiary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = StitchOnTertiary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spending Breakdown Donut Chart
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spending Breakdown",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "View All ›",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W600,
                                color = StitchTertiary,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        DonutChart(
                            categoryTotals = categoryTotals,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Weekly Spending Cadence (Bar visualization)
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "This Week",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurface
                                )
                                Text(
                                    text = "Daily cadence & velocity",
                                    fontSize = 12.sp,
                                    color = StitchOnSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", weeklySpending.sum())}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = StitchOnSurface
                                )
                                Text(
                                    text = "Within target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.W600,
                                    color = StitchSecondary
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        WeeklyBarChart(
                            values = weeklySpending,
                            labels = weekDayLabels,
                            currencySymbol = currencySymbol
                        )
                    }
                }
            }

            // Recent Expenses List
            item {
                SpendlyCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Expenses",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurface
                            )
                            Text(
                                text = "View All ›",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.W600,
                                color = StitchTertiary,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        if (recentTransactions.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent transactions logged",
                                    fontSize = 13.sp,
                                    color = StitchOnSurfaceVariant
                                )
                            }
                        } else {
                            recentTransactions.forEachIndexed { index, tx ->
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { onTransactionClick(tx.id) },
                                    currencySymbol = currencySymbol
                                )
                                if (index < recentTransactions.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(StitchContainer)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Calm Footer Marker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", fontSize = 14.sp, color = StitchOnSurfaceVariant)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "Offline ledger up to date",
                        fontSize = 12.sp,
                        color = StitchOnSurfaceVariant,
                        fontWeight = FontWeight.W500
                    )
                }
            }

            // Spacer for floating dock
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SpendlyTheme {
        HomeScreen(
            onAddTransaction = {},
            onTransactionClick = {},
            onReviewClick = {}
        )
    }
}
