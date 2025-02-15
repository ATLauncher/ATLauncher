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
package com.atlauncher.data.minecraft.loaders.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.graphql.GetFabricLoaderVersionQuery;
import com.atlauncher.graphql.GetFabricLoaderVersionsForMinecraftVersionQuery;
import com.atlauncher.graphql.GetLatestFabricLoaderVersionQuery;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;

public class FabricLoader implements Loader {
    protected String minecraft;
    protected String loaderVersion;
    protected FabricMetaProfile version;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;
    private final Pattern manifestPattern = Pattern.compile("META-INF/[^/]+\\.(SF|DSA|RSA|EC)");

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.loaderVersion = versionOverride.version;
        } else if (metadata.containsKey("loader")) {
            this.loaderVersion = (String) metadata.get("loader");
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Legacy Fabric version");
            this.loaderVersion = this.getLatestVersion();
        }

        this.version = this.getLoader(this.loaderVersion);
    }

    private FabricMetaProfile getLoader(String version) {
        GetFabricLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(GetFabricLoaderVersionQuery.builder().fabricVersion(version)
                        .minecraftVersion(this.minecraft).includeClientJson(
                                !instanceInstaller.isServer)
                        .includeServerJson(instanceInstaller.isServer).build());

        if (response == null || response.fabricLoaderVersion() == null) {
            return null;
        }

        if (instanceInstaller.isServer) {
            return Gsons.DEFAULT.fromJson(response.fabricLoaderVersion().serverJson(), FabricMetaProfile.class);
        }

        return Gsons.DEFAULT.fromJson(response.fabricLoaderVersion().clientJson(), FabricMetaProfile.class);
    }

    public String getLatestVersion() {
        GetLatestFabricLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetLatestFabricLoaderVersionQuery());

        if (response == null || response.fabricLoaderVersions() == null
                || response.fabricLoaderVersions().isEmpty()) {
            return null;
        }

        return response.fabricLoaderVersions().get(0).version();
    }

    @Override
    public List<Library> getLibraries() {
        return new ArrayList<>(this.version.libraries);
    }

    private List<File> getLibraryFiles() {
        return this.getLibraries().stream()
                .map(library -> FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile())
                .collect(Collectors.toList());
    }

    @Override
    public String getMainClass() {
        return this.version.mainClass;
    }

    @Override
    public String getServerJar() {
        return "fabric-server-launch.jar";
    }

    @Override
    public Path getServerJarPath() {
        return null;
    }

    @Override
    public void downloadAndExtractInstaller() throws Exception {

    }

    @Override
    public void runProcessors() {
        if (!this.instanceInstaller.isServer) {
            return;
        }

        makeServerLaunchJar();
    }

    private void makeServerLaunchJar() {
        File file = new File(this.instanceInstaller.root.toFile(), "fabric-server-launch.jar");
        if (file.exists()) {
            Utils.delete(file);
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);

            List<File> libraryFiles = this.getLibraryFiles();
            boolean shadeLibraries = !Utils.matchWholeVersion(this.loaderVersion, "0.12.5", false);

            Set<String> addedEntries = new HashSet<>();
            {
                addedEntries.add("META-INF/MANIFEST.MF");
                zipOutputStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
                manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
                        "net.fabricmc.loader.launch.server.FabricServerLauncher");

                if (!shadeLibraries) {
                    manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, getLibraries().stream()
                            .map(library -> instanceInstaller.root
                                    .relativize(instanceInstaller.root.resolve("libraries")
                                            .resolve(library.downloads.artifact.path))
                                    .normalize().toString())
                            .collect(Collectors.joining(" ")));
                }

                manifest.write(zipOutputStream);

                zipOutputStream.closeEntry();

                addedEntries.add("fabric-server-launch.properties");
                zipOutputStream.putNextEntry(new ZipEntry("fabric-server-launch.properties"));
                zipOutputStream.write(
                        ("launch.mainClass=" + this.version.mainClass
                                + "\n").getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();

                if (shadeLibraries) {
                    byte[] buffer = new byte[32768];

                    for (File f : libraryFiles) {
                        try (FileInputStream is = new FileInputStream(f); JarInputStream jis = new JarInputStream(is)) {
                            JarEntry entry;
                            while ((entry = jis.getNextJarEntry()) != null) {
                                if (!addedEntries.contains(entry.getName())
                                        && !manifestPattern.matcher(entry.getName()).matches()) {
                                    JarEntry newEntry = new JarEntry(entry.getName());
                                    zipOutputStream.putNextEntry(newEntry);

                                    int r;
                                    while ((r = jis.read(buffer, 0, buffer.length)) >= 0) {
                                        zipOutputStream.write(buffer, 0, r);
                                    }

                                    zipOutputStream.closeEntry();
                                    addedEntries.add(entry.getName());
                                }
                            }
                        }
                    }
                }
            }

            zipOutputStream.close();
            outputStream.close();

            FileOutputStream propertiesOutputStream = new FileOutputStream(
                    new File(this.instanceInstaller.root.toFile(), "fabric-server-launcher.properties"));
            propertiesOutputStream.write(("serverJar=" + this.instanceInstaller.getMinecraftJar().getName() + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            propertiesOutputStream.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public Arguments getArguments() {
        return this.version.arguments;
    }

    @Override
    public boolean useMinecraftArguments() {
        return true;
    }

    @Override
    public boolean useMinecraftLibraries() {
        return true;
    }

    public static List<LoaderVersion> getChoosableVersions(String minecraft) {
        try {
            List<String> disabledVersions = ConfigManager.getConfigItem("loaders.fabric.disabledVersions",
                    new ArrayList<>());

            GetFabricLoaderVersionsForMinecraftVersionQuery.Data response = GraphqlClient
                    .callAndWait(new GetFabricLoaderVersionsForMinecraftVersionQuery(minecraft));

            if (response == null || response.loaderVersions() == null
                    || response.loaderVersions().fabric() == null
                    || response.loaderVersions().fabric().isEmpty()) {
                return null;
            }

            return response.loaderVersions().fabric().stream()
                    .filter(fv -> !disabledVersions.contains(fv.version()))
                    .map(version -> new LoaderVersion(version.version(), false, "Fabric"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Library> getInstallLibraries() {
        return null;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(this.loaderVersion, false, "Fabric");
    }
}
