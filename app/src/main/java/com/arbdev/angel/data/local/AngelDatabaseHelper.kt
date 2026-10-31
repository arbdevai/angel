package com.arbdev.angel.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.GoalCategory
import com.arbdev.angel.data.model.TransactionRecord
import com.arbdev.angel.data.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID

class AngelDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "angel_savings.db"
        const val DATABASE_VERSION = 1

        // Table Goals
        const val TABLE_GOALS = "goals"
        const val COL_GOAL_ID = "id"
        const val COL_GOAL_TITLE = "title"
        const val COL_GOAL_TARGET_AMOUNT = "target_amount"
        const val COL_GOAL_CURRENT_AMOUNT = "current_amount"
        const val COL_GOAL_CATEGORY = "category"
        const val COL_GOAL_TARGET_DATE = "target_date"
        const val COL_GOAL_CREATED_AT = "created_at"
        const val COL_GOAL_COLOR_HEX = "color_hex"
        const val COL_GOAL_IS_COMPLETED = "is_completed"
        const val COL_GOAL_NOTE = "note"

        // Table Transactions
        const val TABLE_TRANSACTIONS = "transactions"
        const val COL_TX_ID = "id"
        const val COL_TX_GOAL_ID = "goal_id"
        const val COL_TX_GOAL_TITLE = "goal_title"
        const val COL_TX_AMOUNT = "amount"
        const val COL_TX_TYPE = "type"
        const val COL_TX_TIMESTAMP = "timestamp"
        const val COL_TX_NOTE = "note"

        // Table Daily Log
        const val TABLE_DAILY_LOG = "daily_savings_log"
        const val COL_LOG_DATE = "log_date" // YYYY-MM-DD
        const val COL_LOG_TOTAL_SAVED = "total_saved"
        const val COL_LOG_TX_COUNT = "tx_count"

        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        private val monthFormat = DateTimeFormatter.ofPattern("yyyy-MM", Locale.US)

        fun getTodayDateString(): String = LocalDate.now().format(dateFormat)
        fun getCurrentMonthString(): String = YearMonth.now().format(monthFormat)
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createGoalsSql = """
            CREATE TABLE $TABLE_GOALS (
                $COL_GOAL_ID TEXT PRIMARY KEY,
                $COL_GOAL_TITLE TEXT NOT NULL,
                $COL_GOAL_TARGET_AMOUNT REAL NOT NULL,
                $COL_GOAL_CURRENT_AMOUNT REAL NOT NULL DEFAULT 0.0,
                $COL_GOAL_CATEGORY TEXT NOT NULL,
                $COL_GOAL_TARGET_DATE INTEGER NOT NULL,
                $COL_GOAL_CREATED_AT INTEGER NOT NULL,
                $COL_GOAL_COLOR_HEX TEXT NOT NULL,
                $COL_GOAL_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COL_GOAL_NOTE TEXT
            )
        """.trimIndent()

        val createTransactionsSql = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COL_TX_ID TEXT PRIMARY KEY,
                $COL_TX_GOAL_ID TEXT NOT NULL,
                $COL_TX_GOAL_TITLE TEXT NOT NULL,
                $COL_TX_AMOUNT REAL NOT NULL,
                $COL_TX_TYPE TEXT NOT NULL,
                $COL_TX_TIMESTAMP INTEGER NOT NULL,
                $COL_TX_NOTE TEXT,
                FOREIGN KEY ($COL_TX_GOAL_ID) REFERENCES $TABLE_GOALS($COL_GOAL_ID) ON DELETE CASCADE
            )
        """.trimIndent()

        val createDailyLogSql = """
            CREATE TABLE $TABLE_DAILY_LOG (
                $COL_LOG_DATE TEXT PRIMARY KEY,
                $COL_LOG_TOTAL_SAVED REAL NOT NULL DEFAULT 0.0,
                $COL_LOG_TX_COUNT INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent()

        db.execSQL(createGoalsSql)
        db.execSQL(createTransactionsSql)
        db.execSQL(createDailyLogSql)

        db.execSQL("CREATE INDEX idx_tx_goal_id ON $TABLE_TRANSACTIONS ($COL_TX_GOAL_ID)")
        db.execSQL("CREATE INDEX idx_tx_timestamp ON $TABLE_TRANSACTIONS ($COL_TX_TIMESTAMP)")
        db.execSQL("CREATE INDEX idx_log_date ON $TABLE_DAILY_LOG ($COL_LOG_DATE)")

        // Inisialisasi celengan default pertama agar user langsung memiliki wadah tabungan utama
        val initialGoalId = UUID.randomUUID().toString()
        val initialGoalValues = ContentValues().apply {
            put(COL_GOAL_ID, initialGoalId)
            put(COL_GOAL_TITLE, "Celengan Utama Angel")
            put(COL_GOAL_TARGET_AMOUNT, 1000000.0)
            put(COL_GOAL_CURRENT_AMOUNT, 0.0)
            put(COL_GOAL_CATEGORY, GoalCategory.GENERAL.name)
            put(COL_GOAL_TARGET_DATE, System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))
            put(COL_GOAL_CREATED_AT, System.currentTimeMillis())
            put(COL_GOAL_COLOR_HEX, GoalCategory.GENERAL.colorHex)
            put(COL_GOAL_IS_COMPLETED, 0)
            put(COL_GOAL_NOTE, "Target tabungan celengan pertama bersama Angel")
        }
        db.insert(TABLE_GOALS, null, initialGoalValues)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Migrasi masa depan
    }

    // --- Goal Queries ---
    fun insertGoal(goal: Goal): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GOAL_ID, goal.id)
            put(COL_GOAL_TITLE, goal.title)
            put(COL_GOAL_TARGET_AMOUNT, goal.targetAmount)
            put(COL_GOAL_CURRENT_AMOUNT, goal.currentAmount)
            put(COL_GOAL_CATEGORY, goal.categoryName)
            put(COL_GOAL_TARGET_DATE, goal.targetDateMillis)
            put(COL_GOAL_CREATED_AT, goal.createdAtMillis)
            put(COL_GOAL_COLOR_HEX, goal.colorHex)
            put(COL_GOAL_IS_COMPLETED, if (goal.isCompleted) 1 else 0)
            put(COL_GOAL_NOTE, goal.note)
        }
        return db.insert(TABLE_GOALS, null, values) != -1L
    }

    fun updateGoal(goal: Goal): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_GOAL_TITLE, goal.title)
            put(COL_GOAL_TARGET_AMOUNT, goal.targetAmount)
            put(COL_GOAL_CURRENT_AMOUNT, goal.currentAmount)
            put(COL_GOAL_CATEGORY, goal.categoryName)
            put(COL_GOAL_TARGET_DATE, goal.targetDateMillis)
            put(COL_GOAL_COLOR_HEX, goal.colorHex)
            put(COL_GOAL_IS_COMPLETED, if (goal.isCompleted) 1 else 0)
            put(COL_GOAL_NOTE, goal.note)
        }
        return db.update(TABLE_GOALS, values, "$COL_GOAL_ID = ?", arrayOf(goal.id)) > 0
    }

    fun deleteGoal(goalId: String): Boolean {
        val db = writableDatabase
        return db.delete(TABLE_GOALS, "$COL_GOAL_ID = ?", arrayOf(goalId)) > 0
    }

    fun getAllGoals(): List<Goal> {
        val list = mutableListOf<Goal>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GOALS,
            null,
            null,
            null,
            null,
            null,
            "$COL_GOAL_CREATED_AT DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToGoal(it))
            }
        }
        return list
    }

    fun getGoalById(goalId: String): Goal? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_GOALS,
            null,
            "$COL_GOAL_ID = ?",
            arrayOf(goalId),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToNext()) cursorToGoal(it) else null
        }
    }

    private fun cursorToGoal(cursor: Cursor): Goal {
        return Goal(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_TITLE)),
            targetAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET_AMOUNT)),
            currentAmount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_GOAL_CURRENT_AMOUNT)),
            categoryName = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_CATEGORY)),
            targetDateMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_GOAL_TARGET_DATE)),
            createdAtMillis = cursor.getLong(cursor.getColumnIndexOrThrow(COL_GOAL_CREATED_AT)),
            colorHex = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_COLOR_HEX)),
            isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow(COL_GOAL_IS_COMPLETED)) == 1,
            note = cursor.getString(cursor.getColumnIndexOrThrow(COL_GOAL_NOTE)) ?: ""
        )
    }

    // --- Transactions & Savings ---
    fun addTransaction(
        goalId: String,
        amount: Double,
        type: TransactionType,
        note: String,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            // Read goal inside transaction for strict atomicity
            val cursor = db.query(
                TABLE_GOALS,
                null,
                "$COL_GOAL_ID = ?",
                arrayOf(goalId),
                null,
                null,
                null
            )
            val goal = cursor.use {
                if (it.moveToNext()) cursorToGoal(it) else null
            } ?: return false

            val newAmount = when (type) {
                TransactionType.DEPOSIT -> goal.currentAmount + amount
                TransactionType.WITHDRAW -> (goal.currentAmount - amount).coerceAtLeast(0.0)
            }
            val isCompleted = newAmount >= goal.targetAmount && goal.targetAmount > 0.0

            // 1. Update goal balance
            val goalValues = ContentValues().apply {
                put(COL_GOAL_CURRENT_AMOUNT, newAmount)
                put(COL_GOAL_IS_COMPLETED, if (isCompleted) 1 else 0)
            }
            val updated = db.update(TABLE_GOALS, goalValues, "$COL_GOAL_ID = ?", arrayOf(goalId))
            if (updated <= 0) return false

            // 2. Insert transaction record
            val txId = UUID.randomUUID().toString()
            val txValues = ContentValues().apply {
                put(COL_TX_ID, txId)
                put(COL_TX_GOAL_ID, goalId)
                put(COL_TX_GOAL_TITLE, goal.title)
                put(COL_TX_AMOUNT, amount)
                put(COL_TX_TYPE, type.name)
                put(COL_TX_TIMESTAMP, timestamp)
                put(COL_TX_NOTE, note)
            }
            val insertedTx = db.insert(TABLE_TRANSACTIONS, null, txValues)
            if (insertedTx == -1L) return false

            // 3. Update daily savings log if it's a deposit
            if (type == TransactionType.DEPOSIT) {
                val dateStr = Instant.ofEpochMilli(timestamp)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(dateFormat)

                val logCursor = db.query(
                    TABLE_DAILY_LOG,
                    null,
                    "$COL_LOG_DATE = ?",
                    arrayOf(dateStr),
                    null,
                    null,
                    null
                )
                val exists = logCursor.use { it.moveToFirst() }
                if (exists) {
                    db.execSQL(
                        "UPDATE $TABLE_DAILY_LOG SET $COL_LOG_TOTAL_SAVED = $COL_LOG_TOTAL_SAVED + ?, $COL_LOG_TX_COUNT = $COL_LOG_TX_COUNT + 1 WHERE $COL_LOG_DATE = ?",
                        arrayOf(amount, dateStr)
                    )
                } else {
                    val logValues = ContentValues().apply {
                        put(COL_LOG_DATE, dateStr)
                        put(COL_LOG_TOTAL_SAVED, amount)
                        put(COL_LOG_TX_COUNT, 1)
                    }
                    db.insert(TABLE_DAILY_LOG, null, logValues)
                }
            }

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun getAllTransactions(): List<TransactionRecord> {
        val list = mutableListOf<TransactionRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            null,
            null,
            null,
            null,
            "$COL_TX_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToTransaction(it))
            }
        }
        return list
    }

    fun getTransactionsByGoal(goalId: String): List<TransactionRecord> {
        val list = mutableListOf<TransactionRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            "$COL_TX_GOAL_ID = ?",
            arrayOf(goalId),
            null,
            null,
            "$COL_TX_TIMESTAMP DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(cursorToTransaction(it))
            }
        }
        return list
    }

    private fun cursorToTransaction(cursor: Cursor): TransactionRecord {
        val typeStr = cursor.getString(cursor.getColumnIndexOrThrow(COL_TX_TYPE))
        val type = if (typeStr == TransactionType.WITHDRAW.name) TransactionType.WITHDRAW else TransactionType.DEPOSIT
        return TransactionRecord(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_TX_ID)),
            goalId = cursor.getString(cursor.getColumnIndexOrThrow(COL_TX_GOAL_ID)),
            goalTitle = cursor.getString(cursor.getColumnIndexOrThrow(COL_TX_GOAL_TITLE)),
            amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TX_AMOUNT)),
            type = type,
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COL_TX_TIMESTAMP)),
            note = cursor.getString(cursor.getColumnIndexOrThrow(COL_TX_NOTE)) ?: ""
        )
    }

    // --- Streak & Notification Tracking ---
    fun hasSavedToday(): Boolean {
        val todayStr = getTodayDateString()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOG,
            arrayOf(COL_LOG_TOTAL_SAVED),
            "$COL_LOG_DATE = ? AND $COL_LOG_TOTAL_SAVED > 0",
            arrayOf(todayStr),
            null,
            null,
            null
        )
        return cursor.use { it.moveToFirst() }
    }

    fun getTodaySavedAmount(): Double {
        val todayStr = getTodayDateString()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOG,
            arrayOf(COL_LOG_TOTAL_SAVED),
            "$COL_LOG_DATE = ?",
            arrayOf(todayStr),
            null,
            null,
            null
        )
        return cursor.use {
            if (it.moveToFirst()) it.getDouble(0) else 0.0
        }
    }

    fun calculateStreakDays(): Pair<Int, Int> {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_DAILY_LOG,
            arrayOf(COL_LOG_DATE),
            "$COL_LOG_TOTAL_SAVED > 0",
            null,
            null,
            null,
            "$COL_LOG_DATE DESC"
        )

        val dates = mutableListOf<String>()
        cursor.use {
            while (it.moveToNext()) {
                dates.add(it.getString(0))
            }
        }

        if (dates.isEmpty()) return Pair(0, 0)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayStr = today.format(dateFormat)
        val yesterdayStr = yesterday.format(dateFormat)

        val dateSet = dates.toSet()
        var checkDate = if (dateSet.contains(todayStr)) {
            today
        } else if (dateSet.contains(yesterdayStr)) {
            yesterday
        } else {
            return Pair(0, calculateLongestStreak(dates))
        }

        var currentStreak = 0
        while (dateSet.contains(checkDate.format(dateFormat))) {
            currentStreak++
            checkDate = checkDate.minusDays(1)
        }

        return Pair(currentStreak, maxOf(currentStreak, calculateLongestStreak(dates)))
    }

    private fun calculateLongestStreak(datesDesc: List<String>): Int {
        if (datesDesc.isEmpty()) return 0
        var maxStreak = 1
        var tempStreak = 1

        val sortedAsc = datesDesc.mapNotNull {
            try {
                LocalDate.parse(it, dateFormat)
            } catch (_: Exception) {
                null
            }
        }.sorted()

        for (i in 0 until sortedAsc.size - 1) {
            val daysBetween = ChronoUnit.DAYS.between(sortedAsc[i], sortedAsc[i + 1])
            if (daysBetween == 1L) {
                tempStreak++
                if (tempStreak > maxStreak) maxStreak = tempStreak
            } else if (daysBetween > 1L) {
                tempStreak = 1
            }
        }
        return maxStreak
    }

    fun getRecent7DaysSaved(): List<Boolean> {
        val result = mutableListOf<Boolean>()
        val db = readableDatabase
        var current = LocalDate.now().minusDays(6)

        for (i in 0..6) {
            val dStr = current.format(dateFormat)
            val cursor = db.query(
                TABLE_DAILY_LOG,
                arrayOf(COL_LOG_TOTAL_SAVED),
                "$COL_LOG_DATE = ? AND $COL_LOG_TOTAL_SAVED > 0",
                arrayOf(dStr),
                null,
                null,
                null
            )
            val saved = cursor.use { it.moveToFirst() }
            result.add(saved)
            current = current.plusDays(1)
        }
        return result
    }

    fun getMonthDepositAndWithdraw(): Pair<Double, Double> {
        val currentMonth = getCurrentMonthString()
        val db = readableDatabase
        val cursor = db.rawQuery(
            """
            SELECT $COL_TX_TYPE, SUM($COL_TX_AMOUNT)
            FROM $TABLE_TRANSACTIONS
            WHERE strftime('%Y-%m', datetime($COL_TX_TIMESTAMP / 1000, 'unixepoch', 'localtime')) = ?
            GROUP BY $COL_TX_TYPE
            """.trimIndent(),
            arrayOf(currentMonth)
        )
        var deposit = 0.0
        var withdraw = 0.0
        cursor.use {
            while (it.moveToNext()) {
                val type = it.getString(0)
                val sum = it.getDouble(1)
                if (type == TransactionType.DEPOSIT.name) {
                    deposit = sum
                } else if (type == TransactionType.WITHDRAW.name) {
                    withdraw = sum
                }
            }
        }
        return Pair(deposit, withdraw)
    }

    fun restoreData(goals: List<Goal>, transactions: List<TransactionRecord>): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_TRANSACTIONS, null, null)
            db.delete(TABLE_DAILY_LOG, null, null)
            db.delete(TABLE_GOALS, null, null)

            // 1. Pulihkan seluruh celengan dengan saldo asli
            for (goal in goals) {
                val values = ContentValues().apply {
                    put(COL_GOAL_ID, goal.id)
                    put(COL_GOAL_TITLE, goal.title)
                    put(COL_GOAL_TARGET_AMOUNT, goal.targetAmount)
                    put(COL_GOAL_CURRENT_AMOUNT, goal.currentAmount)
                    put(COL_GOAL_CATEGORY, goal.categoryName)
                    put(COL_GOAL_TARGET_DATE, goal.targetDateMillis)
                    put(COL_GOAL_CREATED_AT, goal.createdAtMillis)
                    put(COL_GOAL_COLOR_HEX, goal.colorHex)
                    put(COL_GOAL_IS_COMPLETED, if (goal.isCompleted) 1 else 0)
                    put(COL_GOAL_NOTE, goal.note)
                }
                db.insert(TABLE_GOALS, null, values)
            }

            // 2. Pulihkan seluruh riwayat transaksi dengan timestamp asli tanpa menambah saldo lagi
            for (tx in transactions) {
                val txValues = ContentValues().apply {
                    put(COL_TX_ID, tx.id)
                    put(COL_TX_GOAL_ID, tx.goalId)
                    put(COL_TX_GOAL_TITLE, tx.goalTitle)
                    put(COL_TX_AMOUNT, tx.amount)
                    put(COL_TX_TYPE, tx.type.name)
                    put(COL_TX_TIMESTAMP, tx.timestamp)
                    put(COL_TX_NOTE, tx.note)
                }
                db.insert(TABLE_TRANSACTIONS, null, txValues)

                // 3. Bangun ulang log harian sesuai tanggal transaksi asli
                if (tx.type == TransactionType.DEPOSIT) {
                    val dateStr = Instant.ofEpochMilli(tx.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(dateFormat)

                    val logCursor = db.query(
                        TABLE_DAILY_LOG,
                        null,
                        "$COL_LOG_DATE = ?",
                        arrayOf(dateStr),
                        null,
                        null,
                        null
                    )
                    val exists = logCursor.use { it.moveToFirst() }
                    if (exists) {
                        db.execSQL(
                            "UPDATE $TABLE_DAILY_LOG SET $COL_LOG_TOTAL_SAVED = $COL_LOG_TOTAL_SAVED + ?, $COL_LOG_TX_COUNT = $COL_LOG_TX_COUNT + 1 WHERE $COL_LOG_DATE = ?",
                            arrayOf(tx.amount, dateStr)
                        )
                    } else {
                        val logValues = ContentValues().apply {
                            put(COL_LOG_DATE, dateStr)
                            put(COL_LOG_TOTAL_SAVED, tx.amount)
                            put(COL_LOG_TX_COUNT, 1)
                        }
                        db.insert(TABLE_DAILY_LOG, null, logValues)
                    }
                }
            }

            db.setTransactionSuccessful()
            return true
        } finally {
            db.endTransaction()
        }
    }

    fun clearAllData() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_TRANSACTIONS, null, null)
            db.delete(TABLE_DAILY_LOG, null, null)
            db.delete(TABLE_GOALS, null, null)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
