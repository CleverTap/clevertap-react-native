package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.Queue

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
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    /** Volatile so SDK callback threads see a new context right away instead of dropping events. */
    @Volatile
    var reactContext: ReactContext? = null

    /** Volatile so a reset from the main thread is visible to SDK callback threads right away. */
    @Volatile
    private var eventsBuffers: Map<CleverTapEvent, Buffer> = createBuffersMap(enableBuffers = true)

    /**
     * Clear all the buffered events from all buffers and set whether all buffers should be enabled
     * after that. Note: with `enableBuffers = false` this DISCARDS whatever is still buffered —
     * it is the "listeners never showed up" safety valve, not a flush.
     *
     * @param enableBuffers enable/disable all buffers after they are cleared
     */
    fun resetAllBuffers(enableBuffers: Boolean) {
        eventsBuffers = createBuffersMap(enableBuffers)
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
        synchronized(buffer.lock) {
            buffer.armedAccounts.add(accountId)
        }
    }

    /**
     * Emit the buffered payloads belonging to the specified account — plus any untagged (global)
     * payloads — for the specified event. Other accounts' payloads stay buffered until their own
     * listeners attach (or [resetAllBuffers] discards them).
     */
    fun flushBuffer(event: CleverTapEvent, accountId: String?) {
        val buffer = eventsBuffers[event] ?: return
        synchronized(buffer.lock) {
            val kept = LinkedList<Any?>()
            while (buffer.size() > 0) {
                val params = buffer.remove()
                val tag = accountTagOf(params)
                if (tag == null || tag == accountId) {
                    sendEvent(event, params)
                } else {
                    kept.add(params)
                }
            }
            kept.forEach { buffer.add(it) }
        }
    }

    /**
     * Emit an event with specified params. The event is buffered when buffering is enabled and
     * the payload's account has not armed this event yet; it is sent immediately otherwise.
     *
     * @param event The event to be emitted
     * @param params Optional event parameters
     *
     * @see [armAccount]
     */
    fun emit(event: CleverTapEvent, params: Any?) {
        val buffer = eventsBuffers[event]
        if (buffer != null && buffer.enabled && !isArmed(buffer, accountTagOf(params))) {
            addToBuffer(event, params)
        } else {
            sendEvent(event, params)
        }
    }

    private fun isArmed(buffer: Buffer, accountTag: String?): Boolean = synchronized(buffer.lock) {
        if (accountTag == null) {
            // Untagged payloads are global: live once anyone listens to the event.
            buffer.armedAccounts.isNotEmpty()
        } else {
            buffer.armedAccounts.contains(accountTag)
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

    /**
     * Adds an event to the buffer for future emission.
     * Events will remain in the buffer until [flushBuffer] delivers them to their account or
     * [resetAllBuffers] discards them.
     *
     * @param event The event to be buffered.
     * @param params Optional event parameters to be sent when the event is emitted.
     */
    private fun addToBuffer(event: CleverTapEvent, params: Any?) {
        val buffer = eventsBuffers[event] ?: return
        buffer.add(params)
        Log.i(LOG_TAG, "Event $event added to buffer.")
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

    private fun createBuffersMap(enableBuffers: Boolean) =
        CleverTapEvent.values().filter { it.bufferable }.associateWith {
            Buffer(enabled = enableBuffers)
        }

    /**
     * A buffer of pending event params. Every access to [items] takes [lock], and [flushBuffer]
     * holds it for the whole drain so an add cannot interleave with a remove.
     */
    private class Buffer(enabled: Boolean) {

        /** Guards [items]. Shared with [flushBuffer] so the drain and the writes use one monitor. */
        val lock = Any()

        /** Read by [emit] on SDK threads, written by [enableBuffer]/[disableBuffer] on others. */
        @Volatile
        var enabled: Boolean = enabled

        private val items: Queue<Any?> = LinkedList()

        fun add(item: Any?) = synchronized(lock) { items.add(item) }

        fun remove(): Any? = synchronized(lock) { items.remove() }

        fun size(): Int = synchronized(lock) { items.size }

        // Accounts whose listeners have attached for this event (null entries are tolerated
        // and simply never match a tagged payload). Guarded by [lock] like everything else.
        val armedAccounts: MutableSet<String?> = mutableSetOf()
    }
}
