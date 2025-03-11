package com.clevertap.react

import android.net.Uri
import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.clevertap.android.sdk.CleverTapAPI
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap

class CleverTapModule(reactContext: ReactApplicationContext?) :
    NativeCleverTapModuleSpec(reactContext) {

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

    override fun setLibrary(libName: String?, libVersion: Double) {
        cleverTapModuleImpl.setLibrary(libName, libVersion.toInt())
    }

    override fun setLocale(locale: String?) {
        cleverTapModuleImpl.setLocale(locale)
    }

    override fun activate() {
        cleverTapModuleImpl.activate()
    }

    //Custom Push Notification
    override fun createNotification(extras: ReadableMap?) {
        cleverTapModuleImpl.createNotification(extras)
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun createNotificationChannel(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Double,
        showBadge: Boolean
    ) {
        cleverTapModuleImpl.createNotificationChannel(
            channelId, channelName, channelDescription, importance.toInt(), showBadge
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun createNotificationChannelGroup(groupId: String?, groupName: String?) {
        cleverTapModuleImpl.createNotificationChannelGroup(groupId, groupName)
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun createNotificationChannelWithGroupId(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Double,
        groupId: String?,
        showBadge: Boolean
    ) {
        cleverTapModuleImpl.createNotificationChannelWithGroupId(
            channelId, channelName, channelDescription, importance.toInt(), groupId, showBadge
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun createNotificationChannelWithGroupIdAndSound(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Double,
        groupId: String?,
        showBadge: Boolean,
        sound: String?
    ) {
        cleverTapModuleImpl.createNotificationChannelWithGroupIdAndSound(
            channelId,
            channelName,
            channelDescription,
            importance.toInt(),
            groupId,
            showBadge,
            sound
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun createNotificationChannelWithSound(
        channelId: String?,
        channelName: String?,
        channelDescription: String?,
        importance: Double,
        showBadge: Boolean,
        sound: String?
    ) {
        cleverTapModuleImpl.createNotificationChannelWithSound(
            channelId, channelName, channelDescription, importance.toInt(), showBadge, sound
        )
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun deleteNotificationChannel(channelId: String?) {
        cleverTapModuleImpl.deleteNotificationChannel(channelId)
    }

    @RequiresApi(api = VERSION_CODES.O)
    override fun deleteNotificationChannelGroup(groupId: String?) {
        cleverTapModuleImpl.deleteNotificationChannelGroup(groupId)
    }

    //Push permission methods
    override fun promptForPushPermission(showFallbackSettings: Boolean) {
        cleverTapModuleImpl.promptForPushPermission(showFallbackSettings)
    }

    override fun promptPushPrimer(localInAppConfig: ReadableMap?) {
        cleverTapModuleImpl.promptPushPrimer(localInAppConfig)
    }

    override fun isPushPermissionGranted(callback: Callback?) {
        cleverTapModuleImpl.isPushPermissionGranted(callback)
    }

    override fun disablePersonalization() {
        cleverTapModuleImpl.disablePersonalization()
    }

    override fun enableDeviceNetworkInfoReporting(value: Boolean) {
        cleverTapModuleImpl.enableDeviceNetworkInfoReporting(value)
    }

    override fun enablePersonalization() {
        cleverTapModuleImpl.enablePersonalization()
    }

    override fun eventGetDetail(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetDetail(eventName, callback)
    }

    override fun eventGetFirstTime(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetFirstTime(eventName, callback)
    }

    override fun eventGetLastTime(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetLastTime(eventName, callback)
    }

    override fun eventGetOccurrences(eventName: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetOccurrences(eventName, callback)
    }

    override fun fetch() {
        cleverTapModuleImpl.fetch()
    }

    override fun fetchAndActivate() {
        cleverTapModuleImpl.fetchAndActivate()
    }

    override fun fetchWithMinimumFetchIntervalInSeconds(interval: Double) {
        cleverTapModuleImpl.fetchWithMinimumFetchIntervalInSeconds(interval.toInt())
    }

    override fun getAllDisplayUnits(callback: Callback?) {
        cleverTapModuleImpl.getAllDisplayUnits(callback)
    }

    override fun getDisplayUnitForId(unitID: String?, callback: Callback?) {
        cleverTapModuleImpl.getDisplayUnitForId(unitID, callback)
    }

    override fun getBoolean(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getBoolean(key, callback)
    }

    override fun getDouble(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getDouble(key, callback)
    }

    override fun getEventHistory(callback: Callback?) {
        cleverTapModuleImpl.getEventHistory(callback)
    }

    override fun getFeatureFlag(name: String?, defaultValue: Boolean, callback: Callback?) {
        cleverTapModuleImpl.getFeatureFlag(name, defaultValue, callback)
    }

    override fun getAllInboxMessages(callback: Callback?) {
        cleverTapModuleImpl.getAllInboxMessages(callback)
    }

    override fun getInboxMessageCount(callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageCount(callback)
    }

    override fun getInboxMessageForId(messageId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageForId(messageId, callback)
    }

    override fun getInboxMessageUnreadCount(callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageUnreadCount(callback)
    }

    override fun deleteInboxMessageForId(messageId: String?) {
        cleverTapModuleImpl.deleteInboxMessageForId(messageId)
    }

    override fun getUnreadInboxMessages(callback: Callback?) {
        cleverTapModuleImpl.getUnreadInboxMessages(callback)
    }

    override fun initializeInbox() {
        cleverTapModuleImpl.initializeInbox()
    }

    override fun markReadInboxMessageForId(messageId: String?) {
        cleverTapModuleImpl.markReadInboxMessageForId(messageId)
    }

    override fun markReadInboxMessagesForIDs(messageIDs: ReadableArray?) {
        cleverTapModuleImpl.markReadInboxMessagesForIDs(messageIDs)
    }

    override fun deleteInboxMessagesForIDs(messageIDs: ReadableArray?) {
        cleverTapModuleImpl.deleteInboxMessagesForIDs(messageIDs)
    }

    override fun pushInboxNotificationClickedEventForId(messageId: String?) {
        cleverTapModuleImpl.pushInboxNotificationClickedEventForId(messageId)
    }

    override fun pushInboxNotificationViewedEventForId(messageId: String?) {
        cleverTapModuleImpl.pushInboxNotificationViewedEventForId(messageId)
    }

    override fun showInbox(styleConfig: ReadableMap?) {
        cleverTapModuleImpl.showInbox(styleConfig)
    }

    override fun dismissInbox() {
        cleverTapModuleImpl.dismissInbox()
    }

    override fun getInitialUrl(callback: Callback?) {
        cleverTapModuleImpl.getInitialUrl(callback)
    }

    override fun getLastFetchTimeStampInMillis(callback: Callback?) {
        cleverTapModuleImpl.getLastFetchTimeStampInMillis(callback)
    }

    override fun getString(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getString(key, callback)
    }

    override fun onUserLogin(profile: ReadableMap?) {
        cleverTapModuleImpl.onUserLogin(profile)
    }

    override fun profileAddMultiValue(value: String?, key: String?) {
        cleverTapModuleImpl.profileAddMultiValue(value, key)
    }

    override fun profileAddMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileAddMultiValues(values, key)
    }

    override fun profileGetCleverTapAttributionIdentifier(callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapAttributionIdentifier(callback)
    }

    override fun profileGetCleverTapID(callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapID(callback)
    }

    override fun getCleverTapID(callback: Callback?) {
        cleverTapModuleImpl.getCleverTapID(callback)
    }

    override fun profileGetProperty(propertyName: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetProperty(propertyName, callback)
    }

    override fun profileRemoveMultiValue(value: String?, key: String?) {
        cleverTapModuleImpl.profileRemoveMultiValue(value, key)
    }

    override fun profileRemoveMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileRemoveMultiValues(values, key)
    }

    override fun profileRemoveValueForKey(key: String?) {
        cleverTapModuleImpl.profileRemoveValueForKey(key)
    }

    override fun profileSet(profile: ReadableMap?) {
        cleverTapModuleImpl.profileSet(profile)
    }

    override fun profileSetMultiValues(values: ReadableArray?, key: String?) {
        cleverTapModuleImpl.profileSetMultiValues(values, key)
    }

    override fun pushDisplayUnitClickedEventForID(unitID: String?) {
        cleverTapModuleImpl.pushDisplayUnitClickedEventForID(unitID)
    }

    override fun pushDisplayUnitViewedEventForID(unitID: String?) {
        cleverTapModuleImpl.pushDisplayUnitViewedEventForID(unitID)
    }

    override fun pushInstallReferrer(source: String?, medium: String?, campaign: String?) {
        cleverTapModuleImpl.pushInstallReferrer(source, medium, campaign)
    }

    override fun recordChargedEvent(details: ReadableMap?, items: ReadableArray?) {
        cleverTapModuleImpl.recordChargedEvent(details, items)
    }

    override fun recordEvent(eventName: String?, props: ReadableMap?) {
        cleverTapModuleImpl.recordEvent(eventName, props)
    }

    override fun recordScreenView(screenName: String?) {
        cleverTapModuleImpl.recordScreenView(screenName)
    }

    // Product Config methods

    override fun registerForPush() {
        cleverTapModuleImpl.registerForPush()
    }

    override fun reset() {
        cleverTapModuleImpl.reset()
    }

    // Feature Flag methods

    override fun sessionGetPreviousVisitTime(callback: Callback?) {
        cleverTapModuleImpl.sessionGetPreviousVisitTime(callback)
    }

    // Developer Options

    override fun sessionGetScreenCount(callback: Callback?) {
        cleverTapModuleImpl.sessionGetScreenCount(callback)
    }

    override fun sessionGetTimeElapsed(callback: Callback?) {
        cleverTapModuleImpl.sessionGetTimeElapsed(callback)
    }

    override fun sessionGetTotalVisits(callback: Callback?) {
        cleverTapModuleImpl.sessionGetTotalVisits(callback)
    }

    override fun sessionGetUTMDetails(callback: Callback?) {
        cleverTapModuleImpl.sessionGetUTMDetails(callback)
    }

    override fun setDebugLevel(level: Double) {
        CleverTapAPI.setDebugLevel(level.toInt())
    }

    override fun setDefaultsMap(map: ReadableMap?) {
        cleverTapModuleImpl.setDefaultsMap(map)
    }

    override fun setLocation(latitude: Double, longitude: Double) {
        cleverTapModuleImpl.setLocation(latitude, longitude)
    }

    override fun setMinimumFetchIntervalInSeconds(interval: Double) {
        cleverTapModuleImpl.setMinimumFetchIntervalInSeconds(interval.toInt())
    }

    //Sets the SDK to offline mode
    override fun setOffline(value: Boolean) {
        cleverTapModuleImpl.setOffline(value)
    }

    override fun setOptOut(value: Boolean) {
        cleverTapModuleImpl.setOptOut(value)
    }

    override fun setPushTokenAsString(token: String?, type: String?) {
        cleverTapModuleImpl.setPushTokenAsString(token, type)
    }

    // Increment/Decrement Operator

    override fun profileIncrementValueForKey(value: Double?, key: String?) {
        cleverTapModuleImpl.profileIncrementValueForKey(value, key)
    }

    override fun profileDecrementValueForKey(value: Double?, key: String?) {
        cleverTapModuleImpl.profileDecrementValueForKey(value, key)
    }

    // InApp Controls

    override fun suspendInAppNotifications() {
        cleverTapModuleImpl.suspendInAppNotifications()
    }

    override fun discardInAppNotifications() {
        cleverTapModuleImpl.discardInAppNotifications()
    }

    override fun resumeInAppNotifications() {
        cleverTapModuleImpl.resumeInAppNotifications()
    }

    override fun setInstanceWithAccountId(accountId: String?) {
        cleverTapModuleImpl.setInstanceWithAccountId(accountId)
    }

    override fun fetchInApps(callback: Callback?) {
        cleverTapModuleImpl.fetchInApps(callback)
    }

    override fun clearInAppResources(expiredOnly: Boolean) {
        cleverTapModuleImpl.clearInAppResources(expiredOnly)
    }

    override fun customTemplateSetDismissed(templateName: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetDismissed(templateName, promise)
    }

    override fun customTemplateSetPresented(templateName: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetPresented(templateName, promise)
    }

    override fun customTemplateRunAction(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateRunAction(templateName, argName, promise)
    }

    override fun customTemplateGetStringArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetStringArg(templateName, argName, promise)
    }

    override fun customTemplateGetNumberArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetNumberArg(templateName, argName, promise)
    }

    override fun customTemplateGetBooleanArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetBooleanArg(templateName, argName, promise)
    }

    override fun customTemplateGetFileArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetFileArg(templateName, argName, promise)
    }

    override fun customTemplateGetObjectArg(
        templateName: String?,
        argName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetObjectArg(templateName, argName, promise)
    }

    override fun customTemplateContextToString(
        templateName: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateContextToString(templateName, promise)
    }

    override fun syncCustomTemplates() {
        cleverTapModuleImpl.syncCustomTemplates()
    }

    override fun syncCustomTemplatesInProd(isProduction: Boolean) {
        cleverTapModuleImpl.syncCustomTemplates()
    }

    override fun syncVariables() {
        cleverTapModuleImpl.syncVariables()
    }

    override fun syncVariablesinProd(isProduction: Boolean) {
        cleverTapModuleImpl.syncVariablesinProd(isProduction, null)
    }

    override fun defineVariables(`object`: ReadableMap?) {
        cleverTapModuleImpl.defineVariables(`object`)
    }

    override fun defineFileVariable(name: String) {
        cleverTapModuleImpl.defineFileVariable(name)
    }

    override fun fetchVariables(callback: Callback?) {
        cleverTapModuleImpl.fetchVariables(callback)
    }

    override fun getVariable(key: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariable(key, callback)
    }

    override fun getVariables(callback: Callback?) {
        cleverTapModuleImpl.getVariables(callback)
    }

    override fun onVariablesChanged() {
        cleverTapModuleImpl.onVariablesChanged()
    }

    override fun onOneTimeVariablesChanged() {
        cleverTapModuleImpl.onOneTimeVariablesChanged()
    }

    override fun onValueChanged(name: String) {
        cleverTapModuleImpl.onValueChanged(name)
    }

    override fun onFileValueChanged(name: String) {
        cleverTapModuleImpl.onFileValueChanged(name)
    }

    override fun onVariablesChangedAndNoDownloadsPending() {
        cleverTapModuleImpl.onVariablesChangedAndNoDownloadsPending()
    }

    override fun onceVariablesChangedAndNoDownloadsPending() {
        cleverTapModuleImpl.onceVariablesChangedAndNoDownloadsPending()
    }

    override fun onEventListenerAdded(eventName: String) {
        cleverTapModuleImpl.onEventListenerAdded(eventName)
    }

    override fun addListener(name: String) {
        return
    }

    override fun removeListeners(count: Double) {
        return
    }

    override fun setPushTokenAsStringWithRegion(
        token: String?,
        withType: String?,
        withRegion: String?
    ) {
        return
    }

    override fun getUserEventLog(eventName: String, callback: Callback?) {
        cleverTapModuleImpl.getUserEventLog(eventName, callback)
    }

    override fun getUserEventLogCount(eventName: String, callback: Callback?) {
        cleverTapModuleImpl.getUserEventLogCount(eventName, callback)
    }

    override fun getUserLastVisitTs(callback: Callback?) {
        cleverTapModuleImpl.getUserLastVisitTs(callback)
    }

    override fun getUserAppLaunchCount(callback: Callback?) {
        cleverTapModuleImpl.getUserAppLaunchCount(callback)
    }

    override fun getUserEventLogHistory(callback: Callback?) {
        cleverTapModuleImpl.getUserEventLogHistory(callback)
    }


    override fun getTypedExportedConstants(): Map<String, Any> {
        return cleverTapModuleImpl.getClevertapConstants()
    }
}
