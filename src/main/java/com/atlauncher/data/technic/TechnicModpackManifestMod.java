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
package com.atlauncher.data.technic;

import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;

public class TechnicModpackManifestMod {
    public String name;
    public String version;
    public String md5;
    public String url;
    public Integer filesize;

    public ModType getType() {
        return ModType.mods;
    }

    public Mod convertToMod() {
        Mod mod = new Mod();

        String filename = url.substring(url.lastIndexOf("/") + 1);

        mod.client = true;
        mod.server = true;
        mod.download = DownloadType.direct;
        mod.file = filename;
        mod.path = "mods/";

        if (filesize != null) {
            mod.filesize = filesize;
        }

        if (md5 != null) {
            mod.md5 = md5;
        }

        mod.name = name;
        mod.url = url;
        mod.type = getType();
        mod.version = version;
        mod.optional = false;

        return mod;
    }
}
