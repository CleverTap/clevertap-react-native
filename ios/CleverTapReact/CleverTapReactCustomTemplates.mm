//
//  CleverTapReactCustomTemplates.m
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import "CleverTapReactCustomTemplates.h"
#import "CleverTapReactTemplatePresenter.h"
#import "CleverTapReactAppFunctionPresenter.h"
#import "CTJsonTemplateProducer.h"
#import "CTCustomTemplatesManager.h"

@implementation CleverTapReactCustomTemplates

+ (void)registerCustomTemplates:(nonnull NSString *)firstJsonAsset, ... __attribute__((sentinel(0, 1))) {
    va_list args;
    va_start(args, firstJsonAsset);
    for (NSString *arg = firstJsonAsset; arg != nil; arg = va_arg(args, NSString*)) {
        NSString *filePath = [[NSBundle mainBundle] pathForResource:arg ofType:@"json"];
        NSString *definitionsJson = [NSString stringWithContentsOfFile:filePath encoding:NSUTF8StringEncoding error:nil];
        CleverTapReactTemplatePresenter *templatePresenter = [[CleverTapReactTemplatePresenter alloc] init];
        CleverTapReactAppFunctionPresenter *functionPresenter = [[CleverTapReactAppFunctionPresenter alloc] init];
        
        CTJsonTemplateProducer *producer = [[CTJsonTemplateProducer alloc] initWithJson:definitionsJson templatePresenter:templatePresenter functionPresenter:functionPresenter];
        [CleverTap registerCustomInAppTemplates:producer];
    }
    va_end(args);
}

@end
