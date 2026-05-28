package com.mhamz.prayerdndmanager.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.domain.PrayerTimeCalculator
import com.mhamz.prayerdndmanager.permissions.PermissionHelper
import com.mhamz.prayerdndmanager.receiver.PrayerAlarmReceiver
import java.time.LocalDateTime

class PrayerAlarmScheduler(
    private val context: Context
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(schedule: PrayerSchedule, settings: AppSettings) {
        if (!schedule.enabled) {
            cancel(schedule.id)
            return
        }
        val start = PrayerTimeCalculator.nextStart(schedule)
        val end = PrayerTimeCalculator.endForStart(schedule, start)
        scheduleAt(
            schedule.id,
            ACTION_PRAYER_START,
            requestCode(REQUEST_START, schedule.id),
            PrayerTimeCalculator.toEpochMillis(start)
        )
        scheduleAt(
            schedule.id,
            ACTION_PRAYER_END,
            requestCode(REQUEST_END, schedule.id),
            PrayerTimeCalculator.toEpochMillis(end)
        )
        if (settings.notifyBeforePrayer) {
            val notificationTime = start.minusMinutes(5)
            if (notificationTime.isAfter(LocalDateTime.now())) {
                scheduleAt(
                    schedule.id,
                    ACTION_PRAYER_PRE_NOTIFY,
                    requestCode(REQUEST_PRE_NOTIFY, schedule.id),
                    PrayerTimeCalculator.toEpochMillis(notificationTime)
                )
            }
        } else {
            cancelIntent(schedule.id, ACTION_PRAYER_PRE_NOTIFY, requestCode(REQUEST_PRE_NOTIFY, schedule.id))
        }
    }

    fun scheduleEndForActivePrayer(schedule: PrayerSchedule, now: LocalDateTime = LocalDateTime.now()) {
        val end = PrayerTimeCalculator.endForActiveWindow(schedule, now) ?: return
        scheduleAt(
            schedule.id,
            ACTION_PRAYER_END,
            requestCode(REQUEST_END, schedule.id),
            PrayerTimeCalculator.toEpochMillis(end)
        )
    }

    fun scheduleAll(schedules: List<PrayerSchedule>, settings: AppSettings) {
        schedules.forEach { schedule(it, settings) }
    }

    fun cancel(scheduleId: Long) {
        cancelIntent(scheduleId, ACTION_PRAYER_START, requestCode(REQUEST_START, scheduleId))
        cancelIntent(scheduleId, ACTION_PRAYER_END, requestCode(REQUEST_END, scheduleId))
        cancelIntent(scheduleId, ACTION_PRAYER_PRE_NOTIFY, requestCode(REQUEST_PRE_NOTIFY, scheduleId))
    }

    fun scheduleQuickDndEnd(durationMinutes: Long): LocalDateTime {
        val endTime = LocalDateTime.now().plusMinutes(durationMinutes)
        scheduleAt(
            QUICK_DND_ID,
            ACTION_QUICK_DND_END,
            REQUEST_QUICK_DND_END,
            PrayerTimeCalculator.toEpochMillis(endTime)
        )
        return endTime
    }

    fun cancelQuickDnd() {
        cancelIntent(QUICK_DND_ID, ACTION_QUICK_DND_END, REQUEST_QUICK_DND_END)
    }

    private fun scheduleAt(scheduleId: Long, action: String, requestCode: Int, triggerAtMillis: Long) {
        val pendingIntent = pendingIntent(scheduleId, action, requestCode)
        if (PermissionHelper.canScheduleExactAlarms(context)) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            // Keep the automation usable if permission is revoked, but the UI warns that timing may drift.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        }
    }

    private fun cancelIntent(scheduleId: Long, action: String, requestCode: Int) {
        alarmManager?.cancel(pendingIntent(scheduleId, action, requestCode))
    }

    private fun pendingIntent(scheduleId: Long, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            this.action = action
            data = Uri.parse("silent-scheduler://alarm/${action.substringAfterLast('.')}/$scheduleId")
            putExtra(EXTRA_SCHEDULE_ID, scheduleId)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_PRAYER_START = "com.mhamz.prayerdndmanager.PRAYER_START"
        const val ACTION_PRAYER_END = "com.mhamz.prayerdndmanager.PRAYER_END"
        const val ACTION_PRAYER_PRE_NOTIFY = "com.mhamz.prayerdndmanager.PRAYER_PRE_NOTIFY"
        const val ACTION_QUICK_DND_END = "com.mhamz.prayerdndmanager.QUICK_DND_END"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val QUICK_DND_ID = -7777L

        private const val REQUEST_START = 100_000
        private const val REQUEST_END = 200_000
        private const val REQUEST_PRE_NOTIFY = 300_000
        private const val REQUEST_QUICK_DND_END = 400_000

        private fun requestCode(base: Int, scheduleId: Long): Int {
            return base + Math.floorMod(scheduleId, 1_000_000L).toInt()
        }
    }
}
