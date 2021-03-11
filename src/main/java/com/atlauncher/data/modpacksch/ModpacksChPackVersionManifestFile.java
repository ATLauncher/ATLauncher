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
package com.atlauncher.data.modpacksch;

import java.util.List;

import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;

public class ModpacksChPackVersionManifestFile {
    public String version;
    public String path;
    public String url;
    public String sha1;
    public int size;
    public List<Object> tags;
    public boolean clientonly;
    public boolean serveronly;
    public boolean optional;
    public int id;
    public String name;
    public ModpacksChPackVersionManifectFileType type;
    public int updated;

    public ModType getType() {
        return ModType.mods;
    }

    public Mod convertToMod() {
        Mod mod = new Mod();

        mod.client = !serveronly;
        mod.server = !clientonly;
        mod.download = DownloadType.direct;
        mod.file = name;
        mod.path = path.substring(0, 2).equalsIgnoreCase("./") ? path.substring(2) : path;
        mod.filesize = size;
        mod.sha1 = sha1;
        mod.name = name;
        mod.url = url;
        mod.type = getType();
        mod.version = version;
        mod.optional = optional;

        return mod;
    }
}
