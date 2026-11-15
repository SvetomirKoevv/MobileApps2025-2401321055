package com.svetomir.spendly.ui.detail

import com.svetomir.spendly.data.local.Category
import com.svetomir.spendly.data.local.Expense
import java.util.Locale
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ShareExpenseTest {

    private lateinit var originalLocale: Locale

    // 15 Nov 2026 12:00 UTC is the same calendar day in every European time zone
    private val coffee = Expense(
        title = "Coffee",
        amount = 3.5,
        category = Category.FOOD,
        date = 1_794_744_000_000L,
    )

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
    fun buildShareText_withoutNote_hasTwoLines() {
        assertEquals(
            "Coffee: 3.50 €\nFood · 15 Nov 2026",
            buildShareText(coffee),
        )
    }

    @Test
    fun buildShareText_withNote_addsNoteLine() {
        assertEquals(
            "Coffee: 3.50 €\nFood · 15 Nov 2026\nNote: with milk",
            buildShareText(coffee.copy(note = "with milk")),
        )
    }

    @Test
    fun buildShareText_blankNote_isLeftOut() {
        assertEquals(
            "Coffee: 3.50 €\nFood · 15 Nov 2026",
            buildShareText(coffee.copy(note = "   ")),
        )
    }
}
