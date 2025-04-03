package com.example.assignment.data

data class Transaction(
    val id: String,
    val amount: Float,
    val description: String,
    val date: String,
    val type: TransactionType
)

enum class TransactionType {
    INCOME,
    EXPENSE
} 