#import "AppDelegate.h"
#import <UserNotifications/UserNotifications.h>

#import <React/RCTBridge.h>
#import <React/RCTBundleURLProvider.h>
#import <React/RCTRootView.h>
#import <React/RCTLinkingManager.h>

#import "CleverTap.h"
#import "CleverTapReactManager.h"
#import "CleverTapReactCustomTemplates.h"

@implementation AppDelegate

# pragma mark - App Launch
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
  self.moduleName = @"Example";
  // You can add your custom initial props in the dictionary below.
  // They will be passed down to the ViewController used by React Native.
  self.initialProps = @{};
  
  // Add CleverTap Account ID and Account Token in your .plist file
  // Initialize CleverTap
#ifdef DEBUG
  [CleverTap setDebugLevel:CleverTapLogDebug];
#endif
  
  [CleverTapReactCustomTemplates registerCustomTemplates:@"templates", nil];
  [CleverTap autoIntegrate];
  [self addNotificationCategories];

  [[CleverTapReactManager sharedInstance] applicationDidLaunchWithOptions:launchOptions];
  
  return [super application:application didFinishLaunchingWithOptions:launchOptions];
}

# pragma mark - Notification Categories
- (void)addNotificationCategories {
    UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
    UNNotificationAction *action1 = [UNNotificationAction actionWithIdentifier:@"action_1" title:@"Back" options:UNNotificationActionOptionNone];
    UNNotificationAction *action2 = [UNNotificationAction actionWithIdentifier:@"action_2" title:@"Next" options:UNNotificationActionOptionNone];
    UNNotificationAction *action3 = [UNNotificationAction actionWithIdentifier:@"action_3" title:@"View In App" options:UNNotificationActionOptionNone];
    UNNotificationCategory *cat = [UNNotificationCategory categoryWithIdentifier:@"CTNotification" actions:@[action1, action2, action3] intentIdentifiers:@[] options:UNNotificationCategoryOptionNone];
    [center setNotificationCategories:[NSSet setWithObjects:cat, nil]];
}

# pragma mark - Deep links
- (BOOL)application:(UIApplication *)app
            openURL:(NSURL *)url
            options:(NSDictionary *)options {
  
  return [RCTLinkingManager application:app openURL:url sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey] annotation:options[UIApplicationOpenURLOptionsAnnotationKey]];
  
}

- (void)openURL:(NSURL*)url options:(NSDictionary<NSString *, id> *)options completionHandler:(void (^ __nullable)(BOOL success))completion {
  
  completion([RCTLinkingManager application:[UIApplication sharedApplication] openURL:url sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey] annotation:options[UIApplicationOpenURLOptionsAnnotationKey]]);
  
}

# pragma mark - Universal links
- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void (^)(NSArray<id<UIUserActivityRestoring>> * _Nullable))restorationHandler {
  return [RCTLinkingManager application:application continueUserActivity:userActivity restorationHandler:restorationHandler];
}

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
  return [self bundleURL];
}

- (NSURL *)bundleURL {
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

@end
