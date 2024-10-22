package com.clevertap.react

enum class CleverTapEvent(val eventName: String, val bufferable: Boolean = false) {

    CLEVERTAP_PROFILE_DID_INITIALIZE("CleverTapProfileDidInitialize", bufferable = true),
    CLEVERTAP_PROFILE_SYNC("CleverTapProfileSync"),
    CLEVERTAP_IN_APP_NOTIFICATION_DISMISSED("CleverTapInAppNotificationDismissed", bufferable = true),
    CLEVERTAP_IN_APP_NOTIFICATION_SHOWED("CleverTapInAppNotificationShowed", bufferable = true),
    CLEVERTAP_INBOX_DID_INITIALIZE("CleverTapInboxDidInitialize", bufferable = true),
    CLEVERTAP_INBOX_MESSAGES_DID_UPDATE("CleverTapInboxMessagesDidUpdate"),
    CLEVERTAP_ON_INBOX_BUTTON_CLICK("CleverTapInboxMessageButtonTapped"),
    CLEVERTAP_ON_INBOX_MESSAGE_CLICK("CleverTapInboxMessageTapped"),
    CLEVERTAP_ON_INAPP_BUTTON_CLICK("CleverTapInAppNotificationButtonTapped", bufferable = true),
    CLEVERTAP_ON_DISPLAY_UNITS_LOADED("CleverTapDisplayUnitsLoaded", bufferable = true),
    CLEVERTAP_FEATURE_FLAGS_DID_UPDATE("CleverTapFeatureFlagsDidUpdate", bufferable = true),
    CLEVERTAP_PRODUCT_CONFIG_DID_INITIALIZE("CleverTapProductConfigDidInitialize", bufferable = true),
    CLEVERTAP_PRODUCT_CONFIG_DID_FETCH("CleverTapProductConfigDidFetch"),
    CLEVERTAP_PRODUCT_CONFIG_DID_ACTIVATE("CleverTapProductConfigDidActivate"),
    CLEVERTAP_PUSH_NOTIFICATION_CLICKED("CleverTapPushNotificationClicked", bufferable = true),
    CLEVERTAP_ON_PUSH_PERMISSION_RESPONSE("CleverTapPushPermissionResponseReceived"),
    CLEVERTAP_ON_VARIABLES_CHANGED("CleverTapOnVariablesChanged"),
    CLEVERTAP_ON_ONE_TIME_VARIABLES_CHANGED("CleverTapOnOneTimeVariablesChanged"),
    CLEVERTAP_ON_VALUE_CHANGED("CleverTapOnValueChanged"),
    CLEVERTAP_CUSTOM_TEMPLATE_PRESENT("CleverTapCustomTemplatePresent", bufferable = true),
    CLEVERTAP_CUSTOM_FUNCTION_PRESENT("CleverTapCustomFunctionPresent", bufferable = true),
    CLEVERTAP_CUSTOM_TEMPLATE_CLOSE("CleverTapCustomTemplateClose"),
    CLEVERTAP_ON_FILE_VALUE_CHANGED("CleverTapOnFileValueChanged"),
    CLEVERTAP_ON_VARIABLES_CHANGED_AND_NO_DOWNLOADS_PENDING("CleverTapOnVariablesChangedAndNoDownloadsPending"),
    CLEVERTAP_ONCE_VARIABLES_CHANGED_AND_NO_DOWNLOADS_PENDING("CleverTapOnceVariablesChangedAndNoDownloadsPending");

    override fun toString(): String {
        return eventName
    }

    companion object {

        @JvmStatic
        fun fromName(eventName: String): CleverTapEvent? {
            return values().find { it.eventName == eventName }
        }
    }
}
