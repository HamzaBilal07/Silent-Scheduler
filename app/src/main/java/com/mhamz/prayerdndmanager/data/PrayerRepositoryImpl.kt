package com.mhamz.prayerdndmanager.data

import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import com.mhamz.prayerdndmanager.domain.ALL_DAYS_MASK
import com.mhamz.prayerdndmanager.domain.FRIDAY_ONLY_MASK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

class PrayerRepositoryImpl(
    private val dao: PrayerScheduleDao
) : PrayerRepository {
    override val schedules: Flow<List<PrayerSchedule>> = dao.observeAll()
        .map { entities -> entities.map { it.toDomain() } }

    override suspend fun getSchedule(id: Long): PrayerSchedule? {
        return dao.getById(id)?.toDomain()
    }

    override suspend fun getEnabledSchedules(): List<PrayerSchedule> {
        return dao.getEnabled().map { it.toDomain() }
    }

    override suspend fun save(schedule: PrayerSchedule): Long {
        val now = System.currentTimeMillis()
        val mask = schedule.repeatDaysMask
        val normalized = schedule.copy(
            repeatDaily = mask == ALL_DAYS_MASK,
            fridayOnly = mask == FRIDAY_ONLY_MASK,
            updatedAt = now,
            createdAt = if (schedule.id == 0L) now else schedule.createdAt
        )
        return if (normalized.id == 0L) {
            dao.insert(normalized.toEntity())
        } else {
            dao.update(normalized.toEntity())
            normalized.id
        }
    }

    override suspend fun delete(schedule: PrayerSchedule) {
        dao.delete(schedule.toEntity())
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        dao.setEnabled(id, enabled, System.currentTimeMillis())
    }

    override suspend fun applyDailyPrayerTimes(times: DailyPrayerTimes): List<PrayerSchedule> {
        val current = dao.getAll().map { it.toDomain() }
        val updates = current.mapNotNull { schedule ->
            when (schedule.name.trim().lowercase()) {
                "fajr" -> prayerUpdate(schedule, times.fajr, times.sunrise)
                "zuhr" -> prayerUpdate(schedule, times.zuhr, times.asr)
                "asr" -> prayerUpdate(schedule, times.asr, times.maghrib)
                "maghrib" -> prayerUpdate(schedule, times.maghrib, times.isha)
                "isha" -> prayerUpdate(schedule, times.isha, times.fajr)
                "jummah" -> prayerUpdate(schedule, times.zuhr, times.asr)
                else -> null
            }
        }
        updates.forEach { save(it) }
        return updates
    }

    override suspend fun clearSchedules() {
        dao.deleteAll()
    }

    private fun prayerUpdate(
        existing: PrayerSchedule,
        start: LocalTime,
        end: LocalTime
    ): PrayerSchedule {
        val now = System.currentTimeMillis()
        val repeatDaysMask = existing.repeatDaysMask
        return PrayerSchedule(
            id = existing.id,
            name = existing.name,
            startHour = start.hour,
            startMinute = start.minute,
            endHour = end.hour,
            endMinute = end.minute,
            enabled = existing.enabled,
            repeatDaily = repeatDaysMask == ALL_DAYS_MASK,
            fridayOnly = repeatDaysMask == FRIDAY_ONLY_MASK,
            repeatDaysMask = repeatDaysMask,
            createdAt = existing.createdAt,
            updatedAt = now
        )
    }
}
