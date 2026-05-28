package com.mhamz.prayerdndmanager.data

import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import kotlinx.coroutines.flow.Flow

interface PrayerRepository {
    val schedules: Flow<List<PrayerSchedule>>

    suspend fun getSchedule(id: Long): PrayerSchedule?
    suspend fun getEnabledSchedules(): List<PrayerSchedule>
    suspend fun save(schedule: PrayerSchedule): Long
    suspend fun delete(schedule: PrayerSchedule)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun applyDailyPrayerTimes(times: DailyPrayerTimes): List<PrayerSchedule>
    suspend fun clearSchedules()
}
