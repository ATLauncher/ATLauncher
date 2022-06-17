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

### Misc
- Switch to using Log4j2 for application logging [#533] (thanks @s0cks)
- Add in some missing documentation [#569] (thanks @Doomsdayrs)
- Update dependencies
- Update to Java 17 for the Windows installer [#601]
- Ask user to confirm deleting user data when uninstalling on Windows
