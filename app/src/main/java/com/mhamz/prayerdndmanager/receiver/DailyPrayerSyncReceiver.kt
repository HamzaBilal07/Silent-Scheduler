package com.mhamz.prayerdndmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhamz.prayerdndmanager.PrayerSilentSchedulerApplication
import com.mhamz.prayerdndmanager.scheduler.DailyPrayerSyncScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class DailyPrayerSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DailyPrayerSyncScheduler.ACTION_DAILY_PRAYER_SYNC) return
        val pendingResult = goAsync()
        val app = context.applicationContext as PrayerSilentSchedulerApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.prayerTimesSyncManager.refreshFromSavedLocation()
                app.container.prayerTimesSyncManager.scheduleNextDailySync()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
