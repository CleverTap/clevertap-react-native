
#import "CleverTapReactManager.h"
#import "CleverTapReact.h"

#import <UIKit/UIKit.h>

#import <React/RCTLog.h>

#import <CleverTapSDK/CleverTap.h>
#import <CleverTapSDK/CleverTap+Inbox.h>
#import <CleverTapSDK/CleverTap+ABTesting.h>
#import <CleverTapSDK/CleverTapEventDetail.h>
#import <CleverTapSDK/CleverTapUTMDetail.h>
#import <CleverTapSDK/CleverTapSyncDelegate.h>
#import <CleverTapSDK/CleverTapInAppNotificationDelegate.h>

@interface CleverTapReactManager() <CleverTapSyncDelegate, CleverTapInAppNotificationDelegate> {
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

@end
