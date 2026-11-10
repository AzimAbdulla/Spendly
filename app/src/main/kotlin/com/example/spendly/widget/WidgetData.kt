package com.example.spendly.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.glance.action.Action
import androidx.glance.appwidget.action.actionStartActivity
import com.example.spendly.MainActivity
import com.example.spendly.SpendlyApplication
import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import com.example.spendly.sms.SmsNotifier
import com.example.spendly.ui.components.stitchCategoryColors
import com.example.spendly.ui.theme.DarkSpendlyColors
import com.example.spendly.ui.theme.LightSpendlyColors
import com.example.spendly.ui.theme.TileTint
import com.example.spendly.ui.theme.TintList
import com.example.spendly.ui.theme.TintMoney
import com.example.spendly.ui.theme.TintSafe
import com.example.spendly.ui.theme.TintTime
import com.example.spendly.ui.theme.tint
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** What the widgets show, read fresh from the database on every update. */
internal data class WidgetData(
    val monthName: String,
    val monthTotal: Double,
    val monthCount: Int,
    val dailyAverage: Double,
    val split: List<Pair<Category, Double>>, // this month, largest first
    val week: List<Double>,                 // last 7 days, today last
    val weekLabels: List<String>,
    val recent: List<Transaction>,
    val reviewCount: Int,
    val lastWeekTotal: Double,              // the 7 days before those
    val todayList: List<Transaction>,       // today's latest expenses
    val payees: List<String>                // who recent bank SMS expenses went to
) {
    val today get() = week.last()
    val weekTotal get() = week.sum()
}

/** How the widgets use colour, picked in the widget settings. */
object WidgetColour {
    /** Each widget gets a wash of its own colour. */
    const val TINTED = "TINTED"
    /** Neutral backgrounds; colour only in the bars, ring and dots, brighter on dark. */
    const val DATA = "DATA"
    /** Neutral backgrounds; each widget's big figure and label take its colour too. */
    const val FIGURES = "FIGURES"
}

/** Colours and amounts as the widget settings ask: the app's tile tints on a dark or light base. */
internal class WidgetStyle(
    private val light: Boolean,
    transparency: Int,
    colour: String,
    private val hideAmounts: Boolean,
    private val symbol: String
) {
    private val tinted = colour == WidgetColour.TINTED
    private val figures = colour == WidgetColour.FIGURES

    private val base = if (light) LightSpendlyColors else DarkSpendlyColors
    private val opacity = 1f - transparency.coerceIn(0, 100) / 100f

    val text = base.textPrimary
    val money: TileTint = base.tint(TintMoney)
    val time: TileTint = base.tint(TintTime)
    val list: TileTint = base.tint(TintList)
    val today: TileTint = base.tint(TintSafe)
    val close: TileTint = base.tint(TintTime)
    val over: TileTint = base.tint(Color(0xFFE2605C))
    val quickFill = base.cardBackground
    val quickSecondary = base.textSecondary
    val review = base.tertiary
    val muted = base.textSecondary
    // Seen on a see-through widget too: a divider line, green for good news and today, amber for the peak
    val outline = (if (light) Color.Black else Color.White).copy(alpha = 0.12f)
    val positive = bright(base.tint(TintSafe).accent)
    val highlight = bright(base.tint(TintTime).accent)
    val neutralBar = if (light) Color(0xFFDFE3EA) else Color(0xFF2A2F3A)
    val plusFill = base.primary
    val onPlus = base.onPrimary

    /** A widget's background: its own colour's wash when tinted, plain otherwise. */
    fun tileFill(tint: TileTint) = fill(if (tinted) tint.background else base.cardBackground)

    /** A tint's colour for a bar or a line of text, lifted like the rest on plain dark widgets. */
    fun accent(tint: TileTint) = bright(tint.accent)

    /** A widget's header label. */
    fun label(tint: TileTint) = when {
        figures -> bright(tint.accent)
        tinted -> tint.secondary
        else -> muted
    }

    /** Secondary text under a figure. */
    fun sub(tint: TileTint) = if (tinted) tint.secondary else muted

    /** A widget's big figure. */
    fun figure(tint: TileTint) = if (figures) bright(tint.accent) else text

    /** A category's colour in bars, rings and dots. */
    fun cat(category: Category) = bright(stitchCategoryColors[category] ?: base.textMuted)

    // Without a coloured wash behind it, colour has to carry more on its own, so it's lifted on dark
    private fun bright(color: Color) = if (light || tinted) color else lerp(color, Color.White, 0.2f)

    fun category(category: Category): TileTint = base.tint(stitchCategoryColors[category] ?: base.textMuted)

    /** A widget's background with the chosen transparency; content stays fully opaque. */
    // A tinted widget keeps a soft wash even at full transparency, or it would look like the plain styles
    fun fill(color: Color) = color.copy(alpha = color.alpha * if (tinted) maxOf(opacity, 0.45f) else opacity)

    /** A short figure for tight spots: ₹850, ₹4.6k. */
    fun compact(value: Double): String = when {
        hideAmounts -> "$symbol ••"
        value >= 1000 -> "$symbol${String.format("%.1f", value / 1000)}k"
        else -> "$symbol${String.format("%,.0f", value)}"
    }

    fun amount(value: Double, decimals: Boolean = false): String =
        if (hideAmounts) "$symbol ••••" else "$symbol${String.format(if (decimals) "%,.2f" else "%,.0f", value)}"

    /** The big figure split so the decimals can be set smaller; blank decimals when amounts are hidden. */
    fun bigAmount(value: Double): Pair<String, String> {
        if (hideAmounts) return "$symbol ••••" to ""
        val (whole, cents) = String.format("%,.2f", value).split(".").let { it[0] to it.getOrElse(1) { "00" } }
        return "$symbol$whole" to ".$cents"
    }
}

internal fun currencySymbol(code: String) = when (code) {
    "INR" -> "₹"
    "EUR" -> "€"
    "GBP" -> "£"
    else -> "$"
}

internal suspend fun loadWidget(context: Context): Pair<WidgetData, WidgetStyle> {
    val app = context.applicationContext as SpendlyApplication
    val prefs = app.preferences
    val all = app.repository.getAllTransactions().first() // newest first
    val today = LocalDate.now()
    val month = YearMonth.from(today)
    val inMonth = all.filter { YearMonth.from(it.date) == month }
    val monthTotal = inMonth.sumOf { it.amount }
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val data = WidgetData(
        monthName = month.format(DateTimeFormatter.ofPattern("MMMM")),
        monthTotal = monthTotal,
        monthCount = inMonth.size,
        dailyAverage = monthTotal / today.dayOfMonth,
        split = inMonth.groupBy { it.category }
            .mapValues { (_, txs) -> txs.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
            .map { it.key to it.value },
        week = days.map { day -> all.filter { it.date == day }.sumOf { it.amount } },
        weekLabels = days.map { it.format(DateTimeFormatter.ofPattern("EEEEE")) },
        recent = all.take(3),
        reviewCount = all.count { it.status == TransactionStatus.NEEDS_REVIEW },
        lastWeekTotal = (13 downTo 7).map { today.minusDays(it.toLong()) }
            .sumOf { day -> all.filter { it.date == day }.sumOf { it.amount } },
        todayList = all.filter { it.date == today }.take(3),
        payees = all.take(10)
            .filter { it.source == TransactionSource.SMS && it.merchant.isNotBlank() && it.merchant != "Unknown Merchant" }
            .map { it.merchant.substringBefore(' ') }
            .distinct()
            .take(2)
    )
    val style = WidgetStyle(
        light = prefs.widgetLightBackground,
        transparency = prefs.widgetTransparency,
        colour = prefs.widgetColour,
        hideAmounts = prefs.widgetHideAmounts,
        symbol = currencySymbol(prefs.defaultCurrency)
    )
    return data to style
}

/** Where a widget tap lands; MainActivity reads it from the launch intent. */
data class WidgetTarget(val screen: String, val category: String?)

object WidgetLaunch {
    const val EXTRA_SCREEN = "com.example.spendly.WIDGET_SCREEN"
    const val EXTRA_CATEGORY = "com.example.spendly.WIDGET_CATEGORY"
    const val HOME = "home"
    const val WEEK = "week"
    const val REVIEW = "review"
    const val ACTIVITY = "activity"
    const val ADD = "add"
}

/** Ignores the extras when Android replays the launching intent from Recents. */
fun Intent.widgetTarget(): WidgetTarget? {
    if (flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0) return null
    val screen = getStringExtra(WidgetLaunch.EXTRA_SCREEN) ?: return null
    return WidgetTarget(screen, getStringExtra(WidgetLaunch.EXTRA_CATEGORY))
}

private fun launch(context: Context, path: String) = Intent(context, MainActivity::class.java)
    // A distinct data URI per target keeps each tap's PendingIntent separate
    .setData(Uri.parse("spendly://widget/$path"))
    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

internal fun openScreen(context: Context, screen: String, category: Category? = null): Action =
    actionStartActivity(
        launch(context, listOfNotNull(screen, category?.name).joinToString("/"))
            .putExtra(WidgetLaunch.EXTRA_SCREEN, screen)
            .apply { category?.let { putExtra(WidgetLaunch.EXTRA_CATEGORY, it.name) } }
    )

internal fun openExpense(context: Context, id: Long): Action =
    actionStartActivity(launch(context, "expense/$id").putExtra(SmsNotifier.EXTRA_TRANSACTION_ID, id))
