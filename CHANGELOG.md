# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.14.0

### New Features
- Add in setting for Default Export Format
- When a mod has distribution outside CurseForge disabled, work around the url being null by:
  - Looking up the mod on Modrinth to get a download url
  - If that fails get the user to download the mod via their browser

### Fixes
- Issue when adding mod from Modrinth to a Quilt loader instance not showing Fabric versions
- Errors logging to console when checking mods on Modrinth via hash

### Misc
