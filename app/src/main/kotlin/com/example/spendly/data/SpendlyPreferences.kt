package com.example.spendly.data

import android.content.Context
import android.content.SharedPreferences

class SpendlyPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("spendly_prefs", Context.MODE_PRIVATE)

    var defaultCurrency: String
        get() = prefs.getString(KEY_DEFAULT_CURRENCY, "INR") ?: "INR"
        set(value) = prefs.edit().putString(KEY_DEFAULT_CURRENCY, value).apply()

    var isSmsAutoSaveEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMS_AUTO_SAVE, true)
        set(value) = prefs.edit().putBoolean(KEY_SMS_AUTO_SAVE, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    companion object {
        private const val KEY_DEFAULT_CURRENCY = "key_default_currency"
        private const val KEY_SMS_AUTO_SAVE = "key_sms_auto_save"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
