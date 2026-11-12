#import "SceneDelegate.h"
#import "AppDelegate.h"

#import <React/RCTLinkingManager.h>
#import <React/RCTRootView.h>

#import "CleverTapReactManager.h"

// Set once React has rendered for the first time. Static because it describes the
// app process, not one scene.
static BOOL sReactContentDidAppear = NO;

// Required for apps built with Xcode 27 or later. RN 0.74 creates the root view in the
// app delegate, so this moves it into the scene's window.
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

  // Links that launched the app. With scenes, Linking.getInitialURL() doesn't see them,
  // so send them as 'url' events once React has rendered and JS is listening.
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
