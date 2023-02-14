# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.28.0

### New Features
- Add ability to override the runtime version per instance
- Add in a unified modpack search to search across multiple platforms at once [#720]
- Add category filtering when adding mods/shaders/worlds/resource packs
- List the timeout and concurrent connection settings when downloads fail

### Fixes
- Remove old OmitStackTraceInFastThrow JVM arg
- Don't allow installing non modpacks by ID in pack browser tabs
- Always show Java path in main settings tab and when instance doesn't use a runtime

### Misc
- Add `xrandr` as a dependency for RPM [#691]
- Update packaging scripts
- Implement view model for VanillaPacksTab [#717]
- Convert Gradle Groovy scripts to Kotlin scripts [#724]
