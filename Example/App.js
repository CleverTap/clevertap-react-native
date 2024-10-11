import React, { Component } from 'react';
// import { NavigationContainer } from '@react-navigation/native';
// import { createNativeStackNavigator } from '@react-navigation/native-stack';
import CustomTemplate from './CustomTemplate';
import {
  LayoutAnimation,
  StyleSheet,
  View,
  Text,
  ScrollView,
  UIManager,
  TouchableOpacity,
  Platform,
  Linking
} from 'react-native';
import { ExpandableListView } from './ExpandableListView';
import { Actions } from './constants';
import * as utils from './utils';

const CleverTap = require('clevertap-react-native');
// const Stack = createNativeStackNavigator();

export default class App extends Component {
  constructor() {
    super();
    if (Platform.OS === 'android') {
      UIManager.setLayoutAnimationEnabledExperimental(true);
    }

    CleverTap.setDebugLevel(3);
    CleverTap.registerForPush(); // iOS push notification registration
    utils.addCleverTapAPIListeners(false);
    CleverTap.initializeInbox();

    // Deep link listener
    Linking.addEventListener('url', utils._handleOpenUrl);
    Linking.getInitialURL().then((url) => {
      if (url) {
        utils._handleOpenUrl({ url });
      }
    }).catch((err) => console.error('launch url error', err));

    // Check CleverTap deep link
    CleverTap.getInitialUrl((err, url) => {
      if (url) {
        utils._handleOpenUrl({ url }, 'CleverTap');
      }
    });

    this.state = { AccordionData: [...this._getAccordionData()] };
  }

  _getAccordionData() {
    return [
      {
        expanded: false,
        category_Name: 'Product Experiences: Vars',
        sub_Category: [
          {
            action: Actions.SYNC_VARIABLES,
            name: 'Sync Variables'
          },
          {
            action: Actions.GET_VARIABLES,
            name: 'Get Variables'
          },
          {
            action: Actions.GET_VARIABLE,
            name: 'Get Variable Value for name \'reactnative_var_string\''
          },
          {
            action: Actions.DEFINE_VARIABLES,
            name: 'Define Variables'
          },
          {
            action: Actions.FETCH_VARIABLES,
            name: 'Fetch Variables'
          },
          {
            action: Actions.VARIABLES_CHANGED,
            name: 'Add \'OnVariablesChanged\' listener'
          },
          {
            action: Actions.VALUE_CHANGED,
            name: 'Add \'OnValueChanged\' listener for name \'reactnative_var_string\''
          }
        ],
      },
      {
        expanded: false,
        category_Name: 'Client Side InApps',
        sub_Category: [
          { action: Actions.FETCH_INAPPS, name: 'Fetch Client Side InApps' },
          { action: Actions.CLEAR_INAPPS, name: 'Clear All InApp Resources' },
          { action: Actions.CLEAR_INAPPS_EXPIRED, name: 'Clear Expired Only InApp Resources' }
        ],
      },
      {
        expanded: false,
        category_Name: 'User Properties',
        sub_Category: [
          { action: Actions.SET_USER_PROFILE, name: 'pushProfile' },
          { action: Actions.SET_MULTI_VALUES, name: 'set Multi Values For Key' },
          {
            action: Actions.REMOVE_MULTI_VALUE,
            name: 'removeMultiValueForKey',
          },
          { action: Actions.ADD_MULTI_VALUE, name: 'addMultiValueForKey' },
          { action: Actions.INCREMENT_VALUE, name: 'Increment Value' },
          { action: Actions.DECREMENT_VALUE, name: 'Decrement Value' },
        ],
      },

      {
        expanded: false,
        category_Name: 'Identity Management',
        sub_Category: [
          { action: Actions.USER_LOGIN, name: 'onUserLogin' },
          { action: Actions.CLEVERTAP_ID, name: 'getCleverTapID' },
        ],
      },

      {
        expanded: false,
        category_Name: 'Location ',
        sub_Category: [
          { action: Actions.USER_LOCATION, name: 'setLocation' },
          { action: Actions.USER_LOCALE, name: 'setLocale' },
        ],
      },

      {
        expanded: false,
        category_Name: 'App Inbox',
        sub_Category: [
          { action: Actions.INITIALIZE_INBOX, name: 'initializeInbox' },
          { action: Actions.SHOW_INBOX, name: 'showAppInbox' },
          { action: Actions.SHOW_INBOX_TABS, name: 'showAppInboxwithTabs' },
          { action: Actions.INBOX_TOTAL_MESSAGE_COUNT, name: 'getInboxMessageCount' },
          {
            action: Actions.INBOX_UNREAD_MESSAGE_COUNT,
            name: 'getInboxMessageUnreadCount',
          },
          { action: Actions.INBOX_ALL_MESSAGES, name: 'getAllInboxMessages' },
          { action: Actions.INBOX_UNREAD_MESSAGES, name: 'getUnreadInboxMessages' },
          { action: Actions.INBOX_MESSAGE_FOR_ID, name: 'getInboxMessageForId' },
          {
            action: Actions.INBOX_DELETE_MESSAGE_FOR_ID,
            name: 'deleteInboxMessage',
          },
          { action: Actions.INBOX_READ_MESSAGE_FOR_ID, name: 'markReadInboxMessage' },
          { action: Actions.INBOX_NOTIFICATION_VIEWED, name: 'pushInboxNotificationViewedEvent' },
          {
            action: Actions.INBOX_NOTIFICATION_CLICKED,
            name: 'pushInboxNotificationClickedEvent',
          },
        ],
      },

      {
        expanded: false,
        category_Name: 'Events',
        sub_Category: [
          { action: Actions.PUSH_EVENT, name: 'pushEvent' },
          { action: Actions.PUSH_CHARGED_EVENT, name: 'pushChargedEvent' },
        ],
      },
      {
        expanded: false,
        category_Name: 'Enable Debugging',
        sub_Category: [{ action: Actions.SET_DEBUG, name: 'Set Debug Level' }],
      },
      {
        expanded: false,
        category_Name: 'Push Notifications',
        sub_Category: [
          { action: Actions.CREATE_NOTIFICATION_GROUP, name: 'createNotificationChannelGroup' },
          { action: Actions.CREATE_NOTIFICATION_CHANNEL, name: 'createNotificationChannel' },
          { action: Actions.DELETE_NOTIFICATION_CHANNEL, name: 'deleteNotificationChannel' },
          {
            action: Actions.DELETE_NOTIFICATION_GROUP,
            name: 'deleteNotificationChannelGroup',
          },
          { action: Actions.PUSH_FCM, name: 'pushFcmRegistrationId' },
          { action: Actions.CREATE_NOTIFICATION, name: 'createNotification' },
          { action: Actions.CREATE_NOTIFICATION_CHANNEL_WITH_SOUND, name: 'createNotificationChannelWithSound' },
          { action: Actions.CREATE_NOTIFICATION_CHANNEL_WITH_GROUP, name: 'createNotificationChannelWithGroupId' },
          { action: Actions.CREATE_NOTIFICATION_CHANNEL_WITH_GROUP_AND_SOUND, name: 'createNotificationChannelWithGroupIdAndSound' },
        ],
      },
      {
        expanded: false,
        category_Name: 'Native Display',
        sub_Category: [
          { action: Actions.DISPLAY_UNIT_ID, name: 'getUnitID' },
          { action: Actions.ALL_DISPLAY_UNITS, name: 'getAllDisplayUnits' },
        ],
      },
      {
        expanded: false,
        category_Name: 'Product Config',
        sub_Category: [
          { action: Actions.PRODUCT_CONFIG_FETCH, name: 'fetch()' },
          { action: Actions.PRODUCT_CONFIG_ACTIVATE, name: 'activate' },
          { action: Actions.PRODUCT_CONFIG_FETCH_AND_ACTIVATE, name: 'fetchAndActivate' },
          {
            action: Actions.PRODUCT_CONFIG_SET_INTERVAL,
            name: 'setMinimumFetchIntervalInSeconds',
          },
          { action: Actions.PRODUCT_CONFIG_GET_BOOL, name: 'getBoolean' },
          { action: Actions.PRODUCT_CONFIG_GET_DOUBLE, name: 'getDouble' },
          { action: Actions.PRODUCT_CONFIG_GET_LONG, name: 'getLong' },
          {
            action: Actions.PRODUCT_CONFIG_GET_STRING,
            name: 'getString',
          },
          { action: Actions.PRODUCT_CONFIG_GET_STRINGS, name: 'getStrings' },
          { action: Actions.PRODUCT_CONFIG_RESET, name: 'reset' },
          ,
          {
            action: Actions.PRODUCT_CONFIG_LAST_FETCH_TS,
            name: 'getLastFetchTimeStampInMillis',
          },
        ],
      },
      {
        expanded: false,
        category_Name: 'Feature Flag',
        sub_Category: [{ action: Actions.GET_FEATURE_FLAG, name: 'getFeatureFlag' }],
      },
      {
        expanded: false,
        category_Name: 'InApp Controls',
        sub_Category: [
          { action: Actions.IN_APPS_SUSPEND, name: 'suspendInAppNotifications' },
          { action: Actions.IN_APPS_DISCARD, name: 'discardInAppNotifications' },
          { action: Actions.IN_APPS_RESUME, name: 'resumeInAppNotifications' },
        ],
      },
      {
        expanded: false,
        category_Name: 'App Personalisation',
        sub_Category: [
          { action: Actions.ENABLE_PERSONALIZATION, name: 'enablePersonalization' }
        ],
      },
      {
        expanded: false,
        category_Name: 'Attributions',
        sub_Category: [
          {
            action: Actions.ATTRIBUTION_IDENTIFIER,
            name: '(Deprecated) get CleverTap Attribution Identifier',
          },
        ],
      },
      {
        expanded: false,
        category_Name: 'GDPR',
        sub_Category: [
          { action: Actions.OPT_OUT, name: 'setOptOut' },
          { action: Actions.ENABLE_NETWORK_INFO, name: 'enableDeviceNetworkInfoReporting' },
        ],
      },
      {
        expanded: false,
        category_Name: 'Listeners',
        sub_Category: [
          { action: Actions.ADD_CLEVERTAP_LISTENERS, name: 'addCleverTapAPIListeners' },
          {
            action: Actions.REMOVE_CLEVERTAP_LISTENERS,
            name: 'removeCleverTapAPIListeners',
          },
        ],
      },
      {
        expanded: false,
        category_Name: 'Custom Templates',
        sub_Category: [
          { action: Actions.SYNC_CUSTOM_TEMPLATES, name: 'Sync Custom Templates' },
          { action: Actions.SYNC_CUSTOM_TEMPLATES_PROD, name: 'Sync Custom Templates In Prod' }
        ],
      },
      {
        expanded: false,
        category_Name: 'Push Templates',
        sub_Category: [
          { action: Actions.RECORD_EVENT, name: 'Send Basic Push' },
          { action: Actions.RECORD_EVENT, name: 'Send Carousel Push' },
          { action: Actions.RECORD_EVENT, name: 'Send Manual Carousel Push' },
          { action: Actions.RECORD_EVENT, name: 'Send Filmstrip Carousel Push' },
          { action: Actions.RECORD_EVENT, name: 'Send Rating Push' },
          { action: Actions.RECORD_EVENT, name: 'Send Product Display Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Linear Product Display Push' },
          { action: Actions.RECORD_EVENT, name: 'Send CTA Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Zero Bezel Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Zero Bezel Text Only Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Timer Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box Notification' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box Reply with Event Notification' },
          {
            action: Actions.RECORD_EVENT,
            name: 'Send Input Box Reply with Auto Open Notification',
          },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box Remind Notification DOC FALSE' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box CTA DOC true' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box CTA DOC false' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box Reminder DOC true' },
          { action: Actions.RECORD_EVENT, name: 'Send Input Box Reminder DOC false' },
        ],
      },
      {
        expanded: false,
        category_Name: 'PROMPT LOCAL IAM',
        sub_Category: [
          { action: Actions.PUSH_PRIMER_HALF_INTERSTITIAL, name: 'Half-Interstitial Local IAM' },
          { action: Actions.PUSH_PRIMER_HALF_INTERSTITIAL_IMAGE, name: 'Half-Interstitial Local IAM with image URL' },
          {
            action: Actions.PUSH_PRIMER_HALF_INTERSTITIAL_FALLBACK,
            name: 'Half-Interstitial Local IAM with fallbackToSettings - true',
          },
          { action: Actions.PUSH_PRIMER_ALERT, name: 'Alert Local IAM' },
          {
            action: Actions.PUSH_PRIMER_ALERT_ORIENTATION,
            name: 'Alert Local IAM with followDeviceOrientation - false',
          },
          { action: Actions.PUSH_PRIMER_ALERT_FALLBACK, name: 'Alert Local IAM with fallbackToSettings - true' },
          {
            action: Actions.HARD_PERMISSION,
            name: 'Hard permission dialog with fallbackToSettings - false',
          },
          {
            action: Actions.HARD_PERMISSION_FALLBACK,
            name: 'Hard permission dialog with fallbackToSettings - true',
          },
        ],
      },
    ];
  }

  onItemPress = (item) => {
    switch (item.action) {
      case Actions.SET_USER_PROFILE:
        utils.set_userProfile();
        break;
      case Actions.SET_MULTI_VALUES:
        CleverTap.profileSetMultiValuesForKey(['a', 'b', 'c'], 'letters');
        break;
      case Actions.REMOVE_MULTI_VALUE:
        CleverTap.profileRemoveMultiValueForKey('b', 'letters');
        break;
      case Actions.ADD_MULTI_VALUE:
        CleverTap.profileAddMultiValueForKey('d', 'letters');
        break;
      case Actions.INCREMENT_VALUE:
        CleverTap.profileIncrementValueForKey(10, 'score');
        CleverTap.profileIncrementValueForKey(3.141, 'PI_Float');
        CleverTap.profileIncrementValueForKey(
          3.141592653589793,
          'PI_Double',
        );
        break;
      case Actions.DECREMENT_VALUE:
        CleverTap.profileDecrementValueForKey(10, 'score');
        CleverTap.profileDecrementValueForKey(3.141, 'PI_Float');
        CleverTap.profileDecrementValueForKey(
          3.141592653589793,
          'PI_Double',
        );
        break;
      case Actions.USER_LOGIN:
        utils.onUser_Login();
        break;
      case Actions.CLEVERTAP_ID:
        utils.getCleverTap_id();
        break;
      case Actions.USER_LOCATION:
        utils.set_userLocation();
        break;
      case Actions.USER_LOCALE:
        utils.set_Locale();
        break;
      case Actions.INITIALIZE_INBOX:
        CleverTap.initializeInbox();
        break;
      case Actions.SHOW_INBOX:
        utils.show_appInbox();
        break;
      case Actions.SHOW_INBOX_TABS:
        utils.show_appInboxwithTabs();
        break;
      case Actions.INBOX_TOTAL_MESSAGE_COUNT:
        utils.get_TotalMessageCount();
        break;
      case Actions.INBOX_UNREAD_MESSAGE_COUNT:
        utils.get_UnreadMessageCount();
        break;
      case Actions.INBOX_ALL_MESSAGES:
        utils.Get_All_InboxMessages();
        break;
      case Actions.INBOX_UNREAD_MESSAGES:
        utils.get_All_InboxUnreadMessages();
        break;
      case Actions.INBOX_MESSAGE_FOR_ID:
        utils.Get_InboxMessageForId();
        break;
      case Actions.INBOX_DELETE_MESSAGE_FOR_ID:
        utils.delete_InboxMessageForId();
        break;
      case Actions.INBOX_READ_MESSAGE_FOR_ID:
        utils.markRead_InboxMessageForId();
        break;
      case Actions.INBOX_NOTIFICATION_VIEWED:
        utils.pushInboxNotificationViewed();
        break;
      case Actions.INBOX_NOTIFICATION_CLICKED:
        utils.pushInboxNotificationClicked();
        break;
      case Actions.PUSH_EVENT:
        utils.pushevent();
        break;
      case Actions.PUSH_CHARGED_EVENT:
        utils.pushChargedEvent();
        break;
      case Actions.SET_DEBUG:
        CleverTap.setDebugLevel(3);
        break;
      case Actions.CREATE_NOTIFICATION_GROUP:
        utils.create_NotificationChannelGroup();
        break;
      case Actions.CREATE_NOTIFICATION_CHANNEL:
        utils.create_NotificationChannel();
        break;
      case Actions.DELETE_NOTIFICATION_CHANNEL:
        utils.delete_NotificationChannel();
        break;
      case Actions.DELETE_NOTIFICATION_GROUP:
        utils.delete_NotificationChannelGroup();
        break;
      case Actions.PUSH_FCM:
        utils.pushFcmRegistrationId();
        break;
      case Actions.CREATE_NOTIFICATION:
        utils.create_notification();
        break;
      case Actions.CREATE_NOTIFICATION_CHANNEL_WITH_SOUND:
        utils.createNotificationChannelWithSound();
        break;
      case Actions.CREATE_NOTIFICATION_CHANNEL_WITH_GROUP:
        utils.createNotificationChannelWithGroupId();
        break;
      case Actions.CREATE_NOTIFICATION_CHANNEL_WITH_GROUP_AND_SOUND:
        utils.createNotificationChannelWithGroupIdAndSound();
        break;
      case Actions.DISPLAY_UNIT_ID:
        utils.getUnitID();
        break;
      case Actions.ALL_DISPLAY_UNITS:
        utils.getAllDisplayUnits();
        break;
      case Actions.PRODUCT_CONFIG_FETCH:
        utils.fetch();
        break;
      case Actions.PRODUCT_CONFIG_ACTIVATE:
        utils.activate();
        break;
      case Actions.PRODUCT_CONFIG_FETCH_AND_ACTIVATE:
        utils.fetchAndActivate();
        break;
      case Actions.PRODUCT_CONFIG_FETCH_INTERVAL:
        utils.fetchwithMinIntervalinsec();
        break;
      case Actions.PRODUCT_CONFIG_SET_INTERVAL:
        utils.setMinimumFetchIntervalInSeconds();
        break;
      case Actions.PRODUCT_CONFIG_GET_BOOL:
        utils.getBoolean();
        break;
      case Actions.PRODUCT_CONFIG_GET_DOUBLE:
        utils.getDouble();
        break;
      case Actions.PRODUCT_CONFIG_GET_LONG:
        utils.getLong();
        break;
      case Actions.PRODUCT_CONFIG_GET_STRING:
        utils.getString();
        break;
      case Actions.PRODUCT_CONFIG_GET_STRINGS:
        utils.getStrings();
        break;
      case Actions.PRODUCT_CONFIG_RESET:
        utils.reset_config();
        break;
      case Actions.PRODUCT_CONFIG_LAST_FETCH_TS:
        utils.getLastFetchTimeStampInMillis();
        break;
      case Actions.GET_FEATURE_FLAG:
        utils.getFeatureFlag();
        break;
      case Actions.IN_APPS_SUSPEND:
        CleverTap.suspendInAppNotifications();
        break;
      case Actions.IN_APPS_DISCARD:
        CleverTap.discardInAppNotifications();
        break;
      case Actions.IN_APPS_RESUME:
        CleverTap.resumeInAppNotifications();
        break;
      case Actions.ENABLE_PERSONALIZATION:
        utils.enablePersonalization();
        break;
      case Actions.ATTRIBUTION_IDENTIFIER:
        utils.GetCleverTapAttributionIdentifier();
        break;
      case Actions.OPT_OUT:
        CleverTap.setOptOut(false);
        break;
      case Actions.ENABLE_NETWORK_INFO:
        CleverTap.enableDeviceNetworkInfoReporting(true);
        break;
      case Actions.ADD_CLEVERTAP_LISTENERS:
        utils.addCleverTapAPIListeners(true);
        break;
      case Actions.REMOVE_CLEVERTAP_LISTENERS:
        utils.removeCleverTapAPIListeners();
        break;
      case Actions.RECORD_EVENT:
        console.log('name: ', item.name);
        CleverTap.recordEvent(item.name);
        break;
      case Actions.PUSH_PRIMER_HALF_INTERSTITIAL:
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
      case Actions.PUSH_PRIMER_HALF_INTERSTITIAL_IMAGE:
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
                'https://clevertap.com/wp-content/themes/clevertap2023/assets/images/ct-logo-2.svg',
              btnBorderRadius: '2',
            });
          }
        });
        break;
      case Actions.PUSH_PRIMER_HALF_INTERSTITIAL_FALLBACK:
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
      case Actions.PUSH_PRIMER_ALERT:
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
      case Actions.PUSH_PRIMER_ALERT_ORIENTATION:
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
      case Actions.PUSH_PRIMER_ALERT_FALLBACK:
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
      case Actions.HARD_PERMISSION:
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
      case Actions.HARD_PERMISSION_FALLBACK:
        CleverTap.isPushPermissionGranted((err, res) => {
          console.log('isPushPermissionGranted', res, err);
          if (res == false) {
            CleverTap.promptForPushPermission(true);
          }
          else {
            alert('Push Notification permission is already granted');
          }
        });
        break;
      case Actions.SYNC_VARIABLES:
        CleverTap.syncVariables()
        break;
      case Actions.GET_VARIABLES:
        CleverTap.getVariables((err, variables) => {
          console.log('getVariables: ', variables, err);
        });
        break;
      case Actions.GET_VARIABLE:
        CleverTap.getVariable('reactnative_var_string', (err, variable) => {
          console.log(`variable value for key \'reactnative_var_string\': ${variable}`);
        });
        break;
      case Actions.DEFINE_VARIABLES:
        let variables = {
          'reactnative_var_string': 'reactnative_var_string_value',
          'reactnative_var_map': {
            'reactnative_var_map_string': 'reactnative_var_map_value'
          },
          'reactnative_var_int': 6,
          'reactnative_var_float': 6.9,
          'reactnative_var_boolean': true
        };
        console.log(`Creating variables: ${JSON.stringify(variables)}`);
        CleverTap.defineVariables(variables);
        break;
      case Actions.FETCH_VARIABLES:
        CleverTap.fetchVariables((err, success) => {
          console.log('fetchVariables result: ', success);
        });
        break;
      case Actions.VARIABLES_CHANGED:
        CleverTap.onVariablesChanged((variables) => {
          console.log('onVariablesChanged: ', variables);
        });
        break;
      case Actions.VALUE_CHANGED:
        CleverTap.onValueChanged('reactnative_var_string', (variable) => {
          console.log('onValueChanged: ', variable);
        });
        break;
      case Actions.FETCH_INAPPS:
        CleverTap.fetchInApps((err, success) => {
          console.log('fetchInApps result: ', success);
        });
        break;
      case Actions.CLEAR_INAPPS:
        CleverTap.clearInAppResources(false);
        break;
      case Actions.CLEAR_INAPPS_EXPIRED:
        CleverTap.clearInAppResources(true);
        break;
      case Actions.SYNC_CUSTOM_TEMPLATES:
        CleverTap.syncCustomTemplates();
        break;
      case Actions.SYNC_CUSTOM_TEMPLATES_PROD:
        CleverTap.syncCustomTemplatesInProd(true);
        break;
      default:
        console.warn('Action not recognized:', item.action);
    }
  }

  updateLayout = (index) => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    const array = [...this.state.AccordionData];
    array[index].expanded = !array[index].expanded;
    this.setState({ AccordionData: array });
  };

  render() {
    return (
      <View style={styles.mainContainer}>
        <ScrollView contentContainerStyle={{ paddingHorizontal: 8, paddingVertical: 5 }}>
          <TouchableOpacity style={styles.header}>
            <Text style={styles.headerText}>CleverTap Example</Text>
          </TouchableOpacity>
          <CustomTemplate />
          {this.state.AccordionData.map((item, key) => (
            <ExpandableListView
              key={item.category_Name}
              onToggleView={() => this.updateLayout(key)}
              onItemPress={this.onItemPress}
              item={item}
            />
          ))}
        </ScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  mainContainer: {
    flex: 1,
    justifyContent: 'center',
    paddingTop: Platform.OS === 'ios' ? 44 : 0,
    backgroundColor: '#fff',
  },
  header: {
    backgroundColor: '#fff',
    flexWrap: 'wrap',
    color: '#fff',
    fontSize: 44,
    padding: 10,
  },
  headerText: {
    width: '100%',
    textAlign: 'center',
    color: '#000',
    fontWeight: 'bold',
    fontSize: 26,
  }
});