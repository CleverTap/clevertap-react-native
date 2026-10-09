package com.clevertap.react

import com.facebook.react.bridge.JavaOnlyMap
import com.facebook.react.bridge.JavaScriptModule
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
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
 * The emitter confines all buffer work to one worker thread. Emits arrive on SDK callback
 * threads (usually main) while arm/flush arrives from the NativeModules thread — these tests
 * play both roles on plain JVM threads, then call [CleverTapEventEmitter.awaitIdle] so the
 * worker has finished before anything is asserted.
 *
 * Two regression guards live here:
 *  - SDK-6021: production crashed with NoSuchElementException at LinkedList.removeFirst when
 *    an unsynchronized add raced a drain. [concurrentEmitAndFlush_neverThrows_andDeliversEveryPayloadOnce]
 *  - Ordering: a listener attaching mid-stream must not let a live event overtake the older
 *    buffered events of its account. [deliveredInEmitOrder_evenWhenListenerAttachesMidStream]
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

    private fun tagged(accountId: String, seq: Int): JavaOnlyMap =
        JavaOnlyMap.of(Constants.CT_ACCOUNT_ID_KEY, accountId, "seq", seq)

    /** Waits for the worker, then returns what React Native received so far. */
    private fun delivered(): List<Pair<String, Any?>> {
        CleverTapEventEmitter.awaitIdle()
        return recorder.events.toList()
    }

    @Before
    fun setUp() {
        recorder = RecordingEmitter()
        CleverTapEventEmitter.reactContext = TestReactContext(recorder)
        // The emitter is a singleton object: start every test with clean, enabled buffers.
        CleverTapEventEmitter.resetAllBuffers(true)
        CleverTapEventEmitter.awaitIdle()
    }

    /**
     * SDK-6021 regression: a writer thread (plays the SDK callback on main) hammers
     * emit() while a drainer thread (plays JS addListener on the NativeModules thread)
     * hammers armAndFlush(). The unfixed code threw NoSuchElementException within
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
                repeat(total) { CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B") }
            } catch (t: Throwable) {
                drainerFailure.set(t)
            }
        }
        writer.join()
        drainer.join()
        // Recover whatever is still buffered, then check integrity.
        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")

        assertNull("writer thread threw: ${writerFailure.get()}", writerFailure.get())
        assertNull("drainer thread threw: ${drainerFailure.get()}", drainerFailure.get())
        assertEquals("every payload must reach JS exactly once", total, delivered().size)
    }

    /**
     * Ordering guard: payloads 0..N are emitted from one SDK thread while the listener
     * attaches (armAndFlush) in the middle of the stream. Whatever was buffered before the
     * attach must be delivered BEFORE anything emitted after it — JS must see 0, 1, 2, ... N
     * with no inversion.
     *
     * The dangerous moment exists only around an account's FIRST arm, so the test resets the
     * buffers and replays that moment many times. With the old two-step arm-then-flush this
     * caught a live event delivered ahead of the older buffered ones within a few rounds.
     */
    @Test
    fun deliveredInEmitOrder_evenWhenListenerAttachesMidStream() {
        val rounds = 300
        val perRound = 2_000
        for (round in 0 until rounds) {
            CleverTapEventEmitter.resetAllBuffers(true)
            recorder.events.clear()
            CleverTapEventEmitter.awaitIdle()

            val writer = thread(name = "sdk-callback-thread") {
                for (seq in 0 until perRound) {
                    CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B", seq))
                }
            }
            // Attach while the writer is mid-stream (a short, varying spin lands the attach at a
            // different point of the burst every round).
            val spin = (round % 50) * 200
            var sink = 0
            repeat(spin) { sink += it }
            CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")
            writer.join()
            CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")

            val sequence = delivered().map { (it.second as ReadableMap).getInt("seq") }
            assertEquals("round $round: every payload must reach JS exactly once", perRound, sequence.size)
            assertEquals("round $round: payloads must reach JS in emit order (sink=$sink)",
                (0 until perRound).toList(), sequence)
        }
    }

    @Test
    fun armAndFlush_deliversOwnAccountAndUntagged_keepsOtherAccounts() {
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B"))
        CleverTapEventEmitter.emit(bufferableEvent, null) // untagged = global

        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")
        // B's payload + the untagged one; A's stays buffered.
        assertEquals(2, delivered().size)

        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_A")
        assertEquals(3, delivered().size)

        // Nothing left: another flush delivers nothing.
        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_A")
        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")
        assertEquals(3, delivered().size)
    }

    @Test
    fun emit_afterArm_sendsImmediatelyForThatAccountOnly() {
        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_B")

        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_B"))
        assertEquals("armed account's payload must not be buffered", 1, delivered().size)

        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        assertEquals("unarmed account's payload must stay buffered", 1, delivered().size)
    }

    @Test
    fun resetAllBuffers_discardsBufferedPayloads_andDisablesBuffering() {
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        CleverTapEventEmitter.resetAllBuffers(false) // the 5-second safety valve

        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ACCT_A")
        assertEquals("discarded payloads must not be delivered", 0, delivered().size)

        // Buffering disabled: everything sends immediately, armed or not.
        CleverTapEventEmitter.emit(bufferableEvent, tagged("ACCT_A"))
        assertEquals(1, delivered().size)
    }

    @Test
    fun nonBufferableEvent_alwaysSendsImmediately() {
        CleverTapEventEmitter.emit(nonBufferableEvent, tagged("ACCT_A"))
        val events = delivered()
        assertEquals(1, events.size)
        assertEquals(nonBufferableEvent.eventName, events[0].first)
    }

    @Test
    fun nullPayload_isBufferedAsGlobal_andDeliveredOnce() {
        CleverTapEventEmitter.emit(bufferableEvent, null)
        assertEquals(0, delivered().size)

        CleverTapEventEmitter.armAndFlush(bufferableEvent, "ANY_ACCOUNT")
        val events = delivered()
        assertEquals(1, events.size)
        assertTrue("null payload must arrive as null", events[0].second == null)
    }
}
