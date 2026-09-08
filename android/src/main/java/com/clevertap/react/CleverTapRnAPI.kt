package com.clevertap.react

import android.content.Context
import android.net.Uri
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI

object CleverTapRnAPI {

    /**
     * Initializes the CleverTap SDK for ReactNative. It is recommended to call this method
     * in [Application.onCreate][android.app.Application.onCreate] or extend
     * [CleverTapApplication] to ensure proper initialization.
     *
     * @param context the Android context. Pass the [Application][android.app.Application]
     * itself (i.e. `this` from `Application.onCreate`) or another application-scoped
     * context — the SDK keeps the context it is given for the life of the process, so an
     * Activity context here would leak that Activity.
     * @param launchConfigs optional list of accounts to create RIGHT NOW, at process start.
     * An account created from JS only exists once the JS bundle runs (~2s into a cold
     * start); events firing before that — e.g. the push tap that launched the app — are
     * lost for it. Accounts listed here get the same protection the default (manifest)
     * account has: created before any Activity runs, listeners attached, early events
     * buffered until JS is ready. On the JS side use `CleverTap.getInstance(accountId)`
     * for these accounts (no config again — the one given here is the source of truth).
     * Accounts NOT listed here must be created from JS with `createInstance(config)` as
     * their first touch each run; they cannot receive launch-time events.
     */
    @JvmStatic
    @JvmOverloads // existing callers with no second argument keep compiling — zero migration
    fun initReactNativeIntegration(context: Context, launchConfigs: List<CleverTapLaunchConfig> = emptyList()) {
        // Create SDK instance and attach the proxy listener right away. This ensures events begin
        // to be handled immediately while the react module might not yet initialized
        CleverTapAPI.getDefaultInstance(context)?.let {
            CleverTapListenerProxy.attachToInstance(it)
        }

        // The launch accounts are created HERE, on the MAIN thread, deliberately:
        // 1. Android guarantees Application.onCreate finishes before any Activity, Service
        //    or Receiver of the app runs — so these instances provably exist before a
        //    push tap (or an FCM token refresh) is processed. A background thread turns
        //    that guarantee into a coin flip and can lose the very event we're here for.
        // 2. CleverTapAPI.instanceWithConfig is NOT safe to call off the main thread: its
        //    get→new→put on the static instances map has no lock, and while an instance
        //    is being built the SDK posts a device-ID callback to the MAIN thread that
        //    re-enters instanceWithConfig (DeviceInfo, "callback on main thread"). Built
        //    from a background thread, that callback can find the map still empty and
        //    construct a SECOND instance of the same account — two event queues, split
        //    events. On the main thread the looper serializes us with that callback.
        // The launch-time cost is small: the SDK runs its heavy work (device ID, prefs,
        // DB) on its own executors; the synchronous part is the same object construction
        // the default account above already pays.
        for (launch in launchConfigs) {
            try {
                val accountId = launch.config.accountId
                if (!launch.cleverTapID.isNullOrBlank() && !launch.config.enableCustomCleverTapId) {
                    // Without this, core ignores the passed ID with no message at all.
                    Log.w(Constants.REACT_MODULE_NAME,
                        "cleverTapID given for '$accountId' but enableCustomCleverTapId is false — " +
                            "the ID will be IGNORED and the SDK will generate its own")
                }
                // Same branching as the JS createInstance path (CleverTapModuleImpl):
                // use the 3-arg overload only when an ID was actually supplied, so the
                // no-custom-ID path stays exactly today's behavior.
                val instance = if (!launch.cleverTapID.isNullOrBlank()) {
                    CleverTapAPI.instanceWithConfig(context, launch.config, launch.cleverTapID)
                } else {
                    CleverTapAPI.instanceWithConfig(context, launch.config)
                }
                if (instance != null) {
                    CleverTapListenerProxy.attachToInstance(instance) // idempotent, safe to re-attach
                } else {
                    Log.w(Constants.REACT_MODULE_NAME,
                        "Launch config for '$accountId' produced no instance, skipping")
                }
            } catch (t: Throwable) {
                // Instance creation can throw (e.g. a registered custom template producer
                // raising on a duplicate name). One bad config must never crash app launch
                // or take the remaining accounts down with it.
                Log.w(Constants.REACT_MODULE_NAME,
                    "Failed to create launch instance for '${launch.config.accountId}', skipping", t)
            }
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
