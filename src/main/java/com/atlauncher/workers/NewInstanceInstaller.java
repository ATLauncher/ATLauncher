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
package com.atlauncher.workers;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.ForgeXzDownloadable;
import com.atlauncher.data.Language;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.AssetObject;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingFile;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.MojangDownload;
import com.atlauncher.data.minecraft.MojangDownloads;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.loaders.Loader;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLibrary;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.CaseFileVisitor;

import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import okhttp3.OkHttpClient;

public class NewInstanceInstaller extends InstanceInstaller {
    protected double percent = 0.0; // Percent done installing
    protected double subPercent = 0.0; // Percent done sub installing
    protected double totalBytes = 0; // Total number of bytes to download
    protected double downloadedBytes = 0; // Total number of bytes downloaded

    public List<Library> libraries = new ArrayList<>();
    public Loader loader;
    public LoaderVersion loaderVersion;
    public com.atlauncher.data.json.Version packVersion;
    public MinecraftVersion minecraftVersion;

    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedOptionsTxt = false; // If options.txt was found and saved
    private boolean savedServersDat = false; // If servers.dat was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved

    public String mainClass;
    public Arguments arguments;

    public NewInstanceInstaller(String instanceName, com.atlauncher.data.Pack pack,
            com.atlauncher.data.PackVersion version, boolean isReinstall, boolean isServer, String shareCode,
            boolean showModsChooser, com.atlauncher.data.loaders.LoaderVersion loaderVersion) {
        super(instanceName, pack, version, isReinstall, isServer, shareCode, showModsChooser, loaderVersion);

        this.loaderVersion = Gsons.MINECRAFT.fromJson(Gsons.DEFAULT.toJson(loaderVersion), LoaderVersion.class);
    }

    public NewInstanceInstaller(String instanceName, com.atlauncher.data.Pack pack,
            com.atlauncher.data.PackVersion version, boolean isReinstall, boolean isServer, String shareCode,
            boolean showModsChooser, LoaderVersion loaderVersion) {
        super(instanceName, pack, version, isReinstall, isServer, shareCode, showModsChooser, null);

        this.loaderVersion = loaderVersion;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        LogManager.info("Started install of " + this.pack.getName() + " - " + this.version);

        try {
            downloadPackVersionJson();

            downloadMinecraftVersionJson();

            System.out.println(this.loaderVersion);

            if (this.packVersion.loader != null) {
                this.loader = this.packVersion.getLoader().getNewLoader(new File(this.getTempDirectory(), "loader"),
                        this, this.loaderVersion);

                downloadLoader();
            }

            if (this.packVersion.messages != null) {
                showMessages();
            }

            determineModsToBeInstalled();

            install();

            return true;
        } catch (Exception e) {
            Network.CLIENT.dispatcher().executorService().shutdown();
            Network.CLIENT.connectionPool().evictAll();
            cancel(true);
            LogManager.logStackTrace(e);
        }

        return false;
    }

    private void downloadPackVersionJson() {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.downloadingpackverisondefinition"));
        fireSubProgressUnknown();

        this.packVersion = com.atlauncher.network.Download.build()
                .setUrl(this.pack.getJsonDownloadUrl(version.getVersion()))
                .asClass(com.atlauncher.data.json.Version.class);

        this.packVersion.compileColours();

        hideSubProgressBar();
    }

    private void downloadMinecraftVersionJson() throws Exception {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.downloadingminecraftdefinition"));
        fireSubProgressUnknown();

        VersionManifest versionManifest = com.atlauncher.network.Download.build()
                .setUrl(String.format("%s/mc/game/version_manifest.json", Constants.LAUNCHER_META_MINECRAFT))
                .asClass(VersionManifest.class);

        VersionManifestVersion minecraftVersion = versionManifest.versions.stream()
                .filter(version -> version.id.equalsIgnoreCase(this.packVersion.getMinecraft())).findFirst()
                .orElse(null);

        if (minecraftVersion == null) {
            throw new Exception(
                    String.format("Failed to find Minecraft version of %s", this.packVersion.getMinecraft()));
        }

        this.minecraftVersion = com.atlauncher.network.Download.build().setUrl(minecraftVersion.url)
                .asClass(MinecraftVersion.class);

        hideSubProgressBar();
    }

    private void downloadLoader() {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.downloadingloader"));
        fireSubProgressUnknown();

        this.loader.downloadAndExtractInstaller();

        hideSubProgressBar();
    }

    private void showMessages() throws Exception {
        int ret = 0;

        if (this.isReinstall && this.packVersion.messages.update != null) {
            ret = this.packVersion.messages.showUpdateMessage(this.pack);
        } else if (this.packVersion.messages.install != null) {
            ret = this.packVersion.messages.showInstallMessage(this.pack);
        }

        if (ret != 0) {
            throw new Exception("Install cancelled after viewing message!");
        }
    }

    private void determineModsToBeInstalled() {
        this.allMods = sortMods(
                (this.isServer ? this.packVersion.getServerInstallMods() : this.packVersion.getClientInstallMods()));

        boolean hasOptional = this.allMods.stream().anyMatch(mod -> mod.isOptional());

        if (this.allMods.size() != 0 && hasOptional) {
            com.atlauncher.gui.dialogs.ModsChooser modsChooser = new com.atlauncher.gui.dialogs.ModsChooser(this);

            if (this.shareCode != null) {
                modsChooser.applyShareCode(shareCode);
            }

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

        modsInstalled = new ArrayList<>();
        for (com.atlauncher.data.json.Mod mod : this.selectedMods) {
            String file = mod.getFile();
            if (this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }
            this.modsInstalled
                    .add(new com.atlauncher.data.DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                            com.atlauncher.data.Type.valueOf(com.atlauncher.data.Type.class, mod.getType().toString()),
                            this.packVersion.getColour(mod.getColour()), mod.getDescription(), false, false, true,
                            mod.getCurseModId(), mod.getCurseFileId()));
        }

        if (this.isReinstall && instance.hasCustomMods()
                && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion().getVersion())) {
            for (com.atlauncher.data.DisableableMod mod : instance.getCustomDisableableMods()) {
                modsInstalled.add(mod);
            }
        }
    }

    private Boolean install() throws Exception {
        this.instanceIsCorrupt = true; // From this point on the instance has become corrupt

        getTempDirectory().mkdirs(); // Make the temp directory
        backupSelectFiles();
        makeDirectories();
        addPercent(5);

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

        downloadMods();
        if (isCancelled()) {
            return false;
        }

        installMods();
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

        // Copy over common configs if any
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getRootDirectory());
        }

        restoreSelectFiles();

        installServerBootScripts();

        return true;
    }

    private void determineMainClass() {
        if (this.packVersion.mainClass != null) {
            if (this.packVersion.mainClass.depends == null && this.packVersion.mainClass.dependsGroup == null) {
                this.mainClass = this.packVersion.mainClass.mainClass;
            } else if (this.packVersion.mainClass.depends != null) {
                String depends = this.packVersion.mainClass.depends;

                if (this.selectedMods.stream().filter(mod -> mod.name.equalsIgnoreCase(depends)).count() != 0) {
                    this.mainClass = this.packVersion.mainClass.mainClass;
                }
            } else if (this.packVersion.getMainClass().hasDependsGroup()) {
                String dependsGroup = this.packVersion.mainClass.dependsGroup;

                if (this.selectedMods.stream().filter(mod -> mod.group.equalsIgnoreCase(dependsGroup)).count() != 0) {
                    this.mainClass = this.packVersion.mainClass.mainClass;
                }
            }
        }

        // if none set by pack, then use the minecraft one
        if (this.mainClass == null) {
            this.mainClass = this.version.getMinecraftVersion().getMojangVersion().getMainClass();
        }
    }

    private void determineArguments() {
        this.arguments = new Arguments();

        if (this.loader != null) {
            if (this.loader.useMinecraftArguments()) {
                if (this.minecraftVersion.arguments.game != null && this.minecraftVersion.arguments.game.size() != 0) {
                    this.arguments.game.addAll(this.minecraftVersion.arguments.game);
                }

                if (this.minecraftVersion.arguments.jvm != null && this.minecraftVersion.arguments.jvm.size() != 0) {
                    this.arguments.jvm.addAll(this.minecraftVersion.arguments.jvm);
                }
            }

            Arguments loaderArguments = this.loader.getArguments();

            if (loaderArguments != null) {
                if (loaderArguments.game != null && loaderArguments.game.size() != 0) {
                    this.arguments.game.addAll(loaderArguments.game);
                }

                if (loaderArguments.jvm != null && loaderArguments.jvm.size() != 0) {
                    this.arguments.jvm.addAll(loaderArguments.jvm);
                }
            }
        } else {
            if (this.minecraftVersion.arguments.game != null && this.minecraftVersion.arguments.game.size() != 0) {
                this.arguments.game.addAll(this.minecraftVersion.arguments.game);
            }

            if (this.minecraftVersion.arguments.jvm != null && this.minecraftVersion.arguments.jvm.size() != 0) {
                this.arguments.jvm.addAll(this.minecraftVersion.arguments.jvm);
            }
        }

        if (this.packVersion.extraArguments != null) {
            boolean add = false;

            if (this.packVersion.extraArguments.depends == null
                    && this.packVersion.extraArguments.dependsGroup == null) {
                add = true;
            } else if (this.packVersion.extraArguments.depends == null) {
                String depends = this.packVersion.extraArguments.depends;

                if (this.selectedMods.stream().filter(mod -> mod.name.equalsIgnoreCase(depends)).count() != 0) {
                    add = true;
                }
            } else if (this.packVersion.extraArguments.dependsGroup == null) {
                String dependsGroup = this.packVersion.extraArguments.dependsGroup;

                if (this.selectedMods.stream().filter(mod -> mod.group.equalsIgnoreCase(dependsGroup)).count() != 0) {
                    add = true;
                }
            }

            if (add) {
                this.arguments.game.addAll(Arrays.asList(this.packVersion.extraArguments.arguments.split(" ")).stream()
                        .map(argument -> new ArgumentRule(argument)).collect(Collectors.toList()));
            }
        }
    }

    protected void downloadResources() throws Exception {
        addPercent(5);

        if (this.isServer || this.minecraftVersion.assetIndex == null) {
            return;
        }

        fireTask(Language.INSTANCE.localize("instance.downloadingresources"));
        fireSubProgressUnknown();
        this.totalBytes = this.downloadedBytes = 0;
        OkHttpClient httpClient = Network.createProgressClient(this);

        MojangAssetIndex assetIndex = this.minecraftVersion.assetIndex;
        File indexFile = new File(App.settings.getIndexesAssetsDir(), assetIndex.id + ".json");

        Downloadable assetIndexDownloadable = new Downloadable(assetIndex.url, indexFile, assetIndex.sha1,
                (int) assetIndex.size, this, false);

        if (assetIndexDownloadable.needToDownload()) {
            assetIndexDownloadable.download();
        }

        AssetIndex index = this.gson.fromJson(new FileReader(indexFile), AssetIndex.class);

        DownloadPool pool = new DownloadPool();

        index.objects.entrySet().stream().forEach(entry -> {
            AssetObject object = entry.getValue();
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);
            File file = new File(App.settings.getObjectsAssetsDir(), filename);

            pool.add(new com.atlauncher.network.Download().setUrl(url).downloadTo(file.toPath()).hash(object.hash)
                    .size(object.size).withInstanceInstaller(this).withHttpClient(httpClient)
                    .withFriendlyFileName(entry.getKey()));
        });

        pool.downsize();

        this.setTotalBytes(pool.totalSize());
        this.fireSubProgress(0);

        pool.downloadAll(this);

        hideSubProgressBar();
    }

    private void downloadMinecraft() {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.downloadingminecraft"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        MojangDownloads downloads = this.minecraftVersion.downloads;

        MojangDownload mojangDownload = this.isServer ? downloads.server : downloads.client;

        Downloadable download = new Downloadable(mojangDownload.url, getMinecraftJarLibrary(), mojangDownload.sha1,
                (int) mojangDownload.size, this, false, getMinecraftJar(), this.isServer);

        if (download.needToDownload()) {
            totalBytes += download.getFilesize();
            download.download(true);
        }

        hideSubProgressBar();
    }

    public File getMinecraftJar() {
        if (isServer) {
            return new File(getRootDirectory(), String.format("minecraft_server.%s.jar", this.minecraftVersion.id));
        }

        return new File(getRootDirectory(), String.format("%s.jar", this.minecraftVersion.id));
    }

    private void downloadLoggingClient() {
        addPercent(5);

        if (this.isServer || this.minecraftVersion.logging == null) {
            return;
        }

        fireTask(Language.INSTANCE.localize("instance.downloadingloggingconfig"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        LoggingFile loggingFile = this.minecraftVersion.logging.client.file;

        Downloadable download = new Downloadable(loggingFile.url,
                new File(App.settings.getLogConfigsDir(), loggingFile.id), loggingFile.sha1, (int) loggingFile.size,
                this, false);

        if (download.needToDownload()) {
            totalBytes += download.getFilesize();
            download.download(true);
        }

        hideSubProgressBar();
    }

    private List<Library> getLibraries() {
        List<Library> libraries = new ArrayList<>();

        List<Library> packVersionLibraries = getPackVersionLibraries();

        if (packVersionLibraries != null && packVersionLibraries.size() != 0) {
            libraries.addAll(packVersionLibraries);
        }

        // Now read in the library jars needed from the loader
        if (this.loader != null) {
            libraries.addAll(this.loader.getLibraries());
        }

        // lastly the Minecraft libraries
        if (this.loader == null || this.loader.useMinecraftArguments()) {
            libraries.addAll(this.minecraftVersion.libraries);
        }

        return libraries;
    }

    private List<Library> getPackVersionLibraries() {
        List<Library> libraries = new ArrayList<>();

        // Now read in the library jars needed from the pack
        for (com.atlauncher.data.json.Library library : this.packVersion.getLibraries()) {
            if (this.isServer && !library.forServer()) {
                continue;
            }

            if (library.depends != null) {
                if (this.selectedMods.stream().filter(mod -> mod.name.equalsIgnoreCase(library.depends)).count() == 0) {
                    continue;
                }
            } else if (library.hasDependsGroup()) {
                if (this.selectedMods.stream().filter(mod -> mod.group.equalsIgnoreCase(library.dependsGroup))
                        .count() == 0) {
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
            download.url = String.format("%s/%s", Constants.ATLAUNCHER_DOWNLOAD_SERVER, library.url);

            Downloads downloads = new Downloads();
            downloads.artifact = download;

            minecraftLibrary.downloads = downloads;

            libraries.add(minecraftLibrary);
        }

        return libraries;
    }

    private void downloadLibraries() {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.downloadinglibraries"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        ExecutorService executor;
        List<Downloadable> downloads = getDownloadableLibraries();
        downloads.addAll(getDownloadableNativeLibraries());

        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());

        for (final Downloadable download : downloads) {
            executor.execute(() -> {
                if (download.needToDownload()) {
                    totalBytes += download.getFilesize();
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(0); // Show the subprogress bar

        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());

        for (final Downloadable download : downloads) {
            executor.execute(() -> {
                if (download.needToDownload()) {
                    fireTask(Language.INSTANCE.localize("common.downloading") + " " + download.getFilename());
                    download.download(true);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        hideSubProgressBar();
    }

    private List<Downloadable> getDownloadableLibraries() {
        return this.getLibraries().stream().map(library -> {
            if (library instanceof ForgeLibrary && ((ForgeLibrary) library).isUsingPackXz()) {
                return new ForgeXzDownloadable(library.downloads.artifact.url,
                        new File(App.settings.getGameLibrariesDir(), library.downloads.artifact.path),
                        library.downloads.artifact.sha1, library.downloads.artifact.size, this, null);
            }

            return new Downloadable(library.downloads.artifact.url,
                    new File(App.settings.getGameLibrariesDir(), library.downloads.artifact.path),
                    library.downloads.artifact.sha1, library.downloads.artifact.size, this, false);
        }).collect(Collectors.toList());
    }

    private List<Downloadable> getDownloadableNativeLibraries() {
        return this.getLibraries().stream().filter(library -> library.hasNativeForOS()).map(library -> {
            Download download = library.getNativeDownloadForOS();

            return new Downloadable(download.url, new File(App.settings.getGameLibrariesDir(), download.path),
                    download.sha1, download.size, this, false);
        }).collect(Collectors.toList());
    }

    private void organiseLibraries() {
        addPercent(5);
        fireTask(Language.INSTANCE.localize("instance.organisinglibraries"));
        fireSubProgressUnknown();

        this.getLibraries().stream().forEach(library -> {
            File libraryFile = new File(App.settings.getGameLibrariesDir(), library.downloads.artifact.path);

            if (isServer) {
                File serverFile = new File(getLibrariesDirectory(), library.downloads.artifact.path);

                serverFile.getParentFile().mkdirs();

                Utils.copyFile(libraryFile, serverFile, true);
            } else if (library.hasNativeForOS()) {
                File nativeFile = new File(App.settings.getGameLibrariesDir(), library.getNativeDownloadForOS().path);

                ZipUtil.unpack(nativeFile, this.getNativesDirectory(), new NameMapper() {
                    public String map(String name) {
                        if (library.extract != null && library.extract.shouldExclude(name)) {
                            return null;
                        }

                        return name;
                    }
                });
            }
        });

        hideSubProgressBar();
    }

    private void downloadMods() {
        addPercent(25);

        if (selectedMods.size() == 0) {
            return;
        }

        fireTask(Language.INSTANCE.localize("instance.downloadingmods"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        List<Downloadable> downloads = this.selectedMods.stream().filter(mod -> mod.download != DownloadType.browser)
                .map(mod -> {
                    return new Downloadable(mod.url, new File(App.settings.getDownloadsDir(), mod.getFile()), mod.md5,
                            mod.filesize, this, false);
                }).collect(Collectors.toList());

        totalBytes = downloads.stream().map(download -> download.getFilesize()).reduce(0, Integer::sum);

        fireSubProgress(0);

        downloads.parallelStream().forEach(download -> {
            if (download.needToDownload()) {
                download.download();
            }
        });

        hideSubProgressBar();
    }

    private void installMods() {
        addPercent(25);

        if (this.selectedMods.size() == 0) {
            return;
        }

        fireTask(Language.INSTANCE.localize("instance.installingmods"));
        fireSubProgressUnknown();

        double subPercentPerMod = 100.0 / this.selectedMods.size();

        this.selectedMods.parallelStream().forEach(mod -> {
            mod.install(this);
            addSubPercent(subPercentPerMod);
        });

        hideSubProgressBar();
    }

    private void runCaseConversion() throws Exception {
        addPercent(5);

        if (this.packVersion.caseAllFiles == null) {
            return;
        }

        if (this.isReinstall && this.instance.getMinecraftVersion().equalsIgnoreCase(this.minecraftVersion.id)) {
            Files.walkFileTree(this.getModsDirectory().toPath(), new CaseFileVisitor(this.packVersion.caseAllFiles,
                    this.instance.getCustomMods(com.atlauncher.data.Type.mods)));
        } else {
            Files.walkFileTree(this.getModsDirectory().toPath(), new CaseFileVisitor(this.packVersion.caseAllFiles));
        }
    }

    private void runActions() {
        addPercent(5);

        if (this.packVersion.actions == null || this.packVersion.actions.size() == 0) {
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

        fireTask(Language.INSTANCE.localize("instance.downloadingconfigs"));

        File configs = new File(App.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version.getVersion() + "/Configs.zip";
        Downloadable configsDownload = new Downloadable(path, configs, null, this, true);
        this.totalBytes = configsDownload.getFilesize();
        this.downloadedBytes = 0;
        configsDownload.download(true);

        if (!configs.exists()) {
            throw new Exception("Failed to download configs for pack!");
        }

        fireSubProgressUnknown();
        fireTask(Language.INSTANCE.localize("instance.extractingconfigs"));

        Utils.unzip(configs, getRootDirectory());
        Utils.delete(configs);
    }

    private void backupSelectFiles() {
        File reis = new File(getModsDirectory(), "rei_minimap");
        if (reis.exists() && reis.isDirectory()) {
            if (Utils.copyDirectory(reis, getTempDirectory(), true)) {
                savedReis = true;
            }
        }

        File zans = new File(getModsDirectory(), "VoxelMods");
        if (zans.exists() && zans.isDirectory()) {
            if (Utils.copyDirectory(zans, getTempDirectory(), true)) {
                savedZans = true;
            }
        }

        File neiCfg = new File(getConfigDirectory(), "NEI.cfg");
        if (neiCfg.exists() && neiCfg.isFile()) {
            if (Utils.copyFile(neiCfg, getTempDirectory())) {
                savedNEICfg = true;
            }
        }

        File optionsTXT = new File(getRootDirectory(), "options.txt");
        if (optionsTXT.exists() && optionsTXT.isFile()) {
            if (Utils.copyFile(optionsTXT, getTempDirectory())) {
                savedOptionsTxt = true;
            }
        }

        File serversDAT = new File(getRootDirectory(), "servers.dat");
        if (serversDAT.exists() && serversDAT.isFile()) {
            if (Utils.copyFile(serversDAT, getTempDirectory())) {
                savedServersDat = true;
            }
        }

        File portalGunSounds = new File(getModsDirectory(), "PortalGunSounds.pak");
        if (portalGunSounds.exists() && portalGunSounds.isFile()) {
            savedPortalGunSounds = true;
            Utils.copyFile(portalGunSounds, getTempDirectory());
        }
    }

    private void restoreSelectFiles() {
        if (savedReis) {
            Utils.copyDirectory(new File(getTempDirectory(), "rei_minimap"),
                    new File(getModsDirectory(), "rei_minimap"));
        }

        if (savedZans) {
            Utils.copyDirectory(new File(getTempDirectory(), "VoxelMods"), new File(getModsDirectory(), "VoxelMods"));
        }

        if (savedNEICfg) {
            Utils.copyFile(new File(getTempDirectory(), "NEI.cfg"), new File(getConfigDirectory(), "NEI.cfg"), true);
        }

        if (savedOptionsTxt) {
            Utils.copyFile(new File(getTempDirectory(), "options.txt"), new File(getRootDirectory(), "options.txt"),
                    true);
        }

        if (savedServersDat) {
            Utils.copyFile(new File(getTempDirectory(), "servers.dat"), new File(getRootDirectory(), "servers.dat"),
                    true);
        }

        if (savedPortalGunSounds) {
            Utils.copyFile(new File(getTempDirectory(), "PortalGunSounds.pak"),
                    new File(getModsDirectory(), "PortalGunSounds.pak"), true);
        }
    }

    private void installServerBootScripts() throws Exception {
        if (!isServer) {
            return;
        }

        File batFile = new File(getRootDirectory(), "LaunchServer.bat");
        File shFile = new File(getRootDirectory(), "LaunchServer.sh");
        Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.bat"), batFile, "%%SERVERJAR%%",
                getServerJar());
        Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.sh"), shFile, "%%SERVERJAR%%",
                getServerJar());
        batFile.setExecutable(true);
        shFile.setExecutable(true);
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

    public void setTotalBytes(long bytes) {
        this.totalBytes = bytes;
        System.out.println("totalBytes: " + totalBytes);
        this.updateProgressBar();
    }

    public void addDownloadedBytes(long bytes) {
        this.downloadedBytes += bytes;
        System.out.println("downloadedBytes: " + downloadedBytes);
        this.updateProgressBar();
    }

    private void updateProgressBar() {
        double progress;
        if (this.totalBytes > 0) {
            System.out.println(this.downloadedBytes / this.totalBytes);
            progress = (this.downloadedBytes / this.totalBytes) * 100.0;
        } else {
            progress = 0.0;
        }
        System.out.println("progress: " + progress);
        double done = this.downloadedBytes / 1024.0 / 1024.0;
        double toDo = this.totalBytes / 1024.0 / 1024.0;
        if (done > toDo) {
            fireSubProgress(100.0, String.format("%.2f MB", done));
        } else {
            fireSubProgress(progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    private void hideSubProgressBar() {
        fireSubProgress(-1);
    }
}
