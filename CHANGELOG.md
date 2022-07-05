# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.18.3

### New Features

### Fixes
- Old launcher logs not being deleted after 14 days
- Fix issues importing from Flatpak [#605] (thanks @Doomsdayrs)
- Issue importing CurseForge format zips with missing overrides in json
- Fix default instance sort setting labeled incorrectly [#615] (thanks @Doomsdayrs)
- Fix mods logger on launch not actually logging everything that was added
- Fix Fabric dependency showing for mods even when installed on another mod platform
- Fix issue with Quilt mods showing as updates for Fabric instances [#622]
- Don't show Fabric dependency when QSL is installed
- Issue when checking for updates on Modrinth not using correct loaders
- Hashes not verified when adding a mod from CurseForge to an instance [#626]
- Importing mrpack files that don't return a valid project from Modrinth api failing
- Manually added mods not being checked on CF/MR when exporting instances
- Force modrinth.index.json to UTF-8 charset

### Misc
- Switch to using Log4j2 for application logging [#533] (thanks @s0cks)
- Add in some missing documentation [#569] (thanks @Doomsdayrs)
- Update dependencies
- Update to Java 17 for the Windows installer [#601]
- Ask user to confirm deleting user data when uninstalling on Windows
- Implement view model for ToolsTab [#612]
- Implement view model for AccountsTab [#607]
- Implement view model for NewsTab [#611]
