#import <Foundation/Foundation.h>
#import "CleverTap.h"

@interface CleverTapReactManager : NSObject

+ (instancetype)sharedInstance;

- (void)applicationDidLaunchWithOptions:(NSDictionary *)options;

- (void)setDelegates:(CleverTap *)cleverTapInstance;

@property NSString *launchDeepLink;
@property(nonatomic, strong) NSDictionary *pendingPushNotificationExtras;

@end
