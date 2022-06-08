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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

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

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeManifestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeMinecraft;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingFile;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthSide;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackFile;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCComponent;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.multimc.MultiMCRequire;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSolderModpack;
import com.atlauncher.exceptions.CommandException;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.CurseForgeUpdateManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ModpacksChUpdateManager;
import com.atlauncher.managers.ModrinthModpackUpdateManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.managers.TechnicModpackUpdateManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CommandExecutor;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.JsonIOException;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;

@Json
public class Instance extends MinecraftVersion {
    public String inheritsFrom;
    public InstanceLauncher launcher;

    public transient Path ROOT;

    private Instant lastPlayed;
    private long numPlays;

    public Instance(MinecraftVersion version) {
        setValues(version);
        this.numPlays = 0;
        this.lastPlayed = Instant.EPOCH;
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

    public String getSafeName() {
        return this.launcher.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getSafePackName() {
        return this.launcher.pack.replaceAll("[^A-Za-z0-9]", "");
    }

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

    public boolean hasUpdate() {
        if (launcher.vanillaInstance) {
            // must be reinstalled
            return false;
        } else if (this.isExternalPack()) {
            if (isModpacksChPack()) {
                ModpacksChPackVersion latestVersion = Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.get(this);

                return latestVersion != null && latestVersion.id != this.launcher.modpacksChPackVersionManifest.id;
            } else if (isCurseForgePack()) {
                CurseForgeFile latestVersion = Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(this);

                return latestVersion != null && latestVersion.id != this.launcher.curseForgeFile.id;
            } else if (isTechnicPack()) {
                if (isTechnicSolderPack()) {
                    TechnicSolderModpack technicSolderModpack = Data.TECHNIC_SOLDER_INSTANCE_LATEST_VERSION.get(this);

                    if (technicSolderModpack == null) {
                        return false;
                    }

                    return !technicSolderModpack.latest.equals(launcher.version);
                } else {
                    TechnicModpack technicModpack = Data.TECHNIC_INSTANCE_LATEST_VERSION.get(this);

                    if (technicModpack == null) {
                        return false;
                    }

                    return !technicModpack.version.equals(launcher.version);
                }
            } else if (isModrinthPack()) {
                ModrinthVersion latestVersion = Data.MODRINTH_INSTANCE_LATEST_VERSION.get(this);

                return latestVersion != null && !latestVersion.id.equals(this.launcher.modrinthVersion.id);
            }
        } else {
            Pack pack = this.getPack();

            if (pack != null) {
                if (pack.hasVersions() && !this.launcher.isDev) {
                    // Lastly check if the current version we installed is different than the latest
                    // version of the Pack and that the latest version of the Pack is not restricted
                    // to disallow updates.
                    if (!pack.getLatestVersion().version.equalsIgnoreCase(this.launcher.version)
                            && !pack.isLatestVersionNoUpdate()) {
                        return true;
                    }
                }

                if (this.launcher.isDev && (this.launcher.hash != null)) {
                    PackVersion devVersion = pack.getDevVersionByName(this.launcher.version);
                    if (devVersion != null && !devVersion.hashMatches(this.launcher.hash)) {
                        return true;
                    }
                }
            }
        }

        return false;
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

                // if a square image, then make it 300x150 (without stretching) centered
                if (img.getHeight(null) == img.getWidth(null)) {
                    BufferedImage dimg = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = dimg.createGraphics();
                    g2d.drawImage(img, 75, 0, 150, 150, null);
                    g2d.dispose();

                    return new ImageIcon(dimg);
                }

                return new ImageIcon(img.getScaledInstance(300, 150, Image.SCALE_SMOOTH));
            } catch (Exception e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of instance " + this.launcher.name, e,
                        false);
            }
        }

        if (getPack() != null) {
            File instancesImage = FileSystem.IMAGES.resolve(this.getSafePackName().toLowerCase() + ".png").toFile();

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
            if (isModpacksChPack()) {
                version = Integer.toString(ModpacksChUpdateManager.getLatestVersion(this).id);
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

    public boolean hasLatestUpdateBeenIgnored() {
        if (launcher.vanillaInstance) {
            return false;
        }

        if (isExternalPack()) {
            if (isModpacksChPack()) {
                return hasUpdateBeenIgnored(Integer.toString(ModpacksChUpdateManager.getLatestVersion(this).id));
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
        if (version == null || this.launcher.ignoredUpdates.size() == 0) {
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
    public boolean prepareForLaunch(ProgressDialog progressDialog, Path nativesTempDir) {
        PerformanceManager.start();
        OkHttpClient httpClient = Network.createProgressClient(progressDialog);

        // make sure latest manifest is being used
        PerformanceManager.start("Grabbing Latest Manifest");
        try {
            progressDialog.setLabel(GetText.tr("Grabbing Latest Manifest"));
            VersionManifestVersion minecraftVersionManifest = MinecraftManager
                    .getMinecraftVersion(id);

            String[] urlParts = minecraftVersionManifest.url.split("/");
            String sha1 = urlParts[urlParts.length - 2];

            com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
                    .cached()
                    .setUrl(minecraftVersionManifest.url).withHttpClient(httpClient);

            if (sha1.length() == 40) {
                download = download.hash(sha1);
            }

            MinecraftVersion minecraftVersion = download.asClass(MinecraftVersion.class);

            if (minecraftVersion != null) {
                setUpdatedValues(minecraftVersion);
                save();
            }
        } catch (Exception e) {
            // ignored
        }
        progressDialog.doneTask();
        PerformanceManager.end("Grabbing Latest Manifest");

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

        if (logging != null) {
            PerformanceManager.start("Downloading Logging Config");
            try {
                progressDialog.setLabel(GetText.tr("Downloading Logging Config"));

                LoggingFile loggingFile = logging.client.file;

                com.atlauncher.network.Download loggerDownload = com.atlauncher.network.Download.build().cached()
                        .setUrl(loggingFile.url).hash(loggingFile.sha1)
                        .size(loggingFile.size).downloadTo(FileSystem.RESOURCES_LOG_CONFIGS.resolve(loggingFile.id))
                        .withHttpClient(httpClient);

                if (loggerDownload.needToDownload()) {
                    progressDialog.setTotalBytes(loggingFile.size);
                    loggerDownload.downloadFile();
                }

                progressDialog.doneTask();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                PerformanceManager.end("Downloading Logging Config");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Downloading Logging Config");
        } else {
            progressDialog.doneTask();
        }

        // download libraries
        PerformanceManager.start("Downloading Libraries");
        progressDialog.setLabel(GetText.tr("Downloading Libraries"));
        DownloadPool librariesPool = new DownloadPool();

        // get non native libraries otherwise we double up
        this.libraries.stream()
                .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                        && library.downloads.artifact.url != null && !library.hasNativeForOS())
                .distinct().forEach(library -> {
                    com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                            .setUrl(library.downloads.artifact.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                            .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                            .withHttpClient(httpClient);

                    librariesPool.add(download);
                });

        this.libraries.stream().filter(Library::hasNativeForOS).forEach(library -> {
            com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

            librariesPool.add(new com.atlauncher.network.Download().setUrl(download.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(download.path)).hash(download.sha1).size(download.size)
                    .withHttpClient(httpClient));
        });

        DownloadPool smallLibrariesPool = librariesPool.downsize();

        progressDialog.setTotalBytes(smallLibrariesPool.totalSize());

        smallLibrariesPool.downloadAll();

        progressDialog.doneTask();
        PerformanceManager.end("Downloading Libraries");

        // download Java runtime
        PerformanceManager.start("Java Runtime");
        if (javaVersion != null && Data.JAVA_RUNTIMES != null && (!OS.isArm() || OS.isMacArm()) && Optional
                .ofNullable(launcher.useJavaProvidedByMinecraft).orElse(App.settings.useJavaProvidedByMinecraft)) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeSystemString = JavaRuntimes.getSystem();

            // if the runtime isn't found, try a force refresh of them
            if (!runtimesForSystem.containsKey(javaVersion.component)) {
                MinecraftManager.loadJavaRuntimes(true);

                runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            }

            if (runtimesForSystem.containsKey(javaVersion.component)) {
                progressDialog.setLabel(GetText.tr("Downloading Java Runtime {0}", javaVersion.majorVersion));

                JavaRuntime runtimeToDownload = runtimesForSystem.get(javaVersion.component).get(0);

                try {
                    JavaRuntimeManifest javaRuntimeManifest = com.atlauncher.network.Download.build().cached()
                            .setUrl(runtimeToDownload.manifest.url).size(runtimeToDownload.manifest.size)
                            .hash(runtimeToDownload.manifest.sha1).downloadTo(FileSystem.MINECRAFT_RUNTIMES
                                    .resolve(javaVersion.component).resolve("manifest.json"))
                            .asClassWithThrow(JavaRuntimeManifest.class);

                    DownloadPool pool = new DownloadPool();

                    // create root directory
                    Path runtimeSystemDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(javaVersion.component)
                            .resolve(runtimeSystemString);
                    Path runtimeDirectory = runtimeSystemDirectory.resolve(javaVersion.component);
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
                    // Files.write(runtimeSystemDirectory.resolve(javaVersion.component
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
        MojangAssetIndex assetIndex = this.assetIndex;

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

        if (smallPool.size() != 0) {
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
        this.libraries.stream().filter(Library::shouldInstall).forEach(library -> {
            if (library.hasNativeForOS()) {
                if ((library.name.contains("glfw") && useSystemGlfw)
                        || (library.name.contains("openal") && useSystemOpenAl)) {
                    return;
                }

                Path nativePath = FileSystem.LIBRARIES.resolve(library.getNativeDownloadForOS().path);

                ArchiveUtils.extract(nativePath, nativesTempDir, name -> {
                    if (library.extract != null && library.extract.shouldExclude(name)) {
                        return null;
                    }

                    return name;
                });
            }
        });

        progressDialog.doneTask();
        PerformanceManager.end("Extracting Natives");

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

        PerformanceManager.end();
        return true;
    }

    public boolean launch() {
        return launch(false);
    }

    public boolean launch(boolean offline) {
        final AbstractAccount account = launcher.account == null ? AccountManager.getSelectedAccount()
                : AccountManager.getAccountByName(launcher.account);

        if (account == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Cannot play instance as you have no account selected.")).build())
                    .setType(DialogManager.ERROR).show();

            App.launcher.setMinecraftLaunched(false);
            return false;
        }

        // if Microsoft account must login again, then make sure to do that
        if (!offline && account instanceof MicrosoftAccount && ((MicrosoftAccount) account).mustLogin) {
            if (!((MicrosoftAccount) account).ensureAccountIsLoggedIn()) {
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

        int maximumMemory = (this.launcher.maximumMemory == null) ? App.settings.maximumMemory
                : this.launcher.maximumMemory;
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
        int permGen = (this.launcher.permGen == null) ? App.settings.metaspace : this.launcher.permGen;
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

        try {
            Files.createDirectory(nativesTempDir);
        } catch (IOException e2) {
            LogManager.logStackTrace(e2, false);
        }

        ProgressDialog<Boolean> prepareDialog = new ProgressDialog<>(GetText.tr("Preparing For Launch"), 7,
                GetText.tr("Preparing For Launch"));
        prepareDialog.addThread(new Thread(() -> {
            LogManager.info("Preparing for launch!");
            prepareDialog.setReturnValue(prepareForLaunch(prepareDialog, nativesTempDir));
            prepareDialog.close();
        }));
        prepareDialog.start();

        if (prepareDialog.getReturnValue() == null || !prepareDialog.getReturnValue()) {
            LogManager.error(
                    "Failed to prepare instance " + this.launcher.name + " for launch. Check the logs and try again.");
            return false;
        }

        Analytics.sendEvent(this.launcher.pack + " - " + this.launcher.version, offline ? "PlayOffline" : "Play",
                getAnalyticsCategory());

        Thread launcher = new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(false);
                }

                LogManager.info("Launching pack " + this.launcher.pack + " " + this.launcher.version + " for "
                        + "Minecraft " + this.id);

                Process process = null;

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

                if (account instanceof MojangAccount) {
                    MojangAccount mojangAccount = (MojangAccount) account;
                    LoginResponse session;

                    if (offline) {
                        session = new LoginResponse(mojangAccount.username);
                        session.setOffline();
                    } else {
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<LoginResponse> loginDialog = new ProgressDialog<>(
                                GetText.tr("Logging Into Minecraft"), 0, GetText.tr("Logging Into Minecraft"),
                                "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(mojangAccount.login());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        session = loginDialog.getReturnValue();

                        if (session == null) {
                            App.launcher.setMinecraftLaunched(false);
                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }
                            return;
                        }
                    }

                    if (enableCommands && preLaunchCommand != null) {
                        if (!executeCommand(preLaunchCommand)) {
                            LogManager.error("Failed to execute pre-launch command");

                            App.launcher.setMinecraftLaunched(false);

                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }

                            return;
                        }
                    }

                    process = MCLauncher.launch(mojangAccount, this, session, nativesTempDir, wrapperCommand, username);
                } else if (account instanceof MicrosoftAccount) {
                    MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;

                    if (!offline) {
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<Boolean> loginDialog = new ProgressDialog<>(GetText.tr("Logging Into Minecraft"),
                                0, GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(microsoftAccount.ensureAccessTokenValid());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        if (!(Boolean) loginDialog.getReturnValue()) {
                            LogManager.error("Failed to login");
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

                            App.launcher.setMinecraftLaunched(false);

                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }

                            return;
                        }
                    }

                    process = MCLauncher.launch(microsoftAccount, this, nativesTempDir, wrapperCommand, username);
                }

                if (process == null) {
                    LogManager.error("Failed to get process for Minecraft");
                    App.launcher.setMinecraftLaunched(false);
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(true);
                    }
                    return;
                }

                if (this.getPack() != null && this.getPack().isLoggingEnabled() && !this.launcher.isDev
                        && App.settings.enableLogs) {
                    App.TASKPOOL.execute(() -> {
                        addPlay(this.launcher.version);
                    });
                }

                if ((App.autoLaunch != null && App.closeLauncher)
                        || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                    System.exit(0);
                }

                if (Optional.ofNullable(this.launcher.enableDiscordIntegration)
                        .orElse(App.settings.enableDiscordIntegration)) {
                    App.ensureDiscordIsInitialized();

                    String playing = this.launcher.pack
                            + (this.launcher.multiMCManifest != null ? " (" + this.launcher.version + ")" : "");

                    DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder("");
                    presence.setDetails(playing);
                    presence.setStartTimestamps(System.currentTimeMillis());

                    if (this.getPack() != null && this.getPack().hasDiscordImage()) {
                        presence.setBigImage(this.getPack().getSafeName().toLowerCase(), playing);
                        presence.setSmallImage("atlauncher", "ATLauncher");
                    } else {
                        presence.setBigImage("atlauncher", playing);
                    }

                    DiscordRPC.discordUpdatePresence(presence.build());
                }

                App.launcher.showKillMinecraft(process);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(isr);
                String line;
                int detectedError = 0;

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
                            "class jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class")) {
                        detectedError = MinecraftError.USING_NEWER_JAVA_THAN_8;
                    }

                    if (!LogManager.showDebug) {
                        line = line.replace(account.minecraftUsername, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.username, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.uuid, "**UUID**");
                        line = line.replace(replaceUUID, "**UUID**");
                    }

                    if (account.getAccessToken() != null) {
                        line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
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
                if (App.discordInitialized) {
                    DiscordRPC.discordClearPresence();
                }
                int exitValue = 0; // Assume we exited fine
                try {
                    exitValue = process.exitValue(); // Try to get the real exit value
                } catch (IllegalThreadStateException e) {
                    process.destroy(); // Kill the process
                }
                if (!App.settings.keepLauncherOpen) {
                    App.console.setVisible(false); // Hide the console to pretend we've closed
                }

                if (exitValue != 0) {
                    LogManager.error(
                            "Oh no. Minecraft crashed. Please check the logs for any errors and provide these logs when asking for support.");

                    if (this.getPack() != null && !this.getPack().system) {
                        LogManager.info("Checking for modifications to the pack since installation.");
                        this.launcher.mods.forEach(mod -> {
                            if (!mod.userAdded && mod.wasSelected && mod.disabled) {
                                LogManager.warn("The mod " + mod.name + " (" + mod.file + ") has been disabled.");
                            }
                        });

                        Files.list(
                                this.ROOT.resolve("mods")).filter(
                                        file -> Files.isRegularFile(file)
                                                && this.launcher.mods.stream()
                                                        .noneMatch(m -> m.type == Type.mods && !m.userAdded
                                                                && m.getFile(this).toPath().equals(file)))
                                .forEach(newMod -> {
                                    LogManager.warn("The mod " + newMod.getFileName().toString() + " has been added.");
                                });
                    }

                    // Submit any pending crash reports from Open Eye if need to since we
                    // exited abnormally
                    if (App.settings.enableLogs && App.settings.enableOpenEyeReporting) {
                        App.TASKPOOL.submit(this::sendOpenEyePendingReports);
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
                if (this.getPack() != null && this.getPack().isLoggingEnabled() && !this.launcher.isDev
                        && App.settings.enableLogs) {
                    final int timePlayed = (int) (end - start) / 1000;
                    if (timePlayed > 0) {
                        App.TASKPOOL.submit(() -> {
                            addTimePlayed(timePlayed, this.launcher.version);
                        });
                    }
                }
                if (App.settings.enableAutomaticBackupAfterLaunch) {
                    backup();
                }
                if (App.settings.keepLauncherOpen) {
                    App.launcher.reloadInstancesPanel();
                    App.launcher.updateData();
                }
                if (Files.isDirectory(nativesTempDir)) {
                    FileUtils.deleteDirectory(nativesTempDir);
                }
                if (usesCustomMinecraftJar() && Files.exists(getCustomMinecraftJarLibraryPath())) {
                    FileUtils.delete(getCustomMinecraftJarLibraryPath());
                }
                if (!App.settings.keepLauncherOpen) {
                    System.exit(0);
                }
            } catch (Exception e1) {
                LogManager.logStackTrace(e1);
                App.launcher.setMinecraftLaunched(false);
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(true);
                }
            }
        });

        this.setLastPlayed(Instant.now());
        this.incrementNumberOfPlays();
        this.save();

        launcher.start();
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

    public void sendOpenEyePendingReports() {
        File reportsDir = this.getRoot().resolve("reports").toFile();
        if (reportsDir.exists()) {
            for (String filename : reportsDir.list(Utils.getOpenEyePendingReportsFileFilter())) {
                File report = new File(reportsDir, filename);
                LogManager.info("OpenEye: Sending pending crash report located at '" + report.getAbsolutePath() + "'");
                OpenEyeReportResponse response = Utils.sendOpenEyePendingReport(report);
                if (response == null) {
                    // Pending report was never sent due to an issue. Won't delete the file in case
                    // it's
                    // a temporary issue and can be sent again later.
                    LogManager.error("OpenEye: Couldn't send pending crash report!");
                } else {
                    // OpenEye returned a response to the report, display that to user if needed.
                    LogManager.info("OpenEye: Pending crash report sent! URL: " + response.getURL());
                    if (response.hasNote()) {
                        int ret = DialogManager.optionDialog().setTitle(GetText.tr("About Your Crash"))
                                .setContent(new HTMLBuilder().center().text(GetText.tr(
                                        "We detected a previous unreported crash generated by the OpenEye mod.<br/><br/>This has now been sent off to OpenEye and you can open the crash report below or continue without viewing it.")
                                        + "<br/><br/>" + response.getNoteDisplay()
                                        + GetText.tr(
                                                "You can turn this off by unchecking the OpenEye Reporting setting in the Settings tab. Click Ok to continue."))
                                        .build())
                                .setType(DialogManager.INFO).addOption(GetText.tr("Open Crash Report"))
                                .addOption(GetText.tr("Ok"), true).show();

                        if (ret == 0) {
                            OS.openWebBrowser(response.getURL());
                        }
                    }
                }
                Utils.delete(report); // Delete the pending report since we've sent it
            }
        }
    }

    public String addPlay(String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);

        try {
            return Utils.sendAPICall("pack/" + this.getPack().getSafeName() + "/play", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Play Not Added!";
    }

    public String addTimePlayed(int time, String version) {
        Map<String, Object> request = new HashMap<>();

        request.put("version", version);
        request.put("time", time);

        try {
            return Utils.sendAPICall("pack/" + this.getPack().getSafeName() + "/timeplayed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Leaderboard Time Not Added!";
    }

    public DisableableMod getDisableableModByCurseModId(int curseModId) {
        return this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromCurseForge() && installedMod.getCurseForgeModId() == curseModId)
                .findFirst().orElse(null);
    }

    public void addFileFromCurseForge(CurseForgeProject mod, CurseForgeFile file, ProgressDialog dialog) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID
                ? this.getRoot().resolve("resourcepacks").resolve(file.fileName)
                : (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID
                        ? this.getRoot().resolve("saves").resolve(file.fileName)
                        : this.getRoot().resolve("mods").resolve(file.fileName));

        // find mods with the same CurseForge project id
        List<DisableableMod> sameMods = this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurseForge()
                        && installedMod.getCurseForgeModId() == mod.id)
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        // TODO: for some reason we never checked hashes, even when downloading from the
        // api, so do that at some point when it's not 4am on a weekday
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
            String filename = file.fileName.replace(" ", "+");
            File fileLocation = downloadLocation.toFile();
            if (!fileLocation.exists()) {
                File downloadsFolderFile = new File(FileSystem.getUserDownloadsPath().toFile(), filename);
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                }

                while (!fileLocation.exists()) {
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
                                .showWithFileMonitoring(fileLocation, downloadsFolderFile, file.fileLength, 1);

                        if (retValue == DialogManager.CLOSED_OPTION) {
                            return;
                        } else if (retValue == 0) {
                            OS.openFileExplorer(FileSystem.DOWNLOADS);
                        }
                    } while (retValue != 1);

                    if (!fileLocation.exists()) {
                        // Check users downloads folder to see if it's there
                        if (downloadsFolderFile.exists()) {
                            Utils.moveFile(downloadsFolderFile, fileLocation, true);
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

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.name, file.displayName, true, file.fileName,
                mod.getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID ? Type.resourcepack
                        : (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID ? Type.worlds
                                : Type.mods),
                null, mod.summary, false, true, true, false, mod, file));

        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
    }

    public void addFileFromModrinth(ModrinthProject mod, ModrinthVersion version, ProgressDialog dialog) {
        ModrinthFile fileToDownload = version.getPrimaryFile();

        Path downloadLocation = FileSystem.DOWNLOADS.resolve(fileToDownload.filename);
        Path finalLocation = this.getRoot().resolve("mods").resolve(fileToDownload.filename);
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

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.title, version.name, true, fileToDownload.filename, Type.mods,
                null, mod.description, false, true, true, false, mod, version));

        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.title));
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

    public Map<String, Object> getShareCodeData() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> mods = new HashMap<>();
        List<Map<String, Object>> optional = new ArrayList<>();

        this.launcher.mods.stream().filter(mod -> mod.optional && !mod.userAdded).forEach(mod -> {
            Map<String, Object> modInfo = new HashMap<>();
            modInfo.put("name", mod.name);
            modInfo.put("selected", true);
            optional.add(modInfo);
        });

        mods.put("optional", optional);
        data.put("mods", mods);

        return data;
    }

    public boolean canBeExported() {
        if (launcher.loaderVersion == null) {
            LogManager.debug("Instance " + launcher.name + " cannot be exported due to: No loader");
            return false;
        }

        return true;
    }

    public boolean export(String name, String version, String author, InstanceExportFormat format, String saveTo,
            List<String> overrides) {
        if (format == InstanceExportFormat.CURSEFORGE) {
            return exportAsCurseForgeZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MODRINTH) {
            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.CURSEFORGE_AND_MODRINTH) {
            if (!exportAsCurseForgeZip(name, version, author, saveTo, overrides)) {
                return false;
            }

            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MULTIMC) {
            return exportAsMultiMcZip(name, version, author, saveTo, overrides);
        }

        return false;
    }

    public boolean exportAsMultiMcZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(safePathName + ".zip");
        MultiMCManifest manifest = new MultiMCManifest();

        manifest.formatVersion = 1;

        manifest.components = new ArrayList<>();

        // lwjgl 3
        MultiMCComponent lwjgl3Component = new MultiMCComponent();
        lwjgl3Component.cachedName = "LWJGL 3";
        lwjgl3Component.cachedVersion = "3.2.2";
        lwjgl3Component.cachedVolatile = true;
        lwjgl3Component.dependencyOnly = true;
        lwjgl3Component.uid = "org.lwjgl3";
        lwjgl3Component.version = "3.2.2";
        manifest.components.add(lwjgl3Component);

        // minecraft
        MultiMCComponent minecraftComponent = new MultiMCComponent();
        minecraftComponent.cachedName = "Minecraft";

        minecraftComponent.cachedRequires = new ArrayList<>();
        MultiMCRequire lwjgl3Require = new MultiMCRequire();
        lwjgl3Require.equals = "3.2.2";
        lwjgl3Require.suggests = "3.2.2";
        lwjgl3Require.uid = "org.lwjgl3";
        minecraftComponent.cachedRequires.add(lwjgl3Require);

        minecraftComponent.cachedVersion = id;
        minecraftComponent.cachedVolatile = true;
        minecraftComponent.dependencyOnly = true;
        minecraftComponent.uid = "org.lwjgl3";
        minecraftComponent.version = "3.2.2";
        manifest.components.add(minecraftComponent);

        // fabric loader
        if (launcher.loaderVersion.type.equals("Fabric")) {
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
        if (launcher.loaderVersion.type.equals("Forge")) {
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
        if (launcher.loaderVersion.type.equals("Quilt")) {
            // mappings
            MultiMCComponent quiltMappingsComponent = new MultiMCComponent();
            quiltMappingsComponent.cachedName = "Hashed Mappings";

            quiltMappingsComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire minecraftRequire = new MultiMCRequire();
            minecraftRequire.equals = id;
            minecraftRequire.uid = "net.minecraft";
            quiltMappingsComponent.cachedRequires.add(minecraftRequire);

            quiltMappingsComponent.cachedVersion = id;
            quiltMappingsComponent.cachedVolatile = true;
            quiltMappingsComponent.dependencyOnly = true;
            quiltMappingsComponent.uid = "org.quiltmc.hashed";
            quiltMappingsComponent.version = id;
            manifest.components.add(quiltMappingsComponent);

            // loader
            MultiMCComponent quiltLoaderComponent = new MultiMCComponent();
            quiltLoaderComponent.cachedName = "Fabric Loader";

            quiltLoaderComponent.cachedRequires = new ArrayList<>();
            MultiMCRequire hashedRequire = new MultiMCRequire();
            hashedRequire.uid = "org.quiltmc.hashed";
            quiltLoaderComponent.cachedRequires.add(hashedRequire);

            quiltLoaderComponent.cachedVersion = launcher.loaderVersion.version;
            quiltLoaderComponent.uid = "org.quiltmc.quilt-loader";
            quiltLoaderComponent.version = launcher.loaderVersion.version;
            manifest.components.add(quiltLoaderComponent);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.launcher.name + "-export");
        FileUtils.createDirectory(tempDir);

        // create mmc-pack.json
        try (FileWriter fileWriter = new FileWriter(tempDir.resolve("mmc-pack.json").toFile())) {
            Gsons.MINECRAFT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return false;
        }

        // create instance.cfg
        Path instanceCfgPath = tempDir.resolve("instance.cfg");
        Properties instanceCfg = new Properties();

        String iconKey = "default";
        if (hasCustomImage()) {
            String customIconFileName = "atlauncher_" + getSafeName().toLowerCase();
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
        instanceCfg.setProperty("MinMemAlloc",
                Optional.ofNullable(launcher.initialMemory).orElse(App.settings.initialMemory) + "");
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

            return false;
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

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return true;
    }

    public boolean exportAsCurseForgeZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.zip", safePathName, version));
        CurseForgeManifest manifest = new CurseForgeManifest();

        // for any mods not from CurseForge, scan for them on CurseForge
        if (!App.settings.dontCheckModsOnCurseForge) {
            Map<Long, DisableableMod> murmurHashes = new HashMap<>();

            this.launcher.mods.stream()
                    .filter(m -> !m.disabled && !m.isFromCurseForge())
                    .forEach(dm -> {
                        try {
                            long hash = Hashing.murmur(dm.getFile(this.ROOT, this.id).toPath());
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
        this.save();

        CurseForgeMinecraft minecraft = new CurseForgeMinecraft();

        List<CurseForgeModLoader> modLoaders = new ArrayList<>();
        CurseForgeModLoader modLoader = new CurseForgeModLoader();

        String loaderType = launcher.loaderVersion.type.toLowerCase();
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
        manifest.files = this.launcher.mods.stream().filter(m -> !m.disabled && m.isFromCurseForge()).map(mod -> {
            CurseForgeManifestFile file = new CurseForgeManifestFile();
            file.projectID = mod.curseForgeProjectId;
            file.fileID = mod.curseForgeFileId;
            file.required = true;

            return file;
        }).collect(Collectors.toList());
        manifest.overrides = "overrides";

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.launcher.name + "-export");
        FileUtils.createDirectory(tempDir);

        // create manifest.json
        try (FileWriter fileWriter = new FileWriter(tempDir.resolve("manifest.json").toFile())) {
            Gsons.MINECRAFT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save manifest.json", e);

            FileUtils.deleteDirectory(tempDir);

            return false;
        }

        // create modlist.html
        StringBuilder sb = new StringBuilder("<ul>");
        this.launcher.mods.stream().filter(m -> !m.disabled && m.isFromCurseForge()).forEach(mod -> {
            if (mod.hasFullCurseForgeInformation()) {
                sb.append("<li><a href=\"").append(mod.curseForgeProject.getWebsiteUrl()).append("\">").append(mod.name)
                        .append("</a></li>");
            } else {
                sb.append("<li>").append(mod.name).append("</li>");
            }
        });
        sb.append("</ul>");

        try (FileWriter fileWriter = new FileWriter(tempDir.resolve("modlist.html").toFile())) {
            fileWriter.write(sb.toString());
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modlist.html", e);

            FileUtils.deleteDirectory(tempDir);

            return false;
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

        // remove files that come from CurseForge or aren't disabled
        launcher.mods.stream().filter(m -> !m.disabled && m.isFromCurseForge()).forEach(mod -> {
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

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return true;
    }

    public boolean exportAsModrinthZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        String safePathName = name.replaceAll("[\\\"?:*<>|]", "");
        Path to = Paths.get(saveTo).resolve(String.format("%s %s.mrpack", safePathName, version));
        ModrinthModpackManifest manifest = new ModrinthModpackManifest();

        // for any mods not from Modrinth, scan for them on Modrinth
        if (!App.settings.dontCheckModsOnModrinth) {
            List<DisableableMod> nonModrinthMods = this.launcher.mods.parallelStream()
                    .filter(m -> !m.disabled && !m.isFromModrinth() && m.getFile(this).exists())
                    .collect(Collectors.toList());

            String[] sha1Hashes = nonModrinthMods.parallelStream()
                    .map(m -> Hashing.sha1(m.getFile(this).toPath()).toString()).toArray(String[]::new);

            Map<String, ModrinthVersion> modrinthVersions = ModrinthApi.getVersionsFromSha1Hashes(sha1Hashes);

            if (modrinthVersions.size() != 0) {
                Map<String, ModrinthProject> modrinthProjects = ModrinthApi.getProjectsAsMap(
                        modrinthVersions.values().parallelStream().map(mv -> mv.projectId).toArray(String[]::new));

                nonModrinthMods.parallelStream().forEach(mod -> {
                    String hash = Hashing.sha1(mod.getFile(this).toPath()).toString();

                    if (modrinthVersions.containsKey(hash)) {
                        ModrinthVersion modrinthVersion = modrinthVersions.get(hash);

                        mod.modrinthVersion = modrinthVersion;
                        mod.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);

                        LogManager.debug("Found matching mod from Modrinth called " + mod.modrinthProject.title);
                    }
                });
                this.save();
            }
        }

        manifest.formatVersion = 1;
        manifest.game = "minecraft";
        manifest.versionId = version;
        manifest.name = name;
        manifest.summary = this.launcher.description;
        manifest.files = this.launcher.mods.parallelStream()
                .filter(m -> !m.disabled && m.isFromModrinth() && m.getFile(this).exists()).map(mod -> {
                    Path modPath = mod.getFile(this).toPath();

                    ModrinthModpackFile file = new ModrinthModpackFile();
                    file.path = this.ROOT.relativize(modPath).toString().replace("\\", "/");

                    String sha1Hash = Hashing.sha1(modPath).toString();

                    file.hashes = new HashMap<>();
                    file.hashes.put("sha1", sha1Hash);
                    file.hashes.put("sha512", Hashing.sha512(modPath).toString());

                    file.env = new HashMap<>();
                    file.env.put("client",
                            mod.modrinthProject.clientSide == ModrinthSide.UNSUPPORTED ? "unsupported"
                                    : "required");
                    file.env.put("server",
                            mod.modrinthProject.serverSide == ModrinthSide.UNSUPPORTED ? "unsupported"
                                    : "required");

                    file.fileSize = modPath.toFile().length();

                    file.downloads = new ArrayList<>();
                    String downloadUrl = "";
                    if (mod.isFromModrinth()) {
                        downloadUrl = mod.modrinthVersion.getFileBySha1(sha1Hash).url;
                    }
                    file.downloads.add(HttpUrl.get(downloadUrl).toString());

                    return file;
                }).collect(Collectors.toList());
        manifest.dependencies = new HashMap<>();

        manifest.dependencies.put("minecraft", this.id);

        if (this.launcher.loaderVersion != null) {
            manifest.dependencies.put(this.launcher.loaderVersion.getTypeForModrinthExport(),
                    this.launcher.loaderVersion.version);
        }

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.launcher.name + "-export");
        FileUtils.createDirectory(tempDir);

        // create modrinth.index.json
        try (FileWriter fileWriter = new FileWriter(tempDir.resolve("modrinth.index.json").toFile())) {
            Gsons.MINECRAFT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save modrinth.index.json", e);

            FileUtils.deleteDirectory(tempDir);

            return false;
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

        // remove files that come from Modrinth or aren't disabled
        launcher.mods.stream().filter(m -> !m.disabled && m.isFromModrinth()).forEach(mod -> {
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

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return true;
    }

    public boolean rename(String newName) {
        String oldName = this.launcher.name;
        File oldDir = getRoot().toFile();
        this.launcher.name = newName;
        this.ROOT = FileSystem.INSTANCES.resolve(this.getSafeName());
        File newDir = getRoot().toFile();
        if (oldDir.renameTo(newDir)) {
            this.save();
            return true;
        } else {
            this.launcher.name = oldName;
            return false;
        }
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(this.getRoot().resolve("instance.json").toFile())) {
            Gsons.MINECRAFT.toJson(this, fileWriter);
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

    public String getName() {
        return launcher.name;
    }

    public String getPackName() {
        return launcher.pack;
    }

    public String getVersion() {
        return launcher.version;
    }

    public LoaderVersion getLoaderVersion() {
        return launcher.loaderVersion;
    }

    public void setNumberOfPlays(final long val) {
        this.numPlays = val;
    }

    public long incrementNumberOfPlays() {
        return this.numPlays++;
    }

    public long decrementNumberOfPlays() {
        return this.numPlays--;
    }

    public long getNumberOfPlays() {
        return this.numPlays;
    }

    public void setLastPlayed(final Instant ts) {
        this.lastPlayed = ts;
    }

    public Instant getLastPlayed() {
        return this.lastPlayed;
    }

    public Instant getLastPlayedOrEpoch() {
        return this.lastPlayed != null ? this.lastPlayed : Instant.EPOCH;
    }

    public String getMainClass() {
        return mainClass;
    }

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

    public boolean isModpacksChPack() {
        return launcher.modpacksChPackManifest != null && launcher.modpacksChPackVersionManifest != null;
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
        return isOldCurseForgePack() || isCurseForgePack() || isModpacksChPack() || isModrinthImport()
                || isMultiMcImport() || isTechnicPack() || isModrinthPack();
    }

    public boolean isUpdatableExternalPack() {
        return isExternalPack() && ((isModpacksChPack()
                && ConfigManager.getConfigItem("platforms.modpacksch.modpacksEnabled", true) == true)
                || (isCurseForgePack()
                        && ConfigManager.getConfigItem("platforms.curseforge.modpacksEnabled", true) == true)
                || (isTechnicPack() && ConfigManager.getConfigItem("platforms.technic.modpacksEnabled", true) == true)
                || (isModrinthPack()
                        && ConfigManager.getConfigItem("platforms.modrinth.modpacksEnabled", true) == true));
    }

    public String getAnalyticsCategory() {
        if (isCurseForgePack()) {
            return "CurseForgeInstance";
        }

        if (isModpacksChPack()) {
            return "ModpacksChInstance";
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

    public boolean hasWebsite() {
        if (isCurseForgePack()) {
            return launcher.curseForgeProject.hasWebsiteUrl();
        }

        if (isModpacksChPack()) {
            return launcher.modpacksChPackManifest.hasTag("FTB");
        }

        return isModrinthPack() || isTechnicPack();
    }

    public String getWebsiteUrl() {
        if (isCurseForgePack() && launcher.curseForgeProject.hasWebsiteUrl()) {
            return launcher.curseForgeProject.getWebsiteUrl();
        }

        if (isModpacksChPack() && launcher.modpacksChPackManifest.hasTag("FTB")) {
            return launcher.modpacksChPackManifest.getWebsiteUrl();
        }

        if (isModrinthPack()) {
            return String.format("https://modrinth.com/modpack/%s", launcher.modrinthProject.slug);
        }

        if (isTechnicPack()) {
            return launcher.technicModpack.platformUrl;
        }

        return null;
    }

    public void update() {
        new InstanceInstallerDialog(this, true, false, null, null, true, null, App.launcher.getParent());
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
        final JDialog dialog = new JDialog(App.launcher.getParent(), GetText.tr("Backing Up {0}", launcher.name),
                ModalityType.DOCUMENT_MODAL);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
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

        Analytics.sendEvent(launcher.pack + " - " + launcher.version, "Backup", getAnalyticsCategory());

        final Thread backupThread = new Thread(() -> {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().replaceAll("[^0-9]", "_");
            String filename = getSafeName() + "-" + time.substring(0, time.lastIndexOf("_")) + ".zip";

            ArchiveUtils.createZip(getRoot(), FileSystem.BACKUPS.resolve(filename),
                    ZipNameMapper.getMapperForBackupMode(backupMode));

            dialog.dispose();
            App.TOASTER.pop(GetText.tr("Backup is complete"));
        });
        backupThread.start();
        dialog.addWindowListener(new WindowAdapter() {
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
        Analytics.sendEvent(launcher.pack + " - " + launcher.version, "Reinstall", getAnalyticsCategory());
        new InstanceInstallerDialog(this);
    }

    public void startRename() {
        Analytics.sendEvent(launcher.pack + " - " + launcher.version, "Rename", getAnalyticsCategory());
        new RenameInstanceDialog(this);
    }

    public void startClone() {
        String clonedName = JOptionPane.showInputDialog(App.launcher.getParent(),
                GetText.tr("Enter a new name for this cloned instance."),
                GetText.tr("Cloning Instance"), JOptionPane.INFORMATION_MESSAGE);

        if (clonedName != null && clonedName.length() >= 1
                && InstanceManager.getInstanceByName(clonedName) == null
                && InstanceManager
                        .getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
                && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1 && !Files.exists(
                        FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            Analytics.sendEvent(launcher.pack + " - " + launcher.version, "Clone",
                    getAnalyticsCategory());

            final String newName = clonedName;
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Cloning Instance"), 0,
                    GetText.tr("Cloning Instance. Please wait..."), null, App.launcher.getParent());
            dialog.addThread(new Thread(() -> {
                InstanceManager.cloneInstance(this, newName);
                dialog.close();
                App.TOASTER.pop(GetText.tr("Cloned Instance Successfully"));
            }));
            dialog.start();
        } else if (clonedName == null || clonedName.equals("")) {
            LogManager.error("Error Occurred While Cloning Instance! Dialog Closed/Cancelled!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
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
                Analytics.sendEvent(launcher.pack + " - " + launcher.version, "ChangeImage", getAnalyticsCategory());
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
        Analytics.sendEvent(launcher.loaderVersion.getAnalyticsValue(), launcher.pack + " - " + launcher.version,
                "ChangeLoaderVersion", getAnalyticsCategory());

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
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(
                            new HTMLBuilder().center()
                                    .text(GetText.tr("{0} {1} has been installed.",
                                            launcher.loaderVersion.getLoaderType(), loaderVersion.version))
                                    .build())
                    .setType(DialogManager.INFO).show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("{0} has not been installed. Check the console for more information.",
                                    launcher.loaderVersion.getLoaderType()))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public void addLoader(LoaderType loaderType) {
        Analytics.sendEvent(loaderType.getAnalyticsValue(), launcher.pack + " - " + launcher.version, "AddLoader",
                getAnalyticsCategory());

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
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("{0} {1} has been installed.", loaderType, loaderVersion.version)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", loaderType))
                    .setContent(new HTMLBuilder().center().text(GetText
                            .tr("{0} has not been installed. Check the console for more information.", loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    private LoaderVersion showLoaderVersionSelector(LoaderType loaderType) {
        ProgressDialog<List<LoaderVersion>> progressDialog = new ProgressDialog<>(
                GetText.tr("Checking For {0} Versions", loaderType), 0,
                GetText.tr("Checking For {0} Versions", loaderType));
        progressDialog.addThread(new Thread(() -> {
            if (loaderType == LoaderType.FABRIC) {
                progressDialog.setReturnValue(FabricLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.FORGE) {
                progressDialog.setReturnValue(ForgeLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.QUILT) {
                progressDialog.setReturnValue(QuiltLoader.getChoosableVersions(id));
            }

            progressDialog.doneTask();
            progressDialog.close();
        }));
        progressDialog.start();

        List<LoaderVersion> loaderVersions = progressDialog.getReturnValue();

        if (loaderVersions == null || loaderVersions.size() == 0) {
            DialogManager.okDialog().setTitle(GetText.tr("No Versions Available For {0}", loaderType))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("{0} has not been {1} as there are no versions available.", loaderType,
                                    launcher.loaderVersion == null ? GetText.tr("installed") : GetText.tr("changed")))
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
                .addItem(new ComboItem<LoaderVersion>(version, version.toStringWithCurrent(this))));

        if (loaderType == LoaderType.FORGE) {
            Optional<LoaderVersion> recommendedVersion = loaderVersions.stream().filter(lv -> lv.recommended)
                    .findFirst();

            if (recommendedVersion.isPresent()) {
                loaderVersionsDropDown.setSelectedIndex(loaderVersions.indexOf(recommendedVersion.get()));
            }
        }

        if (launcher.loaderVersion != null) {
            String loaderVersionString = launcher.loaderVersion.version;

            for (int i = 0; i < loaderVersionsDropDown.getItemCount(); i++) {
                LoaderVersion loaderVersion = ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getItemAt(i))
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
        box.add(new JLabel(GetText.tr("Select {0} Version To Install", loaderType)));
        box.add(Box.createHorizontalGlue());

        panel.add(box);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loaderVersionsDropDown);
        panel.add(Box.createVerticalStrut(20));

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), panel,
                launcher.loaderVersion == null ? GetText.tr("Installing {0}", loaderType)
                        : GetText.tr("Changing {0} Version", loaderType),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret != 0) {
            return null;
        }

        return ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getSelectedItem()).getValue();
    }

    public void removeLoader() {
        Analytics.sendEvent(launcher.loaderVersion.getAnalyticsValue(), launcher.pack + " - " + launcher.version,
                "RemoveLoader", getAnalyticsCategory());
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
            App.launcher.reloadInstancesPanel();

            DialogManager.okDialog().setTitle(GetText.tr("{0} Removed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("{0} has been removed from this instance.", loaderType)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Removed", loaderType))
                    .setContent(new HTMLBuilder().center().text(
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
        } catch (Exception e) {
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

    public boolean isUsingJavaRuntime() {
        return javaVersion != null && Optional.ofNullable(launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft);
    }

    public String getJavaPath() {
        String javaPath = Optional.ofNullable(launcher.javaPath).orElse(App.settings.javaPath);

        // are we using Mojangs provided runtime?
        if (isUsingJavaRuntime()) {
            Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(javaVersion.component)
                    .resolve(JavaRuntimes.getSystem()).resolve(javaVersion.component);

            if (OS.isMac()) {
                runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
            }

            if (Files.isDirectory(runtimeDirectory)) {
                javaPath = runtimeDirectory.toAbsolutePath().toString();
                LogManager.debug(String.format("Using Java runtime %s (major version %d) at path %s",
                        javaVersion.component, javaVersion.majorVersion, javaPath));
            }
        }

        return javaPath;
    }

    public boolean shouldShowWrongJavaWarning() {
        if (launcher.java == null) {
            return false;
        }

        String javaVersion = Java.getVersionForJavaPath(new File(getJavaPath()));

        if (javaVersion.equalsIgnoreCase("Unknown")) {
            return false;
        }

        int majorJavaVersion = Java.parseJavaVersionNumber(javaVersion);

        return !launcher.java.conforms(majorJavaVersion);
    }

    public String getVersionOfPack() {
        if (isModrinthPack()) {
            return String.format("%s (%s)", launcher.modrinthVersion.name, launcher.modrinthVersion.versionNumber);
        }

        return launcher.version;
    }
}
