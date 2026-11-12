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
    /**
     * [autoSave] off sends everything to review. An amount in another currency than
     * [defaultCurrency] always goes to review, since totals are kept in one currency.
     */
    fun toTransaction(
        autoSave: Boolean,
        date: LocalDate = LocalDate.now(),
        time: LocalTime? = null,
        smsKey: String? = null,
        defaultCurrency: String = currency,
        learnedCategory: Category? = null
    ): Transaction {
        val status = if (isHighConfidence && autoSave && currency == defaultCurrency) {
            TransactionStatus.CONFIRMED
        } else {
            TransactionStatus.NEEDS_REVIEW
        }
        return Transaction(
            amount = amount,
            currency = currency,
            merchant = merchant,
            category = learnedCategory ?: category,
            date = date,
            time = time,
            notes = "From SMS: ${rawBody.take(60)}...",
            source = TransactionSource.SMS,
            status = status,
            smsKey = smsKey
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

    // Only money leaving a bank account counts. Card, wallet and FASTag alerts don't name an account
    // (a toll comes out of the FASTag balance, which was already counted when it was topped up)
    private val ACCOUNT_PATTERN = Pattern.compile("(?i)\\b(?:a/c|acct|account)\\b")

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

        // 3. Only debits from a bank account
        if (!ACCOUNT_PATTERN.matcher(trimmed).find()) {
            return null
        }

        // 4. Extract Amount & Currency
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

        // 5. Extract Merchant
        var merchant = extractMerchant(trimmed)
        val isHighConfidence = merchant.isNotBlank() && merchant.length >= 3 && cleanAmount > 0.0

        if (merchant.isBlank()) {
            merchant = "Unknown Merchant"
        }

        // 6. Categorize
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

    // First match wins, so the narrower lists come first (Instamart before Swiggy, a gas bill
    // before fuel). Whole words only: "cab" must not match "cable"
    private val CATEGORY_KEYWORDS: List<Pair<Category, Regex>> = listOf(
        Category.GROCERIES to "instamart|blinkit|zepto|bigbasket|big basket|dmart|d mart|jiomart|grofers|dunzo|supermarket|hypermarket|grocery|groceries|kirana|provision|provisions|vegetables|fruits|milk|dairy|lulu|more retail|reliance fresh|reliance smart|spencers|nature's basket|whole foods|walmart|costco|tesco|sainsbury|aldi|lidl|carrefour",
        Category.UTILITIES to "electricity|electric|kseb|bescom|tneb|msedcl|tata power|adani electricity|water bill|water authority|gas bill|indane|bharat gas|hp gas|lpg|utility|broadband|fibernet|wifi|recharge|prepaid|postpaid|jio|airtel|vodafone|bsnl|act fibernet|dth|tata play|d2h",
        Category.SUBSCRIPTIONS to "netflix|spotify|hotstar|jiocinema|sonyliv|zee5|youtube premium|prime video|amazon prime|apple.com/bill|google one|icloud|disney|patreon|subscription",
        Category.FOOD to "swiggy|zomato|eatsure|dominos|domino's|pizza hut|pizza|kfc|mcdonald|mcdonalds|burger king|burger|subway|starbucks|cafe coffee day|ccd|chaayos|restaurant|restaurants|hotel and restaurant|cafe|coffee|tea|bakery|bakers|biryani|juice|food|foods|dining|canteen|mess|thattukada|dhaba|sweets",
        Category.TRAVEL to "irctc|indian railways|makemytrip|goibibo|cleartrip|yatra|ixigo|redbus|abhibus|indigo|air india|vistara|akasa|spicejet|airline|airlines|airways|flight|airbnb|oyo|booking.com|expedia|resort|travels|travel",
        Category.TRANSPORT to "uber|ola|rapido|namma yatri|blusmart|taxi|cab|cabs|auto|metro|ksrtc|bus|petrol|diesel|fuel|fuels|petroleum|indian oil|iocl|bpcl|bharat petroleum|hpcl|hp petrol|shell|parking|toll|transport|transit|train",
        Category.ENTERTAINMENT to "bookmyshow|pvr|inox|cinepolis|cinema|cinemas|movie|movies|theatre|theater|steam|playstation|xbox|nintendo|gaming|entertainment",
        Category.HEALTH to "pharmacy|pharma|medical|medicals|medplus|apollo|netmeds|pharmeasy|1mg|practo|hospital|clinic|doctor|dental|diagnostics|lab|labs|health",
        Category.SHOPPING to "amazon|flipkart|myntra|ajio|meesho|nykaa|croma|reliance digital|decathlon|ikea|lifestyle|pantaloons|westside|zara|h&m|nike|adidas|shopping|store|stores|mall|mart|textiles|fashion|clothing|footwear|electronics"
    ).map { (category, words) -> category to Regex("(?<![a-z0-9])(?:${words.replace(".", "\\.")})(?![a-z0-9])") }

    /** The payee decides when it can; the rest of the message is only a fallback. */
    fun inferCategory(merchant: String, fullText: String): Category {
        for (text in listOf(merchant.lowercase(), fullText.lowercase())) {
            CATEGORY_KEYWORDS.firstOrNull { it.second.containsMatchIn(text) }?.let { return it.first }
        }
        return Category.OTHER
    }

    /**
     * Banks send alerts from a short name ("VM-FEDBNK") or a short code, never from an ordinary
     * phone number, so a text from a person that happens to mention a payment isn't an expense.
     */
    fun isBankSender(address: String?): Boolean {
        val sender = address?.trim().orEmpty()
        if (sender.isEmpty()) return true
        return !Regex("\\+?[0-9]{8,}").matches(sender.replace(" ", "").replace("-", ""))
    }

    /** Stays the same for one message however it's read: as it arrives, or later from the inbox. */
    fun smsKey(sentMillis: Long, body: String): String = "$sentMillis:${body.trim().hashCode()}"

}
