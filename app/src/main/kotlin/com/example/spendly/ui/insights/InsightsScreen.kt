package com.example.spendly.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CreditCardOff
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
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
        "Last 7 days"
    }

    val periodTotal = periodTransactions.sumOf { it.amount }
    val elapsedUnits = if (isMonthlySelected) today.dayOfMonth.coerceAtLeast(1) else 7
    val dailyAverage = if (periodTotal > 0) periodTotal / elapsedUnits else 0.0

    val categorySums = remember(periodTransactions) {
        periodTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    }
    val activeCategories = categorySums.filter { it.value > 0 }.entries.sortedByDescending { it.value }
    val topCategoryEntry = activeCategories.firstOrNull()
    val topCategory = topCategoryEntry?.key ?: Category.FOOD
    val topCategoryAmount = topCategoryEntry?.value ?: 0.0
    val topCategoryPct = if (periodTotal > 0) ((topCategoryAmount / periodTotal) * 100).toInt() else 0

    // 6-Month dynamic historical data from transactions
    val monthlyHistoricalData = remember(transactions) {
        val past6 = (5 downTo 0).map { YearMonth.now().minusMonths(it.toLong()) }
        past6.map { ym ->
            val sum = transactions.filter { YearMonth.from(it.date) == ym }.sumOf { it.amount }
            val label = ym.format(DateTimeFormatter.ofPattern("MMM"))
            label to sum
        }
    }
    val maxHistoricalVal = (monthlyHistoricalData.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(1.0)

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Period & Cadence Selector Control
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Month Picker Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable {}
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = periodLabel,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = colors.textSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Monthly vs Weekly Segmented Toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            // Monthly
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isMonthlySelected = true }
                                    .then(
                                        if (isMonthlySelected) Modifier.background(colors.containerHigh)
                                        else Modifier
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Monthly",
                                    fontSize = 12.sp,
                                    fontWeight = if (isMonthlySelected) FontWeight.Medium else FontWeight.Medium,
                                    color = if (isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                            // Weekly
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { isMonthlySelected = false }
                                    .then(
                                        if (!isMonthlySelected) Modifier.background(colors.containerHigh)
                                        else Modifier
                                    )
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Weekly",
                                    fontSize = 12.sp,
                                    fontWeight = if (!isMonthlySelected) FontWeight.Medium else FontWeight.Medium,
                                    color = if (!isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Summary figures: label, number, what it compares to
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SpendlyCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Spent",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", periodTotal)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                maxLines = 1,
                                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", dailyAverage)} a day on average",
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    SpendlyCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Top category",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = colors.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = topCategoryEntry?.key?.displayName ?: "None yet",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 20.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (topCategoryEntry != null) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(stitchCategoryColors[topCategory] ?: colors.textMuted, CircleShape)
                                    )
                                }
                                Text(
                                    text = if (topCategoryEntry != null) "$topCategoryPct% of spending" else "No spending yet",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Category Breakdown
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
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
                                Text(
                                    text = "By category",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "${activeCategories.size} ${if (activeCategories.size == 1) "category" else "categories"}",
                                fontSize = 12.sp,
                                color = colors.textSecondary
                            )
                        }

                        // Ranked Category Items
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            activeCategories.forEach { (cat, amt) ->
                                val pct = if (periodTotal > 0) (amt / periodTotal).toFloat() else 0f
                                val catColor = stitchCategoryColors[cat] ?: colors.primary

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(colors.containerHigh),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = cat.getIcon(),
                                                    contentDescription = null,
                                                    tint = colors.textSecondary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Text(
                                                text = cat.displayName,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$currencySymbol${String.format("%,.2f", amt)}",
                                                fontFamily = SpaceGroteskFontFamily,
                                                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum"),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${(pct * 100).toInt()}%",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Normal,
                                                color = colors.textMuted
                                            )
                                        }
                                    }

                                    // Progress track
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape)
                                            .background(colors.containerHigh)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .fillMaxWidth(pct.coerceIn(0.02f, 1f))
                                                .clip(CircleShape)
                                                .background(catColor)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Monthly spending: the main chart on Insights
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Monthly spending",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Last 6 months",
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyHistoricalData.forEachIndexed { index, (label, amt) ->
                                val isCurrent = index == monthlyHistoricalData.lastIndex
                                val fraction = (amt / maxHistoricalVal).toFloat().coerceIn(0f, 1f)
                                val barShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Figure above the current month, as quiet text
                                    Box(modifier = Modifier.height(20.dp), contentAlignment = Alignment.BottomCenter) {
                                        if (isCurrent && amt > 0) {
                                            val compact = if (amt >= 1000) "$currencySymbol${String.format("%,.1f", amt / 1000)}k"
                                            else "$currencySymbol${String.format("%,.0f", amt)}"
                                            Text(
                                                text = compact,
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textPrimary,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .width(24.dp)
                                            .height(100.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(2.dp)
                                                .background(colors.textPrimary.copy(alpha = 0.08f))
                                        )
                                        if (amt > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(maxOf((100 * fraction).dp, 4.dp))
                                                    .clip(barShape)
                                                    .background(if (isCurrent) colors.primary else colors.textMuted.copy(alpha = 0.45f))
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isCurrent) colors.textPrimary else colors.textMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Editorial Disclaimer / Currency Safety Note
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Totals are in $defaultCurrency. Expenses in other currencies keep their original amount and are not converted.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.navigationBarsPadding().height(96.dp))
            }
        }
    }
}
