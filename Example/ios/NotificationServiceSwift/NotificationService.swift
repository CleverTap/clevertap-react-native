import CTNotificationService
import CleverTapSDK

/*
 Note: We have added two Notification Service target for Objective-C and Swift only for sample codes.
 You can activate/deactivate other target to use any one at a time.
 Steps to add/remove target: Click on Example target -> Build Phases -> Target Dependencies -> Click on add/remove items.
 */
class NotificationService: CTNotificationServiceExtension {
  override func didReceive(_ request: UNNotificationRequest, withContentHandler contentHandler: @escaping (UNNotificationContent) -> Void) {
    let profile: Dictionary<String, AnyObject> = [
      "Name": "testUserA1" as AnyObject,
      "Identity": 123456 as AnyObject,
      "Email": "test@test.com" as AnyObject ]
    CleverTap.sharedInstance()?.profilePush(profile)
    CleverTap.sharedInstance()?.recordNotificationViewedEvent(withData: request.content.userInfo)
    super.didReceive(request, withContentHandler: contentHandler)
  }
}
