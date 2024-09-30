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
     * Notify the CleverTap SDK of a launch deep link used in the application.
     *
     * @param uri The deep link as a [Uri]. This is usually the
     * [data][android.content.Intent.getData] of
     * [Activity.getIntent][android.app.Activity.getIntent].
     */
    @JvmStatic
    fun notifyLaunchDeepLink(uri: Uri?) {
        CleverTapModuleImpl.setInitialUri(uri)
    }
}
