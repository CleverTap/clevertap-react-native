#import "CleverTapReact.h"
#import "CleverTapReactManager.h"
#import <CleverTapSDK/CleverTap.h>
#import <CleverTapSDK/CleverTapEventDetail.h>
#import <CleverTapSDK/CleverTapUTMDetail.h>

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>

#import <React/RCTLog.h>

static NSDateFormatter *dateFormatter;

@implementation CleverTapReact

RCT_EXPORT_MODULE();

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (NSDictionary *)constantsToExport {
    return @{
             kCleverTapProfileDidInitialize : kCleverTapProfileDidInitialize,
             kCleverTapProfileSync : kCleverTapProfileSync,
             kCleverTapInAppNotificationDismissed: kCleverTapInAppNotificationDismissed,
             };
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

# pragma mark launch

RCT_EXPORT_METHOD(getInitialUrl:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getInitialUrl]");
    NSString *launchDeepLink = [CleverTapReactManager sharedInstance].launchDeepLink;
    if (launchDeepLink != nil) {
        [self returnResult:launchDeepLink withCallback:callback andError:nil];
    } else {
        [self returnResult:nil withCallback:callback andError:@"CleverTap initialUrl is nil"];
    }
}

#pragma mark Push

RCT_EXPORT_METHOD(registerForPush) {
    RCTLogInfo(@"[CleverTap registerForPush]");
    if (floor(NSFoundationVersionNumber) > NSFoundationVersionNumber_iOS_9_x_Max) {
        UNUserNotificationCenter* center = [UNUserNotificationCenter currentNotificationCenter];
        [center requestAuthorizationWithOptions:(UNAuthorizationOptionAlert | UNAuthorizationOptionSound | UNAuthorizationOptionBadge)
                              completionHandler:^(BOOL granted, NSError * _Nullable error) {
                                  if (granted) {
                                      dispatch_async(dispatch_get_main_queue(), ^(void) {
                                          [[UIApplication sharedApplication] registerForRemoteNotifications];
                                      });
                                  }
                              }];
        
    }
    else {
        UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeBadge | UIUserNotificationTypeAlert | UIUserNotificationTypeSound) categories:nil];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
        [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    }
}

RCT_EXPORT_METHOD(setPushTokenAsString:(NSString*)token withType:(NSString *)type) {
    // type is a no-op in iOS
    RCTLogInfo(@"[CleverTap setPushTokenAsString: %@]", token);
    [[CleverTap sharedInstance] setPushTokenAsString:token];
}

//no-op Android O methods

RCT_EXPORT_METHOD(createNotificationChannel:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(int*)importance withShowBadge:(BOOL*)showBadge){
    RCTLogInfo(@"[no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelwithGroupId:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(int*)importance withGroupId:(NSString*)groupId withShowBadge:(BOOL*)showBadge){
    RCTLogInfo(@"[no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelGroup:(NSString*)groupId withGroupName:(NSString*)groupName){
    RCTLogInfo(@"[no-op in iOS]");
}

RCT_EXPORT_METHOD(deleteNotificationChannel:(NSString*)channelId){
    RCTLogInfo(@"[no-op in iOS]");
}

RCT_EXPORT_METHOD(deleteNotificationChannelGroup:(NSString*)groupId){
    RCTLogInfo(@"[no-op in iOS]");
}



#pragma mark Personalization

RCT_EXPORT_METHOD(enablePersonalization) {
    RCTLogInfo(@"[CleverTap enablePersonalization]");
    [CleverTap enablePersonalization];
}

RCT_EXPORT_METHOD(disablePersonalization) {
    RCTLogInfo(@"[CleverTap disablePersonalization]");
    [CleverTap disablePersonalization];
}


#pragma mark Event API

RCT_EXPORT_METHOD(recordScreenView:(NSString*)screenName) {
    RCTLogInfo(@"[CleverTap recordScreenView]");
    [[CleverTap sharedInstance] recordScreenView:screenName];
}

RCT_EXPORT_METHOD(recordEvent:(NSString*)eventName withProps:(NSDictionary*)props) {
    RCTLogInfo(@"[CleverTap recordEvent:withProps]");
    [[CleverTap sharedInstance] recordEvent:eventName withProps:props];
}

RCT_EXPORT_METHOD(recordChargedEvent:(NSDictionary*)details andItems:(NSArray*)items) {
    RCTLogInfo(@"[CleverTap recordChargedEventWithDetails:andItems:]");
    [[CleverTap sharedInstance] recordChargedEventWithDetails:details andItems:items];
}

RCT_EXPORT_METHOD(eventGetFirstTime:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetFirstTime: %@]", eventName);
    NSTimeInterval result = [[CleverTap sharedInstance] eventGetFirstTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetLastTime:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetLastTime: %@]", eventName);
    NSTimeInterval result = [[CleverTap sharedInstance] eventGetLastTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetOccurrences:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetOccurrences: %@]", eventName);
    int result = [[CleverTap sharedInstance] eventGetOccurrences:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetDetail:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetDetail: %@]", eventName);
    CleverTapEventDetail *detail = [[CleverTap sharedInstance] eventGetDetail:eventName];
    NSDictionary *result = [self _eventDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getEventHistory:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getEventHistory]");
    NSDictionary *history = [[CleverTap sharedInstance] userGetEventHistory];
    NSMutableDictionary *result = [NSMutableDictionary new];
    
    for (NSString *eventName in [history keyEnumerator]) {
        CleverTapEventDetail *detail = history[eventName];
        NSDictionary * _inner = [self _eventDetailToDict:detail];
        result[eventName] = _inner;
    }
    [self returnResult:result withCallback:callback andError:nil];
}


#pragma mark Profile API

RCT_EXPORT_METHOD(setLocation:(double)latitude longitude:(double)longitude) {
    RCTLogInfo(@"[CleverTap setLocation: %f %f]", latitude, longitude);
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(latitude, longitude);
    [CleverTap setLocation:coordinate];
}

RCT_EXPORT_METHOD(profileGetCleverTapAttributionIdentifier:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapAttributionIdentifier]");
    NSString *result = [[CleverTap sharedInstance] profileGetCleverTapAttributionIdentifier];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileGetCleverTapID:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapID]");
    NSString *result = [[CleverTap sharedInstance] profileGetCleverTapID];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(onUserLogin:(NSDictionary*)profile) {
    RCTLogInfo(@"[CleverTap onUserLogin: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[CleverTap sharedInstance] onUserLogin:_profile];
}

RCT_EXPORT_METHOD(profileSet:(NSDictionary*)profile) {
    RCTLogInfo(@"[CleverTap profileSet: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[CleverTap sharedInstance] profilePush:_profile];
}

RCT_EXPORT_METHOD(profileSetGraphUser:(NSDictionary*)profile) {
    RCTLogInfo(@"[CleverTap profileSetGraphUser: %@]", profile);
    [[CleverTap sharedInstance] profilePushGraphUser:profile];
}

RCT_EXPORT_METHOD(profileGetProperty:(NSString*)propertyName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetProperty: %@]", propertyName);
    id result = [[CleverTap sharedInstance] profileGet:propertyName];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileRemoveValueForKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveValueForKey: %@]", key);
    [[CleverTap sharedInstance] profileRemoveValueForKey:key];
}

RCT_EXPORT_METHOD(profileSetMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileSetMultiValues: %@ forKey: %@]", values, key);
    [[CleverTap sharedInstance] profileSetMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValue:(NSString*)value forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileAddMultiValue: %@ forKey: %@]", value, key);
    [[CleverTap sharedInstance] profileAddMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileAddMultiValues: %@ forKey: %@]", values, key);
    [[CleverTap sharedInstance] profileAddMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValue:(NSString*)value forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValue: %@ forKey: %@]", value, key);
    [[CleverTap sharedInstance] profileRemoveMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValues: %@ forKey: %@]", values, key);
    [[CleverTap sharedInstance] profileRemoveMultiValues:values forKey:key];
}


#pragma mark Session API

RCT_EXPORT_METHOD(pushInstallReferrer:(NSString*)source medium:(NSString*)medium campaign:(NSString*)campaign) {
    RCTLogInfo(@"[CleverTap pushInstallReferrer source: %@ medium: %@ campaign: %@]", source, medium, campaign);
    [[CleverTap sharedInstance] pushInstallReferrerSource:source medium:medium campaign:campaign];
}

RCT_EXPORT_METHOD(sessionGetTimeElapsed:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTimeElapsed]");
    NSTimeInterval result = [[CleverTap sharedInstance] sessionGetTimeElapsed];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetTotalVisits:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTotalVisits]");
    int result = [[CleverTap sharedInstance] userGetTotalVisits];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetScreenCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetScreenCount]");
    int result = [[CleverTap sharedInstance] userGetScreenCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetPreviousVisitTime:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetPreviousVisitTime]");
    NSTimeInterval result = [[CleverTap sharedInstance] userGetPreviousVisitTime];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetUTMDetails:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetUTMDetails]");
    CleverTapUTMDetail *detail = [[CleverTap sharedInstance] sessionGetUTMDetails];
    NSDictionary *result = [self _utmDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}


#pragma mark Developer Options

RCT_EXPORT_METHOD(setDebugLevel:(int)level) {
    RCTLogInfo(@"[CleverTap setDebugLevel: %i]", level);
    [CleverTap setDebugLevel:level];
}


#pragma mark private/helpers

- (void)returnResult:(id)result withCallback:(RCTResponseSenderBlock)callback andError:(NSString *)error {
    if (callback == nil) {
        RCTLogInfo(@"CleverTap callback was nil");
        return;
    }
    id e  = error != nil ? error : [NSNull null];
    id r  = result != nil ? result : [NSNull null];
    callback(@[e,r]);
}

- (NSDictionary*)_eventDetailToDict:(CleverTapEventDetail*)detail {
    NSMutableDictionary *_dict = [NSMutableDictionary new];
    
    if(detail) {
        if(detail.eventName) {
            [_dict setObject:detail.eventName forKey:@"eventName"];
        }
        
        if(detail.firstTime){
            [_dict setObject:@(detail.firstTime) forKey:@"firstTime"];
        }
        
        if(detail.lastTime){
            [_dict setObject:@(detail.lastTime) forKey:@"lastTime"];
        }
        
        if(detail.count){
            [_dict setObject:@(detail.count) forKey:@"count"];
        }
    }
    
    return _dict;
}

- (NSDictionary*)_utmDetailToDict:(CleverTapUTMDetail*)detail {
    NSMutableDictionary *_dict = [NSMutableDictionary new];
    
    if(detail) {
        if(detail.source) {
            [_dict setObject:detail.source forKey:@"source"];
        }
        
        if(detail.medium) {
            [_dict setObject:detail.medium forKey:@"medium"];
        }
        
        if(detail.campaign) {
            [_dict setObject:detail.campaign forKey:@"campaign"];
        }
    }
    
    return _dict;
}

- (NSDictionary *)formatProfile:(NSDictionary *)profile {
    NSMutableDictionary *_profile = [NSMutableDictionary new];
    
    for (NSString *key in [profile keyEnumerator]) {
        id value = [profile objectForKey:key];
        
        if([key isEqualToString:@"DOB"]) {
            
            NSDate *dob = nil;
            
            if([value isKindOfClass:[NSString class]]) {
                
                if(!dateFormatter) {
                    dateFormatter = [[NSDateFormatter alloc] init];
                    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
                }
                
                dob = [dateFormatter dateFromString:value];
                
            }
            else if ([value isKindOfClass:[NSNumber class]]) {
                dob = [NSDate dateWithTimeIntervalSince1970:[value doubleValue]];
            }
            
            if(dob) {
                value = dob;
            }
        }
        
        [_profile setObject:value forKey:key];
    }
    
    return _profile;
}


@end
