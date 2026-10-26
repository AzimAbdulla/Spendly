package com.example.spendly.sms

import com.example.spendly.data.model.Category
import org.junit.Assert.*
import org.junit.Test

class SmsParserTest {

    @Test
    fun parseStandardDebitSms_extractsAmountAndMerchant() {
        val sms = "Your A/C ending 1234 was debited by USD 45.50 at Whole Foods on 26-Oct-26. Avbl Bal: USD 1,200."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(45.50, result!!.amount, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Whole Foods", result.merchant)
        assertEquals(Category.FOOD, result.category)
        assertTrue(result.isHighConfidence)
    }

    @Test
    fun parseUpiInrSms_extractsCorrectValues() {
        val sms = "Rs. 350.00 spent on HDFC Bank Card ending 5678 at Swiggy on 26-10-2026. Bal: Rs. 5400."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(350.0, result!!.amount, 0.001)
        assertEquals("INR", result.currency)
        assertEquals("Swiggy", result.merchant)
        assertEquals(Category.FOOD, result.category)
    }

    @Test
    fun parseRideShareSms_infersTransportCategory() {
        val sms = "Paid $8.75 to Uber for your trip. Thank you for riding with Uber."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(8.75, result!!.amount, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Uber", result.merchant)
        assertEquals(Category.TRANSPORT, result.category)
    }

    @Test
    fun parseSubscriptionSms_infersSubscriptionCategory() {
        val sms = "Charged EUR 12.99 at Netflix for monthly subscription."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(12.99, result!!.amount, 0.001)
        assertEquals("EUR", result.currency)
        assertEquals(Category.SUBSCRIPTIONS, result.category)
    }

    @Test
    fun parseOtpSms_returnsNull() {
        val sms = "Your OTP for transaction of USD 50.00 at Amazon is 492810. Do not share this OTP with anyone."
        val result = SmsParser.parse(sms)

        assertNull("OTPs must be ignored by SMS expense tracker", result)
    }

    @Test
    fun parseCreditSms_returnsNull() {
        val sms = "Salary of USD 3,500.00 has been credited to your account ending 9876."
        val result = SmsParser.parse(sms)

        assertNull("Salary/credits must not be counted as spending", result)
    }

    @Test
    fun parseAmbiguousSms_returnsLowConfidence() {
        val sms = "You have spent $15.00 from your card ending 1111."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertFalse("Without clear merchant it should be low confidence", result.isHighConfidence)
    }

    @Test
    fun parseGbpSms_extractsCorrectValues() {
        val sms = "Your debit card was charged £42.00 at Tesco on 25/10/2026."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(42.0, result!!.amount, 0.001)
        assertEquals("GBP", result.currency)
        assertEquals("Tesco", result.merchant)
        assertEquals(Category.FOOD, result.category)
    }

    @Test
    fun parsePurchaseAtShopping_extractsCorrectValues() {
        val sms = "USD 129.99 debited for purchase at Zara on 25-Oct-26."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(129.99, result!!.amount, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Zara", result.merchant)
        assertEquals(Category.SHOPPING, result.category)
    }

    @Test
    fun toTransaction_autoSaveTrue_createsConfirmedTransaction() {
        val sms = "Debited USD 25.00 at Starbucks on 26-Oct-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)

        val tx = parsed!!.toTransaction(autoSave = true)
        assertEquals(com.example.spendly.data.model.TransactionStatus.CONFIRMED, tx.status)
        assertEquals(com.example.spendly.data.model.TransactionSource.SMS, tx.source)
    }

    @Test
    fun toTransaction_autoSaveFalse_createsNeedsReviewTransaction() {
        val sms = "Debited USD 25.00 at Starbucks on 26-Oct-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)

        val tx = parsed!!.toTransaction(autoSave = false)
        assertEquals(com.example.spendly.data.model.TransactionStatus.NEEDS_REVIEW, tx.status)
    }
}
