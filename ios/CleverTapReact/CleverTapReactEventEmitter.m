
#import "CleverTapReactEventEmitter.h"
#import "CleverTapReactManager.h"
#import "CleverTapReact.h"

#import <React/RCTLog.h>

@implementation CleverTapReactEventEmitter

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
    return @[kCleverTapProfileDidInitialize, kCleverTapProfileSync, kCleverTapInAppNotificationDismissed, kCleverTapInboxDidInitialize, kCleverTapInboxMessagesDidUpdate, kCleverTapExperimentsDidUpdate, kCleverTapInAppNotificationButtonTapped, kCleverTapInboxMessageButtonTapped, kCleverTapDisplayUnitsLoaded];
}


- (void)startObserving {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapProfileDidInitialize
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapProfileSync
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapInAppNotificationDismissed
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapInboxDidInitialize
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapInboxMessagesDidUpdate
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapExperimentsDidUpdate
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapDisplayUnitsLoaded
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapInAppNotificationButtonTapped
                                               object:nil];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(emitEventInternal:)
                                                 name:kCleverTapInboxMessageButtonTapped
                                               object:nil];
}

- (void)stopObserving {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)emitEventInternal:(NSNotification *)notification {
    [self sendEventWithName:notification.name body:notification.userInfo];
}


@end
