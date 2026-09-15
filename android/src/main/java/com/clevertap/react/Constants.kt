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
     * Promise rejection codes for createInstance. Part of the public JS contract —
     * apps switch on `e.code`, so the exact strings must never drift between call
     * sites or platforms (iOS mirrors these in CleverTapReact.mm).
     * EINVALID = the config itself is unacceptable (wrong type, missing field).
     * ECREATE  = the config was fine but the native SDK could not create the instance.
     */
    const val ERROR_CODE_INVALID_CONFIG = "EINVALID"
    const val ERROR_CODE_CREATE_FAILED = "ECREATE"

    /**
     * The library name stamped on every instance for analytics attribution.
     * Must match `libName` in src/index.js and the setLibrary call in
     * CleverTapReact.mm — three layers, one value.
     */
    const val LIBRARY_NAME = "React-Native"

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
