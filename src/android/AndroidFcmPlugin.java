package com.androidfcm.cordova;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.webengage.sdk.android.WebEngage;

/**
 * Bridges FCM registration tokens to the WebEngage SDK for Cordova Android apps.
 */
public class AndroidFcmPlugin extends CordovaPlugin {
    private static final String TAG = WE_FCM_CONSTANTS.TAG;

    @Override
    protected void pluginInitialize() {
        Log.d(TAG, "AndroidFcmPlugin initialized (we-cordova-android-fcm)");
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (WE_FCM_CONSTANTS.UPDATE_TOKEN.equals(action)) {
            this.updateToken();
            return true;
        } else if (WE_FCM_CONSTANTS.GET_TOKEN.equals(action)) {
            this.getToken(callbackContext);
            return true;
        } else if (WE_FCM_CONSTANTS.READY.equals(action)) {
            sendSuccessCallback(callbackContext);
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
                com.webengage.sdk.android.Logger.e(TAG, "AndroidFcmPlugin: FCM token error", task.getException());
                return;
            }
            try {
                com.webengage.sdk.android.Logger.d(TAG, "AndroidFcmPlugin: updating WebEngage FCM token");
                WebEngage.get().setRegistrationID(task.getResult());
            } catch (Exception e) {
                com.webengage.sdk.android.Logger.e(TAG, "AndroidFcmPlugin: FCM token error", e);
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
                String message = e != null ? e.getMessage() : WE_FCM_CONSTANTS.ERROR_UNKNOWN_TOKEN;
                com.webengage.sdk.android.Logger.e(TAG, "AndroidFcmPlugin: FCM token error", e);
                if (callbackContext != null) {
                    sendErrorCallback(callbackContext, message);
                }
            }
        });
    }

    // Replies success to the JS caller, with no value.
    private void sendSuccessCallback(CallbackContext callbackContext) {
        callbackContext.success();
    }

    // Replies error to the JS caller with a message.
    private void sendErrorCallback(CallbackContext callbackContext, String message) {
        callbackContext.error(message);
    }
}
