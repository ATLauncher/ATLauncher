#!/usr/bin/env bash
set -euo pipefail

# ============================================================================
#  SECURITY WARNING
#  Piping a script from the internet straight into a shell ("curl ... | bash")
#  runs arbitrary code on your machine. Only do this if you trust the source.
#  To review it before running instead of piping:
#      curl -fsSL <url> -o web-install.sh
#      less web-install.sh        # read it first
#      bash web-install.sh        # then run it
# ============================================================================
#
#  One-command installer for the offline ATLauncher fork. Downloads the prebuilt
#  jar from the latest GitHub release plus a small launcher wrapper and an
#  application-menu entry, installing everything under ~/.local (no root).
#  Requires only: curl and a Java runtime.

REPO="${ATLAUNCHER_OFFLINE_REPO:-tukpot/ATLauncher-offline}"
BRANCH="${ATLAUNCHER_OFFLINE_BRANCH:-master}"
RAW="https://raw.githubusercontent.com/$REPO/$BRANCH/packaging/linux"
JAR_URL="https://github.com/$REPO/releases/latest/download/ATLauncher.jar"

require() {
    command -v "$1" >/dev/null 2>&1 || { echo "Error: '$1' is required but was not found.$2" >&2; exit 1; }
}
require curl ""
require java " Install a Java runtime (JRE 17 recommended), e.g. on Arch: 'sudo pacman -S jre-openjdk'."

DATA_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/atlauncher-offline"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/applications"
ICON_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/scalable/apps"
mkdir -p "$DATA_DIR" "$BIN_DIR" "$DESKTOP_DIR" "$ICON_DIR"

echo "Downloading ATLauncher Offline (latest release) ..."
curl -fsSL "$JAR_URL"                            -o "$DATA_DIR/ATLauncher.jar"
curl -fsSL "$RAW/offline/atlauncher-offline"         -o "$BIN_DIR/atlauncher-offline"
curl -fsSL "$RAW/offline/atlauncher-offline.desktop" -o "$DESKTOP_DIR/atlauncher-offline.desktop"
curl -fsSL "$RAW/_common/atlauncher.svg"             -o "$ICON_DIR/atlauncher-offline.svg"
chmod +x "$BIN_DIR/atlauncher-offline"

# Use absolute paths in the installed .desktop so the menu entry works regardless
# of the desktop session's PATH (~/.local/bin is often not on the GUI session PATH,
# e.g. under KDE/Plasma) and shows the icon without relying on the theme cache.
sed -i "s|^Exec=.*|Exec=$BIN_DIR/atlauncher-offline|; s|^Icon=.*|Icon=$ICON_DIR/atlauncher-offline.svg|" \
    "$DESKTOP_DIR/atlauncher-offline.desktop"

# Refresh desktop/icon caches when the tools are present (ignored if not).
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "$DESKTOP_DIR" >/dev/null 2>&1 || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && \
    gtk-update-icon-cache -f -t "${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor" >/dev/null 2>&1 || true
{ command -v kbuildsycoca6 >/dev/null 2>&1 && kbuildsycoca6 >/dev/null 2>&1; } || \
    { command -v kbuildsycoca5 >/dev/null 2>&1 && kbuildsycoca5 >/dev/null 2>&1; } || true

echo "Installed ATLauncher Offline."
echo "Launch it from your application menu (\"ATLauncher Offline\") or run: atlauncher-offline"
case ":$PATH:" in
    *":$BIN_DIR:"*) : ;;
    *) echo "NOTE: $BIN_DIR is not on your PATH; add it to use the 'atlauncher-offline' command." ;;
esac
