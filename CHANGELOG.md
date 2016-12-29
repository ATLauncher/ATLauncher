# Changelog

## 3.2.3.2
- Fix noconfigs option for packs not doing anything
- Fix scrollbars disappearing in large scroll lists
- Switch log uploading to UTF-8 to prevent possible upload issues
- Fix issue with the PacksTab not refreshing after changing account
- Don't download every single Minecraft json file increasing startup speed
- Add --no-launcher-update command line argument to not allow launcher updates
- Add --working-dir command line argument to set the working directory of the launcher
- Add --skip-integration command line argument to not put files in APPDATA or equivelant
- Fix issue with linked/depends mods not cascading their changes to the mods they enable
- Download resources for all versions of Minecraft and download server/client jar differently
- Remove the --skip-minecraft-version-downloads command line argument as it's no longer necessary
- Add in command line argument --skip-hash-checking to disable the checking of hashes when downloading
- Changed the Edit Mods Dialog screen so that the options presented are applicable to the Minecraft version in use
- When a direct download is used for a mod, check the content length from the url to see if it should be re downloaded
- Fixed issue that gave a bad '32 bit Java on 64 bit Windows' warning when 64 bit Java was installed in the x86 directory
- Fixed an issue with cloning an instance where previously changes made to a clone would affect the original