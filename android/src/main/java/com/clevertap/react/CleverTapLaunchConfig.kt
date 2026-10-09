package com.clevertap.react

import com.clevertap.android.sdk.CleverTapInstanceConfig

/**
 * One account to create at app launch, passed to
 * [CleverTapRnAPI.initReactNativeIntegration].
 *
 * Why this exists: an account created from JavaScript only comes to life when the
 * JS bundle runs (~2 seconds into a cold start). Events that fire before that —
 * most importantly the push tap that LAUNCHED the app — are lost for that account.
 * Listing the account here creates it at process start instead, so those events
 * are caught and buffered until JS attaches its listeners.
 *
 * On the JS side, pick these accounts up with `CleverTap.getInstance(accountId)` —
 * do NOT pass a config again from JS; the one given here is the single source of
 * truth.
 *
 * @param config the account's creation config (id, token, region, ...).
 * @param cleverTapID optional CUSTOM CleverTap ID — the app's own identifier for
 * this device/user (e.g. your customer id "CUST-88231") used INSTEAD of the
 * SDK-generated one. Only meaningful when [config] has
 * `enableCustomCleverTapId = true`, and only applicable at creation time (identity
 * is fixed when the instance is born) — which is why it lives here and has no
 * setter anywhere else.
 */
class CleverTapLaunchConfig @JvmOverloads constructor(
    val config: CleverTapInstanceConfig,
    val cleverTapID: String? = null
)
