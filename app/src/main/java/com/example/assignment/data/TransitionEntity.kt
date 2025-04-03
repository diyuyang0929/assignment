package com.example.assignment.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.assignment.data.Transaction
import com.example.assignment.data.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val amount: Float,
    val description: String,
    val date: String,
    val type: String
) {
    fun toTransaction(): Transaction {
        return Transaction(
            id = id,
            amount = amount,
            description = description,
            date = date,
            type = if (type == "INCOME") TransactionType.INCOME else TransactionType.EXPENSE
        )
    }

    companion object {
        fun fromTransaction(transaction: Transaction): TransactionEntity {
            return TransactionEntity(
                id = transaction.id,
                amount = transaction.amount,
                description = transaction.description,
                date = transaction.date,
                type = transaction.type.name
            )
        }
    }
}