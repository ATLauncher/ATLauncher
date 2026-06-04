#!/usr/bin/env bash
set -euo pipefail

# User-level installer for the offline ATLauncher fork (no root required).
# Installs a launcher wrapper, a .desktop entry and an icon under ~/.local,
# and places the jar in its own data directory.
#
# Usage:
#   ./install.sh                 # build the jar (./gradlew shadowJar), then install
#   ./install.sh path/to.jar     # install an already-built jar

HERE="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$HERE/../../.." && pwd)"

if ! command -v java >/dev/null 2>&1; then
    echo "Error: Java is required but was not found on your system." >&2
    echo "Please install a Java runtime (JRE 17 recommended) and run this installer again:" >&2
    echo "  Arch:          sudo pacman -S jre-openjdk" >&2
    echo "  Debian/Ubuntu: sudo apt install default-jre" >&2
    echo "  Fedora:        sudo dnf install java-latest-openjdk" >&2
    exit 1
fi

DATA_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/atlauncher-offline"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/applications"
ICON_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/scalable/apps"

JAR_SRC="${1:-}"
if [[ -z "$JAR_SRC" ]]; then
    echo "Building jar with ./gradlew shadowJar ..."
    (cd "$REPO_ROOT" && ./gradlew --quiet shadowJar)
    JAR_SRC="$(ls -t "$REPO_ROOT"/build/libs/ATLauncher-*.jar 2>/dev/null | head -n1 || true)"
fi

if [[ -z "$JAR_SRC" || ! -f "$JAR_SRC" ]]; then
    echo "Jar not found: '${JAR_SRC:-<none>}'. Build it first with './gradlew shadowJar'." >&2
    exit 1
fi

mkdir -p "$DATA_DIR" "$BIN_DIR" "$DESKTOP_DIR" "$ICON_DIR"

install -m644 "$JAR_SRC" "$DATA_DIR/ATLauncher.jar"
install -m755 "$HERE/atlauncher-offline" "$BIN_DIR/atlauncher-offline"
install -m644 "$HERE/atlauncher-offline.desktop" "$DESKTOP_DIR/atlauncher-offline.desktop"
install -m644 "$REPO_ROOT/packaging/linux/_common/atlauncher.svg" "$ICON_DIR/atlauncher-offline.svg"

# Refresh desktop/icon caches when the tools are present (ignored if not).
command -v update-desktop-database >/dev/null 2>&1 && update-desktop-database "$DESKTOP_DIR" >/dev/null 2>&1 || true
command -v gtk-update-icon-cache >/dev/null 2>&1 && \
    gtk-update-icon-cache -f -t "${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor" >/dev/null 2>&1 || true

echo "Installed:"
echo "  jar      -> $DATA_DIR/ATLauncher.jar"
echo "  launcher -> $BIN_DIR/atlauncher-offline"
echo "  desktop  -> $DESKTOP_DIR/atlauncher-offline.desktop"
echo "  icon     -> $ICON_DIR/atlauncher-offline.svg"
echo
echo "Launch it from your app menu (\"ATLauncher Offline\") or run: atlauncher-offline"
case ":$PATH:" in
    *":$BIN_DIR:"*) : ;;
    *) echo "NOTE: $BIN_DIR is not on your PATH; add it to use the 'atlauncher-offline' command." ;;
esac
