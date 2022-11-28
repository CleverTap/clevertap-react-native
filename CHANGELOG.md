Change Log
==========

Version 0.9.4 *(28 November 2022)*
-------------------------------------------
- Supports [CleverTap iOS SDK v4.1.5](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-415-november-15-2022)
- Supports `setInstanceWithAccountId` method
- Supports using specific CleverTap instance


Version 0.9.3 *(1 November 2022)*
-------------------------------------------
- Supports CleverTap Android Core SDK [v4.6.6](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md) and associated enhancements


Version 0.9.2 *(7 October 2022)*
-------------------------------------------
- Supports [CleverTap iOS SDK v4.1.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-412-september-16-2022) and associated enhancements
- Supports App Inbox Message tapped listener: `CleverTap.CleverTapInboxMessageTapped`

Version 0.9.1 *(21 September 2022)*
-------------------------------------------
- Supports CleverTap Android Core SDK [v4.6.3](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md) and associated enhancements
- Supports CleverTap Android Push Templates SDK [v1.0.5](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTPUSHTEMPLATESCHANGELOG.md) and associated enhancements
- Supports CleverTap Android HMS SDK [v1.3.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTHUAWEIPUSHCHANGELOG.md) and associated enhancements
- Supports CleverTap Android XPS SDK [v1.5.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTXIAOMIPUSHCHANGELOG.md) and associated Android 12, Region Changes and other enhancements

Version 0.9.0 *(23rd August 2022)*
-------------------------------------------
- Supports [CleverTap Android SDK v4.6.0](https://github.com/CleverTap/clevertap-android-sdk/releases/tag/corev4.6.0_ptv1.0.4)

Version 0.8.1 *(7th March 2022)*
-------------------------------------------
- Supports CleverTap iOS SDK `v4.0.0`
- Abstract out notification click callback logic for killed state in Android.

Version 0.8.0 *(17th January 2022)*
-------------------------------------------
- Supports [CleverTap Android SDK v4.4.0](https://github.com/CleverTap/clevertap-android-sdk/releases/tag/core-v4.4.0)

Version 0.7.0 *(30th November 2021)*
-------------------------------------------
- Supports CleverTap Android SDK `v4.3.1` backing Android 12

Version 0.6.0 *(3rd September 2021)*
-------------------------------------------
- Adds public methods for suspending/discarding & resuming InApp Notifications
- Adds public methods to increment/decrement values set via User properties
- Deprecates `profileGetCleverTapID()` and `profileGetCleverTapAttributionIdentifier()`
- Adds a new public method `getCleverTapID()` as an alternative to above deprecated methods
- Supports CleverTap iOS SDK `v3.10.0`

Version 0.5.2 *(20th July 2021)*
-------------------------------------------
- Supports CleverTap Android SDK `v4.2.0`
- Android SDK `v4.2.0` fixes NPE for `recordScreenView()` in Android

Version 0.5.1 *(5th May 2021)*
-------------------------------------------
- Update and Freeze [CleverTap React Native Podspec](/clevertap-react-native.podspec) to a specific version of a CleverTap iOS SDK
- Supports CleverTap iOS SDK v3.9.3
- Supports CleverTap Android SDK v4.1.1
- Removes Product Experiences (Dynamic Variables) related code
- Removed `pushGooglePlusPerson` and `profileSetGraphUser` API

Version 0.5.0 *(15th October 2020)*
-------------------------------------------
- Supports [CleverTap iOS SDK v3.9.1](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/3.9.1)
- Supports **Major release** of [CleverTap Android SDK v4.0.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md), which will break your existing integration. Please go through [Migration guide](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTV4CHANGES.md) for smooth integration. 
- Adds `removeListener` method to remove single listener at a time.
- Deprecated `removeListeners()` method because it was removing all listeners including listeners defined in an Application [issue/104](https://github.com/CleverTap/clevertap-react-native/issues/104).
- Allow choosing text(with colour) when no messages to display in App Inbox

Version 0.4.5 *(19th August 2020)*
-------------------------------------------
- Adds a callback to provide Push Notifications custom key-value pairs
- Supports CleverTap [Android](https://github.com/CleverTap/clevertap-android-sdk/releases/tag/3.8.2) and [iOS](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/3.8.2) SDK v3.8.2

Version 0.4.4 *(12th June 2020)*
-------------------------------------------
- Use v0.4.5
- Performance improvements and fixes

Version 0.4.3 *(20 May 2020)*
-------------------------------------------
- Use v0.4.5
- Added support for Product Config and Feature Flags methods
- Added fix for TypeError: EventEmitter.removeListeners is not a function

Version 0.4.2 *(15 April 2020)*
-------------------------------------------
- Use v0.4.5
- Added support for Xiaomi/Baidu Push methods

Version 0.4.1 *(30 March 2020)*
-------------------------------------------
- Use v0.4.5
- Update to CleverTap Android SDK v3.7.2

Version 0.4.0 *(17 March 2020)*
-------------------------------------------
- Use v0.4.5
- Adds support for Custom App Inbox & Native Display
- Supports CleverTap Android SDK v3.7.0
- Supports CleverTap iOS SDK v3.7.3

Version 0.3.9 *(26 February 2020)*
-------------------------------------------
- Update to CleverTap Android SDK v3.6.4

Version 0.3.8 *(15 January 2020)*
-------------------------------------------
- Update to CleverTap Android SDK v3.6.3

Version 0.3.7 *(28 December 2019)*
-------------------------------------------
- Added method to custom handle Push Notifications in Android

Version 0.3.6 *(12 December 2019)*
-------------------------------------------
- Update to CleverTap iOS SDK v 3.7.2

Version 0.3.5 *(3 October 2019)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.6.0
- Update to CleverTap iOS SDK v 3.7.0

Version 0.3.4 *(10 June 2019)*
-------------------------------------------
- Added fixes for Typescript

Version 0.3.3 *(29 May 2019)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.5.1
- Update to CleverTap iOS SDK v 3.5.0

Version 0.3.2 *(24 April 2019)*
-------------------------------------------
- Add support for Typescript

Version 0.3.1 *(18 April 2019)*
-------------------------------------------
- Update to CleverTap iOS SDK v 3.4.2
- Added the local clevertap-react-native Podspec for integrating the React Native iOS bridge via Cocoapods.

Version 0.3.0 *(14 February 2019)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.4.2
- Update to CleverTap iOS SDK v 3.4.1

Version 0.2.6 *(13 November 2018)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.3.2

Version 0.2.5 *(31 October 2018)*
-------------------------------------------
- Update to CleverTap iOS SDK v 3.3.0
- Update to CleverTap Android SDK v 3.3.1

Version 0.2.4 *(26 September 2018)*
-------------------------------------------
- Update to CleverTap iOS SDK v 3.2.2

Version 0.2.3 *(11 September 2018)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.2.0
- Update to CleverTap iOS SDK v 3.2.0

Version 0.2.2 *(22 July 2018)*
-------------------------------------------
- Fix Node 10 install.js issue

Version 0.2.1 *(21 May 2018)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.1.10

Version 0.2.0 *(15 May 2018)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.1.9
- Update to CleverTap iOS SDK v 3.1.7
- Support for Android O Notification Channels with custom sound
- New APIs for GDPR compliance
- Adds Android support for recordScreenView API

Version 0.1.9 *(05 January 2018)*
-------------------------------------------
- Update to CleverTap Android SDK v 3.1.8
- Support for Android O Notification Channels


Version 0.1.8 *(26 October 2017)*
-------------------------------------------
- fix react-native dependency 

Version 0.1.7 *(19 October 2017)*
-------------------------------------------
- fix for breaking change in RN v0.47 Android
- update iOS for RN v0.49

Version 0.1.6 *(13 October 2017)*
-------------------------------------------
Update to CleverTap iOS SDK v 3.1.6

Version 0.1.5 *(10 October 2017)*
-------------------------------------------
Update to CleverTapAndroidSDK v 3.1.7

Version 0.1.4 *(21 September 2017)*
-------------------------------------------
*(Supports CleverTap 3.1.5/3.1.6 and React Native v0.41.2)*

Version 0.1.3 *(30 June 2017)*
-------------------------------------------
*(Supports CleverTap 3.1.4 and React Native v0.41.2)*

Adds recordScreenView api for iOS

Version 0.1.2 *(16 February, 2017)*
-------------------------------------------
*(Supports CleverTap 3.1.2 and React Native v0.41.2)*

Add CleverTapSDK framework as zip + postinstall unzip, as npm doesn't like iOS framework symlinks.

Version 0.1.1 *(15 February, 2017)*
-------------------------------------------
*(Supports CleverTap 3.1.2 and React Native v0.41.2)*

Add missing header search path.

Version 0.1.0 *(15 February, 2017)*
-------------------------------------------
*(Supports CleverTap 3.1.2 and React Native v0.41.2)*

Initial release.
