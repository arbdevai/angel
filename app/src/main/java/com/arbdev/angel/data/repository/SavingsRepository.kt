package com.arbdev.angel.data.repository

import android.content.Context
import com.arbdev.angel.data.local.AngelDatabaseHelper
import com.arbdev.angel.data.model.DailyStreakInfo
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.SavingsStats
import com.arbdev.angel.data.model.TransactionRecord
import com.arbdev.angel.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class AngelBackupData(
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0.0",
    val goals: List<Goal> = emptyList(),
    val transactions: List<TransactionRecord> = emptyList()
)

class SavingsRepository private constructor(context: Context) {

    private val dbHelper = AngelDatabaseHelper(context.applicationContext)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionRecord>>(emptyList())
    val transactions: StateFlow<List<TransactionRecord>> = _transactions.asStateFlow()

    private val _streakInfo = MutableStateFlow(DailyStreakInfo())
    val streakInfo: StateFlow<DailyStreakInfo> = _streakInfo.asStateFlow()

    private val _stats = MutableStateFlow(SavingsStats())
    val stats: StateFlow<SavingsStats> = _stats.asStateFlow()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    init {
        scope.launch {
            refreshData()
        }
    }

    suspend fun refreshData() = withContext(Dispatchers.IO) {
        val loadedGoals = dbHelper.getAllGoals()
        val loadedTx = dbHelper.getAllTransactions()
        val hasSaved = dbHelper.hasSavedToday()
        val todaySaved = dbHelper.getTodaySavedAmount()
        val (curStreak, longStreak) = dbHelper.calculateStreakDays()
        val weeklySaved = dbHelper.getRecent7DaysSaved()
        val (monthDeposit, monthWithdraw) = dbHelper.getMonthDepositAndWithdraw()

        val totalBal = loadedGoals.sumOf { it.currentAmount }
        val totalTgt = loadedGoals.sumOf { it.targetAmount }
        val activeCount = loadedGoals.count { !it.isCompleted }
        val completedCount = loadedGoals.count { it.isCompleted }

        _goals.value = loadedGoals
        _transactions.value = loadedTx
        _streakInfo.value = DailyStreakInfo(
            hasSavedToday = hasSaved,
            todaySavedAmount = todaySaved,
            currentStreakDays = curStreak,
            longestStreakDays = longStreak,
            lastSavedDate = if (hasSaved) AngelDatabaseHelper.getTodayDateString() else null,
            recentWeeklyDaysSaved = weeklySaved
        )
        _stats.value = SavingsStats(
            totalBalance = totalBal,
            totalTarget = totalTgt,
            totalSavedThisMonth = monthDeposit,
            totalWithdrawnThisMonth = monthWithdraw,
            activeGoalsCount = activeCount,
            completedGoalsCount = completedCount
        )
    }

    suspend fun saveMoney(goalId: String, amount: Double, note: String): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0.0) return@withContext false
        val success = dbHelper.addTransaction(goalId, amount, TransactionType.DEPOSIT, note)
        if (success) {
            refreshData()
        }
        success
    }

    suspend fun withdrawMoney(goalId: String, amount: Double, note: String): Boolean = withContext(Dispatchers.IO) {
        if (amount <= 0.0) return@withContext false
        val success = dbHelper.addTransaction(goalId, amount, TransactionType.WITHDRAW, note)
        if (success) {
            refreshData()
        }
        success
    }

    suspend fun createGoal(
        title: String,
        targetAmount: Double,
        categoryName: String,
        targetDateMillis: Long,
        colorHex: String,
        note: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (title.isBlank() || targetAmount <= 0.0) return@withContext false
        val newGoal = Goal(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            targetAmount = targetAmount,
            currentAmount = 0.0,
            categoryName = categoryName,
            targetDateMillis = targetDateMillis,
            createdAtMillis = System.currentTimeMillis(),
            colorHex = colorHex,
            isCompleted = false,
            note = note.trim()
        )
        val success = dbHelper.insertGoal(newGoal)
        if (success) {
            refreshData()
        }
        success
    }

    suspend fun updateGoal(goal: Goal): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.updateGoal(goal)
        if (success) {
            refreshData()
        }
        success
    }

    suspend fun deleteGoal(goalId: String): Boolean = withContext(Dispatchers.IO) {
        val success = dbHelper.deleteGoal(goalId)
        if (success) {
            refreshData()
        }
        success
    }

    suspend fun hasSavedToday(): Boolean = withContext(Dispatchers.IO) {
        dbHelper.hasSavedToday()
    }

    suspend fun exportBackupJson(): String = withContext(Dispatchers.IO) {
        val backup = AngelBackupData(
            goals = dbHelper.getAllGoals(),
            transactions = dbHelper.getAllTransactions()
        )
        json.encodeToString(backup)
    }

    suspend fun restoreBackupJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val backup = json.decodeFromString<AngelBackupData>(jsonString)
            if (backup.goals.isEmpty()) {
                return@withContext Result.failure(Exception("File cadangan tidak berisi data celengan."))
            }
            dbHelper.clearAllData()
            for (g in backup.goals) {
                dbHelper.insertGoal(g)
            }
            for (tx in backup.transactions) {
                dbHelper.addTransaction(tx.goalId, tx.amount, tx.type, tx.note)
            }
            refreshData()
            Result.success(backup.goals.size)
        } catch (e: Exception) {
            Result.failure(Exception("Format file cadangan tidak valid: ${e.message}"))
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dbHelper.clearAllData()
        refreshData()
    }

    companion object {
        @Volatile
        private var instance: SavingsRepository? = null

        fun getInstance(context: Context): SavingsRepository {
            return instance ?: synchronized(this) {
                instance ?: SavingsRepository(context).also { instance = it }
            }
        }
    }
}
