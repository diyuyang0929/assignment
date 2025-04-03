package com.example.assignment.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.assignment.data.SavingsGoal

@Composable
fun SavingsGoalCard(
    goal: SavingsGoal,
    onShare: () -> Unit,
    onAddAmount: (Float) -> Unit,
    onDelete: () -> Unit
) {
    var showAddAmountDialog by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = goal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Row {
                    IconButton(onClick = onShare) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            val progress = (goal.currentAmount / goal.targetAmount * 100).toInt()
            Text("Target Amount: $${String.format("%.2f", goal.targetAmount)}")
            Text("Current Amount: $${String.format("%.2f", goal.currentAmount)}")
            Text("Completion Progress: $progress%")

            LinearProgressIndicator(
                progress = goal.currentAmount / goal.targetAmount,
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = { showAddAmountDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Amount")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Amount")
            }
        }
    }

    if (showAddAmountDialog) {
        AlertDialog(
            onDismissRequest = { showAddAmountDialog = false },
            title = { Text("Add Amount") },
            text = {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        amount.toFloatOrNull()?.let { value ->
                            onAddAmount(value)
                            showAddAmountDialog = false
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAmountDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 