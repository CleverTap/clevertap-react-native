//
//  CleverTapReactTemplatePresenter.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactTemplatePresenter.h"
#import "CleverTapReact.h"
#import <React/RCTLog.h>

@implementation CleverTapReactTemplatePresenter

static BOOL _autoDismiss;

+ (BOOL)autoDismiss {
    return _autoDismiss;
}

+ (void)setAutoDismiss:(BOOL)value {
    _autoDismiss = value;
}

- (void)onPresent:(nonnull CTTemplateContext *)context {
    if ([self.class autoDismiss]) {
        RCTLogInfo(@"[CleverTap: Auto dismissing custom template: %@]", [context templateName]);
        [context dismissed];
        return;
    }
    
    [CleverTapReact sendEventOnObserving:kCleverTapCustomTemplatePresent body:context.templateName];
}

- (void)onCloseClicked:(nonnull CTTemplateContext *)context {
    [CleverTapReact sendEventOnObserving:kCleverTapCustomTemplateClose body:context.templateName];
}

@end
