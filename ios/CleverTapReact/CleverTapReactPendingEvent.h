#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface CleverTapReactPendingEvent : NSObject

@property (nonatomic, strong) NSString *name;
@property (nonatomic, strong) id body;

- (instancetype)initWithName:(NSString *)name body:(id)body;

@end

NS_ASSUME_NONNULL_END
