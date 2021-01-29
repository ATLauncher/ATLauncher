# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.3.0

### New Features
- Add support for downloading mods from Modrinth [#440]
- Add delete button to RuntimeDownloaderToolPanel
- Add icons when browsing mods/packs to add
- Add in more options for add mod restrictions [#439]
- Create installer and use optional jre [#443]
- Packs on CurseForge using JumpLoader no longer use Forge [#444]
- Add OS information to Google Analytics
- Use newer universalJavaApplicationStub for OSX app

### Fixes
- Dialog when installing file from CurseForge not showing progress
- Checking for mods on CurseForge running out of memory causing issue
- Reinstalling or Updating CurseForge mods hanging when calling api
- Setup dialog not having correct spacing
- Redact Authorization headers from debug logging
- Close dialog after adding a mod to an instance
- Fix adding mods failing when fingerprinting fails
- When relaunching in debug mode, add the original arguments

### Misc
