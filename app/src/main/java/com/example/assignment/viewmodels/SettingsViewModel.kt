package com.example.assignment.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.assignment.api.RoomApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val roomApi: RoomApi,
    private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("is_dark_mode", false)
        val currency = sharedPref.getString("currency", "USD") ?: "USD"
        _uiState.update { it.copy(isDarkMode = isDarkMode, currency = currency) }
    }

    fun toggleDarkMode() {
        _uiState.update { currentState ->
            val newDarkMode = !currentState.isDarkMode
            val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            sharedPref.edit().putBoolean("is_dark_mode", newDarkMode).apply()
            currentState.copy(isDarkMode = newDarkMode)
        }
    }

    fun toggleNotifications() {
        _uiState.update { currentState ->
            currentState.copy(notificationsEnabled = !currentState.notificationsEnabled)
        }
    }

    fun showCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = true) }
    }

    fun hideCurrencyDialog() {
        _uiState.update { it.copy(showCurrencyDialog = false) }
    }

    fun setCurrency(currency: String) {
        _uiState.update { currentState ->
            val sharedPref = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            sharedPref.edit().putString("currency", currency).apply()
            currentState.copy(
                currency = currency,
                showCurrencyDialog = false
            )
        }
    }

    fun showClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = true) }
    }

    fun hideClearDataDialog() {
        _uiState.update { it.copy(showClearDataDialog = false) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            try {
                roomApi.clearAllTransactions()
                roomApi.deleteAllSavingsGoals()
                hideClearDataDialog()
            } catch (e: Exception) {
            }
        }
    }

    fun showExportDialog() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun hideExportDialog() {
        _uiState.update { it.copy(showExportDialog = false) }
    }

    fun showAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = true) }
    }

    fun hideAboutDialog() {
        _uiState.update { it.copy(showAboutDialog = false) }
    }

    fun exportData(format: String) {
        hideExportDialog()
    }
} 