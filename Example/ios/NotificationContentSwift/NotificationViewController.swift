import CTNotificationContent

/*
 Note: We have added two Notification Content target for Objective-C and Swift only for sample codes.
 You can also use multiple Notification Content target at a time, only you need to add different
`UNNotificationExtensionCategory` for each content target as added here in both Info.plist file,
 to show different category buttons added in AppDelegate file.
 */
class NotificationViewController: CTNotificationViewController {
    override func viewDidLoad() {
        super.viewDidLoad()
    }
}
