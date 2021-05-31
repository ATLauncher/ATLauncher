# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.4.0

### New Features
- Use Java runtimes (when available) for Minecraft [#459]
- Add in new Vanilla pack installer + support for snapshots [#460]
- Allow changing description by double clicking description area
- Add in popup message when launcher has update but they're disabled
- Add setting to networks tab to not validate downloads from modpacks.ch
- Add setting to mods tab to not check mods on CurseForge

### Fixes
- Launcher not working with Java 16 [#465]
- Issues with some CurseForge packs not allowing to be installed
- Rename instance dialog not having a parent window set
- Fix custom vanilla instances deleting the configs folder on reinstall/update [#468]

### Misc
- Force minimum of Java 8u141/7u151
- Get Minecraft versions from Minecraft launcher meta [#460]
- Move to a cleaner and more reusable instance installer method
- Remove user lock which has been broken for a while anyway
