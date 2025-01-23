# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.38.0

### New Features
- Add AboutTab [#568]
- Add version option for CLI [#915]
- Dynaimc fetching of JRE information from nodecdn [#958]
- Add `useDedicatedGpu' setting to support dedicated GPU's on linux [#676]

### Fixes
- Java parameters field not being scrollable

### Misc
- Update gradle wrapper version from 8.2 to 8.9 [#886]
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
- Implement architecture for all settings tabs [#910]
- Implement HierarchyPanel to CreatePackTab [#816]
