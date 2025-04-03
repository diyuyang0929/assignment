package com.example.assignment.utils

import com.example.assignment.data.Transaction
import com.example.assignment.data.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class SpendingAnalyzer {
    private val FINANCIAL_TIPS_API = "https://api.financialmodelingprep.com/api/v3/financial-tips"
    private val MARKET_TRENDS_API = "https://api.financialmodelingprep.com/api/v3/market-trends"
    private val SAVINGS_RATES_API = "https://api.financialmodelingprep.com/api/v3/savings-rates"
    private val API_KEY = "demo"

    suspend fun analyzeAndGenerateSavingsTips(
        transactions: List<Transaction>,
        totalIncome: Float,
        totalExpense: Float
    ): List<SavingsTip> = withContext(Dispatchers.IO) {
        val tips = mutableListOf<SavingsTip>()

        tips.addAll(performLocalAnalysis(transactions, totalIncome, totalExpense))

        try {
            val onlineTips = fetchFinancialTips()
            tips.addAll(onlineTips)
        } catch (e: Exception) {
            tips.add(SavingsTip(
                title = "Network Error",
                description = "Unable to fetch online financial tips. Check your internet connection for more personalized advice.",
                savingsImpact = "Low",
                actionable = false,
                source = "Local"
            ))
        }

        return@withContext tips
    }

    suspend fun recommendSavingsMethod(
        transactions: List<Transaction>,
        totalIncome: Float,
        totalExpense: Float
    ): SavingsMethod = withContext(Dispatchers.IO) {
        val remainingAmount = totalIncome - totalExpense
        val savingsPotential = if (totalIncome > 0) remainingAmount / totalIncome else 0f

        val localRecommendation = getLocalSavingsRecommendation(transactions, savingsPotential)

        try {
            val marketTrends = fetchMarketTrends()
            val savingsRates = fetchSavingsRates()

            return@withContext enhanceRecommendationWithMarketData(
                localRecommendation,
                marketTrends,
                savingsRates,
                savingsPotential
            )
        } catch (e: Exception) {
            return@withContext localRecommendation
        }
    }

    private fun performLocalAnalysis(
        transactions: List<Transaction>,
        totalIncome: Float,
        totalExpense: Float
    ): List<SavingsTip> {
        val tips = mutableListOf<SavingsTip>()

        val incomeExpenseRatio = if (totalExpense > 0) totalIncome / totalExpense else 0f

        val categoryExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.description }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.toDouble() }.toFloat()
            }

        val categoryPercentages = categoryExpenses.mapValues { (_, amount) ->
            if (totalExpense > 0) (amount / totalExpense) * 100 else 0f
        }

        val topExpenseCategories = categoryPercentages.entries
            .sortedByDescending { it.value }
            .take(3)

        if (incomeExpenseRatio < 1.1f) {
            tips.add(SavingsTip(
                title = "Low Income-Expense Ratio",
                description = "Your income is only slightly higher than your expenses. Consider controlling spending or increasing income sources.",
                savingsImpact = "High",
                actionable = true,
                source = "Local"
            ))
        } else if (incomeExpenseRatio > 2.0f) {
            tips.add(SavingsTip(
                title = "High Savings Potential",
                description = "Your income is much higher than your expenses, indicating high savings potential. Consider putting excess funds into savings or investments.",
                savingsImpact = "High",
                actionable = true,
                source = "Local"
            ))
        }

        topExpenseCategories.forEach { (category, percentage) ->
            if (percentage > 30) {
                tips.add(SavingsTip(
                    title = "Reduce ${category} Spending",
                    description = "${category} accounts for ${String.format("%.1f", percentage)}% of your total expenses, which is relatively high. Consider reducing spending in this area.",
                    savingsImpact = "Medium",
                    actionable = true,
                    source = "Local"
                ))
            }
        }

        return tips
    }

    private fun getLocalSavingsRecommendation(
        transactions: List<Transaction>,
        savingsPotential: Float
    ): SavingsMethod {
        val monthlyExpenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.date.substring(0, 7) }
            .mapValues { entry ->
                entry.value.sumOf { it.amount.toDouble() }.toFloat()
            }

        val expenseStability = if (monthlyExpenses.size > 1) {
            val values = monthlyExpenses.values.toList()
            val mean = values.average()
            val variance = values.map { (it - mean) * (it - mean) }.average()
            val stdDev = Math.sqrt(variance)
            stdDev / mean
        } else {
            1.0
        }

        return when {
            savingsPotential > 0.3 && expenseStability < 0.2 -> {
                SavingsMethod(
                    title = "Regular Savings Plan",
                    description = "Your income is stable with high savings potential. Consider setting up automatic transfers to save ${(savingsPotential * 100).toInt()}% of your income regularly.",
                    suitabilityScore = 9,
                    source = "Local Analysis"
                )
            }
            savingsPotential > 0.1 && expenseStability < 0.5 -> {
                SavingsMethod(
                    title = "Flexible Savings Plan",
                    description = "Your spending pattern has some fluctuations. Consider saving a small portion (about ${(savingsPotential * 100).toInt()}%) immediately after receiving income, and adjust the rest based on monthly circumstances.",
                    suitabilityScore = 8,
                    source = "Local Analysis"
                )
            }
            else -> {
                SavingsMethod(
                    title = "Small Change Savings Method",
                    description = "Your spending pattern is highly variable or your savings space is limited. Consider using the small change savings method, saving spare change or a fixed small amount (like $5) after each purchase.",
                    suitabilityScore = 7,
                    source = "Local Analysis"
                )
            }
        }
    }

    private suspend fun fetchFinancialTips(): List<SavingsTip> = withContext(Dispatchers.IO) {
        val url = URL("$FINANCIAL_TIPS_API?apikey=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                return@withContext parseFinancialTips(response)
            } else {
                throw Exception("API request failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchMarketTrends(): JSONObject = withContext(Dispatchers.IO) {
        val url = URL("$MARKET_TRENDS_API?apikey=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                return@withContext JSONObject(response)
            } else {
                throw Exception("API request failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private suspend fun fetchSavingsRates(): JSONObject = withContext(Dispatchers.IO) {
        val url = URL("$SAVINGS_RATES_API?apikey=$API_KEY")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        try {
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                return@withContext JSONObject(response)
            } else {
                throw Exception("API request failed with response code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection): String {
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        val response = StringBuilder()
        var line: String?

        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()

        return response.toString()
    }

    private fun parseFinancialTips(response: String): List<SavingsTip> {
        val tips = mutableListOf<SavingsTip>()

        try {
            val jsonArray = JSONArray(response)

            for (i in 0 until jsonArray.length()) {
                val tipObject = jsonArray.getJSONObject(i)

                val tip = SavingsTip(
                    title = tipObject.getString("title"),
                    description = tipObject.getString("description"),
                    savingsImpact = tipObject.getString("impact"),
                    actionable = tipObject.getBoolean("actionable"),
                    source = "Financial API"
                )

                tips.add(tip)
            }
        } catch (e: Exception) {
            return getSimulatedOnlineTips()
        }

        return tips
    }

    private fun enhanceRecommendationWithMarketData(
        localRecommendation: SavingsMethod,
        marketTrends: JSONObject,
        savingsRates: JSONObject,
        savingsPotential: Float
    ): SavingsMethod {
        try {
            val interestRateTrend = marketTrends.optString("interestRateTrend", "stable")
            val stockMarketTrend = marketTrends.optString("stockMarketTrend", "stable")
            val inflationRate = marketTrends.optDouble("inflationRate", 2.0)


            val highYieldSavingsRate = savingsRates.optDouble("highYieldSavingsRate", 0.5)
            val cdRate = savingsRates.optDouble("cdRate", 1.0)
            val treasuryBondRate = savingsRates.optDouble("treasuryBondRate", 1.5)

            return when {
                inflationRate > 4.0 -> {
                    SavingsMethod(
                        title = "Inflation-Protected Investment Strategy",
                        description = "Current inflation rate is high at ${String.format("%.1f", inflationRate)}%. " +
                                "Consider allocating some savings to inflation-protected securities (TIPS) or " +
                                "other assets that typically perform well during inflation.",
                        suitabilityScore = 9,
                        source = "Market Analysis"
                    )
                }

                interestRateTrend == "rising" && savingsPotential > 0.2 -> {
                    SavingsMethod(
                        title = "High-Yield Savings Strategy",
                        description = "Interest rates are rising. Current high-yield savings accounts offer " +
                                "around ${String.format("%.2f", highYieldSavingsRate)}% APY. " +
                                "Consider allocating funds to high-yield savings accounts or CDs with " +
                                "${String.format("%.2f", cdRate)}% rates for safe returns.",
                        suitabilityScore = 8,
                        source = "Market Analysis"
                    )
                }

                stockMarketTrend in listOf("stable", "bullish") && savingsPotential > 0.15 -> {
                    SavingsMethod(
                        title = "Balanced Investment Strategy",
                        description = "The stock market is currently ${stockMarketTrend}. " +
                                "Consider a balanced approach: keep emergency funds in high-yield savings " +
                                "(${String.format("%.2f", highYieldSavingsRate)}% APY) and invest remaining " +
                                "savings in a diversified portfolio for long-term growth.",
                        suitabilityScore = 9,
                        source = "Market Analysis"
                    )
                }

                else -> {
                    SavingsMethod(
                        title = localRecommendation.title,
                        description = "${localRecommendation.description} Current high-yield savings " +
                                "accounts offer around ${String.format("%.2f", highYieldSavingsRate)}% APY.",
                        suitabilityScore = localRecommendation.suitabilityScore,
                        source = "Combined Analysis"
                    )
                }
            }
        } catch (e: Exception) {
            return localRecommendation
        }
    }

    private fun getSimulatedOnlineTips(): List<SavingsTip> {
        return listOf(
            SavingsTip(
                title = "Emergency Fund First",
                description = "Financial experts recommend building an emergency fund covering 3-6 months of expenses before focusing on other financial goals.",
                savingsImpact = "High",
                actionable = true,
                source = "Financial Experts"
            ),
            SavingsTip(
                title = "Debt Snowball Method",
                description = "Pay off your smallest debts first to build momentum, then tackle larger debts. This psychological win can help maintain motivation.",
                savingsImpact = "Medium",
                actionable = true,
                source = "Financial Experts"
            ),
            SavingsTip(
                title = "Current Market Trend: Inflation Concerns",
                description = "With current inflation trends, consider allocating some savings to inflation-protected securities or assets that typically perform well during inflation.",
                savingsImpact = "Medium",
                actionable = true,
                source = "Market Analysis"
            ),
            SavingsTip(
                title = "High-Yield Savings Accounts",
                description = "Current high-yield savings accounts are offering competitive rates around 4-5% APY, significantly higher than traditional savings accounts.",
                savingsImpact = "Medium",
                actionable = true,
                source = "Market Analysis"
            )
        )
    }
}

data class SavingsTip(
    val title: String,
    val description: String,
    val savingsImpact: String,
    val actionable: Boolean,
    val source: String
)

data class SavingsMethod(
    val title: String,
    val description: String,
    val suitabilityScore: Int,
    val source: String
)