package com.example.spendly.ui.calendar

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.spendly.ui.theme.ChevronLeft
import com.example.spendly.ui.theme.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Transaction
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.TransactionItem
import com.example.spendly.ui.theme.StitchBackground
import com.example.spendly.ui.theme.StitchBorder
import com.example.spendly.ui.theme.StitchCatEntertainment
import com.example.spendly.ui.theme.StitchCatFood
import com.example.spendly.ui.theme.StitchCatGroceries
import com.example.spendly.ui.theme.StitchCatTransport
import com.example.spendly.ui.theme.StitchContainer
import com.example.spendly.ui.theme.StitchContainerLow
import com.example.spendly.ui.theme.StitchOnPrimary
import com.example.spendly.ui.theme.StitchOnSurface
import com.example.spendly.ui.theme.StitchOnSurfaceVariant
import com.example.spendly.ui.theme.StitchPrimary
import com.example.spendly.ui.theme.StitchPrimaryFixed
import com.example.spendly.ui.theme.StitchSecondary
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
                        Box(modifier = Modifier.size(8.dp).background(StitchPrimary, CircleShape))
                        Text(
                            text = "Calendar",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = StitchOnSurfaceVariant)
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(32.dp)
                            .background(StitchPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = StitchOnPrimary, modifier = Modifier.size(18.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StitchBackground)
            )
        },
        containerColor = StitchBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Month Switcher Header Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
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
                            color = StitchContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                            onClick = { selectedMonth = selectedMonth.minusMonths(1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = StitchOnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }

                        Text(
                            text = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )

                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
                            color = StitchContainerLow,
                            border = androidx.compose.foundation.BorderStroke(1.dp, StitchBorder),
                            onClick = { selectedMonth = selectedMonth.plusMonths(1) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = StitchOnSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Active Days Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(StitchPrimaryFixed)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(StitchPrimary, CircleShape))
                            Text(
                                text = "$daysWithTx Active Days",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                color = StitchPrimary
                            )
                        }
                    }
                }
            }

            // Interactive Calendar Grid Card
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Day-of-week header
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                                Text(
                                    text = day,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.W600,
                                    color = StitchOnSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Calendar Days Grid
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
                                                    .width(36.dp)
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .clickable { selectedDate = date }
                                                    .then(
                                                        if (isSelected) Modifier.background(StitchPrimary)
                                                        else Modifier
                                                    )
                                                    .padding(vertical = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Text(
                                                    text = dayNumber.toString(),
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500,
                                                    color = if (isSelected) StitchOnPrimary else StitchOnSurface,
                                                    textAlign = TextAlign.Center
                                                )
                                                // Activity indicator dot
                                                Box(
                                                    modifier = Modifier
                                                        .padding(top = 2.dp)
                                                        .size(4.dp)
                                                        .background(
                                                            when {
                                                                isSelected -> StitchOnPrimary
                                                                hasTx -> StitchSecondary
                                                                else -> Color.Transparent
                                                            },
                                                            CircleShape
                                                        )
                                                )
                                            }
                                        } else {
                                            Spacer(Modifier.width(36.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SELECTED DAY Summary Card
            item {
                SpendlyCard {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SELECTED DAY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurfaceVariant,
                                letterSpacing = 0.8.sp
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(StitchPrimaryFixed)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${dayTransactions.size} Recorded",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.W600,
                                    color = StitchPrimary
                                )
                            }
                        }

                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy")),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.W700,
                            color = StitchOnSurface
                        )

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", dayTotal)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurface
                            )
                            if (dayTotal > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "✓ On Track",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.W600,
                                        color = StitchSecondary
                                    )
                                }
                            }
                        }

                        // Category distribution bar
                        if (dayTotal > 0) {
                            val catGroups = dayTransactions.groupBy { it.category }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape)
                            ) {
                                catGroups.forEach { (cat, txs) ->
                                    val catSum = txs.sumOf { it.amount }
                                    val weight = (catSum / dayTotal).toFloat()
                                    val color = when (cat) {
                                        com.example.spendly.data.model.Category.FOOD -> StitchCatFood
                                        com.example.spendly.data.model.Category.GROCERIES -> StitchCatGroceries
                                        com.example.spendly.data.model.Category.TRANSPORT -> StitchCatTransport
                                        else -> StitchCatEntertainment
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(weight.coerceAtLeast(0.05f))
                                            .fillMaxSize()
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Timeline & Receipts section header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Timeline & Receipts",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = StitchOnSurface
                    )
                    Text(
                        text = "Categorized",
                        fontSize = 12.sp,
                        color = StitchOnSurfaceVariant
                    )
                }
            }

            // Timeline Items
            if (dayTransactions.isEmpty()) {
                item {
                    SpendlyCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No transactions logged on this day",
                                fontSize = 13.sp,
                                color = StitchOnSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                item {
                    SpendlyCard {
                        Column(modifier = Modifier.padding(12.dp)) {
                            dayTransactions.forEachIndexed { idx, tx ->
                                TransactionItem(
                                    transaction = tx,
                                    onClick = { onTransactionClick(tx.id) },
                                    currencySymbol = currencySymbol
                                )
                                if (idx < dayTransactions.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(1.dp)
                                            .background(StitchContainer)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // LEDGER WISDOM card
            item {
                SpendlyCard(
                    backgroundColor = StitchContainerLow,
                    borderColor = Color.Transparent
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StitchContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✦", fontSize = 16.sp, color = StitchPrimary)
                        }
                        Column {
                            Text(
                                text = "LEDGER WISDOM",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.W700,
                                color = StitchOnSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Awareness over restriction. Daily logs create lasting clarity.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                                color = StitchOnSurface
                            )
                        }
                    }
                }
            }

            // Bottom spacer for the floating dock
            item {
                Spacer(Modifier.height(88.dp))
            }
        }
    }
}
