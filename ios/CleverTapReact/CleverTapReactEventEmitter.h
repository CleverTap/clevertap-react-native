#import <React/RCTEventEmitter.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <CTTurboModuleSpec/CTTurboModuleSpec.h>
@interface CleverTapReactEventEmitter: RCTEventEmitter <NativeCleverTapModuleSpec>
#else
@interface CleverTapReactEventEmitter : RCTEventEmitter
#endif

+ (void)sendEventOnObserving:(NSString *)name body:(id)body;

@end
