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
import com.example.spendly.ui.theme.HeroCardGradient
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
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .shadow(6.dp, CircleShape, spotColor = Color(0x667DD9A3))
                                .background(colors.secondary, CircleShape)
                        )
                        // Month indicator pill
                        Surface(
                            shape = CircleShape,
                            color = colors.container,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            modifier = Modifier.clickable {}
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "▾",
                                    fontSize = 10.sp,
                                    color = colors.textSecondary
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
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        if (reviewCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 1.dp, y = (-1).dp)
                                    .size(17.dp)
                                    .background(colors.tertiary, CircleShape)
                                    .border(2.dp, colors.background, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (reviewCount > 9) "9+" else reviewCount.toString(),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1A1100),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    lineHeight = 10.sp
                                )
                            }
                        }
                    }
                }
            }

            // Primary Monthly Spending Card (Hero with Stitch Gradient)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 16.dp,
                            shape = RoundedCornerShape(20.dp),
                            spotColor = Color(0x664F34A8)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(HeroCardGradient)
                        .border(1.dp, Color(0x66CABEFE), RoundedCornerShape(20.dp))
                        .padding(20.dp)
                ) {
                    Column {
                        // Card Header Row
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
                                    tint = Color(0xFFE6DEFF),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "SPENT THIS MONTH",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE6DEFF),
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0x26FFFFFF))
                                    .border(1.dp, Color(0x33FFFFFF), CircleShape)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = dateRangeLabel,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        // Large Outflow Metric (Space Grotesk Bold)
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", monthlyTotal)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 34.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = (-1).sp
                        )

                        Spacer(Modifier.height(6.dp))

                        // Count & Trend pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "$transactionCount recorded expenses",
                                fontSize = 12.sp,
                                color = Color(0xCCFFFFFF),
                                fontWeight = FontWeight.Normal
                            )
                            Text(text = "•", color = Color(0x80FFFFFF), fontSize = 12.sp)
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFDDB4))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF291800),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "+4.2% vs Dec",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF291800)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(18.dp))

                        // Hero Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Primary Add Expense Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp,
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
                                        tint = Color(0xFF1C0C52),
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Add Expense",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1C0C52)
                                    )
                                }
                            }

                            // Secondary Categories Button
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                shape = CircleShape,
                                color = Color(0x26FFFFFF),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x40FFFFFF)),
                                onClick = onViewAllClick
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Tag,
                                        contentDescription = null,
                                        tint = Color(0xFFE6DEFF),
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "Categories",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Spending by Category Donut Chart
            item {
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Spending Breakdown",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            ) {
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(15.dp)
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
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "This Week",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Daily cadence & velocity",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", weeklySpending.sum())}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Within target",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.secondary
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

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
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recent Expenses",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            ) {
                                Text(
                                    text = "View All",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.primary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = colors.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))

                        recentTransactions.forEachIndexed { index, tx ->
                            TransactionItem(
                                transaction = tx,
                                onClick = { onTransactionClick(tx.id) },
                                currencySymbol = currencySymbol
                            )
                            if (index < recentTransactions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 2.dp),
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
                        text = "🛡️ Offline ledger up to date",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary.copy(alpha = 0.7f),
                        letterSpacing = 0.3.sp
                    )
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.height(64.dp))
            }
        }
    }
}
