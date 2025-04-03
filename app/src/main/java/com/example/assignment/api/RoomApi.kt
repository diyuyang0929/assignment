package com.example.assignment.api

import android.content.Context
import com.example.assignment.data.Transaction
import com.example.assignment.data.room.AppDatabase
import com.example.assignment.data.room.TransactionRepository
import kotlinx.coroutines.flow.Flow

class RoomApi(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val repository = TransactionRepository(database.transactionDao())

    val transactions: Flow<List<Transaction>> = repository.allTransactions

    suspend fun addTransaction(transaction: Transaction) {
        repository.insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        repository.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }

    suspend fun clearAllTransactions() {
        repository.deleteAllTransactions()
    }
}