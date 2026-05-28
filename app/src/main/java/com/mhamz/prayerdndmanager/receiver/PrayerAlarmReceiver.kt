package com.mhamz.prayerdndmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhamz.prayerdndmanager.PrayerSilentSchedulerApplication
import com.mhamz.prayerdndmanager.scheduler.PrayerAlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class PrayerAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.applicationContext as PrayerSilentSchedulerApplication
        val scheduleId = intent.getLongExtra(PrayerAlarmScheduler.EXTRA_SCHEDULE_ID, -1L)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    PrayerAlarmScheduler.ACTION_QUICK_DND_END -> app.container.eventHandler.handleQuickDndEnd()
                    else -> {
                        if (scheduleId > 0L) {
                            when (intent.action) {
                                PrayerAlarmScheduler.ACTION_PRAYER_START -> app.container.eventHandler.handleStart(scheduleId)
                                PrayerAlarmScheduler.ACTION_PRAYER_END -> app.container.eventHandler.handleEnd(scheduleId)
                                PrayerAlarmScheduler.ACTION_PRAYER_PRE_NOTIFY -> app.container.eventHandler.handlePreNotify(scheduleId)
                            }
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
