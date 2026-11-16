# Project memory

- Canonical project ID: `MphoCodes/Focus`; origin: `https://github.com/MphoCodes/Focus.git`.
- Native Android launcher, Kotlin/Compose, one app module, API 26 minimum/API 36 target, JDK 17.
- Screen routing lives in `FocusLauncherApp`; screens, shared components and presentation helpers are separate files. Preserve `FocusText` scaling when editing UI.
- Quotas only gate launches through Focus. Notification batching stores counts and does not replay original content. SSID availability and summary timing depend on Android.
- No internet permission. Never commit signing credentials; release workflow uses the existing repository signing secrets.
- Treat `HANDOFF.md` as a dated verification snapshot, not proof of current remote/deployment state.
