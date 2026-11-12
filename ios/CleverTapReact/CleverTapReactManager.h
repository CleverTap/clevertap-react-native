#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#if __has_include(<CleverTapSDK/CleverTap.h>)
#import <CleverTapSDK/CleverTap.h>
#else
#import "CleverTap.h"
#endif
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

/**
 * For scene-based apps. A push that launches the app arrives in the scene's connection
 * options, not the launch options. Call this from scene:willConnectToSession:options:
 * so getInitialUrl returns the push's deep link.
 */
- (void)sceneWillConnectWithOptions:(UISceneConnectionOptions *)connectionOptions
    NS_SWIFT_NAME(sceneWillConnect(options:));

- (void)setDelegates:(CleverTap *)cleverTapInstance;

@property NSString *launchDeepLink;

@end
