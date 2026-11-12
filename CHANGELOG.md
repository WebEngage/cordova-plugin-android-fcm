# Changelog

## 2.0.0

Multi-version support for cordova-android 12.x through 15.x. The Gradle script no
longer owns host-app settings, so it works across AGP 7.4–8.10 / Gradle 7.6–8.14.

### Breaking changes (read before upgrading from 1.0.0)

- **Google Services: check `GradlePluginGoogleServicesEnabled`.** If your `config.xml`
  sets `GradlePluginGoogleServicesEnabled` to `false` (a common 1.0.0 workaround), remove
  that line or set it to `true`; otherwise push stops in 2.0.0. If you apply Google
  Services yourself, apply it exactly once. The build still passes in that case, so the
  only sign is a build warning from the plugin and push not arriving.
- **Host floor raised** to cordova-android `>= 12.0.0` (`plugin.xml` `<engines>` and
  `package.json` `engines.cordovaDependencies["2.0.0"]`). Apps on older hosts stay on
  1.0.0: install `we-cordova-android-fcm@1.0.0` explicitly. Released as 2.0.0 so that
  `^1.x` ranges never pick this up.
- **Requires `cordova-plugin-webengage >= 2.0.0`.** It is declared as a `<dependency>`
  (installed automatically when missing) and in `engines.cordovaDependencies["2.0.0"]`,
  so in a project that has `cordova-plugin-webengage` 1.x installed,
  `cordova plugin add we-cordova-android-fcm` with no version resolves FCM 1.0.0. An
  explicit `@2.0.0` fails there with a dependency error instead.
- **The plugin no longer ships its own Gradle toolchain.** 1.0.0 carried its own
  `buildscript` (AGP 7.2.2, google-services 4.3.12), `allprojects`, SDK-level
  overrides, `lintOptions` and an `ext.postBuildExtras` that always applied the Google
  Services plugin. All of that is removed. `plugin.xml` now sets cordova-android's own
  `<preference name="GradlePluginGoogleServicesEnabled" value="true" />`, so with no
  app change `google-services.json` is still processed. The Google Services version now
  comes from cordova-android (4.3.15 on 12.x/13.x, 4.4.2 on 14.x/15.x); set
  `GradlePluginGoogleServicesVersion` to change it. An app's own `false` overrides the
  plugin (see the first item). The removed `lintOptions { abortOnError false }` applied to
  the whole app module, so a release build can now stop on fatal lint errors that 1.0.0
  hid; fix them, or set `android { lint { abortOnError false } }` in the app's own
  `build-extras.gradle`.
- **Upgrading needs remove + add.** `cordova plugin add` over an installed 1.0.0 does
  nothing ("already installed"). Remove inbox (if used), FCM, then core; add core, FCM,
  then inbox. The same order applies to later core upgrades: Cordova does not remove
  `cordova-plugin-webengage` while this plugin depends on it.
- **The `CustomFirebaseMessagingService` is now `android:exported="false"`** (Firebase
  recommendation); message delivery is unchanged.

### Behavior changes

- `getToken` now calls its error callback when the token cannot be read (in 1.0.0
  neither callback was called). The signature is unchanged. `updateToken()` is
  unchanged: no arguments, fire and forget.
- `CustomFirebaseMessagingService` and `updateToken`: an exception or linkage error while
  handing a push or a token to WebEngage is logged instead of terminating the app.
- `CustomFirebaseMessagingService` starts the WebEngage SDK only for WebEngage pushes (in
  1.0.0 it was started for every push). WebEngage pushes are handled as before; other
  pushes are still ignored.
- The plugin declares `com.google.firebase:firebase-messaging:23.4.1`. It is not a
  transitive dependency of `com.webengage:android-sdk`, so 1.0.0 relied on the app adding
  it (for example in `build-extras.gradle`). Gradle uses the highest declared version:
  an app that declared an older version now gets 23.4.1; that app-level line can be
  removed.
- The FCM token is no longer written to the log by `updateToken`.
- Log tag: all plugin log lines now use `WebEngage-Cordova-FCM` (1.0.0 used
  `we-cordova-android-fcm` and `custom-fcm-service`). Update any logcat filter that used
  the old tags.

### Added

- `<engines>` and `package.json` `cordovaDependencies` for the real host floor and the
  core-plugin floor, so incompatible projects resolve an earlier compatible release when
  no version is pinned.
- `package.json` `files` allowlist, so only the plugin's shipping files are published.

### Changed

- `after_prepare` hook rewritten as `module.exports = function (context)` using
  `context.opts.projectRoot`; it now creates the destination folder and warns when
  `GradlePluginGoogleServicesEnabled` is `false`, also when the app copies
  `google-services.json` itself. Same user-facing behaviour (drop `google-services.json`
  in the project root).
- README rewritten in the official docs layout and merged with the Cordova push
  documentation: prerequisites and minimum versions, npm install, Google Services,
  Firebase key upload, `updateToken` / `getToken`, Android 13 notification permission,
  using your own `FirebaseMessagingService`, and upgrading from 1.0.0.

### Notes

- This plugin is for Cordova apps only. Capacitor apps use `we-cap-android-fcm`.
- Initialising WebEngage is the app's job: call `webengage.engage()` at launch and
  put the license key in the manifest (`com.webengage.sdk.android.key`). When a push
  arrives while the app is not running, the plugin's service starts the SDK from the
  manifest values (unchanged from 1.0.0), because no JavaScript runs in that case.
  A token refresh is passed to the SDK without initialising it (unchanged from 1.0.0);
  the SDK queues it until the app engages.
