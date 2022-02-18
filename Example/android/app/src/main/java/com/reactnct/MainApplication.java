package com.reactnct;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.clevertap.android.pushtemplates.PushTemplateNotificationHandler;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CleverTapAPI.LogLevel;
import com.clevertap.android.sdk.interfaces.NotificationHandler;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.clevertap.react.CleverTapApplication;
import com.facebook.react.PackageList;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.soloader.SoLoader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class MainApplication extends CleverTapApplication
        implements ActivityLifecycleCallbacks, ReactApplication{

    private static final String TAG = "MainApplication";

    private final ReactNativeHost mReactNativeHost =
            new ReactNativeHost(this) {
                @Override
                public boolean getUseDeveloperSupport() {
                    return BuildConfig.DEBUG;
                }

                @Override
                protected List<ReactPackage> getPackages() {
                    @SuppressWarnings("UnnecessaryLocalVariable")
                    List<ReactPackage> packages = new PackageList(this).getPackages();
                    // Packages that cannot be autolinked yet can be added manually here, for example:
                    // packages.add(new MyReactNativePackage());
//            packages.add(new CleverTapPackage()); // not to add if done by autolinking
                    return packages;
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
        CleverTapAPI.setDebugLevel(LogLevel.VERBOSE);
        CleverTapAPI.setNotificationHandler((NotificationHandler) new PushTemplateNotificationHandler());
        CleverTapAPI.getDefaultInstance(getApplicationContext()).enableDeviceNetworkInfoReporting(true);
        registerActivityLifecycleCallbacks(this);
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);
        initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
    }

    /**
     * Loads Flipper in React Native templates. Call this in the onCreate method with something like
     * initializeFlipper(this, getReactNativeHost().getReactInstanceManager());
     */
    private static void initializeFlipper(
            Context context, ReactInstanceManager reactInstanceManager) {
        if (BuildConfig.DEBUG) {
            try {
        /*
         We use reflection here to pick up the class that initializes Flipper,
        since Flipper library is not available in release mode
        */
                Class<?> aClass = Class.forName("com.reactnct.ReactNativeFlipper");
                aClass
                        .getMethod("initializeFlipper", Context.class, ReactInstanceManager.class)
                        .invoke(null, context, reactInstanceManager);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(@NonNull final Activity activity, @Nullable final Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull final Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull final Activity activity) {
        Bundle payload = activity.getIntent().getExtras();
        if (payload != null && payload.containsKey("pt_id") && (payload.getString("pt_id").equals("pt_rating")
                || payload
                .getString("pt_id").equals("pt_product_display"))) {
            NotificationManager nm = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(payload.getInt("notificationId"));
        }
    }

    @Override
    public void onActivityPaused(@NonNull final Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull final Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(@NonNull final Activity activity, @NonNull final Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull final Activity activity) {

    }
}
