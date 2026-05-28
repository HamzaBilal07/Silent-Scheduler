package com.mhamz.prayerdndmanager.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhamz.prayerdndmanager.domain.ALL_DAYS_MASK
import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.PrayerSchedule
import com.mhamz.prayerdndmanager.scheduler.PrayerAlarmScheduler
import com.mhamz.prayerdndmanager.scheduler.SilenceController
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class EditPrayerUiState(
    val id: Long = 0,
    val name: String = "Lecture",
    val startHour: Int = 9,
    val startMinute: Int = 0,
    val endHour: Int = 10,
    val endMinute: Int = 0,
    val enabled: Boolean = false,
    val repeatDaysMask: Int = ALL_DAYS_MASK,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true
)

class EditPrayerViewModel(
    private val scheduleId: Long?,
    private val repository: PrayerRepository,
    private val settingsStore: SettingsStore,
    private val scheduler: PrayerAlarmScheduler,
    private val silenceController: SilenceController
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditPrayerUiState())
    val uiState: StateFlow<EditPrayerUiState> = _uiState.asStateFlow()

    private val _savedEvents = MutableSharedFlow<Unit>()
    val savedEvents: SharedFlow<Unit> = _savedEvents.asSharedFlow()

    init {
        viewModelScope.launch {
            val existing = scheduleId?.takeIf { it > 0L }?.let { repository.getSchedule(it) }
            _uiState.value = if (existing != null) {
                EditPrayerUiState(
                    id = existing.id,
                    name = existing.name,
                    startHour = existing.startHour,
                    startMinute = existing.startMinute,
                    endHour = existing.endHour,
                    endMinute = existing.endMinute,
                    enabled = existing.enabled,
                    repeatDaysMask = existing.repeatDaysMask,
                    createdAt = existing.createdAt,
                    isLoading = false
                )
            } else {
                EditPrayerUiState(isLoading = false)
            }
        }
    }

    fun setName(value: String) = _uiState.update { it.copy(name = value) }

    fun setStartTime(hour: Int, minute: Int) = _uiState.update {
        it.copy(startHour = hour, startMinute = minute)
    }

    fun setEndTime(hour: Int, minute: Int) = _uiState.update {
        it.copy(endHour = hour, endMinute = minute)
    }

    fun setEnabled(value: Boolean) = _uiState.update { it.copy(enabled = value) }

    fun setRepeatDaysMask(value: Int) = _uiState.update { it.copy(repeatDaysMask = value) }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val schedule = PrayerSchedule(
                id = state.id,
                name = state.name.ifBlank { "Prayer" },
                startHour = state.startHour,
                startMinute = state.startMinute,
                endHour = state.endHour,
                endMinute = state.endMinute,
                enabled = state.enabled,
                repeatDaysMask = state.repeatDaysMask,
                createdAt = state.createdAt,
                updatedAt = System.currentTimeMillis()
            )
            val id = repository.save(schedule)
            val saved = schedule.copy(id = id)
            if (saved.enabled) {
                scheduler.schedule(saved, settingsStore.getSettings())
            } else {
                scheduler.cancel(id)
                silenceController.endPrayer(
                    prayerId = id,
                    restorePreviousMode = settingsStore.getSettings().restorePreviousMode
                )
            }
            _savedEvents.emit(Unit)
        }
    }
}
