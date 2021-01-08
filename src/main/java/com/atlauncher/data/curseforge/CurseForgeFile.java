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
package com.atlauncher.data.curseforge;

import java.util.List;

import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;

public class CurseForgeFile {
    public int id;
    public String displayName;
    public String fileName;
    public String fileDate;
    public int fileLength;
    public int releaseType;
    public int fileStatus;
    public String downloadUrl;
    public boolean isAlternate;
    public int alternateFileId;
    public List<CurseForgeFileDependency> dependencies;
    public boolean isAvailable;
    public List<CurseForgeFileModule> modules;
    public long packageFingerprint;
    public List<String> gameVersion;
    public String gameVersionDateReleased;
    public String installMetadata; // unsure of the type of this one, as no public example
    public int serverPackFileId;
    public boolean hasInstallScript;

    public String toString() {
        String releaseTypeString = this.releaseType == 1 ? "" : this.releaseType == 2 ? " (Beta)" : " (Alpha)";
        return this.displayName + releaseTypeString;
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
        mod.website = curseForgeProject.websiteUrl;
        mod.curseForgeProject = curseForgeProject;
        mod.curseForgeFile = this;

        return mod;
    }
}
