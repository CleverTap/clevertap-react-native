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
import com.facebook.react.bridge.WritableMap
import org.json.JSONException
import org.json.JSONObject

/**
 * One listener proxy PER CleverTap account (the default account included). Each proxy knows
 * its own account id and stamps it into every event payload it emits, so JS can route the
 * event to the right account handle.
 *
 * Example: the proxy for account "ACCT_B" turns a profile-init callback into a payload
 * `{CleverTapID: "xyz", __ctAccountId: "ACCT_B"}`; only the "ACCT_B" JS handle's listeners
 * receive it (and the tag is stripped before user code runs).
 */
class CleverTapListenerProxy private constructor(private val accountId: String) : SyncListener,
    InAppNotificationListener, CTInboxListener, InboxMessageButtonListener, InboxMessageListener,
    InAppNotificationButtonListener, DisplayUnitListener, CTProductConfigListener,
    CTFeatureFlagsListener, CTPushNotificationListener, PushPermissionResponseListener {

    companion object {

        private const val LOG_TAG = Constants.REACT_MODULE_NAME

        // ⚠️ LOAD-BEARING: this map is the ONLY strong reference to the per-account proxies.
        // The native SDK holds several listeners as WeakReference (DisplayUnitListener,
        // InAppNotificationButtonListener, CTFeatureFlagsListener, CTProductConfigListener in
        // CallbackManager). The old singleton `object` proxy could never be garbage-collected;
        // these per-account instances CAN. Delete this map and those callbacks die silently
        // after the next garbage collection — it will pass QA and fail in production.
        private val proxies = mutableMapOf<String, CleverTapListenerProxy>()

        /**
         * Attach a proxy to the given instance, creating one per account on first use.
         * Attaching again with the same account reuses the same proxy (safe to call twice).
         */
        @JvmStatic
        fun attachToInstance(instance: CleverTapAPI) {
            val accountId = instance.accountId
            if (accountId == null) {
                Log.e(LOG_TAG, "Cannot attach listeners: instance has no accountId")
                return
            }
            val proxy = synchronized(proxies) {
                proxies.getOrPut(accountId) { CleverTapListenerProxy(accountId) }
            }
            proxy.attach(instance)
        }
    }

    // Why @Synchronized? attach() can be reached from two places that may overlap:
    // the host app's launch init (CleverTapRnAPI.initReactNativeIntegration, main
    // thread) and the React module's own init (resolveInstance, bridge thread). The
    // unregister+register pair below is not atomic on its own: if two threads run it
    // at the same time, both unregister first (nothing to remove) and then BOTH
    // register — the proxy ends up in the SDK's push-permission listener list twice,
    // and one tap on the permission dialog fires TWO identical events to JS. The
    // lock is per proxy (per account), guards only these quick listener-list
    // assignments (no I/O, no callbacks), so it can never block anyone noticeably.
    @Synchronized
    private fun attach(instance: CleverTapAPI) {
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

    // The ONE place where the account tag is added — every callback below emits through here.
    private fun emit(event: CleverTapEvent, params: WritableMap = Arguments.createMap()) {
        params.putString(Constants.CT_ACCOUNT_ID_KEY, accountId)
        CleverTapEventEmitter.emit(event, params)
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
        emit(CleverTapEvent.CLEVERTAP_PROFILE_SYNC, params)
    }

    // SyncListener
    override fun profileDidInitialize(cleverTapID: String?) {
        if (cleverTapID == null) {
            Log.d(LOG_TAG, "profileDidInitialize called with cleverTapID=null")
            return
        }
        val params = Arguments.createMap()
        params.putString("CleverTapID", cleverTapID)
        emit(CleverTapEvent.CLEVERTAP_PROFILE_DID_INITIALIZE, params)
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
        emit(CleverTapEvent.CLEVERTAP_IN_APP_NOTIFICATION_SHOWED, params)
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

        emit(CleverTapEvent.CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED, params)
    }

    // CTInboxListener
    override fun inboxDidInitialize() {
        emit(CleverTapEvent.CLEVERTAP_INBOX_DID_INITIALIZE)
    }

    // CTInboxListener
    override fun inboxMessagesDidUpdate() {
        emit(CleverTapEvent.CLEVERTAP_INBOX_MESSAGES_DID_UPDATE)
    }

    // CTInboxListener
    override fun onInboxButtonClick(payload: HashMap<String, String>?) {
        emit(
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
        emit(CleverTapEvent.CLEVERTAP_ON_INBOX_MESSAGE_CLICK, params)
    }

    // InAppNotificationButtonListener
    override fun onInAppButtonClick(payload: HashMap<String, String>?) {
        emit(
            CleverTapEvent.CLEVERTAP_ON_INAPP_BUTTON_CLICK,
            CleverTapUtils.getWritableMapFromMap(payload)
        )
    }

    // DisplayUnitListener
    override fun onDisplayUnitsLoaded(units: ArrayList<CleverTapDisplayUnit>?) {
        val params = Arguments.createMap()
        params.putArray("displayUnits", CleverTapUtils.getWritableArrayFromDisplayUnitList(units))
        emit(CleverTapEvent.CLEVERTAP_ON_DISPLAY_UNITS_LOADED, params)
    }

    // CTProductConfigListener
    override fun onActivated() {
        emit(CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE)
    }

    // CTProductConfigListener
    override fun onFetched() {
        emit(CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_FETCH)
    }

    // CTProductConfigListener
    override fun onInit() {
        emit(CleverTapEvent.CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE)
    }

    override fun featureFlagsUpdated() {
        emit(CleverTapEvent.CLEVERTAP_FEATURE_FLAGS_DID_UPDATE)
    }

    // CTPushNotificationListener
    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>?) {
        emit(
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
        emit(CleverTapEvent.CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE, params)
    }
}
