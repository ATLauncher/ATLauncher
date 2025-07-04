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
package com.atlauncher.workers;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JDialog;
import javax.swing.SwingWorker;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceLauncher;
import com.atlauncher.data.Server;
import com.atlauncher.data.Type;
import com.atlauncher.data.curseforge.CurseForgeAttachment;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeFileHash;
import com.atlauncher.data.curseforge.CurseForgeFingerprint;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.curseforge.pack.CurseForgeModLoader;
import com.atlauncher.data.ftb.FTBPackArt;
import com.atlauncher.data.ftb.FTBPackArtType;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.data.ftb.FTBPackVersionManifest;
import com.atlauncher.data.ftb.FTBPackVersionManifestFileType;
import com.atlauncher.data.ftb.FTBPackVersionManifestMod;
import com.atlauncher.data.ftb.FTBPackVersionManifestTarget;
import com.atlauncher.data.ftb.FTBPackVersionManifestTargetType;
import com.atlauncher.data.ftb.FTBPackVersionModsManifest;
import com.atlauncher.data.json.Delete;
import com.atlauncher.data.json.Deletes;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Keep;
import com.atlauncher.data.json.Keeps;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.FabricMod;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingFile;
import com.atlauncher.data.minecraft.MCMod;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.MojangDownload;
import com.atlauncher.data.minecraft.MojangDownloads;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricMetaVersion;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLoader;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCComponent;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.multimc.MultiMCPatch;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicModpackAsset;
import com.atlauncher.data.technic.TechnicModpackManifestMod;
import com.atlauncher.data.technic.TechnicSolderModpackManifest;
import com.atlauncher.exceptions.LocalException;
import com.atlauncher.graphql.GetForgeLoaderVersionQuery;
import com.atlauncher.graphql.GetNeoForgeLoaderVersionQuery;
import com.atlauncher.graphql.GetPaperLoaderVersionQuery;
import com.atlauncher.graphql.GetPurpurLoaderVersionQuery;
import com.atlauncher.gui.dialogs.BrowserDownloadDialog;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.managers.ConfigManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.network.GraphqlClient;
import com.atlauncher.network.NetworkClient;
import com.atlauncher.network.analytics.AnalyticsEvent;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.CurseForgeApi;
import com.atlauncher.utils.FTBApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Pair;
import com.atlauncher.utils.TechnicApi;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.CaseFileVisitor;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;

public class InstanceInstaller extends SwingWorker<Boolean, Void> implements NetworkProgressable {
    protected double percent = 0.0; // Percent done installing
    protected double subPercent = 0.0; // Percent done sub installing
    protected double totalBytes = 0; // Total number of bytes to download
    protected double downloadedBytes = 0; // Total number of bytes downloaded

    public Instance instance = null;
    public final String name;
    public final com.atlauncher.data.Pack pack;
    public final com.atlauncher.data.PackVersion version;
    public final boolean showModsChooser;
    public LoaderVersion loaderVersion;
    public CurseForgeManifest curseForgeManifest;
    public Path curseForgeExtractedPath;
    public ModrinthModpackManifest modrinthManifest;
    public Path modrinthExtractedPath;
    public final FTBPackManifest ftbPackManifest;
    public FTBPackVersionManifest ftbPackVersionManifest;
    public final MultiMCManifest multiMCManifest;
    public final Path multiMCExtractedPath;
    public final TechnicModpack technicModpack;
    public TechnicSolderModpackManifest technicSolderModpackManifest;
    public Path technicModpackExtractedPath;
    public List<Mod> technicSolderModsToDownload = new ArrayList<>();
    private List<String> overridePaths = new ArrayList<>();

    public boolean isReinstall;
    public boolean isServer;
    public boolean changingLoader;
    public boolean instanceIsCorrupt;
    public boolean saveMods;

    public final Path root;
    public final Path temp;

    public Loader loader;
    public com.atlauncher.data.json.Version packVersion;
    public VersionManifestVersion minecraftVersionManifest = null;
    public MinecraftVersion minecraftVersion;

    public List<Mod> allMods;
    public List<Mod> selectedMods;
    public List<Mod> unselectedMods = new ArrayList<>();
    public List<DisableableMod> modsInstalled = new ArrayList<>();

    public boolean assetsMapToResources = false;

    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedOptionsTxt = false; // If options.txt was found and saved
    private boolean savedServersDat = false; // If servers.dat was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved

    public String mainClass;
    public Arguments arguments;
    public boolean success;
    private JDialog dialog;

    public InstanceInstaller(String name, com.atlauncher.data.Pack pack, com.atlauncher.data.PackVersion version,
        boolean isReinstall, boolean isServer, boolean changingLoader, boolean saveMods,
        boolean showModsChooser, LoaderVersion loaderVersion, CurseForgeManifest curseForgeManifest,
        Path curseForgeExtractedPath, FTBPackManifest ftbPackManifest,
        ModrinthModpackManifest modrinthManifest, Path modrinthExtractedPath, MultiMCManifest multiMCManifest,
        Path multiMCExtractedPath, TechnicModpack technicModpack, JDialog dialog) {
        this.name = name;
        this.pack = pack;
        this.version = version;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        this.changingLoader = changingLoader;
        this.saveMods = saveMods;
        this.showModsChooser = showModsChooser;
        this.dialog = dialog;

        if (isServer) {
            this.root = FileSystem.SERVERS.resolve(name.replaceAll("[^A-Za-z0-9]", ""));
        } else {
            this.root = FileSystem.INSTANCES.resolve(name.replaceAll("[^A-Za-z0-9]", ""));
        }

        this.temp = FileSystem.TEMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion());

        this.loaderVersion = loaderVersion;
        this.curseForgeManifest = curseForgeManifest;
        this.curseForgeExtractedPath = curseForgeExtractedPath;
        this.ftbPackManifest = ftbPackManifest;
        this.modrinthManifest = modrinthManifest;
        this.modrinthExtractedPath = modrinthExtractedPath;
        this.multiMCManifest = multiMCManifest;
        this.multiMCExtractedPath = multiMCExtractedPath;
        this.technicModpack = technicModpack;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    private boolean success(boolean success) {
        this.success = success;

        return success;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        ErrorReporting.recordPackInstall(this.pack.name, this.version.version, this.loaderVersion);
        LogManager.info("Started install of " + this.pack.name + " version " + this.version.version);

        if (this.loaderVersion != null) {
            LogManager.info("Using loader version " + this.loaderVersion.version);
        }

        try {

            if (pack.curseForgeProject != null && isServer) {
                downloadCurseForgeServerPack();
                if (isCancelled()) {
                    return success(false);
                }

                downloadImage();
                if (isCancelled()) {
                    return success(false);
                }

                saveServerJson();

                return success(true);
            }

            if (changingLoader) {
                generatePackVersionForVanilla();

                downloadMinecraftVersionJson();

                this.loader = this.packVersion.getLoader().getLoader(this.temp.resolve("loader").toFile(), this,
                    this.loaderVersion);

                downloadLoader();
                if (isCancelled()) {
                    return success(false);
                }

                determineMainClass();
                determineArguments();

                downloadLibraries();
                if (isCancelled()) {
                    return success(false);
                }

                organiseLibraries();
                if (isCancelled()) {
                    return success(false);
                }

                installLoader();
                if (isCancelled()) {
                    return success(false);
                }

                if (!this.isServer) {
                    saveInstanceJson();
                } else {
                    saveServerJson();
                }

                return success(true);
            }

            if (curseForgeManifest != null) {
                generatePackVersionFromCurseForgeManifest();
            } else if (pack.curseForgeProject != null) {
                generatePackVersionFromCurseForge();
            } else if (pack.modrinthProject != null) {
                generatePackVersionFromModrinth();
            } else if (modrinthManifest != null) {
                generatePackVersionFromModrinthManifest();
            } else if (ftbPackManifest != null) {
                generatePackVersionFromFTB();
            } else if (multiMCManifest != null) {
                generatePackVersionFromMultiMC();
            } else if (technicModpack != null) {
                if (technicModpack.solder != null) {
                    generatePackVersionFromTechnicSolder();
                } else {
                    generatePackVersionFromTechnicZip();
                }
            } else if (pack.vanillaInstance) {
                generatePackVersionForVanilla();
            } else {
                downloadPackVersionJson();
            }

            downloadMinecraftVersionJson();

            if (this.packVersion.messages != null) {
                showMessages();
            }

            determineModsToBeInstalled();

            if (isCancelled()) {
                return success(false);
            }

            backupSelectFiles();
            addPercent(5);

            prepareFilesystem();

            // we don't know the loader information until we download the mods
            if (technicModpack != null && technicModpack.solder != null) {
                downloadTechnicSolderMods();

                getLoaderInformationFromTechnicModpack(this.root);
            }

            if (this.packVersion.loader != null && this.packVersion.loader.className != null) {
                this.loader = this.packVersion.getLoader().getLoader(this.temp.resolve("loader").toFile(), this,
                    this.loaderVersion);

                if (this.loaderVersion == null) {
                    this.loaderVersion = this.loader.getLoaderVersion();
                }

                downloadLoader();
            }

            install();

            if (isCancelled()) {
                return success(false);
            }

            if (this.isServer && minecraftVersionManifest != null && minecraftVersionManifest.hasInitSettings()
                && (this.loaderVersion == null
                || (!this.loaderVersion.isPaper() && !this.loaderVersion.isPurpur()))) {
                initServerSettings();
            }

            if (!this.isServer) {
                saveInstanceJson();
            } else {
                saveServerJson();
            }

            Analytics.trackEvent(
                AnalyticsEvent.forPackInstalled(pack, version, this.isServer, this.isReinstall,
                    getAnalyticsPlatform(),
                    this.loaderVersion == null ? null : this.loaderVersion.getLoaderType()));

            return success(true);
        } catch (Exception e) {
            success(false);
            cancel(true);
            LogManager.logStackTrace(e);
        }

        return success(false);
    }

    private void downloadCurseForgeServerPack() throws Exception {
        addPercent(5);

        // #. {0} is the platform the modpack is from (e.g. CurseForge/Modrinth)
        fireTask(GetText.tr("Downloading Server Pack From {0}", "CurseForge"));
        fireSubProgressUnknown();

        Path serverPackFile = this.temp.resolve(version._curseForgeFile.fileName.toLowerCase(Locale.ENGLISH));

        if (version._curseForgeFile.downloadUrl == null) {
            if (!App.settings.seenCurseForgeProjectDistributionDialog) {
                App.settings.seenCurseForgeProjectDistributionDialog = true;
                App.settings.save();

                DialogManager.okDialog().setType(DialogManager.WARNING)
                    .setTitle(GetText.tr("Mod Not Available"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We were unable to download this modpack.<br/>This is likely due to the author of the modpack disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to download the modpack manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, we can continue with installing the modpack.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                        .build())
                    .show();
            }

            String filename = version._curseForgeFile.fileName;
            String filename2 = version._curseForgeFile.fileName.replace(" ", "+");

            File fileLocation = FileSystem.DOWNLOADS.resolve(filename).toFile();
            File fileLocation2 = FileSystem.DOWNLOADS.resolve(filename2).toFile();
            if (!fileLocation.exists() && !fileLocation2.exists()) {
                File downloadsFolderFile = new File(FileSystem.getUserDownloadsPath().toFile(),
                    filename);
                File downloadsFolderFile2 = new File(FileSystem.getUserDownloadsPath().toFile(),
                    filename2);
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                } else if (downloadsFolderFile2.exists()) {
                    Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                }

                while (!fileLocation.exists() && !fileLocation2.exists()) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            OS.openWebBrowser(pack.curseForgeProject.getBrowserDownloadUrl(version._curseForgeFile));
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
                            .showWithFileMonitoring(version._curseForgeFile.fileLength, 1, fileLocation,
                                fileLocation2, downloadsFolderFile, downloadsFolderFile2);

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
                    }
                }
            }

            if (fileLocation.exists()) {
                FileUtils.moveFile(fileLocation.toPath(), serverPackFile, true);
            } else if (fileLocation2.exists()) {
                FileUtils.moveFile(fileLocation2.toPath(), serverPackFile, true);
            }
        } else {
            com.atlauncher.network.Download serverPackDownload = com.atlauncher.network.Download.build()
                .setUrl(version._curseForgeFile.downloadUrl).downloadTo(serverPackFile)
                .size(version._curseForgeFile.fileLength);

            Optional<CurseForgeFileHash> md5Hash = version._curseForgeFile.hashes.stream()
                .filter(CurseForgeFileHash::isMd5)
                .findFirst();
            Optional<CurseForgeFileHash> sha1Hash = version._curseForgeFile.hashes.stream()
                .filter(CurseForgeFileHash::isSha1)
                .findFirst();

            if (md5Hash.isPresent()) {
                serverPackDownload = serverPackDownload.hash(md5Hash.get().value);
            } else if (sha1Hash.isPresent()) {
                serverPackDownload = serverPackDownload.hash(sha1Hash.get().value);
            } else {
                serverPackDownload = serverPackDownload.fingerprint(version._curseForgeFile.packageFingerprint);
            }

            serverPackDownload = serverPackDownload.withInstanceInstaller(this)
                .withHttpClient(Network.createProgressClient(this));

            this.setTotalBytes(version._curseForgeFile.fileLength);
            serverPackDownload.downloadFile();
        }

        addPercent(30);
        fireTask(GetText.tr("Extracting Server Pack"));
        fireSubProgressUnknown();

        Path serverPackExtractedPath = this.temp.resolve("serverpack");
        ArchiveUtils.extract(serverPackFile, serverPackExtractedPath);
        Files.delete(serverPackFile);

        File[] directories = serverPackExtractedPath.toFile()
            .listFiles((dir, name) -> !name.startsWith(".") && new File(dir, name).isDirectory());

        // Only 1 folder, so likely a folder within the folder
        if (directories.length == 1) {
            Utils.moveDirectory(directories[0], root.toFile());

            // move any other files in the root
            for (File file : serverPackExtractedPath.toFile().listFiles()) {
                FileUtils.moveFile(file.toPath(), root);
            }
            FileUtils.deleteDirectory(serverPackExtractedPath);
        } else {
            Utils.moveDirectory(serverPackExtractedPath.toFile(), root.toFile());
        }

        hideSubProgressBar();
        addPercent(30);

        this.packVersion = new Version();
        packVersion.version = version._curseForgeFile.displayName;
    }

    private String getAnalyticsPlatform() {
        if (this.curseForgeManifest != null) {
            return "CurseForge";
        } else if (this.ftbPackManifest != null) {
            return "FTB";
        } else if (this.modrinthManifest != null) {
            return "Modrinth";
        } else if (this.multiMCManifest != null) {
            return "MultiMC";
        } else if (this.technicModpack != null) {
            return "Technic";
        } else if (this.pack.vanillaInstance) {
            return "Vanilla";
        }

        return "ATLauncher";
    }

    private void downloadPackVersionJson() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "ATLauncher"));
        fireSubProgressUnknown();

        this.packVersion = NetworkClient.get(this.pack.getJsonDownloadUrl(version.version),
            com.atlauncher.data.json.Version.class);

        if (this.packVersion == null) {
            throw new Exception("Failed to download pack version definition");
        }

        this.packVersion.compileColours();

        hideSubProgressBar();
    }

    private void generatePackVersionFromCurseForgeManifest() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "CurseForge"));
        fireSubProgressUnknown();

        if (!curseForgeManifest.manifestType.equals("minecraftModpack")) {
            throw new Exception("Manifest is not a Minecraft Modpack");
        }

        if (curseForgeManifest.manifestVersion != 1) {
            LogManager.warn("Manifest is version " + curseForgeManifest.manifestVersion + " which may be an issue!");
        }

        this.packVersion = new Version();
        packVersion.version = Optional.ofNullable(curseForgeManifest.version).orElse("1.0.0");
        packVersion.minecraft = curseForgeManifest.minecraft.version;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        packVersion.loader = new com.atlauncher.data.json.Loader();

        boolean hasJumpLoader = curseForgeManifest.files.stream()
            .anyMatch(m -> m.projectID == Constants.CURSEFORGE_JUMPLOADER_MOD_ID);

        if (hasJumpLoader) {
            java.lang.reflect.Type type = new TypeToken<List<FabricMetaVersion>>() {
            }.getType();

            List<FabricMetaVersion> loaders = NetworkClient.get(
                String.format("https://meta.fabricmc.net/v2/versions/loader/%s?limit=1", packVersion.minecraft),
                type);

            if (loaders == null || loaders.isEmpty()) {
                throw new Exception("Failed to get Fabric version for pack containing JumpLoader");
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", packVersion.minecraft);
            loaderMeta.put("loader", loaders.get(0).loader.version);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
        } else {
            CurseForgeModLoader loaderVersion = curseForgeManifest.minecraft.modLoaders.stream().filter(e -> e.primary)
                .findFirst().orElse(null);

            if (loaderVersion == null) {
                throw new Exception("Failed to find loader version");
            }

            if (loaderVersion.id.startsWith("forge-")) {
                String forgeVersionString = loaderVersion.id.replace("forge-", "");

                if (forgeVersionString.equals("recommended")) {
                    forgeVersionString = ForgeLoader.getRecommendedVersion(curseForgeManifest.minecraft.version);

                    // if recommended version is null, use latest
                    if (forgeVersionString == null) {
                        forgeVersionString = ForgeLoader.getLatestVersion(curseForgeManifest.minecraft.version);
                    }
                } else if (forgeVersionString.equals("latest")) {
                    forgeVersionString = ForgeLoader.getLatestVersion(curseForgeManifest.minecraft.version);
                }

                GetForgeLoaderVersionQuery.Data response = GraphqlClient
                    .callAndWait(new GetForgeLoaderVersionQuery(forgeVersionString));

                if (response == null || response.forgeVersion() == null) {
                    throw new Exception("Failed to find loader version for " + forgeVersionString);
                }

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", curseForgeManifest.minecraft.version);

                loaderMeta.put("version", response.forgeVersion().version());
                loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
                if (response.forgeVersion().installerSize() != null
                    && response.forgeVersion().installerSha1Hash() != null) {
                    loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                    loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
                }
                if (response.forgeVersion().universalSize() != null
                    && response.forgeVersion().universalSha1Hash() != null) {
                    loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                    loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
                }
                if (response.forgeVersion().clientSize() != null
                    && response.forgeVersion().clientSha1Hash() != null) {
                    loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                    loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
                }
                if (response.forgeVersion().serverSize() != null
                    && response.forgeVersion().serverSha1Hash() != null) {
                    loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                    loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
                }

                packVersion.loader.metadata = loaderMeta;

                if (Utils.matchVersion(curseForgeManifest.minecraft.version, "1.13", false, true)) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
                } else if (Utils.matchVersion(curseForgeManifest.minecraft.version, "1.5", true, true)) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
                } else {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
                }
            } else if (loaderVersion.id.startsWith("fabric-")) {
                String fabricVersionString = loaderVersion.id.replace("fabric-", "");

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);
                loaderMeta.put("loader", fabricVersionString);
                packVersion.loader.metadata = loaderMeta;

                // not technically supported, no examples, but should be right
                if (MinecraftManager.getMinecraftVersion(packVersion.minecraft).is1132OrOlder()) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
                } else {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
                }
            } else if (loaderVersion.id.startsWith("quilt-")) {
                String quiltVersionString = loaderVersion.id.replace("quilt-", "");

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);
                loaderMeta.put("loader", quiltVersionString);
                packVersion.loader.metadata = loaderMeta;
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader";
            } else if (loaderVersion.id.startsWith("neoforge-")) {
                String neoForgeVersionString = loaderVersion.id.replace("neoforge-", "");

                GetNeoForgeLoaderVersionQuery.Data response = GraphqlClient
                    .callAndWait(new GetNeoForgeLoaderVersionQuery(neoForgeVersionString));

                if (response == null || response.neoForgeVersion() == null) {
                    throw new Exception("Failed to find loader version for " + neoForgeVersionString);
                }

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);

                loaderMeta.put("version", response.neoForgeVersion().version());
                loaderMeta.put("rawVersion", response.neoForgeVersion().rawVersion());

                packVersion.loader.metadata = loaderMeta;
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
            } else {
                throw new Exception("Loader of id " + loaderVersion.id + " is unknown.");
            }
        }

        int[] projectIdsFound = curseForgeManifest.files.stream().mapToInt(file -> file.projectID).toArray();

        Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi.getProjectsAsMap(projectIdsFound);

        List<CurseForgeFile> filesFound = CurseForgeApi
            .getFiles(curseForgeManifest.files.stream().mapToInt(file -> file.fileID).toArray());

        List<Pair<CurseForgeProject, CurseForgeFile>> manualDownloadMods = Collections
            .synchronizedList(new ArrayList<>());

        List<CurseForgeFile> filesForManualDownload = curseForgeManifest.files.parallelStream()
            .filter(file -> {
                Optional<CurseForgeFile> curseForgeFile = filesFound.stream().filter(f -> f.id == file.fileID)
                    .findFirst();

                return curseForgeFile.filter(forgeFile -> forgeFile.downloadUrl == null).isPresent();
            }).map(file -> filesFound.stream().filter(f -> f.id == file.fileID)
                .findFirst().get())
            .collect(Collectors.toList());

        Map<String, ModrinthVersion> modrinthVersions = new HashMap<>();
        Map<String, ModrinthProject> modrinthProjects = new HashMap<>();

        if (!filesForManualDownload.isEmpty()) {
            String[] sha1Hashes = filesForManualDownload.parallelStream()
                .map(file -> file.hashes.stream().filter(CurseForgeFileHash::isSha1).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .map(hash -> hash.value)
                .toArray(String[]::new);

            modrinthVersions.putAll(ModrinthApi.getVersionsFromSha1Hashes(sha1Hashes));

            if (!modrinthVersions.isEmpty()) {
                modrinthProjects.putAll(ModrinthApi.getProjectsAsMap(
                    modrinthVersions.values().parallelStream().map(mv -> mv.projectId).toArray(String[]::new)));
            }
        }

        packVersion.mods = curseForgeManifest.files.parallelStream().map(file -> {
            CurseForgeProject curseForgeProject = Optional.ofNullable(foundProjects.get(file.projectID))
                .orElseGet(() -> CurseForgeApi.getProjectById(file.projectID));

            CurseForgeFile curseForgeFile = filesFound.stream().filter(f -> f.id == file.fileID).findFirst()
                .orElseGet(() -> CurseForgeApi
                    .getFileForProject(file.projectID, file.fileID));

            if (curseForgeFile.downloadUrl == null) {
                LogManager.debug(String.format(Locale.ENGLISH,
                    "File %s (%d) for mod %s (%d) has no downloadUrl and allowModDistribution set to %s",
                    curseForgeFile.displayName, curseForgeFile.id, curseForgeProject.name, curseForgeProject.id,
                    curseForgeProject.allowModDistribution == null ? "null"
                        : curseForgeProject.allowModDistribution.toString()));

                Optional<CurseForgeFileHash> sha1Hash = curseForgeFile.hashes.stream()
                    .filter(CurseForgeFileHash::isSha1)
                    .findFirst();
                if (sha1Hash.isPresent()) {
                    ModrinthVersion modrinthVersion = modrinthVersions.get(sha1Hash.get().value);

                    if (modrinthVersion != null
                        && modrinthProjects.containsKey(modrinthVersion.projectId)) {
                        Mod modToAdd = curseForgeFile.convertToMod(curseForgeProject);
                        modToAdd.url = modrinthVersion.getFileBySha1(sha1Hash.get().value).url;
                        modToAdd.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);
                        modToAdd.modrinthVersion = modrinthVersion;
                        modToAdd.optional = !file.required;

                        LogManager.debug("Found matching mod from Modrinth called " + modToAdd.modrinthProject.title);

                        return modToAdd;
                    }
                }

                manualDownloadMods.add(new Pair<>(curseForgeProject, curseForgeFile));

                Mod modToAdd = curseForgeFile.convertToMod(curseForgeProject);
                modToAdd.download = DownloadType.browser;
                modToAdd.url = curseForgeProject.getBrowserDownloadUrl(curseForgeFile);
                modToAdd.file = curseForgeFile.fileName;
                modToAdd.optional = !file.required;
                return modToAdd;
            }

            Mod mod = curseForgeFile.convertToMod(curseForgeProject);
            mod.optional = !file.required;

            return mod;
        }).filter(Objects::nonNull).collect(Collectors.toList());

        if (!manualDownloadMods.isEmpty() && !App.settings.seenCurseForgeProjectDistributionDialog) {
            App.settings.seenCurseForgeProjectDistributionDialog = true;
            App.settings.save();

            DialogManager.okDialog().setType(DialogManager.WARNING)
                .setTitle(GetText.tr("Mods Not Available"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "We were unable to download some of the mods from this pack.<br/>This is likely due to the author of that mod disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to start downloading these mods manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, you can continue through all the mods that have disabled this toggle.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                    .build())
                .show();
        }

        hideSubProgressBar();
    }

    private void generatePackVersionFromCurseForge() throws Exception {
        addPercent(5);

        // #. {0} is the platform the modpack is from (e.g. CurseForge/Modrinth)
        fireTask(GetText.tr("Downloading Manifest From {0}", "CurseForge"));
        fireSubProgressUnknown();

        Path manifestFile = this.temp.resolve(version._curseForgeFile.fileName.toLowerCase(Locale.ENGLISH));

        if (version._curseForgeFile.downloadUrl == null) {
            if (!App.settings.seenCurseForgeProjectDistributionDialog) {
                App.settings.seenCurseForgeProjectDistributionDialog = true;
                App.settings.save();

                DialogManager.okDialog().setType(DialogManager.WARNING)
                    .setTitle(GetText.tr("Mod Not Available"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We were unable to download this modpack.<br/>This is likely due to the author of the modpack disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to download the modpack manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, we can continue with installing the modpack.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                        .build())
                    .show();
            }

            String filename = version._curseForgeFile.fileName;
            String filename2 = version._curseForgeFile.fileName.replace(" ", "+");

            File fileLocation = FileSystem.DOWNLOADS.resolve(filename).toFile();
            File fileLocation2 = FileSystem.DOWNLOADS.resolve(filename2).toFile();
            if (!fileLocation.exists() && !fileLocation2.exists()) {
                File downloadsFolderFile = new File(FileSystem.getUserDownloadsPath().toFile(),
                    filename);
                File downloadsFolderFile2 = new File(FileSystem.getUserDownloadsPath().toFile(),
                    filename2);
                if (downloadsFolderFile.exists()) {
                    Utils.moveFile(downloadsFolderFile, fileLocation, true);
                } else if (downloadsFolderFile2.exists()) {
                    Utils.moveFile(downloadsFolderFile2, fileLocation, true);
                }

                while (!fileLocation.exists() && !fileLocation2.exists()) {
                    int retValue = 1;
                    do {
                        if (retValue == 1) {
                            OS.openWebBrowser(pack.curseForgeProject.getBrowserDownloadUrl(version._curseForgeFile));
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
                            .showWithFileMonitoring(version._curseForgeFile.fileLength, 1, fileLocation,
                                fileLocation2,
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
                    }
                }
            }

            if (fileLocation.exists()) {
                FileUtils.moveFile(fileLocation.toPath(), manifestFile, true);
            } else if (fileLocation2.exists()) {
                FileUtils.moveFile(fileLocation2.toPath(), manifestFile, true);
            }
        } else {
            com.atlauncher.network.Download manifestDownload = com.atlauncher.network.Download.build()
                .setUrl(version._curseForgeFile.downloadUrl).downloadTo(manifestFile)
                .size(version._curseForgeFile.fileLength);

            Optional<CurseForgeFileHash> md5Hash = version._curseForgeFile.hashes.stream()
                .filter(CurseForgeFileHash::isMd5)
                .findFirst();
            Optional<CurseForgeFileHash> sha1Hash = version._curseForgeFile.hashes.stream()
                .filter(CurseForgeFileHash::isSha1)
                .findFirst();

            if (md5Hash.isPresent()) {
                manifestDownload = manifestDownload.hash(md5Hash.get().value);
            } else if (sha1Hash.isPresent()) {
                manifestDownload = manifestDownload.hash(sha1Hash.get().value);
            } else {
                manifestDownload = manifestDownload.fingerprint(version._curseForgeFile.packageFingerprint);
            }

            manifestDownload = manifestDownload.withInstanceInstaller(this)
                .withHttpClient(Network.createProgressClient(this));

            this.setTotalBytes(version._curseForgeFile.fileLength);
            manifestDownload.downloadFile();
        }

        fireTask(GetText.tr("Extracting Manifest"));
        fireSubProgressUnknown();

        curseForgeManifest = Gsons.DEFAULT.fromJson(ArchiveUtils.getFile(manifestFile, "manifest.json"),
            CurseForgeManifest.class);
        curseForgeExtractedPath = this.temp.resolve("curseforgeimport");

        ArchiveUtils.extract(manifestFile, curseForgeExtractedPath);
        Files.delete(manifestFile);

        generatePackVersionFromCurseForgeManifest();

        hideSubProgressBar();
    }

    private void generatePackVersionFromModrinth() throws Exception {
        addPercent(5);

        // #. {0} is the platform the modpack is from (e.g. CurseForge/Modrinth)
        fireTask(GetText.tr("Downloading Manifest From {0}", "Modrinth"));
        fireSubProgressUnknown();

        ModrinthFile file = version._modrinthVersion.getPrimaryFile();
        String filename = file.filename.toLowerCase(Locale.ENGLISH).replaceAll("[\\\"?:*<>|]", "");

        Path manifestFile = this.temp.resolve(filename);

        com.atlauncher.network.Download manifestDownload = com.atlauncher.network.Download.build().setUrl(file.url)
            .downloadTo(manifestFile).withInstanceInstaller(this)
            .withHttpClient(Network.createProgressClient(this));

        if (file.hashes != null && file.hashes.containsKey("sha512")) {
            manifestDownload = manifestDownload.hash(file.hashes.get("sha512"));
        } else if (file.hashes != null && file.hashes.containsKey("sha1")) {
            manifestDownload = manifestDownload.hash(file.hashes.get("sha1"));
        }

        if (file.size != null && file.size != 0) {
            manifestDownload = manifestDownload.size(file.size);
            this.setTotalBytes(file.size);
        }

        manifestDownload.downloadFile();

        fireTask(GetText.tr("Extracting Manifest"));
        fireSubProgressUnknown();

        modrinthExtractedPath = this.temp.resolve("modrinthimport");
        ArchiveUtils.extract(manifestFile, modrinthExtractedPath);

        try (InputStreamReader fileReader = new InputStreamReader(
            Files.newInputStream(modrinthExtractedPath.resolve("modrinth.index.json")),
            StandardCharsets.UTF_8)) {
            modrinthManifest = Gsons.DEFAULT.fromJson(fileReader, ModrinthModpackManifest.class);
        } catch (Exception e) {
            LogManager.logStackTrace("Failed to read modrinth.index.json file", e);
            Files.delete(manifestFile);
            Files.delete(modrinthExtractedPath);
            throw e;
        }

        Files.delete(manifestFile);

        generatePackVersionFromModrinthManifest();

        hideSubProgressBar();
    }

    private void generatePackVersionFromFTB() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "FTB"));
        fireSubProgressUnknown();

        this.ftbPackVersionManifest = NetworkClient.getCached(
            String.format("%s/modpack/%s/%s", Constants.FTB_API_URL, ftbPackManifest.id,
                this.version._ftbId),
            FTBPackVersionManifest.class,
            new CacheControl.Builder().maxStale(1, TimeUnit.HOURS).build());

        FTBPackVersionManifestTarget minecraftTarget = this.ftbPackVersionManifest.targets.stream()
            .filter(t -> t.type == FTBPackVersionManifestTargetType.GAME).findFirst().orElse(null);

        if (minecraftTarget == null) {
            throw new Exception("Minecraft target couldn't be found.");
        }

        this.packVersion = new Version();
        packVersion.version = this.version.version;
        packVersion.minecraft = minecraftTarget.version;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;
        packVersion.memory = ftbPackVersionManifest.specs.recommended;

        this.version.minecraftVersion = MinecraftManager.getMinecraftVersion(packVersion.minecraft);

        packVersion.loader = new com.atlauncher.data.json.Loader();

        FTBPackVersionManifestTarget modloaderTarget = this.ftbPackVersionManifest.targets.stream()
            .filter(t -> t.type == FTBPackVersionManifestTargetType.MODLOADER).findFirst().orElse(null);

        if (modloaderTarget == null) {
            throw new Exception("Modloader target couldn't be found.");
        }

        if (modloaderTarget.name.equalsIgnoreCase("forge")) {
            String forgeVersionString = modloaderTarget.version;

            GetForgeLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetForgeLoaderVersionQuery(forgeVersionString));

            if (response == null || response.forgeVersion() == null) {
                throw new Exception("Failed to find loader version for " + forgeVersionString);
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", packVersion.minecraft);

            loaderMeta.put("version", response.forgeVersion().version());
            loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
            if (response.forgeVersion().installerSize() != null
                && response.forgeVersion().installerSha1Hash() != null) {
                loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
            }
            if (response.forgeVersion().universalSize() != null
                && response.forgeVersion().universalSha1Hash() != null) {
                loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
            }
            if (response.forgeVersion().clientSize() != null
                && response.forgeVersion().clientSha1Hash() != null) {
                loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
            }
            if (response.forgeVersion().serverSize() != null
                && response.forgeVersion().serverSha1Hash() != null) {
                loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
            }

            packVersion.loader.metadata = loaderMeta;

            if (Utils.matchVersion(packVersion.minecraft, "1.13", false, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
            } else if (Utils.matchVersion(packVersion.minecraft, "1.5", true, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
            } else {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
            }
        } else if (modloaderTarget.name.equalsIgnoreCase("neoforge")) {
            String neoForgeVersionString = modloaderTarget.version;

            GetNeoForgeLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetNeoForgeLoaderVersionQuery(neoForgeVersionString));

            if (response == null || response.neoForgeVersion() == null) {
                throw new Exception("Failed to find loader version for " + neoForgeVersionString);
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", packVersion.minecraft);
            loaderMeta.put("version", response.neoForgeVersion().version());
            loaderMeta.put("rawVersion", response.neoForgeVersion().rawVersion());

            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
        } else if (modloaderTarget.name.equalsIgnoreCase("fabric")) {
            String fabricVersionString = modloaderTarget.version;

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", packVersion.minecraft);
            loaderMeta.put("loader", fabricVersionString);
            packVersion.loader.metadata = loaderMeta;

            // not technically supported, no examples, but should be right
            if (this.version.minecraftVersion.is1132OrOlder()) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
            } else {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
            }
        } else if (modloaderTarget.name.equalsIgnoreCase("quilt")) {
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", packVersion.minecraft);
            loaderMeta.put("loader", modrinthManifest.dependencies.get("quilt-loader"));
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader";
        } else {
            throw new Exception("Unknown modloader with name of " + modloaderTarget.name);
        }

        FTBPackVersionModsManifest modsManifest = FTBApi.getModsManifest(ftbPackManifest.id,
            this.version._ftbId);

        // find the mods with no url provided
        long nonDownloadableMods = ftbPackVersionManifest.files.parallelStream()
            .filter(f -> {
                if (f.type != FTBPackVersionManifestFileType.MOD) {
                    return false;
                }

                if (f.url != null && !f.url.isEmpty()) {
                    return false;
                }

                if (f.curseforge != null && f.curseforge.file != null && f.curseforge.project != null) {
                    return false;
                }

                if (modsManifest == null) {
                    LogManager
                        .error(String.format("Mod %s is not available to be downloaded for this pack", f.name));
                    return true;
                }

                Optional<FTBPackVersionManifestMod> modInfo = modsManifest.mods.parallelStream()
                    .filter(m -> m.filename != null && (m.filename.equalsIgnoreCase(f.name)
                        || m.filename.equalsIgnoreCase(f.name.replace("_", ""))
                        || m.filename.replace("_", " ").equalsIgnoreCase(f.name)))
                    .findFirst();

                if (!modInfo.isPresent()) {
                    LogManager
                        .error(String.format("Mod %s is not available to be downloaded for this pack", f.name));
                    return true;
                }

                return modInfo.get().curseFile == null || modInfo.get().curseProject == null;
            })
            .count();

        // if not all non downloadable mods, we can't install
        if (nonDownloadableMods != 0) {
            DialogManager.okDialog().setType(DialogManager.ERROR)
                .setTitle(GetText.tr("{0} Mods Not Available", nonDownloadableMods))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "Some of the mods for this pack are not available to download.<br/><br/>At this time you can only install this pack via FTB launcher."))
                    .build())
                .show();
            throw new Exception("Cannot install pack as there are files we cannot download");
        }

        // mods with CurseForge information
        List<Pair<Integer, Integer>> curseForgeMods = ftbPackVersionManifest.files.parallelStream()
            .filter(f -> f.type == FTBPackVersionManifestFileType.MOD && (f.url == null || f.url.isEmpty()))
            .map(f -> {
                if (f.curseforge != null && f.curseforge.file != null && f.curseforge.project != null) {
                    return new Pair<Integer, Integer>(f.curseforge.project, f.curseforge.file);
                }

                if (modsManifest == null) {
                    return null;
                }

                Optional<FTBPackVersionManifestMod> modInfo = modsManifest.mods.parallelStream()
                    .filter(m -> m.filename != null && (m.filename.equalsIgnoreCase(f.name)
                        || m.filename.equalsIgnoreCase(f.name.replace("_", ""))
                        || m.filename.replace("_", " ").equalsIgnoreCase(f.name)))
                    .findFirst();

                if (!modInfo.isPresent()) {
                    return null;
                }

                return new Pair<Integer, Integer>(modInfo.get().curseProject, modInfo.get().curseFile);
            }).filter(m -> m != null).collect(Collectors.toList());

        Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi
            .getProjectsAsMap(curseForgeMods.stream().mapToInt(file -> file.left()).toArray());

        List<CurseForgeFile> filesFound = CurseForgeApi
            .getFiles(curseForgeMods.stream().mapToInt(file -> file.right()).toArray());

        List<Pair<CurseForgeProject, CurseForgeFile>> manualDownloadMods = new ArrayList<>();

        List<CurseForgeFile> filesForManualDownload = ftbPackVersionManifest.files.stream()
            .map(file -> {
                if (file.url != null && !file.url.isEmpty()) {
                    return null;
                }

                Optional<FTBPackVersionManifestMod> modInfo = modsManifest == null ? Optional.empty()
                    : modsManifest.mods.parallelStream()
                        .filter(m -> m.filename != null && (m.filename.equalsIgnoreCase(file.name)
                            || m.filename.equalsIgnoreCase(file.name.replace("_", ""))
                            || m.filename.replace("_", " ").equalsIgnoreCase(file.name)))
                        .findFirst();

                int curseFileId = (modInfo.isPresent()
                    && modInfo.get().curseFile != null)
                    ? modInfo.get().curseFile
                    : file.curseforge.file;

                Optional<CurseForgeFile> curseForgeFile = filesFound.stream().filter(f -> f.id == curseFileId)
                    .findFirst();

                if (!curseForgeFile.isPresent() || curseForgeFile.get().downloadUrl != null) {
                    return null;
                }

                return curseForgeFile.get();
            })
            .filter(m -> m != null)
            .collect(Collectors.toList());

        Map<String, ModrinthVersion> modrinthVersions = new HashMap<>();
        Map<String, ModrinthProject> modrinthProjects = new HashMap<>();

        if (filesForManualDownload.size() != 0) {
            String[] sha1Hashes = filesForManualDownload.parallelStream()
                .map(file -> file.hashes.stream().filter(h -> h.isSha1()).findFirst().orElse(null))
                .filter(f -> f != null)
                .map(hash -> hash.value)
                .toArray(String[]::new);

            modrinthVersions.putAll(ModrinthApi.getVersionsFromSha1Hashes(sha1Hashes));

            if (modrinthVersions.size() != 0) {
                modrinthProjects.putAll(ModrinthApi.getProjectsAsMap(
                    modrinthVersions.values().parallelStream().map(mv -> mv.projectId).toArray(String[]::new)));
            }
        }

        packVersion.mods = ftbPackVersionManifest.files.stream()
            .filter(f -> f.type == FTBPackVersionManifestFileType.MOD).map(file -> {
                if (file.url != null && !file.url.isEmpty()) {
                    return file.convertToMod();
                }

                Optional<FTBPackVersionManifestMod> modInfo = modsManifest == null ? Optional.empty()
                    : modsManifest.mods.parallelStream()
                        .filter(m -> {
                            return m.filename != null && (m.filename.equalsIgnoreCase(file.name)
                                || m.filename.equalsIgnoreCase(file.name.replace("_", ""))
                                || m.filename.replace("_", " ").equalsIgnoreCase(file.name));
                        })
                        .findFirst();

                int curseProjectId = ((file.curseforge == null || file.curseforge.project == null
                    || file.curseforge.file == null) && modInfo.isPresent()
                    && modInfo.get().curseProject != null) ? modInfo.get().curseProject
                    : file.curseforge.project;
                int curseFileId = ((file.curseforge == null || file.curseforge.project == null
                    || file.curseforge.file == null) && modInfo.isPresent()
                    && modInfo.get().curseFile != null) ? modInfo.get().curseFile : file.curseforge.file;

                CurseForgeProject curseForgeProject = Optional
                    .ofNullable(foundProjects.get(curseProjectId))
                    .orElseGet(() -> CurseForgeApi.getProjectById(curseProjectId));

                CurseForgeFile curseForgeFile = filesFound.stream().filter(f -> f.id == curseFileId)
                    .findFirst()
                    .orElseGet(() -> CurseForgeApi
                        .getFileForProject(curseProjectId, curseFileId));

                if (curseForgeFile.downloadUrl == null) {
                    LogManager.debug(String.format(Locale.ENGLISH,
                        "File %s (%d) for mod %s (%d) has no downloadUrl and allowModDistribution set to %s",
                        curseForgeFile.displayName, curseForgeFile.id, curseForgeProject.name,
                        curseForgeProject.id,
                        curseForgeProject.allowModDistribution == null ? "null"
                            : curseForgeProject.allowModDistribution.toString()));

                    Optional<CurseForgeFileHash> sha1Hash = curseForgeFile.hashes.stream().filter(h -> h.isSha1())
                        .findFirst();
                    if (sha1Hash.isPresent()) {
                        ModrinthVersion modrinthVersion = modrinthVersions.get(sha1Hash.get().value);

                        if (modrinthVersion != null
                            && modrinthProjects.containsKey(modrinthVersion.projectId)) {
                            Mod modToAdd = curseForgeFile.convertToMod(curseForgeProject);
                            modToAdd.url = modrinthVersion.getFileBySha1(sha1Hash.get().value).url;
                            modToAdd.modrinthProject = modrinthProjects.get(modrinthVersion.projectId);
                            modToAdd.modrinthVersion = modrinthVersion;

                            LogManager.debug(
                                "Found matching mod from Modrinth called " + modToAdd.modrinthProject.title);

                            return modToAdd;
                        }
                    }

                    manualDownloadMods
                        .add(new Pair<CurseForgeProject, CurseForgeFile>(curseForgeProject, curseForgeFile));

                    Mod modToAdd = curseForgeFile.convertToMod(curseForgeProject);
                    modToAdd.download = DownloadType.browser;
                    modToAdd.url = curseForgeProject.getBrowserDownloadUrl(curseForgeFile);
                    modToAdd.file = curseForgeFile.fileName.replace(" ", "+");
                    return modToAdd;
                }

                return curseForgeFile.convertToMod(curseForgeProject);
            })
            .collect(Collectors.toList());

        if (manualDownloadMods.size() != 0 && !App.settings.seenCurseForgeProjectDistributionDialog) {
            App.settings.seenCurseForgeProjectDistributionDialog = true;
            App.settings.save();

            DialogManager.okDialog().setType(DialogManager.WARNING)
                .setTitle(GetText.tr("Mods Not Available"))
                .setContent(new HTMLBuilder().center().text(GetText.tr(
                        "We were unable to download some of the mods from this pack.<br/>This is likely due to the author of that mod disabling third party clients from downloading it.<br/><br/>You'll be prompted shortly to start downloading these mods manually through your browser to your downloads folder.<br/>Once you've downloaded the file that was opened in your browser to your downloads folder, you can continue through all the mods that have disabled this toggle.<br/><br/>This process is unfortunate, but we don't have any choice in this matter and has to be done this way."))
                    .build())
                .show();
        }

        hideSubProgressBar();
    }

    private void generatePackVersionFromTechnicSolder() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "Technic Solder"));
        fireSubProgressUnknown();

        String minecraftVersion = technicModpack.minecraft;

        technicSolderModpackManifest = TechnicApi.getSolderModpackManifest(technicModpack.solder, technicModpack.name,
            this.version.version);

        if (technicSolderModpackManifest.minecraft != null) {
            minecraftVersion = technicSolderModpackManifest.minecraft;
        }

        if (minecraftVersion == null) {
            throw new Exception("Minecraft version couldn't be found.");
        }

        this.packVersion = new Version();
        packVersion.version = this.version.version;
        packVersion.minecraft = minecraftVersion;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        if (technicSolderModpackManifest.memory != null) {
            try {
                packVersion.memory = Integer.parseInt(technicSolderModpackManifest.memory);
            } catch (NumberFormatException ignored) {
                // ignored
            }
        }

        this.version.minecraftVersion = MinecraftManager.getMinecraftVersion(packVersion.minecraft);

        technicSolderModsToDownload.addAll(technicSolderModpackManifest.mods.parallelStream()
            .map(TechnicModpackManifestMod::convertToMod).collect(Collectors.toList()));

        hideSubProgressBar();
    }

    private void generatePackVersionFromTechnicZip() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Downloading modpack.zip"));
        fireSubProgressUnknown();

        Path tempZip = FileSystem.TEMP.resolve("technic-" + technicModpack.name + "-modpack.zip");
        Path unzipLocation = FileSystem.TEMP.resolve("technic-" + technicModpack.name);
        OkHttpClient httpClient = Network.createProgressClient(this);
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(technicModpack.url)
            .downloadTo(tempZip).unzipTo(unzipLocation).withInstanceInstaller(this).withHttpClient(httpClient);

        if (download.needToDownload()) {
            this.setTotalBytes(download.getFilesize());

            download.downloadFile();
        }

        technicModpackExtractedPath = unzipLocation;

        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "Technic Zip"));
        fireSubProgressUnknown();

        if (technicModpack.minecraft == null) {
            throw new Exception("Minecraft version couldn't be found.");
        }

        this.packVersion = new Version();
        packVersion.version = this.version.version;
        packVersion.minecraft = technicModpack.minecraft;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        this.version.minecraftVersion = MinecraftManager.getMinecraftVersion(packVersion.minecraft);

        getLoaderInformationFromTechnicModpack(technicModpackExtractedPath);

        hideSubProgressBar();
    }

    private void getLoaderInformationFromTechnicModpack(Path basePath) throws Exception {
        Path versionJsonPath = basePath.resolve("bin/version.json");
        Path modpackJarPath = basePath.resolve("bin/modpack.jar");

        MinecraftVersion versionJson = null;

        if (Files.exists(versionJsonPath)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(versionJsonPath), StandardCharsets.UTF_8)) {
                versionJson = Gsons.DEFAULT.fromJson(fileReader, MinecraftVersion.class);
            } catch (Exception e) {
                LogManager.error("Error reading in version.json");
                throw e;
            }

            FileUtils.delete(versionJsonPath);
        }

        if (Files.exists(modpackJarPath) && ArchiveUtils.archiveContainsFile(modpackJarPath, "version.json")) {
            try {
                versionJson = Gsons.DEFAULT.fromJson(ArchiveUtils.getFile(modpackJarPath, "version.json"),
                    MinecraftVersion.class);
            } catch (Exception e) {
                LogManager.error("Error reading in version.json from modpack.jar");
                throw e;
            }

            FileUtils.delete(modpackJarPath);
        }

        if (versionJson != null && versionJson.libraries != null) {
            Optional<Library> forgeLibrary = versionJson.libraries.stream()
                .filter(l -> l.name.startsWith("net.minecraftforge:forge:")
                    || l.name.startsWith("net.minecraftforge:minecraftforge:")
                    || l.name.startsWith("net.minecraftforge:fmlloader:"))
                .findFirst();
            Optional<Library> fabricLibrary = versionJson.libraries.stream()
                .filter(l -> l.name.startsWith("net.fabricmc:fabric-loader:")).findFirst();
            boolean isNeoForge = versionJson.id.startsWith("neoforge-");

            if (forgeLibrary.isPresent() || fabricLibrary.isPresent() || isNeoForge) {
                packVersion.loader = new com.atlauncher.data.json.Loader();

                if (forgeLibrary.isPresent()) {
                    String forgeLibraryName = forgeLibrary.get().name;
                    String forgeVersionString = forgeLibraryName.substring(forgeLibraryName.lastIndexOf(":") + 1);

                    GetForgeLoaderVersionQuery.Data response = GraphqlClient
                        .callAndWait(new GetForgeLoaderVersionQuery(forgeVersionString));

                    if (response == null || response.forgeVersion() == null) {
                        throw new Exception("Failed to find loader version for " + forgeVersionString);
                    }

                    Map<String, Object> loaderMeta = new HashMap<>();
                    loaderMeta.put("minecraft", packVersion.minecraft);

                    loaderMeta.put("version", response.forgeVersion().version());
                    loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
                    if (response.forgeVersion().installerSize() != null
                        && response.forgeVersion().installerSha1Hash() != null) {
                        loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                        loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
                    }
                    if (response.forgeVersion().universalSize() != null
                        && response.forgeVersion().universalSha1Hash() != null) {
                        loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                        loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
                    }
                    if (response.forgeVersion().clientSize() != null
                        && response.forgeVersion().clientSha1Hash() != null) {
                        loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                        loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
                    }
                    if (response.forgeVersion().serverSize() != null
                        && response.forgeVersion().serverSha1Hash() != null) {
                        loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                        loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
                    }

                    packVersion.loader.metadata = loaderMeta;

                    if (Utils.matchVersion(packVersion.minecraft, "1.13", false, true)) {
                        packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
                    } else if (Utils.matchVersion(packVersion.minecraft, "1.5", true, true)) {
                        packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
                    } else {
                        packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
                    }
                } else if (fabricLibrary.isPresent()) {
                    String fabricLibraryName = fabricLibrary.get().name;
                    String fabricVersionString = fabricLibraryName.substring(fabricLibraryName.lastIndexOf(":") + 1);

                    Map<String, Object> loaderMeta = new HashMap<>();
                    loaderMeta.put("minecraft", packVersion.minecraft);
                    loaderMeta.put("loader", fabricVersionString);
                    packVersion.loader.metadata = loaderMeta;

                    // not technically supported, no examples, but should be right
                    if (MinecraftManager.getMinecraftVersion(packVersion.minecraft).is1132OrOlder()) {
                        packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
                    } else {
                        packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
                    }
                } else if (isNeoForge) {
                    String neoForgeVersionString = versionJson.id.substring(versionJson.id.lastIndexOf("-") + 1);

                    GetNeoForgeLoaderVersionQuery.Data neoForgeVersionResponse = GraphqlClient
                        .callAndWait(new GetNeoForgeLoaderVersionQuery(neoForgeVersionString));

                    if (neoForgeVersionResponse == null || neoForgeVersionResponse.neoForgeVersion() == null) {
                        throw new Exception("Failed to find loader version for " + neoForgeVersionString);
                    }

                    Map<String, Object> loaderMeta = new HashMap<>();
                    loaderMeta.put("minecraft", packVersion.minecraft);
                    loaderMeta.put("version", neoForgeVersionResponse.neoForgeVersion().version());
                    loaderMeta.put("rawVersion", neoForgeVersionResponse.neoForgeVersion().rawVersion());
                    packVersion.loader.metadata = loaderMeta;
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
                }
            }
        }
    }

    private void generatePackVersionFromModrinthManifest() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "Modrinth"));
        fireSubProgressUnknown();

        if (!modrinthManifest.game.equals("minecraft")) {
            throw new Exception(
                "Cannot install as the manifest is for game " + modrinthManifest.game + " and not for Minecraft");
        }

        if (!modrinthManifest.dependencies.containsKey("minecraft")) {
            throw new Exception("Cannot install as the manifest doesn't contain a minecraft dependency");
        }

        if (modrinthManifest.formatVersion != 1) {
            LogManager.warn("Manifest is version " + modrinthManifest.formatVersion + " which may be an issue!");
        }

        this.packVersion = new Version();
        packVersion.version = Optional.ofNullable(modrinthManifest.versionId).orElse("1.0.0");
        packVersion.minecraft = modrinthManifest.dependencies.get("minecraft");
        this.version.minecraftVersion = MinecraftManager
            .getMinecraftVersion(modrinthManifest.dependencies.get("minecraft"));
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        if (modrinthManifest.dependencies.containsKey("fabric-loader")
            || modrinthManifest.dependencies.containsKey("quilt-loader")
            || modrinthManifest.dependencies.containsKey("neoforge")
            || modrinthManifest.dependencies.containsKey("forge")) {
            packVersion.loader = new com.atlauncher.data.json.Loader();

            if (modrinthManifest.dependencies.containsKey("fabric-loader")) {
                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);
                loaderMeta.put("loader", modrinthManifest.dependencies.get("fabric-loader"));
                packVersion.loader.metadata = loaderMeta;

                // not technically supported, 1 example which seems to work
                if (this.version.minecraftVersion.is1132OrOlder()) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
                } else {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
                }
            } else if (modrinthManifest.dependencies.containsKey("quilt-loader")) {
                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);
                loaderMeta.put("loader", modrinthManifest.dependencies.get("quilt-loader"));
                packVersion.loader.metadata = loaderMeta;
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader";
            } else if (modrinthManifest.dependencies.containsKey("neoforge")) {
                String neoForgeVersionString = modrinthManifest.dependencies.get("neoforge");

                GetNeoForgeLoaderVersionQuery.Data response = GraphqlClient
                    .callAndWait(new GetNeoForgeLoaderVersionQuery(neoForgeVersionString));

                if (response == null || response.neoForgeVersion() == null) {
                    throw new Exception("Failed to find loader version for " + neoForgeVersionString);
                }

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);

                loaderMeta.put("version", response.neoForgeVersion().version());
                loaderMeta.put("rawVersion", response.neoForgeVersion().rawVersion());

                packVersion.loader.metadata = loaderMeta;
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
            } else if (modrinthManifest.dependencies.containsKey("forge")) {
                String forgeVersionString = modrinthManifest.dependencies.get("forge");

                GetForgeLoaderVersionQuery.Data response = GraphqlClient
                    .callAndWait(new GetForgeLoaderVersionQuery(forgeVersionString));

                if (response == null || response.forgeVersion() == null) {
                    throw new Exception("Failed to find loader version for " + forgeVersionString);
                }

                Map<String, Object> loaderMeta = new HashMap<>();
                loaderMeta.put("minecraft", packVersion.minecraft);

                loaderMeta.put("version", response.forgeVersion().version());
                loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
                if (response.forgeVersion().installerSize() != null
                    && response.forgeVersion().installerSha1Hash() != null) {
                    loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                    loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
                }
                if (response.forgeVersion().universalSize() != null
                    && response.forgeVersion().universalSha1Hash() != null) {
                    loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                    loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
                }
                if (response.forgeVersion().clientSize() != null
                    && response.forgeVersion().clientSha1Hash() != null) {
                    loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                    loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
                }
                if (response.forgeVersion().serverSize() != null
                    && response.forgeVersion().serverSha1Hash() != null) {
                    loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                    loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
                }

                packVersion.loader.metadata = loaderMeta;

                if (Utils.matchVersion(packVersion.minecraft, "1.13", false, true)) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
                } else if (Utils.matchVersion(packVersion.minecraft, "1.5", true, true)) {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
                } else {
                    packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
                }
            }
        }

        packVersion.mods = modrinthManifest.files.parallelStream().map(file -> file.convertToMod(isServer))
            .collect(Collectors.toList());

        hideSubProgressBar();
    }

    private void generatePackVersionFromMultiMC() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "MultiMC"));
        fireSubProgressUnknown();

        if (multiMCManifest.formatVersion != 1) {
            throw new Exception("Format is version " + multiMCManifest.formatVersion + " which I cannot install");
        }

        Optional<MultiMCComponent> minecraftVersionComponent = multiMCManifest.components.stream()
            .filter(c -> c.uid.equalsIgnoreCase("net.minecraft")).findFirst();

        if (!minecraftVersionComponent.isPresent()) {
            throw new Exception("No net.minecraft component present in manifest");
        }

        String minecraftVersion = minecraftVersionComponent.get().version;

        this.packVersion = new Version();
        packVersion.version = "1";
        packVersion.minecraft = minecraftVersion;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        packVersion.loader = new com.atlauncher.data.json.Loader();

        MultiMCComponent neoForgedComponent = multiMCManifest.components.stream()
            .filter(c -> c.uid.equalsIgnoreCase("net.neoforged")).findFirst().orElse(null);
        MultiMCComponent forgeComponent = multiMCManifest.components.stream()
            .filter(c -> c.uid.equalsIgnoreCase("net.minecraftforge")).findFirst().orElse(null);
        MultiMCComponent fabricLoaderComponent = multiMCManifest.components.stream()
            .filter(c -> c.uid.equalsIgnoreCase("net.fabricmc.fabric-loader")).findFirst().orElse(null);
        MultiMCComponent quiltLoaderComponent = multiMCManifest.components.stream()
            .filter(c -> c.uid.equalsIgnoreCase("org.quiltmc.quilt-loader")).findFirst().orElse(null);

        if (neoForgedComponent != null) {
            String neoForgeVersionString = neoForgedComponent.version;

            GetNeoForgeLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetNeoForgeLoaderVersionQuery(neoForgeVersionString));

            if (response == null || response.neoForgeVersion() == null) {
                throw new Exception("Failed to find loader version for " + neoForgeVersionString);
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", minecraftVersion);

            loaderMeta.put("version", response.neoForgeVersion().version());
            loaderMeta.put("rawVersion", response.neoForgeVersion().rawVersion());

            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
        } else if (forgeComponent != null) {
            String forgeVersionString = forgeComponent.version;

            GetForgeLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetForgeLoaderVersionQuery(forgeVersionString));

            if (response == null || response.forgeVersion() == null) {
                throw new Exception("Failed to find loader version for " + forgeVersionString);
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", minecraftVersion);

            loaderMeta.put("version", response.forgeVersion().version());
            loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
            if (response.forgeVersion().installerSize() != null
                && response.forgeVersion().installerSha1Hash() != null) {
                loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
            }
            if (response.forgeVersion().universalSize() != null
                && response.forgeVersion().universalSha1Hash() != null) {
                loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
            }
            if (response.forgeVersion().clientSize() != null
                && response.forgeVersion().clientSha1Hash() != null) {
                loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
            }
            if (response.forgeVersion().serverSize() != null
                && response.forgeVersion().serverSha1Hash() != null) {
                loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
            }

            packVersion.loader.metadata = loaderMeta;

            if (Utils.matchVersion(minecraftVersion, "1.13", false, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
            } else if (Utils.matchVersion(packVersion.minecraft, "1.5", true, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
            } else {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
            }
        } else if (fabricLoaderComponent != null) {
            String fabricVersionString = fabricLoaderComponent.version;

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", minecraftVersion);
            loaderMeta.put("loader", fabricVersionString);
            packVersion.loader.metadata = loaderMeta;

            // should probably look for the patch, but this is sound logic
            if (MinecraftManager.getMinecraftVersion(minecraftVersion).is1132OrOlder()) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
            } else {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
            }
        } else if (quiltLoaderComponent != null) {
            String quiltVersionString = quiltLoaderComponent.version;

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", minecraftVersion);
            loaderMeta.put("loader", quiltVersionString);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader";
        }

        hideSubProgressBar();
    }

    private void generatePackVersionForVanilla() throws Exception {
        addPercent(5);
        // #. {0} is the platform the modpack is from
        fireTask(GetText.tr("Generating Pack Version From {0}", "Vanilla Minecraft"));
        fireSubProgressUnknown();

        this.packVersion = new Version();
        packVersion.version = version.minecraftVersion.id;
        packVersion.minecraft = version.minecraftVersion.id;
        packVersion.enableCurseForgeIntegration = true;
        packVersion.enableEditingMods = true;

        if (loaderVersion != null && loaderVersion.isForge()) {
            packVersion.loader = new com.atlauncher.data.json.Loader();

            GetForgeLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetForgeLoaderVersionQuery(loaderVersion.version));

            if (response == null || response.forgeVersion() == null) {
                throw new Exception("Failed to find loader version for " + loaderVersion.version);
            }

            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);

            loaderMeta.put("version", response.forgeVersion().version());
            loaderMeta.put("rawVersion", response.forgeVersion().rawVersion());
            if (response.forgeVersion().installerSize() != null
                && response.forgeVersion().installerSha1Hash() != null) {
                loaderMeta.put("installerSize", response.forgeVersion().installerSize().longValue());
                loaderMeta.put("installerSha1", response.forgeVersion().installerSha1Hash());
            }
            if (response.forgeVersion().universalSize() != null
                && response.forgeVersion().universalSha1Hash() != null) {
                loaderMeta.put("universalSize", response.forgeVersion().universalSize().longValue());
                loaderMeta.put("universalSha1", response.forgeVersion().universalSha1Hash());
            }
            if (response.forgeVersion().clientSize() != null
                && response.forgeVersion().clientSha1Hash() != null) {
                loaderMeta.put("clientSize", response.forgeVersion().clientSize().longValue());
                loaderMeta.put("clientSha1", response.forgeVersion().clientSha1Hash());
            }
            if (response.forgeVersion().serverSize() != null
                && response.forgeVersion().serverSha1Hash() != null) {
                loaderMeta.put("serverSize", response.forgeVersion().serverSize().longValue());
                loaderMeta.put("serverSha1", response.forgeVersion().serverSha1Hash());
            }

            packVersion.loader.metadata = loaderMeta;

            if (Utils.matchVersion(version.minecraftVersion.id, "1.13", false, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
            } else if (Utils.matchVersion(packVersion.minecraft, "1.5", true, true)) {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.LegacyForgeLoader";
            } else {
                packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
            }
        } else if (loaderVersion != null && loaderVersion.isFabric()) {
            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("loader", loaderVersion.version);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.fabric.FabricLoader";
        } else if (loaderVersion != null && loaderVersion.isLegacyFabric()) {
            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("loader", loaderVersion.version);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader";
        } else if (loaderVersion != null && loaderVersion.isNeoForge()) {
            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("loader", loaderVersion.version);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.neoforge.NeoForgeLoader";
        } else if (loaderVersion != null && loaderVersion.isPaper()) {
            String paperBuildString = loaderVersion.version;

            int paperBuild;

            try {
                paperBuild = Integer.parseInt(paperBuildString);
            } catch (NumberFormatException ignored) {
                throw new Exception("Failed to find loader version for " + paperBuildString);
            }

            GetPaperLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetPaperLoaderVersionQuery(paperBuild, version.minecraftVersion.id));

            if (response == null || response.paperVersion() == null) {
                throw new Exception("Failed to find loader version for " + paperBuildString);
            }

            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("build", response.paperVersion().build());
            loaderMeta.put("filename", response.paperVersion().filename());
            loaderMeta.put("sha256", response.paperVersion().sha256());
            loaderMeta.put("downloadUrl", response.paperVersion().downloadUrl());

            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.paper.PaperLoader";
        } else if (loaderVersion != null && loaderVersion.isPurpur()) {
            String purpurBuildString = loaderVersion.version;

            int purpurBuild;

            try {
                purpurBuild = Integer.parseInt(purpurBuildString);
            } catch (NumberFormatException ignored) {
                throw new Exception("Failed to find loader version for " + purpurBuildString);
            }

            GetPurpurLoaderVersionQuery.Data response = GraphqlClient
                .callAndWait(new GetPurpurLoaderVersionQuery(purpurBuild, version.minecraftVersion.id));

            if (response == null || response.purpurVersion() == null) {
                throw new Exception("Failed to find loader version for " + purpurBuildString);
            }

            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("build", response.purpurVersion().build());
            loaderMeta.put("filename", response.purpurVersion().filename());
            loaderMeta.put("md5", response.purpurVersion().md5());
            loaderMeta.put("downloadUrl", response.purpurVersion().downloadUrl());

            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.purpur.PurpurLoader";
        } else if (loaderVersion != null && loaderVersion.isQuilt()) {
            packVersion.loader = new com.atlauncher.data.json.Loader();
            Map<String, Object> loaderMeta = new HashMap<>();
            loaderMeta.put("minecraft", version.minecraftVersion.id);
            loaderMeta.put("loader", loaderVersion.version);
            packVersion.loader.metadata = loaderMeta;
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader";
        }

        hideSubProgressBar();
    }

    private void downloadMinecraftVersionJson() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Downloading Minecraft Definition"));
        fireSubProgressUnknown();

        minecraftVersionManifest = MinecraftManager.getMinecraftVersion(this.packVersion.getMinecraft());

        this.minecraftVersion = com.atlauncher.network.Download.build()
            .setUrl(minecraftVersionManifest.url).hash(minecraftVersionManifest.sha1)
            .size(minecraftVersionManifest.size)
            .downloadTo(FileSystem.MINECRAFT_VERSIONS_JSON.resolve(minecraftVersionManifest.id + ".json"))
            .asClass(MinecraftVersion.class);

        if (this.minecraftVersion == null) {
            LogManager.error("Failed to download Minecraft json.");
            this.cancel(true);
            return;
        }

        hideSubProgressBar();
    }

    private void downloadLoader() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Downloading Loader"));
        fireSubProgressUnknown();

        this.loader.downloadAndExtractInstaller();

        hideSubProgressBar();
    }

    private void showMessages() throws Exception {
        int ret = 0;

        if (this.isReinstall && this.packVersion.messages.update != null) {
            ret = this.packVersion.messages.showUpdateMessage(this.pack);
        } else if (!this.isReinstall && this.packVersion.messages.install != null) {
            ret = this.packVersion.messages.showInstallMessage(this.pack);
        }

        if (ret != 0) {
            throw new LocalException("Install cancelled after viewing message!");
        }
    }

    private void determineModsToBeInstalled() {
        this.allMods = sortMods((this.isServer ? this.packVersion.getServerInstallMods(this)
            : this.packVersion.getClientInstallMods(this)));

        boolean hasOptional = this.allMods.stream().anyMatch(Mod::isOptional);

        if (!this.allMods.isEmpty() && hasOptional) {
            com.atlauncher.gui.dialogs.ModsChooser modsChooser = new com.atlauncher.gui.dialogs.ModsChooser(this);

            if (this.showModsChooser) {
                modsChooser.setVisible(true);
            }

            if (modsChooser.wasClosed()) {
                this.cancel(true);
                return;
            }

            this.selectedMods = modsChooser.getSelectedMods();
            this.unselectedMods = modsChooser.getUnselectedMods();
        }

        if (!hasOptional) {
            this.selectedMods = this.allMods;
        }

        for (com.atlauncher.data.json.Mod mod : this.selectedMods) {
            String file = mod.getFile();

            if (mod.type == ModType.mods
                && this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase(Locale.ENGLISH)
                    + file.substring(file.lastIndexOf("."));
            } else if (mod.type == ModType.mods
                && this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase(Locale.ENGLISH)
                    + file.substring(file.lastIndexOf("."));
            }

            this.modsInstalled.add(new com.atlauncher.data.DisableableMod(mod.getName(), mod.getVersion(),
                mod.isOptional(), file, mod.path,
                com.atlauncher.data.Type.valueOf(com.atlauncher.data.Type.class, mod.getType().toString()),
                this.packVersion.getColour(mod.getColour()), mod.getDescription(), false, false, true, false,
                mod.getCurseForgeProjectId(), mod.getCurseForgeFileId(), mod.curseForgeProject,
                mod.curseForgeFile, mod.modrinthProject, mod.modrinthVersion));
        }

        if (this.isReinstall && instance.hasCustomMods()) {
            // user chose to save mods even though Minecraft version changed, so add them to
            // the installed list
            if (this.saveMods || instance.id.equalsIgnoreCase(version.minecraftVersion.id)) {
                modsInstalled.addAll(instance.getCustomDisableableMods());
            }

            // user choosing to not save mods and Minecraft version changed, so delete
            // custom mods
            if (!this.saveMods && !instance.id.equalsIgnoreCase(version.minecraftVersion.id)) {
                for (com.atlauncher.data.DisableableMod mod : instance.getCustomDisableableMods()) {
                    this.instance.launcher.mods.remove(mod);
                    Utils.delete((mod.isDisabled() ? mod.getDisabledFile(this.instance) : mod.getFile(this.instance)));
                }
            }
        }

        if (this.multiMCManifest != null) {
            String minecraftFolder = Files.exists(multiMCExtractedPath.resolve(".minecraft")) ? ".minecraft"
                : "minecraft";

            if (Files.exists(multiMCExtractedPath.resolve(minecraftFolder + "/mods"))) {
                try (Stream<Path> list = Files.list(multiMCExtractedPath.resolve(minecraftFolder + "/mods"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.mods)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(multiMCExtractedPath.resolve(minecraftFolder + "/mods/" + packVersion.minecraft))) {
                try (Stream<Path> list = Files
                    .list(multiMCExtractedPath.resolve(minecraftFolder + "/mods/" + packVersion.minecraft))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.dependency)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(multiMCExtractedPath.resolve(minecraftFolder + "/mods/ic2"))) {
                try (Stream<Path> list = Files.list(multiMCExtractedPath.resolve(minecraftFolder + "/mods/ic2"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.ic2lib)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }

        if (this.curseForgeManifest != null) {
            if (Files.exists(curseForgeExtractedPath
                .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides") + "/mods"))) {
                try (Stream<Path> list = Files.list(curseForgeExtractedPath
                    .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides") + "/mods"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.mods)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(
                curseForgeExtractedPath
                    .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides")
                        + "/mods/" + packVersion.minecraft))) {
                try (Stream<Path> list = Files.list(
                    curseForgeExtractedPath
                        .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides")
                            + "/mods/" + packVersion.minecraft))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.dependency)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }

        if (this.modrinthManifest != null) {
            if (Files.exists(modrinthExtractedPath.resolve("overrides/mods"))) {
                try (Stream<Path> list = Files.list(modrinthExtractedPath.resolve("overrides/mods"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.mods)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(modrinthExtractedPath.resolve("overrides/mods/" + packVersion.minecraft))) {
                try (Stream<Path> list = Files
                    .list(modrinthExtractedPath.resolve("overrides/mods/" + packVersion.minecraft))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.dependency)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }

        if (this.technicModpack != null && this.technicModpack.solder == null) {
            if (Files.exists(technicModpackExtractedPath.resolve("mods"))) {
                try (Stream<Path> list = Files.list(technicModpackExtractedPath.resolve("mods"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.mods)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(technicModpackExtractedPath.resolve("mods/" + packVersion.minecraft))) {
                try (Stream<Path> list = Files
                    .list(technicModpackExtractedPath.resolve("mods/" + packVersion.minecraft))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.dependency)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }

            if (Files.exists(technicModpackExtractedPath.resolve("mods/ic2"))) {
                try (Stream<Path> list = Files.list(technicModpackExtractedPath.resolve("mods/ic2"))) {
                    this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p))
                        .filter(p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                        .map(p -> convertPathToDisableableMod(p, Type.ic2lib)).collect(Collectors.toList()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
        }
    }

    private DisableableMod convertPathToDisableableMod(Path p, Type t) {
        DisableableMod mod = new DisableableMod();

        mod.optional = true;
        mod.name = p.getFileName().toString();
        mod.version = "Unknown";
        mod.description = null;

        MCMod mcMod = Utils.getMCModForFile(p.toFile());
        if (mcMod != null) {
            mod.name = Optional.ofNullable(mcMod.name).orElse(p.getFileName().toString());
            mod.version = Optional.ofNullable(mcMod.version).orElse("Unknown");
            mod.description = mcMod.description;
        } else {
            FabricMod fabricMod = Utils.getFabricModForFile(p.toFile());
            if (fabricMod != null) {
                mod.name = Optional.ofNullable(fabricMod.name).orElse(p.getFileName().toString());
                mod.version = Optional.ofNullable(fabricMod.version).orElse("Unknown");
                mod.description = fabricMod.description;
            }
        }

        mod.file = p.getFileName().toString();
        mod.type = t;

        return mod;
    }

    private Boolean install() throws Exception {
        this.instanceIsCorrupt = true; // From this point on the instance has become corrupt

        determineMainClass();
        determineArguments();

        downloadResources();
        if (isCancelled()) {
            return false;
        }

        downloadMinecraft();
        if (isCancelled()) {
            return false;
        }

        downloadLoggingClient();
        if (isCancelled()) {
            return false;
        }

        downloadLibraries();
        if (isCancelled()) {
            return false;
        }

        organiseLibraries();
        if (isCancelled()) {
            return false;
        }

        downloadRuntime();
        if (isCancelled()) {
            return false;
        }

        installLoader();
        if (isCancelled()) {
            return false;
        }

        downloadMods();
        if (isCancelled()) {
            return false;
        }

        installMods();
        if (isCancelled()) {
            return false;
        }

        installLegacyJavaFixer();
        if (isCancelled()) {
            return false;
        }

        runCaseConversion();
        if (isCancelled()) {
            return false;
        }

        runActions();
        if (isCancelled()) {
            return false;
        }

        installConfigs();
        if (isCancelled()) {
            return false;
        }

        downloadImage();
        if (isCancelled()) {
            return false;
        }

        checkModsOnCurseForge();
        if (isCancelled()) {
            return false;
        }

        checkModsOnModrinth();
        if (isCancelled()) {
            return false;
        }

        cleanDirectories();
        if (isCancelled()) {
            return false;
        }

        // Copy over common configs if any
        if (FileSystem.COMMON.toFile().listFiles().length != 0) {
            Utils.copyDirectory(FileSystem.COMMON.toFile(), this.root.toFile());
        }

        restoreSelectFiles();

        writeLog4j2XmlFileIfNeeded();

        writeLog4ShellExploitArgumentsForForgeScripts();

        installServerBootScripts();

        return true;
    }

    private void cleanDirectories() {
        if (technicModpack != null) {
            Path binPath = this.root.resolve("bin");

            if (binPath.toFile().exists() && binPath.toFile().listFiles().length == 0) {
                FileUtils.deleteDirectory(binPath);
            }
        }
    }

    private void saveInstanceJson() {
        Instance instance = new Instance(this.minecraftVersion);
        instance.ROOT = this.root;
        InstanceLauncher instanceLauncher;

        if (!this.isReinstall) {
            instanceLauncher = new InstanceLauncher();
            instance.uuid = UUID.randomUUID();
        } else {
            instanceLauncher = this.instance.launcher;
            instance.uuid = this.instance.uuid;
        }

        instance.libraries = this.getLibraries();
        instance.mainClass = this.mainClass;
        instance.arguments = this.arguments;

        instanceLauncher.loaderVersion = this.loaderVersion;

        if (!changingLoader) {
            instanceLauncher.name = this.name;
            instanceLauncher.pack = this.pack.name;
            instanceLauncher.description = this.pack.description;
            instanceLauncher.packId = this.pack.id;
            instanceLauncher.externalPackId = this.pack.externalId;
            instanceLauncher.version = this.packVersion.version;
            instanceLauncher.java = this.packVersion.java;
            instanceLauncher.enableCurseForgeIntegration = this.packVersion.enableCurseForgeIntegration;
            instanceLauncher.enableEditingMods = this.packVersion.enableEditingMods;
            instanceLauncher.isDev = this.version.isDev;
            instanceLauncher.isPlayable = true;
            instanceLauncher.mods = this.modsInstalled;
            instanceLauncher.requiredMemory = getRequiredMemory(this.packVersion.memory);
            instanceLauncher.requiredPermGen = this.packVersion.permGen;
            instanceLauncher.assetsMapToResources = this.assetsMapToResources;
            instanceLauncher.curseForgeProject = this.pack.curseForgeProject;
            instanceLauncher.curseForgeProjectDescription = this.pack.curseForgeProjectDescription;
            instanceLauncher.curseForgeFile = this.version._curseForgeFile;
            instanceLauncher.multiMCManifest = multiMCManifest;
            instanceLauncher.modrinthProject = this.pack.modrinthProject;
            instanceLauncher.modrinthVersion = this.version._modrinthVersion;
            instanceLauncher.modrinthManifest = modrinthManifest;
            instanceLauncher.ftbPackManifest = ftbPackManifest;
            instanceLauncher.ftbPackVersionManifest = ftbPackVersionManifest;
            instanceLauncher.technicModpack = technicModpack;
            instanceLauncher.vanillaInstance = this.pack.vanillaInstance;
            instanceLauncher.overridePaths = overridePaths;

            if (multiMCManifest != null) {
                if (multiMCManifest.config.preLaunchCommand != null
                    && !multiMCManifest.config.preLaunchCommand.equals("null")
                    && !multiMCManifest.config.preLaunchCommand.isEmpty()) {
                    instanceLauncher.preLaunchCommand = multiMCManifest.config.preLaunchCommand;
                    instanceLauncher.enableCommands = true;
                }

                if (multiMCManifest.config.postExitCommand != null
                    && !multiMCManifest.config.postExitCommand.equals("null")
                    && !multiMCManifest.config.postExitCommand.isEmpty()) {
                    instanceLauncher.postExitCommand = multiMCManifest.config.postExitCommand;
                    instanceLauncher.enableCommands = true;
                }
                if (multiMCManifest.config.wrapperCommand != null
                    && !multiMCManifest.config.wrapperCommand.equals("null")
                    && !multiMCManifest.config.wrapperCommand.isEmpty()) {
                    instanceLauncher.wrapperCommand = multiMCManifest.config.wrapperCommand;
                    instanceLauncher.enableCommands = true;
                }
                if (multiMCManifest.config.javaPath != null
                    && !multiMCManifest.config.javaPath.equals("null")
                    && !multiMCManifest.config.javaPath.isEmpty()) {
                    instanceLauncher.javaPath = multiMCManifest.config.javaPath;
                }
                if (multiMCManifest.config.javaArguments != null
                    && !multiMCManifest.config.javaArguments.equals("null")
                    && !multiMCManifest.config.javaArguments.isEmpty()) {
                    instanceLauncher.javaArguments = multiMCManifest.config.javaArguments;
                }
                if (multiMCManifest.config.permGen != null) {
                    instanceLauncher.permGen = multiMCManifest.config.permGen;
                }
                if (multiMCManifest.config.maximumMemory != null) {
                    instanceLauncher.maximumMemory = multiMCManifest.config.maximumMemory;
                }
            }

            if (instanceLauncher.curseForgeManifest != null) {
                instanceLauncher.curseForgeManifest = null;
            }

            if (this.version.isDev) {
                instanceLauncher.hash = this.version.hash;
            }
        }

        instance.launcher = instanceLauncher;

        instance.save();

        InstanceManager.updateInstance(instance);

        // after adding, check for updates if an external pack
        if (instance.isExternalPack()) {
            App.launcher.checkForExternalPackUpdates();
        }
    }

    private Integer getRequiredMemory(Integer defaultMemory) {
        try {
            Path memorySettingsJson = this.root.resolve("config/memorysettings.json");
            if (Files.exists(memorySettingsJson)) {
                JsonObject memorySettingsJsonObject =
                    Gsons.DEFAULT.fromJson(new String(Files.readAllBytes(memorySettingsJson)), JsonObject.class);
                if (memorySettingsJsonObject.has("minimumClient")) {
                    JsonObject minimumClientObject = memorySettingsJsonObject.getAsJsonObject("minimumClient");
                    if (minimumClientObject.has("minimumClient")) {
                        return minimumClientObject.get("minimumClient").getAsInt();
                    }
                }
            }
        } catch (Exception e) {
            // ignored
        }

        return defaultMemory;
    }

    private void initServerSettings() {
        new Thread(() -> {
            try {
                Path javaPath = Paths.get(OS.getJavaHome());
                if (minecraftVersion.javaVersion != null && App.settings.useJavaProvidedByMinecraft) {
                    Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES
                        .resolve(minecraftVersion.javaVersion.component)
                        .resolve(JavaRuntimes.getSystem()).resolve(minecraftVersion.javaVersion.component);

                    if (OS.isMac()) {
                        runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
                    }

                    if (Files.isDirectory(runtimeDirectory)) {
                        javaPath = runtimeDirectory.toAbsolutePath();
                    }
                }

                String output = Utils.runProcess(root, Java.getPathToJavaExecutable(javaPath),
                    "-jar", getMinecraftJar().getAbsolutePath(), "--initSettings");
                LogManager.debug("initServerSettings output");
                LogManager.debug(output);
            } catch (Throwable ignored) {
                // ignored
            }
        }).start();
    }

    private void saveServerJson() {
        Server server = new Server();

        server.ROOT = this.root;
        server.name = this.name;
        server.pack = this.pack.name;
        server.packId = this.pack.id;
        server.externalPackId = this.pack.externalId;
        server.vanillaInstance = this.pack.vanillaInstance;
        server.minecraftVersion = this.version.minecraftVersion.id;
        server.version = this.packVersion.version;
        server.isDev = this.version.isDev;
        server.isPatchedForLog4Shell = true;

        server.curseForgeProject = this.pack.curseForgeProject;
        server.curseForgeFile = this.version._curseForgeFile;
        server.modrinthProject = this.pack.modrinthProject;
        server.modrinthVersion = this.version._modrinthVersion;
        server.modrinthManifest = modrinthManifest;

        if (pack.curseForgeProject == null) {
            server.mods = this.modsInstalled;
            server.javaVersion = this.minecraftVersion.javaVersion;
        }

        if (this.version.isDev) {
            server.hash = this.version.hash;
        }

        server.loaderVersion = this.loaderVersion;

        server.save();

        ServerManager.addServer(server);
    }

    private void determineMainClass() {
        if (this.packVersion.mainClass != null) {
            if (this.packVersion.mainClass.depends == null && this.packVersion.mainClass.dependsGroup == null) {
                this.mainClass = this.packVersion.mainClass.mainClass;
            } else if (this.packVersion.mainClass.depends != null) {
                String depends = this.packVersion.mainClass.depends;

                if (this.selectedMods.stream().anyMatch(mod -> mod.name.equalsIgnoreCase(depends))) {
                    this.mainClass = this.packVersion.mainClass.mainClass;
                }
            } else if (this.packVersion.getMainClass().hasDependsGroup()) {
                String dependsGroup = this.packVersion.mainClass.dependsGroup;

                if (this.selectedMods.stream().anyMatch(mod -> mod.group.equalsIgnoreCase(dependsGroup))) {
                    this.mainClass = this.packVersion.mainClass.mainClass;
                }
            }
        }

        // use the loader provided main class if there is a loader
        if (this.loader != null) {
            this.mainClass = this.loader.getMainClass();
        }

        // if none set by pack, then use the minecraft one
        if (this.mainClass == null) {
            this.mainClass = this.minecraftVersion.mainClass;
        }
    }

    private void determineArguments() {
        this.arguments = new Arguments();

        if (this.loader != null) {
            if (this.loader.useMinecraftArguments()) {
                addMinecraftArguments();
            }

            Arguments loaderArguments = this.loader.getArguments();

            if (loaderArguments != null) {
                if (loaderArguments.game != null && !loaderArguments.game.isEmpty()) {
                    this.arguments.game.addAll(loaderArguments.game);
                }

                if (loaderArguments.jvm != null && !loaderArguments.jvm.isEmpty()) {
                    this.arguments.jvm.addAll(loaderArguments.jvm);
                }
            }
        } else {
            addMinecraftArguments();
        }

        if (this.packVersion.extraArguments != null) {
            boolean add = false;

            if (this.packVersion.extraArguments.depends == null
                && this.packVersion.extraArguments.dependsGroup == null) {
                add = true;
            } else if (this.packVersion.extraArguments.depends == null) {
                String depends = this.packVersion.extraArguments.depends;

                if (this.selectedMods.stream().anyMatch(mod -> mod.name.equalsIgnoreCase(depends))) {
                    add = true;
                }
            } else if (this.packVersion.extraArguments.dependsGroup == null) {
                String dependsGroup = this.packVersion.extraArguments.dependsGroup;

                if (this.selectedMods.stream().anyMatch(mod -> mod.group.equalsIgnoreCase(dependsGroup))) {
                    add = true;
                }
            }

            if (add) {
                this.arguments.game.addAll(Arrays.stream(this.packVersion.extraArguments.arguments.split(" "))
                    .map(ArgumentRule::new).collect(Collectors.toList()));
            }
        }

        // check for any agent patches if from MultiMC import and add them to the jvm args
        if (this.multiMCManifest != null) {
            this.multiMCManifest.getNonStandardComponents().forEach(c -> {
                Path pathToPatch = this.multiMCExtractedPath.resolve("patches/" + c.uid + ".json");

                if (!Files.exists(pathToPatch)) {
                    LogManager.error("Could not find patch for " + c.uid);
                    return;
                }

                try {
                    MultiMCPatch patch = Gsons.DEFAULT.fromJson(new String(Files.readAllBytes(pathToPatch),
                        StandardCharsets.UTF_8), MultiMCPatch.class);

                    patch.agents.forEach(agent -> {
                        this.arguments.jvm.add(new ArgumentRule(null,
                            "-javaagent:" + FileSystem.LIBRARIES.resolve(
                                Utils.convertMavenIdentifierToPath(agent.name))));
                    });
                } catch (IOException e) {
                    LogManager.error("Failed to read patch for " + c.uid + " from " + pathToPatch);
                }
            });
        }
    }

    private void addMinecraftArguments() {
        // older MC versions
        if (this.minecraftVersion.minecraftArguments != null) {
            this.arguments.game.addAll(Arrays.stream(this.minecraftVersion.minecraftArguments.split(" "))
                .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList()));
        }

        // newer MC versions
        if (this.minecraftVersion.arguments != null) {
            if (this.minecraftVersion.arguments.game != null && !this.minecraftVersion.arguments.game.isEmpty()) {
                this.arguments.game.addAll(this.minecraftVersion.arguments.game);
            }

            if (this.minecraftVersion.arguments.jvm != null && !this.minecraftVersion.arguments.jvm.isEmpty()) {
                this.arguments.jvm.addAll(this.minecraftVersion.arguments.jvm);
            }
        }
    }

    protected void downloadResources() throws Exception {
        addPercent(5);

        if (this.isServer || this.minecraftVersion.assetIndex == null) {
            return;
        }

        fireTask(GetText.tr("Organising Resources"));
        fireSubProgressUnknown();
        this.totalBytes = this.downloadedBytes = 0;

        MojangAssetIndex assetIndex = this.minecraftVersion.assetIndex;

        AssetIndex index = com.atlauncher.network.Download.build().setUrl(assetIndex.url).hash(assetIndex.sha1)
            .size(assetIndex.size).downloadTo(FileSystem.RESOURCES_INDEXES.resolve(assetIndex.id + ".json"))
            .asClass(AssetIndex.class);

        if (index.mapToResources) {
            this.assetsMapToResources = true;
        }

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        index.objects.forEach((key, object) -> {
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);

            com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(url)
                .downloadTo(FileSystem.RESOURCES_OBJECTS.resolve(filename)).hash(object.hash).size(object.size)
                .withInstanceInstaller(this).withHttpClient(httpClient).withFriendlyFileName(key);

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        if (!smallPool.isEmpty()) {
            fireTask(GetText.tr("Downloading Resources"));
            this.setTotalBytes(smallPool.totalSize());
            this.fireSubProgress(0);
            smallPool.downloadAll();
        }

        // copy resources to instance
        if (index.mapToResources || assetIndex.id.equalsIgnoreCase("legacy")) {
            fireTask(GetText.tr("Organising Resources"));
            fireSubProgressUnknown();

            index.objects.forEach((key, object) -> {
                String filename = object.hash.substring(0, 2) + "/" + object.hash;

                Path downloadedFile = FileSystem.RESOURCES_OBJECTS.resolve(filename);
                Path assetPath = index.mapToResources ? this.root.resolve("resources/" + key)
                    : FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(key);

                if (!Files.exists(assetPath)) {
                    FileUtils.copyFile(downloadedFile, assetPath, true);
                }
            });
        }

        hideSubProgressBar();
    }

    private void downloadMinecraft() throws Exception {
        addPercent(5);

        // if Paper or Purpur, we don't need to download Minecraft
        if (this.isServer && this.loaderVersion != null
            && (this.loaderVersion.isPaper() || this.loaderVersion.isPurpur())) {
            return;
        }

        fireTask(GetText.tr("Downloading Minecraft"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        MojangDownloads downloads = this.minecraftVersion.downloads;

        MojangDownload mojangDownload = this.isServer ? downloads.server : downloads.client;

        setTotalBytes(mojangDownload.size);

        com.atlauncher.network.Download.build().setUrl(mojangDownload.url).hash(mojangDownload.sha1)
            .size(mojangDownload.size).downloadTo(getMinecraftJarLibrary().toPath())
            .copyTo(this.isServer ? getMinecraftJar().toPath() : null).withInstanceInstaller(this)
            .withHttpClient(Network.createProgressClient(this)).downloadFile();

        hideSubProgressBar();
    }

    public File getMinecraftJar() {
        if (isServer) {
            return new File(this.root.toFile(), String.format("minecraft_server.%s.jar", this.minecraftVersion.id));
        }

        return new File(this.root.toFile(), String.format("%s.jar", this.minecraftVersion.id));
    }

    public File getMinecraftJarLibrary() {
        return getMinecraftJarLibrary(isServer ? "server" : "client");
    }

    public File getMinecraftJarLibrary(String type) {
        return FileSystem.LIBRARIES.resolve(getMinecraftJarLibraryPath(type)).toFile();
    }

    private void downloadLoggingClient() throws Exception {
        addPercent(5);

        if (this.isServer || this.minecraftVersion.logging == null) {
            return;
        }

        fireTask(GetText.tr("Downloading Logging Client"));
        fireSubProgressUnknown();

        LoggingFile loggingFile = this.minecraftVersion.logging.client.file;
        setTotalBytes(loggingFile.size);

        com.atlauncher.network.Download.build().setUrl(loggingFile.url).hash(loggingFile.sha1)
            .size(loggingFile.size).downloadTo(FileSystem.RESOURCES_LOG_CONFIGS.resolve(loggingFile.id))
            .withInstanceInstaller(this).withHttpClient(Network.createProgressClient(this)).downloadFile();

        hideSubProgressBar();
    }

    private List<Library> getLibraries() {
        List<Library> libraries = new ArrayList<>();

        List<Library> packVersionLibraries = getPackVersionLibraries();

        if (packVersionLibraries != null && !packVersionLibraries.isEmpty()) {
            libraries.addAll(packVersionLibraries);
        }

        // Now read in the library jars needed from the loader
        if (this.loader != null) {
            libraries.addAll(this.loader.getLibraries());
        }

        // lastly the Minecraft libraries if not on server
        if (!isServer && (this.loader == null || this.loader.useMinecraftLibraries())) {
            libraries.addAll(this.minecraftVersion.libraries);
        }

        // fix Log4J exploits if not on server (servers seem sensitive to this process)
        if (!isServer) {
            libraries = libraries.stream().map(library -> {
                library.fixLog4jVersion();

                return library;
            }).collect(Collectors.toList());
        }

        return libraries;
    }

    public String getMinecraftJarLibraryPath() {
        return getMinecraftJarLibraryPath(isServer ? "server" : "client");
    }

    public String getMinecraftJarLibraryPath(String type) {
        return ("net/minecraft/" + type + "/" + this.minecraftVersion.id + "/" + type + "-" + this.minecraftVersion.id
            + ".jar").replace("/", File.separatorChar + "");
    }

    public List<String> getLibrariesForLaunch() {
        List<String> libraries = new ArrayList<>();

        libraries.add(this.getMinecraftJarLibraryPath());

        libraries.addAll(this.getLibraries().stream()
            .filter(library -> library.downloads.artifact != null && library.downloads.artifact.path != null)
            .map(library -> library.downloads.artifact.path).collect(Collectors.toList()));

        return libraries;
    }

    public String getMinecraftArguments() {
        return this.arguments.asString();
    }

    public boolean doAssetsMapToResources() {
        return this.assetsMapToResources;
    }

    private List<Library> getPackVersionLibraries() {
        List<Library> libraries = new ArrayList<>();

        // Now read in the library jars needed from the pack
        for (com.atlauncher.data.json.Library library : this.packVersion.getLibraries()) {
            if (this.isServer && !library.forServer()) {
                continue;
            }

            if (library.depends != null) {
                if (this.selectedMods.stream().noneMatch(mod -> mod.name.equalsIgnoreCase(library.depends))) {
                    continue;
                }
            } else if (library.hasDependsGroup()) {
                if (this.selectedMods.stream().noneMatch(mod -> mod.group.equalsIgnoreCase(library.dependsGroup))) {
                    continue;
                }
            }

            Library minecraftLibrary = new Library();

            minecraftLibrary.name = library.file;

            Download download = new Download();
            download.path = library.path != null ? library.path
                : (library.server != null ? library.server : library.file);
            download.sha1 = library.md5;
            download.size = library.filesize;
            download.url = String.format("%s/%s", Constants.DOWNLOAD_SERVER, library.url);

            Downloads downloads = new Downloads();
            downloads.artifact = download;

            minecraftLibrary.downloads = downloads;

            libraries.add(minecraftLibrary);
        }

        return libraries;
    }

    private void downloadLibraries() {
        addPercent(5);
        fireTask(GetText.tr("Downloading Libraries"));
        fireSubProgressUnknown();

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        // get non native libraries otherwise we double up
        this.getLibraries().stream().filter(
                library -> library.shouldInstall() && library.downloads.artifact != null && !library.hasNativeForOS())
            .forEach(library -> {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                    .setUrl(library.downloads.artifact.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                    .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                    .withInstanceInstaller(this).withHttpClient(httpClient);

                pool.add(download);
            });

        if (this.loader != null && this.loader.getInstallLibraries() != null) {
            this.loader.getInstallLibraries().stream()
                .filter(library -> library.downloads.artifact != null
                    && library.downloads.artifact.url != null && !library.downloads.artifact.url.isEmpty())
                .forEach(
                    library -> pool
                        .add(new com.atlauncher.network.Download().setUrl(library.downloads.artifact.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                            .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                            .withInstanceInstaller(this).withHttpClient(httpClient)));
        }

        if (!this.isServer) {
            this.getLibraries().stream().filter(Library::hasNativeForOS).forEach(library -> {
                Download download = library.getNativeDownloadForOS();

                pool.add(new com.atlauncher.network.Download().setUrl(download.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(download.path)).hash(download.sha1).size(download.size)
                    .withInstanceInstaller(this).withHttpClient(httpClient));
            });
        }

        if (this.multiMCManifest != null) {
            // we only download these, as they do not get loaded into the classpath as they support agents only
            this.getMultiMCLibraries().forEach(library -> {
                com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                    .setUrl(library.downloads.artifact.url)
                    .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                    .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                    .withInstanceInstaller(this).withHttpClient(httpClient);

                pool.add(download);
            });
        }

        DownloadPool smallPool = pool.downsize();

        this.setTotalBytes(smallPool.totalSize());
        this.fireSubProgress(0);

        smallPool.downloadAll();

        hideSubProgressBar();
    }

    private void organiseLibraries() {
        addPercent(5);
        fireTask(GetText.tr("Organising Libraries"));
        fireSubProgressUnknown();

        if (isServer) {
            this.getLibraries().stream().filter(Library::shouldInstall)
                .filter(library -> library.downloads.artifact != null).forEach(library -> {
                    File libraryFile = FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile();

                    File serverFile = new File(this.root.resolve("libraries").toFile(),
                        library.downloads.artifact.path);

                    serverFile.getParentFile().mkdirs();

                    Utils.copyFile(libraryFile, serverFile, true);
                });

            if (this.loader != null && this.loader.getInstallLibraries() != null) {
                this.loader.getInstallLibraries().stream().filter(library -> library.downloads.artifact != null)
                    .forEach(library -> {
                        if (isServer) {
                            File libraryFile = FileSystem.LIBRARIES.resolve(library.downloads.artifact.path)
                                .toFile();

                            File serverFile = new File(this.root.resolve("libraries").toFile(),
                                library.downloads.artifact.path);

                            serverFile.getParentFile().mkdirs();

                            Utils.copyFile(libraryFile, serverFile, true);
                        }
                    });
            }

            if (this.loader != null && this.loaderVersion != null && this.loaderVersion.isForge()) {
                if (this.loader.getServerJarPath() != null && Files.exists(this.loader.getServerJarPath())) {
                    FileUtils.copyFile(this.loader.getServerJarPath(), this.root, false);
                }
            }
        }

        hideSubProgressBar();
    }

    private void downloadRuntime() {
        addPercent(5);

        if (minecraftVersion.javaVersion == null || Data.JAVA_RUNTIMES == null
            || !App.settings.useJavaProvidedByMinecraft) {
            return;
        }

        Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
        String runtimeSystemString = JavaRuntimes.getSystem();

        if (runtimesForSystem.containsKey(minecraftVersion.javaVersion.component)
            && !runtimesForSystem.get(minecraftVersion.javaVersion.component).isEmpty()) {
            fireTask(GetText.tr("Downloading Java Runtime {0}", minecraftVersion.javaVersion.majorVersion));
            fireSubProgressUnknown();

            JavaRuntime runtimeToDownload = runtimesForSystem.get(minecraftVersion.javaVersion.component).get(0);

            try {
                JavaRuntimeManifest javaRuntimeManifest = com.atlauncher.network.Download.build()
                    .setUrl(runtimeToDownload.manifest.url).size(runtimeToDownload.manifest.size)
                    .hash(runtimeToDownload.manifest.sha1).downloadTo(FileSystem.MINECRAFT_RUNTIMES
                        .resolve(minecraftVersion.javaVersion.component).resolve("manifest.json"))
                    .asClassWithThrow(JavaRuntimeManifest.class);

                OkHttpClient httpClient = Network.createProgressClient(this);
                DownloadPool pool = new DownloadPool();

                // create root directory
                Path runtimeSystemDirectory = FileSystem.MINECRAFT_RUNTIMES
                    .resolve(minecraftVersion.javaVersion.component).resolve(runtimeSystemString);
                Path runtimeDirectory = runtimeSystemDirectory.resolve(minecraftVersion.javaVersion.component);
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
                            .hash(file.downloads.raw.sha1).size(file.downloads.raw.size).executable(file.executable)
                            .withInstanceInstaller(this).withHttpClient(httpClient);

                        pool.add(download);
                    }
                });

                DownloadPool smallPool = pool.downsize();

                this.setTotalBytes(smallPool.totalSize());
                this.fireSubProgress(0);

                smallPool.downloadAll();

                // write out the version file (theres also a .sha1 file created, but we're not
                // doing that)
                Files.write(runtimeSystemDirectory.resolve(".version"),
                    runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                // Files.write(runtimeSystemDirectory.resolve(minecraftVersion.javaVersion.component
                // + ".sha1"), runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));

                hideSubProgressBar();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to download Java runtime", e);
            }
        }
    }

    private void installLoader() {
        addPercent(5);

        if (this.loader == null) {
            return;
        }

        fireTask(GetText.tr("Installing Loader (May Take Some Time)"));
        fireSubProgressUnknown();

        // run any processors that the loader needs
        this.loader.runProcessors();

        hideSubProgressBar();
    }

    private void downloadMods() throws Exception {
        addPercent(25);

        if (multiMCManifest != null || selectedMods.isEmpty()) {
            return;
        }

        fireTask(GetText.tr("Downloading Mods"));
        fireSubProgressUnknown();

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        this.selectedMods.stream().filter(mod -> mod.download != DownloadType.browser).forEach(mod -> {
            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                .setUrl(mod.getDownloadUrl()).downloadTo(FileSystem.DOWNLOADS.resolve(mod.getFile()))
                .size(mod.filesize).withInstanceInstaller(this).withHttpClient(httpClient);

            if (mod.ignoreFailures) {
                download = download.ignoreFailures();
            }

            if (mod.md5 != null) {
                download = download.hash(mod.md5);
            } else if (mod.sha1 != null) {
                download = download.hash(mod.sha1);
            } else if (mod.sha512 != null) {
                download = download.hash(mod.sha512);
            } else if (mod.fingerprint != null) {
                download = download.fingerprint(mod.fingerprint);
            }

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        this.setTotalBytes(smallPool.totalSize());
        this.fireSubProgress(0);

        smallPool.downloadAll();

        fireSubProgressUnknown();

        List<Mod> browserDownloadMods = this.selectedMods.stream().filter(mod -> mod.download == DownloadType.browser)
            .collect(Collectors.toList());
        if (!browserDownloadMods.isEmpty()) {
            if (curseForgeManifest != null || ftbPackManifest != null) {
                fireTask(GetText.tr("Downloading Browser Mods"));

                BrowserDownloadDialog browserDownloadDialog = new BrowserDownloadDialog(this.dialog,
                    browserDownloadMods);

                for (Mod mod : browserDownloadMods) {
                    if (browserDownloadDialog.modsDownloaded.stream()
                        .noneMatch(m -> m.curseForgeFileId.equals(mod.curseForgeFileId))) {
                        LogManager.info("Browser download mod " + mod.name + " was skipped");
                        Optional<DisableableMod> disableableMod = this.modsInstalled.stream()
                            .filter(m -> m.curseForgeFileId.equals(mod.curseForgeFileId)).findFirst();
                        disableableMod.ifPresent(value -> value.skipped = true);
                    }
                }

                if (!browserDownloadDialog.success) {
                    throw new Exception("Installation cancelled from browser downloads dialog.");
                }
            } else {
                int browserDownloadModsDownloaded = 0;

                for (Mod mod : browserDownloadMods) {
                    if (!isCancelled()) {
                        fireTask(GetText.tr("Downloading Browser Mods"));

                        if (!mod.download(this)) {
                            LogManager.info("Browser download mod " + mod.name + " was skipped");
                            Optional<DisableableMod> disableableMod = this.modsInstalled.stream()
                                .filter(m -> m.file.equals(mod.file)).findFirst();
                            disableableMod.ifPresent(value -> value.skipped = true);
                        }

                        browserDownloadModsDownloaded++;

                        fireSubProgress((browserDownloadModsDownloaded / ((double) browserDownloadMods.size())) * 100.0,
                            String.format(Locale.ENGLISH, "%d/%d", browserDownloadModsDownloaded,
                                browserDownloadMods.size()));
                    }
                }
            }
        }

        hideSubProgressBar();
    }

    private void installMods() {
        addPercent(25);

        if (multiMCManifest != null || this.selectedMods.isEmpty()) {
            return;
        }

        fireTask(GetText.tr("Installing Mods"));
        fireSubProgressUnknown();

        double subPercentPerMod = 100.0 / this.selectedMods.size();

        this.selectedMods.parallelStream().forEach(mod -> {
            mod.install(this);
            addSubPercent(subPercentPerMod);
        });

        hideSubProgressBar();
    }

    private void downloadTechnicSolderMods() {
        addPercent(25);

        if (technicSolderModsToDownload.isEmpty()) {
            return;
        }

        fireTask(GetText.tr("Downloading Mods"));
        fireSubProgressUnknown();

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        technicSolderModsToDownload.stream().forEach(mod -> {
            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                .setUrl(mod.getDownloadUrl())
                .downloadTo(FileSystem.TECHNIC_DOWNLOADS
                    .resolve(String.format("%s/%s", technicModpack.name, mod.getFile())))
                .unzipTo(this.root).size(mod.filesize).withInstanceInstaller(this).withHttpClient(httpClient);

            if (mod.md5 != null) {
                download = download.hash(mod.md5);
            }

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        this.setTotalBytes(smallPool.totalSize());
        this.fireSubProgress(0);

        smallPool.downloadAll();

        if (Files.exists(this.root.resolve("mods"))) {
            try (Stream<Path> list = Files.list(this.root.resolve("mods"))) {
                this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p)).filter(
                        p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                    .map(p -> convertPathToDisableableMod(p, Type.mods)).collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        if (Files.exists(this.root.resolve("mods/" + packVersion.minecraft))) {
            try (Stream<Path> list = Files.list(this.root.resolve("mods/" + packVersion.minecraft))) {
                this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p)).filter(
                        p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                    .map(p -> convertPathToDisableableMod(p, Type.dependency)).collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        if (Files.exists(this.root.resolve("mods/ic2"))) {
            try (Stream<Path> list = Files.list(this.root.resolve("mods/ic2"))) {
                this.modsInstalled.addAll(list.filter(p -> !Files.isDirectory(p)).filter(
                        p -> p.toString().toLowerCase(Locale.ENGLISH).endsWith(".jar")
                            || p.toString().toLowerCase(Locale.ENGLISH).endsWith(".zip"))
                    .map(p -> convertPathToDisableableMod(p, Type.ic2lib)).collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        hideSubProgressBar();
    }

    private void installLegacyJavaFixer() {
        addPercent(5);

        // Only apply the legacy Java fixer on <= 1.7.2 versions of Forge
        if (this.loaderVersion == null || !this.loaderVersion.isForge()
            || (!Utils.matchVersion(minecraftVersion.id, "1.6", true, true)
            && !minecraftVersion.id.equals("1.7.2"))) {
            return;
        }

        // #. {0} is the name of a mod we're installing
        fireTask(GetText.tr("Installing {0}", "Legacy Java Fixer"));
        fireSubProgressUnknown();

        com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
            .setUrl(Constants.LEGACY_JAVA_FIXER_URL).hash(Constants.LEGACY_JAVA_FIXER_MD5)
            .downloadTo(FileSystem.DOWNLOADS.resolve("legacyjavafixer-1.0.jar"))
            .copyTo(root.resolve("mods/legacyjavafixer-1.0.jar"));

        if (download.needToDownload()) {
            try {
                download.downloadFile();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to download Legacy Java Fixer", e);
            }
        } else {
            download.copy();
        }

        DisableableMod mod = new DisableableMod();
        mod.disabled = false;
        mod.userAdded = false;
        mod.wasSelected = true;
        mod.skipped = false;
        mod.file = "legacyjavafixer-1.0.jar";
        mod.type = Type.mods;
        mod.optional = false;
        mod.name = "Legacy Java Fixer";
        mod.version = "1.0";
        mod.description = "Fixes issues with newer Java versions on Minecraft 1.6 and below";

        this.modsInstalled.add(mod);

        hideSubProgressBar();
    }

    private void runCaseConversion() throws Exception {
        addPercent(5);

        if (this.packVersion.caseAllFiles == null) {
            return;
        }

        Files.walkFileTree(this.root.resolve("mods"), new CaseFileVisitor(this.packVersion.caseAllFiles,
            this.selectedMods.stream().filter(m -> m.type == ModType.mods).collect(Collectors.toList())));
    }

    private void runActions() {
        addPercent(5);

        if (this.packVersion.actions == null || this.packVersion.actions.isEmpty()) {
            return;
        }

        for (com.atlauncher.data.json.Action action : this.packVersion.actions) {
            action.execute(this);
        }
    }

    private void installConfigs() throws Exception {
        addPercent(5);

        if (this.packVersion.noConfigs) {
            return;
        }

        if (curseForgeManifest != null) {
            if (Files.exists(this.curseForgeExtractedPath
                .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides")))) {
                fireSubProgressUnknown();
                fireTask(GetText.tr("Copying Overrides"));

                try (Stream<Path> stream = Files.walk(this.curseForgeExtractedPath
                    .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides")))) {
                    stream.filter(Files::isRegularFile).forEach(path -> overridePaths.add(this.curseForgeExtractedPath
                        .resolve(Optional.ofNullable(curseForgeManifest.overrides)
                            .orElse("overrides"))
                        .relativize(
                            path)
                        .toString()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }

                Utils.copyDirectory(this.curseForgeExtractedPath
                        .resolve(Optional.ofNullable(curseForgeManifest.overrides).orElse("overrides")).toFile(),
                    this.root.toFile(), false);
            }
        } else if (modrinthManifest != null) {
            if (Files.exists(this.modrinthExtractedPath.resolve("overrides"))) {
                fireSubProgressUnknown();
                fireTask(GetText.tr("Copying Overrides"));

                try (Stream<Path> stream = Files.walk(this.modrinthExtractedPath.resolve("overrides"))) {
                    stream.filter(Files::isRegularFile).forEach(path -> overridePaths.add(
                        this.modrinthExtractedPath.resolve("overrides").relativize(
                            path).toString()));
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }

                Utils.copyDirectory(this.modrinthExtractedPath.resolve("overrides").toFile(), this.root.toFile(),
                    false);

                if (isServer && Files.isDirectory(this.modrinthExtractedPath.resolve("server-overrides"))) {
                    Utils.copyDirectory(this.modrinthExtractedPath.resolve("server-overrides").toFile(),
                        this.root.toFile(),
                        false);
                } else if (!isServer && Files.isDirectory(this.modrinthExtractedPath.resolve("client-overrides"))) {
                    Utils.copyDirectory(this.modrinthExtractedPath.resolve("client-overrides").toFile(),
                        this.root.toFile(),
                        false);
                }
            }
        } else if (ftbPackManifest != null) {
            fireSubProgressUnknown();
            fireTask(GetText.tr("Calculating Files To Download"));

            List<com.atlauncher.network.Download> filesToDownload = ftbPackVersionManifest.files.parallelStream()
                .filter(f -> f.type != FTBPackVersionManifestFileType.MOD).map(file -> {
                    overridePaths.add(file.getPath() + file.name);

                    com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
                        .setUrl(file.url).size((long) file.size).hash(file.sha1).ignoreFailures()
                        .downloadTo(root.resolve(file.getPath() + file.name))
                        .withInstanceInstaller(this).withHttpClient(Network.createProgressClient(this));

                    return download;
                }).collect(Collectors.toList());

            fireTask(GetText.tr("Creating Config Directories"));

            ftbPackVersionManifest.files.stream()
                .filter(f -> f.type != FTBPackVersionManifestFileType.MOD)
                .map(file -> root.resolve(file.getPath()))
                .forEach(path -> {
                    if (!Files.exists(path)) {
                        try {
                            Files.createDirectories(path);
                        } catch (IOException e) {
                            LogManager.logStackTrace(e);
                        }
                    }
                });

            fireTask(GetText.tr("Downloading Configs"));

            DownloadPool pool = new DownloadPool();
            pool.addAll(filesToDownload);

            DownloadPool smallPool = pool.downsize();

            this.setTotalBytes(smallPool.totalSize());
            this.fireSubProgress(0);

            smallPool.downloadAll();
        } else if (multiMCManifest != null) {
            fireSubProgressUnknown();
            String minecraftFolder = Files.exists(multiMCExtractedPath.resolve(".minecraft")) ? ".minecraft"
                : "minecraft";

            fireTask(GetText.tr("Copying minecraft folder"));
            Utils.copyDirectory(this.multiMCExtractedPath.resolve(minecraftFolder + "/").toFile(), this.root.toFile(),
                false);
        } else if (technicModpack != null) {
            if (technicModpackExtractedPath != null) {
                fireSubProgressUnknown();
                fireTask(GetText.tr("Copying modpack.zip files"));
                Utils.copyDirectory(this.technicModpackExtractedPath.toFile(), this.root.toFile(), false);
            }
        } else if (!pack.vanillaInstance) {
            fireTask(GetText.tr("Downloading Configs"));

            File configs = this.temp.resolve("Configs.zip").toFile();
            String path = "packs/" + pack.getSafeName() + "/versions/" + version.version + "/Configs.zip";

            com.atlauncher.network.Download configsDownload = com.atlauncher.network.Download.build()
                .setUrl(String.format("%s/%s", Constants.DOWNLOAD_SERVER, path)).downloadTo(configs.toPath())
                .size(this.packVersion.configs.filesize).hash(this.packVersion.configs.sha1)
                .withInstanceInstaller(this).withHttpClient(Network.createProgressClient(this));

            this.setTotalBytes(configsDownload.getFilesize());
            configsDownload.downloadFile();

            if (!configs.exists()) {
                throw new Exception("Failed to download configs for pack!");
            }

            // file is empty, so don't try to extract
            if (configs.length() == 0L) {
                return;
            }

            fireSubProgressUnknown();
            fireTask(GetText.tr("Extracting Configs"));

            Path configsExtractedPath = this.temp.resolve("configs");
            ArchiveUtils.extract(configs.toPath(), this.root);
            ArchiveUtils.extract(configs.toPath(), configsExtractedPath);
            Utils.delete(configs);

            // Read the config files and add them to the override paths
            try (Stream<Path> stream = Files.walk(configsExtractedPath)) {
                stream.filter(Files::isRegularFile)
                    .forEach(path2 -> overridePaths.add(configsExtractedPath.relativize(path2).toString()));
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
            FileUtils.deleteDirectory(configsExtractedPath);
        }
    }

    /**
     * Only support agents from MultiMC imports for now.
     */
    private List<Library> getMultiMCLibraries() {
        List<Library> libraries = new ArrayList<>();

        this.multiMCManifest.getNonStandardComponents().forEach(c -> {
            Path pathToPatch = this.multiMCExtractedPath.resolve("patches/" + c.uid + ".json");

            if (!Files.exists(pathToPatch)) {
                LogManager.error("Could not find patch for " + c.uid);
                return;
            }

            try {
                MultiMCPatch patch = Gsons.DEFAULT.fromJson(new String(Files.readAllBytes(pathToPatch),
                    StandardCharsets.UTF_8), MultiMCPatch.class);

                patch.agents.forEach(agent -> {
                    Library library = new Library();

                    Downloads downloads = new Downloads();
                    Download artifact = new Download();
                    artifact.path = Utils.convertMavenIdentifierToPath(agent.name);
                    artifact.size = -1;

                    if (agent.url != null) {
                        artifact.url = String.format("%s%s", Utils.ensureTrailingSlash(agent.url), artifact.path);
                    }

                    downloads.artifact = artifact;

                    library.downloads = downloads;
                    library.name = agent.name;

                    libraries.add(library);
                });
            } catch (IOException e) {
                LogManager.error("Failed to read patch for " + c.uid + " from " + pathToPatch);
            }
        });

        return libraries;
    }

    private void downloadImage() throws Exception {
        addPercent(5);

        if (this.pack.curseForgeProject != null) {
            fireTask(GetText.tr("Downloading Image"));
            CurseForgeAttachment attachment = this.pack.curseForgeProject.getLogo().orElse(null);

            if (attachment != null) {
                com.atlauncher.network.Download imageDownload = com.atlauncher.network.Download.build()
                    .setUrl(attachment.url).downloadTo(root.resolve(isServer ? "server.png" : "instance.png"))
                    .withInstanceInstaller(this)
                    .withHttpClient(Network.createProgressClient(this));

                this.setTotalBytes(imageDownload.getFilesize());
                imageDownload.downloadFile();
            }
        } else if (this.pack.modrinthProject != null) {
            fireTask(GetText.tr("Downloading Image"));
            if (this.pack.modrinthProject.iconUrl != null) {
                com.atlauncher.network.Download imageDownload = com.atlauncher.network.Download.build()
                    .setUrl(this.pack.modrinthProject.iconUrl)
                    .downloadTo(root.resolve(isServer ? "server.png" : "instance.png"))
                    .withInstanceInstaller(this).ignoreFailures()
                    .withHttpClient(Network.createProgressClient(this));

                this.setTotalBytes(imageDownload.getFilesize());
                imageDownload.downloadFile();
            }
        } else if (ftbPackManifest != null) {
            fireTask(GetText.tr("Downloading Image"));
            FTBPackArt art = this.ftbPackManifest.art.stream()
                .filter(a -> a.type == FTBPackArtType.SQUARE).findFirst().orElse(null);

            if (art != null) {
                // we can't check the provided hash and size here otherwise download fails as
                // their api doesn't return the correct info
                com.atlauncher.network.Download imageDownload = com.atlauncher.network.Download.build().setUrl(art.url)
                    .size(art.size).hash(art.sha1)
                    .downloadTo(root.resolve(isServer ? "server.png" : "instance.png")).ignoreFailures()
                    .withInstanceInstaller(this).withHttpClient(Network.createProgressClient(this));

                this.setTotalBytes(art.size);
                imageDownload.downloadFile();
            }
        } else if (technicModpack != null) {
            fireTask(GetText.tr("Downloading Image"));
            TechnicModpackAsset logo = this.technicModpack.logo;

            if (logo != null && logo.url != null && !logo.url.isEmpty()) {
                com.atlauncher.network.Download imageDownload = com.atlauncher.network.Download.build().setUrl(logo.url)
                    .downloadTo(root.resolve("instance.png")).withInstanceInstaller(this)
                    .withHttpClient(Network.createProgressClient(this));

                if (logo.md5 != null) {
                    imageDownload = imageDownload.hash(logo.md5);
                }

                imageDownload.downloadFile();
            }
        } else if (multiMCManifest != null && multiMCManifest.config.iconKey != null && Files.exists(
            multiMCExtractedPath.resolve(multiMCManifest.config.iconKey + ".png"))) {
            fireTask(GetText.tr("Copying Image"));
            Files.copy(multiMCExtractedPath.resolve(multiMCManifest.config.iconKey + ".png"),
                root.resolve("instance.png"));
        }
    }

    private void checkModsOnCurseForge() {
        if (App.settings.dontCheckModsOnCurseForge || this.modsInstalled.isEmpty()) {
            return;
        }

        // #. {0} is the platform we're checking mods on (e.g. CurseForge/Modrinth)
        fireTask(GetText.tr("Checking Mods On {0}", "CurseForge"));
        fireSubProgressUnknown();

        Map<Long, DisableableMod> murmurHashes = new HashMap<>();

        this.modsInstalled.stream().filter(dm -> dm.curseForgeProject == null && dm.curseForgeFile == null)
            .filter(dm -> {
                File file = dm.getFile(root, this.packVersion.minecraft);
                return file != null && file.exists();
            })
            .forEach(dm -> {
                try {
                    long hash = Hashing.murmur(dm.getFile(root, this.packVersion.minecraft).toPath());
                    murmurHashes.put(hash, dm);
                } catch (Throwable t) {
                    LogManager.logStackTrace(t);
                }
            });

        if (!murmurHashes.isEmpty()) {
            CurseForgeFingerprint fingerprintResponse = CurseForgeApi
                .checkFingerprints(murmurHashes.keySet().stream().toArray(Long[]::new));

            if (fingerprintResponse != null && fingerprintResponse.exactMatches != null) {
                int[] projectIdsFound = fingerprintResponse.exactMatches.stream().mapToInt(em -> em.id).toArray();

                if (projectIdsFound.length != 0) {
                    Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi.getProjectsAsMap(projectIdsFound);

                    if (foundProjects != null) {
                        fingerprintResponse.exactMatches.stream().filter(em -> em != null && em.file != null
                            && murmurHashes.containsKey(em.file.packageFingerprint)).forEach(foundMod -> {
                            DisableableMod dm = murmurHashes.get(foundMod.file.packageFingerprint);

                            CurseForgeProject curseForgeProject = foundProjects.get(foundMod.id);

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
                        });
                    }
                }
            }
        }
    }

    private void checkModsOnModrinth() {
        if (App.settings.dontCheckModsOnModrinth || this.modsInstalled.isEmpty()) {
            return;
        }

        // #. {0} is the platform we're checking mods on (e.g. CurseForge/Modrinth)
        fireTask(GetText.tr("Checking Mods On {0}", "Modrinth"));
        fireSubProgressUnknown();

        Map<String, DisableableMod> sha1Hashes = new HashMap<>();

        this.modsInstalled.stream().filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
            .filter(dm -> dm.getFile(root, this.packVersion.minecraft) != null).forEach(dm -> {
                try {
                    sha1Hashes.put(Hashing.sha1(dm.getFile(root, this.packVersion.minecraft).toPath()).toString(),
                        dm);
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
                    Map<String, ModrinthProject> foundProjects = ModrinthApi.getProjectsAsMap(projectIdsFound);

                    if (foundProjects != null) {
                        for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                            ModrinthVersion version = entry.getValue();
                            ModrinthProject project = foundProjects.get(version.projectId);

                            if (project != null) {
                                DisableableMod dm = sha1Hashes.get(entry.getKey());

                                // add Modrinth information
                                dm.modrinthProject = project;
                                dm.modrinthVersion = version;
                                dm.name = project.title;
                                dm.description = project.description;

                                LogManager
                                    .debug(String.format("Found matching mod from Modrinth called %s with file %s",
                                        project.title, version.name));
                            }
                        }
                    }
                }
            }
        }
    }

    public List<Mod> sortMods(List<Mod> original) {
        List<Mod> mods = new ArrayList<>(original);

        for (Mod mod : original) {
            if (mod.isOptional()) {
                if (mod.hasLinked()) {
                    for (Mod mod1 : original) {
                        if (mod1.getName().equalsIgnoreCase(mod.getLinked())) {
                            mods.remove(mod);
                            int index = mods.indexOf(mod1) + 1;
                            mods.add(index, mod);
                        }
                    }

                }
            }
        }

        List<Mod> modss = new ArrayList<>();

        for (Mod mod : mods) {
            if (!mod.isOptional()) {
                modss.add(mod); // Add all non optional mods
            }
        }

        for (Mod mod : mods) {
            if (!modss.contains(mod)) {
                modss.add(mod); // Add the rest
            }
        }

        return modss;
    }

    private void backupSelectFiles() {
        File reis = new File(this.root.resolve("mods").toFile(), "rei_minimap");
        if (reis.exists() && reis.isDirectory()) {
            if (Utils.copyDirectory(reis, this.temp.toFile(), true)) {
                savedReis = true;
            }
        }

        File zans = new File(this.root.resolve("mods").toFile(), "VoxelMods");
        if (zans.exists() && zans.isDirectory()) {
            if (Utils.copyDirectory(zans, this.temp.toFile(), true)) {
                savedZans = true;
            }
        }

        File neiCfg = new File(this.root.resolve("config").toFile(), "NEI.cfg");
        if (neiCfg.exists() && neiCfg.isFile()) {
            if (Utils.copyFile(neiCfg, this.temp.toFile())) {
                savedNEICfg = true;
            }
        }

        File optionsTXT = new File(this.root.toFile(), "options.txt");
        if (optionsTXT.exists() && optionsTXT.isFile()) {
            if (Utils.copyFile(optionsTXT, this.temp.toFile())) {
                savedOptionsTxt = true;
            }
        }

        File serversDAT = new File(this.root.toFile(), "servers.dat");
        if (serversDAT.exists() && serversDAT.isFile()) {
            if (Utils.copyFile(serversDAT, this.temp.toFile())) {
                savedServersDat = true;
            }
        }

        File portalGunSounds = new File(this.root.resolve("mods").toFile(), "PortalGunSounds.pak");
        if (portalGunSounds.exists() && portalGunSounds.isFile()) {
            savedPortalGunSounds = true;
            Utils.copyFile(portalGunSounds, this.temp.toFile());
        }

        if (isReinstall && this.packVersion.keeps != null) {
            Keeps keeps = this.packVersion.keeps;

            if (keeps.hasFileKeeps()) {
                for (Keep keep : keeps.getFiles()) {
                    if (keep.isAllowed()) {
                        File file = keep.getFile(
                            keep.getBase().equalsIgnoreCase("config") ? this.root.resolve("config").toFile()
                                : this.root.toFile());
                        if (file.exists()) {
                            Utils.copyFile(file, this.temp.toFile());
                        }
                    }
                }
            }

            if (keeps.hasFolderKeeps()) {
                for (Keep keep : keeps.getFolders()) {
                    if (keep.isAllowed()) {
                        File file = keep.getFile(
                            keep.getBase().equalsIgnoreCase("config") ? this.root.resolve("config").toFile()
                                : this.root.toFile());
                        if (file.exists() && file.isDirectory()) {
                            Utils.copyDirectory(file, this.temp.toFile(), true);
                        }
                    }
                }
            }
        }
    }

    protected void prepareFilesystem() throws Exception {
        if (isReinstall || isServer) {
            if (Files.isDirectory(this.root.resolve("bin"))) {
                FileUtils.deleteDirectory(this.root.resolve("bin"));
            }

            // only delete the config folder if the overridePaths list is empty
            if (Files.isDirectory(this.root.resolve("config")) && (instance == null
                || this.instance.launcher.overridePaths == null
                || this.instance.launcher.overridePaths.isEmpty())) {
                FileUtils.deleteDirectory(this.root.resolve("config"));
            }

            if (isReinstall) {
                if (Files.isDirectory(this.root.resolve("mods"))) {
                    Utils.deleteWithFilter(this.root.resolve("mods").toFile(),
                        instance.getPackMods(com.atlauncher.data.Type.mods), true);
                }

                if (Files.isDirectory(this.root.resolve("coremods"))) {
                    Utils.deleteWithFilter(this.root.resolve("coremods").toFile(),
                        instance.getPackMods(com.atlauncher.data.Type.coremods), true);
                }

                if (Files.isDirectory(this.root.resolve("jarmods"))) {
                    Utils.deleteWithFilter(this.root.resolve("jarmods").toFile(),
                        instance.getPackMods(com.atlauncher.data.Type.jar), true);

                    Utils.deleteWithFilter(this.root.resolve("jarmods").toFile(),
                        instance.getPackMods(com.atlauncher.data.Type.forge), true);
                }

                if (this.instance.launcher.overridePaths != null && !this.instance.launcher.overridePaths.isEmpty()) {
                    this.instance.launcher.overridePaths.stream()
                        .filter(Objects::nonNull)
                        .forEach(path -> {
                            Path resolvedPath = this.root.resolve(path);

                            if (Files.exists(resolvedPath)) {
                                FileUtils.delete(resolvedPath);

                                // delete empty parent directories recursively up to root
                                try {
                                    Path parentDir = resolvedPath.getParent();
                                    while (parentDir != null && parentDir.startsWith(this.root)
                                        && !parentDir.equals(this.root)
                                        && Files.isDirectory(parentDir)) {

                                        try (Stream<Path> stream = Files.list(parentDir)) {
                                            if (stream.count() == 0) {
                                                FileUtils.deleteDirectory(parentDir);
                                                parentDir = parentDir.getParent();
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                } catch (IOException ignored) {
                                    // ignored
                                }
                            }
                        });
                }
            } else {
                if (Files.isDirectory(this.root.resolve("mods"))) {
                    FileUtils.deleteDirectory(this.root.resolve("mods"));
                }

                if (isReinstall && Files.isDirectory(this.root.resolve("jarmods"))) {
                    FileUtils.deleteDirectory(this.root.resolve("jarmods"));
                }
            }

            if (isReinstall) {
                if (Files.exists(this.root.resolve("texturepacks/TexturePack.zip"))) {
                    FileUtils.delete(this.root.resolve("texturepacks/TexturePack.zip"));
                }

                if (Files.exists(this.root.resolve("resourcepacks/ResourcePack.zip"))) {
                    FileUtils.delete(this.root.resolve("resourcepacks/ResourcePack.zip"));
                }
            } else {
                if (Files.isDirectory(this.root.resolve("libraries"))) {
                    FileUtils.deleteDirectory(this.root.resolve("libraries"));
                }
            }

            if (isReinstall && this.packVersion.deletes != null) {
                Deletes deletes = this.packVersion.deletes;

                if (deletes.hasFileDeletes()) {
                    for (Delete delete : deletes.getFiles()) {
                        if (delete.isAllowed()) {
                            File file = delete.getFile(this.root.toFile());
                            if (file.exists()) {
                                Utils.delete(file);
                            }
                        }
                    }
                }

                if (deletes.hasFolderDeletes()) {
                    for (Delete delete : deletes.getFolders()) {
                        if (delete.isAllowed()) {
                            File file = delete.getFile(this.root.toFile());
                            if (file.exists()) {
                                Utils.delete(file);
                            }
                        }
                    }
                }
            }

            // delete all files downloaded previously if FTB pack
            if (isReinstall && instance != null && instance.isFTBPack()) {
                instance.launcher.ftbPackVersionManifest.files.stream()
                    .filter(f -> f.type != FTBPackVersionManifestFileType.MOD)
                    .map(file -> instance.ROOT.resolve(file.getPath() + file.name))
                    .forEach(path -> {
                        if (Files.exists(path) && !Files.isDirectory(path)) {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                LogManager.logStackTrace(e);
                            }
                        }
                    });

                // now delete all the empty directories left over to cleanup
                instance.launcher.ftbPackVersionManifest.files.stream()
                    .filter(f -> f.type != FTBPackVersionManifestFileType.MOD)
                    .map(file -> instance.ROOT.resolve(file.getPath()))
                    .distinct().sorted(Comparator.comparingInt(Path::getNameCount).reversed()).forEach(path -> {
                        try {
                            if (Files.exists(path) && FileUtils.directoryIsEmpty(path)) {
                                Files.delete(path);
                            }
                        } catch (IOException e) {
                            LogManager.logStackTrace(e);
                        }
                    });
            }
        }

        // make some new directories
        Path[] directories;
        if (isServer) {
            directories = new Path[] { this.root, this.root.resolve("mods"), this.temp,
                this.root.resolve("libraries") };
        } else {
            directories = new Path[] { this.root, this.root.resolve("mods"), this.root.resolve("disabledmods"),
                this.temp, this.temp.resolve("loader"), this.root.resolve("jarmods") };
        }

        for (Path directory : directories) {
            if (!Files.exists(directory)) {
                FileUtils.createDirectory(directory);
            }
        }
    }

    private void restoreSelectFiles() {
        if (savedReis) {
            Utils.copyDirectory(new File(this.temp.toFile(), "rei_minimap"),
                new File(this.root.resolve("mods").toFile(), "rei_minimap"));
        }

        if (savedZans) {
            Utils.copyDirectory(new File(this.temp.toFile(), "VoxelMods"),
                new File(this.root.resolve("mods").toFile(), "VoxelMods"));
        }

        if (savedNEICfg) {
            Utils.copyFile(new File(this.temp.toFile(), "NEI.cfg"),
                new File(this.root.resolve("config").toFile(), "NEI.cfg"), true);
        }

        if (savedOptionsTxt) {
            Utils.copyFile(new File(this.temp.toFile(), "options.txt"), new File(this.root.toFile(), "options.txt"),
                true);
        }

        if (savedServersDat) {
            Utils.copyFile(new File(this.temp.toFile(), "servers.dat"), new File(this.root.toFile(), "servers.dat"),
                true);
        }

        if (savedPortalGunSounds) {
            Utils.copyFile(new File(this.temp.toFile(), "PortalGunSounds.pak"),
                new File(this.root.resolve("mods").toFile(), "PortalGunSounds.pak"), true);
        }

        if (isReinstall && this.packVersion.keeps != null) {
            Keeps keeps = this.packVersion.keeps;

            if (keeps.hasFileKeeps()) {
                for (Keep keep : keeps.getFiles()) {
                    if (keep.isAllowed()) {
                        File from = keep.getFile(this.temp.toFile());
                        File to = keep.getFile(
                            keep.getBase().equalsIgnoreCase("config") ? this.root.resolve("config").toFile()
                                : this.root.toFile());
                        if (from.exists()) {
                            to.getParentFile().mkdirs();
                            Utils.copyFile(from, to, true);
                        }
                    }
                }
            }

            if (keeps.hasFolderKeeps()) {
                for (Keep keep : keeps.getFolders()) {
                    if (keep.isAllowed()) {
                        File from = keep.getFile(this.temp.toFile());
                        File to = keep.getFile(
                            keep.getBase().equalsIgnoreCase("config") ? this.root.resolve("config").toFile()
                                : this.root.toFile());
                        if (from.exists()) {
                            to.getParentFile().mkdirs();
                            Utils.copyDirectory(from, to);
                        }
                    }
                }
            }
        }
    }

    private void installServerBootScripts() throws Exception {
        if (!isServer || (this.loaderVersion != null && !this.loaderVersion.shouldInstallServerScripts())) {
            return;
        }

        Path batPath = this.root.resolve("LaunchServer.bat");
        Path shPath = this.root.resolve("LaunchServer.sh");
        Path tmpBatPath = this.temp.resolve("LaunchServer.bat");
        Path tmpShPath = this.temp.resolve("LaunchServer.sh");
        Path tmp1BatPath = this.temp.resolve("LaunchServer1.bat");
        Path tmp1ShPath = this.temp.resolve("LaunchServer1.sh");
        File batFile = batPath.toFile();
        File shFile = shPath.toFile();

        // write out the server jar filename
        Utils.replaceText(App.class.getResourceAsStream("/server-scripts/LaunchServer.bat"), tmpBatPath.toFile(),
            "%%SERVERJAR%%", getServerJar());
        Utils.replaceText(App.class.getResourceAsStream("/server-scripts/LaunchServer.sh"), tmpShPath.toFile(),
            "%%SERVERJAR%%",
            getServerJar());

        // replace/remove the server arguments (if any)
        Utils.replaceText(Files.newInputStream(tmpBatPath), tmp1BatPath.toFile(), "%%ARGUMENTS%%",
            this.packVersion.serverArguments);
        Utils.replaceText(Files.newInputStream(tmpShPath), tmp1ShPath.toFile(), "%%ARGUMENTS%%",
            this.packVersion.serverArguments);

        // replace/remove the logging arguments for Log4Shell exploit (if any)
        String log4ShellArguments = this.getLog4ShellArguments();
        Utils.replaceText(Files.newInputStream(tmp1BatPath), batFile, "%%LOG4SHELLARGUMENTS%%", log4ShellArguments);
        Utils.replaceText(Files.newInputStream(tmp1ShPath), shFile, "%%LOG4SHELLARGUMENTS%%", log4ShellArguments);

        batFile.setExecutable(true);
        shFile.setExecutable(true);

        Utils.writeResourceToFile(
            App.class.getResourceAsStream("/server-scripts/readme.txt"),
            new File(this.root.toFile(), "_readme.txt"));

        if (this.loaderVersion != null && this.loaderVersion.isForgeLikeAndUsesServerStarterJar()) {
            String url = ConfigManager.getConfigItem("neoForgeServerStarterJar.url", "");
            String[] parts = url.split("/");
            String downloadsFileName = String.format("ServerStarterJar-%s.jar", parts[parts.length - 2]);
            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                .setUrl(url)
                .hash(ConfigManager.getConfigItem("neoForgeServerStarterJar.hash", ""))
                .size(ConfigManager.getConfigItem("neoForgeServerStarterJar.size", 0.0).longValue())
                .downloadTo(FileSystem.DOWNLOADS.resolve(downloadsFileName))
                .copyTo(root.resolve("server.jar"));

            if (download.needToDownload()) {
                try {
                    download.downloadFile();
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to download NeoForge ServerStarterJar", e);
                }
            } else {
                download.copy();
            }
        }
    }

    private void writeLog4ShellExploitArgumentsForForgeScripts() throws Exception {
        if (!isServer || (this.loaderVersion != null && this.loaderVersion.shouldInstallServerScripts())) {
            return;
        }

        if (Files.exists(root.resolve("user_jvm_args.txt")) && minecraftVersionManifest.isLog4ShellExploitable()) {
            Files.write(root.resolve("user_jvm_args.txt"),
                (System.lineSeparator() + this.getLog4ShellArguments()).getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
    }

    private void writeLog4j2XmlFileIfNeeded() throws Exception {
        if (!isServer) {
            return;
        }

        if (minecraftVersionManifest.isLog4ShellExploitable()) {
            if (loaderVersion != null && loaderVersion.isForge()) {
                Utils.writeResourceToFile(App.class.getResourceAsStream(minecraftVersionManifest.getLog4JFileForge()),
                    root.resolve("log4j2.xml").toFile());
            } else {
                Utils.writeResourceToFile(App.class.getResourceAsStream(minecraftVersionManifest.getLog4JFile()),
                    root.resolve("log4j2.xml").toFile());
            }
        }
    }

    private String getLog4ShellArguments() throws Exception {
        if (minecraftVersionManifest.isLog4ShellExploitable()) {
            return "-Dlog4j2.formatMsgNoLookups=true -Dlog4j.configurationFile=log4j2.xml";
        }

        return "";
    }

    public String getServerJar() {
        if (this.loader != null) {
            return this.loader.getServerJar();
        }

        com.atlauncher.data.json.Mod forge = null;
        com.atlauncher.data.json.Mod mcpc = null;
        for (com.atlauncher.data.json.Mod mod : this.selectedMods) {
            if (mod.getType() == com.atlauncher.data.json.ModType.forge) {
                forge = mod;
            } else if (mod.getType() == com.atlauncher.data.json.ModType.mcpc) {
                mcpc = mod;
            }
        }
        if (mcpc != null) {
            return mcpc.getFile();
        } else if (forge != null) {
            return forge.getFile();
        } else {
            return "minecraft_server." + this.version.minecraftVersion.id + ".jar";
        }
    }

    public boolean hasRecommendedMods() {
        for (Mod mod : allMods) {
            if (mod.isRecommended()) {
                return true; // One of the mods is marked as recommended, so return true
            }
        }
        return false; // No non recommended mods found
    }

    public boolean isOnlyRecommendedInGroup(Mod mod) {
        for (Mod modd : allMods) {
            if (modd == mod || !modd.hasGroup()) {
                continue;
            }
            if (modd.getGroup().equalsIgnoreCase(mod.getGroup()) && modd.isRecommended()) {
                return false; // Another mod is recommended. Don't check anything
            }
        }
        return true; // No other recommended mods found in the group
    }

    public Mod getModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.getName().equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }

    public List<Mod> getLinkedMods(Mod mod) {
        List<Mod> linkedMods = new ArrayList<>();
        for (Mod modd : allMods) {
            if (!modd.hasLinked()) {
                continue;
            }
            if (modd.getLinked().equalsIgnoreCase(mod.getName())) {
                linkedMods.add(modd);
            }
        }
        return linkedMods;
    }

    public List<Mod> getGroupedMods(Mod mod) {
        List<Mod> groupedMods = new ArrayList<>();
        for (Mod modd : allMods) {
            if (!modd.hasGroup()) {
                continue;
            }
            if (modd.getGroup().equalsIgnoreCase(mod.getGroup())) {
                if (modd != mod) {
                    groupedMods.add(modd);
                }
            }
        }
        return groupedMods;
    }

    @SuppressWarnings("LabelledBreakTarget")
    public List<Mod> getModsDependancies(Mod mod) {
        List<Mod> dependsMods = new ArrayList<>();
        for (String modName : mod.getDepends()) {
            inner:
            {
                for (Mod modd : allMods) {
                    if (modd.getName().equalsIgnoreCase(modName)) {
                        dependsMods.add(modd);
                        break inner;
                    }
                }
            }
        }
        return dependsMods;
    }

    public List<Mod> dependedMods(Mod mod) {
        List<Mod> dependedMods = new ArrayList<>();
        for (Mod modd : allMods) {
            if (!modd.hasDepends()) {
                continue;
            }
            if (modd.isADependancy(mod)) {
                dependedMods.add(modd);
            }
        }
        return dependedMods;
    }

    public boolean hasADependancy(Mod mod) {
        for (Mod modd : allMods) {
            if (!modd.hasDepends()) {
                continue;
            }
            if (modd.isADependancy(mod)) {
                return true;
            }
        }
        return false;
    }

    public boolean wasModInstalled(String mod) {
        return instance.wasModInstalled(mod);
    }

    public boolean wasModSelected(String mod) {
        return instance.wasModSelected(mod);
    }

    public void fireTask(String name) {
        LogManager.debug("Instance Installer: " + name);
        firePropertyChange("doing", null, name);
    }

    protected void fireProgress(double percent) {
        if (percent > 100.0) {
            percent = 100.0;
        }
        firePropertyChange("progress", null, percent);
    }

    protected void fireSubProgress(double percent) {
        if (percent > 100.0) {
            percent = 100.0;
        }
        firePropertyChange("subprogress", null, percent);
    }

    protected void fireSubProgress(double percent, String paint) {
        if (percent > 100.0) {
            percent = 100.0;
        }
        String[] info = new String[2];
        info[0] = "" + percent;
        info[1] = paint;
        firePropertyChange("subprogress", null, info);
    }

    public void fireSubProgressUnknown() {
        firePropertyChange("subprogressint", null, null);
    }

    protected void addPercent(double percent) {
        this.percent = this.percent + percent;
        if (this.percent > 100.0) {
            this.percent = 100.0;
        }
        fireProgress(this.percent);
    }

    public void setSubPercent(double percent) {
        this.subPercent = percent;
        if (this.subPercent > 100.0) {
            this.subPercent = 100.0;
        }
        fireSubProgress(this.subPercent);
    }

    public void addSubPercent(double percent) {
        this.subPercent = this.subPercent + percent;
        if (this.subPercent > 100.0) {
            this.subPercent = 100.0;
        }

        if (this.subPercent > 100.0) {
            this.subPercent = 100.0;
        }
        fireSubProgress(this.subPercent);
    }

    @Override
    public void setTotalBytes(long bytes) {
        this.downloadedBytes = 0L;
        this.totalBytes = bytes;
        this.updateProgressBar();
    }

    @Override
    public void addDownloadedBytes(long bytes) {
        this.downloadedBytes += bytes;
        this.updateProgressBar();
    }

    @Override
    public void addBytesToDownload(long bytes) {
        this.totalBytes += bytes;
        this.updateProgressBar();
    }

    private void updateProgressBar() {
        double progress;
        if (this.totalBytes > 0) {
            progress = (this.downloadedBytes / this.totalBytes) * 100.0;
        } else {
            progress = 0.0;
        }
        double done = this.downloadedBytes / 1024.0 / 1024.0;
        double toDo = this.totalBytes / 1024.0 / 1024.0;
        if (done > toDo) {
            fireSubProgress(100.0, String.format(Locale.ENGLISH, "%.2f MB", done));
        } else {
            fireSubProgress(progress, String.format(Locale.ENGLISH, "%.2f MB / %.2f MB", done, toDo));
        }
    }

    private void hideSubProgressBar() {
        fireSubProgress(-1);
    }
}
