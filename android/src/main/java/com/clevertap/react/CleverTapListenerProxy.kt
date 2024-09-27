package com.clevertap.react

import android.util.Log
import com.clevertap.android.sdk.CTFeatureFlagsListener
import com.clevertap.android.sdk.CTInboxListener
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.InAppNotificationButtonListener
import com.clevertap.android.sdk.InAppNotificationListener
import com.clevertap.android.sdk.InboxMessageButtonListener
import com.clevertap.android.sdk.InboxMessageListener
import com.clevertap.android.sdk.PushPermissionResponseListener
import com.clevertap.android.sdk.SyncListener
import com.clevertap.android.sdk.displayunits.DisplayUnitListener
import com.clevertap.android.sdk.displayunits.model.CleverTapDisplayUnit
import com.clevertap.android.sdk.inapp.CTInAppNotification
import com.clevertap.android.sdk.inbox.CTInboxMessage
import com.clevertap.android.sdk.product_config.CTProductConfigListener
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener
import com.facebook.react.bridge.Arguments
import org.json.JSONException
import org.json.JSONObject

object CleverTapListenerProxy : SyncListener, InAppNotificationListener, CTInboxListener,
    InboxMessageButtonListener, InboxMessageListener, InAppNotificationButtonListener,
    DisplayUnitListener, CTProductConfigListener, CTFeatureFlagsListener,
    CTPushNotificationListener, PushPermissionResponseListener {

    private const val LOG_TAG = Constants.REACT_MODULE_NAME

    fun attachToInstance(instance: CleverTapAPI) {
        instance.unregisterPushPermissionNotificationResponseListener(this)
        instance.registerPushPermissionNotificationResponseListener(this)
        instance.ctPushNotificationListener = this
        instance.inAppNotificationListener = this
        instance.syncListener = this
        instance.ctNotificationInboxListener = this
        instance.setInboxMessageButtonListener(this)
        instance.setCTInboxMessageListener(this)
        instance.setInAppNotificationButtonListener(this)
        instance.setDisplayUnitListener(this)
        instance.setCTProductConfigListener(this)
        instance.setCTFeatureFlagsListener(this)
    }

    // SyncListener
    override fun profileDataUpdated(updates: JSONObject?) {
        if (updates == null) {
            return
        }

        val updateParams = Arguments.createMap()
        val keys: Iterator<String> = updates.keys()

        while (keys.hasNext()) {
            val key = keys.next()
            try {
                val arr = updates.getJSONArray(key)
                val writableArray = Arguments.createArray()
                for (n in 0 until arr.length()) {
                    val update = arr.getJSONObject(n)
                    writableArray.pushString(update.toString())
                }
                updateParams.putArray(key, writableArray)
            } catch (je: JSONException) {
                try {
                    val value = updates[key]
                    updateParams.putString(key, value.toString())
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Failed sending profile update event", e)
                }
            }
        }

        val params = Arguments.createMap()
        params.putMap("updates", updateParams)
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_PROFILE_SYNC, params)
    }

    // SyncListener
    override fun profileDidInitialize(cleverTapID: String?) {
        if (cleverTapID == null) {
            Log.d(LOG_TAG, "profileDidInitialize called with cleverTapID=null")
            return
        }
        val params = Arguments.createMap()
        params.putString("CleverTapID", cleverTapID)
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_PROFILE_DID_INITIALIZE, params)
    }

    // InAppNotificationListener
    override fun beforeShow(extras: MutableMap<String, Any>?): Boolean {
        return true
    }

    // InAppNotificationListener
    override fun onShow(ctInAppNotification: CTInAppNotification) {
        val params = Arguments.createMap()
        val data = ctInAppNotification.jsonDescription
        if (data != null) {
            params.putMap("data", CleverTapUtils.convertObjectToWritableMap(data))
        }
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_IN_APP_NOTIFICATION_SHOWED, params)
    }

    // InAppNotificationListener
    override fun onDismissed(
        extras: MutableMap<String, Any>?, actionExtras: MutableMap<String, Any>?
    ) {
        val extrasParams = CleverTapUtils.getWritableMapFromMap(extras)
        val actionExtrasParams = CleverTapUtils.getWritableMapFromMap(actionExtras)

        val params = Arguments.createMap()
        params.putMap("extras", extrasParams)
        params.putMap("actionExtras", actionExtrasParams)

        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED, params)
    }

    // CTInboxListener
    override fun inboxDidInitialize() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_INBOX_DID_INITIALIZE,
            Arguments.createMap()
        )
    }

    // CTInboxListener
    override fun inboxMessagesDidUpdate() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_INBOX_MESSAGES_DID_UPDATE,
            Arguments.createMap()
        )
    }

    // CTInboxListener
    override fun onInboxButtonClick(payload: HashMap<String, String>?) {
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_ON_INBOX_BUTTON_CLICK,
            CleverTapUtils.getWritableMapFromMap(payload)
        )
    }

    // CTInboxListener
    override fun onInboxItemClicked(
        message: CTInboxMessage?, contentPageIndex: Int, buttonIndex: Int
    ) {
        val params = Arguments.createMap()
        val data = message?.data
        params.putMap("data",
            data?.let { CleverTapUtils.convertObjectToWritableMap(it) } ?: Arguments.createMap())
        params.putInt("contentPageIndex", contentPageIndex)
        params.putInt("buttonIndex", buttonIndex)
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_ON_INBOX_MESSAGE_CLICK, params)
    }

    // InAppNotificationButtonListener
    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_ON_INAPP_BUTTON_CLICK,
            CleverTapUtils.getWritableMapFromMap(payload)
        )
    }

    // DisplayUnitListener
    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        val params = Arguments.createMap()
        params.putArray("displayUnits", CleverTapUtils.getWritableArrayFromDisplayUnitList(units))
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_ON_DISPLAY_UNITS_LOADED, params)
    }

    // CTProductConfigListener
    override fun onActivated() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE,
            Arguments.createMap()
        )
    }

    // CTProductConfigListener
    override fun onFetched() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_FETCH,
            Arguments.createMap()
        )
    }

    // CTProductConfigListener
    override fun onInit() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE,
            Arguments.createMap()
        )
    }

    override fun featureFlagsUpdated() {
        // passing an empty map
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_FEATURE_FLAGS_DID_UPDATE,
            Arguments.createMap()
        )
    }

    // CTPushNotificationListener
    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
        CleverTapEventEmitter.emit(
            CleverTapEvent.CLEVERTAP_PUSH_NOTIFICATION_CLICKED,
            CleverTapUtils.getWritableMapFromMap(payload)
        )
    }

    // PushPermissionResponseListener
    override fun onPushPermissionResponse(accepted: Boolean) {
        Log.i(
            LOG_TAG, "onPushPermissionResponse result: $accepted"
        )
        val params = Arguments.createMap()
        params.putBoolean("accepted", accepted)
        CleverTapEventEmitter.emit(CleverTapEvent.CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE, params)
    }
}
