package com.clevertap.react

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

@Suppress("unused")
class CleverTapModule(reactContext: ReactApplicationContext?) : ReactContextBaseJavaModule(reactContext) {

    private val cleverTapModuleImpl: CleverTapModuleImpl
    override fun getName(): String {
        return CleverTapModuleImpl.REACT_MODULE_NAME
    }

    init {
        cleverTapModuleImpl = CleverTapModuleImpl(reactContext!!)
    }

    @SuppressLint("RestrictedApi")
    @ReactMethod
    fun setLibrary(libName: String?, libVersion: Int) {
        cleverTapModuleImpl.setLibrary(libName, libVersion)
    }

    @ReactMethod
    fun componentMounted() {
        cleverTapModuleImpl.componentMounted()
    }

    @ReactMethod
    fun setLocale(locale: String?) {
        cleverTapModuleImpl.setLocale(locale)
    }

    @ReactMethod
    fun activate() {
        cleverTapModuleImpl.activate()
    }

    //Custom Push Notification
    @ReactMethod
    fun createNotification(extras: ReadableMap?) {
        cleverTapModuleImpl.createNotification(extras)
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun createNotificationChannel(
        channelId: String?, channelName: String?, channelDescription: String?, importance: Int, showBadge: Boolean
    ) {
        cleverTapModuleImpl.createNotificationChannel(
            channelId, channelName, channelDescription, importance, showBadge
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun createNotificationChannelGroup(groupId: String?, groupName: String?) {
        cleverTapModuleImpl.createNotificationChannelGroup(groupId, groupName)
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun createNotificationChannelWithGroupId(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Int,
        groupId: String?,
        showBadge: Boolean
    ) {
        cleverTapModuleImpl.createNotificationChannelWithGroupId(
            channelId, channelName, channelDescription, importance, groupId, showBadge
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun createNotificationChannelWithGroupIdAndSound(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Int,
        groupId: String?,
        showBadge: Boolean,
        sound: String?
    ) {
        cleverTapModuleImpl.createNotificationChannelWithGroupIdAndSound(
            channelId, channelName, channelDescription, importance, groupId, showBadge, sound
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun createNotificationChannelWithSound(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Int,
        showBadge: Boolean,
        sound: String?
    ) {
        cleverTapModuleImpl.createNotificationChannelWithSound(
            channelId, channelName, channelDescription, importance, showBadge, sound
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun deleteNotificationChannel(channelId: String?) {
        cleverTapModuleImpl.deleteNotificationChannel(channelId)
    }

    @RequiresApi(api = VERSION_CODES.O)
    @ReactMethod
    fun deleteNotificationChannelGroup(groupId: String?) {
        cleverTapModuleImpl.deleteNotificationChannelGroup(groupId)
    }

    //Push permission methods
    @ReactMethod
    fun promptForPushPermission(showFallbackSettings: Boolean) {
        cleverTapModuleImpl.promptForPushPermission(showFallbackSettings)
    }

    @ReactMethod
    fun promptPushPrimer(localInAppConfig: ReadableMap?) {
        cleverTapModuleImpl.promptPushPrimer(localInAppConfig)
    }

    @ReactMethod
    fun isPushPermissionGranted(callback: Callback?) {
        cleverTapModuleImpl.isPushPermissionGranted(callback)
    }

    @ReactMethod
    fun disablePersonalization() {
        cleverTapModuleImpl.disablePersonalization()
    }

    @ReactMethod
    fun enableDeviceNetworkInfoReporting(value: Boolean) {
        cleverTapModuleImpl.enableDeviceNetworkInfoReporting(value)
    }

    @ReactMethod
    fun enablePersonalization() {
        cleverTapModuleImpl.enablePersonalization()
    }

    @ReactMethod
    fun eventGetDetail(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetDetail(eventName, callback)
    }

    @ReactMethod
    fun eventGetFirstTime(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetFirstTime(eventName, callback)
    }

    @ReactMethod
    fun eventGetLastTime(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetLastTime(eventName, callback)
    }

    @ReactMethod
    fun eventGetOccurrences(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetOccurrences(eventName, callback)
    }

    @ReactMethod
    fun fetch() {
        cleverTapModuleImpl.fetch()
    }

    @ReactMethod
    fun fetchAndActivate() {
        cleverTapModuleImpl.fetchAndActivate()
    }

    @ReactMethod
    fun fetchWithMinimumFetchIntervalInSeconds(interval: Int) {
        cleverTapModuleImpl.fetchWithMinimumFetchIntervalInSeconds(interval)
    }

    @ReactMethod
    fun getAllDisplayUnits(callback: Callback?) {
        cleverTapModuleImpl.getAllDisplayUnits(callback)
    }

    @ReactMethod
    fun getBoolean(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getBoolean(key, callback)
    }

    @ReactMethod
    fun getDisplayUnitForId(unitID: String?, callback: Callback?) {
        cleverTapModuleImpl.getDisplayUnitForId(unitID, callback)
    }

    @ReactMethod
    fun getDouble(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getDouble(key, callback)
    }

    @ReactMethod
    fun getEventHistory(callback: Callback?) {
        cleverTapModuleImpl.getEventHistory(callback)
    }

    @ReactMethod
    fun getFeatureFlag(name: String?, defaultValue: Boolean?, callback: Callback?) {
        cleverTapModuleImpl.getFeatureFlag(name, defaultValue, callback)
    }

    @ReactMethod
    fun getAllInboxMessages(callback: Callback?) {
        cleverTapModuleImpl.getAllInboxMessages(callback)
    }

    @ReactMethod
    fun getInboxMessageCount(callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageCount(callback)
    }

    @ReactMethod
    fun getInboxMessageForId(messageId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageForId(messageId, callback)
    }

    @ReactMethod
    fun getInboxMessageUnreadCount(callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageUnreadCount(callback)
    }

    @ReactMethod
    fun deleteInboxMessageForId(messageId: String?) {
        cleverTapModuleImpl.deleteInboxMessageForId(messageId)
    }

    @ReactMethod
    fun getUnreadInboxMessages(callback: Callback?) {
        cleverTapModuleImpl.getUnreadInboxMessages(callback)
    }

    @ReactMethod
    fun initializeInbox() {
        cleverTapModuleImpl.initializeInbox()
    }

    @ReactMethod
    fun markReadInboxMessageForId(messageId: String?) {
        cleverTapModuleImpl.markReadInboxMessageForId(messageId)
    }

    @ReactMethod
    fun markReadInboxMessagesForIDs(messageIDs: ReadableArray?) {
        cleverTapModuleImpl.markReadInboxMessagesForIDs(messageIDs)
    }

    @ReactMethod
    fun deleteInboxMessagesForIDs(messageIDs: ReadableArray?) {
        cleverTapModuleImpl.deleteInboxMessagesForIDs(messageIDs)
    }

    @ReactMethod
    fun pushInboxNotificationClickedEventForId(messageId: String?) {
        cleverTapModuleImpl.pushInboxNotificationClickedEventForId(messageId)
    }

    @ReactMethod
    fun pushInboxNotificationViewedEventForId(messageId: String?) {
        cleverTapModuleImpl.pushInboxNotificationViewedEventForId(messageId)
    }

    @ReactMethod
    fun showInbox(styleConfig: ReadableMap?) {
        cleverTapModuleImpl.showInbox(styleConfig)
    }

    @ReactMethod
    fun dismissInbox() {
        cleverTapModuleImpl.dismissInbox()
    }

    @ReactMethod
    fun getInitialUrl(callback: Callback?) {
        cleverTapModuleImpl.getInitialUrl(callback)
    }

    @ReactMethod
    fun getLastFetchTimeStampInMillis(callback: Callback?) {
        cleverTapModuleImpl.getLastFetchTimeStampInMillis(callback)
    }

    @ReactMethod
    fun getString(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getString(key, callback)
    }

    @ReactMethod
    fun onUserLogin(profile: ReadableMap?) {
        cleverTapModuleImpl.onUserLogin(profile)
    }

    @ReactMethod
    fun profileAddMultiValue(value: String?, key: String?) {
        cleverTapModuleImpl.profileAddMultiValue(value, key)
    }

    @ReactMethod
    fun profileAddMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileAddMultiValues(values, key)
    }

    @ReactMethod
    fun profileGetCleverTapAttributionIdentifier(callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapAttributionIdentifier(callback)
    }

    @ReactMethod
    fun profileGetCleverTapID(callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapID(callback)
    }

    @ReactMethod
    fun getCleverTapID(callback: Callback?) {
        cleverTapModuleImpl.getCleverTapID(callback)
    }

    @ReactMethod
    fun profileGetProperty(propertyName: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetProperty(propertyName, callback)
    }

    @ReactMethod
    fun profileRemoveMultiValue(value: String?, key: String?) {
        cleverTapModuleImpl.profileRemoveMultiValue(value, key)
    }

    @ReactMethod
    fun profileRemoveMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileRemoveMultiValues(values, key)
    }

    @ReactMethod
    fun profileRemoveValueForKey(key: String?) {
        cleverTapModuleImpl.profileRemoveValueForKey(key)
    }

    @ReactMethod
    fun profileSet(profile: ReadableMap?) {
        cleverTapModuleImpl.profileSet(profile)
    }

    @ReactMethod
    fun profileSetMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileSetMultiValues(values, key)
    }

    @ReactMethod
    fun pushDisplayUnitClickedEventForID(unitID: String?) {
        cleverTapModuleImpl.pushDisplayUnitClickedEventForID(unitID)
    }

    @ReactMethod
    fun pushDisplayUnitViewedEventForID(unitID: String?) {
        cleverTapModuleImpl.pushDisplayUnitViewedEventForID(unitID)
    }

    @ReactMethod
    fun pushInstallReferrer(source: String?, medium: String?, campaign: String?) {
        cleverTapModuleImpl.pushInstallReferrer(source, medium, campaign)
    }

    @ReactMethod
    fun recordChargedEvent(details: ReadableMap?, items: ReadableArray?) {
        cleverTapModuleImpl.recordChargedEvent(details, items)
    }

    @ReactMethod
    fun recordEvent(eventName: String?, props: ReadableMap?) {
        cleverTapModuleImpl.recordEvent(eventName, props)
    }

    @ReactMethod
    fun recordScreenView(screenName: String?) {
        cleverTapModuleImpl.recordScreenView(screenName)
    }

    // Product Config methods
    @ReactMethod
    fun registerForPush() {
        cleverTapModuleImpl.registerForPush()
    }

    @ReactMethod
    fun reset() {
        cleverTapModuleImpl.reset()
    }

    // Feature Flag methods
    @ReactMethod
    fun sessionGetPreviousVisitTime(callback: Callback?) {
        cleverTapModuleImpl.sessionGetPreviousVisitTime(callback)
    }

    // Developer Options
    @ReactMethod
    fun sessionGetScreenCount(callback: Callback?) {
        cleverTapModuleImpl.sessionGetScreenCount(callback)
    }

    @ReactMethod
    fun sessionGetTimeElapsed(callback: Callback?) {
        cleverTapModuleImpl.sessionGetTimeElapsed(callback)
    }

    @ReactMethod
    fun sessionGetTotalVisits(callback: Callback?) {
        cleverTapModuleImpl.sessionGetTotalVisits(callback)
    }

    @ReactMethod
    fun sessionGetUTMDetails(callback: Callback?) {
        cleverTapModuleImpl.sessionGetUTMDetails(callback)
    }

    @ReactMethod
    fun setDebugLevel(level: Int) {
        CleverTapAPI.setDebugLevel(level)
    }

    @ReactMethod
    fun setDefaultsMap(map: ReadableMap?) {
        cleverTapModuleImpl.setDefaultsMap(map)
    }

    @ReactMethod
    fun setLocation(latitude: Double, longitude: Double) {
        cleverTapModuleImpl.setLocation(latitude, longitude)
    }

    @ReactMethod
    fun setMinimumFetchIntervalInSeconds(interval: Int) {
        cleverTapModuleImpl.setMinimumFetchIntervalInSeconds(interval)
    }

    //Sets the SDK to offline mode
    @ReactMethod
    fun setOffline(value: Boolean) {
        cleverTapModuleImpl.setOffline(value)
    }

    @ReactMethod
    fun setOptOut(value: Boolean) {
        cleverTapModuleImpl.setOptOut(value)
    }

    @ReactMethod
    fun setPushTokenAsString(token: String?, type: String?) {
        cleverTapModuleImpl.setPushTokenAsString(token, type)
    }

    // Increment/Decrement Operator
    @ReactMethod
    fun profileIncrementValueForKey(value: Double?, key: String?) {
        cleverTapModuleImpl.profileIncrementValueForKey(value, key)
    }

    @ReactMethod
    fun profileDecrementValueForKey(value: Double?, key: String?) {
        cleverTapModuleImpl.profileDecrementValueForKey(value, key)
    }

    // InApp Controls
    @ReactMethod
    fun suspendInAppNotifications() {
        cleverTapModuleImpl.suspendInAppNotifications()
    }

    @ReactMethod
    fun discardInAppNotifications() {
        cleverTapModuleImpl.discardInAppNotifications()
    }

    @ReactMethod
    fun resumeInAppNotifications() {
        cleverTapModuleImpl.resumeInAppNotifications()
    }

    @ReactMethod
    fun setInstanceWithAccountId(accountId: String?) {
        cleverTapModuleImpl.setInstanceWithAccountId(accountId)
    }

    @ReactMethod
    fun fetchInApps(callback: Callback?) {
        cleverTapModuleImpl.fetchInApps(callback)
    }

    @ReactMethod
    fun clearInAppResources(expiredOnly: Boolean) {
        cleverTapModuleImpl.clearInAppResources(expiredOnly)
    }

    @ReactMethod
    fun syncVariables() {
        cleverTapModuleImpl.syncVariables()
    }

    @ReactMethod
    fun syncVariablesinProd(isProduction: Boolean, callback: Callback?) {
        cleverTapModuleImpl.syncVariablesinProd(isProduction, callback)
    }

    @ReactMethod
    fun defineVariables(`object`: ReadableMap) {
        cleverTapModuleImpl.defineVariables(`object`)
    }

    @ReactMethod
    fun fetchVariables(callback: Callback?) {
        cleverTapModuleImpl.fetchVariables(callback)
    }

    @ReactMethod
    fun getVariable(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariable(key, callback)
    }

    @ReactMethod
    fun getVariables(callback: Callback?) {
        cleverTapModuleImpl.getVariables(callback)
    }

    @ReactMethod
    fun onVariablesChanged() {
        cleverTapModuleImpl.onVariablesChanged()
    }

    @ReactMethod
    fun onValueChanged(name: String) {
        cleverTapModuleImpl.onValueChanged(name)
    }

    companion object {

        private val mlaunchURI: Uri? = null
        private const val REACT_MODULE_NAME = "CleverTapReact"
        private const val TAG = REACT_MODULE_NAME
        private const val CLEVERTAP_PROFILE_DID_INITIALIZE = "CleverTapProfileDidInitialize"
        private const val CLEVERTAP_PROFILE_SYNC = "CleverTapProfileSync"
        private const val CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED = "CleverTapInAppNotificationDismissed"
        private const val CLEVERTAP_IN_APP_NOTIFICATION_SHOWED = "CleverTapInAppNotificationShowed"
        private const val FCM = "FCM"
        private const val BPS = "BPS"
        private const val HPS = "HPS"
        private const val CLEVERTAP_INBOX_DID_INITIALIZE = "CleverTapInboxDidInitialize"
        private const val CLEVERTAP_INBOX_MESSAGES_DID_UPDATE = "CleverTapInboxMessagesDidUpdate"
        private const val CLEVERTAP_ON_INBOX_BUTTON_CLICK = "CleverTapInboxMessageButtonTapped"
        private const val CLEVERTAP_ON_INBOX_MESSAGE_CLICK = "CleverTapInboxMessageTapped"
        private const val CLEVERTAP_ON_INAPP_BUTTON_CLICK = "CleverTapInAppNotificationButtonTapped"
        private const val CLEVERTAP_ON_DISPLAY_UNITS_LOADED = "CleverTapDisplayUnitsLoaded"
        private const val CLEVERTAP_FEATURE_FLAGS_DID_UPDATE = "CleverTapFeatureFlagsDidUpdate"
        private const val CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE = "CleverTapProductConfigDidInitialize"
        private const val CLEVERTAP_PRODUCT_CONFIG_DID_FETCH = "CleverTapProductConfigDidFetch"
        private const val CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE = "CleverTapProductConfigDidActivate"
        private const val CLEVERTAP_PUSH_NOTIFICATION_CLICKED = "CleverTapPushNotificationClicked"
        private const val CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE = "CleverTapPushPermissionResponseReceived"
        private const val CLEVERTAP_ON_VARIABLES_CHANGED = "CleverTapOnVariablesChanged"
        private const val CLEVERTAP_ON_VALUE_CHANGED = "CleverTapOnValueChanged"
    }

    override fun getConstants(): Map<String, Any> {
        val constants: MutableMap<String, Any> = HashMap()
        constants[CLEVERTAP_PROFILE_DID_INITIALIZE] = CLEVERTAP_PROFILE_DID_INITIALIZE
        constants[CLEVERTAP_PROFILE_SYNC] = CLEVERTAP_PROFILE_SYNC
        constants[CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED] = CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED
        constants[CLEVERTAP_IN_APP_NOTIFICATION_SHOWED] = CLEVERTAP_IN_APP_NOTIFICATION_SHOWED
        constants[FCM] = FCM
        constants[BPS] = BPS
        constants[HPS] = HPS
        constants[CLEVERTAP_INBOX_DID_INITIALIZE] = CLEVERTAP_INBOX_DID_INITIALIZE
        constants[CLEVERTAP_INBOX_MESSAGES_DID_UPDATE] = CLEVERTAP_INBOX_MESSAGES_DID_UPDATE
        constants[CLEVERTAP_ON_INBOX_BUTTON_CLICK] = CLEVERTAP_ON_INBOX_BUTTON_CLICK
        constants[CLEVERTAP_ON_INBOX_MESSAGE_CLICK] = CLEVERTAP_ON_INBOX_MESSAGE_CLICK
        constants[CLEVERTAP_ON_DISPLAY_UNITS_LOADED] = CLEVERTAP_ON_DISPLAY_UNITS_LOADED
        constants[CLEVERTAP_ON_INAPP_BUTTON_CLICK] = CLEVERTAP_ON_INAPP_BUTTON_CLICK
        constants[CLEVERTAP_FEATURE_FLAGS_DID_UPDATE] = CLEVERTAP_FEATURE_FLAGS_DID_UPDATE
        constants[CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE] = CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE
        constants[CLEVERTAP_PRODUCT_CONFIG_DID_FETCH] = CLEVERTAP_PRODUCT_CONFIG_DID_FETCH
        constants[CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE] = CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE
        constants[CLEVERTAP_PUSH_NOTIFICATION_CLICKED] = CLEVERTAP_PUSH_NOTIFICATION_CLICKED
        constants[CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE] = CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE
        constants[CLEVERTAP_ON_VARIABLES_CHANGED] = CLEVERTAP_ON_VARIABLES_CHANGED
        constants[CLEVERTAP_ON_VALUE_CHANGED] = CLEVERTAP_ON_VALUE_CHANGED
        return constants
    }
}
