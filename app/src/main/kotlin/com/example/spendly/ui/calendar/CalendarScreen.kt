package com.example.spendly.ui.calendar

import com.example.spendly.ui.insights.heatLevels
import com.example.spendly.ui.insights.heatRamp
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
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import com.example.spendly.ui.components.TransactionRows
import com.example.spendly.ui.theme.Motion
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.theme.tint
import com.example.spendly.ui.theme.TintMoney
import com.example.spendly.ui.theme.TintList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    // The month is shared with Home and Insights
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthSelected: (YearMonth) -> Unit = {},
    today: LocalDate = LocalDate.now(),
    onAddForDate: (LocalDate) -> Unit = {},
    currencySymbol: String = "₹"
) {
    val colors = SpendlyTheme.colors
    // Opens on today in the current month, and on the 1st of any other
    var selectedDate by remember(selectedMonth) {
        mutableStateOf(if (YearMonth.from(today) == selectedMonth) today else selectedMonth.atDay(1))
    }

    // Map of dates with transactions in this month
    val txByDate = remember(transactions) {
        transactions.groupBy { it.date }
    }

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
            // Month Switcher
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
                            onClick = { onMonthSelected(selectedMonth.minusMonths(1)) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronLeft,
                                    contentDescription = "Previous month",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        AnimatedContent(
                            targetState = selectedMonth,
                            transitionSpec = { fadeIn(Motion.fadeInAfter()) togetherWith fadeOut(Motion.fadeOutFirst()) },
                            label = "calendarTitle"
                        ) { month ->
                            Text(
                                text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary,
                                letterSpacing = (-0.2).sp
                            )
                        }

                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = colors.container,
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                            enabled = selectedMonth < YearMonth.from(today),
                            onClick = { onMonthSelected(selectedMonth.plusMonths(1)) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = "Next month",
                                    tint = colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Monthly Calendar Card
            item {
                TintedTile(tint = colors.tint(TintMoney), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
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
                                    color = tile.textMuted,
                                    modifier = Modifier.width(40.dp),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.sp
                                )
                            }
                        }

                        // Calendar Grid: a new month slides in from the side you're heading to
                        AnimatedContent(
                            targetState = selectedMonth,
                            transitionSpec = {
                                val forward = targetState > initialState
                                (slideInHorizontally(Motion.enter(delayMillis = Motion.HandOff)) { if (forward) it / 5 else -it / 5 } + fadeIn(Motion.fadeInAfter()))
                                    .togetherWith(slideOutHorizontally(Motion.exit()) { if (forward) -it / 5 else it / 5 } + fadeOut(Motion.fadeOutFirst()))
                                    .using(SizeTransform(clip = false) { _, _ -> Motion.move() })
                            },
                            label = "calendarMonth"
                        ) { month ->
                            val firstDayOfMonth = month.atDay(1)
                            val daysInMonth = month.lengthOfMonth()
                            val leadingEmpty = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
                            val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7
                            // Each day is shaded by how its spend ranks in the month, in four steps
                            val dayTotals = (1..daysInMonth).associateWith { day ->
                                txByDate[month.atDay(day)]?.sumOf { it.amount } ?: 0.0
                            }
                            val levelOf = heatLevels(dayTotals.values)
                            val ramp = heatRamp(tile.primary, tile.textPrimary)
                            val tileBackground = colors.tint(TintMoney).background

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
                                                val date = month.atDay(dayNumber)
                                                val isSelected = date == selectedDate
                                                val level = levelOf(dayTotals[dayNumber] ?: 0.0)

                                                Box(
                                                    modifier = Modifier
                                                        .width(40.dp)
                                                        .height(44.dp)
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .clickable { selectedDate = date }
                                                        .then(if (level > 0) Modifier.background(ramp[level]) else Modifier)
                                                        .then(
                                                            if (isSelected) Modifier.border(1.5.dp, tile.textPrimary, RoundedCornerShape(14.dp))
                                                            else Modifier
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = dayNumber.toString(),
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected || date == today) FontWeight.Medium else FontWeight.Normal,
                                                        // The two strongest steps are bright, so their number goes dark
                                                        color = when {
                                                            level >= 3 -> tileBackground
                                                            date > today -> tile.textMuted
                                                            else -> tile.textPrimary
                                                        },
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                            } else {
                                                // Overflow cell from previous or next month
                                                Box(
                                                    modifier = Modifier
                                                        .width(40.dp)
                                                        .height(44.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val prevMonthDays = month.minusMonths(1).lengthOfMonth()
                                                    val overflowDay = if (dayNumber <= 0) prevMonthDays + dayNumber else dayNumber - daysInMonth
                                                    Text(
                                                        text = overflowDay.toString(),
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Normal,
                                                        color = tile.textMuted.copy(alpha = 0.4f),
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
            }

            // Daily Outflow Stream / Day Details Card
            item {
                TintedTile(tint = colors.tint(TintList), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
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
                                    color = tile.textPrimary
                                )
                                Text(
                                    text = if (dayTransactions.isNotEmpty()) "${dayTransactions.size} ${if (dayTransactions.size == 1) "expense" else "expenses"}" else "No spending recorded",
                                    fontSize = 12.sp,
                                    color = tile.textSecondary
                                )
                            }
                            if (dayTotal > 0) {
                                Text(
                                    text = "$currencySymbol${String.format("%,.2f", dayTotal)}",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tile.textPrimary,
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
                                        .background(tile.containerHigh),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FreeBreakfast,
                                        contentDescription = null,
                                        tint = tile.textSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Nothing spent",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tile.textPrimary
                                )
                                if (selectedDate <= today) {
                                    Text(
                                        text = "Add an expense for this day",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = tile.textPrimary,
                                        modifier = Modifier
                                            .padding(top = 12.dp)
                                            .clip(CircleShape)
                                            .clickable { onAddForDate(selectedDate) }
                                            .background(tile.containerHigh)
                                            .padding(horizontal = 14.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        } else {
                            TransactionRows(
                                transactions = dayTransactions,
                                onTransactionClick = onTransactionClick,
                                currencySymbol = currencySymbol,
                                resetKey = selectedDate
                            )
                        }
                    }
                }
            }

            // Month summary: a real figure instead of a slogan
            item {
                TintedTile(tint = colors.tint(TintMoney), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
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
                            color = tile.textSecondary
                        )
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", monthTotal)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = tile.textPrimary,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                    }
                }
            }

            // Bottom clearance for dock
            item {
                Spacer(Modifier.navigationBarsPadding().height(DockClearance))
            }
        }
    }
}
