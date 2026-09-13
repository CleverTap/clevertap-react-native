package com.clevertap.react

import com.clevertap.react.InstanceConfigRequest.InvalidConfigException
import com.facebook.react.bridge.JavaOnlyArray
import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.ReadableMap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * JVM tests for the createInstance(config) parser. JavaOnlyMap plays the role of the map
 * React Native hands to the bridge; a JS `null` is a null value under a present key.
 *
 * The rules under test: absent or null = "not set", wrong type = EINVALID (named field),
 * and the cleverTapId / useCustomCleverTapId pairing.
 */
class InstanceConfigRequestTest {

    /** A minimal valid config plus any extra key/value pairs. */
    private fun config(vararg extra: Any?): JavaOnlyMap =
        JavaOnlyMap.of("accountId", "ACCT_B", "accountToken", "TOK_B", *extra)

    private fun assertRejects(fieldName: String, config: ReadableMap?) {
        val error = assertThrows(InvalidConfigException::class.java) {
            InstanceConfigRequest.parse(config)
        }
        assertTrue(
            "message must name the field '$fieldName' but was: ${error.message}",
            error.message!!.contains(fieldName)
        )
    }

    @Test
    fun parse_fullConfig_readsEveryField() {
        val request = InstanceConfigRequest.parse(
            config(
                "region", "eu1",
                "proxyDomain", "proxy.example.com",
                "spikyProxyDomain", "spiky.example.com",
                "identityKeys", JavaOnlyArray.of("Email", "Identity"),
                "handshakeDomain", "hs.example.com",
                "logLevel", "debug",
                "analyticsOnly", true,
                "enablePersonalization", false,
                "disableAppLaunchedEvent", true,
                "encryptionLevel", "medium",
                "encryptionInTransit", true,
                "useCustomCleverTapId", true,
                "cleverTapId", "CUST-88231",
                "android", JavaOnlyMap.of(
                    "useGoogleAdId", true,
                    "backgroundSync", false,
                    "pushProviders", JavaOnlyArray.of(
                        JavaOnlyMap.of(
                            "type", "hps", "prefKey", "hps_token",
                            "className", "com.example.Hps", "messagingSDKClassName", "com.example.HpsSdk"
                        )
                    )
                ),
                "ios", JavaOnlyMap.of("disableIDFV", true) // iOS-only block: ignored here
            )
        )

        assertEquals("ACCT_B", request.accountId)
        assertEquals("TOK_B", request.accountToken)
        assertEquals("eu1", request.region)
        assertEquals("proxy.example.com", request.proxyDomain)
        assertEquals("spiky.example.com", request.spikyProxyDomain)
        assertEquals(listOf("Email", "Identity"), request.identityKeys)
        assertEquals("hs.example.com", request.handshakeDomain)
        assertEquals("debug", request.logLevel)
        assertEquals(true, request.analyticsOnly)
        assertEquals(false, request.enablePersonalization)
        assertEquals(true, request.disableAppLaunchedEvent)
        assertEquals("medium", request.encryptionLevel)
        assertEquals(true, request.encryptionInTransit)
        assertEquals(true, request.useCustomCleverTapId)
        assertEquals("CUST-88231", request.cleverTapId)
        assertEquals(true, request.useGoogleAdId)
        assertEquals(false, request.backgroundSync)
        assertEquals(1, request.pushProviders.size)
        assertEquals("hps", request.pushProviders[0].type)
        assertEquals("hps_token", request.pushProviders[0].prefKey)
        assertEquals("com.example.Hps", request.pushProviders[0].className)
        assertEquals("com.example.HpsSdk", request.pushProviders[0].messagingSDKClassName)
    }

    @Test
    fun parse_minimalConfig_leavesEveryOptionalFieldUnset() {
        val request = InstanceConfigRequest.parse(config())

        assertNull(request.region)
        assertNull(request.proxyDomain)
        assertNull(request.identityKeys)
        assertNull(request.logLevel)
        assertNull(request.analyticsOnly)
        assertNull(request.enablePersonalization)
        assertNull(request.useCustomCleverTapId)
        assertNull(request.cleverTapId)
        assertNull(request.useGoogleAdId)
        assertTrue(request.pushProviders.isEmpty())
    }

    /** The case that used to crash the app: a JS null under a present key. */
    @Test
    fun parse_nullValues_readAsNotSet() {
        val request = InstanceConfigRequest.parse(
            config(
                "region", null,
                "identityKeys", null,
                "analyticsOnly", null,
                "encryptionInTransit", null,
                "useCustomCleverTapId", null,
                "cleverTapId", null,
                "android", null
            )
        )

        assertNull(request.region)
        assertNull(request.identityKeys)
        assertNull(request.analyticsOnly)
        assertNull(request.encryptionInTransit)
        assertNull(request.useCustomCleverTapId)
        assertNull(request.cleverTapId)
        assertNull(request.useGoogleAdId)
        assertTrue(request.pushProviders.isEmpty())
    }

    @Test
    fun parse_wrongTypes_areRejectedNamingTheField() {
        assertRejects("analyticsOnly", config("analyticsOnly", "true"))
        assertRejects("analyticsOnly", config("analyticsOnly", 1))
        assertRejects("accountId", JavaOnlyMap.of("accountId", 12345, "accountToken", "TOK_B"))
        assertRejects("region", config("region", true))
        assertRejects("identityKeys", config("identityKeys", "Email"))
        assertRejects("identityKeys", config("identityKeys", JavaOnlyArray.of("Email", 7)))
        assertRejects("android", config("android", "not-an-object"))
        assertRejects("android.useGoogleAdId", config("android", JavaOnlyMap.of("useGoogleAdId", "yes")))
        assertRejects("android.pushProviders", config("android", JavaOnlyMap.of("pushProviders", "fcm")))
    }

    @Test
    fun parse_missingOrBlankCredentials_areRejected() {
        assertRejects("config object", null)
        assertRejects("accountId", JavaOnlyMap.of())
        assertRejects("accountId", JavaOnlyMap.of("accountId", " ", "accountToken", "TOK_B"))
        assertRejects("accountToken", JavaOnlyMap.of("accountId", "ACCT_B", "accountToken", ""))
        assertRejects("accountToken", JavaOnlyMap.of("accountId", "ACCT_B"))
    }

    @Test
    fun parse_cleverTapIdAndFlag_mustComeTogether() {
        // ID without the flag: the native SDK would silently ignore the ID.
        assertRejects("useCustomCleverTapId", config("cleverTapId", "CUST-1"))
        assertRejects("useCustomCleverTapId", config("cleverTapId", "CUST-1", "useCustomCleverTapId", false))
        // Flag without an ID: the account would be left on an error device id.
        assertRejects("cleverTapId", config("useCustomCleverTapId", true))
        assertRejects("cleverTapId", config("useCustomCleverTapId", true, "cleverTapId", "  "))

        // Both, or neither, are fine.
        val both = InstanceConfigRequest.parse(config("useCustomCleverTapId", true, "cleverTapId", "CUST-1"))
        assertEquals(true, both.useCustomCleverTapId)
        assertEquals("CUST-1", both.cleverTapId)
        val neither = InstanceConfigRequest.parse(config("useCustomCleverTapId", false))
        assertEquals(false, neither.useCustomCleverTapId)
        assertNull(neither.cleverTapId)
    }

    @Test
    fun parse_blankRegion_readsAsNoRegion() {
        assertNull(InstanceConfigRequest.parse(config("region", "  ")).region)
        assertEquals("in1", InstanceConfigRequest.parse(config("region", "in1")).region)
    }

    @Test
    fun parse_pushProviderMissingAPart_isRejected() {
        val incomplete = JavaOnlyMap.of("type", "hps", "prefKey", "hps_token", "className", "com.example.Hps")
        assertRejects(
            "android.pushProviders[0].messagingSDKClassName",
            config("android", JavaOnlyMap.of("pushProviders", JavaOnlyArray.of(incomplete)))
        )
        assertRejects(
            "android.pushProviders[0]",
            config("android", JavaOnlyMap.of("pushProviders", JavaOnlyArray.of("fcm")))
        )
    }

    @Test
    fun parse_emptyIdentityKeys_isKeptEmpty_notNull() {
        val request = InstanceConfigRequest.parse(config("identityKeys", JavaOnlyArray.of()))
        assertEquals(emptyList<String>(), request.identityKeys)
        assertFalse(request.identityKeys!!.isNotEmpty())
    }
}
