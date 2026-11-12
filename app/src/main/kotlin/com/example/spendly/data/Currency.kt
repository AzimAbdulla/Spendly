package com.example.spendly.data

/** The one place a currency code becomes the symbol shown in front of an amount. */
fun currencySymbol(code: String): String = when (code) {
    "INR" -> "\u20B9"
    "USD", "CAD", "AUD", "SGD" -> "$"
    "EUR" -> "\u20AC"
    "GBP" -> "\u00A3"
    "JPY" -> "\u00A5"
    else -> "$code "
}
