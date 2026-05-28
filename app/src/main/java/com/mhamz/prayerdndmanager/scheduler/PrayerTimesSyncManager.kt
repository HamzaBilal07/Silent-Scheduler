package com.mhamz.prayerdndmanager.scheduler

import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.PrayerTimesRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrayerTimesSyncManager(
    private val repository: PrayerRepository,
    private val settingsStore: SettingsStore,
    private val prayerTimesRepository: PrayerTimesRepository,
    private val alarmScheduler: PrayerAlarmScheduler,
    private val dailySyncScheduler: DailyPrayerSyncScheduler
) {
    suspend fun refreshFromCurrentLocation(): Result<DailyPrayerTimes> {
        val settings = settingsStore.getSettings()
        return withContext(Dispatchers.IO) {
            prayerTimesRepository.fetchToday(settings.fiqhMethod)
                .onSuccess { applyTimes(it) }
        }
    }

    suspend fun refreshFromSavedLocation(): Result<DailyPrayerTimes> {
        val settings = settingsStore.getSettings()
        return withContext(Dispatchers.IO) {
            prayerTimesRepository.fetchTodayFromSavedLocation(settings.fiqhMethod)
                .onSuccess { applyTimes(it) }
        }
    }

    suspend fun refreshAutomatically(): Result<DailyPrayerTimes> {
        val settings = settingsStore.getSettings()
        return withContext(Dispatchers.IO) {
            val currentLocationResult = prayerTimesRepository.fetchToday(settings.fiqhMethod)
            if (currentLocationResult.isSuccess) {
                currentLocationResult.onSuccess { applyTimes(it) }
            } else {
                val savedLocationResult = prayerTimesRepository.fetchTodayFromSavedLocation(settings.fiqhMethod)
                if (savedLocationResult.isSuccess) {
                    savedLocationResult.onSuccess { applyTimes(it) }
                } else {
                    currentLocationResult
                }
            }
        }
    }

    fun scheduleNextDailySync() {
        dailySyncScheduler.scheduleNextSync()
    }

    private suspend fun applyTimes(times: DailyPrayerTimes) {
        val settings = settingsStore.getSettings()
        if (settings.autoUpdatePrayerTimes) {
            repository.applyDailyPrayerTimes(times)
            repository.getEnabledSchedules().forEach { alarmScheduler.schedule(it, settings) }
        }
        dailySyncScheduler.scheduleNextSync()
    }
}
