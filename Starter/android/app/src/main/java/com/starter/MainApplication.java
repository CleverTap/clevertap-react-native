package com.starter;

import android.app.Application;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.multidex.MultiDexApplication;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.facebook.react.ReactApplication;
import com.clevertap.react.CleverTapPackage;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class MainApplication extends MultiDexApplication implements ReactApplication, CTPushNotificationListener {

  private final String TAG = "MainApplication";

  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
    @Override
    public boolean getUseDeveloperSupport() {
      return BuildConfig.DEBUG;
    }

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
            new CleverTapPackage()
      );
    }

    @Override
    protected String getJSMainModuleName() {
      return "index";
    }
  };

  @Override
  public ReactNativeHost getReactNativeHost() {
    return mReactNativeHost;
  }

  @Override
  public void onCreate() {
    ActivityLifecycleCallback.register(this);
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
    CleverTapAPI.getDefaultInstance(this).setCTPushNotificationListener(this);
  }

  //Push Notification Clicked callback
  @Override
  public void onNotificationClickedPayloadReceived(HashMap<String, Object> payload) {
    Log.e("MainApplication", "onNotificationClickedPayloadReceived called");
    final String CLEVERTAP_PUSH_NOTIFICATION_CLICKED = "CleverTapPushNotificationClicked";

    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      public void run() {

        // Construct and load our normal React JS code bundle
        final ReactInstanceManager mReactInstanceManager = ((ReactApplication) getApplicationContext()).getReactNativeHost().getReactInstanceManager();
        ReactContext context = mReactInstanceManager.getCurrentReactContext();
        // If it's constructed, send a notification
        if (context != null) {
          sendEvent(CLEVERTAP_PUSH_NOTIFICATION_CLICKED,getWritableMapFromMap(payload),context);
        } else {
          // Otherwise wait for construction, then send the notification
          mReactInstanceManager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
            public void onReactContextInitialized(ReactContext context) {
              sendEvent(CLEVERTAP_PUSH_NOTIFICATION_CLICKED,getWritableMapFromMap(payload),context);
              mReactInstanceManager.removeReactInstanceEventListener(this);
            }
          });
          if (!mReactInstanceManager.hasStartedCreatingInitialContext()) {
            // Construct it in the background
            mReactInstanceManager.createReactContextInBackground();
          }
        }

      }
    });

  }

  private void sendEvent(String eventName, Object params,ReactContext context) {

    try {
      context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, params);
    } catch (Throwable t) {
      Log.e(TAG, t.getLocalizedMessage());
    }
  }

  private WritableMap getWritableMapFromMap(Map<String, ? extends Object> var1) {
    JSONObject extras = var1 != null ? new JSONObject(var1) : new JSONObject();
    WritableMap extrasParams = Arguments.createMap();
    Iterator extrasKeys = extras.keys();
    while (extrasKeys.hasNext()) {
      String key = null;
      String value = null;
      try {
        key = extrasKeys.next().toString();
        value = extras.get(key).toString();
      } catch (Throwable t) {
        Log.e(TAG, t.getLocalizedMessage());
      }

      if (key != null && value != null) {
        extrasParams.putString(key, value);
      }
    }
    return extrasParams;
  }
}
