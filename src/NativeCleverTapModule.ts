import type { TurboModule } from 'react-native/Libraries/TurboModule/RCTExport';
import { TurboModuleRegistry } from 'react-native';

export interface Spec extends TurboModule {
  readonly getConstants: () => {
    CleverTapProfileDidInitialize: string;
    CleverTapProfileSync: string;
    CleverTapInAppNotificationDismissed: string;
    CleverTapInAppNotificationShowed: string;
    CleverTapInAppNotificationButtonTapped: string;
    CleverTapCustomTemplatePresent: string;
    CleverTapCustomTemplateClose: string;
    CleverTapCustomFunctionPresent: string;
    CleverTapInboxDidInitialize: string;
    CleverTapInboxMessagesDidUpdate: string;
    CleverTapInboxMessageButtonTapped: string;
    CleverTapInboxMessageTapped: string;
    CleverTapDisplayUnitsLoaded: string;
    CleverTapFeatureFlagsDidUpdate: string;
    CleverTapProductConfigDidInitialize: string;
    CleverTapProductConfigDidFetch: string;
    CleverTapProductConfigDidActivate: string;
    CleverTapPushNotificationClicked: string;
    CleverTapPushPermissionResponseReceived: string;
    CleverTapOnVariablesChanged: string;
    CleverTapOnOneTimeVariablesChanged: string;
    CleverTapOnValueChanged: string;
    CleverTapOnVariablesChangedAndNoDownloadsPending: string;
    CleverTapOnceVariablesChangedAndNoDownloadsPending: string;
    CleverTapOnFileValueChanged: string;
    HPS: string;
    FCM: string;
    BPS: string;
  };
  
  setInstanceWithAccountId(accountId: string): void;
  getInitialUrl(callback: (callback: string) => void): void;
  setLibrary(name: string, andVersion: number): void;
  setLocale(locale: string): void;
  registerForPush(): void;
  setPushTokenAsString(token: string, withType: string): void;
  setPushTokenAsStringWithRegion(token: string, withType: string, withRegion: string): void;
  enablePersonalization(): void;
  disablePersonalization(): void;
  setOffline(enabled: boolean): void;
  setOptOut(enabled: boolean): void;
  enableDeviceNetworkInfoReporting(enabled: boolean): void;
  recordScreenView(screenName: string): void;
  recordEvent(
    eventName: string,
    withProps: Object | null
  ): void;
  recordChargedEvent(
    details: Object | null,
    andItems: string[]
  ): void;
  eventGetFirstTime(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetLastTime(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetOccurrences(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetDetail(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserEventLog(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserEventLogCount(
    eventName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getEventHistory(callback: ((error: Object, result: boolean) => void) | null): void;
  getUserEventLogHistory(callback: ((error: Object, result: boolean) => void) | null): void;
  setLocation(location: number, longitude: number): void;
  profileGetCleverTapAttributionIdentifier(callback: ((error: Object, result: boolean) => void) | null): void;
  profileGetCleverTapID(callback: ((error: Object, result: boolean) => void) | null): void;
  getCleverTapID(callback: ((error: Object, result: boolean) => void) | null): void;
  onUserLogin(profile: Object | null): void;
  profileSet(profile: Object | null): void;
  profileGetProperty(
    propertyName: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  profileRemoveValueForKey(key: string): void;
  profileSetMultiValues(
    values: string[],
    forKey: string
  ): void;
  profileAddMultiValue(value: string, forKey: string): void;
  profileAddMultiValues(
    values: string[],
    forKey: string
  ): void;
  profileRemoveMultiValue(value: string, forKey: string): void;
  profileRemoveMultiValues(
    values: string[],
    forKey: string
  ): void;
  profileIncrementValueForKey(value: number | null, forKey: string): void;
  profileDecrementValueForKey(value: number | null, forKey: string): void;
  pushInstallReferrer(source: string, medium: string, campaign: string): void;
  sessionGetTimeElapsed(callback: ((error: Object, result: boolean) => void) | null): void;
  sessionGetTotalVisits(callback: ((error: Object, result: boolean) => void) | null): void;
  sessionGetScreenCount(callback: ((error: Object, result: boolean) => void) | null): void;
  sessionGetPreviousVisitTime(callback: ((error: Object, result: boolean) => void) | null): void;
  sessionGetUTMDetails(callback: ((error: Object, result: boolean) => void) | null): void;
  getUserLastVisitTs(callback: ((error: Object, result: boolean) => void) | null): void;
  getUserAppLaunchCount(callback: ((error: Object, result: boolean) => void) | null): void;
  createNotificationChannel(
    channelId: string,
    withChannelName: string,
    withChannelDescription: string,
    withImportance: number,
    withShowBadge: boolean
  ): void;
  createNotificationChannelWithSound(
    channelId: string,
    withChannelName: string,
    withChannelDescription: string,
    withImportance: number,
    withShowBadge: boolean,
    withSound: string
  ): void;
  createNotificationChannelWithGroupId(
    channelId: string,
    withChannelName: string,
    withChannelDescription: string,
    withImportance: number,
    withGroupId: string,
    withShowBadge: boolean
  ): void;
  createNotificationChannelWithGroupIdAndSound(
    channelId: string,
    withChannelName: string,
    withChannelDescription: string,
    withImportance: number,
    withGroupId: string,
    withShowBadge: boolean,
    withSound: string
  ): void;
  createNotificationChannelGroup(groupId: string, withGroupName: string): void;
  deleteNotificationChannel(channelId: string): void;
  deleteNotificationChannelGroup(groupId: string): void;
  createNotification(
    extras: Object | null
  ): void;
  setDebugLevel(level: number): void;
  getInboxMessageCount(callback: ((error: Object, result: boolean) => void) | null): void;
  getInboxMessageUnreadCount(callback: ((error: Object, result: boolean) => void) | null): void;
  getAllInboxMessages(callback: ((error: Object, result: boolean) => void) | null): void;
  getUnreadInboxMessages(callback: ((error: Object, result: boolean) => void) | null): void;
  getInboxMessageForId(messageId: string, callback: ((error: Object, result: boolean) => void) | null): void;
  pushInboxNotificationViewedEventForId(messageId: string): void;
  pushInboxNotificationClickedEventForId(messageId: string): void;
  markReadInboxMessageForId(messageId: string): void;
  deleteInboxMessageForId(messageId: string): void;
  markReadInboxMessagesForIDs(messageIds: string[]): void;
  deleteInboxMessagesForIDs(messageIds: string[]): void;
  dismissInbox(): void;
  initializeInbox(): void;
  showInbox(
    styleConfig: Object | null
  ): void;
  getAllDisplayUnits(callback: ((error: Object, result: boolean) => void) | null): void;
  getDisplayUnitForId(
    unitId: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  pushDisplayUnitViewedEventForID(unitId: string): void;
  pushDisplayUnitClickedEventForID(unitId: string): void;
  getFeatureFlag(
    flag: string,
    withdefaultValue: boolean,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  setDefaultsMap(
    jsonDict: Object | null
  ): void;
  fetch(): void;
  fetchWithMinimumFetchIntervalInSeconds(time: number): void;
  activate(): void;
  fetchAndActivate(): void;
  setMinimumFetchIntervalInSeconds(time: number): void;
  getLastFetchTimeStampInMillis(callback: (callback: string) => void): void;
  getString(
    key: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getBoolean(
    key: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getDouble(
    key: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  reset(): void;
  suspendInAppNotifications(): void;
  discardInAppNotifications(): void;
  resumeInAppNotifications(): void;
  fetchInApps(callback: ((error: Object, result: boolean) => void) | null): void;
  clearInAppResources(expiredOnly: boolean): void;
  customTemplateSetDismissed(templateName: string): Promise<void>;
  customTemplateSetPresented(templateName: string): Promise<void>;
  customTemplateRunAction(templateName: string, argName: string): Promise<void>;
  customTemplateGetStringArg(templateName: string, argName: string): Promise<string>;
  customTemplateGetNumberArg(templateName: string, argName: string): Promise<number>;
  customTemplateGetBooleanArg(templateName: string, argName: string): Promise<boolean>;
  customTemplateGetFileArg(templateName: string, argName: string): Promise<string>;
  customTemplateGetObjectArg(templateName: string, argName: string): Promise<any>;
  customTemplateContextToString(templateName: string): Promise<string>;
  syncCustomTemplates(): void;
  syncCustomTemplatesInProd(isProduction: boolean): void;
  promptForPushPermission(showFallbackSettings: boolean): void;
  promptPushPrimer(json: Object): void;
  isPushPermissionGranted(callback: ((error: Object, result: boolean) => void) | null): void;
  syncVariables(): void;
  syncVariablesinProd(isProduction: boolean): void;
  getVariable(
    name: string,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  fetchVariables(callback: ((error: Object, result: boolean) => void) | null): void;
  getVariables(callback: ((error: Object, result: boolean) => void) | null): void;
  defineVariables(
    variables: Object | null
  ): void;
  defineFileVariable(name: string): void;
  onVariablesChanged(): void;
  onOneTimeVariablesChanged(): void;
  onValueChanged(name: string): void;
  onVariablesChangedAndNoDownloadsPending(): void;
  onceVariablesChangedAndNoDownloadsPending(): void;
  onFileValueChanged(name: string): void;

  onEventListenerAdded(eventType: string): void;
  // NativeEventEmitter methods for the New Architecture.
  // The implementations are handled implicitly by React Native.
  addListener: (eventType: string) => void;
  removeListeners: (count: number) => void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('CleverTapReact');