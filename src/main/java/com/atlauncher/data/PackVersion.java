/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.data;

import com.atlauncher.data.curseforge.CurseForgeFile;

public class PackVersion {
    public String version;
    public String hash;
    public MinecraftVersion minecraftVersion;
    public boolean canUpdate = true;
    public boolean isRecommended = true;
    public boolean isDev = false;
    public boolean hasLoader = false;
    public boolean hasChoosableLoader = false;
    public transient Integer _modpacksChId = null;
    public transient CurseForgeFile _curseForgeFile = null;

    public String getSafeVersion() {
        return this.version.replaceAll("[^A-Za-z0-9]", "");
    }

    public String toString() {
        if (this.minecraftVersion == null
                || (this.minecraftVersion.version.equalsIgnoreCase(this.version) && !this.minecraftVersion.snapshot)) {
            return this.version;
        }

        if (this.minecraftVersion.version.equalsIgnoreCase(this.version) && this.minecraftVersion.snapshot) {
            return this.version + " (Snapshot)";
        }

        if (this.minecraftVersion.snapshot) {
            return this.version + " (" + this.minecraftVersion.version + ")" + " (Snapshot)";
        }

        return this.version + " (" + this.minecraftVersion.version + ")";
    }

    public boolean versionMatches(String version) {
        return this.version.equalsIgnoreCase(version);
    }

    public boolean versionMatches(Instance instance) {
        if (instance.isCurseForgePack()) {
            return versionMatches(instance.launcher.curseForgeFile.displayName);
        }

        return versionMatches(instance.launcher.version);
    }

    public boolean hashMatches(String hash) {
        if (this.hash == null || !this.isDev) {
            return false;
        }

        return this.hash.equalsIgnoreCase(hash);
    }

    public boolean hasLoader() {
        return this.hasLoader;
    }

    public boolean hasChoosableLoader() {
        return this.hasChoosableLoader;
    }

}
