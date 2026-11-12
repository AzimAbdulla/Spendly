package com.example.spendly.ui.home

import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.drawBehind
import com.example.spendly.data.currencySymbol
import com.example.spendly.ui.theme.Tune
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import com.example.spendly.ui.theme.ChevronLeft
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Surface
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
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
import com.example.spendly.ui.components.SpendlyCard
import com.example.spendly.ui.components.DockClearance
import com.example.spendly.ui.theme.CalendarToday
import com.example.spendly.ui.theme.ChevronRight
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.getValue
import com.example.spendly.ui.components.TransactionRows
import com.example.spendly.ui.components.animateAmountAsState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.spendly.ui.components.MonthPickerMenu
import java.time.YearMonth
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import com.example.spendly.ui.components.LaunchGate
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.input.pointer.pointerInput
import com.example.spendly.ui.theme.Motion
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import java.time.LocalDate
import kotlin.math.roundToInt
import com.example.spendly.ui.components.rememberAnimatedValues
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.components.TintedTile
import com.example.spendly.ui.components.rememberSquish
import com.example.spendly.ui.components.squish
import com.example.spendly.ui.theme.tint
import com.example.spendly.ui.theme.TileTint
import com.example.spendly.ui.theme.TintMoney
import com.example.spendly.ui.theme.TintList
import com.example.spendly.ui.theme.TintTime
import com.example.spendly.ui.theme.TintTimeAlt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTransactionClick: (Long) -> Unit,
    onReviewClick: () -> Unit,
    onViewAllClick: () -> Unit = {},
    onCategoriesClick: () -> Unit = onViewAllClick,
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    monthlyTotal: Double = SampleData.monthlyTotal,
    monthlyBudget: Double = 0.0,
    transactionCount: Int = SampleData.transactions.size,
    hasAnyExpenses: Boolean = true,
    recentTransactions: List<Transaction> = SampleData.transactions.take(5),
    reviewCount: Int = SampleData.reviewCount,
    weeklySpending: List<Double> = SampleData.weeklySpending,
    today: LocalDate = LocalDate.now(),
    categoryTotals: Map<Category, Double> = SampleData.categoryTotals,
    defaultCurrency: String = "INR",
    selectedMonth: YearMonth = YearMonth.now(),
    onMonthSelected: (YearMonth) -> Unit = {},
) {
    val colors = SpendlyTheme.colors
    val monthLabel = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val dateRangeLabel = "${selectedMonth.format(DateTimeFormatter.ofPattern("MMM"))} 1 – ${selectedMonth.lengthOfMonth()}"
    val currencySymbol = currencySymbol(defaultCurrency)
    val weekDayLabels = (6 downTo 0).map { today.minusDays(it.toLong()).format(DateTimeFormatter.ofPattern("EEE")) }
    val monthName = selectedMonth.format(DateTimeFormatter.ofPattern("MMMM"))
    // The four tiles fade in and lift into place, one after another, once per app start
    val rise = rememberSoftRise(tiles = 4)

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
            // Header: the month between its arrows, and Settings
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Arrows step a month at a time, as on Calendar; the month's name opens the
                    // grid to jump further. Home doesn't go past the current month
                    var monthPickerOpen by remember { mutableStateOf(false) }
                    val atCurrentMonth = !selectedMonth.isBefore(YearMonth.from(today))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MonthStepButton(
                            icon = Icons.Default.ChevronLeft,
                            label = "Previous month",
                            enabled = true,
                            onClick = { onMonthSelected(selectedMonth.minusMonths(1)) }
                        )
                        Box {
                            AnimatedContent(
                                targetState = selectedMonth,
                                transitionSpec = { fadeIn(Motion.fadeInAfter()) togetherWith fadeOut(Motion.fadeOutFirst()) },
                                label = "homeMonthTitle",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable(onClickLabel = "Choose month") { monthPickerOpen = true }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
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
                            MonthPickerMenu(
                                expanded = monthPickerOpen,
                                selected = selectedMonth,
                                onDismiss = { monthPickerOpen = false },
                                onSelect = {
                                    onMonthSelected(it)
                                    monthPickerOpen = false
                                }
                            )
                        }
                        MonthStepButton(
                            icon = Icons.Default.ChevronRight,
                            label = "Next month",
                            enabled = !atCurrentMonth,
                            onClick = { onMonthSelected(selectedMonth.plusMonths(1)) }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.container)
                            .clickable(onClick = onSettingsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Settings",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // First run: nothing recorded yet, so say how expenses get in
            if (!hasAnyExpenses) {
                item {
                    SpendlyCard(shape = RoundedCornerShape(24.dp)) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Start tracking",
                                fontFamily = SpaceGroteskFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "Tap + to add an expense. Spendly can also add them for you from your bank's SMS alerts, read only on this phone.",
                                fontSize = 14.sp,
                                lineHeight = 20.sp,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Set up bank SMS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = colors.onPrimary,
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onSettingsClick)
                                    .background(colors.primary)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            }

            // SMS expenses waiting for a check: one tap to the list where each confirms in place
            if (reviewCount > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .clickable(onClick = onReviewClick)
                            .background(colors.container)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(Modifier.size(8.dp).background(colors.tertiary, CircleShape))
                        Text(
                            text = if (reviewCount == 1) "1 SMS expense to check" else "$reviewCount SMS expenses to check",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Total tile: the month's spend, how many expenses, and the daily average
            item {
                val shownTotal by animateAmountAsState(monthlyTotal)
                // A finished month averages over all its days; the current one over the days so far
                val days = if (selectedMonth == YearMonth.from(today)) today.dayOfMonth else selectedMonth.lengthOfMonth()
                val dailyAverage = monthlyTotal / days.coerceAtLeast(1)
                val (whole, cents) = String.format("%,.2f", shownTotal).split(".").let { it[0] to it.getOrElse(1) { "00" } }
                val squish = rememberSquish()
                TintedTile(
                    tint = colors.tint(TintMoney),
                    modifier = Modifier.softRise(rise[0]).squish(squish),
                    shape = RoundedCornerShape(squish.cornerRadius())
                ) {
                    val tile = SpendlyTheme.colors
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Spent in $monthName",
                            fontSize = 13.sp,
                            color = tile.textSecondary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = buildAnnotatedString {
                                append("$currencySymbol$whole")
                                withStyle(SpanStyle(fontSize = 22.sp, color = tile.textSecondary)) { append(".$cents") }
                            },
                            fontFamily = SpaceGroteskFontFamily,
                            fontSize = 40.sp,
                            lineHeight = 44.sp,
                            fontWeight = FontWeight.Bold,
                            color = tile.textPrimary,
                            letterSpacing = (-0.8).sp,
                            style = androidx.compose.ui.text.TextStyle(fontFeatureSettings = "tnum")
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$transactionCount ${if (transactionCount == 1) "expense" else "expenses"} · " +
                                "$currencySymbol${String.format("%,.0f", dailyAverage)} a day · $dateRangeLabel",
                            fontSize = 13.sp,
                            color = tile.textSecondary
                        )
                        // Against the monthly budget, when one is set in Settings
                        if (monthlyBudget > 0) {
                            val left = monthlyBudget - monthlyTotal
                            val used = rememberAnimatedValues(listOf((monthlyTotal / monthlyBudget).toFloat().coerceIn(0f, 1f)))
                            val over = left < 0
                            Spacer(Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(tile.containerHigh)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .drawBehind {
                                            drawRect(if (over) BudgetOver else tile.primary, size = Size(size.width * used[0].value, size.height))
                                        }
                                        .fillMaxWidth()
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "$currencySymbol${String.format("%,.0f", kotlin.math.abs(left))} " +
                                    (if (over) "over" else "left of") + " your $currencySymbol${String.format("%,.0f", monthlyBudget)} budget",
                                fontSize = 13.sp,
                                color = if (over) BudgetOver else tile.textSecondary
                            )
                        }
                    }
                }
            }

            // Top category ring beside this week's bars
            item {
                // The week tile is amber unless Food & Dining, also amber, leads right beside it
                val topHue = categoryTotals.maxByOrNull { it.value }?.takeIf { it.value > 0 }?.key?.let { stitchCategoryColors[it] }
                val weekHue = if (topHue == TintTime) TintTimeAlt else TintTime
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .softRise(rise[1]),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TopCategoryTile(
                        categoryTotals = categoryTotals,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                    WeekTile(
                        weeklySpending = weeklySpending,
                        weekDayLabels = weekDayLabels,
                        currencySymbol = currencySymbol,
                        tint = colors.tint(weekHue),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // Spending by Category chart
            item {
                SpendlyCard(modifier = Modifier.softRise(rise[2]), shape = RoundedCornerShape(24.dp)) {
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

                        Spacer(Modifier.height(12.dp))

                        // The three biggest; the full breakdown is one tap away
                        val total = categoryTotals.values.sum()
                        val leading = categoryTotals.entries.filter { it.value > 0 }.sortedByDescending { it.value }.take(3)
                        if (leading.isEmpty()) {
                            Text(text = "No expenses in $monthName", fontSize = 14.sp, color = colors.textSecondary)
                        }
                        val shares = rememberAnimatedValues(List(3) { leading.getOrNull(it)?.let { e -> (e.value / total).toFloat() } ?: 0f })
                        leading.forEachIndexed { index, (category, amount) ->
                            val hue = stitchCategoryColors[category] ?: colors.textMuted
                            Column(modifier = Modifier.padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = category.displayName,
                                        fontSize = 14.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "$currencySymbol${String.format("%,.0f", amount)}",
                                        fontFamily = SpaceGroteskFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${(amount / total * 100).roundToInt()}%",
                                        fontSize = 12.sp,
                                        color = colors.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                        modifier = Modifier.width(40.dp)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(colors.containerHigh)
                                        .drawBehind { drawRect(hue, size = Size(size.width * shares[index].value, size.height)) }
                                )
                            }
                        }
                    }
                }
            }

            // Recent Expenses List
            item {
                TintedTile(tint = colors.tint(TintList), modifier = Modifier.softRise(rise[3]), shape = RoundedCornerShape(24.dp)) {
                    val tile = SpendlyTheme.colors
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
                                color = tile.textPrimary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable(onClick = onViewAllClick)
                            ) {
                                Text(
                                    text = "View all",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = tile.textSecondary
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = tile.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (recentTransactions.isEmpty()) {
                            Text(
                                text = "No expenses in $monthName",
                                fontSize = 14.sp,
                                color = tile.textSecondary,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        TransactionRows(
                            transactions = recentTransactions,
                            onTransactionClick = onTransactionClick,
                            currencySymbol = currencySymbol,
                            resetKey = selectedMonth
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

/** The month's biggest category as a ring closing to its share. */
@Composable
private fun TopCategoryTile(categoryTotals: Map<Category, Double>, modifier: Modifier) {
    val colors = SpendlyTheme.colors
    val total = categoryTotals.values.sum()
    val top = categoryTotals.maxByOrNull { it.value }?.takeIf { it.value > 0 }
    val share = if (top != null && total > 0) (top.value / total).toFloat() else 0f
    val shown = rememberAnimatedValues(listOf(share))
    val ringColor = top?.key?.let { stitchCategoryColors[it] } ?: colors.textMuted
    val squish = rememberSquish()

    TintedTile(tint = colors.tint(ringColor), modifier = modifier.squish(squish), shape = RoundedCornerShape(squish.cornerRadius())) {
        val tile = SpendlyTheme.colors
        val track = tile.containerHigh
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                Canvas(modifier = Modifier.size(96.dp)) {
                    val stroke = 11.dp.toPx()
                    val topLeft = Offset(stroke / 2, stroke / 2)
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    drawArc(track, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
                    val sweep = 360f * shown[0].value
                    if (sweep > 0.5f) {
                        drawArc(ringColor, -90f, sweep, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                    }
                }
                Text(
                    text = "${(share * 100).roundToInt()}%",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = tile.textPrimary
                )
            }
            Text(
                text = top?.let { "${it.key.shortName} leads" } ?: "Nothing spent yet",
                fontSize = 12.sp,
                color = tile.textSecondary,
                maxLines = 1
            )
        }
    }
}

/**
 * Last 7 days as compact bars. Tap a day, or drag across the bars, to see what it cost in the
 * header, with the week's total underneath; it starts on today.
 */
@Composable
private fun WeekTile(
    weeklySpending: List<Double>,
    weekDayLabels: List<String>,
    currencySymbol: String,
    tint: TileTint,
    modifier: Modifier
) {
    val maxVal = (weeklySpending.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
    // Bars grow from the baseline on first show, then move to new heights
    val shown = rememberAnimatedValues(weeklySpending.map { (it / maxVal).toFloat().coerceIn(0f, 1f) })
    val squish = rememberSquish()
    val lastIndex = weeklySpending.lastIndex
    var selected by remember { mutableIntStateOf(lastIndex) }
    val day = selected.coerceIn(0, lastIndex)
    val dayLabel = when (lastIndex - day) {
        0 -> "Today"
        1 -> "Yesterday"
        else -> LocalDate.now().minusDays((lastIndex - day).toLong()).format(DateTimeFormatter.ofPattern("EEEE"))
    }

    TintedTile(tint = tint, modifier = modifier.squish(squish), shape = RoundedCornerShape(squish.cornerRadius())) {
        val tile = SpendlyTheme.colors
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = dayLabel, fontSize = 13.sp, color = tile.textSecondary, maxLines = 1)
                Text(
                    text = "$currencySymbol${String.format("%,.0f", weeklySpending.getOrElse(day) { 0.0 })}",
                    fontFamily = SpaceGroteskFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = tile.textPrimary
                )
            }
            Text(
                text = "$currencySymbol${String.format("%,.0f", weeklySpending.sum())} this week",
                fontSize = 11.sp,
                color = tile.textMuted
            )
            // The bars and their letters pick a day on a tap or a sideways drag; a vertical drag
            // still scrolls Home
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .pointerInput(lastIndex) {
                        detectTapGestures(onTap = { selected = dayAt(it.x, size.width, lastIndex) })
                    }
                    .pointerInput(lastIndex) {
                        detectHorizontalDragGestures(onDragStart = { selected = dayAt(it.x, size.width, lastIndex) }) { change, _ ->
                            selected = dayAt(change.position.x, size.width, lastIndex)
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(top = 10.dp, bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    shown.forEachIndexed { index, fraction ->
                        val barColor by animateColorAsState(
                            if (index == day) tile.primary else tile.containerHigh,
                            animationSpec = Motion.move(Motion.Short),
                            label = "weekBar"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(fraction.value.coerceAtLeast(0.04f))
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    weekDayLabels.forEachIndexed { index, label ->
                        Text(
                            text = label.take(1),
                            fontSize = 11.sp,
                            color = if (index == day) tile.textPrimary else tile.textMuted,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/** Which of the week's days sits under [x] in a row [width] wide. */
private fun dayAt(x: Float, width: Int, lastIndex: Int): Int =
    (x / width * (lastIndex + 1)).toInt().coerceIn(0, lastIndex)

// Rising in is a hello when the app starts, not something to sit through on every tab switch
private var roseThisLaunch = false

/** Each tile's progress rising in: 0 hidden and a little low, 1 in place. Starts once the splash has gone. */
@Composable
private fun rememberSoftRise(tiles: Int): List<Animatable<Float, AnimationVector1D>> {
    val rise = remember { List(tiles) { Animatable(if (roseThisLaunch) 1f else 0f) } }
    LaunchedEffect(Unit) {
        if (roseThisLaunch) return@LaunchedEffect
        LaunchGate.await()
        roseThisLaunch = true
        rise.forEachIndexed { index, tile ->
            launch { tile.animateTo(1f, Motion.enter(Motion.Medium + 50, delayMillis = index * 50)) }
        }
    }
    return rise
}

private fun Modifier.softRise(progress: Animatable<Float, AnimationVector1D>): Modifier = graphicsLayer {
    alpha = progress.value
    translationY = 12.dp.toPx() * (1f - progress.value)
}

/** A round arrow beside Home's month title, matching Calendar's; dimmed when there's nowhere to go. */
@Composable
private fun MonthStepButton(icon: ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    val colors = SpendlyTheme.colors
    Surface(
        modifier = Modifier.size(32.dp),
        shape = CircleShape,
        color = colors.container,
        border = BorderStroke(1.dp, colors.border),
        enabled = enabled,
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (enabled) colors.textSecondary else colors.textMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** The budget bar and its caption once the month's spend has passed the budget. */
private val BudgetOver = Color(0xFFE2605C)
