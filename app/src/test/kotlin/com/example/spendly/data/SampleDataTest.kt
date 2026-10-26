package com.example.spendly.data

import com.example.spendly.data.model.TransactionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SampleDataTest {

    @Test
    fun sampleTransactions_isNotEmpty() {
        assertTrue(SampleData.transactions.isNotEmpty())
    }

    @Test
    fun monthlyTotal_matchesSumOfTransactions() {
        val expected = SampleData.transactions.sumOf { it.amount }
        assertEquals(expected, SampleData.monthlyTotal, 0.001)
    }

    @Test
    fun reviewCount_matchesUnconfirmedTransactions() {
        val expected = SampleData.transactions.count { it.status == TransactionStatus.NEEDS_REVIEW }
        assertEquals(expected, SampleData.reviewCount)
    }

    @Test
    fun weeklySpending_hasSevenDays() {
        assertEquals(7, SampleData.weeklySpending.size)
        assertEquals(7, SampleData.weekDayLabels.size)
    }
}
