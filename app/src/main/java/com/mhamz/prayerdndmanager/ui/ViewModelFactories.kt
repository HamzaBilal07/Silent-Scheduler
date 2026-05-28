package com.mhamz.prayerdndmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mhamz.prayerdndmanager.AppContainer

class HomeViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return HomeViewModel(
            repository = container.repository,
            settingsStore = container.settingsStore,
            prayerTimesSyncManager = container.prayerTimesSyncManager,
            scheduler = container.scheduler,
            silenceController = container.silenceController
        ) as T
    }
}

class EditPrayerViewModelFactory(
    private val scheduleId: Long?,
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return EditPrayerViewModel(
            scheduleId = scheduleId,
            repository = container.repository,
            settingsStore = container.settingsStore,
            scheduler = container.scheduler,
            silenceController = container.silenceController
        ) as T
    }
}

class SettingsViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(
            repository = container.repository,
            settingsStore = container.settingsStore,
            scheduler = container.scheduler,
            prayerTimesSyncManager = container.prayerTimesSyncManager,
            silenceController = container.silenceController
        ) as T
    }
}

class PrayerTimesViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PrayerTimesViewModel(
            settingsStore = container.settingsStore,
            prayerTimesSyncManager = container.prayerTimesSyncManager
        ) as T
    }
}

class OnboardingViewModelFactory(
    private val container: AppContainer
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return OnboardingViewModel(container.settingsStore) as T
    }
}
