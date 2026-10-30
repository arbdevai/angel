package com.arbdev.angel.data.model

import kotlinx.serialization.Serializable

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
}
