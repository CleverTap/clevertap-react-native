//
//  CleverTapReactCustomTemplates.h
//  CleverTapReact
//
//  Created by Nikola Zagorchev on 2.10.24.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/// Registers Custom Templates and App Functions using JSON templates definitions.
@interface CleverTapReactCustomTemplates : NSObject

/// Registers Custom Templates and App Functions given JSON templates definition file names.
/// Provide the file name only, without the json extension.
/// Uses the main bundle to get the file path. Ensure the files are aded to the Copy Bundle Resources phase.
/// Supports multiple file names with a nil termination.
///
/// Example registering Custom Templates and App Functions using JSON templates definition.
/// The JSON file name is "templates.json".
/// ```
/// [CleverTapReactCustomTemplates registerCustomTemplates:@"templates", nil];
/// ```
///
/// @param firstJsonAsset The first JSON templates definition file name.
/// @param ... A comma-separated list of additional file names, ending with nil.
+ (void)registerCustomTemplates:(NSString *)firstJsonAsset, ... NS_REQUIRES_NIL_TERMINATION;

/// Registers Custom Templates and App Functions given JSON templates definition file names.
/// Use the bundle where the resources are.
/// Provide the file name only, without the json extension.
/// Supports multiple file names with a nil termination.
///
/// @param bundle The bundle where the JSON resources are.
/// @param firstJsonAsset The first JSON templates definition file name.
/// @param ... A comma-separated list of additional file names, ending with nil.
+ (void)registerCustomTemplates:(NSBundle *)bundle jsonFileNames:(NSString *)firstJsonAsset, ... NS_REQUIRES_NIL_TERMINATION;

@end

NS_ASSUME_NONNULL_END
