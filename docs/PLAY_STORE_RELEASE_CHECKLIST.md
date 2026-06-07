# Google Play Release Checklist

Use this checklist before submitting Focus Launcher to Google Play.

## Build Artifact

- Create a Google Play upload keystore.
- Configure release signing with environment variables or `local.properties`:
  - `FOCUS_RELEASE_STORE_FILE`
  - `FOCUS_RELEASE_STORE_PASSWORD`
  - `FOCUS_RELEASE_KEY_ALIAS`
  - `FOCUS_RELEASE_KEY_PASSWORD`
- Build the Play artifact:

```bash
./gradlew bundleRelease
```

- Upload `app/build/outputs/bundle/release/app-release.aab` to Play Console.

## GitHub Release Secrets

Set these repository secrets before creating another `v*` tag release:

- `FOCUS_RELEASE_KEYSTORE_BASE64`: base64 encoded upload keystore.
- `FOCUS_RELEASE_STORE_PASSWORD`
- `FOCUS_RELEASE_KEY_ALIAS`
- `FOCUS_RELEASE_KEY_PASSWORD`

Create the base64 value locally:

```bash
base64 -i secrets/focus-upload-keystore.jks | pbcopy
```

## Required Store Listing

- App name: Focus Launcher
- Short description: Minimalist Android launcher for reducing distraction.
- Long description: Use `docs/PLAY_STORE_LISTING.md` and keep claims aligned with shipped app behavior.
- Screenshots for phone form factor.
- High-resolution icon.
- Feature graphic.
- Content rating questionnaire.
- Target audience and app category.
- Public privacy policy URL: `https://mphocodes.github.io/Focus/privacy-policy/`.
- Listing copy and data safety draft: `docs/PLAY_STORE_LISTING.md`.

## Data Safety Declarations

Declare only what the app actually accesses and how it is used:

- Installed apps: used to display and configure launcher app lists.
- App usage data: used for daily app quota enforcement.
- Notifications: used for optional notification batching and essential app bypasses.
- Wi-Fi SSID/network information: used only for optional contextual profile triggers.
- Local app activity counters and intent logs: stored locally for focus statistics and prompt reuse.

The app does not include ads, analytics, cloud sync, or third-party tracking SDKs.

## Sensitive Access Review Notes

Play reviewers should see a clear core-functionality reason for each sensitive capability:

- `PACKAGE_USAGE_STATS`: required to measure selected app usage for daily quota enforcement.
- Notification listener service: required to batch non-essential notifications and summarize them.
- `POST_NOTIFICATIONS`: required to show Focus notification summaries on Android 13+.
- Wi-Fi/network state: used only for optional Work/Home/Weekend profile switching.
- App visibility query: required because this is a launcher.

## Manual QA

- Install release build on a clean physical Android device.
- Set Focus Launcher as the default home app.
- Confirm app drawer search and launch.
- Enable notification batching and confirm Android 13+ notification permission prompt appears.
- Grant notification listener access and confirm essential app bypass selection.
- Grant usage access and test a low app quota.
- Test contextual profile manual switching and at least one app allow-list.
- Confirm Privacy & data screen is visible from Settings.
