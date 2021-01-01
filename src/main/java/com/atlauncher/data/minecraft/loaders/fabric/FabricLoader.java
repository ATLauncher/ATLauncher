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
package com.atlauncher.data.minecraft.loaders.fabric;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.atlauncher.FileSystem;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.Download;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.reflect.TypeToken;

public class FabricLoader implements Loader {
    protected String minecraft;
    protected FabricMetaVersion version;
    protected File tempDir;
    protected InstanceInstaller instanceInstaller;

    @Override
    public void set(Map<String, Object> metadata, File tempDir, InstanceInstaller instanceInstaller,
            LoaderVersion versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.version = this.getVersion(versionOverride.version);
        } else if (metadata.containsKey("loader")) {
            this.version = this.getVersion((String) metadata.get("loader"));
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Fabric version");
            this.version = this.getLatestVersion();
        }
    }

    public FabricMetaVersion getLoader(String version) {
        return Download.build()
                .setUrl(String.format("https://meta.fabricmc.net/v2/versions/loader/%s/%s", this.minecraft, version))
                .asClass(FabricMetaVersion.class);
    }

    public FabricMetaVersion getVersion(String version) {
        return this.getLoader(version);
    }

    public FabricMetaVersion getLatestVersion() {
        java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
        }.getType();

        List<FabricMetaVersion> loaders = Download.build()
                .setUrl(String.format("https://meta.fabricmc.net/v2/versions/loader/%s?limit=1", this.minecraft))
                .asType(type);

        if (loaders == null || loaders.size() == 0) {
            return null;
        }

        return loaders.get(0);
    }

    @Override
    public List<Library> getLibraries() {
        List<Library> libraries = new ArrayList<>();

        libraries.add(new FabricLibrary(this.version.loader.maven));
        libraries.add(new FabricLibrary(this.version.intermediary.maven));
        libraries.addAll(this.version.launcherMeta.getLibraries(this.instanceInstaller.isServer));

        return libraries;
    }

    private List<File> getLibraryFiles() {
        return this.getLibraries().stream()
                .map(library -> FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile())
                .collect(Collectors.toList());
    }

    @Override
    public String getMainClass() {
        return this.version.launcherMeta.getMainClass(this.instanceInstaller.isServer);
    }

    @Override
    public String getServerJar() {
        return "fabric-server-launch.jar";
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

            Set<String> addedEntries = new HashSet<>();
            {
                addedEntries.add("META-INF/MANIFEST.MF");
                zipOutputStream.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));

                Manifest manifest = new Manifest();
                manifest.getMainAttributes().put(new Attributes.Name("Manifest-Version"), "1.0");
                manifest.getMainAttributes().put(new Attributes.Name("Main-Class"),
                        "net.fabricmc.loader.launch.server.FabricServerLauncher");
                manifest.write(zipOutputStream);

                zipOutputStream.closeEntry();

                addedEntries.add("fabric-server-launch.properties");
                zipOutputStream.putNextEntry(new ZipEntry("fabric-server-launch.properties"));
                zipOutputStream.write(
                        ("launch.mainClass=" + this.version.launcherMeta.getMainClass(this.instanceInstaller.isServer)
                                + "\n").getBytes(StandardCharsets.UTF_8));
                zipOutputStream.closeEntry();

                byte[] buffer = new byte[32768];

                for (File f : libraryFiles) {
                    try (FileInputStream is = new FileInputStream(f); JarInputStream jis = new JarInputStream(is)) {
                        JarEntry entry;
                        while ((entry = jis.getNextJarEntry()) != null) {
                            if (!addedEntries.contains(entry.getName())) {
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
        return null;
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
        java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
        }.getType();

        List<FabricMetaVersion> versions = Download.build()
                .setUrl(String.format("https://meta.fabricmc.net/v2/versions/loader/%s", minecraft)).asType(type);

        return versions.stream().map(version -> new LoaderVersion(version.loader.version, false, "Fabric"))
                .collect(Collectors.toList());
    }

    @Override
    public List<Library> getInstallLibraries() {
        return null;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(version.loader.version, false, "Fabric");
    }
}
