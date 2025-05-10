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
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.jetbrains.annotations.NotNull;
import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileHash;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeFingerprintedMod;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.CurseForgeSocialLinkType;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeManifestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeMinecraft;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.FMLLibrariesConstants;
import com.atlauncher.data.minecraft.loaders.forge.FMLLibrary;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader;
import com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthProjectType;
import com.atlauncher.data.modrinth.ModrinthSide;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackFile;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCComponent;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.multimc.MultiMCRequire;
import com.atlauncher.exceptions.CommandException;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.graphql.AddPackActionMutation;
import com.atlauncher.graphql.AddPackTimePlayedMutation;
import com.atlauncher.graphql.type.AddPackActionInput;
import com.atlauncher.graphql.type.AddPackTimePlayedInput;
import com.atlauncher.graphql.type.PackLogAction;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.CurseForgeUpdateManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.FTBUpdateManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LWJGLManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ModrinthModpackUpdateManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.managers.TechnicModpackUpdateManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CommandExecutor;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.CurseForgeUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.SecurityUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

@Json
public class Instance extends MinecraftVersion implements ModManagement {
    public UUID uuid;
    public String inheritsFrom;
    public InstanceLauncher launcher;

    public transient Path ROOT;

    /**
     * @deprecated moved within launcher property
     */
    public transient Instant lastPlayed;

    /**
     * @deprecated moved within launcher property
     */
    public transient long numPlays;

    public Instance(MinecraftVersion version) {
        setValues(version);
    }

    public void setValues(MinecraftVersion version) {
        this.id = version.id;
        this.libraries = version.libraries;
        this.mainClass = version.mainClass;
        this.minecraftArguments = version.minecraftArguments;
        this.arguments = version.arguments;
        setUpdatedValues(version);
    }

    public void setUpdatedValues(MinecraftVersion version) {
        this.complianceLevel = version.complianceLevel;
        this.javaVersion = version.javaVersion;
        this.type = version.type;
        this.time = version.time;
        this.releaseTime = version.releaseTime;
        this.minimumLauncherVersion = version.minimumLauncherVersion;
        this.assetIndex = version.assetIndex;
        this.assets = version.assets;
        this.downloads = version.downloads;
        this.rules = version.rules;
        this.logging = version.logging;
    }

    public UUID getUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
            save();
        }
        return uuid;
    }

    public String getSafeName() {
        return this.launcher.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getSafePackName() {
        return this.launcher.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    @Override
    public Path getRoot() {
        return this.ROOT;
    }

    public Pack getPack() {
        if (this.isExternalPack() || this.isVanillaInstance()) {
            return null;
        }

        try {
            return PackManager.getPackByID(this.launcher.packId);
        } catch (InvalidPack e) {
            return null;
        }
    }

    public PackVersion getLatestVersion() {
        Pack pack = this.getPack();

        if (pack != null) {
            if (pack.hasVersions() && !this.launcher.isDev) {
                return pack.getLatestVersion();
            }

            if (this.launcher.isDev) {
                return pack.getLatestDevVersion();
            }
        }

        return null;
    }

    public String getPackDescription() {
        Pack pack = this.getPack();

        if (pack != null) {
            return pack.description;
        } else {
            if (launcher.description != null) {
                return launcher.description;
            }

            return GetText.tr("No Description");
        }
    }

    private boolean hasCustomImage() {
        File customImage = this.getRoot().resolve("instance.png").toFile();

        return customImage.exists();
    }

    public ImageIcon getImage() {
        File customImage = this.getRoot().resolve("instance.png").toFile();

        if (customImage.exists()) {
            try {
                BufferedImage img = ImageIO.read(customImage);
                if (img != null) {
                    // if a square image, then make it 300x150 (without stretching) centered
                    if (img.getHeight(null) == img.getWidth(null)) {
                        BufferedImage dimg = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);

                        Graphics2D g2d = dimg.createGraphics();
                        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                        g2d.drawImage(img, 75, 0, 150, 150, null);
                        g2d.dispose();

                        return new ImageIcon(dimg);
                    }

                    return new ImageIcon(img.getScaledInstance(300, 150, Image.SCALE_SMOOTH));
                }
            } catch (IOException e) {
                LogManager.warn("Error creating scaled image from the custom image of instance " + this.launcher.name
                        + ". Using default image.");
            }
        }

        if (getPack() != null) {
            File instancesImage = FileSystem.IMAGES.resolve(this.getSafePackName().toLowerCase(Locale.ENGLISH) + ".png")
                    .toFile();

            if (instancesImage.exists()) {
                return Utils.getIconImage(instancesImage);
            }
        }

        return Utils.getIconImage("/assets/image/default-image.png");
    }

    public void ignoreUpdate() {
        String version;

        if (launcher.vanillaInstance) {
            return;
        } else if (isExternalPack()) {
            if (isFTBPack()) {
                version = Integer.toString(FTBUpdateManager.getLatestVersion(this).id);
            } else if (isCurseForgePack()) {
                version = Integer.toString(CurseForgeUpdateManager.getLatestVersion(this).id);
            } else if (isTechnicPack()) {
                if (isTechnicSolderPack()) {
                    version = TechnicModpackUpdateManager.getUpToDateSolderModpack(this).latest;
                } else {
                    version = TechnicModpackUpdateManager.getUpToDateModpack(this).version;
                }
            } else if (isModrinthPack()) {
                version = ModrinthModpackUpdateManager.getLatestVersion(this).id;
            } else {
                return;
            }
        } else {
            if (this.launcher.isDev) {
                version = getLatestVersion().hash;
            } else {
                version = getLatestVersion().version;
            }
        }

        if (!hasUpdateBeenIgnored(version)) {
            this.launcher.ignoredUpdates.add(version);
            this.save();
        }
    }

    public void ignoreAllUpdates() {
        this.launcher.ignoreAllUpdates = true;
        this.save();
    }

    public boolean hasLatestUpdateBeenIgnored() {
        if (launcher.vanillaInstance) {
            return false;
        }

        if (isExternalPack()) {
            if (isFTBPack()) {
                return hasUpdateBeenIgnored(Integer.toString(FTBUpdateManager.getLatestVersion(this).id));
            } else if (isCurseForgePack()) {
                return hasUpdateBeenIgnored(Integer.toString(CurseForgeUpdateManager.getLatestVersion(this).id));
            } else if (isTechnicPack()) {
                if (isTechnicSolderPack()) {
                    return hasUpdateBeenIgnored(TechnicModpackUpdateManager.getUpToDateSolderModpack(this).latest);
                } else {
                    return hasUpdateBeenIgnored(TechnicModpackUpdateManager.getUpToDateModpack(this).version);
                }
            } else if (isModrinthPack()) {
                return hasUpdateBeenIgnored(ModrinthModpackUpdateManager.getLatestVersion(this).id);
            }

            return false;
        }

        String version;

        if (this.launcher.isDev) {
            version = getLatestVersion().hash;
        } else {
            version = getLatestVersion().version;
        }

        return hasUpdateBeenIgnored(version);
    }

    private boolean hasUpdateBeenIgnored(String version) {
        if (this.launcher.ignoreAllUpdates) {
            return true;
        }

        if (version == null || this.launcher.ignoredUpdates.isEmpty()) {
            return false;
        }

        return this.launcher.ignoredUpdates.stream().anyMatch(v -> v.equalsIgnoreCase(version));
    }

    public Path getMinecraftJarLibraryPath() {
        return FileSystem.LIBRARIES.resolve(String.format("net/minecraft/client/%1$s/client-%1$s.jar", this.id));
    }

    public Path getCustomMinecraftJarLibraryPath() {
        return ROOT.resolve("bin/minecraft.jar");
    }

    /**
     * This will prepare the instance for launch. It will download the assets,
     * Minecraft jar and libraries, as well as organise the libraries, ready to be
     * played.
     */
    public boolean prepareForLaunch(ProgressDialog<Boolean> progressDialog, Path nativesTempDir,
            Path lwjglNativesTempDir) {
        PerformanceManager.start();
        OkHttpClient httpClient = Network.createProgressClient(progressDialog);

        PerformanceManager.start("Downloading Minecraft");
        try {
            progressDialog.setLabel(GetText.tr("Downloading Minecraft"));
            com.atlauncher.network.Download clientDownload = com.atlauncher.network.Download.build()
                    .setUrl(this.downloads.client.url).hash(this.downloads.client.sha1).size(this.downloads.client.size)
                    .withHttpClient(httpClient).downloadTo(this.getMinecraftJarLibraryPath());

            if (clientDownload.needToDownload()) {
                progressDialog.setTotalBytes(this.downloads.client.size);
                clientDownload.downloadFile();
            }

            progressDialog.doneTask();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            PerformanceManager.end("Downloading Minecraft");
            PerformanceManager.end();
            return false;
        }
        PerformanceManager.end("Downloading Minecraft");

        // download libraries
        PerformanceManager.start("Downloading Libraries");
        progressDialog.setLabel(GetText.tr("Downloading Libraries"));
        DownloadPool librariesPool = new DownloadPool();

        List<Library> librariesMissingWithNoUrl = this.libraries.stream()
                .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                        && library.downloads.artifact.url != null && library.downloads.artifact.url.isEmpty()
                        && !Files.exists(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path)))
                .collect(Collectors.toList());
        if (!librariesMissingWithNoUrl.isEmpty()) {
            DialogManager.okDialog().setTitle(GetText.tr("Missing Libraries Found"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr(
                                    "This instance cannot be started due to missing libraries that cannot be downloaded.<br/><br/>Please reinstall the instance to create those libraries and be able to start this instance again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return false;
        }

        // get non native libraries otherwise we double up
        this.libraries.stream()
                .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                        && library.downloads.artifact.url != null && !library.downloads.artifact.url.isEmpty()
                        && !library.hasNativeForOS())
                .distinct()
                .map(l -> LWJGLManager.shouldReplaceLWJGL3(this)
                        ? LWJGLManager.getReplacementLWJGL3Library(this, l)
                        : l)
                .forEach(library -> {
                    com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                            .setUrl(library.downloads.artifact.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                            .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                            .withHttpClient(httpClient);

                    librariesPool.add(download);
                });

        this.libraries.stream().filter(Library::hasNativeForOS)
                .map(l -> LWJGLManager.shouldReplaceLWJGL3(this)
                        ? LWJGLManager.getReplacementLWJGL3Library(this, l)
                        : l)
                .forEach(library -> {
                    com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

                    librariesPool.add(new com.atlauncher.network.Download().setUrl(download.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(download.path)).hash(download.sha1)
                            .size(download.size)
                            .withHttpClient(httpClient));
                });

        // legacy forge, so check the libs folder
        if (launcher.loaderVersion != null && launcher.loaderVersion.isForge()
                && Utils.matchVersion(id, "1.5", true, true)) {
            List<FMLLibrary> fmlLibraries = FMLLibrariesConstants.fmlLibraries.get(id);

            if (fmlLibraries != null) {
                fmlLibraries.forEach((library) -> {
                    com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                            .setUrl(String.format("%s/fmllibs/%s", Constants.DOWNLOAD_SERVER, library.name))
                            .downloadTo(FileSystem.LIBRARIES.resolve("fmllib/" + library.name))
                            .copyTo(ROOT.resolve("lib/" + library.name)).hash(library.sha1Hash)
                            .size(library.size).withHttpClient(httpClient);

                    librariesPool.add(download);
                });
            }
        }

        DownloadPool smallLibrariesPool = librariesPool.downsize();

        progressDialog.setTotalBytes(smallLibrariesPool.totalSize());

        smallLibrariesPool.downloadAll();

        progressDialog.doneTask();
        PerformanceManager.end("Downloading Libraries");

        // download Java runtime
        PerformanceManager.start("Java Runtime");
        if (javaVersion != null && Data.JAVA_RUNTIMES != null && Optional
                .ofNullable(launcher.useJavaProvidedByMinecraft).orElse(App.settings.useJavaProvidedByMinecraft)) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeSystemString = JavaRuntimes.getSystem();

            String runtimeToUse = Optional.ofNullable(launcher.javaRuntimeOverride).orElse(javaVersion.component);

            if (runtimesForSystem.containsKey(runtimeToUse)
                    && !runtimesForSystem.get(runtimeToUse).isEmpty()) {
                // #. {0} is the version of Java were downloading
                progressDialog.setLabel(GetText.tr("Downloading Java Runtime {0}",
                        runtimesForSystem.get(runtimeToUse).get(0).version.name));

                JavaRuntime runtimeToDownload = runtimesForSystem.get(runtimeToUse).get(0);

                try {
                    JavaRuntimeManifest javaRuntimeManifest = com.atlauncher.network.Download.build()
                            .setUrl(runtimeToDownload.manifest.url).size(runtimeToDownload.manifest.size)
                            .hash(runtimeToDownload.manifest.sha1).downloadTo(FileSystem.MINECRAFT_RUNTIMES
                                    .resolve(runtimeToUse).resolve("manifest.json"))
                            .asClassWithThrow(JavaRuntimeManifest.class);

                    DownloadPool pool = new DownloadPool();

                    // create root directory
                    Path runtimeSystemDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                            .resolve(runtimeSystemString);
                    Path runtimeDirectory = runtimeSystemDirectory.resolve(runtimeToUse);
                    FileUtils.createDirectory(runtimeDirectory);

                    // create all the directories
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.DIRECTORY) {
                            FileUtils.createDirectory(runtimeDirectory.resolve(key));
                        }
                    });

                    // collect the files we need to download
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.FILE) {
                            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                                    .setUrl(file.downloads.raw.url).downloadTo(runtimeDirectory.resolve(key))
                                    .hash(file.downloads.raw.sha1).size(file.downloads.raw.size)
                                    .executable(file.executable).withHttpClient(httpClient);

                            pool.add(download);
                        }
                    });

                    DownloadPool smallPool = pool.downsize();

                    progressDialog.setTotalBytes(smallPool.totalSize());

                    smallPool.downloadAll();

                    // write out the version file (theres also a .sha1 file created, but we're not
                    // doing that)
                    Files.write(runtimeSystemDirectory.resolve(".version"),
                            runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                    // Files.write(runtimeSystemDirectory.resolve(runtimeToUse
                    // + ".sha1"), runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to download Java runtime", e);
                }
            }
        }
        progressDialog.doneTask();
        PerformanceManager.end("Java Runtime");

        // organise assets
        PerformanceManager.start("Organising Resources 1");
        progressDialog.setLabel(GetText.tr("Organising Resources"));

        AssetIndex index = com.atlauncher.network.Download.build().setUrl(assetIndex.url).hash(assetIndex.sha1)
                .size(assetIndex.size).downloadTo(FileSystem.RESOURCES_INDEXES.resolve(assetIndex.id + ".json"))
                .withHttpClient(httpClient).asClass(AssetIndex.class);

        DownloadPool pool = new DownloadPool();

        index.objects.forEach((key, object) -> {
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);

            com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(url)
                    .downloadTo(FileSystem.RESOURCES_OBJECTS.resolve(filename)).hash(object.hash).size(object.size)
                    .withHttpClient(httpClient);

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        if (!smallPool.isEmpty()) {
            progressDialog.setLabel(GetText.tr("Downloading Resources"));

            progressDialog.setTotalBytes(smallPool.totalSize());

            smallPool.downloadAll();
        }
        PerformanceManager.end("Organising Resources 1");

        // copy resources to instance
        if (index.mapToResources || assetIndex.id.equalsIgnoreCase("legacy")) {
            PerformanceManager.start("Organising Resources 2");
            progressDialog.setLabel(GetText.tr("Organising Resources"));

            index.objects.forEach((key, object) -> {
                String filename = object.hash.substring(0, 2) + "/" + object.hash;

                Path downloadedFile = FileSystem.RESOURCES_OBJECTS.resolve(filename);
                Path assetPath = index.mapToResources ? this.ROOT.resolve("resources/" + key)
                        : FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(key);

                if (!Files.exists(assetPath)) {
                    FileUtils.copyFile(downloadedFile, assetPath, true);
                }
            });
            PerformanceManager.end("Organising Resources 2");
        }

        progressDialog.doneTask();

        progressDialog.setLabel(GetText.tr("Organising Libraries"));

        // extract natives to a temp dir
        PerformanceManager.start("Extracting Natives");
        boolean useSystemGlfw = Optional.ofNullable(launcher.useSystemGlfw).orElse(App.settings.useSystemGlfw);
        boolean useSystemOpenAl = Optional.ofNullable(launcher.useSystemOpenAl).orElse(App.settings.useSystemOpenAl);
        this.libraries.stream().filter(Library::shouldInstall)
                .map(l -> LWJGLManager.shouldReplaceLWJGL3(this)
                        ? LWJGLManager.getReplacementLWJGL3Library(this, l)
                        : l)
                .forEach(library -> {
                    if (library.hasNativeForOS()) {
                        if (library.name.contains("glfw") && useSystemGlfw) {
                            LogManager.warn("useSystemGlfw was enabled, not using glfw natives from Minecraft");
                            return;
                        }

                        if (library.name.contains("openal") && useSystemOpenAl) {
                            LogManager.warn("useSystemOpenAl was enabled, not using openal natives from Minecraft");
                            return;
                        }

                        Path nativePath = FileSystem.LIBRARIES.resolve(library.getNativeDownloadForOS().path);

                        ArchiveUtils.extract(nativePath, nativesTempDir, name -> {
                            if (library.extract != null && library.extract.shouldExclude(name)) {
                                return null;
                            }

                            // keep META-INF folder as per normal
                            if (name.startsWith("META-INF")) {
                                return name;
                            }

                            // don't extract folders
                            if (name.endsWith("/")) {
                                return null;
                            }

                            // if it has a / then extract just to root
                            if (name.contains("/")) {
                                return name.substring(name.lastIndexOf("/") + 1);
                            }

                            return name;
                        });
                    }
                });

        progressDialog.doneTask();
        PerformanceManager.end("Extracting Natives");

        if (LWJGLManager.shouldUseLegacyLWJGL(this)) {
            PerformanceManager.start("Extracting Legacy LWJGL");
            progressDialog.setLabel(GetText.tr("Extracting Legacy LWJGL"));

            LWJGLLibrary library = LWJGLManager.getLegacyLWJGLLibrary();

            if (library != null) {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(library.url)
                        .downloadTo(FileSystem.LIBRARIES.resolve(library.path)).unzipTo(lwjglNativesTempDir)
                        .hash(library.sha1).size(library.size).withHttpClient(httpClient);

                if (download.needToDownload()) {
                    progressDialog.setTotalBytes(library.size);

                    try {
                        download.downloadFile();
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }
                } else {
                    download.runPostProcessors();
                }
            }

            progressDialog.doneTask();
            PerformanceManager.end("Extracting Legacy LWJGL");
        }

        if (usesCustomMinecraftJar()) {
            PerformanceManager.start("Creating custom minecraft.jar");
            progressDialog.setLabel(GetText.tr("Creating custom minecraft.jar"));

            if (Files.exists(getCustomMinecraftJarLibraryPath())) {
                FileUtils.delete(getCustomMinecraftJarLibraryPath());
            }

            if (!Utils.combineJars(getMinecraftJar(), getRoot().resolve("bin/modpack.jar").toFile(),
                    getCustomMinecraftJar())) {
                LogManager.error("Failed to combine jars into custom minecraft.jar");
                PerformanceManager.end("Creating custom minecraft.jar");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Creating custom minecraft.jar");
        }
        progressDialog.doneTask();

        if (App.settings.scanModsOnLaunch) {
            PerformanceManager.start("Scanning mods for Fractureiser");
            progressDialog.setLabel(GetText.tr("Scanning mods for Fractureiser"));

            List<Path> foundInfections = new ArrayList<>();
            try {
                foundInfections = SecurityUtils.scanForFractureiser(this.getModPathsFromFilesystem());
            } catch (InterruptedException e) {
                LogManager.logStackTrace("Failed to scan all mods for Fractureiser", e);
            }
            PerformanceManager.end("Scanning mods for Fractureiser");

            if (!foundInfections.isEmpty()) {
                LogManager.error("Infections have been found in your mods. See the below list of paths");
                foundInfections.forEach(p -> LogManager.error(p.toAbsolutePath().toString()));
                return false;
            }
        }
        progressDialog.doneTask();

        PerformanceManager.end();
        return true;
    }

    public boolean launch() {
        return launch(false);
    }

    public boolean launch(boolean offline) {
        final MicrosoftAccount account = launcher.account == null ? AccountManager.getSelectedAccount()
                : AccountManager.getAccountByName(launcher.account);

        if (account == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Cannot play instance as you have no account selected.")).build())
                    .setType(DialogManager.ERROR).show();

            if (AccountManager.getAccounts().isEmpty()) {
                App.navigate(UIConstants.LAUNCHER_ACCOUNTS_TAB);
            }

            App.launcher.setMinecraftLaunched(false);
            return false;
        }

        // if Microsoft account must login again, then make sure to do that
        if (!offline && account.mustLogin) {
            if (!account.ensureAccountIsLoggedIn()) {
                LogManager.info("You must login to your account before continuing.");
                return false;
            }
        }

        String playerName = account.minecraftUsername;

        if (offline) {
            playerName = DialogManager.okDialog().setTitle(GetText.tr("Offline Player Name"))
                    .setContent(GetText.tr("Choose your offline player name:")).showInput(playerName);

            if (playerName == null || playerName.isEmpty()) {
                LogManager.info("No player name provided for offline launch, so cancelling launch.");
                return false;
            }
        }

        final String username = offline ? playerName : account.minecraftUsername;

        int maximumMemory = Optional.ofNullable(this.launcher.maximumMemory).orElse(App.settings.maximumMemory);
        if ((maximumMemory < this.launcher.requiredMemory)
                && (this.launcher.requiredMemory <= OS.getSafeMaximumRam())) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficient Ram"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This pack has set a minimum amount of ram needed to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                            this.launcher.requiredMemory)).build())
                    .setType(DialogManager.ERROR).show();

            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }
        int permGen = Optional.ofNullable(this.launcher.permGen).orElse(App.settings.metaspace);
        if (permGen < this.launcher.requiredPermGen) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficent Permgen"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This pack has set a minimum amount of permgen to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                            this.launcher.requiredPermGen)).build())
                    .setType(DialogManager.ERROR).show();
            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling permgen warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }

        Path nativesTempDir = FileSystem.TEMP.resolve("natives-" + UUID.randomUUID().toString().replace("-", ""));
        Path lwjglNativesTempDir = FileSystem.TEMP
                .resolve("lwjgl-natives-" + UUID.randomUUID().toString().replace("-", ""));

        try {
            Files.createDirectory(nativesTempDir);
        } catch (IOException e2) {
            LogManager.logStackTrace(e2, false);
        }

        if (LWJGLManager.shouldUseLegacyLWJGL(this)) {
            try {
                Files.createDirectory(lwjglNativesTempDir);
            } catch (IOException e2) {
                LogManager.logStackTrace(e2, false);
            }
        }

        ProgressDialog<Boolean> prepareDialog = new ProgressDialog<>(GetText.tr("Preparing For Launch"),
                7,
                GetText.tr("Preparing For Launch"));
        prepareDialog.addThread(new Thread(() -> {
            LogManager.info("Preparing for launch!");
            prepareDialog.setReturnValue(prepareForLaunch(prepareDialog, nativesTempDir, lwjglNativesTempDir));
            prepareDialog.close();
        }));
        prepareDialog.start();

        if (prepareDialog.getReturnValue() != true) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(this, offline, "prepare_failure"));
            LogManager.error(
                    "Failed to prepare instance " + this.launcher.name + " for launch. Check the logs and try again.");
            return false;
        }

        Thread launcherThread = new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(false);
                }

                LogManager.info(String.format("Launching pack %s %s (%s) for Minecraft %s", this.launcher.pack,
                        this.launcher.version, getPlatformName(), this.id));

                boolean enableCommands = Optional.ofNullable(this.launcher.enableCommands)
                        .orElse(App.settings.enableCommands);
                String preLaunchCommand = Optional.ofNullable(this.launcher.preLaunchCommand)
                        .orElse(App.settings.preLaunchCommand);
                String postExitCommand = Optional.ofNullable(this.launcher.postExitCommand)
                        .orElse(App.settings.postExitCommand);
                String wrapperCommand = Optional.ofNullable(this.launcher.wrapperCommand)
                        .orElse(App.settings.wrapperCommand);
                if (!enableCommands) {
                    wrapperCommand = null;
                }

                if (!offline) {
                    LogManager.info("Logging into Minecraft!");
                    ProgressDialog<Boolean> loginDialog = new ProgressDialog<>(GetText.tr("Logging Into Minecraft"),
                            0, GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
                    loginDialog.addThread(new Thread(() -> {
                        loginDialog.setReturnValue(account.ensureAccessTokenValid());
                        loginDialog.close();
                    }));
                    loginDialog.start();

                    if (loginDialog.getReturnValue() == false) {
                        LogManager.error("Failed to login");
                        Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(this, offline, "microsoft_login_failure"));
                        App.launcher.setMinecraftLaunched(false);
                        if (App.launcher.getParent() != null) {
                            App.launcher.getParent().setVisible(true);
                        }
                        DialogManager.okDialog().setTitle(GetText.tr("Error Logging In"))
                                .setContent(GetText.tr("Couldn't login with Microsoft account"))
                                .setType(DialogManager.ERROR).show();
                        return;
                    }
                }

                if (enableCommands && preLaunchCommand != null) {
                    if (!executeCommand(preLaunchCommand)) {
                        LogManager.error("Failed to execute pre-launch command");

                        Analytics.trackEvent(
                                AnalyticsEvent.forInstanceLaunchFailed(this, offline, "pre_launch_failure"));
                        App.launcher.setMinecraftLaunched(false);

                        if (App.launcher.getParent() != null) {
                            App.launcher.getParent().setVisible(true);
                        }

                        return;
                    }
                }

                Process process = MCLauncher.launch(account, this, nativesTempDir,
                        LWJGLManager.shouldUseLegacyLWJGL(this) ? lwjglNativesTempDir : null,
                        wrapperCommand, username);

                if (process == null) {
                    Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(this, offline, "no_process"));
                    LogManager.error("Failed to get process for Minecraft");
                    App.launcher.setMinecraftLaunched(false);
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(true);
                    }
                    return;
                }

                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunched(this, offline));

                if (this.getPack() != null && this.getPack().isLoggingEnabled() && !this.launcher.isDev
                        && App.settings.enableLogs) {
                    App.TASKPOOL.execute(() -> addPlay(this.launcher.version));
                }

                if ((App.autoLaunch != null && App.closeLauncher)
                        || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                    Analytics.endSession();
                    System.exit(0);
                }

                App.launcher.showKillMinecraft(process);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(isr);
                String line;
                int detectedError = 0;
                boolean crashedWithoutKnownResolution = false;

                String replaceUUID = account.uuid.replace("-", "");

                while ((line = br.readLine()) != null) {
                    if (line.contains("java.lang.OutOfMemoryError")
                            || line.contains("There is insufficient memory for the Java Runtime Environment")) {
                        detectedError = MinecraftError.OUT_OF_MEMORY;
                    }

                    if (line.contains("java.util.ConcurrentModificationException")
                            && Utils.matchVersion(this.id, "1.6", true, true)) {
                        detectedError = MinecraftError.CONCURRENT_MODIFICATION_ERROR_1_6;
                    }

                    if (line.contains(
                            "has been compiled by a more recent version of the Java Runtime (class file version 60.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_16_OR_NEWER;
                    }

                    if (line.contains(
                            "has been compiled by a more recent version of the Java Runtime (class file version 61.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_17_OR_NEWER;
                    }

                    if (line.contains(
                            "class jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class")) {
                        detectedError = MinecraftError.USING_NEWER_JAVA_THAN_8;
                    }

                    if (line.contains("Crash report saved to") || line.contains("Minecraft Crash Report")) {
                        crashedWithoutKnownResolution = true;
                    }

                    if (!LogManager.showDebug) {
                        line = line.replaceAll(account.minecraftUsername, "**MINECRAFTUSERNAME**");
                        line = line.replaceAll(account.username, "**MINECRAFTUSERNAME**");
                        line = line.replaceAll(account.uuid, "**UUID**");
                        line = line.replaceAll(replaceUUID, "**UUID**");
                        line = line.replaceAll("\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b", "**IPADDRESS**");
                    }

                    if (account.getAccessToken() != null) {
                        line = line.replaceAll(account.getAccessToken(), "**ACCESSTOKEN**");
                    }

                    if (line.contains("log4j:")) {
                        try {
                            // start of a new event so clear string builder
                            if (line.contains("<log4j:Event>")) {
                                sb.setLength(0);
                            }

                            sb.append(line);

                            // end of the xml object so parse it
                            if (line.contains("</log4j:Event>")) {
                                LogManager.minecraftLog4j(sb.toString());
                                sb.setLength(0);
                            }

                            continue;
                        } catch (Exception e) {
                            // ignored
                        }
                    }

                    LogManager.minecraft(line);
                }
                App.launcher.hideKillMinecraft();
                if (App.launcher.getParent() != null && App.settings.keepLauncherOpen) {
                    App.launcher.getParent().setVisible(true);
                }
                long end = System.currentTimeMillis();
                int exitValue = 0; // Assume we exited fine
                try {
                    exitValue = process.exitValue(); // Try to get the real exit value
                } catch (IllegalThreadStateException e) {
                    process.destroy(); // Kill the process
                }
                if (!App.settings.keepLauncherOpen) {
                    App.console.setVisible(false); // Hide the console to pretend we've closed
                }

                if (exitValue != 0 || crashedWithoutKnownResolution) {
                    App.launcher.setLastInstanceCrash(this);

                    LogManager.error(
                            "Oh no. Minecraft crashed. Please check the logs for any errors and provide these logs when asking for support.");

                    if (hasDisabledJavaRuntime()) {
                        LogManager.warn(
                                "The Use Java Provided By Minecraft option has been disabled. Please enable this option again.");
                    }

                    if (this.getDiscordInviteUrl() != null) {
                        LogManager.error(String.format(
                                "If you're having issues, please visit the Discord server for the modpack at %s",
                                this.getDiscordInviteUrl()));
                    }
                }

                if (detectedError != 0) {
                    MinecraftError.showInformationPopup(detectedError);
                }

                if (enableCommands && postExitCommand != null) {
                    if (!executeCommand(postExitCommand)) {
                        LogManager.error("Failed to execute post-exit command");
                    }
                }

                App.launcher.setMinecraftLaunched(false);
                final int timePlayed = (int) (end - start) / 1000;
                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchCompleted(this, offline, timePlayed));
                if (this.getPack() != null && this.getPack().isLoggingEnabled() && !this.launcher.isDev
                        && App.settings.enableLogs) {
                    if (timePlayed > 0) {
                        App.TASKPOOL.submit(() -> addTimePlayed(timePlayed, this.launcher.version));
                    }
                }
                if (App.settings.enableAutomaticBackupAfterLaunch) {
                    backup();
                }
                if (App.settings.keepLauncherOpen) {
                    App.launcher.updateData();
                }
                if (Files.isDirectory(nativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(nativesTempDir);
                }
                if (Files.isDirectory(lwjglNativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(lwjglNativesTempDir);
                }
                if (usesCustomMinecraftJar() && Files.exists(getCustomMinecraftJarLibraryPath())) {
                    FileUtils.delete(getCustomMinecraftJarLibraryPath());
                }
                if (!App.settings.keepLauncherOpen) {
                    Analytics.endSession();
                    System.exit(0);
                }
            } catch (Exception e1) {
                LogManager.logStackTrace(e1);
                Analytics.trackEvent(AnalyticsEvent.forInstanceLaunchFailed(this, offline, "exception"));
                App.launcher.setMinecraftLaunched(false);
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(true);
                }
            }
        });

        this.setLastPlayed(Instant.now());
        this.incrementNumberOfPlays();
        this.save();

        launcherThread.start();
        return true;
    }

    private boolean executeCommand(String command) {
        try {
            CommandExecutor.executeCommand(this, command);
            return true;
        } catch (CommandException e) {
            String content = GetText.tr("Error executing command");

            if (e.getMessage() != null) {
                content += ":" + System.lineSeparator() + e.getLocalizedMessage();
            }

            content += System.lineSeparator() + GetText.tr("Check the console for details");

            DialogManager.okDialog().setTitle(GetText.tr("Error executing command")).setContent(content)
                    .setType(DialogManager.ERROR).show();

            return false;
        }
    }

    public void addPlay(String version) {
        GraphqlClient
                .mutateAndWait(
                        new AddPackActionMutation(AddPackActionInput.builder().packId(Integer.toString(
                                this.getPack().id))
                                .version(version).action(PackLogAction.PLAY).build()));
    }

    public void addTimePlayed(int time, String version) {
        GraphqlClient
                .mutateAndWait(
                        new AddPackTimePlayedMutation(AddPackTimePlayedInput.builder().packId(Integer.toString(
                                this.getPack().id)).version(version).time(time).build()));
    }

    public DisableableMod getDisableableModByCurseModId(int curseModId) {
        return this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromCurseForge() && installedMod.getCurseForgeModId() == curseModId)
                .findFirst().orElse(null);
    }

    public boolean hasCustomMods() {
        return this.launcher.mods.stream().anyMatch(DisableableMod::isUserAdded);
    }

    public List<String> getCustomMods(Type type) {
        return this.launcher.mods.stream().filter(DisableableMod::isUserAdded).filter(m -> m.getType() == type)
                .map(DisableableMod::getFilename).collect(Collectors.toList());
    }

    public List<String> getPackMods(Type type) {
        return this.launcher.mods.stream().filter(dm -> !dm.userAdded && dm.type == type)
                .map(DisableableMod::getFilename).collect(Collectors.toList());
    }

    public List<DisableableMod> getCustomDisableableMods() {
        return this.launcher.mods.stream().filter(DisableableMod::isUserAdded).collect(Collectors.toList());
    }

    public boolean wasModInstalled(String name) {
        if (this.launcher.mods != null) {
            for (DisableableMod mod : this.launcher.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean wasModSelected(String name) {
        if (this.launcher.mods != null) {
            for (DisableableMod mod : this.launcher.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return mod.wasSelected();
                }
            }
        }
        return false;
    }

    public boolean canBeExported() {
        if (launcher.loaderVersion == null) {
            new Thread(() -> LogManager.debug("Instance " + launcher.name + " cannot be exported due to: No loader"))
                    .start();
            return false;
        }

        return true;
    }

    public Pair<Path, String> export(String name, String version, String author, InstanceExportFormat format,
            String saveTo, List<String> overrides) {
        try {
            Path saveToPath = Paths.get(saveTo);
            if (!Files.isDirectory(saveToPath)) {
                Files.createDirectories(saveToPath);
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to create export directory", e);
            return new Pair<>(null, null);
        }

        if (format == InstanceExportFormat.CURSEFORGE) {
            return exportAsCurseForgeZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MODRINTH) {
            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.CURSEFORGE_AND_MODRINTH) {
            if (exportAsCurseForgeZip(name, version, author, saveTo, overrides).left() == null) {
                return new Pair<>(null, null);
            }

            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MULTIMC) {
            return exportAsMultiMcZip(name, version, author, saveTo, overrides);
        }

        return new Pair<>(null, null);
    }

    public Pair<Path, String> exportAsMultiMcZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(safePathName + ".zip");
        MultiMCManifest manifest = new MultiMCManifest();

        manifest.formatVersion = 1;

        manifest.components = new ArrayList<>();

        Optional<Library> lwjgl3Version = libraries.stream().filter(l -> l.name.contains("org.lwjgl:lwjgl:"))
                .findFirst();

        // minecraft
        MultiMCComponent minecraftComponent = new MultiMCComponent();
        minecraftComponent.cachedName = "Minecraft";
        minecraftComponent.important = true;
        minecraftComponent.cachedVersion = id;
        minecraftComponent.uid = "net.minecraft";
        minecraftComponent.version = id;

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
        if (launcher.loaderVersion.isFabric() || launcher.loaderVersion.isLegacyFabric()) {
            // mappings
            MultiMCComponent fabricMappingsComponent = new MultiMCComponent();
            fabricMappingsComponent.cachedName = "Intermediary Mappings";

            fabricMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = id;
            minecraftRequire.uid = "net.minecraft";
            fabricMappingsComponent.cachedRequires.add(minecraftRequire);

            fabricMappingsComponent.cachedVersion = id;
            fabricMappingsComponent.cachedVolatile = true;
            fabricMappingsComponent.dependencyOnly = true;
            fabricMappingsComponent.uid = "net.fabricmc.intermediary";
            fabricMappingsComponent.version = id;
            manifest.components.add(fabricMappingsComponent);

            // loader
            MultiMCComponent fabricLoaderComponent = new MultiMCComponent();
            fabricLoaderComponent.cachedName = "Fabric Loader";

            fabricLoaderComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire intermediaryRequire = new MultiMCRequire();
            intermediaryRequire.uid = "net.fabricmc.intermediary";
            fabricLoaderComponent.cachedRequires.add(intermediaryRequire);

            fabricLoaderComponent.cachedVersion = launcher.loaderVersion.version;
            fabricLoaderComponent.uid = "net.fabricmc.fabric-loader";
            fabricLoaderComponent.version = launcher.loaderVersion.version;
            manifest.components.add(fabricLoaderComponent);
        }

        // forge loader
        if (launcher.loaderVersion.isNeoForge()) {
            // loader
            MultiMCComponent forgeMappingsComponent = new MultiMCComponent();
            forgeMappingsComponent.cachedName = "Forge";

            forgeMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = id;
            minecraftRequire.uid = "net.minecraft";
            forgeMappingsComponent.cachedRequires.add(minecraftRequire);

            forgeMappingsComponent.cachedVersion = launcher.loaderVersion.version;
            forgeMappingsComponent.uid = "net.neoforged";
            forgeMappingsComponent.version = launcher.loaderVersion.version;
            manifest.components.add(forgeMappingsComponent);
        }

        // forge loader
        if (launcher.loaderVersion.isForge()) {
            // loader
            MultiMCComponent forgeMappingsComponent = new MultiMCComponent();
            forgeMappingsComponent.cachedName = "Forge";

            forgeMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = id;
            minecraftRequire.uid = "net.minecraft";
            forgeMappingsComponent.cachedRequires.add(minecraftRequire);

            forgeMappingsComponent.cachedVersion = launcher.loaderVersion.version;
            forgeMappingsComponent.uid = "net.minecraftforge";
            forgeMappingsComponent.version = launcher.loaderVersion.version;
            manifest.components.add(forgeMappingsComponent);
        }

        // quilt loader
        if (launcher.loaderVersion.isQuilt()) {
            String hashedName = "org.quiltmc.hashed";
            String cachedName = "Hashed Mappings";
            if (!ConfigManager.getConfigItem("loaders.quilt.switchHashedForIntermediary", true)) {
                hashedName = "net.fabricmc.intermediary";
                cachedName = "Intermediary Mappings";
            }

            // mappings
            MultiMCComponent quiltMappingsComponent = new MultiMCComponent();
            quiltMappingsComponent.cachedName = cachedName;

            quiltMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = id;
            minecraftRequire.uid = "net.minecraft";
            quiltMappingsComponent.cachedRequires.add(minecraftRequire);

            quiltMappingsComponent.cachedVersion = id;
            quiltMappingsComponent.cachedVolatile = true;
            quiltMappingsComponent.dependencyOnly = true;
            quiltMappingsComponent.uid = hashedName;
            quiltMappingsComponent.version = id;
            manifest.components.add(quiltMappingsComponent);

            // loader
            MultiMCComponent quiltLoaderComponent = new MultiMCComponent();
            quiltLoaderComponent.cachedName = "Quilt Loader";

            quiltLoaderComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire hashedRequire = new MultiMCRequire();
            hashedRequire.uid = hashedName;
            quiltLoaderComponent.cachedRequires.add(hashedRequire);

            quiltLoaderComponent.cachedVersion = launcher.loaderVersion.version;
            quiltLoaderComponent.uid = "org.quiltmc.quilt-loader";
            quiltLoaderComponent.version = launcher.loaderVersion.version;
            manifest.components.add(quiltLoaderComponent);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create mmc-pack.json
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                Files.newOutputStream(tempDir.resolve("mmc-pack.json")), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<>(null, null);
        }

        // if Legacy Fabric, add patch in
        if (launcher.loaderVersion.type.equals("LegacyFabric")) {
            FileUtils.createDirectory(tempDir.resolve("patches"));

            JsonObject patch = new JsonObject();
            patch.addProperty("formatVersion", 1);
            patch.addProperty("name", "Intermediary Mappings");
            patch.addProperty("uid", "net.fabricmc.intermediary");
            patch.addProperty("version", id);

            JsonArray plusLibraries = new JsonArray();
            JsonObject intermediary = new JsonObject();
            intermediary.addProperty("name", String.format("net.fabricmc:intermediary:%s", id));
            intermediary.addProperty("url", Constants.LEGACY_FABRIC_MAVEN);
            plusLibraries.add(intermediary);
            patch.add("+libraries", plusLibraries);

            // create net.fabricmc.intermediary.json
            try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                    Files.newOutputStream(tempDir.resolve("net.fabricmc.intermediary.json")),
                    StandardCharsets.UTF_8)) {
                Gsons.DEFAULT.toJson(patch, fileWriter);
            } catch (JsonIOException | IOException e) {
                LogManager.logStackTrace("Failed to save net.fabricmc.intermediary.json", e);

                FileUtils.deleteDirectory(tempDir);

                return new Pair<>(null, null);
            }

        }

        // create instance.cfg
        Path instanceCfgPath = tempDir.resolve("instance.cfg");
        Properties instanceCfg = new Properties();

        String iconKey = "default";
        if (hasCustomImage()) {
            String customIconFileName = "atlauncher_" + getSafeName().toLowerCase(Locale.ENGLISH);
            Path customIconPath = tempDir.resolve(customIconFileName + ".png");

            FileUtils.copyFile(this.getRoot().resolve("instance.png"), customIconPath, true);

            iconKey = customIconFileName;
        }

        instanceCfg.setProperty("AutoCloseConsole", "false");
        instanceCfg.setProperty("ForgeVersion", "false");
        instanceCfg.setProperty("InstanceType", "OneSix");
        instanceCfg.setProperty("IntendedVersion", "");
        instanceCfg.setProperty("JavaPath", Optional.ofNullable(launcher.javaPath).orElse(App.settings.javaPath)
                + File.separator + "bin" + File.separator + (OS.isWindows() ? "javaw.exe" : "java"));
        instanceCfg.setProperty("JVMArgs", Optional.ofNullable(launcher.javaArguments).orElse(""));
        instanceCfg.setProperty("LWJGLVersion", "");
        instanceCfg.setProperty("LaunchMaximized", "false");
        instanceCfg.setProperty("LiteloaderVersion", "");
        instanceCfg.setProperty("LogPrePostOutput", "true");
        instanceCfg.setProperty("MCLaunchMethod", "LauncherPart");
        instanceCfg.setProperty("MaxMemAlloc",
                Optional.ofNullable(launcher.maximumMemory).orElse(App.settings.maximumMemory) + "");
        instanceCfg.setProperty("MinecraftWinHeight", App.settings.windowHeight + "");
        instanceCfg.setProperty("MinecraftWinWidth", App.settings.windowWidth + "");
        instanceCfg.setProperty("OverrideCommands",
                launcher.postExitCommand != null || launcher.preLaunchCommand != null || launcher.wrapperCommand != null
                        ? "true"
                        : "false");
        instanceCfg.setProperty("OverrideConsole", "false");
        instanceCfg.setProperty("OverrideJava", launcher.javaPath == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaArgs", launcher.javaArguments == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaLocation", "false");
        instanceCfg.setProperty("OverrideMCLaunchMethod", "false");
        instanceCfg.setProperty("OverrideMemory", launcher.maximumMemory == null ? "false" : "true");
        instanceCfg.setProperty("OverrideNativeWorkarounds", "false");
        instanceCfg.setProperty("OverrideWindow", "false");
        instanceCfg.setProperty("PermGen", Optional.ofNullable(launcher.permGen).orElse(App.settings.metaspace) + "");
        instanceCfg.setProperty("PostExitCommand",
                Optional.ofNullable(launcher.postExitCommand).orElse(App.settings.postExitCommand) + "");
        instanceCfg.setProperty("PreLaunchCommand",
                Optional.ofNullable(launcher.preLaunchCommand).orElse(App.settings.preLaunchCommand) + "");
        instanceCfg.setProperty("ShowConsole", "false");
        instanceCfg.setProperty("ShowConsoleOnError", "true");
        instanceCfg.setProperty("UseNativeGLFW", "false");
        instanceCfg.setProperty("UseNativeOpenAL", "false");
        instanceCfg.setProperty("WrapperCommand",
                Optional.ofNullable(launcher.wrapperCommand).orElse(App.settings.wrapperCommand) + "");
        instanceCfg.setProperty("iconKey", iconKey);
        instanceCfg.setProperty("name", launcher.name);
        instanceCfg.setProperty("lastLaunchTime", "");
        instanceCfg.setProperty("notes", "");
        instanceCfg.setProperty("totalTimePlayed", "0");

        try (OutputStream outputStream = Files.newOutputStream(instanceCfgPath)) {
            instanceCfg.store(outputStream, "Exported by ATLauncher");
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<>(null, null);
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
            if (!path.equalsIgnoreCase(safePathName + ".zip") && getRoot().resolve(path).toFile().exists()
                    && (getRoot().resolve(path).toFile().isFile()
                            || getRoot().resolve(path).toFile().list().length != 0)) {
                if (getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(dotMinecraftPath)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                    .forEach(f -> FileUtils.delete(f, false));
        } catch (IOException ignored) {
            // ignored
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<>(to, null);
    }

    public Pair<Path, String> exportAsCurseForgeZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.zip", safePathName, version));
        CurseForgeManifest manifest = new CurseForgeManifest();

        // for any mods not from CurseForge, scan for them on CurseForge
        Map<Long, DisableableMod> murmurHashes = new HashMap<>();

        this.launcher.mods.stream()
                .filter(m -> !m.disabled && m.type != com.atlauncher.data.Type.worlds)
                .forEach(dm -> {
                    try {
                        long hash = Hashing.murmur(dm.getFile(this.ROOT, this.id).toPath());
                        murmurHashes.put(hash, dm);
                    } catch (IOException e) {
                        LogManager.logStackTrace(e);
                    }
                });

        if (!murmurHashes.isEmpty()) {
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

                                    CurseForgeProject curseForgeProject = foundProjects
                                            .get(foundMod.id);

                                    if (curseForgeProject != null && curseForgeProject.status == 4) {
                                        dm.curseForgeProjectId = foundMod.id;
                                        dm.curseForgeFile = foundMod.file;
                                        dm.curseForgeFileId = foundMod.file.id;
                                        dm.curseForgeProject = curseForgeProject;

                                        LogManager.debug("Found matching mod from CurseForge called "
                                                + dm.curseForgeFile.displayName);
                                    }
                                });
                    }
                }
            }
        }
        this.save();

        CurseForgeMinecraft minecraft = new CurseForgeMinecraft();

        List<CurseForgeModLoader> modLoaders = new ArrayList<>();
        CurseForgeModLoader modLoader = new CurseForgeModLoader();

        String loaderType = launcher.loaderVersion.type.toLowerCase(Locale.ENGLISH);
        String loaderVersion = launcher.loaderVersion.version;

        modLoader.id = loaderType + "-" + loaderVersion;
        modLoader.primary = true;
        modLoaders.add(modLoader);

        minecraft.version = this.id;
        minecraft.modLoaders = modLoaders;

        manifest.minecraft = minecraft;
        manifest.manifestType = "minecraftModpack";
        manifest.manifestVersion = 1;
        manifest.name = name;
        manifest.version = version;
        manifest.author = author;
        manifest.files = this.launcher.mods.stream()
                .filter(m -> !m.disabled && m.isFromCurseForge() && m.hasFullCurseForgeInformation()
                        && m.type != com.atlauncher.data.Type.worlds)
                .filter(mod -> overrides.stream()
                        .anyMatch(path -> getRoot().relativize(mod.getPath(this)).startsWith(path)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Set<Integer> seenFileIds = new HashSet<>();
                            return list.stream()
                                    .filter(mod -> seenFileIds.add(mod.curseForgeFileId))
                                    // #875 - Non available mods/files will be rejected by CurseForge
                                    .filter(mod -> mod.curseForgeFile.isAvailable)
                                    .map(mod -> {
                                        CurseForgeManifestFile file = new CurseForgeManifestFile();
                                        file.projectID = mod.curseForgeProjectId;
                                        file.fileID = mod.curseForgeFileId;
                                        file.required = true;
                                        return file;
                                    })
                                    .collect(Collectors.toList());
                        }));
        manifest.overrides = "overrides";

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create manifest.json
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                Files.newOutputStream(tempDir.resolve("manifest.json")), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save manifest.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<>(null, null);
        }

        // create modlist.html
        StringBuilder sb = new StringBuilder("<ul>");
        this.launcher.mods.stream()
                .filter(m -> !m.disabled && m.isFromCurseForge() && m.hasFullCurseForgeInformation()
                        && m.type != com.atlauncher.data.Type.worlds)
                // #875 - Non available mods/files will be rejected by CurseForge
                .filter(mod -> mod.curseForgeFile.isAvailable)
                .filter(mod -> overrides.stream()
                        .anyMatch(path -> getRoot().relativize(mod.getPath(this)).startsWith(path)))
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
                Files.newOutputStream(tempDir.resolve("modlist.html")), StandardCharsets.UTF_8)) {
            fileWriter.write(sb.toString());
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modlist.html", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<>(null, null);
        }

        // copy over the overrides folder
        Path overridesPath = tempDir.resolve("overrides");
        FileUtils.createDirectory(overridesPath);

        for (String path : overrides) {
            if (!path.equalsIgnoreCase(safePathName + ".zip") && getRoot().resolve(path).toFile().exists()
                    && (getRoot().resolve(path).toFile().isFile()
                            || getRoot().resolve(path).toFile().list().length != 0)) {
                if (getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(overridesPath)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                    .forEach(f -> FileUtils.delete(f, false));
        } catch (IOException ignored) {
            // ignored
        }

        // log files that are not available on CurseForge anymore and put in overrides
        launcher.mods.stream()
                .filter(m -> !m.disabled && m.isFromCurseForge() && m
                        .hasFullCurseForgeInformation())
                .filter(mod -> !mod.curseForgeFile.isAvailable)
                .forEach(mod -> LogManager.warn(String.format(
                        "File %s is no longer available according to the CurseForge api, so putting it in overrides",
                        mod.file)));

        // remove files that come from CurseForge or aren't disabled
        launcher.mods.stream()
                .filter(m -> !m.disabled && m.isFromCurseForge() && m.hasFullCurseForgeInformation()
                        && m.type != com.atlauncher.data.Type.worlds)
                // #875 - Non available mods/files will be rejected by CurseForge
                .filter(mod -> mod.curseForgeFile.isAvailable)
                .forEach(mod -> {
                    File file = mod.getFile(this, overridesPath);

                    if (file != null && file.exists()) {
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

        return new Pair<>(to, null);
    }

    public Pair<Path, String> exportAsModrinthZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.mrpack", safePathName, version));
        ModrinthModpackManifest manifest = new ModrinthModpackManifest();

        // for any mods not from Modrinth, scan for them on Modrinth
        List<DisableableMod> nonModrinthMods = this.launcher.mods.parallelStream()
                .filter(m -> !m.disabled && !m.isFromModrinth() && m.getFile(this).exists())
                .collect(Collectors.toList());

        String[] sha1Hashes = nonModrinthMods.parallelStream()
                .map(m -> Hashing.sha1(m.getFile(this).toPath()).toString()).toArray(String[]::new);

        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi.getVersionsFromSha1Hashes(sha1Hashes);

        if (!modrinthVersions.isEmpty()) {
            Map<String, ModrinthProject> modrinthProjects = ModrinthApi.getProjectsAsMap(
                    modrinthVersions.values().parallelStream().map(mv -> mv.projectId).toArray(String[]::new));

            nonModrinthMods.parallelStream().forEach(mod -> {
                String hash = Hashing.sha1(mod.getFile(this).toPath()).toString();

                if (modrinthVersions.containsKey(hash)) {
                    ModrinthVersion modrinthVersion = modrinthVersions.get(hash);

                    mod.modrinthVersion = modrinthVersion;

                    LogManager.debug("Found matching version from Modrinth called " + mod.modrinthVersion.name);

                    if (modrinthProjects.containsKey(modrinthVersions.get(hash).projectId)) {
                        mod.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);
                    }
                }
            });
            this.save();
        }

        manifest.formatVersion = 1;
        manifest.game = "minecraft";
        manifest.versionId = version;
        manifest.name = name;
        manifest.summary = this.launcher.description;
        manifest.files = this.launcher.mods.parallelStream()
                .filter(m -> !m.disabled && m.modrinthVersion != null && m.getFile(this).exists())
                .filter(mod -> overrides.stream()
                        .anyMatch(path -> getRoot().relativize(mod.getPath(this)).startsWith(path)))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        list -> {
                            Set<String> seenFileIds = new HashSet<>();
                            return list.stream()
                                    .filter(mod -> seenFileIds.add(mod.modrinthVersion.id))
                                    .map(mod -> {
                                        Path modPath = mod.getFile(this).toPath();

                                        ModrinthModpackFile file = new ModrinthModpackFile();
                                        file.path = this.ROOT.relativize(modPath).toString().replace("\\", "/");

                                        String sha1Hash = Hashing.sha1(modPath).toString();

                                        file.hashes = new HashMap<>();
                                        file.hashes.put("sha1", sha1Hash);
                                        file.hashes.put("sha512", Hashing.sha512(modPath).toString());

                                        file.env = new HashMap<>();
                                        // mods are always required on the client ALWAYS ALWAYS ALWAYS (for now)
                                        file.env.put("client", "required");
                                        file.env.put("server", "required");

                                        if (mod.modrinthProject != null
                                                && mod.modrinthProject.serverSide == ModrinthSide.UNSUPPORTED) {
                                            file.env.put("server", "unsupported");
                                        }

                                        file.fileSize = modPath.toFile().length();

                                        file.downloads = new ArrayList<>();
                                        file.downloads.add(HttpUrl.get(mod.modrinthVersion.getFileBySha1(sha1Hash).url)
                                                .toString());

                                        return file;
                                    })
                                    .collect(Collectors.toList());
                        }));
        manifest.dependencies = new HashMap<>();

        manifest.dependencies.put("minecraft", this.id);

        if (this.launcher.loaderVersion != null) {
            manifest.dependencies.put(this.launcher.loaderVersion.getTypeForModrinthExport(),
                    this.launcher.loaderVersion.version);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.getSafeName() + "-export");
        FileUtils.createDirectory(tempDir);

        // create modrinth.index.json
        try (FileOutputStream fos = new FileOutputStream(tempDir.resolve("modrinth.index.json").toFile());
                OutputStreamWriter osw = new OutputStreamWriter(fos,
                        StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, osw);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modrinth.index.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<>(null, null);
        }

        // copy over the overrides folder
        Path overridesPath = tempDir.resolve("overrides");
        FileUtils.createDirectory(overridesPath);

        for (String path : overrides) {
            if (!path.equalsIgnoreCase(safePathName + ".zip") && getRoot().resolve(path).toFile().exists()
                    && (getRoot().resolve(path).toFile().isFile()
                            || getRoot().resolve(path).toFile().list().length != 0)) {
                if (getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(getRoot().resolve(path).toFile(), overridesPath.resolve(path).toFile(), true);
                }
            }
        }

        // remove any .DS_Store files
        try (Stream<Path> walk = Files.walk(overridesPath)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(".DS_Store"))
                    .forEach(f -> FileUtils.delete(f, false));
        } catch (IOException ignored) {
            // ignored
        }

        // remove files that come from Modrinth or aren't disabled
        launcher.mods.stream().filter(m -> !m.disabled && m.modrinthVersion != null).forEach(mod -> {
            File file = mod.getFile(this, overridesPath);

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
                    .forEach(f -> overridesForPermissions.append(String.format("%s\n", tempDir.relativize(f))));
        } catch (IOException ignored) {
            // ignored
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<>(to, overridesForPermissions.toString());
    }

    public boolean rename(String newName) {
        String oldName = this.launcher.name;
        File oldDir = getRoot().toFile();
        this.launcher.name = newName;
        this.ROOT = FileSystem.INSTANCES.resolve(this.getSafeName());
        File newDir = getRoot().toFile();
        if (oldDir.renameTo(newDir)) {
            this.save();
            InstanceManager.updateInstance(this);
            return true;
        } else {
            this.launcher.name = oldName;
            return false;
        }
    }

    @Override
    public void save() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
                Files.newOutputStream(this.getRoot().resolve("instance.json")), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(this, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public File getAssetsDir() {
        if (this.launcher.assetsMapToResources) {
            return this.getRoot().resolve("resources").toFile();
        }

        return FileSystem.RESOURCES_VIRTUAL.resolve(this.assets).toFile();
    }

    public File getRootDirectory() {
        return getRoot().toFile();
    }

    public File getJarModsDirectory() {
        return getRoot().resolve("jarmods").toFile();
    }

    public File getBinDirectory() {
        return getRoot().resolve("bin").toFile();
    }

    public File getNativesDirectory() {
        return getRoot().resolve("bin/natives").toFile();
    }

    public File getMinecraftJar() {
        return getMinecraftJarLibraryPath().toFile();
    }

    public File getCustomMinecraftJar() {
        return getCustomMinecraftJarLibraryPath().toFile();
    }

    @Override
    public String getName() {
        return launcher.name;
    }

    public String getPackName() {
        return launcher.pack;
    }

    public String getVersion() {
        return launcher.version;
    }

    @Override
    public LoaderVersion getLoaderVersion() {
        return launcher.loaderVersion;
    }

    public long incrementNumberOfPlays() {
        if (this.launcher.numPlays == null) {
            this.launcher.numPlays = 0l;
        }

        return this.launcher.numPlays++;
    }

    public long getNumberOfPlays() {
        if (this.launcher.numPlays == null) {
            this.launcher.numPlays = 0l;
        }

        return this.launcher.numPlays;
    }

    public void setLastPlayed(final Instant ts) {
        this.launcher.lastPlayed = ts;
    }

    public Instant getLastPlayedOrEpoch() {
        return this.launcher.lastPlayed != null ? this.launcher.lastPlayed : Instant.EPOCH;
    }

    public String getMainClass() {
        return mainClass;
    }

    @Override
    public String getMinecraftVersion() {
        return id;
    }

    public String getAssets() {
        return assets;
    }

    public int getMemory() {
        return launcher.requiredMemory;
    }

    public int getPermGen() {
        return launcher.requiredPermGen;
    }

    public boolean isOldCurseForgePack() {
        return launcher.curseForgeManifest != null;
    }

    public boolean isCurseForgePack() {
        return launcher.curseForgeProject != null && launcher.curseForgeFile != null;
    }

    public boolean isModrinthImport() {
        return launcher.modrinthManifest != null && launcher.modrinthProject == null;
    }

    public boolean isMultiMcImport() {
        return launcher.multiMCManifest != null;
    }

    public boolean isFTBPack() {
        return launcher.ftbPackManifest != null && launcher.ftbPackVersionManifest != null;
    }

    public boolean isModrinthPack() {
        return launcher.modrinthManifest != null && launcher.modrinthProject != null
                && launcher.modrinthVersion != null;
    }

    public boolean isTechnicPack() {
        return launcher.technicModpack != null;
    }

    public boolean isTechnicSolderPack() {
        return launcher.technicModpack != null && launcher.technicModpack.solder != null;
    }

    public boolean isVanillaInstance() {
        return launcher.vanillaInstance;
    }

    public boolean isExternalPack() {
        return isOldCurseForgePack() || isCurseForgePack() || isFTBPack() || isModrinthImport()
                || isMultiMcImport() || isTechnicPack() || isModrinthPack();
    }

    public boolean isUpdatableExternalPack() {
        return isExternalPack() && ((isCurseForgePack()
                && ConfigManager.getConfigItem("platforms.curseforge.modpacksEnabled", true))
                || (isFTBPack()
                        && ConfigManager.getConfigItem("platforms.ftb.modpacksEnabled", true))
                || (isTechnicPack() && ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true))
                || (isModrinthPack()
                        && ConfigManager.getConfigItem("platforms.modrinth.modpacksEnabled", true)));
    }

    public String getPlatformName() {
        if (isCurseForgePack()) {
            return "CurseForge";
        }

        if (isFTBPack()) {
            return "FTB";
        }

        if (isTechnicSolderPack()) {
            return "TechnicSolder";
        }

        if (isTechnicPack()) {
            return "Technic";
        }

        if (isModrinthPack()) {
            return "Modrinth";
        }

        if (isModrinthImport()) {
            return "ModrinthImport";
        }

        if (isMultiMcImport()) {
            return "MultiMcImport";
        }

        if (isVanillaInstance()) {
            return "Vanilla";
        }

        return "ATLauncher";
    }

    public String getAnalyticsCategory() {
        if (isCurseForgePack()) {
            return "CurseForgeInstance";
        }

        if (isFTBPack()) {
            return "FTBInstance";
        }

        if (isTechnicSolderPack()) {
            return "TechnicSolderInstance";
        }

        if (isTechnicPack()) {
            return "TechnicInstance";
        }

        if (isModrinthPack()) {
            return "ModrinthPack";
        }

        if (isModrinthImport()) {
            return "ModrinthImport";
        }

        if (isMultiMcImport()) {
            return "MultiMcImport";
        }

        if (isVanillaInstance()) {
            return "VanillaInstance";
        }

        return "Instance";
    }

    public void update() {
        InstanceInstallerDialog instanceInstallerDialog = new InstanceInstallerDialog(this, true, false, null, true,
                null, App.launcher.getParent(), null);
        instanceInstallerDialog.setVisible(true);
    }

    public boolean hasCurseForgeProjectId() {
        if (launcher.curseForgeManifest != null) {
            return launcher.curseForgeManifest.projectID != null;
        }

        return launcher.curseForgeProject != null;
    }

    public boolean isUpdatable() {
        if (launcher.vanillaInstance) {
            return true;
        }

        if (isExternalPack()) {
            return isUpdatableExternalPack();
        }

        return launcher.packId != 0 && getPack() != null;
    }

    public void backup() {
        backup(App.settings.backupMode);
    }

    public void backup(BackupMode backupMode) {
        // #. {0} is the name of the instance
        final JDialog dialog = new JDialog(App.launcher.getParent(), GetText.tr("Backing Up {0}", launcher.name),
                ModalityType.DOCUMENT_MODAL);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        // #. {0} is the name of the instance
        JLabel doing = new JLabel(GetText.tr("Backing Up {0}", launcher.name));
        doing.setHorizontalAlignment(JLabel.CENTER);
        doing.setVerticalAlignment(JLabel.TOP);
        topPanel.add(doing);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);

        dialog.add(topPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_backup", this));

        final Thread backupThread = new Thread(() -> {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String timestampString = timestamp.toString().replaceAll("[^0-9]", "_");
            String filename = getSafeName() + "-" + timestampString.substring(0, timestampString.lastIndexOf("_"))
                    + ".zip";

            Path backupsPath = FileSystem.BACKUPS;
            if (App.settings.backupsPath != null) {
                backupsPath = Paths.get(App.settings.backupsPath);
            }

            ArchiveUtils.createZip(getRoot(), backupsPath.resolve(filename),
                    ZipNameMapper.getMapperForBackupMode(backupMode));

            dialog.dispose();
            App.TOASTER.pop(GetText.tr("Backup is complete"));
        });
        backupThread.start();
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                backupThread.interrupt();
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    public boolean canChangeDescription() {
        return isExternalPack() || launcher.vanillaInstance || (getPack() != null && getPack().system);
    }

    public void startReinstall() {
        Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_reinstall", this));
        InstanceInstallerDialog instanceInstallerDialog = new InstanceInstallerDialog(this);
        instanceInstallerDialog.setVisible(true);
    }

    public void startRename() {
        Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_rename", this));
        RenameInstanceDialog renameInstanceDialog = new RenameInstanceDialog(this);
        renameInstanceDialog.setVisible(true);
    }

    public void startClone() {
        String clonedName = JOptionPane.showInputDialog(App.launcher.getParent(),
                GetText.tr("Enter a new name for this cloned instance."),
                GetText.tr("Cloning Instance"), JOptionPane.INFORMATION_MESSAGE);

        if (clonedName != null && !clonedName.isEmpty()
                && InstanceManager.getInstanceByName(clonedName) == null
                && InstanceManager
                        .getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
                && !clonedName.replaceAll("[^A-Za-z0-9]", "").isEmpty() && !Files.exists(
                        FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_clone", this));

            final String newName = clonedName;
            final ProgressDialog<Void> dialog = new ProgressDialog<>(GetText.tr("Cloning Instance"), 0,
                    GetText.tr("Cloning Instance. Please wait..."), null, App.launcher.getParent());
            dialog.addThread(new Thread(() -> {
                InstanceManager.cloneInstance(this, newName);
                dialog.close();
                App.TOASTER.pop(GetText.tr("Cloned Instance Successfully"));
            }));
            dialog.start();
        } else if (clonedName == null || clonedName.isEmpty()) {
            LogManager.error("Error Occurred While Cloning Instance! Dialog Closed/Cancelled!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").isEmpty()) {
            LogManager.error("Error Occurred While Cloning Instance! Invalid Name!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else if (Files
                .exists(FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            LogManager.error(
                    "Error Occurred While Cloning Instance! Folder Already Exists Rename It And Try Again!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else {
            LogManager.error(
                    "Error Occurred While Cloning Instance! Instance With That Name Already Exists!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public void startChangeDescription() {
        JTextArea textArea = new JTextArea(launcher.description);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(300, 150);

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), new JScrollPane(textArea),
                GetText.tr("Changing Description"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret == 0) {
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_description_change", this));
            launcher.description = textArea.getText();
            save();
        }
    }

    public void startChangeImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
        int ret = chooser.showOpenDialog(App.launcher.getParent());
        if (ret == JFileChooser.APPROVE_OPTION) {
            File img = chooser.getSelectedFile();
            if (img.getAbsolutePath().endsWith(".png")) {
                Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_image_change", this));
                try {
                    Utils.safeCopy(img, getRoot().resolve("instance.png").toFile());
                    save();
                } catch (IOException ex) {
                    LogManager.logStackTrace("Failed to set instance image", ex);
                }
            }
        }
    }

    public void changeLoaderVersion() {
        Analytics.trackEvent(
                AnalyticsEvent.forInstanceLoaderEvent("instance_change_loader_version", this, launcher.loaderVersion));

        LoaderVersion loaderVersion = showLoaderVersionSelector(launcher.loaderVersion.getLoaderType());

        if (loaderVersion == null) {
            return;
        }

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), loaderVersion,
                    launcher.description);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.changingLoader = true;
            installable.isServer = false;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(
                            new HTMLBuilder().center()
                                    // #. {0} is the loader (Forge/Fabric/Quilt) {1} is the version
                                    .text(GetText.tr("{0} {1} has been installed.",
                                            launcher.loaderVersion.getLoaderType(), loaderVersion.version))
                                    .build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has not been installed. Check the console for more information.",
                                    launcher.loaderVersion.getLoaderType()))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public void addLoader(LoaderType loaderType) {
        Analytics
                .trackEvent(AnalyticsEvent.forInstanceAddLoader(this, loaderType));

        LoaderVersion loaderVersion = showLoaderVersionSelector(loaderType);

        if (loaderVersion == null) {
            return;
        }

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), loaderVersion,
                    launcher.description);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.addingLoader = true;
            installable.isServer = false;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt) {1} is the version
                            .text(GetText.tr("{0} {1} has been installed.", loaderType, loaderVersion.version)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", loaderType))
                    // #. {0} is the loader (Forge/Fabric/Quilt)
                    .setContent(new HTMLBuilder().center().text(GetText
                            .tr("{0} has not been installed. Check the console for more information.", loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    private LoaderVersion showLoaderVersionSelector(LoaderType loaderType) {
        ProgressDialog<List<LoaderVersion>> progressDialog = new ProgressDialog<>(
                // #. {0} is the loader (Forge/Fabric/Quilt)
                GetText.tr("Checking For {0} Versions", loaderType), 0,
                // #. {0} is the loader (Forge/Fabric/Quilt)
                GetText.tr("Checking For {0} Versions", loaderType));
        progressDialog.addThread(new Thread(() -> {
            if (loaderType == LoaderType.FABRIC) {
                progressDialog.setReturnValue(FabricLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.FORGE) {
                progressDialog.setReturnValue(ForgeLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.LEGACY_FABRIC) {
                progressDialog.setReturnValue(LegacyFabricLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.NEOFORGE) {
                progressDialog.setReturnValue(NeoForgeLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.QUILT) {
                progressDialog.setReturnValue(QuiltLoader.getChoosableVersions(id));
            }

            progressDialog.doneTask();
            progressDialog.close();
        }));
        progressDialog.start();

        List<LoaderVersion> loaderVersions = progressDialog.getReturnValue();

        if (loaderVersions == null || loaderVersions.isEmpty()) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("No Versions Available For {0}", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has not been installed/updated as there are no versions available.",
                                    loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return null;
        }

        JComboBox<ComboItem<LoaderVersion>> loaderVersionsDropDown = new JComboBox<>();

        int loaderVersionLength = 0;

        // ensures that font width is taken into account
        for (LoaderVersion version : loaderVersions) {
            loaderVersionLength = Math.max(loaderVersionLength, loaderVersionsDropDown
                    .getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toStringWithCurrent(this)) + 25);
        }

        loaderVersions.forEach(version -> loaderVersionsDropDown
                .addItem(new ComboItem<>(version, version.toStringWithCurrent(this))));

        if (loaderType == LoaderType.FORGE) {
            Optional<LoaderVersion> recommendedVersion = loaderVersions.stream().filter(lv -> lv.recommended)
                    .findFirst();

            recommendedVersion.ifPresent(
                    loaderVersion -> loaderVersionsDropDown.setSelectedIndex(loaderVersions.indexOf(loaderVersion)));
        }

        if (launcher.loaderVersion != null) {
            String loaderVersionString = launcher.loaderVersion.version;

            for (int i = 0; i < loaderVersionsDropDown.getItemCount(); i++) {
                LoaderVersion loaderVersion = loaderVersionsDropDown.getItemAt(i)
                        .getValue();

                if (loaderVersion.version.equals(loaderVersionString)) {
                    loaderVersionsDropDown.setSelectedIndex(i);
                    break;
                }
            }
        }

        // ensures that the dropdown is at least 200 px wide
        loaderVersionLength = Math.max(200, loaderVersionLength);

        // ensures that there is a maximum width of 400 px to prevent overflow
        loaderVersionLength = Math.min(400, loaderVersionLength);

        loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Box box = Box.createHorizontalBox();
        // #. {0} is the loader (Forge/Fabric/Quilt)
        box.add(new JLabel(GetText.tr("Select {0} Version To Install", loaderType)));
        box.add(Box.createHorizontalGlue());

        panel.add(box);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loaderVersionsDropDown);
        panel.add(Box.createVerticalStrut(20));

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), panel,
                // #. {0} is the loader (Forge/Fabric/Quilt)
                launcher.loaderVersion == null ? GetText.tr("Installing {0}", loaderType)
                        // #. {0} is the loader (Forge/Fabric/Quilt)
                        : GetText.tr("Changing {0} Version", loaderType),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret != 0) {
            return null;
        }

        return ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getSelectedItem()).getValue();
    }

    public void removeLoader() {
        Analytics.trackEvent(
                AnalyticsEvent.forInstanceLoaderEvent("instance_remove_loader", this, launcher.loaderVersion));
        String loaderType = launcher.loaderVersion.type;

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), null,
                    launcher.description);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.removingLoader = true;
            installable.isServer = false;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {

            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Removed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has been removed from this instance.", loaderType)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Removed", loaderType))
                    .setContent(new HTMLBuilder().center().text(
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            GetText.tr("{0} has not been removed. Check the console for more information.", loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public boolean usesCustomMinecraftJar() {
        return Files.exists(getRoot().resolve("bin/modpack.jar"));
    }

    public boolean shouldUseLegacyLaunch() {
        try {
            String[] versionParts = id.split("\\.", 3);

            return Integer.parseInt(versionParts[0]) == 1 && Integer.parseInt(versionParts[1]) < 6;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public boolean usesLegacyLaunch() {
        if (type != VersionManifestVersionType.RELEASE
                || Optional.ofNullable(launcher.disableLegacyLaunching).orElse(App.settings.disableLegacyLaunching)) {
            return false;
        }

        return shouldUseLegacyLaunch();
    }

    public boolean hasDisabledJavaRuntime() {
        if (javaVersion == null) {
            return false;
        }

        return !Optional.ofNullable(launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft);
    }

    public boolean isUsingJavaRuntime() {
        if (javaVersion == null) {
            return launcher.javaRuntimeOverride != null;
        }

        return Optional.ofNullable(launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft);
    }

    public String getJavaPath() {
        String javaPath = Optional.ofNullable(launcher.javaPath).orElse(App.settings.javaPath);

        // are we using Mojangs provided runtime?
        if (isUsingJavaRuntime()) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeToUse = Optional.ofNullable(launcher.javaRuntimeOverride)
                    .orElseGet(() -> javaVersion.component);

            // make sure the runtime is available in the data set (so it's not disabled
            // remotely)
            if (runtimesForSystem.containsKey(runtimeToUse)
                    && !runtimesForSystem.get(runtimeToUse).isEmpty()) {
                Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                        .resolve(JavaRuntimes.getSystem()).resolve(runtimeToUse);

                if (OS.isMac()) {
                    runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
                }

                if (Files.isDirectory(runtimeDirectory)) {
                    javaPath = runtimeDirectory.toAbsolutePath().toString();
                    if (launcher.javaRuntimeOverride != null) {
                        LogManager.info(String.format("Using overriden Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    } else {
                        LogManager.info(String.format("Using Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    }
                }
            }
        }

        return javaPath;
    }

    public boolean shouldShowWrongJavaWarning() {
        if (launcher.java == null) {
            return false;
        }

        String javaVersionNumber = Java.getVersionForJavaPath(new File(getJavaPath()));

        if (javaVersionNumber.equalsIgnoreCase("Unknown")) {
            return false;
        }

        int majorJavaVersion = Java.parseJavaVersionNumber(javaVersionNumber);

        return !launcher.java.conforms(majorJavaVersion);
    }

    public String getVersionOfPack() {
        if (isModrinthPack()) {
            return String.format("%s (%s)", launcher.modrinthVersion.name, launcher.modrinthVersion.versionNumber);
        }

        return launcher.version;
    }

    public List<String> getSinglePlayerWorldNamesFromFilesystem() {
        File[] folders = ROOT.resolve("saves").toFile().listFiles((dir, name) -> new File(dir, name).isDirectory());
        if (folders == null)
            return new ArrayList<>();
        return Arrays.stream(folders).map(File::getName).collect(Collectors.toList());
    }

    public boolean isQuickPlaySupported(QuickPlayOption quickPlayOption) {
        if (quickPlayOption.argumentRuleValue == null) {
            return false;
        }
        return arguments.game.stream().anyMatch(
                argumentRule -> argumentRule.value instanceof List &&
                        ((List<?>) argumentRule.value).contains(quickPlayOption.argumentRuleValue));
    }

    private List<Path> getModPathsFromFilesystem() {
        return getModPathsFromFilesystem(Arrays.asList(ROOT.resolve("mods"),
                ROOT.resolve("resourcepacks"),
                ROOT.resolve("shaderpacks"),
                ROOT.resolve("jarmods")));
    }

    public List<Path> getModPathsFromFilesystem(List<Path> paths) {
        List<Path> files = new ArrayList<>();

        for (Path path : paths) {
            if (!Files.exists(path)) {
                continue;
            }

            try (Stream<Path> stream = Files.list(path)) {
                files.addAll(stream
                        .filter(file -> !Files.isDirectory(file) && Utils.isAcceptedModFile(file))
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace("Error getting mod paths", e);
            }
        }

        return files;
    }

    @Override
    public void scanMissingMods(Window parent) {
        PerformanceManager.start("Instance::scanMissingMods - CheckForAddedMods");

        // files to scan
        List<Path> files = new ArrayList<>();

        // find the mods that have been added by the user manually
        for (Path path : Arrays.asList(ROOT.resolve("mods"), ROOT.resolve("disabledmods"),
                ROOT.resolve("resourcepacks"), ROOT.resolve("shaderpacks"), ROOT.resolve("jarmods"))) {
            if (!Files.exists(path)) {
                continue;
            }

            Type fileType = getTypeOfFileFromPath(path);

            try (Stream<Path> stream = Files.list(path)) {
                files.addAll(stream
                        .filter(file -> !Files.isDirectory(file) && Utils.isAcceptedModFile(file)).filter(
                                file -> launcher.mods.stream()
                                        .noneMatch(mod -> mod.type == fileType
                                                && mod.file.equals(file.getFileName().toString())))
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace("Error scanning missing mods", e);
            }
        }

        if (!files.isEmpty()) {
            final ProgressDialog<Void> progressDialog = new ProgressDialog<>(GetText.tr("Scanning New Mods"), 0,
                    GetText.tr("Scanning New Mods"), parent);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> mods = files.parallelStream()
                        .map(file -> {
                            Type fileType = getTypeOfFileFromPath(file.getParent());

                            return DisableableMod.generateMod(file.toFile(), fileType,
                                    !file.getParent().equals(ROOT.resolve("disabledmods")));
                        })
                        .collect(Collectors.toList());

                if (!App.settings.dontCheckModsOnCurseForge) {
                    Map<Long, DisableableMod> murmurHashes = new HashMap<>();

                    mods.stream()
                            .filter(dm -> dm.curseForgeProject == null && dm.curseForgeFile == null)
                            .filter(dm -> dm.getFile(ROOT, id) != null).forEach(dm -> {
                                try {
                                    long hash = Hashing
                                            .murmur(dm.disabled ? dm.getDisabledFile(this).toPath()
                                                    : dm
                                                            .getFile(ROOT, id).toPath());
                                    murmurHashes.put(hash, dm);
                                } catch (IOException e) {
                                    LogManager.logStackTrace(e);
                                }
                            });

                    if (!murmurHashes.isEmpty()) {
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

                                                CurseForgeProject curseForgeProject = foundProjects
                                                        .get(foundMod.id);

                                                if (curseForgeProject != null && curseForgeProject.status == 4) {
                                                    dm.curseForgeProjectId = foundMod.id;
                                                    dm.curseForgeFile = foundMod.file;
                                                    dm.curseForgeFileId = foundMod.file.id;
                                                    dm.curseForgeProject = curseForgeProject;
                                                    dm.name = curseForgeProject.name;
                                                    dm.description = curseForgeProject.summary;

                                                    LogManager.debug("Found matching mod from CurseForge called "
                                                            + dm.curseForgeFile.displayName);
                                                }

                                                // reset if the file is not approved
                                                if (curseForgeProject != null && curseForgeProject.status != 4) {
                                                    dm.curseForgeProjectId = null;
                                                    dm.curseForgeFile = null;
                                                    dm.curseForgeFileId = null;
                                                    dm.curseForgeProject = null;

                                                    File path = dm.getFile(this);
                                                    MCMod mcMod = Utils.getMCModForFile(path);
                                                    if (mcMod != null) {
                                                        dm.name = Optional.ofNullable(mcMod.name)
                                                                .orElse(path.getName());
                                                        dm.description = mcMod.description;
                                                    } else {
                                                        FabricMod fabricMod = Utils.getFabricModForFile(path);
                                                        if (fabricMod != null) {
                                                            dm.name = Optional.ofNullable(fabricMod.name)
                                                                    .orElse(path.getName());
                                                            dm.description = fabricMod.description;
                                                        }
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                    }
                }

                if (!App.settings.dontCheckModsOnModrinth) {
                    Map<String, DisableableMod> sha1Hashes = new HashMap<>();

                    mods.stream()
                            .filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
                            .filter(dm -> dm.getFile(ROOT, id) != null).forEach(dm -> {
                                try {
                                    sha1Hashes.put(Hashing
                                            .sha1(dm.disabled ? dm.getDisabledFile(this).toPath()
                                                    : dm
                                                            .getFile(ROOT, id).toPath())
                                            .toString(), dm);
                                } catch (Throwable t) {
                                    LogManager.logStackTrace(t);
                                }
                            });

                    if (!sha1Hashes.isEmpty()) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                                .getVersionsFromSha1Hashes(keys.toArray(new String[0]));

                        if (modrinthVersions != null && !modrinthVersions.isEmpty()) {
                            String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                    .toArray(String[]::new);

                            if (projectIdsFound.length != 0) {
                                Map<String, ModrinthProject> foundProjects = ModrinthApi
                                        .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                        ModrinthVersion version = entry.getValue();
                                        ModrinthProject project = foundProjects.get(version.projectId);

                                        if (project != null) {
                                            DisableableMod dm = sha1Hashes.get(entry.getKey());

                                            // add Modrinth information
                                            dm.modrinthProject = project;
                                            dm.modrinthVersion = version;

                                            if (!dm.isFromCurseForge()
                                                    || App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                                                dm.name = project.title;
                                                dm.description = project.description;
                                            }

                                            LogManager.debug(String.format(
                                                    "Found matching mod from Modrinth called %s with file %s",
                                                    project.title, version.name));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mods.forEach(mod -> LogManager.info("Found extra mod with name of " + mod.file));
                launcher.mods.addAll(mods);
                save();
                progressDialog.close();
            }));

            progressDialog.start();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForAddedMods");

        PerformanceManager.start("Instance::scanMissingMods - CheckForRemovedMods");

        // next remove any mods that the no longer exist in the filesystem
        List<DisableableMod> removedMods = launcher.mods.parallelStream().filter(mod -> {
            if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                return false;
            }

            if (mod.disabled) {
                return (mod.getDisabledFile(this) != null && !mod.getDisabledFile(this).exists());
            } else {
                return (mod.getFile(this) != null && !mod.getFile(this).exists());
            }
        }).collect(Collectors.toList());

        if (!removedMods.isEmpty()) {
            removedMods.forEach(mod -> LogManager.info("Mod no longer in filesystem: " + mod.file));
            launcher.mods.removeAll(removedMods);
            save();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForRemovedMods");

        PerformanceManager.start("Instance::scanMissingMods - CheckForDuplicateMods");

        Set<File> seenModFiles = new HashSet<>();
        List<DisableableMod> duplicateMods = launcher.mods.stream()
                .filter(mod -> {
                    if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                        return false;
                    }

                    File file;
                    if (mod.disabled) {
                        file = mod.getDisabledFile(this);
                    } else {
                        file = mod.getFile(this);
                    }

                    if (file == null) {
                        return false;
                    }

                    return !seenModFiles.add(file);
                })
                .collect(Collectors.toList());

        if (!duplicateMods.isEmpty()) {
            duplicateMods.forEach(mod -> LogManager.info("Mod is a duplicate: " + mod.file));
            launcher.mods.removeAll(duplicateMods);
            save();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForDuplicateMods");
    }

    @NotNull
    private Type getTypeOfFileFromPath(Path path) {
        if (path.equals(ROOT.resolve("resourcepacks"))) {
            return Type.resourcepack;
        }

        if (path.equals(ROOT.resolve("shaderpacks"))) {
            return Type.shaderpack;
        }

        if (path.equals(ROOT.resolve("jarmods"))) {
            return Type.jar;
        }

        return Type.mods;
    }

    public boolean showGetHelpButton() {
        if (getPack() != null || isModrinthPack() || isCurseForgePack()) {
            return getDiscordInviteUrl() != null || getSupportUrl() != null || getWikiUrl() != null
                    || getSourceUrl() != null;
        }

        return false;
    }

    public String getDiscordInviteUrl() {
        if (getPack() != null) {
            return getPack().discordInviteURL;
        }

        if (isModrinthPack()) {
            return launcher.modrinthProject.discordUrl;
        }

        if (isCurseForgePack()) {
            if (launcher.curseForgeProject.hasSocialLink(CurseForgeSocialLinkType.DISCORD)) {
                return launcher.curseForgeProject.getSocialLink(CurseForgeSocialLinkType.DISCORD);
            }

            if (launcher.curseForgeProjectDescription != null) {
                String discordLinkFromDescription = CurseForgeUtils
                        .parseDescriptionForDiscordInvite(launcher.curseForgeProjectDescription);
                if (discordLinkFromDescription != null) {
                    return discordLinkFromDescription;
                }
            }

            // allow overriding the discord link from ATLauncher's side
            String overriddenDiscordLink = ConfigManager
                    .<String>getConfigItem(
                            "discordLinkMatching.curseForgeProjectIdsToDiscordLink." + launcher.curseForgeProject.id,
                            null);
            if (overriddenDiscordLink != null) {
                return overriddenDiscordLink;
            }

            // allow overriding the discord link from ATLauncher's side for an author
            if (launcher.curseForgeProject.authors != null && !launcher.curseForgeProject.authors.isEmpty()) {
                String overriddenDiscordLinkForAuthor = ConfigManager
                        .<String>getConfigItem(
                                "discordLinkMatching.curseForgeAuthorIdsToDiscordLink."
                                        + launcher.curseForgeProject.authors.get(0).id,
                                null);
                if (overriddenDiscordLinkForAuthor != null) {
                    return overriddenDiscordLinkForAuthor;
                }
            }
        }

        return null;
    }

    public String getSupportUrl() {
        if (getPack() != null) {
            return getPack().supportURL;
        }

        if (isModrinthPack()) {
            return launcher.modrinthProject.issuesUrl;
        }

        if (isCurseForgePack() && launcher.curseForgeProject.hasIssuesUrl()) {
            return launcher.curseForgeProject.getIssuesUrl();
        }

        return null;
    }

    public boolean hasWebsite() {
        if (isCurseForgePack()) {
            return launcher.curseForgeProject.hasWebsiteUrl();
        }

        if (isFTBPack()) {
            return launcher.ftbPackManifest.hasTag("FTB");
        }

        return isModrinthPack() || isTechnicPack();
    }

    public String getWebsiteUrl() {
        if (isCurseForgePack() && launcher.curseForgeProject.hasWebsiteUrl()) {
            return launcher.curseForgeProject.getWebsiteUrl();
        }

        if (isFTBPack() && launcher.ftbPackManifest.hasTag("FTB")) {
            return launcher.ftbPackManifest.getWebsiteUrl();
        }

        if (isModrinthPack()) {
            return String.format("https://modrinth.com/modpack/%s", launcher.modrinthProject.slug);
        }

        if (isTechnicPack()) {
            return launcher.technicModpack.platformUrl;
        }

        return null;
    }

    public String getWikiUrl() {
        if (isModrinthPack()) {
            return launcher.modrinthProject.wikiUrl;
        }

        if (isCurseForgePack() && launcher.curseForgeProject.hasWikiUrl()) {
            return launcher.curseForgeProject.getWikiUrl();
        }

        return null;
    }

    public String getSourceUrl() {
        if (isModrinthPack()) {
            return launcher.modrinthProject.sourceUrl;
        }

        return null;
    }

    @Override
    public boolean isForgeLikeAndHasInstalledSinytraConnector() {
        if (launcher.loaderVersion == null || !(launcher.loaderVersion.isForge()
                || launcher.loaderVersion.isNeoForge())) {
            return false;
        }

        return launcher.mods.stream().anyMatch(m -> (m.isFromCurseForge()
                && m.getCurseForgeModId() == Constants.CURSEFORGE_SINYTRA_CONNECTOR_MOD_ID)
                || (m.isFromModrinth()
                        && m.modrinthProject.id.equalsIgnoreCase(Constants.MODRINTH_SINYTRA_CONNECTOR_MOD_ID)))
                && App.settings.showFabricModsWhenSinytraInstalled;
    }

    @Override
    public boolean supportsPlugins() {
        return false;
    }

    @Override
    public List<DisableableMod> getMods() {
        return launcher.mods;
    }

    @Override
    public void addMod(DisableableMod mod) {
        launcher.mods.add(mod);
    }

    @Override
    public void addMods(List<DisableableMod> modsToAdd) {
        launcher.mods.addAll(modsToAdd);
    }

    @Override
    public void removeMod(DisableableMod mod) {
        launcher.mods.remove(mod);
        FileUtils.delete(
                (mod.isDisabled()
                        ? mod.getDisabledFile(this)
                        : mod.getFile(this)).toPath(),
                true);
        save();

        // #. {0} is the name of a mod that was removed
        App.TOASTER.pop(GetText.tr("{0} Removed", mod.name));
    }

    @Override
    public void addFileFromCurseForge(CurseForgeProject mod, CurseForgeFile file, ProgressDialog<Void> dialog) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.getInstanceDirectoryPath(this.getRoot()).resolve(file.fileName);

        // find mods with the same CurseForge project id
        List<DisableableMod> sameMods = this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurseForge()
                        && installedMod.getCurseForgeModId() == mod.id)
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        Optional<CurseForgeFileHash> md5Hash = file.hashes.stream().filter(CurseForgeFileHash::isMd5)
                .findFirst();
        Optional<CurseForgeFileHash> sha1Hash = file.hashes.stream().filter(CurseForgeFileHash::isSha1)
                .findFirst();

        if (file.downloadUrl == null) {
            if (!App.settings.seenCurseForgeProjectDistributionDialog) {
                App.settings.seenCurseForgeProjectDistributionDialog = true;
                App.settings.save();

                DialogManager.okDialog().setType(DialogManager.WARNING)
                        .setTitle(GetText.tr("Mod Not Available"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "We were unable to download this mod.<br/>This is likely due to the author of the mod disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to download the mod manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, we can continue with installing the mod.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                                .build())
                        .show();
            }

            dialog.setIndeterminate();
            String filename = file.fileName;
            String filename2 = file.fileName.replace(" ", "+");
            File fileLocation = downloadLocation.toFile();
            File fileLocation2 = FileSystem.DOWNLOADS.resolve(filename2).toFile();
            // if file downloaded already, but hashes don't match, delete it
            if (fileLocation.exists()
                    && ((md5Hash.isPresent()
                            && !Hashing.md5(fileLocation.toPath()).equals(Hashing.toHashCode(md5Hash.get().value)))
                            || (sha1Hash.isPresent()
                                    && !Hashing.sha1(fileLocation.toPath())
                                            .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                FileUtils.delete(fileLocation.toPath());
            } else if (fileLocation2.exists()
                    && ((md5Hash.isPresent()
                            && !Hashing.md5(fileLocation2.toPath()).equals(Hashing.toHashCode(md5Hash.get().value)))
                            || (sha1Hash.isPresent()
                                    && !Hashing.sha1(fileLocation2.toPath())
                                            .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                FileUtils.delete(fileLocation2.toPath());
            }

            if (!fileLocation.exists() && !fileLocation2.exists()) {
                File downloadsFolderFile = new File(FileSystem.getUserDownloadsPath().toFile(), filename);
                File downloadsFolderFile2 = new File(FileSystem.getUserDownloadsPath().toFile(), filename2);
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                } else if (downloadsFolderFile2.exists()) {
                    Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                }

                while (!fileLocation.exists() && !fileLocation2.exists()) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            OS.openWebBrowser(mod.getBrowserDownloadUrl(file));
                        }

                        retValue = DialogManager.optionDialog()
                                .setTitle(GetText.tr("Downloading") + " "
                                        + filename)
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "Browser opened to download file {0}",
                                        filename)
                                        + "<br/><br/>" + GetText.tr("Please save this file to the following location")
                                        + "<br/><br/>"
                                        + (OS.isUsingMacApp()
                                                ? FileSystem.getUserDownloadsPath().toFile().getAbsolutePath()
                                                : FileSystem.DOWNLOADS.toAbsolutePath().toString()
                                                        + " or<br/>"
                                                        + FileSystem.getUserDownloadsPath().toFile()))
                                        .build())
                                .addOption(GetText.tr("Open Folder"), true)
                                .addOption(GetText.tr("I've Downloaded This File")).setType(DialogManager.INFO)
                                .showWithFileMonitoring(file.fileLength, 1, fileLocation, fileLocation2,
                                        downloadsFolderFile, downloadsFolderFile2);

                        if (retValue == DialogManager.CLOSED_OPTION) {
                            return;
                        } else if (retValue == 0) {
                            OS.openFileExplorer(FileSystem.DOWNLOADS);
                        }
                    } while (retValue != 1);

                    if (!fileLocation.exists() && !fileLocation2.exists()) {
                        // Check users downloads folder to see if it's there
                        if (downloadsFolderFile.exists()) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
                        } else if (downloadsFolderFile2.exists()) {
                            Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                        }
                        // Check to see if a browser has added a .zip to the end of the file
                        File zipAddedFile = FileSystem.DOWNLOADS.resolve(file.fileName + ".zip").toFile();
                        if (zipAddedFile.exists()) {
                            Utils.moveFile(zipAddedFile, fileLocation, true);
                        } else {
                            zipAddedFile = new File(FileSystem.getUserDownloadsPath().toFile(), file.fileName + ".zip");
                            if (zipAddedFile.exists()) {
                                Utils.moveFile(zipAddedFile, fileLocation, true);
                            }
                        }
                    }

                    // file downloaded, but hashes don't match, delete it
                    if (fileLocation.exists()
                            && ((md5Hash.isPresent() && !Hashing.md5(fileLocation.toPath())
                                    .equals(Hashing.toHashCode(md5Hash.get().value)))
                                    || (sha1Hash.isPresent()
                                            && !Hashing.sha1(fileLocation.toPath())
                                                    .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                        FileUtils.delete(fileLocation.toPath());
                    } else if (fileLocation2.exists()
                            && ((md5Hash.isPresent() && !Hashing.md5(fileLocation2.toPath())
                                    .equals(Hashing.toHashCode(md5Hash.get().value)))
                                    || (sha1Hash.isPresent()
                                            && !Hashing.sha1(fileLocation2.toPath())
                                                    .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                        FileUtils.delete(fileLocation2.toPath());
                    }
                }
            }

            if (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
                FileUtils.createDirectory(this.getRoot().resolve("saves"));

                ArchiveUtils.extract(downloadLocation, this.getRoot().resolve("saves"));
            } else {
                if (Files.exists(finalLocation)) {
                    FileUtils.delete(finalLocation);
                }

                if (!Files.isDirectory(finalLocation.getParent())) {
                    FileUtils.createDirectory(finalLocation.getParent());
                }

                FileUtils.copyFile(downloadLocation, finalLocation, true);
            }
        } else {
            com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(file.downloadUrl)
                    .downloadTo(downloadLocation).size(file.fileLength)
                    .withHttpClient(Network.createProgressClient(dialog));

            dialog.setTotalBytes(file.fileLength);

            if (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
                download = download.unzipTo(this.getRoot().resolve("saves"));
            } else {
                download = download.copyTo(finalLocation);
                if (Files.exists(finalLocation)) {
                    FileUtils.delete(finalLocation);
                }
            }

            if (md5Hash.isPresent()) {
                download = download.hash(md5Hash.get().value);
            } else if (sha1Hash.isPresent()) {
                download = download.hash(sha1Hash.get().value);
            }

            if (download.needToDownload()) {
                try {
                    download.downloadFile();
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                    DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                            .setContent("Failed to download " + file.fileName + ". Please try again later.").show();
                    return;
                }
            } else {
                download.copy();
            }
        }

        // remove any mods that are from the same mod on CurseForge from the master mod
        // list
        this.launcher.mods = this.launcher.mods.stream()
                .filter(installedMod -> !installedMod.isFromCurseForge() || installedMod.getCurseForgeModId() != mod.id)
                .collect(Collectors.toList());

        DisableableMod dm = new DisableableMod(mod.name, file.displayName, true, file.fileName,
                getTypeFromCurseForgeMod(mod), null, mod.summary, false, true, true, false, mod, file);

        // check for mod on Modrinth
        if (!App.settings.dontCheckModsOnModrinth) {
            ModrinthVersion version = ModrinthApi.getVersionFromSha1Hash(Hashing.sha1(finalLocation).toString());
            if (version != null) {
                ModrinthProject project = ModrinthApi.getProject(version.projectId);
                if (project != null) {
                    // add Modrinth information
                    dm.modrinthProject = project;
                    dm.modrinthVersion = version;
                }
            }
        }

        // add this mod to the instance (if not a world)
        if (dm.type != com.atlauncher.data.Type.worlds) {
            this.launcher.mods.add(dm);
            this.save();
        }

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
    }

    @NotNull
    private static Type getTypeFromCurseForgeMod(CurseForgeProject mod) {
        if (mod.getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return Type.resourcepack;
        }

        if (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
            return Type.worlds;
        }

        if (mod.getRootCategoryId() == Constants.CURSEFORGE_SHADER_PACKS_SECTION_ID) {
            return Type.shaderpack;
        }

        return Type.mods;
    }

    @Override
    public void addFileFromModrinth(ModrinthProject mod, ModrinthVersion version, ModrinthFile file,
            ProgressDialog<Void> dialog) {
        ModrinthFile fileToDownload = Optional.ofNullable(file).orElse(version.getPrimaryFile());

        Path downloadLocation = FileSystem.DOWNLOADS.resolve(fileToDownload.filename);
        Path finalLocation = mod.projectType == ModrinthProjectType.MOD
                ? this.getRoot().resolve("mods").resolve(fileToDownload.filename)
                : (mod.projectType == ModrinthProjectType.SHADER
                        ? this.getRoot().resolve("shaderpacks").resolve(fileToDownload.filename)
                        : this.getRoot().resolve("resourcepacks").resolve(fileToDownload.filename));
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(fileToDownload.url)
                .downloadTo(downloadLocation).copyTo(finalLocation)
                .withHttpClient(Network.createProgressClient(dialog));

        if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha512")) {
            download = download.hash(fileToDownload.hashes.get("sha512"));
        } else if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha1")) {
            download = download.hash(fileToDownload.hashes.get("sha1"));
        }

        if (fileToDownload.size != null && fileToDownload.size != 0) {
            dialog.setTotalBytes(fileToDownload.size);
            download = download.size(fileToDownload.size);
        }

        if (Files.exists(finalLocation)) {
            FileUtils.delete(finalLocation);
        }

        // find mods with the same Modrinth id
        List<DisableableMod> sameMods = this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromModrinth()
                        && installedMod.modrinthProject.id.equalsIgnoreCase(mod.id))
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        if (download.needToDownload()) {
            try {
                download.downloadFile();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                        .setContent("Failed to download " + fileToDownload.filename + ". Please try again later.")
                        .show();
                return;
            }
        } else {
            download.copy();
        }

        // remove any mods that are from the same mod from the master mod list
        this.launcher.mods = this.launcher.mods.stream().filter(
                installedMod -> !installedMod.isFromModrinth()
                        || !installedMod.modrinthProject.id.equalsIgnoreCase(mod.id))
                .collect(Collectors.toList());

        Type modType = getTypeFromModrinthMod(mod);

        DisableableMod dm = new DisableableMod(mod.title, version.name, true, fileToDownload.filename, modType,
                null, mod.description, false, true, true, false, mod, version);

        // check for mod on CurseForge
        if (!App.settings.dontCheckModsOnCurseForge) {
            try {
                CurseForgeFingerprint fingerprint = CurseForgeApi
                        .checkFingerprints(new Long[] { Hashing.murmur(finalLocation) });

                if (fingerprint.exactMatches != null && fingerprint.exactMatches.size() == 1) {
                    CurseForgeFingerprintedMod foundMod = fingerprint.exactMatches.get(0);

                    CurseForgeProject curseForgeProject = CurseForgeApi.getProjectById(foundMod.id);
                    if (curseForgeProject != null && curseForgeProject.status == 4) {
                        dm.curseForgeProjectId = foundMod.id;
                        dm.curseForgeFile = foundMod.file;
                        dm.curseForgeFileId = foundMod.file.id;
                        dm.curseForgeProject = curseForgeProject;
                    }
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        // add this mod
        this.launcher.mods.add(dm);
        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.title));
    }

    @NotNull
    private static Type getTypeFromModrinthMod(ModrinthProject mod) {
        if (mod.projectType == ModrinthProjectType.MOD) {
            return Type.mods;
        }

        if (mod.projectType == ModrinthProjectType.SHADER) {
            return Type.shaderpack;
        }

        return Type.resourcepack;
    }

    public @Nullable Path createSupportPack(Path outputDirectory) {
        try {
            // Create a temporary directory for the support pack
            Path tempDir = FileSystem.TEMP.resolve(this.getSafeName() + "-support-pack");
            FileUtils.createDirectory(tempDir);

            // Copy instance.json
            Path instanceJsonPath = this.ROOT.resolve("instance.json");
            if (Files.exists(instanceJsonPath)) {
                Files.copy(instanceJsonPath, tempDir.resolve("instance.json"), StandardCopyOption.REPLACE_EXISTING);
            }

            // Copy logs
            Path logsDir = this.ROOT.resolve("logs");
            if (Files.exists(logsDir) && Files.isDirectory(logsDir)) {
                Utils.copyDirectory(logsDir.toFile(), tempDir.resolve("logs").toFile());
            }

            // Create a text file with the folder name
            Path folderNameFile = tempDir.resolve("folder_name.txt");
            try (BufferedWriter writer = Files.newBufferedWriter(folderNameFile, StandardCharsets.UTF_8)) {
                writer.write(this.ROOT.getFileName().toString());
            }

            // Create the zip file
            Path zipFilePath = outputDirectory.resolve(this.getSafeName() + "-support-pack.zip");
            ArchiveUtils.createZip(tempDir, zipFilePath);

            // Clean up temporary directory
            FileUtils.deleteDirectory(tempDir);

            LogManager.info("Support pack created at: " + zipFilePath);

            return zipFilePath;
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to create support pack", e);
        }

        return null;
    }
}
