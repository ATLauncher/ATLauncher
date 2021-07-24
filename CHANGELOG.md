# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.5.0

### New Features
- Add in java-version command to LaunchServer scripts
- Add in cancel button to export dialog
- Add in cancel button to instance settings dialog
- Add support for importing and exporting packs in Modrinth format [#491]
- Add ability to disable Discord prescense per instance [#485]
- Add logging of CPU name and JVM args on startup
- Add check on boot for _JAVA_OPTIONS and provide a link to remove it
- Add support for pre and post launch commands (global or per instance) (thanks to @PORTB) [#489]
- Split instance settings up into tabs

### Fixes
- Errors when trying to install Forge for 1.17
- Removing loader from an instance making it non launchable
- The add loader buttons on edit instance showing when they shouldn't
- Issue with modpacks downloading mods and not checking their hashes correctly
- Imported modpacks not being able to be customised [#490]
- Remove memory arguments from launch4j to hopefully fix some OOM errors

### Misc
