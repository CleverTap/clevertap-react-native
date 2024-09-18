package com.clevertap.react

import android.util.Log
import com.clevertap.android.sdk.Application
import com.clevertap.android.sdk.CleverTapAPI
import com.clevertap.android.sdk.pushnotification.CTPushNotificationListener

open class CleverTapApplication : Application(), CTPushNotificationListener {
    override fun onCreate() {
        super.onCreate()
        // Workaround when app is in killed state
        CleverTapAPI.getDefaultInstance(this)?.ctPushNotificationListener = this
    }

    //Push Notification Clicked callback workaround when app is in killed state
    override fun onNotificationClickedPayloadReceived(payload: HashMap<String, Any>) {
        Log.e(TAG, "onNotificationClickedPayloadReceived called")
        CleverTapEventEmitter.addToBuffer(
            CleverTapModuleImpl.CLEVERTAP_PUSH_NOTIFICATION_CLICKED,
            CleverTapUtils.getWritableMapFromMap(payload)
        )
    }

    companion object {
        private const val TAG = "CleverTapApplication"
    }
}
