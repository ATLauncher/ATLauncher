# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.20.0

### New Features
- Add support for ARM devices [#576]
  - This is entirely beta at this point and may or may not work. Please report any issues in our [Discord](https://atl.pw/discord)
- Cache Minecraft version manifests

### Fixes
- Clarify the error popup after launch when needing Java 16 on Java 8 Minecraft
- Change text when installing loader to indicate it may take some time
- Make some dialog windows resize as needed to fit the content
- Disable Discord integration for Arm devices
- Setup dialog showing languages not available
- Launch in debug mode tool panel not working
- Updating skins not checking for valid access token first
- Remove forcing of IPv4
- Use CDN to get Minecraft version information [#631]
- Remove some unecessary caching on HTTP requests

### Misc
- Change translations using loader/mod platforms to use placeholders
- Add more context to some strings
