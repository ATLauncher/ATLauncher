# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.35.0

### New Features
- Add api key setting for Modrinth to allow accessing private packs
- Add easy navigation from ServersTab & InstancesTab to PacksTab & CreatePackTab

### Fixes
- Issue with servers button not disabling when cannot create servers on Create Pack tab
- Issue with some CurseForge server packs breaking
- Issues with UI elements sometimes not enabling/disabling correctly on Create Pack tab
- Importing a CurseForge format pack using NeoForge not working [#826]
- Issue installing 1.20.2 versions of NeoForge
- Instance name/description not resetting after a successful install
- Issues with non English regions and number formatting to api's/logs

### Misc
- Convert ConsoleOpenManager and ConsoleCloseManager into ConsoleStateManager [#814]
- Implement HierarchyPanel for basic life cycle to NewsTab
- Remove old nil card image in favour of the default image
- Implement RxJava in AccountManager
- Implement RxJava in ServerManager
- Implement RxJava in InstanceManager [#795]
- Add UUID to Instances [#795]
- Implement RxJava in all update managers [#795]
