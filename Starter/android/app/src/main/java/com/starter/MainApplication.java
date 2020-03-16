package com.starter;

import android.app.Application;

import com.facebook.react.ReactApplication;
import com.clevertap.react.CleverTapPackage;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.ActivityLifecycleCallback;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

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
    CleverTapAPI.setUIEditorConnectionEnabled(false);
    ActivityLifecycleCallback.register(this); 
    super.onCreate();
    SoLoader.init(this, /* native exopackage */ false);
  }
}
