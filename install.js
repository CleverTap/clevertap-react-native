#!/usr/bin/env node

const fs = require('fs')
const path = require('path')
const extract = require('extract-zip')

const sdkPath = path.join(__dirname, 'ios/CleverTapReact/'); 
const zipFile = sdkPath+'CleverTapSDK.framework.zip';
const errMsg = "error unzipping CleverTapSDK.framework.zip in " + sdkPath + " please unzip manually";

fs.access(zipFile, (err) => {
  if (!err) {
	extract(zipFile, {dir: sdkPath}, function (err) {
        if (!err) {
            fs.unlinkSync(zipFile);
        } else {
            console.log(err);
            console.error(errMsg);
        }
	});
  } else {
      console.error(err);
  }
});
