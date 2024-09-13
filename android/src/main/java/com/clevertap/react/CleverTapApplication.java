package com.clevertap.react;

import static com.clevertap.react.CleverTapUtils.getWritableMapFromMap;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.clevertap.android.sdk.Application;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CleverTapApplication extends Application implements CTPushNotificationListener {
    private static final String TAG = "CleverTapApplication";
    public static Map<String, Object> emitList = new HashMap<>();

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
        final String CLEVERTAP_PUSH_NOTIFICATION_CLICKED = "CleverTapPushNotificationClicked";
        emitList.put(CLEVERTAP_PUSH_NOTIFICATION_CLICKED, getWritableMapFromMap(payload));
    }

    private void sendEvent(String eventName, Object params, ReactContext context) {
        try {
            context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
            Log.e(TAG, "Sending event "+eventName);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

}
