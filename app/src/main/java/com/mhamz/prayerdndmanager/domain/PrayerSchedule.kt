package com.mhamz.prayerdndmanager.domain

import androidx.compose.runtime.Immutable
import java.time.DayOfWeek

const val NO_REPEAT_DAYS_MASK = 0
val ALL_DAYS_MASK: Int = DayOfWeek.entries.fold(0) { mask, day -> mask or dayOfWeekMask(day) }
val WEEKDAYS_MASK: Int = listOf(
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY
).fold(0) { mask, day -> mask or dayOfWeekMask(day) }
val FRIDAY_ONLY_MASK: Int = dayOfWeekMask(DayOfWeek.FRIDAY)

@Immutable
data class PrayerSchedule(
    val id: Long = 0,
    val name: String,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val enabled: Boolean = false,
    val repeatDaily: Boolean = true,
    val fridayOnly: Boolean = false,
    val repeatDaysMask: Int = if (fridayOnly) FRIDAY_ONLY_MASK else if (repeatDaily) ALL_DAYS_MASK else NO_REPEAT_DAYS_MASK,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

val defaultPrayerNames = listOf("Lecture", "Fajr", "Zuhr", "Asr", "Maghrib", "Isha", "Jummah")

fun dayOfWeekMask(day: DayOfWeek): Int = 1 shl (day.value - 1)

fun Int.hasDay(day: DayOfWeek): Boolean = this and dayOfWeekMask(day) != 0

fun Int.withDay(day: DayOfWeek, enabled: Boolean): Int {
    val bit = dayOfWeekMask(day)
    return if (enabled) this or bit else this and bit.inv()
}

fun Int.toRepeatDays(): List<DayOfWeek> {
    return DayOfWeek.entries.filter { hasDay(it) }
}

fun PrayerSchedule.isRepeating(): Boolean = repeatDaysMask != NO_REPEAT_DAYS_MASK

fun PrayerSchedule.runsOn(day: DayOfWeek): Boolean {
    return !isRepeating() || repeatDaysMask.hasDay(day)
}

fun repeatLabelForMask(mask: Int): String {
    return when (mask) {
        NO_REPEAT_DAYS_MASK -> "One time"
        ALL_DAYS_MASK -> "Repeats daily"
        WEEKDAYS_MASK -> "Mon-Fri"
        FRIDAY_ONLY_MASK -> "Friday only"
        else -> mask.toRepeatDays()
            .joinToString(", ") { it.name.take(3).lowercase().replaceFirstChar(Char::uppercase) }
    }
}
