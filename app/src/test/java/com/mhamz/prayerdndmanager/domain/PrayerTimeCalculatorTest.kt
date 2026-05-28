package com.mhamz.prayerdndmanager.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class PrayerTimeCalculatorTest {
    @Test
    fun nextStartUsesTodayWhenPrayerIsStillAhead() {
        val schedule = schedule(startHour = 13, startMinute = 30, endHour = 14, endMinute = 0)
        val now = LocalDateTime.of(2026, 5, 28, 10, 0)

        assertEquals(
            LocalDateTime.of(2026, 5, 28, 13, 30),
            PrayerTimeCalculator.nextStart(schedule, now)
        )
    }

    @Test
    fun endTimeBeforeStartEndsAfterMidnight() {
        val schedule = schedule(startHour = 23, startMinute = 45, endHour = 0, endMinute = 15)
        val start = LocalDateTime.of(2026, 5, 28, 23, 45)

        assertEquals(
            LocalDateTime.of(2026, 5, 29, 0, 15),
            PrayerTimeCalculator.endForStart(schedule, start)
        )
        assertTrue(
            PrayerTimeCalculator.isActiveAt(
                schedule,
                LocalDateTime.of(2026, 5, 29, 0, 5)
            )
        )
    }

    @Test
    fun fridayOnlyUsesTheNextFriday() {
        val thursday = LocalDate.of(2026, 5, 28)
        assertEquals(DayOfWeek.THURSDAY, thursday.dayOfWeek)
        val schedule = schedule(
            startHour = 13,
            startMinute = 15,
            endHour = 14,
            endMinute = 0,
            fridayOnly = true,
            repeatDaily = false
        )

        assertEquals(
            LocalDateTime.of(2026, 5, 29, 13, 15),
            PrayerTimeCalculator.nextStart(schedule, thursday.atTime(9, 0))
        )
    }

    @Test
    fun weekdayScheduleSkipsWeekend() {
        val schedule = schedule(
            startHour = 9,
            startMinute = 0,
            endHour = 10,
            endMinute = 0,
            repeatDaysMask = WEEKDAYS_MASK
        )

        assertEquals(
            LocalDateTime.of(2026, 6, 1, 9, 0),
            PrayerTimeCalculator.nextStart(schedule, LocalDateTime.of(2026, 5, 29, 10, 0))
        )
    }

    @Test
    fun customDaysUseTheNextSelectedDay() {
        val schedule = schedule(
            startHour = 11,
            startMinute = 30,
            endHour = 12,
            endMinute = 0,
            repeatDaysMask = dayOfWeekMask(DayOfWeek.MONDAY) or dayOfWeekMask(DayOfWeek.WEDNESDAY)
        )

        assertEquals(
            LocalDateTime.of(2026, 6, 1, 11, 30),
            PrayerTimeCalculator.nextStart(schedule, LocalDateTime.of(2026, 5, 28, 8, 0))
        )
    }

    @Test
    fun dailyRepeatMovesToTomorrowAfterStartTimePasses() {
        val schedule = schedule(startHour = 5, startMinute = 10, endHour = 5, endMinute = 40)

        assertEquals(
            LocalDateTime.of(2026, 5, 29, 5, 10),
            PrayerTimeCalculator.nextStart(schedule, LocalDateTime.of(2026, 5, 28, 6, 0))
        )
    }

    @Test
    fun oneShotStillCalculatesTheNextOccurrence() {
        val schedule = schedule(
            startHour = 8,
            startMinute = 0,
            endHour = 8,
            endMinute = 30,
            repeatDaily = false
        )

        assertEquals(
            LocalDateTime.of(2026, 5, 29, 8, 0),
            PrayerTimeCalculator.nextStart(schedule, LocalDateTime.of(2026, 5, 28, 9, 0))
        )
        assertFalse(schedule.repeatDaily)
    }

    private fun schedule(
        startHour: Int,
        startMinute: Int,
        endHour: Int,
        endMinute: Int,
        fridayOnly: Boolean = false,
        repeatDaily: Boolean = true,
        repeatDaysMask: Int = if (fridayOnly) FRIDAY_ONLY_MASK else if (repeatDaily) ALL_DAYS_MASK else NO_REPEAT_DAYS_MASK
    ) = PrayerSchedule(
        id = 1,
        name = "Test",
        startHour = startHour,
        startMinute = startMinute,
        endHour = endHour,
        endMinute = endMinute,
        enabled = true,
        repeatDaily = repeatDaily,
        fridayOnly = fridayOnly,
        repeatDaysMask = repeatDaysMask
    )
}
