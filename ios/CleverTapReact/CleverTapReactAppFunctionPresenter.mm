//
//  CleverTapReactAppFunctionPresenter.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactAppFunctionPresenter.h"
#import "CleverTapReact.h"
#import <React/RCTLog.h>

@implementation CleverTapReactAppFunctionPresenter

static BOOL _autoDismiss;

+ (BOOL)autoDismiss {
    return _autoDismiss;
}

+ (void)setAutoDismiss:(BOOL)value {
    _autoDismiss = value;
}

- (void)onPresent:(nonnull CTTemplateContext *)context {
    if ([self.class autoDismiss]) {
        RCTLogInfo(@"[CleverTap: Auto dismissing custom app function: %@]", [context templateName]);
        [context dismissed];
        return;
    }
    
    [CleverTapReact sendEventOnObserving:kCleverTapCustomFunctionPresent body:context.templateName];
}

- (void)onCloseClicked:(nonnull CTTemplateContext *)context {
    // NOOP - App Functions cannot have Action arguments.
}

@end
