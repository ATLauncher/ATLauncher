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
package com.atlauncher.data.modrinth.pack;

import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;

import java.util.List;
import java.util.Map;

public class ModrinthModpackFile {
    public String path;
    public Map<String, String> hashes;
    public Map<String, String> env;
    public List<String> downloads;
    public Long fileSize = null;

    public ModType getType() {
        return ModType.mods;
    }

    public Mod convertToMod(boolean isServer) {
        Mod mod = new Mod();

        String clientEnv = "required";
        String serverEnv = "required";

        if (env != null) {
            clientEnv = env.containsKey("client") ? env.get("client") : "required";
            serverEnv = env.containsKey("server") ? env.get("server") : "required";
        }

        mod.client = !clientEnv.equals("unsupported");
        mod.server = !serverEnv.equals("unsupported");
        mod.download = DownloadType.direct;
        mod.file = path.substring(path.lastIndexOf("/") + 1);
        mod.path = path.substring(0, path.lastIndexOf("/"));
        mod.sha1 = hashes.get("sha1");
        mod.name = path.replace("mods/", "").replace(".jar", "");
        mod.url = downloads.get(0);
        mod.type = getType();
        mod.version = "";
        mod.optional = isServer ? serverEnv.equals("optional") : clientEnv.equals("optional");

        if (fileSize != null) {
            mod.filesize = Math.toIntExact(fileSize);
        }

        return mod;
    }
}
