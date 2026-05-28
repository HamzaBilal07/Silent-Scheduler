# Silent Scheduler Play Store Checklist

This checklist prepares Silent Scheduler for upload to Google Play Console.

## Current Release

- App name: `Silent Scheduler`
- Package name: `com.mhamz.prayerdndmanager`
- Version name: `1.3.9`
- Version code: `15`
- Minimum Android: Android 8.0, API 26
- Target Android: Android 15, API 35

## Build Release Files

Run this command from the project folder:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\prepare-distribution.ps1
```

Expected output:

- Play Store upload: `release\Silent-Scheduler-v1.3.9-code15-playstore.aab`
- Direct install APK: `release\whatsapp\Silent-Scheduler-v1.3.9.apk`
- WhatsApp ZIP backup: `release\whatsapp\Silent-Scheduler-v1.3.9-whatsapp.zip`
- Checksums: `release\SHA256SUMS.txt`

## Signing Files

Keep these files private and backed up:

```text
.keystore\prayer-dnd-manager-upload.jks
keystore.properties
```

Do not upload them to GitHub or send them to anyone. Losing these files can prevent future updates to the same app listing.

## Play Console Setup

Complete these sections in Google Play Console:

- App access: no login required
- Ads: no ads
- Content rating: utility/productivity app
- Target audience: not designed specifically for children
- Data safety: disclose location use for prayer timing calculation
- Privacy policy: publish `PRIVACY_POLICY_DRAFT.md` as a public webpage and use its URL
- Permissions declaration: explain exact alarms and Do Not Disturb as core scheduling functionality

## Data Safety Guidance

Use this as a starting point for the Data Safety form:

- Location: used for app functionality when calculating daily prayer timings and sunrise.
- Personal information: not collected by this app.
- App activity: not collected for analytics or advertising.
- Device or other IDs: not collected for tracking.
- Data sharing: location coordinates are sent to the prayer timing service only when prayer timings are refreshed.
- Data deletion: users can delete schedules, reset schedules, clear Android app storage, or uninstall the app.

## Permissions Explanation

- Do Not Disturb access: allows the app to silence the phone during user-created events.
- Exact alarm access: allows event start and end actions to run at accurate times.
- Notification permission: allows reminders and important permission/status warnings.
- Location permission: allows prayer timings and sunrise to be calculated for the user's area.
- Internet access: allows the app to fetch prayer timing data.
- Boot completed access: allows enabled schedules to be restored after reboot.

## Store Assets

Generated local assets:

- `store-assets\icon-512.png`
- `store-assets\feature-graphic-1024x500.png`
- `store-assets\screenshots\01-events.png`
- `store-assets\screenshots\02-prayer-timings.png`
- `store-assets\screenshots\03-settings.png`

Review all assets before upload. Real screenshots from an actual device are recommended for a production release.

## Store Listing Text

Use `STORE_LISTING_DRAFT.md` for:

- App name
- Short description
- Full description
- Feature list
- Permission explanation

## Final Testing Before Production

Test on at least one physical phone before production release:

- Install the APK successfully.
- Confirm Do Not Disturb access flow works.
- Confirm exact alarm settings open on Android 12+.
- Confirm notification permission flow works on Android 13+.
- Confirm location permission enables prayer timing updates.
- Confirm no-internet state shows a friendly message.
- Confirm adding, editing, disabling, and deleting events works.
- Confirm custom repeat days work.
- Confirm reboot reschedules enabled events.
- Confirm overlapping events keep the phone silent until the last event ends.

## Recommended Release Flow

1. Upload the `.aab` to an internal testing track.
2. Install from Play internal testing on a real phone.
3. Complete final permission and schedule checks.
4. Move to closed testing if needed.
5. Publish to production only after the checklist is complete.
