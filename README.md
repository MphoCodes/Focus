# Focus Launcher

Focus Launcher is a minimalist, distraction-free home screen replacement for Android. Built with Kotlin and Jetpack Compose, it is designed around the principles of **Digital Stoicism**—helping you ask what must be done rather than consuming what is presented to you.

The user interface is inspired by premium, Swiss-style typography mockups from **Google Stitch**.

---

## Features

* **Intent Focus**: Displays a customizable intent header right on the home screen to keep you focused on your goals.
* **Favorites Menu**: Allows selecting up to 7 primary applications. The rest of the interface is kept completely blank to reduce visual clutter.
* **Clean App Drawer**: A borderless, minimalist scrollable listing of all installed launchable applications with live search.
* **Healthy Friction (Pause Mode)**: Designated apps (like social media or attention-grabbing feeds) trigger an intervention countdown timer (5s, 10s, 20s) and ask you why you are opening them before letting you proceed.
* **Hide Applications**: Easily hide distracting apps from both the search results and the drawer.
* **App Disguises**: Rename distracting apps with dull labels to reduce visual triggers while keeping launch behavior intact.
* **Strict App Time Quotas**: Set daily limits for specific apps and hard-block launches once the quota is exhausted.
* **Contextual Focus Profiles**: Configure Work, Home, and Weekend app lists with manual switching, time/day rules, and optional Wi-Fi SSID triggers.
* **Notification Batching**: Intercept non-essential notifications and deliver batched summaries at 12:00 and 18:00 while essential apps bypass the filter.
* **Window Insets Compatibility**: Native edge-to-edge rendering supporting status bar and gesture navigation padding on modern Android versions.
* **Full Localization**: Zero hardcoded strings. All copy is managed via Android resource values (`strings.xml`) for easy translations.

---

## Architecture & Tech Stack

The application conforms to standard Android architecture guidelines:
* **UI Framework**: Jetpack Compose using Material 3 styling tokens.
* **Architecture Pattern**: MVVM (Model-View-ViewModel) with unidirectional data flow (UDF).
* **Concurrency**: Kotlin Coroutines & Flows for reactive state management.
* **Persistence**: Jetpack DataStore (Preferences DataStore) for launcher settings, app lists, profiles, quotas, aliases, and notification batching state.
* **Build System**: Kotlin Gradle DSL (`build.gradle.kts`) with toolchain targets.
* **Java Version**: JDK 17.
* **Target SDK**: Android 16 (API 36).

---

## Getting Started

### Prerequisites
* **Android Studio** (Koala or newer recommended)
* **JDK 17** installed and configured
* **Android SDK 36** (target) and **SDK 26** (minimum)

### How to Compile Locally
1. Clone the repository:
   ```bash
   git clone https://github.com/MphoCodes/Focus.git
   cd Focus
   ```
2. Build the project using the Gradle wrapper:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run unit checks and linting:
   ```bash
   ./gradlew test
   ./gradlew lint
   ```

---

## Releasing Strategy

### 1. Semantic Versioning
This project uses [Semantic Versioning](https://semver.org/):
* **Major version (X.y.z)**: Big changes (e.g., UI redesigns, complete architectural shifts).
* **Minor version (x.Y.z)**: New features (e.g., custom prompts, stats pages).
* **Patch version (x.y.Z)**: Bug fixes, minor visual adjustments, and layout corrections.

Increment the `versionCode` (integer) and update the `versionName` (string) in `app/build.gradle.kts` before creating a release.

### 2. Manual App Store Release
To prepare an Android App Bundle (AAB) for deployment to the Google Play Store:
1. Generate a upload Keystore (if not already done):
   ```bash
   keytool -genkey -v -keystore release-key.keystore -alias focus-key -keyalg RSA -keysize 2048 -validity 10000
   ```
2. Configure project variables or environment values in your local `local.properties` or system variables (`SIGNING_KEY_ALIAS`, `SIGNING_KEY_PASSWORD`, etc.).
3. Build the release App Bundle:
   ```bash
   ./gradlew bundleRelease
   ```
   The generated `.aab` file will be saved under `app/build/outputs/bundle/release/app-release.aab`.

### 3. Automated CI/CD & GitHub Releases
Our release workflow is fully automated through **GitHub Actions**:
* **On Pull Request / Push to `main`**: Runs code compilation checks and Gradle verification.
* **On Pushing a Tag (`v*`)**:
  * Compiles the production-ready Release APK.
  * Automatically creates a published **GitHub Release**.
  * Uploads the generated unsigned release APK as an asset for signing, testing, or distribution preparation.
