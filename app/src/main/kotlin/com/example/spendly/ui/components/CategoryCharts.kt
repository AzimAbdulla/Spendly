package com.example.spendly.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spendly.data.model.Category
import com.example.spendly.ui.theme.SpaceGroteskFontFamily
import com.example.spendly.ui.theme.SpendlyTheme
import com.example.spendly.ui.theme.StitchCatOther
import kotlin.math.roundToInt

/** How Home's "By category" card draws the month's spending; picked in Settings. */
enum class CategoryChartStyle(val label: String) {
    DONUT("Donut"),
    BAR("Stacked bar"),
    TILES("Tiles"),
    RINGS("Rings"),
    HALF_DONUT("Half donut");

    companion object {
        fun fromName(name: String?): CategoryChartStyle = entries.firstOrNull { it.name == name } ?: DONUT
    }
}

@Composable
fun CategoryChart(
    style: CategoryChartStyle,
    categoryTotals: Map<Category, Double>,
    currencySymbol: String,
    modifier: Modifier = Modifier
) {
    when (style) {
        CategoryChartStyle.DONUT -> DonutChart(categoryTotals = categoryTotals, currencySymbol = currencySymbol, modifier = modifier)
        CategoryChartStyle.BAR -> CategoryStackedBar(categoryTotals, currencySymbol, modifier)
        CategoryChartStyle.TILES -> CategoryTiles(categoryTotals, currencySymbol, modifier)
        CategoryChartStyle.RINGS -> CategoryRings(categoryTotals, modifier)
        CategoryChartStyle.HALF_DONUT -> CategoryHalfDonut(categoryTotals, currencySymbol, modifier)
    }
}

// Every style animates from the same per-category values, kept in category order so pieces
// can grow and shrink without trading places (see rememberAnimatedValues)
private fun targetsOf(categoryTotals: Map<Category, Double>) =
    Category.entries.map { (categoryTotals[it] ?: 0.0).toFloat() }

private fun largestFirst(categoryTotals: Map<Category, Double>) =
    categoryTotals.filter { it.value > 0 }.entries.sortedByDescending { it.value }.map { it.key to it.value }

private fun colorOf(category: Category): Color = stitchCategoryColors[category] ?: StitchCatOther

private fun money(currencySymbol: String, amount: Double) = "$currencySymbol${String.format("%,.2f", amount)}"

private val Numbers = TextStyle(fontFeatureSettings = "tnum")

/** Category rows under a chart, largest first: dot, name, amount and share. */
@Composable
private fun CategoryLegend(
    entries: List<Pair<Category, Double>>,
    total: Double,
    currencySymbol: String
) {
    val colors = SpendlyTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        entries.take(5).forEach { (category, amount) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(8.dp).background(colorOf(category), CircleShape))
                    Text(text = category.displayName, fontSize = 14.sp, color = colors.textPrimary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = money(currencySymbol, amount),
                        fontFamily = SpaceGroteskFontFamily,
                        style = Numbers,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${if (total > 0) (amount / total * 100).roundToInt() else 0}%",
                        fontSize = 12.sp,
                        color = colors.textMuted
                    )
                }
            }
        }
    }
}

/** One segmented bar across the card, filling left to right on first show. */
@Composable
private fun CategoryStackedBar(categoryTotals: Map<Category, Double>, currencySymbol: String, modifier: Modifier) {
    val colors = SpendlyTheme.colors
    val targets = targetsOf(categoryTotals)
    val shown = rememberAnimatedValues(targets)
    val track = colors.containerHigh

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(CircleShape)
        ) {
            drawRect(track)
            val values = shown.map { it.value }
            // Growing: scale to the target so the bar fills in. Shrinking: scale to what's drawn
            val scale = maxOf(values.sum(), targets.sum())
            if (scale > 0f) {
                val gap = 3.dp.toPx()
                val drawn = values.indices.filter { values[it] > scale * 0.002f }
                val available = size.width - gap * (drawn.size - 1).coerceAtLeast(0)
                var x = 0f
                drawn.forEach { index ->
                    val width = values[index] / scale * available
                    drawRect(colorOf(Category.entries[index]), topLeft = Offset(x, 0f), size = Size(width, size.height))
                    x += width + gap
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        CategoryLegend(largestFirst(categoryTotals), categoryTotals.values.sum(), currencySymbol)
    }
}

/** A treemap: each category gets room in proportion to what was spent on it. */
@Composable
private fun CategoryTiles(categoryTotals: Map<Category, Double>, currencySymbol: String, modifier: Modifier) {
    val colors = SpendlyTheme.colors
    val targets = targetsOf(categoryTotals)
    val shown = rememberAnimatedValues(targets)
    val values = shown.map { it.value }
    val total = categoryTotals.values.sum()
    val order = Category.entries.indices.filter { targets[it] > 0f }.sortedByDescending { targets[it] }
    // Past six tiles, labels stop fitting, so the smallest fold into one
    val shownTiles = if (order.size > 6) order.take(5) else order
    val folded = order.drop(shownTiles.size)

    BoxWithConstraints(modifier = modifier.fillMaxWidth().height(184.dp)) {
        val density = LocalDensity.current
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()
        if (order.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.containerLow),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Nothing spent yet", fontSize = 13.sp, color = colors.textSecondary)
            }
        } else {
            val live = shownTiles.map { values[it] } + listOfNotNull(folded.takeIf { it.isNotEmpty() }?.sumOf { values[it].toDouble() }?.toFloat())
            val goal = shownTiles.map { targets[it] } + listOfNotNull(folded.takeIf { it.isNotEmpty() }?.sumOf { targets[it].toDouble() }?.toFloat())
            // Layout follows the animated values; on first show they all grow together, so tiles fade in instead
            val weights = if (live.sum() > 0f) live else goal
            val rects = treemap(weights, Rect(0f, 0f, width, height), with(density) { 6.dp.toPx() })
            val appear = (live.sum() / goal.sum()).coerceIn(0f, 1f)
            rects.forEachIndexed { k, rect ->
                if (k < shownTiles.size) {
                    val category = Category.entries[shownTiles[k]]
                    val amount = categoryTotals[category] ?: 0.0
                    Tile(
                        title = category.displayName,
                        detail = "${money(currencySymbol, amount)} · ${(amount / total * 100).roundToInt()}%",
                        color = colorOf(category),
                        rect = rect,
                        alpha = appear
                    )
                } else {
                    Tile(
                        title = "+${folded.size} more",
                        detail = money(currencySymbol, folded.sumOf { categoryTotals[Category.entries[it]] ?: 0.0 }),
                        color = colors.textMuted,
                        rect = rect,
                        alpha = appear
                    )
                }
            }
        }
    }
}

@Composable
private fun Tile(title: String, detail: String, color: Color, rect: Rect, alpha: Float) {
    val colors = SpendlyTheme.colors
    val density = LocalDensity.current
    val shape = RoundedCornerShape(16.dp)
    with(density) {
        Column(
            modifier = Modifier
                .offset { IntOffset(rect.left.roundToInt(), rect.top.roundToInt()) }
                .size(rect.width.toDp(), rect.height.toDp())
                .graphicsLayer { this.alpha = alpha }
                .clip(shape)
                .background(color.copy(alpha = 0.22f))
                .border(1.dp, color.copy(alpha = 0.4f), shape)
                .padding(10.dp)
        ) {
            // Labels only where they fit; the smallest tiles stay colour only
            if (rect.width > 64.dp.toPx() && rect.height > 44.dp.toPx()) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = detail,
                    fontFamily = SpaceGroteskFontFamily,
                    style = Numbers,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Splits [area] between [weights] by halving the list and cutting along the longer side. */
private fun treemap(weights: List<Float>, area: Rect, gap: Float): List<Rect> {
    val out = MutableList(weights.size) { Rect.Zero }
    fun split(indices: List<Int>, r: Rect) {
        if (indices.size == 1) {
            out[indices[0]] = r
            return
        }
        val sum = indices.sumOf { weights[it].toDouble() }.toFloat()
        var first = 0f
        var cut = 0
        while (cut < indices.size - 1 && first + weights[indices[cut]] <= sum / 2f) {
            first += weights[indices[cut]]
            cut++
        }
        if (cut == 0) {
            first = weights[indices[0]]
            cut = 1
        }
        val fraction = if (sum > 0f) first / sum else 0.5f
        if (r.width >= r.height) {
            val w = (r.width - gap) * fraction
            split(indices.subList(0, cut), Rect(r.left, r.top, r.left + w, r.bottom))
            split(indices.subList(cut, indices.size), Rect(r.left + w + gap, r.top, r.right, r.bottom))
        } else {
            val h = (r.height - gap) * fraction
            split(indices.subList(0, cut), Rect(r.left, r.top, r.right, r.top + h))
            split(indices.subList(cut, indices.size), Rect(r.left, r.top + h + gap, r.right, r.bottom))
        }
    }
    split(weights.indices.toList(), area)
    return out
}

/** One ring per top category, each closing as far as its share of the month. */
@Composable
private fun CategoryRings(categoryTotals: Map<Category, Double>, modifier: Modifier) {
    val colors = SpendlyTheme.colors
    val targets = targetsOf(categoryTotals)
    val shown = rememberAnimatedValues(targets)
    val total = categoryTotals.values.sum()
    val sorted = largestFirst(categoryTotals)
    // Inner rings get too small past four
    val ringed = sorted.take(4)
    val track = colors.containerHigh

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Canvas(modifier = Modifier.size(156.dp)) {
            val stroke = 10.dp.toPx()
            val step = stroke + 5.dp.toPx()
            val values = shown.map { it.value }
            val scale = maxOf(values.sum(), targets.sum())
            val rings = ringed.ifEmpty { listOf(Category.OTHER to 0.0) }
            rings.forEachIndexed { k, (category, _) ->
                val inset = stroke / 2 + k * step
                val topLeft = Offset(inset, inset)
                val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
                drawArc(track, -90f, 360f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                val sweep = if (scale > 0f) 360f * values[category.ordinal] / scale else 0f
                if (sweep > 0.5f) {
                    drawArc(colorOf(category), -90f, sweep, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            ringed.forEach { (category, amount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(8.dp).background(colorOf(category), CircleShape))
                        Text(
                            text = category.shortName,
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "${(amount / total * 100).roundToInt()}%",
                        fontFamily = SpaceGroteskFontFamily,
                        style = Numbers,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textPrimary
                    )
                }
            }
            if (sorted.size > ringed.size) {
                Text(
                    text = "+${sorted.size - ringed.size} more in Categories",
                    fontSize = 12.sp,
                    color = colors.textMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            if (sorted.isEmpty()) {
                Text(text = "Nothing spent yet", fontSize = 13.sp, color = colors.textSecondary)
            }
        }
    }
}

/** The donut's arc at half the height, with the month's total inside it. */
@Composable
private fun CategoryHalfDonut(categoryTotals: Map<Category, Double>, currencySymbol: String, modifier: Modifier) {
    val colors = SpendlyTheme.colors
    val targets = targetsOf(categoryTotals)
    val shown = rememberAnimatedValues(targets)
    val total = categoryTotals.values.sum()
    val shownTotal by animateAmountAsState(total)
    val track = colors.containerHigh

    Column(modifier = modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.width(240.dp).height(132.dp)) {
                val stroke = 18.dp.toPx()
                val topLeft = Offset(stroke / 2, stroke / 2)
                val diameter = size.width - stroke
                val arcSize = Size(diameter, diameter)
                drawArc(track, 180f, 180f, false, topLeft, arcSize, style = Stroke(stroke))
                val values = shown.map { it.value }
                val scale = maxOf(values.sum(), targets.sum())
                if (scale > 0f) {
                    val gapDegrees = 2f
                    val drawn = values.indices.filter { values[it] > scale * 0.002f }
                    val available = (180f - gapDegrees * (drawn.size - 1).coerceAtLeast(0)).coerceAtLeast(0f)
                    var start = 180f
                    drawn.forEach { index ->
                        val sweep = values[index] / scale * available
                        drawArc(colorOf(Category.entries[index]), start, sweep, false, topLeft, arcSize, style = Stroke(stroke))
                        start += sweep + gapDegrees
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 4.dp)) {
                Text(
                    text = money(currencySymbol, shownTotal),
                    fontFamily = SpaceGroteskFontFamily,
                    style = Numbers,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                Text(text = "Spent", fontSize = 12.sp, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(16.dp))
        CategoryLegend(largestFirst(categoryTotals), total, currencySymbol)
    }
}
