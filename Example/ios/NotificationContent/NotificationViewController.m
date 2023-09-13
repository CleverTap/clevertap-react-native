#import "NotificationViewController.h"

/*
 Note: We have added two Notification Content target for Objective-C and Swift only for sample codes.
 You can also use multiple Notification Content target at a time, only you need to add different
`UNNotificationExtensionCategory` for each content target as added here in both Info.plist file,
 to show different category buttons added in AppDelegate file.
 */
@implementation NotificationViewController

- (void)viewDidLoad {
    [super viewDidLoad];
}

// optional: implement to get user event type data
- (void)userDidPerformAction:(NSString *)action withProperties:(NSDictionary *)properties {
    NSLog(@"user did perform action: %@ with props: %@", action , properties);
}

// optional: implement to get notification response
- (void)userDidReceiveNotificationResponse:(UNNotificationResponse *)response {
    NSLog(@"user did receive notification response: %@:", response);
}

@end
