## Installing CleverTap React Native

1. `npm install --save clevertap-react-native`

     Link CleverTap for React Native 0.59 or below & Not Using Cocoapods

2. `react-native link clevertap-react-native` **or** [follow the manual linking instructions below](install.md#manual-linking).

    **Note:**
    
    1. For React Native 0.60 or above linking is not required. Read more [here](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md).
    
    2. To disable auto-linking in Android, Add a react-native.config.js to the project root (where the package.json is) to exempt CleverTap package from auto linking:
    
    ```
    module.exports = {
	dependencies: {
   	 	'clevertap-react-native': {
   	  	 platforms: {
     	  	 ios: null,
      	  	 android: null,
     	       },
   	     },
  	   },
	};
    ```
    
  
  ## Steps for iOS ##
  

### CocoaPods source (required to keep receiving updates)

CleverTap's iOS SDK is delivered through CocoaPods. CocoaPods trunk becomes read-only on December 2, 2026. After that date new CleverTap versions are published to CleverTap's own CocoaPods specs repo instead of trunk. Add both `source` lines to the top of your app's `ios/Podfile` so CocoaPods can keep resolving CleverTap:

```ruby
source 'https://github.com/CleverTap/podspecs.git'
source 'https://cdn.cocoapods.org/'
```

Keep the `cdn.cocoapods.org` line. It is still needed so other pods (for example SDWebImage) resolve. Both lines are required: once a Podfile declares any `source`, CocoaPods stops using the default CDN automatically. Existing installs keep working with no change. After editing the Podfile, run `pod install` from your `ios` directory.

### iOS with podspec
- Add `pod 'clevertap-react-native', :path => '../node_modules/clevertap-react-native'` as a dependency in your ios/Podfile.

For your iOS, add the following to your Podfile:

```
target 'YOUR_TARGET_NAME' do  
    use_frameworks!
    pod 'clevertap-react-native', :path => '../node_modules/clevertap-react-native'
end
```

- Run `pod install` from your ios directory.

### iOS without podspec
- Add `pod 'CleverTap-iOS-SDK'` as a dependency in your ios/Podfile.
- `cd ios; pod install --repo-update`
- Note that after pod install, open your project using **[MyProject].xcworkspace** instead of the original .xcodeproj.

### Troubleshooting  

If you're on RN 0.60 or your project configuration doesn't allow to add `use_frameworks!` in the podfile, alternatively, you can add `use_modular_headers!` to enable the stricter search paths and module map generation for all of your pods, or you can add `:modular_headers => true` to a single pod declaration to enable for only that pod.

### iOS with Swift Package Manager (experimental)

Requires React Native 0.87+ and iOS 15+. The package ships a `Package.swift`, so React Native's SPM autolinking picks it up. CocoaPods keeps working as before.

1. `npm install --save clevertap-react-native`
2. Run `npx react-native spm add --deintegrate` to move a CocoaPods app to SPM, or `npx react-native spm` for a new one.
3. On React Native CLI 20.x (shipped with RN 0.87 and 0.88), keep an `ios/Podfile`, even with an empty target. Without it the "Sync SPM Autolinking" phase fails with `CLI config did not provide project.ios.sourceDir`.
4. After `--deintegrate`, delete any leftover React Native lines from the Podfile.
5. Open `YourApp.xcodeproj`, not the `.xcworkspace`.

To stop `run-ios` from running `pod install`, add to `react-native.config.js`:
```js
module.exports = { project: { ios: { automaticPodsInstallation: false } } };
```

In a Swift `AppDelegate`:
```swift
import CleverTapSDK
import CleverTapReact

// in application(_:didFinishLaunchingWithOptions:)
CleverTap.autoIntegrate()
CleverTapReactManager.sharedInstance().applicationDidLaunch(options: launchOptions)
```

In an Objective-C `AppDelegate.mm`, use `#import`. `@import CleverTapReact;` does not work in `.mm` files.
```objc
#import <CleverTapSDK/CleverTap.h>
#import "CleverTapReactManager.h"

// in application:didFinishLaunchingWithOptions:
[CleverTap autoIntegrate];
[[CleverTapReactManager sharedInstance] applicationDidLaunchWithOptions:launchOptions];
```

For push extensions, see [iOS push templates with Swift Package Manager](iospushtemplates.md#swift-package-manager).

If the build fails with `does not contain a binary artifact`, run `npx react-native spm update`.


## Steps for Android ##


- Add the clevertap-android-sdk and firebase-messaging (if you wish to support push notifications) packages in your `android/app/build.gradle`file.
```gradle
dependencies {
	...
	//androidx
    implementation 'androidx.core:core:1.3.0'
    implementation 'androidx.appcompat:appcompat:1.3.1'       //Mandatory for App Inbox
    implementation 'androidx.fragment:fragment:1.3.6'         //Mandatory for InApp
    implementation 'androidx.recyclerview:recyclerview:1.2.1' //Mandatory for App Inbox
    implementation 'androidx.viewpager:viewpager:1.0.0'       //Mandatory for App Inbox
    implementation "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"

    //com.android
    implementation 'com.android.installreferrer:installreferrer:2.2' //Mandatory for React Native SDK v0.3.9+
    implementation 'com.android.support:multidex:1.0.3'

    //react-native  libs
    implementation "com.facebook.react:react-native:+"

    //com.google
    implementation 'com.google.android.gms:play-services-base:17.6.0'
    implementation 'com.google.firebase:firebase-messaging:21.0.0'
    implementation 'com.google.android.material:material:1.4.0'          //Mandatory for App Inbox
    implementation 'com.google.android.exoplayer:exoplayer:2.19.1'      //Optional for Audio/Video
    implementation 'com.google.android.exoplayer:exoplayer-hls:2.19.1'  //Optional for Audio/Video
    implementation 'com.google.android.exoplayer:exoplayer-ui:2.19.1'   //Optional for Audio/Video
    //implementation 'com.google.android.gms:play-services-ads:19.0.1'


    //clevertap
    implementation 'com.clevertap.android:clevertap-android-sdk:8.6.0'


    // other libs
	implementation 'com.github.bumptech.glide:glide:4.12.0' //Mandatory for App Inbox
	
    //Note - ExoPlayer/AndroidX Media3 dependencies are optional but all 3 are required for Audio/Video Inbox and InApp Messages
}
```

### Migrating from `Exoplayer` to `AndroidX Media3` (Optional)

Clevertap ReactNative SDK supports `AndroidX Media3` from `v3.0.0+` to replace the deprecated `ExoPlayer` libraries. For migration change the following dependencies.

|         Old Dependency | New Dependency      |
|-----------------------:|:--------------------|
|     `com.google.android.exoplayer:exoplayer:2.19.1` | `androidx.media3:media3-exoplayer:1.1.1`     |
| `com.google.android.exoplayer:exoplayer-hls:2.19.1` | `androidx.media3:media3-exoplayer-hls:1.1.1` |
|  `com.google.android.exoplayer:exoplayer-ui:2.19.1` | `androidx.media3:media3-ui:1.1.1`  |


## Manual Linking

#### iOS:
- Drag and Drop node_modules/clevertap-react-native/ios/CleverTapReact.xcodeproj into the Libraries folder of your project in XCode ([see Step 1 here](http://facebook.github.io/react-native/docs/linking-libraries-ios.html#manual-linking)).

- Drag and Drop the libCleverTapReact.a product in CleverTapReact.xcodeproj into your project's target's "Link Binary With Libraries" section ([see Step 2 here](http://facebook.github.io/react-native/docs/linking-libraries-ios.html#manual-linking)).

- Add a Header Search Path pointing to `$(SRCROOT)/../node_modules/clevertap-react-native/ios` ([see Step 3 here](http://facebook.github.io/react-native/docs/linking-libraries-ios.html#manual-linking)).

#### Android:
android/settings.gradle

```gradle
include ':clevertap-react-native'
project(':clevertap-react-native').projectDir = new File(settingsDir, '../node_modules/clevertap-react-native/android')
```
android/app/build.gradle
```gradle
dependencies {
    ...
    implementation project(':clevertap-react-native')
}
```

### Troubleshooting
#### Upgrading to clevertap-react-native v3.3.0

To use `clevertap-react-native v3.3.0`, it is recommended to upgrade to **React Native v0.74+**.

If you insist on using an older version of React Native, consider the following possible resolutions for common issues:

##### For ReactNative v0.73
**Error:** `Compiled with an incompatible version of Kotlin`

**Resolution:** Force the Kotlin version in your root `build.gradle`:
```gradle
dependencies {
  ....
  classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0"

  // NOTE: Do not place your application dependencies here; they belong
  // in the individual module build.gradle files
}
```

##### For ReactNative v0.71 or v0.72
**Error:** `java.lang.NullPointerException: Cannot invoke "String.length()" because "<parameter1>" is null`

**Reference:** https://issuetracker.google.com/issues/342522142

**Resolution:** Either upgrade to `AGP 8.0.0+` or use a specific version of D8/R8 compiler as recommended in [this comment](https://issuetracker.google.com/issues/342522142#comment8) 

Now move on to [integrating the SDK](./integration.md).
