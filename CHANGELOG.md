# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.23.0

### New Features
- Add in new instance title format "Instance Name (Version)" [#663]
- Add in support for Modrinth resource packs
- Show if Modpacks.ch pack version is Alpha/Beta
- Add button when allocating more than 8GB ram to explain more
- Set limits and validation on connection timeout
- Switch to using own hosted version of Java runtimes to support ARM

### Fixes
- Issue with skins not updating after changing them [#671]
- Switch to using id rather than updated time when checking modpacks.ch updates
- Anaytics having wrong category on Add Mods dialog for pagination and search
- Keep section dropdown the same when switching platform in Add Mods dialog
- Updates for instances not showing the button until a restart [#677]
- Issue with instance settings allowing setting of memory java params
- Adding mods from CurseForge not filtering out Quilt files on non Quilt instances
- Auto select latest file when updating mods
- FTB pack links not working [#681]
- Update LaunchServer.bat to work with double quotes in javapath and print more info [#682]
- Issue with Arm based Mac not using Java runtimes [#684]
- Installing mods from CurseForge with EmbeddedLibrary dependencies not showing it

### Misc
- Implement view model for InstancesTab [#675]
- Implement view model for ServersTab [#674]
- Disallow new lines in java arguments [#666]
