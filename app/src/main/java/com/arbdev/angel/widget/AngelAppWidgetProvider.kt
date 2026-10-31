package com.arbdev.angel.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.SizeF
import android.widget.RemoteViews
import com.arbdev.angel.MainActivity
import com.arbdev.angel.R
import com.arbdev.angel.data.local.AngelDatabaseHelper
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
                    // PendingIntent Buka Aplikasi
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    val openAppPendingIntent = PendingIntent.getActivity(
                        context,
                        widgetId,
                        openAppIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    // PendingIntent 1-Tap Save
                    val saveIntent = Intent(context, AngelAppWidgetProvider::class.java).apply {
                        action = ACTION_WIDGET_QUICK_SAVE
                    }
                    val savePendingIntent = PendingIntent.getBroadcast(
                        context,
                        widgetId + 2000,
                        saveIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val saveBtnText = if (hasSavedToday) "✓ Sudah Nabung" else "⚡ Nabung ${Formatters.formatRupiah(dailyTarget)}"
                    val streakText = if (hasSavedToday) "✓ $currentStreak Hari" else "Belum Nabung"
                    val streakColor = if (hasSavedToday) Color.parseColor("#10B981") else Color.parseColor("#F59E0B")

                    // 1. Small View (2x2)
                    val smallViews = RemoteViews(context.packageName, R.layout.widget_angel_small).apply {
                        setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
                        setOnClickPendingIntent(R.id.btn_widget_save, savePendingIntent)
                        setTextViewText(R.id.tv_widget_small_amount, if (primaryGoal != null) Formatters.formatRupiah(primaryGoal.currentAmount) else "Rp 0")
                        setTextViewText(R.id.tv_widget_small_streak, streakText)
                        setTextColor(R.id.tv_widget_small_streak, streakColor)
                        setTextViewText(R.id.btn_widget_save, if (hasSavedToday) "✓ Selesai" else "⚡ +${(dailyTarget / 1000).toInt()}k")
                    }

                    // 2. Medium View (4x1 / 4x2)
                    val mediumViews = RemoteViews(context.packageName, R.layout.widget_angel_medium).apply {
                        setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
                        setOnClickPendingIntent(R.id.btn_widget_save, savePendingIntent)
                        setTextViewText(R.id.tv_widget_streak, streakText)
                        setTextColor(R.id.tv_widget_streak, streakColor)
                        setTextViewText(R.id.btn_widget_save, saveBtnText)

                        if (primaryGoal != null) {
                            setTextViewText(R.id.tv_widget_goal_title, primaryGoal.title)
                            setTextViewText(
                                R.id.tv_widget_amount,
                                "${Formatters.formatRupiah(primaryGoal.currentAmount)} / ${Formatters.formatRupiah(primaryGoal.targetAmount)} (${primaryGoal.progressPercentage}%)"
                            )
                            setProgressBar(R.id.pb_widget_progress, 100, primaryGoal.progressPercentage, false)
                            val days = primaryGoal.getEstimatedDaysRemaining(dailyTarget)
                            setTextViewText(R.id.tv_widget_estimate, "Estimasi: ~$days hari lagi")
                        } else {
                            setTextViewText(R.id.tv_widget_goal_title, "Belum Ada Target")
                            setTextViewText(R.id.tv_widget_amount, "Rp 0")
                            setProgressBar(R.id.pb_widget_progress, 100, 0, false)
                            setTextViewText(R.id.tv_widget_estimate, "Buka aplikasi untuk membuat target")
                        }
                    }

                    // 3. Large View (4x3 / 4x4)
                    val largeViews = RemoteViews(context.packageName, R.layout.widget_angel_large).apply {
                        setOnClickPendingIntent(R.id.widget_root, openAppPendingIntent)
                        setOnClickPendingIntent(R.id.btn_widget_save, savePendingIntent)
                        setTextViewText(R.id.tv_widget_streak, streakText)
                        setTextColor(R.id.tv_widget_streak, streakColor)
                        setTextViewText(R.id.btn_widget_save, saveBtnText)

                        if (primaryGoal != null) {
                            setTextViewText(R.id.tv_widget_goal_title, primaryGoal.title)
                            setTextViewText(
                                R.id.tv_widget_amount,
                                "${Formatters.formatRupiah(primaryGoal.currentAmount)} / ${Formatters.formatRupiah(primaryGoal.targetAmount)} (${primaryGoal.progressPercentage}%)"
                            )
                            setProgressBar(R.id.pb_widget_progress, 100, primaryGoal.progressPercentage, false)
                            val days = primaryGoal.getEstimatedDaysRemaining(dailyTarget)
                            val completionDate = primaryGoal.getEstimatedCompletionDateMillis(dailyTarget)
                            setTextViewText(R.id.tv_widget_estimate, "Estimasi: ~$days hari lagi (${Formatters.formatDate(completionDate)})")
                        } else {
                            setTextViewText(R.id.tv_widget_goal_title, "Belum Ada Target")
                            setTextViewText(R.id.tv_widget_amount, "Rp 0")
                            setProgressBar(R.id.pb_widget_progress, 100, 0, false)
                            setTextViewText(R.id.tv_widget_estimate, "Mulai impian Anda sekarang")
                        }
                    }

                    // Responsive RemoteViews untuk Android 12+ (API 31+) & Android 14 (API 34+)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val viewMapping = mapOf(
                            SizeF(120f, 90f) to smallViews,
                            SizeF(200f, 100f) to mediumViews,
                            SizeF(240f, 200f) to largeViews
                        )
                        appWidgetManager.updateAppWidget(widgetId, RemoteViews(viewMapping))
                    } else {
                        appWidgetManager.updateAppWidget(widgetId, mediumViews)
                    }
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
                    val repository = SavingsRepository.getInstance(context)
                    val prefs = UserPreferencesManager(context)
                    val dailyTarget = prefs.dailySavingTarget.first()

                    val goals = repository.goals.value
                    val targetGoal = goals.firstOrNull { !it.isCompleted } ?: goals.firstOrNull()

                    if (targetGoal != null && dailyTarget > 0.0) {
                        val success = repository.saveMoney(
                            targetGoal.id,
                            dailyTarget,
                            "Setoran Widget Homescreen"
                        )
                        if (success) {
                            NotificationHelper.playChimeSound(context)
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
