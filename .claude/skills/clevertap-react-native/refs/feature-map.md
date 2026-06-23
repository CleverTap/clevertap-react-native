# Feature Map — Public Methods and Events

Every public method exposed by `CleverTap` and where it lands on each platform.

JS source: `src/index.js`. Android implementation: `CleverTapModuleImpl.java`. iOS implementation: `CleverTapReact.mm`. Method names are identical across all three unless noted.

## Push notifications

| JS method | Notes |
|---|---|
| `registerForPush()` | iOS prompts for permission; Android no-op on >= API 33 (use `promptForPushPermission`) |
| `setFCMPushToken(token)` | Android only (delegates to `setFCMPushTokenAsString`) |
| `pushRegistrationToken(token, pushType)` | `pushType` constants: `FCM`, `BPS`, `HPS` |
| `promptForPushPermission(showFallbackSettings)` | Android 13+ runtime permission |
| `promptPushPrimer(value)` | Custom in-app primer before OS prompt |
| `isPushPermissionGranted(callback)` | `(error, "true"\|"false")` |
| `createNotification(extras)` | Re-creates a notification from data payload (advanced) |
| `createNotificationChannel(id, name, desc, importance, showBadge)` | Android only |
| `createNotificationChannelWithSound(...)` | Android only |
| `createNotificationChannelWithGroupId(...)` | Android only |
| `createNotificationChannelWithGroupIdAndSound(...)` | Android only |
| `createNotificationChannelGroup(groupId, groupName)` | Android only |
| `deleteNotificationChannel(channelId)` | Android only |
| `deleteNotificationChannelGroup(groupId)` | Android only |
| `enableDeviceNetworkInfoReporting(enable)` | |

## Events & sessions

| JS method | Notes |
|---|---|
| `recordEvent(name, props)` | |
| `recordChargedEvent(details, items)` | `details` is the order, `items` is the array |
| `recordScreenView(screenName)` | |
| `getUserEventLog(name, cb)` | Returns the latest log entry |
| `getUserEventLogCount(name, cb)` | |
| `getUserEventLogHistory(cb)` | Full history map |
| `getUserLastVisitTs(cb)` | |
| `getUserAppLaunchCount(cb)` | |
| `sessionGetTimeElapsed(cb)` | |
| `sessionGetScreenCount(cb)` | |
| `sessionGetUTMDetails(cb)` | |
| `sessionGetTotalVisits(cb)` | *deprecated* |
| `sessionGetPreviousVisitTime(cb)` | *deprecated* |
| `eventGetFirstTime(name, cb)` | *deprecated* — use `getUserEventLog` |
| `eventGetLastTime(name, cb)` | *deprecated* |
| `eventGetOccurrences(name, cb)` | *deprecated* |
| `eventGetDetail(name, cb)` | *deprecated* |
| `getEventHistory(cb)` | *deprecated* |

## Profile & identity

| JS method | Notes |
|---|---|
| `onUserLogin(profile)` | Creates / switches user |
| `profileSet(profile)` | Updates current profile |
| `profileGetProperty(key, cb)` | |
| `getCleverTapID(cb)` | Preferred over the deprecated variants |
| `profileGetCleverTapID(cb)` | *deprecated* |
| `profileGetCleverTapAttributionIdentifier(cb)` | *deprecated* |
| `profileRemoveValueForKey(key)` | |
| `profileSetMultiValuesForKey(values, key)` | |
| `profileAddMultiValueForKey(value, key)` | |
| `profileAddMultiValuesForKey(values, key)` | |
| `profileRemoveMultiValueForKey(value, key)` | |
| `profileRemoveMultiValuesForKey(values, key)` | |
| `profileIncrementValueForKey(value, key)` | |
| `profileDecrementValueForKey(value, key)` | |

## App Inbox

| JS method | Notes |
|---|---|
| `initializeInbox()` | Fires `CleverTapInboxDidInitialize` |
| `showInbox(styleConfig)` | iOS uses `CleverTapInboxViewController`; Android uses `CTInboxActivity` |
| `dismissInbox()` | |
| `getInboxMessageCount(cb)` | |
| `getInboxMessageUnreadCount(cb)` | |
| `getAllInboxMessages(cb)` | |
| `getUnreadInboxMessages(cb)` | |
| `getInboxMessageForId(id, cb)` | |
| `deleteInboxMessageForId(id)` | |
| `deleteInboxMessagesForIDs(ids)` | |
| `markReadInboxMessageForId(id)` | |
| `markReadInboxMessagesForIDs(ids)` | |
| `pushInboxNotificationClickedEventForId(id)` | Analytics ping |
| `pushInboxNotificationViewedEventForId(id)` | Analytics ping |

## In-App notifications

| JS method | Notes |
|---|---|
| `suspendInAppNotifications()` | |
| `discardInAppNotifications(dismissIfVisible)` | |
| `resumeInAppNotifications()` | |
| `fetchInApps(cb)` | |
| `clearInAppResources(expiredOnly)` | |

## Custom Templates

| JS method | Notes |
|---|---|
| `syncCustomTemplates()` | Dev environment |
| `syncCustomTemplatesInProd(isProduction)` | |
| `customTemplateSetPresented(name)` | Promise |
| `customTemplateSetDismissed(name)` | Promise |
| `customTemplateRunAction(name, argName)` | Promise |
| `customTemplateGetStringArg(name, argName)` | Promise<string> |
| `customTemplateGetNumberArg(name, argName)` | Promise<number> |
| `customTemplateGetBooleanArg(name, argName)` | Promise<boolean> |
| `customTemplateGetFileArg(name, argName)` | Promise<string> (local file path) |
| `customTemplateGetObjectArg(name, argName)` | Promise<any> |
| `customTemplateContextToString(name)` | Promise<string> |
| `variants(cb)` | Active variants for the user |

## Variables (Product Experiences)

| JS method | Notes |
|---|---|
| `syncVariables()` | |
| `syncVariablesinProd(isProduction)` | |
| `fetchVariables(cb)` | |
| `defineVariables(variables)` | Object literal of name → default |
| `defineFileVariable(name)` | |
| `getVariable(name, cb)` | |
| `getVariables(cb)` | All defined variables |
| `onVariablesChanged(handler)` | Persistent listener |
| `onOneTimeVariablesChanged(handler)` | Fires once |
| `onValueChanged(name, handler)` | Per-variable listener |
| `onVariablesChangedAndNoDownloadsPending(handler)` | After file vars resolved |
| `onceVariablesChangedAndNoDownloadsPending(handler)` | One-time variant |
| `onFileValueChanged(name, handler)` | File-variable listener |

## Display Units

| JS method | Notes |
|---|---|
| `getAllDisplayUnits(cb)` | |
| `getDisplayUnitForId(id, cb)` | |
| `pushDisplayUnitViewedEventForID(id)` | |
| `pushDisplayUnitClickedEventForID(id)` | |

## Feature Flags (deprecated v1.1.0+)

| JS method | Notes |
|---|---|
| `getFeatureFlag(key, defaultValue, cb)` | |

## Product Config (deprecated v1.1.0+)

| JS method | Notes |
|---|---|
| `setDefaultsMap(map)` | |
| `fetch()` | |
| `fetchAndActivate()` | |
| `fetchWithMinimumIntervalInSeconds(secs)` | |
| `setMinimumFetchIntervalInSeconds(secs)` | |
| `activate()` | |
| `resetProductConfig()` | |
| `getProductConfigString(key, cb)` | |
| `getProductConfigBoolean(key, cb)` | |
| `getNumber(key, cb)` | |
| `getLastFetchTimeStampInMillis(cb)` | |

## Config & lifecycle

| JS method | Notes |
|---|---|
| `setDebugLevel(level)` | 0=off, 1=info, 2=debug, 3=verbose |
| `setLocale(locale)` | |
| `setLocation(lat, lon)` | |
| `setOffline(value)` | |
| `setOptOut(userOptOut, allowSystemEvents)` | |
| `setInstanceWithAccountId(accountId)` | Multi-instance — subsequent calls route to this instance |
| `enablePersonalization()` | |
| `disablePersonalization()` | |
| `unmute()` | |
| `pushInstallReferrer(source, medium, campaign)` | |
| `getInitialUrl(cb)` | Deep link that launched the app |

## Listener API

| JS method | Notes |
|---|---|
| `addListener(eventName, handler)` | Required entry point — triggers buffered-event flush |
| `addOneTimeListener(eventName, handler)` | Auto-removes after first fire |
| `removeListener(eventName)` | |
| `removeListeners()` | *deprecated* — use per-event removal |

## Events

| Event constant (JS) | Bufferable on Android | iOS in `supportedEvents` |
|---|---|---|
| `CleverTapProfileDidInitialize` | yes | yes |
| `CleverTapProfileSync` | no | yes |
| `CleverTapInAppNotificationShowed` | yes | yes |
| `CleverTapInAppNotificationDismissed` | yes | yes |
| `CleverTapInAppNotificationButtonTapped` | yes | yes |
| `CleverTapInboxDidInitialize` | yes | yes |
| `CleverTapInboxMessagesDidUpdate` | no | yes |
| `CleverTapInboxMessageTapped` | no | yes |
| `CleverTapInboxMessageButtonTapped` | no | yes |
| `CleverTapDisplayUnitsLoaded` | yes | yes |
| `CleverTapFeatureFlagsDidUpdate` | yes | yes |
| `CleverTapProductConfigDidInitialize` | yes | yes |
| `CleverTapProductConfigDidFetch` | no | yes |
| `CleverTapProductConfigDidActivate` | no | yes |
| `CleverTapPushNotificationClicked` | yes | yes |
| `CleverTapPushPermissionResponseReceived` | no | yes |
| `CleverTapOnVariablesChanged` | no | yes |
| `CleverTapOnOneTimeVariablesChanged` | no | yes |
| `CleverTapOnValueChanged` | no | yes |
| `CleverTapOnVariablesChangedAndNoDownloadsPending` | no | yes |
| `CleverTapOnceVariablesChangedAndNoDownloadsPending` | no | yes |
| `CleverTapOnFileValueChanged` | no | yes |
| `CleverTapCustomTemplatePresent` | yes | yes |
| `CleverTapCustomFunctionPresent` | yes | yes |
| `CleverTapCustomTemplateClose` | no | yes |

Authoritative payload shape for each event: `docs/callbackPayloadFormat.md`.
