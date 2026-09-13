package com.clevertap.react

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType

/**
 * The validated input of `createInstance(config)`, read from the JS object BEFORE any native
 * SDK object is built. Pure data — no Context, no CleverTapAPI — so it is unit tested on the
 * JVM with JavaOnlyMap (see InstanceConfigRequestTest).
 *
 * Reading rules (the iOS bridge applies the same two):
 *  - a missing key or a JS `null` means "not set" — a server-provided config such as
 *    `{"analyticsOnly": null}` is a normal input;
 *  - a value of the wrong type is a developer mistake and throws [InvalidConfigException],
 *    which createInstance turns into an EINVALID promise rejection naming the field.
 *
 * Why the map is not read where it is used: `ReadableMap.getBoolean` on a JS null or on a
 * string throws inside React Native (NullPointerException / UnexpectedNativeTypeException).
 * Those reads used to run on the main-thread creation task, where an uncaught throw crashes
 * the app instead of rejecting the promise.
 */
class InstanceConfigRequest private constructor(
    val accountId: String,
    val accountToken: String,
    /** Non-blank region, or null for "no region" (the factory call without region). */
    val region: String?,
    val proxyDomain: String?,
    val spikyProxyDomain: String?,
    val identityKeys: List<String>?,
    val handshakeDomain: String?,
    val logLevel: String?,
    val analyticsOnly: Boolean?,
    val enablePersonalization: Boolean?,
    val disableAppLaunchedEvent: Boolean?,
    val encryptionLevel: String?,
    val encryptionInTransit: Boolean?,
    /** null = not given; the SDK config keeps its manifest-derived default then. */
    val useCustomCleverTapId: Boolean?,
    /** Non-blank custom id, or null. Non-null implies `useCustomCleverTapId == true`. */
    val cleverTapId: String?,
    val useGoogleAdId: Boolean?,
    val backgroundSync: Boolean?,
    val pushProviders: List<PushProvider>,
) {

    /** One extra push provider; all four parts are required by the native PushType contract. */
    class PushProvider(
        val type: String,
        val prefKey: String,
        val className: String,
        val messagingSDKClassName: String,
    )

    /** A config value that cannot be accepted. The message names the offending field. */
    class InvalidConfigException(message: String) : IllegalArgumentException(message)

    companion object {

        @JvmStatic
        @Throws(InvalidConfigException::class)
        fun parse(config: ReadableMap?): InstanceConfigRequest {
            if (config == null) {
                throw InvalidConfigException("a config object is required")
            }
            val accountId = optString(config, "accountId")
            val accountToken = optString(config, "accountToken")
            // Reject EMPTY as well as missing: the native SDK only null-checks, so an empty
            // string would create a "zombie" instance whose events go nowhere while every
            // call looks successful.
            if (accountId.isNullOrBlank() || accountToken.isNullOrBlank()) {
                throw InvalidConfigException("accountId and accountToken must be non-empty strings")
            }

            // A custom CleverTap ID can only be supplied AT CREATION, and only works together
            // with the useCustomCleverTapId flag. The native SDK does not fail on a mismatch:
            // an ID without the flag is IGNORED (a random SDK id is generated, the app's id is
            // lost), and the flag without an ID leaves the account on an "error device id".
            // Both only surface as a native debug log that a React Native developer never
            // sees, and identity cannot be repaired later from RN — so reject up front.
            val useCustomCleverTapId = optBoolean(config, "useCustomCleverTapId")
            val cleverTapId = optString(config, "cleverTapId")?.takeIf { it.isNotBlank() }
            if ((useCustomCleverTapId == true) != (cleverTapId != null)) {
                throw InvalidConfigException(
                    "cleverTapId and useCustomCleverTapId: true must be given together (or both" +
                        " left out) — the native SDK ignores an ID without the flag, and the flag" +
                        " without an ID leaves the account with an error device id"
                )
            }

            // Platform-specific options live in nested blocks; each platform reads only its
            // own block (the "ios" block is intentionally ignored here).
            val android = optMap(config, "android")

            return InstanceConfigRequest(
                accountId = accountId,
                accountToken = accountToken,
                region = optString(config, "region")?.takeIf { it.isNotBlank() },
                proxyDomain = optString(config, "proxyDomain"),
                spikyProxyDomain = optString(config, "spikyProxyDomain"),
                identityKeys = optStringList(config, "identityKeys"),
                handshakeDomain = optString(config, "handshakeDomain"),
                logLevel = optString(config, "logLevel"),
                analyticsOnly = optBoolean(config, "analyticsOnly"),
                enablePersonalization = optBoolean(config, "enablePersonalization"),
                disableAppLaunchedEvent = optBoolean(config, "disableAppLaunchedEvent"),
                encryptionLevel = optString(config, "encryptionLevel"),
                encryptionInTransit = optBoolean(config, "encryptionInTransit"),
                useCustomCleverTapId = useCustomCleverTapId,
                cleverTapId = cleverTapId,
                useGoogleAdId = android?.let { optBoolean(it, "useGoogleAdId", "android.") },
                backgroundSync = android?.let { optBoolean(it, "backgroundSync", "android.") },
                pushProviders = android?.let { parsePushProviders(it) } ?: emptyList(),
            )
        }

        // ---- Typed readers. Absent key or JS null -> null. Wrong type -> InvalidConfigException.
        // `prefix` names the enclosing block in the message, e.g. "android.".

        private fun typeOf(map: ReadableMap, key: String): ReadableType? =
            // getType itself throws NoSuchKeyException on a missing key, hence hasKey first.
            if (map.hasKey(key)) map.getType(key) else null

        private fun optBoolean(map: ReadableMap, key: String, prefix: String = ""): Boolean? =
            when (typeOf(map, key)) {
                null, ReadableType.Null -> null
                ReadableType.Boolean -> map.getBoolean(key)
                else -> throw InvalidConfigException("$prefix$key must be a boolean")
            }

        private fun optString(map: ReadableMap, key: String, prefix: String = ""): String? =
            when (typeOf(map, key)) {
                null, ReadableType.Null -> null
                ReadableType.String -> map.getString(key)
                else -> throw InvalidConfigException("$prefix$key must be a string")
            }

        private fun optMap(map: ReadableMap, key: String): ReadableMap? =
            when (typeOf(map, key)) {
                null, ReadableType.Null -> null
                ReadableType.Map -> map.getMap(key)
                else -> throw InvalidConfigException("$key must be an object")
            }

        private fun optArray(map: ReadableMap, key: String, prefix: String = ""): ReadableArray? =
            when (typeOf(map, key)) {
                null, ReadableType.Null -> null
                ReadableType.Array -> map.getArray(key)
                else -> throw InvalidConfigException("$prefix$key must be an array")
            }

        private fun optStringList(map: ReadableMap, key: String): List<String>? {
            val array = optArray(map, key) ?: return null
            return List(array.size()) { index ->
                if (array.getType(index) != ReadableType.String) {
                    throw InvalidConfigException("$key must be an array of strings")
                }
                checkNotNull(array.getString(index))
            }
        }

        private fun requiredString(map: ReadableMap, key: String, prefix: String): String =
            optString(map, key, prefix)?.takeIf { it.isNotBlank() }
                ?: throw InvalidConfigException("$prefix$key is required")

        private fun parsePushProviders(android: ReadableMap): List<PushProvider> {
            val array = optArray(android, "pushProviders", "android.") ?: return emptyList()
            return List(array.size()) { index ->
                val item = "android.pushProviders[$index]"
                if (array.getType(index) != ReadableType.Map) {
                    throw InvalidConfigException("$item must be an object")
                }
                val provider = checkNotNull(array.getMap(index))
                PushProvider(
                    type = requiredString(provider, "type", "$item."),
                    prefKey = requiredString(provider, "prefKey", "$item."),
                    className = requiredString(provider, "className", "$item."),
                    messagingSDKClassName = requiredString(provider, "messagingSDKClassName", "$item."),
                )
            }
        }
    }
}
