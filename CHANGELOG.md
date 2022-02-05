# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.11.0

### New Features
- Add support for browsing and installing modpacks from Modrinth (disabled until modpacks become available)
- Add support for using new CurseForge api (disabled currently)
- Add warnings when changing initial/maximum memory and permgen to potentialy "too high" levels
- When deleting instances, move them to trash/recycle bin if possible [#549]
- Add ability to filter modpacks on ATLauncher/CurseForge/Modrinth by Minecraft version [#541]

### Fixes
- Large memory usage when installing large CurseForge packs due to using murmur hashes [#493]
- Default options on dialogs not being correct
- Hitting check for updates in edit mods screen not showing a loader initially
- Launching a server on Windows from a folder with a space not working [#544]
- Account UUID without dashes not being censored in logs [#546]
- Add some more protection to exceptions when logging out debug network logs
- Change Update Data button to Check For Updates to make it clearer what it does

### Misc
