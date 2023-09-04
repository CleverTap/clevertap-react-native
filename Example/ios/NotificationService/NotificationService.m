#import "NotificationService.h"

@implementation NotificationService

- (void)didReceiveNotificationRequest:(UNNotificationRequest *)request withContentHandler:(void (^)(UNNotificationContent * _Nonnull))contentHandler {
  [super didReceiveNotificationRequest:request withContentHandler:contentHandler];
}

@end
