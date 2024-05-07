#import "CleverTapReactEventEmitter.h"
#import "CleverTapReactPendingEvent.h"
#import "CleverTapReact.h"
#import <React/RCTLog.h>

static NSMutableArray<CleverTapReactPendingEvent *> *pendingEvents;
static BOOL isObserving;

@implementation CleverTapReactEventEmitter

RCT_EXPORT_MODULE();

+ (void)sendEventOnObserving:(NSString *)name body:(id)body {
    if (isObserving) {
        [[NSNotificationCenter defaultCenter] postNotificationName:name object:nil userInfo:body];
        return;
    }
    
    if (!pendingEvents) {
        pendingEvents = [NSMutableArray array];
    }
    
    CleverTapReactPendingEvent *event = [[CleverTapReactPendingEvent alloc] initWithName:name body:body];
    [pendingEvents addObject:event];
}

- (NSArray<NSString *> *)supportedEvents {
    return @[kCleverTapProfileDidInitialize, kCleverTapProfileSync, kCleverTapInAppNotificationDismissed, kCleverTapInboxDidInitialize, kCleverTapInboxMessagesDidUpdate, kCleverTapInAppNotificationButtonTapped, kCleverTapInboxMessageButtonTapped, kCleverTapInboxMessageTapped, kCleverTapDisplayUnitsLoaded,  kCleverTapFeatureFlagsDidUpdate, kCleverTapProductConfigDidFetch, kCleverTapProductConfigDidActivate, kCleverTapProductConfigDidInitialize, kCleverTapPushNotificationClicked, kCleverTapPushPermissionResponseReceived, kCleverTapInAppNotificationShowed, kCleverTapOnVariablesChanged, kCleverTapOnValueChanged];
}

- (void)startObserving {
    NSArray *eventNames = [self supportedEvents];
    for (NSString *eventName in eventNames) {
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(emitEventInternal:)
                                                     name:eventName
                                                   object:nil];
    }
    
    isObserving = YES;
    for (CleverTapReactPendingEvent *ev in pendingEvents) {
        [[NSNotificationCenter defaultCenter] postNotificationName:ev.name object:nil userInfo:ev.body];
    }
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)emitEventInternal:(NSNotification *)notification {
    [self sendEventWithName:notification.name body:notification.userInfo];
}

@end
