package com.mhamz.prayerdndmanager.scheduler

import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.PrayerTimeCalculator
import com.mhamz.prayerdndmanager.domain.isRepeating
import com.mhamz.prayerdndmanager.permissions.PermissionHelper
import java.time.LocalDateTime

class PrayerEventHandler(
    private val repository: PrayerRepository,
    private val settingsStore: SettingsStore,
    private val scheduler: PrayerAlarmScheduler,
    private val prayerTimesSyncManager: PrayerTimesSyncManager,
    private val silenceController: SilenceController,
    private val notifications: AppNotificationHelper,
    private val appContext: android.content.Context
) {
    suspend fun handleStart(scheduleId: Long) {
        val schedule = repository.getSchedule(scheduleId) ?: return
        if (!schedule.enabled) return

        if (!PermissionHelper.hasDndAccess(appContext)) {
            notifications.showPermissionWarning("Grant Do Not Disturb access so ${schedule.name} can silence the phone.")
            return
        }

        val started = silenceController.startPrayer(scheduleId)
        if (started) {
            notifications.showSilentEnabled(schedule.name)
        } else {
            notifications.showPermissionWarning("Silent mode could not be enabled for ${schedule.name}. Check Do Not Disturb access.")
        }
    }

    suspend fun handleEnd(scheduleId: Long) {
        val schedule = repository.getSchedule(scheduleId) ?: return
        val settings = settingsStore.getSettings()
        val restored = silenceController.endPrayer(scheduleId, settings.restorePreviousMode)
        if (restored) {
            notifications.showRestored(schedule.name)
        }

        if (schedule.isRepeating()) {
            scheduler.schedule(schedule, settings)
        } else {
            repository.setEnabled(scheduleId, false)
            scheduler.cancel(scheduleId)
        }
    }

    suspend fun handlePreNotify(scheduleId: Long) {
        val schedule = repository.getSchedule(scheduleId) ?: return
        if (schedule.enabled) {
            notifications.showPrayerStartingSoon(schedule.name)
        }
    }

    suspend fun handleQuickDndEnd() {
        val settings = settingsStore.getSettings()
        val restored = silenceController.endPrayer(
            prayerId = PrayerAlarmScheduler.QUICK_DND_ID,
            restorePreviousMode = settings.restorePreviousMode
        )
        if (restored) {
            notifications.showRestored("Quick DND")
        }
    }

    suspend fun handleBootOrPackageReplaced() {
        prayerTimesSyncManager.refreshFromSavedLocation()
        prayerTimesSyncManager.scheduleNextDailySync()
        val settings = settingsStore.getSettings()
        if (settingsStore.getAutomationState().activePrayerIds.contains(PrayerAlarmScheduler.QUICK_DND_ID)) {
            handleQuickDndEnd()
        }
        val enabled = repository.getEnabledSchedules()
        val now = LocalDateTime.now()
        enabled.forEach { schedule ->
            if (PrayerTimeCalculator.isActiveAt(schedule, now)) {
                if (PermissionHelper.hasDndAccess(appContext)) {
                    silenceController.startPrayer(schedule.id)
                } else {
                    notifications.showPermissionWarning("Grant Do Not Disturb access so active prayers can silence the phone.")
                }
                scheduler.scheduleEndForActivePrayer(schedule, now)
            } else {
                scheduler.schedule(schedule, settings)
            }
        }
    }
}
