var exec = require("cordova/exec");

function AndroidFCMPlugin() {
  if (cordova.platformId === "android") {
    console.log("AndroidFCMPlugin.js is created");
  }
}

AndroidFCMPlugin.prototype.updateToken = function () {
  if (cordova.platformId === "android") {
    exec(null, null, "AndroidFcmPlugin", "updateToken", []);
  }
};

AndroidFCMPlugin.prototype.getToken = function (success, error) {
  if (cordova.platformId === "android") {
    exec(success, error, "AndroidFcmPlugin", "getToken", []);
  }
};

var androidFCMPlugin = new AndroidFCMPlugin();

if (cordova.platformId === "android") {
    exec(function() {
        console.log("AndroidFCMPlugin Ready OK")
    }, function() {
        console.log("AndroidFCMPlugin Ready ERROR")
    }, "AndroidFcmPlugin", "ready", []);
  }

module.exports = androidFCMPlugin;
