#import "CleverTapReactPendingEvent.h"

@implementation CleverTapReactPendingEvent

- (instancetype)initWithName:(NSString *)name body:(id)body {
    self = [super init];
    if (self) {
        _name = name;
        _body = body;
    }
    return self;
}

@end
