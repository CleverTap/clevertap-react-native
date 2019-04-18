## Installing CleverTap React Native

1. `npm install --save clevertap-react-native`
2. `react-native link clevertap-react-native` **or** [follow the manual linking instructions below](#manual-linking).

### iOS with podspec
- Add `pod 'clevertap-react-native', :path => '../node_modules/clevertap-react-native'` to your app target in your Podfile. 
- Run pod install from your ios directory.

### iOS without podspec
- Add `pod 'CleverTap-iOS-SDK'` as a dependency in your ios/Podfile.  [See an example Podfile here](https://github.com/CleverTap/clevertap-react-native/blob/master/ExampleProject/ios/Podfile).
- `cd ios; pod install --repo-update`
- Note that after pod install, open your project using **[MyProject].xcworkspace** instead of the original .xcodeproj.

### Android
- Add the clevertap-android-sdk and firebase-messaging (if you wish to support push notifications) packages in your `android/app/build.gradle`file.
```gradle
dependencies {
	...
    compile 'com.clevertap.android:clevertap-android-sdk:3.4.2'
    compile 'com.google.android.gms:play-services-base:16.0.1'
    compile 'com.google.firebase:firebase-messaging:17.3.3'
    compile 'com.google.android.exoplayer:exoplayer:2.8.4' //Optional for Audio/Video
    compile 'com.google.android.exoplayer:exoplayer-hls:2.8.4' //Optional for Audio/Video
    compile 'com.google.android.exoplayer:exoplayer-ui:2.8.4' //Optional for Audio/Video
    compile 'com.github.bumptech.glide:glide:4.8.0' //Mandatory for App Inbox
    compile 'com.android.support:design:28.0.0' //Mandatory for App Inbox
    compile "com.android.support:appcompat-v7:28.0.0" //Mandatory for App Inbox

    //Note - ExoPlayer dependencies are optional but all 3 are required for Audio/Video Inbox and InApp Messages
}
```

### Manual Linking

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
    compile project(':clevertap-react-native')
}
```

Now move on to [integrating the SDK](./integration.md).
