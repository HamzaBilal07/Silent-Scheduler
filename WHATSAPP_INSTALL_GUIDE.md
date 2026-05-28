# Direct APK Install Guide

This guide is for installing Silent Scheduler directly from an APK file outside the Play Store.

For public distribution, Google Play is recommended. Direct APK sharing is useful for trusted testing, internal distribution, or early access.

## Build The APK

Create a release APK with Android Studio or Gradle:

```bash
./gradlew :app:assembleRelease
```

On Windows:

```powershell
.\gradlew.bat :app:assembleRelease
```

The release APK is generated under:

```text
app/build/outputs/apk/release/
```

## Install On A Phone

1. Transfer the APK to the Android phone.
2. Open the APK from the file manager.
3. If Android asks, allow the file manager or browser to install unknown apps.
4. Tap Install.
5. Open Silent Scheduler.
6. Follow the in-app permission setup.

## Permissions After Install

The app may request:

- Do Not Disturb access
- Exact alarm access on Android 12+
- Notification permission on Android 13+
- Location permission for automatic prayer timings

The app guides the user to the correct Android settings screen when access is needed.

## Updating

Future APK updates must use the same signing key as the previously installed APK. If Android reports a signature mismatch, uninstall the older build before installing the new one.

Uninstalling removes local schedules and app settings.

## Safety

Install APKs only from trusted sources. Do not install modified or unofficial builds.
