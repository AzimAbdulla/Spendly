package com.example.spendly.ui.components

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class PeriodPickerTest {

    private lateinit var originalLocale: Locale

    @Before
    fun useUsMonthNames() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun restoreLocale() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun weekRange_offsetZero_isTheLastSevenDaysIncludingToday() {
        val today = LocalDate.of(2026, 11, 4)
        assertEquals(LocalDate.of(2026, 10, 29) to today, weekRange(0, today))
    }

    @Test
    fun weekRange_eachOffsetStepsBackOneWeek() {
        val today = LocalDate.of(2026, 11, 4)
        assertEquals(LocalDate.of(2026, 10, 22) to LocalDate.of(2026, 10, 28), weekRange(1, today))
    }

    @Test
    fun weekLabel_latestWeek_readsLastSevenDays() {
        assertEquals("Last 7 days", weekLabel(0, LocalDate.of(2026, 11, 4)))
    }

    @Test
    fun weekLabel_withinOneMonth_namesTheMonthOnce() {
        assertEquals("Oct 22 – 28", weekLabel(1, LocalDate.of(2026, 11, 4)))
    }

    @Test
    fun weekLabel_acrossMonths_namesBothMonths() {
        assertEquals("Oct 28 – Nov 3", weekLabel(1, LocalDate.of(2026, 11, 10)))
    }

    @Test
    fun weekLabel_inAnEarlierYear_addsTheYear() {
        assertEquals("Dec 21 – 27, 2025", weekLabel(1, LocalDate.of(2026, 1, 3)))
    }
}
