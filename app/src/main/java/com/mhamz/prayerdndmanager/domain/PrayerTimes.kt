package com.mhamz.prayerdndmanager.domain

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class DailyPrayerTimes(
    val date: LocalDate,
    val fiqhMethod: FiqhMethod,
    val latitude: Double,
    val longitude: Double,
    val fajr: LocalTime,
    val sunrise: LocalTime,
    val zuhr: LocalTime,
    val asr: LocalTime,
    val maghrib: LocalTime,
    val isha: LocalTime
) {
    fun rows(): List<PrayerTimeRow> {
        return listOf(
            PrayerTimeRow("Fajr", fajr, sunrise),
            PrayerTimeRow("Zuhr", zuhr, asr),
            PrayerTimeRow("Asr", asr, maghrib),
            PrayerTimeRow("Maghrib", maghrib, isha),
            PrayerTimeRow("Isha", isha, fajr)
        )
    }
}

@Immutable
data class PrayerTimeRow(
    val name: String,
    val start: LocalTime,
    val end: LocalTime
)

@Immutable
data class QuranAyah(
    val reference: String,
    val arabic: String,
    val urdu: String
)

@Immutable
data class SavedPrayerLocation(
    val latitude: Double,
    val longitude: Double
)
