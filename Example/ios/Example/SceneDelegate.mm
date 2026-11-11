#import "SceneDelegate.h"
#import "AppDelegate.h"

#import <React/RCTLinkingManager.h>
#import <React/RCTRootView.h>

#import "CleverTapReactManager.h"

// Set once React has rendered for the first time. Static because it describes the
// app process, not one scene.
static BOOL sReactContentDidAppear = NO;

// UIScene lifecycle, required for apps built with the iOS 27 SDK (Xcode 27) or later.
// React Native 0.74's RCTAppDelegate still builds the React root view in
// application:didFinishLaunchingWithOptions:, so this scene delegate moves that
// root view controller into a window that belongs to the scene.
@implementation SceneDelegate {
  // Links that launched the app, held until JS can receive them.
  NSArray<UIOpenURLContext *> *_pendingURLContexts;
  NSUserActivity *_pendingUserActivity;
}

- (void)scene:(UIScene *)scene
    willConnectToSession:(UISceneSession *)session
                 options:(UISceneConnectionOptions *)connectionOptions {
  if (![scene isKindOfClass:[UIWindowScene class]]) {
    return;
  }

  // A push tap that launches the app arrives here instead of in the launch options.
  // This lets CleverTap.getInitialUrl() return the push's deep link.
  [[CleverTapReactManager sharedInstance] sceneWillConnectWithOptions:connectionOptions];

  AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
  UIViewController *rootViewController = appDelegate.window.rootViewController;

  self.window = [[UIWindow alloc] initWithWindowScene:(UIWindowScene *)scene];
  self.window.rootViewController = rootViewController;
  appDelegate.window = self.window;
  [self.window makeKeyAndVisible];

  // Deep links and universal links that launched the app. Under the UIScene lifecycle
  // they are not in the launch options, so Linking.getInitialURL() returns null for them.
  // They are sent as Linking 'url' events instead, which only reach JS once it listens.
  // On a cold start JS has not loaded yet, so hold them until React's first render.
  _pendingURLContexts = connectionOptions.URLContexts.allObjects;
  _pendingUserActivity = connectionOptions.userActivities.anyObject;
  if (sReactContentDidAppear) {
    [self flushPendingLinks];
  } else {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(reactContentDidAppear:)
                                                 name:RCTContentDidAppearNotification
                                               object:nil];
  }
}

- (void)reactContentDidAppear:(NSNotification *)notification {
  sReactContentDidAppear = YES;
  [[NSNotificationCenter defaultCenter] removeObserver:self
                                                  name:RCTContentDidAppearNotification
                                                object:nil];
  [self flushPendingLinks];
}

- (void)flushPendingLinks {
  NSArray<UIOpenURLContext *> *contexts = _pendingURLContexts;
  NSUserActivity *userActivity = _pendingUserActivity;
  _pendingURLContexts = nil;
  _pendingUserActivity = nil;
  for (UIOpenURLContext *context in contexts) {
    [self openURLContext:context];
  }
  if (userActivity) {
    [self continueUserActivity:userActivity];
  }
}

# pragma mark - Deep links
- (void)scene:(UIScene *)scene openURLContexts:(NSSet<UIOpenURLContext *> *)URLContexts {
  for (UIOpenURLContext *context in URLContexts) {
    [self openURLContext:context];
  }
}

- (void)openURLContext:(UIOpenURLContext *)context {
  NSMutableDictionary *options = [NSMutableDictionary dictionary];
  if (context.options.sourceApplication) {
    options[UIApplicationOpenURLOptionsSourceApplicationKey] = context.options.sourceApplication;
  }
  if (context.options.annotation) {
    options[UIApplicationOpenURLOptionsAnnotationKey] = context.options.annotation;
  }
  [RCTLinkingManager application:[UIApplication sharedApplication] openURL:context.URL options:options];
}

# pragma mark - Universal links
- (void)scene:(UIScene *)scene continueUserActivity:(NSUserActivity *)userActivity {
  [self continueUserActivity:userActivity];
}

- (void)continueUserActivity:(NSUserActivity *)userActivity {
  [RCTLinkingManager application:[UIApplication sharedApplication]
            continueUserActivity:userActivity
              restorationHandler:^(NSArray<id<UIUserActivityRestoring>> *_Nullable restorableObjects) {}];
}

@end
