
import React, { Component } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  StyleSheet
} from 'react-native';
import { Actions } from './constants'; // Importing constants
import * as utils from './utils';

const CleverTap = require('clevertap-react-native');

export class Expandable_ListView extends Component {
    constructor() {
      super();
      this.state = {
        layout_Height: 0,
      };
    }
  
    UNSAFE_componentWillReceiveProps(nextProps) {
      if (nextProps.item.expanded) {
        this.setState({ layout_Height: null });
      } else {
        this.setState({ layout_Height: 0 });
      }
    }
  
    shouldComponentUpdate(nextProps, nextState) {
      return this.state.layout_Height !== nextState.layout_Height;
    }
  
    show_Selected_Category = (item) => {
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
  
    render() {
      return (
        <View style={styles.Panel_Holder}>
          <TouchableOpacity
            activeOpacity={0.8}
            onPress={this.props.onClickFunction}
            style={styles.category_View}>
            <Text style={styles.category_Text}>
              {this.props.item.category_Name}
            </Text>
            <Image
              source={{
                uri: 'https://reactnativecode.com/wp-content/uploads/2019/02/arrow_right_icon.png',
              }}
              style={styles.iconStyle}
            />
  
          </TouchableOpacity>
          <View style={{ height: this.state.layout_Height, overflow: 'hidden' }}>
            {this.props.item.sub_Category.map((item, key) => (
              <TouchableOpacity
                key={key}
                style={styles.sub_Category_Text}
                onPress={() => this.show_Selected_Category(item)}>
                <Text style={styles.setSubCategoryFontSizeOne}>{item.name}</Text>
                <View style={{ width: '100%', height: 1, backgroundColor: '#000' }} />
              </TouchableOpacity>
            ))}
          </View>
        </View>
      );
    }
  };

  const styles = StyleSheet.create({
    iconStyle: {
      width: 22,
      height: 22,
      justifyContent: 'flex-end',
      alignItems: 'center',
      tintColor: '#fff',
    },
    sub_Category_Text: {
      fontSize: 20,
      color: '#000',
      padding: 10,
    },
    category_Text: {
      textAlign: 'left',
      color: '#fff',
      fontSize: 22,
      padding: 12,
    },
    category_View: {
      marginVertical: 5,
      flexDirection: 'row',
      justifyContent: 'space-between',
      alignItems: 'center',
      backgroundColor: '#DC2626',
    },
    setSubCategoryFontSizeOne: {
      fontSize: 18,
    },
  });