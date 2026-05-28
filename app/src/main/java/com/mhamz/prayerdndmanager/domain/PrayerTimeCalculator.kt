package com.mhamz.prayerdndmanager.domain

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object PrayerTimeCalculator {
    fun nextStart(schedule: PrayerSchedule, now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val startTime = schedule.startTime()
        if (!schedule.isRepeating()) {
            val todayStart = LocalDateTime.of(now.toLocalDate(), startTime)
            return if (todayStart.isAfter(now)) todayStart else todayStart.plusDays(1)
        }

        for (offset in 0..7) {
            val date = now.toLocalDate().plusDays(offset.toLong())
            if (schedule.runsOn(date.dayOfWeek)) {
                val candidate = LocalDateTime.of(date, startTime)
                if (candidate.isAfter(now)) return candidate
            }
        }

        return LocalDateTime.of(now.toLocalDate().plusDays(1), startTime)
    }

    fun endForStart(schedule: PrayerSchedule, start: LocalDateTime): LocalDateTime {
        var end = LocalDateTime.of(start.toLocalDate(), schedule.endTime())
        if (!end.isAfter(start)) {
            end = end.plusDays(1)
        }
        return end
    }

    fun endForActiveWindow(schedule: PrayerSchedule, now: LocalDateTime = LocalDateTime.now()): LocalDateTime? {
        val activeStart = activeStartAt(schedule, now) ?: return null
        return endForStart(schedule, activeStart)
    }

    fun isActiveAt(schedule: PrayerSchedule, now: LocalDateTime = LocalDateTime.now()): Boolean {
        return activeStartAt(schedule, now) != null
    }

    fun nextUpcoming(
        schedules: List<PrayerSchedule>,
        now: LocalDateTime = LocalDateTime.now()
    ): Pair<PrayerSchedule, LocalDateTime>? {
        return schedules
            .filter { it.enabled }
            .map { it to nextStart(it, now) }
            .minByOrNull { it.second }
    }

    fun toEpochMillis(dateTime: LocalDateTime, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return dateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun activeStartAt(schedule: PrayerSchedule, now: LocalDateTime): LocalDateTime? {
        if (!schedule.enabled) return null
        val candidateDates = listOf(now.toLocalDate(), now.toLocalDate().minusDays(1))
        return candidateDates
            .mapNotNull { date ->
                if (!schedule.runsOn(date.dayOfWeek)) {
                    null
                } else {
                    LocalDateTime.of(date, schedule.startTime())
                }
            }
            .firstOrNull { start ->
                val end = endForStart(schedule, start)
                !now.isBefore(start) && now.isBefore(end)
            }
    }
}

fun PrayerSchedule.startTime(): LocalTime = LocalTime.of(startHour, startMinute)

fun PrayerSchedule.endTime(): LocalTime = LocalTime.of(endHour, endMinute)

fun PrayerSchedule.timeRangeText(): String {
    return "${startTime().toDisplayText()} - ${endTime().toDisplayText()}"
}

fun LocalTime.toDisplayText(): String {
    val hour12 = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val suffix = if (hour < 12) "AM" else "PM"
    return "%d:%02d %s".format(hour12, minute, suffix)
}

fun LocalDate.isFriday(): Boolean = dayOfWeek == DayOfWeek.FRIDAY
