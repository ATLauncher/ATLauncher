# Running the offline ATLauncher fork on Linux

This launcher is a Java application, not a native binary, so a Linux user needs a
**JRE installed** (Java 11+, 17 recommended). On Arch: `sudo pacman -S jre-openjdk`.

Double-clicking the `.jar` is unreliable on Linux (most file managers open it as an
archive). The files here give you a proper app-menu entry instead, and make sure the
launcher always runs from its **own folder** (it creates `Configs/`, `instances/` and
`offlineaccounts.json` next to itself — running it from a shared folder triggers the
upstream "wrong install location" warning).

## Quick install (user-level, no root)

```bash
# from the repo root
packaging/linux/offline/install.sh
```

This builds the jar (`./gradlew shadowJar`) and installs:

| Item | Location |
|------|----------|
| jar | `~/.local/share/atlauncher-offline/ATLauncher.jar` |
| launcher command | `~/.local/bin/atlauncher-offline` |
| app-menu entry | `~/.local/share/applications/atlauncher-offline.desktop` |
| icon | `~/.local/share/icons/hicolor/scalable/apps/atlauncher-offline.svg` |

Then launch **"ATLauncher Offline"** from your application menu, or run
`atlauncher-offline` in a terminal (ensure `~/.local/bin` is on your `PATH`).

Already have a built jar? Skip the build:

```bash
packaging/linux/offline/install.sh build/libs/ATLauncher-<version>.jar
```

## Uninstall

```bash
packaging/linux/offline/uninstall.sh           # keeps your accounts/instances
packaging/linux/offline/uninstall.sh --purge   # also deletes the data directory
```

## Run without installing

```bash
./gradlew shadowJar
mkdir -p ~/.local/share/atlauncher-offline
cp build/libs/ATLauncher-*.jar ~/.local/share/atlauncher-offline/ATLauncher.jar
cd ~/.local/share/atlauncher-offline
java -jar ATLauncher.jar --no-launcher-update
```

`--no-launcher-update` is recommended for this fork so it doesn't try to self-update to
upstream ATLauncher.

## Files

- `atlauncher-offline` — wrapper script: finds Java, runs the jar from its own data dir.
- `atlauncher-offline.desktop` — application-menu entry.
- `install.sh` / `uninstall.sh` — user-level install/uninstall under `~/.local`.
