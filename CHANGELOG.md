# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.3.6.0

- Switch to using system property for Vanilla tab
- Fix debug logging not logging if request was cached or not correctly
- Add in SetupDialogComplete tracking event
- Fix mods not behaving between MC version changes
- Fix sort box on Add Mods dialog not showing
- Fix updating Curse mod not selecting current version by default
- Remove Minecraft status checking as api no longer returns correct
- Fix share codes not working on InstanceV2 [#384]
- Fix newer 1.8 skins not showing different arms correctly [#379]
- Hide servers and mods buttons on system packs
- Change Network Tool's download file to 100MB
- Add ability to export packs in CurseForge/Twitch format
- Change the theme system to use [FlatLaF](https://github.com/JFormDesigner/FlatLaf)
- Changing theme will not require a launcher restart
- Remove filler account and account switcher when 0 accounts
- Fix bad date formats and add in more options
- Remove checkbox filters on Packs tab
- Make Java Parameters entry larger
- Remove servers button from system instances
- Move Clone button on instance to right click menu
- Remove the fabric api install button after it's installed
- Show current installed version when updating Curse mod
- Fix system tray icon not being removed on shutdown
- Fix debug mode not censoring tokens
- Add in --close-launcher and --no-console command line arguments
- Fix failed installs still showing in instances
