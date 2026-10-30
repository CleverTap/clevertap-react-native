// swift-tools-version: 6.0
// Swift Package Manager manifest for React Native's experimental SPM support
// (react-native >= 0.87, `npx react-native spm`). CocoaPods integration keeps using
// clevertap-react-native.podspec; this file is only read by React Native's SPM autolinker.
//
// The autolinker symlinks this package to <app>/build/generated/autolinking/libs/CleverTapReact,
// so the relative `.package(path:)` entries below resolve against that location:
//   ../../../../xcframeworks -> <app>/build/xcframeworks   (ReactNative package)
//   ../../../ios             -> <app>/build/generated/ios  (React-GeneratedCode, codegen output)
//
// The product name must match `spm.name` in react-native.config.js.
// Keep the clevertap-ios-sdk version in sync with the CleverTap-iOS-SDK pin in the podspec.

import PackageDescription

let package = Package(
    name: "CleverTapReact",
    // React Native's generated ReactAppHeaders product requires iOS 15, so SPM consumers
    // need iOS 15. CocoaPods consumers keep the podspec's deployment target.
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "CleverTapReact", targets: ["CleverTapReact"]),
    ],
    dependencies: [
        .package(name: "ReactNative", path: "../../../../xcframeworks"),
        .package(name: "React-GeneratedCode", path: "../../../ios"),
        .package(url: "https://github.com/CleverTap/clevertap-ios-sdk.git", exact: "7.8.2"),
    ],
    targets: [
        .target(
            name: "CleverTapReact",
            dependencies: [
                .product(name: "ReactHeaders", package: "ReactNative"),
                .product(name: "ReactNativeHeaders", package: "ReactNative"),
                .product(name: "ReactNativeDependenciesHeaders", package: "ReactNative"),
                .product(name: "ReactAppHeaders", package: "React-GeneratedCode"),
                .product(name: "CleverTapSDK", package: "clevertap-ios-sdk"),
            ],
            path: "ios/CleverTapReact",
            publicHeadersPath: ".",
            // CocoaPods defines RCT_NEW_ARCH_ENABLED on pod targets under the New Architecture;
            // SwiftPM does not, and React Native's SPM path only runs the New Architecture.
            cSettings: [
                .define("RCT_NEW_ARCH_ENABLED"),
            ],
            cxxSettings: [
                .define("RCT_NEW_ARCH_ENABLED"),
                // Match the prebuilt React.framework's config-gated C++ ABI.
                .define("DEBUG", .when(configuration: .debug)),
                .define("NDEBUG", .when(configuration: .release)),
            ],
            linkerSettings: [
                .linkedFramework("UIKit"),
                .linkedFramework("CoreLocation"),
                .linkedFramework("UserNotifications"),
            ]
        ),
    ],
    cxxLanguageStandard: .cxx20
)
