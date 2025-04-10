package com.example.assignment.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.assignment.data.Transaction
import com.example.assignment.data.TransactionType
import com.example.assignment.data.SavingsGoal
import com.example.assignment.api.SavingsApi
import com.example.assignment.api.RoomApi
import com.example.assignment.utils.SavingsMethod
import com.example.assignment.utils.SavingsTip
import com.example.assignment.utils.SpendingAnalyzer
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val savingsGoals: List<SavingsGoal> = emptyList(),
    val showAddIncomeDialog: Boolean = false,
    val showAddExpenseDialog: Boolean = false,
    val showAddToSavingsDialog: Boolean = false,
    val showShareDialog: Boolean = false,
    val showSavingsAdviceDialog: Boolean = false,
    val showStartNewMonthDialog: Boolean = false,
    val savingsTips: List<SavingsTip> = emptyList(),
    val recommendedSavingsMethod: SavingsMethod? = null,
    val isLoadingAdvice: Boolean = false,
    val networkError: String? = null,
    val currency: String = "USD"
)

class HomeViewModel(
    private val savingsApi: SavingsApi,
    private val roomApi: RoomApi,
    private val settingsViewModel: SettingsViewModel
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val spendingAnalyzer = SpendingAnalyzer()

    val transactions: StateFlow<List<Transaction>> = roomApi.transactions
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalIncome: StateFlow<Float> = transactions
        .map { transactions ->
            transactions.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount.toDouble() }.toFloat()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val totalExpense: StateFlow<Float> = transactions
        .map { transactions ->
            transactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount.toDouble() }.toFloat()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val remainingAmount: StateFlow<Float> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val expenseCategories: StateFlow<List<Pair<String, Float>>> = transactions
        .map { transactions ->
            transactions.filter { it.type == TransactionType.EXPENSE && !it.description.startsWith("Monthly savings") && !it.description.startsWith("Saved to") }
                .groupBy { it.description }
                .mapValues { it.value.sumOf { transaction -> transaction.amount.toDouble() }.toFloat() }
                .toList()
                .sortedByDescending { it.second }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            savingsApi.savingsGoals.collect { goals ->
                _uiState.update { it.copy(savingsGoals = goals) }
            }
        }

        viewModelScope.launch {
            transactions.collect { transactions ->
                _uiState.update { it.copy(transactions = transactions) }
            }
        }

        viewModelScope.launch {
            combine(transactions, totalIncome, totalExpense) { trans, income, expense ->
                Triple(trans, income, expense)
            }.collect { (trans, income, expense) ->
                updateSavingsAdvice(trans, income, expense)
            }
        }

        // Listen to currency changes from SettingsViewModel
        settingsViewModel.uiState
            .map { it.currency }
            .onEach { currency ->
                _uiState.update { it.copy(currency = currency) }
            }
            .launchIn(viewModelScope)
    }

    private fun updateSavingsAdvice(
        transactions: List<Transaction>,
        income: Float,
        expense: Float
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoadingAdvice = true, networkError = null) }

                val tips = spendingAnalyzer.analyzeAndGenerateSavingsTips(transactions, income, expense)
                val method = spendingAnalyzer.recommendSavingsMethod(transactions, income, expense)

                _uiState.update {
                    it.copy(
                        savingsTips = tips,
                        recommendedSavingsMethod = method,
                        isLoadingAdvice = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        networkError = "Failed to fetch financial advice: ${e.message}",
                        isLoadingAdvice = false
                    )
                }
            }
        }
    }

    fun refreshFinancialAdvice() {
        val currentTransactions = transactions.value
        val currentIncome = totalIncome.value
        val currentExpense = totalExpense.value

        updateSavingsAdvice(currentTransactions, currentIncome, currentExpense)
    }

    fun addTransaction(amount: Float, description: String, type: TransactionType) {
        val newTransaction = Transaction(
            id = System.currentTimeMillis().toString(),
            amount = amount,
            description = description,
            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
            type = type
        )
        viewModelScope.launch {
            roomApi.addTransaction(newTransaction)
        }
    }

    fun addToSavings(goal: SavingsGoal, amount: Float) {
        viewModelScope.launch {
            savingsApi.addSaving(goal.id, amount)
                .onSuccess {
                    addTransaction(
                        amount = amount,
                        description = "Saved to ${goal.name}",
                        type = TransactionType.EXPENSE
                    )
                }
        }
    }

    fun addAllRemainingToSavings(goal: SavingsGoal) {
        viewModelScope.launch {
            val remaining = remainingAmount.value
            if (remaining > 0) {
                savingsApi.addSaving(goal.id, remaining)
                    .onSuccess {
                        addTransaction(
                            amount = remaining,
                            description = "All remaining saved to ${goal.name}",
                            type = TransactionType.EXPENSE
                        )
                    }
            }
        }
    }

    fun showAddIncomeDialog() {
        _uiState.update { it.copy(showAddIncomeDialog = true) }
    }

    fun hideAddIncomeDialog() {
        _uiState.update { it.copy(showAddIncomeDialog = false) }
    }

    fun showAddExpenseDialog() {
        _uiState.update { it.copy(showAddExpenseDialog = true) }
    }

    fun hideAddExpenseDialog() {
        _uiState.update { it.copy(showAddExpenseDialog = false) }
    }

    fun showAddToSavingsDialog() {
        _uiState.update { it.copy(showAddToSavingsDialog = true) }
    }

    fun hideAddToSavingsDialog() {
        _uiState.update { it.copy(showAddToSavingsDialog = false) }
    }

    fun showShareDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    fun hideShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }

    fun showSavingsAdviceDialog() {
        _uiState.update { it.copy(showSavingsAdviceDialog = true) }
    }

    fun hideSavingsAdviceDialog() {
        _uiState.update { it.copy(showSavingsAdviceDialog = false) }
    }

    fun showStartNewMonthDialog() {
        _uiState.update { it.copy(showStartNewMonthDialog = true) }
    }

    fun hideStartNewMonthDialog() {
        _uiState.update { it.copy(showStartNewMonthDialog = false) }
    }

    fun startNewMonth() {
        viewModelScope.launch {
            try {
                val remaining = remainingAmount.value
                if (remaining > 0) {
                    val firstGoal = uiState.value.savingsGoals.firstOrNull()
                    if (firstGoal != null) {
                        val transaction = Transaction(
                            id = System.currentTimeMillis().toString(),
                            amount = remaining,
                            description = "Monthly savings (${java.time.YearMonth.now().minusMonths(1)})",
                            date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                            type = TransactionType.EXPENSE
                        )
                        roomApi.addTransaction(transaction)
                        
                        savingsApi.addSaving(firstGoal.id, remaining).getOrThrow()
                    }
                }
                
                kotlinx.coroutines.delay(100)
                
                roomApi.clearAllTransactions()
                
                hideStartNewMonthDialog()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(networkError = "Failed to start new month: ${e.message}")
                }
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            roomApi.deleteTransaction(transaction)
        }
    }

    fun clearNetworkError() {
        _uiState.update { it.copy(networkError = null) }
    }

    class Factory(
        private val savingsApi: SavingsApi,
        private val context: Context,
        private val settingsViewModel: SettingsViewModel
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(savingsApi, RoomApi(context), settingsViewModel) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
