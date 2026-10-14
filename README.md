# WebEngage Cordova SDK for Firebase Cloud Messaging (FCM) on Android

Bridges Firebase Cloud Messaging registration tokens and push messages to the
WebEngage Android SDK for Cordova apps.

> **Cordova only.** This plugin targets Cordova Android apps. For Capacitor apps
> use [`we-cap-android-fcm`](https://www.npmjs.com/package/we-cap-android-fcm)
> instead — installing this plugin in a Capacitor project is not supported.

## Supported versions

| Component        | Supported            |
|------------------|----------------------|
| cordova-android  | `>= 12.0.0` (tested 12.0.1, 13.0.0, 14.0.1, 15.1.0) |
| Base plugin      | `cordova-plugin-webengage >= 2.0.0` (installed automatically as a dependency) |
| firebase-messaging | `23.4.1` (bundled by this plugin) |

## Pre-requisites

1. **WebEngage Cordova Plugin** — [`cordova-plugin-webengage`](https://github.com/WebEngage/cordova-plugin).
   It is declared as a dependency of this plugin, so it installs automatically,
   but your app must still initialise it (call `engage()` at launch) and put the
   WebEngage license key in the manifest as documented there. This plugin does not
   initialise WebEngage for you; it only starts the SDK from the manifest values when a
   push arrives while the app is not running.

## Installation

```sh
cordova plugin add https://github.com/WebEngage/cordova-plugin-android-fcm.git
```

(The old `--fetch` flag is obsolete; fetching is the default since cordova@8.)

### 1. Add `google-services.json`

Follow the [Firebase Android setup](https://firebase.google.com/docs/android/setup)
to download `google-services.json`, and save it in the **root of your project
directory**. An `after_prepare` hook copies it into the Android app module on
every build.

### 2. Google Services

Firebase can only start if the Google Services Gradle plugin has processed `google-services.json`.
This plugin turns on cordova-android's official support for that
(`GradlePluginGoogleServicesEnabled`), so **no change is needed in your `config.xml`**.

Requirement: Google Services must be applied exactly once.
- If your app (or `build-extras.gradle`, or another plugin) already applies the Google
  Services plugin itself, remove that.
- If you set `GradlePluginGoogleServicesEnabled` to `false`, you must apply Google Services
  yourself. Otherwise Firebase cannot start and push does not work. Version 1.0.0 applied it
  for you even then; 2.0.0 does not.

Optional, in your `config.xml`:

```xml
<platform name="android">
    <!-- choose the Google Services version; defaults to the cordova-android bundled version -->
    <!-- <preference name="GradlePluginGoogleServicesVersion" value="4.4.2" /> -->
</platform>
```

### 3. Android 13+ notification permission

On Android 13 (API 33) and above, apps must request the runtime permission
`android.permission.POST_NOTIFICATIONS` before notifications are shown. Request
it from your app (for example via the WebEngage push opt-in flow or a permission
plugin). Without it, pushes are delivered but not displayed.

## API

```js
// Push the current FCM token to WebEngage (fire and forget, no arguments).
androidfcm.updateToken();

// Fetch the current FCM token.
androidfcm.getToken(function (token) { /* ... */ }, function (err) { /* ... */ });
```

## One FCM service only

Firebase routes `com.google.firebase.MESSAGING_EVENT` to a single
`FirebaseMessagingService`. This plugin registers
`com.androidfcm.cordova.CustomFirebaseMessagingService`, which forwards
WebEngage messages and drops non-WebEngage payloads. If your app (or another
plugin) also declares an FCM service, only one will receive messages — do not
register a second `FirebaseMessagingService`.

## Sample Application

See the [sample project](https://github.com/WebEngage/cordova-sample).
