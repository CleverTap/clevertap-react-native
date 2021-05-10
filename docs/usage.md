
## Example JS Usage

#### Grab a reference  
```javascript 
const CleverTap = require('clevertap-react-native');
```

## User Properties

#### Update User Profile(Push Profile )
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
				 alert(`CleverTapID: \n ${res}`);
        	});
```

#### Set Location to User Profile
```javascript 
CleverTap.setLocation(34.15, -118.20);
```

#### Record an event  
```javascript 
CleverTap.recordEvent('testEvent');
```

#### Record Charged event
```javascript 
CleverTap.recordChargedEvent({'totalValue': 20, 'category': 'books'}, [{'title': 'book1'}, {'title': 'book2'}, {'title': 'book3'}]);
```


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

#### Get Total message count
```javascript 
CleverTap.getInboxMessageCount((err, res) => {
				console.log('Total Messages: ', res, err);
				alert(`Total Messages: \n ${res}`);
			});	
```

#### Get Total message count
```javascript 
CleverTap.getInboxMessageUnreadCount((err, res) => {
				console.log('Unread Messages: ', res, err);
				alert(`Unread Messages: \n ${res}`);
			});	
```

#### Get All Inbox Messages
```javascript 
CleverTap.getAllInboxMessages((err, res) => {
				console.log('All Inbox Messages: ', res, err);
				alert(`All Inbox Messages: \n ${res}`);
			 });	
```

#### Get all Inbox unread messages
```javascript 
CleverTap.getUnreadInboxMessages((err, res) => {
				 console.log('Unread Inbox Messages: ', res, err);
				 alert(`Unread Inbox Messages: \n ${res}`);
			 });	
```

#### Get inbox Id
```javascript 
CleverTap.getInboxMessageForId('Message Id',(err, res) => {
            		console.log("marking message read = "+res);
					alert(`marking message read: \n ${res}`);
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


## Push Notifications

#### Creating Notification Channel
```javascript 
CleverTap.createNotificationChannel("CtRNS", "Clever Tap React Native Testing", "CT React Native Testing", 1, true);			
```

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
 
## Native Display

#### Get Display Unit for Id
```javascript 
CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
             console.log('Get Display Unit for Id:', res, err);
			 alert(`Get Display Unit for Id: ${res}`);
```

#### Get All Display Units
```javascript 
CleverTap.getAllDisplayUnits((err, res) => {
             console.log('All Display Units: ', res, err);
			 alert(`All Display Units: ${res}`);
        });
```

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
			  alert(`PC is_shown val in boolean : ${res}`);
```
#### Get Long
```javascript 
CleverTap.getNumber('msg_count', (err, res) => {
		      console.log('PC is_shown val in number(long)  :', res, err);
			  alert(`PC is_shown val in number(long) : ${res}`);
		 });
```
#### Get Double
```javascript 
CleverTap.getNumber('price', (err, res) => {
		      console.log('PC price val in number :', res, err);
			  alert(`PC is_shown val in number(double) : ${res}`);
		 });		
```
#### Get String
```javascript 
CleverTap.getProductConfigString('text_color', (err, res) => {
              		console.log('PC text_color val in string :', res, err);
					alert(`PC is_shown val in String : ${res}`);
         	});	
```
#### Get String (JSON)
```javascript 
CleverTap.getProductConfigString('json', (err, res) => {
		      console.log('PC json val in string :', res, err);
			  alert(`PC json val in String : ${res}`);
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
			    alert(`LastFetchTimeStampInMillis in string: ${res}`);
          });		
```

## Feature Flag

#### Get Feature Flag
```javascript 
CleverTap.getFeatureFlag('is_dark_mode', false, (err, res) => {
			      console.log('FF is_dark_mode val in boolean :', res, err);
				  alert(`FF is_dark_mode val in boolean :{res}`);
		     });
```

## App Personalisation

#### Enable Personalization
```javascript 
CleverTap.enablePersonalization();
			alert('enabled Personalization');	
		};
```

#### Get Profile Name
```javascript 
CleverTap.profileGetProperty('Name', (err, res) => {
		    console.log('CleverTap Profile Name: ', res, err);
		 			alert(`CleverTap Profile Name:${res}`);
        });
```

## Attributions

#### Get CleverTap Attribution Identifier
```javascript 
CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
            console.log('CleverTapAttributionIdentifier', res, err);
			alert(`CleverTapAttributionIdentifier${res}`);
        });
```






### For more information,
 - [see included Starter Application](https://github.com/CleverTap/clevertap-react-native/blob/master/Starter/App.js) 
 - [see CleverTap JS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.js)
 - [see CleverTap TS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.d.ts)
