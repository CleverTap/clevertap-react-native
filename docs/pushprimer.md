##  🔖 Overview

Push Primer allows you to enable runtime push permission for sending notifications from an app.

Starting with the v0.9.5 release, CleverTap React Native supports Push primer for push notification runtime permission through local in-app.

For Push Primer, minimum supported version for iOS platform is 10.0 while android 13 for the android platform.

### Push Primer using Half-Interstitial local In-app
```javascript
let localInApp = {
              inAppType: 'half-interstitial',
              titleText: 'Get Notified',
              messageText:
                'Please enable notifications on your device to use Push Notifications.',
              followDeviceOrientation: true,
              positiveBtnText: 'Allow',
              negativeBtnText: 'Cancel',
              backgroundColor: '#FFFFFF',
              btnBorderColor: '#0000FF',
              titleTextColor: '#0000FF',
              messageTextColor: '#000000',
              btnTextColor: '#FFFFFF',
              btnBackgroundColor: '#0000FF',
              btnBorderRadius: '2',
              fallbackToSettings: true,
            };

CleverTap.promptPushPrimer(localInApp);
```

### Push Primer using Alert local In-app
```javascript
CleverTap.promptPushPrimer({
              inAppType: 'alert',
              titleText: 'Get Notified',
              messageText: 'Enable Notification permission',
              followDeviceOrientation: true,
              positiveBtnText: 'Allow',
              negativeBtnText: 'Cancel',
              fallbackToSettings: true,
            });
```

### Prompt the Notification Permission Dialog (without push primer)
It takes boolean as a parameter. If the value passed is true and permission is denied then we fallback to app’s notification settings. If false then we just give the callback saying permission is denied.

```javascript
CleverTap.promptForPushPermission(true);    
```

### Get the Push notification permission status
Returns the status of the push permission in the callback handler.

```javascript
CleverTap.isPushPermissionGranted((err, res) => {
  console.log('isPushPermissionGranted', res, err);
});
```

###  Description of the localInApp Object passed inside the PromptPushPrimer(localInApp) method

Key Name| Parameters | Description | Required
:---:|:---:|:---:|:---
`inAppType` | "half-interstitial" or "alert" | Accepts only half-interstitial & alert type to display the local in-app | Required
`titleText` | String | Sets the title of the local in-app | Required
`messageText` | String | Sets the subtitle of the local in-app | Required
`followDeviceOrientation` | true or false | If true then the local InApp is shown for both portrait and landscape. If it sets false then local InApp only displays for portrait mode | Required
`positiveBtnText` | String | Sets the text of the positive button | Required
`negativeBtnText` | String | Sets the text of the negative button | Required
`fallbackToSettings` | true or false | If true and the permission is denied then we fallback to app’s notification settings, if it’s false then we just give the callback saying permission is denied. | Optional
`backgroundColor` | Accepts Hex color as String | Sets the background color of the local in-app | Optional
`btnBorderColor` | Accepts Hex color as String | Sets the border color of both positive/negative buttons | Optional
`titleTextColor` | Accepts Hex color as String | Sets the title color of the local in-app | Optional
`messageTextColor` | Accepts Hex color as String | Sets the sub-title color of the local in-app | Optional
`btnTextColor` | Accepts Hex color as String | Sets the color of text for both positive/negative buttons | Optional
`btnBackgroundColor` | Accepts Hex color as String | Sets the background color for both positive/negative buttons | Optional
`btnBorderRadius` | String | Sets the radius for both positive/negative buttons. Default radius is “2” if not set | Optional
`fallbackToSettings` | true or false | If the value passed is true then we fallback to app’s notification settings in case permission is denied. If false then we just give the callback saying permission is denied. | Optional


###  Available Callbacks for Push Primer
Based on notification permission grant/deny, CleverTap React Native SDK provides a callback with the permission status.
For this You can register the CleverTapPushPermissionResponseReceived callback:
```javascript
CleverTap.addListener(CleverTap.CleverTapPushPermissionResponseReceived, (e)=>{/*consume the event*/})
```

To unregister the callback use below:
```javascript
    CleverTap.removeListener(CleverTap.CleverTapPushPermissionResponseReceived);
```





