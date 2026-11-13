package com.example.spendly.ui.activity

import com.example.spendly.data.model.spend
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.components.ScreenHeader
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.components.TransactionRows
import com.example.spendly.ui.components.rememberSeenIds
import com.example.spendly.ui.theme.GeistFontFamily
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.TintList
import com.example.spendly.ui.theme.tint
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class ActivityFilter { ALL, REVIEW }

/**
 * Every expense, newest first, a tile per day with the day's total. Search looks at the item,
 * payee, category and amount; the pills narrow it to what needs review or to one category.
 */
@Composable
fun ActivityScreen(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    onConfirm: (Long) -> Unit,
    onConfirmAll: () -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "\u20B9",
    initialFilter: ActivityFilter = ActivityFilter.ALL
) {
    val colors = SpendlyTheme.colors
    var reviewOnly by rememberSaveable { mutableStateOf(initialFilter == ActivityFilter.REVIEW) }
    var category by rememberSaveable { mutableStateOf<Category?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var paidBackOnly by rememberSaveable { mutableStateOf(false) }
    val anyPaidBack = transactions.any { it.paidBack }

    val reviewCount = transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
    val usedCategories = remember(transactions) {
        transactions.groupingBy { it.category }.eachCount().entries.sortedByDescending { it.value }.map { it.key }
    }
    val words = query.trim().lowercase().split(" ").filter { it.isNotEmpty() }
    // The list arrives newest first, so filtering and grouping keep that order
    val filtered = transactions.filter { tx ->
        (!reviewOnly || tx.status == TransactionStatus.NEEDS_REVIEW) &&
            (category == null || tx.category == category) &&
            (!paidBackOnly || tx.paidBack) &&
            words.all { word ->
                tx.item.lowercase().contains(word) || tx.merchant.lowercase().contains(word) ||
                    tx.category.displayName.lowercase().contains(word) || String.format("%.2f", tx.amount).contains(word)
            }
    }
    val days = filtered.groupBy { it.date }.entries.toList()
    val today = LocalDate.now()
    // Tracked across every day's tile, so an expense that starts a new day still arrives in green
    val seen = rememberSeenIds(transactions.map { it.id })

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp)
    ) { _ ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            ScreenHeader(title = "Activity", modifier = Modifier.padding(horizontal = 16.dp))

            // Search
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(CircleShape)
                    .background(colors.container)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    textStyle = TextStyle(fontFamily = GeistFontFamily, fontSize = 14.sp, color = colors.textPrimary),
                    cursorBrush = SolidColor(colors.primary),
                    modifier = Modifier.weight(1f),
                    decorationBox = { field ->
                        if (query.isEmpty()) {
                            Text("Search expenses", fontSize = 14.sp, color = colors.textSecondary)
                        }
                        field()
                    }
                )
                if (query.isNotEmpty()) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = colors.textSecondary,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .clickable { query = "" }
                            .padding(5.dp)
                    )
                }
            }

            // Filters: all, what needs a check, then each category in use
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterPill("All", selected = !reviewOnly && !paidBackOnly && category == null) {
                        reviewOnly = false
                        paidBackOnly = false
                        category = null
                    }
                }
                if (reviewCount > 0 || reviewOnly) {
                    item {
                        FilterPill("Needs review \u00B7 $reviewCount", selected = reviewOnly) { reviewOnly = !reviewOnly }
                    }
                }
                if (anyPaidBack || paidBackOnly) {
                    item {
                        FilterPill("Paid back", selected = paidBackOnly) { paidBackOnly = !paidBackOnly }
                    }
                }
                items(usedCategories) { cat ->
                    FilterPill(cat.displayName, selected = category == cat) { category = if (category == cat) null else cat }
                }
            }

            if (filtered.isEmpty()) {
                val searching = words.isNotEmpty() || category != null || paidBackOnly
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    Text(
                        text = when {
                            searching -> "No matching expenses"
                            reviewOnly -> "Nothing to review"
                            else -> "No expenses yet"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = when {
                            searching -> "Try another word, or clear the filters"
                            reviewOnly -> "Every SMS expense has been checked"
                            else -> "Tap + to add your first expense"
                        },
                        fontSize = 14.sp,
                        color = colors.textSecondary
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (reviewOnly && reviewCount > 1) {
                        item(key = "confirmAll") {
                            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Text(
                                    text = "Confirm all $reviewCount",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable(onClick = onConfirmAll)
                                        .background(colors.container)
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                    days.forEach { (date, dayTransactions) ->
                        item(key = date.toString()) {
                            Row(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .padding(start = 4.dp, end = 4.dp, top = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = when (date) {
                                        today -> "Today"
                                        today.minusDays(1) -> "Yesterday"
                                        else -> date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "EEEE, d MMM" else "d MMM yyyy"))
                                    },
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "$currencySymbol${String.format("%,.0f", dayTransactions.sumOf { it.spend })}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textSecondary
                                )
                            }
                        }
                        item(key = "${date}_card") {
                            TintedTile(tint = colors.tint(TintList), modifier = Modifier.animateItem()) {
                                TransactionRows(
                                    transactions = dayTransactions,
                                    onTransactionClick = onTransactionClick,
                                    currencySymbol = currencySymbol,
                                    onConfirm = onConfirm,
                                    flashBleed = 4.dp,
                                    screenSeen = seen,
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

@Composable
private fun FilterPill(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = SpendlyTheme.colors
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = if (selected) colors.onPrimary else colors.textSecondary,
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .background(if (selected) colors.primary else colors.container)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    )
}
