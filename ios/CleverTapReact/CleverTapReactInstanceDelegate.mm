#import "CleverTapReactInstanceDelegate.h"
#import "CleverTapReact.h"

#import <React/RCTLog.h>

@implementation CleverTapReactInstanceDelegate

- (instancetype)initWithAccountId:(NSString *)accountId {
    self = [super init];
    if (self) {
        _accountId = accountId;
    }
    return self;
}

#pragma mark - Private

// The ONE place where the account tag is added — every callback below posts through here.
// The tag is always the REAL account id, the default account included (no "nil means
// default" convention).
- (void)postNotificationWithName:(NSString *)name andBody:(NSDictionary *)body {
    NSMutableDictionary *tagged = [NSMutableDictionary dictionaryWithDictionary:body ?: @{}];
    tagged[kCleverTapAccountIdKey] = self.accountId;
    [CleverTapReact sendEventOnObserving:name body:tagged];
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
        pushNotificationExtras = [NSMutableDictionary dictionaryWithDictionary:customExtras];
    }
    [self postNotificationWithName:kCleverTapPushNotificationClicked andBody:pushNotificationExtras];
}


#pragma mark - CleverTapInAppNotificationDelegate

- (void)inAppNotificationDismissedWithExtras:(NSDictionary *)extras andActionExtras:(NSDictionary *)actionExtras {
    NSMutableDictionary *body = [NSMutableDictionary new];
    body[@"extras"] = (extras != nil) ? extras : [NSMutableDictionary new];
    body[@"actionExtras"] = (actionExtras != nil) ? actionExtras : [NSMutableDictionary new];
    [self postNotificationWithName:kCleverTapInAppNotificationDismissed andBody:body];
}

- (void)inAppNotificationButtonTappedWithCustomExtras:(NSDictionary *)customExtras {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (customExtras != nil) {
        body = [NSMutableDictionary dictionaryWithDictionary:customExtras];
    }
    [self postNotificationWithName:kCleverTapInAppNotificationButtonTapped andBody:body];
}

- (void)inAppNotificationDidShow:(NSDictionary *)notification {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (notification != nil) {
        body = [NSMutableDictionary dictionaryWithDictionary:notification];
    }
    [self postNotificationWithName:kCleverTapInAppNotificationShowed andBody:body];
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
    NSMutableDictionary *body = [NSMutableDictionary new];
    [self postNotificationWithName:kCleverTapFeatureFlagsDidUpdate andBody:body];
}


#pragma mark - CleverTapProductConfigDelegate

- (void)ctProductConfigFetched {
    NSMutableDictionary *body = [NSMutableDictionary new];
    [self postNotificationWithName:kCleverTapProductConfigDidFetch andBody:body];
}

- (void)ctProductConfigActivated {
    NSMutableDictionary *body = [NSMutableDictionary new];
    [self postNotificationWithName:kCleverTapProductConfigDidActivate andBody:body];
}

- (void)ctProductConfigInitialized {
    NSMutableDictionary *body = [NSMutableDictionary new];
    [self postNotificationWithName:kCleverTapProductConfigDidInitialize andBody:body];
}

#pragma mark - CleverTapPushPermissionDelegate

- (void)onPushPermissionResponse:(BOOL)accepted {
    NSMutableDictionary *body = [NSMutableDictionary new];
    body[@"accepted"] = [NSNumber numberWithBool:accepted];
    [self postNotificationWithName:kCleverTapPushPermissionResponseReceived andBody:body];
}

@end
