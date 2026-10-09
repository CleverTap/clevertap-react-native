//
//  CleverTapReactTemplatePresenter.h
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <Foundation/Foundation.h>
#import "CTTemplatePresenter.h"

NS_ASSUME_NONNULL_BEGIN

/// A `CTTemplatePresenter` handling Custom Templates presentation for ONE CleverTap
/// account. Posts a `kCleverTapCustomTemplatePresent` notification to ReactNative
/// when a Custom Template `onPresent:` is called and `kCleverTapCustomTemplateClose`
/// on `onCloseClicked:` — each body tagged with this presenter's account id so JS
/// routes the event to the right account handle.
@interface CleverTapReactTemplatePresenter : NSObject <CTTemplatePresenter>

- (instancetype)initWithAccountId:(nullable NSString *)accountId;

@property (nonatomic, strong, readonly, nullable) NSString *accountId;

@end

NS_ASSUME_NONNULL_END
