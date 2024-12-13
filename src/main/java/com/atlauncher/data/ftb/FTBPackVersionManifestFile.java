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
package com.atlauncher.data.ftb;

import java.util.List;

import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;

public class FTBPackVersionManifestFile {
    public String version;
    private String path;
    public String url;
    public List<String> mirrors;
    public String sha1;
    public FTBPackVersionManifestFileHashes hashes;
    public int size;
    public List<Object> tags;
    public boolean clientonly;
    public boolean serveronly;
    public boolean optional;
    public long id;
    public String name;
    public FTBPackVersionManifestFileType type;
    public int updated;
    public FTBPackVersionManifestFileCurseForge curseforge;

    /**
     * This ensures that the path returned always ends with a / so that filenames
     * can be appended safely
     *
     * @return String
     */
    public String getPath() {
        String cleanPath = path.substring(0, 2).equalsIgnoreCase("./") ? path.substring(2)
                : path;
        if (!cleanPath.isEmpty() && !cleanPath.endsWith("/")) {
            cleanPath += "/";
        }

        return cleanPath;
    }

    public ModType getType() {
        return ModType.mods;
    }

    public Mod convertToMod() {
        Mod mod = new Mod();

        mod.client = !serveronly;
        mod.server = !clientonly;
        mod.download = DownloadType.direct;
        mod.file = name;
        mod.path = getPath();
        mod.filesize = size;
        mod.sha1 = sha1 != null ? sha1 : hashes.sha1;
        mod.name = name;
        mod.url = url;
        mod.type = getType();
        mod.version = version;
        mod.optional = optional;

        // FTB have wrong hashes quite often
        mod.ignoreFailures = true;

        return mod;
    }
}
