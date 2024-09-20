package com.clevertap.react

import com.clevertap.android.sdk.Application
import com.clevertap.android.sdk.CleverTapAPI

open class CleverTapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Create SDK instance and attach the proxy listener when the app is created. This
        // handles events which are triggered while the CleverTap react module is not yet
        // initialized
        CleverTapAPI.getDefaultInstance(this)?.let {
            CleverTapListenerProxy.attachToInstance(it)
        }
    }
}
