# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.19.2

### New Features
- Add prompt to restart launcher when changing language

### Fixes
- Issue with NPEs when checking for CurseForge mod updates [#608]
- Add --debug-level option back in
- Noisy logs around images for instance/server cards
- Some incorrect colours on light themes
- Issue with main Forge library not downloading correctly from our CDN
- Allow theme fonts for some languages
- Use base font for tabs on languages that don't support the font used
- Some relocalisation not happening for some text/components
- Load language from settings and earlier in boot process

### Misc
- Remove headless dependency from debian package
- Remove openeye reporting
