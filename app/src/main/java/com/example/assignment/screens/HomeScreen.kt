package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.components.FinanceCard
import com.example.assignment.components.TransactionItem
import com.example.assignment.components.BudgetChart
import com.example.assignment.data.TransactionType
import com.example.assignment.data.SavingsGoal
import com.example.assignment.viewmodels.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.clickable
import android.content.Intent
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import com.example.assignment.components.SavingsAdviceDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onNavigateToSavings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val remainingAmount by viewModel.remainingAmount.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.showSavingsAdviceDialog() }) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = "Savings Advice",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { viewModel.showShareDialog() }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    TextButton(
                        onClick = { viewModel.showStartNewMonthDialog() },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Start New Month")
                    }
                }
            )
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { viewModel.showAddIncomeDialog() },
                    modifier = Modifier.padding(end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Income")
                }
                FloatingActionButton(
                    onClick = { viewModel.showAddExpenseDialog() },
                    modifier = Modifier.padding(end = 8.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
                FloatingActionButton(
                    onClick = { viewModel.showAddToSavingsDialog() },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Savings, contentDescription = "Add to Savings")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Text(
                    "Welcome to Finance Tracker",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Easily manage your personal finances",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                FinanceCard(
                    title = "Monthly Income",
                    amount = "$${String.format("%.2f", totalIncome)}",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                FinanceCard(
                    title = "Monthly Expenses",
                    amount = "$${String.format("%.2f", totalExpense)}",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                FinanceCard(
                    title = "Monthly Remaining",
                    amount = "$${String.format("%.2f", remainingAmount)}",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text(
                    "Budget Allocation",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BudgetChart(
                    categories = expenseCategories.filter { it.first != "Remaining" }
                )
            }

            item {
                Text(
                    "Recent Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(uiState.transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onDelete = {
                        viewModel.deleteTransaction(transaction)
                    }
                )
            }
        }
    }

    if (uiState.showAddIncomeDialog) {
        var amount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddIncomeDialog() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Add Income")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Income Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (amount.isNotBlank() && description.isNotBlank()) {
                            viewModel.addTransaction(
                                amount = amount.toFloatOrNull() ?: 0f,
                                description = description,
                                type = TransactionType.INCOME
                            )
                            viewModel.hideAddIncomeDialog()
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddIncomeDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showAddExpenseDialog) {
        var amount by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddExpenseDialog() },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text("Add Expense")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Expense Category") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (amount.isNotBlank() && description.isNotBlank()) {
                            viewModel.addTransaction(
                                amount = amount.toFloatOrNull() ?: 0f,
                                description = description,
                                type = TransactionType.EXPENSE
                            )
                            viewModel.hideAddExpenseDialog()
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddExpenseDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showShareDialog) {
        var selectedPlatform by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { viewModel.hideShareDialog() },
            title = { Text("Share Financial Overview") },
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
                        val shareText = """
                            Financial Overview
                            Monthly Income: $${String.format("%.2f", totalIncome)}
                            Monthly Expenses: $${String.format("%.2f", totalExpense)}
                            Monthly Remaining: $${String.format("%.2f", remainingAmount)}
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
                        viewModel.hideShareDialog()
                    }
                ) {
                    Text("Share")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideShareDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showAddToSavingsDialog) {
        var amount by remember { mutableStateOf("") }
        var selectedGoal by remember { mutableStateOf<SavingsGoal?>(null) }

        AlertDialog(
            onDismissRequest = { viewModel.hideAddToSavingsDialog() },
            title = { Text("Add to Savings Goal") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Monthly Remaining Amount: $${String.format("%.2f", remainingAmount)}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )

                    uiState.savingsGoals.forEach { goal ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGoal = goal },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedGoal == goal,
                                onClick = { selectedGoal = goal }
                            )
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = goal.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "Target: $${String.format("%.2f", goal.targetAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Current: $${String.format("%.2f", goal.currentAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        label = { Text("Savings Amount") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (selectedGoal != null && remainingAmount > 0) {
                        Button(
                            onClick = {
                                viewModel.addAllRemainingToSavings(selectedGoal!!)
                                viewModel.hideAddToSavingsDialog()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Save All")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (amount.isNotBlank() && selectedGoal != null) {
                            val savingsAmount = amount.toFloatOrNull() ?: 0f
                            if (savingsAmount <= remainingAmount) {
                                viewModel.addToSavings(selectedGoal!!, savingsAmount)
                                viewModel.hideAddToSavingsDialog()
                            }
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddToSavingsDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showSavingsAdviceDialog) {
        SavingsAdviceDialog(
            savingsTips = uiState.savingsTips,
            recommendedSavingsMethod = uiState.recommendedSavingsMethod,
            isLoading = uiState.isLoadingAdvice,
            networkError = uiState.networkError,
            onRefresh = { viewModel.refreshFinancialAdvice() },
            onDismiss = { viewModel.hideSavingsAdviceDialog() }
        )
    }

    if (uiState.showStartNewMonthDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideStartNewMonthDialog() },
            title = {
                Text("Start New Month")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Are you sure you want to start a new month? This will:",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        "1. Add remaining balance $${String.format("%.2f", remainingAmount)} to savings goal",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "2. Clear all current month's transactions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "3. Start recording new month's transactions",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startNewMonth()
                        viewModel.hideStartNewMonthDialog()
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideStartNewMonthDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
} 