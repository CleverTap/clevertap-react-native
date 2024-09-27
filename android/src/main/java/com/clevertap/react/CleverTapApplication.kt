package com.clevertap.react

import com.clevertap.android.sdk.Application

open class CleverTapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CleverTapRnAPI.initReactNativeIntegration(this)
    }
}
