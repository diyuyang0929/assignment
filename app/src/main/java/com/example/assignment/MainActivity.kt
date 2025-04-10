package com.example.assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.assignment.navigation.AppNavigation
import com.example.assignment.ui.theme.AssignmentTheme
import com.example.assignment.viewmodels.SettingsViewModel
import com.example.assignment.viewmodels.SettingsViewModelFactory
import com.example.assignment.api.RoomApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(RoomApi(this), this)
            )
            val isDarkMode by settingsViewModel.uiState.collectAsState()
            
            AssignmentTheme(
                darkTheme = isDarkMode.isDarkMode
            ) {
                AppNavigation()
            }
        }
    }
}
