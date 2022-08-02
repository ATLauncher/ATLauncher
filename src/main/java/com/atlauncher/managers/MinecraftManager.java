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

import org.joda.time.format.ISODateTimeFormat;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.LWJGLVersions;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.network.Download;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class MinecraftManager {
    private static boolean forceReloaded = false;

    public static void loadMinecraftVersions() {
        loadMinecraftVersions(false);
    }

    /**
     * Loads info about the different Minecraft versions
     */
    public static void loadMinecraftVersions(boolean force) {
        PerformanceManager.start();
        LogManager.debug("Loading Minecraft versions");

        Data.MINECRAFT.clear();

        VersionManifest versionManifest = null;
        Path manifestPath = FileSystem.JSON.resolve("version_manifest.json");

        try {
            Download download = Download.build().setUrl(Constants.MINECRAFT_VERSION_MANIFEST_URL)
                    .downloadTo(manifestPath);

            if (!force) {
                download = download.cached();
            }

            versionManifest = download.asClassWithThrow(VersionManifest.class);
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

        loadAdditiveVersions();

        LogManager.debug("Finished loading Minecraft versions");
        PerformanceManager.end();
    }

    private static void loadAdditiveVersions() {
        List<VersionManifestVersion> additiveVersionsManifest = null;
        Path additiveManifestPath = FileSystem.JSON.resolve("additive_versions.json");

        java.lang.reflect.Type type = new TypeToken<List<VersionManifestVersion>>() {
        }.getType();

        if (Files.exists(additiveManifestPath)) {
            try {
                additiveVersionsManifest = Gsons.DEFAULT.fromJson(new FileReader(additiveManifestPath.toFile()), type);
            } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e1) {
                LogManager.logStackTrace(e1);
            }
        }

        if (additiveVersionsManifest != null) {
            additiveVersionsManifest.forEach((version) -> {
                Data.MINECRAFT.put(version.id, version);
            });
        }
    }

    /**
     * Loads info about the different LWJGL versions
     */
    public static void loadLWJGLVersions() {
        PerformanceManager.start();

        Data.LWJGL_VERSIONS = null;

        Path lwjglPath = FileSystem.JSON.resolve("lwjgl.json");

        if (Files.exists(lwjglPath)) {
            try {
                Data.LWJGL_VERSIONS = Gsons.DEFAULT.fromJson(new FileReader(lwjglPath.toFile()), LWJGLVersions.class);
            } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e1) {
                LogManager.logStackTrace(e1);
            }
        }

        PerformanceManager.end();
    }

    public static void loadJavaRuntimes() {
        loadJavaRuntimes(false);
    }

    /**
     * Loads info about the java runtimes for Minecraft
     */
    public static void loadJavaRuntimes(boolean force) {
        PerformanceManager.start();
        LogManager.debug("Loading Java runtimes");

        try {
            Download download = Download.build().setUrl(Constants.MINECRAFT_JAVA_RUNTIME_URL);

            if (!force) {
                download = download.cached();
            }

            Data.JAVA_RUNTIMES = download.asClassWithThrow(JavaRuntimes.class);
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
        if (!Data.MINECRAFT.containsKey(version) && !forceReloaded) {
            forceReloaded = true;
            MinecraftManager.loadMinecraftVersions(true);
        }

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
                .filter(e -> {
                    if (e.getValue().type != VersionManifestVersionType.RELEASE) {
                        return false;
                    }

                    // no patch version (for instance 1.19, 1.18, etc)
                    if (version.split("\\.").length == 2) {
                        return e.getKey().startsWith(version);
                    }

                    return e.getKey().startsWith(version.substring(0, version.lastIndexOf(".")));
                })
                .map(e -> e.getValue()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(
            List<VersionManifestVersionType> filterTypes) {
        List<String> disabledVersions = new ArrayList<>();

        filterTypes.forEach(ft -> {
            disabledVersions.addAll(ConfigManager.getConfigItem(
                    String.format("minecraft.%s.disabledVersions", ft.getValue()), new ArrayList<String>()));
        });

        return Data.MINECRAFT.values().stream().filter(mv -> {
            if (disabledVersions.contains(mv.id)) {
                return false;
            }

            return filterTypes.contains(mv.type);
        }).sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
        }).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(VersionManifestVersionType filterType) {
        List<String> disabledVersions = ConfigManager.getConfigItem(
                String.format("minecraft.%s.disabledVersions", filterType.getValue()), new ArrayList<String>());

        return Data.MINECRAFT.values().stream().filter(mv -> {
            if (disabledVersions.contains(mv.id)) {
                return false;
            }

            return mv.type == filterType;
        }).sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
            return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
        }).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getMinecraftVersions() {
        List<String> disabledVersions = new ArrayList<>();

        for (VersionManifestVersionType vt : VersionManifestVersionType.values()) {
            disabledVersions.addAll(ConfigManager.getConfigItem(
                    String.format("minecraft.%s.disabledVersions", vt.getValue()), new ArrayList<String>()));
        }

        return Data.MINECRAFT.values().stream().filter(mv -> !disabledVersions.contains(mv.id))
                .sorted(Comparator.comparingLong((VersionManifestVersion mv) -> {
                    return ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000;
                }).reversed()).collect(Collectors.toList());
    }
}
