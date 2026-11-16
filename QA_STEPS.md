# Manual QA

Run on a disposable emulator or test device. Record the Android version, device model and APK commit. A passing JVM suite does not cover these flows.

## Onboarding and home

- On a fresh installation, advance through all onboarding steps and open default-home settings.
- Set Focus as Home, press Home and reboot; check the launcher returns correctly.
- Switch back to the previous launcher through Android default-app settings.
- Search the drawer, launch an app, change a favorite, hide/unhide an app, rename it and add it to a workspace.
- Change text size and check screens, rows and onboarding for clipping.

## Usage access and quotas

- Set a quota on an app and try opening it without Usage Access; verify the permission explanation.
- Grant access, return to Focus and verify state refreshes.
- Use the selected app beyond its quota, return and try launching it through Focus; verify the blocked state.
- Revoke access and verify the explanation returns. Check day rollover on the test device.
- Remember that links, notifications and other launchers are outside Focus's launch gate.

## Notifications and profiles

- Enable batching and grant listener access plus Android 13+ notification permission.
- Generate disposable notifications from essential and non-essential apps. Verify bypass versus dismissal and count summaries.
- Check summary delivery around noon/18:00, including background/power-saving behavior; original content is not replayed.
- Disable batching and revoke listener access; confirm normal notifications resume.
- Exercise manual profiles, weekday/weekend schedules and Wi-Fi matching where Android exposes the SSID.

## Automated checks

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest
./gradlew connectedDebugAndroidTest
```

The second command requires a connected Android device/emulator and executes the real-activity onboarding navigation test. Use a test installation: the test resets onboarding completion.
