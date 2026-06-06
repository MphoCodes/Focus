# Manual QA Steps

## 1. Usage Access
- Navigate to an app that has a quota.
- Open the app.
- Ensure the prompt for Usage Access is shown if permission is not granted.
- Grant the permission and ensure the app opens correctly.
- Ensure the quota tracks usage correctly.

## 2. Notification Listener Access
- Enable notification batching in Settings.
- You should be prompted to allow Notification Listener access.
- Grant it, then trigger notifications from various apps.
- Verify that non-essential notifications are batched and shown in the summary.

## 3. Launcher Default Setup
- Finish onboarding.
- You should be prompted to set Focus Launcher as the default home app.
- Confirm and restart device/press home button to ensure Focus Launcher intercepts the home intent.

