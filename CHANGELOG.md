# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.37.0

### New Features
- Add AboutTab [#568]
- Add the option to join a minecraft server, world, and realm when launching an instance [#748]
- Add version option for CLI [#915]

### Fixes
- Issue exporting/disabling/deleting worlds downloaded from CurseForge [#927]
- Issue installing Modrinth pack with invalid filename [#923]

### Misc
- Update gradle wrapper version from 8.2 to 8.7 [#886]
- Migrate to the new Gradle version catalogs for libraries and plugins
- Update the `application.yml` GitHub workflow [#889]
- Migrate to Gradle KTS [#898]
- Refactor News stack
- Implement HierarchyPanel in AccountsTab [#838]
- Implement HierarchyPanel in ServersTab [#839]
- Implement HierarchyPanel in ToolsTab [#840]
- Mojang Account support completely removed [#907]
- Update versions of Java tested in GitHub workflows
- Update dependencies
- Recreate UI on re-localization [#912]
- Squash a ton of warnings [#918]
