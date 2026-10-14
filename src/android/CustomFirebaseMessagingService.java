package com.androidfcm.cordova;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import android.util.Log;
import java.util.Map;

public class CustomFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "custom-fcm-service";

    static boolean includesWebEngage() {
        try {
            Class.forName("com.webengage.sdk.android.WebEngage");
            return true;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "WebEngage Not Found", e);
        } catch (Throwable t) {
            Log.e(TAG, "Error while checking for WebEngage", t);
        }
        return false;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // FCM can start the app process with no JavaScript running, so the SDK is started here
        // from the manifest values (license key). Only WebEngage payloads are handed to it.
        Map<String, String> data = remoteMessage.getData();
        if (data != null) {
            if (includesWebEngage()) {
                com.webengage.sdk.android.WebEngage.engage(getApplicationContext());
                if (data.containsKey("source") && "webengage".equals(data.get("source"))) {
                    com.webengage.sdk.android.WebEngage.get().receive(data);
                }
            }
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        // Send token to WebEngage. Initialising the SDK is the app's job (webengage.engage()),
        // not this plugin's: if the SDK is not engaged yet, WebEngage.get() queues the call
        // and runs it once the app engages.
        if (includesWebEngage()) {
            com.webengage.sdk.android.WebEngage.get().setRegistrationID(s);
        }
    }
}
