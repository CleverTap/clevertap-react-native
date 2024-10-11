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
import { Expandable_ListView } from './ExpandableListView';
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

  update_Layout = (index) => {
    LayoutAnimation.configureNext(LayoutAnimation.Presets.easeInEaseOut);
    const array = [...this.state.AccordionData];
    array[index].expanded = !array[index].expanded;
    this.setState({ AccordionData: array });
  };

  render() {
    return (
      <View style={styles.MainContainer}>
        <ScrollView contentContainerStyle={{ paddingHorizontal: 8, paddingVertical: 5 }}>
          <TouchableOpacity style={styles.button}>
            <Text style={styles.button_Text}>CleverTap Example</Text>
          </TouchableOpacity>
          <CustomTemplate />
          {this.state.AccordionData.map((item, key) => (
            <Expandable_ListView
              key={item.category_Name}
              onClickFunction={() => this.update_Layout(key)}
              item={item}
            />
          ))}
        </ScrollView>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  MainContainer: {
    flex: 1,
    justifyContent: 'center',
    paddingTop: Platform.OS === 'ios' ? 44 : 0,
    backgroundColor: '#fff',
  },
  
  button: {
    backgroundColor: '#fff',
    flexWrap: 'wrap',
    color: '#fff',
    fontSize: 44,
    padding: 10,
  },

  button_Text: {
    width: '100%',
    textAlign: 'center',
    color: '#000',
    fontWeight: 'bold',
    fontSize: 26,
  }
});