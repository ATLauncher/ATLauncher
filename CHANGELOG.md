# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.39.0

### New Features
- Add AboutTab [#568]
- Add version option for CLI [#915]
- Removed Discord RPC
- Mojang Account support completely removed [#907]
- Dedicated GPU support for Linux [#986]
- Add option to open instance.json file for an instance
- Add checking of Java install location setting before saving
- Add checking of custom downloads path setting before saving
- Add image to login with Microsoft account rather than just text
- Add option to specify a custom backups path
- Added Tokyonight theme
- Censor IP addresses from Minecraft in launcher logs [#964]
- Add Paper to loader list for creating plugin capable servers [#975]
- Add option to install/manage plugins and mods to servers [#975][#421]
- Add Purpur support for servers
- Dynamic fetching of JRE information from nodecdn [#958]
- Add context menu on instance image to create support pack for support [#994]

### Fixes
- Invalid Java install location causing issues starting the launcher
- Fix invalid custom downloads path not being validated on boot
- Use a scrollpane for the Java Parameter fields [#963]
- The Java Parameter fields no longer accept new line characters [#666]
- Issue with NeoForge reinstalls having disabled fields [#955]
- Issue with the name field missing when reinstalling an instance to a different Minecraft version
- LegacyJavaFixer not being installed on 1.7.2 Forge
- Remember which settings tab was selected when switching to other tabs and back
- More consistent UI between the tabs in the Packs tab
- Clean up some unclosed resources/sockets
- Issue reinstalling/updating instances with null overridePath items
- Issues with tooltip not all showing consistently
- Issues with CurseForge returning bad data getting cached and wrong messages shown to the user

### Misc
- Update gradle wrapper version from 8.2 to 8.12
- Update the `application.yml` GitHub workflow [#889]
- Refactor News stack
- Implement HierarchyPanel in AccountsTab [#838]
- Implement HierarchyPanel in ServersTab [#839]
- Implement HierarchyPanel in ToolsTab [#840]
- Implement HierarchyPanel to CreatePackTab [#816]
- Update versions of Java tested in GitHub workflows
- Recreate UI on re-localization [#912]
- Squash a ton of warnings [#918]
- Implement architecture for all settings tabs [#910]
- Auto update bundled JRE on boot if already installed
- Remove option to not install Java when using the setup as it's completely local
- Don't fail the setup if the JRE fails to download
- Move scan mods on launch setting to Mods tab
- Remove some old config no longer used
- Cleanup dead unused code
- Add errorprone to check for bad code on compilation
- Update compilation of the application to use JDK 17 (still Java 8 compat)
- Move to a simpler NetworkClient for simple json requests rather than use Download