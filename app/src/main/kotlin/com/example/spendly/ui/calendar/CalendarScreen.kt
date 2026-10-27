package com.example.spendly.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
                                .shadow(8.dp, CircleShape, spotColor = Color(0x99CABEFE))
                                .background(colors.primary, CircleShape)
                        )
                        Text(
                            text = "Calendar",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary,
                            letterSpacing = (-0.2).sp
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = colors.textSecondary,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(colors.containerHigh)
                                .border(1.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile",
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
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

                    // Active Days Chip
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.container)
                            .border(1.dp, colors.border, CircleShape)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .shadow(6.dp, CircleShape, spotColor = Color(0xB39E8CFC))
                                    .background(colors.primary, CircleShape)
                            )
                            Text(
                                text = "$daysWithTx Active Days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                        }
                    }
                }
            }

            // Monthly Calendar Card
            item {
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    color = colors.textSecondary,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Calendar Grid
                        val firstDayOfMonth = selectedMonth.atDay(1)
                        val daysInMonth = selectedMonth.lengthOfMonth()
                        val leadingEmpty = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
                        val totalCells = ((leadingEmpty + daysInMonth + 6) / 7) * 7

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                                    .width(38.dp)
                                                    .height(42.dp)
                                                    .clip(CircleShape)
                                                    .clickable { selectedDate = date }
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier
                                                                .background(colors.primary)
                                                                .shadow(8.dp, CircleShape, spotColor = Color(0x809E8CFC))
                                                        } else {
                                                            Modifier
                                                        }
                                                    ),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
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
                                                                    else colors.secondary,
                                                                    CircleShape
                                                                )
                                                        )
                                                    }
                                                } else {
                                                    Spacer(Modifier.height(6.dp))
                                                }
                                            }
                                        } else {
                                            // Overflow cell from previous or next month
                                            Box(
                                                modifier = Modifier
                                                    .width(38.dp)
                                                    .height(42.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val prevMonthDays = selectedMonth.minusMonths(1).lengthOfMonth()
                                                val overflowDay = if (dayNumber <= 0) prevMonthDays + dayNumber else dayNumber - daysInMonth
                                                Text(
                                                    text = overflowDay.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Normal,
                                                    color = colors.textMuted.copy(alpha = 0.35f),
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
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = if (dayTransactions.isNotEmpty()) "${dayTransactions.size} entries recorded" else "No spending recorded",
                                    fontSize = 12.sp,
                                    color = colors.textSecondary
                                )
                            }
                            if (dayTotal > 0) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(colors.error.copy(alpha = 0.15f))
                                        .border(1.dp, colors.error.copy(alpha = 0.3f), CircleShape)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "-$currencySymbol${String.format("%,.2f", dayTotal)}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.error
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(14.dp))

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
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(colors.containerHigh)
                                        .border(1.dp, colors.border, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FreeBreakfast,
                                        contentDescription = null,
                                        tint = colors.textSecondary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(Modifier.height(10.dp))
                                Text(
                                    text = "Quiet Spending Day",
                                    fontFamily = SpaceGroteskFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "No expenses recorded on this day. Tap + to add an entry.",
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
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        color = colors.divider
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Ledger Wisdom Card
            item {
                SpendlyCard(shape = RoundedCornerShape(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(colors.containerHigh)
                                .border(1.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "LEDGER WISDOM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Text(
                                text = "Awareness over restriction. Daily logs create lasting clarity.",
                                fontSize = 12.sp,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Normal
                            )
                        }
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
