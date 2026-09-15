/* Metro configuration for React Native
* https://github.com/facebook/react-native
*
* @format
*/

const path = require('path');
const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');
const exclusionList = require('metro-config/src/defaults/exclusionList');

const wrapperRoot = path.resolve(__dirname, '..');
const escapeForRegExp = (value) => value.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const defaultConfig = getDefaultConfig(__dirname);

const {
 resolver: { sourceExts, assetExts },
} = getDefaultConfig(__dirname);

const config = {
 watchFolders: [wrapperRoot],
 transformer: {
   getTransformOptions: async () => ({
     transform: {
       experimentalImportSupport: false,
       inlineRequires: true,
     },
   }),
   babelTransformerPath: require.resolve('react-native-svg-transformer'),
 },
 resolver: {
   assetExts: assetExts.filter(ext => ext !== 'svg'),
   sourceExts: [...sourceExts, 'svg'],
   nodeModulesPaths: [path.resolve(__dirname, 'node_modules')],
   // The SDK is symlinked from the wrapper repo root, which has its OWN node_modules
   // (used for lint). Without the two lines below, Metro resolves the SDK's
   // `import ... from 'react-native'` to THAT copy — a second, different react-native
   // in the bundle. Everything then works EXCEPT events: native emits on the app
   // copy's DeviceEventEmitter while the SDK listens on the other copy's, so
   // listeners never fire. Block the wrapper's node_modules entirely (the SDK has no
   // runtime deps) and pin react/react-native to the Example's copies.
   blockList: exclusionList([
     new RegExp(escapeForRegExp(path.join(wrapperRoot, 'node_modules')) + '/.*'),
   ]),
   extraNodeModules: {
     react: path.resolve(__dirname, 'node_modules/react'),
     'react-native': path.resolve(__dirname, 'node_modules/react-native'),
   },
 },
};

module.exports = mergeConfig(defaultConfig, config);
