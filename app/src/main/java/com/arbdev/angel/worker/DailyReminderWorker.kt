package com.arbdev.angel.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.arbdev.angel.data.preferences.UserPreferencesManager
import com.arbdev.angel.data.repository.SavingsRepository
import kotlinx.coroutines.flow.first

class DailyReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = UserPreferencesManager(context)
        val isEnabled = prefs.isDailyReminderEnabled.first()

        if (!isEnabled) {
            return Result.success()
        }

        val repository = SavingsRepository.getInstance(context)
        val hasSaved = repository.hasSavedToday()

        if (!hasSaved) {
            NotificationHelper.sendReminderNotification(
                context = context,
                title = "Waktunya Mengisi Celengan Angel",
                message = "Anda belum menabung hari ini. Sisihkan tabungan agar target impian Anda segera tercapai."
            )
        }

        return Result.success()
    }
}
