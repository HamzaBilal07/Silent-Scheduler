package com.mhamz.prayerdndmanager

import android.app.Application
import androidx.room.Room
import com.mhamz.prayerdndmanager.data.AppDatabase
import com.mhamz.prayerdndmanager.data.LocationProvider
import com.mhamz.prayerdndmanager.data.PrayerRepository
import com.mhamz.prayerdndmanager.data.PrayerRepositoryImpl
import com.mhamz.prayerdndmanager.data.PrayerTimesRepository
import com.mhamz.prayerdndmanager.data.SettingsStore
import com.mhamz.prayerdndmanager.scheduler.AppNotificationHelper
import com.mhamz.prayerdndmanager.scheduler.DailyPrayerSyncScheduler
import com.mhamz.prayerdndmanager.scheduler.PrayerAlarmScheduler
import com.mhamz.prayerdndmanager.scheduler.PrayerEventHandler
import com.mhamz.prayerdndmanager.scheduler.PrayerTimesSyncManager
import com.mhamz.prayerdndmanager.scheduler.SilenceController

class PrayerSilentSchedulerApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifications.ensureChannel()
    }
}

class AppContainer(application: Application) {
    private val appContext = application.applicationContext

    val database: AppDatabase = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "prayer_silent_scheduler.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3, AppDatabase.MIGRATION_3_4)
        .build()

    val settingsStore = SettingsStore(appContext)
    val repository: PrayerRepository = PrayerRepositoryImpl(database.prayerScheduleDao())
    val locationProvider = LocationProvider(appContext)
    val prayerTimesRepository = PrayerTimesRepository(locationProvider, settingsStore)
    val scheduler = PrayerAlarmScheduler(appContext)
    val dailyPrayerSyncScheduler = DailyPrayerSyncScheduler(appContext)
    val notifications = AppNotificationHelper(appContext)
    val silenceController = SilenceController(appContext, settingsStore)
    val prayerTimesSyncManager = PrayerTimesSyncManager(
        repository = repository,
        settingsStore = settingsStore,
        prayerTimesRepository = prayerTimesRepository,
        alarmScheduler = scheduler,
        dailySyncScheduler = dailyPrayerSyncScheduler
    )
    val eventHandler = PrayerEventHandler(
        repository = repository,
        settingsStore = settingsStore,
        scheduler = scheduler,
        prayerTimesSyncManager = prayerTimesSyncManager,
        silenceController = silenceController,
        notifications = notifications,
        appContext = appContext
    )
}
