package com.clevertap.react

import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

/**
 * JVM tests for the account-aware event buffers.
 *
 * The race test is the regression guard for SDK-6021: production crashed with
 * NoSuchElementException at LinkedList.removeFirst because Buffer.add() ran with no
 * lock while flushBuffer() drained under one. Emits arrive on SDK callback threads
 * (usually main) while arm/flush runs on the NativeModules thread — these tests play
 * both roles on plain JVM threads.
 */
class CleverTapEventEmitterTest {

    /** Records everything the emitter hands to React Native. */
    private class RecordingEmitter : DeviceEventManagerModule.RCTDeviceEventEmitter {
        val events = CopyOnWriteArrayList<Pair<String, Any?>>()
        override fun emit(eventName: String, data: Any?) {
            events.add(eventName to data)
        }
    }

    /** A ReactContext whose only job is returning the recording emitter. */
    private class TestReactContext(private val recorder: RecordingEmitter) : ReactContext(null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : JavaScriptModule> getJSModule(jsInterface: Class<T>): T = recorder as T
    }

    private lateinit var recorder: RecordingEmitter

    private val bufferableEvent = CleverTapEvent.CLEVERTAP_PROFILE_DID_INITIALIZE
    private val nonBufferableEvent = CleverTapEvent.CLEVERTAP_PROFILE_SYNC

    private fun tagged(accountId: String): JavaOnlyMap =
        JavaOnlyMap.of(Constants.CT_ACCOUNT_ID_KEY, accountId)

    @Before
    fun setUp() {
        recorder = RecordingEmitter()
        CleverTapEventEmitter.reactContext = TestReactContext(recorder)
        // The emitter is a singleton object: start every test with clean, enabled buffers.
        CleverTapEventEmitter.resetAllBuffers(true)
    }

    /**
     * SDK-6021 regression: a writer thread (plays the SDK callback on main) hammers
     * emit() while a drainer thread (plays JS addListener on the NativeModules thread)
     * hammers flushBuffer(). The unfixed code throws NoSuchElementException within
     * milliseconds; the fixed code must survive AND deliver every payload exactly once.
     */
    @Test
    fun concurrentEmitAndFlush_neverThrows_andDeliversEveryPayloadOnce() {
        val total = 50_000
        val writerFailure = AtomicReference<Throwable>()
        val drainerFailure = AtomicReference<Throwable>()

        val writer = thread(name = "sdk-callback-thread") {
            try {
                repeat(total) { CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B")) }
            } catch (t: Throwable) {
                writerFailure.set(t)
            }
        }
        val drainer = thread(name = "native-modules-thread") {
            try {
                repeat(total) { CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_B") }
            } catch (t: Throwable) {
                drainerFailure.set(t)
            }
        }
        writer.join()
        drainer.join()
        // Recover whatever is still buffered, then check integrity.
        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_B")

        assertNull("writer thread threw: ${writerFailure.get()}", writerFailure.get())
        assertNull("drainer thread threw: ${drainerFailure.get()}", drainerFailure.get())
        assertEquals("every payload must reach JS exactly once", total, recorder.events.size)
    }

    @Test
    fun flushBuffer_deliversOwnAccountAndUntagged_keepsOtherAccounts() {
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B"))
        CleverTapEventEmitter.emit(bufferableEvent, null) // untagged = global

        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_B")
        // B's payload + the untagged one; A's stays buffered.
        assertEquals(2, recorder.events.size)

        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_A")
        assertEquals(3, recorder.events.size)

        // Nothing left: another flush delivers nothing.
        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_A")
        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_B")
        assertEquals(3, recorder.events.size)
    }

    @Test
    fun emit_afterArm_sendsImmediatelyForThatAccountOnly() {
        CleverTapEventEmitter.armAccount(bufferableEvent, "ACCT_B")

        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B"))
        assertEquals("armed account's payload must not be buffered", 1, recorder.events.size)

        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        assertEquals("unarmed account's payload must stay buffered", 1, recorder.events.size)
    }

    @Test
    fun resetAllBuffers_discardsBufferedPayloads_andDisablesBuffering() {
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        CleverTapEventEmitter.resetAllBuffers(false) // the 5-second safety valve

        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ACCT_A")
        assertEquals("discarded payloads must not be delivered", 0, recorder.events.size)

        // Buffering disabled: everything sends immediately, armed or not.
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        assertEquals(1, recorder.events.size)
    }

    @Test
    fun nonBufferableEvent_alwaysSendsImmediately() {
        CleverTapEventEmitter.emit(nonBufferableEvent, tagged("ACCT_A"))
        assertEquals(1, recorder.events.size)
        assertEquals(nonBufferableEvent.eventName, recorder.events[0].first)
    }

    @Test
    fun nullPayload_isBufferedAsGlobal_andDeliveredOnce() {
        CleverTapEventEmitter.emit(bufferableEvent, null)
        assertEquals(0, recorder.events.size)

        CleverTapEventEmitter.flushBuffer(bufferableEvent, "ANY_ACCOUNT")
        assertEquals(1, recorder.events.size)
        assertTrue("null payload must arrive as null", recorder.events[0].second == null)
    }
}
