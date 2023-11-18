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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.InstanceLauncherUseCase;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileHash;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeFingerprintedMod;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeManifestFile;
import com.atlauncher.data.curseforge.pack.CurseForgeMinecraft;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
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
import com.atlauncher.gui.dialogs.instancesettings.InstanceEditors;
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
import com.atlauncher.network.Analytics;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;

import okhttp3.HttpUrl;

@Json
public class Instance extends MinecraftVersion {
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
            } catch (IIOException e) {
                LogManager.warn("Error creating scaled image from the custom image of instance " + this.launcher.name
                        + ". Using default image.");
            } catch (Exception e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of instance " + this.launcher.name, e,
                        false);
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
            if (isModpacksChPack()) {
                version = Integer.toString(ModpacksChUpdateManager.getLatestVersion(getUUID()).id);
            } else if (isCurseForgePack()) {
                version = Integer.toString(CurseForgeUpdateManager.getLatestVersion(getUUID()).id);
            } else if (isTechnicPack()) {
                if (isTechnicSolderPack()) {
                    version = TechnicModpackUpdateManager.getUpToDateSolderModpack(getUUID()).latest;
                } else {
                    version = TechnicModpackUpdateManager.getUpToDateModpack(getUUID()).version;
                }
            } else if (isModrinthPack()) {
                version = ModrinthModpackUpdateManager.getLatestVersion(getUUID()).id;
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
            if (isModpacksChPack()) {
                return hasUpdateBeenIgnored(Integer.toString(ModpacksChUpdateManager.getLatestVersion(getUUID()).id));
            } else if (isCurseForgePack()) {
                return hasUpdateBeenIgnored(Integer.toString(CurseForgeUpdateManager.getLatestVersion(getUUID()).id));
            } else if (isTechnicPack()) {
                if (isTechnicSolderPack()) {
                    return hasUpdateBeenIgnored(TechnicModpackUpdateManager.getUpToDateSolderModpack(getUUID()).latest);
                } else {
                    return hasUpdateBeenIgnored(TechnicModpackUpdateManager.getUpToDateModpack(getUUID()).version);
                }
            } else if (isModrinthPack()) {
                return hasUpdateBeenIgnored(ModrinthModpackUpdateManager.getLatestVersion(getUUID()).id);
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
     * @deprecated Moved to InstanceLauncherFunc
     */
    @Deprecated
    public boolean launch() {
        return launch(false);
    }

    /**
     * @deprecated Moved to InstanceLauncherUseCase
     */
    @Deprecated
    public boolean launch(boolean offline) {
        return InstanceLauncherUseCase.launch(this, offline);
    }

    public void addPlay(String version) {
        if (ConfigManager.getConfigItem("useGraphql.packActions", false) == true) {
            GraphqlClient
                    .mutateAndWait(
                            new AddPackActionMutation(AddPackActionInput.builder().packId(Integer.toString(
                                    this.getPack().id))
                                    .version(version).action(PackLogAction.PLAY).build()));
        } else {
            Map<String, Object> request = new HashMap<>();

            request.put("version", version);

            try {
                Utils.sendAPICall("pack/" + this.getPack().getSafeName() + "/play", request);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    public void addTimePlayed(int time, String version) {
        if (ConfigManager.getConfigItem("useGraphql.packActions", false) == true) {
            GraphqlClient
                    .mutateAndWait(
                            new AddPackTimePlayedMutation(AddPackTimePlayedInput.builder().packId(Integer.toString(
                                    this.getPack().id)).version(version).time(time).build()));
        } else {
            Map<String, Object> request = new HashMap<>();

            request.put("version", version);
            request.put("time", time);

            try {
                Utils.sendAPICall("pack/" + this.getPack().getSafeName() + "/timeplayed/", request);
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }
    }

    /**
     * TODO Remove?
     */
    @Deprecated
   public DisableableMod getDisableableModByCurseModId(int curseModId) {
        return this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromCurseForge() && installedMod.getCurseForgeModId() == curseModId)
                .findFirst().orElse(null);
    }

    public void addFileFromCurseForge(CurseForgeProject mod, CurseForgeFile file, ProgressDialog dialog) {
        Path downloadLocation = FileSystem.DOWNLOADS.resolve(file.fileName);
        Path finalLocation = mod.getInstanceDirectoryPath(this.getRoot()).resolve(file.fileName);

        // find mods with the same CurseForge project id
        List<DisableableMod> sameMods = this.launcher.mods.stream()
                .filter(installedMod -> installedMod.isFromCurseForge()
                        && installedMod.getCurseForgeModId() == mod.id)
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        Optional<CurseForgeFileHash> md5Hash = file.hashes.stream().filter(h -> h.isMd5())
                .findFirst();
        Optional<CurseForgeFileHash> sha1Hash = file.hashes.stream().filter(h -> h.isSha1())
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
            String filename = file.fileName.replace(" ", "+");
            File fileLocation = downloadLocation.toFile();
            // if file downloaded already, but hashes don't match, delete it
            if (fileLocation.exists()
                    && ((md5Hash.isPresent()
                            && !Hashing.md5(fileLocation.toPath()).equals(Hashing.toHashCode(md5Hash.get().value)))
                            || (sha1Hash.isPresent()
                                    && !Hashing.sha1(fileLocation.toPath())
                                            .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                FileUtils.delete(fileLocation.toPath());
            }

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

                    // file downloaded, but hashes don't match, delete it
                    if (fileLocation.exists()
                            && ((md5Hash.isPresent() && !Hashing.md5(fileLocation.toPath())
                                    .equals(Hashing.toHashCode(md5Hash.get().value)))
                                    || (sha1Hash.isPresent()
                                            && !Hashing.sha1(fileLocation.toPath())
                                                    .equals(Hashing.toHashCode(sha1Hash.get().value))))) {
                        FileUtils.delete(fileLocation.toPath());
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
                mod.getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID ? Type.resourcepack
                        : (mod.getRootCategoryId() == Constants.CURSEFORGE_WORLDS_SECTION_ID ? Type.worlds : Type.mods),
                null, mod.summary, false, true, true, false, mod, file);

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

        // add this mod
        this.launcher.mods.add(dm);
        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.name));
    }

    public void addFileFromModrinth(ModrinthProject mod, ModrinthVersion version, ModrinthFile file,
            ProgressDialog dialog) {
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

        Type modType = mod.projectType == ModrinthProjectType.MOD ? Type.mods
                : (mod.projectType == ModrinthProjectType.SHADER ? Type.shaderpack : Type.resourcepack);

        DisableableMod dm = new DisableableMod(mod.title, version.name, true, fileToDownload.filename, modType,
                null, mod.description, false, true, true, false, mod, version);

        // check for mod on CurseForge
        if (!App.settings.dontCheckModsOnCurseForge) {
            try {
                CurseForgeFingerprint fingerprint = CurseForgeApi
                        .checkFingerprints(new Long[] { Hashing.murmur(finalLocation) });

                if (fingerprint.exactMatches != null && fingerprint.exactMatches.size() == 1) {
                    CurseForgeFingerprintedMod foundMod = fingerprint.exactMatches.get(0);

                    dm.curseForgeProjectId = foundMod.id;
                    dm.curseForgeFile = foundMod.file;
                    dm.curseForgeFileId = foundMod.file.id;

                    CurseForgeProject curseForgeProject = CurseForgeApi.getProjectById(foundMod.id);
                    if (curseForgeProject != null) {
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

    public boolean hasCustomMods() {
        return this.launcher.mods.stream().anyMatch(DisableableMod::isUserAdded);
    }

    /**
     * TODO Remove?
     */
    @Deprecated
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
            new Thread(() -> LogManager.debug("Instance " + launcher.name + " cannot be exported due to: No loader"))
                    .start();
            return false;
        }

        return true;
    }

    public Pair<Path, String> export(String name, String version, String author, InstanceExportFormat format,
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
            return exportAsCurseForgeZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MODRINTH) {
            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.CURSEFORGE_AND_MODRINTH) {
            if (exportAsCurseForgeZip(name, version, author, saveTo, overrides).left() == null) {
                return new Pair<Path, String>(null, null);
            }

            return exportAsModrinthZip(name, version, author, saveTo, overrides);
        } else if (format == InstanceExportFormat.MULTIMC) {
            return exportAsMultiMcZip(name, version, author, saveTo, overrides);
        }

        return new Pair<Path, String>(null, null);
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
            if (ConfigManager.getConfigItem("loaders.quilt.switchHashedForIntermediary", true) == false) {
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
                new FileOutputStream(tempDir.resolve("mmc-pack.json").toFile()), StandardCharsets.UTF_8)) {
            Gsons.DEFAULT.toJson(manifest, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace("Failed to save mmc-pack.json", e);

            FileUtils.deleteDirectory(tempDir);

            return new Pair<Path, String>(null, null);
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

        if (ConfigManager.getConfigItem("removeInitialMemoryOption", false) == false) {
            instanceCfg.setProperty("MinMemAlloc",
                    Optional.ofNullable(launcher.initialMemory).orElse(App.settings.initialMemory) + "");
        }

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
                    .forEach(f -> {
                        FileUtils.delete(f, false);
                    });
        } catch (IOException ignored) {
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<Path, String>(to, null);
    }

    public Pair<Path, String> exportAsCurseForgeZip(String name, String version, String author, String saveTo,
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
                .filter(m -> !m.disabled && m.isFromCurseForge())
                .filter(mod -> overrides.stream()
                        .anyMatch(path -> getRoot().relativize(mod.getPath(this)).startsWith(path)))
                .map(mod -> {
                    CurseForgeManifestFile file = new CurseForgeManifestFile();
                    file.projectID = mod.curseForgeProjectId;
                    file.fileID = mod.curseForgeFileId;
                    file.required = true;

                    return file;
                }).collect(Collectors.toList());
        manifest.overrides = "overrides";

        // create temp directory to put this in
        Path tempDir = FileSystem.TEMP.resolve(this.getSafeName() + "-export");
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
        this.launcher.mods.stream()
                .filter(m -> !m.disabled && m.isFromCurseForge())
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
                    .forEach(f -> {
                        FileUtils.delete(f, false);
                    });
        } catch (IOException ignored) {
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

        return new Pair<Path, String>(to, null);
    }

    public Pair<Path, String> exportAsModrinthZip(String name, String version, String author, String saveTo,
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

                        LogManager.debug("Found matching version from Modrinth called " + mod.modrinthVersion.name);

                        if (modrinthProjects.containsKey(modrinthVersions.get(hash).projectId)) {
                            mod.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);
                        }
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
                .filter(m -> !m.disabled && m.modrinthVersion != null && m.getFile(this).exists())
                .filter(mod -> overrides.stream()
                        .anyMatch(path -> getRoot().relativize(mod.getPath(this)).startsWith(path)))
                .map(mod -> {
                    Path modPath = mod.getFile(this).toPath();

                    ModrinthModpackFile file = new ModrinthModpackFile();
                    file.path = this.ROOT.relativize(modPath).toString().replace("\\", "/");

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

            return new Pair<Path, String>(null, null);
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
                    .forEach(f -> {
                        FileUtils.delete(f, false);
                    });
        } catch (IOException ignored) {
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
                    .forEach(f -> {
                        overridesForPermissions.append(String.format("%s\n", tempDir.relativize(f)));
                    });
        } catch (IOException ignored) {
        }

        ArchiveUtils.createZip(tempDir, to);

        FileUtils.deleteDirectory(tempDir);

        return new Pair<Path, String>(to, overridesForPermissions.toString());
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

    /**
     * @deprecated InstanceManager shall handle this business code in the future.
     */
    @Deprecated()
    public void save() {
        InstanceManager.saveInstance(this);
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

    /**
     * TODO Remove?
     */
    @Deprecated
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

    public String getPlatformName() {
        if (isCurseForgePack()) {
            return "CurseForge";
        }

        if (isModpacksChPack()) {
            return "ModpacksCh";
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

    /**
     * TODO Remove?
     */
    @Deprecated
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

    public void update() {
        new InstanceInstallerDialog(this, true, false, null, null, true, null, App.launcher.getParent(), null);
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

    /**
     * Moved to InstanceInstallerDialog.launch
     */
    @Deprecated
    public void startReinstall() {
        InstanceInstallerDialog.launch(this);
    }

    /**
     * Moved to RenameInstanceDialog.launch
     */
    @Deprecated
    public void startRename() {
        RenameInstanceDialog.launch(this);
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
            Analytics.trackEvent(AnalyticsEvent.forInstanceEvent("instance_clone", this));

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

    /**
     * @deprecated Use InstanceEditors instead.
     */
    @Deprecated
    public void startChangeDescription() {
        InstanceEditors.startChangeDescription(this);
    }

    /**
     * @deprecated Use InstanceEditors instead.
     */
    @Deprecated
    public void startChangeImage() {
        InstanceEditors.startChangeImage(this);
    }

    /**
     * @deprecated Use InstanceEditors instead.
     */
    @Deprecated
    public void changeLoaderVersion() {
        InstanceEditors.changeLoaderVersion(this);
    }

    public void addLoader(LoaderType loaderType) {
        Analytics
                .trackEvent(AnalyticsEvent.forInstanceAddLoader(this, loaderType));

        LoaderVersion loaderVersion = InstanceEditors.showLoaderVersionSelector(this);

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
                    && runtimesForSystem.get(runtimeToUse).size() != 0) {
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

    public List<Path> getModPathsFromFilesystem() {
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

    public void scanMissingMods() {
        scanMissingMods(App.launcher.getParent());
    }

    public void scanMissingMods(Window parent) {
        PerformanceManager.start("Instance::scanMissingMods - CheckForAddedMods");

        // files to scan
        List<Path> files = new ArrayList<>();

        // find the mods that have been added by the user manually
        for (Path path : Arrays.asList(ROOT.resolve("mods"), ROOT.resolve("disabledmods"),
                ROOT.resolve("resourcepacks"), ROOT.resolve("jarmods"))) {
            if (!Files.exists(path)) {
                continue;
            }

            com.atlauncher.data.Type fileType = path.equals(ROOT.resolve("resourcepacks"))
                    ? com.atlauncher.data.Type.resourcepack
                    : (path.equals(ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                            : com.atlauncher.data.Type.mods);

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

        if (files.size() != 0) {
            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Scanning New Mods"), 0,
                    GetText.tr("Scanning New Mods"), parent);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> mods = files.parallelStream()
                        .map(file -> {
                            com.atlauncher.data.Type fileType = file.getParent().equals(ROOT.resolve("resourcepacks"))
                                    ? com.atlauncher.data.Type.resourcepack
                                    : (file.getParent().equals(ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                                            : com.atlauncher.data.Type.mods);

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
                                                    dm.name = curseForgeProject.name;
                                                    dm.description = curseForgeProject.summary;
                                                }

                                                LogManager.debug("Found matching mod from CurseForge called "
                                                        + dm.curseForgeFile.displayName);
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

                    if (sha1Hashes.size() != 0) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                                .getVersionsFromSha1Hashes(keys.toArray(new String[keys.size()]));

                        if (modrinthVersions != null && modrinthVersions.size() != 0) {
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

        if (removedMods.size() != 0) {
            removedMods.forEach(mod -> LogManager.info("Mod no longer in filesystem: " + mod.file));
            launcher.mods.removeAll(removedMods);
            save();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForRemovedMods");
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

    public void removeMod(DisableableMod foundMod) {
        launcher.mods.remove(foundMod);
        FileUtils.delete(
                (foundMod.isDisabled()
                        ? foundMod.getDisabledFile(this)
                        : foundMod.getFile(this)).toPath(),
                true);
        save();

        // #. {0} is the name of a mod that was removed
        App.TOASTER.pop(GetText.tr("{0} Removed", foundMod.name));
    }
}
