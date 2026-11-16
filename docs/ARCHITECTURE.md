# Architecture

Focus is a single Android application module. Compose renders immutable `LauncherUiState`; user actions call `FocusLauncherViewModel`, which coordinates app access and local settings. It is a practical MVVM-style structure, not a multi-module clean-architecture implementation.

## Responsibilities

| Area | Responsibility |
| --- | --- |
| `MainActivity` | Android activity entry and theme |
| `ui/FocusLauncherApp` | State collection, navigation routing, permission launchers and lifecycle refresh |
| `ui/*Screen(s)` | Onboarding, home/drawer, workspaces, settings, permissions and launch intervention presentation |
| `ui/LauncherComponents`, `SettingsComponents` | Reusable Compose rows, shell, controls and app text scaling |
| `ui/LauncherPresentation` | Display labels, list filtering and schedule presentation helpers |
| `ui/FocusLauncherViewModel` | UI state aggregation, launch checks, profile selection and settings actions |
| `data/repository/AppsRepository` | Package manager, app launch, system settings and usage access |
| `data/repository/SettingsRepository` | Preferences DataStore and serialization |
| `notification` | Notification listener, summary delivery and WorkManager scheduling |

Screen and shared presentation functions are module-internal where they cross file boundaries. File-local helpers remain private. `FocusText` explicitly applies the app's text-size preference; screens must not accidentally bypass it with Material `Text`.

## Data and permission boundaries

Settings, selections, local counters and notification counts stay in DataStore. The manifest does not request internet access. Usage access and notification listener access are opt-in Android settings, distinct from the Android 13+ permission to display notifications.

Quotas are launcher checks, not operating-system enforcement. Notification summaries aggregate counts rather than replaying notification contents. Wi-Fi matching depends on system-provided SSID availability.

## Verification and remaining work

JVM tests cover profile resolution, quota presets, workspace filtering/serialization, settings behavior and notification delivery. Compose instrumentation tests live under `androidTest` and require a device or emulator; ordinary JVM test tasks do not execute them.

The ViewModel still directly constructs its repositories and contains substantial orchestration. Future behavior changes should introduce narrow injectable boundaries as needed, with regression tests, rather than add presentation or Android service logic to screen files. Full localization and broader device coverage remain work in progress.
