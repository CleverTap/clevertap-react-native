#import "CleverTapReactLaunchConfig.h"

@implementation CleverTapReactLaunchConfig

- (instancetype)initWithConfig:(CleverTapInstanceConfig *)config {
    return [self initWithConfig:config cleverTapID:nil];
}

- (instancetype)initWithConfig:(CleverTapInstanceConfig *)config
                   cleverTapID:(NSString *)cleverTapID {
    self = [super init];
    if (self) {
        _config = config;
        _cleverTapID = [cleverTapID copy];
    }
    return self;
}

@end
