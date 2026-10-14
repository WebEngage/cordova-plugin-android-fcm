package com.androidfcm.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import com.google.firebase.messaging.FirebaseMessaging;
import com.webengage.sdk.android.WebEngage;

/**
 * Bridges FCM registration tokens to the WebEngage SDK for Cordova Android apps.
 */
public class AndroidFcmPlugin extends CordovaPlugin {
    private static final String TAG = "we-cordova-android-fcm";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if ("updateToken".equals(action)) {
            this.updateToken();
            return true;
        } else if ("getToken".equals(action)) {
            this.getToken(callbackContext);
            return true;
        } else if ("ready".equals(action)) {
            callbackContext.success();
            return true;
        }
        return false;
    }

    private void updateToken() {
        if (!CustomFirebaseMessagingService.includesWebEngage()) {
            return;
        }
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                com.webengage.sdk.android.Logger.e(TAG, "FCM token error", task.getException());
                return;
            }
            try {
                com.webengage.sdk.android.Logger.d(TAG, "Updating WebEngage FCM token");
                WebEngage.get().setRegistrationID(task.getResult());
            } catch (Exception e) {
                com.webengage.sdk.android.Logger.e(TAG, "FCM token error", e);
            }
        });
    }

    private void getToken(final CallbackContext callbackContext) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult();
                if (callbackContext != null) {
                    callbackContext.success(token);
                }
            } else {
                Exception e = task.getException();
                String message = e != null ? e.getMessage() : "Unknown FCM token error";
                com.webengage.sdk.android.Logger.e(TAG, "FCM token error", e);
                if (callbackContext != null) {
                    callbackContext.error(message);
                }
            }
        });
    }
}
