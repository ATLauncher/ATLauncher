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
- Switch to Mojang's official AuthLib for authentication
- Don't delete symlinks
- Fix issues with dev versions with updates not being able to play until updated
- You can now ignore dev version updates as you can non dev versions
- When adding mods to a pack via the Edit Mods button, the default type is now Mods Folder
- Add in Share Codes to share a pack's optional mod configuration with others. Get it from right clicking an instances image and apply it to a instance when on the optional mods selection screen
- Disable the update item in the instances right click menu if there is no update
- Fixed network checker