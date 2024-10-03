//
//  CleverTapReactAppFunctionPresenter.h
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <Foundation/Foundation.h>
#import "CTTemplatePresenter.h"

NS_ASSUME_NONNULL_BEGIN

/// A `CTTemplatePresenter` handling App Functions presentation.
/// Posts a `kCleverTapCustomFunctionPresent` notification to ReactNative
/// when an App Function onPresent is called.
@interface CleverTapReactAppFunctionPresenter : NSObject <CTTemplatePresenter>

@end

NS_ASSUME_NONNULL_END
