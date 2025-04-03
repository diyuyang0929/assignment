package com.example.assignment

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.example.assignment.data.room.AppDatabase
import com.example.assignment.data.room.TransactionEntity
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import java.io.IOException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var db: AppDatabase

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = AppDatabase.getDatabase(context)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.assignment", appContext.packageName)
    }

    @Test
    fun insertAndReadTransaction() = runBlocking {
        val transaction = TransactionEntity(
            id = "1",
            amount = 100.0f,
            description = "Test Transaction",
            type = "INCOME",
            date = System.currentTimeMillis().toString()
        )

        db.transactionDao().insertTransaction(transaction)
        val allTransactions = db.transactionDao().getAllTransactions().first()
        assertTrue(allTransactions.isNotEmpty())
        assertEquals(transaction.amount, allTransactions[0].amount)
        assertEquals(transaction.description, allTransactions[0].description)
        assertEquals(transaction.type, allTransactions[0].type)
    }

    @Test
    fun deleteTransaction() = runBlocking {
        val transaction = TransactionEntity(
            id = "1",
            amount = 100.0f,
            description = "Test Transaction",
            type = "INCOME",
            date = System.currentTimeMillis().toString()
        )

        db.transactionDao().insertTransaction(transaction)
        db.transactionDao().deleteTransaction(transaction)
        val allTransactions = db.transactionDao().getAllTransactions().first()
        assertTrue(allTransactions.isEmpty())
    }

    @Test
    fun testHomeScreenNavigation() {
        composeTestRule.onNodeWithText("Financial Overview").assertExists()

        composeTestRule.onNodeWithText("Savings Goals").performClick()
        composeTestRule.onNodeWithText("Savings Goals").assertExists()

        composeTestRule.onNodeWithText("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSavingsScreen() {
        composeTestRule.onNodeWithText("Savings Goals").performClick()

        composeTestRule.onNodeWithContentDescription("Add Savings Goal").assertExists()

        composeTestRule.onNodeWithContentDescription("Share").assertExists()
    }

    @Test
    fun testSettingsScreen() {
        composeTestRule.onNodeWithText("Settings").performClick()

        composeTestRule.onNodeWithText("Settings").assertExists()
    }

    @Test
    fun testSavingsAdvice() {
        composeTestRule.onNodeWithContentDescription("Savings Advice").performClick()

        composeTestRule.onNodeWithText("Personalized Savings Advice").assertExists()

        composeTestRule.onNodeWithContentDescription("Refresh Advice").assertExists()
    }

    @Test
    fun testShareFunctionality() {
        composeTestRule.onNodeWithContentDescription("Share").performClick()

        composeTestRule.onNodeWithText("Share Financial Overview").assertExists()

        composeTestRule.onNodeWithText("WhatsApp").assertExists()
        composeTestRule.onNodeWithText("Email").assertExists()
        composeTestRule.onNodeWithText("Other Applications").assertExists()
    }

    @Test
    fun testAddSavingsGoal() {
        composeTestRule.onNodeWithText("Savings Goals").performClick()

        composeTestRule.onNodeWithContentDescription("Add Savings Goal").performClick()

        composeTestRule.onNodeWithText("Add Savings Goal").assertExists()

        composeTestRule.onNodeWithText("Target Amount").assertExists()
        composeTestRule.onNodeWithText("Current Amount").assertExists()
    }
}