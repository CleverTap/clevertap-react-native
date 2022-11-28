#import "CleverTapReactManager.h"
#import "CleverTapReact.h"

#import <UIKit/UIKit.h>
#import <React/RCTLog.h>

#import "CleverTap+Inbox.h"
#import "CleverTapUTMDetail.h"
#import "CleverTapEventDetail.h"
#import "CleverTap+DisplayUnit.h"
#import "CleverTapSyncDelegate.h"
#import "CleverTap+FeatureFlags.h"
#import "CleverTap+ProductConfig.h"
#import "CleverTapPushNotificationDelegate.h"
#import "CleverTapInAppNotificationDelegate.h"

@interface CleverTapReactManager() <CleverTapSyncDelegate, CleverTapInAppNotificationDelegate, CleverTapDisplayUnitDelegate,  CleverTapFeatureFlagsDelegate, CleverTapProductConfigDelegate, CleverTapPushNotificationDelegate> {
}

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
        CleverTap *clevertap = [CleverTap sharedInstance];
        [self setDelegates:clevertap];
    }
    return self;
}

- (void)setDelegates:(CleverTap *)cleverTapInstance {
    [cleverTapInstance setSyncDelegate:self];
    [cleverTapInstance setInAppNotificationDelegate:self];
    [cleverTapInstance setDisplayUnitDelegate:self];
    [cleverTapInstance setPushNotificationDelegate:self];
    [[cleverTapInstance featureFlags] setDelegate:self];
    [[cleverTapInstance productConfig] setDelegate:self];
    [cleverTapInstance setLibrary:@"React-Native"];
}

- (void)applicationDidLaunchWithOptions:(NSDictionary *)options {
    NSDictionary *notification = [options valueForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (notification && notification[@"wzrk_dl"]) {
        self.launchDeepLink = notification[@"wzrk_dl"];
        RCTLogInfo(@"CleverTapReact: setting launch deeplink: %@", self.launchDeepLink);
    }
}

#pragma mark - Private

- (void)postNotificationWithName:(NSString *)name andBody:(NSDictionary *)body {
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil userInfo:body];
}


#pragma mark - CleverTapSyncDelegate

- (void)profileDidInitialize:(NSString*)cleverTapID {
    if(!cleverTapID) {
        return;
    }
    
    [self postNotificationWithName:kCleverTapProfileDidInitialize andBody:@{@"CleverTapID":cleverTapID}];
}

- (void)profileDataUpdated:(NSDictionary *)updates {
    if(!updates) {
        return ;
    }
    [self postNotificationWithName:kCleverTapProfileSync andBody:@{@"updates":updates}];
}


#pragma mark - CleverTapPushNotificationDelegate

- (void)pushNotificationTappedWithCustomExtras:(NSDictionary *)customExtras {
    NSMutableDictionary *pushNotificationExtras = [NSMutableDictionary new];
    if (customExtras != nil) {
        pushNotificationExtras[@"customExtras"] = customExtras;
    }
    [self postNotificationWithName:kCleverTapPushNotificationClicked andBody:pushNotificationExtras];
}


#pragma mark - CleverTapInAppNotificationDelegate

- (void)inAppNotificationDismissedWithExtras:(NSDictionary *)extras andActionExtras:(NSDictionary *)actionExtras {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (extras != nil) {
        body[@"extras"] = extras;
    }
    if (actionExtras != nil) {
        body[@"actionExtras"] = actionExtras;
    }
    [self postNotificationWithName:kCleverTapInAppNotificationDismissed andBody:body];
}

- (void)inAppNotificationButtonTappedWithCustomExtras:(NSDictionary *)customExtras {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (customExtras != nil) {
        body[@"customExtras"] = customExtras;
    }
    [self postNotificationWithName:kCleverTapInAppNotificationButtonTapped andBody:body];
}

- (void)displayUnitsUpdated:(NSArray<CleverTapDisplayUnit *> *)displayUnits {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (displayUnits != nil) {
        NSMutableArray *units = [NSMutableArray new];
        for (CleverTapDisplayUnit *unit in displayUnits) {
            [units addObject:unit.json];
        }
        NSArray *result = [units mutableCopy];
        body[@"displayUnits"] = result;
    }
    [self postNotificationWithName:kCleverTapDisplayUnitsLoaded andBody:body];
}


#pragma mark - CleverTapFeatureFlagsDelegate

- (void)ctFeatureFlagsUpdated {
    [self postNotificationWithName:kCleverTapFeatureFlagsDidUpdate andBody:nil];
}


#pragma mark - CleverTapProductConfigDelegate

- (void)ctProductConfigFetched {
    [self postNotificationWithName:kCleverTapProductConfigDidFetch andBody:nil];
}

- (void)ctProductConfigActivated {
    [self postNotificationWithName:kCleverTapProductConfigDidActivate andBody:nil];
}

- (void)ctProductConfigInitialized {
    [self postNotificationWithName:kCleverTapProductConfigDidInitialize andBody:nil];
}

@end
