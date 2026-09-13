#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 * The validated input of `createInstance(config)`, read from the JS dictionary BEFORE any
 * native SDK object is built. Android twin: InstanceConfigRequest.kt — same fields, same
 * rules, so the two bridges reject the same inputs with the same messages.
 *
 * Reading rules:
 *  - a missing key or a JS `null` (which arrives as NSNull) means "not set" — a
 *    server-provided config such as {"analyticsOnly": null} is a normal input;
 *  - a value of the wrong type is a developer mistake: parse:error: returns nil with a
 *    message naming the field, and createInstance rejects the promise with EINVALID.
 *
 * Why the dictionary is not read where it is used: `[config[@"analyticsOnly"] boolValue]`
 * crashes on NSNull ("unrecognized selector"), and a number under a string key would be
 * handed to the SDK as an NSString.
 *
 * Nullable NSNumber properties are the iOS spelling of Kotlin's `Boolean?`: nil = not set,
 * otherwise @YES / @NO (Objective-C has no separate Boolean class).
 */
@interface CleverTapReactInstanceConfigRequest : NSObject

@property (nonatomic, copy, readonly) NSString *accountId;
@property (nonatomic, copy, readonly) NSString *accountToken;
/// Non-blank region, or nil for "no region".
@property (nonatomic, copy, readonly, nullable) NSString *region;
@property (nonatomic, copy, readonly, nullable) NSString *proxyDomain;
@property (nonatomic, copy, readonly, nullable) NSString *spikyProxyDomain;
@property (nonatomic, copy, readonly, nullable) NSString *handshakeDomain;
@property (nonatomic, copy, readonly, nullable) NSArray<NSString *> *identityKeys;
@property (nonatomic, copy, readonly, nullable) NSString *logLevel;
@property (nonatomic, copy, readonly, nullable) NSString *encryptionLevel;
@property (nonatomic, strong, readonly, nullable) NSNumber *analyticsOnly;
@property (nonatomic, strong, readonly, nullable) NSNumber *enablePersonalization;
@property (nonatomic, strong, readonly, nullable) NSNumber *disableAppLaunchedEvent;
@property (nonatomic, strong, readonly, nullable) NSNumber *encryptionInTransit;
/// nil = not given (the SDK config keeps its default).
@property (nonatomic, strong, readonly, nullable) NSNumber *useCustomCleverTapId;
/// Non-blank custom id, or nil. Non-nil implies useCustomCleverTapId == YES.
@property (nonatomic, copy, readonly, nullable) NSString *cleverTapId;
/// From the iOS-only "ios" block. The "android" block is read by Android alone.
@property (nonatomic, strong, readonly, nullable) NSNumber *disableIDFV;
@property (nonatomic, strong, readonly, nullable) NSNumber *enableFileProtection;

/**
 * Parses and validates `config`.
 *
 * @param config the dictionary React Native handed to createInstance (may be nil).
 * @param error  set to a message naming the field (e.g. "analyticsOnly must be a boolean")
 *               when nil is returned; left nil on success. Must not be NULL.
 * @return the validated request, or nil when the input cannot be accepted.
 */
+ (nullable instancetype)parse:(nullable NSDictionary *)config
                         error:(NSString * _Nullable * _Nonnull)error;

@end

NS_ASSUME_NONNULL_END
