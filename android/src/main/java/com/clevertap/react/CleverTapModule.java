package com.clevertap.react;

import android.location.Location;
import android.util.Log;
import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import com.clevertap.android.sdk.CleverTapAPI;
import com.clevertap.android.sdk.EventDetail;

import com.clevertap.android.sdk.InAppNotificationListener;
import com.clevertap.android.sdk.SyncListener;
import com.clevertap.android.sdk.UTMDetail;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CleverTapModule extends ReactContextBaseJavaModule implements SyncListener, InAppNotificationListener {
    private ReactApplicationContext context;

    private CleverTapAPI mCleverTap;
    private static Uri mlaunchURI;

    private static final String REACT_MODULE_NAME = "CleverTapReact";
    private static final String TAG = REACT_MODULE_NAME;
    private static final String CLEVERTAP_PROFILE_DID_INITIALIZE = "CleverTapProfileDidInitialize";
    private static final String CLEVERTAP_PROFILE_SYNC = "CleverTapProfileSync";
    private static final String CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED = "CleverTapInAppNotificationDismissed";
    private static final String FCM = "FCM";
    private static final String GCM = "GCM";

    public static void setInitialUri(final Uri uri) {
        mlaunchURI = uri;
    }

    @Override
    public String getName() {
        return REACT_MODULE_NAME;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(CLEVERTAP_PROFILE_DID_INITIALIZE, CLEVERTAP_PROFILE_DID_INITIALIZE);
        constants.put(CLEVERTAP_PROFILE_SYNC, CLEVERTAP_PROFILE_SYNC);
        constants.put(CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED, CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED);
        constants.put(FCM, FCM);
        constants.put(GCM, GCM);
        return constants;
    }

    public CleverTapModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        getCleverTapAPI();
    }

    private CleverTapAPI getCleverTapAPI() {
        if (mCleverTap == null) {
            try {
                CleverTapAPI clevertap = CleverTapAPI.getInstance(this.context);
                clevertap.setInAppNotificationListener(this);
                clevertap.setSyncListener(this);
                mCleverTap = clevertap;
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }

        if (mCleverTap == null) {
            Log.e(TAG, "CleverTap not initialized");
        }
        return mCleverTap;
    }

    // launch

    @ReactMethod
    public void getInitialUrl(Callback callback) {
        String error = null;
        String url = null;

        if (mlaunchURI == null) {
            error ="CleverTap InitialUrl is null";
        } else {
            url = mlaunchURI.toString();
        }
        callbackWithErrorAndResult(callback, error, url);
    }

    // push

    @ReactMethod
    public void registerForPush() {
        // no-op in Android
        Log.i(TAG, "CleverTap.registerForPush is a no-op in Android");
    }

    @ReactMethod
    public void setPushTokenAsString(String token, String type) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || token == null || type == null) return;

        if (FCM.equals(type)) {
            clevertap.data.pushFcmRegistrationId(token, true);
        } else if (GCM.equals(type)) {
            clevertap.data.pushGcmRegistrationId(token, true);
        } else {
            Log.e(TAG, "Unknown push token type "+ type);
        }
    }

    //notification channel/group methods for Android O

    @ReactMethod
    public void createNotificationChannel(String channelId, String channelName, String channelDescription, int importance, boolean showBadge){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null) return;
        clevertap.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,showBadge);
        Log.i(TAG, "Notification Channel "+ channelName +" created");
    }

    @ReactMethod
    public void createNotificationChannelwithGroupId(String channelId, String channelName, String channelDescription, int importance, String groupId, boolean showBadge){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null || groupId == null) return;
        clevertap.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,groupId,showBadge);
        Log.i(TAG, "Notification Channel "+ channelName +" with Group Id "+ groupId + " created");
    }

    @ReactMethod
    public void createNotificationChannelGroup(String groupId, String groupName){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null || groupName == null) return;
        clevertap.createNotificationChannelGroup(this.context,groupId,groupName);
        Log.i(TAG, "Notification Channel Group "+ groupName +" created");
    }

    @ReactMethod
    public void deleteNotificationChannel(String channelId){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null) return;
        clevertap.deleteNotificationChannel(this.context,channelId);
        Log.i(TAG, "Notification Channel Id "+ channelId +" deleted");
    }

    @ReactMethod
    public void deleteNotificationChannelGroup(String groupId){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null) return;
        clevertap.deleteNotificationChannelGroup(this.context,groupId);
        Log.i(TAG, "Notification Channel Group Id "+ groupId +" deleted");
    }

    // Personalization

    @ReactMethod
    public void enablePersonalization() {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.enablePersonalization();
    }

    @ReactMethod
    public void disablePersonalization() {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.disablePersonalization();
    }

    // Event API

    @ReactMethod
    public void recordScreenView(String screenName) {
        // no-op in Android
        Log.i(TAG, "CleverTap.recordScreenView is a no-op in Android");
    }

    @ReactMethod
    public void recordEvent(String eventName, ReadableMap props) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        Map<String, Object> finalProps = eventPropsFromReadableMap(props);

        if (finalProps == null) {
            clevertap.event.push(eventName);
        } else {
            clevertap.event.push(eventName, finalProps);
        }
    }

    @ReactMethod
    public void recordChargedEvent(ReadableMap details, ReadableArray items) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || details == null) return;

        HashMap<String, Object> finalDetails = eventPropsFromReadableMap(details);

        ArrayList<HashMap<String, Object>> finalItems = new ArrayList<>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                try {
                    HashMap<String, Object> item = eventPropsFromReadableMap(items.getMap(i));
                    finalItems.add(item);
                } catch (Throwable t) {
                    Log.e(TAG, t.getLocalizedMessage());
                }
            }
        }

        try {
            clevertap.event.push("Charged", finalDetails, finalItems);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    @ReactMethod
    public void eventGetFirstTime(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.event.getFirstTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void eventGetLastTime(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.event.getLastTime(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void eventGetOccurrences(String eventName, Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.event.getCount(eventName);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void eventGetDetail(String eventName, Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            EventDetail detail = clevertap.event.getDetails(eventName);
            result = eventDetailToWritableMap(detail);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void getEventHistory(Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            Map<String, EventDetail> history = clevertap.event.getHistory();
            result = eventHistoryToWritableMap(history);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    // Profile API

    @ReactMethod
    public void setLocation(double latitude, double longitude) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        final Location location = new Location("CleverTapReact");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        clevertap.setLocation(location);
    }

    @ReactMethod
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

    @ReactMethod
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

    @ReactMethod
    public void onUserLogin(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.onUserLogin(finalProfile);
    }

    @ReactMethod
    public void profileSet(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        Map<String, Object> finalProfile = profileFromReadableMap(profile);
        clevertap.profile.push(finalProfile);
    }

    @ReactMethod
    public void profileSetGraphUser(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        try {
            JSONObject finalProfile = jsonObjectFromReadableMap(profile);
            clevertap.profile.pushFacebookUser(finalProfile);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
    }

    @ReactMethod
    @SuppressWarnings("unchecked")
    public void profileGetProperty(String propertyName, Callback callback) {
        String error = null;
        Object result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            Object value = clevertap.profile.getProperty(propertyName);
            // Handle JSONArray for multi-values, otherwise everything should be primitive or String
            if (value instanceof JSONArray) {
                JSONArray valueArray = (JSONArray)value;
                WritableArray writableArray = Arguments.createArray();
                for (int i = 0; i < valueArray.length(); i++) {
                    try {writableArray.pushString(valueArray.get(i).toString());
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

    @ReactMethod
    public void profileRemoveValueForKey(String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.profile.removeValueForKey(key);
    }

    @ReactMethod
    public void profileSetMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.profile.setMultiValuesForKey(key, finalValues);
    }

    @ReactMethod
    public void profileAddMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.profile.addMultiValueForKey(key, value);
    }

    @ReactMethod
    public void profileAddMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.profile.addMultiValuesForKey(key, finalValues);
    }

    @ReactMethod
    public void profileRemoveMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.profile.removeMultiValueForKey(key, value);
    }

    @ReactMethod
    public void profileRemoveMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.profile.removeMultiValuesForKey(key, finalValues);
    }


    // Session API

    @ReactMethod
    public void pushInstallReferrer(String source, String medium, String campaign) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.pushInstallReferrer(source, medium, campaign);
    }

    @ReactMethod
    public void sessionGetTimeElapsed(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.session.getTimeElapsed();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void sessionGetTotalVisits(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.session.getTotalVisits();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void sessionGetScreenCount(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.session.getScreenCount();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void sessionGetPreviousVisitTime(Callback callback) {
        String error = null;
        int result = -1;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            result = clevertap.session.getPreviousVisitTime();
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void sessionGetUTMDetails(Callback callback) {
        String error = null;
        WritableMap result = null;

        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap != null) {
            UTMDetail details = clevertap.session.getUTMDetails();
            result = utmDetailsToWritableMap(details);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }


    // Developer Options

    @ReactMethod
    public void setDebugLevel(int level) {
        CleverTapAPI.setDebugLevel(level);
    }


    // private helpers etc

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

    private void sendEvent(String eventName, @Nullable Object params) {
        try {
            this.context
                    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit(eventName, params);
        } catch (Throwable t) {
            Log.e(TAG, t.getLocalizedMessage());
        }
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

    private HashMap<String, Object> eventPropsFromReadableMap(ReadableMap propsMap) {
        if (propsMap == null) return null;

        HashMap<String, Object> props = new HashMap<>();

        ReadableMapKeySetIterator iterator = propsMap.keySetIterator();

        while (iterator.hasNextKey()) {
            try {
                String key = iterator.nextKey();
                ReadableType readableType = propsMap.getType(key);

                if (readableType == ReadableType.String) {
                    props.put(key, propsMap.getString(key));
                }
                else if (readableType == ReadableType.Boolean) {
                    props.put(key, propsMap.getBoolean(key));
                }
                else if (readableType == ReadableType.Number) {
                    try {
                        props.put(key, propsMap.getDouble(key));
                    } catch (Throwable t) {
                        try {
                            props.put(key, propsMap.getInt(key));
                        } catch (Throwable t1) {
                            Log.e(TAG, "Unhandled ReadableType.Number from ReadableMap");
                        }
                    }
                }
                else {
                    Log.e(TAG, "Unhandled event property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return props;
    }

    private HashMap<String, Object> profileFromReadableMap(ReadableMap profileMap) {
        if (profileMap == null) return null;

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
                }
                else if (readableType == ReadableType.String) {
                    profile.put(key, profileMap.getString(key));
                }
                else if (readableType == ReadableType.Boolean) {
                    profile.put(key, profileMap.getBoolean(key));
                }
                else if (readableType == ReadableType.Number) {
                    try {
                        profile.put(key, profileMap.getDouble(key));
                    } catch (Throwable t) {
                        try {
                            profile.put(key, profileMap.getInt(key));
                        } catch (Throwable t1) {
                            Log.e(TAG, "Unhandled ReadableType.Number from ReadableMap");
                        }
                    }
                }
                else if (readableType == ReadableType.Array) {
                    try {
                        profile.put(key, arrayListStringFromReadableArray(profileMap.getArray(key)));
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Array from ReadableMap");
                    }
                }
                else {
                    Log.e(TAG, "Unhandled profile property ReadableType");
                }
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }
        }
        return profile;
    }

    private static WritableMap eventDetailToWritableMap(EventDetail details)  {
        WritableMap ret = Arguments.createMap();

        if(details != null) {
            ret.putString("name", details.getName());
            ret.putInt("firstTime", details.getFirstTime());
            ret.putInt("lastTime", details.getLastTime());
            ret.putInt("count", details.getCount());
        }
        return ret;
    }

    private static WritableMap utmDetailsToWritableMap(UTMDetail details) {
        WritableMap ret = Arguments.createMap();

        if(details != null) {
            ret.putString("campaign", details.getCampaign());
            ret.putString("source", details.getSource());
            ret.putString("medium", details.getMedium());
        }
        return ret;
    }

    private static WritableMap eventHistoryToWritableMap(Map<String, EventDetail> history) {
        WritableMap ret = Arguments.createMap();

        if(history != null) {
            for (String key : history.keySet()) {
                ret.putMap(key, eventDetailToWritableMap(history.get(key)));
            }
        }
        return ret;
    }

    // Listeners

    // InAppNotificationListener
    public boolean beforeShow(Map<String, Object> var1) {
        return true;
    }

    public void onDismissed(Map<String, Object> var1, @Nullable Map<String, Object> var2) {
        JSONObject extras = var1 != null ? new JSONObject(var1) : new JSONObject();
        JSONObject actionExtras = var2 != null ? new JSONObject(var2) : new JSONObject();

        WritableMap extrasParams = Arguments.createMap();
        Iterator extrasKeys = extras.keys();
        while (extrasKeys.hasNext()) {
            String key = null;
            String value = null;
            try {
                key = extrasKeys.next().toString();
                value = extras.get(key).toString();
            } catch (Throwable t) {
                Log.e(TAG, t.getLocalizedMessage());
            }

            if (key != null && value != null) {
                extrasParams.putString(key, value);
            }
        }

        WritableMap actionExtrasParams = Arguments.createMap();
        Iterator actionExtrasKeys = actionExtras.keys();
        while (actionExtrasKeys.hasNext()) {
            String aKey = null;
            String aValue = null;
            try {
                aKey = actionExtrasKeys.next().toString();
                aValue = actionExtras.get(aKey).toString();
            } catch (Throwable t1) {
                Log.e(TAG, t1.getLocalizedMessage());
            }

            if (aKey != null && aValue != null) {
                actionExtrasParams.putString(aKey, aValue);
            }
        }

        WritableMap params = Arguments.createMap();
        params.putMap("extras", extrasParams);
        params.putMap("actionExtras", actionExtrasParams);

        sendEvent(CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED, params);
    }

    // SyncListener
    public void profileDataUpdated(JSONObject updates) {
        if(updates == null) return;

        WritableMap updateParams = Arguments.createMap();
        Iterator keys = updates.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            try {
                JSONArray arr = updates.getJSONArray(key);
                WritableArray writableArray = Arguments.createArray();
                for(int n = 0; n < arr.length(); n++) {
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

    public void profileDidInitialize (String CleverTapID) {
        if (CleverTapID == null) return;
        WritableMap params = Arguments.createMap();
        params.putString("CleverTapID", CleverTapID);
        sendEvent(CLEVERTAP_PROFILE_DID_INITIALIZE, params);
    }
}
