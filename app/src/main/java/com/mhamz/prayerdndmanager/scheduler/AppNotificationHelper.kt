package com.mhamz.prayerdndmanager.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mhamz.prayerdndmanager.MainActivity
import com.mhamz.prayerdndmanager.R
import com.mhamz.prayerdndmanager.permissions.PermissionHelper

class AppNotificationHelper(
    private val context: Context
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Silent Scheduler",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Prayer automation status and permission warnings"
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showPrayerStartingSoon(prayerName: String) {
        show(
            id = NOTIFICATION_PRE_PRAYER,
            title = "$prayerName starts soon",
            message = "Your phone will be silenced in about 5 minutes."
        )
    }

    fun showSilentEnabled(prayerName: String) {
        show(
            id = NOTIFICATION_ACTIVE,
            title = "Silent mode active",
            message = "$prayerName started. Previous sound mode will be restored after prayer."
        )
    }

    fun showRestored(prayerName: String) {
        show(
            id = NOTIFICATION_RESTORED,
            title = "Sound mode restored",
            message = "$prayerName ended."
        )
    }

    fun showPermissionWarning(message: String) {
        show(
            id = NOTIFICATION_WARNING,
            title = "Permission needed",
            message = message
        )
    }

    private fun show(id: Int, title: String, message: String) {
        ensureChannel()
        if (!PermissionHelper.hasNotificationPermission(context)) return
        val activityIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_prayer)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        notificationManager?.notify(id, notification)
    }

    companion object {
        private const val CHANNEL_ID = "prayer_silent_scheduler"
        private const val NOTIFICATION_PRE_PRAYER = 100
        private const val NOTIFICATION_ACTIVE = 101
        private const val NOTIFICATION_RESTORED = 102
        private const val NOTIFICATION_WARNING = 103
    }
}
