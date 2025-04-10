package com.example.assignment.viewmodels

data class SettingsUiState(
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val currency: String = "USD",
    val showCurrencyDialog: Boolean = false,
    val showClearDataDialog: Boolean = false,
    val showExportDialog: Boolean = false,
    val showAboutDialog: Boolean = false
) 