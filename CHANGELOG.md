# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.8.0

### New Features
- Add in wrapper command to allow wrapping of the command used to launcher Minecraft [#511] (thanks to @xz-dev)
- Add list of mods and loader info when launching an instance [#510]

### Fixes
- Servers created with Fabric loader >= 0.12.0 not launching [#513]
- Failure to launch Minecraft causing the main frame to not show itself again
- Searching not working when a pack has no description
- Pre/post launch commands not being exported to MultiMC format [#512]

### Misc
- Remove dependency on pack200 [#516]
