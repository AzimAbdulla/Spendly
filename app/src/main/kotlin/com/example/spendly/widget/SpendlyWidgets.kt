package com.example.spendly.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Build
import android.text.format.DateFormat
import android.util.TypedValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.ColumnScope
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.spendly.R
import com.example.spendly.data.model.Category
import com.example.spendly.ui.components.stitchCategoryColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

// =========================================================================
// Home screen widgets, sized 6x2 by default so One UI can stack them in one spot.
// Each reads fresh data on update; SpendlyApplication updates them when expenses change.
// =========================================================================

/** Redraws every Spendly widget, all at once. */
suspend fun updateSpendlyWidgets(context: Context) = coroutineScope {
    widgetTick.value++
    listOf(OverviewWidget(), WeekWidget(), RecentWidget(), TodayWidget(), QuickAddWidget())
        .forEach { widget -> launch { widget.updateAll(context) } }
}

// Glance keeps a widget's session alive for a while after an update, and an update to a live
// session only recomposes it without running provideGlance again. So the content reloads its
// data itself whenever this ticks; otherwise a second change inside that window wouldn't show.
private val widgetTick = MutableStateFlow(0)

private suspend fun GlanceAppWidget.provide(context: Context, content: @Composable (WidgetData, WidgetStyle) -> Unit) {
    val first = loadWidget(context)
    provideContent {
        val tick by widgetTick.collectAsState()
        val loaded by produceState(first, tick) { value = loadWidget(context) }
        content(loaded.first, loaded.second)
    }
}

class OverviewWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provide(context) { data, style -> OverviewContent(data, style) }
    }
}

class WeekWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provide(context) { data, style -> WeekContent(data, style) }
    }
}

class RecentWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provide(context) { data, style -> RecentContent(data, style) }
    }
}

class TodayWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provide(context) { data, style -> TodayContent(data, style) }
    }
}

class QuickAddWidget : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provide(context) { data, style -> QuickAddContent(data, style) }
    }
}

class OverviewWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = OverviewWidget() }
class WeekWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = WeekWidget() }
class RecentWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = RecentWidget() }
class TodayWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = TodayWidget() }
class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() { override val glanceAppWidget = QuickAddWidget() }

private val HorizontalPadding = 18.dp

/**
 * How much taller than the compact layout (150dp) the launcher made this widget: 0 at 150dp, 1 by
 * 200dp. One UI's 6x2 cell is around 190dp, so type, the ring, bars and buttons grow with it
 * instead of leaving empty bands.
 */
@Composable
private fun roominess(): Float = ((LocalSize.current.height - 150.dp) / 50.dp).coerceIn(0f, 1f)

@Composable
private fun verticalPadding(): Dp = lerp(14.dp, 18.dp, roominess())

/** Width of [text] in the system font, which the launcher draws widget text with (One UI's own font on a Galaxy). */
private fun textWidth(context: Context, text: String, size: TextUnit, bold: Boolean = false): Dp {
    val metrics = context.resources.displayMetrics
    val paint = Paint().apply {
        textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size.value, metrics)
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
    }
    return (paint.measureText(text) / metrics.density).dp
}

/** [preferred], shrunk when a line measuring [measured] at that size would overflow [width]. */
private fun fitting(preferred: TextUnit, measured: Dp, width: Dp): TextUnit =
    (preferred.value * minOf(1f, width / measured)).sp

/** The widget's own tile; corners follow the launcher's widget radius on Android 12+ (One UI's included). */
@Composable
private fun WidgetTile(
    fill: Color,
    onClick: Action?,
    // Glanceable widgets keep their content together when the launcher gives them extra height
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable ColumnScope.() -> Unit
) {
    val corners = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        GlanceModifier.cornerRadius(android.R.dimen.system_app_widget_background_radius)
    } else {
        GlanceModifier.cornerRadius(26.dp)
    }
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .then(corners)
            .background(fill)
            .padding(horizontal = HorizontalPadding, vertical = verticalPadding())
            .then(if (onClick != null) GlanceModifier.clickable(onClick) else GlanceModifier),
        verticalAlignment = verticalAlignment,
        content = content
    )
}

@Composable
private fun Label(text: String, color: Color, size: TextUnit = 13.sp, weight: FontWeight = FontWeight.Normal, modifier: GlanceModifier = GlanceModifier) {
    Text(
        text = text,
        modifier = modifier,
        style = TextStyle(color = ColorProvider(color), fontSize = size, fontWeight = weight),
        maxLines = 1
    )
}

@Composable
private fun Dot(color: Color, size: Dp = 7.dp) {
    Box(modifier = GlanceModifier.size(size).cornerRadius(size / 2).background(color)) {}
}

/** The big figure with smaller decimals, like the app's total tile. */
@Composable
private fun BigAmount(whole: String, decimals: String, color: Color, secondary: Color, size: TextUnit) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(whole, style = TextStyle(color = ColorProvider(color), fontSize = size, fontWeight = FontWeight.Bold), maxLines = 1)
        if (decimals.isNotEmpty()) {
            Text(
                decimals,
                modifier = GlanceModifier.padding(bottom = (size.value / 10).dp),
                style = TextStyle(color = ColorProvider(secondary), fontSize = size * 0.57f, fontWeight = FontWeight.Medium),
                maxLines = 1
            )
        }
    }
}

// ---------- Overview: month total, top-category ring, category split ----------

@Composable
private fun OverviewContent(data: WidgetData, style: WidgetStyle) {
    val context = LocalContext.current
    val size = LocalSize.current
    val room = roominess()
    val tint = style.money
    val top = data.split.firstOrNull()
    val showRing = size.width >= 280.dp
    val showLegend = size.height >= 150.dp && data.split.isNotEmpty()
    val ring = lerp(58.dp, 84.dp, room)
    val label = lerp(13.sp, 14.5.sp, room)
    val small = lerp(12.sp, 13.5.sp, room)

    WidgetTile(fill = style.tileFill(tint), onClick = openScreen(context, WidgetLaunch.HOME)) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(modifier = GlanceModifier.defaultWeight()) {
                Label("${data.monthName} · Spendly", style.label(tint), size = label, weight = FontWeight.Medium)
                val (whole, decimals) = style.bigAmount(data.monthTotal)
                val amountWidth = size.width - HorizontalPadding * 2 - if (showRing && top != null) ring + 12.dp else 0.dp
                val amountSize = lerp(30.sp, 42.sp, room)
                val measured = textWidth(context, whole, amountSize, bold = true) + textWidth(context, decimals, amountSize * 0.57f)
                BigAmount(whole, decimals, style.figure(tint), style.sub(tint), size = fitting(amountSize, measured, amountWidth))
                Label(
                    if (data.monthCount == 0) "No expenses yet"
                    else "${data.monthCount} ${if (data.monthCount == 1) "expense" else "expenses"} · ${style.amount(data.dailyAverage)} a day",
                    style.sub(tint),
                    size = label
                )
            }
            if (showRing && top != null) {
                val share = (top.second / data.monthTotal).toFloat()
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(contentAlignment = Alignment.Center, modifier = GlanceModifier.size(ring)) {
                        Image(
                            provider = ImageProvider(ringBitmap(context, ring, share, style.cat(top.first), tint.track)),
                            contentDescription = null,
                            modifier = GlanceModifier.size(ring)
                        )
                        Label("${(share * 100).roundToInt()}%", style.text, size = lerp(14.sp, 19.sp, room), weight = FontWeight.Bold)
                    }
                    Label(top.first.shortName, style.sub(tint), size = small)
                }
            }
        }
        // The split keeps to the bottom edge, however tall the launcher makes the widget
        Spacer(GlanceModifier.height(14.dp))
        Spacer(GlanceModifier.defaultWeight())
        SplitBar(data, style, width = size.width - HorizontalPadding * 2, track = tint.track, thickness = lerp(8.dp, 10.dp, room))
        if (showLegend) {
            Spacer(GlanceModifier.height(lerp(7.dp, 9.dp, room)))
            val dot = lerp(7.dp, 8.dp, room)
            val entries = data.split.take(4).map { (category, amount) ->
                category to " ${category.shortName} ${style.compact(amount)}"
            }
            // As many entries as fit on one line
            var left = size.width - HorizontalPadding * 2
            val shown = entries.takeWhile { (_, text) ->
                left -= dot + 10.dp + textWidth(context, text, small)
                left >= 0.dp
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                shown.forEach { (category, text) ->
                    Dot(style.cat(category), size = dot)
                    Label(text, style.sub(tint), size = small, modifier = GlanceModifier.padding(end = 10.dp))
                }
            }
        }
    }
}

// Glance rows and columns hold at most 10 children, so gaps live inside each item (as padding
// on a wrapper) instead of as separate spacers

/** Category shares as one segmented bar; segments are sized boxes since widgets can't draw freely. */
@Composable
private fun SplitBar(data: WidgetData, style: WidgetStyle, width: Dp, track: Color, thickness: Dp) {
    val gap = 3.dp
    Row(modifier = GlanceModifier.fillMaxWidth().height(thickness)) {
        if (data.monthTotal <= 0.0) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(thickness).cornerRadius(thickness / 2).background(track)) {}
        } else {
            val available = width - gap * (data.split.size - 1)
            data.split.forEachIndexed { index, (category, amount) ->
                val segment = (available * (amount / data.monthTotal).toFloat()).coerceAtLeast(3.dp)
                Box(modifier = GlanceModifier.padding(end = if (index < data.split.lastIndex) gap else 0.dp)) {
                    Box(
                        modifier = GlanceModifier
                            .width(segment)
                            .height(thickness)
                            .cornerRadius(thickness / 2)
                            .background(style.cat(category))
                    ) {}
                }
            }
        }
    }
}

/** The ring is drawn into a bitmap: widgets have no canvas of their own. */
private fun ringBitmap(context: Context, size: Dp, share: Float, ring: Color, track: Color): Bitmap {
    val density = context.resources.displayMetrics.density
    val px = (size.value * density).roundToInt()
    val stroke = size.value * 0.12f * density
    val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = stroke
        strokeCap = Paint.Cap.ROUND
    }
    val bounds = RectF(stroke / 2, stroke / 2, px - stroke / 2, px - stroke / 2)
    paint.color = track.toArgb()
    canvas.drawArc(bounds, 0f, 360f, false, paint)
    if (share > 0f) {
        paint.color = ring.toArgb()
        canvas.drawArc(bounds, -90f, 360f * share.coerceIn(0f, 1f), false, paint)
    }
    return bitmap
}

// ---------- This week: total against last week, seven bars with their amounts ----------

@Composable
private fun WeekContent(data: WidgetData, style: WidgetStyle) {
    val context = LocalContext.current
    val size = LocalSize.current
    val room = roominess()
    val tint = style.time
    val maxDay = (data.week.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
    val peak = data.week.indexOf(data.week.maxOrNull()).takeIf { (data.week.maxOrNull() ?: 0.0) > 0 }
    // Bars take the height left under the header, amounts and day letters
    val barArea = (size.height - verticalPadding() * 2 - 96.dp).coerceIn(16.dp, 120.dp)
    val total = style.amount(data.weekTotal)
    val totalSize = lerp(26.sp, 34.sp, room)
    val change = if (data.lastWeekTotal > 0) ((data.weekTotal - data.lastWeekTotal) / data.lastWeekTotal * 100).roundToInt() else null

    WidgetTile(fill = style.tileFill(tint), onClick = openScreen(context, WidgetLaunch.WEEK), verticalAlignment = Alignment.Top) {
        Label("This week", style.label(tint), size = lerp(13.sp, 14.5.sp, room))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                total,
                style = TextStyle(
                    color = ColorProvider(style.figure(tint)),
                    fontSize = fitting(totalSize, textWidth(context, total, totalSize, bold = true), size.width / 2),
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            if (change != null) {
                Box(modifier = GlanceModifier.padding(start = 10.dp)) {
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(style.neutralBar)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Label(
                            "${if (change > 0) "+" else "−"}${kotlin.math.abs(change)}% vs last week",
                            if (change > 0) style.highlight else style.positive,
                            size = 12.sp,
                            weight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            data.week.forEachIndexed { index, value ->
                val color = when (index) {
                    data.week.lastIndex -> style.positive
                    peak -> style.highlight
                    else -> style.neutralBar
                }
                val figure = when (index) {
                    data.week.lastIndex, peak -> color
                    else -> style.muted
                }
                Column(
                    modifier = GlanceModifier.defaultWeight().padding(horizontal = 3.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Label(if (value > 0) style.compact(value) else "", figure, size = 10.sp)
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height((barArea * (value / maxDay).toFloat()).coerceAtLeast(4.dp))
                            .cornerRadius(6.dp)
                            .background(color)
                    ) {}
                    Label(
                        data.weekLabels[index],
                        if (index == data.week.lastIndex) style.text else style.muted,
                        size = lerp(11.sp, 12.sp, room),
                        modifier = GlanceModifier.padding(top = 3.dp)
                    )
                }
            }
        }
    }
}

// ---------- Recent: latest expenses, what's waiting for review, and who SMS expenses went to ----------

@Composable
private fun RecentContent(data: WidgetData, style: WidgetStyle) {
    val context = LocalContext.current
    val size = LocalSize.current
    val room = roominess()
    val tint = style.list
    val clock = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a")
    val today = LocalDate.now()
    val text = lerp(14.sp, 15.5.sp, room)
    // Each row has its amount over its time, about 40dp; rows share what's left under the header and footer
    val showFooter = size.height >= 150.dp
    val rows = ((size.height - verticalPadding() * 2 - 30.dp - if (showFooter) 28.dp else 0.dp) / 40.dp).toInt().coerceIn(1, 3)

    WidgetTile(fill = style.tileFill(tint), onClick = openScreen(context, WidgetLaunch.ACTIVITY)) {
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Label("Recent", style.figure(tint), size = lerp(16.sp, 17.5.sp, room), weight = FontWeight.Medium, modifier = GlanceModifier.defaultWeight())
            if (data.reviewCount > 0) {
                Row(
                    modifier = GlanceModifier
                        .cornerRadius(12.dp)
                        .background(style.neutralBar)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .clickable(openScreen(context, WidgetLaunch.REVIEW)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Dot(style.review, size = 6.dp)
                    Label(" ${data.reviewCount} to review", style.text, size = 12.sp)
                }
            }
        }
        if (data.recent.isEmpty()) {
            Label("No expenses yet", style.muted, size = text, modifier = GlanceModifier.padding(top = 8.dp))
        }
        data.recent.take(rows).forEach { tx ->
            // The time on today's expenses; otherwise the day
            val whenLabel = when {
                tx.date == today -> tx.time?.format(clock) ?: "Today"
                tx.date == today.minusDays(1) -> "Yesterday"
                else -> tx.date.format(DateTimeFormatter.ofPattern("d MMM"))
            }
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .defaultWeight()
                    .clickable(openExpense(context, tx.id)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Dot(style.cat(tx.category), size = 8.dp)
                Label("  " + tx.item.ifBlank { tx.category.displayName }, style.text, size = text, modifier = GlanceModifier.defaultWeight())
                Column(horizontalAlignment = Alignment.End) {
                    Label("−" + style.amount(tx.amount, decimals = true), style.text, size = text, weight = FontWeight.Medium)
                    Label(whenLabel, style.muted, size = 12.sp)
                }
            }
        }
        if (showFooter) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(1.dp).background(style.outline)) {}
            Row(modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Label(
                    if (data.payees.isEmpty()) "" else "${data.payees.joinToString(", ")} logged",
                    style.muted,
                    size = 12.sp,
                    modifier = GlanceModifier.defaultWeight()
                )
                Label("View all ›", style.positive, size = 13.sp, weight = FontWeight.Medium)
            }
        }
    }
}

// ---------- Today: today's spend against the daily average, and its latest expenses ----------

@Composable
private fun TodayContent(data: WidgetData, style: WidgetStyle) {
    val context = LocalContext.current
    val size = LocalSize.current
    val room = roominess()
    val clock = DateTimeFormatter.ofPattern(if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a")
    // Pace against the daily average: green with room to spare, amber from 80%, red once over
    val pace = if (data.dailyAverage > 0) data.today / data.dailyAverage else 0.0
    val tint = when {
        pace > 1.0 -> style.over
        pace >= 0.8 -> style.close
        else -> style.today
    }
    val width = size.width - HorizontalPadding * 2
    // Rows of today's expenses as fit under the figure, about 20dp each
    val rows = ((size.height - verticalPadding() * 2 - 104.dp) / 20.dp).toInt().coerceIn(0, 3)

    WidgetTile(fill = style.tileFill(tint), onClick = openScreen(context, WidgetLaunch.ACTIVITY), verticalAlignment = Alignment.Top) {
        Label("Today", style.label(tint), size = lerp(13.sp, 14.5.sp, room))
        Label(style.amount(data.today), style.figure(tint), size = lerp(24.sp, 30.sp, room), weight = FontWeight.Bold)
        if (data.monthCount > 0) {
            Box(
                modifier = GlanceModifier.padding(vertical = 5.dp).width(width).height(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Box(GlanceModifier.fillMaxWidth().height(6.dp).cornerRadius(3.dp).background(style.neutralBar)) {}
                Box(
                    GlanceModifier
                        .width(width * pace.coerceIn(0.04, 1.0).toFloat())
                        .height(6.dp)
                        .cornerRadius(3.dp)
                        .background(style.accent(tint))
                ) {}
            }
            Label(
                if (pace > 1.0) "${style.amount(data.today - data.dailyAverage)} over your ${style.amount(data.dailyAverage)} average"
                else "${style.amount(data.dailyAverage - data.today)} left of your ${style.amount(data.dailyAverage)} average",
                style.accent(tint),
                size = 12.sp
            )
        }
        if (rows > 0 && data.todayList.isNotEmpty()) {
            Spacer(GlanceModifier.defaultWeight())
            Column(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(12.dp)
                    .background(style.neutralBar.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                data.todayList.take(rows).forEach { tx ->
                    Row(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 2.dp)) {
                        Label(
                            listOfNotNull(tx.time?.format(clock), tx.item.ifBlank { tx.category.shortName }).joinToString(" "),
                            style.muted,
                            size = 12.sp,
                            modifier = GlanceModifier.defaultWeight()
                        )
                        Label(style.compact(tx.amount), style.text, size = 12.sp)
                    }
                }
            }
        }
    }
}

// ---------- Quick add: category shortcuts and a plain add ----------

private val QuickCategories = listOf(
    Category.FOOD to R.drawable.ic_widget_dining,
    Category.GROCERIES to R.drawable.ic_widget_cart,
    Category.TRANSPORT to R.drawable.ic_widget_car,
    Category.SHOPPING to R.drawable.ic_widget_bag
)

@Composable
private fun QuickAddContent(data: WidgetData, style: WidgetStyle) {
    val context = LocalContext.current
    val size = LocalSize.current
    val room = roominess()
    // As many buttons as fit at the 48dp minimum touch size with 8dp between, as Google's canonical
    // toolbar widget does; narrow widgets drop shortcuts and always keep the plain add
    val width = size.width - HorizontalPadding * 2
    val buttons = ((width + 8.dp) / 56.dp).toInt().coerceIn(2, QuickCategories.size + 1)
    val shortcuts = QuickCategories.take(buttons - 1)
    // Buttons grow with the height, as far as an equal share of the width allows
    val diameter = minOf(lerp(48.dp, 60.dp, room), width / buttons - 8.dp)

    WidgetTile(fill = style.fill(style.quickFill), onClick = null, verticalAlignment = Alignment.Top) {
        if (size.height >= 170.dp) {
            // With height to spare, today's spend becomes the big figure the other widgets lead with
            val small = lerp(13.sp, 14.5.sp, room)
            Label("Add expense", style.quickSecondary, size = small, weight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    style.amount(data.today),
                    style = TextStyle(color = ColorProvider(style.text), fontSize = 30.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Label(" today", style.quickSecondary, size = small, modifier = GlanceModifier.padding(bottom = 4.dp))
            }
        } else {
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Label("Add expense", style.text, size = 15.sp, weight = FontWeight.Medium, modifier = GlanceModifier.defaultWeight())
                Label("Today ${style.amount(data.today)}", style.quickSecondary)
            }
        }
        // Buttons sit at the bottom, within easy reach of a thumb
        Spacer(GlanceModifier.defaultWeight())
        Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            shortcuts.forEach { (category, icon) ->
                val tint = style.category(category)
                QuickButton(
                    label = category.shortName,
                    icon = icon,
                    fill = tint.track,
                    iconColor = tint.accent,
                    labelColor = style.quickSecondary,
                    diameter = diameter,
                    onClick = openScreen(context, WidgetLaunch.ADD, category)
                )
                Spacer(GlanceModifier.defaultWeight())
            }
            QuickButton(
                label = "Add",
                icon = R.drawable.ic_widget_add,
                fill = style.plusFill,
                iconColor = style.onPlus,
                labelColor = style.quickSecondary,
                diameter = diameter + 4.dp,
                onClick = openScreen(context, WidgetLaunch.ADD)
            )
        }
    }
}

@Composable
private fun QuickButton(label: String, icon: Int, fill: Color, iconColor: Color, labelColor: Color, diameter: Dp, onClick: Action) {
    Column(
        modifier = GlanceModifier.clickable(onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = GlanceModifier.size(diameter).cornerRadius(diameter / 2).background(fill),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = label,
                modifier = GlanceModifier.size(diameter * 0.46f),
                colorFilter = ColorFilter.tint(ColorProvider(iconColor))
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        Label(label, labelColor, size = 12.sp)
    }
}
