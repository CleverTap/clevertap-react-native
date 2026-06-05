/* Metro configuration for React Native
* https://github.com/facebook/react-native
*
* @format
*/

const path = require('path');
const { getDefaultConfig, mergeConfig } = require('@react-native/metro-config');

const wrapperRoot = path.resolve(__dirname, '..');

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
 },
};

module.exports = mergeConfig(defaultConfig, config);
