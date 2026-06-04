#!/usr/bin/env bash
set -euo pipefail

# Removes the user-level install created by install.sh.
# Leaves the data directory (accounts/instances) intact unless you pass --purge.

DATA_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/atlauncher-offline"
BIN_DIR="$HOME/.local/bin"
DESKTOP_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/applications"
ICON_DIR="${XDG_DATA_HOME:-$HOME/.local/share}/icons/hicolor/scalable/apps"

rm -f "$BIN_DIR/atlauncher-offline" \
      "$DESKTOP_DIR/atlauncher-offline.desktop" \
      "$ICON_DIR/atlauncher-offline.svg"

echo "Removed launcher wrapper, desktop entry and icon."

if [[ "${1:-}" == "--purge" ]]; then
    rm -rf "$DATA_DIR"
    echo "Purged data directory: $DATA_DIR"
else
    echo "Kept your data directory: $DATA_DIR"
    echo "Remove it too with: $0 --purge   (deletes accounts, instances, configs)"
fi
