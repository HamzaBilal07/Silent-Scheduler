# Silent Scheduler

Silent Scheduler is a native Android app that automatically enables Do Not Disturb during scheduled events and restores the previous sound mode when the event ends.

The app is designed for prayers, lectures, meetings, study sessions, and any other time when the phone should stay silent without the user having to remember to turn sound back on.

## Highlights

- Native Android app built with Kotlin and Jetpack Compose
- Simple alarm-clock style event scheduling
- Unlimited custom events stored locally with Room
- Custom repeat days, including Monday-Friday, Friday-only, daily, or one-time events
- Do Not Disturb automation using Android system access
- Previous sound mode restore after the event ends
- Quick DND timer for 15, 30, or 60 minutes
- Daily prayer timings loaded from the internet using the phone location
- Hanafi and Jafria fiqah options
- Sunrise time on the Events page
- Offline daily Quran ayah with Urdu translation
- Automatic rescheduling after reboot or app update
- Local settings storage using DataStore

## App Identity

- App name: `Silent Scheduler`
- Package name: `com.mhamz.prayerdndmanager`
- Current version: `1.3.9`
- Version code: `15`
- Minimum Android version: Android 8.0, API 26
- Target Android version: Android 15, API 35

## Permissions Used

Silent Scheduler only asks for permissions needed for its core features:

- Do Not Disturb access: used to silence the phone during scheduled events.
- Exact alarm access: used to start and end DND schedules on time.
- Notification permission: used for reminders and permission/status warnings.
- Location permission: used to calculate prayer timings and sunrise.
- Internet access: used to load prayer timings from the online prayer timing API.
- Boot completed access: used to reschedule enabled events after reboot.

If a permission is missing, the app does not crash. It shows a clear warning and guides the user to the correct Android settings screen.

## Build Requirements

This project uses:

- Kotlin
- Jetpack Compose Material 3
- Room
- DataStore
- Coroutines and Flow
- Gradle wrapper
- Android Gradle Plugin 8.7.3
- Compile SDK 35
- Java 17

The helper scripts in `scripts/` expect the shared Android runtime from the neighboring local project used during development. If you are opening this project in Android Studio on another machine, install the normal Android Studio SDK/JDK requirements and sync the Gradle project.

## Build From Command Line

From the project folder:

```powershell
cd "C:\Users\mhamz\OneDrive - FAST National University\Python Files\PrayerSilentScheduler"
.\gradlew.bat :app:assembleDebug
```

Run unit tests:

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

Create signed release files:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-distribution.ps1
```

## Install On A Connected Phone

Connect the phone with USB debugging enabled, then run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-final-apk.ps1
```

The script installs the latest release APK from:

```text
release\whatsapp\Silent-Scheduler-v1.3.9.apk
```

## Latest Release Files

- Play Store upload: `release\Silent-Scheduler-v1.3.9-code15-playstore.aab`
- Direct install APK: `release\whatsapp\Silent-Scheduler-v1.3.9.apk`
- WhatsApp ZIP backup: `release\whatsapp\Silent-Scheduler-v1.3.9-whatsapp.zip`

## Important Signing Warning

Do not publish these private files:

```text
.keystore/
keystore.properties
```

They are ignored by `.gitignore` and must stay private. They are required for future app updates signed with the same release key.

Use `keystore.properties.example` as the public template.

## Project Structure

```text
app/src/main/java/com/mhamz/prayerdndmanager/
  data/          Room, repositories, DataStore, location/prayer timing fetch
  domain/        Domain models, time calculations, ayah provider
  permissions/   Permission checks and settings intents
  receiver/      Alarm, daily sync, and reboot receivers
  scheduler/     AlarmManager scheduling and DND control
  ui/            Compose screens, navigation, and ViewModels
```

## Documentation

- `WHATSAPP_INSTALL_GUIDE.md`: direct APK sharing instructions
- `PLAY_STORE_CHECKLIST.md`: Play Console release checklist
- `STORE_LISTING_DRAFT.md`: Google Play listing text
- `PRIVACY_POLICY_DRAFT.md`: privacy policy draft for publishing

## License

Add a `LICENSE` file before publishing publicly on GitHub. MIT is a simple option if you want others to use, modify, and share the code.
