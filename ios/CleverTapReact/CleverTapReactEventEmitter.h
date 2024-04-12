#import <React/RCTEventEmitter.h>

@interface CleverTapReactEventEmitter : RCTEventEmitter

+ (void)sendEventOnObserving:(NSString *)name body:(id)body;

@end
