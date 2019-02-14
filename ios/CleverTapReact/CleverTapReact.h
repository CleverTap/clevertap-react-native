#import <React/RCTBridgeModule.h>

static NSString *const kCleverTapProfileDidInitialize       = @"CleverTapProfileDidInitialize";
static NSString *const kCleverTapProfileSync                = @"CleverTapProfileSync";
static NSString *const kCleverTapInAppNotificationDismissed = @"CleverTapInAppNotificationDismissed";
static NSString *const kCleverTapInboxDidInitialize         = @"CleverTapInboxDidInitialize";
static NSString *const kCleverTapInboxMessagesDidUpdate     = @"CleverTapInboxMessagesDidUpdate";

@interface CleverTapReact : NSObject <RCTBridgeModule>

@end
