//
//  CleverTapReactCustomTemplates.h
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface CleverTapReactCustomTemplates : NSObject

+ (void)registerCustomTemplates:(NSString *)firstJsonAsset, ... NS_REQUIRES_NIL_TERMINATION;

@end

NS_ASSUME_NONNULL_END
