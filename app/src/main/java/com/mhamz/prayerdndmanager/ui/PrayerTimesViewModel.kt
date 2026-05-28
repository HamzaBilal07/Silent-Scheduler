package com.mhamz.prayerdndmanager.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.AppSettings
import com.mhamz.prayerdndmanager.domain.DailyQuranAyahProvider
import com.mhamz.prayerdndmanager.domain.FiqhMethod
import com.mhamz.prayerdndmanager.domain.QuranAyah
import com.mhamz.prayerdndmanager.scheduler.PrayerTimesSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime

@Immutable
data class PrayerTimesScreenUiState(
    val settings: AppSettings = AppSettings(),
    val prayerTimesState: PrayerTimesUiState = PrayerTimesUiState(),
    val ayah: QuranAyah = DailyQuranAyahProvider.today()
)

class PrayerTimesViewModel(
    private val settingsStore: SettingsStore,
    private val prayerTimesSyncManager: PrayerTimesSyncManager
) : ViewModel() {
    private val prayerTimesState = MutableStateFlow(PrayerTimesUiState())
    private val currentDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<PrayerTimesScreenUiState> = combine(
        settingsStore.settings,
        prayerTimesState,
        currentDate
    ) { settings, timesState, date ->
        PrayerTimesScreenUiState(
            settings = settings,
            prayerTimesState = timesState,
            ayah = DailyQuranAyahProvider.today(date)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        PrayerTimesScreenUiState()
    )

    init {
        startDateTicker()
    }

    private fun startDateTicker() {
        viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now()
                val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plusSeconds(2)
                val delayMillis = Duration.between(now, nextMidnight).toMillis().coerceAtLeast(60_000)
                delay(delayMillis)
                currentDate.value = LocalDate.now()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            if (prayerTimesState.value.isLoading) return@launch
            prayerTimesState.value = prayerTimesState.value.copy(isLoading = true, error = null)
            val result = prayerTimesSyncManager.refreshAutomatically()
            result.fold(
                onSuccess = { times ->
                    prayerTimesState.value = PrayerTimesUiState(times = times)
                },
                onFailure = { error ->
                    prayerTimesState.value = PrayerTimesUiState(
                        times = prayerTimesState.value.times,
                        error = error.message ?: "Prayer times could not be updated."
                    )
                }
            )
        }
    }

    fun setFiqhMethod(method: FiqhMethod) {
        viewModelScope.launch {
            settingsStore.setFiqhMethod(method)
            refresh()
        }
    }
}
