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
package com.atlauncher.data.curseforge;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.joda.time.format.ISODateTimeFormat;

import com.atlauncher.annot.ExcludeFromGsonSerialization;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.managers.MinecraftManager;
import com.google.gson.annotations.SerializedName;

public class CurseForgeFile {
    public int id;
    public int gameId;
    public boolean isAvailable;
    public String displayName;
    public String fileName;
    public Integer releaseType;
    public int fileStatus;
    public String fileDate;
    public int fileLength;
    @ExcludeFromGsonSerialization
    public String downloadUrl;
    public List<CurseForgeFileDependency> dependencies;
    public int alternateFileId;
    public List<CurseForgeFileModule> modules;
    public boolean isServerPack;
    public Integer serverPackFileId;

    public List<CurseForgeFileHash> hashes = new ArrayList<>();
    @ExcludeFromGsonSerialization
    public int downloadCount;
    public List<CurseForgeSortableGameVersion> sortableGameVersions = new ArrayList<>();

    @SerializedName(value = "gameVersions", alternate = { "gameVersion" })
    public List<String> gameVersions;

    @SerializedName(value = "fileFingerprint", alternate = { "packageFingerprint" })
    public long packageFingerprint;

    @SerializedName(value = "modId", alternate = { "projectId" })
    public int modId;

    public String getDisplayName() {
        if (this.isModPack() && displayName.length() >= 4
                && displayName.substring(displayName.length() - 4).equalsIgnoreCase(".zip")) {
            return displayName.substring(0, displayName.length() - 4);
        }

        return displayName;
    }

    public String toString() {
        if (this.releaseType == null) {
            return displayName;
        }

        String releaseTypeString = this.releaseType == 1 ? "" : this.releaseType == 2 ? " (Beta)" : " (Alpha)";
        return displayName + releaseTypeString;
    }

    private boolean isModPack() {
        if (modules == null) {
            return false;
        }

        return modules.stream().anyMatch(m -> m.name.equals("manifest.json"));
    }

    public boolean isReleaseType() {
        return this.releaseType == 1;
    }

    public boolean isBetaType() {
        return this.releaseType == 2;
    }

    public boolean isAlphaType() {
        return this.releaseType == 3;
    }

    public Mod convertToMod(CurseForgeProject curseForgeProject) {
        Mod mod = new Mod();

        mod.curseForgeFileId = id;
        mod.curseForgeProjectId = curseForgeProject.id;
        mod.client = true;
        mod.description = curseForgeProject.summary;
        mod.download = DownloadType.direct;
        mod.file = fileName;
        mod.filesize = fileLength;
        mod.fingerprint = packageFingerprint;
        mod.name = curseForgeProject.name;
        mod.type = curseForgeProject.getModType();
        mod.url = downloadUrl;
        mod.version = displayName;
        mod.website = curseForgeProject.getWebsiteUrl();
        mod.curseForgeProject = curseForgeProject;
        mod.curseForgeFile = this;

        Optional<CurseForgeFileHash> md5Hash = hashes.stream().filter(CurseForgeFileHash::isMd5)
                .findFirst();
        md5Hash.ifPresent(curseForgeFileHash -> mod.md5 = curseForgeFileHash.value);

        Optional<CurseForgeFileHash> sha1Hash = hashes.stream().filter(CurseForgeFileHash::isSha1)
                .findFirst();
        sha1Hash.ifPresent(curseForgeFileHash -> mod.sha1 = curseForgeFileHash.value);

        return mod;
    }

    public String getGameVersion() {
        // CurseForge api returning no versions for some reason
        if (gameVersions.isEmpty()) {
            return null;
        }

        // only 1 version, so grab that
        if (gameVersions.size() == 1) {
            return gameVersions.get(0);
        }

        // if more than 1, we need to filter out non Minecraft versions (loaders for
        // instance) and then order them by Minecraft versions release date to make sure
        // we use the newest (SkyFactory 4 lists 3 Minecraft versions for some reason)
        Optional<String> minecraftVersion = gameVersions.stream().filter(MinecraftManager::isMinecraftVersion)
                .map(gv -> {
                    try {
                        return MinecraftManager.getMinecraftVersion(gv);
                    } catch (InvalidMinecraftVersion e) {
                        // this should never happen because of the filter
                        return null;
                    }
                }).filter(Objects::nonNull).sorted(Comparator.comparingLong((VersionManifestVersion mv) ->
                ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000
            ).reversed()).map(mv -> mv.id).findFirst();

        // worse case if nothing comes back, just grab the first item
        return minecraftVersion.orElseGet(() -> gameVersions.get(0));
    }
}
