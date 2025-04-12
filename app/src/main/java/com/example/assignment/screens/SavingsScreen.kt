package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector

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
                title = { 
                    Text(
                        "Savings Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
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
                // Welcome Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Your Savings Journey",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Track and achieve your financial goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            statistics?.let { stats ->
                item {
                    // Statistics Card
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
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Overall Progress",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Progress Circle
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                CircularProgressIndicator(
                                    progress = stats.averageProgress / 100f,
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 12.dp,
                                    strokeCap = StrokeCap.Round
                                )
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${String.format("%.1f", stats.averageProgress)}%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "Average Progress",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                StatItem(
                                    title = "Total Goals",
                                    value = stats.totalGoals.toString(),
                                    icon = Icons.Default.Flag
                                )
                                StatItem(
                                    title = "Total Target",
                                    value = "$${String.format("%.2f", stats.totalTargetAmount)}",
                                    icon = Icons.Default.AttachMoney
                                )
                                StatItem(
                                    title = "Total Saved",
                                    value = "$${String.format("%.2f", stats.totalCurrentAmount)}",
                                    icon = Icons.Default.Savings
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Your Goals",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
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
                                    Toast.makeText(context, "Progress updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { e ->
                                    Toast.makeText(context, "Failed to update progress: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    },
                    onDelete = {
                        scope.launch {
                            savingsApi.deleteSavingsGoal(goal.id)
                                .onSuccess {
                                    Toast.makeText(context, "Goal deleted successfully", Toast.LENGTH_SHORT).show()
                                }
                                .onFailure { e ->
                                    Toast.makeText(context, "Failed to delete goal: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                )
            }
        }
    }

    // Add Goal Dialog
    if (showAddGoalDialog) {
        var name by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { 
                Text(
                    "Add New Goal",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Goal Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Target Amount") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && amount.isNotBlank()) {
                            scope.launch {
                                savingsApi.addSavingsGoal(name, amount.toFloatOrNull() ?: 0f)
                                    .onSuccess {
                                        showAddGoalDialog = false
                                        Toast.makeText(context, "Goal added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                    .onFailure { e ->
                                        Toast.makeText(context, "Failed to add goal: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Add Goal")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showAddGoalDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Share Dialog
    if (showShareDialog) {
        var selectedPlatform by remember { mutableStateOf("") }
        val goal = selectedGoal!!

        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { 
                Text(
                    "Share Progress",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Share your savings progress with others",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Share options
                    listOf(
                        "WhatsApp" to Icons.Default.Chat,
                        "Messenger" to Icons.Default.Send,
                        "Other Apps" to Icons.Default.Share
                    ).forEach { (platform, icon) ->
                        ListItem(
                            headlineContent = { Text(platform) },
                            leadingContent = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = platform,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .clickable { selectedPlatform = platform }
                                .background(
                                    if (selectedPlatform == platform) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val progress = (goal.currentAmount / goal.targetAmount * 100).toInt()
                        val shareText = """
                            🎯 Savings Goal: ${goal.name}
                            💰 Target Amount: $${String.format("%.2f", goal.targetAmount)}
                            💵 Current Progress: $${String.format("%.2f", goal.currentAmount)}
                            📈 Completion: ${progress}%
                        """.trimIndent()

                        val intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share via"))
                        showShareDialog = false
                    },
                    enabled = selectedPlatform.isNotEmpty(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showShareDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 