package com.clevertap.react

import android.util.Log
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.LinkedList
import java.util.Queue

object CleverTapEventEmitter {
    private const val LOG_TAG = "CleverTapEventEmitter"

    private var bufferedEvents: Queue<Pair<String, Any>> = LinkedList()
    var bufferAll = false

    fun flushBuffer(reactContext: ReactContext) {
        while (bufferedEvents.isNotEmpty()) {
            val eventPair = bufferedEvents.remove()
            sendEvent(eventPair.first, eventPair.second, reactContext)
        }
    }

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
