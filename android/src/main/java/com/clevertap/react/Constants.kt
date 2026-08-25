package com.clevertap.react

object ErrorMessages {
    const val CLEVERTAP_NOT_INITIALIZED = "CleverTap not initialized"
    const val PRODUCTCONFIG_NOT_INITIALIZED = "Product Config not initialized"
    const val FF_NOT_INITIALIZED = "Feature Flags not initialized"
}

object Constants {
    const val REACT_MODULE_NAME = "CleverTapReact"

    const val FCM = "FCM"

    /**
     * Key stamped into every event payload with the REAL account id of the CleverTap
     * instance that fired it (the default account included — there is no "null means
     * default" convention). JS routes each event to the right account handle by this
     * tag and strips it before user handlers run.
     */
    const val CT_ACCOUNT_ID_KEY = "__ctAccountId"

    /**
     * Key that carries a primitive event payload inside the tagged map. Some events
     * (custom templates) deliver a bare string to user code; a string cannot hold the
     * account tag, so native wraps it — {__ctAccountId: id, __ctPayload: "name"} —
     * and the JS demux unwraps it, delivering exactly the string users always got.
     */
    const val CT_PAYLOAD_KEY = "__ctPayload"
}
