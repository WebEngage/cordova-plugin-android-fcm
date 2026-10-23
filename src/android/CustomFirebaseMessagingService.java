package com.androidfcm.cordova;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = WE_FCM_CONSTANTS.TAG;

    static boolean includesWebEngage() {
        try {
            Class.forName(WE_FCM_CONSTANTS.WEBENGAGE_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "CustomFirebaseMessagingService: WebEngage not found", e);
        } catch (Throwable t) {
            Log.e(TAG, "CustomFirebaseMessagingService: error while checking for WebEngage", t);
        }
        return false;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // FCM can start the app process with no JavaScript running, so the SDK is started here
        // from the manifest values (license key). Only WebEngage payloads are handed to it.
        // This runs in the app process for every push: an exception here must not terminate the app.
        try {
            Map<String, String> data = remoteMessage.getData();
            if (data != null) {
                if (includesWebEngage()) {
                    com.webengage.sdk.android.WebEngage.engage(getApplicationContext());
                    if (data.containsKey(WE_FCM_CONSTANTS.PAYLOAD_SOURCE) && WE_FCM_CONSTANTS.PAYLOAD_SOURCE_WEBENGAGE.equals(data.get(WE_FCM_CONSTANTS.PAYLOAD_SOURCE))) {
                        com.webengage.sdk.android.WebEngage.get().receive(data);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "CustomFirebaseMessagingService: push was not handed to WebEngage", e);
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        // Send token to WebEngage. Initialising the SDK is the app's job (webengage.engage()),
        // not this plugin's: if the SDK is not engaged yet, WebEngage.get() queues the call
        // and runs it once the app engages.
        try {
            if (includesWebEngage()) {
                com.webengage.sdk.android.WebEngage.get().setRegistrationID(s);
            }
        } catch (Exception e) {
            Log.e(TAG, "CustomFirebaseMessagingService: token was not handed to WebEngage", e);
        }
    }
}
