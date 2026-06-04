# Installing the offline ATLauncher fork on Linux

Gives you a clean, ready-to-use install: an **"ATLauncher Offline"** entry in your
application menu and an `atlauncher-offline` command, with everything kept tidy in
its own folder (`~/.local/share/atlauncher-offline/`). No root required. Java is
required — the installer tells you if it's missing.

## Quick install (one command)

> ⚠️ **Heads up:** this pipes a script from the internet straight into your shell,
> which runs code on your machine. Only do it if you trust the source. Prefer to
> read it first? Open [`web-install.sh`](web-install.sh) and run it manually.

```bash
curl -fsSL https://raw.githubusercontent.com/tukpot/ATLauncher-offline/master/packaging/linux/offline/web-install.sh | bash
```

This downloads the prebuilt jar from the latest release plus the menu entry, and
installs them under `~/.local`. Then launch **"ATLauncher Offline"** from your app
menu, or run `atlauncher-offline`.

## From a cloned repo

If you already have the repo checked out (builds the jar from source):

```bash
packaging/linux/offline/install.sh
```

## Uninstall

```bash
packaging/linux/offline/uninstall.sh           # keeps your accounts and instances
packaging/linux/offline/uninstall.sh --purge   # also removes saved data
```
