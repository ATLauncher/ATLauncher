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
package com.atlauncher.managers;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.MinecraftVersion;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MinecraftManager {
    /**
     * Loads info about the different Minecraft versions
     */
    public static void loadMinecraftVersions() {
        PerformanceManager.start();
        LogManager.debug("Loading Minecraft versions");

        Data.MINECRAFT.clear();

        try {
            java.lang.reflect.Type type = new TypeToken<List<MinecraftVersion>>() {
            }.getType();
            List<MinecraftVersion> list = Gsons.DEFAULT_ALT
                    .fromJson(new FileReader(FileSystem.JSON.resolve("minecraft.json").toFile()), type);

            if (list == null) {
                LogManager.error("Error loading Minecraft Versions. List was null. Exiting!");
                System.exit(1); // Cannot recover from this so exit
            }

            for (MinecraftVersion mv : list) {
                Data.MINECRAFT.put(mv.version, mv);
            }
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }
        LogManager.debug("Finished loading Minecraft versions");
        PerformanceManager.end();
    }

    public static boolean isMinecraftVersion(String version) {
        return Data.MINECRAFT.containsKey(version);
    }

    public static MinecraftVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        if (Data.MINECRAFT.containsKey(version)) {
            return Data.MINECRAFT.get(version);
        }
        throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
    }

    public static List<MinecraftVersion> getMajorMinecraftVersions(String version) throws InvalidMinecraftVersion {
        MinecraftVersion parentVersion = getMinecraftVersion(version);

        // this doesn't apply for snapshots
        if (parentVersion.snapshot) {
            List<MinecraftVersion> singleList = new ArrayList<>();
            singleList.add(parentVersion);
            return singleList;
        }

        return Data.MINECRAFT.entrySet().stream()
                .filter(e -> e.getKey().startsWith(version.substring(0, version.lastIndexOf("."))))
                .map(e -> e.getValue()).collect(Collectors.toList());
    }
}
