package com.clevertap.react;

import static com.clevertap.react.CleverTapUtils.convertObjectToWritableMap;
import static com.clevertap.react.CleverTapUtils.getWritableArrayFromDisplayUnitList;
import static com.clevertap.react.Constants.FCM;
import static com.clevertap.react.Constants.REACT_MODULE_NAME;

import android.annotation.SuppressLint;
import android.location.Location;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.CleverTapInstanceConfig;
import com.clevertap.android.sdk.cryption.EncryptionLevel;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.UTMDetail;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.clevertap.android.sdk.events.EventDetail;
import com.clevertap.android.sdk.featureFlags.CTFeatureFlagsController;
import com.clevertap.android.sdk.inapp.CTLocalInApp;
import com.clevertap.android.sdk.FetchInboxCallback;
import com.clevertap.android.sdk.inapp.callbacks.FetchInAppsCallback;
import com.clevertap.android.sdk.inapp.customtemplates.CustomTemplateContext;
import com.clevertap.android.sdk.inbox.CTInboxMessage;
import com.clevertap.android.sdk.interfaces.OnInitCleverTapIDListener;
import com.clevertap.android.sdk.product_config.CTProductConfigController;
import com.clevertap.android.sdk.pushnotification.PushType;
import com.clevertap.android.sdk.usereventlogs.UserEventLog;
import com.clevertap.android.sdk.variables.CTVariableUtils;
import com.clevertap.android.sdk.variables.Var;
import com.clevertap.android.sdk.variables.callbacks.FetchVariablesCallback;
import com.clevertap.android.sdk.variables.callbacks.VariableCallback;
import com.clevertap.android.sdk.variables.callbacks.VariablesChangedCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

public class CleverTapModuleImpl {

    private static final String TAG = REACT_MODULE_NAME;

    @SuppressWarnings("FieldCanBeLocal")
    private enum InBoxMessages {
        ALL(0),
        UNREAD(1);

        private final int value;

        InBoxMessages(final int newValue) {
            value = newValue;
        }

    }

    // The deep link from the push notification that launched the app.
    //
    // Why volatile? Two DIFFERENT threads touch this field: the host app WRITES it on
    // the main thread at launch (CleverTapRnAPI.setInitialUri in Activity.onCreate),
    // and JS READS it later from the bridge thread (getInitialUrl). Without volatile,
    // Java does not promise that the reading thread ever sees the writing thread's
    // value — it may keep seeing null. Example of the bug this prevents: the app is
    // opened from a push with deep link "myapp://offer/42"; the main thread stores it;
    // JS calls getInitialUrl() a moment later and still gets "InitialUrl is null", so
    // the app never opens the offer screen — no crash, no error, just a silently lost
    // deep link. volatile makes the write visible to every thread immediately.
    private static volatile Uri sLaunchUri;

    // Per-account variable registries: REAL account id -> (variable name -> Var).
    // Without the account level, two accounts defining the same variable name would
    // overwrite each other and reads/listeners would silently serve the wrong account.
    // ⚠️ Thread safety is mandatory: bridge methods run on the native-modules thread,
    // createInstance runs on the main thread, and the SDK fires variable callbacks on
    // its own threads — all touch this map. ConcurrentHashMap on BOTH levels; it
    // forbids null keys/values, so callers must null-guard what they put in.
    private static final Map<String, Map<String, Object>> accountVariables = new ConcurrentHashMap<>();

    public static void setInitialUri(final Uri uri) {
        sLaunchUri = uri;
    }

    private final ReactApplicationContext context;

    // The "default slot": the instance that unaddressed top-level CleverTap calls use.
    // null means "not resolved yet" -> falls back to the SDK default (manifest) instance.
    // setInstanceWithAccountId swaps this pointer (legacy behavior).
    //
    // Why volatile? This pointer can be touched from more than one thread over the
    // module's life: the constructor resolves it on whatever thread React Native
    // creates the module on, bridge methods read and swap it on the NativeModules
    // thread, and createInstance deliberately runs its work on the main thread.
    // Without volatile, a thread is allowed to keep seeing a STALE pointer after
    // another thread swapped it. Example of the bug this prevents: an app calls
    // setInstanceWithAccountId("B") and immediately records an event from a code
    // path on another thread — the stale read would silently send that event to the
    // OLD account. volatile costs nothing here (single reference read/write, no
    // lock, nothing the main thread can block on) and removes the whole question.
    private volatile CleverTapAPI mDefaultCleverTap;

    // Accounts whose listeners are already wired, so initCtInstance runs exactly once per
    // account. Thread-safe: touched from the native-modules thread AND the main thread
    // (createInstance runs on main — see the note inside it).
    private final Set<String> initedAccountIds = Collections.synchronizedSet(new HashSet<>());

    public CleverTapModuleImpl(ReactApplicationContext reactContext) {
        this.context = reactContext;
        enableEventEmitter(reactContext);
        getCleverTapAPI();
    }

    public Map<String, Object> getClevertapConstants() {
        Map<String, Object> constants = new HashMap<>();
        for (CleverTapEvent event : CleverTapEvent.values()) {
            constants.put(event.getEventName(), event.getEventName());
        }
        constants.put(FCM, FCM);
        return constants;
    }

    // Remembered from the JS import-time setLibrary call so that EVERY instance wired
    // later (createInstance, getInstance calls, a slot swap) reports the same wrapper
    // name and version — the stamping happens in initCtInstance, the one choke point
    // every instance passes through exactly once. Without this, secondary accounts
    // under-reported the wrapper version, and in an app with no manifest account the
    // version was lost entirely (there was no default instance to stamp at import
    // time). volatile: written on the bridge thread at JS import, read wherever an
    // instance is first wired (createInstance wires on the main thread).
    private volatile String customSdkName;
    private volatile int customSdkVersion;

    @SuppressLint("RestrictedApi")
    public void setLibrary(String libName, int libVersion) {
        customSdkName = libName;
        customSdkVersion = libVersion;
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.setCustomSdkVersion(libName, libVersion);
        }

    }

    public void setLocale(String locale, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.setLocale(locale);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void activate(String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }
        productConfigController.activate();
    }


    //Custom Push Notification
    public void createNotification(ReadableMap extras) {
        // No "default instance" guard on purpose. The ACCOUNT for this notification is
        // chosen by the native SDK from the payload itself — createNotification reads
        // wzrk_acct_id from the bundle and routes to THAT account's instance (rendering
        // and the Notification Viewed event land on the right account automatically).
        // The old guard only checked that the DEFAULT (manifest) account existed, which
        // silently broke this method for apps that create their instances from JS.
        // Example: an app with no manifest credentials receives a push for its JS-created
        // account 'B' and hands the payload here — with the guard this returned without
        // a trace; now the native SDK finds (or, on a cold process, restores) account B
        // and renders the notification.
        if (extras == null) {
            Log.w(TAG, "createNotification called with null extras — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotification");
        JSONObject extrasJsonObject;
        try {
            extrasJsonObject = jsonObjectFromReadableMap(extras);
            Bundle bundle = new Bundle();
            for (Iterator<String> entry = extrasJsonObject.keys(); entry.hasNext(); ) {
                String key = entry.next();
                String str = extrasJsonObject.optString(key);
                bundle.putString(key, str);
            }
            CleverTapAPI.createNotification(this.context, bundle);
        } catch (JSONException e) {
            Log.e(TAG, "createNotification failed to parse extras — notification not shown", e);
        }
    }

    // No "default instance" guard on the notification-channel methods below, on purpose.
    // Notification channels are an OS-level, app-wide resource — the native static picks
    // ANY available CleverTap instance itself (the default account, or the first created
    // one) and only uses it for a background executor and a logger; the real work is a
    // plain NotificationManager call.
    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannel(String channelId, String channelName, String channelDescription,
                                          int importance, boolean showBadge) {
        if (channelId == null || channelName == null || channelDescription == null) {
            Log.w(TAG, "createNotificationChannel called with null arguments — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotificationChannel");
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                showBadge);
        Log.i(TAG, "Notification Channel " + channelName + " creation requested");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelGroup(String groupId, String groupName) {
        if (groupId == null || groupName == null) {
            Log.w(TAG, "createNotificationChannelGroup called with null arguments — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotificationChannelGroup");
        CleverTapAPI.createNotificationChannelGroup(this.context, groupId, groupName);
        Log.i(TAG, "Notification Channel Group " + groupName + " creation requested");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithGroupId(String channelId, String channelName, String channelDescription,
                                                     int importance, String groupId, boolean showBadge) {
        if (channelId == null || channelName == null || channelDescription == null || groupId == null) {
            Log.w(TAG, "createNotificationChannelWithGroupId called with null arguments — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotificationChannelWithGroupId");
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                groupId, showBadge);
        Log.i(TAG, "Notification Channel " + channelName + " with Group Id " + groupId + " creation requested");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithGroupIdAndSound(String channelId, String channelName,
                                                             String channelDescription, int importance, String groupId, boolean showBadge, String sound) {
        if (channelId == null || channelName == null || channelDescription == null
                || groupId == null || sound == null) {
            Log.w(TAG, "createNotificationChannelWithGroupIdAndSound called with null arguments — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotificationChannelWithGroupIdAndSound");
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                groupId, showBadge, sound);
        Log.i(TAG, "Notification Channel " + channelName + " with Group Id " + groupId + " and sound file " + sound
                + " creation requested");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithSound(String channelId, String channelName, String channelDescription,
                                                   int importance, boolean showBadge, String sound) {
        if (channelId == null || channelName == null || channelDescription == null || sound == null) {
            Log.w(TAG, "createNotificationChannelWithSound called with null arguments — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("createNotificationChannelWithSound");
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                showBadge, sound);
        Log.i(TAG, "Notification Channel " + channelName + " with sound file " + sound + " creation requested");
    }


    @RequiresApi(api = VERSION_CODES.O)
    public void deleteNotificationChannel(String channelId) {
        if (channelId == null) {
            Log.w(TAG, "deleteNotificationChannel called with null channelId — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("deleteNotificationChannel");
        CleverTapAPI.deleteNotificationChannel(this.context, channelId);
        Log.i(TAG, "Notification Channel Id " + channelId + " deletion requested");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void deleteNotificationChannelGroup(String groupId) {
        if (groupId == null) {
            Log.w(TAG, "deleteNotificationChannelGroup called with null groupId — ignored");
            return;
        }
        warnIfNoInstanceExistsYet("deleteNotificationChannelGroup");
        CleverTapAPI.deleteNotificationChannelGroup(this.context, groupId);
        Log.i(TAG, "Notification Channel Group Id " + groupId + " deletion requested");
    }

    // Push permission methods. Routed by accountId like every other native INSTANCE
    // method: the OS permission itself is app-wide, but the prompt runs through the
    // resolved account's in-app machinery and — verified in the native SDK — the
    // permission RESPONSE is delivered only to the PROMPTING instance's listeners,
    // so the CleverTapPushPermissionResponseReceived event reaches the handle that
    // asked. Example: handleB.promptForPushPermission(true) → the user answers →
    // only handleB's listener fires (tagged ACCT_B).
    public void promptForPushPermission(boolean showFallbackSettings, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            // Must run on the main thread. startActivity() internally walks the outgoing
            // activity's view hierarchy (Activity.cancelInputsAndStartExitTransition),
            // which is main-thread-only. Calling it from the native modules thread races
            // against the main thread's view removals and can crash with an NPE inside
            // ViewGroup.dispatchCancelPendingInputEvents.
            UiThreadUtil.runOnUiThread(
                    () -> cleverTap.promptForPushPermission(showFallbackSettings));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void promptPushPrimer(ReadableMap localInAppConfig, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            JSONObject jsonObject = localInAppConfigFromReadableMap(localInAppConfig);
            // Main thread required, same reason as promptForPushPermission above. The
            // ReadableMap conversion stays on the calling thread; only the UI call hops.
            UiThreadUtil.runOnUiThread(() -> cleverTap.promptPushPrimer(jsonObject));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void isPushPermissionGranted(String accountId, final Callback callback) {
        final CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            boolean isPushPermissionGranted = clevertap.isPushPermissionGranted();
            callbackWithErrorAndResult(callback, null, isPushPermissionGranted);
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void disablePersonalization(String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.disablePersonalization();
    }

    public void enableDeviceNetworkInfoReporting(boolean value, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.enableDeviceNetworkInfoReporting(value);
    }

    public void enablePersonalization(String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.enablePersonalization();
    }


    public void getUserEventLog(String eventName, String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            UserEventLog eventLog = clevertap.getUserEventLog(eventName);
            result = eventLogToWritableMap(eventLog);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getUserEventLogCount(String eventName, String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getUserEventLogCount(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getUserLastVisitTs(String accountId, Callback callback) {
        String error = null;
        double result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getUserLastVisitTs();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getUserAppLaunchCount(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getUserAppLaunchCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getUserEventLogHistory(String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            Map<String, UserEventLog> history = clevertap.getUserEventLogHistory();
            result = eventLogHistoryToWritableMap(history);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @Deprecated(since = "3.2.0")
    public void eventGetDetail(String eventName, String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            EventDetail detail = clevertap.getDetails(eventName);
            result = eventDetailToWritableMap(detail);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }


    @Deprecated(since = "3.2.0")
    public void eventGetFirstTime(String eventName, String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getFirstTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @Deprecated(since = "3.2.0")
    public void eventGetLastTime(String eventName, String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getLastTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @Deprecated(since = "3.2.0")
    public void eventGetOccurrences(String eventName, String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getCount(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void fetch(String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetch();
    }

    public void fetchAndActivate(String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetchAndActivate();
    }

    public void fetchWithMinimumFetchIntervalInSeconds(int interval, String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetch(interval);
    }

    public void getAllDisplayUnits(String accountId, Callback callback) {
        String error = null;
        WritableArray result = Arguments.createArray();

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            result = getWritableArrayFromDisplayUnitList(cleverTap.getAllDisplayUnits());
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getBoolean(String key, String accountId, Callback callback) {
        String error = null;
        Boolean result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getBoolean(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED;
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getDisplayUnitForId(String unitID, String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CleverTapDisplayUnit displayUnit = cleverTap.getDisplayUnitForId(unitID);
            if (displayUnit != null && displayUnit.getJsonObject() != null) {
                result = convertObjectToWritableMap(displayUnit.getJsonObject());
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getDouble(String key, String accountId, Callback callback) {
        String error = null;
        Double result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getDouble(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED;
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @Deprecated(since = "3.2.0")
    public void getEventHistory(String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            Map<String, EventDetail> history = clevertap.getHistory();
            result = eventHistoryToWritableMap(history);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getFeatureFlag(String name, Boolean defaultValue, String accountId, Callback callback) {
        String error = null;
        Boolean result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTFeatureFlagsController featureFlagsController = cleverTap.featureFlag();
            if (featureFlagsController != null) {
                result = featureFlagsController.get(name, defaultValue);
            } else {
                error = ErrorMessages.FF_NOT_INITIALIZED;
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getAllInboxMessages(String accountId, Callback callback) {
        getInboxMessages(InBoxMessages.ALL, accountId, callback);
    }

    public void getInboxMessageCount(String accountId, Callback callback) {

        String error = null;
        int result = -1;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            result = cleverTap.getInboxMessageCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getInboxMessageForId(String messageId, String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTInboxMessage inboxMessage = cleverTap.getInboxMessageForId(messageId);

            if (inboxMessage != null && inboxMessage.getData() != null) {
                result = convertObjectToWritableMap(inboxMessage.getData());
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getInboxMessageUnreadCount(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            result = cleverTap.getInboxMessageUnreadCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void deleteInboxMessageForId(String messageId, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.deleteInboxMessage(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void getUnreadInboxMessages(String accountId, Callback callback) {
        getInboxMessages(InBoxMessages.UNREAD, accountId, callback);
    }


    //App Inbox methods
    public void initializeInbox(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.initializeInbox();
            Log.e(TAG, "initializeInbox Called");
        }
    }

    public void markReadInboxMessageForId(String messageId, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.markReadInboxMessage(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void markReadInboxMessagesForIDs(final ReadableArray messageIDs, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.markReadInboxMessagesForIDs(arrayListStringFromReadableArray(messageIDs));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void deleteInboxMessagesForIDs(final ReadableArray messageIDs, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.deleteInboxMessagesForIDs(arrayListStringFromReadableArray(messageIDs));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void pushInboxNotificationClickedEventForId(String messageId, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.pushInboxNotificationClickedEvent(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void pushInboxNotificationViewedEventForId(String messageId, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.pushInboxNotificationViewedEvent(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void fetchInbox(String accountId, final Callback callback) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap == null) {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
            Log.e(TAG, error);
            if (callback != null) {
                callbackWithErrorAndResult(callback, error, null);
            }
            return;
        }
        if (callback == null) {
            cleverTap.fetchInbox();
        } else {
            cleverTap.fetchInbox((FetchInboxCallback) success ->
                callbackWithErrorAndResult(callback, null, success));
        }
    }

    public void showInbox(ReadableMap styleConfig, String accountId) {
        CTInboxStyleConfig inboxStyleConfig = styleConfigFromReadableMap(styleConfig);
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.showAppInbox(inboxStyleConfig);
        }
    }

    public void dismissInbox(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.dismissAppInbox();
        }
    }

    public void getInitialUrl(Callback callback) {
        String error = null;
        String url = null;

        if (sLaunchUri == null) {
            error = "CleverTap InitialUrl is null";
        } else {
            url = sLaunchUri.toString();
        }
        callbackWithErrorAndResult(callback, error, url);
    }

    public void getLastFetchTimeStampInMillis(String accountId, Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = String.valueOf(productConfigController.getLastFetchTimeStampInMillis());
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED;
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getString(String key, String accountId, Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getString(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED;
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void onUserLogin(ReadableMap profile, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.onUserLogin(finalProfile);
    }

    public void profileAddMultiValue(String value, String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.addMultiValueForKey(key, value);
    }

    public void profileAddMultiValues(ReadableArray values, String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.addMultiValuesForKey(key, finalValues);
    }

    public void profileGetCleverTapAttributionIdentifier(String accountId, Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getCleverTapAttributionIdentifier();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void profileGetCleverTapID(String accountId, Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getCleverTapID();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getCleverTapID(String accountId, final Callback callback) {
        final CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            clevertap.getCleverTapID(new OnInitCleverTapIDListener() {
                @Override
                public void onInitCleverTapID(final String cleverTapID) {
                    // Callback on main thread
                    callbackWithErrorAndResult(callback, null, cleverTapID);
                }

            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void profileGetProperty(String propertyName, String accountId, Callback callback) {
        String error = null;
        Object result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            Object value = clevertap.getProperty(propertyName);
            // Handle JSONArray for multi-values, otherwise everything should be primitive or String
            if (value instanceof JSONArray) {
                JSONArray valueArray = (JSONArray) value;
                WritableArray writableArray = Arguments.createArray();
                for (int i = 0; i < valueArray.length(); i++) {
                    try {
                        writableArray.pushString(valueArray.get(i).toString());
                    } catch (JSONException e) {
                        //no-op
                    }
                }
                result = writableArray;
            } else {
                result = value;
            }

        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void profileRemoveMultiValue(String value, String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.removeMultiValueForKey(key, value);
    }

    public void profileRemoveMultiValues(ReadableArray values, String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.removeMultiValuesForKey(key, finalValues);
    }

    public void profileRemoveValueForKey(String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.removeValueForKey(key);
    }

    public void profileSet(ReadableMap profile, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.pushProfile(finalProfile);
    }

    public void profileSetMultiValues(ReadableArray values, String key, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.setMultiValuesForKey(key, finalValues);
    }

    public void pushDisplayUnitClickedEventForID(String unitID, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.pushDisplayUnitClickedEventForID(unitID);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void pushDisplayUnitViewedEventForID(String unitID, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.pushDisplayUnitViewedEventForID(unitID);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
        }
    }

    public void pushDisplayUnitElementClickedEventForID(String unitID, ReadableMap additionalProperties, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap == null) {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED);
            return;
        }
        HashMap<String, Object> props = additionalProperties != null
            ? eventPropsFromReadableMap(additionalProperties, Object.class)
            : new HashMap<>();
        cleverTap.pushDisplayUnitElementClickedEventForID(unitID, props);
    }

    public void pushInstallReferrer(String source, String medium, String campaign, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.pushInstallReferrer(source, medium, campaign);
    }

    public void recordChargedEvent(ReadableMap details, ReadableArray items, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null || details == null) {
            return;
        }

        HashMap<String, Object> finalDetails = eventPropsFromReadableMap(details, Object.class);

        ArrayList<HashMap<String, Object>> finalItems = new ArrayList<>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                try {
                    HashMap<String, Object> item = eventPropsFromReadableMap(items.getMap(i), Object.class);
                    finalItems.add(item);
                } catch (Throwable t) {
                    Log.e(TAG, t.getLocalizedMessage());
                }
            }
        }

        try {
            clevertap.pushChargedEvent(finalDetails, finalItems);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    public void recordEvent(String eventName, ReadableMap props, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }

        Map<String, Object> finalProps = eventPropsFromReadableMap(props, Object.class);

        if (finalProps == null) {
            clevertap.pushEvent(eventName);
        } else {
            clevertap.pushEvent(eventName, finalProps);
        }
    }

    public void recordScreenView(String screenName, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        try {
            clevertap.recordScreen(screenName);
        } catch (NullPointerException npe) {
            Log.e(TAG, "Something went wrong in native SDK!");
            npe.printStackTrace();
        }
    }

    public void registerForPush() {
        // no-op in Android
        Log.i(TAG, "CleverTap.registerForPush is a no-op in Android");
    }

    public void reset(String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        productConfigController.reset();
    }

    @Deprecated(since = "3.2.0")
    public void sessionGetPreviousVisitTime(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getPreviousVisitTime();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetScreenCount(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getScreenCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetTimeElapsed(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getTimeElapsed();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @Deprecated(since = "3.2.0")
    public void sessionGetTotalVisits(String accountId, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            result = clevertap.getTotalVisits();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetUTMDetails(String accountId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap != null) {
            UTMDetail details = clevertap.getUTMDetails();
            result = utmDetailsToWritableMap(details);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void setDebugLevel(int level) {
        CleverTapAPI.setDebugLevel(level);
    }

    public void setDefaultsMap(ReadableMap map, String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        HashMap<String, Object> finalMap = eventPropsFromReadableMap(map, Object.class);
        productConfigController.setDefaults(finalMap);
    }

    public void setLocation(double latitude, double longitude, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        final Location location = new Location("CleverTapReact");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        clevertap.setLocation(location);
    }

    public void setMinimumFetchIntervalInSeconds(int interval, String accountId) {
        CTProductConfigController productConfigController = getCtProductConfigController(accountId);
        if (productConfigController == null) {
            return;
        }

        productConfigController.setMinimumFetchIntervalInSeconds(interval);
    }

    //Sets the SDK to offline mode
    public void setOffline(boolean value, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }
        clevertap.setOffline(value);
    }

    public void setOptOut(boolean userOptOut, Boolean allowSystemEvents, String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return;
        }

        if (allowSystemEvents != null) {
            clevertap.setOptOut(userOptOut, allowSystemEvents);
        } else {
            clevertap.setOptOut(userOptOut);
        }
    }

    public void pushRegistrationToken(String token, ReadableMap type, String accountId) {
        Logger.v("pushRegistrationToken called with: token = [" + token + "], type = [" + type + "]");
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null || token == null || type == null) {
            return;
        }

        PushType pushType = pushTypeFromReadableMap(type);
        if (pushType != null) {
            clevertap.pushRegistrationToken(token, pushType, true);
        }
    }

    public void setFCMPushTokenAsString(String token, String accountId) {
        Logger.v("setFCMPushTokenAsString called with: token = [" + token + "]");
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null || token == null) {
            return;
        }
        clevertap.pushFcmRegistrationId(token, true);
    }

    // Increment/Decrement Operator
    public void profileIncrementValueForKey(Double value, String key, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.incrementValue(key, value);
        }
    }

    public void profileDecrementValueForKey(Double value, String key, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.decrementValue(key, value);
        }
    }

    // InApp Controls
    public void suspendInAppNotifications(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.suspendInAppNotifications();
        }
    }

    public void discardInAppNotifications(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.discardInAppNotifications();
        }
    }

    public void discardInAppNotifications(Boolean dismissInAppIfVisible, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            if (dismissInAppIfVisible != null) {
                cleverTap.discardInAppNotifications(dismissInAppIfVisible);
            } else {
                cleverTap.discardInAppNotifications();
            }
        }
    }

    public void resumeInAppNotifications(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.resumeInAppNotifications();
        }
    }

    public void dismissPipInApp(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.dismissPipInApp();
        }
    }

    public void unmute(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.unmute();
        }
    }

    public void customTemplateSetDismissed(String templateName, String accountId, Promise promise) {
        resolveWithTemplateContext(templateName, accountId, promise, templateContext -> {
            templateContext.setDismissed();
            return null;
        });
    }

    public void customTemplateSetPresented(String templateName, String accountId, Promise promise) {
        resolveWithTemplateContext(templateName, accountId, promise, templateContext -> {
            templateContext.setPresented();
            return null;
        });
    }

    public void customTemplateRunAction(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                customTemplateContext -> {
                    if (customTemplateContext instanceof CustomTemplateContext.TemplateContext) {
                        ((CustomTemplateContext.TemplateContext) customTemplateContext).triggerActionArgument(argName, null);
                    }
                    return null;
                }
        );
    }

    public void customTemplateGetStringArg(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> templateContext.getString(argName)
        );
    }

    public void customTemplateGetNumberArg(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> templateContext.getDouble(argName)
        );
    }

    public void customTemplateGetBooleanArg(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> templateContext.getBoolean(argName)
        );
    }

    public void customTemplateGetFileArg(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> templateContext.getFile(argName)
        );
    }

    public void customTemplateGetObjectArg(String templateName, String argName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> {
                    Map<String, Object> mapArg = templateContext.getMap(argName);
                    if (mapArg != null) {
                        return CleverTapUtils.MapUtil.toWritableMap(mapArg);
                    } else {
                        return null;
                    }
                }
        );
    }

    public void customTemplateContextToString(String templateName, String accountId, Promise promise) {
        resolveWithTemplateContext(
                templateName,
                accountId,
                promise,
                templateContext -> templateContext.toString()
        );
    }

    public void syncCustomTemplates(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.syncRegisteredInAppTemplates();
        }
    }

    public void variants(String accountId, final Callback callback) {
        WritableArray result = null;
        String error = null;
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            List<Map<String, Object>> variantsList = cleverTap.variants();
            result = variantsToWritableArray(variantsList);
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    // Active template contexts live PER INSTANCE in the native SDK — asking the wrong
    // account always answers "not currently being presented", so the account must be
    // resolved here, not hardcoded to the default slot.
    private void resolveWithTemplateContext(String templateName, String accountId, Promise promise,
            TemplateContextAction action) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            CustomTemplateContext templateContext = cleverTap.getActiveContextForTemplate(templateName);
            if (templateContext != null) {
                promise.resolve(action.execute(templateContext));
            } else {
                promise.reject("CustomTemplateError", "Custom template: " + templateName + " is not currently being presented");
            }
        } else {
            promise.reject("CustomTemplateError", "CleverTap is not initialized");
        }
    }

    @FunctionalInterface
    private interface TemplateContextAction {
        Object execute(CustomTemplateContext context);
    }

    /**************************************************
     *  Product Experience Remote Config methods starts
     *************************************************/
    public void syncVariables(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.syncVariables();
        }
    }

    public void syncVariablesinProd(boolean isProduction, String accountId) {
        Log.i(TAG, "CleverTap syncVariablesinProd is no-op in Android");
    }

    public void fetchVariables(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.fetchVariables();
        }
    }

    /**
     * Returns the variable registry belonging to the given instance's account,
     * creating it atomically on first use.
     */
    private Map<String, Object> variablesFor(CleverTapAPI cleverTap) {
        String accountKey = cleverTap.getAccountId();
        if (accountKey == null) {
            // ConcurrentHashMap forbids null keys; an instance without an account id
            // cannot own variables. Hand back an isolated map so callers safely no-op.
            Log.w(TAG, "Variables unavailable: instance has no accountId");
            return new ConcurrentHashMap<>();
        }
        return accountVariables.computeIfAbsent(accountKey, k -> new ConcurrentHashMap<>());
    }

    public void defineVariables(ReadableMap object, String accountId) {
        if (object == null) {
            Log.w(TAG, "defineVariables called with null variables object");
            return;
        }
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            Map<String, Object> accountVars = variablesFor(cleverTap);
            for (Map.Entry<String, Object> entry : object.toHashMap().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                Var<Object> variable = cleverTap.defineVariable(key, value);
                if (variable != null) {
                    accountVars.put(key, variable);
                } else {
                    // ConcurrentHashMap forbids null values; also nothing to read later.
                    Log.w(TAG, "defineVariable returned null for name " + key);
                }
            }
        }
    }

    public void defineFileVariable(String name, String accountId) {
        // ConcurrentHashMap throws NullPointerException on null KEYS (even for reads,
        // unlike HashMap) — reject null before it can reach the registry or the SDK.
        if (name == null) {
            Log.w(TAG, "defineFileVariable called with null name");
            return;
        }
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            Var<String> variable = cleverTap.defineFileVariable(name);
            if (variable != null) {
                variablesFor(cleverTap).put(name, variable);
            } else {
                Log.w(TAG, "defineFileVariable returned null for name " + name);
            }
        }
    }

    public void fetchVariables(String accountId, final Callback callback) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.fetchVariables(new FetchVariablesCallback() {
                @Override
                public void onVariablesFetched(final boolean isSuccess) {
                    callbackWithErrorAndResult(callback, null, isSuccess);
                }
            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void getVariable(String key, String accountId, final Callback callback) {
        String error = null;
        Object result = null;
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            try {
                result = getVariableValue(variablesFor(cleverTap), key);
            } catch (IllegalArgumentException e) {
                error = e.getLocalizedMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getVariables(String accountId, final Callback callback) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap == null) {
            callbackWithErrorAndResult(callback, ErrorMessages.CLEVERTAP_NOT_INITIALIZED, null);
            return;
        }
        callbackWithErrorAndResult(callback, null, getVariablesValues(variablesFor(cleverTap)));
    }

    public void onValueChanged(final String name, String accountId) {
        // Resolve first: the listener must attach to THIS account's variable (never a
        // same-named variable of another account) and the emitted event carries the
        // REAL account id of the instance the callback belongs to.
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return; // resolveInstance already warned
        }
        final String accountKey = clevertap.getAccountId();
        final Map<String, Object> accountVars = variablesFor(clevertap);
        // name null-guard first: ConcurrentHashMap.containsKey(null) throws NPE.
        if (name != null && accountVars.containsKey(name)) {

            Var<Object> var = (Var<Object>) accountVars.get(name);
            if (var != null) {
                var.addValueChangedCallback(new VariableCallback<Object>() {
                    @Override
                    public void onValueChanged(final Var<Object> variable) {
                        WritableMap result = null;
                        try {
                            result = getVariableValueAsWritableMap(accountVars, name);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                        sendEvent(CleverTapEvent.CLEVERTAP_ON_VALUE_CHANGED, result, accountKey);
                    }
                });
            } else {
                Log.d(TAG, "Variable value with name = " + name + " contains null value. Not setting onValueChanged callback.");
            }
        } else {
            Log.e(TAG, "Variable name = " + name + " does not exist. Make sure you set variable first.");
        }
    }

    public void onFileValueChanged(final String name, String accountId) {
        // Resolve first: the listener must attach to THIS account's file variable and
        // the emitted event carries the REAL account id of the owning instance.
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return; // resolveInstance already warned
        }
        final String accountKey = clevertap.getAccountId();
        final Map<String, Object> accountVars = variablesFor(clevertap);
        // name null-guard first: ConcurrentHashMap.containsKey(null) throws NPE.
        if (name != null && accountVars.containsKey(name)) {

            Var<Object> var = (Var<Object>) accountVars.get(name);
            if (var != null) {
                var.addFileReadyHandler(new VariableCallback<Object>() {
                    @Override
                    public void onValueChanged(final Var<Object> variable) {
                        WritableMap result = null;
                        try {
                            result = getVariableValueAsWritableMap(accountVars, name);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                        sendEvent(CleverTapEvent.CLEVERTAP_ON_FILE_VALUE_CHANGED, result, accountKey);
                    }
                });
            } else {
                Log.d(TAG, "File variable object with name = " + name + " contains null value. Not setting onFileValueChanged callback.");
            }
        } else {
            Log.e(TAG, "File variable name = " + name + " does not exist. Make sure you set file variable first.");
        }
    }

    public void onVariablesChanged(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            final String accountKey = cleverTap.getAccountId();
            final Map<String, Object> accountVars = variablesFor(cleverTap);
            cleverTap.addVariablesChangedCallback(new VariablesChangedCallback() {
                @Override
                public void variablesChanged() {
                    sendEvent(CleverTapEvent.CLEVERTAP_ON_VARIABLES_CHANGED, getVariablesValues(accountVars), accountKey);
                }
            });
        }
    }

    public void onOneTimeVariablesChanged(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            final String accountKey = cleverTap.getAccountId();
            final Map<String, Object> accountVars = variablesFor(cleverTap);
            cleverTap.addOneTimeVariablesChangedCallback(new VariablesChangedCallback() {
                @Override
                public void variablesChanged() {
                    sendEvent(CleverTapEvent.CLEVERTAP_ON_ONE_TIME_VARIABLES_CHANGED, getVariablesValues(accountVars), accountKey);
                }
            });
        }
    }

    public void onVariablesChangedAndNoDownloadsPending(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            final String accountKey = cleverTap.getAccountId();
            final Map<String, Object> accountVars = variablesFor(cleverTap);
            cleverTap.onVariablesChangedAndNoDownloadsPending(new VariablesChangedCallback() {
                @Override
                public void variablesChanged() {
                    sendEvent(CleverTapEvent.CLEVERTAP_ON_VARIABLES_CHANGED_AND_NO_DOWNLOADS_PENDING,
                            getVariablesValues(accountVars), accountKey);
                }
            });
        }
    }

    public void onceVariablesChangedAndNoDownloadsPending(String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            final String accountKey = cleverTap.getAccountId();
            final Map<String, Object> accountVars = variablesFor(cleverTap);
            cleverTap.onceVariablesChangedAndNoDownloadsPending(new VariablesChangedCallback() {
                @Override
                public void variablesChanged() {
                    sendEvent(CleverTapEvent.CLEVERTAP_ONCE_VARIABLES_CHANGED_AND_NO_DOWNLOADS_PENDING,
                            getVariablesValues(accountVars), accountKey);
                }
            });
        }
    }

    /************************************************
     *  Product Experience Remote Config methods ends
     ************************************************/

    public void clearInAppResources(final boolean expiredOnly, String accountId) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.clearInAppResources(expiredOnly);
        }
    }

    public void fetchInApps(String accountId, final Callback callback) {
        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {
            cleverTap.fetchInApps(new FetchInAppsCallback() {
                @Override
                public void onInAppsFetched(final boolean isSuccess) {
                    callbackWithErrorAndResult(callback, null, isSuccess);
                }
            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED;
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void onEventListenerAdded(String eventName, String accountId) {
        CleverTapEvent event = CleverTapEvent.fromName(eventName);
        if (event == null) {
            Log.e(TAG, "Event listener added for unsupported event " + eventName);
            return;
        }
        // Arm the buffer for ONLY this listener's account (null = the default slot, resolved
        // here) and flush that account's buffered events. Other accounts' buffered events stay
        // buffered until their own listeners attach — flushing everything here would silently
        // drop them, because their listeners are not attached yet to receive the delivery.
        CleverTapAPI instance = resolveInstance(accountId);
        String accountKey = instance != null ? instance.getAccountId() : null;
        Log.i(TAG, "onEventListenerAdded: " + eventName + " accountId=" + accountId
                + " resolved account=" + accountKey);
        CleverTapEventEmitter.INSTANCE.armAccount(event, accountKey);
        CleverTapEventEmitter.INSTANCE.flushBuffer(event, accountKey);
    }

    private void enableEventEmitter(ReactContext reactContext) {
        CleverTapEventEmitter.INSTANCE.setReactContext(reactContext);
        // disable buffers after a delay in order to give some time for event listeners to attach
        // and receive initially buffered events. After that all buffers will be cleared and disabled
        // and events will continue to be sent immediately.
        new Handler().postDelayed(() -> CleverTapEventEmitter.INSTANCE.resetAllBuffers(false), 5000);
    }

    /**
     * result must be primitive, String or com.facebook.react.bridge.WritableArray/WritableMap
     * see https://github.com/facebook/react-native/issues/3101#issuecomment-143954448
     */
    private void callbackWithErrorAndResult(Callback callback, String error, Object result) {
        if (callback == null) {
            Log.i(TAG, "CleverTap callback is null");
            return;
        }
        try {
            callback.invoke(error, result);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    private Object getVariableValue(Map<String, Object> accountVars, String name) {
        // null-guard first: ConcurrentHashMap.containsKey(null) throws NPE (HashMap
        // returned false). A null name must take the graceful "does not exist" path.
        if (name != null && accountVars.containsKey(name)) {
            Var<?> variable = (Var<?>) accountVars.get(name);
            Object variableValue = variable.value();
            Object value;
            switch (variable.kind()) {
                case CTVariableUtils.DICTIONARY:
                    value = CleverTapUtils.MapUtil.toWritableMap((Map<String, Object>) variableValue);
                    break;
                default:
                    value = variableValue;
            }
            return value;
        }
        throw new IllegalArgumentException(
                "Variable name = " + name + " does not exist. Make sure you set variable first.");
    }

    private WritableMap getVariableValueAsWritableMap(Map<String, Object> accountVars, String name) {
        // Same null-guard rule as getVariableValue (ConcurrentHashMap NPEs on null keys).
        if (name != null && accountVars.containsKey(name)) {
            Var<?> variable = (Var<?>) accountVars.get(name);
            Object variableValue = variable.value();
            return CleverTapUtils.MapUtil.addValue(name, variable.value());
        }
        throw new IllegalArgumentException(
                "Variable name = " + name + " does not exist.");
    }

    private WritableMap getVariablesValues(Map<String, Object> accountVars) {
        WritableMap writableMap = Arguments.createMap();
        for (Map.Entry<String, Object> entry : accountVars.entrySet()) {
            String key = entry.getKey();
            Var<?> variable = (Var<?>) entry.getValue();

            WritableMap variableWritableMap = CleverTapUtils.MapUtil.addValue(key, variable.value());
            writableMap.merge(variableWritableMap);
        }
        return writableMap;
    }

    private boolean checkKitkatVersion(String methodName) {
        if (VERSION.SDK_INT < VERSION_CODES.KITKAT) {
            Log.e(TAG, "Call requires API level 19 (current min is " + VERSION.SDK_INT + "):" + methodName);
            return false;
        }
        return true;
    }

    private PushType pushTypeFromReadableMap(ReadableMap readableMap) {
        String type = readableMap.getString("type");
        String prefKey =  readableMap.getString("prefKey");

        if (type == null || prefKey == null) {
            return null;
        }

        return new PushType(
                type,
                prefKey,
                readableMap.getString("className"),
                readableMap.getString("messagingSDKClassName"));
    }

    @SuppressWarnings("SameParameterValue")
    private <T> HashMap<String, T> eventPropsFromReadableMap(ReadableMap propsMap, Class<T> tClass) {
        if (propsMap == null) {
            return null;
        }

        HashMap<String, T> props = new HashMap<>();

        ReadableMapKeySetIterator iterator = propsMap.keySetIterator();

        while (iterator.hasNextKey()) {
            try {
                String key = iterator.nextKey();
                ReadableType readableType = propsMap.getType(key);

                if (readableType == ReadableType.String) {
                    props.put(key, tClass.cast(propsMap.getString(key)));
                } else if (readableType == ReadableType.Boolean) {
                    props.put(key, tClass.cast(propsMap.getBoolean(key)));
                } else if (readableType == ReadableType.Number) {
                    try {
                        props.put(key, tClass.cast(propsMap.getDouble(key)));
                    } catch (Throwable t) {
                        try {
                            props.put(key, tClass.cast(propsMap.getInt(key)));
                        } catch (Throwable t1) {
                            Log.e(TAG, "Unhandled ReadableType.Number from ReadableMap");
                        }
                    }
                } else if (readableType == ReadableType.Map) {
                    try {
                        ReadableMap nestedMap = propsMap.getMap(key);
                        if (nestedMap != null) {
                            props.put(key, tClass.cast(CleverTapUtils.MapUtil.toMap(nestedMap)));
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Map from ReadableMap");
                    }
                } else if (readableType == ReadableType.Array) {
                    try {
                        ReadableArray nestedArray = propsMap.getArray(key);
                        if (nestedArray != null) {
                            props.put(key, tClass.cast(CleverTapUtils.MapUtil.ArrayUtil.toArray(nestedArray)));
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Array from ReadableMap");
                    }
                } else {
                    Log.e(TAG, "Unhandled event property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return props;
    }

    @SuppressLint("RestrictedApi")
    private void initCtInstance(CleverTapAPI clevertap) {
        clevertap.setLibrary("React-Native");
        // Stamp the wrapper version remembered from the import-time setLibrary call
        // (see customSdkName above) — every account's analytics report it, not just
        // the default's. JS calls setLibrary at module import, before any account can
        // be wired, so the fields are always populated by the time we get here.
        if (customSdkName != null) {
            clevertap.setCustomSdkVersion(customSdkName, customSdkVersion);
        }
        // One proxy per account; the proxy registry keeps the strong references (see the
        // LOAD-BEARING note in CleverTapListenerProxy).
        CleverTapListenerProxy.attachToInstance(clevertap);
    }

    /**
     * Resolves the CleverTap instance for the given account id.
     *
     * accountId == null -> the DEFAULT SLOT (today's behavior, unchanged).
     * accountId != null -> the instance for that account, or null if it does not exist.
     *
     * Example: resolveInstance(null) returns the manifest account; after
     * setInstanceWithAccountId("B") it returns account B. resolveInstance("C")
     * returns account C if it was created (in this run, or restored by the native
     * SDK from a previous run) — otherwise it logs ONE warning and returns null.
     */
    @Nullable
    private CleverTapAPI resolveInstance(@Nullable String accountId) {
        CleverTapAPI instance;
        if (accountId == null) {
            if (mDefaultCleverTap == null) {
                mDefaultCleverTap = CleverTapAPI.getDefaultInstance(this.context);
            }
            instance = mDefaultCleverTap;
        } else {
            instance = CleverTapAPI.getGlobalInstance(this.context, accountId);
        }

        if (instance == null) {
            // The ONE warning that covers every bridge method. Method bodies just
            // null-check and return — do not add per-method warnings, and do not
            // remove this one: without it a typo'd accountId silently drops every call.
            if (accountId == null) {
                Log.w(TAG, "CleverTap default instance is not available — call ignored");
            } else {
                Log.w(TAG, "CleverTap instance not found for accountId: " + accountId + " — call ignored");
            }
            return null;
        }

        String key = instance.getAccountId();
        if (key != null && initedAccountIds.add(key)) {
            initCtInstance(instance); // wires listeners exactly once per account (Point 4)
        }
        return instance;
    }

    private CleverTapAPI getCleverTapAPI() {
        return resolveInstance(null);
    }

    /**
     * The native channel/notification statics need at least ONE CleverTap instance to
     * exist (any account — they only borrow its executor and logger). Their own
     * "no instance found" log is verbose-gated and invisible at the default log level,
     * so if nothing was ever created we would drop the call with no trace. This check
     * uses only this module's own state — no CleverTap core internals — which is why
     * the message says "will drop" conditionally
     */
    private void warnIfNoInstanceExistsYet(String methodName) {
        if (mDefaultCleverTap == null && initedAccountIds.isEmpty()) {
            Log.w(TAG, methodName + ": no CleverTap instance exists yet in this app run — "
                    + "the native SDK will drop this call. Call createInstance(config) first.");
        }
    }

    public void setInstanceWithAccountId(String accountId) {
        if (mDefaultCleverTap == null || !accountId.equals(mDefaultCleverTap.getAccountId())) {
            CleverTapAPI cleverTap = CleverTapAPI.getGlobalInstance(this.context, accountId);
            if (cleverTap != null) {
                mDefaultCleverTap = cleverTap; // swap the default slot (legacy behavior)
                resolveInstance(accountId);    // ensure listeners are wired exactly once
                Log.i(TAG, "CleverTap instance changed for accountId " + accountId);
            }
        }
    }

    /**
     * Creates an additional CleverTap account from JavaScript.
     *
     * Idempotent: calling it again for an existing accountId resolves with that
     * account and ignores the new config (a warning is logged).
     *
     * Example: createInstance({accountId: 'ACCT_B', accountToken: 'TOK_B', region: 'eu1'})
     * resolves with {accountId: 'ACCT_B'} and resolveInstance("ACCT_B") starts working.
     */
    public void createInstance(ReadableMap config, Promise promise) {
        String accountId = config != null ? config.getString("accountId") : null;
        String accountToken = config != null ? config.getString("accountToken") : null;
        // Reject EMPTY as well as missing: the native SDK only null-checks, so an
        // empty string would create a "zombie" instance whose events go nowhere
        // while every call looks successful.
        if (accountId == null || accountId.trim().isEmpty()
                || accountToken == null || accountToken.trim().isEmpty()) {
            promise.reject("EINVALID", "createInstance requires non-empty accountId and accountToken");
            return;
        }

        // ⚠️ The creation MUST run on the MAIN thread. The native SDK's DeviceInfo posts a
        // deviceIDCreated callback to the main thread that RE-ENTERS instanceWithConfig
        // (DeviceInfo.java, "callback on main thread"). instanceWithConfig's get→new→put on
        // the static instances map is not synchronized, so creating from another thread can
        // race that callback: TWO CleverTapAPI objects get built for the same account, the
        // map keeps the callback's copy, and our listeners end up attached to an orphan
        // (observed on device: "CleverTap SDK initialized" logged twice, different objects).
        // Running here on main serializes us with that callback: when it re-enters, the map
        // already holds our instance and it is returned instead of constructed again.
        final ReadableMap finalConfig = config;
        com.facebook.react.bridge.UiThreadUtil.runOnUiThread(() -> {

            String region = finalConfig.hasKey("region") ? finalConfig.getString("region") : null;
            CleverTapInstanceConfig ctConfig = (region != null && !region.trim().isEmpty())
                    ? CleverTapInstanceConfig.createInstance(this.context, accountId, accountToken, region)
                    : CleverTapInstanceConfig.createInstance(this.context, accountId, accountToken);
            if (ctConfig == null) {
                promise.reject("ECREATE", "createInstance could not build a config for accountId " + accountId);
                return;
            }
            applyOptionalConfig(ctConfig, finalConfig);

            // A custom CleverTap ID can only be supplied AT CREATION on both platforms.
            // Without this, useCustomCleverTapId=true would create an instance that
            // waits for an ID nobody can ever provide (error device id).
            String cleverTapId = finalConfig.hasKey("cleverTapId")
                    ? finalConfig.getString("cleverTapId") : null;
            // Instance creation can THROW, not just return null: registered custom
            // template producers run inside it, and e.g. duplicate template names
            // raise CustomTemplateException. We are on the MAIN thread here — an
            // uncaught throw would crash the app instead of rejecting the promise.
            CleverTapAPI instance;
            try {
                instance = (cleverTapId != null && !cleverTapId.trim().isEmpty())
                        ? CleverTapAPI.instanceWithConfig(this.context, ctConfig, cleverTapId)
                        : CleverTapAPI.instanceWithConfig(this.context, ctConfig);
            } catch (Throwable t) {
                promise.reject("ECREATE", "createInstance failed for accountId " + accountId, t);
                return;
            }
            if (instance == null) {
                promise.reject("ECREATE", "createInstance failed for accountId " + accountId);
                return;
            }
            resolveInstance(accountId); // wires listeners + setLibrary via initCtInstance
            promise.resolve(accountIdResult(accountId));
        });
    }

    /**
     * Resolves the account id the default slot currently points to (or null when
     * no default account exists). JS uses this once to route the top-level
     * CleverTap object's events; see the multi-instance design docs (point 5).
     */
    public void getDefaultAccountId(Promise promise) {
        CleverTapAPI defaultInstance = resolveInstance(null);
        String accountId = defaultInstance != null ? defaultInstance.getAccountId() : null;
        Log.i(TAG, "getDefaultAccountId -> " + accountId);
        promise.resolve(accountId);
    }

    private WritableMap accountIdResult(String accountId) {
        WritableMap result = Arguments.createMap();
        result.putString("accountId", accountId);
        return result;
    }

    // Android applies BOTH region and proxy settings when given together; iOS can
    // only honor region and warns that proxy was ignored (documented platform
    // difference — the iOS config's region/proxy fields are constructor-only).
    private void applyOptionalConfig(CleverTapInstanceConfig ctConfig, ReadableMap config) {
        if (config.hasKey("proxyDomain")) {
            ctConfig.setProxyDomain(config.getString("proxyDomain"));
        }
        if (config.hasKey("spikyProxyDomain")) {
            ctConfig.setSpikyProxyDomain(config.getString("spikyProxyDomain"));
        }
        if (config.hasKey("identityKeys")) {
            ReadableArray keys = config.getArray("identityKeys");
            if (keys != null && keys.size() > 0) {
                String[] identityKeys = new String[keys.size()];
                for (int i = 0; i < keys.size(); i++) {
                    identityKeys[i] = keys.getString(i);
                }
                ctConfig.setIdentityKeys(identityKeys);
            }
        }
        if (config.hasKey("handshakeDomain")) {
            ctConfig.setCustomHandshakeDomain(config.getString("handshakeDomain"));
        }
        if (config.hasKey("logLevel")) {
            ctConfig.setDebugLevel(toLogLevel(config.getString("logLevel")));
        }
        if (config.hasKey("analyticsOnly")) {
            ctConfig.setAnalyticsOnly(config.getBoolean("analyticsOnly"));
        }
        if (config.hasKey("enablePersonalization")) {
            ctConfig.enablePersonalization(config.getBoolean("enablePersonalization"));
        }
        if (config.hasKey("disableAppLaunchedEvent")) {
            ctConfig.setDisableAppLaunchedEvent(config.getBoolean("disableAppLaunchedEvent"));
        }
        if (config.hasKey("encryptionLevel")) {
            ctConfig.setEncryptionLevel(toEncryptionLevel(config.getString("encryptionLevel")));
        }
        if (config.hasKey("encryptionInTransit")) {
            ctConfig.setEncryptionInTransit(config.getBoolean("encryptionInTransit"));
        }
        if (config.hasKey("useCustomCleverTapId")) {
            ctConfig.setEnableCustomCleverTapId(config.getBoolean("useCustomCleverTapId"));
        }
        applyAndroidOnlyConfig(ctConfig, config.hasKey("android") ? config.getMap("android") : null);
        // The "ios" block is intentionally ignored here — each platform reads only
        // its own nested block, so platform-targeted config needs no warnings.
    }

    private void applyAndroidOnlyConfig(CleverTapInstanceConfig ctConfig, ReadableMap androidConfig) {
        if (androidConfig == null) {
            return;
        }
        if (androidConfig.hasKey("useGoogleAdId")) {
            ctConfig.useGoogleAdId(androidConfig.getBoolean("useGoogleAdId"));
        }
        if (androidConfig.hasKey("backgroundSync")) {
            ctConfig.setBackgroundSync(androidConfig.getBoolean("backgroundSync"));
        }
        if (androidConfig.hasKey("pushProviders")) {
            ReadableArray providers = androidConfig.getArray("pushProviders");
            if (providers != null) {
                for (int i = 0; i < providers.size(); i++) {
                    ReadableMap provider = providers.getMap(i);
                    if (provider == null) {
                        continue;
                    }
                    String type = provider.getString("type");
                    String prefKey = provider.getString("prefKey");
                    String className = provider.getString("className");
                    String messagingSDKClassName = provider.getString("messagingSDKClassName");
                    // All four parts are required by the native PushType contract.
                    if (type == null || prefKey == null || className == null
                            || messagingSDKClassName == null) {
                        Log.w(TAG, "createInstance: pushProviders[" + i
                                + "] is missing one of type/prefKey/className/messagingSDKClassName; skipped");
                        continue;
                    }
                    ctConfig.addPushType(new PushType(type, prefKey, className, messagingSDKClassName));
                }
            }
        }
    }

    // 'none' -> NONE(0), 'medium' -> MEDIUM(1, PII only), 'high' -> FULL_DATA(2, all data).
    // (iOS maps the same strings to CleverTapEncryptionNone/Medium/High.)
    private EncryptionLevel toEncryptionLevel(String level) {
        if ("medium".equals(level)) {
            return EncryptionLevel.MEDIUM;
        }
        if ("high".equals(level)) {
            return EncryptionLevel.FULL_DATA;
        }
        return EncryptionLevel.NONE;
    }

    // 'off' -> OFF(-1), 'info' -> INFO(0), 'debug' -> DEBUG(2), 'verbose' -> VERBOSE(3).
    // (iOS has no verbose level and maps 'verbose' to its debug level.)
    private CleverTapAPI.LogLevel toLogLevel(String level) {
        if ("off".equals(level)) {
            return CleverTapAPI.LogLevel.OFF;
        }
        if ("debug".equals(level)) {
            return CleverTapAPI.LogLevel.DEBUG;
        }
        if ("verbose".equals(level)) {
            return CleverTapAPI.LogLevel.VERBOSE;
        }
        return CleverTapAPI.LogLevel.INFO;
    }

    private CTProductConfigController getCtProductConfigController(String accountId) {
        CleverTapAPI clevertap = resolveInstance(accountId);
        if (clevertap == null) {
            return null;
        }

        return clevertap.productConfig();
    }

    private void getInboxMessages(InBoxMessages type, String accountId, Callback callback) {
        String error = null;
        ArrayList<CTInboxMessage> inboxMessages = new ArrayList<>();
        WritableArray result = Arguments.createArray();

        CleverTapAPI cleverTap = resolveInstance(accountId);
        if (cleverTap != null) {

            if (type == InBoxMessages.ALL) {
                inboxMessages = cleverTap.getAllInboxMessages();
            } else if (type == InBoxMessages.UNREAD) {
                inboxMessages = cleverTap.getUnreadInboxMessages();
            }

            for (CTInboxMessage message : inboxMessages) {
                if (message != null && message.getData() != null) {
                    result.pushMap(convertObjectToWritableMap(message.getData()));
                }
            }
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    private HashMap<String, Object> profileFromReadableMap(ReadableMap profileMap) {
        if (profileMap == null) {
            return null;
        }

        HashMap<String, Object> profile = new HashMap<>();

        ReadableMapKeySetIterator iterator = profileMap.keySetIterator();

        while (iterator.hasNextKey()) {
            try {
                String key = iterator.nextKey();
                ReadableType readableType = profileMap.getType(key);

                if ("DOB".equals(key) && (readableType == ReadableType.String) && !profileMap.getString(key).startsWith("$D_")) {
                    String dob = profileMap.getString(key);
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    try {
                        Date date = format.parse(dob);
                        profile.put(key, date);
                    } catch (Throwable t) {
                        Log.e(TAG, t.getLocalizedMessage());
                    }
                } else if (readableType == ReadableType.String) {
                    profile.put(key, profileMap.getString(key));
                } else if (readableType == ReadableType.Boolean) {
                    profile.put(key, profileMap.getBoolean(key));
                } else if (readableType == ReadableType.Number) {
                    try {
                        profile.put(key, profileMap.getDouble(key));
                    } catch (Throwable t) {
                        try {
                            profile.put(key, profileMap.getInt(key));
                        } catch (Throwable t1) {
                            Log.e(TAG, "Unhandled ReadableType.Number from ReadableMap");
                        }
                    }
                } else if (readableType == ReadableType.Array) {
                    try {
                        profile.put(key, arrayListStringFromReadableArray(profileMap.getArray(key)));
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Array from ReadableMap");
                    }
                } else if (readableType == ReadableType.Map) {
                    try {
                        ReadableMap nestedMap = profileMap.getMap(key);
                        if (nestedMap != null) {
                            profile.put(key, CleverTapUtils.MapUtil.toMap(nestedMap));
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Map from ReadableMap");
                    }
                } else {
                    Log.e(TAG, "Unhandled profile property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return profile;
    }

    /**
     * Emits an event to JS, stamped with the account it belongs to.
     *
     * Example: sendEvent(CLEVERTAP_ON_VARIABLES_CHANGED, values, "ACCT_B") adds
     * {"__ctAccountId": "ACCT_B"} to the payload, so the JS side delivers the event
     * to account B's listeners only. With accountKey == null the payload stays
     * unstamped and the JS side treats the event as global (legacy behavior).
     */
    private void sendEvent(@NonNull CleverTapEvent eventName, @Nullable Object params,
            @Nullable String accountKey) {
        Object payload = params;
        if (accountKey != null) {
            // Keep the payload's fields when it already is a map; otherwise build a
            // fresh map so there is something to stamp the account id on.
            WritableMap map = params instanceof WritableMap ? (WritableMap) params : Arguments.createMap();
            map.putString(Constants.CT_ACCOUNT_ID_KEY, accountKey);
            payload = map;
        }
        CleverTapEventEmitter.INSTANCE.emit(eventName, payload);
    }

    /**
     * retrieves the localInAppConfig from the given ReadableMap.
     *
     * @param readableMap - the map config, received from the host application
     * @return the Json of the localInAppConfig
     */
    private JSONObject localInAppConfigFromReadableMap(ReadableMap readableMap) {
        if (readableMap == null) {
            return null;
        }
        CTLocalInApp.InAppType inAppType = null;
        String titleText = null, messageText = null, positiveBtnText = null, negativeBtnText = null,
                backgroundColor = null, btnBorderColor = null, titleTextColor = null, messageTextColor = null,
                btnTextColor = null, imageUrl = null, btnBackgroundColor = null, btnBorderRadius = null, altText = null;
        boolean fallbackToSettings = false, followDeviceOrientation = false;

        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            try {
                String configKey = iterator.nextKey();
                ReadableType readableType = readableMap.getType(configKey);
                if ("inAppType".equals(configKey) && readableType == ReadableType.String) {
                    inAppType = inAppTypeFromString(readableMap.getString(configKey));
                }
                if ("titleText".equals(configKey) && readableType == ReadableType.String) {
                    titleText = readableMap.getString(configKey);
                }
                if ("messageText".equals(configKey) && readableType == ReadableType.String) {
                    messageText = readableMap.getString(configKey);
                }
                if ("followDeviceOrientation".equals(configKey) && readableType == ReadableType.Boolean) {
                    followDeviceOrientation = readableMap.getBoolean(configKey);
                }
                if ("positiveBtnText".equals(configKey) && readableType == ReadableType.String) {
                    positiveBtnText = readableMap.getString(configKey);
                }
                if ("negativeBtnText".equals(configKey) && readableType == ReadableType.String) {
                    negativeBtnText = readableMap.getString(configKey);
                }
                if ("fallbackToSettings".equals(configKey) && readableType == ReadableType.Boolean) {
                    fallbackToSettings = readableMap.getBoolean(configKey);
                }
                if ("backgroundColor".equals(configKey) && readableType == ReadableType.String) {
                    backgroundColor = readableMap.getString(configKey);
                }
                if ("btnBorderColor".equals(configKey) && readableType == ReadableType.String) {
                    btnBorderColor = readableMap.getString(configKey);
                }
                if ("titleTextColor".equals(configKey) && readableType == ReadableType.String) {
                    titleTextColor = readableMap.getString(configKey);
                }
                if ("messageTextColor".equals(configKey) && readableType == ReadableType.String) {
                    messageTextColor = readableMap.getString(configKey);
                }
                if ("btnTextColor".equals(configKey) && readableType == ReadableType.String) {
                    btnTextColor = readableMap.getString(configKey);
                }
                if ("imageUrl".equals(configKey) && readableType == ReadableType.String) {
                    imageUrl = readableMap.getString(configKey);
                }
                if ("altText".equals(configKey) && readableType == ReadableType.String) {
                    altText = readableMap.getString(configKey);
                }
                if ("btnBackgroundColor".equals(configKey) && readableType == ReadableType.String) {
                    btnBackgroundColor = readableMap.getString(configKey);
                }
                if ("btnBorderRadius".equals(configKey) && readableType == ReadableType.String) {
                    btnBorderRadius = readableMap.getString(configKey);
                }
            } catch (Throwable t) {
                Log.e(TAG, "invalid parameters in push primer config" + t.getLocalizedMessage());
                return null;
            }
        }

        //creates the builder instance of localInApp with all the required parameters
        CTLocalInApp.Builder.Builder6 builderWithRequiredParams = getLocalInAppBuilderWithRequiredParam(
                inAppType, titleText, messageText, followDeviceOrientation, positiveBtnText, negativeBtnText
        );

        //adds the optional parameters to the builder instance
        if (backgroundColor != null) {
            builderWithRequiredParams.setBackgroundColor(backgroundColor);
        }
        if (btnBorderColor != null) {
            builderWithRequiredParams.setBtnBorderColor(btnBorderColor);
        }
        if (titleTextColor != null) {
            builderWithRequiredParams.setTitleTextColor(titleTextColor);
        }
        if (messageTextColor != null) {
            builderWithRequiredParams.setMessageTextColor(messageTextColor);
        }
        if (btnTextColor != null) {
            builderWithRequiredParams.setBtnTextColor(btnTextColor);
        }
        if (imageUrl != null) {
            builderWithRequiredParams.setImageUrl(imageUrl, altText);
        }
        if (btnBackgroundColor != null) {
            builderWithRequiredParams.setBtnBackgroundColor(btnBackgroundColor);
        }
        if (btnBorderRadius != null) {
            builderWithRequiredParams.setBtnBorderRadius(btnBorderRadius);
        }
        builderWithRequiredParams.setFallbackToSettings(fallbackToSettings);

        JSONObject localInAppConfig = builderWithRequiredParams.build();
        Log.i(TAG, "LocalInAppConfig for push primer prompt: " + localInAppConfig);
        return localInAppConfig;
    }

    /**
     * Creates an instance of the {@link CTLocalInApp.Builder.Builder6} with the required parameters.
     *
     * @return the {@link CTLocalInApp.Builder.Builder6} instance
     */
    private CTLocalInApp.Builder.Builder6 getLocalInAppBuilderWithRequiredParam(CTLocalInApp.InAppType inAppType,
                                                                                String titleText,
                                                                                String messageText,
                                                                                boolean followDeviceOrientation,
                                                                                String positiveBtnText,
                                                                                String negativeBtnText) {
        //throws exception if any of the required parameter is missing
        if (inAppType == null || titleText == null || messageText == null || positiveBtnText == null
                || negativeBtnText == null) {
            throw new IllegalArgumentException("mandatory parameters are missing in push primer config");
        }

        CTLocalInApp.Builder builder = CTLocalInApp.builder();
        return builder.setInAppType(inAppType)
                .setTitleText(titleText)
                .setMessageText(messageText)
                .followDeviceOrientation(followDeviceOrientation)
                .setPositiveBtnText(positiveBtnText)
                .setNegativeBtnText(negativeBtnText);
    }

    //returns InAppType type from the given string
    private CTLocalInApp.InAppType inAppTypeFromString(String inAppType) {
        if (inAppType == null) {
            return null;
        }
        switch (inAppType) {
            case "half-interstitial":
                return CTLocalInApp.InAppType.HALF_INTERSTITIAL;
            case "alert":
                return CTLocalInApp.InAppType.ALERT;
            default:
                return null;
        }
    }

    private CTInboxStyleConfig styleConfigFromReadableMap(ReadableMap styleConfig) {
        if (styleConfig == null) {
            return new CTInboxStyleConfig();
        }

        CTInboxStyleConfig ctInboxStyleConfig = new CTInboxStyleConfig();
        ReadableMapKeySetIterator iterator = styleConfig.keySetIterator();
        while (iterator.hasNextKey()) {
            try {
                String styleConfigKey = iterator.nextKey();
                ReadableType readableType = styleConfig.getType(styleConfigKey);
                if ("navBarTitle".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String navBarTitle = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarTitle(navBarTitle);
                }
                if ("navBarTitleColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String navBarTitleColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarTitleColor(navBarTitleColor);
                }
                if ("navBarColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String navBarColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarColor(navBarColor);
                }
                if ("inboxBackgroundColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String inboxBackgroundColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setInboxBackgroundColor(inboxBackgroundColor);
                }
                if ("backButtonColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String backButtonColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setBackButtonColor(backButtonColor);
                }
                if ("unselectedTabColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String unselectedTabColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setUnselectedTabColor(unselectedTabColor);
                }
                if ("selectedTabColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String selectedTabColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setSelectedTabColor(selectedTabColor);
                }
                if ("selectedTabIndicatorColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String selectedTabIndicatorColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setSelectedTabIndicatorColor(selectedTabIndicatorColor);
                }
                if ("tabBackgroundColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String tabBackgroundColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setTabBackgroundColor(tabBackgroundColor);
                }
                if ("tabs".equals(styleConfigKey) && readableType == ReadableType.Array) {
                    try {
                        ArrayList<String> tabsList = arrayListStringFromReadableArray(
                                styleConfig.getArray(styleConfigKey));
                        ctInboxStyleConfig.setTabs(tabsList);
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Array from ReadableMap");
                    }
                }

                if ("noMessageText".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String noMessageTitle = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNoMessageViewText(noMessageTitle);
                }
                if ("noMessageTextColor".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String noMessageTitleColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNoMessageViewTextColor(noMessageTitleColor);
                }
                if ("firstTabTitle".equals(styleConfigKey) && readableType == ReadableType.String) {
                    String firstTabTitle = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setFirstTabTitle(firstTabTitle);
                }

            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
                return new CTInboxStyleConfig();
            }
        }
        return ctInboxStyleConfig;
    }

    private static ArrayList<String> arrayListStringFromReadableArray(ReadableArray readableArray) {
        ArrayList<String> array = new ArrayList<>();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.add(String.valueOf(readableArray.getBoolean(i)));
                    break;
                case Number:
                    array.add(String.valueOf(readableArray.getDouble(i)));
                    break;
                case String:
                    array.add(readableArray.getString(i));
                    break;
            }
        }
        return array;
    }

    public static WritableArray variantsToWritableArray(List<Map<String, Object>> variantsList) {
        WritableArray result = Arguments.createArray();
        if (variantsList != null) {
            result = CleverTapUtils.MapUtil.ArrayUtil.toWritableArray(new ArrayList<>(variantsList));
        }
        return result;
    }

    private static WritableMap eventLogToWritableMap(UserEventLog eventLog) {
        WritableMap ret = Arguments.createMap();

        if (eventLog != null) {
            ret.putString("eventName", eventLog.getEventName());
            ret.putString("normalizedEventName", eventLog.getNormalizedEventName());
            ret.putDouble("firstTime", eventLog.getFirstTs());
            ret.putDouble("lastTime", eventLog.getLastTs());
            ret.putInt("count", eventLog.getCountOfEvents());
            ret.putString("deviceID", eventLog.getDeviceID());
        }
        return ret;
    }

    private static WritableMap eventLogHistoryToWritableMap(Map<String, UserEventLog> history) {
        WritableMap ret = Arguments.createMap();

        if (history != null) {
            for (String key : history.keySet()) {
                ret.putMap(key, eventLogToWritableMap(history.get(key)));
            }
        }
        return ret;
    }

    @Deprecated(since = "3.2.0")
    private static WritableMap eventDetailToWritableMap(EventDetail details) {
        WritableMap ret = Arguments.createMap();

        if (details != null) {
            ret.putString("name", details.getName());
            ret.putInt("firstTime", details.getFirstTime());
            ret.putInt("lastTime", details.getLastTime());
            ret.putInt("count", details.getCount());
        }
        return ret;
    }

    @Deprecated(since = "3.2.0")
    private static WritableMap eventHistoryToWritableMap(Map<String, EventDetail> history) {
        WritableMap ret = Arguments.createMap();

        if (history != null) {
            for (String key : history.keySet()) {
                ret.putMap(key, eventDetailToWritableMap(history.get(key)));
            }
        }
        return ret;
    }

    private static JSONArray jsonArrayFromReadableArray(ReadableArray readableArray) throws JSONException {
        JSONArray array = new JSONArray();
        for (int i = 0; i < readableArray.size(); i++) {
            switch (readableArray.getType(i)) {
                case Null:
                    break;
                case Boolean:
                    array.put(readableArray.getBoolean(i));
                    break;
                case Number:
                    array.put(readableArray.getDouble(i));
                    break;
                case String:
                    array.put(readableArray.getString(i));
                    break;
                case Map:
                    array.put(jsonObjectFromReadableMap(readableArray.getMap(i)));
                    break;
                case Array:
                    array.put(jsonArrayFromReadableArray(readableArray.getArray(i)));
                    break;
            }
        }
        return array;
    }

    private static JSONObject jsonObjectFromReadableMap(ReadableMap readableMap) throws JSONException {
        JSONObject object = new JSONObject();
        ReadableMapKeySetIterator iterator = readableMap.keySetIterator();
        while (iterator.hasNextKey()) {
            String key = iterator.nextKey();
            switch (readableMap.getType(key)) {
                case Null:
                    object.put(key, JSONObject.NULL);
                    break;
                case Boolean:
                    object.put(key, readableMap.getBoolean(key));
                    break;
                case Number:
                    object.put(key, readableMap.getDouble(key));
                    break;
                case String:
                    object.put(key, readableMap.getString(key));
                    break;
                case Map:
                    object.put(key, jsonObjectFromReadableMap(readableMap.getMap(key)));
                    break;
                case Array:
                    object.put(key, jsonArrayFromReadableArray(readableMap.getArray(key)));
                    break;
            }
        }
        return object;
    }

    private static WritableMap utmDetailsToWritableMap(UTMDetail details) {
        WritableMap ret = Arguments.createMap();

        if (details != null) {
            ret.putString("campaign", details.getCampaign());
            ret.putString("source", details.getSource());
            ret.putString("medium", details.getMedium());
        }
        return ret;
    }
}
