package com.example.spendly.ui.insights

import androidx.compose.foundation.background
import com.example.spendly.ui.theme.Motion
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.border
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
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.getIcon
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.example.spendly.ui.components.GlassSegmentedControl
import com.example.spendly.ui.components.MonthPickerMenu
import androidx.compose.runtime.mutableIntStateOf
import com.example.spendly.ui.components.WeekPickerMenu
import com.example.spendly.ui.components.weekLabel
import com.example.spendly.ui.components.weekRange
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.theme.tint
import com.example.spendly.ui.theme.TintMoney
import com.example.spendly.ui.theme.TintTime
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier,
    currencySymbol: String = "₹",
    defaultCurrency: String = "INR",
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthSelected: (YearMonth) -> Unit = {},
    weeklyRequest: Int = 0
) {
    val colors = SpendlyTheme.colors
    var isMonthlySelected by remember { mutableStateOf(true) }
    // The This week widget opens Insights in weekly view
    LaunchedEffect(weeklyRequest) {
        if (weeklyRequest > 0) isMonthlySelected = false
    }
    var monthPickerOpen by remember { mutableStateOf(false) }
    // Weekly mode: 0 is the last 7 days, 1 the 7 days before that, and so on
    var weekOffset by remember { mutableIntStateOf(0) }
    var weekPickerOpen by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    // Dynamically filter transactions based on selected timeframe
    val (weekStart, weekEnd) = weekRange(weekOffset, today)
    val periodTransactions = remember(transactions, isMonthlySelected, selectedMonth, weekOffset) {
        if (isMonthlySelected) {
            transactions.filter { YearMonth.from(it.date) == selectedMonth }
        } else {
            transactions.filter { !it.date.isBefore(weekStart) && !it.date.isAfter(weekEnd) }
        }
    }

    val periodLabel = if (isMonthlySelected) {
        selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    } else {
        weekLabel(weekOffset, today)
    }

    val periodTotal = periodTransactions.sumOf { it.amount }
    // A finished month averages over all its days; the current one over the days so far
    val elapsedUnits = when {
        !isMonthlySelected -> 7
        selectedMonth == YearMonth.from(today) -> today.dayOfMonth.coerceAtLeast(1)
        else -> selectedMonth.lengthOfMonth()
    }
    val dailyAverage = if (periodTotal > 0) periodTotal / elapsedUnits else 0.0

    val categorySums = remember(periodTransactions) {
        periodTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.amount } }
    }
    val activeCategories = categorySums.filter { it.value > 0 }.entries.sortedByDescending { it.value }
    val topCategoryEntry = activeCategories.firstOrNull()
    val topCategory = topCategoryEntry?.key ?: Category.FOOD
    val topCategoryAmount = topCategoryEntry?.value ?: 0.0
    val topCategoryPct = if (periodTotal > 0) ((topCategoryAmount / periodTotal) * 100).toInt() else 0

    // The trend chart: six months ending at the selected one, or the selected week's seven days.
    // Each bar is (short label, full label, amount)
    val trendBars = remember(transactions, isMonthlySelected, selectedMonth, weekStart) {
        if (isMonthlySelected) {
            (5 downTo 0).map { selectedMonth.minusMonths(it.toLong()) }.map { ym ->
                Triple(
                    ym.format(DateTimeFormatter.ofPattern("MMM")),
                    ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    transactions.filter { YearMonth.from(it.date) == ym }.sumOf { it.amount }
                )
            }
        } else {
            (0..6).map { weekStart.plusDays(it.toLong()) }.map { date ->
                Triple(
                    date.format(DateTimeFormatter.ofPattern("EEE")),
                    if (date == today) "Today" else date.format(DateTimeFormatter.ofPattern("EEEE, d MMM")),
                    transactions.filter { it.date == date }.sumOf { it.amount }
                )
            }
        }
    }
    val maxTrendVal = (trendBars.maxOfOrNull { it.third } ?: 1.0).coerceAtLeast(1.0)
    // The picked bar starts on the latest one, again whenever the period changes
    var pickedBar by remember(isMonthlySelected, selectedMonth, weekStart) { mutableIntStateOf(trendBars.lastIndex) }
    val picked = trendBars[pickedBar.coerceIn(0, trendBars.lastIndex)]

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
                    // Period pill: a month picker in Monthly, a week picker in Weekly
                    Box {
                    Surface(
                        onClick = { if (isMonthlySelected) monthPickerOpen = true else weekPickerOpen = true },
                        shape = RoundedCornerShape(12.dp),
                        color = colors.container,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
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
                    MonthPickerMenu(
                        expanded = monthPickerOpen,
                        selected = selectedMonth,
                        onDismiss = { monthPickerOpen = false },
                        onSelect = {
                            onMonthSelected(it)
                            monthPickerOpen = false
                        }
                    )
                    WeekPickerMenu(
                        expanded = weekPickerOpen,
                        selectedOffset = weekOffset,
                        onDismiss = { weekPickerOpen = false },
                        onSelect = {
                            weekOffset = it
                            weekPickerOpen = false
                        }
                    )
                    }

                    // Monthly vs Weekly: one full-height pill slides to the selected half
                    GlassSegmentedControl(
                        items = listOf(true to "Monthly", false to "Weekly"),
                        selected = isMonthlySelected,
                        onSelect = { isMonthlySelected = it }
                    )
                }
            }

            // Summary figures: label, number, what it compares to
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TintedTile(
                        tint = colors.tint(TintMoney),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        val tile = SpendlyTheme.colors
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Spent",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = tile.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", periodTotal)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = tile.textPrimary,
                                maxLines = 1,
                                style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.2f", dailyAverage)} a day on average",
                                fontSize = 12.sp,
                                color = tile.textSecondary,
                                maxLines = 1
                            )
                        }
                    }

                    TintedTile(
                        tint = colors.tint(topCategoryEntry?.key?.let { stitchCategoryColors[it] } ?: colors.textMuted),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        val tile = SpendlyTheme.colors
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Top category",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = tile.textSecondary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = topCategoryEntry?.key?.displayName ?: "None yet",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 20.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.Medium,
                                color = tile.textPrimary,
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
                                            .background(stitchCategoryColors[topCategory] ?: tile.textMuted, CircleShape)
                                    )
                                }
                                Text(
                                    text = if (topCategoryEntry != null) "$topCategoryPct% of spending" else "No spending yet",
                                    fontSize = 12.sp,
                                    color = tile.textSecondary
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

            // Spending trend, the main chart on Insights: months in monthly mode, days in weekly.
            // Tap a bar, or drag sideways across them, to see its spend in the header
            item {
                TintedTile(tint = colors.tint(TintTime), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isMonthlySelected) "Monthly spending" else "Daily spending",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = tile.textPrimary
                            )
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", picked.third)}",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = tile.textPrimary
                            )
                        }
                        Text(
                            text = picked.second,
                            fontSize = 12.sp,
                            color = tile.textSecondary
                        )

                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .padding(horizontal = 4.dp)
                                .pointerInput(trendBars.size) {
                                    detectTapGestures(onTap = { pickedBar = barAt(it.x, size.width, trendBars.size) })
                                }
                                .pointerInput(trendBars.size) {
                                    detectHorizontalDragGestures(onDragStart = { pickedBar = barAt(it.x, size.width, trendBars.size) }) { change, _ ->
                                        pickedBar = barAt(change.position.x, size.width, trendBars.size)
                                    }
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            trendBars.forEachIndexed { index, (label, _, amt) ->
                                val isPicked = index == pickedBar
                                val fraction = (amt / maxTrendVal).toFloat().coerceIn(0f, 1f)
                                val barShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                val barColor by animateColorAsState(
                                    if (isPicked) tile.primary else tile.textMuted.copy(alpha = 0.45f),
                                    animationSpec = Motion.move(Motion.Short),
                                    label = "trendBar"
                                )

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Bottom,
                                    modifier = Modifier.weight(1f)
                                ) {
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
                                                .background(tile.textPrimary.copy(alpha = 0.08f))
                                        )
                                        if (amt > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(maxOf((100 * fraction).dp, 4.dp))
                                                    .clip(barShape)
                                                    .background(barColor)
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(8.dp))

                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isPicked) tile.textPrimary else tile.textMuted,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
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

/** Which of [count] equal columns sits under [x] in a row [width] wide. */
private fun barAt(x: Float, width: Int, count: Int): Int = (x / width * count).toInt().coerceIn(0, count - 1)
