#import "NotificationService.h"
#import <CleverTap-iOS-SDK/CleverTap.h>

@implementation NotificationService

- (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler {
  // Add CleverTap Account ID and Account token in your target .plist file
  [CleverTap setDebugLevel:2];
  NSDictionary *profile = @{
                              @"Name": @"testUserA1",
                              @"Identity": @123456,
                              @"Email": @"test@test.com"
  };
  [[CleverTap sharedInstance] profilePush:profile];
  [[CleverTap sharedInstance] recordNotificationViewedEventWithData: request.content.userInfo];

  [super didReceiveNotificationRequest:request withContentHandler:contentHandler];
}

@end
