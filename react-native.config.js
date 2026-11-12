module.exports = {
  // Names the SwiftPM product `CleverTapReact`, matching the CocoaPods module, instead of the
  // npm-derived `ClevertapReactNative`. Only RN 0.87 reads this. RN 0.88+ reads `swiftpmConfig`
  // in package.json, so keep both in sync.
  spm: {
    name: 'CleverTapReact',
  },
};
