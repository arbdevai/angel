package com.arbdev.angel.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.arbdev.angel.MainActivity
import com.arbdev.angel.R
import com.arbdev.angel.widget.AngelAppWidgetProvider

object NotificationHelper {

    const val CHANNEL_ID = "angel_daily_reminder_channel"
    const val CHANNEL_NAME = "Pengingat Celengan Angel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                        context.packageName + "/" + R.raw.angel_chime
            )

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Pengingat evaluasi harian celengan Angel"
                enableLights(true)
                enableVibration(true)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendReminderNotification(
        context: Context,
        title: String = "Waktunya Mengisi Celengan Angel",
        message: String = "Anda belum menabung hari ini. Sisihkan tabungan agar target impian Anda segera tercapai."
    ) {
        createNotificationChannel(context)

        // Intent Buka Aplikasi
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Langsung: 1-Tap Quick Save langsung dari notifikasi
        val quickSaveIntent = Intent(context, AngelAppWidgetProvider::class.java).apply {
            action = AngelAppWidgetProvider.ACTION_WIDGET_QUICK_SAVE
        }
        val quickSavePendingIntent = PendingIntent.getBroadcast(
            context,
            101,
            quickSaveIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                    context.packageName + "/" + R.raw.angel_chime
        )

        val largeIconBitmap = try {
            BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher)
        } catch (_: Exception) {
            null
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_angel)
            .setLargeIcon(largeIconBitmap)
            .setContentTitle(title)
            .setContentText(message)
            .setSubText("Celengan Digital")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setSummaryText("Target Tabungan Harian")
            )
            .setColor(0xFF2563EB.toInt())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setShowWhen(true)
            .setSound(soundUri)
            .addAction(
                R.drawable.ic_notification_angel,
                "⚡ Setor Sekarang",
                quickSavePendingIntent
            )
            .addAction(
                R.drawable.ic_notification_angel,
                "Buka Aplikasi",
                openAppPendingIntent
            )

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(NOTIFICATION_ID, builder.build())
        } catch (_: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    fun playChimeSound(context: Context) {
        try {
            val mediaPlayer = MediaPlayer.create(context, R.raw.angel_chime)
            mediaPlayer?.setOnCompletionListener { mp ->
                try {
                    mp.release()
                } catch (_: Exception) {
                }
            }
            mediaPlayer?.start()
        } catch (_: Exception) {
        }
    }
}
