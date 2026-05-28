package com.mhamz.prayerdndmanager.domain

import androidx.compose.runtime.Immutable

@Immutable
data class AutomationState(
    val activePrayerIds: Set<Long> = emptySet(),
    val previousInterruptionFilter: Int? = null,
    val previousRingerMode: Int? = null
) {
    val isActive: Boolean = activePrayerIds.isNotEmpty()
}

@Immutable
data class SoundSnapshot(
    val interruptionFilter: Int,
    val ringerMode: Int
)

object AutomationStateReducer {
    fun startPrayer(
        state: AutomationState,
        prayerId: Long,
        previousMode: SoundSnapshot
    ): AutomationState {
        val shouldCapturePrevious = state.activePrayerIds.isEmpty()
        return state.copy(
            activePrayerIds = state.activePrayerIds + prayerId,
            previousInterruptionFilter = if (shouldCapturePrevious) {
                previousMode.interruptionFilter
            } else {
                state.previousInterruptionFilter
            },
            previousRingerMode = if (shouldCapturePrevious) {
                previousMode.ringerMode
            } else {
                state.previousRingerMode
            }
        )
    }

    fun endPrayer(state: AutomationState, prayerId: Long): AutomationState {
        val remaining = state.activePrayerIds - prayerId
        return if (remaining.isEmpty()) {
            AutomationState()
        } else {
            state.copy(activePrayerIds = remaining)
        }
    }
}
