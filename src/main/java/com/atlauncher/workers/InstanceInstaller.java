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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import javax.swing.SwingWorker;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.Constants;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.Type;
import com.atlauncher.data.json.Action;
import com.atlauncher.data.json.CaseType;
import com.atlauncher.data.json.Delete;
import com.atlauncher.data.json.Deletes;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.loaders.LoaderVersion;
import com.atlauncher.data.mojang.ArgumentRule;
import com.atlauncher.data.mojang.AssetIndex;
import com.atlauncher.data.mojang.AssetObject;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.DownloadsItem;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.atlauncher.data.mojang.Library;
import com.atlauncher.data.mojang.LoggingFile;
import com.atlauncher.data.mojang.MojangAssetIndex;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.data.mojang.MojangDownload;
import com.atlauncher.data.mojang.MojangDownloads;
import com.atlauncher.data.mojang.MojangVersion;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {

    private final Gson gson; // GSON Parser
    private final String shareCode;
    private final boolean showModsChooser;
    private String instanceName;
    private Pack pack;
    private Version jsonVersion;
    private PackVersion version;
    private LoaderVersion loaderVersion;
    private boolean isReinstall;
    private boolean isServer;
    private String jarOrder;
    private boolean instanceIsCorrupt = false; // If the instance should be set as corrupt
    private boolean savedReis = false; // If Reis Minimap stuff was found and saved
    private boolean savedZans = false; // If Zans Minimap stuff was found and saved
    private boolean savedNEICfg = false; // If NEI Config was found and saved
    private boolean savedOptionsTxt = false; // If options.txt was found and saved
    private boolean savedServersDat = false; // If servers.dat was found and saved
    private boolean savedPortalGunSounds = false; // If Portal Gun Sounds was found and saved
    private boolean extractedTexturePack = false; // If there is an extracted texturepack
    private boolean extractedResourcePack = false; // If there is an extracted resourcepack
    private boolean assetsMapToResources = false;
    private int permgen = 0;
    private int memory = 0;
    private List<String> libraries = new ArrayList<>();
    private List<String> arguments = new ArrayList<>();
    private String extraArguments = null;
    private String mainClass = null;
    private int percent = 0; // Percent done installing
    private List<Mod> allMods;
    private List<Mod> selectedMods;
    private List<Mod> unselectedMods = new ArrayList<>();
    private int totalDownloads = 0; // Total number of downloads to download
    private int doneDownloads = 0; // Total number of downloads downloaded
    private int totalBytes = 0; // Total number of bytes to download
    private int downloadedBytes = 0; // Total number of bytes downloaded
    private Instance instance = null;
    private List<DisableableMod> modsInstalled;
    private List<File> serverLibraries;
    private List<File> forgeLibraries = new ArrayList<>();
    private com.atlauncher.data.loaders.Loader loader;

    public InstanceInstaller(String instanceName, Pack pack, PackVersion version, boolean isReinstall, boolean isServer,
            String shareCode, boolean showModsChooser, LoaderVersion loaderVersion) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        this.shareCode = shareCode;
        this.showModsChooser = showModsChooser;
        this.loaderVersion = loaderVersion;
        if (isServer) {
            serverLibraries = new ArrayList<>();
        }
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
        builder.registerTypeAdapter(File.class, new FileTypeAdapter());
        builder.setPrettyPrinting();
        this.gson = builder.create();
    }

    public Pack getPack() {
        return this.pack;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public boolean isServer() {
        return this.isServer;
    }

    public String getInstanceName() {
        return this.instanceName;
    }

    public List<DisableableMod> getModsInstalled() {
        return this.modsInstalled;
    }

    public String getInstanceSafeName() {
        return this.instanceName.replaceAll("[^A-Za-z0-9]", "");
    }

    public File getRootDirectory() {
        if (isServer) {
            return new File(App.settings.getServersDir(), pack.getSafeName() + "_" + version.getSafeVersion());
        }
        return new File(App.settings.getInstancesDir(), getInstanceSafeName());
    }

    public File getTempDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_" + version.getSafeVersion());
    }

    public File getTempJarDirectory() {
        return new File(App.settings.getTempDir(), pack.getSafeName() + "_" + version.getSafeVersion() + "_JarTemp");
    }

    public File getTempActionsDirectory() {
        return new File(App.settings.getTempDir(),
                pack.getSafeName() + "_" + version.getSafeVersion() + "_ActionsTemp");
    }

    public File getTempTexturePackDirectory() {
        return new File(App.settings.getTempDir(),
                pack.getSafeName() + "_" + version.getSafeVersion() + "_TexturePackTemp");
    }

    public File getTempResourcePackDirectory() {
        return new File(App.settings.getTempDir(),
                pack.getSafeName() + "_" + version.getSafeVersion() + "_ResourcePackTemp");
    }

    public File getLibrariesDirectory() {
        return new File(getRootDirectory(), "libraries");
    }

    public File getTexturePacksDirectory() {
        return new File(getRootDirectory(), "texturepacks");
    }

    public File getShaderPacksDirectory() {
        return new File(getRootDirectory(), "shaderpacks");
    }

    public File getResourcePacksDirectory() {
        return new File(getRootDirectory(), "resourcepacks");
    }

    public File getConfigDirectory() {
        return new File(getRootDirectory(), "config");
    }

    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    public File getIC2LibDirectory() {
        return new File(getModsDirectory(), "ic2");
    }

    public File getDenLibDirectory() {
        return new File(getModsDirectory(), "denlib");
    }

    public File getFlanDirectory() {
        return new File(getRootDirectory(), "Flan");
    }

    public File getDependencyDirectory() {
        return new File(getModsDirectory(), this.version.getMinecraftVersion().getVersion());
    }

    public File getPluginsDirectory() {
        return new File(getRootDirectory(), "plugins");
    }

    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    public File getDisabledModsDirectory() {
        return new File(getRootDirectory(), "disabledmods");
    }

    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public boolean hasActions() {
        return this.jsonVersion.hasActions();
    }

    public PackVersion getVersion() {
        return this.version;
    }

    public File getMinecraftJar() {
        if (isServer) {
            return new File(getRootDirectory(),
                    "minecraft_server." + this.version.getMinecraftVersion().getVersion() + ".jar");
        }
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public File getMinecraftJarLibrary() {
        return getMinecraftJarLibrary(isServer ? "server" : "client");
    }

    public File getMinecraftJarLibrary(String type) {
        return new File(App.settings.getGameLibrariesDir(), getMinecraftJarLibraryPath(type));
    }

    public String getMinecraftJarLibraryPath() {
        return getMinecraftJarLibraryPath(isServer ? "server" : "client");
    }

    public String getMinecraftJarLibraryPath(String type) {
        return "net/minecraft/" + type + "/" + this.version.getMinecraftVersion().getVersion() + "/" + type + "-"
                + this.version.getMinecraftVersion().getVersion() + ".jar".replace("/", File.separatorChar + "");
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    public void setTexturePackExtracted() {
        this.extractedTexturePack = true;
    }

    public void setResourcePackExtracted() {
        this.extractedResourcePack = true;
    }

    public void addToJarOrder(String file) {
        if (jarOrder == null) {
            jarOrder = file;
        } else {
            jarOrder = jarOrder + "," + file;
        }
    }

    public boolean wasModInstalled(String mod) {
        return instance != null && instance.wasModInstalled(mod);
    }

    public boolean wasModSelected(String mod) {
        return instance != null && instance.wasModSelected(mod);
    }

    public boolean isReinstall() {
        return this.isReinstall;
    }

    public boolean doAssetsMapToResources() {
        return this.assetsMapToResources;
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

    private void makeDirectories() {
        if (isReinstall || isServer) {
            // We're reinstalling or installing a server so delete these folders
            Utils.delete(getBinDirectory());
            Utils.delete(getConfigDirectory());
            if (instance != null
                    && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion().getVersion())
                    && instance.hasCustomMods()) {
                Utils.deleteWithFilter(getModsDirectory(), instance.getCustomMods(Type.mods));
                if (this.version.getMinecraftVersion().usesCoreMods()) {
                    Utils.deleteWithFilter(getCoreModsDirectory(), instance.getCustomMods(Type.coremods));
                }
                if (isReinstall) {
                    Utils.deleteWithFilter(getJarModsDirectory(), instance.getCustomMods(Type.jar));
                }
            } else {
                Utils.delete(getModsDirectory());
                if (this.version.getMinecraftVersion().usesCoreMods()) {
                    Utils.delete(getCoreModsDirectory());
                }
                if (isReinstall) {
                    Utils.delete(getJarModsDirectory()); // Only delete if it's not a server
                }
            }
            if (isReinstall) {
                Utils.delete(new File(getTexturePacksDirectory(), "TexturePack.zip"));
                Utils.delete(new File(getResourcePacksDirectory(), "ResourcePack.zip"));
            } else {
                Utils.delete(getLibrariesDirectory()); // Only delete if it's a server
            }
            if (this.instance != null && this.jsonVersion.hasDeletes()) {
                Deletes deletes = this.jsonVersion.getDeletes();

                if (deletes.hasFileDeletes()) {
                    for (Delete delete : deletes.getFiles()) {
                        if (delete.isAllowed()) {
                            File file = delete.getFile(this.instance.getRootDirectory());
                            if (file.exists()) {
                                Utils.delete(file);
                            }
                        }
                    }
                }

                if (deletes.hasFolderDeletes()) {
                    for (Delete delete : deletes.getFolders()) {
                        if (delete.isAllowed()) {
                            File file = delete.getFile(this.instance.getRootDirectory());
                            if (file.exists()) {
                                Utils.delete(file);
                            }
                        }
                    }
                }
            }
        }
        File[] directories;
        if (isServer) {
            directories = new File[] { getRootDirectory(), getModsDirectory(), getTempDirectory(),
                    getLibrariesDirectory() };
        } else {
            directories = new File[] { getRootDirectory(), getModsDirectory(), getDisabledModsDirectory(),
                    getTempDirectory(), getJarModsDirectory(), getBinDirectory(), getNativesDirectory() };
        }
        for (File directory : directories) {
            directory.mkdir();
        }
        if (this.version.getMinecraftVersion().usesCoreMods()) {
            getCoreModsDirectory().mkdir();
        }
    }

    private List<Downloadable> getDownloadableMods() {
        List<Downloadable> mods = new ArrayList<>();

        for (Mod mod : this.selectedMods) {
            if (mod.getDownload() == DownloadType.server) {
                Downloadable downloadable;

                downloadable = new Downloadable(mod.getUrl(), new File(App.settings.getDownloadsDir(), mod.getFile()),
                        mod.getMD5(), mod.getFilesize(), this, true);

                mods.add(downloadable);
            }
        }

        if (Utils.matchVersion(this.getVersion().getMinecraftVersion().getVersion(), "1.6", true, true)) {
            mods.add(new Downloadable(Constants.LEGACY_JAVA_FIXER_URL,
                    new File(App.settings.getDownloadsDir(), "legacyjavafixer-1.0.jar"),
                    Constants.LEGACY_JAVA_FIXER_MD5, -1, this, false));
        }

        return mods;
    }

    private void doActions() {
        for (Action action : this.jsonVersion.getActions()) {
            action.execute(this);
        }
    }

    private void installMods() {
        for (Mod mod : this.selectedMods) {
            if (!isCancelled()) {
                fireTask(Language.INSTANCE.localize("common.installing") + " " + mod.getName());
                addPercent(this.selectedMods.size() / 40);
                mod.install(this);
            }
        }

        if (Utils.matchVersion(this.getVersion().getMinecraftVersion().getVersion(), "1.6", true, true)) {
            Utils.copyFile(new File(App.settings.getDownloadsDir(), "legacyjavafixer-1.0.jar"),
                    this.getModsDirectory());
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

    private void downloadResources() {
        fireTask(Language.INSTANCE.localize("instance.downloadingresources"));
        fireSubProgressUnknown();
        ExecutorService executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());
        ArrayList<Downloadable> downloads = getResources();
        totalBytes = 0;
        downloadedBytes = 0;

        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                totalBytes += download.getFilesize();
            }
        }

        fireSubProgress(0); // Show the subprogress bar
        for (final Downloadable download : downloads) {
            executor.execute(() -> {
                if (download.needToDownload()) {
                    fireTask(Language.INSTANCE.localize("common.downloading") + " " + download.getFilename());
                    download.download(true);
                } else {
                    download.copyFile();
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadLoader() {
        fireTask(Language.INSTANCE.localize("instance.downloadingloader"));
        fireSubProgressUnknown();
        totalBytes = 0;
        downloadedBytes = 0;

        this.loader.downloadAndExtractInstaller();

        List<Downloadable> downloads = this.loader.getDownloadableLibraries();

        fireTask(Language.INSTANCE.localize("instance.downloadingloaderlibraries"));

        ExecutorService executor;
        totalBytes = 0;
        downloadedBytes = 0;

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

        fireSubProgress(-1); // Hide the subprogress bar
    }

    public void installLoader() {
        fireTask(Language.INSTANCE.localize("instance.installingloader"));
        fireSubProgressUnknown();

        // run any processors that the loader needs
        this.loader.runProcessors();

        // add the libraries for the loader
        this.libraries.addAll(this.loader.getLibraries());
        for (String library : this.libraries) {
            LogManager.debug(library);
        }

        // add the arguments for the loader
        this.arguments.addAll(this.loader.getArguments());

        // add the arguments for Minecraft
        MojangVersion mojangVersion = this.version.getMinecraftVersion().getMojangVersion();

        if (this.loader.useMinecraftArguments()) {
            if (mojangVersion.hasArguments()) {
                for (ArgumentRule argument : mojangVersion.getArguments().getGame()) {
                    if (argument.applies()) {
                        this.arguments.add(argument.getValue());
                    }
                }
            } else {
                for (String argument : mojangVersion.getMinecraftArguments().split(" ")) {
                    // make sure not to duplicate any since Forge copies what Minecraft include
                    if (!this.arguments.contains(argument)) {
                        this.arguments.add(argument);
                    }
                }
            }
        }

        // add the mainclass for the loader
        this.mainClass = this.loader.getMainClass();

        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadLibraries() {
        fireTask(Language.INSTANCE.localize("instance.downloadinglibraries"));
        fireSubProgressUnknown();
        ExecutorService executor;
        ArrayList<Downloadable> downloads = getLibraries();
        totalBytes = 0;
        downloadedBytes = 0;

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
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadMods(List<Mod> mods) {
        fireSubProgressUnknown();
        ExecutorService executor;
        List<Downloadable> downloads = getDownloadableMods();
        totalBytes = 0;
        downloadedBytes = 0;

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
                    download.download(true);
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(-1); // Hide the subprogress bar

        for (Mod mod : mods) {
            if (!downloads.contains(mod) && !isCancelled()) {
                fireTask(Language.INSTANCE.localize("common.downloading") + " "
                        + (mod.isFilePattern() ? mod.getName() : mod.getFile()));
                mod.download(this);
                fireSubProgress(-1); // Hide the subprogress bar
            }
        }

    }

    private void organiseLibraries() {
        List<String> libraryNamesAdded = new ArrayList<>();
        fireTask(Language.INSTANCE.localize("instance.organisinglibraries"));
        fireSubProgressUnknown();
        if (!isServer) {
            for (File libraryFile : forgeLibraries) {
                if (!libraryFile.exists()) {
                    LogManager.error("Cannot install instance because the library file " + libraryFile.getAbsolutePath()
                            + " wasn't found!");
                    this.cancel(true);
                    return;
                }
                libraryNamesAdded.add(libraryFile.getName().substring(0, libraryFile.getName().lastIndexOf("-")));
            }
            for (Library library : this.version.getMinecraftVersion().getMojangVersion().getLibraries()) {
                if (library.shouldInstall()) {
                    if (libraryNamesAdded.contains(
                            library.getFile().getName().substring(0, library.getFile().getName().lastIndexOf("-")))) {
                        continue;
                    }
                    if (library.getFile().exists()) {
                        if (library.shouldExtract()) {
                            if (library.hasNatives()) {
                                Utils.unzip(library.getNativeFile(), getNativesDirectory(), library.getExtractRule());
                            } else {
                                Utils.unzip(library.getFile(), getNativesDirectory(), library.getExtractRule());
                            }
                        }
                    } else {
                        LogManager.error("Cannot install instance because the library file "
                                + library.getFile().getAbsolutePath() + " wasn't found!");
                        this.cancel(true);
                        return;
                    }
                }
            }
        }

        if (this.isServer) {
            for (File file : serverLibraries) {
                file.getParentFile().mkdirs();
                Utils.copyFile(new File(App.settings.getGameLibrariesDir(), file.getName()), file, true);
            }
        }

        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void doCaseConversions(File dir) {
        File[] files;
        if (isReinstall
                && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion().getVersion())) {
            final List<String> customMods = instance.getCustomMods(Type.mods);
            FilenameFilter ffFilter = (dir1, name) -> !customMods.contains(name);
            files = dir.listFiles(ffFilter);
        } else {
            files = dir.listFiles();
        }
        for (File file : files) {
            if (file.isFile() && (file.getName().endsWith("jar") || file.getName().endsWith("zip")
                    || file.getName().endsWith("litemod"))) {
                if (this.jsonVersion.getCaseAllFiles() == CaseType.upper) {
                    file.renameTo(new File(file.getParentFile(),
                            file.getName().substring(0, file.getName().lastIndexOf(".")).toUpperCase() + file.getName()
                                    .substring(file.getName().lastIndexOf("" + "."), file.getName().length())));
                } else if (this.jsonVersion.getCaseAllFiles() == CaseType.lower) {
                    file.renameTo(new File(file.getParentFile(), file.getName().toLowerCase()));
                }
            }
        }
    }

    private ArrayList<Downloadable> getResources() {
        ArrayList<Downloadable> downloads = new ArrayList<>(); // All the files
        File objectsFolder = new File(App.settings.getAssetsDir(), "objects");
        File indexesFolder = new File(App.settings.getAssetsDir(), "indexes");
        File virtualFolder = new File(App.settings.getAssetsDir(), "virtual");
        File resourcesFolder = new File(this.getRootDirectory(), "resources");
        String assetVersion = this.version.getMinecraftVersion().getMojangVersion().getAssets();
        File virtualRoot = new File(virtualFolder, assetVersion);
        File indexFile = new File(indexesFolder, assetVersion + ".json");
        objectsFolder.mkdirs();
        indexesFolder.mkdirs();
        virtualFolder.mkdirs();
        try {
            MojangAssetIndex assetIndex = this.version.getMinecraftVersion().getMojangVersion().getAssetIndex();
            new Downloadable(assetIndex.getUrl(), indexFile, assetIndex.getSha1(), (int) assetIndex.getSize(), this,
                    false).download(false);
            AssetIndex index = (AssetIndex) this.gson.fromJson(new FileReader(indexFile), AssetIndex.class);

            if (index.isVirtual()) {
                virtualRoot.mkdirs();
            }

            if (index.mapsToResources()) {
                resourcesFolder.mkdirs();
                this.assetsMapToResources = true;
            }

            for (Map.Entry<String, AssetObject> entry : index.getObjects().entrySet()) {
                AssetObject object = entry.getValue();
                String filename = object.getHash().substring(0, 2) + "/" + object.getHash();
                File file = new File(objectsFolder, filename);

                boolean needToCopy = index.isVirtual() || index.mapsToResources();
                File copyTo = new File(
                        index.isVirtual() ? virtualRoot : index.mapsToResources() ? resourcesFolder : null,
                        entry.getKey());

                if (object.needToDownload(file)) {
                    downloads.add(new Downloadable(MojangConstants.RESOURCES_BASE.getURL(filename), file,
                            object.getHash(), (int) object.getSize(), this, false, copyTo, needToCopy));
                } else {
                    if (needToCopy) {
                        copyTo.mkdirs();
                        Utils.copyFile(file, copyTo, true);
                    }
                }
            }
        } catch (JsonSyntaxException | FileNotFoundException | JsonIOException e) {
            LogManager.logStackTrace(e);
        }

        return downloads;
    }

    public ArrayList<Downloadable> getLibraries() {
        ArrayList<Downloadable> libraries = new ArrayList<>();
        List<String> libraryNamesAdded = new ArrayList<>();

        // Now read in the library jars needed from the pack
        for (com.atlauncher.data.json.Library library : this.jsonVersion.getLibraries()) {
            if (this.isServer && !library.forServer()) {
                continue;
            }

            if (library.hasDepends()) {
                boolean found = false;
                for (Mod mod : selectedMods) {
                    if (library.getDepends().equalsIgnoreCase(mod.getName())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            } else if (library.hasDependsGroup()) {
                boolean found = false;
                for (Mod mod : selectedMods) {
                    if (library.getDependsGroup().equalsIgnoreCase(mod.getGroup())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }

            if (!library.getUrl().startsWith("http://") && !library.getUrl().startsWith("https://")) {
                library.setDownloadType(DownloadType.server);
            }

            if (library.hasPath()) {
                this.libraries.add(library.getPath());
            } else {
                this.libraries.add(library.getFile());
            }

            forgeLibraries.add(library.getDownloadPath());
            File downloadTo = library.getDownloadPath();

            if (this.isServer && library.forServer()) {
                this.serverLibraries.add(new File(getLibrariesDirectory(), library.getServer()));
            }

            if (library.shouldForce() && downloadTo.exists()) {
                Utils.delete(downloadTo);
            }

            if (library.getDownloadType() == DownloadType.server) {
                libraries.add(new Downloadable(library.getUrl(), downloadTo, library.getMD5(), library.getFilesize(),
                        this, true));
            } else if (library.getDownloadType() == DownloadType.direct) {
                libraries.add(new Downloadable(library.getUrl(), downloadTo, library.getMD5(), this, false));
            } else {
                LogManager.error("DownloadType for server library " + library.getFile() + " is invalid with a "
                        + "value of " + library.getDownloadType());
                this.cancel(true);
                return null;
            }
            if (library.getFile().contains("-")) {
                libraryNamesAdded.add(library.getFile().substring(0, library.getFile().lastIndexOf("-")));
            } else {
                libraryNamesAdded.add(library.getFile());
            }
        }
        // Now read in the library jars needed from Mojang
        if (this.loader == null || this.loader.useMinecraftLibraries()) {
            for (Library library : this.version.getMinecraftVersion().getMojangVersion().getLibraries()) {
                if (library.shouldInstall()) {
                    if (libraryNamesAdded.contains(
                            library.getFile().getName().substring(0, library.getFile().getName().lastIndexOf("-")))) {
                        LogManager.debug("Not adding library " + library.getName() + " as it's been overwritten "
                                + "already by the packs libraries!");
                        continue;
                    }

                    if (!library.shouldExtract()) {
                        this.libraries.add(library.getPathFromRoot());
                    }

                    if (library.hasArtifact()) {
                        DownloadsItem artifact = library.getDownloads().getArtifact();
                        libraries.add(new Downloadable(library.getURL(), library.getFile(), artifact.getSha1(),
                                artifact.getSize(), this, false));
                    } else {
                        libraries.add(new Downloadable(library.getURL(), library.getFile(), null, this, false));
                    }

                    if (library.hasNatives()) {
                        this.libraries.add(library.getNativePathFromRoot());
                        DownloadsItem nativeClassifier = library.getNativeClassifier();
                        libraries.add(new Downloadable(library.getNativeURL(), library.getNativeFile(),
                                nativeClassifier.getSha1(), nativeClassifier.getSize(), this, false));
                    }
                }
            }
        }
        return libraries;
    }

    public void downloadMinecraft() {
        totalBytes = 0;
        downloadedBytes = 0;
        MojangDownloads downloads = this.version.getMinecraftVersion().getMojangVersion().getDownloads();

        MojangDownload mojangDownload;

        if (isServer) {
            mojangDownload = downloads.getServer();
        } else {
            mojangDownload = downloads.getClient();
        }

        Downloadable download = new Downloadable(mojangDownload.getUrl(), getMinecraftJarLibrary(),
                mojangDownload.getSha1(), (int) mojangDownload.getSize(), this, false, getMinecraftJar(), true);

        fireTask(Language.INSTANCE.localize("instance.downloadingminecraft"));
        fireSubProgressUnknown();

        if (download.needToDownload()) {
            totalBytes += download.getFilesize();
            download.download(true);
        }

        if (this.isServer) {
            Utils.copyFile(getMinecraftJarLibrary(), getMinecraftJar(), true);
        } else {
            libraries.add(getMinecraftJarLibraryPath()); // add it to the libraries list for client only
        }

        fireSubProgress(-1); // Hide the subprogress bar
    }

    public void downloadLoggingClient() {
        totalBytes = 0;
        downloadedBytes = 0;
        LoggingFile loggingFile = this.version.getMinecraftVersion().getMojangVersion().getLogging().getClient()
                .getFile();

        Downloadable download = new Downloadable(loggingFile.getUrl(),
                new File(App.settings.getLogConfigsDir(), loggingFile.getId()), loggingFile.getSha1(),
                (int) loggingFile.getSize(), this, false);

        fireTask(Language.INSTANCE.localize("instance.downloadingloggingconfig"));
        fireSubProgressUnknown();

        if (download.needToDownload()) {
            totalBytes += download.getFilesize();
            download.download(true);
        }

        fireSubProgress(-1); // Hide the subprogress bar
    }

    public void deleteMetaInf() {
        File inputFile = getMinecraftJar();
        File outputTmpFile = new File(App.settings.getTempDir(), pack.getSafeName() + "-minecraft.jar");
        try {
            JarInputStream input = new JarInputStream(new FileInputStream(inputFile));
            JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile));
            JarEntry entry;

            while ((entry = input.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    continue;
                }
                output.putNextEntry(entry);
                byte buffer[] = new byte[1024];
                int amo;
                while ((amo = input.read(buffer, 0, 1024)) != -1) {
                    output.write(buffer, 0, amo);
                }
                output.closeEntry();
            }

            input.close();
            output.close();

            inputFile.delete();
            outputTmpFile.renameTo(inputFile);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void configurePack() {
        // Download the configs zip file
        fireTask(Language.INSTANCE.localize("instance.downloadingconfigs"));
        File configs = new File(App.settings.getTempDir(), "Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version.getVersion() + "/Configs.zip";
        Downloadable configsDownload = new Downloadable(path, configs, null, this, true);
        this.totalBytes = configsDownload.getFilesize();
        this.downloadedBytes = 0;
        configsDownload.download(true); // Download the file

        if (!configs.exists()) {
            LogManager.warn("Failed to download configs for pack!");
            this.cancel(true);
            return;
        }

        // Extract the configs zip file
        fireSubProgressUnknown();
        fireTask(Language.INSTANCE.localize("instance.extractingconfigs"));
        Utils.unzip(configs, getRootDirectory());
        Utils.delete(configs);
    }

    public String getServerJar() {
        if (this.jsonVersion.hasLoader()) {
            return this.loader.getServerJar();
        }

        Mod forge = null; // The Forge Mod
        Mod mcpc = null; // The MCPC Mod
        for (Mod mod : selectedMods) {
            if (mod.getType() == ModType.forge) {
                forge = mod;
            } else if (mod.getType() == ModType.mcpc) {
                mcpc = mod;
            }
        }
        if (mcpc != null) {
            return mcpc.getFile();
        } else if (forge != null) {
            return forge.getFile();
        } else {
            return "minecraft_server." + this.version.getMinecraftVersion().getVersion() + ".jar";
        }
    }

    public Version getJsonVersion() {
        return this.jsonVersion;
    }

    public boolean hasJarMods() {
        for (Mod mod : selectedMods) {
            if (!mod.installOnServer() && this.isServer) {
                continue;
            }
            if (mod.getType() == ModType.jar) {
                return true;
            } else if (mod.getType() == ModType.decomp
                    && mod.getDecompType() == com.atlauncher.data.json.DecompType.jar) {
                return true;
            }
        }

        return false;
    }

    public boolean hasForge() {
        for (Mod mod : selectedMods) {
            if (!mod.installOnServer() && isServer) {
                continue;
            }
            if (mod.getType() == ModType.forge) {
                return true;
            }
        }

        return false;
    }

    public List<Mod> getMods() {
        return this.allMods;
    }

    public boolean shouldCoruptInstance() {
        return this.instanceIsCorrupt;
    }

    public int getPermGen() {
        return this.permgen;
    }

    public int getMemory() {
        return this.memory;
    }

    public List<String> getLibrariesForLaunch() {
        return this.libraries;
    }

    public String getExtraArguments() {
        return this.extraArguments;
    }

    public String getMinecraftArguments() {
        return this.version.getMinecraftVersion().getMojangVersion().getMinecraftArguments();
    }

    public List<String> getArguments() {
        return this.arguments;
    }

    public boolean hasArguments() {
        return this.arguments.size() != 0;
    }

    public String getMainClass() {
        if (this.mainClass == null) {
            this.setMainClass();
        }

        return this.mainClass;
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

    private Boolean installUsingJSON() throws Exception {
        if (this.jsonVersion == null) {
            return false;
        }
        if (this.jsonVersion.hasMessages()) {
            if (this.isReinstall && this.jsonVersion.getMessages().hasUpdateMessage()) {
                if (this.jsonVersion.getMessages().showUpdateMessage(this.pack) != 0) {
                    LogManager.error("Instance Install Cancelled After Viewing Message!");
                    cancel(true);
                    return false;
                }
            } else if (this.jsonVersion.getMessages().hasInstallMessage()) {
                if (this.jsonVersion.getMessages().showInstallMessage(this.pack) != 0) {
                    LogManager.error("Instance Install Cancelled After Viewing Message!");
                    cancel(true);
                    return false;
                }
            }
        }

        this.jsonVersion.compileColours();

        this.allMods = sortMods(
                (this.isServer ? this.jsonVersion.getServerInstallMods() : this.jsonVersion.getClientInstallMods()));

        boolean hasOptional = false;
        for (Mod mod : this.allMods) {
            if (mod.isOptional()) {
                hasOptional = true;
                break;
            }
        }

        if (this.allMods.size() != 0 && hasOptional) {
            ModsChooser modsChooser = new ModsChooser(this);

            if (this.shareCode != null) {
                modsChooser.applyShareCode(shareCode);
            }

            if (this.showModsChooser) {
                modsChooser.setVisible(true);
            }

            if (modsChooser.wasClosed()) {
                this.cancel(true);
                return false;
            }
            this.selectedMods = modsChooser.getSelectedMods();
            this.unselectedMods = modsChooser.getUnselectedMods();
        }
        if (!hasOptional) {
            this.selectedMods = this.allMods;
        }
        modsInstalled = new ArrayList<>();
        for (Mod mod : this.selectedMods) {
            String file = mod.getFile();
            if (this.jsonVersion.getCaseAllFiles() == CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.jsonVersion.getCaseAllFiles() == CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }
            this.modsInstalled.add(new DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                    Type.valueOf(Type.class, mod.getType().toString()), this.jsonVersion.getColour(mod.getColour()),
                    mod.getDescription(), false, false, true, mod.getCurseModId(), mod.getCurseFileId()));
        }

        if (this.isReinstall && instance.hasCustomMods()
                && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion().getVersion())) {
            for (DisableableMod mod : instance.getCustomDisableableMods()) {
                modsInstalled.add(mod);
            }
        }
        this.instanceIsCorrupt = true; // From this point on the instance is corrupt
        getTempDirectory().mkdirs(); // Make the temp directory
        backupSelectFiles();
        makeDirectories();
        addPercent(5);
        if (this.jsonVersion.hasLoader()) {
            setMainClass();
        }
        setExtraArguments();
        if (!this.isServer && this.version.getMinecraftVersion().getMojangVersion().getAssetIndex() != null) {
            downloadResources(); // Download Minecraft Resources
            if (isCancelled()) {
                return false;
            }
        }

        downloadMinecraft(); // Download Minecraft
        if (isCancelled()) {
            return false;
        }

        if (!this.isServer && this.version.getMinecraftVersion().getMojangVersion().hasLogging()) {
            downloadLoggingClient(); // Download logging client
        }

        downloadLibraries(); // Download Libraries
        if (isCancelled()) {
            return false;
        }
        organiseLibraries(); // Organise the libraries
        if (isCancelled()) {
            return false;
        }

        if (this.jsonVersion.hasLoader()) {
            try {
                this.loader = this.jsonVersion.getLoader().getLoader(new File(this.getTempDirectory(), "loader"), this,
                        this.loaderVersion);
            } catch (Throwable e) {
                LogManager.logStackTrace(e);
                LogManager.error("Cannot install instance because the loader failed to create");
                return false;
            }

            downloadLoader(); // Download Loader
            if (isCancelled()) {
                return false;
            }

            installLoader(); // Install Loader
            if (isCancelled()) {
                return false;
            }
        }

        addPercent(5);
        if (this.isServer && this.hasJarMods()) {
            fireTask(Language.INSTANCE.localize("server.extractingjar"));
            fireSubProgressUnknown();
            Utils.unzip(getMinecraftJar(), getTempJarDirectory());
        }
        if (!this.isServer && this.hasJarMods() && !this.hasForge()) {
            deleteMetaInf();
        }
        addPercent(5);
        if (selectedMods.size() != 0) {
            addPercent(40);
            fireTask(Language.INSTANCE.localize("instance.downloadingmods"));
            downloadMods(selectedMods);
            if (isCancelled()) {
                return false;
            }
            addPercent(40);
            installMods();
        } else {
            addPercent(80);
        }
        if (isCancelled()) {
            return false;
        }
        if (this.jsonVersion.shouldCaseAllFiles()) {
            doCaseConversions(getModsDirectory());
        }
        if (isServer && hasJarMods()) {
            fireTask(Language.INSTANCE.localize("server.zippingjar"));
            fireSubProgressUnknown();
            Utils.zip(getTempJarDirectory(), getMinecraftJar());
        }
        if (extractedTexturePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingtexturepackfiles"));
            fireSubProgressUnknown();
            if (!getTexturePacksDirectory().exists()) {
                getTexturePacksDirectory().mkdir();
            }
            Utils.zip(getTempTexturePackDirectory(), new File(getTexturePacksDirectory(), "TexturePack.zip"));
        }
        if (extractedResourcePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingresourcepackfiles"));
            fireSubProgressUnknown();
            if (!getResourcePacksDirectory().exists()) {
                getResourcePacksDirectory().mkdir();
            }
            Utils.zip(getTempResourcePackDirectory(), new File(getResourcePacksDirectory(), "ResourcePack.zip"));
        }
        if (isCancelled()) {
            return false;
        }
        if (hasActions()) {
            doActions();
        }
        if (isCancelled()) {
            return false;
        }
        if (!this.jsonVersion.hasNoConfigs()) {
            configurePack();
        }
        if (isCancelled()) {
            return false;
        }
        // Copy over common configs if any
        if (App.settings.getCommonConfigsDir().listFiles().length != 0) {
            Utils.copyDirectory(App.settings.getCommonConfigsDir(), getRootDirectory());
        }
        restoreSelectFiles();
        if (isServer) {
            File batFile = new File(getRootDirectory(), "LaunchServer.bat");
            File shFile = new File(getRootDirectory(), "LaunchServer.sh");
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.bat"), batFile, "%%SERVERJAR%%",
                    getServerJar());
            Utils.replaceText(new File(App.settings.getLibrariesDir(), "LaunchServer.sh"), shFile, "%%SERVERJAR%%",
                    getServerJar());
            batFile.setExecutable(true);
            shFile.setExecutable(true);
        }

        // add in the deselected mods to the instance.json
        for (Mod mod : this.unselectedMods) {
            String file = mod.getFile();
            if (this.jsonVersion.getCaseAllFiles() == CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.jsonVersion.getCaseAllFiles() == CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }

            this.modsInstalled.add(new DisableableMod(mod.getName(), mod.getVersion(), mod.isOptional(), file,
                    Type.valueOf(Type.class, mod.getType().toString()), this.jsonVersion.getColour(mod.getColour()),
                    mod.getDescription(), false, false, false, mod.getCurseModId(), mod.getCurseFileId()));
        }

        return true;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        LogManager.info("Started install of " + this.pack.getName() + " - " + this.version);

        if (this.loaderVersion != null) {
            LogManager.info("Using loader version " + this.loaderVersion.getVersion());
        }

        try {
            this.jsonVersion = Gsons.DEFAULT.fromJson(this.pack.getJSON(version.getVersion()), Version.class);
            return installUsingJSON();
        } catch (JsonParseException e) {
            LogManager.logStackTrace("Couldn't parse JSON of pack!", e);
        }

        return false;
    }

    private void setMainClass() {
        if (this.jsonVersion.hasMainClass()) {
            if (!this.jsonVersion.getMainClass().hasDepends() && !this.jsonVersion.getMainClass().hasDependsGroup()) {
                this.mainClass = this.jsonVersion.getMainClass().getMainClass();
            } else if (this.jsonVersion.getMainClass().hasDepends()) {
                String depends = this.jsonVersion.getMainClass().getDepends();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (mod.getName().equals(depends)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    this.mainClass = this.jsonVersion.getMainClass().getMainClass();
                }
            } else if (this.jsonVersion.getMainClass().hasDependsGroup()) {
                String depends = this.jsonVersion.getMainClass().getDependsGroup();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (!mod.hasGroup()) {
                        continue; // No group, continue
                    }
                    if (mod.getGroup().equals(depends)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    this.mainClass = this.jsonVersion.getMainClass().getMainClass();
                }
            }
        }
        if (this.mainClass == null) {
            this.mainClass = this.version.getMinecraftVersion().getMojangVersion().getMainClass();
        }
    }

    private void setExtraArguments() {
        if (this.jsonVersion.hasExtraArguments()) {
            if (!this.jsonVersion.getExtraArguments().hasDepends()
                    && !this.jsonVersion.getExtraArguments().hasDependsGroup()) {
                this.extraArguments = this.jsonVersion.getExtraArguments().getArguments();
            } else if (this.jsonVersion.getExtraArguments().hasDepends()) {
                String depends = this.jsonVersion.getExtraArguments().getDepends();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (mod.getName().equals(depends)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    this.extraArguments = this.jsonVersion.getExtraArguments().getArguments();
                }
            } else if (this.jsonVersion.getExtraArguments().hasDependsGroup()) {
                String depends = this.jsonVersion.getExtraArguments().getDependsGroup();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (!mod.hasGroup()) {
                        continue; // No group, continue
                    }
                    if (mod.getGroup().equals(depends)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    this.extraArguments = this.jsonVersion.getExtraArguments().getArguments();
                }
            }
        }
    }

    public void resetDownloadedBytes(int bytes) {
        totalBytes = bytes;
        downloadedBytes = 0;
    }

    public void fireTask(String name) {
        firePropertyChange("doing", null, name);
    }

    private void fireProgress(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        firePropertyChange("progress", null, percent);
    }

    private void fireSubProgress(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        firePropertyChange("subprogress", null, percent);
    }

    private void fireSubProgress(int percent, String paint) {
        if (percent > 100) {
            percent = 100;
        }
        String[] info = new String[2];
        info[0] = "" + percent;
        info[1] = paint;
        firePropertyChange("subprogress", null, info);
    }

    public void fireSubProgressUnknown() {
        firePropertyChange("subprogressint", null, null);
    }

    private void addPercent(int percent) {
        this.percent = this.percent + percent;
        if (this.percent > 100) {
            this.percent = 100;
        }
        fireProgress(this.percent);
    }

    public void setSubPercent(int percent) {
        if (percent > 100) {
            percent = 100;
        }
        fireSubProgress(percent);
    }

    public void setDownloadDone() {
        this.doneDownloads++;
        float progress;
        if (this.totalDownloads > 0) {
            progress = ((float) this.doneDownloads / (float) this.totalDownloads) * 100;
        } else {
            progress = 0;
        }
        fireSubProgress((int) progress);
    }

    public void addDownloadedBytes(int bytes) {
        this.downloadedBytes += bytes;
        this.updateProgressBar();
    }

    public void addTotalDownloadedBytes(int bytes) {
        this.totalBytes += bytes;
        this.updateProgressBar();
    }

    private void updateProgressBar() {
        float progress;
        if (this.totalBytes > 0) {
            progress = ((float) this.downloadedBytes / (float) this.totalBytes) * 100;
        } else {
            progress = 0;
        }
        float done = (float) this.downloadedBytes / 1024 / 1024;
        float toDo = (float) this.totalBytes / 1024 / 1024;
        if (done > toDo) {
            fireSubProgress(100, String.format("%.2f MB", done));
        } else {
            fireSubProgress((int) progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    public String getShareCodeData(String code) {
        String shareCodeData = null;

        try {
            java.lang.reflect.Type type = new TypeToken<APIResponse<String>>() {
            }.getType();
            APIResponse<String> response = Gsons.DEFAULT.fromJson(
                    Utils.sendGetAPICall(
                            "pack/" + this.pack.getSafeName() + "/" + version.getVersion() + "/share-code/" + code),
                    type);

            if (!response.wasError()) {
                shareCodeData = response.getData();
            }
        } catch (IOException e) {
            LogManager.logStackTrace("API call failed", e);
        }

        return shareCodeData;
    }
}
