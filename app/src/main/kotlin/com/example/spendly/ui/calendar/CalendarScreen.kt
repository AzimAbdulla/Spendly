package com.example.spendly.ui.calendar

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
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.TransactionItem
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹"
) {
    val colors = SpendlyTheme.colors
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }

    // Map of dates with transactions in this month
    val txByDate = remember(transactions) {
        transactions.groupBy { it.date }
    }

    val daysWithTx = txByDate.keys.count { it.year == selectedMonth.year && it.month == selectedMonth.month }
    val dayTransactions = txByDate[selectedDate] ?: emptyList()
    val dayTotal = dayTransactions.sumOf { it.amount }
    val monthTotal = txByDate.filterKeys { it.year == selectedMonth.year && it.month == selectedMonth.month }
        .values.sumOf { list -> list.sumOf { it.amount } }

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
            // Month Switcher & Quick Meta Row
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
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = colors.container,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            onClick = { selectedMonth = selectedMonth.minusMonths(1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Prev Month",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            letterSpacing = (-0.2).sp
                        )

                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = colors.container,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            onClick = { selectedMonth = selectedMonth.plusMonths(1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Days with spending, as quiet meta text
                    Text(
                        text = "$daysWithTx ${if (daysWithTx == 1) "day" else "days"} with spending",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = colors.textSecondary
                    )
                }
            }

            // Monthly Calendar Card
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Day headers (S, M, T, W, T, F, S)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textMuted,
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.sp
                                )
                            }
                        }

                        // Calendar Grid
                        val firstDayOfMonth = selectedMonth.atDay(1)
                        val daysInMonth = selectedMonth.lengthOfMonth()
                        val leadingEmpty = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
                        val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (week in 0 until (totalCells / 7)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    for (dayOfWeek in 0..6) {
                                        val cellIndex = week * 7 + dayOfWeek
                                        val dayNumber = cellIndex - leadingEmpty + 1

                                        if (dayNumber in 1..daysInMonth) {
                                            val date = selectedMonth.atDay(dayNumber)
                                            val isSelected = date == selectedDate
                                            val hasTx = txByDate.containsKey(date)

                                            Column(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(44.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .clickable { selectedDate = date }
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier
                                                                .background(colors.primary)
                                                        } else {
                                                            Modifier
                                                        }
                                                    ),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                                                    color = if (isSelected) colors.onPrimary else colors.textPrimary,
                                                    textAlign = TextAlign.Center
                                                )
                                                // Activity indicator dots
                                                if (hasTx) {
                                                    Row(
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                        modifier = Modifier.padding(top = 2.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .background(
                                                                    if (isSelected) colors.onPrimary
                                                                    else colors.textSecondary,
                                                                    CircleShape
                                                                )
                                                        )
                                                    }
                                                } else {
                                                    Spacer(Modifier.height(8.dp))
                                                }
                                            }
                                        } else {
                                            // Overflow cell from previous or next month
                                            Box(
                                                modifier = Modifier
                                                    .width(40.dp)
                                                    .height(44.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val prevMonthDays = selectedMonth.minusMonths(1).lengthOfMonth()
                                                val overflowDay = if (dayNumber <= 0) prevMonthDays + dayNumber else dayNumber - daysInMonth
                                                Text(
                                                    text = overflowDay.toString(),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = colors.textMuted.copy(alpha = 0.4f),
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Daily Outflow Stream / Day Details Card
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
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (dayTransactions.isNotEmpty()) "${dayTransactions.size} ${if (dayTransactions.size == 1) "expense" else "expenses"}" else "No spending recorded",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            if (dayTotal > 0) {
                                Text(
                                    text = "-$currencySymbol${String.format("%,.2f", dayTotal)}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary,
                                    style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        if (dayTransactions.isEmpty()) {
                            // Empty State Container
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(colors.containerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FreeBreakfast,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Nothing spent",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "Tap + to add an expense for this day",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 4.dp, start = 20.dp, end = 20.dp)
                                )
                            }
                        } else {
                            dayTransactions.forEachIndexed { index, tx ->
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { onTransactionClick(tx.id) },
                                    currencySymbol = currencySymbol
                                )
                                if (index < dayTransactions.lastIndex) {
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

            // Month summary: a real figure instead of a slogan
            item {
                SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${selectedMonth.format(DateTimeFormatter.ofPattern("MMMM"))} total",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", monthTotal)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
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
