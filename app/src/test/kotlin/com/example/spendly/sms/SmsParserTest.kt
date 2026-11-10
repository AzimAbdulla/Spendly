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

    // Federal Bank's UPI debit format (names, account and reference numbers changed)
    @Test
    fun parseFederalBankUpiDebits_extractsAmountAndPayee() {
        val messages = listOf(
            "Debited Rs 140.00 from a/c X1234 on 04Nov26 18:41 via UPI to Arun K. Ref 111122223333.Bal Rs 4958.03. Not you?Call 18004251199 -Federal Bank" to (140.0 to "Arun K"),
            "Debited Rs 30.00 from a/c X1234 on 03Nov26 15:14 via UPI to Staff and cu. Ref 222233334444.Bal Rs 6690.63. Not you?Call 18004251199 -Federal Bank" to (30.0 to "Staff And Cu"),
            "Debited Rs 180.00 from a/c X1234 on 02Nov26 02:13 via UPI to RAHUL P. Ref 333344445555.Bal Rs 6800.63. Not you?Call 18004251199 -Federal Bank" to (180.0 to "RAHUL P"),
            "Debited Rs 409.00 from a/c X1234 on 01Nov26 16:43 via UPI to Swiggy Ltd. Ref 444455556666.Bal Rs 2030.63. Not you?Call 18004251199 -Federal Bank" to (409.0 to "Swiggy Ltd")
        )
        for ((sms, expected) in messages) {
            val result = SmsParser.parse(sms)

            assertNotNull(sms, result)
            assertEquals(expected.first, result!!.amount, 0.001)
            assertEquals("INR", result.currency)
            assertEquals(expected.second, result.merchant)
            assertTrue(result.isHighConfidence)
        }
        assertEquals(Category.FOOD, SmsParser.parse(messages.last().first)!!.category)
    }

    // A toll comes out of the FASTag balance, not the bank account, so it isn't an expense here
    @Test
    fun parseFastagTollSms_returnsNull() {
        val sms = "Rs.30.00 paid for Kumbalam on 05-Nov-2026 12:21:28 for KL01AB1234 via Federal Bank FASTag. Bal Rs.220.00. Call 18002669520 for discrepancy. Always wear seat belt in front and rear seats while travelling. In case of emergency, kindly call NHAI Helpline 1033 -Federal Bank"

        assertNull(SmsParser.parse(sms))
    }

    @Test
    fun parseCardSmsWithoutAccount_returnsNull() {
        val sms = "Rs. 350.00 spent on HDFC Bank Card ending 5678 at Swiggy on 26-10-2026. Bal: Rs. 5400."

        assertNull("Only debits from a bank account are recorded", SmsParser.parse(sms))
    }

    @Test
    fun parseRideShareSms_infersTransportCategory() {
        val sms = "Paid $8.75 from a/c XX1234 to Uber for your trip. Thank you for riding with Uber."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(8.75, result!!.amount, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Uber", result.merchant)
        assertEquals(Category.TRANSPORT, result.category)
    }

    @Test
    fun parseSubscriptionSms_infersSubscriptionCategory() {
        val sms = "Your a/c XX1234 was charged EUR 12.99 at Netflix for monthly subscription."
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
        val sms = "You have spent $15.00 from your account ending 1111."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(15.0, result!!.amount, 0.001)
        assertFalse("Without clear merchant it should be low confidence", result.isHighConfidence)
    }

    @Test
    fun parseGbpSms_extractsCorrectValues() {
        val sms = "Your account ending 4321 was charged £42.00 at Tesco on 25/10/2026."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(42.0, result!!.amount, 0.001)
        assertEquals("GBP", result.currency)
        assertEquals("Tesco", result.merchant)
        assertEquals(Category.FOOD, result.category)
    }

    @Test
    fun parsePurchaseAtShopping_extractsCorrectValues() {
        val sms = "USD 129.99 debited from a/c XX1234 for purchase at Zara on 25-Oct-26."
        val result = SmsParser.parse(sms)

        assertNotNull(result)
        assertEquals(129.99, result!!.amount, 0.001)
        assertEquals("USD", result.currency)
        assertEquals("Zara", result.merchant)
        assertEquals(Category.SHOPPING, result.category)
    }

    @Test
    fun toTransaction_autoSaveTrue_createsConfirmedTransaction() {
        val sms = "Debited USD 25.00 from a/c XX1234 at Starbucks on 26-Oct-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)

        val tx = parsed!!.toTransaction(autoSave = true)
        assertEquals(com.example.spendly.data.model.TransactionStatus.CONFIRMED, tx.status)
        assertEquals(com.example.spendly.data.model.TransactionSource.SMS, tx.source)
    }

    @Test
    fun toTransaction_autoSaveFalse_createsNeedsReviewTransaction() {
        val sms = "Debited USD 25.00 from a/c XX1234 at Starbucks on 26-Oct-26."
        val parsed = SmsParser.parse(sms)
        assertNotNull(parsed)

        val tx = parsed!!.toTransaction(autoSave = false)
        assertEquals(com.example.spendly.data.model.TransactionStatus.NEEDS_REVIEW, tx.status)
    }
}
