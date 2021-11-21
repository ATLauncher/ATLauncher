# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.10.0

### New Features
- Add in a new packs browser containing all platforms packs in one place
- Run `-initSettings` with the server jar to generate eula.txt and server.properties [#523]
- Add settings to use system install for GLFW/OpenAL [#524]

### Fixes
- Issue with PermSize option being used on custom Java paths in some cases [#520]
- Wrong Java version warning not working and displaying correctly
- Remove tools and the old getMemory tool
- Inject Lets Encrypt certificate on Java 8 < 141
- Not all Forge libraries using our mirror
- If the Java runtime doesn't exist for a Minecraft version, force refresh them
- Issue with collapsing/expanding instances lagging when there's more than a couple

### Misc
- Clean up Java utility class to clean up references to "Minecraft" Java concept
- Add version and change name of uninstall record for the setup exe
- Update setup Java version to Temurin 8u312b07
