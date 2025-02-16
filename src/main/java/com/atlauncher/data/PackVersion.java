/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
import com.atlauncher.data.ftb.FTBPackVersionType;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.modrinth.ModrinthVersion;

public class PackVersion {
    public String version;
    public String hash;
    public VersionManifestVersion minecraftVersion;
    public boolean canUpdate = true;
    public boolean isRecommended = true;
    public boolean isDev = false;
    public boolean hasLoader = false;
    public boolean hasChoosableLoader = false;
    public String loaderType;
    public transient Integer _ftbId = null;
    public transient FTBPackVersionType _ftbType = null;
    public transient CurseForgeFile _curseForgeFile = null;
    public transient ModrinthVersion _modrinthVersion = null;
    public transient boolean _technicRecommended = false;
    public transient boolean _technicLatest = false;

    public String getSafeVersion() {
        return this.version.replaceAll("[^A-Za-z0-9]", "");
    }

    @Override
    public String toString() {
        String versionString = getVersionString();

        if (_technicRecommended) {
            return versionString + " (Recommended)";
        }

        if (_technicLatest) {
            return versionString + " (Latest)";
        }

        if (_ftbType == FTBPackVersionType.BETA) {
            return versionString + " (Beta)";
        }

        if (_ftbType == FTBPackVersionType.ALPHA) {
            return versionString + " (Alpha)";
        }

        if (_curseForgeFile != null && _curseForgeFile.isBetaType()) {
            return versionString + " (Beta)";
        }

        if (_curseForgeFile != null && _curseForgeFile.isAlphaType()) {
            return versionString + " (Alpha)";
        }

        return versionString;
    }

    private String getVersionString() {
        if (this.minecraftVersion == null || (this.minecraftVersion.id.equalsIgnoreCase(this.version)
                && this.minecraftVersion.type != VersionManifestVersionType.SNAPSHOT
                && this.minecraftVersion.type != VersionManifestVersionType.EXPERIMENT)) {
            return this.version;
        }

        if (this.minecraftVersion.id.equalsIgnoreCase(this.version)
                && this.minecraftVersion.type == VersionManifestVersionType.SNAPSHOT) {
            return this.version + " (Snapshot)";
        }

        if (this.minecraftVersion.id.equalsIgnoreCase(this.version)
                && this.minecraftVersion.type == VersionManifestVersionType.EXPERIMENT) {
            return this.version + " (Experiment)";
        }

        if (this.minecraftVersion.type == VersionManifestVersionType.SNAPSHOT) {
            return this.version + " (" + this.minecraftVersion.id + ")" + " (Snapshot)";
        }

        if (this.minecraftVersion.type == VersionManifestVersionType.EXPERIMENT) {
            return this.version + " (" + this.minecraftVersion.id + ")" + " (Experiment)";
        }

        return this.version + " (" + this.minecraftVersion.id + ")";
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
