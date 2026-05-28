package com.mhamz.prayerdndmanager.scheduler

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.domain.AutomationStateReducer
import com.mhamz.prayerdndmanager.domain.SoundSnapshot
import com.mhamz.prayerdndmanager.permissions.PermissionHelper
import kotlinx.coroutines.delay

class SilenceController(
    private val context: Context,
    private val settingsStore: SettingsStore
) {
    private val notificationManager = context.getSystemService(NotificationManager::class.java)
    private val audioManager = context.getSystemService(AudioManager::class.java)

    suspend fun startPrayer(prayerId: Long): Boolean {
        if (!PermissionHelper.hasDndAccess(context)) return false
        val currentState = settingsStore.getAutomationState()
        val previous = SoundSnapshot(
            interruptionFilter = notificationManager?.currentInterruptionFilter
                ?: NotificationManager.INTERRUPTION_FILTER_ALL,
            ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        )
        val nextState = AutomationStateReducer.startPrayer(currentState, prayerId, previous)
        return try {
            settingsStore.saveAutomationState(nextState)
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            true
        } catch (_: SecurityException) {
            false
        }
    }

    suspend fun endPrayer(prayerId: Long, restorePreviousMode: Boolean): Boolean {
        val currentState = settingsStore.getAutomationState()
        val remainingState = AutomationStateReducer.endPrayer(currentState, prayerId)
        if (remainingState.isActive) {
            settingsStore.saveAutomationState(remainingState)
            return false
        }
        return try {
            if (restorePreviousMode && PermissionHelper.hasDndAccess(context)) {
                currentState.previousInterruptionFilter?.let {
                    notificationManager?.setInterruptionFilter(it)
                }
                currentState.previousRingerMode?.let {
                    audioManager?.ringerMode = it
                }
            }
            settingsStore.clearAutomationState()
            true
        } catch (_: SecurityException) {
            settingsStore.clearAutomationState()
            false
        }
    }

    suspend fun testSilentMode(): Boolean {
        if (!PermissionHelper.hasDndAccess(context)) return false
        val previousFilter = notificationManager?.currentInterruptionFilter
            ?: NotificationManager.INTERRUPTION_FILTER_ALL
        val previousRinger = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        return try {
            notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            delay(3_000)
            notificationManager?.setInterruptionFilter(previousFilter)
            audioManager?.ringerMode = previousRinger
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
