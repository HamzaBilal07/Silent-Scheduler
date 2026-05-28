package com.mhamz.prayerdndmanager.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.AutomationState
import com.mhamz.prayerdndmanager.domain.FiqhMethod
import com.mhamz.prayerdndmanager.domain.SavedPrayerLocation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "prayer_silent_settings")

class SettingsStore(context: Context) {
    private val dataStore = context.applicationContext.settingsDataStore

    val settings: Flow<AppSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            AppSettings(
                restorePreviousMode = prefs[RESTORE_PREVIOUS_MODE] ?: true,
                notifyBeforePrayer = prefs[NOTIFY_BEFORE_PRAYER] ?: false,
                onboardingComplete = prefs[ONBOARDING_COMPLETE] ?: false,
                fiqhMethod = prefs[FIQH_METHOD]?.let { runCatching { FiqhMethod.valueOf(it) }.getOrNull() }
                    ?: FiqhMethod.HANAFI,
                autoUpdatePrayerTimes = prefs[AUTO_UPDATE_PRAYER_TIMES] ?: true
            )
        }

    val automationState: Flow<AutomationState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            AutomationState(
                activePrayerIds = prefs[ACTIVE_PRAYER_IDS].toIdSet(),
                previousInterruptionFilter = prefs[PREVIOUS_INTERRUPTION_FILTER],
                previousRingerMode = prefs[PREVIOUS_RINGER_MODE]
            )
        }

    suspend fun setRestorePreviousMode(value: Boolean) {
        dataStore.edit { it[RESTORE_PREVIOUS_MODE] = value }
    }

    suspend fun setNotifyBeforePrayer(value: Boolean) {
        dataStore.edit { it[NOTIFY_BEFORE_PRAYER] = value }
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[ONBOARDING_COMPLETE] = value }
    }

    suspend fun setFiqhMethod(value: FiqhMethod) {
        dataStore.edit { it[FIQH_METHOD] = value.name }
    }

    suspend fun setAutoUpdatePrayerTimes(value: Boolean) {
        dataStore.edit { it[AUTO_UPDATE_PRAYER_TIMES] = value }
    }

    suspend fun savePrayerLocation(latitude: Double, longitude: Double) {
        dataStore.edit {
            it[LAST_PRAYER_LATITUDE] = latitude
            it[LAST_PRAYER_LONGITUDE] = longitude
        }
    }

    suspend fun getPrayerLocation(): SavedPrayerLocation? {
        val prefs = dataStore.data.first()
        val latitude = prefs[LAST_PRAYER_LATITUDE]
        val longitude = prefs[LAST_PRAYER_LONGITUDE]
        return if (latitude != null && longitude != null) {
            SavedPrayerLocation(latitude, longitude)
        } else {
            null
        }
    }

    suspend fun getSettings(): AppSettings = settings.first()

    suspend fun getAutomationState(): AutomationState = automationState.first()

    suspend fun saveAutomationState(state: AutomationState) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_PRAYER_IDS] = state.activePrayerIds.sorted().joinToString(",")
            state.previousInterruptionFilter?.let {
                prefs[PREVIOUS_INTERRUPTION_FILTER] = it
            } ?: prefs.remove(PREVIOUS_INTERRUPTION_FILTER)
            state.previousRingerMode?.let {
                prefs[PREVIOUS_RINGER_MODE] = it
            } ?: prefs.remove(PREVIOUS_RINGER_MODE)
        }
    }

    suspend fun clearAutomationState() {
        saveAutomationState(AutomationState())
    }

    private fun String?.toIdSet(): Set<Long> {
        return this
            ?.split(",")
            ?.mapNotNull { it.trim().toLongOrNull() }
            ?.toSet()
            .orEmpty()
    }

    private companion object {
        val RESTORE_PREVIOUS_MODE = booleanPreferencesKey("restore_previous_mode")
        val NOTIFY_BEFORE_PRAYER = booleanPreferencesKey("notify_before_prayer")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val FIQH_METHOD = stringPreferencesKey("fiqh_method")
        val AUTO_UPDATE_PRAYER_TIMES = booleanPreferencesKey("auto_update_prayer_times")
        val LAST_PRAYER_LATITUDE = doublePreferencesKey("last_prayer_latitude")
        val LAST_PRAYER_LONGITUDE = doublePreferencesKey("last_prayer_longitude")
        val ACTIVE_PRAYER_IDS = stringPreferencesKey("active_prayer_ids")
        val PREVIOUS_INTERRUPTION_FILTER = intPreferencesKey("previous_interruption_filter")
        val PREVIOUS_RINGER_MODE = intPreferencesKey("previous_ringer_mode")
    }
}
