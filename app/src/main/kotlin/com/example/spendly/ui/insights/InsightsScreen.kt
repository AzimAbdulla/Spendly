package com.example.spendly.ui.insights

import com.example.spendly.data.model.spend
import kotlin.math.roundToInt
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.example.spendly.ui.theme.TintTimeAlt
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

    val periodTotal = periodTransactions.sumOf { it.spend }
    // A finished month averages over all its days; the current one over the days so far
    val elapsedUnits = when {
        !isMonthlySelected -> 7
        selectedMonth == YearMonth.from(today) -> today.dayOfMonth.coerceAtLeast(1)
        else -> selectedMonth.lengthOfMonth()
    }
    val dailyAverage = if (periodTotal > 0) periodTotal / elapsedUnits else 0.0

    val categorySums = remember(periodTransactions) {
        periodTransactions.groupBy { it.category }.mapValues { it.value.sumOf { tx -> tx.spend } }
    }
    val activeCategories = categorySums.filter { it.value > 0 }.entries.sortedByDescending { it.value }
    val topCategoryEntry = activeCategories.firstOrNull()
    val topCategory = topCategoryEntry?.key ?: Category.FOOD
    val topCategoryAmount = topCategoryEntry?.value ?: 0.0
    // The time-based charts here (the weekly grid and the trend) share one hue
    val timeHue = TintTimeAlt
    val topCategoryPct = if (periodTotal > 0) ((topCategoryAmount / periodTotal) * 100).roundToInt() else 0

    // The trend chart: six months ending at the selected one, or the selected week's seven days.
    // Each bar is (short label, full label, amount)
    val trendBars = remember(transactions, isMonthlySelected, selectedMonth, weekStart) {
        if (isMonthlySelected) {
            (5 downTo 0).map { selectedMonth.minusMonths(it.toLong()) }.map { ym ->
                Triple(
                    ym.format(DateTimeFormatter.ofPattern("MMM")),
                    ym.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    transactions.filter { YearMonth.from(it.date) == ym }.sumOf { it.spend }
                )
            }
        } else {
            (0..6).map { weekStart.plusDays(it.toLong()) }.map { date ->
                Triple(
                    date.format(DateTimeFormatter.ofPattern("EEE")),
                    if (date == today) "Today" else date.format(DateTimeFormatter.ofPattern("EEEE, d MMM")),
                    transactions.filter { it.date == date }.sumOf { it.spend }
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

            // The period in one tile: its total, the daily average and what led
            item {
                TintedTile(tint = colors.tint(TintMoney), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Spent", fontSize = 13.sp, color = tile.textSecondary)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", periodTotal)}",
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = tile.textPrimary,
                            maxLines = 1,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (topCategoryEntry == null) "No spending yet" else
                                "$currencySymbol${String.format("%,.0f", dailyAverage)} a day \u00B7 ${topCategory.displayName} leads at $topCategoryPct%",
                            fontSize = 13.sp,
                            color = tile.textSecondary,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Weekly: when in the day the money goes, as a grid of shaded squares. (The month's
            // day-by-day shading lives on the Calendar screen)
            if (!isMonthlySelected) {
                item {
                    WeekHeatmap(
                        weekStart = weekStart,
                        transactions = periodTransactions,
                        hue = timeHue,
                        currencySymbol = currencySymbol
                    )
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
                                                text = "${(pct * 100).roundToInt()}%",
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
                TintedTile(tint = colors.tint(timeHue), shape = RoundedCornerShape(24.dp)) {
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

/** The parts of the day the weekly grid sorts expenses into, by the hour they start at. */
private val DayParts = listOf("Morning" to 5, "Afternoon" to 12, "Evening" to 17, "Night" to 21)

/**
 * The selected week as a grid of shaded squares: a column per day and a row per part of the day
 * (morning from 5am, afternoon from noon, evening from 5pm, night from 9pm), so it shows when in
 * the day money goes. Tap a square to show it in the header, press
 * and hold for its details. Expenses saved without a time of day (before times were recorded)
 * can't be placed, so they're left out of the grid.
 */
@Composable
private fun WeekHeatmap(weekStart: LocalDate, transactions: List<Transaction>, hue: Color, currencySymbol: String) {
    val colors = SpendlyTheme.colors
    val today = LocalDate.now()
    // Keyed by column (day of the week shown) then row (part of the day)
    val byCell = remember(transactions, weekStart) {
        transactions.filter { it.time != null }.groupBy { tx ->
            val hour = tx.time!!.hour
            val part = if (hour < DayParts[0].second) DayParts.lastIndex else DayParts.indexOfLast { hour >= it.second }
            (tx.date.toEpochDay() - weekStart.toEpochDay()).toInt() to part
        }
    }
    val totals = remember(byCell) { byCell.mapValues { (_, txs) -> txs.sumOf { it.spend } } }
    val level = heatLevels(totals.values)
    val ramp = heatRamp(hue, colors.textPrimary)
    val rows = DayParts.size

    // Starts on the week's busiest square, the one most worth a look
    val busiest = totals.maxByOrNull { it.value }?.key ?: (6 to 0)
    var picked by remember(weekStart, busiest) { mutableStateOf(busiest) }
    var held by remember(weekStart) { mutableStateOf<Pair<Int, Int>?>(null) }
    val shown = held ?: picked
    val haptics = LocalHapticFeedback.current
    val gapPx = with(LocalDensity.current) { HeatGap.toPx() }
    fun title(cell: Pair<Int, Int>, pattern: String) =
        "${weekStart.plusDays(cell.first.toLong()).format(DateTimeFormatter.ofPattern(pattern))} ${DayParts[cell.second].first.lowercase()}"

    SpendlyCard(shape = RoundedCornerShape(24.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "When you spend",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(
                    text = "$currencySymbol${String.format("%,.0f", totals[shown] ?: 0.0)}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }
            Text(text = title(shown, "EEEE"), fontSize = 12.sp, color = colors.textSecondary)

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(HeatGap)) {
                Spacer(Modifier.width(HeatRowLabel))
                for (column in 0..6) {
                    val date = weekStart.plusDays(column.toLong())
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("EEEEE")),
                        fontSize = 11.sp,
                        color = if (date == today) colors.textPrimary else colors.textMuted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(HeatGap)) {
                // Row labels sit outside the touch area, sharing each row's height
                Column(Modifier.width(HeatRowLabel), verticalArrangement = Arrangement.spacedBy(HeatGap)) {
                    DayParts.forEach { (name, _) ->
                        Box(Modifier.fillMaxWidth().aspectRatio(HeatRowLabel / WeekCellHeight), contentAlignment = Alignment.CenterStart) {
                            Text(name, fontSize = 11.sp, color = colors.textMuted, maxLines = 1)
                        }
                    }
                }
                Box(Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier
                            .pointerInput(weekStart) {
                                detectTapGestures(onTap = { position ->
                                    picked = heatCellAt(position, size, columns = 7, rows = rows, gapPx = gapPx)
                                })
                            }
                            .pointerInput(weekStart) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { position ->
                                        held = heatCellAt(position, size, columns = 7, rows = rows, gapPx = gapPx)
                                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragEnd = { held?.let { picked = it }; held = null },
                                    onDragCancel = { held = null }
                                ) { change, _ ->
                                    held = heatCellAt(change.position, size, columns = 7, rows = rows, gapPx = gapPx)
                                }
                            },
                        verticalArrangement = Arrangement.spacedBy(HeatGap)
                    ) {
                        for (row in 0 until rows) {
                            Row(Modifier.height(WeekCellHeight), horizontalArrangement = Arrangement.spacedBy(HeatGap)) {
                                for (column in 0..6) {
                                    val cell = column to row
                                    val fill by animateColorAsState(
                                        ramp[level(totals[cell] ?: 0.0)],
                                        animationSpec = Motion.move(),
                                        label = "weekHeatCell"
                                    )
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                            .clip(HeatCellShape)
                                            .background(fill)
                                            .then(
                                                if (cell == shown) Modifier.border(2.dp, colors.textPrimary, HeatCellShape)
                                                else Modifier
                                            )
                                    )
                                }
                            }
                        }
                    }

                    held?.let { cell ->
                        val expenses = byCell[cell].orEmpty()
                        HeatPopup(
                            title = title(cell, "EEEE, d MMMM,"),
                            expenses = expenses,
                            note = expenseCount(expenses.size),
                            currencySymbol = currencySymbol,
                            centerX = (cell.first + 0.5f) / 7f,
                            top = cell.second.toFloat() / rows
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            HeatLegend(ramp)
        }
    }
}

/** Empty, then four steps of [hue]. */
internal fun heatRamp(hue: Color, ink: Color) = listOf(
    ink.copy(alpha = 0.07f),
    hue.copy(alpha = 0.28f),
    hue.copy(alpha = 0.50f),
    hue.copy(alpha = 0.75f),
    hue
)

/**
 * Sorts amounts into the ramp's steps by how they rank among [amounts] (quartiles), so one huge
 * figure doesn't wash the rest out. Zero is always the empty step.
 */
internal fun heatLevels(amounts: Collection<Double>): (Double) -> Int {
    val spends = amounts.filter { it > 0 }.sorted()
    // The upper bounds of the first three steps; anything above the last is the darkest
    val steps = if (spends.isEmpty()) emptyList() else listOf(0.25, 0.5, 0.75).map { spends[((spends.size - 1) * it).toInt()] }
    return { amount -> if (amount <= 0) 0 else 1 + steps.count { amount > it } }
}

/** The column and row under a touch in a grid of equal squares with [gapPx] between them. */
private fun heatCellAt(position: Offset, size: IntSize, columns: Int, rows: Int, gapPx: Float): Pair<Int, Int> {
    val cellWidth = (size.width - gapPx * (columns - 1)) / columns
    val cellHeight = (size.height - gapPx * (rows - 1)) / rows
    return (position.x / (cellWidth + gapPx)).toInt().coerceIn(0, columns - 1) to
        (position.y / (cellHeight + gapPx)).toInt().coerceIn(0, rows - 1)
}

private fun expenseCount(count: Int) = when (count) {
    0 -> "No spending"
    1 -> "1 expense"
    else -> "$count expenses"
}

/** Less to more, bottom right like GitHub's. */
@Composable
private fun HeatLegend(ramp: List<Color>) {
    val colors = SpendlyTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Less", fontSize = 11.sp, color = colors.textMuted, modifier = Modifier.padding(end = 2.dp))
        ramp.forEach { Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(it)) }
        Text("More", fontSize = 11.sp, color = colors.textMuted, modifier = Modifier.padding(start = 2.dp))
    }
}

/**
 * A held square's details, floating just above it: what it covers, the spend, a line of context
 * ([note]) and the largest expense. It can't take focus, so the finger
 * that opened it keeps sliding across the grid underneath.
 */
@Composable
private fun HeatPopup(
    title: String,
    expenses: List<Transaction>,
    note: String,
    currencySymbol: String,
    centerX: Float,
    top: Float
) {
    val colors = SpendlyTheme.colors
    val density = LocalDensity.current
    val total = expenses.sumOf { it.spend }
    val largest = expenses.filter { !it.paidBack }.maxByOrNull { it.amount }
    val position = remember(centerX, top, density) {
        object : PopupPositionProvider {
            override fun calculatePosition(
                anchorBounds: IntRect,
                windowSize: IntSize,
                layoutDirection: LayoutDirection,
                popupContentSize: IntSize
            ): IntOffset {
                val margin = with(density) { 12.dp.roundToPx() }
                val x = (anchorBounds.left + anchorBounds.width * centerX - popupContentSize.width / 2f).toInt()
                    .coerceIn(margin, (windowSize.width - popupContentSize.width - margin).coerceAtLeast(margin))
                val y = (anchorBounds.top + anchorBounds.height * top).toInt() - popupContentSize.height - margin
                return IntOffset(x, y.coerceAtLeast(margin))
            }
        }
    }
    Popup(popupPositionProvider = position, properties = PopupProperties(focusable = false)) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(colors.popup)
                .border(1.dp, colors.border, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(title, fontSize = 12.sp, color = colors.textSecondary)
            Text(
                text = "$currencySymbol${String.format("%,.2f", total)}",
                fontFamily = SpaceGroteskFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(text = note, fontSize = 12.sp, color = colors.textSecondary)
            if (largest != null) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(8.dp)
                            .background(stitchCategoryColors[largest.category] ?: colors.textMuted, CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = largest.item.ifBlank { largest.merchant }.ifBlank { largest.category.displayName },
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "$currencySymbol${String.format("%,.0f", largest.amount)}",
                        fontSize = 13.sp,
                        color = colors.textSecondary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private val HeatGap = 4.dp
private val HeatRowLabel = 64.dp
private val WeekCellHeight = 34.dp
private val HeatCellShape = RoundedCornerShape(8.dp)

/** A day square's width over its height: a little wider than tall, so five weeks stay compact. */
private const val HeatCellRatio = 1.25f
