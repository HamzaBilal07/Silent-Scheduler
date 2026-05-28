package com.mhamz.prayerdndmanager.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.mhamz.prayerdndmanager.receiver.DailyPrayerSyncReceiver
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class DailyPrayerSyncScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun scheduleNextSync() {
        val now = LocalDateTime.now()
        var next = LocalDateTime.of(now.toLocalDate(), LocalTime.of(2, 10))
        if (!next.isAfter(now)) next = next.plusDays(1)
        val triggerAt = next.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val intent = Intent(context, DailyPrayerSyncReceiver::class.java).apply {
            action = ACTION_DAILY_PRAYER_SYNC
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_DAILY_SYNC,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    companion object {
        const val ACTION_DAILY_PRAYER_SYNC = "com.mhamz.prayerdndmanager.DAILY_PRAYER_SYNC"
        private const val REQUEST_DAILY_SYNC = 410_000
    }
}
