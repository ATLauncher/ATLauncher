# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.37.0

### New Features
- Add the option to join a minecraft server, world, and realm when launching an instance [#748]
- Use NeoForge server start jar to allow launching servers using our scripts (partially) [#921]
- Add a readme file when creating servers using our scripts
- Add prompt to update outdated Java [#930]
- Allow installing Fabric mods to Forge and NeoForge instances when Sinytra Connector is installed [#942]

### Fixes
- Issue exporting/disabling/deleting worlds downloaded from CurseForge [#927]
- Issue installing Modrinth pack with invalid filename [#923]
- Issue with launching servers with a space in the name not removing Java path correctly on Windows
- Remove warnings on too much memory being allocated

### Misc
- Speed up CreatePackTab [#933]
- Rename "New Instance" to "Install" [#936]
