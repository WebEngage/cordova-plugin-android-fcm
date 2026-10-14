var fs = require('fs');
var path = require('path');

var TAG = '[we-cordova-android-fcm]';

// Copies google-services.json from the project root into the Android app module after
// prepare. The Google Services Gradle plugin (enabled through cordova-android's
// GradlePluginGoogleServicesEnabled preference, see plugin.xml) reads it from there.
module.exports = function (context) {
    var projectRoot = (context && context.opts && context.opts.projectRoot) || process.cwd();

    var srcFile = path.join(projectRoot, 'google-services.json');
    var destDir = path.join(projectRoot, 'platforms', 'android', 'app');

    if (!fs.existsSync(srcFile)) {
        console.log(TAG + ' google-services.json not found in the project root; skipping copy.');
        return;
    }

    try {
        fs.mkdirSync(destDir, { recursive: true });
        fs.copyFileSync(srcFile, path.join(destDir, 'google-services.json'));
        console.log(TAG + ' google-services.json copied to platforms/android/app.');
    } catch (err) {
        console.error(TAG + ' could not copy google-services.json: ' + err);
        return;
    }

    // plugin.xml turns the preference on, so it is only false when the app set it to false.
    // Without Google Services applied by someone, Firebase cannot start and push does not work.
    var gradleConfig = path.join(projectRoot, 'platforms', 'android', 'cdv-gradle-config.json');
    try {
        var config = JSON.parse(fs.readFileSync(gradleConfig, 'utf8'));
        if (config.IS_GRADLE_PLUGIN_GOOGLE_SERVICES_ENABLED === false) {
            console.warn(TAG + ' GradlePluginGoogleServicesEnabled is false. google-services.json is only ' +
                'processed if the app applies the Google Services Gradle plugin itself; otherwise Firebase ' +
                'will not start and push will not work.');
        }
    } catch (ignore) {
        // cdv-gradle-config.json is absent on some hosts; the check is advisory only.
    }
};
