import {DeviceEventEmitter, NativeEventEmitter, NativeModules} from 'react-native';

const CleverTapReact = NativeModules.CleverTapReact;
const EventEmitter = NativeModules.CleverTapReactEventEmitter ? new NativeEventEmitter(NativeModules.CleverTapReactEventEmitter) : DeviceEventEmitter;

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
    FCM: CleverTapReact.FCM,
    XPS: CleverTapReact.XPS,
    BPS: CleverTapReact.BPS,
    HPS: CleverTapReact.HPS,
    CleverTapInboxDidInitialize: CleverTapReact.CleverTapInboxDidInitialize,
    CleverTapInboxMessagesDidUpdate: CleverTapReact.CleverTapInboxMessagesDidUpdate,
    CleverTapExperimentsDidUpdate: CleverTapReact.CleverTapExperimentsDidUpdate,
    CleverTapInboxMessageButtonTapped: CleverTapReact.CleverTapInboxMessageButtonTapped,
    CleverTapDisplayUnitsLoaded: CleverTapReact.CleverTapDisplayUnitsLoaded,
    CleverTapInAppNotificationButtonTapped: CleverTapReact.CleverTapInAppNotificationButtonTapped,

    /**
    * Add a CleverTap event listener
    * supported events are CleverTap.CleverTapProfileDidInitialize, CleverTap.CleverTapProfileSync,CleverTap.CleverTapOnInboxButtonClick
    * ,CleverTap.CleverTapOnInAppButtonClick,CleverTap.CleverTapOnDisplayUnitsLoaded and CleverTap.CleverTapInAppNotificationDismissed
    * @param {string} eventName - the CleverTap event name
    * @param {function(event)} your event handler
    */
    addListener: function(eventName, handler) {
        if (EventEmitter) {
            EventEmitter.addListener(eventName, handler);
        }
    },

    /**
    * Remove all CleverTap event listeners
    */
    removeListeners: function() {
        if (EventEmitter) {
            EventEmitter.removeListeners();
        }
    },

    /**
    * If an application is launched from a push notification click, returns the CleverTap deep link included in the push notification
    * @param {function(err, res)} callback that return the url as string in res or a string error in err
    */
    getInitialUrl: function(callback) {
        callWithCallback('getInitialUrl', null, callback);
    },

    /**
    * Registers the application to receive push notifications
    * only necessary for iOS.
    */
    registerForPush: function() {
        CleverTapReact.registerForPush();
    },

    /**
    * Manually set the push token on the CleverTap user profile
    * @param {string} token - the device token
    * @param {string} type - for Android only, specifying the type of push service token. Values can be CleverTap.FCM for Firebase or CleverTap.XPS for Xiaomi or CleverTap.BPS for Baidu or CleverTap.HPS for Huawei
    */
    setPushToken: function(token, type) {
        CleverTapReact.setPushTokenAsString(token, type);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannel: function(channelId, channelName, channelDescription, importance, showBadge){
        CleverTapReact.createNotificationChannel(channelId,channelName,channelDescription,importance,showBadge);
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
    createNotificationChannelWithSound: function(channelId, channelName, channelDescription, importance, showBadge, sound){
        CleverTapReact.createNotificationChannelWithSound(channelId,channelName,channelDescription,importance,showBadge,sound);
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
    createNotificationChannelWithGroupId: function(channelId, channelName, channelDescription, importance, groupId, showBadge){
        CleverTapReact.createNotificationChannelWithGroupId(channelId,channelName,channelDescription,importance,groupId,showBadge);
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
    createNotificationChannelWithGroupIdAndSound: function(channelId, channelName, channelDescription, importance, groupId, showBadge, sound){
        CleverTapReact.createNotificationChannelWithGroupIdAndSound(channelId,channelName,channelDescription,importance,groupId,showBadge,sound);
    },

    /**
    * Method to create Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    * @param {string} groupName - A String for setting the name of the notification channel group
    */
    createNotificationChannelGroup: function(groupId, groupName){
        CleverTapReact.createNotificationChannelGroup(groupId,groupName);
    },

    /**
    * Method to delete Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    */
    deleteNotificationChannel: function(channelId){
        CleverTapReact.deleteNotificationChannel(channelId);
    },

    /**
    * Method to delete Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    */
    deleteNotificationChannelGroup: function(groupId){
        CleverTapReact.deleteNotificationChannelGroup(groupId);
    },

    /**
    * Method to show the App Inbox
    * @param {object} extras - key-value data from RemoteMessage.getData().  keys and values are strings
    */
    createNotification: function(extras) {
        CleverTapReact.createNotification(extras);
    },

    /**
    * Enables tracking opt out for the currently active user.
    * @param {boolean} value - A boolean for enabling or disabling tracking for current user
    */
    setOptOut: function(value){
        CleverTapReact.setOptOut(value);
    },

    /**
    * Sets the CleverTap SDK to offline mode
    * @param {boolean} value - A boolean for enabling or disabling sending events for current user
    */
    setOffline: function(value){
        CleverTapReact.setOffline(value);
    },

    /**
    * Enables the reporting of device network related information, including IP address. This reporting is disabled by default.
    * @param {boolean} - A boolean for enabling or disabling device network related information to be sent to CleverTap
    */
    enableDeviceNetworkInfoReporting: function(value){
        CleverTapReact.enableDeviceNetworkInfoReporting(value);
    },

    /**
    * Enables the personalization API.  Call this prior to using the profile/event API getters
    */
    enablePersonalization: function() {
        CleverTapReact.enablePersonalization();
    },

    /**
    * Disables the personalization API.
    */
    disablePersonalization: function() {
        CleverTapReact.disablePersonalization();
    },

    /**
    * Record a Screen View
    * @param {string} screenName - the name of the screen
    */
    recordScreenView: function(screenName) {
        CleverTapReact.recordScreenView(screenName);
    },

    /**
    * Record an event with optional event properties
    * @param {string} eventName - the name of the event
    * @param {object} props - the key-value properties of the event.
    * keys are strings and values can be string, number or boolean.
    */
    recordEvent: function(eventName, props) {
        CleverTapReact.recordEvent(eventName, props);
    },

    /**
    * Record the special Charged event
    * @param {object} details - the key-value properties for the transaction.
    * @param {array<object>} items - an array of objects containing the key-value data for the items that make up the transaction.
    */
    recordChargedEvent: function(details, items) {
        CleverTapReact.recordChargedEvent(details, items);
    },

    /**
    * Get the time of the first occurrence of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetFirstTime: function(eventName, callback) {
        callWithCallback('eventGetFirstTime', [eventName], callback);
    },

    /**
    * Get the time of the most recent occurrence of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetLastTime: function(eventName, callback) {
        callWithCallback('eventGetLastTime', [eventName], callback);
    },

    /**
    * Get the number of occurrences of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of int
    */
    eventGetOccurrences: function(eventName, callback) {
        callWithCallback('eventGetOccurrences', [eventName], callback);
    },

    /**
    * Get the summary details of an event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of object {"eventName": <string>, "firstTime":<epoch seconds>, "lastTime": <epoch seconds>, "count": <int>} or empty object
    */
    eventGetDetail: function(eventName, callback) {
        callWithCallback('eventGetDetail', [eventName], callback);
    },

    /**
    * Get the user's event history
    * @param {function(err, res)} callback that returns a res of object {"eventName1":<event1 details object>, "eventName2":<event2 details object>}
    */
    getEventHistory: function(callback) {
        callWithCallback('getEventHistory', null, callback);
    },

    /**
    * Set the user's location as a latitude,longitude coordinate
    * @param {float} latitude
    * @param {float} longitude
    */
    setLocation: function(latitude, longitude) {
        CleverTapReact.setLocation(latitude, longitude);
    },

    /**
    * Get a unique CleverTap identifier suitable for use with install attribution providers
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapAttributionIdentifier: function(callback) {
        callWithCallback('profileGetCleverTapAttributionIdentifier', null, callback);
    },

    /**
    * Get the user profile's CleverTap identifier value
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapID: function(callback) {
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
    onUserLogin: function(profile) {
        CleverTapReact.onUserLogin(profile);
    },

    /**
    * Set key-value properties on a user profile
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    profileSet: function(profile) {
        CleverTapReact.profileSet(profile);
    },

    /**
    * Set key-value facebook properties on a user profile
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    profileSetGraphUser: function(user) {
        CleverTapReact.profileSetGraphUser(user);
    },

    /**
    * Get the value of a profile property
    * @param {string} the property key
    * @param {function(err, res)} callback that returns a res of the property value or null
    */
    profileGetProperty: function(key, callback) {
        callWithCallback('profileGetProperty', [key], callback);
    },

    /**
    * Remove a key-value from the user profile
    * @param {string} the key to remove
    */
    profileRemoveValueForKey: function(key) {
        CleverTapReact.profileRemoveValueForKey(key);
    },

    /**
    * Set an array of strings as a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileSetMultiValuesForKey: function(values, key) {
        CleverTapReact.profileSetMultiValues(values, key);
    },

    /**
    * Add a string value to a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileAddMultiValueForKey: function(value, key) {
        CleverTapReact.profileAddMultiValue(value, key);
    },

    /**
    * Add an array of strings to a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileAddMultiValuesForKey: function(values, key) {
        CleverTapReact.profileAddMultiValues(values, key);
    },

    /**
    * Remove a string value from a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileRemoveMultiValueForKey: function(value, key) {
        CleverTapReact.profileRemoveMultiValue(value, key);
    },

    /**
    * Remove an array of strings from a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileRemoveMultiValuesForKey: function(values, key) {
        CleverTapReact.profileRemoveMultiValues(values, key);
    },

    /**
    * Manually track the utm app install referrer
    * @param {string} the utm referrer source
    * @param {string} the utm referrer medium
    * @param {string} the utm referrer campaign
    */
    pushInstallReferrer: function(source, medium, campaign) {
        CleverTapReact.pushInstallReferrer(source, medium, campaign);
    },

    /**
    * Get the elapsed time of the current user session
    * @param {function(err, res)} callback that returns a res of int seconds
    */
    sessionGetTimeElapsed: function(callback) {
        callWithCallback('sessionGetTimeElapsed', null, callback);
    },

    /**
    * Get the total number of vists by the user
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetTotalVisits: function(callback) {
        callWithCallback('sessionGetTotalVisits', null, callback);
    },

    /**
    * Get the number of screens viewed by the user during the session
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetScreenCount: function(callback) {
        callWithCallback('sessionGetScreenCount', null, callback);
    },

    /**
    * Get the most recent previous visit time of the user
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    sessionGetPreviousVisitTime: function(callback) {
        callWithCallback('sessionGetPreviousVisitTime', null, callback);
    },

    /**
    * Get the utm referrer info for the current session
    * @param {function(err, res)} callback that returns a res of object {"source": <string>, "medium": <string>, "campaign": <string>} or empty object
    */
    sessionGetUTMDetails: function(callback) {
        callWithCallback('sessionGetUTMDetails', null, callback);
    },

    /**
    * Method to initalize the App Inbox
    */
    initializeInbox: function() {
        CleverTapReact.initializeInbox();
    },

    /**
    * Method to show the App Inbox
    * @param {object} styleConfig - key-value profile properties.  keys and values are strings
    */
    showInbox: function(styleConfig) {
        CleverTapReact.showInbox(styleConfig);
    },

   /**
    * Get the total number of Inbox Messages
    * @param {function(err, res)} callback that returns a res of count of inbox messages or -1
    */
    getInboxMessageCount: function(callback) {
        callWithCallback('getInboxMessageCount', null, callback);
    },

   /**
    * Get the total number of Unread Inbox Messages
    * @param {function(err, res)} callback that returns a res of count of unread inbox messages or -1
    */
    getInboxMessageUnreadCount: function(callback) {
        callWithCallback('getInboxMessageUnreadCount', null, callback);
    },

   /**
    * Get All inbox messages
    * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
    */
    getAllInboxMessages : function(callback){
        callWithCallback('getAllInboxMessages', null, callback);
    },

   /**
    * Get All unread inbox messages
    * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
    */
    getUnreadInboxMessages: function(callback) {
        callWithCallback('getUnreadInboxMessages', null, callback);
    },

   /**
    * Get Inbox Message that belongs to the given message id
    * @param {function(err, res)} callback that returns json string representation of CTInboxMessage
    */
    getInboxMessageForId: function(messageId,callback) {
        callWithCallback('getInboxMessageForId', [messageId], callback);
    },

   /**
    * Deletes Inbox Message that belongs to the given message id
    * @param {string} message id of inbox message of type CTInboxMessage
    */
    deleteInboxMessageForId:function(messageId) {
        CleverTapReact.deleteInboxMessageForId(messageId);
    },

   /**
    * Marks Inbox Message that belongs to the given message id as read
    * @param {string} message id of inbox message of type CTInboxMessage
    */
    markReadInboxMessageForId:function(messageId) {
       CleverTapReact.markReadInboxMessageForId(messageId);
    },

   /**
    * Pushes the Notification Clicked event for App Inbox to CleverTap.
    * @param {string} message id of inbox message of type CTInboxMessage
    */
    pushInboxNotificationClickedEventForId: function(messageId) {
       CleverTapReact.pushInboxNotificationClickedEventForId(messageId);
    },

   /**
    * Pushes the Notification Viewed event for App Inbox to CleverTap.
    * @param {string} message id of inbox message of type CTInboxMessage
    */
    pushInboxNotificationViewedEventForId: function(messageId) {
       CleverTapReact.pushInboxNotificationViewedEventForId(messageId);
    },

   /**
    * Get all display units
    * @param {function(err, res)} callback that returns a list of json string representation of CleverTapDisplayUnit
    */
    getAllDisplayUnits: function(callback) {
        callWithCallback('getAllDisplayUnits', null, callback);
    },

   /**
    * Get display unit for given unitID.
    * @param {string} unit id of display unit of type CleverTapDisplayUnit
    * @param {function(err, res)} callback that returns a json string representation of CleverTapDisplayUnit
    */
    getDisplayUnitForId: function(unitID,callback) {
       callWithCallback('getDisplayUnitForId', [unitID], callback);
    },

   /**
    * Raises the Display Unit Viewed event
    * @param {string} unit id of display unit of type CleverTapDisplayUnit
    */
    pushDisplayUnitViewedEventForID: function(unitID) {
       CleverTapReact.pushDisplayUnitViewedEventForID(unitID);
    },

   /**
    * Raises the Display Unit Clicked event
    * @param {string} unit id of display unit of type CleverTapDisplayUnit
    */
    pushDisplayUnitClickedEventForID: function(unitID) {
       CleverTapReact.pushDisplayUnitClickedEventForID(unitID);
    },

    /**
     * Registers an ABTesting variable of type Boolean for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerBooleanVariable: function(name){
        CleverTapReact.registerBooleanVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Double for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerDoubleVariable: function(name){
        CleverTapReact.registerDoubleVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Integer for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerIntegerVariable: function(name){
        CleverTapReact.registerIntegerVariable(name);
    },

    /**
     * Registers an ABTesting variable of type String for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerStringVariable: function(name){
        CleverTapReact.registerStringVariable(name);
    },

    /**
     * Registers an ABTesting variable of type List of Boolean for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerListOfBooleanVariable: function(name){
        CleverTapReact.registerListOfBooleanVariable(name);
    },

    /**
     * Registers an ABTesting variable of type List of Double for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerListOfDoubleVariable: function(name){
        CleverTapReact.registerListOfDoubleVariable(name);
    },

    /**
     * Registers an ABTesting variable of type List of Integer for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerListOfIntegerVariable: function(name){
        CleverTapReact.registerListOfIntegerVariable(name);
    },

    /**
     * Registers an ABTesting variable of type List of String for ease of editing on the CleverTap Dashboard
     * @param name {String} the name of the variable
     */
    registerListOfStringVariable: function(name){
        CleverTapReact.registerListOfStringVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Map of Boolean for ease of editing on the CleverTap Dashboard
     * @param name {@link String} the name of the variable
     */
    registerMapOfBooleanVariable: function(name){
        CleverTapReact.registerMapOfBooleanVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Map of Double for ease of editing on the CleverTap Dashboard
     * @param name {@link String} the name of the variable
     */
    registerMapOfDoubleVariable: function(name){
        CleverTapReact.registerMapOfDoubleVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Map of Integer for ease of editing on the CleverTap Dashboard
     * @param name {@link String} the name of the variable
     */
    registerMapOfIntegerVariable: function(name){
        CleverTapReact.registerMapOfIntegerVariable(name);
    },

    /**
     * Registers an ABTesting variable of type Map of String for ease of editing on the CleverTap Dashboard
     * @param name {@link String} the name of the variable
     */
    registerMapOfStringVariable: function(name){
        CleverTapReact.registerMapOfStringVariable(name);
    },

    /**
     * Returns the Boolean value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Boolean} the value set by the Experiment or the default value if unset
     */
    getBooleanVariable: function(name,defaultValue,callback){
        callWithCallback('getBooleanVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Double value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Double} the value set by the Experiment or the default value if unset
     */
    getDoubleVariable: function(name,defaultValue,callback){
        callWithCallback('getDoubleVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Integer value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Integer} the value set by the Experiment or the default value if unset
     */
    getIntegerVariable: function(name,defaultValue,callback){
        callWithCallback('getIntegerVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the String value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {String} the value set by the Experiment or the default value if unset
     */
    getStringVariable: function(name,defaultValue,callback){
        callWithCallback('getStringVariable', [name,defaultValue], callback);
    },


    /**
     * Returns the List of Boolean value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {List of Boolean} the value set by the Experiment or the default value if unset
     */
    getListOfBooleanVariable: function(name,defaultValue,callback){
        callWithCallback('getListOfBooleanVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the List of Double value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {List of Double} the value set by the Experiment or the default value if unset
     */
    getListOfDoubleVariable: function(name,defaultValue,callback){
        callWithCallback('getListOfDoubleVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the List of Integer value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {List of Integer} the value set by the Experiment or the default value if unset
     */
    getListOfIntegerVariable: function(name,defaultValue,callback){
        callWithCallback('getListOfIntegerVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the List of String value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {List of String} the value set by the Experiment or the default value if unset
     */
    getListOfStringVariable: function(name,defaultValue,callback){
        callWithCallback('getListOfStringVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Map of Boolean value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Map of Boolean} the value set by the Experiment or the default value if unset
     */
    getMapOfBooleanVariable: function(name,defaultValue,callback){
        callWithCallback('getMapOfBooleanVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Map of Double value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Map of Double} the value set by the Experiment or the default value if unset
     */
    getMapOfDoubleVariable: function(name,defaultValue,callback){
        callWithCallback('getMapOfDoubleVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Map of Integer value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Map of Integer} the value set by the Experiment or the default value if unset
     */
    getMapOfIntegerVariable: function(name,defaultValue,callback){
        callWithCallback('getMapOfIntegerVariable', [name,defaultValue], callback);
    },

    /**
     * Returns the Map of String value of the named variable set via an AB Testing Experiment or the default value if unset
     * @param name - the name of the variable
     * @param defaultValue - the default value to return if the value has not been set via an AB Testing Experiment
     * @return {Map of String} the value set by the Experiment or the default value if unset
     */
    getMapOfStringVariable: function(name,defaultValue,callback){
        callWithCallback('getMapOfStringVariable', [name,defaultValue], callback);
    },

    /**
     * Disables/Enables the ability to send Dynamic Variables to the CleverTap Dashboard
     * Disabled by default
     */
    setUIEditorConnectionEnabled: function(enabled){
        CleverTapReact.setUIEditorConnectionEnabled(enabled);
    },

    /**
    * Set the SDK debug level
    * @param {int} 0 = off, 1 = on
    */
    setDebugLevel: function(level) {
        CleverTapReact.setDebugLevel(level);
    }
};

module.exports = CleverTap;
