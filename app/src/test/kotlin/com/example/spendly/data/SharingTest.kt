package com.example.spendly.data

import com.example.spendly.data.model.Category
import com.example.spendly.data.model.Transaction
import com.example.spendly.data.model.owedAmount
import com.example.spendly.data.model.sharing
import com.example.spendly.data.model.spend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** What each answer to "Who is this for?" counts as spending and as owed. */
class SharingTest {

    private fun expense(paidBack: Boolean = false, owed: Boolean = false, myShare: Double? = null) = Transaction(
        amount = 1500.0, merchant = "", category = Category.FOOD, date = LocalDate.of(2026, 11, 13),
        paidBack = paidBack, owed = owed, myShare = myShare
    )

    @Test
    fun justMe_countsAllAndOwesNothing() {
        assertEquals(1500.0, expense().spend, 0.001)
        assertEquals(0.0, expense().owedAmount, 0.001)
    }

    @Test
    fun owed_countsAllUntilPaid() {
        val owed = expense(owed = true)
        assertEquals(1500.0, owed.spend, 0.001)
        assertEquals(1500.0, owed.owedAmount, 0.001)

        val paid = owed.copy(paidBack = owed.sharing.settled().paidBack, owed = false)
        assertEquals(0.0, paid.spend, 0.001)
        assertEquals(0.0, paid.owedAmount, 0.001)
    }

    @Test
    fun split_countsOnlyMyShare_beforeAndAfterPayment() {
        val split = expense(owed = true, myShare = 400.0)
        assertEquals(400.0, split.spend, 0.001)
        assertEquals(1100.0, split.owedAmount, 0.001)

        val settled = split.sharing.settled()
        assertFalse(settled.owed)
        assertFalse("A settled split keeps its share, it isn't paid back", settled.paidBack)
        val after = split.copy(owed = settled.owed, paidBack = settled.paidBack)
        assertEquals(400.0, after.spend, 0.001)
        assertEquals(0.0, after.owedAmount, 0.001)
    }

    @Test
    fun paidBack_countsNothing() {
        assertEquals(0.0, expense(paidBack = true).spend, 0.001)
        assertEquals(0.0, expense(paidBack = true).owedAmount, 0.001)
    }

    @Test
    fun aShareAboveTheAmountNeverCountsMoreThanTheAmount() {
        assertEquals(1500.0, expense(myShare = 9000.0).spend, 0.001)
    }

    @Test
    fun justMe_isTheDefault() {
        assertTrue(expense().sharing.isJustMe)
        assertFalse(expense(owed = true).sharing.isJustMe)
    }
}
