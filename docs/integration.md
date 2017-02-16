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
#import <CleverTapSDK/CleverTap.h>
#import <CleverTapReact/CleverTapReactManager.h>
```
[See the Example Project](https://github.com/CleverTap/clevertap-react-native/blob/master/ExampleProject/ios/ExampleProject/AppDelegate.m).

### Android
1. Follow the integration instructions [starting with Step 2 here](https://support.clevertap.com/docs/android/getting-started.html).

2. Add CleverTapPackage to the packages list in MainApplication.java (`android/app/src/[...]/MainApplication.java`)
    ```java
    // ...

    // CleverTap imports
	import com.clevertap.android.sdk.ActivityLifecycleCallback;
	import com.clevertap.react.CleverTapPackage;

    //...

    // add CleverTapPackage to react-native package list
    @Override
      protected List<ReactPackage> getPackages() {
        return Arrays.<ReactPackage>asList(
                new MainReactPackage(),
                new CleverTapPackage(), // <-- add this

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
[See the Example Project](https://github.com/CleverTap/clevertap-react-native/tree/master/ExampleProject/android/app/src/main).

