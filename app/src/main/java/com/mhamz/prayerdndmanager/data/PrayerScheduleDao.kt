package com.mhamz.prayerdndmanager.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerScheduleDao {
    @Query("SELECT * FROM prayer_schedules ORDER BY CASE name WHEN 'Fajr' THEN 1 WHEN 'Zuhr' THEN 2 WHEN 'Asr' THEN 3 WHEN 'Maghrib' THEN 4 WHEN 'Isha' THEN 5 WHEN 'Jummah' THEN 6 ELSE 7 END, startHour, startMinute")
    fun observeAll(): Flow<List<PrayerScheduleEntity>>

    @Query("SELECT * FROM prayer_schedules")
    suspend fun getAll(): List<PrayerScheduleEntity>

    @Query("SELECT * FROM prayer_schedules WHERE id = :id")
    suspend fun getById(id: Long): PrayerScheduleEntity?

    @Query("SELECT * FROM prayer_schedules WHERE enabled = 1")
    suspend fun getEnabled(): List<PrayerScheduleEntity>

    @Query("SELECT COUNT(*) FROM prayer_schedules")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: PrayerScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<PrayerScheduleEntity>)

    @Update
    suspend fun update(schedule: PrayerScheduleEntity)

    @Delete
    suspend fun delete(schedule: PrayerScheduleEntity)

    @Query("DELETE FROM prayer_schedules")
    suspend fun deleteAll()

    @Query("UPDATE prayer_schedules SET enabled = :enabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, updatedAt: Long)
}
