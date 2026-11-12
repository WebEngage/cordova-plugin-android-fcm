var fs = require('fs');
var path = require('path');

var TAG = '[we-cordova-android-fcm]';

// Runs after prepare. Copies google-services.json from the project root into the Android app
// module, where the Google Services Gradle plugin (enabled through cordova-android's
// GradlePluginGoogleServicesEnabled preference, see plugin.xml) reads it.
module.exports = function (context) {
    var projectRoot = (context && context.opts && context.opts.projectRoot) || process.cwd();

    copyGoogleServicesJson(projectRoot);
    // Checked even when the file is not in the project root: an app can copy it with its own
    // <resource-file>, and the preference matters either way.
    warnIfGoogleServicesDisabled(projectRoot);
};

function copyGoogleServicesJson(projectRoot) {
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
    }
}

// plugin.xml turns the preference on, so it is only false when the app set it to false.
// The build still succeeds then, but unless the app applies Google Services itself, Firebase
// cannot start and push does not work, so this warning is the only sign.
function warnIfGoogleServicesDisabled(projectRoot) {
    var gradleConfig = path.join(projectRoot, 'platforms', 'android', 'cdv-gradle-config.json');
    try {
        var config = JSON.parse(fs.readFileSync(gradleConfig, 'utf8'));
        if (config.IS_GRADLE_PLUGIN_GOOGLE_SERVICES_ENABLED === false) {
            console.warn(TAG + ' GradlePluginGoogleServicesEnabled is false in config.xml, so ' +
                'google-services.json is not processed and push will not work. Remove that preference ' +
                'or set it to true, unless the app applies the Google Services Gradle plugin itself.');
        }
    } catch (ignore) {
        // cdv-gradle-config.json is absent on some hosts; the check is advisory only.
    }
}
