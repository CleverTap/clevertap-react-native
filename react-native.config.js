module.exports = {
  // React Native's SPM autolinker would otherwise derive `ClevertapReactNative` from the npm name.
  // Keep the SwiftPM product name the same as the CocoaPods module name (see Package.swift).
  spm: {
    name: 'CleverTapReact',
  },
};
