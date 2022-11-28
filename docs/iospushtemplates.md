# iOS Push Templates integration with React Native

CleverTap Push Templates SDK helps you engage with your users using fancy push notification templates built specifically to work with [CleverTap](https://eu1.dashboard.clevertap.com/login.html).

# Table of contents

- [Installation](#installation)
- [Dashboard Usage](#dashboard-usage)
- [Template Types](#template-types)
- [Template Keys](#template-keys)

# Installation

[(Back to top)](#table-of-contents)
- Add Notification Content Extension (Push Templates) in a React Native iOS Project
Notification Content Extension is an app extension that provides a custom interface when a user previews your notification in the notification center. To enable the functionality of CleverTap iOS Push templates, we need this extension in the project which act as subclass to our CTNotificationContent framework.

Open Example.xcodeproj (or your app’s .xcodeproj file) in the ios folder of React Native project. Go to File > New > Target… and search with the name “Notification Content Extension“

![NotificationContentTarget](https://github.com/CleverTap/clevertap-react-native/blob/task/SDK-2395-RN-pushtemplates-support/static/NotificationContentTarget.png)

Add “Notification Content“ as target name.

![NotificationContent](https://github.com/CleverTap/clevertap-react-native/blob/task/SDK-2395-RN-pushtemplates-support/static/NotificationContent.png)

Refer to https://github.com/CleverTap/CTNotificationContent#-setup for setting up Notification Content target so that it imports CTNotificationContent framework in the code. Make sure to add the necessary key-values in  the Info.plist file of NotificationContent target so that it recognises the identifier of the incoming notification.  

Alternatively, go to finder and replace newly created NotificationContent  (or your content extension) folder with the NotificationContent folder in CTNotificationContent Example project repository.

![Finder](https://github.com/CleverTap/clevertap-react-native/blob/task/SDK-2395-RN-pushtemplates-support/static/Finder.png)

Add the following to a file named Podfile in the ios directory of your project.
```
use_frameworks!
target 'NotificationContent' do
    pod 'CTNotificationContent'
end
```

The final Podfile should look something like this:
```
require_relative '../node_modules/react-native/scripts/react_native_pods'
require_relative '../node_modules/@react-native-community/cli-platform-ios/native_modules'

platform :ios, '10.0'

use_frameworks!
target 'NotificationContent' do
    pod 'CTNotificationContent'
end

target 'Example' do
    
  config = use_native_modules!

  use_react_native!(:path => config["reactNativePath"])
  
  pod 'RNReanimated', :path => '../node_modules/react-native-reanimated'
  pod 'RNGestureHandler', :path => '../node_modules/react-native-gesture-handler'

  post_install do |installer|
    react_native_post_install(installer)
    # Apple Silicon builds require a library path tweak for Swift library discovery or "symbol not found" for swift things
    installer.aggregate_targets.each do |aggregate_target|
     aggregate_target.user_project.native_targets.each do |target|
      target.build_configurations.each do |config|
       config.build_settings['LIBRARY_SEARCH_PATHS'] = ['$(SDKROOT)/usr/lib/swift', '$(inherited)']
      end
     end
     aggregate_target.user_project.save
    end
   end
end
```
Now run the command pod install again. This will download and configure CTNotificationContent(iOS push templates) in your project.
```
For React Native 0.69 or below you need to change the build phase order so that React Native can use the iOS framework added with the help of podfile
Please check your React-Native version of the project before proceeding further. Go to package.json file in your project and check react-native key and check for the version.

If the react native version of your project is below 0.69 you need to drag the Generate Specs above Headers as explained below.
```
## For React Native version 0.69 or below:

Open Example.xcworkspace (or your app’s .xcworkspace file). Go to the Pods section in Xcode -> select FBReactNativeSpec -> Build Phases -> drag Generate Specs above Headers .
![GenerateSpecs](https://github.com/CleverTap/clevertap-react-native/blob/task/SDK-2395-RN-pushtemplates-support/static/GenerateSpecs.png)

## For React Native version 0.70 or above:

We must disable "hermes" and "flipper" from the Podfile since we are using use_frameworks and this causes build issues if both of them are enabled. 

Please observe that hermes_enabled is set from its default true state to false  by adding :hermes_enabled => false inside  use_react_native! snippet.
The podfile mentions this in comments:
```
Note that if you have use_frameworks! enabled, Flipper will not work and
    # you should disable the next line.
```
so we have added a # to comment the line
```
:flipper_configuration => FlipperConfiguration.enabled
```
The new Podfile should look like this:
```
require_relative '../node_modules/react-native/scripts/react_native_pods'
require_relative '../node_modules/@react-native-community/cli-platform-ios/native_modules'
platform :ios, '12.4'
install! 'cocoapods', :deterministic_uuids => false
use_frameworks!
target 'NotificationContent' do
    pod 'CTNotificationContent'
end  
target 'reactnativedemo' do
  config = use_native_modules!
  # Flags change depending on the env values.
  flags = get_default_flags()
  use_react_native!(
    :path => config[:reactNativePath],
    # Hermes is now enabled by default. Disable by setting this flag to false.
    # Upcoming versions of React Native may rely on get_default_flags(), but
    # we make it explicit here to aid in the React Native upgrade process.
    :hermes_enabled => false,
    :fabric_enabled => flags[:fabric_enabled],
    # Enables Flipper.
    #
    # Note that if you have use_frameworks! enabled, Flipper will not work and
    # you should disable the next line.
    # :flipper_configuration => FlipperConfiguration.enabled,
    # An absolute path to your application root.
    :app_path => "#{Pod::Config.instance.installation_root}/.."
  )
  post_install do |installer|
    react_native_post_install(
      installer,
      # Set `mac_catalyst_enabled` to `true` in order to apply patches
      # necessary for Mac Catalyst builds
      :mac_catalyst_enabled => false
    )
    __apply_Xcode_12_5_M1_post_install_workaround(installer)
  end
end
```

### Out of the box


# Dashboard Usage

[(Back to top)](#table-of-contents)
While creating a Push Notification campaign on CleverTap, just follow the steps below -

1. On the "WHAT" section pass the desired required values in the "title" and "message" fields (NOTE: These are iOS alert title and body).
![Dashboard alert](https://github.com/CleverTap/CTNotificationContent/blob/master/images/dashboard_alert.png)
2. Click on "Advanced" and then click on "Rich Media" and select Single or Carousel template.
![Dashboard Rich Media](https://github.com/CleverTap/CTNotificationContent/blob/master/images/dashboard_richMedia.png)
3. For adding custom key-value pair, add the [template Keys](#template-keys) individually or into one JSON object and use the `pt_json` key to fill in the values.
![Dashboard Custom Key individual](https://github.com/CleverTap/CTNotificationContent/blob/master/images/dashboard_customKeysIndividual.png)
![Dashboard Custom Key JSON](https://github.com/CleverTap/CTNotificationContent/blob/master/images/dashboard_customKeyValue.png)
4. Send a test push and schedule!

# Template Types

[(Back to top)](#table-of-contents)

## Rich Media
### Single Media
Single media is for basic view with single image.
![Single Media](https://github.com/CleverTap/CTNotificationContent/blob/master/images/SingleMedia.png)

### Content Slider
Content Slider is for image slideshow view where user can add multiple images with different captions, sub-captions, and actions.

<img src="https://github.com/CleverTap/CTNotificationContent/blob/master/images/ContentSlider.gif" alt="Content slider" width="450" height="800"/>

## Custom key-value pair

### Basic Template
Basic Template is the basic push notification received on apps where user can also update text colour, background colour.

![Custom Basic template](https://github.com/CleverTap/CTNotificationContent/blob/master/images/CustomBasicTemplate.png)

### Auto Carousel Template
Auto carousel is an automatic revolving carousel push notification where user can also update text colour, background colour.

<img src="https://github.com/CleverTap/CTNotificationContent/blob/master/images/CustomAutoCarousel.gif" alt="Auto carousel" width="450" height="800"/>

### Manual Carousel Template
This is the manual version of the carousel. The user can navigate to the next/previous image by clicking on the Next/Back buttons.
---
**NOTE:**

For iOS 12 and above, you need to configure your Notification Content target Info.plist to reflect the category identifier you registered: `NSExtension -> NSExtensionAttributes -> UNNotificationExtensionCategory`.  In addition, set the `UNNotificationExtensionInitialContentSizeRatio -> 0.1` ,  `UNNotificationExtensionDefaultContentHidden -> true` and `UNNotificationExtensionUserInteractionEnabled -> 1`.

For iOS 11 and below, the previous/next buttons will not work. Please use notification actions with identifiers `action_1` and `action_2` for this purpose.

---

<img src="https://github.com/CleverTap/CTNotificationContent/blob/master/images/CustomManualCarousel.gif" alt="Manual carousel" width="450" height="800"/>

### Timer Template
This template features a live countdown timer. You can even choose to show different title, message, and background image after the timer expires.

<img src="https://github.com/CleverTap/CTNotificationContent/blob/master/images/CustomTimerTemplate.gif" alt="Timer template" width="450" height="800"/>

### Zero Bezel Template

The Zero Bezel template ensures that the background image covers the entire available surface area of the push notification. All the text is overlayed on the image.

![Zero Bezel template](https://github.com/CleverTap/CTNotificationContent/blob/master/images/ZeroBezel.png)

### Rating Template

Rating template lets your users give you feedback.

**NOTE:**

For iOS 12 and above, you need to configure your Notification Content target Info.plist to reflect the category identifier you registered: `NSExtension -> NSExtensionAttributes -> UNNotificationExtensionCategory`.  In addition, set the `UNNotificationExtensionInitialContentSizeRatio -> 0.1` ,  `UNNotificationExtensionDefaultContentHidden -> true` and `UNNotificationExtensionUserInteractionEnabled -> 1`.
For iOS 11 and below, it will fallback to a basic template.

---

![Rating](https://github.com/CleverTap/CTNotificationContent/blob/master/images/Rating.gif)

### Product Catalog Template

Product catalog template lets you show case different images of a product (or a product catalog) before the user can decide to click on the "BUY NOW" option which can take them directly to the product via deep links. This template has two variants.

**NOTE:**

For iOS 12 and above, you need to configure your Notification Content target Info.plist to reflect the category identifier you registered: `NSExtension -> NSExtensionAttributes -> UNNotificationExtensionCategory`.  In addition, set the `UNNotificationExtensionInitialContentSizeRatio -> 0.1` ,  `UNNotificationExtensionDefaultContentHidden -> true` and `UNNotificationExtensionUserInteractionEnabled -> 1`.
For iOS 11 and below, it will fallback to a basic template.

---

### Vertical View 

![Product Display](https://github.com/CleverTap/CTNotificationContent/blob/master/images/ProductDisplayVertical.gif)

### Linear View

Use the following keys to enable linear view variant of this template.

Template Key | Required | Value
---:|:---:|:---
pt_product_display_linear | Optional | `true`

![Product Display](https://github.com/CleverTap/CTNotificationContent/blob/master/images/ProductDisplayLinear.gif)

### WebView Template 

WebView template lets you load a remote https URL.

![WebView Template](https://github.com/CleverTap/CTNotificationContent/blob/master/images/WebView.gif)

**Note:** If any image can't be downloaded, the template falls back to basic template with caption and sub caption only.

# Template Keys

[(Back to top)](#table-of-contents)

## Rich Media
### Content Slider
Configure your APNS payload:

Then, when sending notifications via [APNS](https://developer.apple.com/library/content/documentation/NetworkingInternet/Conceptual/RemoteNotificationsPG/APNSOverview.html):
- include the mutable-content flag in your payload aps entry (this key must be present in the aps payload or the system will not call your app extension) 
- for the Image Slideshow view, add the `ct_ContentSlider` key with a json object value, see example below, to the payload, outside of the aps entry.



```
{

    "aps": {
        "alert": {
            "body": "test message",
            "title": "test title",
        },
        "category": "CTNotification",
        "mutable-content": true,
      },
    "ct_ContentSlider": {
        "orientation": "landscape", // landscape assumes 16:9 images, remove to display default square/portrait images
        "showsPaging": true, // optional to display UIPageControl
        "autoPlay": true, // optional to auto play the slideshow
        "autoDismiss": true, // optional to auto dismiss the notification on item actionUrl launch
        "items":[
            {
                "caption": "caption one",
                "subcaption": "subcaption one",
                "imageUrl": "https://s3.amazonaws.com/ct-demo-images/landscape-1.jpg",
                "actionUrl": "com.clevertap.ctcontent.example://item/one"
            }, 
            {
                "caption": "caption two", 
                "subcaption": "subcaption two", 
                "imageUrl": "https://s3.amazonaws.com/ct-demo-images/landscape-2.jpg",
                "actionUrl": "com.clevertap.ctcontent.example://item/two"
            }
       ]
   }
}
```
## Custom key-value pair

### Basic Template
Basic Template Keys | Required | Description
 ---:|:---:|:---| 
pt_id | Required | Value - `pt_basic`
pt_title | Required | Title
pt_msg | Required | Message
pt_msg_summary | Required | Message line when Notification is expanded
pt_bg | Required | Background Color in HEX
pt_big_img | Optional | Image
pt_dl1 | Optional | One Deep Link
pt_title_clr | Optional | Title Color in HEX
pt_msg_clr | Optional | Message Color in HEX
pt_json | Optional | Above keys in JSON format

### Auto Carousel Template

Auto Carousel Template Keys | Required | Description
  ---:|:---:|:--- 
pt_id | Required | Value - `pt_carousel`
pt_title | Required | Title
pt_msg | Required | Message
pt_msg_summary | Optional | Message line when Notification is expanded
pt_dl1 | Required | Deep Link
pt_img1 | Required | Image One
pt_img2 | Required | Image Two
pt_img3 | Required | Image Three
pt_bg | Required | Background Color in HEX
pt_title_clr | Optional | Title Color in HEX
pt_msg_clr | Optional | Message Color in HEX
pt_json | Optional | Above keys in JSON format

### Manual Carousel Template

Manual Carousel Template Keys | Required | Description
  ---:|:---:|:--- 
pt_id | Required | Value - `pt_manual_carousel`
pt_title | Required | Title
pt_msg | Required | Message
pt_msg_summary | Optional | Message line when Notification is expanded
pt_dl1 | Required | Deep Link One
pt_img1 | Required | Image One
pt_img2 | Required | Image Two
pt_img3 | Required | Image Three
pt_bg | Required | Background Color in HEX
pt_title_clr | Optional | Title Color in HEX
pt_msg_clr | Optional | Message Color in HEX
pt_json | Optional | Above keys in JSON format

### Timer Template

Timer Template Keys | Required | Description
  ---:|:---:|:--- 
pt_id | Required | Value - `pt_timer`
pt_title | Required | Title
pt_title_alt | Optional | Title to show after timer expires
pt_msg | Required | Message
pt_msg_alt | Optional | Message to show after timer expires
pt_msg_summary | Optional | Message line when Notification is expanded
pt_dl1 | Required | Deep Link
pt_big_img | Optional | Image
pt_big_img_alt | Optional | Image to show when timer expires
pt_bg | Required | Background Color in HEX
pt_chrono_title_clr | Optional | Color for timer text in HEX
pt_timer_threshold | Required | Timer duration in seconds. Will be given higher priority. 
pt_timer_end | Optional | Epoch Timestamp to countdown to (for example, $D_1595871380 or 1595871380). Not needed if pt_timer_threshold is specified.
pt_title_clr | Optional | Title Color in HEX
pt_msg_clr | Optional | Message Color in HEX
pt_json | Optional | Above keys in JSON format

### Zero Bezel Template
 
 Zero Bezel Template Keys | Required | Description 
  ---:|:---:|:--- 
  pt_id | Required | Value - `pt_zero_bezel`
  pt_title | Required | Title 
  pt_msg | Required | Message
  pt_msg_summary | Optional | Message line when Notification is expanded
  pt_subtitle | Optional | Subtitle
  pt_big_img | Required | Image
  pt_dl1 | Required | Deep Link
  pt_title_clr | Optional | Title Color in HEX
  pt_msg_clr | Optional | Message Color in HEX
  pt_json | Optional | Above keys in JSON format

### Rating Template

Rating Template Keys | Required | Description
 ---:|:---:|:--- 
pt_id | Required  | Value - `pt_rating`
pt_title | Required  | Title
pt_msg | Required  | Message
pt_big_img | Optional | Image
pt_msg_summary | Optional | Message line when Notification is expanded
pt_subtitle | Optional | Subtitle
pt_default_dl | Required  | Default Deep Link for Push Notification
pt_dl1 | Required  | Deep Link for first/all star(s)
pt_dl2 | Optional | Deep Link for second star
pt_dl3 | Optional | Deep Link for third star
pt_dl4 | Optional | Deep Link for fourth star
pt_dl5 | Optional | Deep Link for fifth star
pt_bg | Required  | Background Color in HEX
pt_ico | Optional | Large Icon
pt_title_clr | Optional | Title Color in HEX
pt_msg_clr | Optional | Message Color in HEX
pt_json | Optional | Above keys in JSON format

### Product Catalog Template

Product Catalog Template Keys | Required | Description
 ---:|:---:|:--- 
pt_id | Required  | Value - `pt_product_display`
pt_title | Required  | Title
pt_msg | Required  | Message
pt_subtitle | Optional  | Subtitle
pt_img1 | Required  | Image One
pt_img2 | Required  | Image Two
pt_img3 | Optional  | Image Three
pt_bt1 | Required  | Big text for first image
pt_bt2 | Required  | Big text for second image
pt_bt3 | Required  | Big text for third image
pt_st1 | Required  | Small text for first image
pt_st2 | Required  | Small text for second image
pt_st3 | Required  | Small text for third image
pt_dl1 | Required  | Deep Link for first image
pt_dl2 | Required  | Deep Link for second image
pt_dl3 | Required  | Deep Link for third image
pt_price1 | Required  | Price for first image
pt_price2 | Required  | Price for second image
pt_price3 | Required  | Price for third image
pt_bg | Required  | Background Color in HEX
pt_product_display_action | Required  | Action Button Label Text
pt_product_display_linear | Optional  | Linear Layout Template ("true"/"false")
pt_product_display_action_clr | Required  | Action Button Background Color in HEX
pt_title_clr | Optional  | Title Color in HEX
pt_msg_clr | Optional  | Message Color in HEX
pt_json | Optional  | Above keys in JSON format

### WebView Template

WebView Template Keys | Required | Description
 ---:|:---:|:--- 
pt_id | Required  | Value - `pt_web_view`
pt_dl1 | Required  | Deep Link
pt_url | Required  | URL to load
pt_orientation | Optional  | Value - `landscape` or `portrait`
pt_json | Optional  | Above keys in JSON format
