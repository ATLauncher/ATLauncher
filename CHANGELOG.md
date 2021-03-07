# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.3.1

### New Features
- Add in Download Clearer tool to remove up old downloads
- Add in text field with login url on Microsoft login dialog [#453]
- When using a choosable loader, show the name of the loader

### Fixes
- The check for isRuntime in JavaInfo throwing NPE
- Fix using native dialog not applying to instance import [#446]
- Allow resizing (and fix sizes of) some dialogs [#452]
- Fix cancelling login with Microsoft reloading the account selector
- Fix exporting Fabric instances not working
- Fix adding mod version showing wrong files for loader [#454]

### Misc
- Regenerate known shared/broken analytics client ids
- Add svg icon for AUR packages and remove hardcoded path [#448]
- Change images/icons and arrange them better for themes
