# Silent Scheduler Play Store Checklist

This checklist summarizes the steps required to prepare Silent Scheduler for Google Play.

## App Identity

- App name: `Silent Scheduler`
- Package name: `com.mhamz.prayerdndmanager`
- Minimum SDK: 26
- Target SDK: 35
- App category: Productivity / Tools

## Release Signing

Google Play releases must be signed with a private upload key.

Keep these files private:

```text
.keystore/
keystore.properties
```

Create `keystore.properties` locally for your own machine. Do not commit real passwords or private signing keys.

## Build Release Artifacts

After signing is configured, build the release APK and Android App Bundle:

```bash
./gradlew :app:assembleRelease :app:bundleRelease
```

On Windows:

```powershell
.\gradlew.bat :app:assembleRelease :app:bundleRelease
```

The Play Store upload file is generated under:

```text
app/build/outputs/bundle/release/
```

## Play Console Setup

Complete these Play Console sections before production release:

- App access: no login required
- Ads: no ads
- Content rating: utility/productivity app
- Target audience: not designed specifically for children
- Data safety: disclose location use for prayer timing calculation
- Privacy policy: publish `PRIVACY_POLICY_DRAFT.md` as a public webpage and use that URL
- Permissions declaration: explain exact alarms and Do Not Disturb as core scheduling functionality

## Data Safety Guidance

Use this as a starting point for the Play Console Data Safety form:

- Location: used for app functionality when calculating daily prayer timings and sunrise.
- Personal information: not collected by this app.
- App activity: not collected for analytics or advertising.
- Device or other IDs: not collected for tracking.
- Data sharing: location coordinates are sent to the prayer timing service only when prayer timings are refreshed.
- Data deletion: users can delete schedules, reset schedules, clear Android app storage, or uninstall the app.

## Permission Explanation

- Do Not Disturb access: allows the app to silence the phone during user-created events.
- Exact alarm access: allows event start and end actions to run at accurate times.
- Notification permission: allows reminders and important status messages.
- Location permission: allows prayer timings and sunrise to be calculated for the user's area.
- Internet access: allows the app to fetch prayer timing data.
- Boot completed access: allows enabled schedules to be restored after reboot.

## Store Assets

Recommended assets:

- 512 x 512 app icon
- 1024 x 500 feature graphic
- Phone screenshots for Events, Prayer Timings, and Settings
- Short description
- Full description
- Privacy policy URL

Generated draft assets may be placed under `store-assets/`, but final Play Store screenshots should be reviewed on a real device before upload.

## Final Testing

Test on at least one physical phone before publishing:

- Install the release build successfully.
- Confirm Do Not Disturb access flow works.
- Confirm exact alarm settings open on Android 12+.
- Confirm notification permission flow works on Android 13+.
- Confirm location permission enables prayer timing updates.
- Confirm no-internet state shows a friendly message.
- Confirm adding, editing, disabling, and deleting events works.
- Confirm custom repeat days work.
- Confirm reboot reschedules enabled events.
- Confirm overlapping events keep the phone silent until the final event ends.

## Recommended Release Flow

1. Upload the Android App Bundle to an internal testing track.
2. Install from Play internal testing on a physical device.
3. Complete the final permission and scheduling checks.
4. Move to closed or open testing if needed.
5. Publish to production only after testing is complete.
