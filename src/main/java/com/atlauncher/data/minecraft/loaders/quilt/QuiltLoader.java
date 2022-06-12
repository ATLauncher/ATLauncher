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
package com.atlauncher.data.minecraft.loaders.quilt;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlauncher.FileSystem;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.network.Download;
import com.atlauncher.utils.Utils;
import com.atlauncher.workers.InstanceInstaller;
import com.google.gson.reflect.TypeToken;

public class QuiltLoader implements Loader {
    private static final Logger LOG = LogManager.getLogger(QuiltLoader.class);

    protected String minecraft;
    protected QuiltMetaVersion version;
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
            LOG.debug("Downloading latest Quilt version");
            this.version = this.getLatestVersion();
        }
    }

    public QuiltMetaVersion getLoader(String version) {
        return Download.build()
                .setUrl(String.format("https://meta.quiltmc.org/v3/versions/loader/%s/%s", this.minecraft, version))
                .asClass(QuiltMetaVersion.class);
    }

    public QuiltMetaVersion getVersion(String version) {
        return this.getLoader(version);
    }

    public QuiltMetaVersion getLatestVersion() {
        java.lang.reflect.Type type = new TypeToken<List<QuiltMetaVersion>>() {
        }.getType();

        List<QuiltMetaVersion> loaders = Download.build()
                .setUrl(String.format("https://meta.quiltmc.org/v3/versions/loader/%s?limit=1", this.minecraft))
                .asType(type);

        if (loaders == null || loaders.size() == 0) {
            return null;
        }

        return loaders.get(0);
    }

    @Override
    public List<Library> getLibraries() {
        List<Library> libraries = new ArrayList<>();

        libraries.add(new QuiltLibrary(this.version.loader.maven));

        if (ConfigManager.getConfigItem("loaders.quilt.switchHashedForIntermediary", true) == false) {
            libraries.add(new QuiltLibrary(this.version.hashed.maven.replace("org.quiltmc:hashed",
                    "net.fabricmc:intermediary"), Constants.FABRIC_MAVEN));
        } else {
            libraries.add(new QuiltLibrary(this.version.hashed.maven));
        }

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
        return "quilt-server-launch.jar";
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
        File file = new File(this.instanceInstaller.root.toFile(), "quilt-server-launch.jar");
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
                        "org.quiltmc.loader.impl.launch.server.QuiltServerLauncher");
                manifest.write(zipOutputStream);

                zipOutputStream.closeEntry();

                addedEntries.add("quilt-server-launch.properties");
                zipOutputStream.putNextEntry(new ZipEntry("quilt-server-launch.properties"));
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
                    new File(this.instanceInstaller.root.toFile(), "quilt-server-launcher.properties"));
            propertiesOutputStream.write(("serverJar=" + this.instanceInstaller.getMinecraftJar().getName() + "\n")
                    .getBytes(StandardCharsets.UTF_8));
            propertiesOutputStream.close();
        } catch (IOException e) {
            LOG.error("error", e);
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
        java.lang.reflect.Type type = new TypeToken<List<QuiltMetaVersion>>() {
        }.getType();

        try {
            List<QuiltMetaVersion> versions = Download.build()
                    .setUrl(String.format("https://meta.quiltmc.org/v3/versions/loader/%s", minecraft))
                    .asTypeWithThrow(type);

            List<String> disabledVersions = ConfigManager.getConfigItem("loaders.quilt.disabledVersions",
                    new ArrayList<String>());

            return versions.stream().filter(fv -> !disabledVersions.contains(fv.loader.version))
                    .map(version -> new LoaderVersion(version.loader.version, false, "Quilt"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public List<Library> getInstallLibraries() {
        return null;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return new LoaderVersion(version.loader.version, false, "Quilt");
    }
}
