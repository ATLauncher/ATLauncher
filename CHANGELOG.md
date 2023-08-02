# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.31.0

### New Features
- Add dialog when exporting instances for Modrinth showing the override files [#785]
- Support installing servers from CurseForge packs
- Support searching CurseForge for NeoForge mods
- Convert packs from one ATLauncher pack to another, used for migration in ATLauncher backend
- Use device code login method for Microsoft authentication [#755]

### Fixes
- Exporting an instance not working if the directory doesn't exist
- Don't allow NeoForge instances to be exported (no platforms support modpacks yet)
- Issue with servers copy/pasted in the servers folder not being linked up to the right folder
- File not being shown correctly when export is complete
- Importing MultiMC zips not importing settings [#792]
- Issue with saveMods throwing NPE
- Log headers of request when 429 response is returned from a download [#771]

### Misc
