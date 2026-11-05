module.exports = {
  // React Native's SPM autolinker would otherwise derive `ClevertapReactNative` from the npm name.
  // Keep the SwiftPM product name the same as the CocoaPods module name (see Package.swift).
  // React Native 0.88 and later read the name from `swiftpmConfig` in package.json and warn that
  // this block is deprecated. React Native 0.87 only reads this block, so keep both in sync.
  spm: {
    name: 'CleverTapReact',
  },
};
