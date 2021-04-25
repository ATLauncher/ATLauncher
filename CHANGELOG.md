# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.3.2

### New Features
- Add ability to export instances in different formats [#455]
- Save config folder when backing up an instance [#463]
- Add configurable backup modes [#463]
- Add config to enable manually added mods by default
- Add settings tab for Mods specific things

### Fixes
- Some modpacks.ch packs not installing correctly
- Checking mods on CurseForge sometimes failing
- Loading settings sometimes returning a NPE
- Forge maven changing url
- Don't start up the launcher when running on Java 16 or newer [#465]
- Importing CurseForge instances without a version failing [#467]
- Checking for mod updates showing when it shouldn't [#466]

### Misc
- Remove integration files from being written [#462]
