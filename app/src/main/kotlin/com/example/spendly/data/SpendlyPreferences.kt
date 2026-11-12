package com.example.spendly.data

import android.content.Context
import android.content.SharedPreferences
import com.example.spendly.data.model.Category

class SpendlyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)

    var defaultCurrency: String
        get() = prefs.getString(KEY_DEFAULT_CURRENCY, "INR") ?: "INR"
        set(value) = prefs.edit().putString(KEY_DEFAULT_CURRENCY, value).apply()

    var isSmsAutoSaveEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMS_AUTO_SAVE, true)
        set(value) = prefs.edit().putBoolean(KEY_SMS_AUTO_SAVE, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "DARK") ?: "DARK"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    // Monthly spending target in the default currency; 0 means none is set
    var monthlyBudget: Double
        get() = prefs.getFloat(KEY_MONTHLY_BUDGET, 0f).toDouble()
        set(value) = prefs.edit().putFloat(KEY_MONTHLY_BUDGET, value.toFloat()).apply()

    // The category the user last gave a payee, so its next SMS expense arrives already sorted
    fun learnedCategory(merchant: String): Category? =
        prefs.getString(KEY_MERCHANT_CATEGORY + merchant.trim().lowercase(), null)
            ?.let { name -> Category.entries.firstOrNull { it.name == name } }

    fun learnCategory(merchant: String, category: Category) {
        val name = merchant.trim().lowercase()
        if (name.isEmpty() || name == "unknown merchant") return
        prefs.edit().putString(KEY_MERCHANT_CATEGORY + name, category.name).apply()
    }

    // Home screen widgets, shared by every Spendly widget
    var widgetLightBackground: Boolean
        get() = prefs.getBoolean(KEY_WIDGET_LIGHT, false)
        set(value) = prefs.edit().putBoolean(KEY_WIDGET_LIGHT, value).apply()

    var widgetTransparency: Int
        get() = prefs.getInt(KEY_WIDGET_TRANSPARENCY, 0)
        set(value) = prefs.edit().putInt(KEY_WIDGET_TRANSPARENCY, value).apply()

    // "TINTED", "DATA" or "FIGURES": how the widgets use colour (see WidgetColour)
    var widgetColour: String
        get() = prefs.getString(KEY_WIDGET_COLOUR, "TINTED") ?: "TINTED"
        set(value) = prefs.edit().putString(KEY_WIDGET_COLOUR, value).apply()

    var widgetHideAmounts: Boolean
        get() = prefs.getBoolean(KEY_WIDGET_HIDE_AMOUNTS, false)
        set(value) = prefs.edit().putBoolean(KEY_WIDGET_HIDE_AMOUNTS, value).apply()

    // SharedPreferences keeps listeners only weakly, so the one listener lives here
    private var changeListener: SharedPreferences.OnSharedPreferenceChangeListener? = null

    fun onChange(block: () -> Unit) {
        changeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> block() }
            .also(prefs::registerOnSharedPreferenceChangeListener)
    }

    companion object {
        private const val KEY_DEFAULT_CURRENCY = "key_default_currency"
        private const val KEY_SMS_AUTO_SAVE = "key_sms_auto_save"
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_MONTHLY_BUDGET = "key_monthly_budget"
        private const val KEY_MERCHANT_CATEGORY = "merchant_category_"
        private const val KEY_WIDGET_LIGHT = "key_widget_light"
        private const val KEY_WIDGET_TRANSPARENCY = "key_widget_transparency"
        private const val KEY_WIDGET_HIDE_AMOUNTS = "key_widget_hide_amounts"
        private const val KEY_WIDGET_COLOUR = "key_widget_colour"
    }
}
