package com.mhamz.prayerdndmanager.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.AutomationState
import com.mhamz.prayerdndmanager.domain.FiqhMethod
import com.mhamz.prayerdndmanager.scheduler.PrayerAlarmScheduler
import com.mhamz.prayerdndmanager.scheduler.PrayerTimesSyncManager
import com.mhamz.prayerdndmanager.scheduler.SilenceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val automationState: AutomationState = AutomationState(),
    val testMessage: String? = null
)

class SettingsViewModel(
    private val repository: PrayerRepository,
    private val settingsStore: SettingsStore,
    private val scheduler: PrayerAlarmScheduler,
    private val prayerTimesSyncManager: PrayerTimesSyncManager,
    private val silenceController: SilenceController
) : ViewModel() {
    private val testMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsStore.settings,
        settingsStore.automationState,
        testMessage
    ) { settings, automationState, message ->
        SettingsUiState(settings, automationState, message)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        SettingsUiState()
    )

    fun setRestorePreviousMode(value: Boolean) {
        viewModelScope.launch { settingsStore.setRestorePreviousMode(value) }
    }

    fun setNotifyBeforePrayer(value: Boolean) {
        viewModelScope.launch {
            settingsStore.setNotifyBeforePrayer(value)
            val settings = settingsStore.getSettings()
            repository.getEnabledSchedules().forEach { scheduler.schedule(it, settings) }
        }
    }

    fun setFiqhMethod(value: FiqhMethod) {
        viewModelScope.launch {
            settingsStore.setFiqhMethod(value)
            prayerTimesSyncManager.refreshFromSavedLocation()
        }
    }

    fun setAutoUpdatePrayerTimes(value: Boolean) {
        viewModelScope.launch {
            settingsStore.setAutoUpdatePrayerTimes(value)
            if (value) {
                prayerTimesSyncManager.refreshFromSavedLocation()
            }
        }
    }

    fun testSilentMode() {
        viewModelScope.launch {
            testMessage.value = "Testing silent mode..."
            testMessage.value = if (silenceController.testSilentMode()) {
                "Silent mode test completed."
            } else {
                "Grant Do Not Disturb access, then try again."
            }
        }
    }

    fun resetSchedules() {
        viewModelScope.launch {
            val settings = settingsStore.getSettings()
            settingsStore.getAutomationState().activePrayerIds.forEach { activeId ->
                silenceController.endPrayer(activeId, settings.restorePreviousMode)
            }
            repository.getEnabledSchedules().forEach { scheduler.cancel(it.id) }
            repository.clearSchedules()
            testMessage.value = "Schedules cleared."
        }
    }
}
