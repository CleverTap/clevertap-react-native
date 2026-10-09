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

- (void)applicationDidLaunchWithOptions:(NSDictionary *)options
                          launchConfigs:(NSArray<CleverTapReactLaunchConfig *> *)launchConfigs {
    // The launch accounts are created HERE, on the main thread, inside
    // didFinishLaunchingWithOptions — deliberately:
    // 1. The SDK hands the launch push payload out via the "did finish launching"
    //    notification, which fires right after didFinishLaunchingWithOptions returns,
    //    and ONLY to instances that already exist at that moment. An instance still
    //    being built on a background queue misses the launch push forever — the very
    //    event this API exists to catch.
    // 2. [CleverTap instanceWithConfig:] mutates an unlocked shared dictionary; it is
    //    not safe to call from a background queue while the main thread can touch it.
    // The launch-time cost is small: the SDK runs its heavy work on its own queues;
    // the synchronous part is the same setup the default account already pays.
    for (CleverTapReactLaunchConfig *launch in launchConfigs) {
        @try {
            NSString *accountId = launch.config.accountId;
            if (launch.cleverTapID.length > 0 && !launch.config.useCustomCleverTapId) {
                // Without this, the SDK ignores the passed ID with no message at all.
                RCTLogWarn(@"CleverTapReact: cleverTapID given for '%@' but useCustomCleverTapId "
                           "is NO — the ID will be IGNORED and the SDK will generate its own",
                           accountId);
            }
            // Same branching as the JS createInstance path: use the two-argument
            // factory only when an ID was actually supplied, so the no-custom-ID
            // path stays exactly today's behavior.
            CleverTap *instance = (launch.cleverTapID.length > 0)
                ? [CleverTap instanceWithConfig:launch.config andCleverTapID:launch.cleverTapID]
                : [CleverTap instanceWithConfig:launch.config];
            if (instance) {
                [self setDelegates:instance]; // idempotent — same wiring the default account gets
            } else {
                RCTLogWarn(@"CleverTapReact: launch config for '%@' produced no instance, skipping",
                           accountId);
            }
        } @catch (NSException *e) {
            // Instance creation can throw (e.g. a registered custom template raising on a
            // duplicate name). One bad config must never crash app launch or take the
            // remaining accounts down with it.
            RCTLogWarn(@"CleverTapReact: failed to create launch instance for '%@', skipping: %@",
                       launch.config.accountId, e);
        }
    }

    [self applicationDidLaunchWithOptions:options]; // existing deep-link capture, unchanged
}

@end
