# Changelog

## 3.3.0.3

- Downloads things not downloading when size is the same
- Don't send errors when creating directories
- Fix NPE with JavaInfo due to GC
- Fix temp directory not clearing on restart
- Fix launching with native libraries in classpath
- Fix logging Minecraft messages causing high cpu/ram and locking
- Fix issue loading instances when there were none
- Fix downloads redownloading without a hash, but size matches
- Turn off mutex in exe
- Fix organising filesystem in linux
- Fix fabric not installing correctly due to bad urls
- [#365] fix duplicate launch arguments being used
- Fix text when no instances are installed
