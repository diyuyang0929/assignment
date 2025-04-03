package com.example.assignment.api

import com.example.assignment.data.SavingsGoal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class SavingsApi {
    private val _savingsGoals = MutableStateFlow<List<SavingsGoal>>(emptyList())
    val savingsGoals: Flow<List<SavingsGoal>> = _savingsGoals

    suspend fun addSavingsGoal(name: String, targetAmount: Float): Result<SavingsGoal> {
        return try {
            val newGoal = SavingsGoal(
                id = System.currentTimeMillis().toString(),
                name = name,
                targetAmount = targetAmount,
                currentAmount = 0f
            )
            _savingsGoals.value = _savingsGoals.value + newGoal
            Result.success(newGoal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSavingsProgress(goalId: String, amount: Float): Result<SavingsGoal> {
        return try {
            val totalCurrentAmount = _savingsGoals.value.sumOf { it.currentAmount.toDouble() }.toFloat()

            if (totalCurrentAmount < amount) {
                return Result.failure(Exception("Not enough total current amount"))
            }

            val updatedGoals = _savingsGoals.value.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(currentAmount = goal.currentAmount + amount)
                } else {
                    val decreaseAmount = (goal.currentAmount / totalCurrentAmount) * amount
                    goal.copy(currentAmount = goal.currentAmount - decreaseAmount)
                }
            }
            _savingsGoals.value = updatedGoals
            Result.success(updatedGoals.find { it.id == goalId }!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addSaving(goalId: String, amount: Float): Result<SavingsGoal> {
        return try {
            val updatedGoals = _savingsGoals.value.map { goal ->
                if (goal.id == goalId) {
                    goal.copy(currentAmount = goal.currentAmount + amount)
                } else {
                    goal
                }
            }
            _savingsGoals.value = updatedGoals
            Result.success(updatedGoals.find { it.id == goalId }!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteSavingsGoal(goalId: String): Result<Unit> {
        return try {
            _savingsGoals.value = _savingsGoals.value.filter { it.id != goalId }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSavingsStatistics(): Result<SavingsStatistics> {
        return try {
            val goals = _savingsGoals.value
            val totalTarget = goals.sumOf { it.targetAmount.toDouble() }
            val totalCurrent = goals.sumOf { it.currentAmount.toDouble() }
            val averageProgress = if (goals.isNotEmpty()) {
                (totalCurrent / totalTarget * 100).toFloat()
            } else {
                0f
            }

            Result.success(SavingsStatistics(
                totalTargetAmount = totalTarget.toFloat(),
                totalCurrentAmount = totalCurrent.toFloat(),
                averageProgress = averageProgress,
                totalGoals = goals.size
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class SavingsStatistics(
    val totalTargetAmount: Float,
    val totalCurrentAmount: Float,
    val averageProgress: Float,
    val totalGoals: Int
)
