/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.curse.pack.CurseManifest;
import com.atlauncher.data.curse.pack.CurseManifestFile;
import com.atlauncher.data.curse.pack.CurseMinecraft;
import com.atlauncher.data.curse.pack.CurseModLoader;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import okhttp3.OkHttpClient;

@Json
public class InstanceV2 extends MinecraftVersion {
    public String inheritsFrom;
    public InstanceV2Launcher launcher;

    public transient Path ROOT;

    public InstanceV2(MinecraftVersion version) {
        this.id = version.id;
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
        if (this.launcher.curseManifest != null) {
            return null;
        }

        try {
            return PackManager.getPackByID(this.launcher.packId);
        } catch (InvalidPack e) {
            return null;
        }
    }

    public boolean hasUpdate() {
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

        return Utils.getIconImage(FileSystem.IMAGES.resolve("defaultimage.png").toFile());
    }

    public void ignoreUpdate() {
        String version;

        if (this.launcher.isDev) {
            version = getLatestVersion().hash;
        } else {
            version = getLatestVersion().version;
        }

        if (!hasUpdateBeenIgnored(version)) {
            this.launcher.ignoredUpdates.add(version);
            this.save();
        }
    }

    public boolean hasLatestUpdateBeenIgnored() {
        String version;

        if (this.launcher.isDev) {
            version = getLatestVersion().hash;
        } else {
            version = getLatestVersion().version;
        }

        return hasUpdateBeenIgnored(version);
    }

    public boolean hasUpdateBeenIgnored(String version) {
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
        final AbstractAccount account = AccountManager.getSelectedAccount();

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

            ProgressDialog prepareDialog = new ProgressDialog(GetText.tr("Preparing For Launch"), 4,
                    GetText.tr("Preparing For Launch"));
            prepareDialog.addThread(new Thread(() -> {
                LogManager.info("Preparing for launch!");
                prepareDialog.setReturnValue(prepareForLaunch(prepareDialog, nativesTempDir));
                prepareDialog.close();
            }));
            prepareDialog.start();

            if (prepareDialog.getReturnValue() == null || !(boolean) prepareDialog.getReturnValue()) {
                LogManager.error("Failed to prepare instance " + this.launcher.name
                        + " for launch. Check the logs and try again.");
                return false;
            }

            Analytics.sendEvent(this.launcher.pack + " - " + this.launcher.version, "Play",
                    (launcher.curseManifest != null ? "CursePack" : "InstanceV2"));

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
                        ProgressDialog loginDialog = new ProgressDialog(GetText.tr("Logging Into Minecraft"), 0,
                                GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(mojangAccount.login());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        final LoginResponse session = (LoginResponse) loginDialog.getReturnValue();

                        if (session == null) {
                            return;
                        }

                        process = MCLauncher.launch(mojangAccount, this, session, nativesTempDir);
                    } else if (account instanceof MicrosoftAccount) {
                        MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;
                        process = MCLauncher.launch(microsoftAccount, this, nativesTempDir);
                    }

                    if (process == null) {
                        LogManager.error("Failed to get process for Minecraft");
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
                        String playing = this.launcher.pack + " (" + this.launcher.version + ")";

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
                    if (App.settings.keepLauncherOpen && App.launcher.checkForUpdatedFiles()) {
                        App.launcher.reloadLauncherData();
                    }
                    if (Files.isDirectory(nativesTempDir)) {
                        FileUtils.deleteDirectory(nativesTempDir);
                    }
                    if (!App.settings.keepLauncherOpen) {
                        System.exit(0);
                    }
                } catch (IOException e1) {
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

    public Path getAssetsDir() {
        if (this.launcher.assetsMapToResources) {
            return this.getRoot().resolve("resources");
        }

        return FileSystem.RESOURCES_VIRTUAL.resolve(this.assets);
    }

    public DisableableMod getDisableableModByCurseModId(int curseModId) {
        return this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurse() && installedMod.getCurseModId() == curseModId)
                .findFirst().orElse(null);
    }

    public void addFileFromCurse(CurseMod mod, CurseFile file) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID
                ? this.getRoot().resolve("resourcepacks").resolve(file.fileName)
                : (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID
                        ? this.getRoot().resolve("saves").resolve(file.fileName)
                        : this.getRoot().resolve("mods").resolve(file.fileName));
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(file.downloadUrl)
                .downloadTo(downloadLocation).size(file.fileLength);

        if (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID) {
            download = download.unzipTo(this.getRoot().resolve("saves"));
        } else {
            download = download.copyTo(finalLocation);
            if (Files.exists(finalLocation)) {
                FileUtils.delete(finalLocation);
            }
        }

        // find mods with the same curse mod id
        List<DisableableMod> sameMods = this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurse() && installedMod.getCurseModId() == mod.id)
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

        // remove any mods that are from the same mod on Curse from the master mod list
        this.launcher.mods = this.launcher.mods.stream()
                .filter(installedMod -> !installedMod.isFromCurse() || installedMod.getCurseModId() != mod.id)
                .collect(Collectors.toList());

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.name, file.displayName, true, file.fileName,
                mod.categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID ? Type.resourcepack
                        : (mod.categorySection.gameCategoryId == Constants.CURSE_WORLDS_SECTION_ID ? Type.worlds
                                : Type.mods),
                null, mod.summary, false, true, true, mod, file));

        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
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

        // check pack is a system pack or imported from Curse
        if ((getPack() != null && !getPack().system) && launcher.curseManifest == null) {
            LogManager.debug("Instance " + launcher.name
                    + " cannot be exported due to: Not being a system pack or imported from Curse");
            return false;
        }

        // make sure there's at least one mod from Curse
        if (launcher.mods.stream().noneMatch(DisableableMod::isFromCurse)) {
            LogManager.debug("Instance " + launcher.name + " cannot be exported due to: No mods from Curse");
            return false;
        }

        return true;
    }

    public boolean exportAsCurseZip(String name, String author, String saveTo, List<String> overrides) {
        Path to = Paths.get(saveTo).resolve(name + ".zip");
        CurseManifest manifest = new CurseManifest();

        CurseMinecraft minecraft = new CurseMinecraft();

        List<CurseModLoader> modLoaders = new ArrayList<>();
        CurseModLoader modLoader = new CurseModLoader();

        String loaderVersion = launcher.loaderVersion.version;

        // Since Curse treats Farbic as a second class citizen :(, we need to force a
        // forge loader and people need to use Jumploader in order to have Fabric packs
        // (https://www.curseforge.com/minecraft/mc-mods/jumploader)
        if (launcher.loaderVersion.type.equals("Fabric")) {
            loaderVersion = ForgeLoader.getRecommendedVersion(id);
        }

        modLoader.id = "forge-" + loaderVersion;
        modLoader.primary = true;
        modLoaders.add(modLoader);

        minecraft.version = this.id;
        minecraft.modLoaders = modLoaders;

        manifest.minecraft = minecraft;
        manifest.manifestType = "minecraftModpack";
        manifest.manifestVersion = 1;
        manifest.name = name;
        manifest.version = this.launcher.version;
        manifest.author = author;
        manifest.files = this.launcher.mods.stream().filter(DisableableMod::isFromCurse).map(mod -> {
            CurseManifestFile file = new CurseManifestFile();
            file.projectID = mod.curseModId;
            file.fileID = mod.curseFileId;
            file.required = !mod.disabled;

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
        this.launcher.mods.stream().filter(DisableableMod::isFromCurse).forEach(mod -> {
            if (mod.hasFullCurseInformation()) {
                sb.append("<li><a href=\"").append(mod.curseMod.websiteUrl).append("\">").append(mod.name)
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

        // remove files that come from Curse
        launcher.mods.stream().filter(DisableableMod::isFromCurse).forEach(mod -> {
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
}
