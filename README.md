Push notifications are messages that pop up on mobile devices. App publishers can send them at any time, even if the recipient isn't currently using the app or their device.

The WebEngage FCM plugin connects Firebase Cloud Messaging (FCM) to the WebEngage SDK in your Cordova Android app, so that you can send push notifications from the WebEngage dashboard.

> Supported from `we-cordova-android-fcm` 2.0.0 with `cordova-plugin-webengage` 2.0.0 onwards.

## Prerequisites

Before integrating push notifications, ensure the following:

1. Complete the WebEngage SDK initialization steps in the [Cordova integration guide](https://docs.webengage.com/docs/cordova), including the license code in `config.xml` and the `webengage.engage()` call at app launch.

2. Create a Firebase project for your Android app.

3. Make sure your app uses a supported version:

| Requirement                                        | Minimum version |
| -------------------------------------------------- | --------------- |
| `cordova-android`                                  | 12.0.0          |
| `cordova-plugin-webengage` (WebEngage Cordova SDK) | 2.0.0           |

<Callout icon="⚠️" theme="warn">
  ### Using an older Cordova version?

  If your app uses `cordova-android` 11 or lower, install version 1.0.0 explicitly:

  `cordova plugin add we-cordova-android-fcm@1.0.0`

  Version 2.0.0 is skipped on these versions with only a warning, and the app builds without push notifications.
</Callout>

<Callout icon="⚠️" theme="warn">
  ### Already have your own FirebaseMessagingService?

  Android delivers push messages to only one `FirebaseMessagingService`. This plugin registers its own, so if your app or another plugin (for another push provider) already has one, don't install this plugin. Follow [Using your own FirebaseMessagingService](#using-your-own-firebasemessagingservice) instead.
</Callout>

## Installation

### Step 1: Integrate WebEngage SDK

Integrate the WebEngage SDK by following the integration steps mentioned in the [Cordova documentation](https://docs.webengage.com/docs/cordova).

### Step 2: Install the FCM plugin

Install the plugin using the following command:

```bash
cordova plugin add we-cordova-android-fcm
```

### Step 3: Add google-services.json

Follow the steps under [Android FCM Documentation](https://firebase.google.com/docs/android/setup) to get the `google-services.json` file from your Firebase project, then save it in the root of your project directory (next to `config.xml`).

The plugin copies the file into your Android project and turns on Google Services processing on every build, so no change is needed in your `config.xml`.

<Callout icon="⚠️" theme="warn">
  ### GradlePluginGoogleServicesEnabled

  If your `config.xml` sets `GradlePluginGoogleServicesEnabled` to `false` (a common 1.0.0 workaround), remove that line or set it to `true`; otherwise push stops in 2.0.0. If you apply Google Services yourself, apply it exactly once.

  The build still succeeds in that case. The only sign is a `[we-cordova-android-fcm] GradlePluginGoogleServicesEnabled is false` warning in the build output.
</Callout>

### Step 4: Upload your Firebase key to the WebEngage dashboard

1. In the Firebase Console, go to **Project Settings** → **Service Accounts** → **Generate New Private Key**.
2. In the WebEngage dashboard, go to **Data Platform** → **Integrations** → **Push Setup**, and upload that JSON key.

Push notifications are not delivered until this step is done, even if every step above is correct.

<br />

## FCM APIs

<Callout icon="📘" theme="info">
  ### Callbacks

  `getToken()` returns the token only through the success callback, so always pass one. `updateToken()` takes no arguments.
</Callout>

### Update Token

The `updateToken()` function sends the current FCM token of the device to WebEngage. Call it at every app launch.

#### Function Signature

```javascript
androidfcm.updateToken();
```

#### Usage

In your `www/js/index.js`:

```javascript
var app = {
    ...
    onDeviceReady: function() {
        ...
        androidfcm.updateToken();
    }
}
```

<br />

### Get Token

The `getToken()` function returns the current FCM token of the device, for example to use it in your own code. You don't need it to send push notifications through WebEngage.

#### Function Signature

```javascript
androidfcm.getToken(successCallback, errorCallback)
```

#### Parameters

| Parameter       | Description                                                | Mandatory |
| --------------- | ---------------------------------------------------------- | --------- |
| successCallback | Receives the FCM token.                                    | Yes       |
| errorCallback   | Receives an error message if the token cannot be fetched.  | No        |

#### Returns

The success callback receives the FCM token as a string.

#### Usage

```javascript
androidfcm.getToken(
    function(token) {
        console.log("FCM token: " + token);
    },
    function(error) {
        console.error("Error getting FCM token: " + error);
    }
);
```

<br />

***

## Making your app compatible with Android 13 push changes

From Android 13 onwards, apps must ask the user for permission before they can show push notifications. Until the user grants it, notifications are delivered but not displayed.

Request the runtime permission, and pass WebEngage the actual result. Never pass `true` regardless of what the user chose. The example below uses [`cordova-plugin-android-permissions`](https://github.com/apache/cordova-plugin-android-permissions) and [`cordova-plugin-device`](https://github.com/apache/cordova-plugin-device):

```javascript
function requestPushPermission() {
    var permissions = cordova.plugins.permissions;

    permissions.checkPermission(permissions.POST_NOTIFICATIONS, function (status) {
        if (status.hasPermission) {
            webengage.user.setDevicePushOptIn(true);
        } else {
            permissions.requestPermission(permissions.POST_NOTIFICATIONS, function (status) {
                webengage.user.setDevicePushOptIn(status.hasPermission);
            }, function (error) {
                console.error("Error requesting push notification permission:", error);
            });
        }
    }, function (error) {
        console.error("Error checking push notification permission:", error);
    });
}

// Only needed on Android 13+
document.addEventListener("deviceready", function () {
    if (cordova.platformId === "android" && parseFloat(device.version) >= 13) {
        requestPushPermission();
    }
});
```

<Callout icon="📘" theme="info">
  ### Please note

  - Pass `true` or `false` based on the permission the user actually granted. If they deny it, they won't receive push notifications.
  - Showing the permission prompt is the app's responsibility, not the SDK's.
</Callout>

<br />

***

## Using your own FirebaseMessagingService

Use this instead of installing the plugin if your app already has its own `FirebaseMessagingService` (for another push provider, for example). Add the code below to your existing service. It hands WebEngage push messages and tokens to the WebEngage SDK, so you don't need the FCM APIs above.

```java Java
import com.google.firebase.messaging.RemoteMessage;
import com.webengage.sdk.android.WebEngage;

import java.util.Map;

@Override
public void onMessageReceived(RemoteMessage remoteMessage) {
    Map<String, String> data = remoteMessage.getData();
    if (data != null && "webengage".equals(data.get("source"))) {
        WebEngage.engage(getApplicationContext());
        WebEngage.get().receive(data);
        return;
    }
    // your other provider(s)' handling
}

@Override
public void onNewToken(String token) {
    super.onNewToken(token);
    WebEngage.get().setRegistrationID(token);
}
```
```kotlin Kotlin
import com.google.firebase.messaging.RemoteMessage
import com.webengage.sdk.android.WebEngage

override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val data = remoteMessage.data
    if (data["source"] == "webengage") {
        WebEngage.engage(applicationContext)
        WebEngage.get().receive(data)
        return
    }
    // your other provider(s)' handling
}

override fun onNewToken(token: String) {
    super.onNewToken(token)
    WebEngage.get().setRegistrationID(token)
}
```

Then complete these steps:

1. [Step 1: Integrate WebEngage SDK](#step-1-integrate-webengage-sdk).
2. Make sure `google-services.json` is processed. Your existing push setup usually does this already. If not, add `<preference name="GradlePluginGoogleServicesEnabled" value="true" />` inside `<platform name="android">` in your `config.xml`.
3. [Step 4: Upload your Firebase key to the WebEngage dashboard](#step-4-upload-your-firebase-key-to-the-webengage-dashboard).

<br />

***

## Upgrading from 1.0.0

Version 2.0.0 requires `cordova-plugin-webengage` 2.0.0 or newer, so upgrade both plugins together.

Running `cordova plugin add` on a plugin that is already installed does not update it. Remove the WebEngage plugins and add them again, in this order:

```bash
# 1. Remove (Notification Inbox first, then FCM, then the core plugin)
cordova plugin rm we-notificationinbox-cordova   # only if you use Notification Inbox
cordova plugin rm we-cordova-android-fcm
cordova plugin rm cordova-plugin-webengage

# 2. Add (core plugin first, then FCM, then Notification Inbox)
cordova plugin add cordova-plugin-webengage@2.0.0
cordova plugin add we-cordova-android-fcm@2.0.0
cordova plugin add we-notificationinbox-cordova@2.0.0   # only if you use Notification Inbox
```

Run `cordova plugin ls` and confirm that all WebEngage plugins show version 2.0.0.

After upgrading:

- If your `config.xml` sets `GradlePluginGoogleServicesEnabled` to `false`, remove that line or set it to `true`. See [Step 3](#step-3-add-google-servicesjson).
- Optional: the plugin now adds the `firebase-messaging` dependency and Google Services itself, so you can remove the `firebase-messaging` line and any Google Services setup you added to `build-extras.gradle` for 1.0.0.

<Callout icon="📘" theme="info">
  ### Later upgrades of the core plugin

  Cordova does not remove `cordova-plugin-webengage` while this plugin is installed. To upgrade the core plugin later, use the same remove and add order as above.
</Callout>

## Sample Application

You can refer to our [sample project](https://github.com/WebEngage/cordova-sample).

Please feel free to drop in a few lines at **[support@webengage.com](mailto:support@webengage.com)** in case you have any further queries. We're always just an email away.
