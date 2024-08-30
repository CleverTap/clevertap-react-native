package com.clevertap.react;

import static com.clevertap.react.CleverTapUtils.getWritableMapFromMap;
import static com.clevertap.react.CleverTapUtils.sendEventEnsureInitialization;

import android.util.Log;

import com.clevertap.android.sdk.Application;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.facebook.react.ReactApplication;

import java.util.HashMap;

public class CleverTapApplication extends Application implements CTPushNotificationListener {
    private static final String TAG = "CleverTapApplication";
    @Override
    public void onCreate() {
        super.onCreate();
        CleverTapAPI.getDefaultInstance(this)
                .setCTPushNotificationListener(this);// Workaround when app is in killed state
    }

    //Push Notification Clicked callback workaround when app is in killed state
    @Override
    public void onNotificationClickedPayloadReceived(final HashMap<String, Object> payload) {
        Log.e(TAG, "onNotificationClickedPayloadReceived called");
        sendEventEnsureInitialization("CleverTapPushNotificationClicked",
                getWritableMapFromMap(payload),
                (ReactApplication) getApplicationContext());
    }
}
