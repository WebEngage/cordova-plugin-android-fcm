var exec = require("cordova/exec");

function AndroidFCMPlugin() {}

// Sends the current FCM token to WebEngage (fire and forget).
AndroidFCMPlugin.prototype.updateToken = function () {
  if (cordova.platformId === "android") {
    exec(null, null, "AndroidFcmPlugin", "updateToken", []);
  }
};

// Reads the current FCM token.
AndroidFCMPlugin.prototype.getToken = function (success, error) {
  if (cordova.platformId === "android") {
    exec(success, error, "AndroidFcmPlugin", "getToken", []);
  }
};

var androidFCMPlugin = new AndroidFCMPlugin();

if (cordova.platformId === "android") {
  // Handshake with the native side. Only a failure is worth logging: it means the
  // native plugin class is not registered (broken install or build).
  exec(null, function (err) {
    console.error("AndroidFCMPlugin: native plugin is not available", err);
  }, "AndroidFcmPlugin", "ready", []);
}

module.exports = androidFCMPlugin;
