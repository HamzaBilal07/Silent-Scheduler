# WhatsApp APK Install Guide

Use this guide when sharing Silent Scheduler directly with another Android phone outside the Play Store.

## Recommended File

Send this APK:

```text
release\whatsapp\Silent-Scheduler-v1.3.9.apk
```

If WhatsApp blocks the APK, send the ZIP backup instead:

```text
release\whatsapp\Silent-Scheduler-v1.3.9-whatsapp.zip
```

The receiver should extract the ZIP first, then install the APK inside it.

## Install Steps On The Other Phone

1. Download the APK or ZIP from WhatsApp.
2. If using the ZIP, extract it first.
3. Tap the APK file.
4. If Android asks, allow WhatsApp or the file manager to install unknown apps.
5. Tap Install.
6. Open Silent Scheduler.
7. Follow the in-app permission setup.

## Required Access After Install

The app may ask for:

- Do Not Disturb access
- Exact alarm access on Android 12+
- Notification permission on Android 13+
- Location permission for automatic prayer timings

The app guides the user to the exact Android settings screen when access is needed.

## Updating Later

Future APK updates must be signed with the same release key. If the phone already has this release build installed, a newer signed APK can be installed over it normally.

If Android says the update is incompatible, uninstall the older test/development version once, then install the latest APK again. Uninstalling removes local app schedules and settings.

## Safety Note

Only share APKs generated from this project by the trusted developer. Users should avoid installing APK files from unknown or modified sources.
