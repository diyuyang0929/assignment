package com.example.assignment

import com.example.assignment.data.Transaction
import com.example.assignment.data.TransactionType
import com.example.assignment.data.SavingsGoal
import com.example.assignment.utils.SpendingAnalyzer
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.runBlocking

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testSavingsGoalProgress() {
        val goal = SavingsGoal(
            id = "1",
            name = "Test Goal",
            targetAmount = 1000f,
            currentAmount = 500f
        )
        
        assertEquals(50f, goal.currentAmount / goal.targetAmount * 100)
    }

    @Test
    fun testTransactionTypeConversion() {
        val incomeTransaction = Transaction(
            id = "1",
            amount = 100f,
            description = "Salary",
            date = dateFormat.format(Date()),
            type = TransactionType.INCOME
        )
        
        val expenseTransaction = Transaction(
            id = "2",
            amount = 50f,
            description = "Food",
            date = dateFormat.format(Date()),
            type = TransactionType.EXPENSE
        )

        assertEquals(TransactionType.INCOME, incomeTransaction.type)
        assertEquals(TransactionType.EXPENSE, expenseTransaction.type)
    }

    @Test
    fun testTransactionAmountCalculation() {
        val transactions = listOf(
            Transaction(
                id = "1",
                amount = 1000f,
                description = "Salary",
                date = dateFormat.format(Date()),
                type = TransactionType.INCOME
            ),
            Transaction(
                id = "2",
                amount = 300f,
                description = "Rent",
                date = dateFormat.format(Date()),
                type = TransactionType.EXPENSE
            ),
            Transaction(
                id = "3",
                amount = 200f,
                description = "Food",
                date = dateFormat.format(Date()),
                type = TransactionType.EXPENSE
            )
        )

        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount.toDouble() }
            .toFloat()

        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount.toDouble() }
            .toFloat()

        assertEquals(1000f, totalIncome)
        assertEquals(500f, totalExpense)
        assertEquals(500f, totalIncome - totalExpense)
    }

    @Test
    fun testSpendingAnalyzer() = runBlocking {
        val analyzer = SpendingAnalyzer()
        val transactions = listOf(
            Transaction(
                id = "1",
                amount = 10000f,
                description = "Salary",
                date = dateFormat.format(Date()),
                type = TransactionType.INCOME
            ),
            Transaction(
                id = "2",
                amount = 2000f,
                description = "Rent",
                date = dateFormat.format(Date()),
                type = TransactionType.EXPENSE
            ),
            Transaction(
                id = "3",
                amount = 1000f,
                description = "Food",
                date = dateFormat.format(Date()),
                type = TransactionType.EXPENSE
            )
        )

        val tips = analyzer.analyzeAndGenerateSavingsTips(transactions, 10000f, 3000f)
        assertTrue(tips.isNotEmpty())
        assertTrue(tips.any { it.title.contains("High Savings Potential") })
    }

    @Test
    fun testSavingsGoalCompletion() {
        val goal = SavingsGoal(
            id = "1",
            name = "Test Goal",
            targetAmount = 1000f,
            currentAmount = 1000f
        )
        
        assertTrue(goal.currentAmount >= goal.targetAmount)
        assertEquals(100f, goal.currentAmount / goal.targetAmount * 100)
    }

    @Test
    fun testTransactionDateFormat() {
        val date = Date()
        val formattedDate = dateFormat.format(date)
        assertTrue(formattedDate.matches(Regex("\\d{4}-\\d{2}-\\d{2}")))
    }
}