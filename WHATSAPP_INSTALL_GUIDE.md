# WhatsApp APK Install Guide

Send this file to other Android phones:

```text
release\whatsapp\Silent-Scheduler-v1.3.9.apk
```

If WhatsApp blocks the APK file, send this ZIP instead:

```text
release\whatsapp\Silent-Scheduler-v1.3.9-whatsapp.zip
```

## Steps For The Other Phone

1. Download the APK from WhatsApp.
2. If Android asks, allow WhatsApp to install unknown apps.
3. Tap Install.
4. Open Silent Scheduler.
5. Grant the requested accesses:
   - Do Not Disturb access
   - Exact alarm access
   - Notification permission
   - Location permission, if prayer timings are needed

## Important

The APK is signed with the same release key as the Play Store build. Future APK updates must be signed with the same `.keystore` file in this project.

If a phone already has an older development/test version installed and Android says the update is incompatible, uninstall the old Silent Scheduler app once, then install this APK again. This happens because the old test app was signed with a different development key.
