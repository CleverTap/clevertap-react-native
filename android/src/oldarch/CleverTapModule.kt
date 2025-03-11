package com.clevertap.react

import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

@Suppress("unused")
class CleverTapModule(reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {

    companion object {

        @Deprecated(
            message = "Use CleverTapRnAPI.setInitialUri(uri) instead",
            replaceWith = ReplaceWith(
                expression = "CleverTapRnAPI.setInitialUri(uri)",
                imports = ["com.clevertap.react.CleverTapRnAPI"]
            )
        )
        @JvmStatic
        fun setInitialUri(uri: Uri?) {
            CleverTapModuleImpl.setInitialUri(uri)
        }
    }

    private val cleverTapModuleImpl: CleverTapModuleImpl = CleverTapModuleImpl(reactContext!!)

    override fun getName(): String {
        return Constants.REACT_MODULE_NAME
    }

    @ReactMethod
    fun setLibrary(libName: String?, libVersion: Int) {
        cleverTapModuleImpl.setLibrary(libName, libVersion)
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
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Int,
        showBadge: Boolean
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
    fun customTemplateSetDismissed(templateName: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetDismissed(templateName, promise)
    }

    @ReactMethod
    fun customTemplateSetPresented(templateName: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetPresented(templateName, promise)
    }

    @ReactMethod
    fun customTemplateRunAction(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateRunAction(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateGetStringArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetStringArg(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateGetNumberArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetNumberArg(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateGetBooleanArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetBooleanArg(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateGetFileArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetFileArg(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateGetObjectArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetObjectArg(templateName, argName, promise)
    }

    @ReactMethod
    fun customTemplateContextToString(
        templateName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateContextToString(templateName, promise)
    }

    @ReactMethod
    fun syncCustomTemplates() {
        cleverTapModuleImpl.syncCustomTemplates()
    }

    @ReactMethod
    fun syncCustomTemplatesInProd(isProduction: Boolean) {
        cleverTapModuleImpl.syncCustomTemplates()
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
    fun defineFileVariable(name: String) {
        cleverTapModuleImpl.defineFileVariable(name)
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
    fun onOneTimeVariablesChanged() {
        cleverTapModuleImpl.onOneTimeVariablesChanged()
    }

    @ReactMethod
    fun onValueChanged(name: String) {
        cleverTapModuleImpl.onValueChanged(name)
    }

    @ReactMethod
    fun onFileValueChanged(name: String) {
        cleverTapModuleImpl.onFileValueChanged(name)
    }

    @ReactMethod
    fun onVariablesChangedAndNoDownloadsPending() {
        cleverTapModuleImpl.onVariablesChangedAndNoDownloadsPending()
    }

    @ReactMethod
    fun onceVariablesChangedAndNoDownloadsPending() {
        cleverTapModuleImpl.onceVariablesChangedAndNoDownloadsPending()
    }

    @ReactMethod
    fun onEventListenerAdded(eventName: String) {
        cleverTapModuleImpl.onEventListenerAdded(eventName)
    }

    @ReactMethod
    fun getUserEventLog(eventName: String, callback: Callback) {
        cleverTapModuleImpl.getUserEventLog(eventName, callback)
    }

    @ReactMethod
    fun getUserEventLogCount(eventName: String, callback: Callback) {
        cleverTapModuleImpl.getUserEventLogCount(eventName, callback)
    }

    @ReactMethod
    fun getUserLastVisitTs(callback: Callback) {
        cleverTapModuleImpl.getUserLastVisitTs(callback)
    }

    @ReactMethod
    fun getUserAppLaunchCount(callback: Callback) {
        cleverTapModuleImpl.getUserAppLaunchCount(callback)
    }

    @ReactMethod
    fun getUserEventLogHistory(callback: Callback) {
        cleverTapModuleImpl.getUserEventLogHistory(callback)
    }

    override fun getConstants(): Map<String, Any> {
        return cleverTapModuleImpl.getClevertapConstants()
    }
}
