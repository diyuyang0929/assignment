package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.components.SavingsGoalCard
import com.example.assignment.data.SavingsGoal
import androidx.compose.foundation.clickable
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.assignment.api.SavingsApi
import com.example.assignment.api.SavingsStatistics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    savingsApi: SavingsApi
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var selectedGoal by remember { mutableStateOf<SavingsGoal?>(null) }
    var statistics by remember { mutableStateOf<SavingsStatistics?>(null) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val savingsGoals by savingsApi.savingsGoals.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        scope.launch {
            savingsApi.getSavingsStatistics().onSuccess { stats ->
                statistics = stats
            }.onFailure { e ->
                Toast.makeText(context, "Failed to load statistics: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Savings Goals", fontSize = 20.sp, fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddGoalDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Savings Goal")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Your Savings Goals",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Track your savings progress",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            statistics?.let { stats ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Overall Statistics",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Total Target Amount: $${String.format("%.2f", stats.totalTargetAmount)}")
                            Text("Total Current Amount: $${String.format("%.2f", stats.totalCurrentAmount)}")
                            Text("Average Progress: ${String.format("%.1f", stats.averageProgress)}%")
                            Text("Number of Savings Goals: ${stats.totalGoals}")
                        }
                    }
                }
            }

            items(savingsGoals) { goal ->
                SavingsGoalCard(
                    goal = goal,
                    onShare = {
                        selectedGoal = goal
                        showShareDialog = true
                    },
                    onAddAmount = { amount ->
                        scope.launch {
                            savingsApi.updateSavingsProgress(goal.id, amount)
                                .onSuccess {
                                    Toast.makeText(context, "Update successful", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { e ->
                                    Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            savingsApi.deleteSavingsGoal(goal.id)
                                .onSuccess {
                                    Toast.makeText(context, "Delete successful", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { e ->
                                    Toast.makeText(context, "Delete failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }
        }
    }

    if (showAddGoalDialog) {
        var name by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("Add Savings Goal") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Goal Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Target Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank() && amount.isNotBlank()) {
                            scope.launch {
                                savingsApi.addSavingsGoal(name, amount.toFloatOrNull() ?: 0f)
                                    .onSuccess {
                                        showAddGoalDialog = false
                                        Toast.makeText(context, "Added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure { e ->
                                        Toast.makeText(context, "Add failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showShareDialog && selectedGoal != null) {
        var selectedPlatform by remember { mutableStateOf<String?>(null) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Savings Goal") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ListItem(
                        headlineContent = { Text("Share to WeChat") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "WeChat",
                                tint = Color(0xFF07C160)
                            )
                        },
                        modifier = Modifier.clickable { selectedPlatform = "wechat" }
                    )
                    ListItem(
                        headlineContent = { Text("Share to WhatsApp") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "WhatsApp",
                                tint = Color(0xFF25D366)
                            )
                        },
                        modifier = Modifier.clickable { selectedPlatform = "whatsapp" }
                    )
                    ListItem(
                        headlineContent = { Text("Other Applications") },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Other Applications",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.clickable { selectedPlatform = "other" }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val goal = selectedGoal!!
                        val progress = (goal.currentAmount / goal.targetAmount * 100).toInt()
                        val shareText = """
                            Savings Goal: ${goal.name}
                            Target Amount: $${String.format("%.2f", goal.targetAmount)}
                            Current Progress: $${String.format("%.2f", goal.currentAmount)}
                            Completion Progress: ${progress}%
                        """.trimIndent()

                        when (selectedPlatform) {
                            "wechat" -> {
                                val intent = Intent()
                                intent.action = Intent.ACTION_SEND
                                intent.putExtra(Intent.EXTRA_TEXT, shareText)
                                intent.type = "text/plain"
                                intent.setPackage("com.tencent.mm")

                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WeChat not installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "whatsapp" -> {
                                val intent = Intent()
                                intent.action = Intent.ACTION_SEND
                                intent.putExtra(Intent.EXTRA_TEXT, shareText)
                                intent.type = "text/plain"
                                intent.setPackage("com.whatsapp")

                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                                }
                            }
                            "other" -> {
                                val intent = Intent()
                                intent.action = Intent.ACTION_SEND
                                intent.putExtra(Intent.EXTRA_TEXT, shareText)
                                intent.type = "text/plain"
                                context.startActivity(Intent.createChooser(intent, "Share to"))
                            }
                        }
                        showShareDialog = false
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { showShareDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 