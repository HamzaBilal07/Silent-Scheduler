package com.mhamz.prayerdndmanager.scheduler

import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.PrayerSchedule
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

        val now = LocalDateTime.now()
        val settings = settingsStore.getSettings()
        if (!PrayerTimeCalculator.isActiveAt(schedule, now)) {
            finishExpiredOrReschedule(schedule, settings)
            return
        }

        if (!PermissionHelper.hasDndAccess(appContext)) {
            notifications.showPermissionWarning("Grant Do Not Disturb access so ${schedule.name} can silence the phone.")
            scheduler.scheduleEndForActivePrayer(schedule, now)
            return
        }

        val started = silenceController.startPrayer(scheduleId)
        if (started) {
            notifications.showSilentEnabled(schedule.name)
            scheduler.scheduleEndForActivePrayer(schedule, now)
        } else {
            notifications.showPermissionWarning("Silent mode could not be enabled for ${schedule.name}. Check Do Not Disturb access.")
        }
    }

    suspend fun handleEnd(scheduleId: Long) {
        val settings = settingsStore.getSettings()
        val schedule = repository.getSchedule(scheduleId)
        if (schedule == null) {
            silenceController.endPrayer(scheduleId, settings.restorePreviousMode)
            scheduler.cancel(scheduleId)
            return
        }

        val restored = silenceController.endPrayer(scheduleId, settings.restorePreviousMode)
        if (restored) {
            notifications.showRestored(schedule.name)
        }

        finishExpiredOrReschedule(schedule, settings)
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
        if (settingsStore.getAutomationState().activePrayerIds.contains(PrayerAlarmScheduler.QUICK_DND_ID)) {
            handleQuickDndEnd()
        }
        reconcileAutomation()
    }

    suspend fun reconcileAutomation() {
        val settings = settingsStore.getSettings()
        val enabled = repository.getEnabledSchedules()
        val now = LocalDateTime.now()
        val byId = enabled.associateBy { it.id }
        val storedState = settingsStore.getAutomationState()

        storedState.activePrayerIds
            .filter { it != PrayerAlarmScheduler.QUICK_DND_ID }
            .forEach { activeId ->
                val schedule = byId[activeId]
                val stillActive = schedule != null && PrayerTimeCalculator.isActiveAt(schedule, now)
                if (!stillActive) {
                    silenceController.endPrayer(activeId, settings.restorePreviousMode)
                    if (schedule != null && !schedule.isRepeating()) {
                        repository.setEnabled(activeId, false)
                        scheduler.cancel(activeId)
                    }
                }
            }

        val activeNow = enabled.filter { PrayerTimeCalculator.isActiveAt(it, now) }
        val latestActiveIds = settingsStore.getAutomationState().activePrayerIds
        activeNow.forEach { schedule ->
            if (!latestActiveIds.contains(schedule.id)) {
                if (PermissionHelper.hasDndAccess(appContext)) {
                    silenceController.startPrayer(schedule.id)
                } else {
                    notifications.showPermissionWarning("Grant Do Not Disturb access so ${schedule.name} can silence the phone.")
                }
            }
            scheduler.scheduleEndForActivePrayer(schedule, now)
        }

        enabled
            .filterNot { activeNow.any { active -> active.id == it.id } }
            .forEach { schedule ->
                scheduler.schedule(schedule, settings)
            }
    }

    private suspend fun finishExpiredOrReschedule(
        schedule: PrayerSchedule,
        settings: AppSettings
    ) {
        if (schedule.isRepeating()) {
            scheduler.schedule(schedule, settings)
        } else {
            repository.setEnabled(schedule.id, false)
            scheduler.cancel(schedule.id)
        }
    }
}
