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
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
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
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.CurseForgeProjectLatestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeManifestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeMinecraft;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthMod;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.multimc.MultiMCComponent;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.multimc.MultiMCRequire;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.CurseForgeUpdateManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ModpacksChUpdateManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.JsonIOException;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import okhttp3.OkHttpClient;

@Json
public class Instance extends MinecraftVersion {
    public String inheritsFrom;
    public InstanceLauncher launcher;

    public transient Path ROOT;

    public Instance(MinecraftVersion version) {
        this.id = version.id;
        this.complianceLevel = version.complianceLevel;
        this.javaVersion = version.javaVersion;
        this.arguments = version.arguments;
        this.minecraftArguments = version.minecraftArguments;
        this.type = version.type;
        this.time = version.time;
        this.releaseTime = version.releaseTime;
        this.minimumLauncherVersion = version.minimumLauncherVersion;
        this.assetIndex = version.assetIndex;
        this.assets = version.assets;
        this.downloads = version.downloads;
        this.logging = version.logging;
        this.libraries = version.libraries;
        this.rules = version.rules;
        this.mainClass = version.mainClass;
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
        if (this.isExternalPack()) {
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
            if (this.type == VersionManifestVersionType.SNAPSHOT) {
                return !MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.SNAPSHOT).get(0).id
                        .equals(this.id);
            } else if (this.type == VersionManifestVersionType.RELEASE) {
                return !MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.RELEASE).get(0).id
                        .equals(this.id);
            } else if (this.type == VersionManifestVersionType.OLD_BETA) {
                return !MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_BETA).get(0).id
                        .equals(this.id);
            } else if (this.type == VersionManifestVersionType.OLD_ALPHA) {
                return !MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_ALPHA).get(0).id
                        .equals(this.id);
            }
        } else if (this.isExternalPack()) {
            if (isModpacksChPack()) {
                ModpacksChPackVersion latestVersion = Data.MODPACKS_CH_INSTANCE_LATEST_VERSION.get(this);

                return latestVersion != null && latestVersion.id != this.launcher.modpacksChPackVersionManifest.id;
            } else if (isCurseForgePack()) {
                CurseForgeProjectLatestFile latestVersion = Data.CURSEFORGE_INSTANCE_LATEST_VERSION.get(this);

                return latestVersion != null && latestVersion.id != this.launcher.curseForgeFile.id;
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
            } catch (IOException e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of instance " + this.launcher.name, e);
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
            if (this.type == VersionManifestVersionType.SNAPSHOT) {
                version = MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.SNAPSHOT).get(0).id;
            } else if (this.type == VersionManifestVersionType.RELEASE) {
                version = MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.RELEASE).get(0).id;
            } else if (this.type == VersionManifestVersionType.OLD_BETA) {
                version = MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_BETA).get(0).id;
            } else if (this.type == VersionManifestVersionType.OLD_ALPHA) {
                version = MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_ALPHA).get(0).id;
            } else {
                return;
            }
        } else if (isExternalPack()) {
            if (isModpacksChPack()) {
                version = Integer.toString(ModpacksChUpdateManager.getLatestVersion(this).id);
            } else if (isCurseForgePack()) {
                version = Integer.toString(CurseForgeUpdateManager.getLatestVersion(this).id);
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
            if (this.type == VersionManifestVersionType.SNAPSHOT) {
                return hasUpdateBeenIgnored(
                        MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.SNAPSHOT).get(0).id);
            } else if (this.type == VersionManifestVersionType.RELEASE) {
                return hasUpdateBeenIgnored(
                        MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.RELEASE).get(0).id);
            } else if (this.type == VersionManifestVersionType.OLD_BETA) {
                return hasUpdateBeenIgnored(
                        MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_BETA).get(0).id);
            } else if (this.type == VersionManifestVersionType.OLD_ALPHA) {
                return hasUpdateBeenIgnored(
                        MinecraftManager.getFilteredMinecraftVersions(VersionManifestVersionType.OLD_ALPHA).get(0).id);
            }

            return false;
        }

        if (isExternalPack()) {
            if (isModpacksChPack()) {
                return hasUpdateBeenIgnored(Integer.toString(ModpacksChUpdateManager.getLatestVersion(this).id));
            } else if (isCurseForgePack()) {
                return hasUpdateBeenIgnored(Integer.toString(CurseForgeUpdateManager.getLatestVersion(this).id));
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

    /**
     * This will prepare the instance for launch. It will download the assets,
     * Minecraft jar and libraries, as well as organise the libraries, ready to be
     * played.
     */
    public boolean prepareForLaunch(ProgressDialog progressDialog, Path nativesTempDir) {
        OkHttpClient httpClient = Network.createProgressClient(progressDialog);

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
            return false;
        }

        // download libraries
        progressDialog.setLabel(GetText.tr("Downloading Libraries"));
        DownloadPool librariesPool = new DownloadPool();

        // get non native libraries otherwise we double up
        this.libraries.stream().filter(
                library -> library.shouldInstall() && library.downloads.artifact != null && !library.hasNativeForOS())
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

        // download Java runtime
        if (javaVersion != null && Data.JAVA_RUNTIMES != null && App.settings.useJavaProvidedByMinecraft) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeSystemString = JavaRuntimes.getSystem();

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

        // organise assets
        progressDialog.setLabel(GetText.tr("Downloading Resources"));
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

        progressDialog.setTotalBytes(smallPool.totalSize());

        smallPool.downloadAll();

        // copy resources to instance
        if (index.mapToResources || assetIndex.id.equalsIgnoreCase("legacy")) {
            index.objects.forEach((key, object) -> {
                String filename = object.hash.substring(0, 2) + "/" + object.hash;

                Path downloadedFile = FileSystem.RESOURCES_OBJECTS.resolve(filename);

                if (index.mapToResources) {
                    FileUtils.copyFile(downloadedFile, this.getRoot().resolve("resources/" + key), true);
                } else if (assetIndex.id.equalsIgnoreCase("legacy")) {
                    FileUtils.copyFile(downloadedFile, FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(key), true);
                }
            });
        }

        progressDialog.doneTask();

        progressDialog.setLabel(GetText.tr("Organising Libraries"));

        // extract natives to a temp dir
        this.libraries.stream().filter(Library::shouldInstall).forEach(library -> {
            if (library.hasNativeForOS()) {
                File nativeFile = FileSystem.LIBRARIES.resolve(library.getNativeDownloadForOS().path).toFile();

                ZipUtil.unpack(nativeFile, nativesTempDir.toFile(), name -> {
                    if (library.extract != null && library.extract.shouldExclude(name)) {
                        return null;
                    }

                    return name;
                });
            }
        });

        progressDialog.doneTask();

        return true;
    }

    public boolean launch() {
        final AbstractAccount account = launcher.account == null ? AccountManager.getSelectedAccount()
                : AccountManager.getAccountByName(launcher.account);

        if (account == null) {
            DialogManager.okDialog().setTitle(GetText.tr("No Account Selected"))
                    .setContent(new HTMLBuilder().center()
                            .text(GetText.tr("Cannot play instance as you have no account selected.")).build())
                    .setType(DialogManager.ERROR).show();

            App.launcher.setMinecraftLaunched(false);
            return false;
        } else {
            int maximumMemory = (this.launcher.maximumMemory == null) ? App.settings.maximumMemory
                    : this.launcher.maximumMemory;
            if ((maximumMemory < this.launcher.requiredMemory)
                    && (this.launcher.requiredMemory <= OS.getSafeMaximumRam())) {
                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Insufficient Ram"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "This pack has set a minimum amount of ram needed to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                                this.launcher.requiredMemory)).build())
                        .setLookAndFeel(DialogManager.YES_NO_OPTION).setType(DialogManager.ERROR)
                        .setDefaultOption(DialogManager.YES_OPTION).show();

                if (ret != 0) {
                    LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                    App.launcher.setMinecraftLaunched(false);
                    return false;
                }
            }
            int permGen = (this.launcher.permGen == null) ? App.settings.metaspace : this.launcher.permGen;
            if (permGen < this.launcher.requiredPermGen) {
                int ret = DialogManager.optionDialog().setTitle(GetText.tr("Insufficent Permgen"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "This pack has set a minimum amount of permgen to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                                this.launcher.requiredPermGen)).build())
                        .setLookAndFeel(DialogManager.YES_NO_OPTION).setType(DialogManager.ERROR)
                        .setDefaultOption(DialogManager.YES_OPTION).show();
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

            ProgressDialog<Boolean> prepareDialog = new ProgressDialog<>(GetText.tr("Preparing For Launch"), 5,
                    GetText.tr("Preparing For Launch"));
            prepareDialog.addThread(new Thread(() -> {
                LogManager.info("Preparing for launch!");
                prepareDialog.setReturnValue(prepareForLaunch(prepareDialog, nativesTempDir));
                prepareDialog.close();
            }));
            prepareDialog.start();

            if (prepareDialog.getReturnValue() == null || !prepareDialog.getReturnValue()) {
                LogManager.error("Failed to prepare instance " + this.launcher.name
                        + " for launch. Check the logs and try again.");
                return false;
            }

            Analytics.sendEvent(this.launcher.pack + " - " + this.launcher.version, "Play", getAnalyticsCategory());

            Thread launcher = new Thread(() -> {
                try {
                    long start = System.currentTimeMillis();
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(false);
                    }

                    LogManager.info("Launching pack " + this.launcher.pack + " " + this.launcher.version + " for "
                            + "Minecraft " + this.id);

                    Process process = null;

                    if (account instanceof MojangAccount) {
                        MojangAccount mojangAccount = (MojangAccount) account;
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<LoginResponse> loginDialog = new ProgressDialog<>(
                                GetText.tr("Logging Into Minecraft"), 0, GetText.tr("Logging Into Minecraft"),
                                "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(mojangAccount.login());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        final LoginResponse session = loginDialog.getReturnValue();

                        if (session == null) {
                            App.launcher.setMinecraftLaunched(false);
                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }
                            return;
                        }

                        process = MCLauncher.launch(mojangAccount, this, session, nativesTempDir);
                    } else if (account instanceof MicrosoftAccount) {
                        MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;

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

                        process = MCLauncher.launch(microsoftAccount, this, nativesTempDir);
                    }

                    if (process == null) {
                        LogManager.error("Failed to get process for Minecraft");
                        App.launcher.setMinecraftLaunched(false);
                        if (App.launcher.getParent() != null) {
                            App.launcher.getParent().setVisible(true);
                        }
                        return;
                    }

                    if ((App.autoLaunch != null && App.closeLauncher)
                            || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                        if (App.settings.enableLogs) {
                            addTimePlayed(1, this.launcher.version); // count the stats, just without time played
                        }

                        System.exit(0);
                    }

                    if (App.settings.enableDiscordIntegration && App.discordInitialized) {
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
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    int detectedError = 0;

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
                                "class jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class")) {
                            detectedError = MinecraftError.USING_NEWER_JAVA_THAN_8;
                        }

                        if (!LogManager.showDebug) {
                            line = line.replace(account.minecraftUsername, "**MINECRAFTUSERNAME**");
                            line = line.replace(account.username, "**MINECRAFTUSERNAME**");
                            line = line.replace(account.uuid, "**UUID**");
                            if (account.getAccessToken() != null) {
                                line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
                            }
                        }
                        LogManager.minecraft(line);
                    }
                    App.launcher.hideKillMinecraft();
                    if (App.launcher.getParent() != null && App.settings.keepLauncherOpen) {
                        App.launcher.getParent().setVisible(true);
                    }
                    long end = System.currentTimeMillis();
                    if (App.settings.enableDiscordIntegration && App.discordInitialized) {
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

                            Files.list(this.ROOT.resolve("mods"))
                                    .filter(file -> Files.isRegularFile(file)
                                            && this.launcher.mods.stream()
                                                    .noneMatch(m -> m.type == Type.mods && !m.userAdded
                                                            && m.getFile(this).toPath().equals(file)))
                                    .forEach(newMod -> {
                                        LogManager.warn(
                                                "The mod " + newMod.getFileName().toString() + " has been added.");
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
                        App.launcher.updateData();
                    }
                    if (Files.isDirectory(nativesTempDir)) {
                        FileUtils.deleteDirectory(nativesTempDir);
                    }
                    if (!App.settings.keepLauncherOpen) {
                        System.exit(0);
                    }
                } catch (Exception e1) {
                    LogManager.logStackTrace(e1);
                }
            });
            launcher.start();
            return true;
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

    public void addFileFromCurse(CurseForgeProject mod, CurseForgeFile file, ProgressDialog dialog) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.categorySection.gameCategoryId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID
                ? this.getRoot().resolve("resourcepacks").resolve(file.fileName)
                : (mod.categorySection.gameCategoryId == Constants.CURSEFORGE_WORLDS_SECTION_ID
                        ? this.getRoot().resolve("saves").resolve(file.fileName)
                        : this.getRoot().resolve("mods").resolve(file.fileName));
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(file.downloadUrl)
                .downloadTo(downloadLocation).size(file.fileLength)
                .withHttpClient(Network.createProgressClient(dialog));

        dialog.setTotalBytes(file.fileLength);

        if (mod.categorySection.gameCategoryId == Constants.CURSEFORGE_WORLDS_SECTION_ID) {
            download = download.unzipTo(this.getRoot().resolve("saves"));
        } else {
            download = download.copyTo(finalLocation);
            if (Files.exists(finalLocation)) {
                FileUtils.delete(finalLocation);
            }
        }

        // find mods with the same CurseForge project id
        List<DisableableMod> sameMods = this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurseForge() && installedMod.getCurseForgeModId() == mod.id)
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

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

        // remove any mods that are from the same mod on CurseForge from the master mod
        // list
        this.launcher.mods = this.launcher.mods.stream()
                .filter(installedMod -> !installedMod.isFromCurseForge() || installedMod.getCurseForgeModId() != mod.id)
                .collect(Collectors.toList());

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.name, file.displayName, true, file.fileName,
                mod.categorySection.gameCategoryId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID ? Type.resourcepack
                        : (mod.categorySection.gameCategoryId == Constants.CURSEFORGE_WORLDS_SECTION_ID ? Type.worlds
                                : Type.mods),
                null, mod.summary, false, true, true, mod, file));

        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
    }

    public void addFileFromModrinth(ModrinthMod mod, ModrinthVersion version, ProgressDialog dialog) {
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

        if (Files.exists(finalLocation)) {
            FileUtils.delete(finalLocation);
        }

        // find mods with the same Modrinth id
        List<DisableableMod> sameMods = this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromModrinth() && installedMod.modrinthMod.id.equalsIgnoreCase(mod.id))
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
                installedMod -> !installedMod.isFromModrinth() || !installedMod.modrinthMod.id.equalsIgnoreCase(mod.id))
                .collect(Collectors.toList());

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.title, version.name, true, fileToDownload.filename, Type.mods,
                null, mod.description, false, true, true, mod, version));

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

        // check pack is a system pack or imported from CurseForge
        if ((getPack() != null && !getPack().system) && isCurseForgePack()) {
            LogManager.debug("Instance " + launcher.name
                    + " cannot be exported due to: Not being a system pack or imported from CurseForge");
            return false;
        }

        return true;
    }

    public boolean export(String name, String version, String author, InstanceExportFormat format, String saveTo,
            List<String> overrides) {
        if (format == InstanceExportFormat.ATLAUNCHER) {
            return exportAsCurseZip(name, version, author, saveTo, overrides, true);
        } else if (format == InstanceExportFormat.CURSEFORGE) {
            return exportAsCurseZip(name, version, author, saveTo, overrides, false);
        } else if (format == InstanceExportFormat.MULTIMC) {
            return exportAsMultiMcZip(name, version, author, saveTo, overrides);
        }

        return false;
    }

    public boolean exportAsMultiMcZip(String name, String version, String author, String saveTo,
            List<String> overrides) {
        Path to = Paths.get(saveTo).resolve(name + ".zip");
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
        instanceCfg.setProperty("OverrideCommands", "false");
        instanceCfg.setProperty("OverrideConsole", "false");
        instanceCfg.setProperty("OverrideJava", launcher.javaPath == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaArgs", launcher.javaArguments == null ? "false" : "true");
        instanceCfg.setProperty("OverrideJavaLocation", "false");
        instanceCfg.setProperty("OverrideMCLaunchMethod", "false");
        instanceCfg.setProperty("OverrideMemory", launcher.maximumMemory == null ? "false" : "true");
        instanceCfg.setProperty("OverrideNativeWorkarounds", "false");
        instanceCfg.setProperty("OverrideWindow", "false");
        instanceCfg.setProperty("PermGen", Optional.ofNullable(launcher.permGen).orElse(App.settings.metaspace) + "");
        instanceCfg.setProperty("PostExitCommand", "");
        instanceCfg.setProperty("PreLaunchCommand", "");
        instanceCfg.setProperty("ShowConsole", "false");
        instanceCfg.setProperty("ShowConsoleOnError", "true");
        instanceCfg.setProperty("UseNativeGLFW", "false");
        instanceCfg.setProperty("UseNativeOpenAL", "false");
        instanceCfg.setProperty("WrapperCommand", "");
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
            if (!path.equalsIgnoreCase(name + ".zip") && getRoot().resolve(path).toFile().exists()
                    && (getRoot().resolve(path).toFile().isFile()
                            || getRoot().resolve(path).toFile().list().length != 0)) {
                if (getRoot().resolve(path).toFile().isDirectory()) {
                    Utils.copyDirectory(getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile());
                } else {
                    Utils.copyFile(getRoot().resolve(path).toFile(), dotMinecraftPath.resolve(path).toFile(), true);
                }
            }
        }

        ZipUtil.pack(tempDir.toFile(), to.toFile());

        FileUtils.deleteDirectory(tempDir);

        return true;
    }

    public boolean exportAsCurseZip(String name, String version, String author, String saveTo, List<String> overrides,
            boolean supportFabric) {
        Path to = Paths.get(saveTo).resolve(name + ".zip");
        CurseForgeManifest manifest = new CurseForgeManifest();

        CurseForgeMinecraft minecraft = new CurseForgeMinecraft();

        List<CurseForgeModLoader> modLoaders = new ArrayList<>();
        CurseForgeModLoader modLoader = new CurseForgeModLoader();

        String loaderType = launcher.loaderVersion.type.toLowerCase();
        String loaderVersion = launcher.loaderVersion.version;

        // Since CurseForge treats Farbic as a second class citizen :(, we need to force
        // Forge loader and people need to use Jumploader in order to have Fabric packs
        // (https://www.curseforge.com/minecraft/mc-mods/jumploader)
        if (launcher.loaderVersion.type.equals("Fabric") && !supportFabric) {
            loaderType = "forge";
            loaderVersion = ForgeLoader.getRecommendedVersion(id);

            // no recommended version, so grab latest
            if (loaderVersion == null) {
                loaderVersion = ForgeLoader.getLatestVersion(id);
            }
        }

        if (loaderVersion == null) {
            LogManager.error("Failed to get loader version for this pack");
            return false;
        }

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
                sb.append("<li><a href=\"").append(mod.curseForgeProject.websiteUrl).append("\">").append(mod.name)
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
            if (!path.equalsIgnoreCase(name + ".zip") && getRoot().resolve(path).toFile().exists()
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

        ZipUtil.pack(tempDir.toFile(), to.toFile());

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

    public InstanceSettings getSettings() {
        InstanceSettings settings = new InstanceSettings();
        settings.initialMemory = launcher.initialMemory;
        settings.maximumMemory = launcher.maximumMemory;
        settings.permGen = launcher.permGen;
        settings.javaPath = launcher.javaPath;
        settings.javaArguments = launcher.javaArguments;

        return settings;
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

    public boolean isMultiMcImport() {
        return launcher.multiMCManifest != null;
    }

    public boolean isModpacksChPack() {
        return launcher.modpacksChPackManifest != null && launcher.modpacksChPackVersionManifest != null;
    }

    public boolean isVanillaInstance() {
        return launcher.vanillaInstance;
    }

    public boolean isExternalPack() {
        return isOldCurseForgePack() || isCurseForgePack() || isModpacksChPack() || isMultiMcImport();
    }

    public boolean isUpdatableExternalPack() {
        return isExternalPack() && (isModpacksChPack() || isCurseForgePack());
    }

    public String getAnalyticsCategory() {
        if (isCurseForgePack()) {
            return "CurseForgeInstance";
        }

        if (isModpacksChPack()) {
            return "ModpacksChInstance";
        }

        if (isVanillaInstance()) {
            return "VanillaInstance";
        }

        return "Instance";
    }

    public void update() {
        new InstanceInstallerDialog(this, true, false, null, null, true, null, null, App.launcher.getParent());
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

            ZipUtil.pack(getRoot().toFile(), FileSystem.BACKUPS.resolve(filename).toFile(),
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
}
