import { DeviceEventEmitter, NativeEventEmitter, NativeModules } from 'react-native';

const CleverTapReact = NativeModules.CleverTapReact;
const EventEmitter = NativeModules.CleverTapReactEventEmitter ? new NativeEventEmitter(NativeModules.CleverTapReactEventEmitter) : DeviceEventEmitter;

/**
* Set the CleverTap React-Native library name with current version
* @param {string} libName - Library name will be "React-Native"
* @param {int} libVersion - The updated library version. If current version is 1.1.0 then pass as 10100  
*/
const libName = 'React-Native';
const libVersion = 10101; 
CleverTapReact.setLibrary(libName,libVersion);

function defaultCallback(method, err, res) {
    if (err) {
        console.log('CleverTap ' + method + ' default callback error', err);
    } else {
        console.log('CleverTap ' + method + ' default callback result', res);
    }
}

/**
* Calls a CleverTap method with a default callback if necessary
* @param {string} method - the CleverTap method name as a string
* @param {array} args - The method args
* @param {function(err, res)} callback - callback
*/
function callWithCallback(method, args, callback) {
    if (typeof callback === 'undefined' || callback == null || typeof callback !== 'function') {
        callback = (err, res) => {
            defaultCallback(method, err, res);
        };
    }

    if (args == null) {
        args = [];
    }

    args.push(callback);

    CleverTapReact[method].apply(this, args);
}

var CleverTap = {

    CleverTapProfileDidInitialize: CleverTapReact.CleverTapProfileDidInitialize,
    CleverTapProfileSync: CleverTapReact.CleverTapProfileSync,
    CleverTapInAppNotificationDismissed: CleverTapReact.CleverTapInAppNotificationDismissed,
    CleverTapInAppNotificationShowed: CleverTapReact.CleverTapInAppNotificationShowed,
    FCM: CleverTapReact.FCM,
    XPS: CleverTapReact.XPS,
    BPS: CleverTapReact.BPS,
    HPS: CleverTapReact.HPS,
    CleverTapInboxDidInitialize: CleverTapReact.CleverTapInboxDidInitialize,
    CleverTapInboxMessagesDidUpdate: CleverTapReact.CleverTapInboxMessagesDidUpdate,
    CleverTapInboxMessageButtonTapped: CleverTapReact.CleverTapInboxMessageButtonTapped,
    CleverTapInboxMessageTapped: CleverTapReact.CleverTapInboxMessageTapped,
    CleverTapDisplayUnitsLoaded: CleverTapReact.CleverTapDisplayUnitsLoaded,
    CleverTapInAppNotificationButtonTapped: CleverTapReact.CleverTapInAppNotificationButtonTapped,
    CleverTapFeatureFlagsDidUpdate: CleverTapReact.CleverTapFeatureFlagsDidUpdate, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidInitialize: CleverTapReact.CleverTapProductConfigDidInitialize, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidFetch: CleverTapReact.CleverTapProductConfigDidFetch, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidActivate: CleverTapReact.CleverTapProductConfigDidActivate, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapPushNotificationClicked: CleverTapReact.CleverTapPushNotificationClicked,
    CleverTapPushPermissionResponseReceived: CleverTapReact.CleverTapPushPermissionResponseReceived,
    CleverTapOnVariablesChanged: CleverTapReact.CleverTapOnVariablesChanged,
    CleverTapOnValueChanged: CleverTapReact.CleverTapOnValueChanged,

    /**
    * Add a CleverTap event listener
    * supported events are CleverTap.CleverTapProfileDidInitialize, CleverTap.CleverTapProfileSync,CleverTap.CleverTapOnInboxButtonClick
    * ,CleverTap.CleverTapOnInAppButtonClick,CleverTap.CleverTapOnDisplayUnitsLoaded and CleverTap.CleverTapInAppNotificationDismissed
    * @param {string} eventName - the CleverTap event name
    * @param {function(event)} your event handler
    */
    addListener: function (eventName, handler) {
        if (EventEmitter) {
            EventEmitter.addListener(eventName, handler);
        }
    },

    /**
    * Removes all of the registered listeners for given eventName.
    *
    * @param {string} eventName -  name of the event whose registered listeners to remove
    */
    removeListener: function (eventName) {
        if (EventEmitter) {
            EventEmitter.removeAllListeners(eventName);
        }
    },

    /**
    *  @deprecated - Since version 0.5.0. Use removeListener(eventName) instead
    *  Remove all event listeners
    */
    removeListeners: function () {
        if (DeviceEventEmitter) {
            DeviceEventEmitter.removeAllListeners();
        }
    },

    /**
    * If an application is launched from a push notification click, returns the CleverTap deep link included in the push notification
    * @param {function(err, res)} callback that return the url as string in res or a string error in err
    */
    getInitialUrl: function (callback) {
        callWithCallback('getInitialUrl', null, callback);
    },

    /**
    * Registers the application to receive push notifications
    * only necessary for iOS.
    */
    registerForPush: function () {
        CleverTapReact.registerForPush();
    },

    /**
     * Manually set the push token on the CleverTap user profile
     * @param {string} token - the device token
     * @param {string} type - for Android only, specifying the type of push service token. Values can be CleverTap.FCM for Firebase or CleverTap.XPS for Xiaomi or CleverTap.BPS for Baidu or CleverTap.HPS for Huawei,
     * @param {string} region - for xps only ,to specify the region
     */
    setPushToken: function (token, type, region = "") {
        console.log(`CleverTap RN | setPushToken | received : token: '${token}' | type:'${type}' | region:'${region}' `)
        if (type === CleverTap.XPS) {
            CleverTapReact.setPushTokenAsStringWithRegion(token, type, region);
        }
        else {
            CleverTapReact.setPushTokenAsString(token, type);
        }
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannel: function (channelId, channelName, channelDescription, importance, showBadge) {
        CleverTapReact.createNotificationChannel(channelId, channelName, channelDescription, importance, showBadge);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    * @param {string} sound - A String for setting the custom sound of the notification channel
    */
    createNotificationChannelWithSound: function (channelId, channelName, channelDescription, importance, showBadge, sound) {
        CleverTapReact.createNotificationChannelWithSound(channelId, channelName, channelDescription, importance, showBadge, sound);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {string} groupId - A String for setting the notification channel as a part of a notification group
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannelWithGroupId: function (channelId, channelName, channelDescription, importance, groupId, showBadge) {
        CleverTapReact.createNotificationChannelWithGroupId(channelId, channelName, channelDescription, importance, groupId, showBadge);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {string} groupId - A String for setting the notification channel as a part of a notification group
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    * @param {string} sound - A String for setting the custom sound of the notification channel
    */
    createNotificationChannelWithGroupIdAndSound: function (channelId, channelName, channelDescription, importance, groupId, showBadge, sound) {
        CleverTapReact.createNotificationChannelWithGroupIdAndSound(channelId, channelName, channelDescription, importance, groupId, showBadge, sound);
    },

    /**
    * Method to create Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    * @param {string} groupName - A String for setting the name of the notification channel group
    */
    createNotificationChannelGroup: function (groupId, groupName) {
        CleverTapReact.createNotificationChannelGroup(groupId, groupName);
    },

    /**
    * Method to delete Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    */
    deleteNotificationChannel: function (channelId) {
        CleverTapReact.deleteNotificationChannel(channelId);
    },

    /**
    * Method to delete Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    */
    deleteNotificationChannelGroup: function (groupId) {
        CleverTapReact.deleteNotificationChannelGroup(groupId);
    },

    /**
    * Method to show the App Inbox
    * @param {object} extras - key-value data from RemoteMessage.getData().  keys and values are strings
    */
    createNotification: function (extras) {
        CleverTapReact.createNotification(extras);
    },

    /**
    * Method to prompt the hard permission dialog directly, if the push primer is not required.
     * @param {string} showFallbackSettings - If the value is true then SDK shows an alert dialog which routes to app's notification settings page.
    */
    promptForPushPermission: function (showFallbackSettings) {
        CleverTapReact.promptForPushPermission(showFallbackSettings);
    },

    /**
    * Method to prompt the push primer for android 13 onwards.
    * @param {object} value - key-value belongs to the localInApp properties. Refer documentation for details.
    */
    promptPushPrimer: function (value) {
        CleverTapReact.promptPushPrimer(value);
    },

    /**
    * Returns true/false based on whether push permission is granted or denied.
    *
    * @param {function(err, res)} non-null callback to retrieve the result
    */
    isPushPermissionGranted: function (callback) {
        callWithCallback('isPushPermissionGranted', null, callback);
    },

    /**
    * Enables tracking opt out for the currently active user.
    * @param {boolean} value - A boolean for enabling or disabling tracking for current user
    */
    setOptOut: function (value) {
        CleverTapReact.setOptOut(value);
    },

    /**
    * Sets the CleverTap SDK to offline mode
    * @param {boolean} value - A boolean for enabling or disabling sending events for current user
    */
    setOffline: function (value) {
        CleverTapReact.setOffline(value);
    },

    /**
    * Enables the reporting of device network related information, including IP address. This reporting is disabled by default.
    * @param {boolean} - A boolean for enabling or disabling device network related information to be sent to CleverTap
    */
    enableDeviceNetworkInfoReporting: function (value) {
        CleverTapReact.enableDeviceNetworkInfoReporting(value);
    },

    /**
    * Enables the personalization API.  Call this prior to using the profile/event API getters
    */
    enablePersonalization: function () {
        CleverTapReact.enablePersonalization();
    },

    /**
    * Disables the personalization API.
    */
    disablePersonalization: function () {
        CleverTapReact.disablePersonalization();
    },

    /**
    * Record a Screen View
    * @param {string} screenName - the name of the screen
    */
    recordScreenView: function (screenName) {
        CleverTapReact.recordScreenView(screenName);
    },

    /**
    * Record an event with optional event properties
    * @param {string} eventName - the name of the event
    * @param {object} props - the key-value properties of the event.
    * keys are strings and values can be string, number or boolean.
    */
    recordEvent: function (eventName, props) {
        convertDateToEpochInProperties(props);
        CleverTapReact.recordEvent(eventName, props);
    },

    /**
    * Record the special Charged event
    * @param {object} details - the key-value properties for the transaction.
    * @param {array<object>} items - an array of objects containing the key-value data for the items that make up the transaction.
    */
    recordChargedEvent: function (details, items) {
        convertDateToEpochInProperties(details);
        if (Array.isArray(items) && items.length) {
            items.forEach(value => {
                convertDateToEpochInProperties(value);
            });
        }
        CleverTapReact.recordChargedEvent(details, items);
    },

    /**
    * Get the time of the first occurrence of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetFirstTime: function (eventName, callback) {
        callWithCallback('eventGetFirstTime', [eventName], callback);
    },

    /**
    * Get the time of the most recent occurrence of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetLastTime: function (eventName, callback) {
        callWithCallback('eventGetLastTime', [eventName], callback);
    },

    /**
    * Get the number of occurrences of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of int
    */
    eventGetOccurrences: function (eventName, callback) {
        callWithCallback('eventGetOccurrences', [eventName], callback);
    },

    /**
    * Get the summary details of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of object {"eventName": <string>, "firstTime":<epoch seconds>, "lastTime": <epoch seconds>, "count": <int>} or empty object
    */
    eventGetDetail: function (eventName, callback) {
        callWithCallback('eventGetDetail', [eventName], callback);
    },

    /**
    * Get the user's event history
    * @param {function(err, res)} callback that returns a res of object {"eventName1":<event1 details object>, "eventName2":<event2 details object>}
    */
    getEventHistory: function (callback) {
        callWithCallback('getEventHistory', null, callback);
    },

    /**
    * Set the user's location as a latitude,longitude coordinate
    * @param {float} latitude
    * @param {float} longitude
    */
    setLocation: function (latitude, longitude) {
        CleverTapReact.setLocation(latitude, longitude);
    },

    /**
     * @deprecated - Since version 0.6.0. Use getCleverTapID(callback) instead
    * Get a unique CleverTap identifier suitable for use with install attribution providers
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapAttributionIdentifier: function (callback) {
        callWithCallback('profileGetCleverTapAttributionIdentifier', null, callback);
    },

    /**
     * @deprecated - Since version 0.6.0. Use getCleverTapID(callback) instead
    * Get the user profile's CleverTap identifier value
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapID: function (callback) {
        callWithCallback('profileGetCleverTapID', null, callback);
    },

    /**
    * Creates a separate and distinct user profile identified by one or more of Identity, Email, FBID or GPID values, and populated with the key-values included in the profile dictionary.
    * If your app is used by multiple users, you can use this method to assign them each a unique profile to track them separately.
    * If instead you wish to assign multiple Identity, Email, FBID and/or GPID values to the same user profile, use profileSet rather than this method.
    * If none of Identity, Email, FBID or GPID is included in the profile object, all properties values will be associated with the current user profile.
    * When initially installed on this device, your app is assigned an "anonymous" profile.
    * The first time you identify a user on this device (whether via onUserLogin or profileSet), the "anonymous" history on the device will be associated with the newly identified user.
    * Then, use this method to switch between subsequent separate identified users.
    * Please note that switching from one identified user to another is a costly operation
    * in that the current session for the previous user is automatically closed
    * and data relating to the old user removed, and a new session is started
    * for the new user and data for that user refreshed via a network call to CleverTap.
    * In addition, any global frequency caps are reset as part of the switch.
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    onUserLogin: function (profile) {
        convertDateToEpochInProperties(profile);
        CleverTapReact.onUserLogin(profile);
    },

    /**
    * Set key-value properties on a user profile
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    profileSet: function (profile) {
        convertDateToEpochInProperties(profile);
        CleverTapReact.profileSet(profile);
    },

    /**
    * Get the value of a profile property
    * @param {string} the property key
    * @param {function(err, res)} callback that returns a res of the property value or null
    */
    profileGetProperty: function (key, callback) {
        callWithCallback('profileGetProperty', [key], callback);
    },

    /**
    * Remove a key-value from the user profile. Alternatively this method can also be used to remove PII data
    * (for eg. Email,Name,Phone), locally from database and shared prefs
    * @param {string} the key to remove
    */
    profileRemoveValueForKey: function (key) {
        CleverTapReact.profileRemoveValueForKey(key);
    },

    /**
    * Set an array of strings as a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileSetMultiValuesForKey: function (values, key) {
        CleverTapReact.profileSetMultiValues(values, key);
    },

    /**
    * Add a string value to a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileAddMultiValueForKey: function (value, key) {
        CleverTapReact.profileAddMultiValue(value, key);
    },

    /**
    * Add an array of strings to a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileAddMultiValuesForKey: function (values, key) {
        CleverTapReact.profileAddMultiValues(values, key);
    },

    /**
    * Remove a string value from a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileRemoveMultiValueForKey: function (value, key) {
        CleverTapReact.profileRemoveMultiValue(value, key);
    },

    /**
    * Remove an array of strings from a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileRemoveMultiValuesForKey: function (values, key) {
        CleverTapReact.profileRemoveMultiValues(values, key);
    },

    /**
    * This method is used to increment the given value
    *
    * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
    * @param key   {string} profile property
    */
    profileIncrementValueForKey: function (value, key) {
        CleverTapReact.profileIncrementValueForKey(value, key);
    },

    /**
     * This method is used to decrement the given value
     *
     * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
     * @param key   {string} profile property
     */
    profileDecrementValueForKey: function (value, key) {
        CleverTapReact.profileDecrementValueForKey(value, key);
    },

    /**
    * Manually track the utm app install referrer
    * @param {string} the utm referrer source
    * @param {string} the utm referrer medium
    * @param {string} the utm referrer campaign
    */
    pushInstallReferrer: function (source, medium, campaign) {
        CleverTapReact.pushInstallReferrer(source, medium, campaign);
    },

    /**
    * Get the elapsed time of the current user session
    * @param {function(err, res)} callback that returns a res of int seconds
    */
    sessionGetTimeElapsed: function (callback) {
        callWithCallback('sessionGetTimeElapsed', null, callback);
    },

    /**
    * Get the total number of vists by the user
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetTotalVisits: function (callback) {
        callWithCallback('sessionGetTotalVisits', null, callback);
    },

    /**
    * Get the number of screens viewed by the user during the session
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetScreenCount: function (callback) {
        callWithCallback('sessionGetScreenCount', null, callback);
    },

    /**
    * Get the most recent previous visit time of the user
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    sessionGetPreviousVisitTime: function (callback) {
        callWithCallback('sessionGetPreviousVisitTime', null, callback);
    },

    /**
    * Get the utm referrer info for the current session
    * @param {function(err, res)} callback that returns a res of object {"source": <string>, "medium": <string>, "campaign": <string>} or empty object
    */
    sessionGetUTMDetails: function (callback) {
        callWithCallback('sessionGetUTMDetails', null, callback);
    },

    /**
    * Method to initalize the App Inbox
    */
    initializeInbox: function () {
        CleverTapReact.initializeInbox();
    },

    /**
    * Method to show the App Inbox
    * @param {object} styleConfig - key-value profile properties.  keys and values are strings
    */
    showInbox: function (styleConfig) {
        CleverTapReact.showInbox(styleConfig);
    },

    /**
     * Method to dismiss the App Inbox
     */
    dismissInbox: function () {
        CleverTapReact.dismissInbox();
    },

    /**
     * Get the total number of Inbox Messages
     * @param {function(err, res)} callback that returns a res of count of inbox messages or -1
     */
    getInboxMessageCount: function (callback) {
        callWithCallback('getInboxMessageCount', null, callback);
    },

    /**
     * Get the total number of Unread Inbox Messages
     * @param {function(err, res)} callback that returns a res of count of unread inbox messages or -1
     */
    getInboxMessageUnreadCount: function (callback) {
        callWithCallback('getInboxMessageUnreadCount', null, callback);
    },

    /**
     * Get All inbox messages
     * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
     */
    getAllInboxMessages: function (callback) {
        callWithCallback('getAllInboxMessages', null, callback);
    },

    /**
     * Get All unread inbox messages
     * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
     */
    getUnreadInboxMessages: function (callback) {
        callWithCallback('getUnreadInboxMessages', null, callback);
    },

    /**
     * Get Inbox Message that belongs to the given message id
     * @param {function(err, res)} callback that returns json string representation of CTInboxMessage
     */
    getInboxMessageForId: function (messageId, callback) {
        callWithCallback('getInboxMessageForId', [messageId], callback);
    },

    /**
     * Deletes Inbox Message that belongs to the given message id
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    deleteInboxMessageForId: function (messageId) {
        CleverTapReact.deleteInboxMessageForId(messageId);
    },

    /**
     * Deletes multiple Inbox Messages that belongs to the given message ids
     * @param {array} messageIds a collection of ids of inbox messages
     */
    deleteInboxMessagesForIDs: function (messageIds) {
        CleverTapReact.deleteInboxMessagesForIDs(messageIds);
    },

    /**
     * Marks Inbox Message that belongs to the given message id as read
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    markReadInboxMessageForId: function (messageId) {
        CleverTapReact.markReadInboxMessageForId(messageId);
    },

    /**
     * Marks multiple Inbox Messages that belongs to the given message ids as read
     * @param {array} messageIds a collection of ids of inbox messages
     */
    markReadInboxMessagesForIDs: function (messageIds) {
        CleverTapReact.markReadInboxMessagesForIDs(messageIds);
    },

    /**
     * Pushes the Notification Clicked event for App Inbox to CleverTap.
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    pushInboxNotificationClickedEventForId: function (messageId) {
        CleverTapReact.pushInboxNotificationClickedEventForId(messageId);
    },

    /**
     * Pushes the Notification Viewed event for App Inbox to CleverTap.
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    pushInboxNotificationViewedEventForId: function (messageId) {
        CleverTapReact.pushInboxNotificationViewedEventForId(messageId);
    },

    /**
     * Get all display units
     * @param {function(err, res)} callback that returns a list of json string representation of CleverTapDisplayUnit
     */
    getAllDisplayUnits: function (callback) {
        callWithCallback('getAllDisplayUnits', null, callback);
    },

    /**
     * Get display unit for given unitID.
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     * @param {function(err, res)} callback that returns a json string representation of CleverTapDisplayUnit
     */
    getDisplayUnitForId: function (unitID, callback) {
        callWithCallback('getDisplayUnitForId', [unitID], callback);
    },

    /**
     * Raises the Display Unit Viewed event
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     */
    pushDisplayUnitViewedEventForID: function (unitID) {
        CleverTapReact.pushDisplayUnitViewedEventForID(unitID);
    },

    /**
     * Raises the Display Unit Clicked event
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     */
    pushDisplayUnitClickedEventForID: function (unitID) {
        CleverTapReact.pushDisplayUnitClickedEventForID(unitID);
    },


    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Sets default product config params using the given object.
     * @param {object} productConfigMap - key-value product config properties.  keys are strings and values can be string, double, integer, boolean or json in string format.
     */
    setDefaultsMap: function (productConfigMap) {
        CleverTapReact.setDefaultsMap(productConfigMap);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Starts fetching product configs, adhering to the default minimum fetch interval.
     */
    fetch: function () {
        CleverTapReact.fetch();
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Starts fetching product configs, adhering to the specified minimum fetch interval in seconds.
     * @param {int} intervalInSecs - minimum fetch interval in seconds.
     */
    fetchWithMinimumIntervalInSeconds: function (intervalInSecs) {
        CleverTapReact.fetchWithMinimumFetchIntervalInSeconds(intervalInSecs);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Activates the most recently fetched product configs, so that the fetched key value pairs take effect.
     */
    activate: function () {
        CleverTapReact.activate();
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Asynchronously fetches and then activates the fetched product configs.
     */
    fetchAndActivate: function () {
        CleverTapReact.fetchAndActivate();
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Sets the minimum interval in seconds between successive fetch calls.
     * @param {int} intervalInSecs - interval in seconds between successive fetch calls.
     */
    setMinimumFetchIntervalInSeconds: function (intervalInSecs) {
        CleverTapReact.setMinimumFetchIntervalInSeconds(intervalInSecs);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Deletes all activated, fetched and defaults configs as well as all Product Config settings.
     */
    resetProductConfig: function () {
        CleverTapReact.reset();
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     *
     * Returns the product config parameter value for the given key as a String.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type string if present else blank
     */
    getProductConfigString: function (key, callback) {
        callWithCallback('getString', [key], callback);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the product config parameter value for the given key as a boolean.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type boolean if present else false
     */
    getProductConfigBoolean: function (key, callback) {
        callWithCallback('getBoolean', [key], callback);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the product config parameter value for the given key as a number.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type number if present else 0
     */
    getNumber: function (key, callback) {
        callWithCallback('getDouble', [key], callback);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the last fetched timestamp in millis.
     * @param {function(err, res)} callback that returns value of timestamp in millis as a string.
     */
    getLastFetchTimeStampInMillis: function (callback) {
        callWithCallback('getLastFetchTimeStampInMillis', null, callback);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Getter to return the feature flag configured at the dashboard
     * @param {string} key of the feature flag
     * @param {string} default value of the key, in case we don't find any feature flag with the key.
     * @param {function(err, res)} callback that returns a feature flag value of type boolean if present else provided default value
     */
    getFeatureFlag: function (name, defaultValue, callback) {
        callWithCallback('getFeatureFlag', [name, defaultValue], callback);
    },

    /**
     * Returns a unique identifier through callback by which CleverTap identifies this user
     *
     * @param {function(err, res)} non-null callback to retrieve identifier
     */
    getCleverTapID: function (callback) {
        callWithCallback('getCleverTapID', null, callback);
    },

    /**
     * Suspends display of InApp Notifications.
     * The InApp Notifications are queued once this method is called
     * and will be displayed once resumeInAppNotifications() is called.
     */
    suspendInAppNotifications: function () {
        CleverTapReact.suspendInAppNotifications();
    },

    /**
     * Suspends the display of InApp Notifications and discards any new InApp Notifications to be shown
     * after this method is called.
     * The InApp Notifications will be displayed only once resumeInAppNotifications() is called.
     */
    discardInAppNotifications: function () {
        CleverTapReact.discardInAppNotifications();
    },

    /**
     * Resumes display of InApp Notifications.
     *
     * If suspendInAppNotifications() was called previously, calling this method will instantly show
     * all queued InApp Notifications and also resume InApp Notifications on events raised after this
     * method is called.
     *
     * If discardInAppNotifications() was called previously, calling this method will only resume
     * InApp Notifications on events raised after this method is called.
     */
    resumeInAppNotifications: function () {
        CleverTapReact.resumeInAppNotifications();
    },

    /**
    * Set the SDK debug level
    * @param {int} 0 = off, 1 = on
    */
    setDebugLevel: function (level) {
        CleverTapReact.setDebugLevel(level);
    },

    /**
     * Change the native instance of CleverTapAPI by using the instance for
     * specific account. Used by Leanplum RN SDK.
     *
     * @param accountId The ID of the account to use when switching instance.
     */
    setInstanceWithAccountId: function (accountId) {
        CleverTapReact.setInstanceWithAccountId(accountId);
    },

    /**
    * Uploads variables to the server. Requires Development/Debug build/configuration.
    */
    syncVariables: function () {
        CleverTapReact.syncVariables();
    },

    /**
    * Uploads variables to the server.
    *
    * @param {boolean} isProduction Provide `true` if variables must be sync in Productuon build/configuration.
    */
    syncVariablesinProd: function (isProduction) {
        CleverTapReact.syncVariablesinProd(isProduction)
    },

    /**
    * Forces variables to update from the server.
    *
    */
    fetchVariables: function (callback) {
        callWithCallback('fetchVariables', null, callback);
    },

    /**
     * Create variables. 
     * 
     * @param {object} variables The JSON Object specifying the varibles to be created.
     */
    defineVariables: function (variables) {
        CleverTapReact.defineVariables(variables);
    },

    /**
     * Get a variable or a group for the specified name.
     * 
     * @param {string} name - name.
     */
    getVariable: function (name, callback) {
        callWithCallback('getVariable', [name], callback);
    },

    /**
     * Get all variables via a JSON object.
     * 
     */
    getVariables: function (callback) {
        callWithCallback('getVariables', null, callback);
    },

    /**
     *  Adds a callback to be invoked when variables are initialised with server values. Will be called each time new values are fetched.
     * 
     * @param {function} handler The callback to add
     */
    onVariablesChanged: function (handler) {
        CleverTapReact.onVariablesChanged();
        this.addListener(CleverTapReact.CleverTapOnVariablesChanged, handler);
    },

    /**
     * Called when the value of the variable changes.
     * 
     * @param {name} string the name of the variable
     * @param {function} handler The callback to add
     */
    onValueChanged: function (name, handler) {
        CleverTapReact.onValueChanged(name);
        this.addListener(CleverTapReact.CleverTapOnValueChanged, handler);
    }
};

function convertDateToEpochInProperties(map) {
    /**
     * Conversion of date object in suitable CleverTap format(Epoch)
     */
    if (map) {
        for (let [key, value] of Object.entries(map)) {
            if (Object.prototype.toString.call(value) === '[object Date]') {
                map[key] = "$D_" + Math.floor(value.getTime() / 1000);
            }
        }
    }

};

module.exports = CleverTap;
