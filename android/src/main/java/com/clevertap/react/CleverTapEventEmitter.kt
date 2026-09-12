package com.clevertap.react

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CleverTapEventEmitter is responsible for emitting events to the React Native JavaScript layer.
 * It manages event buffers and allows events to be queued which is useful when needing to send an
 * event while the ReactContext of the application is not yet initialized or listeners have not
 * attached yet. Only events specified as [CleverTapEvent.bufferable] will be considered for
 * buffering, all other events will be emitted immediately.
 *
 * Buffers are ACCOUNT-AWARE. Every payload carries the account id it belongs to (stamped under
 * [Constants.CT_ACCOUNT_ID_KEY] by [CleverTapListenerProxy]). A listener arms an event only for
 * its own account and receives only that account's buffered payloads ([armAndFlush]), while the
 * other accounts' payloads stay buffered.
 *
 * Why: with two accounts, account B's listener attaching first must not drain and drop account
 * A's buffered events (A's listeners are not attached yet — the delivery would go nowhere and
 * the events would be lost forever, e.g. the push tap that launched the app).
 *
 * Payloads with no account tag are treated as GLOBAL: they go live once ANY account arms the
 * event (same behavior as before for events that are not per-account, like custom templates).
 *
 * ⚠️ THREADING MODEL — thread confinement, no locks. Emits arrive on the native SDK's callback
 * threads (usually main) while arming/flushing arrives from the NativeModules thread. Instead
 * of sharing the buffers between those threads under locks, EVERY operation is posted to one
 * single-thread [worker] and runs there, one after another, in submission order:
 *
 *  - No data race is possible: only the worker thread ever touches a [Buffer]. This is what
 *    prevents the SDK-6021 production crash (NoSuchElementException from an unsynchronized
 *    LinkedList add racing a drain).
 *  - Ordering is preserved: "decide whether to buffer" and "send to React Native" happen in the
 *    SAME task, and a flush sends its whole drained list in ONE task, so a live event can never
 *    overtake older buffered events of the same account (the arm-then-flush window a two-step
 *    API had).
 *  - No deadlock is possible: there are no locks to order. React Native is only ever called from
 *    the worker, never while holding anything.
 *
 * `Executors.newSingleThreadExecutor` guarantees sequential execution in FIFO order, and
 * submitting a task happens-before the task runs (java.util.concurrent memory consistency), so
 * payloads built on the SDK thread are safely visible to the worker. `RCTDeviceEventEmitter.emit`
 * may be called from any thread: it only enqueues the call onto the JS thread.
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    // Written once from the module constructor thread, read on the worker.
    @Volatile
    var reactContext: ReactContext? = null

    // The ONE thread that owns all buffer state. Daemon so an idle worker never keeps a plain
    // JVM (unit tests, tooling) alive after everything else has finished; on Android the OS
    // ends the process regardless. Named for thread dumps and profilers.
    private val worker: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "CleverTapEventEmitter").apply { isDaemon = true }
    }

    // Fixed keys, immutable map, built once: safe to read from any thread. The Buffers inside
    // are only ever touched on the worker.
    private val eventsBuffers: Map<CleverTapEvent, Buffer> =
        CleverTapEvent.values().filter { it.bufferable }.associateWith { Buffer(enabled = true) }

    /**
     * Clear all the buffered events from all buffers and set whether all buffers should be
     * enabled after that. Note: with `enableBuffers = false` this DISCARDS whatever is still
     * buffered — it is the "listeners never showed up" safety valve, not a flush.
     *
     * @param enableBuffers enable/disable all buffers after they are cleared
     */
    fun resetAllBuffers(enableBuffers: Boolean) {
        post("resetAllBuffers") {
            eventsBuffers.values.forEach { buffer -> buffer.reset(enableBuffers) }
            Log.i(LOG_TAG, "Buffers reset and enabled: $enableBuffers")
        }
    }

    /**
     * A listener for the specified account attached to the specified event: from now on that
     * account's payloads are sent immediately, and the payloads it already has buffered — plus
     * any untagged (global) payloads — are sent now, in order. Other accounts' payloads stay
     * buffered until their own listeners attach (or [resetAllBuffers] discards them).
     *
     * Arm and flush are deliberately ONE operation: done as two calls, a same-account event
     * arriving between them would be sent ahead of the older buffered ones.
     *
     * @param accountId the listener's account; null is tolerated and simply never matches a
     * tagged payload.
     */
    fun armAndFlush(event: CleverTapEvent, accountId: String?) {
        val buffer = eventsBuffers[event] ?: return
        post("armAndFlush($event, $accountId)") {
            buffer.arm(accountId)
            val toSend = buffer.drainFor(accountId)
            Log.i(LOG_TAG, "Armed $event for account $accountId, flushing ${toSend.size}")
            toSend.forEach { params -> sendEvent(event, params) }
        }
    }

    /**
     * Emit an event with specified params. The event is buffered when buffering is enabled and
     * the payload's account has not armed this event yet; it is sent immediately otherwise.
     *
     * @param event The event to be emitted
     * @param params Optional event parameters
     *
     * @see [armAndFlush]
     */
    fun emit(event: CleverTapEvent, params: Any?) {
        val buffer = eventsBuffers[event]
        if (buffer == null) {
            // Not bufferable: still goes through the worker so it keeps its place in line
            // behind anything emitted before it.
            post("emit($event)") { sendEvent(event, params) }
            return
        }
        post("emit($event)") {
            val tag = accountTagOf(params)
            if (buffer.offer(tag, params)) {
                Log.i(LOG_TAG, "Buffered $event for account $tag (not armed yet)")
            } else {
                Log.i(LOG_TAG, "Emitting $event for account $tag")
                sendEvent(event, params)
            }
        }
    }

    /**
     * Blocks until every operation posted before this call has finished. Tests use it to
     * observe the worker's results deterministically; production code never needs it.
     */
    @VisibleForTesting
    internal fun awaitIdle() {
        worker.submit { }.get()
    }

    // Every operation goes through here. A task that throws must not take the worker thread
    // down with it (an uncaught exception on a non-main thread crashes the app): log and move
    // on to the next task, exactly as sendEvent already does for the React Native call.
    private fun post(what: String, task: () -> Unit) {
        worker.execute {
            try {
                task()
            } catch (t: Throwable) {
                Log.e(LOG_TAG, "$what failed", t)
            }
        }
    }

    private fun accountTagOf(params: Any?): String? {
        val map = params as? ReadableMap ?: return null
        return if (map.hasKey(Constants.CT_ACCOUNT_ID_KEY)) {
            map.getString(Constants.CT_ACCOUNT_ID_KEY)
        } else {
            null
        }
    }

    private fun sendEvent(event: CleverTapEvent, params: Any?) {
        val context = reactContext
        if (context == null) {
            Log.e(LOG_TAG, "Sending event $event failed. ReactContext is null")
            return
        }

        try {
            context.getJSModule(
                DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
            ).emit(event.eventName, params)
            Log.i(LOG_TAG, "Sending event $event")
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Sending event $event failed", t)
        }
    }

    /**
     * One event's buffer. Plain data, CONFINED TO THE WORKER THREAD: every method is only ever
     * called from a task posted through [post], so no synchronization is needed or wanted here.
     */
    private class Buffer(private var enabled: Boolean) {

        private val items = LinkedList<Any?>()

        // Accounts whose listeners have attached for this event (null entries are tolerated
        // and simply never match a tagged payload).
        private val armedAccounts = mutableSetOf<String?>()

        /**
         * Buffers the payload and returns true when buffering is on and the payload's
         * account has not armed this event yet; returns false (caller sends immediately)
         * otherwise.
         */
        fun offer(accountTag: String?, item: Any?): Boolean {
            val armed = if (accountTag == null) {
                // Untagged payloads are global: live once anyone listens to the event.
                armedAccounts.isNotEmpty()
            } else {
                armedAccounts.contains(accountTag)
            }
            if (!enabled || armed) {
                return false
            }
            items.add(item)
            return true
        }

        fun arm(accountId: String?) {
            armedAccounts.add(accountId)
        }

        /**
         * Removes and returns the payloads belonging to the given account — plus untagged
         * (global) ones — keeping the other accounts' payloads buffered, in order.
         */
        fun drainFor(accountId: String?): List<Any?> {
            if (items.isEmpty()) {
                return emptyList()
            }
            // Pre-sized for the worst case (every item matches) so the list never regrows.
            val send = ArrayList<Any?>(items.size)
            // Remove matching payloads in place with the iterator (O(1) per unhook on a
            // LinkedList) — the other accounts' payloads stay buffered without being
            // drained into a temporary list and copied back.
            val iterator = items.iterator()
            while (iterator.hasNext()) {
                val params = iterator.next()
                val tag = accountTagOf(params)
                if (tag == null || tag == accountId) {
                    send.add(params)
                    iterator.remove()
                }
            }
            return send
        }

        fun reset(enable: Boolean) {
            items.clear()
            armedAccounts.clear()
            enabled = enable
        }
    }
}
