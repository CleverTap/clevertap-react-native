//
//  CleverTapReactCustomTemplates.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <React/RCTLog.h>

#import "CleverTapReactCustomTemplates.h"
#import "CleverTapReactTemplatePresenter.h"
#import "CleverTapReactAppFunctionPresenter.h"
#import "CTJsonTemplateProducer.h"
#import "CTCustomTemplatesManager.h"

/// A `CTTemplateProducer` bound to one JSON definition. The SDK calls
/// `defineTemplates:` once per CleverTap instance — the default AND every secondary
/// account, including instances created later from JS — handing us that instance's
/// config. Each account gets the SAME json definitions, but wired to presenters that
/// know THAT account's id, so its template events can be routed to the right JS
/// handle. Parsing is delegated to the SDK's own `CTJsonTemplateProducer`.
@interface CleverTapReactAccountTemplateProducer : NSObject <CTTemplateProducer>

@property (nonatomic, strong) NSString *json;

@end

@implementation CleverTapReactAccountTemplateProducer

- (NSSet<CTCustomTemplate *> *)defineTemplates:(CleverTapInstanceConfig *)instanceConfig {
    CleverTapReactTemplatePresenter *templatePresenter =
        [[CleverTapReactTemplatePresenter alloc] initWithAccountId:instanceConfig.accountId];
    CleverTapReactAppFunctionPresenter *functionPresenter =
        [[CleverTapReactAppFunctionPresenter alloc] initWithAccountId:instanceConfig.accountId];
    CTJsonTemplateProducer *jsonProducer =
        [[CTJsonTemplateProducer alloc] initWithJson:self.json
                                   templatePresenter:templatePresenter
                                   functionPresenter:functionPresenter];
    return [jsonProducer defineTemplates:instanceConfig];
}

@end

@implementation CleverTapReactCustomTemplates

+ (void)registerCustomTemplates:(nonnull NSString *)firstJsonAsset, ... NS_REQUIRES_NIL_TERMINATION {
    va_list args;
    va_start(args, firstJsonAsset);

    NSBundle *bundle = [NSBundle mainBundle];
    [self registerCustomTemplates:bundle firstJsonAsset:firstJsonAsset args:args];
    va_end(args);
}

+ (void)registerCustomTemplates:(nonnull NSBundle *)bundle jsonFileNames:(nonnull NSString *)firstJsonAsset, ... NS_REQUIRES_NIL_TERMINATION {
    va_list args;
    va_start(args, firstJsonAsset);

    [self registerCustomTemplates:bundle firstJsonAsset:firstJsonAsset args:args];
    va_end(args);
}

+ (void)registerCustomTemplates:(NSBundle * _Nonnull)bundle firstJsonAsset:(NSString * _Nonnull)firstJsonAsset args:(va_list)args  {
    for (NSString *arg = firstJsonAsset; arg != nil; arg = va_arg(args, NSString*)) {
        NSString *filePath = [bundle pathForResource:arg ofType:@"json"];
        if (filePath) {
            NSString *definitionsJson = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:nil];

            CleverTapReactAccountTemplateProducer *producer = [CleverTapReactAccountTemplateProducer new];
            producer.json = definitionsJson;
            [CleverTap registerCustomInAppTemplates:producer];
        } else {
            RCTLogError(@"Custom templates JSON file not found. File name: \"%@\" in bundle: %@.", arg, bundle);
        }
    }
}

@end
