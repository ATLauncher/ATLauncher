# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.2.1

### New Features

### Fixes
- Fix downloading files causing multiple connections to open
- Fix downloading files without hashes causing multiple connections
- Fix downloading files causing connections to stay open
- Fix downloading from a pool causing write issues when multiple dirs
- Fix launch arguments having some oddities and missing Tricks arg
- Fix instance installer progress being off in some places
- Fix issue with extraArguments being null on instance conversion

### Misc
- Use hash/size for forge loaders from server
- Switch to using own Forge mirror for installer and older libraries
