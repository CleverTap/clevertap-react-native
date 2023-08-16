
## Example Usage

#### Grab a reference  
```javascript 
const CleverTap = require('clevertap-react-native');
```

## User Profiles

#### Update User Profile(Push Profile)

```javascript 
CleverTap.profileSet({"Identity":11102008, "Name":"React-Test Profile","Email":"r@gmail.com","Gender":"Male","DOB":"1995-10-14", "custom":1.73});
```

#### Set Multi Values For Key 

```javascript 
CleverTap.profileSetMultiValuesForKey(['a', 'b', 'c'], 'letters');
```

#### Remove Multi Value For Key 

```javascript 
CleverTap.profileRemoveMultiValueForKey('b', 'letters');
```

#### Add Multi Value For Key

```javascript 
CleverTap.profileAddMultiValueForKey('d', 'letters');
```

#### Create a User profile when user logs in (On User Login)

```javascript 
CleverTap.onUserLogin({'Name': 'React-Test', 'Identity': '11102008', 'Email': 'r@gmail.com', 'custom1': 43});
```

#### Get CleverTap Reference id

```javascript
CleverTap.profileGetCleverTapID((err, res) => {
         console.log('CleverTapID', res, err);
});
```

#### Set Location to User Profile

```javascript 
CleverTap.setLocation(34.15, -118.20);
```

#### Increment a User Profile property

```javascript 
CleverTap.profileIncrementValueForKey(1, "score");
```

#### Decrement a User Profile property

```javascript 
CleverTap.profileDecrementValueForKey(1, "score");
```

-----------

## User Events

#### Record an event  

```javascript 
CleverTap.recordEvent('testEvent');
```

#### Record an event with event Properties  

```javascript 
CleverTap.recordEvent('Product Viewed', {'Product Name': 'Dairy Milk','Category': 'Chocolate','Amount': 20.00});
```

#### Record Charged event

```javascript 
CleverTap.recordChargedEvent({'totalValue': 20, 'category': 'books'}, [{'title': 'book1'}, {'title': 'book2'}, {'title': 'book3'}]);
```

-----------

## Encryption of PII data
PII data is stored across the SDK and could be sensitive information. From CleverTap SDK v5.2.0 onwards, you can enable encryption for PII data wiz. Email, Identity, Name and Phone.

Currently 2 levels of encryption are supported i.e None(0) and Medium(1). Encryption level is None by default.
**None** - All stored data is in plaintext
**Medium** - PII data is encrypted completely.

The only way to set the encryption level for the default instance is through the `AndroidManifest.xml`.
For the same, add the following to the `AndroidManifest.xml` file:
```XML
<meta-data
    android:name="CLEVERTAP_ENCRYPTION_LEVEL"
    android:value="1" />
```

Different instances can have different encryption levels. To set an encryption level for an additional instance:

```kotlin
val clevertapAdditionalInstanceConfig = CleverTapInstanceConfig.createInstance(
    applicationContext,
    "ADDITIONAL_CLEVERTAP_ACCOUNT_ID",
    "ADDITIONAL_CLEVERTAP_ACCOUNT_TOKEN"
)

clevertapAdditionalInstanceConfig.setEncryptionLevel(CryptHandler.EncryptionLevel.MEDIUM)
val clevertapAdditionalInstance = CleverTapAPI.instanceWithConfig(applicationContext ,clevertapAdditionalInstanceConfig)
```

-----------

## App Inbox

#### Initialize the CleverTap App Inbox Method

```javascript 
CleverTap.initializeInbox();
```

#### Show the App Inbox

```javascript
CleverTap.showInbox({'tabs':['Offers','Promotions'],'navBarTitle':'My App Inbox','navBarTitleColor':'#FF0000','navBarColor':'#FFFFFF','inboxBackgroundColor':'#AED6F1','backButtonColor':'#00FF00'
                                ,'unselectedTabColor':'#0000FF','selectedTabColor':'#FF0000','selectedTabIndicatorColor':'#000000',
                                'noMessageText':'No message(s)','noMessageTextColor':'#FF0000'});
 ```

#### App Inbox Item Click Callback

```javascript 
CleverTap.addListener(CleverTap.CleverTapInboxMessageTapped, (event) => {
    console.log("App Inbox item: ", event.data);
    //following contentPageIndex and buttonIndex fields are available from CleverTap React Native SDK v0.9.6 onwards and below v1.0.0 
    console.log("Content Page index: " + event.contentPageIndex);
    console.log("Button index: " + event.buttonIndex);
});
```

#### App Inbox Button Click Callback
```javascript
CleverTap.addListener(CleverTap.CleverTapInboxMessageButtonTapped, (event) => {/*consume the payload*/});
```

#### Dismiss the App Inbox
```javascript
CleverTap.dismissInbox();
```

### Mark read all inbox messages by array of messageIds
```javascript
CleverTap.markReadInboxMessagesForIDs(['1', '2', '3']);	
```

### Delete all inbox messages by array of messageIds
```javascript
CleverTap.deleteInboxMessagesForIDs(['1', '2', '3']);	
```

#### Get Total message count

```javascript 
CleverTap.getInboxMessageCount((err, res) => {
	console.log('Total Messages: ', res, err);
});	
```

#### Get Total message count

```javascript 
CleverTap.getInboxMessageUnreadCount((err, res) => {
	console.log('Unread Messages: ', res, err);
});	
```

#### Get All Inbox Messages

```javascript 
CleverTap.getAllInboxMessages((err, res) => {
	console.log('All Inbox Messages: ', res, err);
});	
```

#### Get all Inbox unread messages

```javascript 
CleverTap.getUnreadInboxMessages((err, res) => {
	console.log('Unread Inbox Messages: ', res, err);
});	
```

#### Get inbox Id

```javascript 
CleverTap.getInboxMessageForId('Message Id',(err, res) => {
        console.log("marking message read = "+res);
});			
```

#### Delete message with id

```javascript 
CleverTap.deleteInboxMessageForId('Message Id');		
```

#### Mark a message as Read for inbox Id

```javascript 
CleverTap.markReadInboxMessageForId('Message Id');		
```

#### pushInbox Notification Viewed Event For Id

```javascript 
CleverTap.pushInboxNotificationViewedEventForId('Message Id');		
```

#### push Inbox Notification Clicked Event For Id

```javascript 
CleverTap.pushInboxNotificationClickedEventForId('Message Id');			
```

-----------

## Push primer for notification Permission (Android and iOS)
Follow the [Push Primer integration doc](pushprimer.md).

-----------
## Push Notifications

#### Creating Notification Channel

```javascript 
CleverTap.createNotificationChannel("CtRNS", "Clever Tap React Native Testing", "CT React Native Testing", 1, true);			
```

#### Default Notification Channel
Starting from CleverTap React Native SDK v1.2.0, we have introduced a new feature that allows developers to define a default notification channel for their app. This feature provides flexibility in handling push notifications. Please note that this is only supported for clevertap core notifications. Support for push templates will be released soon. To specify the default notification channel ID, you can add the following metadata in your app's manifest file:

```XML
<meta-data android:name="CLEVERTAP_DEFAULT_CHANNEL_ID" android:value="your_default_channel_id" />
```

By including this metadata, you can define a specific notification channel that CleverTap will use if the channel provided in push payload is not registered by your app. This ensures that push notifications are displayed consistently even if the app's notification channels are not set up.

In case the SDK does not find the default channel ID specified in the manifest, it will automatically fallback to using a default channel called "Miscellaneous". This ensures that push notifications are still delivered, even if no specific default channel is specified in the manifest.

This enhancement provides developers with greater control over the default notification channel used by CleverTap for push notifications, ensuring a seamless and customizable user experience.

#### Delete Notification Channel

```javascript 
CleverTap.deleteNotificationChannel("RNTesting");		
```

#### Creating a group notification channel

```javascript 
CleverTap.createNotificationChannelGroup(String groupId, String groupName);		
```

#### Delete a group notification channel

```javascript 
CleverTap.deleteNotificationChannelGroup(String groupId);			
```

#### Registering Fcm Token

```javascript 
CleverTap.setPushToken("<Replace with FCM Token value>", CleverTap.FCM);
```

-----------
 
## Native Display

#### Get Display Unit for Id

```javascript 
CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
        console.log('Get Display Unit for Id:', res, err);
});
```

#### Get All Display Units

```javascript 
CleverTap.getAllDisplayUnits((err, res) => {
        console.log('All Display Units: ', res, err);
});
```

-----------

## Product Config 

#### Set Product Configuration to default

```javascript 
CleverTap.setDefaultsMap({'text_color': 'red', 'msg_count': 100, 'price': 100.50, 'is_shown': true, 'json': '{"key":"val"}'});
```

#### Fetching product configs

```javascript 
CleverTap.fetch();
```

#### Activate the most recently fetched product config

```javascript 
CleverTap.activate();
```

#### Fetch And Activate product config

```javascript 
CleverTap.fetchAndActivate();
```

#### Fetch Minimum Time Interval

```javascript 
CleverTap.fetchWithMinimumIntervalInSeconds(60);
```

#### Set Minimum Time Interval for Fetch 

```javascript 
CleverTap.setMinimumFetchIntervalInSeconds(60);
```

#### Get Boolean key

```javascript 
CleverTap.getProductConfigBoolean('is_shown', (err, res) => {
	console.log('PC is_shown val in boolean :', res, err);
});
```
#### Get Long

```javascript 
CleverTap.getNumber('msg_count', (err, res) => {
	console.log('PC is_shown val in number(long)  :', res, err);
});
```
#### Get Double

```javascript 
CleverTap.getNumber('price', (err, res) => {
	console.log('PC price val in number :', res, err);
});		
```
#### Get String

```javascript 
CleverTap.getProductConfigString('text_color', (err, res) => {
        console.log('PC text_color val in string :', res, err);
});	
```
#### Get String (JSON)

```javascript 
CleverTap.getProductConfigString('json', (err, res) => {
	console.log('PC json val in string :', res, err);
});	
```

#### Delete all activated, fetched and defaults configs

```javascript 
CleverTap.resetProductConfig();
```

#### Get last fetched timestamp in millis

```javascript 
CleverTap.getLastFetchTimeStampInMillis((err, res) => {
        console.log('LastFetchTimeStampInMillis in string: ', res, err);
});		
```

## Feature Flag

#### Get Feature Flag

```javascript 
CleverTap.getFeatureFlag('is_dark_mode', false, (err, res) => {
	console.log('FF is_dark_mode val in boolean :', res, err);
});
```

## CleverTap ID

#### Get CleverTap ID

```javascript 
CleverTap.getCleverTapID((err, res) => {
        console.log('CleverTapID', res, err);
});
```

-----------

## App Personalisation

#### Enable Personalization

```javascript 
CleverTap.enablePersonalization();		
```

#### Disable Personalization

```javascript 
CleverTap.disablePersonalization();
```

#### Get Profile Name

```javascript 
CleverTap.profileGetProperty('Name', (err, res) => {
	console.log('CleverTap Profile Name: ', res, err);
});
```

-----------

## Debugging

```javascript 
CleverTap.setDebugLevel(3);
```

## Attributions

#### Get CleverTap Attribution Identifier

```javascript 
CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
         console.log('CleverTapAttributionIdentifier', res, err);
});
```

-----------

## InApp Notification Controls

#### Suspend InApp Notifications

```javascript 
CleverTap.suspendInAppNotifications();
```

#### Discard InApp Notifications

```javascript 
CleverTap.discardInAppNotifications();
```

#### Resume InApp Notifications

```javascript 
CleverTap.resumeInAppNotifications();
```


### For more information,
 - [See included Example Application](/Example/App.js) 
 - [See CleverTap JS interface](/index.js)
 - [See CleverTap TS interface](/index.d.ts)
