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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.spendly.ui.theme.ActiveBarGradient
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
        "Last 7 Days"
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
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                        shape = CircleShape,
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable {}
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = periodLabel,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Text(text = "▾", fontSize = 10.sp, color = colors.textSecondary)
                        }
                    }

                    // Monthly vs Weekly Segmented Toggle
                    Surface(
                        shape = CircleShape,
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
                    ) {
                        Row(modifier = Modifier.padding(3.dp)) {
                            // Monthly
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
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
                                    fontWeight = if (isMonthlySelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                            // Weekly
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
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
                                    fontWeight = if (!isMonthlySelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (!isMonthlySelected) colors.textPrimary else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Summary Metrics Row (Strictly Outflow Only)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Total Spent
                    SpendlyCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCardOff,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "TOTAL SPENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            val parts = String.format("%,.2f", periodTotal).split(".")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$currencySymbol${parts[0]}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    letterSpacing = (-0.5).sp
                                )
                                if (parts.size > 1) {
                                    Text(
                                        text = ".${parts[1]}",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Avg daily:",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", dailyAverage)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary
                                )
                            }
                        }
                    }

                    // Card 2: Top Category
                    SpendlyCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = colors.tertiary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "TOP CATEGORY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.6.sp
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = topCategory.displayName,
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                maxLines = 1
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .shadow(4.dp, CircleShape, spotColor = Color(0xB39E8CFC))
                                        .background(colors.primary, CircleShape)
                                )
                                Text(
                                    text = "$topCategoryPct% of total",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.primary
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Category Breakdown
            item {
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(14.dp)
                                        .background(colors.primary, CircleShape)
                                )
                                Text(
                                    text = "Category Breakdown",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "${activeCategories.size} active categories",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }

                        // Ranked Category Items
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            activeCategories.forEach { (cat, amt) ->
                                val pct = if (periodTotal > 0) (amt / periodTotal).toFloat() else 0f
                                val catColor = stitchCategoryColors[cat] ?: colors.primary

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                                    .background(catColor.copy(alpha = 0.15f))
                                                    .border(1.dp, catColor.copy(alpha = 0.3f), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = cat.getIcon(),
                                                    contentDescription = null,
                                                    tint = catColor,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                            Text(
                                                text = cat.displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "$currencySymbol${String.format("%,.0f", amt)}",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "${(pct * 100).toInt()}%",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = colors.textSecondary
                                            )
                                        }
                                    }

                                    // Progress track
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(CircleShape)
                                            .background(if (colors.isDark) Color(0xFF1D1D28) else colors.containerHigh)
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

            // Historical Outflow Bar Chart Card
            item {
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Historical Outflow",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "6-month spending velocity",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(colors.secondary.copy(alpha = 0.15f))
                                    .border(1.dp, colors.secondary.copy(alpha = 0.3f), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "MoM: -8.4%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.secondary
                                )
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // 6-Month Bar Graph
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            monthlyHistoricalData.forEachIndexed { index, (label, amt) ->
                                val isCurrent = index == monthlyHistoricalData.lastIndex
                                val fraction = (amt / maxHistoricalVal).toFloat().coerceIn(0.08f, 1f)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (isCurrent && amt > 0) {
                                        Box(
                                            modifier = Modifier
                                                .shadow(6.dp, CircleShape, spotColor = Color(0xB39E8CFC))
                                                .clip(CircleShape)
                                                .background(colors.primary)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$currencySymbol${String.format("%,.0f", amt / 1000)}k",
                                                fontFamily = SpaceGroteskFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.onPrimary
                                            )
                                        }
                                        Spacer(Modifier.height(4.dp))
                                    } else {
                                        Spacer(Modifier.height(18.dp))
                                    }

                                    // Bar container
                                    Box(
                                        modifier = Modifier
                                            .width(22.dp)
                                            .height((70 * fraction).dp)
                                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                            .then(
                                                if (isCurrent) {
                                                    Modifier
                                                        .background(ActiveBarGradient)
                                                        .shadow(10.dp, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp), spotColor = Color(0x999E8CFC))
                                                        .border(1.dp, Color(0x66CABEFE), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                } else {
                                                    Modifier
                                                        .background(if (colors.isDark) Color(0x22FFFFFF) else colors.containerHigh)
                                                        .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                                }
                                            )
                                    )

                                    Spacer(Modifier.height(6.dp))

                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) colors.primary else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Editorial Disclaimer / Currency Safety Note
            item {
                SpendlyCard(shape = RoundedCornerShape(14.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Amounts calculated strictly in $defaultCurrency. Offline ledger privacy maintained. Foreign card logs retain original billing receipts without speculative recalculation.",
                            fontSize = 11.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}
