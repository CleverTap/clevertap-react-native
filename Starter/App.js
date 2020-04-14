/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, {Component} from 'react';
import {Platform, StyleSheet, Text, View, Button, Alert, TouchableHighlight, Linking} from 'react-native';
const CleverTap = require('clevertap-react-native');

const instructions = Platform.select({
  ios: 'Press Cmd+R to reload,\n' + 'Cmd+D or shake for dev menu',
  android:
    'Double tap R on your keyboard to reload,\n' +
    'Shake or press menu button for dev menu',
});

type Props = {};
export default class App extends Component<Props> {

  componentWillMount() {
      console.log('Component WILL MOUNT123!')

   }
    componentDidMount() {
        // optional: add listeners for CleverTap Events
        CleverTap.addListener(CleverTap.CleverTapProfileDidInitialize, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProfileDidInitialize, event); });
        CleverTap.addListener(CleverTap.CleverTapProfileSync, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapProfileSync, event); });
        CleverTap.addListener(CleverTap.CleverTapInAppNotificationDismissed, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapInAppNotificationDismissed, event); });
        CleverTap.addListener(CleverTap.CleverTapInboxDidInitialize, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxDidInitialize,event); });
        CleverTap.addListener(CleverTap.CleverTapInboxMessagesDidUpdate, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxMessagesDidUpdate,event); });
        CleverTap.addListener(CleverTap.CleverTapInboxMessageButtonTapped, (event) => { this._handleCleverTapInbox(CleverTap.CleverTapInboxMessageButtonTapped,event); });
        CleverTap.addListener(CleverTap.CleverTapDisplayUnitsLoaded, (event) => { this._handleCleverTapDisplayUnitsLoaded(CleverTap.CleverTapDisplayUnitsLoaded,event); });
        CleverTap.addListener(CleverTap.CleverTapInAppNotificationButtonTapped, (event) => { this._handleCleverTapEvent(CleverTap.CleverTapInAppNotificationButtonTapped,event); });

        CleverTap.setDebugLevel(1);
        // for iOS only: register for push notifications
        CleverTap.registerForPush();

        // for iOS only; record a Screen View
        CleverTap.recordScreenView('HomeView');

        //initialize the App Inbox
        CleverTap.initializeInbox();

        // Listener to handle incoming deep links
        Linking.addEventListener('url', this._handleOpenUrl);

        // this handles the case where a deep link launches the application
        Linking.getInitialURL().then((url) => {
            if (url) {
                console.log('launch url', url);
                this._handleOpenUrl({url});
            }
        }).catch(err => console.error('launch url error', err));

        // check to see if CleverTap has a launch deep link
        // handles the case where the app is launched from a push notification containing a deep link
        CleverTap.getInitialUrl((err, url) => {
            if (url) {
                console.log('CleverTap launch url', url);
                this._handleOpenUrl({url}, 'CleverTap');
            } else if (err) {
                console.log('CleverTap launch url', err);
            }
        });
    }

    componentWillUnmount() {
        // clean up listeners
        Linking.removeEventListener('url', this._handleOpenUrl);
        CleverTap.removeListeners();
    }

    _handleOpenUrl(event, from) {
        console.log('handleOpenUrl', event.url, from);
    }

    _handleCleverTapEvent(eventName, event) {
        console.log('handleCleverTapEvent', eventName, event);
    }

    _handleCleverTapInbox(eventName,event){
        console.log('handleCleverTapInbox',eventName,event);
    }

    _handleCleverTapDisplayUnitsLoaded(eventName,event){
        console.log('handleCleverTapDisplayUnitsLoaded',eventName,event);
    }

    _recordEvent(event) {
        CleverTap.recordEvent('testEvent');
        CleverTap.recordEvent('testEventWithProps', {'foo': 'bar'});
        CleverTap.setPushToken("abcdfcm",CleverTap.FCM);
    }

     _recordChargedEvent(event) {
        CleverTap.recordChargedEvent({'totalValue': 20, 'category': 'books'}, [{'title': 'book1'}, {'title': 'book2'}, {'title': 'book3'}]);
    }

    _updateUserProfile(event) {
        CleverTap.profileSet({'Name': 'testUserA1', 'Identity': '123456', 'Email': 'test@test.com', 'custom1': 123});
        CleverTap.profileSetMultiValuesForKey(['a', 'b', 'c'], 'letters');
        CleverTap.profileAddMultiValueForKey('d', 'letters');
        CleverTap.profileAddMultiValuesForKey(['e', 'f'], 'letters');
        CleverTap.profileRemoveMultiValueForKey('b', 'letters');
        CleverTap.profileRemoveMultiValuesForKey(['a', 'c'], 'letters');
        CleverTap.setLocation(34.15, -118.20);
    }

    _getUserProfileProperty(event) {
        CleverTap.enablePersonalization();

        CleverTap.profileGetProperty('Name', (err, res) => {
            console.log('CleverTap Profile Name: ', res, err);
        });

        CleverTap.profileGetCleverTapID((err, res) => {
            console.log('CleverTapID', res, err);
        });

        CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
            console.log('CleverTapAttributionIdentifier', res, err);
        });
    }
    
    _openInbox(event){
        CleverTap.showInbox({'tabs':['Offers','Promotions'],'navBarTitle':'My App Inbox','navBarTitleColor':'#FF0000','navBarColor':'#FFFFFF','inboxBackgroundColor':'#AED6F1','backButtonColor':'#00FF00'
                                ,'unselectedTabColor':'#0000FF','selectedTabColor':'#FF0000','selectedTabIndicatorColor':'#000000'});
    }

    _showCounts(event){
        CleverTap.getInboxMessageCount((err, res) => {
            console.log('Total Messages: ', res, err);
        });
        CleverTap.getInboxMessageUnreadCount((err, res) => {
            console.log('Unread Messages: ', res, err);
        });
    }

    _getAllInboxMessages(event){
        CleverTap.getAllInboxMessages((err, res) => {
            console.log('All Inbox Messages: ', res, err);
         });
    }

    _getUnreadInboxMessages(event){
        CleverTap.getUnreadInboxMessages((err, res) => {
             console.log('Unread Inbox Messages: ', res, err);
         });
    }

    _customAppInboxAPI(event){
        CleverTap.getInboxMessageForId('Message Id',(err, res) => {
            console.log("marking message read = "+res);
        });

        CleverTap.deleteInboxMessageForId('Message Id');
        CleverTap.markReadInboxMessageForId('Message Id');
        CleverTap.pushInboxNotificationViewedEventForId('Message Id');
        CleverTap.pushInboxNotificationClickedEventForId('Message Id');
    }

    _getDisplayUnitForId(event){
        CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
             console.log('Get Display Unit for Id:', res, err);
        });
     }

    _getAllDisplayUnits(event){
        CleverTap.getAllDisplayUnits((err, res) => {
             console.log('All Display Units: ', res, err);
        });
    }
  
  render() {
    return (
      <View style={styles.container}>
            <TouchableHighlight style={styles.button}
              onPress={this._recordEvent}>
              <Text>Record Event</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._recordChargedEvent}>
              <Text>Record Charged Event</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._updateUserProfile}>
              <Text>Update User Profile</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._getUserProfileProperty}>
              <Text>Get User Profile Property</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._openInbox}>
              <Text>Open Inbox</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._showCounts}>
              <Text>Show Counts</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._getAllInboxMessages}>
              <Text>Get all inbox messages</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._getUnreadInboxMessages}>
              <Text>Get unread messages</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._customAppInboxAPI}>
              <Text>Custom App Inbox API</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._getDisplayUnitForId}>
              <Text>Get Display Unit For Id</Text>
            </TouchableHighlight>
            <TouchableHighlight style={styles.button}
              onPress={this._getAllDisplayUnits}>
              <Text>Get All Display Units</Text>
            </TouchableHighlight>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 5,
  },
  button: {
        marginBottom: 20
  }
});
