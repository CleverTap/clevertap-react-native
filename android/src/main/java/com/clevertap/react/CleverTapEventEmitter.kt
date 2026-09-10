package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList

/**
 * CleverTapEventEmitter is responsible for emitting events to the React Native JavaScript layer.
 * It manages event buffers and allows events to be queued which is useful when needing to send an
 * event while the ReactContext of the application is not yet initialized or listeners have not
 * attached yet. Only events specified as [CleverTapEvent.bufferable] will be considered for
 * buffering, all other events will be emitted immediately.
 *
 * Buffers are ACCOUNT-AWARE. Every payload carries the account id it belongs to (stamped under
 * [Constants.CT_ACCOUNT_ID_KEY] by [CleverTapListenerProxy]). A listener arms an event only for
 * its own account ([armAccount]), and [flushBuffer] delivers only that account's payloads while
 * KEEPING the other accounts' payloads buffered.
 *
 * Why: with two accounts, account B's listener attaching first must not drain and drop account
 * A's buffered events (A's listeners are not attached yet — the delivery would go nowhere and
 * the events would be lost forever, e.g. the push tap that launched the app).
 *
 * Payloads with no account tag are treated as GLOBAL: they go live once ANY account arms the
 * event (same behavior as before for events that are not per-account, like custom templates).
 *
 * ⚠️ THREAD SAFETY (SDK-6021): emits arrive on the native SDK's callback threads (usually main)
 * while arming/flushing runs on the NativeModules thread. ALL buffer state — the item list, the
 * armed-accounts set AND the enabled flag — is therefore guarded by ONE monitor per [Buffer],
 * and the buffer/send decision in [emit] is a single atomic [Buffer.offer] (a separate
 * check-then-add allowed a payload to slip in AFTER its account's flush and be silently
 * discarded later). A previous version locked only the drain, which crashed in production with
 * NoSuchElementException at LinkedList.removeFirst (unsynchronized add racing the drain).
 * Events are always SENT outside the lock — never invoke React Native while holding it.
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    // Written once from the module constructor thread, read from every emitting thread.
    @Volatile
    var reactContext: ReactContext? = null

    // Fixed keys, immutable map: safe to read from any thread without a lock. All mutable
    // state lives INSIDE each Buffer, guarded by that buffer's monitor — the map itself is
    // never replaced (a swapped map let a racing emit buffer into a discarded copy).
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
        eventsBuffers.values.forEach { buffer -> buffer.reset(enableBuffers) }
        Log.i(LOG_TAG, "Buffers reset and enabled: $enableBuffers")
    }

    /**
     * Mark an account's listeners as attached for the specified event. From this point on,
     * payloads tagged with that account id are sent immediately instead of buffered.
     *
     * @see [flushBuffer]
     */
    fun armAccount(event: CleverTapEvent, accountId: String?) {
        val buffer = eventsBuffers[event] ?: return
        buffer.arm(accountId)
        Log.i(LOG_TAG, "Armed $event for account $accountId")
    }

    /**
     * Emit the buffered payloads belonging to the specified account — plus any untagged (global)
     * payloads — for the specified event. Other accounts' payloads stay buffered until their own
     * listeners attach (or [resetAllBuffers] discards them).
     */
    fun flushBuffer(event: CleverTapEvent, accountId: String?) {
        val buffer = eventsBuffers[event] ?: return
        // Drain under the lock, send outside it: React Native must never be invoked
        // while a buffer monitor is held.
        val toSend = buffer.drainFor(accountId)
        if (toSend.isNotEmpty()) {
            Log.i(LOG_TAG, "Flushing $event for account $accountId: sending ${toSend.size}")
        }
        toSend.forEach { params -> sendEvent(event, params) }
    }

    /**
     * Emit an event with specified params. The event is buffered when buffering is enabled and
     * the payload's account has not armed this event yet; it is sent immediately otherwise.
     * The decision and the enqueue are ONE atomic step ([Buffer.offer]) so a payload can never
     * slip into the buffer after its account's flush already ran.
     *
     * @param event The event to be emitted
     * @param params Optional event parameters
     *
     * @see [armAccount]
     */
    fun emit(event: CleverTapEvent, params: Any?) {
        val tag = accountTagOf(params)
        val buffer = eventsBuffers[event]
        if (buffer != null && buffer.offer(tag, params)) {
            Log.i(LOG_TAG, "Buffered $event for account $tag (not armed yet)")
        } else {
            Log.i(LOG_TAG, "Emitting $event for account $tag")
            sendEvent(event, params)
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
     * One event's buffer. EVERY member is guarded by this buffer's own monitor — reads
     * included. Only pure data work happens inside the lock; the emitter sends events
     * after the lock is released.
     */
    private class Buffer(enabled: Boolean) {

        // All guarded by synchronized(this):
        private var enabled: Boolean = enabled
        private val items = LinkedList<Any?>()
        // Accounts whose listeners have attached for this event (null entries are tolerated
        // and simply never match a tagged payload).
        private val armedAccounts = mutableSetOf<String?>()

        /**
         * Buffers the payload and returns true when buffering is on and the payload's
         * account has not armed this event yet; returns false (caller sends immediately)
         * otherwise. Check and enqueue are one atomic step on purpose.
         */
        @Synchronized
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

        @Synchronized
        fun arm(accountId: String?) {
            armedAccounts.add(accountId)
        }

        /**
         * Removes and returns the payloads belonging to the given account — plus untagged
         * (global) ones — keeping the other accounts' payloads buffered, in order.
         */
        @Synchronized
        fun drainFor(accountId: String?): List<Any?> {
            if (items.isEmpty()) {
                return emptyList()
            }
            // Pre-sized for the worst case (every item matches) so the list never regrows;
            // items.size is a stored counter on LinkedList, and the lock keeps it stable.
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

        @Synchronized
        fun reset(enable: Boolean) {
            items.clear()
            armedAccounts.clear()
            enabled = enable
        }
    }
}
