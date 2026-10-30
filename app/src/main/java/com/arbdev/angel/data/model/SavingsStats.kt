package com.arbdev.angel.data.model

data class DailyStreakInfo(
    val hasSavedToday: Boolean = false,
    val todaySavedAmount: Double = 0.0,
    val currentStreakDays: Int = 0,
    val longestStreakDays: Int = 0,
    val lastSavedDate: String? = null,
    val recentWeeklyDaysSaved: List<Boolean> = List(7) { false }
)

data class SavingsStats(
    val totalBalance: Double = 0.0,
    val totalTarget: Double = 0.0,
    val totalSavedThisMonth: Double = 0.0,
    val totalWithdrawnThisMonth: Double = 0.0,
    val activeGoalsCount: Int = 0,
    val completedGoalsCount: Int = 0
) {
    val overallProgressFraction: Float
        get() = if (totalTarget > 0.0) {
            (totalBalance / totalTarget).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }

    val overallProgressPercent: Int
        get() = (overallProgressFraction * 100).toInt()
}
