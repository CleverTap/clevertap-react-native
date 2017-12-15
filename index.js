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
    GCM: CleverTapReact.GCM,

    /**
    * Add a CleverTap event listener
    * supported events are CleverTap.CleverTapProfileDidInitialize, CleverTap.CleverTapProfileSync and CleverTap.CleverTapInAppNotificationDismissed
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
    * @param {string} type - for Android only, specify CleverTap.GCM or CleverTap.FCM
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
    * @param {string} groupId - A String for setting the notification channel as a part of a notification group
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannelwithGroupId: function(channelId, channelName, channelDescription, importance, groupId, showBadge){
        CleverTapReact.createNotificationChannelwithGroupId(channelId,channelName,channelDescription,importance,groupId,showBadge);
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
    * Enables the personalization API.  Call this prior to using the profile/event API getters
    */
    enablePersonalization: function() {
        CleverTapReact.enablePersonalization();
    },

    /**
    * Disbles the personalization API.
    */
    disablePersonalization: function() {
        CleverTapReact.disablePersonalization();
    },

    /**
    * Record a Screen View; iOS only
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
    * Set the SDK debug level
    * @param {int} 0 = off, 1 = on
    */
    setDebugLevel: function(level) {
        CleverTapReact.setDebugLevel(level);
    }
};

module.exports = CleverTap;
