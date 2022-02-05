# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.11.0

### New Features
- Add support for browsing and installing modpacks from Modrinth (disabled until modpacks become available)
- Add support for using new CurseForge api (disabled currently)
- Add warnings when changing initial/maximum memory and permgen to potentialy "too high" levels
- When deleting instances, move them to trash/recycle bin if possible [#549]

### Fixes
- Large memory usage when installing large CurseForge packs due to using murmur hashes [#493]
- Default options on dialogs not being correct

### Misc
