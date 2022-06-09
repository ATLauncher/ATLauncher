# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.18.0

### New Features
- When installing a pack, check for mod metadata from Modrinth
- When scanning for manually added mods, check Modrinth as well
- When drag and dropping mods, check on Modrinth for the file
- When importing mrpack file, check if it exists on Modrinth to sync it up for updates/reinstalls

### Fixes
- Issue with DBus packages causing install issues with some Forge versions [#564]
- Modrinth exports trying to include non existent files
- Packs through Technic without a logo not installing
- Use Modrinths multi hash check endpoint instead of individually
- Remove disk information from logging
- Make no the default option when removing mods from an instance
- Log error when no way to open web browser/file explorer
- Use user downloads folder for Flatpak browser downloads
- Don't prompt to install Fabric API when user already has Quilt Standard Libraries
- When updating/reinstalling mods with metadata from multiple platforms, use the default platform setting
- Adding pack manually through Modrinth/CurseForge not showing a progress dialog

### Misc
- Remove some unecessary files from the shadow jar
