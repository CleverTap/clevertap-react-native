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
    fun setLocale(locale: String?, accountId: String?) {
        cleverTapModuleImpl.setLocale(locale, accountId)
    }

    @ReactMethod
    fun activate(accountId: String?) {
        cleverTapModuleImpl.activate(accountId)
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
    fun disablePersonalization(accountId: String?) {
        cleverTapModuleImpl.disablePersonalization(accountId)
    }

    @ReactMethod
    fun enableDeviceNetworkInfoReporting(value: Boolean, accountId: String?) {
        cleverTapModuleImpl.enableDeviceNetworkInfoReporting(value, accountId)
    }

    @ReactMethod
    fun enablePersonalization(accountId: String?) {
        cleverTapModuleImpl.enablePersonalization(accountId)
    }

    @ReactMethod
    fun eventGetDetail(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetDetail(eventName, accountId, callback)
    }

    @ReactMethod
    fun eventGetFirstTime(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetFirstTime(eventName, accountId, callback)
    }

    @ReactMethod
    fun eventGetLastTime(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetLastTime(eventName, accountId, callback)
    }

    @ReactMethod
    fun eventGetOccurrences(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetOccurrences(eventName, accountId, callback)
    }

    @ReactMethod
    fun fetch(accountId: String?) {
        cleverTapModuleImpl.fetch(accountId)
    }

    @ReactMethod
    fun fetchAndActivate(accountId: String?) {
        cleverTapModuleImpl.fetchAndActivate(accountId)
    }

    @ReactMethod
    fun fetchWithMinimumFetchIntervalInSeconds(interval: Int, accountId: String?) {
        cleverTapModuleImpl.fetchWithMinimumFetchIntervalInSeconds(interval, accountId)
    }

    @ReactMethod
    fun getAllDisplayUnits(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getAllDisplayUnits(accountId, callback)
    }

    @ReactMethod
    fun getBoolean(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getBoolean(key, accountId, callback)
    }

    @ReactMethod
    fun getDisplayUnitForId(unitID: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getDisplayUnitForId(unitID, accountId, callback)
    }

    @ReactMethod
    fun getDouble(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getDouble(key, accountId, callback)
    }

    @ReactMethod
    fun getEventHistory(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getEventHistory(accountId, callback)
    }

    @ReactMethod
    fun getFeatureFlag(name: String?, defaultValue: Boolean?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getFeatureFlag(name, defaultValue, accountId, callback)
    }

    @ReactMethod
    fun getAllInboxMessages(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getAllInboxMessages(accountId, callback)
    }

    @ReactMethod
    fun getInboxMessageCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageCount(accountId, callback)
    }

    @ReactMethod
    fun getInboxMessageForId(messageId: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageForId(messageId, accountId, callback)
    }

    @ReactMethod
    fun getInboxMessageUnreadCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageUnreadCount(accountId, callback)
    }

    @ReactMethod
    fun deleteInboxMessageForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.deleteInboxMessageForId(messageId, accountId)
    }

    @ReactMethod
    fun getUnreadInboxMessages(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUnreadInboxMessages(accountId, callback)
    }

    @ReactMethod
    fun initializeInbox(accountId: String?) {
        cleverTapModuleImpl.initializeInbox(accountId)
    }

    @ReactMethod
    fun markReadInboxMessageForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.markReadInboxMessageForId(messageId, accountId)
    }

    @ReactMethod
    fun markReadInboxMessagesForIDs(messageIDs: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.markReadInboxMessagesForIDs(messageIDs, accountId)
    }

    @ReactMethod
    fun deleteInboxMessagesForIDs(messageIDs: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.deleteInboxMessagesForIDs(messageIDs, accountId)
    }

    @ReactMethod
    fun pushInboxNotificationClickedEventForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.pushInboxNotificationClickedEventForId(messageId, accountId)
    }

    @ReactMethod
    fun pushInboxNotificationViewedEventForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.pushInboxNotificationViewedEventForId(messageId, accountId)
    }

    @ReactMethod
    fun fetchInbox(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchInbox(accountId, callback)
    }

    @ReactMethod
    fun showInbox(styleConfig: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.showInbox(styleConfig, accountId)
    }

    @ReactMethod
    fun dismissInbox(accountId: String?) {
        cleverTapModuleImpl.dismissInbox(accountId)
    }

    @ReactMethod
    fun getInitialUrl(callback: Callback?) {
        cleverTapModuleImpl.getInitialUrl(callback)
    }

    @ReactMethod
    fun getLastFetchTimeStampInMillis(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getLastFetchTimeStampInMillis(accountId, callback)
    }

    @ReactMethod
    fun getString(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getString(key, accountId, callback)
    }

    @ReactMethod
    fun onUserLogin(profile: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.onUserLogin(profile, accountId)
    }

    @ReactMethod
    fun profileAddMultiValue(value: String?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileAddMultiValue(value, key, accountId)
    }

    @ReactMethod
    fun profileAddMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileAddMultiValues(values, key, accountId)
    }

    @ReactMethod
    fun profileGetCleverTapAttributionIdentifier(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapAttributionIdentifier(accountId, callback)
    }

    @ReactMethod
    fun profileGetCleverTapID(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapID(accountId, callback)
    }

    @ReactMethod
    fun getCleverTapID(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getCleverTapID(accountId, callback)
    }

    @ReactMethod
    fun profileGetProperty(propertyName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetProperty(propertyName, accountId, callback)
    }

    @ReactMethod
    fun profileRemoveMultiValue(value: String?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveMultiValue(value, key, accountId)
    }

    @ReactMethod
    fun profileRemoveMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveMultiValues(values, key, accountId)
    }

    @ReactMethod
    fun profileRemoveValueForKey(key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveValueForKey(key, accountId)
    }

    @ReactMethod
    fun profileSet(profile: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.profileSet(profile, accountId)
    }

    @ReactMethod
    fun profileSetMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileSetMultiValues(values, key, accountId)
    }

    @ReactMethod
    fun pushDisplayUnitClickedEventForID(unitID: String?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitClickedEventForID(unitID, accountId)
    }

    @ReactMethod
    fun pushDisplayUnitViewedEventForID(unitID: String?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitViewedEventForID(unitID, accountId)
    }

    @ReactMethod
    fun pushDisplayUnitElementClickedEventForID(unitID: String?, additionalProperties: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitElementClickedEventForID(unitID, additionalProperties, accountId)
    }

    @ReactMethod
    fun pushInstallReferrer(source: String?, medium: String?, campaign: String?, accountId: String?) {
        cleverTapModuleImpl.pushInstallReferrer(source, medium, campaign, accountId)
    }

    @ReactMethod
    fun recordChargedEvent(details: ReadableMap?, items: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.recordChargedEvent(details, items, accountId)
    }

    @ReactMethod
    fun recordEvent(eventName: String?, props: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.recordEvent(eventName, props, accountId)
    }

    @ReactMethod
    fun recordScreenView(screenName: String?, accountId: String?) {
        cleverTapModuleImpl.recordScreenView(screenName, accountId)
    }

    // Product Config methods
    @ReactMethod
    fun registerForPush() {
        cleverTapModuleImpl.registerForPush()
    }

    @ReactMethod
    fun reset(accountId: String?) {
        cleverTapModuleImpl.reset(accountId)
    }

    // Feature Flag methods
    @ReactMethod
    fun sessionGetPreviousVisitTime(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetPreviousVisitTime(accountId, callback)
    }

    // Developer Options
    @ReactMethod
    fun sessionGetScreenCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetScreenCount(accountId, callback)
    }

    @ReactMethod
    fun sessionGetTimeElapsed(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetTimeElapsed(accountId, callback)
    }

    @ReactMethod
    fun sessionGetTotalVisits(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetTotalVisits(accountId, callback)
    }

    @ReactMethod
    fun sessionGetUTMDetails(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetUTMDetails(accountId, callback)
    }

    @ReactMethod
    fun setDebugLevel(level: Int) {
        CleverTapAPI.setDebugLevel(level)
    }

    @ReactMethod
    fun setDefaultsMap(map: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.setDefaultsMap(map, accountId)
    }

    @ReactMethod
    fun setLocation(latitude: Double, longitude: Double, accountId: String?) {
        cleverTapModuleImpl.setLocation(latitude, longitude, accountId)
    }

    @ReactMethod
    fun setMinimumFetchIntervalInSeconds(interval: Int, accountId: String?) {
        cleverTapModuleImpl.setMinimumFetchIntervalInSeconds(interval, accountId)
    }

    //Sets the SDK to offline mode
    @ReactMethod
    fun setOffline(value: Boolean, accountId: String?) {
        cleverTapModuleImpl.setOffline(value, accountId)
    }

    @ReactMethod
    fun setOptOut(userOptOut: Boolean, allowSystemEvents: Boolean?, accountId: String?) {
        cleverTapModuleImpl.setOptOut(userOptOut, allowSystemEvents, accountId)
    }

    @ReactMethod
    fun pushRegistrationToken(token: String?, type: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.pushRegistrationToken(token, type, accountId)
    }

    @ReactMethod
    fun setFCMPushTokenAsString(token: String?, accountId: String?) {
        cleverTapModuleImpl.setFCMPushTokenAsString(token, accountId)
    }

    // iOS-only method; present so JS can call it uniformly on both architectures.
    @ReactMethod
    fun setPushTokenAsStringWithRegion(
        token: String?,
        withType: String?,
        withRegion: String?,
        accountId: String?
    ) {
        return
    }

    // Increment/Decrement Operator
    @ReactMethod
    fun profileIncrementValueForKey(value: Double?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileIncrementValueForKey(value, key, accountId)
    }

    @ReactMethod
    fun profileDecrementValueForKey(value: Double?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileDecrementValueForKey(value, key, accountId)
    }

    // InApp Controls
    @ReactMethod
    fun suspendInAppNotifications(accountId: String?) {
        cleverTapModuleImpl.suspendInAppNotifications(accountId)
    }

    @ReactMethod
    fun discardInAppNotifications(dismissInAppIfVisible: Boolean?, accountId: String?) {
        cleverTapModuleImpl.discardInAppNotifications(dismissInAppIfVisible, accountId)
    }

    @ReactMethod
    fun resumeInAppNotifications(accountId: String?) {
        cleverTapModuleImpl.resumeInAppNotifications(accountId)
    }

    @ReactMethod
    fun dismissPipInApp(accountId: String?) {
        cleverTapModuleImpl.dismissPipInApp(accountId)
    }

    @ReactMethod
    fun unmute(accountId: String?) {
        cleverTapModuleImpl.unmute(accountId)
    }

    @ReactMethod
    fun setInstanceWithAccountId(accountId: String?) {
        cleverTapModuleImpl.setInstanceWithAccountId(accountId)
    }

    @ReactMethod
    fun createInstance(config: ReadableMap?, promise: Promise?) {
        cleverTapModuleImpl.createInstance(config, promise)
    }

    @ReactMethod
    fun getDefaultAccountId(promise: Promise?) {
        cleverTapModuleImpl.getDefaultAccountId(promise)
    }

    @ReactMethod
    fun fetchInApps(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchInApps(accountId, callback)
    }

    @ReactMethod
    fun clearInAppResources(expiredOnly: Boolean, accountId: String?) {
        cleverTapModuleImpl.clearInAppResources(expiredOnly, accountId)
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
    fun variants(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.variants(accountId, callback)
    }

    @ReactMethod
    fun syncVariables(accountId: String?) {
        cleverTapModuleImpl.syncVariables(accountId)
    }

    @ReactMethod
    fun syncVariablesinProd(isProduction: Boolean, accountId: String?) {
        // Must match the spec's (isProduction, accountId) shape — the old-arch bridge
        // checks the exact argument count. The impl's callback param is unused (no-op
        // on Android), so pass null like the new-arch shim does.
        cleverTapModuleImpl.syncVariablesinProd(isProduction, accountId)
    }

    @ReactMethod
    fun defineVariables(`object`: ReadableMap, accountId: String?) {
        cleverTapModuleImpl.defineVariables(`object`, accountId)
    }

    @ReactMethod
    fun defineFileVariable(name: String, accountId: String?) {
        cleverTapModuleImpl.defineFileVariable(name, accountId)
    }

    @ReactMethod
    fun fetchVariables(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchVariables(accountId, callback)
    }

    @ReactMethod
    fun getVariable(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariable(key, accountId, callback)
    }

    @ReactMethod
    fun getVariables(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariables(accountId, callback)
    }

    @ReactMethod
    fun onVariablesChanged(accountId: String?) {
        cleverTapModuleImpl.onVariablesChanged(accountId)
    }

    @ReactMethod
    fun onOneTimeVariablesChanged(accountId: String?) {
        cleverTapModuleImpl.onOneTimeVariablesChanged(accountId)
    }

    @ReactMethod
    fun onValueChanged(name: String, accountId: String?) {
        cleverTapModuleImpl.onValueChanged(name, accountId)
    }

    @ReactMethod
    fun onFileValueChanged(name: String, accountId: String?) {
        cleverTapModuleImpl.onFileValueChanged(name, accountId)
    }

    @ReactMethod
    fun onVariablesChangedAndNoDownloadsPending(accountId: String?) {
        cleverTapModuleImpl.onVariablesChangedAndNoDownloadsPending(accountId)
    }

    @ReactMethod
    fun onceVariablesChangedAndNoDownloadsPending(accountId: String?) {
        cleverTapModuleImpl.onceVariablesChangedAndNoDownloadsPending(accountId)
    }

    @ReactMethod
    fun onEventListenerAdded(eventName: String, accountId: String?) {
        cleverTapModuleImpl.onEventListenerAdded(eventName, accountId)
    }

    @ReactMethod
    fun getUserEventLog(eventName: String, accountId: String?, callback: Callback) {
        cleverTapModuleImpl.getUserEventLog(eventName, accountId, callback)
    }

    @ReactMethod
    fun getUserEventLogCount(eventName: String, accountId: String?, callback: Callback) {
        cleverTapModuleImpl.getUserEventLogCount(eventName, accountId, callback)
    }

    @ReactMethod
    fun getUserLastVisitTs(accountId: String?, callback: Callback) {
        cleverTapModuleImpl.getUserLastVisitTs(accountId, callback)
    }

    @ReactMethod
    fun getUserAppLaunchCount(accountId: String?, callback: Callback) {
        cleverTapModuleImpl.getUserAppLaunchCount(accountId, callback)
    }

    @ReactMethod
    fun getUserEventLogHistory(accountId: String?, callback: Callback) {
        cleverTapModuleImpl.getUserEventLogHistory(accountId, callback)
    }

    override fun getConstants(): Map<String, Any> {
        return cleverTapModuleImpl.getClevertapConstants()
    }
}
