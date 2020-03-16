#import "CleverTapReact.h"
#import "CleverTapReactManager.h"

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>

#import <React/RCTLog.h>

#import "CleverTap.h"
#import "CleverTap+Inbox.h"
#import "CleverTap+ABTesting.h"
#import "CleverTapEventDetail.h"
#import "CleverTapUTMDetail.h"
#import "CleverTap+DisplayUnit.h"

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
        kCleverTapInboxDidInitialize: kCleverTapInboxDidInitialize,
        kCleverTapInboxMessagesDidUpdate: kCleverTapInboxMessagesDidUpdate,
        kCleverTapExperimentsDidUpdate: kCleverTapExperimentsDidUpdate,
        kCleverTapInboxMessageButtonTapped: kCleverTapInboxMessageButtonTapped,
        kCleverTapInAppNotificationButtonTapped: kCleverTapInAppNotificationButtonTapped,
        kCleverTapDisplayUnitsLoaded: kCleverTapDisplayUnitsLoaded,
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

#pragma mark Personalization

RCT_EXPORT_METHOD(enablePersonalization) {
    RCTLogInfo(@"[CleverTap enablePersonalization]");
    [CleverTap enablePersonalization];
}

RCT_EXPORT_METHOD(disablePersonalization) {
    RCTLogInfo(@"[CleverTap disablePersonalization]");
    [CleverTap disablePersonalization];
}

#pragma mark Offline API

RCT_EXPORT_METHOD(setOffline:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap setOffline:  %i]", enabled);
    [[CleverTap sharedInstance] setOffline:enabled];
}

#pragma mark OptOut API

RCT_EXPORT_METHOD(setOptOut:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap setOptOut:  %i]", enabled);
    [[CleverTap sharedInstance] setOptOut:enabled];
}

RCT_EXPORT_METHOD(enableDeviceNetworkInfoReporting:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap enableDeviceNetworkInfoReporting: %i]", enabled);
    [[CleverTap sharedInstance] enableDeviceNetworkInfoReporting:enabled];
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

#pragma mark no-op Android O methods

RCT_EXPORT_METHOD(createNotificationChannel:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(NSInteger)importance withShowBadge:(BOOL)showBadge){
    RCTLogInfo(@"[CleverTap createNotificationChannel is no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelWithSound:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(NSInteger)importance withShowBadge:(BOOL)showBadge withSound:(NSString*)sound){
    RCTLogInfo(@"[CleverTap createNotificationChannelWithSound is no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelWithGroupId:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(NSInteger)importance withGroupId:(NSString*)groupId withShowBadge:(BOOL)showBadge){
    RCTLogInfo(@"[CleverTap createNotificationChannelWithGroupId is no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelWithGroupIdAndSound:(NSString*)channelId withChannelName:(NSString*)channelName withChannelDescription:(NSString*)channelDescription withImportance:(NSInteger)importance withGroupId:(NSString*)groupId withShowBadge:(BOOL)showBadge withSound:(NSString*)sound){
    RCTLogInfo(@"[CleverTap createNotificationChannelWithGroupIdAndSound is no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotificationChannelGroup:(NSString*)groupId withGroupName:(NSString*)groupName){
    RCTLogInfo(@"[CleverTap createNotificationChannelGroup is no-op in iOS]");
}

RCT_EXPORT_METHOD(deleteNotificationChannel:(NSString*)channelId){
    RCTLogInfo(@"[CleverTap deleteNotificationChannel is no-op in iOS]");
}

RCT_EXPORT_METHOD(deleteNotificationChannelGroup:(NSString*)groupId){
    RCTLogInfo(@"[CleverTap deleteNotificationChannelGroup is no-op in iOS]");
}

RCT_EXPORT_METHOD(createNotification:(NSDictionary*)extras) {
    RCTLogInfo(@"[CleverTap createNotification is no-op in iOS]");
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

#pragma mark App Inbox

RCT_EXPORT_METHOD(getInboxMessageCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageCount]");
    int result = (int)[[CleverTap sharedInstance] getInboxMessageCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageUnreadCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageUnreadCount]");
    int result = (int)[[CleverTap sharedInstance] getInboxMessageUnreadCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getAllInboxMessages:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[CleverTap sharedInstance] getAllInboxMessages];
    NSMutableArray *allMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [allMessages addObject:message.json];
    }
    NSArray *result = [allMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUnreadInboxMessages:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUnreadInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[CleverTap sharedInstance] getUnreadInboxMessages];
    NSMutableArray *unreadMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [unreadMessages addObject:message.json];
    }
    NSArray *result = [unreadMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageForId:(NSString*)messageId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getInboxMessageForId]");
    CleverTapInboxMessage * message = [[CleverTap sharedInstance] getInboxMessageForId:messageId];
    NSDictionary *result = message.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushInboxNotificationViewedEventForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationViewedEventForId]");
    [[CleverTap sharedInstance] recordInboxNotificationViewedEventForID:messageId];
}

RCT_EXPORT_METHOD(pushInboxNotificationClickedEventForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationClickedEventForId]");
    [[CleverTap sharedInstance] recordInboxNotificationClickedEventForID:messageId];
}

RCT_EXPORT_METHOD(markReadInboxMessageForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap markReadInboxMessageForId]");
    [[CleverTap sharedInstance] markReadInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(deleteInboxMessageForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap deleteInboxMessageForId]");
    [[CleverTap sharedInstance] deleteInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(initializeInbox) {
    RCTLogInfo(@"[CleverTap Inbox Initialize]");
    [[CleverTap sharedInstance] initializeInboxWithCallback:^(BOOL success) {
        if (success) {
            RCTLogInfo(@"[Inbox initialized]");
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxDidInitialize object:nil userInfo:nil];
            [[CleverTap sharedInstance] registerInboxUpdatedBlock:^{
                RCTLogInfo(@"[Inbox updated]");
                [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessagesDidUpdate object:nil userInfo:nil];
            }];
        }
    }];
}

RCT_EXPORT_METHOD(showInbox:(NSDictionary*)styleConfig) {
    RCTLogInfo(@"[CleverTap Show Inbox]");
    CleverTapInboxViewController *inboxController = [[CleverTap sharedInstance] newInboxViewControllerWithConfig:[self _dictToInboxStyleConfig:styleConfig? styleConfig : nil] andDelegate:(id <CleverTapInboxViewControllerDelegate>)self];
    if (inboxController) {
        UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:inboxController];
        UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
        UIViewController *mainViewController = keyWindow.rootViewController;
        [mainViewController presentViewController:navigationController animated:YES completion:nil];
    }
}

- (CleverTapInboxStyleConfig*)_dictToInboxStyleConfig: (NSDictionary *)dict {
    CleverTapInboxStyleConfig *_config = [CleverTapInboxStyleConfig new];
    NSString *title = [dict valueForKey:@"navBarTitle"];
    if (title) {
        _config.title = title;
    }
    NSArray *messageTags = [dict valueForKey:@"tabs"];
    if (messageTags) {
        _config.messageTags = messageTags;
    }
    NSString *backgroundColor = [dict valueForKey:@"inboxBackgroundColor"];
    if (backgroundColor) {
        _config.backgroundColor = [self ct_colorWithHexString:backgroundColor alpha:1.0];
    }
    NSString *navigationBarTintColor = [dict valueForKey:@"navBarColor"];
    if (navigationBarTintColor) {
        _config.navigationBarTintColor = [self ct_colorWithHexString:navigationBarTintColor alpha:1.0];
    }
    NSString *navigationTintColor = [dict valueForKey:@"navBarTitleColor"];
    if (navigationTintColor) {
        _config.navigationTintColor = [self ct_colorWithHexString:navigationTintColor alpha:1.0];
    }
    NSString *tabBackgroundColor = [dict valueForKey:@"tabBackgroundColor"];
    if (tabBackgroundColor) {
        _config.navigationBarTintColor = [self ct_colorWithHexString:tabBackgroundColor alpha:1.0];
    }
    NSString *tabSelectedBgColor = [dict valueForKey:@"tabSelectedBgColor"];
    if (tabSelectedBgColor) {
        _config.tabSelectedBgColor = [self ct_colorWithHexString:tabSelectedBgColor alpha:1.0];
    }
    NSString *tabSelectedTextColor = [dict valueForKey:@"tabSelectedTextColor"];
    if (tabSelectedTextColor) {
        _config.tabSelectedTextColor = [self ct_colorWithHexString:tabSelectedTextColor alpha:1.0];
    }
    NSString *tabUnSelectedTextColor = [dict valueForKey:@"tabUnSelectedTextColor"];
    if (tabUnSelectedTextColor) {
        _config.tabUnSelectedTextColor = [self ct_colorWithHexString:tabUnSelectedTextColor alpha:1.0];
    }
    return _config;
}
- (UIColor *)ct_colorWithHexString:(NSString *)string alpha:(CGFloat)alpha{
    if (![string isKindOfClass:[NSString class]] || [string length] == 0) {
        return [UIColor colorWithRed:0.0f green:0.0f blue:0.0f alpha:1.0f];
    }
    unsigned int hexint = 0;
    NSScanner *scanner = [NSScanner scannerWithString:string];
    [scanner setCharactersToBeSkipped:[NSCharacterSet
                                       characterSetWithCharactersInString:@"#"]];
    [scanner scanHexInt:&hexint];
    UIColor *color =
    [UIColor colorWithRed:((CGFloat) ((hexint & 0xFF0000) >> 16))/255
                    green:((CGFloat) ((hexint & 0xFF00) >> 8))/255
                     blue:((CGFloat) (hexint & 0xFF))/255
                    alpha:alpha];
    return color;
}

- (void)messageButtonTappedWithCustomExtras:(NSDictionary *)customExtras {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if (customExtras != nil) {
        body[@"customExtras"] = customExtras;
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessageButtonTapped object:nil userInfo:body];
}

#pragma mark Dynamic Variables

RCT_EXPORT_METHOD(setUIEditorConnectionEnabled:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap setUIEditorConnectionEnabled:  %i]", enabled);
    [CleverTap setUIEditorConnectionEnabled:enabled];
}

RCT_EXPORT_METHOD(registerBooleanVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerBoolVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerBoolVariableWithName:name];
}

RCT_EXPORT_METHOD(registerDoubleVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerDoubleVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerDoubleVariableWithName:name];
}

RCT_EXPORT_METHOD(registerStringVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerBoolVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerStringVariableWithName:name];
}

RCT_EXPORT_METHOD(registerIntegerVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerBoolVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerIntegerVariableWithName:name];
}

RCT_EXPORT_METHOD(registerListOfBooleanVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerArrayOfBoolVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerArrayOfBoolVariableWithName:name];
}

RCT_EXPORT_METHOD(registerListOfDoubleVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerArrayOfDoubleVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerArrayOfDoubleVariableWithName:name];
}

RCT_EXPORT_METHOD(registerListOfStringVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerArrayOfStringVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerArrayOfStringVariableWithName:name];
}

RCT_EXPORT_METHOD(registerListOfIntegerVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerArrayOfIntegerVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerArrayOfIntegerVariableWithName:name];
}

RCT_EXPORT_METHOD(registerMapOfBooleanVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerDictionaryOfBoolVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerDictionaryOfBoolVariableWithName:name];
}

RCT_EXPORT_METHOD(registerMapOfDoubleVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerDictionaryOfDoubleVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerDictionaryOfDoubleVariableWithName:name];
}

RCT_EXPORT_METHOD(registerMapOfStringVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerDictionaryOfStringVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerDictionaryOfStringVariableWithName:name];
}

RCT_EXPORT_METHOD(registerMapOfIntegerVariable:(NSString *)name) {
    RCTLogInfo(@"[CleverTap registerDictionaryOfIntegerVariableWithName: %@]", name);
    [[CleverTap sharedInstance] registerDictionaryOfIntegerVariableWithName:name];
}

RCT_EXPORT_METHOD(getBooleanVariable:(NSString* _Nonnull)name defaultValue:(BOOL)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getBoolVariableWithName:defaultValue]");
    BOOL result = [[CleverTap sharedInstance] getBoolVariableWithName:name defaultValue:defaultValue];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getStringVariable:(NSString* _Nonnull)name defaultValue:(NSString * _Nonnull)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getStringVariableWithName:defaultValue]");
    NSString *result = [[CleverTap sharedInstance] getStringVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getDoubleVariable:(NSString* _Nonnull)name defaultValue:(double)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDoubleVariableWithName:defaultValue]");
    double result = [[CleverTap sharedInstance] getDoubleVariableWithName:name defaultValue:defaultValue];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getIntegerVariable:(NSString* _Nonnull)name defaultValue:(int)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getIntegerVariableWithName:defaultValue]");
    int result = [[CleverTap sharedInstance] getIntegerVariableWithName:name defaultValue:defaultValue];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getListOfBooleanVariable:(NSString* _Nonnull)name defaultValue:(NSArray<NSNumber*>* _Nonnull)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getArrayOfBoolVariableWithName:defaultValue]");
    NSArray *result = [[CleverTap sharedInstance] getArrayOfBoolVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getListOfStringVariable:(NSString* _Nonnull)name defaultValue:(NSArray<NSString*>* _Nonnull)defaultValue callback:(RCTResponseSenderBlock)callback)  {
    RCTLogInfo(@"[CleverTap getArrayOfStringVariableWithName:defaultValue]");
    NSArray *result = [[CleverTap sharedInstance] getArrayOfStringVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getListOfDoubleVariable:(NSString* _Nonnull)name defaultValue:(NSArray<NSNumber*>* _Nonnull)defaultValue callback:(RCTResponseSenderBlock)callback)  {
    RCTLogInfo(@"[CleverTap getArrayOfStringVariableWithName:defaultValue]");
    NSArray *result = [[CleverTap sharedInstance] getArrayOfDoubleVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getListOfIntegerVariable:(NSString* _Nonnull)name defaultValue:(NSArray<NSNumber*>* _Nonnull)defaultValue callback:(RCTResponseSenderBlock)callback)  {
    RCTLogInfo(@"[CleverTap getArrayOfStringVariableWithName:defaultValue]");
    NSArray *result = [[CleverTap sharedInstance] getArrayOfIntegerVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getMapOfBooleanVariable:(NSString* _Nonnull)name defaultValue:(NSDictionary*)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDictionaryOfBoolVariableWithName:defaultValue]");
    NSDictionary *result = [[CleverTap sharedInstance] getDictionaryOfBoolVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getMapOfStringVariable:(NSString* _Nonnull)name defaultValue:(NSDictionary*)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDictionaryOfStringVariableWithName:defaultValue]");
    NSDictionary *result = [[CleverTap sharedInstance] getDictionaryOfStringVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getMapOfDoubleVariable:(NSString* _Nonnull)name defaultValue:(NSDictionary*)defaultValue callback:(RCTResponseSenderBlock)callback)  {
    RCTLogInfo(@"[CleverTap getDictionaryOfDoubleVariableWithName:defaultValue]");
    NSDictionary *result = [[CleverTap sharedInstance] getDictionaryOfDoubleVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getMapOfIntegerVariable:(NSString* _Nonnull)name defaultValue:(NSDictionary*)defaultValue callback:(RCTResponseSenderBlock)callback)  {
    RCTLogInfo(@"[CleverTap getDictionaryOfIntegerVariableWithName:defaultValue]");
    NSDictionary *result = [[CleverTap sharedInstance] getDictionaryOfIntegerVariableWithName:name defaultValue:defaultValue];
    [self returnResult:result withCallback:callback andError:nil];
}

#pragma mark Display Units

RCT_EXPORT_METHOD(getAllDisplayUnits:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllDisplayUnits]");
    NSArray <CleverTapDisplayUnit*> *units = [[CleverTap sharedInstance] getAllDisplayUnits];
    NSMutableArray *displayUnits = [NSMutableArray new];
    for (CleverTapDisplayUnit *unit in units) {
        [displayUnits addObject:unit.json];
    }
    NSArray *result = [displayUnits mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getDisplayUnitForId:(NSString*)unitId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDisplayUnitForId]");
    CleverTapDisplayUnit * displayUnit = [[CleverTap sharedInstance] getDisplayUnitForID:unitId];
    NSDictionary *result = displayUnit.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushDisplayUnitViewedEventForID:(NSString*)unitId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitViewedEventForID]");
    [[CleverTap sharedInstance] recordDisplayUnitViewedEventForID:unitId];
}

RCT_EXPORT_METHOD(pushDisplayUnitClickedEventForID:(NSString*)unitId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitClickedEventForID]");
    [[CleverTap sharedInstance] recordDisplayUnitClickedEventForID:unitId];
}

@end
