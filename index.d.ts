  /*******************
   * Listners & Deeplinks
   ******************/

   /**
    * Add a CleverTap event listener
    * supported events are CleverTap.CleverTapProfileDidInitialize, CleverTap.CleverTapProfileSync and CleverTap.CleverTapInAppNotificationDismissed
    * @param {string} eventName - the CleverTap event name
    * @param {function(event)} your event handler
    */
   export function addListener(
   eventName: string,
   handler: Function
   ): void;

   /**
    * Removes all of the registered listeners for given eventName.
    *
    * @param {string} eventName -  name of the event whose registered listeners to remove
    */
   export function removeListener(eventName: string): void;

   /**
    * @deprecated - Since version 5.0.0. Use removeListener(eventName) instead
    * Remove all event listeners
    */
   export function removeListeners(): void;

   /**
    * If an application is launched from a push notification click, returns the CleverTap deep link included in the push notification
    * @param {function(err, res)} callback that return the url as string in res or a string error in err
    */
   export function getInitialUrl(callback: Callback): void;

  /*******************
   * Personalization
   ******************/

  /**
   * Personalization
   * Enables the Personalization API
   */
  export function enablePersonalization(): void;

  /**
   * Enables tracking opt out for the currently active user.
   * @param optOut {boolean}
   */
  export function setOptOut(optOut: boolean): void;

  /**
   * Enables the reporting of device network related information, including IP address.  This reporting is disabled by default.
   * @param enable {boolean}
   */
  export function enableDeviceNetworkInfoReporting(enable: boolean): void;

  /*******************
   * Push
   ******************/

  /**
   * Registers for push notifications
   */
  export function registerForPush(): void;

  /**
   * Manually set the push token on the CleverTap user profile
   * @param {string} token - the device token
   * @param {string} type - for Android only, specifying the type of push service token. Values can be CleverTap.FCM for Firebase or CleverTap.XPS for Xiaomi or CleverTap.BPS for Baidu or CleverTap.HPS for Huawei,
   * @param {string} region - for xps only ,to specify the region
   */
  export function setPushToken(token: string, type: string,region?:string): void;

  /**
   * Create Notification Channel for Android O+
   * @param channelID {string}
   * @param channelName {string}
   * @param channelDescription {string}
   * @param importance {number}
   * @param showBadge {boolean}
   */
  export function createNotificationChannel(
    channelID: string,
    channelName: string,
    channelDescription: string,
    importance: number,
    showBadge: boolean
  ): void;

  /**
   * Create Notification Channel for Android O+
   * @param channelID {string}
   * @param channelName {string}
   * @param channelDescription {string}
   * @param importance {number}
   * @param showBadge {boolean}
   * @param sound {string}
   */
  export function createNotificationChannelWithSound(
    channelID: string,
    channelName: string,
    channelDescription: string,
    importance: number,
    showBadge: boolean,
    sound: string
  ): void;

  /**
   * Create Notification Channel with Group ID for Android O+
   * @param channelID {string}
   * @param channelName {string}
   * @param channelDescription {string}
   * @param importance {number}
   * @param groupId {string}
   * @param showBadge {boolean}
   * @param sound {string}
   */
  export function createNotificationChannelWithGroupId(
    channelID: string,
    channelName: string,
    channelDescription: string,
    importance: number,
    groupId: string,
    showBadge: boolean
  ): void;

  /**
   * Create Notification Channel with Group ID for Android O+
   * @param channelID {string}
   * @param channelName {string}
   * @param channelDescription {string}
   * @param importance {number}
   * @param groupId {string}
   * @param showBadge {boolean}
   */
  export function createNotificationChannelWithGroupIdAndSound(
    channelID: string,
    channelName: string,
    channelDescription: string,
    importance: number,
    groupId: string,
    showBadge: boolean,
    sound: string
  ): void;

  /**
   * Create Notification Channel Group for Android O+
   * @param groupID {string}
   * @param groupName {string}
   */
  export function createNotificationChannelGroup(
    groupID: string,
    groupName: string
  ): void;

  /**
   * Delete Notification Channel for Android O+
   * @param channelID {string}
   */
  export function deleteNotificationChannel(channelID: string): void;

  /**
   * Delete Notification Group for Android O+
   * @param groupID {string}
   */
  export function deleteNotificationChannelGroup(groupID: string): void;

  /**
   * Create Notification for Custom Handling Push Notifications
   * @param extras {any}
   */
  export function createNotification(extras: any): void;

  /**
 * Call this method to prompt the hard permission dialog directly, if the push primer is not required.
 * @param showFallbackSettings : {boolean} - Pass true to show an alert dialog which routes to app's notification settings page. 
 */
export function promptForPushPermission(showFallbackSettings: boolean): void;

/**
 * Call this method to prompt the push primer flow.
 * @param localInAppConfig : {any}  object
 */
export function promptPushPrimer(localInAppConfig: any): void;

/**
 * Returns true/false based on whether push permission is granted or denied.
 *
 * @param {function(err, res)} non-null callback to retrieve the result
 */
export function isPushPermissionGranted(callback: CallbackString): void;

  /*******************
   * Events
   ******************/

  /**
   * Record Screen View
   * @param screenName {string}
   */
  export function recordScreenView(screenName: string): void;
  /**
   *  Record Event with Name and Event properties
   * @param eventName {string}
   * @param eventProps {any}
   */
  export function recordEvent(
    eventName: string,
    eventProps: any
  ): void;

  /**
   *  Record Charged Event with Details and Items
   * @param details {any}  object with transaction details
   * @param items {any}  array of items purchased
   */
  export function recordChargedEvent(
    details: any,
    items: any
  ): void;

  /**
   * Get Event First Time
   * @param eventName {string}
   * callback returns epoch seconds or -1
   */
  export function eventGetFirstTime(eventName: string, callback: Callback): void;

  /**
   * Get Event Last Time
   * @param eventName {string}
   * callback returns epoch seconds or -1
   */
  export function eventGetLastTime(eventName: string, callback: Callback): void;

  /**
   * Get Event Number of Occurrences
   * @param eventName {string}
   * calls back with int or -1
   */
  export function eventGetOccurrences(eventName: string, callback: Callback): void;

  /**
   * Get Event Details
   * @param eventName {string}
   * calls back with object {"eventName": <string>, "firstTime":<epoch seconds>, "lastTime": <epoch seconds>, "count": <int>} or empty object
   */
  export function eventGetDetail(eventName: string, callback: Callback): void;

  /**
   * Get Event History
   * calls back with object {"eventName1":<event1 details object>, "eventName2":<event2 details object>}
   */
  export function getEventHistory(callback: Callback): void;

  /**
   * Set location
   * @param lat {number}
   * @param lon {number}
   */
  export function setLocation(lat: number, lon: number): void;

  /**
   * Creates a separate and distinct user profile identified by one or more of Identity, Email, FBID or GPID values,
   * and populated with the key-values included in the profile dictionary.
   * If your app is used by multiple users, you can use this method to assign them each a unique profile to track them separately.
   * If instead you wish to assign multiple Identity, Email, FBID and/or GPID values to the same user profile,
   * use profileSet rather than this method.
   * If none of Identity, Email, FBID or GPID is included in the profile dictionary,
   * all properties values will be associated with the current user profile.
   * When initially installed on this device, your app is assigned an "anonymous" profile.
   * The first time you identify a user on this device (whether via onUserLogin or profileSet),
   * the "anonymous" history on the device will be associated with the newly identified user.
   * Then, use this method to switch between subsequent separate identified users.
   * Please note that switching from one identified user to another is a costly operation
   * in that the current session for the previous user is automatically closed
   * and data relating to the old user removed, and a new session is started
   * for the new user and data for that user refreshed via a network call to CleverTap.
   * In addition, any global frequency caps are reset as part of the switch.
   * @param profile {any} object
   */
  export function onUserLogin(profile: any): void;

  /**
   * Set profile attributes
   * @param profile {any} object
   */
  export function profileSet(profile: any): void;

  /**
   * Get User Profile Property
   * @param propertyName {string}
   * calls back with value of propertyName or false
   */
  export function profileGetProperty(propertyName: string, callback: Callback): void;

  /**
   * @deprecated
   * Since version 0.6.0. Use `getCleverTapID(callback)` instead.
   *
   * Get a unique CleverTap identifier suitable for use with install attribution providers.
   * @param {function(err, res)} callback that returns a string res
   */
  export function profileGetCleverTapAttributionIdentifier(callback: CallbackString): void;

  /**
   * @deprecated
   * Since version 0.6.0. Use `getCleverTapID(callback)` instead.
   *
   * Get User Profile CleverTapID
   * @param {function(err, res)} callback that returns a string res
   */
  export function profileGetCleverTapID(callback: CallbackString): void;

  /**
   * Returns a unique identifier through callback by which CleverTap identifies this user
   *
   * @param {function(err, res)} non-null callback to retrieve identifier
   */
  export function getCleverTapID(callback: CallbackString): void;

  /**
   * Remove the property specified by key from the user profile. Alternatively this method
   * can also be used to remove PII data (for eg. Email,Name,Phone), locally from database and shared prefs
   * @param key {string}
   */
  export function profileRemoveValueForKey(key: string): void;

  /**
   * Method for setting a multi-value user profile property
   * @param key {string}
   * @param values {any} array of strings
   */
  export function profileSetMultiValuesForKey(values: any, key: string): void;

  /**
   * Method for adding a value to a multi-value user profile property
   * @param key {string}
   * @param value {string}
   */
  export function profileAddMultiValueForKey(value: string, key: string): void;

  /**
   * Method for adding values to a multi-value user profile property
   * @param key {string}
   * @param values {any} array of strings
   */
  export function profileAddMultiValuesForKey(values: any, key: string): void;
  /**
   * Method for removing a value from a multi-value user profile property
   * @param key {string}
   * @param value {string}
   */
  export function profileRemoveMultiValueForKey(value: string, key: string): void;

  /**
   * Method for removing a value from a multi-value user profile property
   * @param key {string}
   * @param values {any} array of strings
   */
  export function profileRemoveMultiValuesForKey(values: any, key: string): void;

  /*******************************
   * Increment/Decrement Operators
   *******************************/

   /**
   * This method is used to increment the given value
   *
   * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
   * @param key   {string} profile property
   */
  export function profileIncrementValueForKey(value:number, key:string): void;

  /**
   * This method is used to decrement the given value
   *
   * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
   * @param key   {string} profile property
   */
  export function profileDecrementValueForKey(value:number, key:string): void;

  /*******************
   * Session
   ******************/

  /**
   * Get Session Elapsed Time
   * calls back with seconds
   */
  export function sessionGetTimeElapsed(callback: Callback): void;

  /**
   * Get Session Total Visits
   * calls back with int or -1
   */
  export function sessionGetTotalVisits(callback: Callback): void;

  /**
   * Get Session Screen Count
   * calls back with with int
   */
  export function sessionGetScreenCount(callback: Callback): void;

  /**
   * Get Session Previous Visit Time
   * calls back with epoch seconds or -1
   */
  export function sessionGetPreviousVisitTime(callback: Callback): void;

  /**
   * Get Sesssion Referrer UTM details
   * object {"source": <string>, "medium": <string>, "campaign": <string>} or empty object
   */
  export function sessionGetUTMDetails(callback: Callback): void;

  /**
   * Call this to manually track the utm details for an incoming install referrer
   * @param source {string}
   * @param medium {string}
   * @param campaign {string}
   */
  export function pushInstallReferrer(
    source: string,
    medium: string,
    campaign: string
  ): void;

  /****************************
  * Notification Inbox methods
  ****************************/
  /**
   * Call this method to initialize the App Inbox
   */
  export function initializeInbox(): void;

  /**
   * Call this method to get the count of unread Inbox messages
   */
  export function getInboxMessageUnreadCount(callback: Callback): void;

  /**
   * Call this method to get the count of total Inbox messages
   */
  export function getInboxMessageCount(callback: Callback): void;

  /**
   * Call this method to open the App Inbox
   * @param styleConfig : any or empty object
   */
  export function showInbox(styleConfig: any): void;

   /**
   * Call this method to dismiss the App Inbox
   */
  export function dismissInbox(): void;
  
  /**
   * Call this method to get all inbox messages
   */
  export function getAllInboxMessages(callback: Callback): void;

  /**
   * Call this method to get all unread inbox messages
   */
  export function getUnreadInboxMessages(callback: Callback): void;

  /**
   * Call this method to get inbox message that belongs to the given message id
   */
  export function getInboxMessageForId(messageId: string, callback: Callback): void;

  /**
   * Call this method to delete inbox message that belongs to the given message id
   */
  export function deleteInboxMessageForId(messageId: string): void;

  /**
   * Call this method to delete multiple inbox messages that belongs to the given message ids
   */
  export function deleteInboxMessagesForIDs(messageIds: any): void;

  /**
   * Call this method to mark inbox message as read
   */
  export function markReadInboxMessageForId(messageId: string): void;

  /**
   * Call this method to mark multiple inbox messages as read
   */
  export function markReadInboxMessagesForIDs(messageIds: any): void;

  /**
   * Call this method to push the Notification Clicked event for App Inbox to CleverTap
   */
  export function pushInboxNotificationClickedEventForId(messageId: string): void;

  /**
   * Call this method to push the Notification Viewed event for App Inbox to CleverTap
   */
  export function pushInboxNotificationViewedEventForId(messageId: string): void;

  /****************************
  * Native Display Methods
  ****************************/

  /**
   * Call this method to get all display units
   */
  export function getAllDisplayUnits(callback: Callback): void;

  /**
   * Call this method to get display unit that belongs to the given unit id
   */
  export function getDisplayUnitForId(unitID: string, callback: Callback): void;

  /**
   * Call this method to raise display unit viewed event
   */
  export function pushDisplayUnitViewedEventForID(unitID: string): void;

  /**
   * Call this method to raise display unit clicked event
   */
  export function pushDisplayUnitClickedEventForID(unitID: string): void;

  /*******************
   * Product Configs
   ******************/ 
  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Sets default product config params using the given object.
   * @param productConfigMap {any} key-value product config properties. keys are strings and values can be string, double, integer, boolean or json in string format.
   */
  export function setDefaultsMap(productConfigMap: any): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Starts fetching product configs, adhering to the default minimum fetch interval.
   */
  export function fetch(): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Starts fetching product configs, adhering to the default minimum fetch interval.
   * @param intervalInSecs {number}  minimum fetch interval in seconds.
   */
  export function fetchWithMinimumIntervalInSeconds(intervalInSecs: number): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Activates the most recently fetched product configs, so that the fetched key value pairs take effect.
   */
  export function activate(): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Asynchronously fetches and then activates the fetched product configs.
   */
  export function fetchAndActivate(): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Sets the minimum interval in seconds between successive fetch calls.
   * @param intervalInSecs {number} interval in seconds between successive fetch calls.
   */
  export function setMinimumFetchIntervalInSeconds(intervalInSecs: number): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Deletes all activated, fetched and defaults configs as well as all Product Config settings.
   */
  export function resetProductConfig(): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Returns the product config parameter value for the given key as a String.
   * @param key {string} - the name of the key
   * @param callback {Callback} - callback that returns a value of type string if present else blank
   */
  export function getProductConfigString(
    key: string,
    callback: Callback): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Returns the product config parameter value for the given key as a boolean.
   * @param key {string} - the name of the key
   * @param callback {Callback} - callback that returns a value of type boolean if present else false
   */
  export function getProductConfigBoolean(
    key: string,
    callback: Callback): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Returns the product config parameter value for the given key as a number.
   * @param key {string} - the name of the key
   * @param callback {Callback} - callback that returns a value of type number if present else 0
   */
  export function getNumber(
    key: string,
    callback: Callback): void;

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Returns the last fetched timestamp in millis.
   * @param callback {Callback} - callback that returns value of timestamp in millis as a string.
   */
  export function getLastFetchTimeStampInMillis(callback: Callback): void;

  /*******************
   * Feature Flags
   ******************/

  /**
   * @deprecated
   * Since version 1.1.0 and will be removed in the future versions of this SDK.
   *
   * Getter to return the feature flag configured at the dashboard
   * @param key {string} - the name of the key
   * @param defaultValue {boolean} - default value of the key, in case we don't find any feature flag with the key.
   * @param callback {Callback} - callback that returns a feature flag value of type boolean if present else provided default value
   */
  export function getFeatureFlag(
    key: string,
    defaultValue: boolean,
    callback: Callback): void;


  /*******************
   * InApp Controls
   ******************/

  /**
   * Suspends display of InApp Notifications.
   * The InApp Notifications are queued once this method is called
   * and will be displayed once resumeInAppNotifications() is called.
   */
  export function suspendInAppNotifications(): void;

  /**
   * Suspends the display of InApp Notifications and discards any new InApp Notifications to be shown
   * after this method is called.
   * The InApp Notifications will be displayed only once resumeInAppNotifications() is called.
   */
  export function discardInAppNotifications(): void;

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
  export function resumeInAppNotifications(): void;

  /*******************
   * Instances
   ******************/

  /**
    * Change the native instance of CleverTapAPI by using the instance for
    * specific account. Used by Leanplum RN SDK.
    *
    * @param accountId {string} - The ID of the account to use when switching instance.
    */
  export function setInstanceWithAccountId(accountId: string): void;

  /*******************
   * Product Experiences: Vars
   ******************/

  /**
   * Uploads variables to the server. Requires Development/Debug build/configuration.
   */
  export function syncVariables(): void;

  /**
   * Uploads variables to the server.
   * 
   * @param isProduction Provide `true` if variables must be sync in Productuon build/configuration.
   */
  export function syncVariablesinProd(isProduction: boolean): void;

  /**
   *  Forces variables to update from the server.
   *
   * @param {function(err, res)} a callback with a boolean flag whether the update was successful.
   */
  export function fetchVariables(callback: Callback): void;

  /**
   *  Create variables. 
   * 
   * @param {object} variables The JSON Object specifying the varibles to be created.
   */
  export function defineVariables(variables: object): void;
  
  /**
   * Get all variables via a JSON object.
   * 
   */
  export function getVariables(callback: Callback): void;

  /**
   * Get a variable or a group for the specified name.
   * 
   * @param {string} name - name.
   */
  export function getVariable(name: string, callback: Callback): void;

  /**
    *  Adds a callback to be invoked when variables are initialised with server values. Will be called each time new values are fetched.
    * 
    * @param {function} handler The callback to add
    */
  export function onVariablesChanged(handler: Function): void;

  /**
    * Called when the value of the variable changes.
    * 
    * @param {name} string the name of the variable
    * @param {function} handler The callback to add
    */
  export function onValueChanged(name: string, handler: Function): void;

  /*******************
   * Developer Options
   ******************/
  /**
   * 0 is off, 1 is info, 2 is debug, default is 1
   * @param level {number}
   */
  export function setDebugLevel(level: number): void;

  type Callback = (err: object, res: object) => void;
  type CallbackString = (err: object, res: string) => void;

  export const FCM: string;
  export const XPS: string;
  export const BPS: string;
  export const HPS: string;
  export const CleverTapProfileDidInitialize: string;
  export const CleverTapProfileSync: string;
  export const CleverTapInAppNotificationDismissed: string;
  export const CleverTapInAppNotificationShowed: string;
  export const CleverTapInboxDidInitialize: string;
  export const CleverTapInboxMessagesDidUpdate: string;
  export const CleverTapInboxMessageButtonTapped: string;
  export const CleverTapInboxMessageTapped: string;
  export const CleverTapDisplayUnitsLoaded: string;
  export const CleverTapInAppNotificationButtonTapped: string;
  export const CleverTapFeatureFlagsDidUpdate: string;
  export const CleverTapProductConfigDidInitialize: string;
  export const CleverTapProductConfigDidFetch: string;
  export const CleverTapProductConfigDidActivate: string;
  export const CleverTapPushNotificationClicked: string;
  export const CleverTapPushPermissionResponseReceived: string;
  export const CleverTapOnVariablesChanged: string;
  export const CleverTapOnValueChanged: string;