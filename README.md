# Silent Scheduler

Native Android app written in Kotlin with Jetpack Compose.

The app lets users schedule Do Not Disturb for lectures, meetings, prayers, and other events. It can also fetch daily prayer times and sunrise from the internet using the phone location.
It includes Quick DND for instantly silencing the phone for 15, 30, or 60 minutes without creating a schedule.

Package installed on the phone: `com.mhamz.prayerdndmanager`

## Build

From this folder:

```powershell
.\gradlew.bat :app:assembleDebug
```

For a phone you actually use, install the optimized release build:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\run-release.ps1
```

The debug script is useful while developing but is slower on older phones:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-debug.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run-debug.ps1
```

All helper scripts use the shared local Android runtime from the neighboring `SmartStudyPlannerPomodoro` project.

## Updating The Phone After Code Changes

Whenever code changes on the laptop, the phone does not update automatically. Reinstall the newest optimized build:

```powershell
cd "C:\Users\mhamz\OneDrive - FAST National University\Python Files\PrayerSilentScheduler"
powershell -ExecutionPolicy Bypass -File .\scripts\run-release.ps1
```

Because this version changed from the old development package to the Play-ready package `com.mhamz.prayerdndmanager`, it installs as a new app one time. After this, future changes installed with `run-release.ps1` update the same app.

For Play Store packaging, see `PLAY_STORE_CHECKLIST.md`.
For APK sharing through WhatsApp, see `WHATSAPP_INSTALL_GUIDE.md`.

## Notes

- First launch starts with an empty event list. Tap Add to create your own DND schedule.
- Quick DND can temporarily silence the phone immediately and restore sound automatically.
- The onboarding and settings screens include a "Grant missing access" button that opens the next required Android permission screen automatically.
- The event name picker includes Lecture, Fajr, Zuhr, Asr, Maghrib, Isha, and Jummah, and you can also type a custom name.
- Do Not Disturb access is required before the app can silence the phone. Android opens the Do Not Disturb access list; turn on Silent Scheduler there.
- Android 12+ users may need exact alarm permission for reliable timing.
- Android 13+ users may need notification permission for reminders and warnings.
- Location and internet are used to fetch daily prayer times and sunrise. Fiqah can be set to Hanafi or Jafria in Settings.
