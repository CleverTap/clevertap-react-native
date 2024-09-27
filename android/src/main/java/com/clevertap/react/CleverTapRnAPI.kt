package com.clevertap.react

import android.content.Context
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapRnAPI {

    /**
     * Initializes the CleverTap SDK for ReactNative. It is recommended to call this method
     * in [Application.onCreate][com.clevertap.android.sdk.Application.onCreate] or extend
     * [CleverTapApplication] to ensure proper initialization.
     */
    @JvmStatic
    fun initReactNativeIntegration(context: Context) {
        // Create SDK instance and attach the proxy listener right away. This ensures events begin
        // to be handled immediately while the react module might not yet initialized
        CleverTapAPI.getDefaultInstance(context)?.let {
            CleverTapListenerProxy.attachToInstance(it)
        }
    }
}
