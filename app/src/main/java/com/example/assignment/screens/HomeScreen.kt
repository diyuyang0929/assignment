package com.example.assignment.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector

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
                title = { 
                    Column {
                        Text(
                            "Financial Overview",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            "Track your monthly finances",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.showSavingsAdviceDialog() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = "Savings Advice"
                            )
                        }
                        IconButton(
                            onClick = { viewModel.showShareDialog() },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        Button(
                            onClick = { viewModel.showStartNewMonthDialog() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Start New Month",
                                    modifier = Modifier.size(16.dp)
                                )
                                Text("New Month")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Welcome Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Welcome Back!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Track your finances and achieve your goals",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                // Financial Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Monthly Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            FinanceSummaryItem(
                                title = "Income",
                                amount = totalIncome,
                                icon = Icons.Default.TrendingUp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            FinanceSummaryItem(
                                title = "Expenses",
                                amount = totalExpense,
                                icon = Icons.Default.TrendingDown,
                                color = MaterialTheme.colorScheme.error
                            )
                            FinanceSummaryItem(
                                title = "Remaining",
                                amount = remainingAmount,
                                icon = Icons.Default.AccountBalance,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }

            item {
                // Budget Allocation Card
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Budget Allocation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        BudgetChart(
                            categories = expenseCategories,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    }
                }
            }

            item {
                // Recent Transactions Card
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Recent Transactions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (uiState.transactions.isEmpty()) {
                            Text(
                                "No transactions yet",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                uiState.transactions.take(5).forEach { transaction ->
                                    TransactionItem(transaction = transaction)
                                }
                            }
                        }

                        // 添加交易按钮
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = { viewModel.showAddIncomeDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Add Income",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Add Income")
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Button(
                                onClick = { viewModel.showAddExpenseDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Add Expense",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Add Expense")
                                }
                            }
                        }
                    }
                }
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
                        "Monthly Remaining Amount: ${uiState.currency}${String.format("%.2f", remainingAmount)}",
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
                                    text = "Target: ${uiState.currency}${String.format("%.2f", goal.targetAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Current: ${uiState.currency}${String.format("%.2f", goal.currentAmount)}",
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

@Composable
fun FinanceSummaryItem(
    title: String,
    amount: Float,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$${String.format("%.2f", amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
} 