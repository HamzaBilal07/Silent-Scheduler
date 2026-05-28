package com.mhamz.prayerdndmanager.data

import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import com.mhamz.prayerdndmanager.domain.FiqhMethod
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class PrayerTimesRepository(
    private val locationProvider: LocationProvider,
    private val settingsStore: SettingsStore
) {
    suspend fun fetchToday(fiqhMethod: FiqhMethod): Result<DailyPrayerTimes> {
        val location = locationProvider.getLocation()
            ?: return Result.failure(IllegalStateException("Location permission or location signal is missing."))
        settingsStore.savePrayerLocation(location.latitude, location.longitude)
        return runCatching {
            fetchFromAlAdhan(
                latitude = location.latitude,
                longitude = location.longitude,
                fiqhMethod = fiqhMethod
            )
        }.mapPrayerTimingError()
    }

    suspend fun fetchTodayFromSavedLocation(fiqhMethod: FiqhMethod): Result<DailyPrayerTimes> {
        val location = settingsStore.getPrayerLocation()
            ?: return Result.failure(IllegalStateException("No saved location yet. Allow location access so prayer timings can update automatically."))
        return runCatching {
            fetchFromAlAdhan(
                latitude = location.latitude,
                longitude = location.longitude,
                fiqhMethod = fiqhMethod
            )
        }.mapPrayerTimingError()
    }

    fun fetchFromAlAdhan(
        latitude: Double,
        longitude: Double,
        fiqhMethod: FiqhMethod
    ): DailyPrayerTimes {
        val today = LocalDate.now()
        val date = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        val method = if (fiqhMethod == FiqhMethod.JAFRIA) 0 else 1
        val school = if (fiqhMethod == FiqhMethod.HANAFI) 1 else 0
        val midnightMode = if (fiqhMethod == FiqhMethod.JAFRIA) 1 else 0
        val url = URL(
            "https://api.aladhan.com/v1/timings/$date" +
                "?latitude=$latitude" +
                "&longitude=$longitude" +
                "&method=$method" +
                "&school=$school" +
                "&midnightMode=$midnightMode" +
                "&iso8601=false"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
        }
        return connection.use {
            if (responseCode !in 200..299) {
                throw IllegalStateException("Prayer times server returned HTTP $responseCode.")
            }
            val body = inputStream.bufferedReader().use { it.readText() }
            val timings = JSONObject(body)
                .getJSONObject("data")
                .getJSONObject("timings")
            DailyPrayerTimes(
                date = today,
                fiqhMethod = fiqhMethod,
                latitude = latitude,
                longitude = longitude,
                fajr = timings.localTime("Fajr"),
                sunrise = timings.localTime("Sunrise"),
                zuhr = timings.localTime("Dhuhr"),
                asr = timings.localTime("Asr"),
                maghrib = timings.localTime("Maghrib"),
                isha = timings.localTime("Isha")
            )
        }
    }

    private fun JSONObject.localTime(key: String): LocalTime {
        val raw = getString(key).substringBefore(" ").trim()
        return LocalTime.parse(raw, DateTimeFormatter.ofPattern("H:mm", Locale.US))
    }
}

private fun <T> Result<T>.mapPrayerTimingError(): Result<T> {
    return fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toPrayerTimingError()) }
    )
}

private fun Throwable.toPrayerTimingError(): Throwable {
    return when (this) {
        is UnknownHostException,
        is SocketTimeoutException,
        is IOException -> IllegalStateException("No internet connection. Check Wi-Fi or mobile data and try again.")
        else -> this
    }
}

private inline fun <T : HttpURLConnection, R> T.use(block: T.() -> R): R {
    return try {
        block()
    } finally {
        disconnect()
    }
}
