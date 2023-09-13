#import "NotificationService.h"
#import <CleverTap-iOS-SDK/CleverTap.h>

/*
 Note: We have added two Notification Service target for Objective-C and Swift only for sample codes.
 You can activate/deactivate other target to use any one at a time.
 Steps to add/remove target: Click on Example target -> Build Phases -> Target Dependencies -> Click on add/remove items.
 */
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
