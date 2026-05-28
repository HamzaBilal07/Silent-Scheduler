package com.mhamz.prayerdndmanager.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.AutomationState
import com.mhamz.prayerdndmanager.domain.DailyPrayerTimes
import com.mhamz.prayerdndmanager.domain.DailyQuranAyahProvider
import com.mhamz.prayerdndmanager.domain.QuranAyah
import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.domain.PrayerTimeCalculator
import com.mhamz.prayerdndmanager.scheduler.PrayerAlarmScheduler
import com.mhamz.prayerdndmanager.scheduler.PrayerTimesSyncManager
import com.mhamz.prayerdndmanager.scheduler.SilenceController
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
import java.time.format.DateTimeFormatter

@Immutable
data class HomeUiState(
    val schedules: List<PrayerSchedule> = emptyList(),
    val automationState: AutomationState = AutomationState(),
    val nextPrayer: Pair<PrayerSchedule, java.time.LocalDateTime>? = null,
    val prayerTimesState: PrayerTimesUiState = PrayerTimesUiState(),
    val ayah: QuranAyah = DailyQuranAyahProvider.today(),
    val quickDndActive: Boolean = false,
    val quickDndMessage: String? = null
)

@Immutable
data class PrayerTimesUiState(
    val isLoading: Boolean = false,
    val times: DailyPrayerTimes? = null,
    val error: String? = null
)

class HomeViewModel(
    private val repository: PrayerRepository,
    private val settingsStore: SettingsStore,
    private val prayerTimesSyncManager: PrayerTimesSyncManager,
    private val scheduler: PrayerAlarmScheduler,
    private val silenceController: SilenceController
) : ViewModel() {
    private val prayerTimesState = MutableStateFlow(PrayerTimesUiState())
    private val quickDndMessage = MutableStateFlow<String?>(null)
    private val currentDate = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<HomeUiState> = combine(
        repository.schedules,
        settingsStore.automationState,
        prayerTimesState,
        quickDndMessage,
        currentDate
    ) { schedules, automation, timesState, quickMessage, date ->
        HomeUiState(
            schedules = schedules,
            automationState = automation,
            nextPrayer = PrayerTimeCalculator.nextUpcoming(schedules),
            prayerTimesState = timesState,
            ayah = DailyQuranAyahProvider.today(date),
            quickDndActive = automation.activePrayerIds.contains(PrayerAlarmScheduler.QUICK_DND_ID),
            quickDndMessage = quickMessage
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState()
    )

    init {
        viewModelScope.launch {
            prayerTimesSyncManager.scheduleNextDailySync()
        }
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

    fun refreshPrayerTimes() {
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

    fun setEnabled(schedule: PrayerSchedule, enabled: Boolean) {
        viewModelScope.launch {
            val updated = schedule.copy(enabled = enabled, updatedAt = System.currentTimeMillis())
            repository.save(updated)
            if (enabled) {
                scheduler.schedule(updated, settingsStore.getSettings())
            } else {
                scheduler.cancel(schedule.id)
                silenceController.endPrayer(
                    prayerId = schedule.id,
                    restorePreviousMode = settingsStore.getSettings().restorePreviousMode
                )
            }
        }
    }

    fun delete(schedule: PrayerSchedule) {
        viewModelScope.launch {
            scheduler.cancel(schedule.id)
            silenceController.endPrayer(
                prayerId = schedule.id,
                restorePreviousMode = settingsStore.getSettings().restorePreviousMode
            )
            repository.delete(schedule)
        }
    }

    fun startQuickDnd(minutes: Long) {
        viewModelScope.launch {
            val started = silenceController.startPrayer(PrayerAlarmScheduler.QUICK_DND_ID)
            quickDndMessage.value = if (started) {
                val endTime = scheduler.scheduleQuickDndEnd(minutes)
                val formatter = DateTimeFormatter.ofPattern("h:mm a")
                "Quick DND is on until ${endTime.format(formatter)}."
            } else {
                "Grant Do Not Disturb access to use Quick DND."
            }
        }
    }

    fun stopQuickDnd() {
        viewModelScope.launch {
            scheduler.cancelQuickDnd()
            val restored = silenceController.endPrayer(
                prayerId = PrayerAlarmScheduler.QUICK_DND_ID,
                restorePreviousMode = settingsStore.getSettings().restorePreviousMode
            )
            quickDndMessage.value = if (restored) {
                "Quick DND stopped. Sound mode restored."
            } else {
                "Quick DND stopped."
            }
        }
    }
}
