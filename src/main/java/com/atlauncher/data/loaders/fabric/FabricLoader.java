/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
package com.atlauncher.data.loaders.fabric;

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

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.HashableDownloadable;
import com.atlauncher.data.loaders.Loader;
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
            String versionOverride) {
        this.minecraft = (String) metadata.get("minecraft");
        this.tempDir = tempDir;
        this.instanceInstaller = instanceInstaller;

        if (versionOverride != null) {
            this.version = this.getVersion(versionOverride);
        } else if (metadata.containsKey("loader")) {
            this.version = this.getVersion((String) metadata.get("loader"));
        } else if ((boolean) metadata.get("latest")) {
            LogManager.debug("Downloading latest Fabric version");
            this.version = this.getLatestVersion();
        }
    }

    public List<FabricMetaVersion> getLoaders() {
        try {
            Downloadable loaderVersions = new Downloadable(
                    String.format("https://meta.fabricmc.net/v2/versions/loader/%s", this.minecraft), false);

            String contents = loaderVersions.getContents();

            java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
            }.getType();
            return Gsons.DEFAULT_ALT.fromJson(contents, type);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public FabricMetaVersion getLoader(String version) {
        try {
            Downloadable loaderVersion = new Downloadable(
                    String.format("https://meta.fabricmc.net/v2/versions/loader/%s/%s", this.minecraft, version),
                    false);

            return Gsons.DEFAULT_ALT.fromJson(loaderVersion.getContents(), FabricMetaVersion.class);
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }

    public FabricMetaVersion getVersion(String version) {
        return this.getLoader(version);
    }

    public FabricMetaVersion getLatestVersion() {
        List<FabricMetaVersion> loaders = this.getLoaders();

        if (loaders == null || loaders.size() == 0) {
            return null;
        }

        return loaders.get(0);
    }

    @Override
    public List<Downloadable> getDownloadableLibraries() {
        return this.getLibrariesNeeded().stream().map(library -> {
            String libraryPath = Utils.convertMavenIdentifierToPath(library.getName());

            return new HashableDownloadable(library.getUrl() + libraryPath,
                    new File(App.settings.getGameLibrariesDir(), libraryPath), this.instanceInstaller);
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> getLibraries() {
        return this.getLibrariesNeeded().stream().map(library -> Utils.convertMavenIdentifierToPath(library.getName()))
                .collect(Collectors.toList());
    }

    private List<File> getLibraryFiles() {
        return this.getLibrariesNeeded().stream().map(library -> new File(App.settings.getGameLibrariesDir(),
                Utils.convertMavenIdentifierToPath(library.getName()))).collect(Collectors.toList());
    }

    private List<Library> getLibrariesNeeded() {
        List<Library> libraries = new ArrayList<>();

        libraries.add(new Library(this.version.getLoader().getMaven()));
        libraries.add(new Library(this.version.getIntermediary().getMaven()));
        libraries.addAll(this.version.getLauncherMeta().getLibraries(this.instanceInstaller.isServer()));

        return libraries;
    }

    @Override
    public String getMainClass() {
        return this.version.getLauncherMeta().getMainClass(this.instanceInstaller.isServer());
    }

    @Override
    public String getServerJar() {
        return "fabric-server-launch.jar";
    }

    @Override
    public boolean useMinecraftArguments() {
        return true;
    }

    @Override
    public void downloadAndExtractInstaller() {

    }

    @Override
    public void runProcessors() {
        if (!this.instanceInstaller.isServer()) {
            return;
        }

        makeServerLaunchJar();
    }

    private void makeServerLaunchJar() {
        File file = new File(this.instanceInstaller.getRootDirectory(), "fabric-server-launch.jar");
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
                zipOutputStream.write(("launch.mainClass="
                        + this.version.getLauncherMeta().getMainClass(this.instanceInstaller.isServer()) + "\n")
                                .getBytes(StandardCharsets.UTF_8));
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
                    new File(this.instanceInstaller.getRootDirectory(), "fabric-server-launcher.properties"));
            propertiesOutputStream.write(("serverJar=" + this.instanceInstaller.getMinecraftJar().getName() + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            propertiesOutputStream.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    @Override
    public List<String> getArguments() {
        return new ArrayList<>();
    }

    @Override
    public boolean useMinecraftLibraries() {
        return true;
    }

    public static List<String> getChoosableVersions(String minecraft) {
        try {
            Downloadable loaderVersions = new Downloadable(
                    String.format("https://meta.fabricmc.net/v2/versions/loader/%s", minecraft), false);

            String contents = loaderVersions.getContents();

            java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
            }.getType();

            List<FabricMetaVersion> versions = Gsons.DEFAULT_ALT.fromJson(contents, type);

            return versions.stream().map(version -> version.getLoader().getVersion()).collect(Collectors.toList());
        } catch (Throwable e) {
            LogManager.logStackTrace(e);
        }

        return null;
    }
}
