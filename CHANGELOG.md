# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.9.0

### New Features
- Don't allow initial memory to exceed maximum memory [#517]
- Log the applications arguments when booting
- Add in a command line option to print help (`--help`)
- Allow the `--launch` parameter to work with an instances name as well as directory name
- Add Technic modpack support [#477]
- Add progress dialog when uploading a paste
- Add current installed version when reinstalling/updating an instance

### Fixes
- When selecting the bin folder of a Java install, correctly parse that for the launcher
- Launcher not loading if cmd working directory doesn't exist [#518]
- Logins not working for Microsoft accounts using game pass
- Installer not working on Forge versions older than 10.x
- Issue with MetaSpace arguments applying incorrectly to custom instance Java versions
- Don't copy resource files if we don't need to on launch/install

### Misc
