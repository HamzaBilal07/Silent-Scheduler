# Silent Scheduler Play Store Checklist

## Current App Identity

- App name: Silent Scheduler
- Package name: `com.mhamz.prayerdndmanager`
- Version: `1.3.9`
- Version code: `15`
- Minimum Android: Android 8.0, API 26
- Target Android: Android 15, API 35

## Build Signed Release Files

Run this from the project folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-distribution.ps1
```

Generated files:

- Play Store upload: `release\Silent-Scheduler-v1.3.9-code15-playstore.aab`
- Direct install APK: `release\whatsapp\Silent-Scheduler-v1.3.9.apk`
- WhatsApp ZIP backup: `release\whatsapp\Silent-Scheduler-v1.3.9-whatsapp.zip`

Do not delete these private signing files:

- `.keystore\prayer-dnd-manager-upload.jks`
- `keystore.properties`

Back them up privately. They are required for all future updates to this app.

## Play Console App Content

Complete these Play Console sections before production release:

- App access: no special login required.
- Ads: no ads.
- Content rating: utility/productivity style app.
- Target audience: not designed specifically for children.
- Data safety: disclose location use for app functionality.
- Privacy policy: publish `PRIVACY_POLICY_DRAFT.md` as a webpage and use that URL.
- Permissions declaration: explain exact alarm access as the core event/prayer DND scheduling feature.

## Data Safety Suggested Answers

- Location: collected/transmitted for app functionality when fetching prayer timings.
- Personal info: not collected by this app.
- Photos/videos/audio/files/contacts/calendar/SMS/call logs: not collected.
- App activity: not collected for analytics or ads.
- Data sharing: location is sent to the prayer timing service only to calculate timings.
- Data deletion: users can delete schedules, clear schedules, clear app storage, or uninstall.

## Store Listing Assets

Generated local assets:

- `store-assets\icon-512.png`
- `store-assets\feature-graphic-1024x500.png`
- `store-assets\screenshots\01-events.png`
- `store-assets\screenshots\02-prayer-timings.png`
- `store-assets\screenshots\03-settings.png`

Review screenshots before upload. Replacing them with real device screenshots is recommended for final public launch.

## Store Listing Text

Use `STORE_LISTING_DRAFT.md` for app name, short description, and full description.

## Final Manual Checks

- Install the WhatsApp APK on a second phone.
- Confirm DND access flow works.
- Confirm exact alarm access flow works on Android 12+.
- Confirm notification permission flow works on Android 13+.
- Confirm location permission fetches prayer timings.
- Confirm disabling an event cancels its DND automation.
- Upload the `.aab` to an internal testing track before production.
