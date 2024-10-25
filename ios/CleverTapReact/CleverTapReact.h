#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>

static NSString *const kCleverTapProfileDidInitialize = @"CleverTapProfileDidInitialize";
static NSString *const kCleverTapProfileSync = @"CleverTapProfileSync";
static NSString *const kCleverTapInAppNotificationDismissed = @"CleverTapInAppNotificationDismissed";
static NSString *const kCleverTapInAppNotificationButtonTapped = @"CleverTapInAppNotificationButtonTapped";
static NSString *const kCleverTapInboxDidInitialize = @"CleverTapInboxDidInitialize";
static NSString *const kCleverTapInboxMessagesDidUpdate = @"CleverTapInboxMessagesDidUpdate";
static NSString *const kCleverTapInboxMessageButtonTapped = @"CleverTapInboxMessageButtonTapped";
static NSString *const kCleverTapInboxMessageTapped = @"CleverTapInboxMessageTapped";
static NSString *const kCleverTapDisplayUnitsLoaded = @"CleverTapDisplayUnitsLoaded";
static NSString *const kCleverTapFeatureFlagsDidUpdate = @"CleverTapFeatureFlagsDidUpdate";
static NSString *const kCleverTapProductConfigDidFetch = @"CleverTapProductConfigDidFetch";
static NSString *const kCleverTapProductConfigDidActivate = @"CleverTapProductConfigDidActivate";
static NSString *const kCleverTapProductConfigDidInitialize = @"CleverTapProductConfigDidInitialize";
static NSString *const kCleverTapPushNotificationClicked = @"CleverTapPushNotificationClicked";
static NSString *const kCleverTapPushPermissionResponseReceived = @"CleverTapPushPermissionResponseReceived";
static NSString *const kCleverTapInAppNotificationShowed = @"CleverTapInAppNotificationShowed";
static NSString *const kCleverTapOnVariablesChanged = @"CleverTapOnVariablesChanged";
static NSString *const kCleverTapOnOneTimeVariablesChanged = @"CleverTapOnOneTimeVariablesChanged";
static NSString *const kCleverTapOnValueChanged = @"CleverTapOnValueChanged";
static NSString *const kCleverTapOnVariablesChangedAndNoDownloadsPending = @"CleverTapOnVariablesChangedAndNoDownloadsPending";
static NSString *const kCleverTapOnceVariablesChangedAndNoDownloadsPending = @"CleverTapOnceVariablesChangedAndNoDownloadsPending";
static NSString *const kCleverTapOnFileValueChanged = @"CleverTapOnFileValueChanged";
static NSString *const kCleverTapCustomTemplatePresent = @"CleverTapCustomTemplatePresent";
static NSString *const kCleverTapCustomFunctionPresent = @"CleverTapCustomFunctionPresent";
static NSString *const kCleverTapCustomTemplateClose = @"CleverTapCustomTemplateClose";
static NSString *const kXPS = @"XPS";

#ifdef RCT_NEW_ARCH_ENABLED
#import <CTTurboModuleSpec/CTTurboModuleSpec.h>
@interface CleverTapReact: RCTEventEmitter <NativeCleverTapModuleSpec>
#else
#import <React/RCTBridgeModule.h>
@interface CleverTapReact: RCTEventEmitter <RCTBridgeModule>
#endif

+ (void)sendEventOnObserving:(NSString *)name body:(id)body;

@end
