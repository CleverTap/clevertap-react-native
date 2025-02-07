Change Log
==========

Version 3.2.0 *(5 February 2025)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.1.2](https://github.com/CleverTap/clevertap-android-sdk/blob/develop/docs/CTCORECHANGELOG.md#version-712-january-29-2025).
  * Adds support to hide large icon in android notifications by sending `wzrk_hide_large_icon` key in notification payload.
  
* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.1.0](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-710-january-21-2024).

* **[Android and iOS Platform]**
- Adds support for triggering InApps based on first-time event filtering in multiple triggers. Now you can create campaign triggers that combine recurring and first-time events. For example: Trigger a campaign when "Charged" occurs (every time) OR "App Launched" occurs (first time only).
- Adds new user-level event log tracking system to store and manage user event history. New APIs include:
  - `getUserEventLog(<eventName>)`: Get details about a specific event
  - `getUserEventLogCount(<eventName>)`: Get count of times an event occurred
  - `getUserLastVisitTs()`: Get timestamp of user's last app visit
  - `getUserAppLaunchCount()`: Get total number of times user has launched the app
  - `getUserEventLogHistory()`: Get full event history for current user

#### API Changes

- **Deprecates:**  The old event tracking APIs tracked events at the device level rather than the user level, making it difficult to maintain accurate user-specific event histories, especially in multi-user scenarios. The following methods have been deprecated in favor of new user-specific event tracking APIs that provide more accurate, user-level analytics. These deprecated methods will be removed in future versions with prior notice:
  - `eventGetDetail()`: Use `getUserEventLog()` instead for user-specific event details
  - `eventGetOccurrences()`: Use `getUserEventLogCount()` instead for user-specific event counts
  - `eventGetFirstTime()`: Use `getUserEventLog()` instead for user-specific first occurrence timestamp
  - `eventGetLastTime()`: Use `getUserEventLog()` instead for user-specific last occurrence timestamp
  - `sessionGetPreviousVisitTime()`: Use `getUserLastVisitTs()` instead for user-specific last visit timestamp
  - `sessionGetTotalVisits()`: Use `getUserAppLaunchCount()` instead for user-specific app launch count
  - `getEventHistory()`: Use `getUserEventLogHistory()` instead for user-specific event history

Version 3.1.1 *(6 November 2024)*
-------------------------------------------
**Bug Fixes**

* **[iOS Platform]**
  * Fixes a bug where the push notification callback was getting triggered twice in killed state.

Version 3.1.0 *(25 October 2024)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.0.2](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-702-october-10-2024).
  * Adds support for custom handshake domain configuration in android manifest

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.0.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-702-october-10-2024).
  * Adds support for custom handshake domains.

* **[Android and iOS Platform]**
  * Adds support for File Type Variables in Remote Config. Please refer to the [Remote Config Variables](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/docs/Variables.md) doc to read more on how to integrate this in your app.
  * Adds support for Custom Code Templates. Please refer to the [CustomCodeTemplates.md](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CustomCodeTemplates.md) doc to read more on how to integrate this in your app.
  * Adds support for custom code in-app templates definitions through a json scheme.

**Bug Fixes**
* **[Android and iOS Platform]**
  * Fixes a missing import statement in the index.js file https://github.com/CleverTap/clevertap-react-native/issues/431
  * Fixes https://github.com/CleverTap/clevertap-react-native/issues/426

Version 3.0.0 *(8 October 2024)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v7.0.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-701-september-2-2024).
  * Removes character limit of maximum 3 lines from `AppInbox` messages.
  * Deprecates `CleverTapModule.setInitialUri()` in favour of `CleverTapRnAPI.setInitialUri()`. Refer to [step 4](docs/integration.md#step4)

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v7.0.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-701-august-22-2024).

* **[Android and iOS Platform]**
  * Adds support for triggering InApps based on user attribute changes.
  * Migrates the bridge to a backwards-compatible [New Architecture Turbo Module](docs/integration.md).
    * The CleverTap ReactNative SDK continues to be compatible with both the Old and the New Architecture.

**Breaking Changes**
* **[Android Platform]**
  * Ensure that your custom `Application` class, extends the `CleverTapApplication` or calls `CleverTapRnAPI.initReactNativeIntegration(this);` to enable the functionality of `ClevertapPushNotificationClicked` and few other callbacks linked to killed state. Refer to [step 3](docs/integration.md#step3a)

**Bug Fixes**
* **[Android Platform]**
  * Fixes an ANR caused by extremely old InApp campaigns.
  * Fixes an issue where incorrect callbacks were sent for InApps when the phone was rotated.
  * Fixes an issue where an InApp was displayed even after all the campaigns were stopped.
  * Fixes an issue where the InApp image was not shown when the phone was rotated to landscape.
  * Fixes an issue where certain URLs loaded incorrectly in custom HTML InApp templates.

Version 2.2.1 *(12 April 2024)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v6.2.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-621-april-11-2024).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v6.2.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-621-april-12-2024).

**Bug Fixes**
* **[Android Platform]**
  * Fixes a crash due to `IllegalArgumentException` caused by allowedPushType `XPS` enum.

* **[iOS Platform]**
  * Fixes a build error related to privacy manifests when statically linking the SDK using Cocoapods.


Version 2.2.0 *(5 April 2024)*
-------------------------------------------
> ⚠️ **NOTE**
2.2.0 produces a crash, please update to 2.2.1 and above. 

**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v6.2.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-620-april-3-2024).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v6.2.0](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/6.2.0).
  * Updates privacy manifests.

**API Changes**
  * Removes all Xiaomi related public methods as the Xiaomi SDK has been discontinued. Details [here](https://developer.clevertap.com/docs/discontinuation-of-xiaomi-push-service).
  * Changes the function definition of `setPushToken` to `setPushToken: function (token, type)` i.e it no more accepts `region` as a parameter.

**Bug Fixes**
* **[Android Platform]**
  * Extends the push primer callback to notify permission denial when cancel button is clicked on `PromptForSettings` alert dialog.
  * Fixes a crash due to `NullPointerException` related to `deviceInfo.deviceId`.
  * Fixes an ANR related to `isMainProcess` check.
  * Fixes an ANR due to eager initialisation of `CtApi` triggered by DeviceId generation.
  * Fixes an android build issue related to `package name not found` for apps with `ReactNative` version _0.70 or lower_.

* **[iOS Platform]**
  * Fixes a bug where client side in-apps were not discarded when rendering status is set to "discard".


Version 2.1.0 *(7 March 2024)*
-------------------------------------------
**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v6.1.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-611-january-15-2024).
  * Supports Android 14, made it compliant with Android 14 requirements. Details [here](https://developer.android.com/about/versions/14/summary)
  * Upgrades AGP to 8.2.2 for building the SDK and adds related consumer proguard rules
  * Deprecates Xiaomi public methods as we are sunsetting SDK. Details [here](https://dev.mi.com/distribute/doc/details?pId=1555).
  * Adds Accessibility ids for UI components of SDK
  * Migrates JobScheduler to WorkManager for Pull Notifications.

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v6.1.0](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/6.1.0).
  * Adds privacy manifests for CleverTap-iOS-SDK & CleverTapLocation.

**Bug Fixes**
* **[Android Platform]**
  * Fixes InApps crash in a rare activity destroyed race condition
  * Fixes Potential ANR in a race condition of SDK initialisation in multithreaded setup

* **[iOS Platform]**
  * Fixed a crash due to out of bounds in NSLocale implementation.

Version 2.0.0 *(15 February 2024)*
-------------------------------------------
**What's new**
* **[Android Platform]**
  * Supports [CleverTap Android SDK v6.0.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-600-january-15-2024).

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v6.0.0](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/6.0.0).
  
* **[Android and iOS Platform]**
  * Adds support for client-side in-apps.
  * Adds new API `clearInAppResources(boolean)` to delete images and gifs which are preloaded for inapps in cs mode
  * Adds new API `fetchInApps()` to explicitly fetch InApps from the server

**Bug Fixes**
* **[Android Platform]**
  * Fixes a bug where JavaScript was not working for custom-html InApp header/footer templates.
  * Fixes an NPE related to AppInbox APIs.
  * Fixes a ClassCastException in defineVariable API of Product Experiences.
  * Fixes a resource name conflict with the firebase library in fcm_fallback_notification_channel_label
  * Fixes a StrictMode Violation spawning from ctVariables.init().
  * Removes use of lossy conversions leading to an issue in PushTemplates.
  * Handles an edge case related to migration of encryption level when local db is out of memory

* **[iOS Platform]**
  * Fixes a bug where some in-apps were not being dismissed.

Version 1.2.1 *(25 October 2023)*
-------------------------------------------
**What's new**
* **[Android Platform]**
  * Supports [CleverTap Android SDK v5.2.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-521-october-12-2023).
  * Adds Custom Proxy Domain functionality for Push Impressions and Events raised from CleverTap Android SDK. Please refer to [Usage.md](docs/Usage.md#integrate-custom-proxy-domain) file to read more on how to configure custom proxy domains in Android.

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v5.2.1](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/5.2.1).
  * Adds support to enable `NSFileProtectionComplete` to secure App’s document directory.
  
* **[Android and iOS Platform]**
  * Adds in-built support to send the default locale(i.e.language and country) data to the dashboard and exposed public API `CleverTapPlugin.setLocale(Locale locale)` to set the custom locale, for LP Parity.
  * Adds support for Integration Debugger to view errors and events on the dashboard when the debugLevel is set to 3 using `CleverTapPlugin.setDebugLevel(3)`.
  * Adds support to configure first tab title in App Inbox.

**Changes**
* **[iOS Platform]**
  * Updated logic to retrieve country code using NSLocale above iOS 16 as `CTCarrier` is deprecated above iOS 16 with no replacements, see [Apple Doc](https://developer.apple.com/documentation/coretelephony/ctcarrier).
  * Updated logic to not send carrier name above iOS 16 in CTCarrier field.

**Bug Fixes**
* **[iOS Platform]**
  * Fixes a crash in iOS 17/Xcode 15 related to alert inapps.

Version 1.2.0 *(18th August 2023)*
-------------------------------------------

**What's new**

* **[Android Platform]**
  * Supports [CleverTap Android SDK v5.2.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-520-august-10-2023).
  * ***Note: RenderMax Push SDK functionality is now supported directly within the CleverTap Core SDK***. Please remove the [integrated RenderMax SDK](https://developer.clevertap.com/docs/react-native-push-notification#integrate-rendermax-push-sdk-with-react-native) before you upgrade to CleverTap React Native SDK for this version.
  * Adds support for developer defined default notification channel. Please refer to the [Usage.md](https://github.com/CleverTap/clevertap-react-native/blob/master/docs/usage.md#default-notification-channel) file to read more on how to setup default channel in your app. Also please note that this is only supported for CleverTap core notifications. Support for push templates will be released soon.

* **[iOS Platform]**
  * Supports [CleverTap iOS SDK v5.2.0](https://github.com/CleverTap/clevertap-ios-sdk/releases/tag/5.2.0).

* **[Android and iOS Platform]**
  * Adds support for encryption of PII data wiz. Email, Identity, Name and Phone. Please refer to [Usage.md](https://github.com/CleverTap/clevertap-react-native/blob/master/docs/usage.md#encryption-of-pii-data) file to read more on how to enable/disable encryption of PII data.
  * Adds support for custom KV pairs common to all inbox messages in App Inbox.

**API Changes**
* **[Android Platform]**
  * Adds `SCCampaignOptOut` Event to Restricted Events Name List for **internal use**.
  * Adds custom sdk versions to `af` field for **internal use**.

**Breaking API Changes**
* **[Android Platform]**
  * **CTFlushPushImpressionsWork breaks custom WorkerFactory implementation of an App**:
    * If you are using custom `WorkFactory` implementation of `WorkManager` for Android platform then make sure that you correctly handle workers defined by CleverTap SDK and other third party dependencies.
    * You must return `null` from `createWorker()` for any unknown workerClassName. Please check implementation provided in the blog [here](https://medium.com/androiddevelopers/customizing-workmanager-fundamentals-fdaa17c46dd2).

**Bug Fixes**
* **[Android Platform]**
  * Fixes [#393](https://github.com/CleverTap/clevertap-android-sdk/issues/393) - push permission flow crash when context in CoreMetadata is null.
  * Fixes [#428](https://github.com/CleverTap/clevertap-android-sdk/issues/428) - Race-condition when detecting if an in-app message should show.
  * Fixes Push primer alert dialog freeze behavior, which became unresponsive when clicked outside the window.
  * Fixes a bug where addMultiValueForKey and addMultiValuesForKey were overwriting the current values of the user properties instead of appending it.

Version 1.1.2 *(31st July 2023)*
-------------------------------------------
**New Updates**
- Supports [CleverTap iOS SDK v5.1.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-512-july-28-2023).

**Bug Fixes**
- [iOS platform]Fixed a bug where the App Inbox would appear empty.

Version 1.1.1 *(2nd May 2023)*
-------------------------------------------
- Bug fixes and performance improvements.

Version 1.1.0 *(29th May 2023)*
-------------------------------------------
**New Updates**
- Supports [CleverTap Android SDK v5.0.0](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-500-may-5-2023).
- Supports [CleverTap iOS SDK v5.0.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-501-may-17-2023).
- Adds support for **Remote Config Variables**. Please refer to the [Remote Config Variables doc](https://github.com/CleverTap/clevertap-react-native/blob/da2c3188fb5db6248f136e52f4b9e3372c26e78a/docs/Variables.md) to read more on how to integrate this to your app.
- Adds new API `dismissInbox()` to dismiss the App Inbox screen.
- Adds new APIs, `markReadInboxMessagesForIDs(Array)` and `deleteInboxMessagesForIDs(Array)` to mark read and delete an array of Inbox Messages respectively.

**API Changes**

***Deprecated:*** The following methods and classes related to Product Config and Feature Flags have been marked as deprecated in this release, instead use new Remote Config Variables feature. These methods and classes will be removed in the future versions with prior notice.
    
  - Product config
    - Methods
      - `setDefaultsMap`
      - `fetch`
      - `activate`
      - `fetchAndActivate`
      - `setMinimumFetchIntervalInSeconds`
      - `resetProductConfig`
      - `getProductConfigString`
      - `getProductConfigBoolean`
      - `getNumber`
      - `getLastFetchTimeStampInMillis`

    - Callbacks
      - `CleverTap.CleverTapProductConfigDidInitialize`
      - `CleverTap.CleverTapProductConfigDidFetch`
      - `CleverTap.CleverTapProductConfigDidActivate`

  - Feature flags
    - `getFeatureFlag`
    - `CleverTap.CleverTapFeatureFlagsDidUpdate` callback

**Breaking Change**
- Streamlines the payload for various callbacks across Android and iOS platform. Refer [doc](https://github.com/CleverTap/clevertap-react-native/blob/master/docs/callbackPayloadFormat.md) for detailed changes.

**Changes**
- ***[Android and iOS platforms]: Adds `contentPageIndex` and `buttonIndex` arguments to the payload sent via `CleverTap.CleverTapInboxMessageTapped` listener:*** The `contentPageIndex` indicates the page index of the content, which ranges from 0 to the total number of pages for carousel templates. For non-carousel templates, the value is always 0, as they only have one page of content. The `buttonIndex` represents the index of the App Inbox button clicked (0, 1, or 2). A value of -1 in `buttonIndex` indicates the App Inbox item is clicked.
- ***[Android Platform] Behavioral change of CleverTap.CleverTapInboxMessageTapped listener:*** Previously, the callback was raised when the App Inbox item is clicked. Now, it is also raised when the App Inbox button is clicked. It matches the behavior in iOS platform.

**Bug Fixes**
- Fixes a bug where App Inbox was not respecting the App Inbox background color when no tabs are provided.
- Fixes the non-EU retry mechanism bug.

Version 1.0.3 *(3rd May 2023)*
-------------------------------------------
- Fixes a bug where notification clicked callbacks were not working for killed state in iOS.

Version 1.0.2 *(3rd April 2023)*
-------------------------------------------
- Supports [CleverTap iOS SDK v4.2.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-422-april-03-2023)

Version 1.0.1 *(8th March 2023)*
-------------------------------------------
- Supports [CleverTap Android SDK v4.7.5](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-475-march-6-2023)
- Supports [CleverTap Android RenderMax SDK v1.0.3](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTRENDERMAXCHANGELOG.md#version-103-march-6-2023)
- Supports [CleverTap Android Push Templates SDK v1.0.8](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTPUSHTEMPLATESCHANGELOG.md#version-108-march-8-2023)
- Make sure you update all three above versions for compatibility and smooth working.

Version 1.0.0 *(20 January 2023)*
-------------------------------------------
- Adds below new public APIs to support [CleverTap Android SDK v4.7.2](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-472-december-16-2022) and [CleverTap iOS SDK v4.2.0](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-420-december-13-2022)
  - `isPushPermissionGranted()`, `promptPushPrimer(object)`, `promptForPushPermission(boolean)` 
- Adds push permission callback method which returns true/false after user allow/deny the notification permission.
- Refer [Push Primer doc](./docs/pushprimer.md) for more details.

Version 0.9.7 *(3rd May 2023)*
-------------------------------------------
- Fixes a bug where notification clicked callbacks were not working for killed state in iOS. 
- [Android] Make sure the maximum deployment version is Android 12.

Version 0.9.6 *(3 April 2023)*
-------------------------------------------
- Supports [CleverTap Android SDK v4.6.9](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-468-march-22-2023)
- Supports [CleverTap iOS SDK v4.2.2](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-422-april-03-2023)
- **[Breaking Change]**: Renames the `itemIndex` field with the `contentPageIndex` field in the payload of the `CleverTap.CleverTapInboxMessageTapped` callback.
- **[Parity with iOS platform]**:
  The `CleverTap.CleverTapInboxMessageTapped` callback now provides a different value for `contentPageIndex`(ex-`itemIndex`) compared to before. Previously, it used to indicate the position of the clicked item within the list container of the App Inbox. However, now it indicates the page index of the content, which ranges from 0 to the total number of pages for carousel templates. For non-carousel templates, the value is always 0, as they only have one page of content.
- **[Type Definitions support in typescript]**: Supports type definitions for the event names that are available for Javascript.

Version 0.9.5 *(27 March 2023)*
-------------------------------------------
- Supports [CleverTap Android SDK v4.6.8](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTCORECHANGELOG.md#version-468-march-22-2023)
- Supports [CleverTap iOS SDK v4.2.1](https://github.com/CleverTap/clevertap-ios-sdk/blob/master/CHANGELOG.md#version-421-march-22-2023)
- Supports [CleverTap Android RenderMax SDK v1.0.3](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTRENDERMAXCHANGELOG.md#version-103-march-6-2023)
- Supports [CleverTap Push Templates SDK v1.0.5.1](https://github.com/CleverTap/clevertap-android-sdk/blob/master/docs/CTPUSHTEMPLATESCHANGELOG.md#version-1051-march-15-2023).
- Make sure you update all four above versions for compatibility and smooth working.
- **Note:** This release is being done for Android 12 targeted users.
- **[Android and iOS platforms]**:
Adds `itemIndex` and `buttonIndex` arguments to the payload sent via App Inbox Message tapped listener: `CleverTap.CleverTapInboxMessageTapped`. The `itemIndex` corresponds the index of the item clicked in the list whereas the `buttonIndex` for the App Inbox button clicked (0, 1, or 2). A value of -1 in `buttonIndex` indicates the App Inbox item is clicked.
- **[Android Platform] Behavioral change of `CleverTap.CleverTapInboxMessageTapped` listener**:
Previously, the callback was raised when the App Inbox item is clicked. Now, it is also raised when the App Inbox button is clicked. It matches the behavior in iOS platform. 

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
