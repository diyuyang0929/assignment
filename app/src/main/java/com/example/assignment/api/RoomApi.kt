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

    suspend fun deleteAllSavingsGoals() {
        // 由于储蓄目标目前是存储在内存中的，这个方法暂时不需要实现
        // 当储蓄目标被移动到Room数据库时，这里需要实现实际的删除逻辑
    }
}