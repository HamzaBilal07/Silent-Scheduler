package com.mhamz.prayerdndmanager.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.mhamz.prayerdndmanager.MainActivity
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
        scheduleEndAlarms(schedule.id, PrayerTimeCalculator.toEpochMillis(end))
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
        scheduleEndAlarms(schedule.id, PrayerTimeCalculator.toEpochMillis(end))
    }

    fun scheduleAll(schedules: List<PrayerSchedule>, settings: AppSettings) {
        schedules.forEach { schedule(it, settings) }
    }

    fun cancel(scheduleId: Long) {
        cancelIntent(scheduleId, ACTION_PRAYER_START, requestCode(REQUEST_START, scheduleId))
        cancelIntent(scheduleId, ACTION_PRAYER_END, requestCode(REQUEST_END, scheduleId))
        cancelIntent(scheduleId, ACTION_PRAYER_END_BACKUP, requestCode(REQUEST_END_BACKUP, scheduleId))
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
        if (isCriticalAutomationAction(action)) {
            // AlarmClock alarms are allowed to wake the app process even when the UI is closed.
            alarmManager?.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAtMillis, showIntent(scheduleId, action, requestCode)),
                pendingIntent
            )
        } else if (PermissionHelper.canScheduleExactAlarms(context)) {
            alarmManager?.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager?.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    private fun scheduleEndAlarms(scheduleId: Long, endAtMillis: Long) {
        scheduleAt(
            scheduleId,
            ACTION_PRAYER_END,
            requestCode(REQUEST_END, scheduleId),
            endAtMillis
        )
        scheduleAt(
            scheduleId,
            ACTION_PRAYER_END_BACKUP,
            requestCode(REQUEST_END_BACKUP, scheduleId),
            endAtMillis + RESTORE_WATCHDOG_DELAY_MS
        )
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

    private fun showIntent(scheduleId: Long, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            data = Uri.parse("silent-scheduler://open/${action.substringAfterLast('.')}/$scheduleId")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context,
            requestCode + REQUEST_SHOW_OFFSET,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val ACTION_PRAYER_START = "com.mhamz.prayerdndmanager.PRAYER_START"
        const val ACTION_PRAYER_END = "com.mhamz.prayerdndmanager.PRAYER_END"
        const val ACTION_PRAYER_END_BACKUP = "com.mhamz.prayerdndmanager.PRAYER_END_BACKUP"
        const val ACTION_PRAYER_PRE_NOTIFY = "com.mhamz.prayerdndmanager.PRAYER_PRE_NOTIFY"
        const val ACTION_QUICK_DND_END = "com.mhamz.prayerdndmanager.QUICK_DND_END"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
        const val QUICK_DND_ID = -7777L

        private const val REQUEST_START = 100_000
        private const val REQUEST_END = 200_000
        private const val REQUEST_END_BACKUP = 250_000
        private const val REQUEST_PRE_NOTIFY = 300_000
        private const val REQUEST_QUICK_DND_END = 400_000
        private const val REQUEST_SHOW_OFFSET = 2_000_000
        private const val RESTORE_WATCHDOG_DELAY_MS = 60_000L

        private fun isCriticalAutomationAction(action: String): Boolean {
            return action == ACTION_PRAYER_START ||
                action == ACTION_PRAYER_END ||
                action == ACTION_PRAYER_END_BACKUP ||
                action == ACTION_QUICK_DND_END
        }

        private fun requestCode(base: Int, scheduleId: Long): Int {
            return base + Math.floorMod(scheduleId, 1_000_000L).toInt()
        }
    }
}
