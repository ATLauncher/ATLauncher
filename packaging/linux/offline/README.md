# Installing the offline ATLauncher fork on Linux

`install.sh` sets up a clean, ready-to-use install for you, so you don't have to
do it by hand. It adds an **"ATLauncher Offline"** entry to your application menu
and an `atlauncher-offline` command, and keeps everything tidy in its own folder
(`~/.local/share/atlauncher-offline/`).

## Install

```bash
packaging/linux/offline/install.sh
```

Then launch **"ATLauncher Offline"** from your application menu, or run
`atlauncher-offline` in a terminal.

Java is required — if it's missing, the installer tells you exactly what to install.

## Uninstall

```bash
packaging/linux/offline/uninstall.sh           # keeps your accounts and instances
packaging/linux/offline/uninstall.sh --purge   # also removes saved data
```
