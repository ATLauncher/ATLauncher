# Changelog

This changelog only contains the changes that are unreleased. For changes for individual releases, please visit the
[releases](https://github.com/ATLauncher/ATLauncher/releases) page on GitHub.

## 3.4.0.0

As a summary of the below changes, ATLauncher now has a new look, including multiple different themes. While the look
and feel is very similar, it's now more rounded, modern and consistent, with extra attention to detail put in to make
sure screens are laid out correctly.

As well as a bunch of bug fixes and improvements under the hood, ATLauncher also now starts up quicker. We've removed a
bunch of bloat which means you can now get playing even quicker.

Also included is now the ability to import and export instances. This now makes it even easier to create a pack on
ATLauncher using the Forge or Fabric base packs, add a bunch of mods using CurseForge integration, then export that
instance you've created and share it around with friends to simply import into their own launcher.

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
- Fix errors creating directories when copying Downloads
- Fix old Minecraft versions sounds not all copying over correctly
- Fix crashes on boot for ARM devices [#390]
- Add in tests to forge/fabric servers for network checker tool
- Add in higher timeouts for network calls
- Add in CPU and GPU logging to console
- Fetch stylesheets and fonts from the correct module, allowing the launcher to run in Java 9 and above [#395]
- Fix issue with instances and renamed folders not launching
- Remove ability to clone V1 instances and prompt user to reinstall
- Add warning when Minecraft crashes on new Java versions [#388]
- Fix servers not launching on Windows due to quoting issue [#399]
- Add in performance debug logging at level 5
- Make startup faster by caching memory tool values [#192]
- Move checking for username changes to account context menu [#192]
- Remove pack images no longer necessary on boot
- Switch to settings json file instead of a properties file
- Add in import button for CurseForge/Twitch export files
- Allow editing non ATLauncher instances descriptions
- Add in Get Help button to instances
- Add configs download hashing
- Add host to ip resolution to Network Checker Tool
