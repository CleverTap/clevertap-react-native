#import "CleverTapReactInstanceConfigRequest.h"

#pragma mark - Typed readers

// Every field is read through these. A missing key or NSNull reads as nil ("not set"). A
// wrong type records a message naming the field in *error. The error is STICKY: once set,
// later reads return nil without doing anything, so parse:error: can read every field in
// a row and check *error once at the end — the same shape as the Kotlin readers throwing
// InvalidConfigException, without using Objective-C exceptions for control flow.
// `prefix` names the enclosing block in the message ("ios.").
static id ctRead(NSDictionary *dict, NSString *key, Class expectedClass, NSString *typeName,
                 NSString *prefix, NSString **error) {
    if (*error != nil) {
        return nil;
    }
    id value = dict[key];
    if (value == nil || value == [NSNull null]) {
        return nil;
    }
    if (![value isKindOfClass:expectedClass]) {
        *error = [NSString stringWithFormat:@"%@%@ must be %@", prefix, key, typeName];
        return nil;
    }
    return value;
}

static NSString *ctReadString(NSDictionary *dict, NSString *key, NSString *prefix, NSString **error) {
    return ctRead(dict, key, [NSString class], @"a string", prefix, error);
}

// JS booleans arrive as NSNumber — but so do JS numbers. Only a real boolean (a CFBoolean
// underneath) is accepted, so `analyticsOnly: 1` is rejected exactly as Android rejects it
// with ReadableType.Boolean; otherwise boolValue would quietly turn 2 into YES.
static NSNumber *ctReadBool(NSDictionary *dict, NSString *key, NSString *prefix, NSString **error) {
    NSNumber *value = ctRead(dict, key, [NSNumber class], @"a boolean", prefix, error);
    if (value != nil && CFGetTypeID((__bridge CFTypeRef)value) != CFBooleanGetTypeID()) {
        *error = [NSString stringWithFormat:@"%@%@ must be a boolean", prefix, key];
        return nil;
    }
    return value;
}

// A string that must be exactly one of `allowed`. A typo or a different case must not fall
// back silently to a default level — `encryptionLevel: "HIGH"` used to mean "none" without
// a word to the developer.
static NSString *ctReadEnum(NSDictionary *dict, NSString *key, NSArray<NSString *> *allowed, NSString **error) {
    NSString *value = ctReadString(dict, key, @"", error);
    if (value != nil && ![allowed containsObject:value]) {
        *error = [NSString stringWithFormat:@"%@ must be one of %@", key, [allowed componentsJoinedByString:@", "]];
        return nil;
    }
    return value;
}

static NSDictionary *ctReadDict(NSDictionary *dict, NSString *key, NSString *prefix, NSString **error) {
    return ctRead(dict, key, [NSDictionary class], @"an object", prefix, error);
}

static NSArray<NSString *> *ctReadStringArray(NSDictionary *dict, NSString *key, NSString *prefix, NSString **error) {
    NSArray *array = ctRead(dict, key, [NSArray class], @"an array", prefix, error);
    for (id item in array) {
        if (![item isKindOfClass:[NSString class]]) {
            *error = [NSString stringWithFormat:@"%@%@ must be an array of strings", prefix, key];
            return nil;
        }
    }
    return array;
}

/// The string itself when it has non-whitespace content, otherwise nil.
static NSString *ctNonBlank(NSString *string) {
    NSString *trimmed = [string stringByTrimmingCharactersInSet:[NSCharacterSet whitespaceAndNewlineCharacterSet]];
    return trimmed.length > 0 ? string : nil;
}

#pragma mark - Request

@implementation CleverTapReactInstanceConfigRequest

+ (instancetype)parse:(NSDictionary *)config error:(NSString **)error {
    *error = nil;
    if (![config isKindOfClass:[NSDictionary class]]) {
        *error = @"a config object is required";
        return nil;
    }

    NSString *accountId = ctReadString(config, @"accountId", @"", error);
    NSString *accountToken = ctReadString(config, @"accountToken", @"", error);
    if (*error != nil) {
        return nil;
    }
    // Reject EMPTY as well as missing: the native SDK only nil-checks, so an empty string
    // would create a "zombie" instance whose events go nowhere while every call looks fine.
    if (ctNonBlank(accountId) == nil || ctNonBlank(accountToken) == nil) {
        *error = @"accountId and accountToken must be non-empty strings";
        return nil;
    }

    // A custom CleverTap ID can only be supplied AT CREATION, and only works together with
    // the useCustomCleverTapId flag. The native SDK does not fail on a mismatch: an ID
    // without the flag is IGNORED (CTDeviceInfo logs it and generates its own id, the app's
    // id is lost), and the flag without an ID leaves the account on an "error device id".
    // Both only surface as a native log a React Native developer never sees, and identity
    // cannot be repaired later from RN — so reject up front.
    NSNumber *useCustomCleverTapId = ctReadBool(config, @"useCustomCleverTapId", @"", error);
    NSString *cleverTapId = ctNonBlank(ctReadString(config, @"cleverTapId", @"", error));
    if (*error != nil) {
        return nil;
    }
    if (useCustomCleverTapId.boolValue != (cleverTapId != nil)) {
        *error = @"cleverTapId and useCustomCleverTapId: true must be given together (or both left out)"
                  " — the native SDK ignores an ID without the flag, and the flag without an ID leaves"
                  " the account with an error device id";
        return nil;
    }

    // Platform-specific options live in nested blocks; each platform reads only its own.
    NSDictionary *iosBlock = ctReadDict(config, @"ios", @"", error);

    CleverTapReactInstanceConfigRequest *request = [[self alloc] init];
    request->_accountId = [accountId copy];
    request->_accountToken = [accountToken copy];
    request->_region = [ctNonBlank(ctReadString(config, @"region", @"", error)) copy];
    request->_proxyDomain = [ctReadString(config, @"proxyDomain", @"", error) copy];
    request->_spikyProxyDomain = [ctReadString(config, @"spikyProxyDomain", @"", error) copy];
    request->_handshakeDomain = [ctReadString(config, @"handshakeDomain", @"", error) copy];
    request->_identityKeys = [ctReadStringArray(config, @"identityKeys", @"", error) copy];
    // Same accepted words as Android (InstanceConfigRequest.LOG_LEVELS / ENCRYPTION_LEVELS);
    // iOS maps "verbose" to its debug level.
    request->_logLevel = [ctReadEnum(config, @"logLevel", @[@"off", @"info", @"debug", @"verbose"], error) copy];
    request->_encryptionLevel = [ctReadEnum(config, @"encryptionLevel", @[@"none", @"medium", @"high"], error) copy];
    request->_analyticsOnly = ctReadBool(config, @"analyticsOnly", @"", error);
    request->_enablePersonalization = ctReadBool(config, @"enablePersonalization", @"", error);
    request->_disableAppLaunchedEvent = ctReadBool(config, @"disableAppLaunchedEvent", @"", error);
    request->_encryptionInTransit = ctReadBool(config, @"encryptionInTransit", @"", error);
    request->_useCustomCleverTapId = useCustomCleverTapId;
    request->_cleverTapId = [cleverTapId copy];
    if (iosBlock != nil) {
        request->_disableIDFV = ctReadBool(iosBlock, @"disableIDFV", @"ios.", error);
        request->_enableFileProtection = ctReadBool(iosBlock, @"enableFileProtection", @"ios.", error);
    }
    if (*error != nil) {
        return nil;
    }
    return request;
}

@end
