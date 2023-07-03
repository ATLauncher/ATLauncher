# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.29.0

### New Features
- Scan mods for malware (specifically Fractureiser for now) before launching an instance

### Fixes
- Make Console sizing a bit more responsive and flexible with sizes
- Issue with mod files with invalid characters causing Edit Mods screen to not open
- Issue with loading in some dates from JSON
- Issue with opening instance settings on some systems
- Make sure accounts valid before loading in to fix NPE
- Issues with unicode characters breaking instances and not showing properly
- Issue with threads trying to write to a non thread safe list causing install issues
- Creating vanilla packs or importing not checking for account first
- Issue with collapsing things without an account causing an NPE [#770]
- Issue with importing from MultiMC causing issues with post/pre launch commands/wrapper [#765]

### Misc
- Consolodate GSON configs
- Implement view model for VanillaPacksTab [#743]
- Switch analytics from Google Analytics to own platform [#775]