package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.assignment.viewmodels.SettingsViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Appearance Settings Card
        SettingsCard(
            title = "Appearance Settings",
            icon = Icons.Default.Palette
        ) {
            SettingsItem(
                title = "Dark Mode",
                icon = Icons.Default.DarkMode,
                action = {
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Settings Card
        SettingsCard(
            title = "Notification Settings",
            icon = Icons.Default.Notifications
        ) {
            SettingsItem(
                title = "Enable Notifications",
                icon = Icons.Default.NotificationsActive,
                action = {
                    Switch(
                        checked = uiState.notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Currency Settings Card
        SettingsCard(
            title = "Currency Settings",
            icon = Icons.Default.AttachMoney
        ) {
            SettingsItem(
                title = "Current Currency",
                subtitle = uiState.currency,
                icon = Icons.Default.CurrencyExchange,
                action = {
                    Button(
                        onClick = { viewModel.showCurrencyDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Change")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Data Management Card
        SettingsCard(
            title = "Data Management",
            icon = Icons.Default.Storage
        ) {
            SettingsItem(
                title = "Clear All Data",
                icon = Icons.Default.Delete,
                action = {
                    Button(
                        onClick = { viewModel.showClearDataDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Clear")
                    }
                }
            )
            SettingsItem(
                title = "Export Data",
                icon = Icons.Default.FileDownload,
                action = {
                    Button(
                        onClick = { viewModel.showExportDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Export")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About Card
        SettingsCard(
            title = "About",
            icon = Icons.Default.Info
        ) {
            SettingsItem(
                title = "App Information",
                icon = Icons.Default.Apps,
                action = {
                    Button(
                        onClick = { viewModel.showAboutDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("View")
                    }
                }
            )
        }
    }

    if (uiState.showCurrencyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCurrencyDialog() },
            title = { Text("Select Currency") },
            text = {
                Column {
                    listOf("USD", "EUR", "GBP", "CNY", "JPY").forEach { currency ->
                        TextButton(
                            onClick = {
                                viewModel.setCurrency(currency)
                            }
                        ) {
                            Text(currency)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideCurrencyDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataDialog() },
            title = { Text("Confirm Clear") },
            text = { Text("Are you sure you want to clear all data? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllData()
                        viewModel.hideClearDataDialog()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showExportDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideExportDialog() },
            title = { Text("Export Data") },
            text = {
                Column {
                    listOf("CSV", "PDF", "JSON").forEach { format ->
                        TextButton(
                            onClick = {
                                viewModel.exportData(format)
                                viewModel.hideExportDialog()
                            }
                        ) {
                            Text(format)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideExportDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showAboutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAboutDialog() },
            title = { Text("About") },
            text = {
                Column {
                    Text("Version: 1.0.0")
                    Text("Developer: Diyuyang")
                    Text("Copyright © 2025")
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideAboutDialog() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        action()
    }
} 