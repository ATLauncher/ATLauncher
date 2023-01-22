# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.24.2

### New Features

### Fixes
- Default Java icon in macOS Dock when using Java 9 or later [#655] (@WhiteBear60)
- Issue with Instance settings allow an invalid Java path to be saved
- Issue with Quilt based modpacks not exporting to MultiMC format correctly [#710]
- Issue with scanning missing mods throwing errors when folder doesn't exist
- Add some error checking around modpacks.ch to prevent issues when api errors
- Use quilt meta profiles for client/server same as Fabric
- Browsing mods not going back to first page when changing query [#714]

### Misc
- Move missing graphql usage for loader versions outside Vanilla Minecraft tab
- Move pack action api calls to graphql when configured on
