package com.clevertap.react

import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.clevertap.android.sdk.Application

open class CleverTapApplication : Application() {

    /**
     * Override to name the accounts that must exist at app launch (e.g. accounts that
     * receive pushes — the tap that cold-starts the app fires before JS can create
     * them). See [CleverTapLaunchConfig] for what to return; the default is "none",
     * which keeps existing apps byte-for-byte unchanged.
     *
     * Example:
     * ```
     * override fun launchConfigs() = listOf(
     *     CleverTapLaunchConfig(CleverTapInstanceConfig.createInstance(this, "B-ID", "B-TOKEN", "in1"))
     * )
     * ```
     */
    open fun launchConfigs(): List<CleverTapLaunchConfig> = emptyList()

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        CleverTapRnAPI.initReactNativeIntegration(this, launchConfigs())
    }
}
