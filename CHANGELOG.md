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

### Fixes
- Remove old OmitStackTraceInFastThrow JVM arg
- Don't allow installing non modpacks by ID in pack browser tabs
- Always show Java path in main settings tab and when instance doesn't use a runtime
- Issue with Quilt exported CurseForge packs not importing
- Issue with mod images not being scaled smoothly [#731]
- Fix UTF-8 support in ATLauncher console [#722]

### Misc
- Add `xrandr` as a dependency for RPM [#691]
- Update packaging scripts
- Implement view model for VanillaPacksTab [#717]
- Convert Gradle Groovy scripts to Kotlin scripts [#724]
