# Changelog

## 3.2.3.2
- Don't download every single Minecraft json file increasing startup speed
- Fix issue with linked/depends mods not cascading their changes to the mods they enable
- Add in command line argument --skip-hash-checking to disable the checking of hashes when downloading
- When a direct download is used for a mod, check the content length from the url to see if it should be re downloaded
- Fixed issue that gave a bad '32 bit Java on 64 bit Windows' warning when 64 bit Java was installed in the x86 directory