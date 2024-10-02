package com.clevertap.react

import android.content.Context
import android.net.Uri
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapRnAPI {

    /**
     * Initializes the CleverTap SDK for ReactNative. It is recommended to call this method
     * in [Application.onCreate][android.app.Application.onCreate] or extend
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

    /**
     * Set the initial Uri for the CleverTap SDK. This can later be accessed through 
     * CleverTap.getInitialUrl in the ReactNative application.
     *
     * @param uri The initial [Uri]. This is usually the
     * [data][android.content.Intent.getData] of
     * [Activity.getIntent][android.app.Activity.getIntent].
     */
    @JvmStatic
    fun setInitialUri(uri: Uri?) {
        CleverTapModuleImpl.setInitialUri(uri)
    }
}
