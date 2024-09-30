package com.clevertap.react

import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.Application

open class CleverTapApplication : Application() {
    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        CleverTapRnAPI.initReactNativeIntegration(this)
    }
}
