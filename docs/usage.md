
## Example JS Usage
#### Grab a reference  
```javascript 
const CleverTap = require('clevertap-react-native');
```

#### Record an event  
```javascript 
CleverTap.recordEvent('testEvent');
```

#### Record Charged event
```javascript 
CleverTap.recordChargedEvent({'totalValue': 20, 'category': 'books'}, [{'title': 'book1'}, {'title': 'book2'}, {'title': 'book3'}]);
```

#### Update a user profile 
```javascript 
CleverTap.onUserLogin({'Name': 'testUserA1', 'Identity': '123456', 'Email': 'test@test.com', 'custom1': 123});
```

#### App Inbox

##### Initialize the CleverTap App Inbox Method
```javascript 
CleverTap.initializeInbox();
```

##### Show the App Inbox
```javascript
CleverTap.showInbox({'tabs':['Offers','Promotions'],'navBarTitle':'My App Inbox','navBarTitleColor':'#FF0000','navBarColor':'#FFFFFF','inboxBackgroundColor':'#AED6F1','backButtonColor':'#00FF00'
                                ,'unselectedTabColor':'#0000FF','selectedTabColor':'#FF0000','selectedTabIndicatorColor':'#000000',
                                'noMessageText':'No message(s)','noMessageTextColor':'#FF0000'});
 ```

### For more information,
 - [see included Starter Application](https://github.com/CleverTap/clevertap-react-native/blob/master/Starter/App.js) 
 - [see CleverTap JS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.js)
 - [see CleverTap TS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.d.ts)
