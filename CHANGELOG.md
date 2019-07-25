# Changelog

## 3.3.0.0

- Completely reworked the instance installer (instances should install faster now)
- Rework the instance.json file which follows same pattern as the Minecraft one
- Rework the way downloads are handled
- Remove the option to download from multiple ATLauncher servers
- Add support for adding resource packs from Curse
- Fix Curse mod file selector dependencies not showing well
- When installing an instance, give focus to name field and select all when clicked
- [#255] Add tool to download and use recommended version of Java to use
- [#350] Add in anonymous analytics to track usage for future decision making
- Add in Sentry error reporting to get information about unexpected client errors
- Fix console not wrapping long lines
- Backups will now backup your saves as well as some other files such as your game options
- Scan certain folders when looking for installed Java versions
- Fix Java warning prompts from showing when they shouldn't
- Cache some HTTP requests
- Remove advanced backup. We're looking into how to update and make this better for the future
- Change the way that languages are loaded, allowing for easier translations going forward
- Add in ability to update Curse mods from edit mods dialog
- Add in Servers button to packs and instances to find servers to play on
- Add ability to drag and drop mods into the edit mods dialog
- Only delete mods that the pack itself installed on reinstall/update
- Add Backups settings tab to allow backing up mods on backup
- Fix authentication using static clientToken causing issues with logins
- Catch errors in discord initialization
- Fix pack codes not working for semi public packs
