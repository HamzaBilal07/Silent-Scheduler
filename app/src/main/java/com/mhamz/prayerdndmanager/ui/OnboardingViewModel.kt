package com.mhamz.prayerdndmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mhamz.prayerdndmanager.data.SettingsStore
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {
    fun complete() {
        viewModelScope.launch {
            settingsStore.setOnboardingComplete(true)
        }
    }
}
