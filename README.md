# Silent Scheduler

Silent Scheduler is a native Android app that automatically enables Do Not Disturb during scheduled events and restores the previous sound mode when the event ends.

It is useful for prayers, lectures, meetings, study sessions, work hours, and any situation where a phone should stay silent for a fixed time without the user forgetting to turn sound back on.

## Features

- Alarm-style event scheduling
- Unlimited custom events stored locally
- Daily, one-time, Friday-only, weekday, and custom-day repeats
- Do Not Disturb automation with previous sound mode restoration
- Reliable alarm scheduling with reboot and app-update rescheduling
- Location-based daily prayer timings
- Hanafi and Jafria prayer timing options
- Current prayer row highlighting
- Sunrise time and daily Quran ayah with Urdu translation
- Quick DND timer
- Simple Material 3 interface built with Jetpack Compose

## Download And Install

The easiest way to use the app is to download the latest APK from GitHub Releases.

1. Open the [Releases page](https://github.com/HamzaBilal07/Silent-Scheduler/releases).
2. Open the latest release.
3. Download the APK file, for example `Silent-Scheduler-v1.4.1.apk`.
4. On your Android phone, open the APK.
5. If Android asks, allow installation from that source.
6. Open Silent Scheduler and grant the required permissions from the onboarding/settings screen.

Required permissions:

- Do Not Disturb access: needed to enable and restore DND.
- Exact alarm access: needed for accurate start and end times.
- Notification permission: needed for status and reminder notifications.
- Location permission: needed only for location-based prayer timings.

## Build From Source

Requirements:

- Android Studio
- JDK 17
- Android SDK 35

Clone the repository:

```bash
git clone https://github.com/HamzaBilal07/Silent-Scheduler.git
cd Silent-Scheduler
```

Open the project in Android Studio, let Gradle sync, then run the `app` configuration on a phone or emulator.

Build a debug APK:

```bash
./gradlew :app:assembleDebug
```

On Windows:

```powershell
.\gradlew.bat :app:assembleDebug
```

Run unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

## Release Builds

Release builds require a private signing keystore. Signing files are intentionally not included in this repository.

Never commit:

```text
.keystore/
keystore.properties
*.jks
*.keystore
*.p12
*.pem
release/
```

After configuring local signing, build release artifacts:

```bash
./gradlew :app:assembleRelease :app:bundleRelease
```

Use:

- APK for direct sharing/installing on Android phones.
- AAB for Google Play Console upload.

## Updating GitHub Releases

Release APK/AAB files should be uploaded to GitHub Releases, not committed to the repository.

Manual GitHub steps:

1. Go to the [Releases page](https://github.com/HamzaBilal07/Silent-Scheduler/releases).
2. Click **Draft a new release**.
3. Choose the latest tag, for example `v1.4.1`.
4. Title it `Silent Scheduler v1.4.1`.
5. Upload the generated APK, AAB, ZIP backup, and checksum file.
6. Mark it as the latest release.
7. Publish the release.
8. If an older release should no longer be shown as latest, edit or delete that older release after the new one is published.

GitHub CLI alternative:

```powershell
gh auth login
gh release create v1.4.1 `
  "release\whatsapp\Silent-Scheduler-v1.4.1.apk" `
  "release\Silent-Scheduler-v1.4.1-code17-playstore.aab" `
  "release\whatsapp\Silent-Scheduler-v1.4.1-whatsapp.zip" `
  "release\SHA256SUMS.txt" `
  --repo HamzaBilal07/Silent-Scheduler `
  --title "Silent Scheduler v1.4.1" `
  --notes "Adds current prayer row highlighting, system-following orientation, and reliability improvements." `
  --latest
```

To remove an old release with GitHub CLI:

```powershell
gh release delete v1.3.9 --repo HamzaBilal07/Silent-Scheduler --yes
```

Only delete old releases when you are sure users no longer need that version.

## Project Structure

```text
app/src/main/java/com/mhamz/prayerdndmanager/
  data/          Room database, repositories, DataStore, location and prayer timings
  domain/        Models, repeat-day logic, time calculations, ayah provider
  permissions/   Permission checks and Android settings helpers
  receiver/      Alarm, reboot, and daily sync receivers
  scheduler/     AlarmManager scheduling and DND control
  ui/            Compose screens, navigation, and ViewModels
```

## Repository Contents

This repository keeps source code, Gradle files, tests, app resources, Room schemas, documentation, and public store assets.

Generated APK/AAB files, private signing files, local scripts, and machine-specific files are excluded from Git.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
