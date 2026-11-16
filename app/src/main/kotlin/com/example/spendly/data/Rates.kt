package com.example.spendly.data

import android.content.Context
import com.example.spendly.data.model.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** The currencies an expense can be in. */
val supportedCurrencies = listOf("INR", "AED", "USD", "EUR", "GBP", "JPY", "CAD", "AUD", "SGD", "CHF")

/** Where an expense's exchange rate came from. Stored by name. */
object RateSource {
    const val NONE = "NONE"           // same currency as the display currency when saved
    const val ESTIMATED = "ESTIMATED" // the last rate known on the phone; replaced by that day's rate once online
    const val FETCHED = "FETCHED"     // that day's rate, never changed again
    const val MANUAL = "MANUAL"       // typed by the user, never changed by a fetch
}

/**
 * The display currency and the latest known rates, as units of each currency per one US dollar.
 * Any pair converts through that table.
 */
data class Fx(val display: String, val perUsd: Map<String, Double>, val updated: LocalDate? = null) {
    /** How many [to] one [from] is worth, or null when either currency is unknown. */
    fun rate(from: String, to: String = display): Double? {
        if (from == to) return 1.0
        val f = perUsd[from] ?: return null
        val t = perUsd[to] ?: return null
        return if (f > 0) t / f else null
    }

    /**
     * [t] as the screens use it: `rate` turned into "display currency per one of its own". The
     * rate saved with the expense is used as it is when it was saved for this display currency;
     * one saved for another display currency is carried across at today's rate. The stored row is
     * never changed by this.
     */
    fun inDisplay(t: Transaction): Transaction {
        val effective = when {
            t.currency == display -> null
            t.rate != null && t.rateCurrency == display -> t.rate
            t.rate != null && t.rateCurrency != null -> rate(t.rateCurrency)?.let { t.rate * it }
            else -> rate(t.currency)
        }
        val shownIn = if (effective != null) display else null
        return if (effective == t.rate && shownIn == t.rateCurrency) t else t.copy(rate = effective, rateCurrency = shownIn)
    }

    companion object {
        /** Rates of 16 Nov 2026, so a first run with no connection still converts sensibly. */
        val Bundled = mapOf(
            "USD" to 1.0, "INR" to 94.1106, "AED" to 3.6725, "EUR" to 0.9073, "GBP" to 0.7569,
            "JPY" to 159.4253, "CAD" to 1.4274, "AUD" to 1.4801, "SGD" to 1.2882, "CHF" to 0.8385
        )
    }
}

/**
 * Exchange rates: kept on the phone, refreshed from a public rates file when there's a
 * connection. Nothing about the user or their expenses is ever sent; the only request is for
 * that file. Every failure is silent, because the app must work the same with no connection.
 */
class Rates(context: Context, displayCurrency: String) {
    private val store = context.getSharedPreferences("spendly_rates", Context.MODE_PRIVATE)

    private val _fx = MutableStateFlow(
        Fx(
            display = displayCurrency,
            perUsd = Fx.Bundled + supportedCurrencies.mapNotNull { code ->
                store.getFloat(code, 0f).takeIf { it > 0 }?.let { code to it.toDouble() }
            },
            updated = store.getString(KEY_UPDATED, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        )
    )
    val fx: StateFlow<Fx> = _fx.asStateFlow()

    fun setDisplay(code: String) = _fx.update { it.copy(display = code) }

    /** Fetches today's rates, at most once a day. True when the rates on the phone are today's. */
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        val today = LocalDate.now()
        if (_fx.value.updated == today) return@withContext true
        val table = latest() ?: return@withContext false
        val edit = store.edit().putString(KEY_UPDATED, today.toString())
        table.forEach { (code, value) -> edit.putFloat(code, value.toFloat()) }
        edit.apply()
        _fx.update { it.copy(perUsd = it.perUsd + table, updated = today) }
        true
    }

    /** What one [from] was worth in [to] on [date], or null when it can't be fetched now. */
    suspend fun onDate(from: String, to: String, date: LocalDate): Double? = withContext(Dispatchers.IO) {
        val f = from.lowercase()
        val json = get("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@$date/v1/currencies/$f.min.json") ?: return@withContext null
        json.optJSONObject(f)?.optDouble(to.lowercase())?.takeIf { it.isFinite() && it > 0 }
    }

    private fun latest(): Map<String, Double>? {
        get("https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/usd.min.json")
            ?.optJSONObject("usd")?.let { table(it) { code -> code.lowercase() } }?.let { return it }
        return get("https://open.er-api.com/v6/latest/USD")?.optJSONObject("rates")?.let { table(it) { code -> code } }
    }

    private fun table(json: JSONObject, key: (String) -> String): Map<String, Double>? =
        supportedCurrencies.mapNotNull { code ->
            json.optDouble(key(code)).takeIf { it.isFinite() && it > 0 }?.let { code to it }
        }.toMap().takeIf { it.size == supportedCurrencies.size }

    private fun get(url: String): JSONObject? = runCatching {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 6000
        connection.readTimeout = 6000
        try {
            if (connection.responseCode != 200) null
            else JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }.getOrNull()

    private companion object {
        const val KEY_UPDATED = "updated"
    }
}
