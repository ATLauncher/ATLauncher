/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.managers;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.data.version.MinecraftVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.utils.FileUtils;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.util.List;

public class MinecraftVersionManager {
    public static void loadMinecraftVersions() {
        LogManager.debug("Loading Minecraft versions");

        if (Files.exists(FileSystem.CONFIGS.resolve("Versions"))) {
            FileUtils.deleteDirectory(FileSystem.CONFIGS.resolve("Versions"));
        }

        Data.MINECRAFT_VERSIONS.clear();

        try {
            java.lang.reflect.Type type = new TypeToken<List<MinecraftVersion>>() {
            }.getType();
            List<MinecraftVersion> versions = JsonFile.of("minecraftversions.json", type);

            for (MinecraftVersion version : versions) {
                Data.MINECRAFT_VERSIONS.put(version.getVersion(), version);
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading Minecraft versions");
    }

    public static MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        if (Data.MINECRAFT_VERSIONS.containsKey(version)) {
            return Data.MINECRAFT_VERSIONS.get(version);
        }

        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }
}
