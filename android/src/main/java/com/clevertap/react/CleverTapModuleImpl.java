package com.clevertap.react;

import static com.clevertap.react.CleverTapUtils.convertObjectToWritableMap;
import static com.clevertap.react.CleverTapUtils.getWritableArrayFromDisplayUnitList;
import static com.clevertap.react.CleverTapUtils.getWritableMapFromMap;

import android.annotation.SuppressLint;
import android.location.Location;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.RequiresApi;
import com.clevertap.android.sdk.CTFeatureFlagsListener;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.InAppNotificationButtonListener;
import com.clevertap.android.sdk.InAppNotificationListener;
import com.clevertap.android.sdk.InboxMessageButtonListener;
import com.clevertap.android.sdk.InboxMessageListener;
import com.clevertap.android.sdk.Logger;
import com.clevertap.android.sdk.PushPermissionResponseListener;
import com.clevertap.android.sdk.SyncListener;
import com.clevertap.android.sdk.UTMDetail;
import com.clevertap.android.sdk.displayunits.DisplayUnitListener;
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit;
import com.clevertap.android.sdk.events.EventDetail;
import com.clevertap.android.sdk.featureFlags.CTFeatureFlagsController;
import com.clevertap.android.sdk.inapp.CTInAppNotification;
import com.clevertap.android.sdk.inapp.CTLocalInApp;
import com.clevertap.android.sdk.inbox.CTInboxMessage;
import com.clevertap.android.sdk.interfaces.OnInitCleverTapIDListener;
import com.clevertap.android.sdk.product_config.CTProductConfigController;
import com.clevertap.android.sdk.product_config.CTProductConfigListener;
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener;
import com.clevertap.android.sdk.variables.CTVariableUtils;
import com.clevertap.android.sdk.variables.Var;
import com.clevertap.android.sdk.variables.callbacks.FetchVariablesCallback;
import com.clevertap.android.sdk.variables.callbacks.VariableCallback;
import com.clevertap.android.sdk.variables.callbacks.VariablesChangedCallback;
import com.clevertap.android.sdk.inapp.callbacks.FetchInAppsCallback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"unused", "RedundantSuppression"})
public class CleverTapModuleImpl implements SyncListener,
        InAppNotificationListener, CTInboxListener,
        InboxMessageButtonListener, InboxMessageListener,
        InAppNotificationButtonListener, DisplayUnitListener, CTProductConfigListener,
        CTFeatureFlagsListener, CTPushNotificationListener, PushPermissionResponseListener {

    @SuppressWarnings("FieldCanBeLocal")
    private enum InBoxMessages {
        ALL(0),
        UNREAD(1);

        private final int value;

        InBoxMessages(final int newValue) {
            value = newValue;
        }

    }

    private enum ErrorMessages {

        CLEVERTAP_NOT_INITIALIZED("CleverTap not initialized"),
        PRODUCTCONFIG_NOT_INITIALIZED("Product Config not initialized"),
        FF_NOT_INITIALIZED("Feature Flags not initialized");

        private final String errorMessage;

        ErrorMessages(String s) {
            errorMessage = s;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    private static Uri mlaunchURI;

    public static final String REACT_MODULE_NAME = "CleverTapReact";

    private static final String TAG = REACT_MODULE_NAME;

    private static final String CLEVERTAP_PROFILE_DID_INITIALIZE = "CleverTapProfileDidInitialize";

    private static final String CLEVERTAP_PROFILE_SYNC = "CleverTapProfileSync";

    private static final String CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED = "CleverTapInAppNotificationDismissed";

    private static final String CLEVERTAP_IN_APP_NOTIFICATION_SHOWED = "CleverTapInAppNotificationShowed";

    private static final String FCM = "FCM";

    private static final String BPS = "BPS";

    private static final String HPS = "HPS";

    private static final String CLEVERTAP_INBOX_DID_INITIALIZE = "CleverTapInboxDidInitialize";

    private static final String CLEVERTAP_INBOX_MESSAGES_DID_UPDATE = "CleverTapInboxMessagesDidUpdate";

    private static final String CLEVERTAP_ON_INBOX_BUTTON_CLICK = "CleverTapInboxMessageButtonTapped";

    private static final String CLEVERTAP_ON_INBOX_MESSAGE_CLICK = "CleverTapInboxMessageTapped";

    private static final String CLEVERTAP_ON_INAPP_BUTTON_CLICK = "CleverTapInAppNotificationButtonTapped";

    private static final String CLEVERTAP_ON_DISPLAY_UNITS_LOADED = "CleverTapDisplayUnitsLoaded";

    private static final String CLEVERTAP_FEATURE_FLAGS_DID_UPDATE = "CleverTapFeatureFlagsDidUpdate";

    private static final String CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE = "CleverTapProductConfigDidInitialize";

    private static final String CLEVERTAP_PRODUCT_CONFIG_DID_FETCH = "CleverTapProductConfigDidFetch";

    private static final String CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE = "CleverTapProductConfigDidActivate";

    private static final String CLEVERTAP_PUSH_NOTIFICATION_CLICKED = "CleverTapPushNotificationClicked";

    private static final String CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE = "CleverTapPushPermissionResponseReceived";

    private static final String CLEVERTAP_ON_VARIABLES_CHANGED = "CleverTapOnVariablesChanged";

    private static final String CLEVERTAP_ON_VALUE_CHANGED = "CleverTapOnValueChanged";

    private final ReactApplicationContext context;

    private CleverTapAPI mCleverTap;

    public static Map<String, Object> variables = new HashMap<>();

    public static void setInitialUri(final Uri uri) {
        mlaunchURI = uri;
    }

    public CleverTapModuleImpl(ReactApplicationContext reactContext) {
        this.context = reactContext;
        getCleverTapAPI();
    }

    @SuppressLint("RestrictedApi")
    public void setLibrary(String libName, int libVersion) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.setCustomSdkVersion(libName, libVersion);
        }
    }

    public void setLocale(String locale) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.setLocale(locale);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void activate() {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }
        productConfigController.activate();
    }

    // InAppNotificationListener
    public boolean beforeShow(Map<String, Object> var1) {
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onShow(CTInAppNotification inAppNotification) {
        WritableMap params = Arguments.createMap();
        JSONObject data = inAppNotification.getJsonDescription();
        if (data != null) {
            params.putMap("data", convertObjectToWritableMap(data));
        }
        sendEvent(CLEVERTAP_IN_APP_NOTIFICATION_SHOWED, params);
    }

    //Custom Push Notification
    public void createNotification(ReadableMap extras) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
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
            e.printStackTrace();
        }
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannel(String channelId, String channelName, String channelDescription,
            int importance, boolean showBadge) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null) {
            return;
        }
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                showBadge);
        Log.i(TAG, "Notification Channel " + channelName + " created");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelGroup(String groupId, String groupName) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null || groupName == null) {
            return;
        }
        CleverTapAPI.createNotificationChannelGroup(this.context, groupId, groupName);
        Log.i(TAG, "Notification Channel Group " + groupName + " created");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithGroupId(String channelId, String channelName, String channelDescription,
            int importance, String groupId, boolean showBadge) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null
                || groupId == null) {
            return;
        }
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                groupId, showBadge);
        Log.i(TAG, "Notification Channel " + channelName + " with Group Id " + groupId + " created");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithGroupIdAndSound(String channelId, String channelName,
            String channelDescription, int importance, String groupId, boolean showBadge, String sound) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null
                || groupId == null || sound == null) {
            return;
        }
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                groupId, showBadge, sound);
        Log.i(TAG, "Notification Channel " + channelName + " with Group Id " + groupId + " and sound file " + sound
                + " created");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void createNotificationChannelWithSound(String channelId, String channelName, String channelDescription,
            int importance, boolean showBadge, String sound) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null
                || sound == null) {
            return;
        }
        CleverTapAPI.createNotificationChannel(this.context, channelId, channelName, channelDescription, importance,
                showBadge, sound);
        Log.i(TAG, "Notification Channel " + channelName + " with sound file " + sound + " created");
    }


    @RequiresApi(api = VERSION_CODES.O)
    public void deleteNotificationChannel(String channelId) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null) {
            return;
        }
        CleverTapAPI.deleteNotificationChannel(this.context, channelId);
        Log.i(TAG, "Notification Channel Id " + channelId + " deleted");
    }

    @RequiresApi(api = VERSION_CODES.O)
    public void deleteNotificationChannelGroup(String groupId) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null) {
            return;
        }
        CleverTapAPI.deleteNotificationChannelGroup(this.context, groupId);
        Log.i(TAG, "Notification Channel Group Id " + groupId + " deleted");
    }

    //Push permission methods
    public void promptForPushPermission(boolean showFallbackSettings) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.promptForPushPermission(showFallbackSettings);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void promptPushPrimer(ReadableMap localInAppConfig) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            JSONObject jsonObject = localInAppConfigFromReadableMap(localInAppConfig);
            cleverTap.promptPushPrimer(jsonObject);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void isPushPermissionGranted(final Callback callback) {
        final CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            boolean isPushPermissionGranted = clevertap.isPushPermissionGranted();
            callbackWithErrorAndResult(callback, null, isPushPermissionGranted);
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    @Override
    public void onPushPermissionResponse(boolean accepted) {
        Log.i(TAG, "onPushPermissionResponse result: " + accepted);
        WritableMap params = Arguments.createMap();
        params.putBoolean("accepted", accepted);
        sendEvent(CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE, params);
    }

    public void disablePersonalization() {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.disablePersonalization();
    }

    public void enableDeviceNetworkInfoReporting(boolean value) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.enableDeviceNetworkInfoReporting(value);
    }

    public void enablePersonalization() {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.enablePersonalization();
    }

    public void eventGetDetail(String eventName, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            EventDetail detail = clevertap.getDetails(eventName);
            result = eventDetailToWritableMap(detail);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void eventGetFirstTime(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getFirstTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void eventGetLastTime(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getLastTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void eventGetOccurrences(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getCount(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    //Feature Flags Callback
    @Override
    public void featureFlagsUpdated() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_FEATURE_FLAGS_DID_UPDATE, params);//passing empty map
    }

    public void fetch() {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetch();
    }

    public void fetchAndActivate() {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetchAndActivate();
    }

    public void fetchWithMinimumFetchIntervalInSeconds(int interval) {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        productConfigController.fetch(interval);
    }

    public void getAllDisplayUnits(Callback callback) {
        String error = null;
        WritableArray result = Arguments.createArray();

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            result = getWritableArrayFromDisplayUnitList(cleverTap.getAllDisplayUnits());
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getBoolean(String key, Callback callback) {
        String error = null;
        Boolean result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getBoolean(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED.getErrorMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getDisplayUnitForId(String unitID, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CleverTapDisplayUnit displayUnit = cleverTap.getDisplayUnitForId(unitID);
            if (displayUnit != null && displayUnit.getJsonObject() != null) {
                result = convertObjectToWritableMap(displayUnit.getJsonObject());
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getDouble(String key, Callback callback) {
        String error = null;
        Double result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getDouble(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED.getErrorMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getEventHistory(Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            Map<String, EventDetail> history = clevertap.getHistory();
            result = eventHistoryToWritableMap(history);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getFeatureFlag(String name, Boolean defaultValue, Callback callback) {
        String error = null;
        Boolean result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTFeatureFlagsController featureFlagsController = cleverTap.featureFlag();
            if (featureFlagsController != null) {
                result = featureFlagsController.get(name, defaultValue);
            } else {
                error = ErrorMessages.FF_NOT_INITIALIZED.getErrorMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getAllInboxMessages(Callback callback) {
        getInboxMessages(callback, InBoxMessages.ALL);
    }

    public void getInboxMessageCount(Callback callback) {

        String error = null;
        int result = -1;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            result = cleverTap.getInboxMessageCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getInboxMessageForId(String messageId, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTInboxMessage inboxMessage = cleverTap.getInboxMessageForId(messageId);

            if (inboxMessage != null && inboxMessage.getData() != null) {
                result = convertObjectToWritableMap(inboxMessage.getData());
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getInboxMessageUnreadCount(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            result = cleverTap.getInboxMessageUnreadCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void deleteInboxMessageForId(String messageId) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.deleteInboxMessage(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void getUnreadInboxMessages(Callback callback) {
        getInboxMessages(callback, InBoxMessages.UNREAD);
    }


    //App Inbox methods
    public void initializeInbox() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.initializeInbox();
            Log.e(TAG, "initializeInbox Called");
        }
    }

    public void markReadInboxMessageForId(String messageId) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.markReadInboxMessage(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void markReadInboxMessagesForIDs(final ReadableArray messageIDs) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.markReadInboxMessagesForIDs(arrayListStringFromReadableArray(messageIDs));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void deleteInboxMessagesForIDs(final ReadableArray messageIDs) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.deleteInboxMessagesForIDs(arrayListStringFromReadableArray(messageIDs));
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    //Inbox Callbacks
    public void inboxDidInitialize() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_INBOX_DID_INITIALIZE, params);//passing empty map
    }
    public void inboxMessagesDidUpdate() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_INBOX_MESSAGES_DID_UPDATE, params); //passing empty map
    }
    public void onInboxButtonClick(HashMap<String, String> payload) {
        sendEvent(CLEVERTAP_ON_INBOX_BUTTON_CLICK, getWritableMapFromMap(payload));
    }

    public void pushInboxNotificationClickedEventForId(String messageId) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.pushInboxNotificationClickedEvent(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void pushInboxNotificationViewedEventForId(String messageId) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.pushInboxNotificationViewedEvent(messageId);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void showInbox(ReadableMap styleConfig) {
        CTInboxStyleConfig inboxStyleConfig = styleConfigFromReadableMap(styleConfig);
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.showAppInbox(inboxStyleConfig);
        }
    }

    public void dismissInbox() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.dismissAppInbox();
        }
    }

    public void getInitialUrl(Callback callback) {
        String error = null;
        String url = null;

        if (mlaunchURI == null) {
            error = "CleverTap InitialUrl is null";
        } else {
            url = mlaunchURI.toString();
        }
        callbackWithErrorAndResult(callback, error, url);
    }

    public void getLastFetchTimeStampInMillis(Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = String.valueOf(productConfigController.getLastFetchTimeStampInMillis());
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED.getErrorMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getString(String key, Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            CTProductConfigController productConfigController = cleverTap.productConfig();
            if (productConfigController != null) {
                result = productConfigController.getString(key);
            } else {
                error = ErrorMessages.PRODUCTCONFIG_NOT_INITIALIZED.getErrorMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }


    @Override
    public void onActivated() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE, params);//passing empty map
    }

    public void onDismissed(Map<String, Object> var1, @Nullable Map<String, Object> var2) {

        WritableMap extrasParams = getWritableMapFromMap(var1);
        WritableMap actionExtrasParams = getWritableMapFromMap(var2);

        WritableMap params = Arguments.createMap();
        params.putMap("extras", extrasParams);
        params.putMap("actionExtras", actionExtrasParams);

        sendEvent(CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED, params);
    }

    //Native Display callback
    public void onDisplayUnitsLoaded(ArrayList<CleverTapDisplayUnit> units) {
        WritableMap params = Arguments.createMap();
        params.putArray("displayUnits", getWritableArrayFromDisplayUnitList(units));
        sendEvent(CLEVERTAP_ON_DISPLAY_UNITS_LOADED, params);
    }

    @Override
    public void onFetched() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_PRODUCT_CONFIG_DID_FETCH, params);//passing empty map
    }

    //InApp Notification callback
    public void onInAppButtonClick(HashMap<String, String> hashMap) {
        sendEvent(CLEVERTAP_ON_INAPP_BUTTON_CLICK, getWritableMapFromMap(hashMap));
    }

    @Override
    public void onInboxItemClicked(CTInboxMessage message, int contentPageIndex, int buttonIndex) {
        WritableMap params = Arguments.createMap();
        JSONObject data = message.getData();
        params.putMap("data", (data != null ? convertObjectToWritableMap(data) : params));
        params.putInt("contentPageIndex", contentPageIndex);
        params.putInt("buttonIndex", buttonIndex);
        sendEvent(CLEVERTAP_ON_INBOX_MESSAGE_CLICK, params);
    }

    //Product Config Callback
    @Override
    public void onInit() {
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE, params);//passing empty map
    }

    //Push Notification Clicked callback
    @Override
    public void onNotificationClickedPayloadReceived(HashMap<String, Object> payload) {
        sendEvent(CLEVERTAP_PUSH_NOTIFICATION_CLICKED, getWritableMapFromMap(payload));
    }

    public void onUserLogin(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.onUserLogin(finalProfile);
    }

    public void profileAddMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.addMultiValueForKey(key, value);
    }

    public void profileAddMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.addMultiValuesForKey(key, finalValues);
    }

    // SyncListener
    @SuppressWarnings("rawtypes")
    public void profileDataUpdated(JSONObject updates) {
        if (updates == null) {
            return;
        }

        WritableMap updateParams = Arguments.createMap();
        Iterator keys = updates.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            try {
                JSONArray arr = updates.getJSONArray(key);
                WritableArray writableArray = Arguments.createArray();
                for (int n = 0; n < arr.length(); n++) {
                    JSONObject object = arr.getJSONObject(n);
                    writableArray.pushString(object.toString());
                }
                updateParams.putArray(key, writableArray);

            } catch (Throwable t) {
                try {
                    Object value = updates.get(key);
                    updateParams.putString(key, value.toString());
                } catch (Throwable t1) {
                    Log.e(TAG, t1.getLocalizedMessage());
                }
            }
        }

        WritableMap params = Arguments.createMap();
        params.putMap("updates", updateParams);
        sendEvent(CLEVERTAP_PROFILE_SYNC, params);
    }

    public void profileDidInitialize(String CleverTapID) {
        if (CleverTapID == null) {
            return;
        }
        WritableMap params = Arguments.createMap();
        params.putString("CleverTapID", CleverTapID);
        sendEvent(CLEVERTAP_PROFILE_DID_INITIALIZE, params);
    }

    public void profileGetCleverTapAttributionIdentifier(Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getCleverTapAttributionIdentifier();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void profileGetCleverTapID(Callback callback) {
        String error = null;
        String result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getCleverTapID();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getCleverTapID(final Callback callback) {
        final CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            clevertap.getCleverTapID(new OnInitCleverTapIDListener() {
                @Override
                public void onInitCleverTapID(final String cleverTapID) {
                    // Callback on main thread
                    callbackWithErrorAndResult(callback, null, cleverTapID);
                }

            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void profileGetProperty(String propertyName, Callback callback) {
        String error = null;
        Object result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
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

    public void profileRemoveMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.removeMultiValueForKey(key, value);
    }

    public void profileRemoveMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.removeMultiValuesForKey(key, finalValues);
    }

    public void profileRemoveValueForKey(String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.removeValueForKey(key);
    }

    public void profileSet(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.pushProfile(finalProfile);
    }

    public void profileSetMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.setMultiValuesForKey(key, finalValues);
    }

    public void pushDisplayUnitClickedEventForID(String unitID) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.pushDisplayUnitClickedEventForID(unitID);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void pushDisplayUnitViewedEventForID(String unitID) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.pushDisplayUnitViewedEventForID(unitID);
        } else {
            Log.e(TAG, ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage());
        }
    }

    public void pushInstallReferrer(String source, String medium, String campaign) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.pushInstallReferrer(source, medium, campaign);
    }

    public void recordChargedEvent(ReadableMap details, ReadableArray items) {
        CleverTapAPI clevertap = getCleverTapAPI();
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

    public void recordEvent(String eventName, ReadableMap props) {
        CleverTapAPI clevertap = getCleverTapAPI();
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

    public void recordScreenView(String screenName) {
        CleverTapAPI clevertap = getCleverTapAPI();
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

    public void reset() {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        productConfigController.reset();
    }

    public void sessionGetPreviousVisitTime(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getPreviousVisitTime();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetScreenCount(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getScreenCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetTimeElapsed(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getTimeElapsed();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetTotalVisits(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.getTotalVisits();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void sessionGetUTMDetails(Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
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

    public void setDefaultsMap(ReadableMap map) {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        HashMap<String, Object> finalMap = eventPropsFromReadableMap(map, Object.class);
        productConfigController.setDefaults(finalMap);
    }

    public void setLocation(double latitude, double longitude) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        final Location location = new Location("CleverTapReact");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        clevertap.setLocation(location);
    }

    public void setMinimumFetchIntervalInSeconds(int interval) {
        CTProductConfigController productConfigController = getCtProductConfigController();
        if (productConfigController == null) {
            return;
        }

        productConfigController.setMinimumFetchIntervalInSeconds(interval);
    }

    //Sets the SDK to offline mode
    public void setOffline(boolean value) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.setOffline(value);
    }

    public void setOptOut(boolean value) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return;
        }
        clevertap.setOptOut(value);
    }


    public void setPushTokenAsString(String token, String type) {
        Logger.v("setPushTokenAsString() called with: token = [" + token + "], type = [" + type + "]");
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || token == null || type == null) {
            return;
        }

        switch (type) {
            case FCM:
                clevertap.pushFcmRegistrationId(token, true);
                break;
            case BPS:
                clevertap.pushBaiduRegistrationId(token, true);
                break;
            case HPS:
                clevertap.pushHuaweiRegistrationId(token, true);
                break;
            default:
                Log.e(TAG, "Unknown push token type " + type);
                break;
        }
    }

    // Increment/Decrement Operator
    public void profileIncrementValueForKey(Double value, String key) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.incrementValue(key, value);
        }
    }

    public void profileDecrementValueForKey(Double value, String key) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.decrementValue(key, value);
        }
    }

    // InApp Controls
    public void suspendInAppNotifications() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.suspendInAppNotifications();
        }
    }

    public void discardInAppNotifications() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.discardInAppNotifications();
        }
    }

    public void resumeInAppNotifications() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.resumeInAppNotifications();
        }
    }

    /**************************************************
     *  Product Experience Remote Config methods starts
     *************************************************/
    public void syncVariables() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.syncVariables();
        }
    }

    public void syncVariablesinProd(boolean isProduction, Callback callback) {
        Log.i(TAG, "CleverTap syncVariablesinProd is no-op in Android");
    }

    public void fetchVariables() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.fetchVariables();
        }
    }

    public void defineVariables(ReadableMap object) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            for (Map.Entry<String, Object> entry : object.toHashMap().entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                variables.put(key, cleverTap.defineVariable(key, value));
            }
        }
    }

    public void fetchVariables(final Callback callback) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.fetchVariables(new FetchVariablesCallback() {
                @Override
                public void onVariablesFetched(final boolean isSuccess) {
                    callbackWithErrorAndResult(callback, null, isSuccess);
                }
            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
            callbackWithErrorAndResult(callback, error, null);
        }
    }

    public void getVariable(String key, final Callback callback) {
        String error = null;
        Object result = null;
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            try {
                result = getVariableValue(key);
            } catch (IllegalArgumentException e) {
                error = e.getLocalizedMessage();
            }
        } else {
            error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    public void getVariables(final Callback callback) {
        callbackWithErrorAndResult(callback, null, getVariablesValues());
    }

    public void onValueChanged(final String name) {
        if (variables.containsKey(name)) {

            Var<Object> var = (Var<Object>) variables.get(name);
            if (var != null) {
                var.addValueChangedCallback(new VariableCallback<Object>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onValueChanged(final Var<Object> variable) {
                        WritableMap result = null;
                        try {
                            result = getVariableValueAsWritableMap(name);
                        } catch (IllegalArgumentException e) {
                            Log.e(TAG, e.getLocalizedMessage());
                        }
                        sendEvent(CLEVERTAP_ON_VALUE_CHANGED, result);
                    }
                });
            } else {
                Log.d(TAG, "Variable value with name = " + name + " contains null value. Not setting onValueChanged callback.");
            }
        } else {
            Log.e(TAG, "Variable name = " + name + " does not exist. Make sure you set variable first.");
        }
    }

    public void onVariablesChanged() {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.addVariablesChangedCallback(new VariablesChangedCallback() {
                @Override
                public void variablesChanged() {
                    sendEvent(CLEVERTAP_ON_VARIABLES_CHANGED, getVariablesValues());
                }
            });
        }
    }
    /************************************************
     *  Product Experience Remote Config methods ends
     ************************************************/

    public void clearInAppResources(final boolean expiredOnly) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.clearInAppResources(expiredOnly);
        }
    }

    public void fetchInApps(final Callback callback) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            cleverTap.fetchInApps(new FetchInAppsCallback() {
                @Override
                public void onInAppsFetched(final boolean isSuccess) {
                    callbackWithErrorAndResult(callback, null, isSuccess);
                }
            });
        } else {
            String error = ErrorMessages.CLEVERTAP_NOT_INITIALIZED.getErrorMessage();
            callbackWithErrorAndResult(callback, error, null);
        }
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

    @SuppressLint("RestrictedApi")
    private Object getVariableValue(String name) {
        if (variables.containsKey(name)) {
            Var<?> variable = (Var<?>) variables.get(name);
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

    @SuppressLint("RestrictedApi")
    private WritableMap getVariableValueAsWritableMap(String name) {
        if (variables.containsKey(name)) {
            Var<?> variable = (Var<?>) variables.get(name);
            Object variableValue = variable.value();
            return CleverTapUtils.MapUtil.addValue(name, variable.value());
        }
        throw new IllegalArgumentException(
                "Variable name = " + name + " does not exist.");
    }

    private WritableMap getVariablesValues() {
        WritableMap writableMap = Arguments.createMap();
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
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
                } else {
                    Log.e(TAG, "Unhandled event property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return props;
    }

    // Listeners
    private void registerListeners(CleverTapAPI clevertap) {
        clevertap.registerPushPermissionNotificationResponseListener(this);
        clevertap.setCTPushNotificationListener(this);
        clevertap.setInAppNotificationListener(this);
        clevertap.setSyncListener(this);
        clevertap.setCTNotificationInboxListener(this);
        clevertap.setInboxMessageButtonListener(this);
        clevertap.setCTInboxMessageListener(this);
        clevertap.setInAppNotificationButtonListener(this);
        clevertap.setDisplayUnitListener(this);
        clevertap.setCTProductConfigListener(this);
        clevertap.setCTFeatureFlagsListener(this);
        clevertap.setLibrary("React-Native");
    }

    private CleverTapAPI getCleverTapAPI() {
        if (mCleverTap == null) {
            CleverTapAPI clevertap = CleverTapAPI.getDefaultInstance(this.context);
            if (clevertap != null) {
                registerListeners(clevertap);
            }
            mCleverTap = clevertap;
        }

        return mCleverTap;
    }

    public void setInstanceWithAccountId(String accountId) {
        if (mCleverTap == null || !accountId.equals(mCleverTap.getAccountId())) {
            CleverTapAPI cleverTap = CleverTapAPI.getGlobalInstance(this.context, accountId);
            if (cleverTap != null) {
                registerListeners(cleverTap);
                mCleverTap = cleverTap;
                Log.i(TAG, "CleverTap instance changed for accountId " + accountId);
            }
        }
    }

    private CTProductConfigController getCtProductConfigController() {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) {
            return null;
        }

        return clevertap.productConfig();
    }

    private void getInboxMessages(Callback callback, InBoxMessages type) {
        String error = null;
        ArrayList<CTInboxMessage> inboxMessages = new ArrayList<>();
        WritableArray result = Arguments.createArray();

        CleverTapAPI cleverTap = getCleverTapAPI();
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

                if ("DOB".equals(key) && (readableType == ReadableType.String)) {
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
                } else {
                    Log.e(TAG, "Unhandled profile property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return profile;
    }

    private void sendEvent(String eventName, @Nullable Object params) {
        try {
            this.context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    /**
     * retrieves the localInAppConfig from the given ReadableMap.
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
                btnTextColor = null, imageUrl = null, btnBackgroundColor = null, btnBorderRadius = null;
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
            builderWithRequiredParams.setImageUrl(imageUrl);
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
