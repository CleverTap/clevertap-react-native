#import <Foundation/Foundation.h>
#import "CleverTap.h"
#import "CleverTapSyncDelegate.h"
#import "CleverTapInAppNotificationDelegate.h"
#import "CleverTap+DisplayUnit.h"
#import "CleverTap+FeatureFlags.h"
#import "CleverTap+ProductConfig.h"
#import "CleverTapPushNotificationDelegate.h"
#import "CleverTap+PushPermission.h"

NS_ASSUME_NONNULL_BEGIN

/// One delegate object PER CleverTap account (the default account included). Each delegate
/// knows its own account id and stamps it into every event body it posts, so JS can route
/// the event to the right account handle.
///
/// Example: the delegate for account "ACCT_B" turns a profile-init callback into a body
/// `{CleverTapID: "xyz", __ctAccountId: "ACCT_B"}`; only the "ACCT_B" JS handle's listeners
/// receive it (and the tag is stripped before user code runs).
@interface CleverTapReactInstanceDelegate : NSObject <CleverTapSyncDelegate, CleverTapInAppNotificationDelegate, CleverTapDisplayUnitDelegate, CleverTapFeatureFlagsDelegate, CleverTapProductConfigDelegate, CleverTapPushNotificationDelegate, CleverTapPushPermissionDelegate>

- (instancetype)initWithAccountId:(NSString *)accountId;

@property (nonatomic, strong, readonly) NSString *accountId;

@end

NS_ASSUME_NONNULL_END
