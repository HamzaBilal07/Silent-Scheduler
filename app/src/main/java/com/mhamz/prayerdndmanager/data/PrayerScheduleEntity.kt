package com.mhamz.prayerdndmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mhamz.prayerdndmanager.domain.PrayerSchedule

@Entity(tableName = "prayer_schedules")
data class PrayerScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val enabled: Boolean,
    val repeatDaily: Boolean,
    val fridayOnly: Boolean,
    val repeatDaysMask: Int,
    val createdAt: Long,
    val updatedAt: Long
)

fun PrayerScheduleEntity.toDomain(): PrayerSchedule = PrayerSchedule(
    id = id,
    name = name,
    startHour = startHour,
    startMinute = startMinute,
    endHour = endHour,
    endMinute = endMinute,
    enabled = enabled,
    repeatDaily = repeatDaily,
    fridayOnly = fridayOnly,
    repeatDaysMask = repeatDaysMask,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun PrayerSchedule.toEntity(): PrayerScheduleEntity = PrayerScheduleEntity(
    id = id,
    name = name,
    startHour = startHour,
    startMinute = startMinute,
    endHour = endHour,
    endMinute = endMinute,
    enabled = enabled,
    repeatDaily = repeatDaily,
    fridayOnly = fridayOnly,
    repeatDaysMask = repeatDaysMask,
    createdAt = createdAt,
    updatedAt = updatedAt
)
