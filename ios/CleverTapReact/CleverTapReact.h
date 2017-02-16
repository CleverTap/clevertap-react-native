#import <React/RCTBridgeModule.h>

static NSString *const kCleverTapProfileDidInitialize       = @"CleverTapProfileDidInitialize";
static NSString *const kCleverTapProfileSync                = @"CleverTapProfileSync";
static NSString *const kCleverTapInAppNotificationDismissed = @"CleverTapInAppNotificationDismissed";

@interface CleverTapReact : NSObject <RCTBridgeModule>

@end
