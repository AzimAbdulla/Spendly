package com.example.spendly.sms

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.TransactionSource
import com.example.spendly.data.model.TransactionStatus
import java.time.LocalDate
import java.time.LocalTime
import java.util.regex.Pattern

data class ParsedSms(
    val amount: Double,
    val currency: String,
    val merchant: String,
    val category: Category,
    val isHighConfidence: Boolean,
    val rawBody: String
) {
    fun toTransaction(autoSave: Boolean, date: LocalDate = LocalDate.now(), time: LocalTime? = null): Transaction {
        val status = if (isHighConfidence && autoSave) {
            TransactionStatus.CONFIRMED
        } else {
            TransactionStatus.NEEDS_REVIEW
        }
        return Transaction(
            amount = amount,
            currency = currency,
            merchant = merchant,
            category = category,
            date = date,
            time = time,
            notes = "From SMS: ${rawBody.take(60)}...",
            source = TransactionSource.SMS,
            status = status
        )
    }
}

object SmsParser {

    private val OTP_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(otp|verification code|one time password|security code|secret code)\\b"),
        Pattern.compile("(?i)\\b(do not share|never share|valid for \\d+ (mins|minutes))\\b")
    )

    private val CREDIT_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(credited|deposited|refunded|received money|cashback received)\\b")
    )

    private val DEBIT_KEYWORDS = listOf(
        "debited", "spent", "paid", "charged", "purchase", "txn of", "sent to", "withdrawn", "used at"
    )

    // Regex for amounts with currency
    // Matches: $45.50, USD 45.50, INR 350.00, Rs. 350, EUR 18.50, etc.
    private val AMOUNT_PATTERN = Pattern.compile(
        "(?i)(?:(USD|EUR|GBP|INR|Rs\\.?|CAD|AUD|CHF|SGD|AED|\\$|€|£|₹)\\s*([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)|([0-9]+(?:,[0-9]{3})*(?:\\.[0-9]{1,2})?)\\s*(USD|EUR|GBP|INR|Rs\\.?|CAD|AUD|CHF|SGD|AED))"
    )

    // Regex for merchant extraction - ordered by precedence
    private val MERCHANT_PATTERNS = listOf(
        Pattern.compile("(?i)\\b(?:at|to|vpa|info:|merchant:?)\\s+([a-zA-Z0-9&'._\\s]{2,30}?)(?=\\s+(?:for|on|via|using|ref|bal|avbl|avl|balance|dated|dt|ending|card|ac|a/c|with|txn|tx|\\.|\\,|\\;)|$)"),
        Pattern.compile("(?i)\\b(?:spent on|purchase at|paid to|purchase of)\\s+([a-zA-Z0-9&'._\\s]{2,30}?)(?=\\s+(?:for|on|via|using|ref|bal|avbl|avl|balance|dated|dt|ending|card|ac|a/c|with|txn|tx|\\.|\\,|\\;)|$)")
    )

    fun parse(smsBody: String): ParsedSms? {
        val trimmed = smsBody.trim()
        if (trimmed.isBlank()) return null

        // 1. Filter out OTPs
        for (pattern in OTP_PATTERNS) {
            if (pattern.matcher(trimmed).find()) {
                return null
            }
        }

        // 2. Filter out pure credit/refund messages (unless it also specifically says debited/spent)
        val hasDebitKeyword = DEBIT_KEYWORDS.any { trimmed.contains(it, ignoreCase = true) }
        if (!hasDebitKeyword) {
            return null
        }
        for (pattern in CREDIT_PATTERNS) {
            if (pattern.matcher(trimmed).find() && !trimmed.contains("debited", ignoreCase = true)) {
                return null
            }
        }

        // 3. Extract Amount & Currency
        val amountMatcher = AMOUNT_PATTERN.matcher(trimmed)
        if (!amountMatcher.find()) {
            return null
        }

        var rawCurrency: String? = null
        var rawAmount: String? = null

        if (amountMatcher.group(1) != null) {
            rawCurrency = amountMatcher.group(1)
            rawAmount = amountMatcher.group(2)
        } else if (amountMatcher.group(3) != null) {
            rawAmount = amountMatcher.group(3)
            rawCurrency = amountMatcher.group(4)
        }

        if (rawAmount == null) return null

        val cleanAmount = rawAmount.replace(",", "").toDoubleOrNull() ?: return null
        if (cleanAmount <= 0.0) return null

        val normalizedCurrency = normalizeCurrency(rawCurrency)

        // 4. Extract Merchant
        var merchant = extractMerchant(trimmed)
        val isHighConfidence = merchant.isNotBlank() && merchant.length >= 3 && cleanAmount > 0.0

        if (merchant.isBlank()) {
            merchant = "Unknown Merchant"
        }

        // 5. Categorize
        val category = inferCategory(merchant, trimmed)

        return ParsedSms(
            amount = cleanAmount,
            currency = normalizedCurrency,
            merchant = merchant,
            category = category,
            isHighConfidence = isHighConfidence,
            rawBody = trimmed
        )
    }

    private fun normalizeCurrency(raw: String?): String {
        return when (raw?.uppercase()?.trim()) {
            "$", "USD" -> "USD"
            "€", "EUR" -> "EUR"
            "£", "GBP" -> "GBP"
            "₹", "RS", "RS.", "INR" -> "INR"
            "CAD" -> "CAD"
            "AUD" -> "AUD"
            "CHF" -> "CHF"
            "SGD" -> "SGD"
            "AED" -> "AED"
            else -> "USD"
        }
    }

    private fun extractMerchant(text: String): String {
        for (pattern in MERCHANT_PATTERNS) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim() ?: ""
                val cleaned = candidate
                    .replace(Regex("(?i)^(the|a|an)\\s+"), "")
                    .replace(Regex("[^a-zA-Z0-9&'\\s]"), "")
                    .trim()
                if (isValidMerchant(cleaned)) {
                    return cleaned.split(" ").filter { it.isNotBlank() }.joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
                }
            }
        }
        return ""
    }

    private fun isValidMerchant(candidate: String): Boolean {
        val lower = candidate.lowercase().trim()
        if (lower.length !in 2..35) return false

        val invalidTokens = setOf(
            "bank", "card", "account", "a/c", "ac", "ending", "debit", "credit",
            "balance", "bal", "available", "avbl", "payment", "alert", "transaction", "amount",
            "usd", "inr", "eur", "gbp", "cad", "aud", "otp", "your", "info", "vpa"
        )
        val tokens = lower.split(Regex("[\\s_]+"))
        if (tokens.any { it in invalidTokens }) return false

        val digitsCount = lower.count { it.isDigit() }
        val lettersCount = lower.count { it.isLetter() }
        if (digitsCount > 0 && digitsCount >= lettersCount) return false

        return true
    }

    fun inferCategory(merchant: String, fullText: String): Category {
        val combined = "$merchant $fullText".lowercase()
        return when {
            combined.containsAny("food", "restaurant", "cafe", "coffee", "starbucks", "swiggy", "zomato", "uber eats", "mcdonald", "burger", "pizza", "dining", "bakery", "subway") ->
                Category.FOOD
            combined.containsAny("uber", "lyft", "grab", "taxi", "cab", "metro", "fuel", "gas", "petrol", "shell", "chevron", "parking", "transport", "train", "flight", "transit") ->
                Category.TRANSPORT
            combined.containsAny("supermarket", "grocery", "whole foods", "walmart", "target", "costco", "trader joe", "kroger", "tesco", "sainsbury", "asda", "aldi", "lidl", "carrefour") ->
                Category.FOOD
            combined.containsAny("amazon", "zara", "h&m", "nike", "shopping", "store", "ebay", "walmart", "apple store", "mall", "clothing") ->
                Category.SHOPPING
            combined.containsAny("netflix", "spotify", "apple.com/bill", "youtube", "prime video", "disney", "hulu", "patreon", "subscription") ->
                Category.SUBSCRIPTIONS
            combined.containsAny("cinema", "movie", "theater", "theatre", "amc", "steam", "playstation", "xbox", "nintendo", "entertainment") ->
                Category.ENTERTAINMENT
            combined.containsAny("pharmacy", "health", "hospital", "doctor", "clinic", "cvs", "walgreens", "dental", "medical") ->
                Category.HEALTH
            combined.containsAny("electric", "water", "gas bill", "utility", "power", "internet", "broadband", "verizon", "at&t", "t-mobile") ->
                Category.UTILITIES
            combined.containsAny("hotel", "airbnb", "airline", "booking.com", "expedia", "resort", "travel") ->
                Category.TRAVEL
            else -> Category.OTHER
        }
    }

    private fun String.containsAny(vararg keywords: String): Boolean {
        return keywords.any { this.contains(it) }
    }
}
