# Focus Launcher Play Store Listing

Use this copy in Google Play Console for the first production or internal test submission.

## App Details

- App name: Focus Launcher
- Short description: Minimalist Android launcher for reducing distraction.
- Category: Productivity
- Tags: launcher, focus, productivity, minimalist
- Privacy policy: https://mphocodes.github.io/Focus/privacy-policy/

## Full Description

Focus Launcher is a minimalist Android home screen built to make your phone quieter.

Replace a noisy launcher with a black-and-white interface focused on intent, search, and deliberate app opening. Choose the apps that deserve space on your home screen, hide distractions from the drawer, and rename attention traps with dull labels so they carry less visual pull.

Focus Launcher includes soft friction tools for apps you tend to open on autopilot. Add intent prompts before opening selected apps, set daily app quotas, and use context profiles to keep different app lists for work, home, and weekends.

Notification batching helps hold non-essential notifications and release them as simple summaries, while essential apps can bypass the filter.

The first-run walkthrough introduces the core setup choices, explains sensitive permissions in context, and lets users start with the launcher before enabling optional focus tools.

Privacy is central to the design. Focus Launcher stores settings and focus activity locally on your device. It does not include ads, analytics, cloud sync, or third-party tracking SDKs.

Key features:

- Minimal home screen with a focused favorites list.
- Fast searchable app drawer.
- Hidden apps and app disguises for reducing visual triggers.
- Intent prompts before opening selected apps.
- Daily app quotas for selected apps.
- Work, home, and weekend context profiles.
- Optional notification batching with essential app bypass.
- First-run walkthrough for setup, focus tools, and privacy expectations.
- Local-only settings and privacy-first design.
- Black theme, grayscale mode, text size, and simple accent options.

## Screenshot Captions

Use real device screenshots from the release build. Recommended order:

1. Make your phone quiet.
2. Search apps only when needed.
3. Choose the apps that deserve space.
4. Hide and rename distractions.
5. Pause before opening attention traps.
6. Set daily limits for selected apps.
7. Switch app lists for work, home, and weekends.
8. Keep focus data local.

## Store Assets

- App icon source: `docs/branding/generated-assets/logo-concept-01-focus-ring-grid-512.png`
- Android vector icon: `app/src/main/res/drawable/ic_launcher.xml`
- Feature graphic draft: `docs/branding/generated-assets/product-feature-graphic-1024x500.png`
- Product mockups:
  - `docs/branding/generated-assets/product-hero-landscape-phone.png`
  - `docs/branding/generated-assets/product-promo-portrait-phone.png`

Before final public submission, prefer replacing generated product mockups with screenshots captured from the signed release build on an Android device or emulator.

## Data Safety Draft

Declare these only if Play Console asks for each data type:

- App activity: app launches, intent prompts, deflections, and quota-related counters are stored locally for focus features.
- Installed apps: used locally to show the launcher app list, favorites, hidden apps, disguises, quotas, profiles, and notification bypass selections.
- App info and performance: no analytics or crash reporting SDK is included.
- Device or other IDs: not collected by Focus Launcher.
- Location: not collected. Wi-Fi SSID/network state is used only on-device for optional context profile switching.
- Notifications: notification listener access is used only on-device for optional notification batching and essential app bypass.

Data handling:

- No data is shared with third parties.
- No data is uploaded to a server.
- No ads or tracking SDKs are included.
- Users can delete local Focus data by clearing app data or uninstalling the app.

## Review Notes

Focus Launcher needs launcher visibility to display installed launchable apps and to operate as a home screen replacement.

The first-run walkthrough explains the launcher, focus friction, context profiles, notification batching, and local-only privacy model before users reach the main app. It does not request sensitive permissions during the walkthrough.

Usage access is requested only when the user enables daily app quotas. Notification listener access is requested only when the user enables notification batching. Wi-Fi/network state is used only when the user enables context profiles with SSID-based switching.

## Release Notes

### 1.2.0

- Added a first-run walkthrough that introduces the launcher, friction tools, profiles, notification batching, and local privacy.
- Updated onboarding visuals for home/search, focus friction, and privacy.
- No change to data collection, sharing, ads, analytics, or cloud sync behavior.
