package com.example.spendly.ui.insights

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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.PieChart
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.TrendingUp
import com.example.spendly.ui.theme.getColor
import com.example.spendly.ui.theme.getIcon
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    defaultCurrency: String = "INR"
) {
    val colors = SpendlyTheme.colors
    var isMonthlySelected by remember { mutableStateOf(true) }

    val today = LocalDate.now()
    val currentMonth = YearMonth.now()

    // Dynamically filter transactions based on selected timeframe
    val periodTransactions = remember(transactions, isMonthlySelected) {
        if (isMonthlySelected) {
            transactions.filter { YearMonth.from(it.date) == currentMonth }
        } else {
            val startOfWeek = today.minusDays(6)
            transactions.filter { !it.date.isBefore(startOfWeek) && !it.date.isAfter(today) }
        }
    }

    val periodLabel = if (isMonthlySelected) {
        currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } else {
        "Last 7 Days"
    }

    val periodTotal = periodTransactions.sumOf { it.amount }
    val elapsedUnits = if (isMonthlySelected) today.dayOfMonth.coerceAtLeast(1) else 7
    val dailyAverage = if (periodTotal > 0) periodTotal / elapsedUnits else 0.0

    val categorySums = remember(periodTransactions) {
        periodTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    }
    val activeCategories = categorySums.filter { it.value > 0 }
    val topCategoryEntry = activeCategories.maxByOrNull { it.value }
    val topCategory = topCategoryEntry?.key ?: Category.FOOD
    val topCategoryAmount = topCategoryEntry?.value ?: 0.0
    val topCategoryPct = if (periodTotal > 0) ((topCategoryAmount / periodTotal) * 100).toInt() else 0

    // 6-Month dynamic historical data from transactions
    val monthlyHistoricalData = remember(transactions) {
        val past6 = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        past6.map { ym ->
            val sum = transactions.filter { YearMonth.from(it.date) == ym }.sumOf { it.amount }
            val label = ym.format(DateTimeFormatter.ofPattern("MMM"))
            Triple(label, sum, ym == currentMonth)
        }
    }
    val maxHistoricalSum = monthlyHistoricalData.maxOfOrNull { it.second }?.takeIf { it > 0.0 } ?: 1.0

    Scaffold(
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
                                .background(colors.primary, CircleShape)
                        )
                        Text(
                            text = "Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.W700,
                            color = colors.textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        containerColor = colors.background
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period Picker & Segmented Pill Switcher
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Period indicator pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.container
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = periodLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Monthly | Weekly segmented toggle
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isMonthlySelected) colors.cardBackground else Color.Transparent)
                                    .clickable { isMonthlySelected = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Monthly",
                                    fontSize = 11.sp,
                                    fontWeight = if (isMonthlySelected) FontWeight.W700 else FontWeight.W500,
                                    color = if (isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isMonthlySelected) colors.cardBackground else Color.Transparent)
                                    .clickable { isMonthlySelected = false }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Weekly",
                                    fontSize = 11.sp,
                                    fontWeight = if (!isMonthlySelected) FontWeight.W700 else FontWeight.W500,
                                    color = if (!isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Two Metric Cards Side-by-Side
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: TOTAL SPENT
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "TOTAL SPENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.W700,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", periodTotal)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W700,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Avg daily: $currencySymbol${String.format("%,.0f", dailyAverage)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W500,
                                color = colors.primary
                            )
                        }
                    }

                    // Card 2: TOP CATEGORY
                    SpendlyCard(modifier = Modifier.weight(1f)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PieChart,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "TOP CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.W700,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Text(
                                text = if (periodTotal > 0) topCategory.displayName else "None",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.W700,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(6.dp).background(colors.primary, CircleShape))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = if (periodTotal > 0) "$topCategoryPct% of total" else "No spend",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.W500,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Category Breakdown Card with Horizontal Progress Bars
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors.primary)
                                )
                                Text(
                                    text = "Category Breakdown",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "${activeCategories.size} active categories",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }

                        if (activeCategories.isEmpty()) {
                            Text(
                                text = "No category data recorded for this period",
                                fontSize = 13.sp,
                                color = colors.textSecondary,
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            activeCategories.entries
                                .sortedByDescending { it.value }
                                .forEach { (cat, amt) ->
                                    val pct = if (periodTotal > 0) ((amt / periodTotal) * 100).toInt() else 0

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(colors.container, CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = cat.getIcon(),
                                                        contentDescription = cat.displayName,
                                                        tint = colors.textPrimary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                                Column {
                                                    Text(
                                                        text = cat.displayName,
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.W600,
                                                        color = colors.textPrimary
                                                    )
                                                    Text(
                                                        text = "Active spending",
                                                        fontSize = 11.sp,
                                                        color = colors.textSecondary
                                                    )
                                                }
                                            }

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    text = "$currencySymbol${String.format("%,.0f", amt)}",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.W700,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = "$pct%",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.W600,
                                                    color = colors.primary
                                                )
                                            }
                                        }

                                        // Horizontal Progress bar
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(CircleShape)
                                                .background(colors.container)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(fraction = (pct / 100f).coerceIn(0.02f, 1f))
                                                    .height(6.dp)
                                                    .clip(CircleShape)
                                                    .background(cat.getColor())
                                            )
                                        }
                                    }
                                }
                        }
                    }
                }
            }

            // Historical Outflow 6-Month Trend Card (Real DB Calculations)
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .height(18.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(colors.primary)
                                )
                                Text(
                                    text = "Historical Outflow",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W700,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "6-Month Trend",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }

                        // 6-Month Dynamic Bar Chart
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyHistoricalData.forEach { (m, sum, isCurrent) ->
                                val frac = (sum / maxHistoricalSum).toFloat().coerceIn(0.08f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (sum > 0) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (isCurrent) colors.primary else colors.container)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            val displayK = if (sum >= 1000) {
                                                "$currencySymbol${String.format("%,.1fk", sum / 1000)}"
                                            } else {
                                                "$currencySymbol${sum.toInt()}"
                                            }
                                            Text(
                                                text = displayK,
                                                color = if (isCurrent) colors.onPrimary else colors.textPrimary,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.W700
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(26.dp)
                                            .height((75 * frac).dp)
                                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                            .background(if (isCurrent) colors.primary else colors.container)
                                    )

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = m,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.W700 else FontWeight.W500,
                                        color = if (isCurrent) colors.primary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom clearance spacer
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
