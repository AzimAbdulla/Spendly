package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.converted
import com.example.spendly.data.model.owedAmount
import com.example.spendly.data.model.spend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test
import java.time.LocalDate

class FxTest {
    private val table = mapOf("USD" to 1.0, "INR" to 90.0, "AED" to 3.6)
    private val inr = Fx("INR", table)
    private val aed = Fx("AED", table)

    private fun expense(amount: Double, currency: String, rate: Double? = null, rateCurrency: String? = null) =
        Transaction(amount = amount, currency = currency, merchant = "", category = Category.FOOD, date = LocalDate.of(2026, 11, 1), rate = rate, rateCurrency = rateCurrency)

    @Test
    fun rate_goesThroughTheTable() {
        assertEquals(25.0, inr.rate("AED")!!, 1e-9)
        assertEquals(1.0, inr.rate("INR")!!, 1e-9)
        assertEquals(0.04, aed.rate("INR")!!, 1e-9)
        assertNull(inr.rate("XYZ"))
    }

    @Test
    fun expenseInTheDisplayCurrency_isLeftAlone() {
        val t = expense(2000.0, "INR")
        assertSame(t, inr.inDisplay(t))
        assertEquals(2000.0, inr.inDisplay(t).spend, 1e-9)
    }

    @Test
    fun storedRate_isKeptWhateverTodaysRateIs() {
        // Saved when 1 AED was 22.9; today's table says 25
        val shown = inr.inDisplay(expense(150.0, "AED", rate = 22.9, rateCurrency = "INR"))
        assertEquals(150.0, shown.amount, 1e-9)
        assertEquals("AED", shown.currency)
        assertEquals(3435.0, shown.spend, 1e-9)
    }

    @Test
    fun india_uae_india_addsUpInOneCurrency() {
        val trip = listOf(
            expense(2000.0, "INR"),
            expense(150.0, "AED", rate = 22.9, rateCurrency = "INR"),
            expense(500.0, "INR")
        ).map(inr::inDisplay)
        assertEquals(5935.0, trip.sumOf { it.spend }, 1e-9)
    }

    @Test
    fun changingTheDisplayCurrency_convertsWithoutTouchingWhatWasPaid() {
        val stored = expense(150.0, "AED", rate = 22.9, rateCurrency = "INR")
        // Shown in AED, an AED expense is simply itself
        val inAed = aed.inDisplay(stored)
        assertEquals(150.0, inAed.spend, 1e-9)
        // and an INR one comes across at today's rate
        assertEquals(80.0, aed.inDisplay(expense(2000.0, "INR")).spend, 1e-9)
        // The stored row never changed, so going back to INR gives the original figure again
        assertEquals(3435.0, inr.inDisplay(stored).spend, 1e-9)
    }

    @Test
    fun rateSavedForAnotherDisplayCurrency_isCarriedAcross() {
        // 100 USD saved while totals were in INR at 88; now shown in AED (1 INR = 0.04 AED)
        val shown = aed.inDisplay(expense(100.0, "USD", rate = 88.0, rateCurrency = "INR"))
        assertEquals(100.0 * 88.0 * 0.04, shown.spend, 1e-9)
    }

    @Test
    fun oldExpenseWithNoRate_usesTheLatestOne() {
        assertEquals(3750.0, inr.inDisplay(expense(150.0, "AED")).spend, 1e-9)
    }

    @Test
    fun splitsAndOwedMoney_convertToo() {
        val split = inr.inDisplay(expense(300.0, "AED", rate = 20.0, rateCurrency = "INR").copy(myShare = 100.0, owed = true))
        assertEquals(2000.0, split.spend, 1e-9)
        assertEquals(4000.0, split.owedAmount, 1e-9)
        assertEquals(6000.0, split.converted, 1e-9)
    }

    @Test
    fun manySmallAmounts_addUpWithoutRoundingEachOne() {
        // 1000 expenses of AED 0.33 at 22.915: rounding each to paise first would drift
        val total = List(1000) { inr.inDisplay(expense(0.33, "AED", rate = 22.915, rateCurrency = "INR")) }.sumOf { it.spend }
        assertEquals(7561.95, total, 0.005)
    }
}
