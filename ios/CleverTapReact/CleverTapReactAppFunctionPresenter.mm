//
//  CleverTapReactAppFunctionPresenter.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactAppFunctionPresenter.h"
#import "CleverTapReact.h"

@implementation CleverTapReactAppFunctionPresenter

- (void)onPresent:(nonnull CTTemplateContext *)context {
    [CleverTapReact sendEventOnObserving:kCleverTapCustomFunctionPresent body:context.templateName];
}

- (void)onCloseClicked:(nonnull CTTemplateContext *)context {
    // NOOP - App Functions cannot have Action arguments.
}

@end
