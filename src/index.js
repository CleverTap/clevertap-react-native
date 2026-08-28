import { DeviceEventEmitter, NativeEventEmitter, NativeModules, Platform } from 'react-native';

const CleverTapReact = require('./NativeCleverTapModule').default;
const EventEmitter = Platform.select({
    ios: new NativeEventEmitter(CleverTapReact),
    android: DeviceEventEmitter
  });
// NativeModules.CleverTapReactEventEmitter ? new NativeEventEmitter(CleverTapReact) : DeviceEventEmitter;
/**
* Set the CleverTap React-Native library name with current version
* @param {string} libName - Library name will be "React-Native"
* @param {int} libVersion - The updated library version. If current version is 1.1.0 then pass as 10100  
*/
const libName = 'React-Native';
const libVersion = 40301;
CleverTapReact.setLibrary(libName,libVersion);

function defaultCallback(method, err, res) {
    if (err) {
        console.log('CleverTap ' + method + ' default callback error', err);
    } else {
        console.log('CleverTap ' + method + ' default callback result', res);
    }
}

/**
* Calls a CleverTap method with a default callback if necessary
* @param {string} method - the CleverTap method name as a string
* @param {array} args - The method args
* @param {function(err, res)} callback - callback
* @param {string} accountId - which account the call is for; pass null for the
* default account. Only pass it for native methods that accept an accountId
* (the native argument order is accountId, then callback). Leave it undefined
* for methods with no accountId parameter: the old-architecture Android bridge
* checks the exact argument count, so an extra argument would throw.
*
* ⚠️ ORDER IS LOAD-BEARING: the callback MUST be the LAST argument. React
* Native's old-architecture bridge reads callbacks off the END of the argument
* list and throws "Cannot have a non-function arg after a function arg." if
* anything follows a function (NativeModules.js — not dev-only, crashes
* release builds). Never append anything after the callback.
*/
function callWithCallback(method, args, callback, accountId) {
    if (typeof callback === 'undefined' || callback == null || typeof callback !== 'function') {
        callback = (err, res) => {
            defaultCallback(method, err, res);
        };
    }

    if (args == null) {
        args = [];
    }

    if (accountId !== undefined) {
        args.push(accountId);
    }

    args.push(callback);

    CleverTapReact[method].apply(this, args);
}

/* ---------------------------------------------------------------------------
 * Multi-instance support (see MULTI_INSTANCE_OVERVIEW.md).
 *
 * Every native event payload carries the REAL account id of the instance that
 * fired it, under CT_ACCOUNT_ID_KEY. A central "sorting office" (demux) below
 * keeps ONE native subscription per event name, reads the tag, and re-delivers
 * the event under a per-account key like 'ACCT_B::CleverTapProfileSync'. Each
 * account handle subscribes to exactly its own key, so no handle is ever woken
 * for another account's events.
 * ------------------------------------------------------------------------- */

// Defined once — no magic strings. Must match Constants.CT_ACCOUNT_ID_KEY (Android)
// and kCleverTapAccountIdKey (iOS).
const CT_ACCOUNT_ID_KEY = '__ctAccountId';
// Wrapper for primitive event payloads. Custom template events deliver a bare
// STRING (the template name) to user code; a string cannot carry the account tag,
// so native wraps it — {__ctAccountId, __ctPayload} — and routeEvent unwraps it.
// Must match Constants.CT_PAYLOAD_KEY (Android) and kCleverTapPayloadKey (iOS).
const CT_PAYLOAD_KEY = '__ctPayload';
// Internal routing key for the top-level CleverTap object (the "default slot").
const DEFAULT_SLOT_KEY = '__default__';

const routedKey = (accountKey, eventName) => accountKey + '::' + eventName;

// Keeps the trailing native arg explicit: null = the default account. It must always
// be passed — the old-architecture Android bridge throws on a missing trailing arg.
const toAccountArg = (accountId) => (accountId === undefined ? null : accountId);

// Debug logs for the event routing pipeline. __DEV__ builds only.
const routeDebug = (message) => {
    if (__DEV__) {
        console.log('[CleverTap][MultiInstance] ' + message);
    }
};

// Which account do the top-level CleverTap listeners follow? Asked from native once;
// setInstanceWithAccountId updates it synchronously (legacy "slot swap").
let currentDefaultAccountId = null;
let slotSwapped = false;
const defaultAccountIdReady = CleverTapReact.getDefaultAccountId().then((id) => {
    if (!slotSwapped) {
        currentDefaultAccountId = id;
    }
    routeDebug('default accountId resolved: ' + currentDefaultAccountId);
    return currentDefaultAccountId;
}).catch((error) => {
    // Never swallow this silently: without the default account id, NO default-account
    // event can be routed to top-level listeners.
    console.warn('[CleverTap][MultiInstance] getDefaultAccountId failed — top-level listeners cannot receive events:', error);
    return currentDefaultAccountId;
});

const routedHandlers = new Map(); // routedKey -> Set<handler>
const nativeSubscriptions = new Map(); // eventName -> emitter subscription

function deliverRouted(key, payload) {
    const handlers = routedHandlers.get(key);
    if (handlers && handlers.size > 0) {
        routeDebug('delivering to "' + key + '" (' + handlers.size + ' handler(s))');
        // Why try/catch around EACH handler? One event can have several independent
        // listeners (e.g. an analytics module and a navigation module both listening
        // to CleverTapProfileSync). Without the guard, the FIRST handler that throws
        // would stop the loop — the remaining listeners would silently never hear an
        // event that was delivered to the app, and the error would bubble into the
        // native event emitter. A listener's bug should cost only that listener, so
        // we log it loudly and keep delivering to the others.
        handlers.forEach((handler) => {
            try {
                handler(payload);
            } catch (error) {
                console.error('[CleverTap] a listener for "' + key + '" threw:', error);
            }
        });
        return true;
    }
    return false;
}

function routeEvent(eventName, event) {
    // A payload that is not an object can carry no account tag, and the `in`
    // operator below THROWS on primitives ("Cannot use 'in' operator..."). Custom
    // template events deliver the bare template name as a string, so without this
    // guard every template present/close crashed the app.
    const isObject = event !== null && typeof event === 'object';
    const tag = isObject ? event[CT_ACCOUNT_ID_KEY] : null;
    routeDebug('received "' + eventName + '" tag=' + tag +
        ' defaultAccountId=' + currentDefaultAccountId);
    // Immutability: never mutate the shared payload. Every handler receives the same
    // sanitized copy, without the internal tag.
    let payload = event;
    if (isObject && CT_ACCOUNT_ID_KEY in event) {
        payload = Object.assign({}, event);
        delete payload[CT_ACCOUNT_ID_KEY];
    }
    // Native wraps primitive payloads (custom template events send the template name
    // as a string) so they can carry the account tag. Unwrap here: handlers keep
    // receiving exactly what released SDKs delivered — the bare string.
    if (payload !== null && typeof payload === 'object' && CT_PAYLOAD_KEY in payload) {
        payload = payload[CT_PAYLOAD_KEY];
    }
    if (tag == null) {
        // Untagged events go to the top-level CleverTap listeners — same audience
        // as before the demux existed.
        if (!deliverRouted(routedKey(DEFAULT_SLOT_KEY, eventName), payload)) {
            routeDebug('dropped untagged "' + eventName + '" — no top-level listener');
        }
        return;
    }
    let delivered = deliverRouted(routedKey(tag, eventName), payload);
    if (tag === currentDefaultAccountId) {
        delivered = deliverRouted(routedKey(DEFAULT_SLOT_KEY, eventName), payload) || delivered;
    }
    if (!delivered) {
        routeDebug('dropped "' + eventName + '" tag=' + tag + ' — no listener matched' +
            (currentDefaultAccountId === null
                ? ' (default accountId not resolved yet — see getDefaultAccountId)'
                : ''));
    }
}

function ensureNativeSubscription(eventName) {
    if (!EventEmitter || nativeSubscriptions.has(eventName)) {
        return;
    }
    nativeSubscriptions.set(eventName,
        EventEmitter.addListener(eventName, (event) => routeEvent(eventName, event)));
}

function addListenerForHandle(accountId, eventName, handler) {
    const accountKey = accountId === undefined ? DEFAULT_SLOT_KEY : accountId;
    ensureNativeSubscription(eventName);
    const key = routedKey(accountKey, eventName);
    if (!routedHandlers.has(key)) {
        routedHandlers.set(key, new Set());
    }
    routedHandlers.get(key).add(handler);
    routeDebug('listener added for "' + key + '"');
    // Arm the native buffered-event flush for this account. For the top-level object,
    // wait until the default account id is known — otherwise a flushed event could
    // arrive before routeEvent can recognize it as the default account's.
    if (accountId === undefined) {
        defaultAccountIdReady.then(() => {
            routeDebug('arming native flush for "' + eventName + '" (default slot)');
            CleverTapReact.onEventListenerAdded(eventName, null);
        });
    } else {
        routeDebug('arming native flush for "' + eventName + '" (account ' + accountId + ')');
        CleverTapReact.onEventListenerAdded(eventName, accountId);
    }
    return {
        remove: () => {
            const handlers = routedHandlers.get(key);
            if (handlers) {
                handlers.delete(handler);
            }
        }
    };
}

function removeListenersForHandle(accountId, eventName) {
    const accountKey = accountId === undefined ? DEFAULT_SLOT_KEY : accountId;
    // Deletes ONLY this handle's handlers — other handles' listeners for the same
    // event keep working (this fixes the "removeListener kills everyone" bug class).
    routedHandlers.delete(routedKey(accountKey, eventName));
}

/**
 * Builds a handle for one CleverTap account. Every method forwards the handle's
 * accountId as the trailing native argument; listeners receive only this account's
 * events. The handle is frozen so its shape cannot be mutated by callers.
 *
 * OS-level methods (push registration, notification channels, initial URL) exist on
 * the handle for shape consistency but warn and do nothing — call them on the
 * top-level CleverTap object. setDebugLevel is global by design and is not on the
 * handle at all. Custom template DEFINITIONS are app-wide (every account gets the
 * registered templates), but presenting, argument reads and dismissal are
 * per-account — those methods are on the handle.
 */
function createHandle(accountId) {
    const handle = {
        accountId: accountId,

        recordEvent: (eventName, props) => {
            convertDateToEpochInProperties(props);
            CleverTapReact.recordEvent(eventName, props, toAccountArg(accountId));
        },
        onUserLogin: (profile) => {
            convertDateToEpochInProperties(profile);
            CleverTapReact.onUserLogin(profile, toAccountArg(accountId));
        },
        profileSet: (profile) => {
            convertDateToEpochInProperties(profile);
            CleverTapReact.profileSet(profile, toAccountArg(accountId));
        },
        getCleverTapID: (callback) =>
            callWithCallback('getCleverTapID', null, callback, toAccountArg(accountId)),

        // --- Locale & push tokens ---
        setLocale: (locale) =>
            CleverTapReact.setLocale(locale, toAccountArg(accountId)),
        pushRegistrationToken: (token, pushType) =>
            CleverTapReact.pushRegistrationToken(token, pushType, toAccountArg(accountId)),
        setFCMPushToken: (token) =>
            CleverTapReact.setFCMPushTokenAsString(token, toAccountArg(accountId)),
        // setPushTokenAsStringWithRegion is intentionally NOT on the handle: it is a
        // dead legacy method (no-op on every platform) and the top-level CleverTap
        // object does not expose it either.

        // --- Consent, personalization & connectivity ---
        setOptOut: (userOptOut, allowSystemEvents = false) =>
            CleverTapReact.setOptOut(userOptOut, allowSystemEvents, toAccountArg(accountId)),
        setOffline: (value) =>
            CleverTapReact.setOffline(value, toAccountArg(accountId)),
        unmute: () =>
            CleverTapReact.unmute(toAccountArg(accountId)),
        enableDeviceNetworkInfoReporting: (value) =>
            CleverTapReact.enableDeviceNetworkInfoReporting(value, toAccountArg(accountId)),
        enablePersonalization: () =>
            CleverTapReact.enablePersonalization(toAccountArg(accountId)),
        disablePersonalization: () =>
            CleverTapReact.disablePersonalization(toAccountArg(accountId)),

        // --- Events ---
        recordScreenView: (screenName) =>
            CleverTapReact.recordScreenView(screenName, toAccountArg(accountId)),
        recordChargedEvent: (details, items) => {
            convertDateToEpochInProperties(details);
            if (Array.isArray(items) && items.length) {
                items.forEach(value => {
                    convertDateToEpochInProperties(value);
                });
            }
            CleverTapReact.recordChargedEvent(details, items, toAccountArg(accountId));
        },
        eventGetFirstTime: (eventName, callback) =>
            callWithCallback('eventGetFirstTime', [eventName], callback, toAccountArg(accountId)),
        eventGetLastTime: (eventName, callback) =>
            callWithCallback('eventGetLastTime', [eventName], callback, toAccountArg(accountId)),
        eventGetOccurrences: (eventName, callback) =>
            callWithCallback('eventGetOccurrences', [eventName], callback, toAccountArg(accountId)),
        eventGetDetail: (eventName, callback) =>
            callWithCallback('eventGetDetail', [eventName], callback, toAccountArg(accountId)),
        getEventHistory: (callback) =>
            callWithCallback('getEventHistory', null, callback, toAccountArg(accountId)),
        getUserEventLog: (eventName, callback) =>
            callWithCallback('getUserEventLog', [eventName], callback, toAccountArg(accountId)),
        getUserEventLogCount: (eventName, callback) =>
            callWithCallback('getUserEventLogCount', [eventName], callback, toAccountArg(accountId)),
        getUserEventLogHistory: (callback) =>
            callWithCallback('getUserEventLogHistory', null, callback, toAccountArg(accountId)),

        // --- Location & profile ---
        setLocation: (latitude, longitude) =>
            CleverTapReact.setLocation(latitude, longitude, toAccountArg(accountId)),
        profileGetCleverTapAttributionIdentifier: (callback) =>
            callWithCallback('profileGetCleverTapAttributionIdentifier', null, callback, toAccountArg(accountId)),
        profileGetCleverTapID: (callback) =>
            callWithCallback('profileGetCleverTapID', null, callback, toAccountArg(accountId)),
        profileGetProperty: (key, callback) =>
            callWithCallback('profileGetProperty', [key], callback, toAccountArg(accountId)),
        profileRemoveValueForKey: (key) =>
            CleverTapReact.profileRemoveValueForKey(key, toAccountArg(accountId)),
        profileSetMultiValuesForKey: (values, key) =>
            CleverTapReact.profileSetMultiValues(values, key, toAccountArg(accountId)),
        profileAddMultiValueForKey: (value, key) =>
            CleverTapReact.profileAddMultiValue(value, key, toAccountArg(accountId)),
        profileAddMultiValuesForKey: (values, key) =>
            CleverTapReact.profileAddMultiValues(values, key, toAccountArg(accountId)),
        profileRemoveMultiValueForKey: (value, key) =>
            CleverTapReact.profileRemoveMultiValue(value, key, toAccountArg(accountId)),
        profileRemoveMultiValuesForKey: (values, key) =>
            CleverTapReact.profileRemoveMultiValues(values, key, toAccountArg(accountId)),
        profileIncrementValueForKey: (value, key) =>
            CleverTapReact.profileIncrementValueForKey(value, key, toAccountArg(accountId)),
        profileDecrementValueForKey: (value, key) =>
            CleverTapReact.profileDecrementValueForKey(value, key, toAccountArg(accountId)),
        pushInstallReferrer: (source, medium, campaign) =>
            CleverTapReact.pushInstallReferrer(source, medium, campaign, toAccountArg(accountId)),

        // --- Session ---
        sessionGetTimeElapsed: (callback) =>
            callWithCallback('sessionGetTimeElapsed', null, callback, toAccountArg(accountId)),
        sessionGetTotalVisits: (callback) =>
            callWithCallback('sessionGetTotalVisits', null, callback, toAccountArg(accountId)),
        getUserLastVisitTs: (callback) =>
            callWithCallback('getUserLastVisitTs', null, callback, toAccountArg(accountId)),
        getUserAppLaunchCount: (callback) =>
            callWithCallback('getUserAppLaunchCount', null, callback, toAccountArg(accountId)),
        sessionGetScreenCount: (callback) =>
            callWithCallback('sessionGetScreenCount', null, callback, toAccountArg(accountId)),
        sessionGetPreviousVisitTime: (callback) =>
            callWithCallback('sessionGetPreviousVisitTime', null, callback, toAccountArg(accountId)),
        sessionGetUTMDetails: (callback) =>
            callWithCallback('sessionGetUTMDetails', null, callback, toAccountArg(accountId)),

        // --- App Inbox ---
        initializeInbox: () =>
            CleverTapReact.initializeInbox(toAccountArg(accountId)),
        fetchInbox: (callback) =>
            callWithCallback('fetchInbox', null, callback, toAccountArg(accountId)),
        showInbox: (styleConfig) =>
            CleverTapReact.showInbox(styleConfig, toAccountArg(accountId)),
        dismissInbox: () =>
            CleverTapReact.dismissInbox(toAccountArg(accountId)),
        getInboxMessageCount: (callback) =>
            callWithCallback('getInboxMessageCount', null, callback, toAccountArg(accountId)),
        getInboxMessageUnreadCount: (callback) =>
            callWithCallback('getInboxMessageUnreadCount', null, callback, toAccountArg(accountId)),
        getAllInboxMessages: (callback) =>
            callWithCallback('getAllInboxMessages', null, callback, toAccountArg(accountId)),
        getUnreadInboxMessages: (callback) =>
            callWithCallback('getUnreadInboxMessages', null, callback, toAccountArg(accountId)),
        getInboxMessageForId: (messageId, callback) =>
            callWithCallback('getInboxMessageForId', [messageId], callback, toAccountArg(accountId)),
        deleteInboxMessageForId: (messageId) =>
            CleverTapReact.deleteInboxMessageForId(messageId, toAccountArg(accountId)),
        deleteInboxMessagesForIDs: (messageIds) =>
            CleverTapReact.deleteInboxMessagesForIDs(messageIds, toAccountArg(accountId)),
        markReadInboxMessageForId: (messageId) =>
            CleverTapReact.markReadInboxMessageForId(messageId, toAccountArg(accountId)),
        markReadInboxMessagesForIDs: (messageIds) =>
            CleverTapReact.markReadInboxMessagesForIDs(messageIds, toAccountArg(accountId)),
        pushInboxNotificationClickedEventForId: (messageId) =>
            CleverTapReact.pushInboxNotificationClickedEventForId(messageId, toAccountArg(accountId)),
        pushInboxNotificationViewedEventForId: (messageId) =>
            CleverTapReact.pushInboxNotificationViewedEventForId(messageId, toAccountArg(accountId)),

        // --- Native Display ---
        getAllDisplayUnits: (callback) =>
            callWithCallback('getAllDisplayUnits', null, callback, toAccountArg(accountId)),
        getDisplayUnitForId: (unitID, callback) =>
            callWithCallback('getDisplayUnitForId', [unitID], callback, toAccountArg(accountId)),
        pushDisplayUnitViewedEventForID: (unitID) =>
            CleverTapReact.pushDisplayUnitViewedEventForID(unitID, toAccountArg(accountId)),
        pushDisplayUnitClickedEventForID: (unitID) =>
            CleverTapReact.pushDisplayUnitClickedEventForID(unitID, toAccountArg(accountId)),
        pushDisplayUnitElementClickedEventForID: (unitID, additionalProperties) =>
            CleverTapReact.pushDisplayUnitElementClickedEventForID(unitID, additionalProperties, toAccountArg(accountId)),

        // --- Product Config & Feature Flags (deprecated natively, still routed) ---
        setDefaultsMap: (productConfigMap) =>
            CleverTapReact.setDefaultsMap(productConfigMap, toAccountArg(accountId)),
        fetch: () =>
            CleverTapReact.fetch(toAccountArg(accountId)),
        fetchWithMinimumIntervalInSeconds: (intervalInSecs) =>
            CleverTapReact.fetchWithMinimumFetchIntervalInSeconds(intervalInSecs, toAccountArg(accountId)),
        activate: () =>
            CleverTapReact.activate(toAccountArg(accountId)),
        fetchAndActivate: () =>
            CleverTapReact.fetchAndActivate(toAccountArg(accountId)),
        setMinimumFetchIntervalInSeconds: (intervalInSecs) =>
            CleverTapReact.setMinimumFetchIntervalInSeconds(intervalInSecs, toAccountArg(accountId)),
        resetProductConfig: () =>
            CleverTapReact.reset(toAccountArg(accountId)),
        getProductConfigString: (key, callback) =>
            callWithCallback('getString', [key], callback, toAccountArg(accountId)),
        getProductConfigBoolean: (key, callback) =>
            callWithCallback('getBoolean', [key], callback, toAccountArg(accountId)),
        getNumber: (key, callback) =>
            callWithCallback('getDouble', [key], callback, toAccountArg(accountId)),
        getLastFetchTimeStampInMillis: (callback) =>
            callWithCallback('getLastFetchTimeStampInMillis', null, callback, toAccountArg(accountId)),
        getFeatureFlag: (name, defaultValue, callback) =>
            callWithCallback('getFeatureFlag', [name, defaultValue], callback, toAccountArg(accountId)),

        // --- InApp controls ---
        suspendInAppNotifications: () =>
            CleverTapReact.suspendInAppNotifications(toAccountArg(accountId)),
        discardInAppNotifications: (dismissInAppIfVisible = false) =>
            CleverTapReact.discardInAppNotifications(dismissInAppIfVisible, toAccountArg(accountId)),
        resumeInAppNotifications: () =>
            CleverTapReact.resumeInAppNotifications(toAccountArg(accountId)),
        dismissPipInApp: () =>
            CleverTapReact.dismissPipInApp(toAccountArg(accountId)),
        fetchInApps: (callback) =>
            callWithCallback('fetchInApps', null, callback, toAccountArg(accountId)),
        clearInAppResources: (expiredOnly) =>
            CleverTapReact.clearInAppResources(expiredOnly, toAccountArg(accountId)),

        // --- Product Experiences: Vars ---
        syncVariables: () =>
            CleverTapReact.syncVariables(toAccountArg(accountId)),
        syncVariablesinProd: (isProduction) =>
            CleverTapReact.syncVariablesinProd(isProduction, toAccountArg(accountId)),
        fetchVariables: (callback) =>
            callWithCallback('fetchVariables', null, callback, toAccountArg(accountId)),
        defineVariables: (variables) =>
            CleverTapReact.defineVariables(variables, toAccountArg(accountId)),
        defineFileVariable: (fileVariable) =>
            CleverTapReact.defineFileVariable(fileVariable, toAccountArg(accountId)),
        getVariable: (name, callback) =>
            callWithCallback('getVariable', [name], callback, toAccountArg(accountId)),
        getVariables: (callback) =>
            callWithCallback('getVariables', null, callback, toAccountArg(accountId)),
        onVariablesChanged: (handler) => {
            CleverTapReact.onVariablesChanged(toAccountArg(accountId));
            addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnVariablesChanged, handler);
        },
        onOneTimeVariablesChanged: (handler) => {
            const subscription = addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnOneTimeVariablesChanged, (event) => {
                handler(event);
                subscription.remove();
            });
            CleverTapReact.onOneTimeVariablesChanged(toAccountArg(accountId));
        },
        onValueChanged: (name, handler) => {
            CleverTapReact.onValueChanged(name, toAccountArg(accountId));
            addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnValueChanged, handler);
        },
        onVariablesChangedAndNoDownloadsPending: (handler) => {
            addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnVariablesChangedAndNoDownloadsPending, handler);
            CleverTapReact.onVariablesChangedAndNoDownloadsPending(toAccountArg(accountId));
        },
        onceVariablesChangedAndNoDownloadsPending: (handler) => {
            const subscription = addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnceVariablesChangedAndNoDownloadsPending, (event) => {
                handler(event);
                subscription.remove();
            });
            CleverTapReact.onceVariablesChangedAndNoDownloadsPending(toAccountArg(accountId));
        },
        onFileValueChanged: (name, handler) => {
            addListenerForHandle(accountId, CleverTapReact.getConstants().CleverTapOnFileValueChanged, handler);
            CleverTapReact.onFileValueChanged(name, toAccountArg(accountId));
        },
        variants: (callback) =>
            callWithCallback('variants', null, callback, toAccountArg(accountId)),

        // --- Custom templates ---
        // The active template context lives PER ACCOUNT natively: only the account
        // whose campaign presented the template can read its arguments or dismiss it.
        // Definitions stay app-wide (registered at launch for every account).
        syncCustomTemplates: () =>
            CleverTapReact.syncCustomTemplates(toAccountArg(accountId)),
        syncCustomTemplatesInProd: (isProduction) =>
            CleverTapReact.syncCustomTemplatesInProd(isProduction, toAccountArg(accountId)),
        customTemplateSetDismissed: (templateName) =>
            CleverTapReact.customTemplateSetDismissed(templateName, toAccountArg(accountId)),
        customTemplateSetPresented: (templateName) =>
            CleverTapReact.customTemplateSetPresented(templateName, toAccountArg(accountId)),
        customTemplateRunAction: (templateName, argName) =>
            CleverTapReact.customTemplateRunAction(templateName, argName, toAccountArg(accountId)),
        customTemplateGetStringArg: (templateName, argName) =>
            CleverTapReact.customTemplateGetStringArg(templateName, argName, toAccountArg(accountId)),
        customTemplateGetNumberArg: (templateName, argName) =>
            CleverTapReact.customTemplateGetNumberArg(templateName, argName, toAccountArg(accountId)),
        customTemplateGetBooleanArg: (templateName, argName) =>
            CleverTapReact.customTemplateGetBooleanArg(templateName, argName, toAccountArg(accountId)),
        customTemplateGetFileArg: (templateName, argName) =>
            CleverTapReact.customTemplateGetFileArg(templateName, argName, toAccountArg(accountId)),
        customTemplateGetObjectArg: (templateName, argName) =>
            CleverTapReact.customTemplateGetObjectArg(templateName, argName, toAccountArg(accountId)),
        customTemplateContextToString: (templateName) =>
            CleverTapReact.customTemplateContextToString(templateName, toAccountArg(accountId)),

        // --- OS-level methods: on the handle for shape consistency only.
        // They warn and do nothing; call them on the top-level CleverTap object. ---
        registerForPush: () => {
            console.warn('[CleverTap] registerForPush is not supported on account handles; call it on the top-level CleverTap object');
        },
        promptForPushPermission: (showFallbackSettings) => {
            console.warn('[CleverTap] promptForPushPermission is not supported on account handles; call it on the top-level CleverTap object');
        },
        promptPushPrimer: (value) => {
            console.warn('[CleverTap] promptPushPrimer is not supported on account handles; call it on the top-level CleverTap object');
        },
        // Why do these two stubs CALL the callback instead of only warning? A caller
        // that waits for the callback (or wraps it in a Promise) would otherwise wait
        // forever — the warning scrolls by, the await never resolves. Completing with
        // an error keeps every caller's control flow alive. Example:
        //   const granted = await promisify(handle.isPushPermissionGranted)();
        // hangs forever without this; with it, the promise rejects with a clear message.
        isPushPermissionGranted: (callback) => {
            console.warn('[CleverTap] isPushPermissionGranted is not supported on account handles; call it on the top-level CleverTap object');
            if (typeof callback === 'function') {
                callback('isPushPermissionGranted is not supported on account handles', null);
            }
        },
        getInitialUrl: (callback) => {
            console.warn('[CleverTap] getInitialUrl is not supported on account handles; call it on the top-level CleverTap object');
            if (typeof callback === 'function') {
                callback('getInitialUrl is not supported on account handles', null);
            }
        },
        createNotificationChannel: (channelId, channelName, channelDescription, importance, showBadge) => {
            console.warn('[CleverTap] createNotificationChannel is not supported on account handles; call it on the top-level CleverTap object');
        },
        createNotificationChannelWithSound: (channelId, channelName, channelDescription, importance, showBadge, sound) => {
            console.warn('[CleverTap] createNotificationChannelWithSound is not supported on account handles; call it on the top-level CleverTap object');
        },
        createNotificationChannelWithGroupId: (channelId, channelName, channelDescription, importance, groupId, showBadge) => {
            console.warn('[CleverTap] createNotificationChannelWithGroupId is not supported on account handles; call it on the top-level CleverTap object');
        },
        createNotificationChannelWithGroupIdAndSound: (channelId, channelName, channelDescription, importance, groupId, showBadge, sound) => {
            console.warn('[CleverTap] createNotificationChannelWithGroupIdAndSound is not supported on account handles; call it on the top-level CleverTap object');
        },
        createNotificationChannelGroup: (groupId, groupName) => {
            console.warn('[CleverTap] createNotificationChannelGroup is not supported on account handles; call it on the top-level CleverTap object');
        },
        deleteNotificationChannel: (channelId) => {
            console.warn('[CleverTap] deleteNotificationChannel is not supported on account handles; call it on the top-level CleverTap object');
        },
        deleteNotificationChannelGroup: (groupId) => {
            console.warn('[CleverTap] deleteNotificationChannelGroup is not supported on account handles; call it on the top-level CleverTap object');
        },
        createNotification: (extras) => {
            console.warn('[CleverTap] createNotification is not supported on account handles; call it on the top-level CleverTap object');
        },

        addListener: (eventName, handler) => addListenerForHandle(accountId, eventName, handler),
        // Like addListener, but the handler runs only ONCE — for the first matching
        // event of THIS account — and then detaches itself. Example: wait for account
        // B's first profile init without remembering to clean up:
        //   handleB.addOneTimeListener(CleverTap.CleverTapProfileDidInitialize, (e) => ...);
        // The subscription removes itself from inside the wrapper, so a second event
        // can never fire the handler again. Mirrors CleverTap.addOneTimeListener.
        addOneTimeListener: (eventName, handler) => {
            const subscription = addListenerForHandle(accountId, eventName, (event) => {
                handler(event);
                subscription.remove();
            });
            return subscription;
        },
        removeListener: (eventName) => removeListenersForHandle(accountId, eventName)
    };
    return Object.freeze(handle);
}

// Handles are memoized: getInstance('B') twice returns the same frozen object.
const handleCache = new Map();
function getOrMakeHandle(accountId) {
    if (!handleCache.has(accountId)) {
        handleCache.set(accountId, createHandle(accountId));
    }
    return handleCache.get(accountId);
}

var CleverTap = {
    CleverTapProfileDidInitialize: CleverTapReact.getConstants().CleverTapProfileDidInitialize,
    CleverTapProfileSync: CleverTapReact.getConstants().CleverTapProfileSync,
    CleverTapInAppNotificationDismissed: CleverTapReact.getConstants().CleverTapInAppNotificationDismissed,
    CleverTapInAppNotificationShowed: CleverTapReact.getConstants().CleverTapInAppNotificationShowed,
    CleverTapInAppNotificationButtonTapped: CleverTapReact.getConstants().CleverTapInAppNotificationButtonTapped,
    CleverTapCustomTemplatePresent: CleverTapReact.getConstants().CleverTapCustomTemplatePresent,
    CleverTapCustomFunctionPresent: CleverTapReact.getConstants().CleverTapCustomFunctionPresent,
    CleverTapCustomTemplateClose: CleverTapReact.getConstants().CleverTapCustomTemplateClose,
    FCM: CleverTapReact.getConstants().FCM,
    CleverTapInboxDidInitialize: CleverTapReact.getConstants().CleverTapInboxDidInitialize,
    CleverTapInboxMessagesDidUpdate: CleverTapReact.getConstants().CleverTapInboxMessagesDidUpdate,
    CleverTapInboxMessageButtonTapped: CleverTapReact.getConstants().CleverTapInboxMessageButtonTapped,
    CleverTapInboxMessageTapped: CleverTapReact.getConstants().CleverTapInboxMessageTapped,
    CleverTapDisplayUnitsLoaded: CleverTapReact.getConstants().CleverTapDisplayUnitsLoaded,
    CleverTapFeatureFlagsDidUpdate: CleverTapReact.getConstants().CleverTapFeatureFlagsDidUpdate, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidInitialize: CleverTapReact.getConstants().CleverTapProductConfigDidInitialize, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidFetch: CleverTapReact.getConstants().CleverTapProductConfigDidFetch, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapProductConfigDidActivate: CleverTapReact.getConstants().CleverTapProductConfigDidActivate, // @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
    CleverTapPushNotificationClicked: CleverTapReact.getConstants().CleverTapPushNotificationClicked,
    CleverTapPushPermissionResponseReceived: CleverTapReact.getConstants().CleverTapPushPermissionResponseReceived,
    CleverTapOnVariablesChanged: CleverTapReact.getConstants().CleverTapOnVariablesChanged,
    CleverTapOnOneTimeVariablesChanged: CleverTapReact.getConstants().CleverTapOnOneTimeVariablesChanged,
    CleverTapOnValueChanged: CleverTapReact.getConstants().CleverTapOnValueChanged,
    CleverTapOnVariablesChangedAndNoDownloadsPending: CleverTapReact.getConstants().CleverTapOnVariablesChangedAndNoDownloadsPending,
    CleverTapOnceVariablesChangedAndNoDownloadsPending: CleverTapReact.getConstants().CleverTapOnceVariablesChangedAndNoDownloadsPending,
    CleverTapOnFileValueChanged: CleverTapReact.getConstants().CleverTapOnFileValueChanged,

    /**
    * Add a CleverTap event listener
    * supported events are CleverTap.CleverTapProfileDidInitialize, CleverTap.CleverTapProfileSync,CleverTap.CleverTapOnInboxButtonClick
    * ,CleverTap.CleverTapOnInAppButtonClick,CleverTap.CleverTapOnDisplayUnitsLoaded and CleverTap.CleverTapInAppNotificationDismissed
    * @param {string} eventName - the CleverTap event name
    * @param {function(event)} your event handler
    */
    addListener: function (eventName, handler) {
        // Routed through the demux: fires for the default account's events (and untagged
        // global events). Returns a subscription: const sub = addListener(...); sub.remove().
        return addListenerForHandle(undefined, eventName, handler);
    },
    addOneTimeListener: function (eventName, handler) {
        const subscription = addListenerForHandle(undefined, eventName, (event) => {
            handler(event);
            subscription.remove();
        });
        return subscription;
    },

    /**
    * Removes the listeners registered through CleverTap.addListener for given eventName.
    * Listeners added on other account handles (or directly on the raw event emitter)
    * are NOT touched — see the CHANGELOG behavior note.
    *
    * @param {string} eventName -  name of the event whose registered listeners to remove
    */
    removeListener: function (eventName) {
        removeListenersForHandle(undefined, eventName);
    },

    /**
    *  @deprecated - Since version 0.5.0. Use removeListener(eventName) instead
    *  Remove all event listeners
    */
    removeListeners: function () {
        // Tear down the demux state too, so a later addListener starts clean
        // (native subscriptions are re-created on demand).
        nativeSubscriptions.forEach((subscription) => subscription.remove());
        nativeSubscriptions.clear();
        routedHandlers.clear();
        if (DeviceEventEmitter) {
            DeviceEventEmitter.removeAllListeners();
        }
    },

    /**
    * If an application is launched from a push notification click, returns the CleverTap deep link included in the push notification
    * @param {function(err, res)} callback that return the url as string in res or a string error in err
    */
    getInitialUrl: function (callback) {
        callWithCallback('getInitialUrl', null, callback);
    },

    /**
    * Call this method to set Locale. If Language is english and country is US the locale format which you can set is en_US
    * @param {string} locale - the locale string
    */
    setLocale: function (locale) {
        CleverTapReact.setLocale(locale, null);
    },

    /**
    * Registers the application to receive push notifications
    * only necessary for iOS.
    */
    registerForPush: function () {
        CleverTapReact.registerForPush();
    },

    /**
     * Manually set the push token on the CleverTap user profile
     * @param {string} token - the device token
     * @param {object} pushType - object with the following keys "type", "prefKey", "className", "messagingSDKClassName";
     */
    pushRegistrationToken: function (token, pushType) {
        console.log(`CleverTap RN | pushRegistrationToken| received : token: '${token}'`)
        CleverTapReact.pushRegistrationToken(token, pushType, null);
    },

    /**
     * Manually set the push token on the CleverTap user profile
     * @param {string} token - the fcm device token
     */
    setFCMPushToken: function (token) {
        console.log(`CleverTap RN | setFCMPushToken | received : token: '${token}'`)
        CleverTapReact.setFCMPushTokenAsString(token, null);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannel: function (channelId, channelName, channelDescription, importance, showBadge) {
        CleverTapReact.createNotificationChannel(channelId, channelName, channelDescription, importance, showBadge);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    * @param {string} sound - A String for setting the custom sound of the notification channel
    */
    createNotificationChannelWithSound: function (channelId, channelName, channelDescription, importance, showBadge, sound) {
        CleverTapReact.createNotificationChannelWithSound(channelId, channelName, channelDescription, importance, showBadge, sound);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {string} groupId - A String for setting the notification channel as a part of a notification group
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    */
    createNotificationChannelWithGroupId: function (channelId, channelName, channelDescription, importance, groupId, showBadge) {
        CleverTapReact.createNotificationChannelWithGroupId(channelId, channelName, channelDescription, importance, groupId, showBadge);
    },

    /**
    * Method to create Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    * @param {string} channelName - A String for setting the name of the notification channel
    * @param {string} channelDescription - A String for setting the description of the notification channel
    * @param {int} importance - An Integer value setting the importance of the notifications sent in this channel
    * @param {string} groupId - A String for setting the notification channel as a part of a notification group
    * @param {boolean} showBadge - A boolean value as to whether this channel shows a badge
    * @param {string} sound - A String for setting the custom sound of the notification channel
    */
    createNotificationChannelWithGroupIdAndSound: function (channelId, channelName, channelDescription, importance, groupId, showBadge, sound) {
        CleverTapReact.createNotificationChannelWithGroupIdAndSound(channelId, channelName, channelDescription, importance, groupId, showBadge, sound);
    },

    /**
    * Method to create Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    * @param {string} groupName - A String for setting the name of the notification channel group
    */
    createNotificationChannelGroup: function (groupId, groupName) {
        CleverTapReact.createNotificationChannelGroup(groupId, groupName);
    },

    /**
    * Method to delete Notification Channels in Android O
    * @param {string} channelId - A String for setting the id of the notification channel
    */
    deleteNotificationChannel: function (channelId) {
        CleverTapReact.deleteNotificationChannel(channelId);
    },

    /**
    * Method to delete Notification Channel Groups in Android O
    * @param {string} groupId - A String for setting the id of the notification channel group
    */
    deleteNotificationChannelGroup: function (groupId) {
        CleverTapReact.deleteNotificationChannelGroup(groupId);
    },

    /**
    * Method to show the App Inbox
    * @param {object} extras - key-value data from RemoteMessage.getData().  keys and values are strings
    */
    createNotification: function (extras) {
        CleverTapReact.createNotification(extras);
    },

    /**
    * Method to prompt the hard permission dialog directly, if the push primer is not required.
     * @param {string} showFallbackSettings - If the value is true then SDK shows an alert dialog which routes to app's notification settings page.
    */
    promptForPushPermission: function (showFallbackSettings) {
        CleverTapReact.promptForPushPermission(showFallbackSettings);
    },

    /**
    * Method to prompt the push primer for android 13 onwards.
    * @param {object} value - key-value belongs to the localInApp properties. Refer documentation for details.
    */
    promptPushPrimer: function (value) {
        CleverTapReact.promptPushPrimer(value);
    },

    /**
    * Returns true/false based on whether push permission is granted or denied.
    *
    * @param {function(err, res)} non-null callback to retrieve the result
    */
    isPushPermissionGranted: function (callback) {
        callWithCallback('isPushPermissionGranted', null, callback);
    },

    /**
     * Sets the user's consent for event and profile tracking.
     *
     * You must call this method separately for each active user profile,
     * for example, when switching user profiles using `onUserLogin`.
     *
     * Consent Scenarios:
     *
     * 1. **Complete Opt-Out**  
     *    `userOptOut = true`, `allowSystemEvents = false`  
     *    → No events (custom or system) are saved locally or remotely. Maximum privacy.
     *
     * 2. **Full Opt-In**  
     *    `userOptOut = false`, `allowSystemEvents = true`  
     *    → All events (custom and system) are tracked. Default behavior.
     *
     * 3. **Partial Opt-In**  
     *    `userOptOut = true`, `allowSystemEvents = true`  
     *    → Only system events (e.g., app launch, notification viewed) are tracked. Custom events are ignored.
     *
     * ⚠️ The combination `userOptOut = false` and `allowSystemEvents = false` is invalid.  
     * In such cases, the SDK defaults to **Full Opt-In**.
     *
     * To re-enable full tracking after opting out, call with:  
     * `userOptOut = false`, `allowSystemEvents = true`.
     *
     * @param {boolean} userOptOut - Set to `true` to disable custom event tracking.
     * @param {boolean} allowSystemEvents - Set to `true` to allow system-level event tracking.
     * @returns {void}
 */
    setOptOut: function(userOptOut, allowSystemEvents = false) {
        CleverTapReact.setOptOut(userOptOut, allowSystemEvents, null);
    },

    /**
    * Sets the CleverTap SDK to offline mode
    * @param {boolean} value - A boolean for enabling or disabling sending events for current user
    */
    setOffline: function (value) {
        CleverTapReact.setOffline(value, null);
    },

    /**
    * Clears a backend-imposed mute state on the CleverTap SDK, resuming network operations immediately.
    */
    unmute: function () {
        CleverTapReact.unmute(null);
    },

    /**
    * Enables the reporting of device network related information, including IP address. This reporting is disabled by default.
    * @param {boolean} - A boolean for enabling or disabling device network related information to be sent to CleverTap
    */
    enableDeviceNetworkInfoReporting: function (value) {
        CleverTapReact.enableDeviceNetworkInfoReporting(value, null);
    },

    /**
    * Enables the personalization API.  Call this prior to using the profile/event API getters
    */
    enablePersonalization: function () {
        CleverTapReact.enablePersonalization(null);
    },

    /**
    * Disables the personalization API.
    */
    disablePersonalization: function () {
        CleverTapReact.disablePersonalization(null);
    },

    /**
    * Record a Screen View
    * @param {string} screenName - the name of the screen
    */
    recordScreenView: function (screenName) {
        CleverTapReact.recordScreenView(screenName, null);
    },

    /**
    * Record an event with optional event properties
    * @param {string} eventName - the name of the event
    * @param {object} props - the key-value properties of the event.
    * keys are strings and values can be string, number or boolean.
    */
    recordEvent: function (eventName, props) {
        convertDateToEpochInProperties(props);
        // The trailing accountId MUST be passed explicitly (null = default account):
        // the old-architecture Android bridge throws on a missing trailing argument.
        CleverTapReact.recordEvent(eventName, props, null);
    },

    /**
    * Record the special Charged event
    * @param {object} details - the key-value properties for the transaction.
    * @param {array<object>} items - an array of objects containing the key-value data for the items that make up the transaction.
    */
    recordChargedEvent: function (details, items) {
        convertDateToEpochInProperties(details);
        if (Array.isArray(items) && items.length) {
            items.forEach(value => {
                convertDateToEpochInProperties(value);
            });
        }
        CleverTapReact.recordChargedEvent(details, items, null);
    },

    /**
    * Get the time of the first occurrence of an event
    * @deprecated - Since version 3.2.0. Use getUserEventLog() instead
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetFirstTime: function (eventName, callback) {
        callWithCallback('eventGetFirstTime', [eventName], callback, null);
    },

    /**
    * Get the time of the most recent occurrence of an event
    * @deprecated - Since version 3.2.0. Use getUserEventLog() instead
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    eventGetLastTime: function (eventName, callback) {
        callWithCallback('eventGetLastTime', [eventName], callback, null);
    },

    /**
    * Get the number of occurrences of an event
    * @deprecated - Since version 3.2.0. Use getUserEventLogCount() instead
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of int
    */
    eventGetOccurrences: function (eventName, callback) {
        callWithCallback('eventGetOccurrences', [eventName], callback, null);
    },

    /**
    * Get the summary details of an event
    * @deprecated - Since version 3.2.0. Use getUserEventLog() instead
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of object {"eventName": <string>, "firstTime":<epoch seconds>, "lastTime": <epoch seconds>, "count": <int>} or empty object
    */
    eventGetDetail: function (eventName, callback) {
        callWithCallback('eventGetDetail', [eventName], callback, null);
    },

    /**
    * Get the user's event history
    * @deprecated - Since version 3.2.0. Use getUserEventLogHistory() instead
    * @param {function(err, res)} callback that returns a res of object {"eventName1":<event1 details object>, "eventName2":<event2 details object>}
    */
    getEventHistory: function (callback) {
        callWithCallback('getEventHistory', null, callback, null);
    },
    /**
    * Get the details of a specific event
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of object {"eventName": <string>, "firstTime":<epoch seconds>, "lastTime": <epoch seconds>, "count": <int>, "deviceID": <string>, "normalizedEventName": <string>} or empty object
    */
    getUserEventLog: function (eventName, callback) {
        callWithCallback('getUserEventLog', [eventName], callback, null);
    },
    
    /**
    * Get the count of times an event occured
    * @param {string} eventName - the name of the event
    * @param {function(err, res)} callback that returns a res of int
    */
    getUserEventLogCount: function (eventName, callback) {
        callWithCallback('getUserEventLogCount', [eventName], callback, null);
    },

    /**
    * Get full event hostory for current user
    * @param {function(err, res)} callback that returns a res of object {"eventName1":<event1 details object>, "eventName2":<event2 details object>}
    */
    getUserEventLogHistory: function (callback) {
        callWithCallback('getUserEventLogHistory', null, callback, null);
    },

    /**
    * Set the user's location as a latitude,longitude coordinate
    * @param {float} latitude
    * @param {float} longitude
    */
    setLocation: function (latitude, longitude) {
        CleverTapReact.setLocation(latitude, longitude, null);
    },

    /**
     * @deprecated - Since version 0.6.0. Use getCleverTapID(callback) instead
    * Get a unique CleverTap identifier suitable for use with install attribution providers
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapAttributionIdentifier: function (callback) {
        callWithCallback('profileGetCleverTapAttributionIdentifier', null, callback, null);
    },

    /**
     * @deprecated - Since version 0.6.0. Use getCleverTapID(callback) instead
    * Get the user profile's CleverTap identifier value
    * @param {function(err, res)} callback that returns a string res
    */
    profileGetCleverTapID: function (callback) {
        callWithCallback('profileGetCleverTapID', null, callback, null);
    },

    /**
    * Creates a separate and distinct user profile identified by one or more of Identity, Email, FBID or GPID values, and populated with the key-values included in the profile dictionary.
    * If your app is used by multiple users, you can use this method to assign them each a unique profile to track them separately.
    * If instead you wish to assign multiple Identity, Email, FBID and/or GPID values to the same user profile, use profileSet rather than this method.
    * If none of Identity, Email, FBID or GPID is included in the profile object, all properties values will be associated with the current user profile.
    * When initially installed on this device, your app is assigned an "anonymous" profile.
    * The first time you identify a user on this device (whether via onUserLogin or profileSet), the "anonymous" history on the device will be associated with the newly identified user.
    * Then, use this method to switch between subsequent separate identified users.
    * Please note that switching from one identified user to another is a costly operation
    * in that the current session for the previous user is automatically closed
    * and data relating to the old user removed, and a new session is started
    * for the new user and data for that user refreshed via a network call to CleverTap.
    * In addition, any global frequency caps are reset as part of the switch.
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    onUserLogin: function (profile) {
        convertDateToEpochInProperties(profile);
        CleverTapReact.onUserLogin(profile, null);
    },

    /**
    * Set key-value properties on a user profile
    * @param {object} profile - key-value profile properties.  keys are strings and values can be string, number or boolean.
    */
    profileSet: function (profile) {
        convertDateToEpochInProperties(profile);
        CleverTapReact.profileSet(profile, null);
    },

    /**
    * Get the value of a profile property
    * @param {string} the property key
    * @param {function(err, res)} callback that returns a res of the property value or null
    */
    profileGetProperty: function (key, callback) {
        callWithCallback('profileGetProperty', [key], callback, null);
    },

    /**
    * Remove a key-value from the user profile. Alternatively this method can also be used to remove PII data
    * (for eg. Email,Name,Phone), locally from database and shared prefs
    * @param {string} the key to remove
    */
    profileRemoveValueForKey: function (key) {
        CleverTapReact.profileRemoveValueForKey(key, null);
    },

    /**
    * Set an array of strings as a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileSetMultiValuesForKey: function (values, key) {
        CleverTapReact.profileSetMultiValues(values, key, null);
    },

    /**
    * Add a string value to a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileAddMultiValueForKey: function (value, key) {
        CleverTapReact.profileAddMultiValue(value, key, null);
    },

    /**
    * Add an array of strings to a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileAddMultiValuesForKey: function (values, key) {
        CleverTapReact.profileAddMultiValues(values, key, null);
    },

    /**
    * Remove a string value from a multi-value user profile property
    * @param {string} value
    * @param {string} the property key
    */
    profileRemoveMultiValueForKey: function (value, key) {
        CleverTapReact.profileRemoveMultiValue(value, key, null);
    },

    /**
    * Remove an array of strings from a multi-value user profile property
    * @param {array} an array of string values
    * @param {string} the property key
    */
    profileRemoveMultiValuesForKey: function (values, key) {
        CleverTapReact.profileRemoveMultiValues(values, key, null);
    },

    /**
    * This method is used to increment the given value
    *
    * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
    * @param key   {string} profile property
    */
    profileIncrementValueForKey: function (value, key) {
        CleverTapReact.profileIncrementValueForKey(value, key, null);
    },

    /**
     * This method is used to decrement the given value
     *
     * @param value {Number} can be int,double or float only (NaN,Infinity etc not supported)
     * @param key   {string} profile property
     */
    profileDecrementValueForKey: function (value, key) {
        CleverTapReact.profileDecrementValueForKey(value, key, null);
    },

    /**
    * Manually track the utm app install referrer
    * @param {string} the utm referrer source
    * @param {string} the utm referrer medium
    * @param {string} the utm referrer campaign
    */
    pushInstallReferrer: function (source, medium, campaign) {
        CleverTapReact.pushInstallReferrer(source, medium, campaign, null);
    },

    /**
    * Get the elapsed time of the current user session
    * @param {function(err, res)} callback that returns a res of int seconds
    */
    sessionGetTimeElapsed: function (callback) {
        callWithCallback('sessionGetTimeElapsed', null, callback, null);
    },

    /**
    * Get the total number of vists by the user
    * @deprecated - Since version 3.2.0. Use getUserAppLaunchCount() instead
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetTotalVisits: function (callback) {
        callWithCallback('sessionGetTotalVisits', null, callback, null);
    },

    /**
    * Get timestamp of user's last app visit
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    getUserLastVisitTs: function (callback) {
        callWithCallback('getUserLastVisitTs', null, callback, null);
    },
    
    /**
    * Get the total number of times user has launched the app
    * @param {function(err, res)} callback that returns a res of int
    */
    getUserAppLaunchCount: function (callback) {
        callWithCallback('getUserAppLaunchCount', null, callback, null);
    },

    /**
    * Get the number of screens viewed by the user during the session
    * @param {function(err, res)} callback that returns a res of int
    */
    sessionGetScreenCount: function (callback) {
        callWithCallback('sessionGetScreenCount', null, callback, null);
    },

    /**
    * Get the most recent previous visit time of the user
    * @deprecated - Since version 3.2.0. Use getUserLastVisits() instead
    * @param {function(err, res)} callback that returns a res of epoch seconds or -1
    */
    sessionGetPreviousVisitTime: function (callback) {
        callWithCallback('sessionGetPreviousVisitTime', null, callback, null);
    },

    /**
    * Get the utm referrer info for the current session
    * @param {function(err, res)} callback that returns a res of object {"source": <string>, "medium": <string>, "campaign": <string>} or empty object
    */
    sessionGetUTMDetails: function (callback) {
        callWithCallback('sessionGetUTMDetails', null, callback, null);
    },

    /**
    * Method to initalize the App Inbox
    */
    initializeInbox: function () {
        CleverTapReact.initializeInbox(null);
    },

    /**
     * Triggers an on-demand App Inbox refresh from the server.
     * @param {function(err, res)} callback optional callback with a boolean flag indicating whether the fetch was successful
     */
    fetchInbox: function (callback) {
        callWithCallback('fetchInbox', null, callback, null);
    },

    /**
    * Method to show the App Inbox
    * @param {object} styleConfig - key-value profile properties.  keys and values are strings
    */
    showInbox: function (styleConfig) {
        CleverTapReact.showInbox(styleConfig, null);
    },

    /**
     * Method to dismiss the App Inbox
     */
    dismissInbox: function () {
        CleverTapReact.dismissInbox(null);
    },

    /**
     * Get the total number of Inbox Messages
     * @param {function(err, res)} callback that returns a res of count of inbox messages or -1
     */
    getInboxMessageCount: function (callback) {
        callWithCallback('getInboxMessageCount', null, callback, null);
    },

    /**
     * Get the total number of Unread Inbox Messages
     * @param {function(err, res)} callback that returns a res of count of unread inbox messages or -1
     */
    getInboxMessageUnreadCount: function (callback) {
        callWithCallback('getInboxMessageUnreadCount', null, callback, null);
    },

    /**
     * Get All inbox messages
     * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
     */
    getAllInboxMessages: function (callback) {
        callWithCallback('getAllInboxMessages', null, callback, null);
    },

    /**
     * Get All unread inbox messages
     * @param {function(err, res)} callback that returns a list of json string representation of CTInboxMessage
     */
    getUnreadInboxMessages: function (callback) {
        callWithCallback('getUnreadInboxMessages', null, callback, null);
    },

    /**
     * Get Inbox Message that belongs to the given message id
     * @param {function(err, res)} callback that returns json string representation of CTInboxMessage
     */
    getInboxMessageForId: function (messageId, callback) {
        callWithCallback('getInboxMessageForId', [messageId], callback, null);
    },

    /**
     * Deletes Inbox Message that belongs to the given message id
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    deleteInboxMessageForId: function (messageId) {
        CleverTapReact.deleteInboxMessageForId(messageId, null);
    },

    /**
     * Deletes multiple Inbox Messages that belongs to the given message ids
     * @param {array} messageIds a collection of ids of inbox messages
     */
    deleteInboxMessagesForIDs: function (messageIds) {
        CleverTapReact.deleteInboxMessagesForIDs(messageIds, null);
    },

    /**
     * Marks Inbox Message that belongs to the given message id as read
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    markReadInboxMessageForId: function (messageId) {
        CleverTapReact.markReadInboxMessageForId(messageId, null);
    },

    /**
     * Marks multiple Inbox Messages that belongs to the given message ids as read
     * @param {array} messageIds a collection of ids of inbox messages
     */
    markReadInboxMessagesForIDs: function (messageIds) {
        CleverTapReact.markReadInboxMessagesForIDs(messageIds, null);
    },

    /**
     * Pushes the Notification Clicked event for App Inbox to CleverTap.
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    pushInboxNotificationClickedEventForId: function (messageId) {
        CleverTapReact.pushInboxNotificationClickedEventForId(messageId, null);
    },

    /**
     * Pushes the Notification Viewed event for App Inbox to CleverTap.
     * @param {string} message id of inbox message of type CTInboxMessage
     */
    pushInboxNotificationViewedEventForId: function (messageId) {
        CleverTapReact.pushInboxNotificationViewedEventForId(messageId, null);
    },

    /**
     * Get all display units
     * @param {function(err, res)} callback that returns a list of json string representation of CleverTapDisplayUnit
     */
    getAllDisplayUnits: function (callback) {
        callWithCallback('getAllDisplayUnits', null, callback, null);
    },

    /**
     * Get display unit for given unitID.
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     * @param {function(err, res)} callback that returns a json string representation of CleverTapDisplayUnit
     */
    getDisplayUnitForId: function (unitID, callback) {
        callWithCallback('getDisplayUnitForId', [unitID], callback, null);
    },

    /**
     * Raises the Display Unit Viewed event
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     */
    pushDisplayUnitViewedEventForID: function (unitID) {
        CleverTapReact.pushDisplayUnitViewedEventForID(unitID, null);
    },

    /**
     * Raises the Display Unit Clicked event
     * @param {string} unit id of display unit of type CleverTapDisplayUnit
     */
    pushDisplayUnitClickedEventForID: function (unitID) {
        CleverTapReact.pushDisplayUnitClickedEventForID(unitID, null);
    },

    /**
     * Records an element-level Notification Clicked event for a specific element within a Display Unit
     * @param {string} unitID - unique id of the display unit
     * @param {object} additionalProperties - optional per-click context (e.g. element id, action url, custom KVs)
     */
    pushDisplayUnitElementClickedEventForID: function (unitID, additionalProperties) {
        CleverTapReact.pushDisplayUnitElementClickedEventForID(unitID, additionalProperties, null);
    },


    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Sets default product config params using the given object.
     * @param {object} productConfigMap - key-value product config properties.  keys are strings and values can be string, double, integer, boolean or json in string format.
     */
    setDefaultsMap: function (productConfigMap) {
        CleverTapReact.setDefaultsMap(productConfigMap, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Starts fetching product configs, adhering to the default minimum fetch interval.
     */
    fetch: function () {
        CleverTapReact.fetch(null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Starts fetching product configs, adhering to the specified minimum fetch interval in seconds.
     * @param {int} intervalInSecs - minimum fetch interval in seconds.
     */
    fetchWithMinimumIntervalInSeconds: function (intervalInSecs) {
        CleverTapReact.fetchWithMinimumFetchIntervalInSeconds(intervalInSecs, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Activates the most recently fetched product configs, so that the fetched key value pairs take effect.
     */
    activate: function () {
        CleverTapReact.activate(null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Asynchronously fetches and then activates the fetched product configs.
     */
    fetchAndActivate: function () {
        CleverTapReact.fetchAndActivate(null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Sets the minimum interval in seconds between successive fetch calls.
     * @param {int} intervalInSecs - interval in seconds between successive fetch calls.
     */
    setMinimumFetchIntervalInSeconds: function (intervalInSecs) {
        CleverTapReact.setMinimumFetchIntervalInSeconds(intervalInSecs, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Deletes all activated, fetched and defaults configs as well as all Product Config settings.
     */
    resetProductConfig: function () {
        CleverTapReact.reset(null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     *
     * Returns the product config parameter value for the given key as a String.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type string if present else blank
     */
    getProductConfigString: function (key, callback) {
        callWithCallback('getString', [key], callback, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the product config parameter value for the given key as a boolean.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type boolean if present else false
     */
    getProductConfigBoolean: function (key, callback) {
        callWithCallback('getBoolean', [key], callback, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the product config parameter value for the given key as a number.
     * @param {string} the property key
     * @param {function(err, res)} callback that returns a value of type number if present else 0
     */
    getNumber: function (key, callback) {
        callWithCallback('getDouble', [key], callback, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Returns the last fetched timestamp in millis.
     * @param {function(err, res)} callback that returns value of timestamp in millis as a string.
     */
    getLastFetchTimeStampInMillis: function (callback) {
        callWithCallback('getLastFetchTimeStampInMillis', null, callback, null);
    },

    /**
     * @deprecated - Since version 1.1.0 and will be removed in the future versions of this SDK.
     * 
     * Getter to return the feature flag configured at the dashboard
     * @param {string} key of the feature flag
     * @param {string} default value of the key, in case we don't find any feature flag with the key.
     * @param {function(err, res)} callback that returns a feature flag value of type boolean if present else provided default value
     */
    getFeatureFlag: function (name, defaultValue, callback) {
        callWithCallback('getFeatureFlag', [name, defaultValue], callback, null);
    },

    /**
     * Returns a unique identifier through callback by which CleverTap identifies this user
     *
     * @param {function(err, res)} non-null callback to retrieve identifier
     */
    getCleverTapID: function (callback) {
        callWithCallback('getCleverTapID', null, callback, null);
    },

    /**
     * Suspends display of InApp Notifications.
     * The InApp Notifications are queued once this method is called
     * and will be displayed once resumeInAppNotifications() is called.
     */
    suspendInAppNotifications: function () {
        CleverTapReact.suspendInAppNotifications(null);
    },

    /**
     * Suspends the display of InApp Notifications and discards any new InApp Notifications to be shown
     * after this method is called.
     * The InApp Notifications will be displayed only once resumeInAppNotifications() is called.
     * @param {boolean} dismissInAppIfVisible - Optional. If true, dismisses the currently visible InApp notification.
     */
    discardInAppNotifications: function (dismissInAppIfVisible = false) {
        CleverTapReact.discardInAppNotifications(dismissInAppIfVisible, null);
    },

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
    resumeInAppNotifications: function () {
        CleverTapReact.resumeInAppNotifications(null);
    },

    /**
     * Dismisses the currently visible Picture-in-Picture (PIP) InApp Notification, if any.
     *
     * This is a no-op when no PIP InApp Notification is visible, and other InApp Notification
     * types are never affected.
     *
     * Note: dismissing frees the InApp display slot, so the next queued InApp Notification (if
     * any) may show immediately. To keep a screen free of all InApp Notifications, pair this with
     * suspendInAppNotifications() on screen entry and resumeInAppNotifications() on exit.
     */
    dismissPipInApp: function () {
        CleverTapReact.dismissPipInApp(null);
    },

    /**
    * Set the SDK debug level
    * @param {int} 0 = off, 1 = on
    */
    setDebugLevel: function (level) {
        CleverTapReact.setDebugLevel(level);
    },

    /**
     * Change the native instance of CleverTapAPI by using the instance for
     * specific account. Used by Leanplum RN SDK.
     *
     * @param accountId The ID of the account to use when switching instance.
     */
    setInstanceWithAccountId: function (accountId) {
        // Legacy "slot swap": top-level calls AND listeners follow this account from now on.
        slotSwapped = true;
        currentDefaultAccountId = accountId;
        CleverTapReact.setInstanceWithAccountId(accountId);
    },

    /**
    * Creates an additional CleverTap account from JavaScript and resolves with its handle.
    *
    * Config semantics match the native SDKs exactly: on a FRESH app launch the config
    * you pass is applied (and persisted by the native SDK) — so fetching your config
    * from a server and calling createInstance on every launch works, and config changes
    * take effect on the next launch. Calling createInstance again for the same account
    * in the SAME app run resolves with the existing instance and the new config is not
    * applied (the native SDK keeps the original for the life of the process).
    *
    * @example
    * const accountB = await CleverTap.createInstance({
    *     accountId: 'ACCT_B', accountToken: 'TOK_B', region: 'eu1'
    * });
    * accountB.recordEvent('Purchase', { amount: 9 });
    *
    * @param {object} config - { accountId, accountToken, region?, proxyDomain?,
    * spikyProxyDomain?, identityKeys?, logLevel?, encryptionLevel?, encryptionInTransit?,
    * useCustomCleverTapId? }. Note: on iOS, region wins over proxyDomain (warned);
    * Android applies both.
    * @returns {Promise<object>} resolves with the account's handle
    */
    createInstance: function (config) {
        return CleverTapReact.createInstance(config).then((result) => getOrMakeHandle(result.accountId));
    },

    /**
    * Returns the handle for an account. ALWAYS returns a handle (never null): the native
    * SDKs persist account configs, so an account may exist natively even when this app run
    * never called createInstance. Calls on a handle whose account does not exist natively
    * log one warning and do nothing.
    *
    * @example
    * const accountB = CleverTap.getInstance('ACCT_B');
    * accountB.addListener(CleverTap.CleverTapProfileDidInitialize, (e) => { });
    *
    * @param {string} accountId - The account id
    * @returns {object} the account's handle
    */
    getInstance: function (accountId) {
        if (typeof accountId !== 'string' || accountId.length === 0) {
            console.error('[CleverTap] getInstance called with an invalid accountId (' +
                accountId + '); returning the DEFAULT account handle. Pass the real ' +
                'account id string to address a specific account.');
            return getOrMakeHandle(undefined);
        }
        return getOrMakeHandle(accountId);
    },

    /**
    * Uploads variables to the server. Requires Development/Debug build/configuration.
    */
    syncVariables: function () {
        CleverTapReact.syncVariables(null);
    },

    /**
    * Uploads variables to the server.
    *
    * @param {boolean} isProduction Provide `true` if variables must be sync in Productuon build/configuration.
    */
    syncVariablesinProd: function (isProduction) {
        CleverTapReact.syncVariablesinProd(isProduction, null);
    },

    /**
    * Forces variables to update from the server.
    *
    */
    fetchVariables: function (callback) {
        callWithCallback('fetchVariables', null, callback, null);
    },

    /**
     * Create variables. 
     * 
     * @param {object} variables The JSON Object specifying the varibles to be created.
     */
    defineVariables: function (variables) {
        CleverTapReact.defineVariables(variables, null);
    },

    /**
    * Create File Variable
    * @param {string} fileVariable - the file variable string
    */
    defineFileVariable: function (fileVariable) {
        CleverTapReact.defineFileVariable(fileVariable, null);
    },
    
    /**
     * Get a variable or a group for the specified name.
     * 
     * @param {string} name - name.
     */
    getVariable: function (name, callback) {
        callWithCallback('getVariable', [name], callback, null);
    },

    /**
     * Get all variables via a JSON object.
     * 
     */
    getVariables: function (callback) {
        callWithCallback('getVariables', null, callback, null);
    },

    /**
     *  Adds a callback to be invoked when variables are initialised with server values. Will be called each time new values are fetched.
     * 
     * @param {function} handler The callback to add
     */
    onVariablesChanged: function (handler) {
        CleverTapReact.onVariablesChanged(null);
        this.addListener(CleverTapReact.getConstants().CleverTapOnVariablesChanged, handler);
    },

    /**
     *  Adds a callback to be invoked only once on app start, or when added if server values are already received
     *
     * @param {function} handler The callback to add
     */
    onOneTimeVariablesChanged: function (handler) {
        this.addOneTimeListener(CleverTapReact.getConstants().CleverTapOnOneTimeVariablesChanged, handler);
        CleverTapReact.onOneTimeVariablesChanged(null);
    },

    /**
     * Called when the value of the variable changes.
     * 
     * @param {name} string the name of the variable
     * @param {function} handler The callback to add
     */
    onValueChanged: function (name, handler) {
        CleverTapReact.onValueChanged(name, null);
        this.addListener(CleverTapReact.getConstants().CleverTapOnValueChanged, handler);
    },

    /**
     *  Adds a callback to be invoked when variables are initialised with server values. Will be called each time new values are fetched.
     * 
     * @param {function} handler The callback to add
     */
    onVariablesChangedAndNoDownloadsPending: function (handler) {
        this.addListener(CleverTapReact.getConstants().CleverTapOnVariablesChangedAndNoDownloadsPending, handler);
        CleverTapReact.onVariablesChangedAndNoDownloadsPending(null);
    },

    /**
     *  Adds a callback to be invoked only once for when new values are fetched and downloaded
     *
     * @param {function} handler The callback to add
     */
    onceVariablesChangedAndNoDownloadsPending: function (handler) {
        this.addOneTimeListener(CleverTapReact.getConstants().CleverTapOnceVariablesChangedAndNoDownloadsPending, handler);
        CleverTapReact.onceVariablesChangedAndNoDownloadsPending(null);
    },

    /**
     * Called when the value of the file variable is downloaded and ready.
     * 
     * @param {name} string the name of the file variable
     * @param {function} handler The callback to add
     */
    onFileValueChanged: function (name, handler) {
        this.addListener(CleverTapReact.getConstants().CleverTapOnFileValueChanged, handler);
        CleverTapReact.onFileValueChanged(name, null);
    },

    /**
     * Fetches In Apps from server.
     *
     * @param callback {function(err, res)} a callback with a boolean flag whether the update was successful
     */
    fetchInApps: function (callback) {
        callWithCallback('fetchInApps', null, callback, null);
    },

    /**
     * Deletes all images and gifs which are preloaded for inapps in cs mode
     *
     * @param {boolean} expiredOnly to clear only assets which will not be needed further for inapps
     */
    clearInAppResources: function (expiredOnly) {
        CleverTapReact.clearInAppResources(expiredOnly, null);
    },

    /**
     * Uploads Custom in-app templates and app functions to the server.
     * Requires Development/Debug build/configuration.
     */
    syncCustomTemplates: function () {
        CleverTapReact.syncCustomTemplates(null);
    },

    /**
     * Uploads Custom in-app templates and app functions to the server.
     *
     * @param {boolean} isProduction Provide `true` if templates must be sync in Productuon build/configuration.
     */
    syncCustomTemplatesInProd: function (isProduction) {
        CleverTapReact.syncCustomTemplatesInProd(isProduction, null)
    },

    /**
     * Returns information about the active variants for the current user. Each variant will contain
     * an "id" key mapping to the numeric ID of the variant.
     * 
     * @param {function(err, res)} callback that returns a list of variant objects
     */
    variants: function (callback) {
        callWithCallback('variants', null, callback, null);
    },

    /**
     * Notify the SDK that an active custom template is dismissed. The active custom template is considered to be
     * visible to the user until this method is called. Since the SDK can show only one InApp message at a time, all
     * other messages will be queued until the current one is dismissed.
     * 
     * @param {string} templateName The name of the active template
     */
    customTemplateSetDismissed: function (templateName) {
        return CleverTapReact.customTemplateSetDismissed(templateName, null);
    },

    /**
     * Notify the SDK that an active custom template is presented to the user
     * 
     * @param {string} templateName The name of the active template
     */
    customTemplateSetPresented: function (templateName) {
        return CleverTapReact.customTemplateSetPresented(templateName, null);
    },

    /**
     * Trigger a custom template action argument by name.
     * 
     * @param {string} templateName The name of an active template for which the action is defined
     * @param {string} argName The action argument name
     */
    customTemplateRunAction: function (templateName, argName) {
        return CleverTapReact.customTemplateRunAction(templateName, argName, null);
    },

    /**
     * Retrieve a string argument by name.
     *
     * @param {string} templateName The name of an active template for which the argument is defined
     * @param {string} argName The action argument name
     * 
     * @returns {string} The argument value or null if no such argument is defined for the template.
     */
    customTemplateGetStringArg: function (templateName, argName) {
       return CleverTapReact.customTemplateGetStringArg(templateName, argName, null);
    },

    /**
     * Retrieve a number argument by name.
     *
     * @param {string} templateName The name of an active template for which the argument is defined
     * @param {string} argName The action argument name
     * 
     * @returns {number} The argument value or null if no such argument is defined for the template.
     */
    customTemplateGetNumberArg: function (templateName, argName) {
        return CleverTapReact.customTemplateGetNumberArg(templateName, argName, null);
    },

    /**
     * Retrieve a boolean argument by name.
     *
     * @param {string} templateName The name of an active template for which the argument is defined
     * @param {stirng} argName The action argument name
     * 
     * @returns {boolean} The argument value or null if no such argument is defined for the template.
     */
    customTemplateGetBooleanArg: function (templateName, argName) {
        return CleverTapReact.customTemplateGetBooleanArg(templateName, argName, null);
    },

    /**
     * Retrieve a file argument by name.
     *
     * @param {string} templateName The name of an active template for which the argument is defined
     * @param {string} argName The action argument name
     * 
     * @returns {string} The file path to the file or null if no such argument is defined for the template.
     */
    customTemplateGetFileArg: function (templateName, argName) {
        return CleverTapReact.customTemplateGetFileArg(templateName, argName, null);
    },

    /**
     * Retrieve an object argument by name.
     *
     * @param {string} templateName The name of an active template for which the argument is defined
     * @param {string} argName The action argument name
     * 
     * @returns {any} The argument value or null if no such argument is defined for the template.
     */
    customTemplateGetObjectArg: function (templateName, argName) {
        return CleverTapReact.customTemplateGetObjectArg(templateName, argName, null);
    },

    /**
     * Get a string representation of an active's template context with information about all arguments. 
     * 
     * @param {string} templateName The name of an active template
     * @returns {string}
     */
    customTemplateContextToString: function (templateName) {
        return CleverTapReact.customTemplateContextToString(templateName, null);
    }
};

function convertDateToEpochInProperties(map) {
    /**
     * Conversion of date object in suitable CleverTap format(Epoch)
     * Recursively handles nested objects and arrays
     */
    if (map) {
        for (let [key, value] of Object.entries(map)) {
            if (Object.prototype.toString.call(value) === '[object Date]') {
                map[key] = "$D_" + Math.floor(value.getTime() / 1000);
            } else if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
                // Recursively convert dates in nested objects
                convertDateToEpochInProperties(value);
            } else if (Array.isArray(value)) {
                  value.forEach((item, index) => {
                      if (Object.prototype.toString.call(item) === '[object Date]') {
                          value[index] = "$D_" + Math.floor(item.getTime() / 1000);
                      } else if (item !== null && typeof item === 'object') {
                          convertDateToEpochInProperties(item);
                      }
                  });
              }
        }
    }

};

module.exports = CleverTap;
