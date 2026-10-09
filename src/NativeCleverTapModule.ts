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
    FCM: string;
  };

  // Every routed method takes `accountId: string | null` as a REQUIRED parameter, on
  // purpose one shape everywhere: JS always passes it (null = the default account)
  // because the old-architecture Android bridge checks the exact argument count and
  // throws on a missing trailing argument. It is never optional, even where TypeScript
  // would allow `?` — and the callback-taking methods could not use `?` anyway
  // (TS1016: a required parameter cannot follow an optional one). The same rule makes
  // the two nullable booleans that sit before an accountId (setOptOut's
  // allowSystemEvents, discardInAppNotifications' dismissInAppIfVisible) `boolean | null`
  // rather than `?`. Codegen emits the same nullable native parameter for both spellings.
  setInstanceWithAccountId(accountId: string): void;
  createInstance(config: Object): Promise<Object>;
  getDefaultAccountId(): Promise<string | null>;
  getInitialUrl(callback: (callback: string) => void): void;
  setLibrary(name: string, andVersion: number): void;
  setLocale(locale: string, accountId: string | null): void;
  registerForPush(): void;
  setFCMPushTokenAsString(token: string, accountId: string | null): void;
  pushRegistrationToken(token: string, pushType: Object | null, accountId: string | null): void;
  setPushTokenAsStringWithRegion(
    token: string,
    withType: string,
    withRegion: string,
    accountId: string | null
  ): void;
  enablePersonalization(accountId: string | null): void;
  disablePersonalization(accountId: string | null): void;
  setOffline(enabled: boolean, accountId: string | null): void;
  setOptOut(userOptOut: boolean, allowSystemEvents: boolean | null, accountId: string | null): void;
  enableDeviceNetworkInfoReporting(enabled: boolean, accountId: string | null): void;
  unmute(accountId: string | null): void;
  recordScreenView(screenName: string, accountId: string | null): void;
  recordEvent(
    eventName: string,
    withProps: Object | null,
    accountId: string | null
  ): void;
  recordChargedEvent(
    details: Object | null,
    andItems: string[],
    accountId: string | null
  ): void;
  eventGetFirstTime(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetLastTime(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetOccurrences(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  eventGetDetail(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserEventLog(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserEventLogCount(
    eventName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getEventHistory(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserEventLogHistory(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  setLocation(location: number, longitude: number, accountId: string | null): void;
  profileGetCleverTapAttributionIdentifier(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  profileGetCleverTapID(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getCleverTapID(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  onUserLogin(profile: Object | null, accountId: string | null): void;
  profileSet(profile: Object | null, accountId: string | null): void;
  profileGetProperty(
    propertyName: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  profileRemoveValueForKey(key: string, accountId: string | null): void;
  profileSetMultiValues(
    values: string[],
    forKey: string,
    accountId: string | null
  ): void;
  profileAddMultiValue(value: string, forKey: string, accountId: string | null): void;
  profileAddMultiValues(
    values: string[],
    forKey: string,
    accountId: string | null
  ): void;
  profileRemoveMultiValue(value: string, forKey: string, accountId: string | null): void;
  profileRemoveMultiValues(
    values: string[],
    forKey: string,
    accountId: string | null
  ): void;
  profileIncrementValueForKey(value: number | null, forKey: string, accountId: string | null): void;
  profileDecrementValueForKey(value: number | null, forKey: string, accountId: string | null): void;
  pushInstallReferrer(
    source: string,
    medium: string,
    campaign: string,
    accountId: string | null
  ): void;
  sessionGetTimeElapsed(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  sessionGetTotalVisits(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  sessionGetScreenCount(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  sessionGetPreviousVisitTime(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  sessionGetUTMDetails(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserLastVisitTs(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUserAppLaunchCount(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
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
  getInboxMessageCount(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getInboxMessageUnreadCount(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getAllInboxMessages(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getUnreadInboxMessages(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getInboxMessageForId(
    messageId: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  pushInboxNotificationViewedEventForId(messageId: string, accountId: string | null): void;
  pushInboxNotificationClickedEventForId(messageId: string, accountId: string | null): void;
  markReadInboxMessageForId(messageId: string, accountId: string | null): void;
  deleteInboxMessageForId(messageId: string, accountId: string | null): void;
  markReadInboxMessagesForIDs(messageIds: string[], accountId: string | null): void;
  deleteInboxMessagesForIDs(messageIds: string[], accountId: string | null): void;
  dismissInbox(accountId: string | null): void;
  initializeInbox(accountId: string | null): void;
  fetchInbox(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  showInbox(
    styleConfig: Object | null,
    accountId: string | null
  ): void;
  getAllDisplayUnits(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getDisplayUnitForId(
    unitId: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  pushDisplayUnitViewedEventForID(unitId: string, accountId: string | null): void;
  pushDisplayUnitClickedEventForID(unitId: string, accountId: string | null): void;
  pushDisplayUnitElementClickedEventForID(
    unitId: string,
    additionalProperties: Object | null,
    accountId: string | null
  ): void;
  getFeatureFlag(
    flag: string,
    withdefaultValue: boolean,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  setDefaultsMap(
    jsonDict: Object | null,
    accountId: string | null
  ): void;
  fetch(accountId: string | null): void;
  fetchWithMinimumFetchIntervalInSeconds(time: number, accountId: string | null): void;
  activate(accountId: string | null): void;
  fetchAndActivate(accountId: string | null): void;
  setMinimumFetchIntervalInSeconds(time: number, accountId: string | null): void;
  getLastFetchTimeStampInMillis(accountId: string | null, callback: (callback: string) => void): void;
  getString(
    key: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getBoolean(
    key: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getDouble(
    key: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  reset(accountId: string | null): void;
  suspendInAppNotifications(accountId: string | null): void;
  discardInAppNotifications(dismissInAppIfVisible: boolean | null, accountId: string | null): void;
  resumeInAppNotifications(accountId: string | null): void;
  dismissPipInApp(accountId: string | null): void;
  fetchInApps(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  clearInAppResources(expiredOnly: boolean, accountId: string | null): void;
  // On the customTemplate* methods the accountId comes BEFORE the implicit promise
  // (the promise is the always-last argument — same iron rule as trailing callbacks).
  customTemplateSetDismissed(templateName: string, accountId: string | null): Promise<void>;
  customTemplateSetPresented(templateName: string, accountId: string | null): Promise<void>;
  customTemplateRunAction(templateName: string, argName: string, accountId: string | null): Promise<void>;
  customTemplateGetStringArg(templateName: string, argName: string, accountId: string | null): Promise<string>;
  customTemplateGetNumberArg(templateName: string, argName: string, accountId: string | null): Promise<number>;
  customTemplateGetBooleanArg(templateName: string, argName: string, accountId: string | null): Promise<boolean>;
  customTemplateGetFileArg(templateName: string, argName: string, accountId: string | null): Promise<string>;
  customTemplateGetObjectArg(templateName: string, argName: string, accountId: string | null): Promise<any>;
  customTemplateContextToString(templateName: string, accountId: string | null): Promise<string>;
  syncCustomTemplates(accountId: string | null): void;
  syncCustomTemplatesInProd(isProduction: boolean, accountId: string | null): void;
  // Native instance methods (the permission RESPONSE event is routed per account),
  // so they take the trailing accountId like every other routed method — with the
  // callback staying LAST on isPushPermissionGranted (old-arch iron rule).
  promptForPushPermission(showFallbackSettings: boolean, accountId: string | null): void;
  promptPushPrimer(json: Object, accountId: string | null): void;
  isPushPermissionGranted(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  syncVariables(accountId: string | null): void;
  syncVariablesinProd(isProduction: boolean, accountId: string | null): void;
  getVariable(
    name: string,
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  fetchVariables(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  getVariables(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;
  defineVariables(
    variables: Object | null,
    accountId: string | null
  ): void;
  defineFileVariable(name: string, accountId: string | null): void;
  onVariablesChanged(accountId: string | null): void;
  onOneTimeVariablesChanged(accountId: string | null): void;
  onValueChanged(name: string, accountId: string | null): void;
  onVariablesChangedAndNoDownloadsPending(accountId: string | null): void;
  onceVariablesChangedAndNoDownloadsPending(accountId: string | null): void;
  onFileValueChanged(name: string, accountId: string | null): void;
  variants(
    accountId: string | null,
    callback: ((error: Object, result: boolean) => void) | null
  ): void;

  onEventListenerAdded(eventType: string, accountId: string | null): void;
  // NativeEventEmitter methods for the New Architecture.
  // The implementations are handled implicitly by React Native.
  addListener: (eventType: string) => void;
  removeListeners: (count: number) => void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('CleverTapReact');
