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
package com.atlauncher.data.minecraft;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

final class MinecraftLibraryUpdates {
    private MinecraftLibraryUpdates() {
    }

    static JsonArray merge(JsonArray installed, JsonArray previous, JsonArray mojang,
        Predicate<String> acceptsNewLibrary, Consumer<String> recordChange) {
        Map<List<?>, JsonObject> oldLibraries = new HashMap<>();
        Set<List<?>> oldCoordinates = new HashSet<>();
        Set<List<?>> sharedOldCoordinates = new HashSet<>();
        Set<List<?>> ambiguous = new HashSet<>();
        if (previous != null) {
            for (JsonElement entry : previous) {
                JsonObject library = entry.getAsJsonObject();
                List<?> key = libraryKey(library);
                if (oldLibraries.putIfAbsent(key, library) != null) {
                    ambiguous.add(key);
                }
                if (!oldCoordinates.add(coordinates(library))) {
                    sharedOldCoordinates.add(coordinates(library));
                }
            }
        }

        Map<List<?>, JsonObject> upstream = new HashMap<>();
        Map<List<?>, JsonObject> upstreamByCoordinates = new HashMap<>();
        Set<List<?>> sharedCoordinates = new HashSet<>();
        for (JsonElement element : mojang) {
            JsonObject library = element.getAsJsonObject();
            List<?> key = libraryKey(library);
            if (upstream.putIfAbsent(key, library) != null) {
                ambiguous.add(key);
            }
            if (upstreamByCoordinates.putIfAbsent(coordinates(library), library) != null) {
                sharedCoordinates.add(coordinates(library));
            }
        }

        // Without a prior definition, the download source is all we have to identify Mojang entries.
        // With a definition, compare each entry independently so a loader's newer version does not
        // mask the old Mojang entry and exact copies of the old download can all be refreshed.
        Map<List<?>, JsonObject> installedMojang = new HashMap<>();
        Set<List<?>> installedCoordinates = new HashSet<>();
        for (JsonElement element : installed) {
            JsonObject library = element.getAsJsonObject();
            installedCoordinates.add(coordinates(library));
            if (previous == null && hasMojangDownloads(library)) {
                List<?> key = libraryKey(library);
                JsonObject existing = installedMojang.putIfAbsent(key, library);
                if (existing != null && (!existing.get("name").equals(library.get("name"))
                    || !Objects.equals(existing.get("downloads"), library.get("downloads")))) {
                    ambiguous.add(key);
                }
            }
        }

        // A matching Mojang download identifies the old and new releases of the artifact.
        // Use those releases to reconcile missing or withdrawn variants in legacy instances.
        Set<List<?>> installedMojangReleases = new HashSet<>();
        Set<List<?>> upstreamMojangReleases = new HashSet<>();
        installedMojang.forEach((key, library) -> {
            JsonObject latest = upstream.get(key);
            if (latest != null && !ambiguous.contains(key) && hasMojangDownloads(latest)) {
                installedMojangReleases.add(artifactRelease(library));
                upstreamMojangReleases.add(artifactRelease(latest));
            }
        });

        JsonArray merged = new JsonArray();
        for (JsonElement element : installed) {
            JsonObject library = element.getAsJsonObject();
            List<?> key = libraryKey(library);
            JsonObject latest = upstream.get(key);
            JsonObject old = oldLibraries.get(key);
            boolean matchesOld = old != null && !ambiguous.contains(key) && old.get("name").equals(library.get("name"))
                && Objects.equals(old.get("downloads"), library.get("downloads"));
            boolean legacyMojangLibrary = previous == null && !ambiguous.contains(key) && hasMojangDownloads(library);
            if (matchesOld && latest == null && !sharedCoordinates.contains(coordinates(library))
                && !sharedOldCoordinates.contains(coordinates(library))) {
                latest = upstreamByCoordinates.get(coordinates(library));
            }
            if (latest == null && !upstreamByCoordinates.containsKey(coordinates(library))
                && (matchesOld || (legacyMojangLibrary && installedMojangReleases.contains(artifactRelease(library))))) {
                recordChange.accept("Removed library " + library.get("name").getAsString());
                continue;
            }
            if (latest != null && !ambiguous.contains(key) && (matchesOld
                || (legacyMojangLibrary && hasMojangDownloads(latest)))) {
                JsonObject before = library;
                library = library.deepCopy();
                for (String field : Arrays.asList("name", "downloads", "rules", "natives", "extract")) {
                    if (latest.has(field)) {
                        library.add(field, latest.get(field).deepCopy());
                    } else {
                        library.remove(field);
                    }
                }
                if (!library.equals(before)) {
                    String oldName = before.get("name").getAsString();
                    String newName = library.get("name").getAsString();
                    if (oldName.equals(newName)) {
                        MinecraftVersionUpdates.describeFieldChanges("", before, library,
                            change -> recordChange.accept(change + " for library " + newName));
                    } else {
                        recordChange.accept("Updated library " + oldName + " to " + newName);
                    }
                }
            }
            merged.add(library);
        }
        for (JsonElement entry : mojang) {
            JsonObject library = entry.getAsJsonObject();
            List<?> key = coordinates(library);
            if (!oldCoordinates.contains(key) && !installedCoordinates.contains(key)
                && (previous != null || (upstreamMojangReleases.contains(artifactRelease(library))
                    && !sharedCoordinates.contains(key) && hasMojangDownloads(library)))
                && acceptsNewLibrary.test(library.get("name").getAsString())) {
                merged.add(entry.deepCopy());
                recordChange.accept("Added library " + library.get("name").getAsString());
            }
        }
        return merged;
    }

    private static List<?> libraryKey(JsonObject library) {
        return Arrays.asList(coordinates(library), library.get("rules"), library.get("natives"), library.get("extract"));
    }

    private static List<?> coordinates(JsonObject library) {
        String name = library.get("name").getAsString();
        return Arrays.asList(Utils.convertMavenIdentifierToNameAndVersion(name).left(), extension(name));
    }

    private static List<?> artifactRelease(JsonObject library) {
        String name = library.get("name").getAsString();
        Pair<String, String> coordinates = Utils.convertMavenIdentifierToNameAndVersion(name);
        String[] parts = coordinates.left().split(":", 3);
        String artifact = parts.length < 2 ? coordinates.left() : parts[0] + ":" + parts[1];
        return Arrays.asList(artifact, coordinates.right(), extension(name));
    }

    private static String extension(String name) {
        int extension = name.indexOf('@');
        return extension == -1 ? "@jar" : name.substring(extension);
    }

    private static boolean hasMojangDownloads(JsonObject library) {
        Downloads downloads = Gsons.DEFAULT.fromJson(library.get("downloads"), Downloads.class);
        if (downloads == null || (downloads.artifact == null
            && (downloads.classifiers == null || downloads.classifiers.isEmpty()))) {
            return false;
        }
        return (downloads.artifact == null || isMojangDownload(downloads.artifact))
            && (downloads.classifiers == null || downloads.classifiers.values().stream()
                .allMatch(MinecraftLibraryUpdates::isMojangDownload));
    }

    private static boolean isMojangDownload(Download download) {
        return download != null && download.path != null && download.sha1 != null
            && (Constants.MINECRAFT_LIBRARIES + download.path).equals(download.url);
    }
}
