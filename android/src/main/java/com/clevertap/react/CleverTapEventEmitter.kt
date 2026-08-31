package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.Queue

/**
 * CleverTapEventEmitter is responsible for emitting events to the React Native JavaScript layer.
 * It manages event buffers and allows events to be queued which is useful when needing to send an
 * event while the ReactContext of the application is not yet initialized. Only events specified as
 * [CleverTapEvent.bufferable] will be considered for buffering, all other events will be emitted
 * immediately.
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    /**
     * Set whenever a React module is constructed, which happens once per React context and so
     * again after a reload. It is read by [sendEvent] on SDK callback threads, so it is volatile
     * to make a new context visible to them immediately. Otherwise those threads can keep reading
     * a stale value and silently drop events.
     */
    @Volatile
    var reactContext: ReactContext? = null

    /**
     * Replaced wholesale by [resetAllBuffers] on the main thread, and read by [emit] on SDK
     * callback threads. Volatile so a reset is visible to them right away.
     */
    @Volatile
    private var eventsBuffers: Map<CleverTapEvent, Buffer> = createBuffersMap(enableBuffers = true)

    /**
     * Enable buffering for the specified event. While enabled, all events which should be emitted
     * will be added to the buffer. Only buffers for events specified as [CleverTapEvent.bufferable]
     * can be enabled.
     *
     * @see [disableBuffer]
     */
    fun enableBuffer(event: CleverTapEvent) {
        eventsBuffers[event]?.enabled = true
    }

    /**
     * Disable buffering for the specified event. This will only have effect for events specified as
     * [CleverTapEvent.bufferable].
     *
     * @see [enableBuffer]
     */
    fun disableBuffer(event: CleverTapEvent) {
        eventsBuffers[event]?.enabled = false
    }

    /**
     * Clear all the buffered events from all buffers and set whether all buffers should be enabled
     * after that.
     *
     * @param enableBuffers enable/disable all buffers after they are cleared
     */
    fun resetAllBuffers(enableBuffers: Boolean) {
        eventsBuffers = createBuffersMap(enableBuffers)
        Log.i(LOG_TAG, "Buffers reset and enabled: $enableBuffers")
    }

    /**
     * Emit all currently buffered events for the specified event name.
     */
    fun flushBuffer(event: CleverTapEvent) {
        val buffer = eventsBuffers[event] ?: return
        synchronized(buffer) {
            while (buffer.size() > 0) {
                val params = buffer.remove()
                sendEvent(event, params)
            }
        }
    }

    /**
     * Emit an event with specified params. It will be buffered if buffering for the event is enabled or
     * it will be send immediately otherwise.
     *
     * @param event The event to be emitted
     * @param params Optional event parameters
     *
     * @see [enableBuffer]
     * @see [disableBuffer]
     */
    fun emit(event: CleverTapEvent, params: Any?) {
        if (eventsBuffers[event]?.enabled == true) {
            addToBuffer(event, params)
        } else {
            sendEvent(event, params)
        }
    }

    /**
     * Adds an event to the buffer for future emission.
     * Events will remain in the buffer until [flushBuffer] for the same event name is called.
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
     * A buffer of pending event params. All access to [items] is guarded by this instance's
     * monitor, which [flushBuffer] also holds to keep a whole drain atomic. Guarding only the
     * drain is not enough: [addToBuffer] writes from SDK callback threads while [flushBuffer]
     * removes, and an unguarded add can interleave with removeFirst() so that `size` stays above
     * zero while `first` is already null, making the next remove() throw NoSuchElementException.
     */
    private class Buffer(enabled: Boolean) {

        /** Read by [emit] on SDK threads, written by [enableBuffer]/[disableBuffer] on others. */
        @Volatile
        var enabled: Boolean = enabled

        private val items: Queue<Any?> = LinkedList()

        @Synchronized
        fun add(item: Any?) = items.add(item)

        @Synchronized
        fun remove(): Any? = items.remove()

        @Synchronized
        fun size(): Int = items.size
    }
}
