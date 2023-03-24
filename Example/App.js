/* eslint-disable */
import React, { Component } from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createNativeStackNavigator } from '@react-navigation/native-stack';

import {
    Alert,
    LayoutAnimation,
    StyleSheet,
    View,
    Text,
    ScrollView,
    UIManager,
    TouchableOpacity,
    Platform,
    Image,
    Linking,
    ToastAndroid
} from 'react-native';

const CleverTap = require('clevertap-react-native');
const Stack = createNativeStackNavigator();

class Expandable_ListView extends Component {

    constructor() {

        super();

        this.state = {

            layout_Height: 0

        }
    }

    UNSAFE_componentWillReceiveProps(nextProps) {
        if (nextProps.item.expanded) {
            this.setState(() => {
                return {
                    layout_Height: null
                }
            });
        } else {
            this.setState(() => {
                return {
                    layout_Height: 0
                }
            });
        }
    }

    shouldComponentUpdate(nextProps, nextState) {
        if (this.state.layout_Height !== nextState.layout_Height) {
            return true;
        }
        return false;
    }


    //In this Function You can write the items to be called w.r.t list id:
    show_Selected_Category = (item) => {
        switch (item.id) {
          case 1:
            set_userProfile();
            break;
          case 2:
            CleverTap.profileSetMultiValuesForKey(['a', 'b', 'c'], 'letters');
            break;
          case 3:
            CleverTap.profileRemoveMultiValueForKey('b', 'letters');
            break;
          case 4:
            CleverTap.profileRemoveMultiValueForKey('b', 'letters');
            break;
          case 5:
            CleverTap.profileAddMultiValueForKey('d', 'letters');
            break;
          case 500:
            CleverTap.profileIncrementValueForKey(10, 'score');
            CleverTap.profileIncrementValueForKey(3.141, 'PI_Float');
            CleverTap.profileIncrementValueForKey(
              3.141592653589793,
              'PI_Double',
            );
            break;
          case 501:
            CleverTap.profileDecrementValueForKey(10, 'score');
            CleverTap.profileDecrementValueForKey(3.141, 'PI_Float');
            CleverTap.profileDecrementValueForKey(
              3.141592653589793,
              'PI_Double',
            );
            break;
          case 6:
            onUser_Login();
            break;
          case 7: //Removing a Value from the Multiple Values
            removeMultiValuesForKey();
            break;
          case 8:
            removeValueForKey();
            break;
          case 9:
            getCleverTap_id();
            break;
          case 10:
            set_userLocation();
            break;
          case 11:
            CleverTap.initializeInbox();
            break;
          case 12:
            show_appInbox();
            break;
          case 55:
            show_appInboxwithTabs();
            break;
          case 13:
            get_TotalMessageCount();
            break;
          case 14:
            get_UnreadMessageCount();
            break;
          case 15:
            Get_All_InboxMessages();
            break;
          case 16:
            get_All_InboxUnreadMessages();
            break;
          case 17:
            Get_InboxMessageForId();
            break;
          case 18:
            delete_InboxMessageForId();
            break;
          case 19:
            markRead_InboxMessageForId();
            break;
          case 20:
            pushInboxNotificationViewed();
            break;
          case 21:
            pushInboxNotificationClicked();
            break;
          case 60:
            CleverTap.dismissAppInbox();
            break;
          case 22:
            pushevent();
            break;
          case 23:
            pushChargedEvent();
            break;
          case 24:
            CleverTap.setDebugLevel(3);
            break;
          case 25:
            create_NotificationChannelGroup();
            break;
          case 26:
            create_NotificationChannel();
            break;
          case 27:
            delete_NotificationChannel();
            break;
          case 28:
            delete_NotificationChannelGroup();
            break;
          case 29:
            pushFcmRegistrationId();
            break;
          case 30:
            create_notification();
            break;
          case 300:
            createNotificationChannelWithSound();
            break;
          case 301:
            createNotificationChannelWithGroupId();
            break;
          case 302:
            createNotificationChannelWithGroupIdAndSound();
            break;
          case 31:
            getUnitID();
            break;
          case 32:
            getAllDisplayUnits();
            break;
          case 33:
            fetch();
            break;
          case 34:
            activate();
            break;
          case 35:
            fetchAndActivate();
            break;
          case 36:
            fetchwithMinIntervalinsec();
            break;
          case 37:
            setMinimumFetchIntervalInSeconds();
            break;
          case 38:
            getBoolean();
            break;
          case 39:
            getDouble();
            break;
          case 40:
            getLong();
            break;
          case 41:
            getString();
            break;
          case 42:
            getStrings();
            break;
          case 43:
            reset_config();
            break;
          case 44:
            getLastFetchTimeStampInMillis();
            break;
          case 45:
            getFeatureFlag();
            break;
          case 450:
            CleverTap.suspendInAppNotifications();
            break;
          case 451:
            CleverTap.discardInAppNotifications();
            break;
          case 452:
            CleverTap.resumeInAppNotifications();
            break;
          case 46:
            enablePersonalization();
            break;
          case 47:
            profile_getProperty();
            break;
          case 48:
            GetCleverTapAttributionIdentifier();
            break;
          case 49:
            CleverTap.setOptOut(false);
            break;
          case 50:
            CleverTap.enableDeviceNetworkInfoReporting(true);
            break;
          case 51:
            CleverTap.enablePersonalization();
            break;
          case 52:
            CleverTap.setOffline(false);
            break;
          case 53:
            addCleverTapAPIListeners(true);
            break;
          case 54:
            removeCleverTapAPIListeners();
            break;
          case 61:
          case 62:
          case 63:
          case 64:
          case 65:
          case 66:
          case 67:
          case 68:
          case 69:
          case 690:
          case 691:
          case 692:
          case 693:
          case 694:
          case 695:
          case 696:
          case 697:
          case 698:
            CleverTap.recordEvent(item.name);
            break;
          case 70:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
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
                });
              }
            });
            break;
          case 71:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
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
                  imageUrl:
                    'https://icons.iconarchive.com/icons/treetog/junior/64/camera-icon.png',
                  btnBorderRadius: '2',
                });
              }
            });
            break;
          case 72:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
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
                });
              }
            });
            break;
          case 73:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
                  inAppType: 'alert',
                  titleText: 'Get Notified',
                  messageText: 'Enable Notification permission',
                  followDeviceOrientation: true,
                  positiveBtnText: 'Allow',
                  negativeBtnText: 'Cancel',
                });
              }
            });
            break;
          case 74:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
                  inAppType: 'alert',
                  titleText: 'Get Notified',
                  messageText: 'Enable Notification permission',
                  followDeviceOrientation: false,
                  positiveBtnText: 'Allow',
                  negativeBtnText: 'Cancel',
                });
              }
            });
            break;
          case 75:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == true) {
                alert('Push Notification permission is already granted');
              }
              else {
                CleverTap.promptPushPrimer({
                  inAppType: 'alert',
                  titleText: 'Get Notified',
                  messageText: 'Enable Notification permission',
                  followDeviceOrientation: false,
                  positiveBtnText: 'Allow',
                  negativeBtnText: 'Cancel',
                  fallbackToSettings: true,
                });
              }
            });
            break;
          case 76:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == false) {
                CleverTap.promptForPushPermission(false);
              }
              else {
                alert('Push Notification permission is already granted');
              }
            });
            break;
          case 77:
            CleverTap.isPushPermissionGranted((err, res) => {
              console.log('isPushPermissionGranted', res, err);
              if (res == false) {
                CleverTap.promptForPushPermission(true);
              }
              else{
                alert('Push Notification permission is already granted');
              }
            });
            break;
        }
    }

    render() {
        return (

            <View style={styles.Panel_Holder}>

                <TouchableOpacity activeOpacity={0.8} onPress={this.props.onClickFunction} style={styles.category_View}>

                    <Text style={styles.category_Text}>{this.props.item.category_Name} </Text>

                    <Image
                        source={{ uri: 'https://reactnativecode.com/wp-content/uploads/2019/02/arrow_right_icon.png' }}
                        style={styles.iconStyle} />

                </TouchableOpacity>

                <View style={{ height: this.state.layout_Height, overflow: 'hidden' }}>

                    {
                        this.props.item.sub_Category.map((item, key) => (

                            <TouchableOpacity key={key} style={styles.sub_Category_Text}

                                onPress={this.show_Selected_Category.bind(this, item)}>

                                <Text style={styles.setSubCategoryFontSizeOne}> {item.name} </Text>

                                <View style={{ width: '100%', height: 1, backgroundColor: '#000' }} />

                            </TouchableOpacity>

                        ))
                    }

                </View>
            </View>
        );
    }
}

export default class App extends Component {

    constructor() {
        super();

        if (Platform.OS === 'android') {

            UIManager.setLayoutAnimationEnabledExperimental(true)

        }

        CleverTap.setDebugLevel(3);
        // for iOS only: register for push notifications
        CleverTap.registerForPush();
        addCleverTapAPIListeners(false);
        CleverTap.initializeInbox();

        // Listener to handle incoming deep links
        Linking.addEventListener('url', _handleOpenUrl);

        /// this handles the case where a deep link launches the application
        Linking.getInitialURL().then((url) => {
            if (url) {
                console.log('launch url', url);
                _handleOpenUrl({ url });
            }
        }).catch(err => console.error('launch url error', err));

        // check to see if CleverTap has a launch deep link
        // handles the case where the app is launched from a push notification containing a deep link
        CleverTap.getInitialUrl((err, url) => {
            if (url) {
                console.log('CleverTap launch url', url);
                _handleOpenUrl({ url }, 'CleverTap');
            } else if (err) {
                console.log('CleverTap launch url', err);
            }
        });

        const array = [
          {
            expanded: false,
            category_Name: 'User Properties',
            sub_Category: [
              {id: 1, name: 'pushProfile'},
              {id: 2, name: 'set Multi Values For Key'},
              {
                id: 3,
                name: 'removeMultiValueForKey',
              },
              {id: 4, name: 'removeValueForKey'},
              {id: 5, name: 'addMultiValueForKey'},
              {id: 500, name: 'Increment Value'},
              {id: 501, name: 'Decrement Value'},
            ],
          },

          {
            expanded: false,
            category_Name: 'Identity Management',
            sub_Category: [
              {id: 6, name: 'onUserLogin'},
              {id: 7, name: 'removeMultiValueForKey'},
              {
                id: 8,
                name: 'removeValueForKey',
              },
              {id: 9, name: 'getCleverTapID'},
            ],
          },

          {
            expanded: false,
            category_Name: 'Location ',
            sub_Category: [{id: 10, name: 'setLocation'}],
          },

          {
            expanded: false,
            category_Name: 'App Inbox',
            sub_Category: [
              {id: 11, name: 'initializeInbox'},
              {id: 12, name: 'showAppInbox'},
              {id: 55, name: 'showAppInboxwithTabs'},
              {id: 13, name: 'getInboxMessageCount'},
              {
                id: 14,
                name: 'getInboxMessageUnreadCount',
              },
              {id: 15, name: 'getAllInboxMessages'},
              {id: 16, name: 'getUnreadInboxMessages'},
              {id: 16, name: 'getInboxMessageForId'},
              {
                id: 18,
                name: 'deleteInboxMessage',
              },
              {id: 19, name: 'markReadInboxMessage'},
              {id: 20, name: 'pushInboxNotificationViewedEvent'},
              {
                id: 21,
                name: 'pushInboxNotificationClickedEvent',
              },
              {
                id: 60,
                name: 'dismissAppInbox',
              },
            ],
          },

          {
            expanded: false,
            category_Name: 'Events',
            sub_Category: [
              {id: 22, name: 'pushEvent'},
              {id: 23, name: 'pushChargedEvent'},
            ],
          },

          {
            expanded: false,
            category_Name: 'Enable Debugging',
            sub_Category: [{id: 24, name: 'Set Debug Level'}],
          },
          {
            expanded: false,
            category_Name: 'Push Notifications',
            sub_Category: [
              {id: 25, name: 'createNotificationChannelGroup'},
              {id: 26, name: 'createNotificationChannel'},
              {id: 27, name: 'deleteNotificationChannel'},
              {
                id: 28,
                name: 'deleteNotificationChannelGroup',
              },
              {id: 29, name: 'pushFcmRegistrationId'},
              {id: 30, name: 'createNotification'},
              {id: 300, name: 'createNotificationChannelWithSound'},
              {id: 301, name: 'createNotificationChannelWithGroupId'},
              {id: 302, name: 'createNotificationChannelWithGroupIdAndSound'},
            ],
          },
          {
            expanded: false,
            category_Name: 'Native Display',
            sub_Category: [
              {id: 31, name: 'getUnitID'},
              {id: 32, name: 'getAllDisplayUnits'},
            ],
          },
          {
            expanded: false,
            category_Name: 'Product Config',
            sub_Category: [
              {id: 33, name: 'productConfig setDefault'},
              {id: 34, name: 'fetch()'},
              {id: 35, name: 'activate'},
              {id: 36, name: 'fetchAndActivate'},
              {
                id: 37,
                name: 'setMinimumFetchIntervalInSeconds',
              },
              {id: 38, name: 'getBoolean'},
              {id: 39, name: 'getDouble'},
              {id: 40, name: 'getLong'},
              {
                id: 41,
                name: 'getString',
              },
              {id: 42, name: 'getString'},
              {id: 43, name: 'reset'},
              ,
              {
                id: 44,
                name: 'getLastFetchTimeStampInMillis',
              },
            ],
          },
          {
            expanded: false,
            category_Name: 'Feature Flag',
            sub_Category: [{id: 45, name: 'getFeatureFlag'}],
          },
          {
            expanded: false,
            category_Name: 'InApp Controls',
            sub_Category: [
              {id: 450, name: 'suspendInAppNotifications'},
              {id: 451, name: 'discardInAppNotifications'},
              {id: 452, name: 'resumeInAppNotifications'},
            ],
          },
          {
            expanded: false,
            category_Name: 'App Personalisation',
            sub_Category: [
              {id: 46, name: 'enablePersonalization'},
              {id: 47, name: 'get profile Property'},
            ],
          },
          {
            expanded: false,
            category_Name: 'Attributions',
            sub_Category: [
              {
                id: 48,
                name: '(Deprecated) get CleverTap Attribution Identifier',
              },
            ],
          },
          {
            expanded: false,
            category_Name: 'GDPR',
            sub_Category: [
              {id: 49, name: 'setOptOut'},
              {id: 50, name: 'enableDeviceNetworkInfoReporting'},
            ],
          },
          {
            expanded: false,
            category_Name: 'Multi-Instance',
            sub_Category: [
              {id: 51, name: 'enablePersonalization'},
              {id: 52, name: 'setOffline'},
            ],
          },
          {
            expanded: false,
            category_Name: 'Listeners',
            sub_Category: [
              {id: 53, name: 'addCleverTapAPIListeners'},
              {
                id: 54,
                name: 'removeCleverTapAPIListeners',
              },
            ],
          },
          {
            expanded: false,
            category_Name: 'Push Templates',
            sub_Category: [
              {id: 60, name: 'Send Basic Push'},
              {id: 61, name: 'Send Carousel Push'},
              {id: 62, name: 'Send Manual Carousel Push'},
              {id: 63, name: 'Send Filmstrip Carousel Push'},
              {id: 64, name: 'Send Rating Push'},
              {id: 65, name: 'Send Product Display Notification'},
              {id: 66, name: 'Send Linear Product Display Push'},
              {id: 67, name: 'Send CTA Notification'},
              {id: 68, name: 'Send Zero Bezel Notification'},
              {id: 69, name: 'Send Zero Bezel Text Only Notification'},
              {id: 690, name: 'Send Timer Notification'},
              {id: 691, name: 'Send Input Box Notification'},
              {id: 692, name: 'Send Input Box Reply with Event Notification'},
              {
                id: 693,
                name: 'Send Input Box Reply with Auto Open Notification',
              },
              {id: 694, name: 'Send Input Box Remind Notification DOC FALSE'},
              {id: 695, name: 'Send Input Box CTA DOC true'},
              {id: 696, name: 'Send Input Box CTA DOC false'},
              {id: 697, name: 'Send Input Box Reminder DOC true'},
              {id: 698, name: 'Send Input Box Reminder DOC false'},
            ],
          },
          {
            expanded: false,
            category_Name: 'PROMPT LOCAL IAM',
            sub_Category: [
              {id: 70, name: 'Half-Interstitial Local IAM'},
              {id: 71, name: 'Half-Interstitial Local IAM with image URL'},
              {
                id: 72,
                name: 'Half-Interstitial Local IAM with fallbackToSettings - true',
              },
              {id: 73, name: 'Alert Local IAM'},
              {
                id: 74,
                name: 'Alert Local IAM with followDeviceOrientation - false',
              },
              {id: 75, name: 'Alert Local IAM with fallbackToSettings - true'},
              {
                id: 76,
                name: 'Hard permission dialog with fallbackToSettings - false',
              },
              {
                id: 77,
                name: 'Hard permission dialog with fallbackToSettings - true',
              },
            ],
          },
        ];

        this.state = { AccordionData: [...array] }
    }

    update_Layout = (index) => {

        LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);

        const array = [...this.state.AccordionData];

        array[index]['expanded'] = !array[index]['expanded'];

        this.setState(() => {
            return {
                AccordionData: array
            }
        });
    }

    render() {
        return (
            <View style={styles.MainContainer}>

                <ScrollView contentContainerStyle={{ paddingHorizontal: 8, paddingVertical: 5 }}>
                    <TouchableOpacity
                        style={styles.button}>
                        <Text style={styles.button_Text}>CleverTap Example</Text>

                    </TouchableOpacity>
                    {
                        this.state.AccordionData.map((item, key) =>
                        (
                            <Expandable_ListView key={item.category_Name}
                                onClickFunction={this.update_Layout.bind(this, key)} item={item} />
                        ))
                    }

                </ScrollView>

            </View>
        );
    }
}

set_userProfile = () => {

    alert('User Profile Updated');

    CleverTap.profileSet({
        'Name': 'testUserA1', 'Identity': '123456', 'Email': 'test@test.com', 'custom1': 123,
        'birthdate': new Date('2020-03-03T06:35:31')
    });

};

//Identity_Management
onUser_Login = () => {
    alert('User Profile Updated');

    //On user Login
    CleverTap.onUserLogin({
        'Name': 'testUserA1', 'Identity': new Date().getTime() + '',
        'Email': new Date().getTime() + 'testmobile@test.com', 'custom1': 123,
        'birthdate': new Date('1992-12-22T06:35:31')
    })

};
removeMultiValuesForKey = () => {
    alert('User Profile Updated');

    //Removing Multiple Values
    CleverTap.profileRemoveMultiValuesForKey(['a', 'c'], 'letters');

};
removeValueForKey = () => {
    alert('User Profile Updated');

    //Removing Value for key
    CleverTap.profileRemoveValueForKey("letters");

};
getCleverTap_id = () => {
    // Below method is deprecated since 0.6.0, please check index.js for deprecation, instead use CleverTap.getCleverTapID()
    /*CleverTap.profileGetCleverTapID((err, res) => {
        console.log('CleverTapID', res, err);
        alert(`CleverTapID: \n ${res}`);
    });*/

    // Use below newly added method
    CleverTap.getCleverTapID((err, res) => {
        console.log('CleverTapID', res, err);
        alert(`CleverTapID: \n ${res}`);
    });
}
// Location
set_userLocation = () => {
    alert('User Location set');

    CleverTap.setLocation(34.15, -118.20);

};
///Events

pushevent = () => {
    alert('Event Recorded');

    //Recording an Event
    CleverTap.recordEvent('testEvent');
    CleverTap.recordEvent('Send Basic Push');
    CleverTap.recordEvent('testEventWithProps', { 'start': new Date(), 'foo': 'bar' });
};

pushChargedEvent = () => {
    alert('Charged Event Recorded');

    //Recording an Event
    CleverTap.recordChargedEvent({ 'totalValue': 20, 'category': 'books', 'purchase_date': new Date() },
        [{ 'title': 'book1', 'published_date': new Date('2010-12-12T06:35:31'), 'author': 'ABC' },
        { 'title': 'book2', 'published_date': new Date('2000-12-12T06:35:31') },
        { 'title': 'book3', 'published_date': new Date(), 'author': 'XYZ' }]
    );

};
//App Inbox

show_appInbox = () => {

    //Show Inbox
    CleverTap.showInbox({
        'navBarTitle': 'My App Inbox',
        'navBarTitleColor': '#FF0000',
        'navBarColor': '#FFFFFF',
        'inboxBackgroundColor': '#AED6F1',
        'backButtonColor': '#00FF00',
        'unselectedTabColor': '#0000FF',
        'selectedTabColor': '#FF0000',
        'selectedTabIndicatorColor': '#000000',
        'noMessageText': 'No message(s)',
        'noMessageTextColor': '#FF0000'
    });


};

show_appInboxwithTabs = () => {

    //Show Inbox
    CleverTap.showInbox({
        'tabs': ['Offers', 'Promotions'],
        'navBarTitle': 'My App Inbox',
        'navBarTitleColor': '#FF0000',
        'navBarColor': '#FFFFFF',
        'inboxBackgroundColor': '#AED6F1',
        'backButtonColor': '#00FF00',
        'unselectedTabColor': '#0000FF',
        'selectedTabColor': '#FF0000',
        'selectedTabIndicatorColor': '#000000',
        'noMessageText': 'No message(s)',
        'noMessageTextColor': '#FF0000'
    });


};

get_TotalMessageCount = () => {
    //Get Total messagecount

    CleverTap.getInboxMessageCount((err, res) => {
        console.log('Total Messages: ', res, err);
        alert(`Total Messages: \n ${res}`);
    });
};
get_UnreadMessageCount = () => {

    //Get the count of unread messages
    CleverTap.getInboxMessageUnreadCount((err, res) => {
        console.log('Unread Messages: ', res, err);
        alert(`Unread Messages: \n ${res}`);
    });
};
Get_All_InboxMessages = () => {

    //Get All Inbox Messages
    CleverTap.getAllInboxMessages((err, res) => {
        console.log('All Inbox Messages: ', res, err);
        alert(`All Inbox Messages: \n ${res}`);
    });
};
get_All_InboxUnreadMessages = () => {

    //get all Inbox unread messages
    CleverTap.getUnreadInboxMessages((err, res) => {
        console.log('Unread Inbox Messages: ', res, err);
        alert(`Unread Inbox Messages: \n ${res}`);
    });
};
Get_InboxMessageForId = () => {
    //Get inbox Id

    CleverTap.getInboxMessageForId('Message Id', (err, res) => {
        console.log("marking message read = " + res);
        alert(`marking message read: \n ${res}`);
    });
};

delete_InboxMessageForId = () => {
    //Get inbox Id
    alert('Check Console for values');
    CleverTap.deleteInboxMessageForId('Message Id');

};

markRead_InboxMessageForId = () => {
    //Get inbox Id
    alert('Check Console for values');
    CleverTap.markReadInboxMessageForId('Message Id');

};
pushInboxNotificationViewed = () => {
    //Get inbox Id
    alert('Check Console for values');
    CleverTap.pushInboxNotificationViewedEventForId('Message Id');

};
pushInboxNotificationClicked = () => {
    //Get inbox Id
    alert('Check Console for values');
    CleverTap.pushInboxNotificationClickedEventForId('Message Id');

};

///Push Notification
create_NotificationChannel = () => {
    alert('Notification Channel Created');
    //Creating Notification Channel
    CleverTap.createNotificationChannel("CtRNS", "Clever Tap React Native Testing", "CT React Native Testing", 1, true);
    CleverTap.createNotificationChannel("BRTesting", "Clever Tap BR Testing", "CT BR Testing", 1, true);
    CleverTap.createNotificationChannel("PTTesting", "Clever Tap PT Testing", "CT PT Testing", 1, true);

};
delete_NotificationChannel = () => {
    alert('Notification Channel Deleted');
    //Delete Notification Channel
    CleverTap.deleteNotificationChannel("CtRNS")

};

create_NotificationChannelGroup = () => {
    alert('Notification Channel Group Created');
    //Creating a group notification channel
    CleverTap.createNotificationChannelGroup("Offers", "All Offers related notifications")

};
delete_NotificationChannelGroup = () => {
    alert('Notification Channel Group Deleted');
    //Delete a group notification channel
    CleverTap.deleteNotificationChannelGroup("Offers")

};

pushFcmRegistrationId = () => {
    alert('Registered FCM Id for Push');
    //Setting up a Push Notification
    if (Platform.OS === 'android') {
        // Use only during custom implementation and make sure that FCM credentials used to generate token are same as CleverTap
        // or else two different tokens will be pushed to BackEnd resulting in unwanted behavior
        // => https://github.com/CleverTap/clevertap-react-native/issues/166
        // => https://developer.clevertap.com/docs/android#section-custom-android-push-notifications-handling
        CleverTap.setPushToken("1000test000token000fcm", CleverTap.FCM);
        //CleverTap.setPushToken("111056687894", CleverTap.HMS);//for Huawei push
        CleverTap.setPushToken("1000test000token000xps", CleverTap.XPS, "Europe");//for Xiaomi push
        //CleverTap.setPushToken("111056687894", CleverTap.BPS);//for Baidu push

    }

};
create_notification = () => {

    // createNotification in your custom implementation => https://developer.clevertap.com/docs/android#section-custom-android-push-notifications-handling

    // Please note, extras passed in below method is just for showcase, you need to pass the one that you receive from FCM
    CleverTap.createNotification({
        'wzrk_acct_id': '88R-R54-5Z6Z',
        'nm': 'Testing 1..2..3..',
        'nt': 'Test event',
        'pr': 'max',
        'wzrk_pivot': 'wzrk_default',
        'wzrk_ttl_s': '2419200',
        'wzrk_cid': 'CtRNS',
        'wzrk_pid': new Date().getTime(),
        'wzrk_rnv': false,
        'wzrk_ttl': '1627053990',
        'wzrk_push_amp': false,
        'wzrk_bc': '',
        'wzrk_bi': '2',
        'wzrk_dt': 'FIREBASE',
        'wzrk_id': '1624627506_20210625',
        'wzrk_pn': true
    });

};
//Native Display
getUnitID = () => {

    CleverTap.getDisplayUnitForId('Unit Id', (err, res) => {
        console.log('Get Display Unit for Id:', res, err);
        alert(`Get Display Unit for Id: ${res}`);
    });

};
getAllDisplayUnits = () => {

    CleverTap.getAllDisplayUnits((err, res) => {
        console.log('All Display Units: ', res, err);
        alert(`All Display Units: ${res}`);
    });

};
// Product Config 

productConfig = () => {
    alert('Product Configuration set to default');
    //Product config:
    CleverTap.setDefaultsMap({
        'text_color': 'red',
        'msg_count': 100,
        'price': 100.50,
        'is_shown': true,
        'json': '{"key":"val"}'
    });

};
fetch = () => {
    alert('Check Console for update result');
    //Fetch
    CleverTap.fetch();

};
activate = () => {
    alert('Check Console for update result');
    //Activate
    CleverTap.activate();

};
fetchAndActivate = () => {
    alert('Check Console for update result');

    //Fetch And Activate
    CleverTap.fetchAndActivate();

};
fetchwithMinIntervalinsec = () => {
    alert('Check Console for update result');

    //Fetch Minimum Time Interval
    CleverTap.fetchWithMinimumIntervalInSeconds(60);

};

setMinimumFetchIntervalInSeconds = () => {
    alert('Check Console for update result');

    //Set Minimum Interval
    CleverTap.setMinimumFetchIntervalInSeconds(60);

};
getBoolean = () => {

    //Boolean
    CleverTap.getProductConfigBoolean('is_shown', (err, res) => {
        console.log('PC is_shown val in boolean :', res, err);
        alert(`PC is_shown val in boolean: ${res}`);
    });


};
getLong = () => {
    alert('Check Console for update result');

    //Number
    CleverTap.getNumber('msg_count', (err, res) => {
        console.log('PC is_shown val in number(long)  :', res, err);
        alert(`PC is_shown val in number(long): ${res}`);
    });


};
getDouble = () => {

    CleverTap.getNumber('price', (err, res) => {
        console.log('PC price val in number :', res, err);
        alert(`PC is_shown val in number(double) : ${res}`);
    });
};
getString = () => {
    alert('Check Console for update result');

    //Set Minimum Interval
    //String
    CleverTap.getProductConfigString('text_color', (err, res) => {
        console.log('PC text_color val in string :', res, err);
        alert(`PC is_shown val in String : ${res}`);
    });

};
getStrings = () => {
    alert('Check Console for update result');

    //Set Minimum Interval
    CleverTap.getProductConfigString('json', (err, res) => {
        console.log('PC json val in string :', res, err);
        alert(`PC json val in String: ${res}`);
    });

};
reset_config = () => {
    alert('Check Console for update result');
    //Reset Product config
    CleverTap.resetProductConfig();
};

getLastFetchTimeStampInMillis = () => {

    //get Last Fetch TimeStamp In Milliseconds
    CleverTap.getLastFetchTimeStampInMillis((err, res) => {
        console.log('LastFetchTimeStampInMillis in string: ', res, err);
        alert(`LastFetchTimeStampInMillis in string: ${res}`);
    });
};
//feature flag
getFeatureFlag = () => {

    //Feature flag
    CleverTap.getFeatureFlag('is_dark_mode', false, (err, res) => {
        console.log('FF is_dark_mode val in boolean :', res, err);
        alert(`FF is_dark_mode val in boolean: ${res}`);
    });
};

//App Personalisation

enablePersonalization = () => {


    //enablePersonalization
    CleverTap.enablePersonalization();
    alert('enabled Personalization');

};
profile_getProperty = () => {


    //CleverTap Profile Name:
    CleverTap.profileGetProperty('Name', (err, res) => {
        console.log('CleverTap Profile Name: ', res, err);
        alert(`CleverTap Profile Name: ${res}`);
    });

};
///Attributions
GetCleverTapAttributionIdentifier = () => {

    // Below method is deprecated since 0.6.0, please check index.js for deprecation, use CleverTap.getCleverTapID(callback) instead
    //Default Instance
    CleverTap.profileGetCleverTapAttributionIdentifier((err, res) => {
        console.log('CleverTapAttributionIdentifier', res, err);
        alert(`CleverTapAttributionIdentifier: ${res}`);
    });

};

function _handleOpenUrl(event, from) {
    console.log('handleOpenUrl', event.url, from);
}

function removeCleverTapAPIListeners() {
    // clean up listeners

    Linking.removeEventListener('url', _handleOpenUrl);
    CleverTap.removeListener(CleverTap.CleverTapProfileDidInitialize);
    CleverTap.removeListener(CleverTap.CleverTapProfileSync);
    CleverTap.removeListener(CleverTap.CleverTapInAppNotificationDismissed);
    CleverTap.removeListener(CleverTap.CleverTapInAppNotificationShowed);
    CleverTap.removeListener(CleverTap.CleverTapInboxDidInitialize);
    CleverTap.removeListener(CleverTap.CleverTapInboxMessagesDidUpdate);
    CleverTap.removeListener(CleverTap.CleverTapInboxMessageButtonTapped);
    CleverTap.removeListener(CleverTap.CleverTapDisplayUnitsLoaded);
    CleverTap.removeListener(CleverTap.CleverTapInAppNotificationButtonTapped);
    CleverTap.removeListener(CleverTap.CleverTapFeatureFlagsDidUpdate);
    CleverTap.removeListener(CleverTap.CleverTapProductConfigDidInitialize);
    CleverTap.removeListener(CleverTap.CleverTapProductConfigDidFetch);
    CleverTap.removeListener(CleverTap.CleverTapProductConfigDidActivate);
    CleverTap.removeListener(CleverTap.CleverTapPushNotificationClicked);
    CleverTap.removeListener(CleverTap.CleverTapPushPermissionResponseReceived);
    alert("Listeners removed successfully");
}

function addCleverTapAPIListeners(fromClick) {
    // optional: add listeners for CleverTap Events
    CleverTap.addListener(CleverTap.CleverTapProfileDidInitialize, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapProfileDidInitialize, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProfileSync, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapProfileSync, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInAppNotificationDismissed, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapInAppNotificationDismissed, event);
    });
  CleverTap.addListener(CleverTap.CleverTapInAppNotificationShowed, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapInAppNotificationShowed, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxDidInitialize, (event) => {
        _handleCleverTapInbox(CleverTap.CleverTapInboxDidInitialize, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessagesDidUpdate, (event) => {
        _handleCleverTapInbox(CleverTap.CleverTapInboxMessagesDidUpdate, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessageButtonTapped, (event) => {
        _handleCleverTapInbox(CleverTap.CleverTapInboxMessageButtonTapped, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInboxMessageTapped, (event) => {
        _handleCleverTapInbox(CleverTap.CleverTapInboxMessageTapped, event);
    });

    CleverTap.addListener(CleverTap.CleverTapDisplayUnitsLoaded, (event) => {
        _handleCleverTapDisplayUnitsLoaded(CleverTap.CleverTapDisplayUnitsLoaded, event);
    });
    CleverTap.addListener(CleverTap.CleverTapInAppNotificationButtonTapped, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapInAppNotificationButtonTapped, event);
    });
    CleverTap.addListener(CleverTap.CleverTapFeatureFlagsDidUpdate, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapFeatureFlagsDidUpdate, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProductConfigDidInitialize, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapProductConfigDidInitialize, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProductConfigDidFetch, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapProductConfigDidFetch, event);
    });
    CleverTap.addListener(CleverTap.CleverTapProductConfigDidActivate, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapProductConfigDidActivate, event);
    });
    CleverTap.addListener(CleverTap.CleverTapPushNotificationClicked, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapPushNotificationClicked, event);
    });
    CleverTap.addListener(CleverTap.CleverTapPushPermissionResponseReceived, (event) => {
        _handleCleverTapEvent(CleverTap.CleverTapPushPermissionResponseReceived, event);
    });
    if (fromClick) {
        alert("Listeners added successfully");
    }
}

function createNotificationChannelWithSound() {
    // https://developer.clevertap.com/docs/add-a-sound-file-to-your-android-app

    CleverTap.createNotificationChannelWithSound("CtRNS", "Clever Tap React Native Testing",
        "CT React Native Testing", 1, true, "glitch.mp3")
}

function createNotificationChannelWithGroupId() {
    // https://developer.clevertap.com/docs/android#section-push-notifications-for-android-o

    CleverTap.createNotificationChannelWithGroupId("offersMonthly", "Monthly Offers",
        "Offers given at every month", 1, "Offers", true)
    CleverTap.createNotificationChannelWithGroupId("offersQuarterly", "Quarterly Offers",
        "Offers given at every Quarter", 1, "Offers", true)
}


function createNotificationChannelWithGroupIdAndSound() {
    // https://developer.clevertap.com/docs/android#section-push-notifications-for-android-o

    CleverTap.createNotificationChannelWithGroupIdAndSound("offersMonthly", "Monthly Offers",
        "Offers given at every month", 1, "Offers", true, "glitch.mp3")
    CleverTap.createNotificationChannelWithGroupIdAndSound("offersQuarterly", "Quarterly Offers",
        "Offers given at every Quarter", 1, "Offers", true, "glitch.mp3")

}


function _handleCleverTapEvent(eventName, event) {
    console.log('handleCleverTapEvent', eventName, event);
    ToastAndroid.show(`${eventName} called!`, ToastAndroid.SHORT);
}

function _handleCleverTapInbox(eventName, event) {
    if (eventName == "CleverTapInboxMessageTapped"){
      console.log(event.message);
      console.log("Index : " + event.index);
      console.log("Button index : " + event.buttonIndex);
    }
    else {
    console.log('handleCleverTapInbox', eventName, event);
    }
    ToastAndroid.show(`${eventName} called!`, ToastAndroid.SHORT);
}

function _handleCleverTapDisplayUnitsLoaded(eventName, event) {
    console.log('handleCleverTapDisplayUnitsLoaded', eventName, event);
    ToastAndroid.show(`${eventName} called!`, ToastAndroid.SHORT);
}


const styles = StyleSheet.create({

    MainContainer: {
        flex: 1,
        justifyContent: 'center',
        paddingTop: (Platform.OS === 'ios') ? 44 : 0,
        backgroundColor: '#fff'
    },

    iconStyle: {
        width: 22,
        height: 22,
        justifyContent: 'flex-end',
        alignItems: 'center',
        tintColor: '#fff'
    },

    sub_Category_Text: {
        fontSize: 20,
        color: '#000',
        padding: 10
    },

    category_Text: {
        textAlign: 'left',
        color: '#fff',
        fontSize: 22,
        padding: 12
    },

    category_View: {
        marginVertical: 5,
        flexDirection: 'row',
        justifyContent: 'space-between',
        alignItems: 'center',
        backgroundColor: '#DC2626'
    },

    Btn: {
        padding: 10,
        backgroundColor: '#FF6F00'
    },

    button: {
        backgroundColor: "#fff",
        flexWrap: "wrap",
        color: '#fff',
        fontSize: 44,
        padding: 10
    },

    button_Text: {
        width: '100%',
        textAlign: 'center',
        color: '#000',
        fontWeight: 'bold',
        fontSize: 26
    },

    setSubCategoryFontSizeOne: {
        fontSize: 18
    },

});
