Changelog
====================================

### 3.2.2.2

- Fix create server button being available when it shouldn't
- Log which pack/version is being launched when a user launches an instance
- When a download fails and a progress bar is shown, add those bytes done to the total so not to overflow the progress
- Fix NPE when a user has no skin on their account
- Let downloads use non user selectable servers and set libraries to download from multiple servers if it fails
- When clearing tried servers, set the users download server back to original
- Change the way filesizes for mod downloads are retrieved
- Fix issue with themes with invalid font's causing halts
- Change themes to use zip files for more control. Existing themes will no longer work
- New logging system, logs are now stored in baseDir/Logs/ with an option to only keep X days worth (defaults to 7)
- Add in Log Clearer tool to remove all logs
- Enter button now edits/adds an account in the Account tab
- Add in build versioning for beta testing