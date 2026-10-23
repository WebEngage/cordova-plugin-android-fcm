var exec = require("cordova/exec");
var Constants = require("./WEConstants");

function AndroidFCMPlugin() {}

// Sends the current FCM token to WebEngage (fire and forget).
AndroidFCMPlugin.prototype.updateToken = function () {
  if (cordova.platformId === Constants.PLATFORM_ANDROID) {
    exec(null, null, Constants.PLUGIN, Constants.UPDATE_TOKEN, []);
  }
};

// Reads the current FCM token.
AndroidFCMPlugin.prototype.getToken = function (success, error) {
  if (cordova.platformId === Constants.PLATFORM_ANDROID) {
    exec(success, error, Constants.PLUGIN, Constants.GET_TOKEN, []);
  }
};

var androidFCMPlugin = new AndroidFCMPlugin();

if (cordova.platformId === Constants.PLATFORM_ANDROID) {
  // Handshake with the native side. Only a failure is worth logging: it means the
  // native plugin class is not registered (broken install or build).
  exec(null, function (err) {
    console.error(Constants.ERROR_NATIVE_UNAVAILABLE, err);
  }, Constants.PLUGIN, Constants.READY, []);
}

module.exports = androidFCMPlugin;
