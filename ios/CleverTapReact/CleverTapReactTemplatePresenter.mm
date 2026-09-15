//
//  CleverTapReactTemplatePresenter.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactTemplatePresenter.h"
#import "CleverTapReact.h"

@implementation CleverTapReactTemplatePresenter

- (instancetype)initWithAccountId:(NSString *)accountId {
    self = [super init];
    if (self) {
        _accountId = accountId;
    }
    return self;
}

- (void)onPresent:(nonnull CTTemplateContext *)context {
    [CleverTapReact sendEventOnObserving:kCleverTapCustomTemplatePresent
                                    body:[self wrapTemplateName:context.templateName]];
}

- (void)onCloseClicked:(nonnull CTTemplateContext *)context {
    [CleverTapReact sendEventOnObserving:kCleverTapCustomTemplateClose
                                    body:[self wrapTemplateName:context.templateName]];
}

// The public payload of template events is (and stays) the template name STRING.
// A string cannot carry the account tag, so native wraps it and the JS demux
// unwraps it before user handlers run. A nil account id (never seen in practice —
// the config requires one) degrades to an untagged body instead of failing.
- (NSDictionary *)wrapTemplateName:(NSString *)templateName {
    if (self.accountId == nil) {
        return @{kCleverTapPayloadKey: templateName};
    }
    return @{kCleverTapAccountIdKey: self.accountId, kCleverTapPayloadKey: templateName};
}

@end
