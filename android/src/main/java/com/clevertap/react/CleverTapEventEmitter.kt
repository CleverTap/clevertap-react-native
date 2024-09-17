package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.Queue

/**
 * CleverTapEventEmitter is responsible for emitting events to the React Native JavaScript layer.
 * It manages an event buffer and allows events to be queued when the component might not be ready ye
 */
object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    private var bufferedEvents: Queue<Pair<String, Any>> = LinkedList()

    // When true, all events will be buffered instead of being sent immediately.
    var bufferAll = false

    fun flushBuffer(reactContext: ReactContext) {
        while (bufferedEvents.isNotEmpty()) {
            val eventPair = bufferedEvents.remove()
            sendEvent(eventPair.first, eventPair.second, reactContext)
        }
    }

    /**
     * Adds an event to the buffer for future emission.
     * Events will remain in the buffer until flushBuffer() is called.
     *
     * @param eventName The name of the event to be buffered.
     * @param params The event parameters to be sent when the event is emitted.
     */
    fun addToBuffer(eventName: String, params: Any) {
        bufferedEvents.add(eventName to params)
    }

    fun emit(eventName: String, params: Any, context: ReactContext?) {
        if (bufferAll || context == null) {
            addToBuffer(eventName, params)
        } else {
            sendEvent(eventName, params, context)
        }
    }

    private fun sendEvent(eventName: String, params: Any, context: ReactContext) {
        try {
            context.getJSModule(
                DeviceEventManagerModule.RCTDeviceEventEmitter::class.java
            ).emit(eventName, params)
            Log.e(LOG_TAG, "Sending event $eventName")
        } catch (t: Throwable) {
            Log.e(LOG_TAG, "Sending event $eventName failed", t)
        }
    }
}
