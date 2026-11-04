package com.example.spendly.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.spendly.ui.theme.ChevronRight
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.Tag
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onReviewClick: () -> Unit,
    onViewAllClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = onViewAllClick,
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
    val colors = SpendlyTheme.colors
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
            // Sub-header Row: Spendly Brand + Month Pill + Review Bell
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Spendly",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                        // Month indicator pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.container,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier.clickable {}
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 12.sp,
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
                    }

                    // Notification bell with unclipped badge
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(colors.container)
                                .border(1.dp, colors.border, CircleShape)
                                .clickable(onClick = onReviewClick),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Reviews",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (reviewCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                                    .size(18.dp)
                                    .background(colors.tertiary, CircleShape)
                                    .border(2.dp, colors.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (reviewCount > 9) "9+" else reviewCount.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1A1100),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // Monthly total: the one bright moment on Home, set large on calm space
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 4.dp, top = 12.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = "Spent in ${now.format(DateTimeFormatter.ofPattern("MMMM"))}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$currencySymbol${String.format("%,.2f", monthlyTotal)}",
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 44.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary,
                        letterSpacing = (-0.9).sp,
                        style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$transactionCount ${if (transactionCount == 1) "expense" else "expenses"} · $dateRangeLabel",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Primary: off-white fill from the ramp, dark text
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.textPrimary,
                            onClick = onAddTransaction
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = colors.background,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Add expense",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.background
                                )
                            }
                        }

                        // Secondary: raised surface with a hairline
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            color = colors.containerHigh,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            onClick = onCategoriesClick
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Tag,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Categories",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Spending by Category Donut Chart
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "By category",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onCategoriesClick)
                            ) {
                                Text(
                                    text = "View all",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.textSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "This week",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Last 7 days",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format("%,.2f", weeklySpending.sum())}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
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
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent expenses",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            ) {
                                Text(
                                    text = "View all",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = colors.textSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        recentTransactions.forEachIndexed { index, tx ->
                            TransactionItem(
                                transaction = tx,
                                onClick = { onTransactionClick(tx.id) },
                                currencySymbol = currencySymbol
                            )
                            if (index < recentTransactions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 56.dp),
                                    color = colors.divider
                                )
                            }
                        }
                    }
                }
            }

            // End-of-feed calm marker
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stored on this phone only",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        letterSpacing = 0.sp
                    )
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.navigationBarsPadding().height(96.dp))
            }
        }
    }
}
