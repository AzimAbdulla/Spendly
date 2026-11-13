package com.example.spendly.ui.categories

import com.example.spendly.data.model.spend
import kotlin.math.roundToInt
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.example.spendly.ui.components.TransactionRows
import com.example.spendly.ui.components.ScreenHeader
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.theme.tint

/**
 * Categories overview: every category with spending in the chosen month,
 * ranked by amount, each opening its own list of expenses.
 */
@Composable
fun CategoriesScreen(
    transactions: List<Transaction>,
    onCategoryClick: (Category, YearMonth) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    // Shared with Home, which this opens from
    month: YearMonth = YearMonth.now(),
    onMonthChange: (YearMonth) -> Unit = {},
    currencySymbol: String = "₹"
) {
    val colors = SpendlyTheme.colors

    val monthTransactions = transactions.filter { YearMonth.from(it.date) == month }
    val monthTotal = monthTransactions.sumOf { it.spend }
    val byCategory = monthTransactions
        .groupBy { it.category }
        .map { (cat, list) -> Triple(cat, list.sumOf { it.spend }, list.size) }
        .sortedByDescending { it.second }

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
            item {
                ScreenHeader(title = "Categories", onBack = onBack)
            }

            item {
                MonthSwitcher(month = month, onMonthChange = onMonthChange)
            }

            // Month total, set like the Home total but smaller
            item {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(
                        text = "Spent in ${month.format(DateTimeFormatter.ofPattern("MMMM"))}",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatAmount(currencySymbol, monthTotal),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${byCategory.size} ${if (byCategory.size == 1) "category" else "categories"}",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }

            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    if (byCategory.isEmpty()) {
                        EmptyState(
                            title = "Nothing spent",
                            body = "No expenses in ${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
                        )
                    } else {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                            byCategory.forEachIndexed { index, (cat, amount, count) ->
                                CategoryRow(
                                    category = cat,
                                    amount = amount,
                                    count = count,
                                    share = if (monthTotal > 0) (amount / monthTotal).toFloat() else 0f,
                                    currencySymbol = currencySymbol,
                                    onClick = { onCategoryClick(cat, month) }
                                )
                                if (index < byCategory.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 56.dp),
                                        color = colors.divider
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.navigationBarsPadding().height(DockClearance))
            }
        }
    }
}

/**
 * One category's expenses for a month, newest first, with its total on top.
 */
@Composable
fun CategoryDetailScreen(
    category: Category,
    initialMonth: YearMonth,
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val colors = SpendlyTheme.colors
    var month by remember(initialMonth) { mutableStateOf(initialMonth) }

    val items = transactions
        .filter { it.category == category && YearMonth.from(it.date) == month }
        .sortedByDescending { it.date }
    val total = items.sumOf { it.spend }
    val monthAll = transactions.filter { YearMonth.from(it.date) == month }.sumOf { it.spend }
    val sharePct = if (monthAll > 0) ((total / monthAll) * 100).roundToInt() else 0

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
            item {
                ScreenHeader(title = category.displayName, onBack = onBack)
            }

            item {
                MonthSwitcher(month = month, onMonthChange = { month = it })
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(stitchCategoryColors[category] ?: colors.textMuted, CircleShape)
                        )
                        Text(
                            text = "Spent on ${category.displayName.lowercase()}",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = formatAmount(currencySymbol, total),
                        fontFamily = SpaceGroteskFontFamily,
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary,
                        style = TextStyle(fontFeatureSettings = "tnum")
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (items.isEmpty()) "No expenses this month"
                        else "${items.size} ${if (items.size == 1) "expense" else "expenses"} · $sharePct% of ${month.format(DateTimeFormatter.ofPattern("MMMM"))}",
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            }

            item {
                TintedTile(tint = colors.tint(stitchCategoryColors[category] ?: colors.textMuted), shape = RoundedCornerShape(24.dp)) {
                    if (items.isEmpty()) {
                        EmptyState(
                            title = "Nothing spent",
                            body = "No ${category.displayName.lowercase()} expenses in ${month.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}"
                        )
                    } else {
                        TransactionRows(
                            transactions = items,
                            onTransactionClick = onTransactionClick,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            currencySymbol = currencySymbol,
                            resetKey = month
                        )
                    }
                }
            }

            item {
                Spacer(Modifier.navigationBarsPadding().height(DockClearance))
            }
        }
    }
}

@Composable
private fun MonthSwitcher(month: YearMonth, onMonthChange: (YearMonth) -> Unit) {
    val colors = SpendlyTheme.colors
    val isCurrent = month >= YearMonth.now()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MonthArrow(
            icon = Icons.Default.ChevronLeft,
            description = "Previous month",
            enabled = true,
            onClick = { onMonthChange(month.minusMonths(1)) }
        )
        Text(
            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        MonthArrow(
            icon = Icons.Default.ChevronRight,
            description = "Next month",
            enabled = !isCurrent,
            onClick = { onMonthChange(month.plusMonths(1)) }
        )
    }
}

@Composable
private fun MonthArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val colors = SpendlyTheme.colors
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = colors.container,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        enabled = enabled,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = description,
                tint = if (enabled) colors.textSecondary else colors.textMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CategoryRow(
    category: Category,
    amount: Double,
    count: Int,
    share: Float,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val colors = SpendlyTheme.colors
    val catColor = stitchCategoryColors[category] ?: colors.textMuted

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.tint(catColor).track),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.getIcon(),
                contentDescription = null,
                tint = colors.tint(catColor).accent,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1
                )
                Text(
                    text = formatAmount(currencySymbol, amount),
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    style = TextStyle(fontFeatureSettings = "tnum")
                )
            }
            Text(
                text = "$count ${if (count == 1) "expense" else "expenses"} · ${(share * 100).roundToInt()}%",
                fontSize = 12.sp,
                color = colors.textSecondary
            )
            // Share of the month, in the category's own hue
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
                        .fillMaxWidth(share.coerceIn(0.02f, 1f))
                        .clip(CircleShape)
                        .background(catColor)
                )
            }
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = colors.textMuted,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun EmptyState(title: String, body: String) {
    val colors = SpendlyTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontFamily = SpaceGroteskFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = body,
            fontSize = 12.sp,
            color = colors.textSecondary
        )
    }
}

private fun formatAmount(symbol: String, amount: Double): String =
    "$symbol${String.format("%,.2f", amount)}"
