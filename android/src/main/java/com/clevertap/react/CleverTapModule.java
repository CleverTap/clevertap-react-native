package com.clevertap.react;

import android.location.Location;
import android.telecom.Call;
import android.util.Log;
import android.net.Uri;

import com.clevertap.android.sdk.CTExperimentsListener;
import com.clevertap.android.sdk.CTInboxListener;
import com.clevertap.android.sdk.CTInboxStyleConfig;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import javax.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CleverTapModule extends ReactContextBaseJavaModule implements SyncListener, InAppNotificationListener, CTInboxListener, CTExperimentsListener {
    private ReactApplicationContext context;

    private CleverTapAPI mCleverTap;
    private static Uri mlaunchURI;

    private static final String REACT_MODULE_NAME = "CleverTapReact";
    private static final String TAG = REACT_MODULE_NAME;
    private static final String CLEVERTAP_PROFILE_DID_INITIALIZE = "CleverTapProfileDidInitialize";
    private static final String CLEVERTAP_PROFILE_SYNC = "CleverTapProfileSync";
    private static final String CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED = "CleverTapInAppNotificationDismissed";
    private static final String FCM = "FCM";
    private static final String CLEVERTAP_INBOX_DID_INITIALIZE = "CleverTapInboxDidInitialize";
    private static final String CLEVERTAP_INBOX_MESSAGES_DID_UPDATE = "CleverTapInboxMessagesDidUpdate";
    private static final String CLEVERTAP_EXPERIMENTS_DID_UPDATE = "CleverTapExperimentsDidUpdate";

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
        constants.put(CLEVERTAP_INBOX_DID_INITIALIZE,CLEVERTAP_INBOX_DID_INITIALIZE);
        constants.put(CLEVERTAP_INBOX_MESSAGES_DID_UPDATE,CLEVERTAP_INBOX_MESSAGES_DID_UPDATE);
        return constants;
    }

    public CleverTapModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.context = reactContext;
        getCleverTapAPI();
    }

    private CleverTapAPI getCleverTapAPI() {
        if (mCleverTap == null) {
            CleverTapAPI clevertap = CleverTapAPI.getDefaultInstance(this.context);
            if (clevertap != null) {
                clevertap.setInAppNotificationListener(this);
                clevertap.setSyncListener(this);
                clevertap.setCTNotificationInboxListener(this);
                clevertap.setCTExperimentsListener(this);
                clevertap.setLibrary("React-Native");
            }
            mCleverTap = clevertap;
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
            clevertap.pushFcmRegistrationId(token, true);
        } else {
            Log.e(TAG, "Unknown push token type "+ type);
        }
    }

    //UI Editor connection
    @ReactMethod
    public void setUIEditorConnectionEnabled(boolean enabled){
        CleverTapAPI clevertap = getCleverTapAPI();
        if(clevertap == null) return;
        CleverTapAPI.setUIEditorConnectionEnabled(enabled);
        Log.i(TAG,"UI Editor connection enabled - " + enabled);
    }

    //notification channel/group methods for Android O

    @ReactMethod
    public void createNotificationChannel(String channelId, String channelName, String channelDescription, int importance, boolean showBadge){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null) return;
        CleverTapAPI.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,showBadge);
        Log.i(TAG, "Notification Channel "+ channelName +" created");
    }

    @ReactMethod
    public void createNotificationChannelWithSound(String channelId, String channelName, String channelDescription, int importance, boolean showBadge, String sound){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null || sound == null) return;
        CleverTapAPI.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,showBadge,sound);
        Log.i(TAG, "Notification Channel "+ channelName +" with sound file "+sound+" created");
    }

    @ReactMethod
    public void createNotificationChannelWithGroupId(String channelId, String channelName, String channelDescription, int importance, String groupId, boolean showBadge){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null || groupId == null) return;
        CleverTapAPI.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,groupId,showBadge);
        Log.i(TAG, "Notification Channel "+ channelName +" with Group Id "+ groupId + " created");
    }

    @ReactMethod
    public void createNotificationChannelWithGroupIdAndSound(String channelId, String channelName, String channelDescription, int importance, String groupId, boolean showBadge, String sound){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null || channelName == null || channelDescription == null || groupId == null || sound == null) return;
        CleverTapAPI.createNotificationChannel(this.context,channelId,channelName,channelDescription,importance,groupId,showBadge,sound);
        Log.i(TAG, "Notification Channel "+ channelName +" with Group Id "+ groupId + " and sound file "+sound+" created");
    }

    @ReactMethod
    public void createNotificationChannelGroup(String groupId, String groupName){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null || groupName == null) return;
        CleverTapAPI.createNotificationChannelGroup(this.context,groupId,groupName);
        Log.i(TAG, "Notification Channel Group "+ groupName +" created");
    }

    @ReactMethod
    public void deleteNotificationChannel(String channelId){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || channelId == null) return;
        CleverTapAPI.deleteNotificationChannel(this.context,channelId);
        Log.i(TAG, "Notification Channel Id "+ channelId +" deleted");
    }

    @ReactMethod
    public void deleteNotificationChannelGroup(String groupId){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null || groupId == null) return;
        CleverTapAPI.deleteNotificationChannelGroup(this.context,groupId);
        Log.i(TAG, "Notification Channel Group Id "+ groupId +" deleted");
    }

    //Enables tracking opt out for the currently active user. 

    @ReactMethod
    public void setOptOut(boolean value){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.setOptOut(value);
    }

    //Sets the SDK to offline mode
    @ReactMethod
    public void setOffline(boolean value){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.setOffline(value);
    }

    //Enables the reporting of device network-related information, including IP address.  This reporting is disabled by default.

    @ReactMethod
    public void enableDeviceNetworkInfoReporting(boolean value){
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.enableDeviceNetworkInfoReporting(value);
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
        CleverTapAPI clevertap = getCleverTapAPI();
        if(clevertap == null) return;
        clevertap.recordScreen(screenName);
    }

    @ReactMethod
    public void recordEvent(String eventName, ReadableMap props) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        Map<String, Object> finalProps = eventPropsFromReadableMap(props);

        if (finalProps == null) {
            clevertap.pushEvent(eventName);
        } else {
            clevertap.pushEvent(eventName, finalProps);
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
            clevertap.pushChargedEvent(finalDetails, finalItems);
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
            result = clevertap.getFirstTime(eventName);
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
            result = clevertap.getLastTime(eventName);
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
            result = clevertap.getCount(eventName);
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
            EventDetail detail = clevertap.getDetails(eventName);
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
            Map<String, EventDetail> history = clevertap.getHistory();
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
        clevertap.pushProfile(finalProfile);
    }

    @ReactMethod
    public void profileSetGraphUser(ReadableMap profile) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;

        try {
            JSONObject finalProfile = jsonObjectFromReadableMap(profile);
            clevertap.pushFacebookUser(finalProfile);
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
            Object value = clevertap.getProperty(propertyName);
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
        clevertap.removeValueForKey(key);
    }

    @ReactMethod
    public void profileSetMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.setMultiValuesForKey(key, finalValues);
    }

    @ReactMethod
    public void profileAddMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.addMultiValueForKey(key, value);
    }

    @ReactMethod
    public void profileAddMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.addMultiValuesForKey(key, finalValues);
    }

    @ReactMethod
    public void profileRemoveMultiValue(String value, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        clevertap.removeMultiValueForKey(key, value);
    }

    @ReactMethod
    public void profileRemoveMultiValues(ReadableArray values, String key) {
        CleverTapAPI clevertap = getCleverTapAPI();
        if (clevertap == null) return;
        ArrayList<String> finalValues = arrayListStringFromReadableArray(values);
        clevertap.removeMultiValuesForKey(key, finalValues);
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
            result = clevertap.getTimeElapsed();
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
            result = clevertap.getTotalVisits();
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
            result = clevertap.getScreenCount();
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
            result = clevertap.getPreviousVisitTime();
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
            UTMDetail details = clevertap.getUTMDetails();
            result = utmDetailsToWritableMap(details);
        } else {
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback, error, result);
    }

    //App Inbox methods
    @ReactMethod
    public void initializeInbox(){
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.initializeInbox();
            Log.e(TAG, "initializeInbox Called");
        }
    }

    @ReactMethod
    public void showInbox(ReadableMap styleConfig){
        CTInboxStyleConfig inboxStyleConfig = styleConfigFromReadableMap(styleConfig);
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.showAppInbox(inboxStyleConfig);
        }
    }

    @ReactMethod
    public void getInboxMessageCount(Callback callback){

        String error = null;
        int result = -1;

        CleverTapAPI cleverTap = getCleverTapAPI();
        if (cleverTap != null) {
            result = cleverTap.getInboxMessageCount();
        } else {
            error = "CleverTap not initialized";
        }
        Log.d(TAG,"inside total count");
        callbackWithErrorAndResult(callback, error, result);
    }

    @ReactMethod
    public void getInboxMessageUnreadCount(Callback callback){
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

    //Dynamic Variables Methods
    @ReactMethod
    public void registerBooleanVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerBooleanVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerDoubleVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerDoubleVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerIntegerVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerIntegerVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerStringVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerStringVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerListOfBooleanVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerListOfBooleanVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerListOfDoubleVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerListOfDoubleVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerListOfIntegerVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerListOfIntegerVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerListOfStringVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerListOfStringVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerMapOfBooleanVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerMapOfBooleanVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerMapOfDoubleVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerMapOfDoubleVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerMapOfIntegerVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerMapOfIntegerVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void registerMapOfStringVariable(String name) {
        CleverTapAPI cleverTap = getCleverTapAPI();
        if(cleverTap != null){
            cleverTap.registerMapOfStringVariable(name);
        }else{
            Log.e(TAG, "CleverTap not initialized");
        }
    }

    @ReactMethod
    public void getBooleanVariable(String name, Boolean defaultValue, Callback callback){
        String error = null;
        Boolean result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getBooleanVariable(name,defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getDoubleVariable(String name, Double defaultValue, Callback callback){
        String error = null;
        Double result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getDoubleVariable(name,defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getIntegerVariable(String name, int defaultValue, Callback callback){
        String error = null;
        int result = -1;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getIntegerVariable(name,defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getStringVariable(String name, String defaultValue, Callback callback){
        String error = null;
        String result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getStringVariable(name,defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getListOfBooleanVariable(String name, List<Boolean> defaultValue, Callback callback){
        String error = null;
        List<Boolean> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getListOfBooleanVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getListOfDoubleVariable(String name, List<Double> defaultValue, Callback callback){
        String error = null;
        List<Double> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getListOfDoubleVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getListOfIntegerVariable(String name, List<Integer> defaultValue, Callback callback){
        String error = null;
        List<Integer> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getListOfIntegerVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getListOfStringVariable(String name, List<String> defaultValue, Callback callback){
        String error = null;
        List<String> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getListOfStringVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getMapOfBooleanVariable(String name, Map<String,Boolean> defaultValue, Callback callback){
        String error = null;
        Map<String,Boolean> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getMapOfBooleanVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getMapOfDoubleVariable(String name, Map<String,Double> defaultValue, Callback callback){
        String error = null;
        Map<String,Double> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getMapOfDoubleVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getMapOfIntegerVariable(String name, Map<String,Integer> defaultValue, Callback callback){
        String error = null;
        Map<String,Integer> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getMapOfIntegerVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
    }

    @ReactMethod
    public void getMapOfStringVariable(String name, Map<String,String> defaultValue, Callback callback){
        String error = null;
        Map<String,String> result = null;

        CleverTapAPI cleverTap =  getCleverTapAPI();
        if(cleverTap != null){
            result = cleverTap.getMapOfStringVariable(name, defaultValue);
        }else{
            error = "CleverTap not initialized";
        }
        callbackWithErrorAndResult(callback,error,result);
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

    private CTInboxStyleConfig styleConfigFromReadableMap(ReadableMap styleConfig){
        if(styleConfig==null) return new CTInboxStyleConfig();

        CTInboxStyleConfig ctInboxStyleConfig = new CTInboxStyleConfig();
        ReadableMapKeySetIterator iterator = styleConfig.keySetIterator();
        while(iterator.hasNextKey()){
            try{
                String styleConfigKey = iterator.nextKey();
                ReadableType readableType = styleConfig.getType(styleConfigKey);
                if("navBarTitle".equals(styleConfigKey) && readableType == ReadableType.String){
                    String navBarTitle = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarTitle(navBarTitle);
                }
                if("navBarTitleColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String navBarTitleColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarTitleColor(navBarTitleColor);
                }
                if("navBarColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String navBarColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setNavBarColor(navBarColor);
                }
                if("inboxBackgroundColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String inboxBackgroundColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setInboxBackgroundColor(inboxBackgroundColor);
                }
                if("backButtonColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String backButtonColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setBackButtonColor(backButtonColor);
                }
                if("unselectedTabColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String unselectedTabColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setUnselectedTabColor(unselectedTabColor);
                }
                if("selectedTabColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String selectedTabColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setSelectedTabColor(selectedTabColor);
                }
                if("selectedTabIndicatorColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String selectedTabIndicatorColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setSelectedTabIndicatorColor(selectedTabIndicatorColor);
                }
                if("tabBackgroundColor".equals(styleConfigKey) && readableType == ReadableType.String){
                    String tabBackgroundColor = styleConfig.getString(styleConfigKey);
                    ctInboxStyleConfig.setTabBackgroundColor(tabBackgroundColor);
                }
                if("tabs".equals(styleConfigKey) && readableType == ReadableType.Array){
                    try {
                        ArrayList<String> tabsList = arrayListStringFromReadableArray(styleConfig.getArray(styleConfigKey));
                        ctInboxStyleConfig.setTabs(tabsList);
                    } catch (Throwable t) {
                        Log.e(TAG, "Unhandled ReadableType.Array from ReadableMap");
                    }
                }
            }catch (Throwable t){
                Log.e(TAG,t.getLocalizedMessage());
                return new CTInboxStyleConfig();
            }
        }
        return ctInboxStyleConfig;
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

    //Inbox Callbacks
    public void inboxDidInitialize(){
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_INBOX_DID_INITIALIZE,params);//passing empty map
    }

    public void inboxMessagesDidUpdate(){
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_INBOX_MESSAGES_DID_UPDATE,params); //passing empty map
    }

    //Experiments Callback
    public void CTExperimentsUpdated(){
        WritableMap params = Arguments.createMap();
        sendEvent(CLEVERTAP_EXPERIMENTS_DID_UPDATE,params);//passing empty map
    }
}
