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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.LWJGLLibrary;
import com.atlauncher.data.LWJGLMajorVersion;
import com.atlauncher.data.LWJGLVersion;
import com.atlauncher.data.LWJGLVersions;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class LWJGLManager {
    /**
     * Loads info about the different LWJGL versions
     */
    public static void loadLWJGLVersions() {
        PerformanceManager.start();

        Data.LWJGL_VERSIONS = null;

        Path lwjglPath = FileSystem.JSON.resolve("lwjgl.json");

        if (Files.exists(lwjglPath)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                    new FileInputStream(lwjglPath.toFile()), StandardCharsets.UTF_8)) {
                Data.LWJGL_VERSIONS = Gsons.DEFAULT.fromJson(fileReader, LWJGLVersions.class);
            } catch (JsonSyntaxException | IOException | JsonIOException e1) {
                LogManager.logStackTrace(e1);
            }
        }

        PerformanceManager.end();
    }

    public static boolean usesLegacyLWJGL(MinecraftVersion minecraftVersion) {
        return Data.LWJGL_VERSIONS.legacyLwjglVersions.contains(minecraftVersion.id);
    }

    public static LWJGLLibrary getLegacyLWJGLLibrary() {
        Optional<LWJGLMajorVersion> version = Data.LWJGL_VERSIONS.versions.stream().filter(v -> v.version == 2)
                .findFirst();

        if (!version.isPresent() || version.get().versions.isEmpty()) {
            return null;
        }

        LWJGLVersion lwjglVersion = version.get().versions.get(0);

        if (!lwjglVersion.libraries.containsKey("lwjgl")) {
            return null;
        }

        Optional<LWJGLLibrary> library = Optional
                .ofNullable(lwjglVersion.libraries.get("lwjgl").get(OS.getLWJGLClassifier()));

        return library.orElse(null);

    }

    public static Library getReplacementLWJGL3Library(MinecraftVersion minecraftVersion, Library library) {
        if (!library.name.contains("lwjgl") || !library.name.contains(":3")) {
            return library;
        }

        LogManager.debug(String.format("Looking at replacing LWJGL 3 library %s", library.name));

        Optional<LWJGLMajorVersion> version = Data.LWJGL_VERSIONS.versions.stream().filter(v -> v.version == 3)
                .findFirst();

        if (!version.isPresent() || version.get().versions.isEmpty()) {
            LogManager.debug(String.format("Not replacing library %s as major version 3 not found",
                    library.name));
            return library;
        }

        String lwjglStringVersion = library.name.split(":")[2];
        String libraryName = library.name.split(":")[1];

        // use 3.3.1 at a minimum, else match to same version as Minecraft
        String versionToUse = Utils.matchWholeVersion(lwjglStringVersion, "3.3.1", true) ? lwjglStringVersion : "3.3.1";

        Optional<LWJGLVersion> lwjglVersion = version.get().versions.stream()
                .filter(v -> v.version.equals(versionToUse)).findFirst();

        if (!lwjglVersion.isPresent() || !lwjglVersion.get().libraries.containsKey(libraryName)) {
            LogManager.debug(String.format("Not replacing library %s as no version (%s) or library found for %s",
                    library.name, versionToUse, libraryName));
            return library;
        }

        // take a copy of this so we're not modifying the original
        Library replacedLibrary = Gsons.DEFAULT.fromJson(Gsons.DEFAULT.toJson(library), Library.class);

        // 1.19-pre1 and onwards removed natives/classifiers, but we're worried about
        // the base library, no natives library here
        if ((library.natives == null || library.natives.isEmpty()) && !library.name.contains(":natives-")) {
            Optional<LWJGLLibrary> lwjglLibrary = Optional
                    .ofNullable(lwjglVersion.get().libraries.get(libraryName).get("*"));

            if (!lwjglLibrary.isPresent()) {
                LogManager.debug(String.format("Not replacing library %s as couldn't find the library information",
                        library.name));
                return library;
            }

            // if sha1 matches, assume library is good
            if (library.downloads.artifact.sha1.equals(lwjglLibrary.get().sha1)) {
                LogManager.debug(String.format("Not replacing library %s as the file is the same", library.name));
                return library;
            }

            LogManager.debug(String.format("Replacing library %s", library.name));

            // update the artifact download
            replacedLibrary.downloads.artifact.path = lwjglLibrary.get().path;
            replacedLibrary.downloads.artifact.sha1 = lwjglLibrary.get().sha1;
            replacedLibrary.downloads.artifact.url = lwjglLibrary.get().url;
            replacedLibrary.downloads.artifact.size = lwjglLibrary.get().size;

            // match the name up (as version can change)
            replacedLibrary.name = lwjglLibrary.get().name;
        }

        // now worry about 1.19-pre1 format natives
        if ((library.natives == null || library.natives.isEmpty()) && library.name.contains(":natives-")) {
            Optional<LWJGLLibrary> lwjglLibrary = Optional
                    .ofNullable(lwjglVersion.get().libraries.get(libraryName).get(OS.getLWJGLClassifier()));

            if (!lwjglLibrary.isPresent()) {
                LogManager.debug(String.format(
                        "Not replacing library %s as couldn't find the library information for classifier %s",
                        library.name, OS.getLWJGLClassifier()));
                return library;
            }

            // if sha1 matches, assume library is good
            if (library.downloads.artifact.sha1.equals(lwjglLibrary.get().sha1)) {
                LogManager.debug(String.format("Not replacing library %s as the file is the same", library.name));
                return library;
            }

            LogManager.debug(String.format("Replacing native library %s", library.name));

            // update the artifact download
            replacedLibrary.downloads.artifact.path = lwjglLibrary.get().path;
            replacedLibrary.downloads.artifact.sha1 = lwjglLibrary.get().sha1;
            replacedLibrary.downloads.artifact.url = lwjglLibrary.get().url;
            replacedLibrary.downloads.artifact.size = lwjglLibrary.get().size;

            // match the name up (as version can change)
            replacedLibrary.name = lwjglLibrary.get().name;
        }

        // now the old version natives format
        if (library.natives != null && !library.natives.isEmpty()) {
            Optional<LWJGLLibrary> lwjglLibrary = Optional
                    .ofNullable(lwjglVersion.get().libraries.get(libraryName).get(OS.getLWJGLClassifier()));

            if (!lwjglLibrary.isPresent()) {
                LogManager.debug(String.format(
                        "Not replacing library %s as couldn't find the library information for classifier %s",
                        library.name, OS.getLWJGLClassifier()));
                return library;
            }

            // if sha1 matches, assume library is good
            if (library.downloads.artifact.sha1.equals(lwjglLibrary.get().sha1)) {
                LogManager.debug(String.format("Not replacing library %s as the file is the same", library.name));
                return library;
            }

            LogManager.debug(String.format("Replacing native library %s", library.name));

            // need to change the natives classifier
            Download nativeDownload = replacedLibrary.getNativeDownloadForOS();
            nativeDownload.path = lwjglLibrary.get().path;
            nativeDownload.sha1 = lwjglLibrary.get().sha1;
            nativeDownload.url = lwjglLibrary.get().url;
            nativeDownload.size = lwjglLibrary.get().size;
        }

        return replacedLibrary;
    }

    /**
     * We only replace LWJGL 2 if the user is on linux ARM
     */
    public static boolean shouldUseLegacyLWJGL(MinecraftVersion minecraftVersion) {
        return ConfigManager.getConfigItem("useLwjglReplacement", false) && App.settings.enableArmSupport
                && usesLegacyLWJGL(minecraftVersion) && OS.isArm() && OS.isLinux();
    }

    /**
     * We only replace LWJGL 3 if the user is on ARM (unless on Mac ARM where
     * Minecraft provides natives for it already)
     */
    public static boolean shouldReplaceLWJGL3(MinecraftVersion minecraftVersion) {
        return ConfigManager.getConfigItem("useLwjglReplacement", false) && App.settings.enableArmSupport
                && !usesLegacyLWJGL(minecraftVersion)
                && (OS.isArm() && (!OS.isMacArm() || !minecraftVersion.libraries.stream().anyMatch(
                        l -> l.name.startsWith("org.lwjgl:lwjgl") && l.name.endsWith("natives-macos-arm64"))));
    }
}
