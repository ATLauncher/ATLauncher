# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.3.1

### New Features
- Add in Download Clearer tool to remove up old downloads
- Add in text field with login url on Microsoft login dialog [#453]

### Fixes
- The check for isRuntime in JavaInfo throwing NPE
- Fix using native dialog not applying to instance import [#446]
- Allow resizing (and fix sizes of) some dialogs [#452]
- Fix cancelling login with Microsoft reloading the account selector

### Misc
- Regenerate known shared/broken analytics client ids
- Add svg icon for AUR packages and remove hardcoded path [#448]
