package com.svetomir.spendly.ui.util

import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FormattersTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun formatAmount_alwaysShowsTwoDecimals() {
        assertEquals("3.50 €", formatAmount(3.5))
        assertEquals("0.00 €", formatAmount(0.0))
    }

    @Test
    fun formatAmount_roundsToCents() {
        assertEquals("1234.57 €", formatAmount(1234.567))
    }

    @Test
    fun formatDate_showsDayMonthYear() {
        // 15 Nov 2026 12:00 UTC is the same calendar day in every European time zone
        assertEquals("15 Nov 2026", formatDate(1_794_744_000_000L))
    }
}
