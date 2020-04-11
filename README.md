# CleverTap React Native SDK
[![npm version](https://badge.fury.io/js/clevertap-react-native.svg)](https://badge.fury.io/js/clevertap-react-native)

## Install and Integration
1. `npm install --save clevertap-react-native`
2. Follow the [install instructions](./docs/install.md)
3. Follow the [integration instructions](./docs/integration.md)

## Additional Resources
- [CleverTap Android SDK Integration guide](https://support.clevertap.com/docs/android/getting-started.html)
- [CleverTap iOS SDK Integration guide](https://support.clevertap.com/docs/ios/getting-started.html)

## Example JS Usage
### Grab a reference  
`const CleverTap = require('clevertap-react-native');`

### Record an event  
`CleverTap.recordEvent('testEvent');`

### Update a user profile  
`CleverTap.onUserLogin({'Name': 'testUserA1', 'Identity': '123456', 'Email': 'test@test.com', 'custom1': 123});`

### For more: 
 - [see the included Example Project](https://github.com/CleverTap/clevertap-react-native/blob/master/Starter/App.js) 
 - [see the CleverTap JS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.js)
 - [see the CleverTap TS interface](https://github.com/CleverTap/clevertap-react-native/blob/master/index.d.ts)
