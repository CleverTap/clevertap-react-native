#import <Foundation/Foundation.h>
#import "CleverTap.h"
#import "CleverTapReactLaunchConfig.h"

@interface CleverTapReactManager : NSObject

+ (instancetype)sharedInstance;

- (void)applicationDidLaunchWithOptions:(NSDictionary *)options;

/**
 * Same as applicationDidLaunchWithOptions:, plus creates the listed accounts
 * RIGHT NOW, before the launch notification fires. An account created from JS
 * only exists once the JS bundle runs (~2s into a cold start); events firing
 * before that — e.g. the push tap that launched the app — are lost for it.
 * Accounts listed here are created with their delegates attached while still
 * inside didFinishLaunchingWithOptions, so they receive the launch push payload
 * and buffer early events until JS is ready. On the JS side use
 * `CleverTap.getInstance(accountId)` for these accounts (no config again — the
 * one given here is the source of truth). Accounts NOT listed must be created
 * from JS with `createInstance(config)` as their first touch each run; they
 * cannot receive launch-time events.
 */
- (void)applicationDidLaunchWithOptions:(NSDictionary *)options
                          launchConfigs:(NSArray<CleverTapReactLaunchConfig *> *)launchConfigs;

- (void)setDelegates:(CleverTap *)cleverTapInstance;

@property NSString *launchDeepLink;

@end
