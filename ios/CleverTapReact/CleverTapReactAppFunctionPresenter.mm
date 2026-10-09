//
//  CleverTapReactAppFunctionPresenter.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactAppFunctionPresenter.h"
#import "CleverTapReact.h"

@implementation CleverTapReactAppFunctionPresenter

- (instancetype)initWithAccountId:(NSString *)accountId {
    self = [super init];
    if (self) {
        _accountId = accountId;
    }
    return self;
}

- (void)onPresent:(nonnull CTTemplateContext *)context {
    // Same string-in-a-wrapper rule as the template presenter: the public payload
    // stays the template name string; the tag lets JS route it per account.
    NSDictionary *body = self.accountId == nil
        ? @{kCleverTapPayloadKey: context.templateName}
        : @{kCleverTapAccountIdKey: self.accountId, kCleverTapPayloadKey: context.templateName};
    [CleverTapReact sendEventOnObserving:kCleverTapCustomFunctionPresent body:body];
}

- (void)onCloseClicked:(nonnull CTTemplateContext *)context {
    // NOOP - App Functions cannot have Action arguments.
}

@end
