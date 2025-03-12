## Integrating the CleverTap SDK

After [install](./install.md), you will need to integrate the CleverTap SDK into your iOS and Android apps.

Clevertap supports the [ReactNative New Architecture](https://reactnative.dev/docs/the-new-architecture/landing-page) starting from clevertap-react-native **v3.0.0**, while still maintaining compatibility with the old architecture. Please refer to the [official guide](https://github.com/reactwg/react-native-new-architecture/blob/main/docs/enable-apps.md) to **optionally** enable New Architecture in your app.

> ⚠️ For app maintainers to migrate to the New Architecture, **all of their dependencies** must support the New Architecture as well


### iOS
1. Follow the integration instructions [here](https://developer.clevertap.com/docs/react-native-quick-start-guide#ios-integration).
2. In your `AppDelegate didFinishLaunchingWithOptions:` notify the CleverTap React SDK of application launch:
```objc
[CleverTap autoIntegrate]; // integrate CleverTap SDK using the autoIntegrate option
[[CleverTapReactManager sharedInstance] applicationDidLaunchWithOptions:launchOptions];
```
NOTE:  Don't forget to add the CleverTap imports at the top of the file.
```objc
#import <CleverTap-iOS-SDK/CleverTap.h>
#import <clevertap-react-native/CleverTapReactManager.h>
```

Note: Need to use **@import CleverTapSDK;** instead of **#import <CleverTap-iOS-SDK/CleverTap.h>** and **@import CleverTapReact;** instead of **#import <clevertap-react-native/CleverTapReactManager.h>** in the AppDelegate class in case if using ```use_modular_headers!``` in the podfile.

[See the Example Project](/Example/ios/Example/AppDelegate.mm).

### Android
1. Follow the integration instructions [here](https://developer.clevertap.com/docs/react-native-quick-start-guide#android-integration).

2. Add CleverTapPackage to the packages list in MainApplication.java (`android/app/src/[...]/MainApplication.java`)
    ```java
    // ...
    
    // CleverTap imports
    import com.clevertap.react.CleverTapPackage;
    
    //...
    
    // add CleverTapPackage to react-native package list
        @Override
        protected List<ReactPackage> getPackages() {
            List<ReactPackage> packages = new PackageList(this).getPackages();
            // Packages that cannot be autolinked yet can be added manually here, for
            // example:
            packages.add(new CleverTapPackage());// only needed when not auto-linking
            return packages;
        }
    ```

3. Initialise Clevertap ReactNative Integration - This adds support for `ClevertapPushNotiificationClicked` from killed state and registers the `ActivityLifecycleCallback`
- <a name="step3a"></a>From clevertap-react-native **v3.0.0** onwards, if developers **don't** want their `Application` class to extend `CleverTapApplication`, they should call `CleverTapRnAPI.initReactNativeIntegration(this)` and `ActivityLifecycleCallback.register(this)` from the `onCreate()` to support Push Notification click callback in killed state.

    ```java
    import com.clevertap.react.CleverTapRnAPI;
    import com.clevertap.android.sdk.ActivityLifecycleCallback;
    import com.clevertap.android.sdk.CleverTapAPI;
    import com.clevertap.android.sdk.CleverTapAPI.LogLevel;
    // ...
    
    public class MainApplication implements ReactApplication 
    {
        // ...
    
        @Override
        public void onCreate() {
            CleverTapAPI.setDebugLevel(LogLevel.VERBOSE);
            ActivityLifecycleCallback.register(this);
            CleverTapRnAPI.initReactNativeIntegration(this);
            super.onCreate();
            // ...
        }
    
    // ...
    }
    ```

<div style="text-align:center; font-size: larger; font-weight: bold;">OR</div>
<br>

- From clevertap-react-native **v3.0.0** onwards developers can make their `Application` class extend `CleverTapApplication` to support out of the box integration. Before **v3.0.0** developers were forced to register activity lifecycle in their `Application` class manually which is being abstract out in `CleverTapApplication` class.
 
    ```java
    import com.clevertap.react.CleverTapApplication;
    import com.clevertap.android.sdk.ActivityLifecycleCallback;
    import com.clevertap.android.sdk.CleverTapAPI;
    import com.clevertap.android.sdk.CleverTapAPI.LogLevel;
    // other imports
    
    public class MainApplication extends CleverTapApplication
           implements ActivityLifecycleCallbacks, ReactApplication
    {
        // ...
        @Override
        public void onCreate() {
          CleverTapAPI.setDebugLevel(LogLevel.VERBOSE);
          ActivityLifecycleCallback.register(this); // Not required for v3.0.0+
          super.onCreate();
          // ...
        }
    }
    ```
4. <a name="step4"></a> Optionally override onCreate in MainActivity.java to notify CleverTap of a launch deep link  (`android/app/src/[...]/MainActivity.java`)
    ```java
    import com.clevertap.react.CleverTapRnAPI;
    import android.os.Bundle;
   
    
    public class MainActivity extends ReactActivity {
        // ...

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            CleverTapRnAPI.setInitialUri(getIntent().getData()); // From v3.0.0+
    	}

        // ...
    }
    ```

[See the Example Project](/Example/app/App.js) 

