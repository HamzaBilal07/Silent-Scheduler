package com.mhamz.prayerdndmanager.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mhamz.prayerdndmanager.PrayerSilentSchedulerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (
            action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            return
        }
        val pendingResult = goAsync()
        val app = context.applicationContext as PrayerSilentSchedulerApplication
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                app.container.eventHandler.handleBootOrPackageReplaced()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
