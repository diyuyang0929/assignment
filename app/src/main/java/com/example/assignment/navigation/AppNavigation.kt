package com.example.assignment.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.assignment.screens.HomeScreen
import com.example.assignment.screens.SavingsScreen
import com.example.assignment.screens.SettingsScreen
import androidx.compose.foundation.layout.padding
import com.example.assignment.api.SavingsApi
import com.example.assignment.viewmodels.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val savingsApi = remember { SavingsApi() }
    var selectedItem by remember { mutableStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Financial Overview") },
                    label = { Text("Financial Overview") },
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Savings, contentDescription = "Savings Goals") },
                    label = { Text("Savings Goals") },
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("savings") {
                            popUpTo("savings") { inclusive = true }
                        }
                    }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("settings") {
                            popUpTo("settings") { inclusive = true }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(savingsApi, context)
                )
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToSavings = { navController.navigate("savings") }
                )
            }
            composable("savings") {
                SavingsScreen(
                    savingsApi = savingsApi
                )
            }
            composable("settings") {
                SettingsScreen()
            }
        }
    }
}