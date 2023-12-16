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
package com.atlauncher.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceExportFormat;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeManifestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeMinecraft;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthSide;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackFile;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCComponent;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.multimc.MultiMCRequire;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;

/**
 * @since 2023 / 12 / 16
 */
public class InstanceExport {

    private static boolean hasCustomImage(Instance instance) {
        File customImage = instance.getRoot().resolve("instance.png").toFile();

        return customImage.exists();
    }

    public static Pair<Path, String> export(Instance instance,
                                            String name,
                                            String version,
                                            String author,
                                            InstanceExportFormat format,
                                            String saveTo,
                                            List<String> overrides) {
        try {
            if (!Files.isDirectory(Paths.get(saveTo))) {
                Files.createDirectories(Paths.get(saveTo));
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to create export directory", e);
            return new Pair<Path, String>(null, null);
        }

        if (format == InstanceExportFormat.CURSEFORGE) {
            return exportAsCurseForgeZip(instance, name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MODRINTH) {
            return exportAsModrinthZip(instance, name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.CURSEFORGE_AND_MODRINTH) {
            if (exportAsCurseForgeZip(instance, name, version, author, saveTo, overrides).left() == null) {
                return new Pair<Path, String>(null, null);
            }

            return exportAsModrinthZip(instance, name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MULTIMC) {
            return exportAsMultiMcZip(instance, name, version, author, saveTo, overrides);
        }

        return new Pair<Path, String>(null, null);
    }

    public static Pair<Path, String> exportAsMultiMcZip(Instance instance,
                                                        String name,
                                                        String version,
                                                        String author,
                                                        String saveTo,
                                                        List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(safePathName + ".zip");
        MultiMCManifest manifest = new MultiMCManifest();

        manifest.formatVersion = 1;

        manifest.components = new ArrayList<>();

        Optional<Library> lwjgl3Version = instance.libraries.stream().filter(l -> l.name.contains("org.lwjgl:lwjgl:"))
            .findFirst();

        // minecraft
        MultiMCComponent minecraftComponent = new MultiMCComponent();
        minecraftComponent.cachedName = "Minecraft";
        minecraftComponent.important = true;
        minecraftComponent.cachedVersion = instance.id;
        minecraftComponent.uid = "net.minecraft";
        minecraftComponent.version = instance.id;

        if (lwjgl3Version.isPresent()) {
            String lwjgl3VersionString = lwjgl3Version.get().name.replace("org.lwjgl:lwjgl:", "");

            // lwjgl 3
            MultiMCComponent lwjgl3Component = new MultiMCComponent();
            lwjgl3Component.cachedName = "LWJGL 3";
            lwjgl3Component.cachedVersion = lwjgl3VersionString;
            lwjgl3Component.cachedVolatile = true;
            lwjgl3Component.dependencyOnly = true;
            lwjgl3Component.uid = "org.lwjgl3";
            lwjgl3Component.version = lwjgl3VersionString;
            manifest.components.add(lwjgl3Component);

            minecraftComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire lwjgl3Require = new MultiMCRequire();
            lwjgl3Require.equals = lwjgl3VersionString;
            lwjgl3Require.suggests = lwjgl3VersionString;
            lwjgl3Require.uid = "org.lwjgl3";
            minecraftComponent.cachedRequires.add(lwjgl3Require);
        }

        manifest.components.add(minecraftComponent);

        // fabric loader
        if (instance.launcher.loaderVersion.isFabric() || instance.launcher.loaderVersion.isLegacyFabric()) {
            // mappings
            MultiMCComponent fabricMappingsComponent = new MultiMCComponent();
            fabricMappingsComponent.cachedName = "Intermediary Mappings";

            fabricMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = instance.id;
            minecraftRequire.uid = "net.minecraft";
            fabricMappingsComponent.cachedRequires.add(minecraftRequire);

            fabricMappingsComponent.cachedVersion = instance.id;
            fabricMappingsComponent.cachedVolatile = true;
            fabricMappingsComponent.dependencyOnly = true;
            fabricMappingsComponent.uid = "net.fabricmc.intermediary";
            fabricMappingsComponent.version = instance.id;
            manifest.components.add(fabricMappingsComponent);

            // loader
            MultiMCComponent fabricLoaderComponent = new MultiMCComponent();
            fabricLoaderComponent.cachedName = "Fabric Loader";

            fabricLoaderComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire intermediaryRequire = new MultiMCRequire();
            intermediaryRequire.uid = "net.fabricmc.intermediary";
            fabricLoaderComponent.cachedRequires.add(intermediaryRequire);

            fabricLoaderComponent.cachedVersion = instance.launcher.loaderVersion.version;
            fabricLoaderComponent.uid = "net.fabricmc.fabric-loader";
            fabricLoaderComponent.version = instance.launcher.loaderVersion.version;
            manifest.components.add(fabricLoaderComponent);
        }

        // forge loader
        if (instance.launcher.loaderVersion.isNeoForge()) {
            // loader
            MultiMCComponent forgeMappingsComponent = new MultiMCComponent();
            forgeMappingsComponent.cachedName = "Forge";

            forgeMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = instance.id;
            minecraftRequire.uid = "net.minecraft";
            forgeMappingsComponent.cachedRequires.add(minecraftRequire);

            forgeMappingsComponent.cachedVersion = instance.launcher.loaderVersion.version;
            forgeMappingsComponent.uid = "net.neoforged";
            forgeMappingsComponent.version = instance.launcher.loaderVersion.version;
            manifest.components.add(forgeMappingsComponent);
        }

        // forge loader
        if (instance.launcher.loaderVersion.isForge()) {
            // loader
            MultiMCComponent forgeMappingsComponent = new MultiMCComponent();
            forgeMappingsComponent.cachedName = "Forge";

            forgeMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = instance.id;
            minecraftRequire.uid = "net.minecraft";
            forgeMappingsComponent.cachedRequires.add(minecraftRequire);

            forgeMappingsComponent.cachedVersion = instance.launcher.loaderVersion.version;
            forgeMappingsComponent.uid = "net.minecraftforge";
            forgeMappingsComponent.version = instance.launcher.loaderVersion.version;
            manifest.components.add(forgeMappingsComponent);
        }

        // quilt loader
        if (instance.launcher.loaderVersion.isQuilt()) {
            String hashedName = "org.quiltmc.hashed";
            String cachedName = "Hashed Mappings";
            if (ConfigManager.getConfigItem("loaders.quilt.switchHashedForIntermediary", true) == false) {
                hashedName = "net.fabricmc.intermediary";
                cachedName = "Intermediary Mappings";
            }

            // mappings
            MultiMCComponent quiltMappingsComponent = new MultiMCComponent();
            quiltMappingsComponent.cachedName = cachedName;

            quiltMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = instance.id;
            minecraftRequire.uid = "net.minecraft";
            quiltMappingsComponent.cachedRequires.add(minecraftRequire);

            quiltMappingsComponent.cachedVersion = instance.id;
            quiltMappingsComponent.cachedVolatile = true;
            quiltMappingsComponent.dependencyOnly = true;
            quiltMappingsComponent.uid = hashedName;
            quiltMappingsComponent.version = instance.id;
            manifest.components.add(quiltMappingsComponent);

            // loader
            MultiMCComponent quiltLoaderComponent = new MultiMCComponent();
            quiltLoaderComponent.cachedName = "Quilt Loader";

            quiltLoaderComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire hashedRequire = new MultiMCRequire();
            hashedRequire.uid = hashedName;
            quiltLoaderComponent.cachedRequires.add(hashedRequire);

            quiltLoaderComponent.cachedVersion = instance.launcher.loaderVersion.version;
            quiltLoaderComponent.uid = "org.quiltmc.quilt-loader";
            quiltLoaderComponent.version = instance.launcher.loaderVersion.version;
            manifest.components.add(quiltLoaderComponent);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(instance.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create mmc-pack.json
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            new FileOutputStream(tempDir.resolve("mmc-pack.json").toFile()), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
        }

        // if Legacy Fabric, add patch in
        if (instance.launcher.loaderVersion.type.equals("LegacyFabric")) {
            FileUtils.createDirectory(tempDir.resolve("patches"));

            JsonObject patch = new JsonObject();
            patch.addProperty("formatVersion", 1);
            patch.addProperty("name", "Intermediary Mappings");
            patch.addProperty("uid", "net.fabricmc.intermediary");
            patch.addProperty("version", instance.id);

            JsonArray plusLibraries = new JsonArray();
            JsonObject intermediary = new JsonObject();
            intermediary.addProperty("name", String.format("net.fabricmc:intermediary:%s", instance.id));
            intermediary.addProperty("url", Constants.LEGACY_FABRIC_MAVEN);
            plusLibraries.add(intermediary);
            patch.add("+libraries", plusLibraries);

            // create net.fabricmc.intermediary.json
            try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                new FileOutputStream(tempDir.resolve("net.fabricmc.intermediary.json").toFile()),
                StandardCharsets.UTF_8)) {
                Gsons.DEFAULT.toJson(patch, fileWriter);
            } catch (JsonIOException | IOException e) {
                LogManager.logStackTrace("Failed to save net.fabricmc.intermediary.json", e);

                FileUtils.deleteDirectory(tempDir);

                return new Pair<Path, String>(null, null);
            }

        }

        // create instance.cfg
        Path instanceCfgPath = tempDir.resolve("instance.cfg");
        Properties instanceCfg = new Properties();

        String iconKey = "default";
        if (hasCustomImage(instance)) {
            String customIconFileName = "atlauncher_" + instance.getSafeName().toLowerCase(Locale.ENGLISH);
            Path customIconPath = tempDir.resolve(customIconFileName + ".png");

            FileUtils.copyFile(instance.getRoot().resolve("instance.png"), customIconPath, true);

            iconKey = customIconFileName;
        }

        instanceCfg.setProperty("AutoCloseConsole", "false");
        instanceCfg.setProperty("ForgeVersion", "false");
        instanceCfg.setProperty("InstanceType", "OneSix");
        instanceCfg.setProperty("IntendedVersion", "");
        instanceCfg.setProperty("JavaPath", Optional.ofNullable(instance.launcher.javaPath).orElse(App.settings.javaPath)
            + File.separator + "bin" + File.separator + (OS.isWindows() ? "javaw.exe" : "java"));
        instanceCfg.setProperty("JVMArgs", Optional.ofNullable(instance.launcher.javaArguments).orElse(""));
        instanceCfg.setProperty("LWJGLVersion", "");
        instanceCfg.setProperty("LaunchMaximized", "false");
        instanceCfg.setProperty("LiteloaderVersion", "");
        instanceCfg.setProperty("LogPrePostOutput", "true");
        instanceCfg.setProperty("MCLaunchMethod", "LauncherPart");
        instanceCfg.setProperty("MaxMemAlloc",
            Optional.ofNullable(instance.launcher.maximumMemory).orElse(App.settings.maximumMemory) + "");

        if (ConfigManager.getConfigItem("removeInitialMemoryOption", false) == false) {
            instanceCfg.setProperty("MinMemAlloc",
                Optional.ofNullable(instance.launcher.initialMemory).orElse(App.settings.initialMemory) + "");
        }

        instanceCfg.setProperty("MinecraftWinHeight", App.settings.windowHeight + "");
        instanceCfg.setProperty("MinecraftWinWidth", App.settings.windowWidth + "");
        instanceCfg.setProperty("OverrideCommands",
            instance.launcher.postExitCommand != null || instance.launcher.preLaunchCommand != null || instance.launcher.wrapperCommand != null
                ? "true"
                : "false");
        instanceCfg.setProperty("OverrideConsole", "false");
        instanceCfg.setProperty("OverrideJava", instance.launcher.javaPath == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaArgs", instance.launcher.javaArguments == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaLocation", "false");
        instanceCfg.setProperty("OverrideMCLaunchMethod", "false");
        instanceCfg.setProperty("OverrideMemory", instance.launcher.maximumMemory == null ? "false" : "true");
        instanceCfg.setProperty("OverrideNativeWorkarounds", "false");
        instanceCfg.setProperty("OverrideWindow", "false");
        instanceCfg.setProperty("PermGen", Optional.ofNullable(instance.launcher.permGen).orElse(App.settings.metaspace) + "");
        instanceCfg.setProperty("PostExitCommand",
            Optional.ofNullable(instance.launcher.postExitCommand).orElse(App.settings.postExitCommand) + "");
        instanceCfg.setProperty("PreLaunchCommand",
            Optional.ofNullable(instance.launcher.preLaunchCommand).orElse(App.settings.preLaunchCommand) + "");
        instanceCfg.setProperty("ShowConsole", "false");
        instanceCfg.setProperty("ShowConsoleOnError", "true");
        instanceCfg.setProperty("UseNativeGLFW", "false");
        instanceCfg.setProperty("UseNativeOpenAL", "false");
        instanceCfg.setProperty("WrapperCommand",
            Optional.ofNullable(instance.launcher.wrapperCommand).orElse(App.settings.wrapperCommand) + "");
        instanceCfg.setProperty("iconKey", iconKey);
        instanceCfg.setProperty("name", instance.launcher.name);
        instanceCfg.setProperty("lastLaunchTime", "");
        instanceCfg.setProperty("notes", "");
        instanceCfg.setProperty("totalTimePlayed", "0");

        try (OutputStream outputStream = Files.newOutputStream(instanceCfgPath)) {
            instanceCfg.store(outputStream, "Exported by ATLauncher");
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
        }

        // create an empty .packignore file
        Path packignoreFile = tempDir.resolve(".packignore");
        try {
            packignoreFile.toFile().createNewFile();
        } catch (IOException ignored) {
            // this is okay to ignore, it's unused but seems to be there by default
        }

        // copy over the files into the .minecraft folder
        Path dotMinecraftPath = tempDir.resolve(".minecraft");
        FileUtils.createDirectory(dotMinecraftPath);

        for (String path : overrides) {
            if (!path.equalsIgnoreCase(safePathName + ".zip") && instance.getRoot().resolve(path).toFile().exists()
                && (instance.getRoot().resolve(path).toFile().isFile()
                || instance.getRoot().resolve(path).toFile().list().length != 0)) {
                if (instance.getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(instance.getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(instance.getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(dotMinecraftPath)) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                .forEach(f -> {
                    FileUtils.delete(f, false);
                });
        } catch (IOException ignored) {
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<Path, String>(to, null);
    }

    public static Pair<Path, String> exportAsCurseForgeZip(Instance instance,
                                                           String name,
                                                           String version,
                                                           String author,
                                                           String saveTo,
                                                           List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.zip", safePathName, version));
        CurseForgeManifest manifest = new CurseForgeManifest();

        // for any mods not from CurseForge, scan for them on CurseForge
        if (!App.settings.dontCheckModsOnCurseForge) {
            Map<Long, DisableableMod> murmurHashes = new HashMap<>();

            instance.launcher.mods.stream()
                .filter(m -> !m.disabled && !m.isFromCurseForge())
                .forEach(dm -> {
                    try {
                        long hash = Hashing.murmur(dm.getFile(instance.ROOT, instance.id).toPath());
                        murmurHashes.put(hash, dm);
                    } catch (Throwable t) {
                        LogManager.logStackTrace(t);
                    }
                });

            if (murmurHashes.size() != 0) {
                CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                    .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

                if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                    int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id)
                        .toArray();

                    if (projectIdsFound.length != 0) {
                        Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
                            .getProjectsAsMap(projectIdsFound);

                        if (foundProjects != null) {
                            fingerprintResponse.exactMatches.stream()
                                .filter(em -> em != null && em.file != null
                                    && murmurHashes.containsKey(em.file.packageFingerprint))
                                .forEach(foundMod -> {
                                    DisableableMod dm = murmurHashes
                                        .get(foundMod.file.packageFingerprint);

                                    // add CurseForge information
                                    dm.curseForgeProjectId = foundMod.id;
                                    dm.curseForgeFile = foundMod.file;
                                    dm.curseForgeFileId = foundMod.file.id;

                                    CurseForgeProject curseForgeProject = foundProjects
                                        .get(foundMod.id);

                                    if (curseForgeProject != null) {
                                        dm.curseForgeProject = curseForgeProject;
                                    }

                                    LogManager.debug("Found matching mod from CurseForge called "
                                        + dm.curseForgeFile.displayName);
                                });
                        }
                    }
                }
            }
        }
        instance.save();

        CurseForgeMinecraft minecraft = new CurseForgeMinecraft();

        List<CurseForgeModLoader> modLoaders = new ArrayList<>();
        CurseForgeModLoader modLoader = new CurseForgeModLoader();

        String loaderType = instance.launcher.loaderVersion.type.toLowerCase(Locale.ENGLISH);
        String loaderVersion = instance.launcher.loaderVersion.version;

        modLoader.id = loaderType + "-" + loaderVersion;
        modLoader.primary = true;
        modLoaders.add(modLoader);

        minecraft.version = instance.id;
        minecraft.modLoaders = modLoaders;

        manifest.minecraft = minecraft;
        manifest.manifestType = "minecraftModpack";
        manifest.manifestVersion = 1;
        manifest.name = name;
        manifest.version = version;
        manifest.author = author;
        manifest.files = instance.launcher.mods.stream()
            .filter(m -> !m.disabled && m.isFromCurseForge())
            .filter(mod -> overrides.stream()
                .anyMatch(path -> instance.getRoot().relativize(mod.getPath(instance)).startsWith(path)))
            .map(mod -> {
                CurseForgeManifestFile file = new CurseForgeManifestFile();
                file.projectID = mod.curseForgeProjectId;
                file.fileID = mod.curseForgeFileId;
                file.required = true;

                return file;
            }).collect(Collectors.toList());
        manifest.overrides = "overrides";

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(instance.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create manifest.json
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            new FileOutputStream(tempDir.resolve("manifest.json").toFile()), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save manifest.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
        }

        // create modlist.html
        StringBuilder sb = new StringBuilder("<ul>");
        instance.launcher.mods.stream()
            .filter(m -> !m.disabled && m.isFromCurseForge())
            .filter(mod -> overrides.stream()
                .anyMatch(path -> instance.getRoot().relativize(mod.getPath(instance)).startsWith(path)))
            .forEach(mod -> {
                if (mod.hasFullCurseForgeInformation()) {
                    sb.append("<li><a href=\"").append(mod.curseForgeProject.getWebsiteUrl()).append("\">")
                        .append(mod.name)
                        .append("</a></li>");
                } else {
                    sb.append("<li>").append(mod.name).append("</li>");
                }
            });
        sb.append("</ul>");

        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            new FileOutputStream(tempDir.resolve("modlist.html").toFile()), StandardCharsets.UTF_8)) {
            fileWriter.write(sb.toString());
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modlist.html", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
        }

        // copy over the overrides folder
        Path overridesPath = tempDir.resolve("overrides");
        FileUtils.createDirectory(overridesPath);

        for (String path : overrides) {
            if (!path.equalsIgnoreCase(safePathName + ".zip") && instance.getRoot().resolve(path).toFile().exists()
                && (instance.getRoot().resolve(path).toFile().isFile()
                || instance.getRoot().resolve(path).toFile().list().length != 0)) {
                if (instance.getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(instance.getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(instance.getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(overridesPath)) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                .forEach(f -> {
                    FileUtils.delete(f, false);
                });
        } catch (IOException ignored) {
        }

        // remove files that come from CurseForge or aren't disabled
        instance.launcher.mods.stream().filter(m -> !m.disabled && m.isFromCurseForge()).forEach(mod -> {
            File file = mod.getFile(instance, overridesPath);

            if (file.exists()) {
                FileUtils.delete(file.toPath());
            }
        });

        for (String path : overrides) {
            // if no files, remove the directory
            if (overridesPath.resolve(path).toFile().isDirectory()
                && overridesPath.resolve(path).toFile().list().length == 0) {
                FileUtils.deleteDirectory(overridesPath.resolve(path));
            }
        }

        // if overrides folder itself is empty, then remove it
        if (overridesPath.toFile().list().length == 0) {
            FileUtils.deleteDirectory(overridesPath);
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<Path, String>(to, null);
    }

    public static Pair<Path, String> exportAsModrinthZip(Instance instance,
                                                         String name,
                                                         String version,
                                                         String author,
                                                         String saveTo,
                                                         List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.mrpack", safePathName, version));
        ModrinthModpackManifest manifest = new ModrinthModpackManifest();

        // for any mods not from Modrinth, scan for them on Modrinth
        if (!App.settings.dontCheckModsOnModrinth) {
            List<DisableableMod> nonModrinthMods = instance.launcher.mods.parallelStream()
                .filter(m -> !m.disabled && !m.isFromModrinth() && m.getFile(instance).exists())
                .collect(Collectors.toList());

            String[] sha1Hashes = nonModrinthMods.parallelStream()
                .map(m -> Hashing.sha1(m.getFile(instance).toPath()).toString()).toArray(String[]::new);

            Map<String, ModrinthVersion> modrinthVersions = ModrinthApi.getVersionsFromSha1Hashes(sha1Hashes);

            if (modrinthVersions.size() != 0) {
                Map<String, ModrinthProject> modrinthProjects = ModrinthApi.getProjectsAsMap(
                    modrinthVersions.values().parallelStream().map(mv -> mv.projectId).toArray(String[]::new));

                nonModrinthMods.parallelStream().forEach(mod -> {
                    String hash = Hashing.sha1(mod.getFile(instance).toPath()).toString();

                    if (modrinthVersions.containsKey(hash)) {
                        ModrinthVersion modrinthVersion = modrinthVersions.get(hash);

                        mod.modrinthVersion = modrinthVersion;

                        LogManager.debug("Found matching version from Modrinth called " + mod.modrinthVersion.name);

                        if (modrinthProjects.containsKey(modrinthVersions.get(hash).projectId)) {
                            mod.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);
                        }
                    }
                });
                instance.save();
            }
        }

        manifest.formatVersion = 1;
        manifest.game = "minecraft";
        manifest.versionId = version;
        manifest.name = name;
        manifest.summary = instance.launcher.description;
        manifest.files = instance.launcher.mods.parallelStream()
            .filter(m -> !m.disabled && m.modrinthVersion != null && m.getFile(instance).exists())
            .filter(mod -> overrides.stream()
                .anyMatch(path -> instance.getRoot().relativize(mod.getPath(instance)).startsWith(path)))
            .map(mod -> {
                Path modPath = mod.getFile(instance).toPath();

                ModrinthModpackFile file = new ModrinthModpackFile();
                file.path = instance.ROOT.relativize(modPath).toString().replace("\\", "/");

                String sha1Hash = Hashing.sha1(modPath).toString();

                file.hashes = new HashMap<>();
                file.hashes.put("sha1", sha1Hash);
                file.hashes.put("sha512", Hashing.sha512(modPath).toString());

                file.env = new HashMap<>();

                if (mod.modrinthProject != null) {
                    file.env.put("client",
                        mod.modrinthProject.clientSide == ModrinthSide.UNSUPPORTED ? "unsupported"
                            : "required");
                    file.env.put("server",
                        mod.modrinthProject.serverSide == ModrinthSide.UNSUPPORTED ? "unsupported"
                            : "required");
                } else {
                    file.env.put("client", "required");
                    file.env.put("server", "required");
                }

                file.fileSize = modPath.toFile().length();

                file.downloads = new ArrayList<>();
                file.downloads.add(HttpUrl.get(mod.modrinthVersion.getFileBySha1(sha1Hash).url).toString());

                return file;
            }).collect(Collectors.toList());
        manifest.dependencies = new HashMap<>();

        manifest.dependencies.put("minecraft", instance.id);

        if (instance.launcher.loaderVersion != null) {
            manifest.dependencies.put(instance.launcher.loaderVersion.getTypeForModrinthExport(),
                instance.launcher.loaderVersion.version);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(instance.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create modrinth.index.json
        try (FileOutputStream fos = new FileOutputStream(tempDir.resolve("modrinth.index.json").toFile());
             OutputStreamWriter osw = new OutputStreamWriter(fos,
                 StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, osw);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modrinth.index.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
        }

        // copy over the overrides folder
        Path overridesPath = tempDir.resolve("overrides");
        FileUtils.createDirectory(overridesPath);

        for (String path : overrides) {
            if (!path.equalsIgnoreCase(safePathName + ".zip") && instance.getRoot().resolve(path).toFile().exists()
                && (instance.getRoot().resolve(path).toFile().isFile()
                || instance.getRoot().resolve(path).toFile().list().length != 0)) {
                if (instance.getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(instance.getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(instance.getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(overridesPath)) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                .forEach(f -> {
                    FileUtils.delete(f, false);
                });
        } catch (IOException ignored) {
        }

        // remove files that come from Modrinth or aren't disabled
        instance.launcher.mods.stream().filter(m -> !m.disabled && m.modrinthVersion != null).forEach(mod -> {
            File file = mod.getFile(instance, overridesPath);

            if (file.exists()) {
                FileUtils.delete(file.toPath());
            }
        });

        for (String path : overrides) {
            // if no files, remove the directory
            if (overridesPath.resolve(path).toFile().isDirectory()
                && overridesPath.resolve(path).toFile().list().length == 0) {
                FileUtils.deleteDirectory(overridesPath.resolve(path));
            }
        }

        // if overrides folder itself is empty, then remove it
        if (overridesPath.toFile().list().length == 0) {
            FileUtils.deleteDirectory(overridesPath);
        }

        // find any override jar/zip files
        StringBuilder overridesForPermissions = new StringBuilder();
        try (Stream<Path> walk = Files.walk(overridesPath)) {
            walk.filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".jar")
                    || path.getFileName().toString().endsWith(".zip"))
                .forEach(f -> {
                    overridesForPermissions.append(String.format("%s\n", tempDir.relativize(f)));
                });
        } catch (IOException ignored) {
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<Path, String>(to, overridesForPermissions.toString());
    }
}
