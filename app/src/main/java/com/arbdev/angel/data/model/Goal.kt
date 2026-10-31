package com.arbdev.angel.data.model

import kotlinx.serialization.Serializable
import kotlin.math.ceil

@Serializable
data class Goal(
    val id: String,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val categoryName: String = "GENERAL",
    val targetDateMillis: Long = 0L,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val colorHex: String = "#64748B",
    val isCompleted: Boolean = false,
    val note: String = ""
) {
    val category: GoalCategory
        get() = GoalCategory.fromName(categoryName)

    val progressFraction: Float
        get() = if (targetAmount > 0.0) {
            (currentAmount / targetAmount).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressPercentage: Int
        get() = (progressFraction * 100).toInt()

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)

    fun getEstimatedDaysRemaining(dailyTarget: Double): Long {
        if (isCompleted || remainingAmount <= 0.0) return 0L
        val target = if (dailyTarget > 0.0) dailyTarget else 20000.0
        val days = ceil(remainingAmount / target).toLong()
        return days.coerceIn(1L, 36500L) // Maksimal batas wajar 100 tahun
    }

    fun getEstimatedCompletionDateMillis(dailyTarget: Double): Long {
        val days = getEstimatedDaysRemaining(dailyTarget)
        val millis = days * 24L * 60L * 60L * 1000L
        val now = System.currentTimeMillis()
        return if (Long.MAX_VALUE - now > millis) now + millis else Long.MAX_VALUE
    }
}
