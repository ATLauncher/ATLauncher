# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.13.0

### New Features
- Remove ability to add new/relogin to Mojang accounts
- Use image for login to Microsoft button
- Add ability to change sort order for ATLauncher and CurseForge packs
- Add in Name and Featured sort fields for CurseForge [#554]
- Add ability to add a CurseForge/Modrinth pack from the packs browser by id/slug/url [#554]
- Condense the search fields around the application to be inline and simpler
- More advanced server script for Linux/OSX to mimick more closely the Windows one

### Fixes
- Remove Vercel links from being valid from Modrinth imports
- Modrinth packs using dependencies not installing
- Fix some random errors seen popping up
- Issue with Technic packs being removed trying to still check for updates
- Detect Adoptium Java builds with JavaChecker
- New versions of Quilt using hashed library instead of intermediary and not working
- Don't download/use Minecraft runtimes for Arm based machines (except M1)
- aarch64 not being included as a valid ARM arch
- Update OSX app icon [#502]
- Log clearer tool NPE when folder doesn't exist
- Issue with OSX not using runtime when running initServerSettings
- Issue with trying to delete directories that dont exist on server install
- Graphical glitch when changing instance image [#560]
- System tray menu not being themed

### Misc
- Update Discord icon
- Update all dependency versions
- Remove references to old CurseForge api
- Remove old sort order setting for ATLauncher packs
- Update Flatlaf version
- Remove server script for OSX as can share the Linux one
