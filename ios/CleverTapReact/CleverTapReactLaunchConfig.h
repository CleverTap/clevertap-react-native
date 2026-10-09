#import <Foundation/Foundation.h>
#import "CleverTapInstanceConfig.h"

NS_ASSUME_NONNULL_BEGIN

/**
 * One account to create at app launch, passed to
 * -[CleverTapReactManager applicationDidLaunchWithOptions:launchConfigs:].
 *
 * Why this exists: an account created from JavaScript only comes to life when the
 * JS bundle runs (~2 seconds into a cold start). Events that fire before that —
 * most importantly the push tap that LAUNCHED the app — are lost for that account.
 * Listing the account here creates it inside didFinishLaunchingWithOptions instead,
 * so those events are caught and held until JS attaches its listeners.
 *
 * On the JS side, pick these accounts up with `CleverTap.getInstance(accountId)` —
 * do NOT pass a config again from JS; the one given here is the single source of
 * truth.
 */
@interface CleverTapReactLaunchConfig : NSObject

/// The account's creation config (id, token, region, ...).
@property (nonatomic, strong, readonly) CleverTapInstanceConfig *config;

/// Optional CUSTOM CleverTap ID — the app's own identifier for this device/user
/// (e.g. your customer id "CUST-88231") used INSTEAD of the SDK-generated one.
/// Only meaningful when config.useCustomCleverTapId is YES, and only applicable
/// at creation time (identity is fixed when the instance is born) — which is why
/// it lives here and has no setter anywhere else.
@property (nonatomic, copy, readonly, nullable) NSString *cleverTapID;

- (instancetype)initWithConfig:(CleverTapInstanceConfig *)config;
- (instancetype)initWithConfig:(CleverTapInstanceConfig *)config
                   cleverTapID:(nullable NSString *)cleverTapID;

- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
