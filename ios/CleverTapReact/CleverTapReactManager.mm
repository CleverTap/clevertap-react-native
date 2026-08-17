#import "CleverTapReactManager.h"
#import "CleverTapReact.h"
#import "CleverTapReactInstanceDelegate.h"
#import "CleverTapInstanceConfig.h"

#import <UIKit/UIKit.h>
#import <React/RCTLog.h>
#import <React/RCTBridge.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTRootView.h>

@interface CleverTapReactManager()

// ⚠️ LOAD-BEARING: ALL CleverTap iOS delegates are declared `weak` on the SDK side
// (syncDelegate, pushNotificationDelegate, the in-app delegate, ...). This dictionary is
// the ONLY strong reference keeping the per-account delegate handlers alive. The old
// design used this singleton manager itself as the delegate, which could never be
// deallocated; these small per-account handler objects CAN. Delete this dictionary and
// events die silently after ARC frees the handlers — it passes QA and fails in production.
@property (nonatomic, strong) NSMutableDictionary<NSString *, CleverTapReactInstanceDelegate *> *handlers;

@end

@implementation CleverTapReactManager

+ (instancetype)sharedInstance {
    static CleverTapReactManager *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}


- (instancetype)init {
    self = [super init];
    if (self) {
        _handlers = [NSMutableDictionary new];
        // Wire the default account through the same per-account path used for every other
        // account, so its events carry the account tag from the very first callback.
        CleverTap *clevertap = [CleverTap sharedInstance];
        if (clevertap != nil) {
            [self setDelegates:clevertap];
        }
    }
    return self;
}

/// Sets all delegates for the given instance to that account's handler, creating the
/// handler on first use. Calling again for the same account reuses the same handler.
- (void)setDelegates:(CleverTap *)cleverTapInstance {
    NSString *accountId = cleverTapInstance.config.accountId;
    if (accountId == nil) {
        RCTLogWarn(@"CleverTapReactManager: cannot set delegates, instance has no accountId");
        return;
    }
    CleverTapReactInstanceDelegate *handler = self.handlers[accountId];
    if (handler == nil) {
        handler = [[CleverTapReactInstanceDelegate alloc] initWithAccountId:accountId];
        self.handlers[accountId] = handler;
    }
    [cleverTapInstance setSyncDelegate:handler];
    [cleverTapInstance setInAppNotificationDelegate:handler];
    [cleverTapInstance setDisplayUnitDelegate:handler];
    [cleverTapInstance setPushNotificationDelegate:handler];
    [[cleverTapInstance featureFlags] setDelegate:handler];
    [[cleverTapInstance productConfig] setDelegate:handler];
    [cleverTapInstance setPushPermissionDelegate:handler];
}


- (void)applicationDidLaunchWithOptions:(NSDictionary *)options {
    NSDictionary *notification = [options valueForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (notification){
        if (notification[@"wzrk_dl"]) {
            self.launchDeepLink = notification[@"wzrk_dl"];
            RCTLogInfo(@"CleverTapReact: setting launch deeplink: %@", self.launchDeepLink);
        }
    }
}

@end
