
#import "CleverTapReactManager.h"
#import "CleverTapReact.h"

#import <UIKit/UIKit.h>

#import <React/RCTLog.h>

#import "CleverTap.h"
#import "CleverTap+Inbox.h"
#import "CleverTap+ABTesting.h"
#import "CleverTap+DisplayUnit.h"
#import "CleverTapEventDetail.h"
#import "CleverTapUTMDetail.h"
#import "CleverTapSyncDelegate.h"
#import "CleverTapInAppNotificationDelegate.h"

@interface CleverTapReactManager() <CleverTapSyncDelegate, CleverTapInAppNotificationDelegate, CleverTapDisplayUnitDelegate> {
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
        [clevertap setSyncDelegate:self];
        [clevertap setInAppNotificationDelegate:self];
        [clevertap setDisplayUnitDelegate:self];
        [clevertap setLibrary:@"React-Native"];
        [clevertap registerExperimentsUpdatedBlock:^{
            [self postNotificationWithName:kCleverTapExperimentsDidUpdate andBody:nil];
        }];
    }
    return self;
}


- (void)applicationDidLaunchWithOptions:(NSDictionary *)options {
    NSDictionary *notification = [options valueForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
    if (notification && notification[@"wzrk_dl"]) {
        self.launchDeepLink = notification[@"wzrk_dl"];
        RCTLogInfo(@"CleverTapReact: setting launch deeplink: %@", self.launchDeepLink);
    }
}

#pragma mark Private

- (void)postNotificationWithName:(NSString *)name andBody:(NSDictionary *)body {
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil userInfo:body];
}

#pragma mark CleverTapSyncDelegate

-(void)profileDidInitialize:(NSString*)cleverTapID {
    if(!cleverTapID) {
        return;
    }
    
    [self postNotificationWithName:kCleverTapProfileDidInitialize andBody:@{@"CleverTapID":cleverTapID}];
}

-(void)profileDataUpdated:(NSDictionary *)updates {
    if(!updates) {
        return ;
    }
    [self postNotificationWithName:kCleverTapProfileSync andBody:@{@"updates":updates}];
}

#pragma mark CleverTapInAppNotificationDelegate

-(void)inAppNotificationDismissedWithExtras:(NSDictionary *)extras andActionExtras:(NSDictionary *)actionExtras {
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

@end
