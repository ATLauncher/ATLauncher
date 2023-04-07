# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.28.0

### New Features
- Add ability to override the runtime version per instance
- Add in a unified modpack search to search across multiple platforms at once [#720]
- Add category filtering when adding mods/shaders/worlds/resource packs
- List the timeout and concurrent connection settings when downloads fail
- Remove initial memory argument [#727]
- Add name of platform an instance is from when launching [#740]
- Add in Get Help button to more instances when available [#734]
- Add dialog warning user when trying to skip external download mods from CurseForge
- Add logging for Java version and install method on launch if analytics enabled

### Fixes
- Remove old OmitStackTraceInFastThrow JVM arg
- Don't allow installing non modpacks by ID in pack browser tabs
- Always show Java path in main settings tab and when instance doesn't use a runtime
- Issue with Quilt exported CurseForge packs not importing
- Issue with mod images not being scaled smoothly [#731]
- Non jar/zip files in bin folder being added to classpath on launch [#737]
- Issue with Modrinth packs showing override mods as added by the user [#735]
- Issue with importing some mrpack files
- Slow down the scroll speed on the News tab

### Misc
- Remove ATLauncher Featured tab from Packs tab