#import <Foundation/Foundation.h>

@interface CleverTapReactManager : NSObject

+ (instancetype)sharedInstance;

- (void)applicationDidLaunchWithOptions:(NSDictionary *)options;

@property NSString *launchDeepLink;

@end
