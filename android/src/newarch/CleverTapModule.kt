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

    override fun setLocale(locale: String?, accountId: String?) {
        cleverTapModuleImpl.setLocale(locale, accountId)
    }

    override fun activate(accountId: String?) {
        cleverTapModuleImpl.activate(accountId)
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

    override fun disablePersonalization(accountId: String?) {
        cleverTapModuleImpl.disablePersonalization(accountId)
    }

    override fun enableDeviceNetworkInfoReporting(value: Boolean, accountId: String?) {
        cleverTapModuleImpl.enableDeviceNetworkInfoReporting(value, accountId)
    }

    override fun enablePersonalization(accountId: String?) {
        cleverTapModuleImpl.enablePersonalization(accountId)
    }

    override fun eventGetDetail(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetDetail(eventName, accountId, callback)
    }

    override fun eventGetFirstTime(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetFirstTime(eventName, accountId, callback)
    }

    override fun eventGetLastTime(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetLastTime(eventName, accountId, callback)
    }

    override fun eventGetOccurrences(eventName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.eventGetOccurrences(eventName, accountId, callback)
    }

    override fun fetch(accountId: String?) {
        cleverTapModuleImpl.fetch(accountId)
    }

    override fun fetchAndActivate(accountId: String?) {
        cleverTapModuleImpl.fetchAndActivate(accountId)
    }

    override fun fetchWithMinimumFetchIntervalInSeconds(interval: Double, accountId: String?) {
        cleverTapModuleImpl.fetchWithMinimumFetchIntervalInSeconds(interval.toInt(), accountId)
    }

    override fun getAllDisplayUnits(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getAllDisplayUnits(accountId, callback)
    }

    override fun getDisplayUnitForId(unitID: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getDisplayUnitForId(unitID, accountId, callback)
    }

    override fun getBoolean(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getBoolean(key, accountId, callback)
    }

    override fun getDouble(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getDouble(key, accountId, callback)
    }

    override fun getEventHistory(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getEventHistory(accountId, callback)
    }

    override fun getFeatureFlag(name: String?, defaultValue: Boolean, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getFeatureFlag(name, defaultValue, accountId, callback)
    }

    override fun getAllInboxMessages(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getAllInboxMessages(accountId, callback)
    }

    override fun getInboxMessageCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageCount(accountId, callback)
    }

    override fun getInboxMessageForId(messageId: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageForId(messageId, accountId, callback)
    }

    override fun getInboxMessageUnreadCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getInboxMessageUnreadCount(accountId, callback)
    }

    override fun deleteInboxMessageForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.deleteInboxMessageForId(messageId, accountId)
    }

    override fun getUnreadInboxMessages(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUnreadInboxMessages(accountId, callback)
    }

    override fun initializeInbox(accountId: String?) {
        cleverTapModuleImpl.initializeInbox(accountId)
    }

    override fun markReadInboxMessageForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.markReadInboxMessageForId(messageId, accountId)
    }

    override fun markReadInboxMessagesForIDs(messageIDs: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.markReadInboxMessagesForIDs(messageIDs, accountId)
    }

    override fun deleteInboxMessagesForIDs(messageIDs: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.deleteInboxMessagesForIDs(messageIDs, accountId)
    }

    override fun pushInboxNotificationClickedEventForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.pushInboxNotificationClickedEventForId(messageId, accountId)
    }

    override fun pushInboxNotificationViewedEventForId(messageId: String?, accountId: String?) {
        cleverTapModuleImpl.pushInboxNotificationViewedEventForId(messageId, accountId)
    }

    override fun fetchInbox(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchInbox(accountId, callback)
    }

    override fun showInbox(styleConfig: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.showInbox(styleConfig, accountId)
    }

    override fun dismissInbox(accountId: String?) {
        cleverTapModuleImpl.dismissInbox(accountId)
    }

    override fun getInitialUrl(callback: Callback?) {
        cleverTapModuleImpl.getInitialUrl(callback)
    }

    override fun getLastFetchTimeStampInMillis(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getLastFetchTimeStampInMillis(accountId, callback)
    }

    override fun getString(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getString(key, accountId, callback)
    }

    override fun onUserLogin(profile: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.onUserLogin(profile, accountId)
    }

    override fun profileAddMultiValue(value: String?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileAddMultiValue(value, key, accountId)
    }

    override fun profileAddMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileAddMultiValues(values, key, accountId)
    }

    override fun profileGetCleverTapAttributionIdentifier(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapAttributionIdentifier(accountId, callback)
    }

    override fun profileGetCleverTapID(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetCleverTapID(accountId, callback)
    }

    override fun getCleverTapID(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getCleverTapID(accountId, callback)
    }

    override fun profileGetProperty(propertyName: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.profileGetProperty(propertyName, accountId, callback)
    }

    override fun profileRemoveMultiValue(value: String?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveMultiValue(value, key, accountId)
    }

    override fun profileRemoveMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveMultiValues(values, key, accountId)
    }

    override fun profileRemoveValueForKey(key: String?, accountId: String?) {
        cleverTapModuleImpl.profileRemoveValueForKey(key, accountId)
    }

    override fun profileSet(profile: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.profileSet(profile, accountId)
    }

    override fun profileSetMultiValues(values: ReadableArray?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileSetMultiValues(values, key, accountId)
    }

    override fun pushDisplayUnitClickedEventForID(unitID: String?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitClickedEventForID(unitID, accountId)
    }

    override fun pushDisplayUnitViewedEventForID(unitID: String?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitViewedEventForID(unitID, accountId)
    }

    override fun pushDisplayUnitElementClickedEventForID(unitID: String?, additionalProperties: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.pushDisplayUnitElementClickedEventForID(unitID, additionalProperties, accountId)
    }

    override fun pushInstallReferrer(source: String?, medium: String?, campaign: String?, accountId: String?) {
        cleverTapModuleImpl.pushInstallReferrer(source, medium, campaign, accountId)
    }

    override fun recordChargedEvent(details: ReadableMap?, items: ReadableArray?, accountId: String?) {
        cleverTapModuleImpl.recordChargedEvent(details, items, accountId)
    }

    override fun recordEvent(eventName: String?, props: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.recordEvent(eventName, props, accountId)
    }

    override fun recordScreenView(screenName: String?, accountId: String?) {
        cleverTapModuleImpl.recordScreenView(screenName, accountId)
    }

    // Product Config methods

    override fun registerForPush() {
        cleverTapModuleImpl.registerForPush()
    }

    override fun reset(accountId: String?) {
        cleverTapModuleImpl.reset(accountId)
    }

    // Feature Flag methods

    override fun sessionGetPreviousVisitTime(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetPreviousVisitTime(accountId, callback)
    }

    // Developer Options

    override fun sessionGetScreenCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetScreenCount(accountId, callback)
    }

    override fun sessionGetTimeElapsed(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetTimeElapsed(accountId, callback)
    }

    override fun sessionGetTotalVisits(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetTotalVisits(accountId, callback)
    }

    override fun sessionGetUTMDetails(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.sessionGetUTMDetails(accountId, callback)
    }

    override fun setDebugLevel(level: Double) {
        CleverTapAPI.setDebugLevel(level.toInt())
    }

    override fun setDefaultsMap(map: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.setDefaultsMap(map, accountId)
    }

    override fun setLocation(latitude: Double, longitude: Double, accountId: String?) {
        cleverTapModuleImpl.setLocation(latitude, longitude, accountId)
    }

    override fun setMinimumFetchIntervalInSeconds(interval: Double, accountId: String?) {
        cleverTapModuleImpl.setMinimumFetchIntervalInSeconds(interval.toInt(), accountId)
    }

    //Sets the SDK to offline mode
    override fun setOffline(value: Boolean, accountId: String?) {
        cleverTapModuleImpl.setOffline(value, accountId)
    }

    override fun setOptOut(userOptOut: Boolean, allowSystemEvents: Boolean?, accountId: String?) {
        cleverTapModuleImpl.setOptOut(userOptOut, allowSystemEvents, accountId)
    }

    override fun pushRegistrationToken(token: String?, pushType: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.pushRegistrationToken(token, pushType, accountId)
    }

    override fun setFCMPushTokenAsString(token: String?, accountId: String?) {
        cleverTapModuleImpl.setFCMPushTokenAsString(token, accountId)
    }

    // Increment/Decrement Operator

    override fun profileIncrementValueForKey(value: Double?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileIncrementValueForKey(value, key, accountId)
    }

    override fun profileDecrementValueForKey(value: Double?, key: String?, accountId: String?) {
        cleverTapModuleImpl.profileDecrementValueForKey(value, key, accountId)
    }

    // InApp Controls

    override fun suspendInAppNotifications(accountId: String?) {
        cleverTapModuleImpl.suspendInAppNotifications(accountId)
    }

    override fun discardInAppNotifications(dismissInAppIfVisible: Boolean?, accountId: String?) {
        cleverTapModuleImpl.discardInAppNotifications(dismissInAppIfVisible, accountId)
    }

    override fun resumeInAppNotifications(accountId: String?) {
        cleverTapModuleImpl.resumeInAppNotifications(accountId)
    }

    override fun dismissPipInApp(accountId: String?) {
        cleverTapModuleImpl.dismissPipInApp(accountId)
    }

    override fun unmute(accountId: String?) {
        cleverTapModuleImpl.unmute(accountId)
    }

    override fun setInstanceWithAccountId(accountId: String?) {
        cleverTapModuleImpl.setInstanceWithAccountId(accountId)
    }

    override fun createInstance(config: ReadableMap?, promise: Promise?) {
        cleverTapModuleImpl.createInstance(config, promise)
    }

    override fun getDefaultAccountId(promise: Promise?) {
        cleverTapModuleImpl.getDefaultAccountId(promise)
    }

    override fun fetchInApps(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchInApps(accountId, callback)
    }

    override fun clearInAppResources(expiredOnly: Boolean, accountId: String?) {
        cleverTapModuleImpl.clearInAppResources(expiredOnly, accountId)
    }

    override fun customTemplateSetDismissed(templateName: String?, accountId: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetDismissed(templateName, accountId, promise)
    }

    override fun customTemplateSetPresented(templateName: String?, accountId: String?, promise: Promise?) {
        cleverTapModuleImpl.customTemplateSetPresented(templateName, accountId, promise)
    }

    override fun customTemplateRunAction(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateRunAction(templateName, argName, accountId, promise)
    }

    override fun customTemplateGetStringArg(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetStringArg(templateName, argName, accountId, promise)
    }

    override fun customTemplateGetNumberArg(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetNumberArg(templateName, argName, accountId, promise)
    }

    override fun customTemplateGetBooleanArg(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetBooleanArg(templateName, argName, accountId, promise)
    }

    override fun customTemplateGetFileArg(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetFileArg(templateName, argName, accountId, promise)
    }

    override fun customTemplateGetObjectArg(
        templateName: String?,
        argName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateGetObjectArg(templateName, argName, accountId, promise)
    }

    override fun customTemplateContextToString(
        templateName: String?,
        accountId: String?,
        promise: Promise?
    ) {
        cleverTapModuleImpl.customTemplateContextToString(templateName, accountId, promise)
    }

    override fun syncCustomTemplates(accountId: String?) {
        cleverTapModuleImpl.syncCustomTemplates(accountId)
    }

    override fun syncCustomTemplatesInProd(isProduction: Boolean, accountId: String?) {
        // Android has no isProduction variant natively; route by account like the
        // parameterless sync (the spec keeps isProduction for iOS parity).
        cleverTapModuleImpl.syncCustomTemplates(accountId)
    }

    override fun variants(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.variants(accountId, callback)
    }

    override fun syncVariables(accountId: String?) {
        cleverTapModuleImpl.syncVariables(accountId)
    }

    override fun syncVariablesinProd(isProduction: Boolean, accountId: String?) {
        cleverTapModuleImpl.syncVariablesinProd(isProduction, accountId)
    }

    override fun defineVariables(`object`: ReadableMap?, accountId: String?) {
        cleverTapModuleImpl.defineVariables(`object`, accountId)
    }

    override fun defineFileVariable(name: String, accountId: String?) {
        cleverTapModuleImpl.defineFileVariable(name, accountId)
    }

    override fun fetchVariables(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.fetchVariables(accountId, callback)
    }

    override fun getVariable(key: String?, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariable(key, accountId, callback)
    }

    override fun getVariables(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getVariables(accountId, callback)
    }

    override fun onVariablesChanged(accountId: String?) {
        cleverTapModuleImpl.onVariablesChanged(accountId)
    }

    override fun onOneTimeVariablesChanged(accountId: String?) {
        cleverTapModuleImpl.onOneTimeVariablesChanged(accountId)
    }

    override fun onValueChanged(name: String, accountId: String?) {
        cleverTapModuleImpl.onValueChanged(name, accountId)
    }

    override fun onFileValueChanged(name: String, accountId: String?) {
        cleverTapModuleImpl.onFileValueChanged(name, accountId)
    }

    override fun onVariablesChangedAndNoDownloadsPending(accountId: String?) {
        cleverTapModuleImpl.onVariablesChangedAndNoDownloadsPending(accountId)
    }

    override fun onceVariablesChangedAndNoDownloadsPending(accountId: String?) {
        cleverTapModuleImpl.onceVariablesChangedAndNoDownloadsPending(accountId)
    }

    override fun onEventListenerAdded(eventName: String, accountId: String?) {
        cleverTapModuleImpl.onEventListenerAdded(eventName, accountId)
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
        withRegion: String?,
        accountId: String?
    ) {
        return
    }

    override fun getUserEventLog(eventName: String, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUserEventLog(eventName, accountId, callback)
    }

    override fun getUserEventLogCount(eventName: String, accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUserEventLogCount(eventName, accountId, callback)
    }

    override fun getUserLastVisitTs(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUserLastVisitTs(accountId, callback)
    }

    override fun getUserAppLaunchCount(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUserAppLaunchCount(accountId, callback)
    }

    override fun getUserEventLogHistory(accountId: String?, callback: Callback?) {
        cleverTapModuleImpl.getUserEventLogHistory(accountId, callback)
    }


    override fun getTypedExportedConstants(): Map<String, Any> {
        return cleverTapModuleImpl.getClevertapConstants()
    }
}
