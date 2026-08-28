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
#import "CleverTapReactPendingEvent.h"
#import "CTTemplateContext.h"

#ifdef RCT_NEW_ARCH_ENABLED
#import <CTTurboModuleSpec/CTTurboModuleSpec.h>
#endif

static NSDateFormatter *dateFormatter;

@interface CleverTapReact()
// The "default slot": the instance that unaddressed top-level CleverTap calls use.
// nil means "not resolved yet" -> falls back to [CleverTap sharedInstance].
// setInstanceWithAccountId swaps this pointer (legacy behavior).
@property(nonatomic, strong) CleverTap *defaultInstance;
// Accounts whose delegates are already wired, so wiring happens exactly once per account.
@property(nonatomic, strong) NSMutableSet<NSString *> *wiredAccountIds;
// Which account's App Inbox is currently presented (this module is the inbox view's
// delegate, so inbox tap events are stamped with this account id).
@property(nonatomic, strong) NSString *inboxAccountId;
// Per-account variable registries: REAL account id -> (variable name -> CTVar).
// Without the account level, two accounts defining the same variable name would
// overwrite each other and reads/listeners would silently serve the wrong account.
// ⚠️ Thread safety is mandatory: the SDK invokes variable callbacks on its own
// threads while bridge methods run on the main queue — EVERY access goes through
// @synchronized (self.variablesByAccount).
@property(nonatomic, strong) NSMutableDictionary<NSString *, NSMutableDictionary *> *variablesByAccount;
@end

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
        kCleverTapOnVariablesChanged: kCleverTapOnVariablesChanged,
        kCleverTapOnOneTimeVariablesChanged: kCleverTapOnOneTimeVariablesChanged,
        kCleverTapOnValueChanged: kCleverTapOnValueChanged,
        kCleverTapOnVariablesChangedAndNoDownloadsPending: kCleverTapOnVariablesChangedAndNoDownloadsPending,
        kCleverTapOnceVariablesChangedAndNoDownloadsPending: kCleverTapOnceVariablesChangedAndNoDownloadsPending,
        kCleverTapOnFileValueChanged: kCleverTapOnFileValueChanged,
        kCleverTapCustomTemplatePresent: kCleverTapCustomTemplatePresent,
        kCleverTapCustomFunctionPresent: kCleverTapCustomFunctionPresent,
        kCleverTapCustomTemplateClose: kCleverTapCustomTemplateClose,
        kXPS: kXPS
    };
}

- (NSDictionary *)getConstants {
    return [self constantsToExport];
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}


# pragma mark - Launch

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.variablesByAccount = [NSMutableDictionary dictionary];
        self.wiredAccountIds = [NSMutableSet set];
    }
    return self;
}

/// Resolves the CleverTap instance for the given account id.
///
/// accountId == nil -> the DEFAULT SLOT (today's behavior, unchanged).
/// accountId != nil -> the instance for that account, or nil if it does not exist.
///
/// Example: resolveInstance:nil returns the plist account; after
/// setInstanceWithAccountId:@"B" it returns account B. resolveInstance:@"C"
/// returns account C if it was created (in this run, or restored by the native
/// SDK from a previous run) — otherwise it logs ONE warning and returns nil
/// (callers message nil, which is a safe no-op in Objective-C).
- (CleverTap *)resolveInstance:(NSString *)accountId {
    CleverTap *instance;
    if (accountId == nil) {
        if (self.defaultInstance == nil) {
            self.defaultInstance = [CleverTap sharedInstance];
        }
        instance = self.defaultInstance;
    } else {
        instance = [CleverTap getGlobalInstance:accountId];
    }

    if (instance == nil) {
        // The ONE warning that covers every bridge method (same rule as Android):
        // without it a typo'd accountId silently drops every call.
        if (accountId == nil) {
            RCTLogWarn(@"CleverTap default instance is not available — call ignored");
        } else {
            RCTLogWarn(@"CleverTap instance not found for accountId: %@ — call ignored", accountId);
        }
        return nil;
    }

    NSString *key = instance.config.accountId;
    if (key != nil && ![self.wiredAccountIds containsObject:key]) {
        [self.wiredAccountIds addObject:key];
        [[CleverTapReactManager sharedInstance] setDelegates:instance]; // per-account handlers arrive in Point 4
    }
    return instance;
}

// Existing accessor used by every method — now just the default slot.
- (CleverTap *)cleverTapInstance {
    return [self resolveInstance:nil];
}

RCT_EXPORT_METHOD(setInstanceWithAccountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setInstanceWithAccountId]");

    CleverTap *instance = [CleverTap getGlobalInstance:accountId];
    if (instance == nil) {
        RCTLogWarn(@"CleverTapInstance not found for accountId: %@", accountId);
        return;
    }

    self.defaultInstance = instance;  // swap the default slot (legacy behavior)
    [self resolveInstance:accountId]; // ensure delegates are wired exactly once
}

// 'off' -> Off(-1), 'info' -> Info(0), 'debug'/'verbose' -> Debug(1).
// iOS has no verbose level; Android maps 'verbose' to its real verbose level.
static CleverTapLogLevel ctLogLevelFromString(NSString *level) {
    if ([level isEqualToString:@"off"]) return CleverTapLogOff;
    if ([level isEqualToString:@"debug"] || [level isEqualToString:@"verbose"]) return CleverTapLogDebug;
    return CleverTapLogInfo;
}

// 'none' -> None(0), 'medium' -> Medium(1, PII only), 'high' -> High(2, all data).
// (Android maps the same strings to NONE/MEDIUM/FULL_DATA.)
static CleverTapEncryptionLevel ctEncryptionLevelFromString(NSString *level) {
    if ([level isEqualToString:@"medium"]) return CleverTapEncryptionMedium;
    if ([level isEqualToString:@"high"]) return CleverTapEncryptionHigh;
    return CleverTapEncryptionNone;
}

RCT_EXPORT_METHOD(createInstance:(NSDictionary *)config
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    RCTLogInfo(@"[CleverTap createInstance]");

    NSString *accountId = config[@"accountId"];
    NSString *token = config[@"accountToken"];
    // .length == 0 covers BOTH missing and empty — an empty string would create a
    // "zombie" instance whose events go nowhere while every call looks successful.
    if (accountId.length == 0 || token.length == 0) {
        reject(@"EINVALID", @"createInstance requires non-empty accountId and accountToken", nil);
        return;
    }


    NSString *region = config[@"region"];
    NSString *proxy = config[@"proxyDomain"];
    NSString *spiky = config[@"spikyProxyDomain"];

    // region/proxyDomain/spikyProxyDomain are READONLY on the iOS config — they can
    // only be set through one of the four initializers, and none of them accepts
    // region AND proxy together. Agreed rule: region wins, proxy settings are
    // ignored with a warning (Android applies both).
    CleverTapInstanceConfig *ctConfig;
    if (region.length > 0) {
        if (proxy.length > 0 || spiky.length > 0) {
            RCTLogWarn(@"createInstance: iOS cannot combine region with proxyDomain/spikyProxyDomain; region applied, proxy settings ignored (Android applies both)");
        }
        ctConfig = [[CleverTapInstanceConfig alloc] initWithAccountId:accountId accountToken:token accountRegion:region];
    } else if (proxy.length > 0 && spiky.length > 0) {
        ctConfig = [[CleverTapInstanceConfig alloc] initWithAccountId:accountId accountToken:token proxyDomain:proxy spikyProxyDomain:spiky];
    } else if (proxy.length > 0) {
        ctConfig = [[CleverTapInstanceConfig alloc] initWithAccountId:accountId accountToken:token proxyDomain:proxy];
    } else {
        if (spiky.length > 0) {
            RCTLogWarn(@"createInstance: spikyProxyDomain requires proxyDomain; ignored");
        }
        ctConfig = [[CleverTapInstanceConfig alloc] initWithAccountId:accountId accountToken:token];
    }

    // These ARE writable properties on the iOS config:
    if (config[@"handshakeDomain"]) {
        ctConfig.handshakeDomain = config[@"handshakeDomain"];
    }
    if (config[@"identityKeys"]) {
        ctConfig.identityKeys = config[@"identityKeys"];
    }
    if (config[@"logLevel"]) {
        ctConfig.logLevel = ctLogLevelFromString(config[@"logLevel"]);
    }
    if (config[@"analyticsOnly"]) {
        ctConfig.analyticsOnly = [config[@"analyticsOnly"] boolValue];
    }
    if (config[@"enablePersonalization"]) {
        ctConfig.enablePersonalization = [config[@"enablePersonalization"] boolValue];
    }
    if (config[@"disableAppLaunchedEvent"]) {
        ctConfig.disableAppLaunchedEvent = [config[@"disableAppLaunchedEvent"] boolValue];
    }
    if (config[@"encryptionLevel"]) {
        ctConfig.encryptionLevel = ctEncryptionLevelFromString(config[@"encryptionLevel"]);
    }
    if (config[@"encryptionInTransit"]) {
        ctConfig.encryptionInTransitEnabled = [config[@"encryptionInTransit"] boolValue];
    }
    if (config[@"useCustomCleverTapId"]) {
        ctConfig.useCustomCleverTapId = [config[@"useCustomCleverTapId"] boolValue];
    }
    // Platform-specific options live in nested blocks; each platform reads only its
    // own block (the "android" block is intentionally ignored here).
    NSDictionary *iosConfig = config[@"ios"];
    if ([iosConfig isKindOfClass:[NSDictionary class]]) {
        if (iosConfig[@"disableIDFV"]) {
            ctConfig.disableIDFV = [iosConfig[@"disableIDFV"] boolValue];
        }
        if (iosConfig[@"enableFileProtection"]) {
            ctConfig.enableFileProtection = [iosConfig[@"enableFileProtection"] boolValue];
        }
    }

    // A custom CleverTap ID can only be supplied AT CREATION on both platforms.
    // Without this, useCustomCleverTapId=true would create an instance that waits
    // for an ID nobody can ever provide (error device id).
    NSString *cleverTapId = config[@"cleverTapId"];
    // Instance creation can THROW, not just return nil: registered custom template
    // producers run inside it, and e.g. duplicate template names raise NSException
    // (CleverTapCustomTemplateException). An uncaught throw would crash the app
    // instead of rejecting the promise.
    CleverTap *instance;
    @try {
        instance = (cleverTapId.length > 0)
            ? [CleverTap instanceWithConfig:ctConfig andCleverTapID:cleverTapId]
            : [CleverTap instanceWithConfig:ctConfig];
    } @catch (NSException *exception) {
        reject(@"ECREATE", [NSString stringWithFormat:@"createInstance failed for accountId %@: %@",
                            accountId, exception.reason], nil);
        return;
    }
    if (instance == nil) {
        reject(@"ECREATE", [NSString stringWithFormat:@"createInstance failed for accountId %@", accountId], nil);
        return;
    }
    [instance setLibrary:@"React-Native"];
    [self resolveInstance:accountId]; // wires delegates exactly once
    resolve(@{@"accountId": accountId});
}

// Resolves the account id the default slot currently points to (or null when no
// default account exists). JS uses this once to route the top-level CleverTap
// object's events; see the multi-instance design docs (point 5).
RCT_EXPORT_METHOD(getDefaultAccountId:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject) {
    RCTLogInfo(@"[CleverTap getDefaultAccountId]");
    CleverTap *instance = [self resolveInstance:nil];
    NSString *accountId = instance.config.accountId;
    resolve(accountId ?: (id)[NSNull null]);
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

RCT_EXPORT_METHOD(setLibrary:(NSString*)name andVersion:(double)version) {
    int libVersion = (int)version;
    RCTLogInfo(@"[CleverTap setLibrary:%@ andVersion:%d]", name, libVersion);
    [[self cleverTapInstance] setLibrary:name];
    [[self cleverTapInstance] setCustomSdkVersion:name version:libVersion];
}

RCT_EXPORT_METHOD(setLocale:(NSString*)locale accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setLocale:%@]", locale);
    NSLocale *userLocale = [NSLocale localeWithLocaleIdentifier:locale];
    [[self resolveInstance:accountId] setLocale:userLocale];
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

RCT_EXPORT_METHOD(setFCMPushTokenAsString:(NSString*)token accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setPushTokenAsString: %@]", token);
    [[self resolveInstance:accountId] setPushTokenAsString:token];
}

RCT_EXPORT_METHOD(pushRegistrationToken:(NSString*)token withPushType:(NSDictionary*)pushType accountId:(NSString*)accountId) {
    NSString *type = pushType[@"type"];
    if ([type isEqualToString:@"fcm"]) {
        [self setFCMPushTokenAsString:token accountId:accountId];
    } else {
        RCTLogInfo(@"[CleverTap pushRegistrationToken for types other than FCM is no-op in iOS]");
    }
}

// setPushTokenAsStringWithRegion is a no-op in iOS
RCT_EXPORT_METHOD(setPushTokenAsStringWithRegion:(NSString*)token withType:(NSString *)type withRegion:(NSString *)region accountId:(NSString*)accountId){
    RCTLogInfo(@"[CleverTap setPushTokenAsStringWithRegion is no-op in iOS]");
}

#pragma mark - Personalization

RCT_EXPORT_METHOD(enablePersonalization:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap enablePersonalization]");
    [CleverTap enablePersonalization];
}

RCT_EXPORT_METHOD(disablePersonalization:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap disablePersonalization]");
    [CleverTap disablePersonalization];
}


#pragma mark - Offline API

RCT_EXPORT_METHOD(setOffline:(BOOL)enabled accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setOffline:  %i]", enabled);
    [[self resolveInstance:accountId] setOffline:enabled];
}


#pragma mark - OptOut API

RCT_EXPORT_METHOD(setOptOut:(BOOL)userOptOut allowSystemEvents:(BOOL)allowSystemEvents accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setOptOut and allowSystemEvents: %d, %d]", userOptOut, allowSystemEvents);
    CleverTap *instance = [self resolveInstance:accountId];
    if (allowSystemEvents) {
        [instance setOptOut:userOptOut allowSystemEvents:allowSystemEvents];
    } else {
        [instance setOptOut:userOptOut];
    }
}

RCT_EXPORT_METHOD(enableDeviceNetworkInfoReporting:(BOOL)enabled accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap enableDeviceNetworkInfoReporting: %i]", enabled);
    [[self resolveInstance:accountId] enableDeviceNetworkInfoReporting:enabled];
}


#pragma mark - Event API

RCT_EXPORT_METHOD(recordScreenView:(NSString*)screenName accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap recordScreenView]");
    [[self resolveInstance:accountId] recordScreenView:screenName];
}

RCT_EXPORT_METHOD(recordEvent:(NSString*)eventName withProps:(NSDictionary*)props accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap recordEvent:withProps]");
    [[self resolveInstance:accountId] recordEvent:eventName withProps:props];
}

RCT_EXPORT_METHOD(recordChargedEvent:(NSDictionary*)details andItems:(NSArray*)items accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap recordChargedEventWithDetails:andItems:]");
    [[self resolveInstance:accountId] recordChargedEventWithDetails:details andItems:items];
}

RCT_EXPORT_METHOD(eventGetFirstTime:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetFirstTime: %@]", eventName);
    NSTimeInterval result = [[self resolveInstance:accountId] eventGetFirstTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetLastTime:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetLastTime: %@]", eventName);
    NSTimeInterval result = [[self resolveInstance:accountId] eventGetLastTime:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetOccurrences:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetOccurrences: %@]", eventName);
    int result = [[self resolveInstance:accountId] eventGetOccurrences:eventName];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(eventGetDetail:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap eventGetDetail: %@]", eventName);
    CleverTapEventDetail *detail = [[self resolveInstance:accountId] eventGetDetail:eventName];
    NSDictionary *result = [self _eventDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getEventHistory:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getEventHistory]");
    NSDictionary *history = [[self resolveInstance:accountId] userGetEventHistory];
    NSMutableDictionary *result = [NSMutableDictionary new];
    
    for (NSString *eventName in [history keyEnumerator]) {
        CleverTapEventDetail *detail = history[eventName];
        NSDictionary * _inner = [self _eventDetailToDict:detail];
        result[eventName] = _inner;
    }
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUserEventLog:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserEventLog: %@]", eventName);
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        CleverTapEventDetail *detail = [[self resolveInstance:accountId] getUserEventLog:eventName];
        NSDictionary *result = [self _eventDetailToDict:detail];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self returnResult:result withCallback:callback andError:nil];
        });
    });
}

RCT_EXPORT_METHOD(getUserEventLogCount:(NSString*)eventName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserEventLogCount: %@]", eventName);
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        int result = [[self resolveInstance:accountId] getUserEventLogCount:eventName];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self returnResult:@(result) withCallback:callback andError:nil];
        });
    });
}

RCT_EXPORT_METHOD(getUserEventLogHistory:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserEventLogHistory]");
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        NSDictionary *history = [[self resolveInstance:accountId] getUserEventLogHistory];
        NSMutableDictionary *result = [NSMutableDictionary new];
    
        for (NSString *eventName in [history keyEnumerator]) {
            CleverTapEventDetail *detail = history[eventName];
            NSDictionary * _inner = [self _eventDetailToDict:detail];
            result[eventName] = _inner;
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            [self returnResult:result withCallback:callback andError:nil];
        });
    });
}

#pragma mark - Profile API

RCT_EXPORT_METHOD(setLocation:(double)latitude longitude:(double)longitude accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setLocation: %f %f]", latitude, longitude);
    CLLocationCoordinate2D coordinate = CLLocationCoordinate2DMake(latitude, longitude);
    // Use the INSTANCE method, not the class method: [CleverTap setLocation:] is
    // hardwired to [CleverTap sharedInstance] (the plist account), so it silently
    // ignored both the accountId and a swapped default slot.
    [[self resolveInstance:accountId] setLocation:coordinate];
}

RCT_EXPORT_METHOD(profileGetCleverTapAttributionIdentifier:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapAttributionIdentifier]");
    NSString *result = [[self resolveInstance:accountId] profileGetCleverTapAttributionIdentifier];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileGetCleverTapID:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetCleverTapID]");
    NSString *result = [[self resolveInstance:accountId] profileGetCleverTapID];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getCleverTapID:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getCleverTapID]");
    NSString *result = [[self resolveInstance:accountId] profileGetCleverTapID];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(onUserLogin:(NSDictionary*)profile accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onUserLogin: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[self resolveInstance:accountId] onUserLogin:_profile];
}

RCT_EXPORT_METHOD(profileSet:(NSDictionary*)profile accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileSet: %@]", profile);
    NSDictionary *_profile = [self formatProfile:profile];
    [[self resolveInstance:accountId] profilePush:_profile];
}

RCT_EXPORT_METHOD(profileGetProperty:(NSString*)propertyName accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap profileGetProperty: %@]", propertyName);
    id result = [[self resolveInstance:accountId] profileGet:propertyName];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(profileRemoveValueForKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileRemoveValueForKey: %@]", key);
    [[self resolveInstance:accountId] profileRemoveValueForKey:key];
}

RCT_EXPORT_METHOD(profileSetMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileSetMultiValues: %@ forKey: %@]", values, key);
    [[self resolveInstance:accountId] profileSetMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValue:(NSString*)value forKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileAddMultiValue: %@ forKey: %@]", value, key);
    [[self resolveInstance:accountId] profileAddMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileAddMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileAddMultiValues: %@ forKey: %@]", values, key);
    [[self resolveInstance:accountId] profileAddMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValue:(NSString*)value forKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValue: %@ forKey: %@]", value, key);
    [[self resolveInstance:accountId] profileRemoveMultiValue:value forKey:key];
}

RCT_EXPORT_METHOD(profileRemoveMultiValues:(NSArray<NSString*>*)values forKey:(NSString*)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileRemoveMultiValues: %@ forKey: %@]", values, key);
    [[self resolveInstance:accountId] profileRemoveMultiValues:values forKey:key];
}

RCT_EXPORT_METHOD(profileIncrementValueForKey:(NSNumber* _Nonnull)value forKey:(NSString* _Nonnull)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileIncrementValueBy: %@ forKey: %@]", value, key);
    [[self resolveInstance:accountId] profileIncrementValueBy:value forKey:key];
}

RCT_EXPORT_METHOD(profileDecrementValueForKey:(NSNumber* _Nonnull)value forKey:(NSString* _Nonnull)key accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap profileDecrementValueBy: %@ forKey: %@]", value, key);
    [[self resolveInstance:accountId] profileDecrementValueBy:value forKey:key];
}

#pragma mark - Session API

RCT_EXPORT_METHOD(pushInstallReferrer:(NSString*)source medium:(NSString*)medium campaign:(NSString*)campaign accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushInstallReferrer source: %@ medium: %@ campaign: %@]", source, medium, campaign);
    [[self resolveInstance:accountId] pushInstallReferrerSource:source medium:medium campaign:campaign];
}

RCT_EXPORT_METHOD(sessionGetTimeElapsed:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTimeElapsed]");
    NSTimeInterval result = [[self resolveInstance:accountId] sessionGetTimeElapsed];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetTotalVisits:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetTotalVisits]");
    int result = [[self resolveInstance:accountId] userGetTotalVisits];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetScreenCount:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetScreenCount]");
    int result = [[self resolveInstance:accountId] userGetScreenCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetPreviousVisitTime:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetPreviousVisitTime]");
    NSTimeInterval result = [[self resolveInstance:accountId] userGetPreviousVisitTime];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(sessionGetUTMDetails:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap sessionGetUTMDetails]");
    CleverTapUTMDetail *detail = [[self resolveInstance:accountId] sessionGetUTMDetails];
    NSDictionary *result = [self _utmDetailToDict:detail];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUserLastVisitTs:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserLastVisitTs]");
    NSTimeInterval result = [[self resolveInstance:accountId] getUserLastVisitTs];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUserAppLaunchCount:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUserAppLaunchCount]");
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        int result = [[self resolveInstance:accountId] getUserAppLaunchCount];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self returnResult:@(result) withCallback:callback andError:nil];
        });
    });
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

RCT_EXPORT_METHOD(setDebugLevel:(double)level) {
    int debugLevel = (int)level;
    RCTLogInfo(@"[CleverTap setDebugLevel: %i]", debugLevel);
    [CleverTap setDebugLevel:debugLevel];
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
        
        if(detail.normalizedEventName){
            [_dict setObject:detail.normalizedEventName forKey:@"normalizedEventName"];
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
        
        if(detail.deviceID){
            [_dict setObject:detail.deviceID forKey:@"deviceID"];
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
            
            if([value isKindOfClass:[NSString class]] && ![value hasPrefix:@"$D_"]) {
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

- (CTVar *)createVarForName:(NSString *)name andValue:(id)value usingInstance:(CleverTap *)instance {

    if ([value isKindOfClass:[NSString class]]) {
        return [instance defineVar:name withString:value];
    }
    if ([value isKindOfClass:[NSDictionary class]]) {
        return [instance defineVar:name withDictionary:value];
    }
    if ([value isKindOfClass:[NSNumber class]]) {
        if ([self isBoolNumber:value]) {
            return [instance defineVar:name withBool:value];
        }
        return [instance defineVar:name withNumber:value];
    }
    return nil;
}

- (BOOL)isBoolNumber:(NSNumber *)number {
    CFTypeID boolID = CFBooleanGetTypeID();
    CFTypeID numID = CFGetTypeID(CFBridgingRetain(number));
    return (numID == boolID);
}

/// Returns the variable registry belonging to the given instance's account, creating
/// it on first use. An instance without an account id gets an isolated empty registry
/// so callers safely no-op.
- (NSMutableDictionary *)variablesForInstance:(CleverTap *)instance {
    NSString *accountKey = instance.config.accountId;
    if (accountKey == nil) {
        RCTLogWarn(@"[CleverTap variables unavailable: instance has no accountId]");
        return [NSMutableDictionary dictionary];
    }
    @synchronized (self.variablesByAccount) {
        NSMutableDictionary *accountVars = self.variablesByAccount[accountKey];
        if (accountVars == nil) {
            accountVars = [NSMutableDictionary dictionary];
            self.variablesByAccount[accountKey] = accountVars;
        }
        return accountVars;
    }
}

- (CTVar *)varForName:(NSString *)name usingInstance:(CleverTap *)instance {
    NSMutableDictionary *accountVars = [self variablesForInstance:instance];
    @synchronized (self.variablesByAccount) {
        return accountVars[name];
    }
}

- (NSMutableDictionary *)getVariableValuesForInstance:(CleverTap *)instance {
    // Snapshot the registry under the lock, then read the CTVar values OUTSIDE it.
    // Never call into SDK objects (var.value) while holding our lock: if the SDK
    // ever synchronizes that getter internally, reading it under our lock could
    // form a lock-order inversion with SDK threads that call back into us.
    NSDictionary *snapshot;
    NSMutableDictionary *accountVars = [self variablesForInstance:instance];
    @synchronized (self.variablesByAccount) {
        snapshot = [accountVars copy];
    }
    NSMutableDictionary *varValues = [NSMutableDictionary dictionary];
    [snapshot enumerateKeysAndObjectsUsingBlock:^(id  _Nonnull key, CTVar*  _Nonnull var, BOOL * _Nonnull stop) {
        varValues[key] = var.value;
    }];
    return varValues;
}

#pragma mark - App Inbox

RCT_EXPORT_METHOD(getInboxMessageCount:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageCount]");
    int result = (int)[[self resolveInstance:accountId] getInboxMessageCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageUnreadCount:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap inboxMessageUnreadCount]");
    int result = (int)[[self resolveInstance:accountId] getInboxMessageUnreadCount];
    [self returnResult:@(result) withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getAllInboxMessages:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[self resolveInstance:accountId] getAllInboxMessages];
    NSMutableArray *allMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [allMessages addObject:message.json];
    }
    NSArray *result = [allMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getUnreadInboxMessages:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getUnreadInboxMessages]");
    NSArray<CleverTapInboxMessage *> *messageList = [[self resolveInstance:accountId] getUnreadInboxMessages];
    NSMutableArray *unreadMessages = [NSMutableArray new];
    for (CleverTapInboxMessage *message in messageList) {
        [unreadMessages addObject:message.json];
    }
    NSArray *result = [unreadMessages mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getInboxMessageForId:(NSString*)messageId accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getInboxMessageForId]");
    CleverTapInboxMessage * message = [[self resolveInstance:accountId] getInboxMessageForId:messageId];
    NSDictionary *result = message.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushInboxNotificationViewedEventForId:(NSString*)messageId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationViewedEventForId]");
    [[self resolveInstance:accountId] recordInboxNotificationViewedEventForID:messageId];
}

RCT_EXPORT_METHOD(pushInboxNotificationClickedEventForId:(NSString*)messageId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushInboxNotificationClickedEventForId]");
    [[self resolveInstance:accountId] recordInboxNotificationClickedEventForID:messageId];
}

RCT_EXPORT_METHOD(markReadInboxMessageForId:(NSString*)messageId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap markReadInboxMessageForId]");
    [[self resolveInstance:accountId] markReadInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(deleteInboxMessageForId:(NSString*)messageId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap deleteInboxMessageForId]");
    [[self resolveInstance:accountId] deleteInboxMessageForID:messageId];
}

RCT_EXPORT_METHOD(markReadInboxMessagesForIDs:(NSArray*)messageIds accountId:(NSString*)accountId) {
    if (!messageIds) return;
    RCTLogInfo(@"[CleverTap markReadInboxMessagesForIDs]");
    [[self resolveInstance:accountId] markReadInboxMessagesForIDs:messageIds];
}

RCT_EXPORT_METHOD(deleteInboxMessagesForIDs:(NSArray*)messageIds accountId:(NSString*)accountId) {
    if (!messageIds) return;
    RCTLogInfo(@"[CleverTap deleteInboxMessagesForIDs]");
    [[self resolveInstance:accountId] deleteInboxMessagesForIDs:messageIds];
}

RCT_EXPORT_METHOD(dismissInbox:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap dismissAppInbox]");
    [[self resolveInstance:accountId] dismissAppInbox];
}

RCT_EXPORT_METHOD(fetchInbox:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetchInbox]");
    CleverTap *instance = [self resolveInstance:accountId];
    if (callback == NULL) {
        [instance fetchInboxWithCallback:nil];
    } else {
        [instance fetchInboxWithCallback:^(BOOL success) {
            [self returnResult:@(success) withCallback:callback andError:nil];
        }];
    }
}

RCT_EXPORT_METHOD(initializeInbox:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap Inbox Initialize]");
    CleverTap *instance = [self resolveInstance:accountId];
    // Stamp the inbox events with the REAL account id so JS routes them to the right
    // handle (Android's inbox events arrive tagged via the listener proxy — same rule).
    NSString *accountKey = instance.config.accountId;
    [instance initializeInboxWithCallback:^(BOOL success) {
        if (success) {
            RCTLogInfo(@"[Inbox initialized]");
            NSMutableDictionary *body = [NSMutableDictionary new];
            if (accountKey != nil) {
                body[kCleverTapAccountIdKey] = accountKey;
            }
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxDidInitialize object:nil userInfo:body];
            [instance registerInboxUpdatedBlock:^{
                RCTLogInfo(@"[Inbox updated]");
                [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessagesDidUpdate object:nil userInfo:body];
            }];
        }
    }];
}

RCT_EXPORT_METHOD(showInbox:(NSDictionary*)styleConfig accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap Show Inbox]");
    UIWindow *keyWindow = [[UIApplication sharedApplication] keyWindow];
    UIViewController *mainViewController = keyWindow.rootViewController;
    if (mainViewController.presentedViewController) {
        RCTLogInfo(@"CleverTap : Could not present App Inbox because a view controller is already being presented.");
        return;
    }
    
    CleverTap *instance = [self resolveInstance:accountId];
    CleverTapInboxViewController *inboxController = [instance newInboxViewControllerWithConfig:[self _dictToInboxStyleConfig:styleConfig? styleConfig : nil] andDelegate:(id <CleverTapInboxViewControllerDelegate>)self];
    if (inboxController) {
        // Remember whose inbox is on screen: this module receives the inbox tap
        // delegate callbacks and stamps their events with this account id.
        self.inboxAccountId = instance.config.accountId;
        UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:inboxController];
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
    NSString *firstTabTitle = [dict valueForKey:@"firstTabTitle"];
    if (firstTabTitle) {
        _config.firstTabTitle = firstTabTitle;
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
    if (self.inboxAccountId != nil) {
        body[kCleverTapAccountIdKey] = self.inboxAccountId;
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
    if (self.inboxAccountId != nil) {
        body[kCleverTapAccountIdKey] = self.inboxAccountId;
    }

    [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapInboxMessageTapped object:nil userInfo:body];
}


#pragma mark - Display Units

RCT_EXPORT_METHOD(getAllDisplayUnits:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getAllDisplayUnits]");
    NSArray <CleverTapDisplayUnit*> *units = [[self resolveInstance:accountId] getAllDisplayUnits];
    NSMutableArray *displayUnits = [NSMutableArray new];
    for (CleverTapDisplayUnit *unit in units) {
        [displayUnits addObject:unit.json];
    }
    NSArray *result = [displayUnits mutableCopy];
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getDisplayUnitForId:(NSString*)unitId accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getDisplayUnitForId]");
    CleverTapDisplayUnit * displayUnit = [[self resolveInstance:accountId] getDisplayUnitForID:unitId];
    NSDictionary *result = displayUnit.json;
    [self returnResult:result withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(pushDisplayUnitViewedEventForID:(NSString*)unitId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitViewedEventForID]");
    [[self resolveInstance:accountId] recordDisplayUnitViewedEventForID:unitId];
}

RCT_EXPORT_METHOD(pushDisplayUnitClickedEventForID:(NSString*)unitId accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitClickedEventForID]");
    [[self resolveInstance:accountId] recordDisplayUnitClickedEventForID:unitId];
}

RCT_EXPORT_METHOD(pushDisplayUnitElementClickedEventForID:(NSString*)unitId withAdditionalProperties:(NSDictionary*)additionalProperties accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap pushDisplayUnitElementClickedEventForID]");
    [[self resolveInstance:accountId] recordDisplayUnitElementClickedEventForID:unitId additionalProperties:additionalProperties];
}


# pragma mark - Feature Flag

RCT_EXPORT_METHOD(getFeatureFlag:(NSString*)flag withdefaultValue:(BOOL)defaultValue accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getFeatureFlag]");
    BOOL result = [[[self resolveInstance:accountId] featureFlags] get:flag withDefaultValue:defaultValue];
    [self returnResult:@(result) withCallback:callback andError:nil];
}


#pragma mark - Product Config

RCT_EXPORT_METHOD(setDefaultsMap:(NSDictionary*)jsonDict accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap setDefaultsMap]");
    [[[self resolveInstance:accountId] productConfig] setDefaults:jsonDict];
}

RCT_EXPORT_METHOD(fetch:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch]");
    [[[self resolveInstance:accountId] productConfig] fetch];
}

RCT_EXPORT_METHOD(fetchWithMinimumFetchIntervalInSeconds:(double)time accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch with minimum Interval]");
    [[[self resolveInstance:accountId] productConfig] fetchWithMinimumInterval: time];
}

RCT_EXPORT_METHOD(activate:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Activate]");
    [[[self resolveInstance:accountId] productConfig] activate];
}

RCT_EXPORT_METHOD(fetchAndActivate:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Fetch and Activate]");
    [[[self resolveInstance:accountId] productConfig] fetchAndActivate];
}

RCT_EXPORT_METHOD(setMinimumFetchIntervalInSeconds:(double)time accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Minimum Time Interval Setup]");
    [[[self resolveInstance:accountId] productConfig] setMinimumFetchInterval: time];
}

RCT_EXPORT_METHOD(getLastFetchTimeStampInMillis:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap Last Fetch Config time]");
    NSTimeInterval result = [[[[self resolveInstance:accountId] productConfig] getLastFetchTimeStamp] timeIntervalSince1970] * 1000;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getString:(NSString*)key accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch String value for Key]");
    NSString *result = [[[self resolveInstance:accountId] productConfig] get:key].stringValue;
    [self returnResult: result withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getBoolean:(NSString*)key accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch Bool value for Key]");
    BOOL result = [[[self resolveInstance:accountId] productConfig] get:key].boolValue;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(getDouble:(NSString*)key accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetch Double value for Key]");
    long result = [[[self resolveInstance:accountId] productConfig] get:key].numberValue.doubleValue;
    [self returnResult: @(result) withCallback: callback andError:nil];
}

RCT_EXPORT_METHOD(reset:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap ProductConfig Reset]");
    [[[self resolveInstance:accountId] productConfig] reset];
}

#pragma mark - InApp Notification Controls

RCT_EXPORT_METHOD(suspendInAppNotifications:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap suspendInAppNotifications");
    [[self resolveInstance:accountId] suspendInAppNotifications];
}

RCT_EXPORT_METHOD(discardInAppNotifications:(BOOL)dismissInAppIfVisible accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap discardInAppNotifications: %d]", dismissInAppIfVisible);
    [[self resolveInstance:accountId] discardInAppNotifications:dismissInAppIfVisible];
}

RCT_EXPORT_METHOD(resumeInAppNotifications:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap resumeInAppNotifications");
    [[self resolveInstance:accountId] resumeInAppNotifications];
}

RCT_EXPORT_METHOD(dismissPipInApp:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap dismissPipInApp]");
    [[self resolveInstance:accountId] dismissPipInApp];
}

RCT_EXPORT_METHOD(unmute:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap unmute]");
    [[self resolveInstance:accountId] unmute];
}

#pragma mark - InApp Controls

RCT_EXPORT_METHOD(fetchInApps:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetchInApps]");
    [[self resolveInstance:accountId] fetchInApps:^(BOOL success) {
        [self returnResult:@(success) withCallback:callback andError:nil];
    }];
}

RCT_EXPORT_METHOD(clearInAppResources:(BOOL)expiredOnly accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap clearInAppResources");
    [[self resolveInstance:accountId] clearInAppResources: expiredOnly];
}

#pragma mark - Push Permission

- (CTLocalInApp*)_localInAppConfigFromReadableMap: (NSDictionary *)json {
    CTLocalInApp *inAppBuilder;
    CTLocalInAppType inAppType = HALF_INTERSTITIAL;
    //Required parameters
    NSString *titleText = nil, *messageText = nil, *followDeviceOrientation = nil, *positiveBtnText = nil, *negativeBtnText = nil;
    //Additional parameters
    NSString *fallbackToSettings = nil, *backgroundColor = nil, *btnBorderColor = nil, *titleTextColor = nil, *messageTextColor = nil, *btnTextColor = nil, *imageUrl = nil, *btnBackgroundColor = nil, *btnBorderRadius = nil, *altText = nil;
    
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
    
    if (json[@"altText"]) {
        altText = [json valueForKey:@"altText"];
    }
    
    if (json[@"imageUrl"]) {
        imageUrl = [json valueForKey:@"imageUrl"];
        [inAppBuilder setImageUrl:imageUrl contentDescription:altText];
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

RCT_EXPORT_METHOD(syncVariables:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap syncVariables]");
    [[self resolveInstance:accountId]syncVariables];
}

RCT_EXPORT_METHOD(syncVariablesinProd:(BOOL)isProduction accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap syncVariables:isProduction]");
    [[self resolveInstance:accountId]syncVariables:isProduction];
}

RCT_EXPORT_METHOD(getVariable:(NSString * _Nonnull)name accountId:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getVariable:name]");
    CTVar *var = [self varForName:name usingInstance:[self resolveInstance:accountId]];
    [self returnResult:var.value withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(getVariables:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap getVariables]");

    NSMutableDictionary *varValues = [self getVariableValuesForInstance:[self resolveInstance:accountId]];
    [self returnResult:varValues withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(variants:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap variants]");

    NSArray<NSDictionary<NSString*,id>*> *variants = [[self resolveInstance:accountId]variants];
    [self returnResult:variants withCallback:callback andError:nil];
}

RCT_EXPORT_METHOD(fetchVariables:(NSString*)accountId callback:(RCTResponseSenderBlock)callback) {
    RCTLogInfo(@"[CleverTap fetchVariables]");
    [[self resolveInstance:accountId]fetchVariables:^(BOOL success) {
        [self returnResult:@(success) withCallback:callback andError:nil];
    }];
}

RCT_EXPORT_METHOD(defineVariables:(NSDictionary*)variables accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap defineVariables]");

    if (!variables) return;

    CleverTap *instance = [self resolveInstance:accountId];
    NSMutableDictionary *accountVars = [self variablesForInstance:instance];
    [variables enumerateKeysAndObjectsUsingBlock:^(NSString*  _Nonnull key, id  _Nonnull value, BOOL * _Nonnull stop) {
        CTVar *var = [self createVarForName:key andValue:value usingInstance:instance];

        if (var) {
            @synchronized (self.variablesByAccount) {
                accountVars[key] = var;
            }
        }
    }];
}

RCT_EXPORT_METHOD(defineFileVariable:(NSString*)fileVariable accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap defineFileVariable]");
    if (!fileVariable) return;
    CleverTap *instance = [self resolveInstance:accountId];
    CTVar *fileVar = [instance defineFileVar:fileVariable];
    if (fileVar) {
        NSMutableDictionary *accountVars = [self variablesForInstance:instance];
        @synchronized (self.variablesByAccount) {
            accountVars[fileVariable] = fileVar;
        }
    }
}

RCT_EXPORT_METHOD(onVariablesChanged:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onVariablesChanged]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    [instance onVariablesChanged:^{
        NSMutableDictionary *body = [self getVariableValuesForInstance:instance];
        if (accountKey != nil) {
            body[kCleverTapAccountIdKey] = accountKey;
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnVariablesChanged object:nil userInfo:body];
    }];
}

RCT_EXPORT_METHOD(onOneTimeVariablesChanged:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onOneTimeVariablesChanged]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    [instance onceVariablesChanged:^{
        NSMutableDictionary *body = [self getVariableValuesForInstance:instance];
        if (accountKey != nil) {
            body[kCleverTapAccountIdKey] = accountKey;
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnOneTimeVariablesChanged object:nil userInfo:body];
    }];
}

RCT_EXPORT_METHOD(onValueChanged:(NSString*)name accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onValueChanged]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    CTVar *var = [self varForName:name usingInstance:instance];
    if (var) {
        [var onValueChanged:^{
            NSMutableDictionary *varResult = [@{
                var.name: var.value
            } mutableCopy];
            if (accountKey != nil) {
                varResult[kCleverTapAccountIdKey] = accountKey;
            }
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnValueChanged object:nil userInfo:varResult];
        }];
    }
}

RCT_EXPORT_METHOD(onVariablesChangedAndNoDownloadsPending:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onVariablesChangedAndNoDownloadsPending]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    [instance onVariablesChangedAndNoDownloadsPending:^{
        NSMutableDictionary *body = [self getVariableValuesForInstance:instance];
        if (accountKey != nil) {
            body[kCleverTapAccountIdKey] = accountKey;
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnVariablesChangedAndNoDownloadsPending object:nil userInfo:body];
    }];
}

RCT_EXPORT_METHOD(onceVariablesChangedAndNoDownloadsPending:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onceVariablesChangedAndNoDownloadsPending]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    [instance onceVariablesChangedAndNoDownloadsPending:^{
        NSMutableDictionary *body = [self getVariableValuesForInstance:instance];
        if (accountKey != nil) {
            body[kCleverTapAccountIdKey] = accountKey;
        }
        [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnceVariablesChangedAndNoDownloadsPending object:nil userInfo:body];
    }];
}

RCT_EXPORT_METHOD(onFileValueChanged:(NSString*)name accountId:(NSString*)accountId) {
    RCTLogInfo(@"[CleverTap onFileChanged]");
    CleverTap *instance = [self resolveInstance:accountId];
    NSString *accountKey = instance.config.accountId;
    CTVar *var = [self varForName:name usingInstance:instance];
    if (var) {
        [var onFileIsReady:^{
            NSMutableDictionary *varFileResult = [@{
                var.name: var.value
            } mutableCopy];
            if (accountKey != nil) {
                varFileResult[kCleverTapAccountIdKey] = accountKey;
            }
            [[NSNotificationCenter defaultCenter] postNotificationName:kCleverTapOnFileValueChanged object:nil userInfo:varFileResult];
        }];
    }
}

# pragma mark - Custom Code Templates

RCT_EXPORT_METHOD(syncCustomTemplates:(NSString *)accountId) {
    RCTLogInfo(@"[CleverTap syncCustomTemplates]");
    [[self resolveInstance:accountId] syncCustomTemplates];
}

RCT_EXPORT_METHOD(syncCustomTemplatesInProd:(BOOL)isProduction accountId:(NSString *)accountId) {
    RCTLogInfo(@"[CleverTap syncCustomTemplates:isProduction]");
    [[self resolveInstance:accountId] syncCustomTemplates:isProduction];
}

// ⚠️ In every customTemplate* method the accountId comes BEFORE resolve/reject:
// the promise pair is the implicitly-last argument pattern of the bridge (same
// iron rule as trailing callbacks — nothing may follow it).

RCT_EXPORT_METHOD(customTemplateGetBooleanArg:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        NSNumber *number = [context numberNamed:argName];
        return number ? number : [NSNull null];
    }];
}

RCT_EXPORT_METHOD(customTemplateGetFileArg:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        NSString *filePath = [context fileNamed:argName];
        return filePath ? filePath : [NSNull null];
    }];
}

RCT_EXPORT_METHOD(customTemplateGetNumberArg:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        NSNumber *number = [context numberNamed:argName];
        return number ? number : [NSNull null];
    }];
}

RCT_EXPORT_METHOD(customTemplateGetObjectArg:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        NSDictionary *dictionary = [context dictionaryNamed:argName];
        return dictionary ? dictionary : [NSNull null];
    }];
}

RCT_EXPORT_METHOD(customTemplateGetStringArg:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        NSString *str = [context stringNamed:argName];
        return str ? str : [NSNull null];
    }];
}

RCT_EXPORT_METHOD(customTemplateRunAction:(NSString *)templateName argName:(NSString *)argName accountId:(NSString *)accountId resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        [context triggerActionNamed:argName];
        return nil;
    }];
}

RCT_EXPORT_METHOD(customTemplateSetDismissed:(NSString *)templateName
                         accountId:(NSString *)accountId
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        [context dismissed];
        return nil;
    }];
}

RCT_EXPORT_METHOD(customTemplateSetPresented:(NSString *)templateName
                         accountId:(NSString *)accountId
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        [context presented];
        return nil;
    }];
}

RCT_EXPORT_METHOD(customTemplateContextToString:(NSString *)templateName
                         accountId:(NSString *)accountId
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject) {
    [self resolveWithTemplateContext:templateName accountId:accountId resolve:resolve reject:reject block:^id(CTTemplateContext *context) {
        return [context debugDescription];
    }];
}

// Active template contexts live PER INSTANCE in the native SDK — asking the wrong
// account always answers "not currently being presented", so the account must be
// resolved here, not hardcoded to the default slot.
- (void)resolveWithTemplateContext:(NSString *)templateName
                         accountId:(NSString *)accountId
                           resolve:(RCTPromiseResolveBlock)resolve
                            reject:(RCTPromiseRejectBlock)reject
                             block: (id (^)(CTTemplateContext *context))blockName {
    CleverTap *instance = [self resolveInstance:accountId];
    if (!instance) {
        reject(@"CustomTemplateError", @"CleverTap is not initialized", nil);
        return;
    }

    CTTemplateContext *context  = [instance activeContextForTemplate:templateName];
    if (!context) {
        reject(@"CustomTemplateError",
               [NSString stringWithFormat:@"Custom template: %@ is not currently being presented", templateName],
               nil);
        return;
    }

    resolve(blockName(context));
}

# pragma mark - Event emitter

/// A collection of events sent before ReactNative has started observing events.
static NSMutableDictionary<NSString *, NSMutableArray<CleverTapReactPendingEvent *> *> *pendingEvents = [NSMutableDictionary dictionary];

/// Indicates if ``startObserving`` has been called which means a listener/observer has been added.
static BOOL isObserving;

/// A set of event names that a listener/observer has been added for.
static NSMutableSet<NSString *> *observedEvents = [NSMutableSet set];

/// A set of event names that needs to be observed since they can be sent before ReactNative has started observing events.
static NSMutableSet<NSString *> *observableEvents = [NSMutableSet setWithObjects:
                                                     kCleverTapPushNotificationClicked,
                                                     kCleverTapProfileDidInitialize,
                                                     kCleverTapDisplayUnitsLoaded,
                                                     kCleverTapInAppNotificationShowed,
                                                     kCleverTapInAppNotificationDismissed,
                                                     kCleverTapInAppNotificationButtonTapped,
                                                     kCleverTapProductConfigDidInitialize,
                                                     kCleverTapCustomTemplatePresent,
                                                     kCleverTapCustomFunctionPresent,
                                                     kCleverTapCustomTemplateClose,
                                                     kCleverTapFeatureFlagsDidUpdate, nil];

/// Time out in seconds, after which pending events are cleared.
/// See ``startObserving`` for details.
const int PENDING_EVENTS_TIME_OUT = 5;

/// Builds the key used in ``observedEvents``. The queue is ACCOUNT-AWARE: each account
/// observes an event separately ("accountId::eventName"). Bodies with no account tag are
/// global and use the bare event name as their key.
static NSString *observedEventKey(NSString *name, NSString *accountId) {
    return accountId != nil ? [NSString stringWithFormat:@"%@::%@", accountId, name] : name;
}

/// Reads the account tag from an event body (nil for untagged/global bodies).
static NSString *accountTagOfBody(id body) {
    if ([body isKindOfClass:[NSDictionary class]]) {
        return ((NSDictionary *)body)[kCleverTapAccountIdKey];
    }
    return nil;
}

/// Called when an observer/listener is added for the event.
/// Marks the event observed for the listener's account and posts ONLY that account's
/// pending events (plus untagged/global ones). Other accounts' pending events stay queued
/// until their own listeners attach — posting everything here would silently drop them,
/// because their listeners are not attached yet to receive the delivery.
///
/// @param name The name of the observed event.
/// @param accountId The account the listener belongs to; nil means the default slot.
RCT_EXPORT_METHOD(onEventListenerAdded:(NSString*)name accountId:(NSString*)accountId) {
    NSString *accountKey = accountId ?: [self resolveInstance:nil].config.accountId;
    RCTLogInfo(@"[CleverTap onEventListenerAdded: %@ accountId=%@ resolved account=%@]", name, accountId, accountKey);
    [observedEvents addObject:observedEventKey(name, accountKey)];
    // Untagged (global) bodies go live once ANY listener observes the event:
    [observedEvents addObject:name];

    NSMutableArray<CleverTapReactPendingEvent *> *pendingEventsForName = pendingEvents[name];
    if (pendingEventsForName) {
        RCTLogInfo(@"[CleverTap: Posting pending events for event: %@]", name);
        NSMutableArray<CleverTapReactPendingEvent *> *remaining = [NSMutableArray array];
        for (CleverTapReactPendingEvent *event in pendingEventsForName) {
            NSString *tag = accountTagOfBody(event.body);
            if (tag == nil || (accountKey != nil && [tag isEqualToString:accountKey])) {
                RCTLogInfo(@"[CleverTap: posting pending event: %@ with body: %@]", event.name, event.body);
                [[NSNotificationCenter defaultCenter] postNotificationName:event.name object:nil userInfo:event.body];
            } else {
                [remaining addObject:event];
            }
        }
        // Replayed events are removed so a second listener cannot receive duplicates.
        pendingEvents[name] = remaining;
    }
}

/// Send event when ReactNative has started observing events.
/// This happens when the first observer/listener is added in ReactNative.
/// If events are sent before that, the events are queued PER ACCOUNT: a body is queued
/// until a listener for ITS account observes the event (see ``onEventListenerAdded``).
/// Events expected to be queued are specified in ``observableEvents``.
///
/// ⚠️ THREAD SAFETY: the queue state (``pendingEvents``, ``observedEvents``,
/// ``observableEvents``, ``isObserving``) is MAIN-CONFINED. Its other mutators —
/// ``onEventListenerAdded`` and ``startObserving`` (methodQueue is main) and the
/// ``clearPendingEvents`` timeout (dispatch_after on main) — already run on main, but
/// SDK callbacks arrive elsewhere: profileDidInitialize is dispatched on a GLOBAL
/// BACKGROUND queue (verified at CleverTap-iOS-SDK 7.8.1, CleverTap.m) and the
/// push-tap delegate runs on its caller's thread. Mutating these NSMutable
/// collections cross-thread can corrupt them or drop a pending event, so off-main
/// callers hop to main here. An async hop (not a lock) on purpose: delivery is
/// already asynchronous, ordering per account is preserved (main is serial), and no
/// lock means no new main-thread blocking to reason about.
///
/// @param name The event name.
/// @param body The event body parameters.
+ (void)sendEventOnObserving:(NSString *)name body:(id)body {
    if (![NSThread isMainThread]) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self sendEventOnObservingMain:name body:body];
        });
        return;
    }
    [self sendEventOnObservingMain:name body:body];
}

/// Main-thread half of ``sendEventOnObserving`` — the ONLY reader/writer of the
/// pending-events state besides the (already main) listener/observer callbacks.
+ (void)sendEventOnObservingMain:(NSString *)name body:(id)body {
    if (!isObserving && ![observableEvents containsObject:name]) {
        RCTLogWarn(@"[CleverTap: %@ is sent before observing and is not part of the observable events]", name);
        [observableEvents addObject:name];
    }

    NSString *tag = accountTagOfBody(body);
    if ([observableEvents containsObject:name]
        && ![observedEvents containsObject:observedEventKey(name, tag)]) {
        if (!pendingEvents[name]) {
            pendingEvents[name] = [NSMutableArray array];
        }

        RCTLogInfo(@"[CleverTap: queueing %@ for account %@ (not observed yet)]", name, tag);
        CleverTapReactPendingEvent *event = [[CleverTapReactPendingEvent alloc] initWithName:name body:body];
        [pendingEvents[name] addObject:event];
        return;
    }

    RCTLogInfo(@"[CleverTap: posting %@ for account %@]", name, tag);
    [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil userInfo:body];
}

- (NSArray<NSString *> *)supportedEvents {
    return @[kCleverTapProfileDidInitialize,
             kCleverTapProfileSync,
             kCleverTapInAppNotificationShowed,
             kCleverTapInAppNotificationDismissed,
             kCleverTapInAppNotificationButtonTapped,
             kCleverTapInboxDidInitialize,
             kCleverTapInboxMessagesDidUpdate,
             kCleverTapInboxMessageButtonTapped,
             kCleverTapInboxMessageTapped,
             kCleverTapDisplayUnitsLoaded,
             kCleverTapFeatureFlagsDidUpdate,
             kCleverTapProductConfigDidFetch,
             kCleverTapProductConfigDidActivate,
             kCleverTapProductConfigDidInitialize,
             kCleverTapPushNotificationClicked,
             kCleverTapPushPermissionResponseReceived,
             kCleverTapOnVariablesChanged,
             kCleverTapOnOneTimeVariablesChanged,
             kCleverTapOnValueChanged,
             kCleverTapOnVariablesChangedAndNoDownloadsPending,
             kCleverTapOnceVariablesChangedAndNoDownloadsPending,
             kCleverTapOnFileValueChanged,
             kCleverTapCustomTemplatePresent,
             kCleverTapCustomFunctionPresent,
             kCleverTapCustomTemplateClose];
}

- (void)startObserving {
    RCTLogInfo(@"[CleverTap startObserving]");
    NSArray *eventNames = [self supportedEvents];
    for (NSString *eventName in eventNames) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(emitEventInternal:)
                                                     name:eventName
                                                   object:nil];
    }
    
    isObserving = YES;
    
    // Clear the pending events that no listeners were added for.
    // Clear the events after PENDING_EVENTS_TIME_OUT of when the first observer is added.
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(PENDING_EVENTS_TIME_OUT * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        RCTLogInfo(@"[CleverTap: Removing pending events which were not observed]");
        [CleverTapReact clearPendingEvents];
    });
}

+ (void)clearPendingEvents {
    pendingEvents = [NSMutableDictionary dictionary];
    observableEvents = [NSMutableSet set];
    observedEvents = [NSMutableSet set];
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)emitEventInternal:(NSNotification *)notification {
    [self sendEventWithName:notification.name body:notification.userInfo];
}

# pragma mark - Turbo Module

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params {
  return std::make_shared<facebook::react::NativeCleverTapModuleSpecJSI>(params);
}
#endif

@end
