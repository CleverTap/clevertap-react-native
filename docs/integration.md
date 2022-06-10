## Integrating the CleverTap SDK

After [install](./install.md), you will need to integrate the CleverTap SDK into your iOS and Android apps.

### iOS
1. Follow the integration instructions [starting with Step 2 here](https://support.clevertap.com/docs/ios/getting-started.html).
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

[See the Example Project](/Example/ios/Example/AppDelegate.m).

### Android
1. Follow the integration instructions [starting with Step 2 here](https://support.clevertap.com/docs/android/getting-started.html).

2. Add CleverTapPackage to the packages list in MainApplication.java (`android/app/src/[...]/MainApplication.java`)
    ```java
    // ...

    // CleverTap imports
	import com.clevertap.android.sdk.ActivityLifecycleCallback; 
    import com.clevertap.react.CleverTapPackage; 
    import com.clevertap.android.sdk.CleverTapAPI;


    //...

    // add CleverTapPackage to react-native package list
    @Override
      protected List<ReactPackage> getPackages() {
        List<ReactPackage> packages = new PackageList(this).getPackages(); 
        // Packages that cannot be autolinked yet can be added manually here, for 
        // example: 
        packages.add(new CleverTapPackage());// only needed when not auto-linking
        return packages;

    // ...

    // add onCreate() override
    @Override
    public void onCreate() {
	    // Register the CleverTap ActivityLifecycleCallback; before calling super
        ActivityLifecycleCallback.register(this);	
        super.onCreate();
    }
    ```

3. Optionally Override onCreate in MainActivity.java to notify CleverTap of a launch deep link  (`android/app/src/[...]/MainActivity.java`)
    ```java
    import com.clevertap.react.CleverTapModule;
    import android.os.Bundle;
    
    public class MainActivity extends ReactActivity {
		// ...

		@Override
   		protected void onCreate(Bundle savedInstanceState) {
        	super.onCreate(savedInstanceState);
        	CleverTapModule.setInitialUri(getIntent().getData());
    	}

        // ...
    }
    ```
4. From clevertap-react-native **v0.8.1** onwards developers can make their `Application` class extend `CleverTapApplication` to support Push Notification click callback out of the box and to register activity lifecycle events. Before v0.8.1 developers were forced to write logic for push click callback and register activity lifecycle to their `Application` class manually which is being abstract out in `CleverTapApplication` class.
 
    ```
   import com.clevertap.react.CleverTapApplication;
   // other imports
   
   public class MainApplication extends CleverTapApplication
           implements ActivityLifecycleCallbacks, ReactApplication
   {
        // ...
   }
    ```
[see the included Example Project](/Example/App.js) 

