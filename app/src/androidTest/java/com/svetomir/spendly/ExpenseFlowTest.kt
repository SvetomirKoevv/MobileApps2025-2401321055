package com.svetomir.spendly

import androidx.annotation.StringRes
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for the main user flow, running on a device with a fresh in-memory database
 * (see TestDatabaseModule). Compose test APIs find and click elements; Espresso handles
 * the system Back button.
 */
@HiltAndroidTest
class ExpenseFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun text(@StringRes id: Int): String = composeRule.activity.getString(id)

    private fun addExpense(title: String, amount: String) {
        composeRule.onNodeWithContentDescription(text(R.string.add_expense)).performClick()
        composeRule.onNodeWithText(text(R.string.title_label)).performTextInput(title)
        composeRule.onNodeWithText(text(R.string.amount_label)).performTextInput(amount)
        composeRule.onNodeWithText(text(R.string.save)).performClick()
        composeRule.waitForIdle()
    }

    /** Scrolls the vertical expense list (not the horizontal chip rows) and opens [title]. */
    private fun scrollToAndClick(title: String) {
        val verticalList = hasScrollToNodeAction() and
            SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        composeRule.onNode(verticalList).performScrollToNode(hasText(title))
        composeRule.onNodeWithText(title).performClick()
    }

    @Test
    fun startsWithEmptyState() {
        composeRule.onNodeWithText(text(R.string.empty_title)).assertIsDisplayed()
    }

    @Test
    fun addedExpense_appearsInListWithTotal() {
        addExpense(title = "Coffee", amount = "3.50")

        composeRule.onNodeWithText("Coffee").assertExists()
        composeRule.onNodeWithText(text(R.string.total_spent)).assertIsDisplayed()
    }

    @Test
    fun saveWithEmptyTitle_showsErrorAndStaysOnForm() {
        composeRule.onNodeWithContentDescription(text(R.string.add_expense)).performClick()
        composeRule.onNodeWithText(text(R.string.amount_label)).performTextInput("5")

        composeRule.onNodeWithText(text(R.string.save)).performClick()

        composeRule.onNodeWithText(text(R.string.title_error)).assertIsDisplayed()
    }

    @Test
    fun openDetails_thenEspressoBack_returnsToList() {
        addExpense(title = "Taxi", amount = "12")

        scrollToAndClick("Taxi")
        composeRule.onNodeWithText(text(R.string.expense_details)).assertIsDisplayed()

        Espresso.pressBack()

        composeRule.onNodeWithText(text(R.string.total_spent)).assertIsDisplayed()
    }

    @Test
    fun deleteExpense_withConfirmation_removesIt() {
        addExpense(title = "Lunch", amount = "8")
        scrollToAndClick("Lunch")

        composeRule.onNodeWithContentDescription(text(R.string.delete)).performClick()
        composeRule.onNodeWithText(text(R.string.delete)).performClick()

        composeRule.onNodeWithText(text(R.string.empty_title)).assertIsDisplayed()
    }

    @Test
    fun search_hidesNonMatchingExpenses() {
        addExpense(title = "Coffee", amount = "3")
        addExpense(title = "Taxi", amount = "12")

        composeRule.onNodeWithText(text(R.string.search_hint)).performTextInput("cof")

        composeRule.onNodeWithText("Coffee").assertExists()
        composeRule.onNodeWithText("Taxi").assertDoesNotExist()
    }
}
