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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

class MinecraftLibraryUpdatesTest {
    @Test
    void updatesMojangVersionAndNativeWhilePreservingManualChanges() {
        JsonObject old = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.1", "old");
        JsonObject current = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.1-mojang-2.15.0.3", "new");
        JsonObject oldWindows = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.1:natives-windows", "old-windows");
        JsonObject currentWindows = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.1-mojang-2.15.0.3:natives-windows",
            "new-windows");
        JsonObject removedLinux = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.1-mojang-2.15.0.3:natives-linux",
            "new-linux");
        JsonObject custom = library("org.lwjgl:lwjgl-freetype:3.4.2", "https://example.com/", "custom");
        JsonObject unrelated = library("mod.loader:dependency:1.0", "https://example.com/", "loader");
        oldWindows.addProperty("customField", "keep");

        JsonArray installed = libraries(unrelated, old, custom, oldWindows);
        List<String> messages = new ArrayList<>();
        JsonArray updated = MinecraftLibraryUpdates.merge(installed, null, libraries(current, removedLinux, currentWindows),
            name -> false, messages::add);

        currentWindows.addProperty("customField", "keep");
        assertEquals(libraries(unrelated, current, custom, currentWindows), updated);
        assertEquals(libraries(unrelated, old, custom, oldWindows), installed);
        assertEquals(Arrays.asList("Updated library org.lwjgl:lwjgl-freetype:3.4.1 to org.lwjgl:lwjgl-freetype:3.4.1-mojang-2.15.0.3",
            "Updated library org.lwjgl:lwjgl-freetype:3.4.1:natives-windows to "
                + "org.lwjgl:lwjgl-freetype:3.4.1-mojang-2.15.0.3:natives-windows"), messages);
    }

    @Test
    void updatesChecksumEvenWhenVersionAndUrlHaveNotChanged() {
        String name = "com.mojang:example:1.0";
        JsonObject old = mojangLibrary(name, "old");
        JsonObject current = mojangLibrary(name, "new");

        List<String> messages = new ArrayList<>();
        assertEquals(libraries(current), MinecraftLibraryUpdates.merge(libraries(old), null, libraries(current),
            ignored -> false, messages::add));
        assertEquals(Collections.singletonList("Updated downloads.artifact.sha1 for library " + name), messages);
    }

    @Test
    void updatesMatchingCopiesOfMojangLibraryWhileKeepingLoaderVersionsAndOverrides() {
        String name = "com.mojang:example:1.0";
        JsonObject old = mojangLibrary(name, "old");
        JsonObject patched = mojangLibrary(name, "patched");
        JsonObject newer = mojangLibrary("com.mojang:example:1.1", "loader");
        JsonObject override = library(name, "https://example.com/", "custom");
        JsonArray installed = libraries(newer, override, old, old.deepCopy());
        List<String> messages = new ArrayList<>();

        JsonArray updated = MinecraftLibraryUpdates.merge(installed, libraries(old), libraries(patched), ignored -> false,
            messages::add);

        assertEquals(libraries(newer, override, patched, patched), updated);
        assertEquals(Arrays.asList("Updated downloads.artifact.sha1 for library " + name,
            "Updated downloads.artifact.sha1 for library " + name), messages);
    }

    @Test
    void reconcilesMojangNativeVariantsWithoutPreviousDefinition() {
        JsonObject old = mojangLibrary("org.lwjgl:lwjgl-freetype:3.3.3", "old");
        JsonObject current = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.3-mojang-2.15.0", "new");
        JsonObject withdrawn = mojangLibrary("org.lwjgl:lwjgl-freetype:3.3.3:natives-windows-x86", "old-x86");
        JsonObject added = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.3-mojang-2.15.0:natives-macos", "new-macos");
        JsonObject loaderNative = library("org.lwjgl:lwjgl-freetype:3.3.3:natives-macos-patch",
            "https://example.com/", "loader-native");
        JsonObject newerNative = mojangLibrary("org.lwjgl:lwjgl-freetype:3.4.4:natives-windows-x86", "newer-x86");
        JsonObject unrelated = mojangLibrary("example:standalone:1.0", "standalone");
        JsonObject unrelatedUpstream = mojangLibrary("example:other:1.0", "other");
        JsonArray installed = libraries(old, withdrawn, loaderNative, newerNative, unrelated);
        List<String> messages = new ArrayList<>();

        JsonArray updated = MinecraftLibraryUpdates.merge(installed, null, libraries(current, added, unrelatedUpstream),
            name -> true, messages::add);

        assertEquals(libraries(current, withdrawn, loaderNative, newerNative, unrelated, added), updated);
        assertEquals(Arrays.asList("Updated library org.lwjgl:lwjgl-freetype:3.3.3 to "
            + "org.lwjgl:lwjgl-freetype:3.4.3-mojang-2.15.0", "Added library "
            + "org.lwjgl:lwjgl-freetype:3.4.3-mojang-2.15.0:natives-macos"), messages);

        updated = MinecraftLibraryUpdates.merge(libraries(old, withdrawn, loaderNative, unrelated), null,
            libraries(current, added, unrelatedUpstream), name -> true, message -> {});

        assertEquals(libraries(current, loaderNative, unrelated, added), updated);
    }

    @Test
    void updatesIdenticalMojangCopiesWithoutPreviousDefinition() {
        String name = "com.mojang:example:1.0";
        JsonObject old = mojangLibrary(name, "old");
        JsonObject patched = mojangLibrary(name, "patched");

        assertEquals(libraries(patched, patched), MinecraftLibraryUpdates.merge(libraries(old, old.deepCopy()), null,
            libraries(patched), ignored -> false, message -> {}));
    }

    @Test
    void doesNotGuessWhenMultipleInstalledEntriesCouldMatch() {
        JsonArray installed = libraries(mojangLibrary("com.mojang:example:1.0", "old"),
            mojangLibrary("com.mojang:example:1.1", "manual"));
        JsonArray upstream = libraries(mojangLibrary("com.mojang:example:1.2", "new"));

        List<String> messages = new ArrayList<>();
        assertEquals(installed, MinecraftLibraryUpdates.merge(installed, null, upstream, name -> false, messages::add));
        assertTrue(messages.isEmpty());
    }

    private static JsonObject mojangLibrary(String name, String hash) {
        return library(name, Constants.MINECRAFT_LIBRARIES, hash);
    }

    private static JsonObject library(String name, String baseUrl, String hash) {
        Library library = new Library();
        library.name = name;
        library.downloads = new Downloads();
        library.downloads.artifact = new Download();
        library.downloads.artifact.path = Utils.convertMavenIdentifierToPath(name);
        library.downloads.artifact.url = baseUrl + library.downloads.artifact.path;
        library.downloads.artifact.sha1 = hash;
        return Gsons.DEFAULT.toJsonTree(library).getAsJsonObject();
    }

    private static JsonArray libraries(JsonObject... libraries) {
        JsonArray array = new JsonArray();
        for (JsonObject library : libraries) {
            array.add(library);
        }
        return array;
    }
}
