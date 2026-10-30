package com.arbdev.angel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.arbdev.angel.MainActivity
import com.arbdev.angel.R
import com.arbdev.angel.data.local.AngelDatabaseHelper
import com.arbdev.angel.data.model.Goal
import com.arbdev.angel.data.model.TransactionType
import com.arbdev.angel.data.preferences.UserPreferencesManager
import com.arbdev.angel.data.repository.SavingsRepository
import com.arbdev.angel.util.Formatters
import com.arbdev.angel.worker.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AngelAppWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_WIDGET_QUICK_SAVE = "com.arbdev.angel.ACTION_WIDGET_QUICK_SAVE"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, AngelAppWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isNotEmpty()) {
                val intent = Intent(context, AngelAppWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        }
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                val dbHelper = AngelDatabaseHelper(context)
                val prefs = UserPreferencesManager(context)
                val dailyTarget = prefs.dailySavingTarget.first()

                val goals = dbHelper.getAllGoals()
                val primaryGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()
                val hasSavedToday = dbHelper.hasSavedToday()
                val (currentStreak, _) = dbHelper.calculateStreakDays()

                for (widgetId in appWidgetIds) {
                    val views = RemoteViews(context.packageName, R.layout.widget_angel_savings)

                    // Intent buka aplikasi saat klik area widget
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openAppPendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)

                    // Bind Streak Status
                    if (hasSavedToday) {
                        views.setTextViewText(R.id.tv_widget_streak, "✓ Tercatat ($currentStreak Hari)")
                        views.setTextColor(R.id.tv_widget_streak, Color.parseColor("#10B981"))
                    } else {
                        views.setTextViewText(R.id.tv_widget_streak, "Belum Nabung")
                        views.setTextColor(R.id.tv_widget_streak, Color.parseColor("#F59E0B"))
                    }

                    // Bind Goal Info
                    if (primaryGoal != null) {
                        views.setTextViewText(R.id.tv_widget_goal_title, primaryGoal.title)
                        views.setTextViewText(
                            R.id.tv_widget_amount,
                            "${Formatters.formatRupiah(primaryGoal.currentAmount)} / ${Formatters.formatRupiah(primaryGoal.targetAmount)} (${primaryGoal.progressPercentage}%)"
                        )
                        views.setProgressBar(R.id.pb_widget_progress, 100, primaryGoal.progressPercentage, false)

                        val daysRemaining = primaryGoal.getEstimatedDaysRemaining(dailyTarget)
                        views.setTextViewText(R.id.tv_widget_estimate, "Estimasi: ~$daysRemaining hari lagi")
                    } else {
                        views.setTextViewText(R.id.tv_widget_goal_title, "Belum Ada Target")
                        views.setTextViewText(R.id.tv_widget_amount, "Rp 0")
                        views.setProgressBar(R.id.pb_widget_progress, 100, 0, false)
                        views.setTextViewText(R.id.tv_widget_estimate, "Buka aplikasi untuk membuat celengan")
                    }

                    // Bind 1-Tap Quick Action Button
                    val saveIntent = Intent(context, AngelAppWidgetProvider::class.java).apply {
                        action = ACTION_WIDGET_QUICK_SAVE
                    }
                    val savePendingIntent = PendingIntent.getBroadcast(
                        context,
                        widgetId + 2000,
                        saveIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )
                    views.setOnClickPendingIntent(R.id.btn_widget_save, savePendingIntent)

                    if (hasSavedToday) {
                        views.setTextViewText(R.id.btn_widget_save, "✓ Sudah Menabung Hari Ini")
                    } else {
                        views.setTextViewText(R.id.btn_widget_save, "⚡ Nabung ${Formatters.formatRupiah(dailyTarget)}")
                    }

                    appWidgetManager.updateAppWidget(widgetId, views)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_WIDGET_QUICK_SAVE) {
            val pendingResult = goAsync()
            scope.launch {
                try {
                    val dbHelper = AngelDatabaseHelper(context)
                    val prefs = UserPreferencesManager(context)
                    val dailyTarget = prefs.dailySavingTarget.first()

                    val goals = dbHelper.getAllGoals()
                    val targetGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()

                    if (targetGoal != null && dailyTarget > 0.0) {
                        dbHelper.addTransaction(
                            targetGoal.id,
                            dailyTarget,
                            TransactionType.DEPOSIT,
                            "Setoran Widget Homescreen"
                        )
                        // Putar audio lonceng Angel
                        NotificationHelper.playChimeSound(context)

                        // Sinkronkan data ke repository jika aplikasi sedang aktif
                        SavingsRepository.getInstance(context).refreshData()

                        // Perbarui seluruh widget di homescreen
                        updateAllWidgets(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
