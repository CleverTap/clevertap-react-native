//
//  CleverTapReactAppFunctionPresenter.h
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <Foundation/Foundation.h>
#if __has_include(<CleverTapSDK/CleverTap.h>)
#import <CleverTapSDK/CTTemplatePresenter.h>
#else
#import "CTTemplatePresenter.h"
#endif

NS_ASSUME_NONNULL_BEGIN

/// A `CTTemplatePresenter` handling App Functions presentation for ONE CleverTap
/// account. Posts a `kCleverTapCustomFunctionPresent` notification to ReactNative
/// when an App Function onPresent is called — the body tagged with this presenter's
/// account id so JS routes the event to the right account handle.
@interface CleverTapReactAppFunctionPresenter : NSObject <CTTemplatePresenter>

- (instancetype)initWithAccountId:(nullable NSString *)accountId;

@property (nonatomic, strong, readonly, nullable) NSString *accountId;

@end

NS_ASSUME_NONNULL_END
