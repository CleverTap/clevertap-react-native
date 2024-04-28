import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
    getInitialUrl(callback: Callback): void;
    setLocale(locale: string): void;
    enablePersonalization(): void;
    setOptOut(optOut: boolean): void;
    enableDeviceNetworkInfoReporting(enable: boolean): void;
    registerForPush(): void;
    setPushToken(token: string, type: string): void;

    createNotificationChannel(
        channelID: string,
        channelName: string,
        channelDescription: string,
        importance: number,
        showBadge: boolean
      ): void;

    createNotificationChannelWithSound(
        channelID: string,
        channelName: string,
        channelDescription: string,
        importance: number,
        showBadge: boolean,
        sound: string
      ): void;

    createNotificationChannelWithGroupId(
        channelID: string,
        channelName: string,
        channelDescription: string,
        importance: number,
        groupId: string,
        showBadge: boolean
      ): void;

    createNotificationChannelWithGroupIdAndSound(
        channelID: string,
        channelName: string,
        channelDescription: string,
        importance: number,
        groupId: string,
        showBadge: boolean,
        sound: string
      ): void;

    createNotificationChannelGroup(
        groupID: string,
        groupName: string
      ): void;

    deleteNotificationChannel(channelID: string): void;
    deleteNotificationChannelGroup(groupID: string): void;
    createNotification(extras: any): void;
    promptForPushPermission(showFallbackSettings: boolean): void;
    promptPushPrimer(localInAppConfig: any): void;
    isPushPermissionGranted(callback: CallbackString): void;
    recordScreenView(screenName: string): void;

    recordEvent(
        eventName: string,
        eventProps: any
      ): void;

    recordChargedEvent(
        details: any,
        items: any
      ): void;

    eventGetFirstTime(eventName: string, callback: Callback): void;
    eventGetLastTime(eventName: string, callback: Callback): void;
    eventGetOccurrences(eventName: string, callback: Callback): void;
    eventGetDetail(eventName: string, callback: Callback): void;
    getEventHistory(callback: Callback): void;
    setLocation(lat: number, lon: number): void;
    onUserLogin(profile: any): void;
    profileSet(profile: any): void;
    profileGetProperty(propertyName: string, callback: Callback): void;
    profileGetCleverTapAttributionIdentifier(callback: CallbackString): void;
    profileGetCleverTapID(callback: CallbackString): void;
    getCleverTapID(callback: CallbackString): void;
    profileRemoveValueForKey(key: string): void;
    profileSetMultiValuesForKey(values: any, key: string): void;
    profileAddMultiValueForKey(value: string, key: string): void;
    profileAddMultiValuesForKey(values: any, key: string): void;
    profileRemoveMultiValueForKey(value: string, key: string): void;
    profileRemoveMultiValuesForKey(values: any, key: string): void;
    profileIncrementValueForKey(value:number, key:string): void;
    profileDecrementValueForKey(value:number, key:string): void;
    sessionGetTimeElapsed(callback: Callback): void;
    sessionGetTotalVisits(callback: Callback): void;
    sessionGetScreenCount(callback: Callback): void;
    sessionGetPreviousVisitTime(callback: Callback): void;
    sessionGetUTMDetails(callback: Callback): void;
    pushInstallReferrer(
        source: string,
        medium: string,
        campaign: string
      ): void;
    initializeInbox(): void;
    getInboxMessageUnreadCount(callback: Callback): void;
    getInboxMessageCount(callback: Callback): void;
    showInbox(styleConfig: any): void;
    dismissInbox(): void;
    getAllInboxMessages(callback: Callback): void;
    getUnreadInboxMessages(callback: Callback): void;
    getInboxMessageForId(messageId: string, callback: Callback): void;
    deleteInboxMessageForId(messageId: string): void;
    deleteInboxMessagesForIDs(messageIds: any): void;
    markReadInboxMessageForId(messageId: string): void;
    markReadInboxMessagesForIDs(messageIds: any): void;
    pushInboxNotificationClickedEventForId(messageId: string): void;
    pushInboxNotificationViewedEventForId(messageId: string): void;
    getAllDisplayUnits(callback: Callback): void;
    getDisplayUnitForId(unitID: string, callback: Callback): void;
    pushDisplayUnitViewedEventForID(unitID: string): void;
    pushDisplayUnitClickedEventForID(unitID: string): void;
    setDefaultsMap(productConfigMap: any): void;
    fetch(): void;
    fetchWithMinimumIntervalInSeconds(intervalInSecs: number): void;
    activate(): void;
    fetchAndActivate(): void;
    setMinimumFetchIntervalInSeconds(intervalInSecs: number): void;
    resetProductConfig(): void;
    getProductConfigString(
        key: string,
        callback: Callback): void;
    getProductConfigBoolean(
        key: string,
        callback: Callback): void;
    getNumber(
        key: string,
        callback: Callback): void;
    getLastFetchTimeStampInMillis(callback: Callback): void;
    getFeatureFlag(
        key: string,
        defaultValue: boolean,
        callback: Callback): void;
    suspendInAppNotifications(): void;
    discardInAppNotifications(): void;
    resumeInAppNotifications(): void;
    setInstanceWithAccountId(accountId: string): void;
    syncVariables(): void;
    syncVariablesinProd(isProduction: boolean): void;
    fetchVariables(callback: Callback): void;
    defineVariables(variables: object): void;
    getVariables(callback: Callback): void;
    getVariable(name: string, callback: Callback): void;
    onVariablesChanged(handler: Function): void;
    onValueChanged(name: string, handler: Function): void;
    fetchInApps(callback: Callback): void;
    clearInAppResources(expiredOnly: boolean): void;
    setDebugLevel(level: number): void;

    // NativeEventEmitter methods for the New Architecture.
    // The implementations are handled implicitly by React Native.
    addListener(
        eventName: string,
        handler: Function
        ): void;
    removeListener(eventName: string): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('CleverTapReactBridge');