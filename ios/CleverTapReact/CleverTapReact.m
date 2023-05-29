#import "CleverTapReact.h"
#import "CleverTapReactManager.h"

#import <UserNotifications/UserNotifications.h>
#import <CoreLocation/CoreLocation.h>

#import <React/RCTLog.h>

#import "CleverTap.h"
#import "CleverTap+Inbox.h"
#import "CleverTapEventDetail.h"
#import "CleverTapUTMDetail.h"
#import "CleverTap+DisplayUnit.h"
#import "CleverTap+FeatureFlags.h"
#import "CleverTap+ProductConfig.h"
#import "CleverTap+InAppNotifications.h"
#import "CleverTapInstanceConfig.h"
#import "CTLocalInApp.h"
#import "Clevertap+PushPermission.h"
#import "CleverTap+CTVar.h"
#import "CTVar.h"

static NSDateFormatter *dateFormatter;

@interface CleverTapReact()
@property CleverTap *cleverTapInstance;
@property(nonatomic, strong) NSMutableDictionary *allVariables;
@end

@implementation CleverTapReact

@synthesize cleverTapInstance = _cleverTapInstance;

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
        kCleverTapInboxMessageButtonTapped: kCleverTapInboxMessageButtonTapped,
        kCleverTapInboxMessageTapped: kCleverTapInboxMessageTapped,
        kCleverTapInAppNotificationButtonTapped: kCleverTapInAppNotificationButtonTapped,
        kCleverTapDisplayUnitsLoaded: kCleverTapDisplayUnitsLoaded,
        kCleverTapFeatureFlagsDidUpdate: kCleverTapFeatureFlagsDidUpdate,
        kCleverTapProductConfigDidFetch: kCleverTapProductConfigDidFetch,
        kCleverTapProductConfigDidActivate: kCleverTapProductConfigDidActivate,
        kCleverTapProductConfigDidInitialize: kCleverTapProductConfigDidInitialize,
        kCleverTapPushNotificationClicked: kCleverTapPushNotificationClicked,
        kCleverTapPushPermissionResponseReceived: kCleverTapPushPermissionResponseReceived,
        kCleverTapInAppNotificationShowed: kCleverTapInAppNotificationShowed,
        kCleverTapOnVariablesChanged:
            kCleverTapOnVariablesChanged,
        kCleverTapOnValueChanged:
            kCleverTapOnValueChanged,
        kXPS: kXPS
    };
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}


# pragma mark - Launch

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.allVariables = [NSMutableDictionary dictionary];
    }
    return self;
}

- (CleverTap *)cleverTapInstance {
    if (_cleverTapInstance != nil) {
        return _cleverTapInstance;
    }
    return [CleverTap sharedInstance];
}

- (void)setCleverTapInstance:(CleverTap *)instance {
    _cleverTapInstance = instance;
}

RCT_EXPORT_METHOD(setInstanceWithAccountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setInstanceWithAccountId]");
    
    CleverTap *instance = [CleverTap getGlobalInstance:accountId];
    if (instance == nil) {
        RCTLogWarn(@"CleverTapInstance not found for accountId: %@", accountId);
        return;
    }
    
    [self setCleverTapInstance:instance];
    [[CleverTapReactManager sharedInstance] setDelegates:instance];
}

RCT_EXPORT_METHOD(getInitialUrl:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getInitialUrl]");
    NSString *launchDeepLink = [CleverTapReactManager sharedInstance].launchDeepLink;
    if (launchDeepLink != nil) {
        [self returnResult:launchDeepLink withCallback:callback andError:nil];
    } else {
        [self returnResult:nil withCallback:callback andError:@"CleverTap initialUrl is nil"];
    }
}

RCT_EXPORT_METHOD(setLibrary:(NSString*)name andVersion:(int)version) {
    RCTLogInfo(@"[CleverTap setLibrary:%@ andVersion:%d]", name, version);
    [[self cleverTapInstance] setLibrary:name];
    [[self cleverTapInstance] setCustomSdkVersion:name version:version];
}

#pragma mark - Push Notifications

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
    [[self cleverTapInstance] setPushTokenAsString:token];
}

// setPushTokenAsStringWithRegion is a no-op in iOS
RCT_EXPORT_METHOD(setPushTokenAsStringWithRegion:(NSString*)token withType:(NSString *)type withRegion:(NSString *)region){
    RCTLogInfo(@"[CleverTap setPushTokenAsStringWithRegion is no-op in iOS]");
}

#pragma mark - Personalization

RCT_EXPORT_METHOD(enablePersonalization) {
    RCTLogInfo(@"[CleverTap enablePersonalization]");
    [CleverTap enablePersonalization];
}

RCT_EXPORT_METHOD(disablePersonalization) {
    RCTLogInfo(@"[CleverTap disablePersonalization]");
    [CleverTap disablePersonalization];
}


#pragma mark - Offline API

RCT_EXPORT_METHOD(setOffline:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap setOffline:  %i]", enabled);
    [[self cleverTapInstance] setOffline:enabled];
}


#pragma mark - OptOut API

RCT_EXPORT_METHOD(setOptOut:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap setOptOut:  %i]", enabled);
    [[self cleverTapInstance] setOptOut:enabled];
}

RCT_EXPORT_METHOD(enableDeviceNetworkInfoReporting:(BOOL)enabled) {
    RCTLogInfo(@"[CleverTap enableDeviceNetworkInfoReporting: %i]", enabled);
    [[self cleverTapInstance] enableDeviceNetworkInfoReporting:enabled];
}


#pragma mark - Event API

RCT_EXPORT_METHOD(recordScreenView:(NSString*)screenName) {
    RCTLogInfo(@"[CleverTap recordScreenView]");
    [[self cleverTapInstance] recordScreenView:screenName];
}

RCT_EXPORT_METHOD(recordEvent:(NSString*)eventName withProps:(NSDictionary*)props) {
    RCTLogInfo(@"[CleverTap recordEvent:withProps]");
    [[self cleverTapInstance] recordEvent:eventName withProps:props];
}

RCT_EXPORT_METHOD(recordChargedEvent:(NSDictionary*)details andItems:(NSArray*)items) {
    RCTLogInfo(@"[CleverTap recordChargedEventWithDetails:andItems:]");
    [[self cleverTapInstance] recordChargedEventWithDetails:details andItems:items];
}

RCT_EXPORT_METHOD(eventGetFirstTime:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetFirstTime: %@]", eventName);
    NSTimeInterval result = [[self cleverTapInstance] eventGetFirstTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetLastTime:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetLastTime: %@]", eventName);
    NSTimeInterval result = [[self cleverTapInstance] eventGetLastTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetOccurrences:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetOccurrences: %@]", eventName);
    int result = [[self cleverTapInstance] eventGetOccurrences:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetDetail:(NSString*)eventName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetDetail: %@]", eventName);
    CleverTapEventDetail *detail = [[self cleverTapInstance] eventGetDetail:eventName];
    NSDictionary *result = [self _eventDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getEventHistory:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getEventHistory]");
    NSDictionary *history = [[self cleverTapInstance] userGetEventHistory];
    NSMutableDictionary *result = [NSMutableDictionary new];
    
    for (NSString *eventName in [history keyEnumerator]) {
        CleverTapEventDetail *detail = history[eventName];
        NSDictionary * _inner = [self _eventDetailToDict:detail];
        result[eventName] = _inner;
    }
    [self returnResult:result withCallback:callback andError:nil];
}


#pragma mark - Profile API

RCT_EXPORT_METHOD(setLocation:(double)latitude longitude:(double)longitude) {
    RCTLogInfo(@"[CleverTap setLocation: %f %f]", latitude, longitude);
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(latitude, longitude);
    [CleverTap setLocation:coordinate];
}

RCT_EXPORT_METHOD(profileGetCleverTapAttributionIdentifier:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapAttributionIdentifier]");
    NSString *result = [[self cleverTapInstance] profileGetCleverTapAttributionIdentifier];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileGetCleverTapID:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapID]");
    NSString *result = [[self cleverTapInstance] profileGetCleverTapID];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getCleverTapID:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getCleverTapID]");
    NSString *result = [[self cleverTapInstance] profileGetCleverTapID];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(onUserLogin:(NSDictionary*)profile) {
    RCTLogInfo(@"[CleverTap onUserLogin: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[self cleverTapInstance] onUserLogin:_profile];
}

RCT_EXPORT_METHOD(profileSet:(NSDictionary*)profile) {
    RCTLogInfo(@"[CleverTap profileSet: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[self cleverTapInstance] profilePush:_profile];
}

RCT_EXPORT_METHOD(profileGetProperty:(NSString*)propertyName callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetProperty: %@]", propertyName);
    id result = [[self cleverTapInstance] profileGet:propertyName];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileRemoveValueForKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveValueForKey: %@]", key);
    [[self cleverTapInstance] profileRemoveValueForKey:key];
}

RCT_EXPORT_METHOD(profileSetMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileSetMultiValues: %@ forKey: %@]", values, key);
    [[self cleverTapInstance] profileSetMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValue:(NSString*)value forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileAddMultiValue: %@ forKey: %@]", value, key);
    [[self cleverTapInstance] profileAddMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileAddMultiValues: %@ forKey: %@]", values, key);
    [[self cleverTapInstance] profileAddMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValue:(NSString*)value forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValue: %@ forKey: %@]", value, key);
    [[self cleverTapInstance] profileRemoveMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValues: %@ forKey: %@]", values, key);
    [[self cleverTapInstance] profileRemoveMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileIncrementValueForKey:(NSNumber* _Nonnull)value forKey:(NSString* _Nonnull)key) {
    RCTLogInfo(@"[CleverTap profileIncrementValueBy: %@ forKey: %@]", value, key);
    [[self cleverTapInstance] profileIncrementValueBy:value forKey:key];
}

RCT_EXPORT_METHOD(profileDecrementValueForKey:(NSNumber* _Nonnull)value forKey:(NSString* _Nonnull)key) {
    RCTLogInfo(@"[CleverTap profileDecrementValueBy: %@ forKey: %@]", value, key);
    [[self cleverTapInstance] profileDecrementValueBy:value forKey:key];
}

#pragma mark - Session API

RCT_EXPORT_METHOD(pushInstallReferrer:(NSString*)source medium:(NSString*)medium campaign:(NSString*)campaign) {
    RCTLogInfo(@"[CleverTap pushInstallReferrer source: %@ medium: %@ campaign: %@]", source, medium, campaign);
    [[self cleverTapInstance] pushInstallReferrerSource:source medium:medium campaign:campaign];
}

RCT_EXPORT_METHOD(sessionGetTimeElapsed:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTimeElapsed]");
    NSTimeInterval result = [[self cleverTapInstance] sessionGetTimeElapsed];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetTotalVisits:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTotalVisits]");
    int result = [[self cleverTapInstance] userGetTotalVisits];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetScreenCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetScreenCount]");
    int result = [[self cleverTapInstance] userGetScreenCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetPreviousVisitTime:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetPreviousVisitTime]");
    NSTimeInterval result = [[self cleverTapInstance] userGetPreviousVisitTime];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetUTMDetails:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetUTMDetails]");
    CleverTapUTMDetail *detail = [[self cleverTapInstance] sessionGetUTMDetails];
    NSDictionary *result = [self _utmDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}


#pragma mark - no-op Android O methods

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


#pragma mark - Developer Options

RCT_EXPORT_METHOD(setDebugLevel:(int)level) {
    RCTLogInfo(@"[CleverTap setDebugLevel: %i]", level);
    [CleverTap setDebugLevel:level];
}


#pragma mark - Private/Helpers

- (void)returnResult:(id)result withCallback:(RCTResponseSenderBlock)callback andError:(NSString *)error {
    if (callback == nil) {
        RCTLogInfo(@"CleverTap callback was nil");
        return;
    }
    id e  = error != nil ? error : [NSNull null];
    id r  = result != nil ? result : [NSNull null];
    callback(@[e,r]);
}

- (NSDictionary *)_eventDetailToDict:(CleverTapEventDetail*)detail {
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

- (NSDictionary *)_utmDetailToDict:(CleverTapUTMDetail*)detail {
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

- (CTVar *)createVarForName:(NSString *)name andValue:(id)value {

    if ([value isKindOfClass:[NSString class]]) {
        return [[self cleverTapInstance]defineVar:name withString:value];
    }
    if ([value isKindOfClass:[NSDictionary class]]) {
        return [[self cleverTapInstance]defineVar:name withDictionary:value];
    }
    if ([value isKindOfClass:[NSNumber class]]) {
        if ([self isBoolNumber:value]) {
            return [[self cleverTapInstance]defineVar:name withBool:value];
        }
        return [[self cleverTapInstance]defineVar:name withNumber:value];
    }
    return nil;
}

- (BOOL)isBoolNumber:(NSNumber *)number {
    CFTypeID boolID = CFBooleanGetTypeID();
    CFTypeID numID = CFGetTypeID(CFBridgingRetain(number));
    return (numID == boolID);
}

- (NSMutableDictionary *)getVariableValues {
    NSMutableDictionary *varValues = [NSMutableDictionary dictionary];
    [self.allVariables enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, CTVar*  _Nonnull var, BOOL * _Nonnull stop) {
        varValues[key] = var.value;
    }];
    return varValues;
}

#pragma mark - App Inbox

RCT_EXPORT_METHOD(getInboxMessageCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageCount]");
    int result = (int)[[self cleverTapInstance] getInboxMessageCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageUnreadCount:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageUnreadCount]");
    int result = (int)[[self cleverTapInstance] getInboxMessageUnreadCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getAllInboxMessages:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[self cleverTapInstance] getAllInboxMessages];
    NSMutableArray *allMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [allMessages addObject:message.json];
    }
    NSArray *result = [allMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUnreadInboxMessages:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUnreadInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[self cleverTapInstance] getUnreadInboxMessages];
    NSMutableArray *unreadMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [unreadMessages addObject:message.json];
    }
    NSArray *result = [unreadMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageForId:(NSString*)messageId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getInboxMessageForId]");
    CleverTapInboxMessage * message = [[self cleverTapInstance] getInboxMessageForId:messageId];
    NSDictionary *result = message.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushInboxNotificationViewedEventForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationViewedEventForId]");
    [[self cleverTapInstance] recordInboxNotificationViewedEventForID:messageId];
}

RCT_EXPORT_METHOD(pushInboxNotificationClickedEventForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationClickedEventForId]");
    [[self cleverTapInstance] recordInboxNotificationClickedEventForID:messageId];
}

RCT_EXPORT_METHOD(markReadInboxMessageForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap markReadInboxMessageForId]");
    [[self cleverTapInstance] markReadInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(deleteInboxMessageForId:(NSString*)messageId) {
    RCTLogInfo(@"[CleverTap deleteInboxMessageForId]");
    [[self cleverTapInstance] deleteInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(markReadInboxMessagesForIDs:(NSArray*)messageIds) {
    if (!messageIds) return;
    RCTLogInfo(@"[CleverTap markReadInboxMessagesForIDs]");
    [[self cleverTapInstance] markReadInboxMessagesForIDs:messageIds];
}

RCT_EXPORT_METHOD(deleteInboxMessagesForIDs:(NSArray*)messageIds) {
    if (!messageIds) return;
    RCTLogInfo(@"[CleverTap deleteInboxMessagesForIDs]");
    [[self cleverTapInstance] deleteInboxMessagesForIDs:messageIds];
}

RCT_EXPORT_METHOD(dismissInbox) {
    RCTLogInfo(@"[CleverTap dismissAppInbox]");
    [[self cleverTapInstance] dismissAppInbox];
}

RCT_EXPORT_METHOD(initializeInbox) {
    RCTLogInfo(@"[CleverTap Inbox Initialize]");
    [[self cleverTapInstance] initializeInboxWithCallback:^(BOOL success) {
        if (success) {
            RCTLogInfo(@"[Inbox initialized]");
            NSMutableDictionary *body = [NSMutableDictionary new];
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxDidInitialize object:nil userInfo:body];
            [[self cleverTapInstance] registerInboxUpdatedBlock:^{
                RCTLogInfo(@"[Inbox updated]");
                [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessagesDidUpdate object:nil userInfo:body];
            }];
        }
    }];
}

RCT_EXPORT_METHOD(showInbox:(NSDictionary*)styleConfig) {
    RCTLogInfo(@"[CleverTap Show Inbox]");
    CleverTapInboxViewController *inboxController = [[self cleverTapInstance] newInboxViewControllerWithConfig:[self _dictToInboxStyleConfig:styleConfig? styleConfig : nil] andDelegate:(id <CleverTapInboxViewControllerDelegate>)self];
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
    NSString *noMessageTextColor = [dict valueForKey:@"noMessageTextColor"];
    if (noMessageTextColor) {
        _config.noMessageViewTextColor = [self ct_colorWithHexString:noMessageTextColor alpha:1.0];
    }
    NSString *noMessageText = [dict valueForKey:@"noMessageText"];
    if (noMessageText) {
        _config.noMessageViewText = noMessageText;
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
        body = [NSMutableDictionary dictionaryWithDictionary:customExtras];
    }
    [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessageButtonTapped object:nil userInfo:body];
}

- (void)messageDidSelect:(CleverTapInboxMessage *_Nonnull)message atIndex:(int)index withButtonIndex:(int)buttonIndex {
    NSMutableDictionary *body = [NSMutableDictionary new];
    if ([message json] != nil) {
        body[@"data"] = [NSMutableDictionary dictionaryWithDictionary:[message json]];
    } else {
        body[@"data"] = [NSMutableDictionary new];
    }
    body[@"contentPageIndex"] = @(index);
    body[@"buttonIndex"] = @(buttonIndex);

    [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessageTapped object:nil userInfo:body];
}


#pragma mark - Display Units

RCT_EXPORT_METHOD(getAllDisplayUnits:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllDisplayUnits]");
    NSArray <CleverTapDisplayUnit*> *units = [[self cleverTapInstance] getAllDisplayUnits];
    NSMutableArray *displayUnits = [NSMutableArray new];
    for (CleverTapDisplayUnit *unit in units) {
        [displayUnits addObject:unit.json];
    }
    NSArray *result = [displayUnits mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getDisplayUnitForId:(NSString*)unitId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDisplayUnitForId]");
    CleverTapDisplayUnit * displayUnit = [[self cleverTapInstance] getDisplayUnitForID:unitId];
    NSDictionary *result = displayUnit.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushDisplayUnitViewedEventForID:(NSString*)unitId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitViewedEventForID]");
    [[self cleverTapInstance] recordDisplayUnitViewedEventForID:unitId];
}

RCT_EXPORT_METHOD(pushDisplayUnitClickedEventForID:(NSString*)unitId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitClickedEventForID]");
    [[self cleverTapInstance] recordDisplayUnitClickedEventForID:unitId];
}


# pragma mark - Feature Flag

RCT_EXPORT_METHOD(getFeatureFlag:(NSString*)flag withdefaultValue:(BOOL)defaultValue callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getFeatureFlag]");
    BOOL result = [[[self cleverTapInstance] featureFlags] get:flag withDefaultValue:defaultValue];
    [self returnResult:@(result) withCallback:callback andError:nil];
}


#pragma mark - Product Config

RCT_EXPORT_METHOD(setDefaultsMap:(NSDictionary*)jsonDict) {
    RCTLogInfo(@"[CleverTap setDefaultsMap]");
    [[[self cleverTapInstance] productConfig] setDefaults:jsonDict];
}

RCT_EXPORT_METHOD(fetch) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch]");
    [[[self cleverTapInstance] productConfig] fetch];
}

RCT_EXPORT_METHOD(fetchWithMinimumFetchIntervalInSeconds:(double)time) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch with minimum Interval]");
    [[[self cleverTapInstance] productConfig] fetchWithMinimumInterval: time];
}

RCT_EXPORT_METHOD(activate) {
    RCTLogInfo(@"[CleverTap ProductConfig Activate]");
    [[[self cleverTapInstance] productConfig] activate];
}

RCT_EXPORT_METHOD(fetchAndActivate) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch and Activate]");
    [[[self cleverTapInstance] productConfig] fetchAndActivate];
}

RCT_EXPORT_METHOD(setMinimumFetchIntervalInSeconds:(double)time) {
    RCTLogInfo(@"[CleverTap ProductConfig Minimum Time Interval Setup]");
    [[[self cleverTapInstance] productConfig] setMinimumFetchInterval: time];
}

RCT_EXPORT_METHOD(getLastFetchTimeStampInMillis:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap Last Fetch Config time]");
    NSTimeInterval result = [[[[self cleverTapInstance] productConfig] getLastFetchTimeStamp] timeIntervalSince1970] * 1000;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getString:(NSString*)key callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch String value for Key]");
    NSString *result = [[[self cleverTapInstance] productConfig] get:key].stringValue;
    [self returnResult: result withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getBoolean:(NSString*)key callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch Bool value for Key]");
    BOOL result = [[[self cleverTapInstance] productConfig] get:key].boolValue;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getDouble:(NSString*)key callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch Double value for Key]");
    long result = [[[self cleverTapInstance] productConfig] get:key].numberValue.doubleValue;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(reset) {
    RCTLogInfo(@"[CleverTap ProductConfig Reset]");
    [[[self cleverTapInstance] productConfig] reset];
}

#pragma mark - InApp Notification Controls

RCT_EXPORT_METHOD(suspendInAppNotifications) {
    RCTLogInfo(@"[CleverTap suspendInAppNotifications");
    [[self cleverTapInstance] suspendInAppNotifications];
}

RCT_EXPORT_METHOD(discardInAppNotifications) {
    RCTLogInfo(@"[CleverTap discardInAppNotifications");
    [[self cleverTapInstance] discardInAppNotifications];
}

RCT_EXPORT_METHOD(resumeInAppNotifications) {
    RCTLogInfo(@"[CleverTap resumeInAppNotifications");
    [[self cleverTapInstance] resumeInAppNotifications];
}

#pragma mark - Push Permission

- (CTLocalInApp*)_localInAppConfigFromReadableMap: (NSDictionary *)json {
    CTLocalInApp *inAppBuilder;
    CTLocalInAppType inAppType = HALF_INTERSTITIAL;
    //Required parameters
    NSString *titleText = nil, *messageText = nil, *followDeviceOrientation = nil, *positiveBtnText = nil, *negativeBtnText = nil;
    //Additional parameters
    NSString *fallbackToSettings = nil, *backgroundColor = nil, *btnBorderColor = nil, *titleTextColor = nil, *messageTextColor = nil, *btnTextColor = nil, *imageUrl = nil, *btnBackgroundColor = nil, *btnBorderRadius = nil;
    
    if ([json[@"inAppType"]  isEqual: @"half-interstitial"]){
        inAppType = HALF_INTERSTITIAL;
    }
    else {
        inAppType = ALERT;
    }
    if (json[@"titleText"]) {
        titleText = [json valueForKey:@"titleText"];
    }
    if (json[@"messageText"]) {
        messageText = [json valueForKey:@"messageText"];
    }
    if (json[@"followDeviceOrientation"]) {
        followDeviceOrientation = [json valueForKey:@"followDeviceOrientation"];
    }
    if (json[@"positiveBtnText"]) {
        positiveBtnText = [json valueForKey:@"positiveBtnText"];
    }
    
    if (json[@"negativeBtnText"]) {
        negativeBtnText = [json valueForKey:@"negativeBtnText"];
    }
    
    //creates the builder instance with all the required parameters
    inAppBuilder = [[CTLocalInApp alloc] initWithInAppType:inAppType
                                                 titleText:titleText
                                               messageText:messageText
                                   followDeviceOrientation:followDeviceOrientation
                                           positiveBtnText:positiveBtnText
                                           negativeBtnText:negativeBtnText];
    
    //adds optional parameters to the builder instance
    if (json[@"fallbackToSettings"]) {
        fallbackToSettings = [json valueForKey:@"fallbackToSettings"];
        [inAppBuilder setFallbackToSettings:fallbackToSettings];
    }
    if (json[@"backgroundColor"]) {
        backgroundColor = [json valueForKey:@"backgroundColor"];
        [inAppBuilder setBackgroundColor:backgroundColor];
    }
    if (json[@"btnBorderColor"]) {
        btnBorderColor = [json valueForKey:@"btnBorderColor"];
        [inAppBuilder setBtnBorderColor:btnBorderColor];
    }
    if (json[@"titleTextColor"]) {
        titleTextColor = [json valueForKey:@"titleTextColor"];
        [inAppBuilder setTitleTextColor:titleTextColor];
    }
    if (json[@"messageTextColor"]) {
        messageTextColor = [json valueForKey:@"messageTextColor"];
        [inAppBuilder setMessageTextColor:messageTextColor];
    }
    if (json[@"btnTextColor"]) {
        btnTextColor = [json valueForKey:@"btnTextColor"];
        [inAppBuilder setBtnTextColor:btnTextColor];
    }
    if (json[@"imageUrl"]) {
        imageUrl = [json valueForKey:@"imageUrl"];
        [inAppBuilder setImageUrl:imageUrl];
    }
    if (json[@"btnBackgroundColor"]) {
        btnBackgroundColor = [json valueForKey:@"btnBackgroundColor"];
        [inAppBuilder setBtnBackgroundColor:btnBackgroundColor];
    }
    if (json[@"btnBorderRadius"]) {
        btnBorderRadius = [json valueForKey:@"btnBorderRadius"];
        [inAppBuilder setBtnBorderRadius:btnBorderRadius];
    }
    return inAppBuilder;
}

RCT_EXPORT_METHOD(promptForPushPermission:(BOOL)showFallbackSettings){
    RCTLogInfo(@"[CleverTap promptForPushPermission: %i]", showFallbackSettings);
    [[self cleverTapInstance] promptForPushPermission:showFallbackSettings];
}

RCT_EXPORT_METHOD(promptPushPrimer:(NSDictionary *_Nonnull)json){
    RCTLogInfo(@"[CleverTap promptPushPrimer]");
    CTLocalInApp *localInAppBuilder = [self _localInAppConfigFromReadableMap:json];
    [[self cleverTapInstance] promptPushPrimer:localInAppBuilder.getLocalInAppSettings];
}

RCT_EXPORT_METHOD(isPushPermissionGranted:(RCTResponseSenderBlock)callback){
    if (@available(iOS 10.0, *)) {
        [[self cleverTapInstance] getNotificationPermissionStatusWithCompletionHandler:^(UNAuthorizationStatus status) {
                BOOL result = (status == UNAuthorizationStatusAuthorized);
                RCTLogInfo(@"[CleverTap isPushPermissionGranted: %i]", result);
                [self returnResult:@(result) withCallback:callback andError:nil];
            }];
    } else {
        // Fallback on earlier versions
        RCTLogInfo(@"Push Notification is available from iOS v10.0 or later");
    }
}

#pragma mark - Product Experiences: Vars

RCT_EXPORT_METHOD(syncVariables) {
    RCTLogInfo(@"[CleverTap syncVariables]");
    [[self cleverTapInstance]syncVariables];
}

RCT_EXPORT_METHOD(syncVariablesinProd:(BOOL)isProduction) {
    RCTLogInfo(@"[CleverTap syncVariables:isProduction]");
    [[self cleverTapInstance]syncVariables:isProduction];
}

RCT_EXPORT_METHOD(getVariable:(NSString * _Nonnull)name callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getVariable:name]");
    CTVar *var = self.allVariables[name];
    [self returnResult:var.value withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getVariables:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getVariables]");

    NSMutableDictionary *varValues = [self getVariableValues];
    [self returnResult:varValues withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(fetchVariables:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetchVariables]");
    [[self cleverTapInstance]fetchVariables:^(BOOL success) {
        [self returnResult:@(success) withCallback:callback andError:nil];
    }];
}

RCT_EXPORT_METHOD(defineVariables:(NSDictionary*)variables) {
    RCTLogInfo(@"[CleverTap defineVariables]");

    if (!variables) return;

    [variables enumerateKeysAndObjectsUsingBlock:^(NSString*  _Nonnull key, id  _Nonnull value, BOOL * _Nonnull stop) {
        CTVar *var = [self createVarForName:key andValue:value];

        if (var) {
            self.allVariables[key] = var;
        }
    }];
}

RCT_EXPORT_METHOD(onVariablesChanged) {
    RCTLogInfo(@"[CleverTap onVariablesChanged]");
    [[self cleverTapInstance]onVariablesChanged:^{
        [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnVariablesChanged object:nil userInfo:[self getVariableValues]];
    }];
}

RCT_EXPORT_METHOD(onValueChanged:(NSString*)name) {
    RCTLogInfo(@"[CleverTap onValueChanged]");
    CTVar *var = self.allVariables[name];
    if (var) {
        [var onValueChanged:^{
            NSDictionary *varResult = @{
                var.name: var.value
            };
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnValueChanged object:nil userInfo:varResult];
        }];
    }
}

@end
