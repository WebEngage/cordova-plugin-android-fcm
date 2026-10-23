# Changelog

## 2.0.0

Multi-version support for cordova-android 12.x through 15.x. The Gradle script no
longer owns host-app settings, so it works across AGP 7.4–8.10 / Gradle 7.6–8.14.

### Breaking changes (read before upgrading from 1.0.0)

- **Host floor raised** to cordova-android `>= 12.0.0` (`plugin.xml` `<engines>` and
  `package.json` `engines.cordovaDependencies["2.0.0"]`). Apps on older hosts stay on
  1.0.0 (pin it exactly; a `^1.0.0` range keeps resolving to 1.x only). Released as
  2.0.0 so that `^1.x` ranges never pick this up silently.
- **Requires `cordova-plugin-webengage >= 2.0.0`.** It is declared as a
  `<dependency>`, so it is installed automatically when it is missing. The Java
  sources import WebEngage classes directly.
- **The plugin no longer ships its own Gradle toolchain.** 1.0.0 carried its own
  `buildscript` (AGP 7.2.2, google-services 4.3.12), `allprojects`, SDK-level
  overrides, `lintOptions` and an `ext.postBuildExtras` that applied the Google
  Services plugin. All of that is removed. The plugin now turns on cordova-android's
  own support instead: `plugin.xml` sets
  `<preference name="GradlePluginGoogleServicesEnabled" value="true" />` for the
  app, so **no change is needed in the app** and `google-services.json` is still
  processed. The Google Services version now comes from cordova-android (4.3.15 on
  12.x/13.x, 4.4.2 on 14.x/15.x); set `GradlePluginGoogleServicesVersion` to change it.
  Requirement: Google Services must be applied exactly once.
  - An app that applies the Google Services plugin itself (for example in
    `build-extras.gradle`) must remove that.
  - **An app that sets `GradlePluginGoogleServicesEnabled` to `false` must apply Google
    Services itself.** 1.0.0 applied it even then; 2.0.0 does not, and without it
    Firebase cannot start and push does not work. A build warning is printed when the
    preference is false.
- **Upgrading:** `cordova plugin add` over an installed 1.0.0 does nothing ("already
  installed"). Run `cordova plugin remove we-cordova-android-fcm`, then
  `cordova plugin add we-cordova-android-fcm`.
- **The `CustomFirebaseMessagingService` is now `android:exported="false"`** (Firebase
  recommendation); message delivery is unchanged.

### Added

- `<engines>` and `package.json` `cordovaDependencies` for the real host floor, so
  incompatible older platforms resolve an earlier compatible release when no version
  is pinned.
- Explicit `com.google.firebase:firebase-messaging:23.4.1` dependency. It is not a
  transitive dependency of `com.webengage:android-sdk`, so builds previously relied on
  the app adding it manually (for example in `build-extras.gradle`). That app-level
  line can now be removed; if the app declares a newer version, the newer one wins.
- `package.json` `files` allowlist, so only the plugin's shipping files are published.
- `getToken` now calls its error callback when the token cannot be read (before, neither
  callback was called in that case). `updateToken()` is unchanged: no arguments, fire and
  forget.

### Changed

- `CustomFirebaseMessagingService`: an exception while handing a push or a new token to WebEngage is logged instead of terminating the app.
- `after_prepare` hook rewritten as `module.exports = function (context)` using
  `context.opts.projectRoot`; it now creates the destination folder. Same user-facing
  behaviour (drop `google-services.json` in the project root).
- README rewritten: supported-versions table, Google Services, Android 13
  notification permission, Cordova-only / Capacitor notice, and the one-FCM-service
  rule.

### Notes

- Initialising WebEngage is the app's job: call `webengage.engage()` at launch and
  put the license key in the manifest (`com.webengage.sdk.android.key`). When a push
  arrives while the app is not running, the plugin's service starts the SDK from the
  manifest values (unchanged from 1.0.0), because no JavaScript runs in that case.
  A token refresh is passed to the SDK without initialising it (unchanged from 1.0.0);
  the SDK queues it until the app engages.
