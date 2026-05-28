package com.mhamz.prayerdndmanager.domain

import androidx.compose.runtime.Immutable

@Immutable
data class AppSettings(
    val restorePreviousMode: Boolean = true,
    val notifyBeforePrayer: Boolean = false,
    val onboardingComplete: Boolean = false,
    val fiqhMethod: FiqhMethod = FiqhMethod.HANAFI,
    val autoUpdatePrayerTimes: Boolean = true
)

enum class FiqhMethod(val label: String) {
    HANAFI("Hanafi"),
    JAFRIA("Jafria")
}
