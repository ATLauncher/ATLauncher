# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.2.0

### New Features
- Add in version field when exporting an instance [#419]
- Allow importing packs from url [#425]
- Add MultiMC instance importing [#426]
- Allow setting which account to use in Instance settings
- Add setting to hide pack name and version from Instance card

### Fixes
- Fix exported instances including disabled mods [#420]
- Clean up old instance format [#422]
- Fix some issues with remembering window size and position option [#423]
- Fix instance installation not cancelling on download exceptions
- Remove system info logging on startup
- Move previous/next page buttons on packs to edges
- Remove bottom set of buttons on system pack cards
- Fix add mods dialog not being wide enough
- Fix potential NPE in PerformanceManager
- Fix issue with opening wrong browser in linux [#424]
- Fix long mod names in Add Mod dropdown not showing correctly [#427]
- Fix install issues with older Forge versions
- Fix share code on instance context menu showing when it shouldn't

### Misc
- Tweak building of artifacts
- Make installs from CurseForge quicker
- Use 31st December for showing date format options [#429]
