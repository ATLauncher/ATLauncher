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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.SwingWorker;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Constants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.InstanceV2Launcher;
import com.atlauncher.data.Server;
import com.atlauncher.data.Type;
import com.atlauncher.data.curse.CurseAttachment;
import com.atlauncher.data.curse.CurseFile;
import com.atlauncher.data.curse.CurseMod;
import com.atlauncher.data.curse.pack.CurseManifest;
import com.atlauncher.data.curse.pack.CurseModLoader;
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
import com.atlauncher.data.minecraft.loaders.forge.ATLauncherApiForgeVersion;
import com.atlauncher.data.minecraft.loaders.forge.ForgeLibrary;
import com.atlauncher.exceptions.LocalException;
import com.atlauncher.interfaces.NetworkProgressable;
import com.atlauncher.network.Analytics;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.utils.CurseApi;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.CaseFileVisitor;
import com.google.gson.reflect.TypeToken;

import org.mini2Dx.gettext.GetText;
import org.zeroturnaround.zip.ZipUtil;

import okhttp3.OkHttpClient;

public class InstanceInstaller extends SwingWorker<Boolean, Void> implements NetworkProgressable {
    protected double percent = 0.0; // Percent done installing
    protected double subPercent = 0.0; // Percent done sub installing
    protected double totalBytes = 0; // Total number of bytes to download
    protected double downloadedBytes = 0; // Total number of bytes downloaded

    public Instance instance = null;
    public InstanceV2 instanceV2 = null;
    public final String name;
    public final com.atlauncher.data.Pack pack;
    public final com.atlauncher.data.PackVersion version;
    public final String shareCode;
    public final boolean showModsChooser;
    public LoaderVersion loaderVersion;
    public final CurseManifest curseManifest;
    public final File manifestFile;

    public boolean isReinstall;
    public boolean isServer;
    public boolean instanceIsCorrupt;
    public boolean saveMods = false;

    public final Path root;
    public final Path temp;

    public Loader loader;
    public com.atlauncher.data.json.Version packVersion;
    public MinecraftVersion minecraftVersion;

    public List<Mod> allMods;
    public List<Mod> selectedMods;
    public List<Mod> unselectedMods = new ArrayList<>();
    public List<DisableableMod> modsInstalled;

    public boolean assetsMapToResources = false;

    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedOptionsTxt = false; // If options.txt was found and saved
    private boolean savedServersDat = false; // If servers.dat was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved

    public String mainClass;
    public Arguments arguments;

    public InstanceInstaller(String name, com.atlauncher.data.Pack pack, com.atlauncher.data.PackVersion version,
            boolean isReinstall, boolean isServer, boolean saveMods, String shareCode, boolean showModsChooser,
            LoaderVersion loaderVersion, CurseManifest curseManifest, File manifestFile) {
        this.name = name;
        this.pack = pack;
        this.version = version;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        this.saveMods = saveMods;
        this.shareCode = shareCode;
        this.showModsChooser = showModsChooser;

        if (isServer) {
            this.root = FileSystem.SERVERS.resolve(name.replaceAll("[^A-Za-z0-9]", ""));
        } else {
            this.root = FileSystem.INSTANCES.resolve(name.replaceAll("[^A-Za-z0-9]", ""));
        }

        this.temp = FileSystem.TEMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion());

        this.loaderVersion = loaderVersion;
        this.curseManifest = curseManifest;
        this.manifestFile = manifestFile;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public void setInstance(InstanceV2 instanceV2) {
        this.instanceV2 = instanceV2;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        ErrorReporting.recordPackInstall(this.pack.name, this.version.version, this.loaderVersion);
        LogManager.info("Started install of " + this.pack.name + " version " + this.version.version);

        if (this.loaderVersion != null) {
            LogManager.info("Using loader version " + this.loaderVersion.version);
        }

        try {
            if (curseManifest == null) {
                downloadPackVersionJson();
            } else {
                generatePackVersionFromCurse();
            }

            downloadMinecraftVersionJson();

            if (this.packVersion.messages != null) {
                showMessages();
            }

            determineModsToBeInstalled();

            backupSelectFiles();
            addPercent(5);

            prepareFilesystem();

            if (this.packVersion.loader != null) {
                this.loader = this.packVersion.getLoader().getLoader(this.temp.resolve("loader").toFile(), this,
                        this.loaderVersion);

                if (this.loaderVersion == null) {
                    this.loaderVersion = this.loader.getLoaderVersion();
                }

                downloadLoader();
            }
            install();

            if (!this.isServer) {
                saveInstanceJson();
            } else {
                saveServerJson();
            }

            Analytics.sendEvent(pack.name + " - " + version.version,
                    (this.isServer ? "Server" : "") + (this.isReinstall ? "Reinstalled" : "Installed"),
                    (this.curseManifest == null ? "Pack" : "CursePack"));

            return true;
        } catch (Exception e) {
            cancel(true);
            LogManager.logStackTrace(e);
        }

        return false;
    }

    private void downloadPackVersionJson() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Downloading Pack Version Definition"));
        fireSubProgressUnknown();

        this.packVersion = com.atlauncher.network.Download.build().cached()
                .setUrl(this.pack.getJsonDownloadUrl(version.version)).asClass(com.atlauncher.data.json.Version.class);

        if (this.packVersion == null) {
            throw new Exception("Failed to download pack version definition");
        }

        this.packVersion.compileColours();

        hideSubProgressBar();
    }

    private void generatePackVersionFromCurse() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Generating Pack Version Definition From Curse"));
        fireSubProgressUnknown();

        if (!curseManifest.manifestType.equals("minecraftModpack")) {
            throw new Exception("Manifest is not a Minecraft Modpack");
        }

        if (curseManifest.manifestVersion != 1) {
            throw new Exception("Manifest is version " + curseManifest.manifestVersion + " which I cannot install");
        }

        this.packVersion = new Version();
        packVersion.version = curseManifest.version;
        packVersion.minecraft = curseManifest.minecraft.version;
        packVersion.enableCurseIntegration = true;
        packVersion.enableEditingMods = true;

        packVersion.loader = new com.atlauncher.data.json.Loader();

        CurseModLoader forgeVersion = curseManifest.minecraft.modLoaders.stream().filter(e -> e.primary).findFirst()
                .orElse(null);

        if (forgeVersion == null) {
            throw new Exception("Failed to find Forge version");
        }

        String forgeVersionString = forgeVersion.id.replace("forge-", "");

        java.lang.reflect.Type type = new TypeToken<APIResponse<ATLauncherApiForgeVersion>>() {
        }.getType();

        APIResponse<ATLauncherApiForgeVersion> forgeVersionInfo = com.atlauncher.network.Download.build()
                .setUrl(String.format("%sforge-version/%s", Constants.API_BASE_URL, forgeVersionString)).asType(type);

        Map<String, Object> loaderMeta = new HashMap<>();
        loaderMeta.put("minecraft", curseManifest.minecraft.version);
        loaderMeta.put("version", forgeVersionInfo.getData().version);
        loaderMeta.put("rawVersion", forgeVersionInfo.getData().raw_version);
        packVersion.loader.metadata = loaderMeta;

        if (Utils.matchVersion(curseManifest.minecraft.version, "1.13", false, true)) {
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.Forge113Loader";
        } else {
            packVersion.loader.className = "com.atlauncher.data.minecraft.loaders.forge.ForgeLoader";
        }

        packVersion.mods = curseManifest.files.parallelStream().map(file -> {
            CurseMod curseMod = CurseApi.getModById(file.projectID);
            CurseFile curseFile = CurseApi.getFileForMod(file.projectID, file.fileID);

            Mod mod = curseFile.convertToMod(curseMod);
            mod.optional = !file.required;

            return mod;
        }).collect(Collectors.toList());

        hideSubProgressBar();
    }

    private void downloadMinecraftVersionJson() throws Exception {
        addPercent(5);
        fireTask(GetText.tr("Downloading Minecraft Definition"));
        fireSubProgressUnknown();

        VersionManifest versionManifest = com.atlauncher.network.Download.build().cached()
                .setUrl(String.format("%s/mc/game/version_manifest.json", Constants.LAUNCHER_META_MINECRAFT))
                .asClass(VersionManifest.class);

        VersionManifestVersion minecraftVersion = versionManifest.versions.stream()
                .filter(version -> version.id.equalsIgnoreCase(this.packVersion.getMinecraft())).findFirst()
                .orElse(null);

        if (minecraftVersion == null) {
            throw new Exception(
                    String.format("Failed to find Minecraft version of %s", this.packVersion.getMinecraft()));
        }

        this.minecraftVersion = com.atlauncher.network.Download.build().cached().setUrl(minecraftVersion.url)
                .downloadTo(this.temp.resolve("minecraft.json")).asClass(MinecraftVersion.class);

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
        this.allMods = sortMods(
                (this.isServer ? this.packVersion.getServerInstallMods() : this.packVersion.getClientInstallMods()));

        boolean hasOptional = this.allMods.stream().anyMatch(Mod::isOptional);

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

            if (mod.type == ModType.mods
                    && this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (mod.type == ModType.mods
                    && this.packVersion.getCaseAllFiles() == com.atlauncher.data.json.CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }

            this.modsInstalled
                    .add(new com.atlauncher.data.DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                            com.atlauncher.data.Type.valueOf(com.atlauncher.data.Type.class, mod.getType().toString()),
                            this.packVersion.getColour(mod.getColour()), mod.getDescription(), false, false, true,
                            mod.getCurseModId(), mod.getCurseFileId()));
        }

        if (this.isReinstall && (instanceV2 != null ? instanceV2.hasCustomMods() : instance.hasCustomMods())) {
            // user chose to save mods even though Minecraft version changed, so add them to
            // the installed list
            if (this.saveMods || (instanceV2 != null ? instanceV2.id : instance.getMinecraftVersion())
                    .equalsIgnoreCase(version.minecraftVersion.version)) {
                for (com.atlauncher.data.DisableableMod mod : (instanceV2 != null
                        ? instanceV2.getCustomDisableableMods()
                        : instance.getCustomDisableableMods())) {
                    modsInstalled.add(mod);
                }
            }

            // user choosing to not save mods and Minecraft version changed, so delete
            // custom mods
            if (!this.saveMods && !(instanceV2 != null ? instanceV2.id : instance.getMinecraftVersion())
                    .equalsIgnoreCase(version.minecraftVersion.version)) {
                for (com.atlauncher.data.DisableableMod mod : (instanceV2 != null
                        ? instanceV2.getCustomDisableableMods()
                        : instance.getCustomDisableableMods())) {
                    if (this.instanceV2 != null) {
                        this.instanceV2.launcher.mods.remove(mod);
                        Utils.delete((mod.isDisabled() ? mod.getDisabledFile(this.instanceV2)
                                : mod.getFile(this.instanceV2)));
                    } else {
                        instance.removeInstalledMod(mod);
                    }
                }
            }
        }
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

        downloadInstanceImage();
        if (isCancelled()) {
            return false;
        }

        // Copy over common configs if any
        if (FileSystem.COMMON.toFile().listFiles().length != 0) {
            Utils.copyDirectory(FileSystem.COMMON.toFile(), this.root.toFile());
        }

        restoreSelectFiles();

        installServerBootScripts();

        return true;
    }

    private void saveInstanceJson() {
        InstanceV2 instance = new InstanceV2(this.minecraftVersion);
        InstanceV2Launcher instanceLauncher;

        if (!this.isReinstall || this.instance != null) {
            instanceLauncher = new InstanceV2Launcher();
        } else {
            instanceLauncher = this.instanceV2.launcher;
        }

        instance.libraries = this.getLibraries();
        instance.mainClass = this.mainClass;
        instance.arguments = this.arguments;

        instanceLauncher.name = this.name;
        instanceLauncher.pack = this.pack.name;
        instanceLauncher.description = this.pack.description;
        instanceLauncher.packId = this.pack.id;
        instanceLauncher.version = this.packVersion.version;
        instanceLauncher.java = this.packVersion.java;
        instanceLauncher.enableCurseIntegration = this.packVersion.enableCurseIntegration;
        instanceLauncher.enableEditingMods = this.packVersion.enableEditingMods;
        instanceLauncher.loaderVersion = this.loaderVersion;
        instanceLauncher.isDev = this.version.isDev;
        instanceLauncher.isPlayable = true;
        instanceLauncher.mods = this.modsInstalled;
        instanceLauncher.requiredMemory = this.packVersion.memory;
        instanceLauncher.requiredPermGen = this.packVersion.permGen;
        instanceLauncher.assetsMapToResources = this.assetsMapToResources;
        instanceLauncher.curseManifest = curseManifest;

        if (this.version.isDev) {
            instanceLauncher.hash = this.version.hash;
        }

        instance.launcher = instanceLauncher;

        instance.save();

        if (this.instanceV2 != null) {
            App.settings.instancesV2.remove(this.instanceV2);
        }

        App.settings.instancesV2.add(instance);

        if (this.instance != null) {
            App.settings.instances.remove(this.instance);
        }

        App.settings.reloadInstancesPanel();
    }

    private void saveServerJson() {
        Server server = new Server();

        server.name = this.name;
        server.pack = this.pack.name;
        server.packId = this.pack.id;
        server.version = this.packVersion.version;
        server.isDev = this.version.isDev;
        server.mods = this.modsInstalled;

        if (this.version.isDev) {
            server.hash = this.version.hash;
        }

        server.save();

        App.settings.servers.add(server);

        App.settings.reloadServersPanel();
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
                if (loaderArguments.game != null && loaderArguments.game.size() != 0) {
                    this.arguments.game.addAll(loaderArguments.game);
                }

                if (loaderArguments.jvm != null && loaderArguments.jvm.size() != 0) {
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
                        .map(ArgumentRule::new).collect(Collectors.toList()));
            }
        }
    }

    private void addMinecraftArguments() {
        // older MC versions
        if (this.minecraftVersion.minecraftArguments != null) {
            this.arguments.game.addAll(Arrays.asList(this.minecraftVersion.minecraftArguments.split(" ")).stream()
                    .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList()));
        }

        // newer MC versions
        if (this.minecraftVersion.arguments != null) {
            if (this.minecraftVersion.arguments.game != null && this.minecraftVersion.arguments.game.size() != 0) {
                this.arguments.game.addAll(this.minecraftVersion.arguments.game);
            }

            if (this.minecraftVersion.arguments.jvm != null && this.minecraftVersion.arguments.jvm.size() != 0) {
                this.arguments.jvm.addAll(this.minecraftVersion.arguments.jvm);
            }
        }
    }

    protected void downloadResources() throws Exception {
        addPercent(5);

        if (this.isServer || this.minecraftVersion.assetIndex == null) {
            return;
        }

        fireTask(GetText.tr("Downloading Resources"));
        fireSubProgressUnknown();
        this.totalBytes = this.downloadedBytes = 0;

        MojangAssetIndex assetIndex = this.minecraftVersion.assetIndex;

        AssetIndex index = com.atlauncher.network.Download.build().cached().setUrl(assetIndex.url).hash(assetIndex.sha1)
                .size(assetIndex.size).downloadTo(FileSystem.RESOURCES_INDEXES.resolve(assetIndex.id + ".json"))
                .asClass(AssetIndex.class);

        if (index.mapToResources) {
            this.assetsMapToResources = true;
        }

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        index.objects.entrySet().stream().forEach(entry -> {
            AssetObject object = entry.getValue();
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);

            com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(url)
                    .downloadTo(FileSystem.RESOURCES_OBJECTS.resolve(filename)).hash(object.hash).size(object.size)
                    .withInstanceInstaller(this).withHttpClient(httpClient).withFriendlyFileName(entry.getKey());

            if (index.mapToResources) {
                download = download
                        .copyTo(new File(new File(this.root.toFile(), "resources"), entry.getKey()).toPath());
            } else if (assetIndex.id.equalsIgnoreCase("legacy")) {
                download = download.copyTo(FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(entry.getKey()));
            }

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        this.setTotalBytes(smallPool.totalSize());
        this.fireSubProgress(0);

        smallPool.downloadAll();

        hideSubProgressBar();
    }

    private void downloadMinecraft() throws Exception {
        addPercent(5);
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

        com.atlauncher.network.Download.build().cached().setUrl(loggingFile.url).hash(loggingFile.sha1)
                .size(loggingFile.size).downloadTo(FileSystem.RESOURCES_LOG_CONFIGS.resolve(loggingFile.id))
                .withInstanceInstaller(this).withHttpClient(Network.createProgressClient(this)).downloadFile();

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
        if (this.loader == null || this.loader.useMinecraftLibraries()) {
            libraries.addAll(this.minecraftVersion.libraries);
        }

        return libraries;
    }

    public String getMinecraftJarLibraryPath() {
        return getMinecraftJarLibraryPath(isServer ? "server" : "client");
    }

    public String getMinecraftJarLibraryPath(String type) {
        return "net/minecraft/" + type + "/" + this.minecraftVersion.id + "/" + type + "-" + this.minecraftVersion.id
                + ".jar".replace("/", File.separatorChar + "");
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
            this.loader.getInstallLibraries().stream().filter(library -> library.downloads.artifact != null).forEach(
                    library -> pool.add(new com.atlauncher.network.Download().setUrl(library.downloads.artifact.url)
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

            if (this.loader != null) {
                Library forgeLibrary = this.loader.getLibraries().stream()
                        .filter(library -> library.name.startsWith("net.minecraftforge:forge")).findFirst()
                        .orElse(null);

                if (forgeLibrary != null) {
                    File extractedLibraryFile = FileSystem.LIBRARIES.resolve(forgeLibrary.downloads.artifact.path)
                            .toFile();
                    Utils.copyFile(extractedLibraryFile, new File(this.root.toFile(), this.loader.getServerJar()),
                            true);
                }
            }
        }

        hideSubProgressBar();
    }

    private void installLoader() {
        addPercent(5);

        if (this.loader == null) {
            return;
        }

        fireTask(GetText.tr("Installing Loader"));
        fireSubProgressUnknown();

        // run any processors that the loader needs
        this.loader.runProcessors();

        hideSubProgressBar();
    }

    private void downloadMods() {
        addPercent(25);

        if (selectedMods.size() == 0) {
            return;
        }

        fireTask(GetText.tr("Downloading Mods"));
        fireSubProgressUnknown();

        OkHttpClient httpClient = Network.createProgressClient(this);
        DownloadPool pool = new DownloadPool();

        this.selectedMods.stream().filter(mod -> mod.download != DownloadType.browser).forEach(mod -> {
            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                    .setUrl(mod.getDownloadUrl()).downloadTo(FileSystem.DOWNLOADS.resolve(mod.getFile())).hash(mod.md5)
                    .size(mod.filesize).withInstanceInstaller(this).withHttpClient(httpClient);

            if (mod.fingerprint != null) {
                download = download.fingerprint(mod.fingerprint);
            }

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        this.setTotalBytes(smallPool.totalSize());
        this.fireSubProgress(0);

        smallPool.downloadAll();

        fireSubProgressUnknown();

        this.selectedMods.stream().filter(mod -> mod.download == DownloadType.browser)
                .forEach(mod -> mod.download(this));

        hideSubProgressBar();
    }

    private void installMods() {
        addPercent(25);

        if (this.selectedMods.size() == 0) {
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

    private void installLegacyJavaFixer() {
        addPercent(5);

        if (this.allMods.size() == 0 || !Utils.matchVersion(minecraftVersion.id, "1.6", true, true)) {
            return;
        }

        fireTask(GetText.tr("Installing Legacy Java Fixer"));
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

        if (curseManifest == null) {
            fireTask(GetText.tr("Downloading Configs"));

            File configs = this.temp.resolve("Configs.zip").toFile();
            String path = "packs/" + pack.getSafeName() + "/versions/" + version.version + "/Configs.zip";

            com.atlauncher.network.Download configsDownload = com.atlauncher.network.Download.build()
                    .setUrl(String.format("%s/%s", Constants.DOWNLOAD_SERVER, path)).downloadTo(configs.toPath())
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

            ZipUtil.unpack(configs, this.root.toFile());
            Utils.delete(configs);
        } else {
            fireSubProgressUnknown();
            fireTask(GetText.tr("Copying Overrides"));
            ZipUtil.unpack(manifestFile, this.temp.resolve("manifest").toFile());
            Utils.copyDirectory(
                    this.temp.resolve("manifest/" + Optional.of(curseManifest.overrides).orElse("overrides")).toFile(),
                    this.root.toFile(), false);
        }
    }

    private void downloadInstanceImage() throws Exception {
        addPercent(5);

        if (curseManifest == null || this.pack.cursePack == null) {
            return;
        }

        fireTask(GetText.tr("Downloading Instance Image"));

        CurseAttachment attachment = this.pack.cursePack.attachments.stream().filter(a -> a.isDefault).findFirst()
                .orElse(null);

        if (attachment != null) {
            com.atlauncher.network.Download imageDownload = com.atlauncher.network.Download.build()
                    .setUrl(attachment.url).downloadTo(root.resolve("instance.png")).withInstanceInstaller(this)
                    .withHttpClient(Network.createProgressClient(this));

            this.setTotalBytes(imageDownload.getFilesize());
            imageDownload.downloadFile();
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

            if (Files.isDirectory(this.root.resolve("config"))) {
                FileUtils.deleteDirectory(this.root.resolve("config"));
            }

            if (isReinstall) {
                if (Files.isDirectory(this.root.resolve("mods"))) {
                    Utils.deleteWithFilter(this.root.resolve("mods").toFile(),
                            (instanceV2 != null ? instanceV2.getPackMods(com.atlauncher.data.Type.mods)
                                    : instance.getPackMods(com.atlauncher.data.Type.mods)),
                            true);
                }

                if (Files.isDirectory(this.root.resolve("coremods"))) {
                    Utils.deleteWithFilter(this.root.resolve("coremods").toFile(),
                            (instanceV2 != null ? instanceV2.getPackMods(com.atlauncher.data.Type.coremods)
                                    : instance.getPackMods(com.atlauncher.data.Type.coremods)),
                            true);
                }

                if (Files.isDirectory(this.root.resolve("jarmods"))) {
                    Utils.deleteWithFilter(this.root.resolve("jarmods").toFile(),
                            (instanceV2 != null ? instanceV2.getPackMods(com.atlauncher.data.Type.jar)
                                    : instance.getPackMods(com.atlauncher.data.Type.jar)),
                            true);

                    Utils.deleteWithFilter(this.root.resolve("jarmods").toFile(),
                            (instanceV2 != null ? instanceV2.getPackMods(com.atlauncher.data.Type.forge)
                                    : instance.getPackMods(com.atlauncher.data.Type.forge)),
                            true);
                }
            } else {
                FileUtils.deleteDirectory(this.root.resolve("mods"));

                if (this.version.minecraftVersion.coremods && Files.isDirectory(this.root.resolve("coremods"))) {
                    FileUtils.deleteDirectory(this.root.resolve("coremods"));
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
                FileUtils.deleteDirectory(this.root.resolve("libraries"));
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

        if (this.version.minecraftVersion.coremods) {
            FileUtils.createDirectory(this.root.resolve("coremods"));
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
        if (!isServer) {
            return;
        }

        File batFile = new File(this.root.toFile(), "LaunchServer.bat");
        File shFile = new File(this.root.toFile(), "LaunchServer.sh");
        File commandFile = new File(this.root.toFile(), "LaunchServer.command");
        Utils.replaceText(App.class.getResourceAsStream("/server-scripts/LaunchServer.bat"), batFile, "%%SERVERJAR%%",
                getServerJar());
        Utils.replaceText(App.class.getResourceAsStream("/server-scripts/LaunchServer.sh"), shFile, "%%SERVERJAR%%",
                getServerJar());
        Utils.replaceText(App.class.getResourceAsStream("/server-scripts/LaunchServer.command"), commandFile,
                "%%SERVERJAR%%", getServerJar());
        batFile.setExecutable(true);
        shFile.setExecutable(true);
        commandFile.setExecutable(true);
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
            return "minecraft_server." + this.version.minecraftVersion.version + ".jar";
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

    public List<Mod> getModsDependancies(Mod mod) {
        List<Mod> dependsMods = new ArrayList<>();
        for (String name : mod.getDepends()) {
            inner: {
                for (Mod modd : allMods) {
                    if (modd.getName().equalsIgnoreCase(name)) {
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
        return instance != null
                && (instanceV2 != null ? instanceV2.wasModInstalled(mod) : instance.wasModInstalled(mod));
    }

    public boolean wasModSelected(String mod) {
        return instance != null && (instanceV2 != null ? instanceV2.wasModSelected(mod) : instance.wasModSelected(mod));
    }

    public String getShareCodeData(String code) {
        String shareCodeData = null;

        try {
            java.lang.reflect.Type type = new TypeToken<APIResponse<String>>() {
            }.getType();
            APIResponse<String> response = Gsons.DEFAULT.fromJson(Utils.sendGetAPICall(
                    "pack/" + this.pack.getSafeName() + "/" + version.version + "/share-code/" + code), type);

            if (!response.wasError()) {
                shareCodeData = response.getData();
            }
        } catch (IOException e) {
            LogManager.logStackTrace("API call failed", e);
        }

        return shareCodeData;
    }

    public void fireTask(String name) {
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
            fireSubProgress(100.0, String.format("%.2f MB", done));
        } else {
            fireSubProgress(progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    private void hideSubProgressBar() {
        fireSubProgress(-1);
    }
}
