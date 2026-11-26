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

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.joda.time.format.ISODateTimeFormat;

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import okhttp3.Request;
import okhttp3.Response;

public class MinecraftManager {
    private static Map<String, VersionManifestVersion> mojangVersions;

    /**
     * Uses Mojang's live index so a revised definition is visible even before the launcher CDN updates.
     */
    public static synchronized VersionManifestVersion getMojangVersion(String id) throws IOException {
        if (mojangVersions == null) {
            VersionManifest manifest = Gsons.DEFAULT.fromJson(
                new String(fetchMojangMetadata(Constants.MINECRAFT_VERSION_MANIFEST_URL), StandardCharsets.UTF_8),
                VersionManifest.class);
            if (manifest == null || manifest.versions == null || manifest.versions.isEmpty()) {
                throw new IOException("Missing versions in Mojang's version manifest");
            }
            mojangVersions = manifest.versions.stream().collect(Collectors.toMap(entry -> entry.id, entry -> entry));
        }
        return mojangVersions.get(id);
    }

    public static JsonObject getMojangVersionDefinition(VersionManifestVersion version) throws IOException {
        JsonObject cached = getCachedMojangRevision(version.id, version.sha1);
        if (cached != null) {
            return cached;
        }
        Path installerCache = FileSystem.MINECRAFT_VERSIONS_JSON.resolve(version.id + ".json");
        byte[] definition = Files.isRegularFile(installerCache) ? Files.readAllBytes(installerCache) : null;
        if (!matchesMojangHash(definition, version.sha1)) {
            definition = fetchMojangMetadata(version.url);
            if (!matchesMojangHash(definition, version.sha1)) {
                throw new IOException("Mojang version JSON checksum mismatch for " + version.id);
            }
        }
        JsonObject json = parseMojangVersion(version.id, definition);
        saveMojangRevision(version.sha1, definition);
        return json;
    }

    private static JsonObject getCachedMojangRevision(String id, String sha1) throws IOException {
        if (sha1 == null) {
            return null;
        }
        Path path = revisionPath(sha1);
        byte[] definition = Files.isRegularFile(path) ? Files.readAllBytes(path) : null;
        return matchesMojangHash(definition, sha1) ? parseMojangVersion(id, definition) : null;
    }

    public static JsonObject getPreviousMojangDefinition(String id, String sha1, String time) throws IOException {
        JsonObject revision = getCachedMojangRevision(id, sha1);
        if (revision != null) {
            return revision;
        }
        Path path = FileSystem.MINECRAFT_VERSIONS_JSON.resolve(id + ".json");
        if (!Files.isRegularFile(path)) {
            return null;
        }
        byte[] definition = Files.readAllBytes(path);
        if (sha1 != null) {
            return matchesMojangHash(definition, sha1) ? parseMojangVersion(id, definition) : null;
        }
        JsonObject json = parseMojangVersion(id, definition);
        return time != null && json.has("time") && Objects.equals(time, json.get("time").getAsString()) ? json : null;
    }

    public static String rememberInstalledMojangRevision(Path definitionPath, String sha1) throws IOException {
        byte[] definition = Files.readAllBytes(definitionPath);
        if (!matchesMojangHash(definition, sha1)) {
            return null;
        }
        saveMojangRevision(sha1, definition);
        return sha1;
    }

    private static JsonObject parseMojangVersion(String id, byte[] definition) {
        JsonObject json = Gsons.DEFAULT.fromJson(new String(definition, StandardCharsets.UTF_8), JsonObject.class);
        if (json == null || !json.has("id") || !id.equals(json.get("id").getAsString())
            || !json.has("downloads") || !json.has("assetIndex")) {
            throw new JsonParseException("Invalid Mojang version JSON for " + id);
        }
        return json;
    }

    private static Path revisionPath(String sha1) throws IOException {
        if (sha1 == null || !sha1.matches("[a-f0-9]{40}")) {
            throw new IOException("Invalid Minecraft version SHA-1");
        }
        return FileSystem.MINECRAFT_VERSION_REVISIONS.resolve(sha1 + ".json");
    }

    private static void saveMojangRevision(String sha1, byte[] definition) throws IOException {
        Path destination = revisionPath(sha1);
        Path temporary = Files.createTempFile(FileSystem.MINECRAFT_VERSION_REVISIONS, "version-", ".tmp");
        try {
            Files.write(temporary, definition);
            Files.move(temporary, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static byte[] fetchMojangMetadata(String url) throws IOException {
        try (Response response = Network.CLIENT.newCall(new Request.Builder().url(url).build()).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                throw new IOException("HTTP " + response.code() + " fetching " + url);
            }
            return response.body().bytes();
        }
    }

    private static boolean matchesMojangHash(byte[] definition, String sha1) {
        return definition != null && com.google.common.hash.Hashing.sha1().hashBytes(definition).toString().equals(sha1);
    }

    /**
     * Loads info about the different Minecraft versions
     */
    public static void loadMinecraftVersions() {
        PerformanceManager.start();
        LogManager.debug("Loading Minecraft versions");

        Data.MINECRAFT.clear();

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(FileSystem.JSON.resolve("minecraft_versions.json")),
                StandardCharsets.UTF_8)) {
            VersionManifest versionManifest = Gsons.DEFAULT.fromJson(fileReader, VersionManifest.class);

            if (versionManifest != null) {
                versionManifest.versions.forEach((version) -> Data.MINECRAFT.put(version.id, version));
            }
        } catch (JsonSyntaxException | IOException | JsonIOException e) {
            LogManager.logStackTrace(e);
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

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(FileSystem.JSON.resolve("java_runtimes.json")),
                StandardCharsets.UTF_8)) {
            Data.JAVA_RUNTIMES = Gsons.DEFAULT.fromJson(fileReader, JavaRuntimes.class);
        } catch (JsonSyntaxException | IOException | JsonIOException e) {
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

        String versionFamily = version.contains(".") ? version.substring(0, version.lastIndexOf(".")) : version;
        String versionPrefix = version.split("\\.").length == 2 ? version : versionFamily;

        return Data.MINECRAFT.entrySet().stream()
                .filter(e -> {
                    if (e.getValue().type != VersionManifestVersionType.RELEASE) {
                        return false;
                    }

                    return e.getKey().equals(versionPrefix) || e.getKey().startsWith(versionPrefix + ".");
                })
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(
            List<VersionManifestVersionType> filterTypes) {
        List<String> disabledVersions = new ArrayList<>();

        filterTypes.forEach(ft -> disabledVersions.addAll(ConfigManager.getConfigItem(
                String.format("minecraft.%s.disabledVersions", ft.getValue()), new ArrayList<>())));

        return Data.MINECRAFT.values().stream().filter(mv -> {
            if (disabledVersions.contains(mv.id)) {
                return false;
            }

            return filterTypes.contains(mv.type);
        }).sorted(Comparator.comparingLong((VersionManifestVersion mv) ->
            ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000
        ).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getFilteredMinecraftVersions(VersionManifestVersionType filterType) {
        List<String> disabledVersions = ConfigManager.getConfigItem(
                String.format("minecraft.%s.disabledVersions", filterType.getValue()), new ArrayList<>());

        return Data.MINECRAFT.values().stream().filter(mv -> {
            if (disabledVersions.contains(mv.id)) {
                return false;
            }

            return mv.type == filterType;
        }).sorted(Comparator.comparingLong((VersionManifestVersion mv) ->
            ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000
        ).reversed()).collect(Collectors.toList());
    }

    public static List<VersionManifestVersion> getMinecraftVersions() {
        List<String> disabledVersions = new ArrayList<>();

        for (VersionManifestVersionType vt : VersionManifestVersionType.values()) {
            disabledVersions.addAll(ConfigManager.getConfigItem(
                    String.format("minecraft.%s.disabledVersions", vt.getValue()), new ArrayList<>()));
        }

        return Data.MINECRAFT.values().stream().filter(mv -> !disabledVersions.contains(mv.id))
                .sorted(Comparator.comparingLong((VersionManifestVersion mv) ->
                    ISODateTimeFormat.dateTimeParser().parseDateTime(mv.releaseTime).getMillis() / 1000
                ).reversed()).collect(Collectors.toList());
    }
}
