package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.Queue

/**
 * CleverTapEventEmitter is responsible for emitting events to the React Native JavaScript layer.
 * It manages event buffers and allows events to be queued which is useful when needing to send an
 * event while the ReactContext of the application is not yet initialized. Only events specified in
 * [bufferableEvents] will be considered for buffering, all other events will be emitted
 * immediately.
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    val bufferableEvents = arrayOf(
        CleverTapModuleImpl.CLEVERTAP_PUSH_NOTIFICATION_CLICKED
    )

    private data class Buffer(var enabled: Boolean) {
        val items: Queue<Any?> = LinkedList()
    }

    private val eventsBuffers: Map<String, Buffer> =
        bufferableEvents.associateWith {
            Buffer(enabled = true)
        }

    /**
     * Enable buffering for the specified event. While enabled, all events which should be emitted
     * will be added to the buffer. Only buffers for events specified in [bufferableEvents] can be
     * enabled.
     *
     * @see [disableBuffer]
     */
    fun enableBuffer(eventName: String) {
        eventsBuffers[eventName]?.enabled = true
    }

    /**
     * Disable buffering for the specified event. This will only have effect for events specified in
     * [bufferableEvents].
     *
     * @see [enableBuffer]
     */
    fun disableBuffer(eventName: String) {
        eventsBuffers[eventName]?.enabled = false
    }

    /**
     * Enable all buffers for all events specified in [bufferableEvents].
     *
     * @see [enableBuffer]
     */
    fun enableAllBuffers() {
        eventsBuffers.forEach {
            it.value.enabled = true
        }
    }

    /**
     * Emit all currently buffered events for the specified event name.
     */
    fun flushBuffer(eventName: String, reactContext: ReactContext) {
        val bufferedParams = eventsBuffers[eventName]?.items ?: return
        while (bufferedParams.isNotEmpty()) {
            val params = bufferedParams.remove()
            sendEvent(eventName, params, reactContext)
        }
    }

    /**
     * Adds an event to the buffer for future emission.
     * Events will remain in the buffer until [flushBuffer] for the same event name is called.
     *
     * @param eventName The name of the event to be buffered.
     * @param params Optional event parameters to be sent when the event is emitted.
     */
    fun addToBuffer(eventName: String, params: Any?) {
        val buffer = eventsBuffers[eventName] ?: return
        buffer.items.add(params)
        Log.i(LOG_TAG, "Event $eventName added to buffer.")
    }

    /**
     * Emit an event with specified params. It will be buffered if buffering for the event is enabled or
     * it will be send immediately otherwise.
     *
     * @param eventName The name of the event to be emitted
     * @param params Optional event parameters
     * @param context The react context through which to emit the event. If the event is not buffered
     * this must not be null.
     *
     * @see [enableBuffer]
     * @see [disableBuffer]
     */
    fun emit(eventName: String, params: Any?, context: ReactContext?) {
        if (eventsBuffers[eventName]?.enabled == true) {
            addToBuffer(eventName, params)
        } else {
            if (context == null) {
                Log.e(LOG_TAG, "Sending event $eventName failed. ReactContext is null")
                return
            }
            sendEvent(eventName, params, context)
        }
    }

    private fun sendEvent(eventName: String, params: Any?, context: ReactContext) {
        try {
            context.getJSModule(
                DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
            ).emit(eventName, params)
            Log.i(LOG_TAG, "Sending event $eventName")
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Sending event $eventName failed", t)
        }
    }
}
