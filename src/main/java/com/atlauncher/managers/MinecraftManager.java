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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.network.Download;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import org.joda.time.format.ISODateTimeFormat;

public class MinecraftManager {
    /**
     * Loads info about the different Minecraft versions
     */
    public static void loadMinecraftVersions() {
        PerformanceManager.start();
        LogManager.debug("Loading Minecraft versions");

        Data.MINECRAFT.clear();

        VersionManifest versionManifest = null;
        Path manifestPath = FileSystem.JSON.resolve("version_manifest.json");

        try {
            versionManifest = Download.build().cached().setUrl(Constants.MINECRAFT_VERSION_MANIFEST_URL)
                    .downloadTo(manifestPath).asClassWithThrow(VersionManifest.class);
        } catch (IOException e) {
            LogManager.logStackTrace(e);

            if (Files.exists(manifestPath)) {
                try {
                    versionManifest = Gsons.DEFAULT.fromJson(new FileReader(manifestPath.toFile()),
                            VersionManifest.class);
                } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e1) {
                    LogManager.logStackTrace(e1);
                }
            }
        }

        if (versionManifest != null) {
            versionManifest.versions.forEach((version) -> {
                Data.MINECRAFT.put(version.id, version);
            });
        }

        LogManager.debug("Finished loading Minecraft versions");
        PerformanceManager.end();
    }

    /**
     * Loads info about the java runtimes for Minecraft
     */
    public static void loadJavaRuntimes() {
        PerformanceManager.start();
        LogManager.debug("Loading Java runtimes");

        try {
            Data.JAVA_RUNTIMES = Download.build().cached().setUrl(Constants.MINECRAFT_JAVA_RUNTIME_URL)
                    .asClassWithThrow(JavaRuntimes.class);
        } catch (IOException e) {
            // safe to ignore, we'll just not use it
            LogManager.logStackTrace(e);
        }

        LogManager.debug("Finished loading Java runtimes");
        PerformanceManager.end();
    }

    public static boolean isMinecraftVersion(String version) {
        return Data.MINECRAFT.containsKey(version);
    }

    public static VersionManifestVersion getMinecraftVersion(String version) throws InvalidMinecraftVersion {
        if (!Data.MINECRAFT.containsKey(version)) {
            throw new InvalidMinecraftVersion("No Minecraft version found matching " + version);
        }

        return Data.MINECRAFT.get(version);
    }

    public static List<VersionManifestVersion> getMajorMinecraftVersions(String version)
            throws InvalidMinecraftVersion {
        VersionManifestVersion parentVersion = getMinecraftVersion(version);

        // this doesn't apply for anything other than release types
        if (parentVersion.type != VersionManifestVersionType.RELEASE) {
            List<VersionManifestVersion> singleList = new ArrayList<>();
            singleList.add(parentVersion);
            return singleList;
        }

        return Data.MINECRAFT.entrySet().stream()
                .filter(e -> e.getValue().type == VersionManifestVersionType.RELEASE
                        && e.getKey().startsWith(version.substring(0, version.lastIndexOf("."))))
                .map(e -> e.getValue()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(
            List<VersionManifestVersionType> filterTypes) {
        return Data.MINECRAFT.values().stream().filter(mv -> filterTypes.contains(mv.type))
                .sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
                }).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(VersionManifestVersionType filterType) {
        return Data.MINECRAFT.values().stream().filter(mv -> mv.type == filterType)
                .sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
                }).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getMinecraftVersions() {
        return Data.MINECRAFT.values().stream().sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
        }).reversed()).collect(Collectors.toList());
    }
}
