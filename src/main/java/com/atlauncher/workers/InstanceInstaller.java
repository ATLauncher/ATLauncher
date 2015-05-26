/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.Network;
import com.atlauncher.data.APIResponse;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.json.Action;
import com.atlauncher.data.json.CaseType;
import com.atlauncher.data.json.Delete;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.mojang.AssetIndex;
import com.atlauncher.data.mojang.AssetObject;
import com.atlauncher.data.mojang.DateTypeAdapter;
import com.atlauncher.data.mojang.EnumTypeAdapterFactory;
import com.atlauncher.data.mojang.FileTypeAdapter;
import com.atlauncher.data.mojang.Library;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.CaseFileVisitor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import javax.swing.SwingWorker;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {

    private final Gson gson; // GSON Parser
    private String instanceName;
    private Pack pack;
    private Version jsonVersion;
    private PackVersion version;
    private boolean isReinstall;
    private boolean isServer;
    private final String shareCode;
    private final boolean showModsChooser;
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
    private int permgen = 0;
    private int memory = 0;
    private String librariesNeeded = null;
    private String extraArguments = null;
    private String mainClass = null;
    private int percent = 0; // Percent done installing
    private List<Mod> allMods;
    private List<Mod> selectedMods;
    private int totalDownloads = 0; // Total number of downloads to download
    private int doneDownloads = 0; // Total number of downloads downloaded
    private int totalBytes = 0; // Total number of bytes to download
    private int downloadedBytes = 0; // Total number of bytes downloaded
    private Instance instance = null;
    private List<DisableableMod> modsInstalled;
    private List<Path> serverLibraries;
    private List<String> forgeLibraries = new ArrayList<String>();

    public InstanceInstaller(String instanceName, Pack pack, PackVersion version, boolean isReinstall, boolean
            isServer, String shareCode, boolean showModsChooser) {
        this.instanceName = instanceName;
        this.pack = pack;
        this.version = version;
        this.isReinstall = isReinstall;
        this.isServer = isServer;
        this.shareCode = shareCode;
        this.showModsChooser = showModsChooser;
        if (isServer) {
            serverLibraries = new ArrayList<Path>();
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

    public boolean isLegacy() {
        return this.version.getMinecraftVersion().isLegacy();
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

    public Path getRootDirectory() {
        if (isServer) {
            return FileSystem.SERVERS.resolve(pack.getSafeName() + "_" + version.getSafeVersion());
        }

        return FileSystem.INSTANCES.resolve(getInstanceSafeName());
    }

    public Path getTempDirectory() {
        return FileSystem.TMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion());
    }

    public Path getTempJarDirectory() {
        return FileSystem.TMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion() + "_JarTemp");
    }

    public Path getTempActionsDirectory() {
        return FileSystem.TMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion() + "_ActionsTemp");
    }

    public Path getTempTexturePackDirectory() {
        return FileSystem.TMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion() + "_TexturePackTemp");
    }

    public Path getTempResourcePackDirectory() {
        return FileSystem.TMP.resolve(pack.getSafeName() + "_" + version.getSafeVersion() + "_ResourcePackTemp");
    }

    public Path getLibrariesDirectory() {
        return this.getRootDirectory().resolve("libraries");
    }

    public Path getTexturePacksDirectory() {
        return this.getRootDirectory().resolve("texturepacks");
    }

    public Path getShaderPacksDirectory() {
        return this.getRootDirectory().resolve("shaderpacks");
    }

    public Path getResourcePacksDirectory() {
        return this.getRootDirectory().resolve("resourcepacks");
    }

    public Path getConfigDirectory() {
        return this.getRootDirectory().resolve("config");
    }

    public Path getModsDirectory() {
        return this.getRootDirectory().resolve("mods");
    }

    public Path getIC2LibDirectory() {
        return this.getModsDirectory().resolve("ic2");
    }

    public Path getDenLibDirectory() {
        return this.getModsDirectory().resolve("denlib");
    }

    public Path getFlanDirectory() {
        return this.getRootDirectory().resolve("Flan");
    }

    public Path getDependencyDirectory() {
        return this.getModsDirectory().resolve(this.version.getMinecraftVersion().getVersion());
    }

    public Path getPluginsDirectory() {
        return this.getRootDirectory().resolve("plugins");
    }

    public Path getCoreModsDirectory() {
        return this.getRootDirectory().resolve("coremods");
    }

    public Path getJarModsDirectory() {
        return this.getRootDirectory().resolve("jarmods");
    }

    public Path getDisabledModsDirectory() {
        return this.getRootDirectory().resolve("disabledmods");
    }

    public Path getBinDirectory() {
        return this.getRootDirectory().resolve("bin");
    }

    public Path getNativesDirectory() {
        return this.getBinDirectory().resolve("natives");
    }

    public boolean hasActions() {
        return this.jsonVersion.hasActions();
    }

    public PackVersion getVersion() {
        return this.version;
    }

    public Path getMinecraftJar() {
        if (isServer) {
            return this.getRootDirectory().resolve("minecraft_server." + this.version.getMinecraftVersion()
                    .getVersion() + ".jar");
        }
        return this.getBinDirectory().resolve("minecraft.jar");
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
            if (!isLegacy()) {
                jarOrder = jarOrder + "," + file;
            } else {
                jarOrder = file + "," + jarOrder;
            }
        }
    }

    public boolean wasModInstalled(String mod) {
        return instance != null && instance.wasModInstalled(mod);
    }

    public boolean isReinstall() {
        return this.isReinstall;
    }

    public Mod getModByName(String name) {
        for (Mod mod : allMods) {
            if (mod.name.equalsIgnoreCase(name)) {
                return mod;
            }
        }
        return null;
    }

    public List<Mod> getLinkedMods(Mod mod) {
        List<Mod> linkedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (!modd.hasLinked()) {
                continue;
            }
            if (modd.linked.equalsIgnoreCase(mod.name)) {
                linkedMods.add(modd);
            }
        }
        return linkedMods;
    }

    public List<Mod> getGroupedMods(Mod mod) {
        List<Mod> groupedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (!modd.hasGroup()) {
                continue;
            }
            if (modd.group.equalsIgnoreCase(mod.group)) {
                if (modd != mod) {
                    groupedMods.add(modd);
                }
            }
        }
        return groupedMods;
    }

    public List<Mod> getModsDependancies(Mod mod) {
        List<Mod> dependsMods = new ArrayList<Mod>();
        for (String name : mod.depends) {
            inner:
            {
                for (Mod modd : allMods) {
                    if (modd.name.equalsIgnoreCase(name)) {
                        dependsMods.add(modd);
                        break inner;
                    }
                }
            }
        }
        return dependsMods;
    }

    public List<Mod> dependedMods(Mod mod) {
        List<Mod> dependedMods = new ArrayList<Mod>();
        for (Mod modd : allMods) {
            if (!modd.hasDepends()) {
                continue;
            }
            if (modd.isDependencyOf(mod)) {
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
            if (modd.isDependencyOf(mod)) {
                return true;
            }
        }
        return false;
    }

    private void makeDirectories() {
        if (isReinstall || isServer) {
            // We're reinstalling or installing a server so delete these folders
            if (Files.exists(this.getBinDirectory()) && Files.isDirectory(this.getBinDirectory())) {
                FileUtils.deleteDirectory(this.getBinDirectory());
            }
            if (Files.exists(this.getConfigDirectory()) && Files.isDirectory(this.getConfigDirectory())) {
                FileUtils.deleteDirectory(this.getConfigDirectory());
            }

            if (instance != null && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion()
                    .getVersion()) && instance.hasCustomMods()) {
                FileUtils.deleteSpecifiedFiles(this.getModsDirectory(), instance.getCustomMods(ModType.MODS));
                if (this.version.getMinecraftVersion().usesCoreMods()) {
                    FileUtils.deleteSpecifiedFiles(this.getCoreModsDirectory(), instance.getCustomMods(ModType
                            .COREMODS));
                }
                if (isReinstall) {
                    FileUtils.deleteSpecifiedFiles(this.getJarModsDirectory(), instance.getCustomMods(ModType.JAR));
                }
            } else {
                FileUtils.deleteDirectory(this.getModsDirectory());
                if (this.version.getMinecraftVersion().usesCoreMods()) {
                    FileUtils.deleteDirectory(this.getCoreModsDirectory());
                }
                if (isReinstall) {
                    FileUtils.deleteDirectory(this.getJarModsDirectory()); // Only delete if it's not a server
                }
            }
            if (isReinstall) {
                if (Files.exists(this.getTexturePacksDirectory().resolve("TexturePack.zip"))) {
                    FileUtils.delete(this.getTexturePacksDirectory().resolve("TexturePack.zip"));
                }

                if (Files.exists(this.getTexturePacksDirectory().resolve("ResourcePack.zip"))) {
                    FileUtils.delete(this.getResourcePacksDirectory().resolve("ResourcePack.zip"));
                }
            } else {
                if (Files.exists(this.getLibrariesDirectory()) && Files.isDirectory(this.getLibrariesDirectory())) {
                    FileUtils.deleteDirectory(this.getLibrariesDirectory()); // Only delete if it's a server
                }
            }
            if (this.instance != null) {
                if (this.jsonVersion.hasDeletes()) {
                    // Do the files
                    for (Delete delete : this.jsonVersion.getDeletes().getFiles()) {
                        Path file = delete.getFile(this.instance);
                        if (delete.isValid() && Files.exists(file)) {
                            FileUtils.delete(file);
                        }
                    }

                    // Do the folders
                    for (Delete delete : this.jsonVersion.getDeletes().getFolders()) {
                        Path file = delete.getFile(this.instance);
                        if (delete.isValid() && Files.exists(file)) {
                            FileUtils.deleteDirectory(file);
                        }
                    }
                }
            }
        }

        Path[] directories;
        if (isServer) {
            directories = new Path[]{this.getRootDirectory(), this.getModsDirectory(), this.getLibrariesDirectory()};
        } else {
            directories = new Path[]{this.getRootDirectory(), this.getModsDirectory(), this.getDisabledModsDirectory
                    (), this.getJarModsDirectory(), this.getBinDirectory(), this.getNativesDirectory()};
        }

        for (Path directory : directories) {
            FileUtils.createDirectory(directory);
        }

        if (this.version.getMinecraftVersion().usesCoreMods()) {
            FileUtils.createDirectory(this.getCoreModsDirectory());
        }
    }

    private List<Downloadable> getDownloadableMods() {
        List<Downloadable> mods = new ArrayList<Downloadable>();

        for (Mod mod : this.selectedMods) {
            if (mod.download == DownloadType.SERVER) {
                mods.add(new Downloadable(mod.getUrl(), mod.md5, FileSystem.DOWNLOADS.resolve(mod.getFile()), mod
                        .filesize, true, this));
            }
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
                fireTask(Language.INSTANCE.localize("common.installing") + " " + mod.name);
                addPercent(this.selectedMods.size() / 40);
                try {
                    mod.install(this);
                } catch (Exception e) {
                    LogManager.logStackTrace(e);
                }
            }
        }
    }

    public boolean hasRecommendedMods() {
        for (Mod mod : allMods) {
            if (mod.recommended) {
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
            if (modd.group.equalsIgnoreCase(mod.group) && modd.recommended) {
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

        // Sets up the progress client for Downloadables to show file download progress
        Network.setupProgressClient(this);

        for (Downloadable download : downloads) {
            if (download.needToDownload()) {
                totalBytes += download.getFilesize();
            }
        }

        fireSubProgress(0); // Show the subprogress bar
        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (download.needToDownload()) {
                            fireTask(Language.INSTANCE.localize("common.downloading") + " " + download.to.getFileName
                                    ().toString());
                            download.download();
                        } else {
                            download.copy();
                        }
                    } catch (Exception e) {
                        e.printStackTrace(System.err);
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    private void downloadLibraries() {
        fireTask(Language.INSTANCE.localize("instance.downloadinglibraries"));
        fireSubProgressUnknown();
        ExecutorService executor;
        ArrayList<Downloadable> downloads = getLibraries();
        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());
        totalBytes = 0;
        downloadedBytes = 0;

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (download.needToDownload()) {
                        totalBytes += download.getFilesize();
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(0); // Show the subprogress bar

        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (download.needToDownload()) {
                            fireTask(Language.INSTANCE.localize("common.downloading") + " " + download.to.getFileName
                                    ().toString());
                            download.download();
                        }
                    } catch (Exception e) {
                        LogManager.logStackTrace(e);
                        e.printStackTrace(System.err);
                    }
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
        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());
        totalBytes = 0;
        downloadedBytes = 0;

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (download.needToDownload()) {
                        totalBytes += download.getFilesize();
                    }
                }
            });
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(0); // Show the subprogress bar

        executor = Executors.newFixedThreadPool(App.settings.getConcurrentConnections());

        for (final Downloadable download : downloads) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (download.needToDownload()) {
                            download.download();
                        }
                    } catch (Exception e) {
                        LogManager.logStackTrace(e);
                        e.printStackTrace(System.err);
                    }
                }
            });
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }

        fireSubProgress(-1); // Hide the subprogress bar

        for (Mod mod : mods) {
            if (!downloads.contains(mod) && !isCancelled()) {
                fireTask(Language.INSTANCE.localize("common.downloading") + " " + (mod.filePattern ? mod.name : mod
                        .getFile()));
                try {
                    mod.download(this);
                } catch (Exception e) {
                    LogManager.logStackTrace(e);
                }
                fireSubProgress(-1); // Hide the subprogress bar
            }
        }
    }

    private void organiseLibraries() {
        List<String> libraryNamesAdded = new ArrayList<String>();
        fireTask(Language.INSTANCE.localize("instance.organisinglibraries"));
        fireSubProgressUnknown();
        if (!isServer) {
            for (String libraryFile : forgeLibraries) {
                Path library = FileSystem.LIBRARIES.resolve(libraryFile);
                if (Files.exists(library)) {
                    FileUtils.copyFile(library, this.getBinDirectory());
                } else {
                    LogManager.error("Cannot install instance because the library file " + library + " wasn't found!");
                    this.cancel(true);
                    return;
                }
                libraryNamesAdded.add(library.getFileName().toString().substring(0, library.getFileName().toString()
                        .lastIndexOf("-")));
            }
            for (Library library : this.version.getMinecraftVersion().getMojangVersion().getLibraries()) {
                if (library.shouldInstall()) {
                    if (libraryNamesAdded.contains(library.getFilePath().getFileName().toString().substring(0,
                            library.getFilePath().getFileName().toString().lastIndexOf("-")))) {
                        continue;
                    }
                    if (Files.exists(library.getFilePath())) {
                        if (library.shouldExtract()) {
                            FileUtils.unzip(library.getFilePath(), this.getNativesDirectory(), library.getExtractRule
                                    ());
                        } else {
                            FileUtils.copyFile(library.getFilePath(), this.getBinDirectory());
                        }
                    } else {
                        LogManager.error("Cannot install instance because the library file " + library.getFilePath()
                                + " wasn't found!");
                        this.cancel(true);
                        return;
                    }
                }
            }
        }
        Path toCopy, copyTo;
        boolean withFilename = false;
        if (isServer) {
            toCopy = FileSystem.JARS.resolve("minecraft_server." + this.version.getMinecraftVersion().getVersion() +
                    ".jar");
            copyTo = this.getRootDirectory();
        } else {
            toCopy = FileSystem.JARS.resolve(this.version.getMinecraftVersion().getVersion() + ".jar");
            copyTo = this.getBinDirectory().resolve("minecraft.jar");
            withFilename = true;
        }
        if (Files.exists(toCopy)) {
            FileUtils.copyFile(toCopy, copyTo, withFilename);
        } else {
            LogManager.error("Cannot install instance because the library file " + toCopy + " wasn't found!");
            this.cancel(true);
            return;
        }
        fireSubProgress(-1); // Hide the subprogress bar
    }

    /**
     * @deprecated use doCaseConversions(Path)
     */
    private void doCaseConversions(File dir) {
        this.doCaseConversions(dir.toPath());
    }

    // TODO: Switch to use NIO and paths natively
    private void doCaseConversions(Path dir) {
        try {
            if (isReinstall && instance.getMinecraftVersion().equalsIgnoreCase(version.getMinecraftVersion()
                    .getVersion())) {
                Files.walkFileTree(dir, new CaseFileVisitor(this.jsonVersion.getCaseAllFiles(), instance
                        .getCustomMods(ModType.MODS)));
            } else {
                Files.walkFileTree(dir, new CaseFileVisitor(this.jsonVersion.getCaseAllFiles()));
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Error casing files while installing instance!", e);
        }
    }

    private ArrayList<Downloadable> getResources() {
        ArrayList<Downloadable> downloads = new ArrayList<Downloadable>(); // All the files
        String assetVersion = this.version.getMinecraftVersion().getMojangVersion().getAssets();
        Path virtualRoot = FileSystem.RESOURCES_VIRTUAL.resolve(assetVersion);
        Path indexFile = FileSystem.RESOURCES_INDEXES.resolve(assetVersion + ".json");

        try {
            new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("indexes/" + assetVersion + ".json"), null,
                    indexFile, -1, false, this).download();
            AssetIndex index = this.gson.fromJson(new FileReader(indexFile.toFile()), AssetIndex.class);

            if (!index.isVirtual() && !Files.exists(virtualRoot)) {
                FileUtils.createDirectory(virtualRoot);
            }

            for (Map.Entry<String, AssetObject> entry : index.getObjects().entrySet()) {
                AssetObject object = entry.getValue();
                String filename = object.getHash().substring(0, 2) + "/" + object.getHash();
                Path file = FileSystem.RESOURCES_OBJECTS.resolve(filename);
                Path virtualFile = virtualRoot.resolve(entry.getKey());

                if (object.needToDownload(file)) {
                    downloads.add(new Downloadable(MojangConstants.RESOURCES_BASE.getURL(filename), object.getHash(),
                            file, (int) object.getSize(), false, this));
                } else {
                    if (index.isVirtual()) {
                        FileUtils.createDirectory(virtualFile.getParent());
                        FileUtils.copyFile(file, virtualFile, true);
                    }
                }
            }
        } catch (JsonSyntaxException | JsonIOException | IOException e) {
            LogManager.logStackTrace("Error processing resources for Minecraft!", e);
        }

        return downloads;
    }

    public ArrayList<Downloadable> getLibraries() {
        ArrayList<Downloadable> libraries = new ArrayList<Downloadable>();
        List<String> libraryNamesAdded = new ArrayList<String>();

        // Now read in the library jars needed from the pack
        for (com.atlauncher.data.json.Library library : this.jsonVersion.getLibraries()) {
            if (library.hasDepends()) {
                boolean found = false;
                for (Mod mod : selectedMods) {
                    if (library.getDepends().equalsIgnoreCase(mod.name)) {
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
                    if (library.getDependsGroup().equalsIgnoreCase(mod.group)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    continue;
                }
            }

            if (!library.getUrl().startsWith("http://") && !library.getUrl().startsWith("https://")) {
                library.setDownloadType(DownloadType.SERVER);
            }

            if (librariesNeeded == null) {
                this.librariesNeeded = library.getFile();
            } else {
                this.librariesNeeded += "," + library.getFile();
            }

            forgeLibraries.add(library.getFile());

            if (this.isServer) {
                if (!library.forServer()) {
                    continue;
                }
                serverLibraries.add(this.getLibrariesDirectory().resolve(library.getServer()));
            }

            Path downloadTo = FileSystem.LIBRARIES.resolve(library.getFile());
            if (library.getDownloadType() == DownloadType.SERVER) {
                libraries.add(new Downloadable(library.getUrl(), library.getMD5(), downloadTo, library.getFilesize(),
                        true, this));
            } else if (library.getDownloadType() == DownloadType.DIRECT) {
                libraries.add(new Downloadable(library.getUrl(), library.getMD5(), downloadTo, -1, false, this));
            } else {
                LogManager.error("DownloadType for server library " + library.getFile() + " is invalid with a " +
                        "value of " + library.getDownloadType());
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
        if (!this.isServer) {
            for (Library library : this.version.getMinecraftVersion().getMojangVersion().getLibraries()) {
                if (library.shouldInstall()) {
                    if (libraryNamesAdded.contains(library.getFilePath().getFileName().toString().substring(0,
                            library.getFilePath().getFileName().toString().lastIndexOf("-")))) {
                        LogManager.debug("Not adding library " + library.getName() + " as it's been overwritten " +
                                "already by the packs libraries!");
                        continue;
                    }
                    if (!library.shouldExtract()) {
                        if (librariesNeeded == null) {
                            this.librariesNeeded = library.getFilePath().getFileName().toString();
                        } else {
                            this.librariesNeeded += "," + library.getFilePath().getFileName().toString();
                        }
                    }
                    libraries.add(new Downloadable(library.getURL(), null, library.getFilePath(), -1, false, this));
                }
            }
        }

        // Add Minecraft.jar
        if (isServer) {
            libraries.add(new Downloadable(this.getServerURL(), null, FileSystem.JARS.resolve("minecraft_server." +
                    this.version.getMinecraftVersion().getVersion() + ".jar"), -1, false, this));
        } else {
            libraries.add(new Downloadable(this.getClientURL(), null, FileSystem.JARS.resolve(this.version
                    .getMinecraftVersion().getVersion() + ".jar"), -1, false, this));
        }
        return libraries;
    }

    private String getClientURL() {
        return MojangConstants.DOWNLOAD_BASE.getURL("versions/" + this.version.getMinecraftVersion().getVersion() +
                "/" + this.version.getMinecraftVersion().getVersion() + ".jar");
    }

    private String getServerURL() {
        return MojangConstants.DOWNLOAD_BASE.getURL("versions/" + this.version.getMinecraftVersion().getVersion() +
                "/minecraft_server." + this.version.getMinecraftVersion().getVersion() + ".jar");
    }

    // TODO: Switch to NIO operations and possibly move to Utils/FileUtils (new class)
    public void deleteMetaInf() {
        Path inputFile = this.getMinecraftJar();
        Path outputTmpFile = FileSystem.TMP.resolve(pack.getSafeName() + "-minecraft.jar");
        try {
            JarInputStream input = new JarInputStream(new FileInputStream(inputFile.toFile()));
            JarOutputStream output = new JarOutputStream(new FileOutputStream(outputTmpFile.toFile()));
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

            FileUtils.delete(inputFile);
            FileUtils.moveFile(outputTmpFile, inputFile);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public void configurePack() {
        // Download the configs zip file
        fireTask(Language.INSTANCE.localize("instance.downloadingconfigs"));
        Path configs = this.getTempDirectory().resolve("Configs.zip");
        String path = "packs/" + pack.getSafeName() + "/versions/" + version.getVersion() + "/Configs.zip";
        Downloadable configsDownload = new Downloadable(path, null, configs, -1, true, this);

        this.resetDownloadedBytes(0);

        try {
            configsDownload.download(); // Download the file
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        // Extract the configs zip file
        fireSubProgressUnknown();
        fireTask(Language.INSTANCE.localize("instance.extractingconfigs"));
        FileUtils.unzip(configs, this.getRootDirectory());
        FileUtils.delete(configs);
    }

    public String getServerJar() {
        Mod forge = null; // The Forge Mod
        Mod mcpc = null; // The MCPC Mod
        for (Mod mod : selectedMods) {
            if (mod.type == ModType.FORGE) {
                forge = mod;
            } else if (mod.type == ModType.MCPC) {
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
            if (!mod.server && this.isServer) {
                continue;
            }
            if (mod.type == ModType.JAR) {
                return true;
            } else if (mod.type == ModType.DECOMP && mod.decompType == com.atlauncher.data.json.DecompType.jar) {
                return true;
            }
        }

        return false;
    }

    public boolean hasForge() {
        for (Mod mod : selectedMods) {
            if (!mod.server && isServer) {
                continue;
            }
            if (mod.type == ModType.FORGE) {
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

    public String getLibrariesNeeded() {
        return this.librariesNeeded;
    }

    public String getExtraArguments() {
        return this.extraArguments;
    }

    public String getMinecraftArguments() {
        return this.version.getMinecraftVersion().getMojangVersion().getMinecraftArguments();
    }

    public String getMainClass() {
        if (this.mainClass == null) {
            return this.version.getMinecraftVersion().getMojangVersion().getMainClass();
        }
        return this.mainClass;
    }

    public List<Mod> sortMods(List<Mod> original) {
        List<Mod> mods = new ArrayList<Mod>(original);

        for (Mod mod : original) {
            if (mod.optional) {
                if (mod.hasLinked()) {
                    for (Mod mod1 : original) {
                        if (mod1.name.equalsIgnoreCase(mod.linked)) {
                            mods.remove(mod);
                            int index = mods.indexOf(mod1) + 1;
                            mods.add(index, mod);
                        }
                    }

                }
            }
        }

        List<Mod> modss = new ArrayList<Mod>();

        for (Mod mod : mods) {
            if (!mod.optional) {
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
        Path reis = this.getModsDirectory().resolve("rei_minimap");
        if (Files.exists(reis) && Files.isDirectory(reis)) {
            if (FileUtils.copyDirectory(reis, this.getTempDirectory(), true)) {
                savedReis = true;
            }
        }

        Path zans = this.getModsDirectory().resolve("VoxelMods");
        if (Files.exists(zans) && Files.isDirectory(zans)) {
            if (FileUtils.copyDirectory(zans, this.getTempDirectory(), true)) {
                savedZans = true;
            }
        }

        Path neiCfg = this.getConfigDirectory().resolve("NEI.cfg");
        if (Files.exists(neiCfg) && Files.isRegularFile(neiCfg)) {
            if (FileUtils.copyFile(neiCfg, this.getTempDirectory())) {
                savedNEICfg = true;
            }
        }

        Path optionsTXT = this.getRootDirectory().resolve("options.txt");
        if (Files.exists(optionsTXT) && Files.isRegularFile(optionsTXT)) {
            if (FileUtils.copyFile(optionsTXT, this.getTempDirectory())) {
                savedOptionsTxt = true;
            }
        }

        Path serversDAT = this.getRootDirectory().resolve("servers.dat");
        if (Files.exists(serversDAT) && Files.isRegularFile(serversDAT)) {
            if (FileUtils.copyFile(serversDAT, this.getTempDirectory())) {
                savedServersDat = true;
            }
        }

        Path portalGunSounds = this.getModsDirectory().resolve("PortalGunSounds.pak");
        if (Files.exists(portalGunSounds) && Files.isRegularFile(portalGunSounds)) {
            savedPortalGunSounds = true;
            FileUtils.copyFile(portalGunSounds, this.getTempDirectory());
        }
    }

    private void restoreSelectFiles() {
        if (savedReis) {
            FileUtils.copyDirectory(this.getTempDirectory().resolve("rei_minimap"), this.getModsDirectory().resolve
                    ("rei_minimap"));
        }

        if (savedZans) {
            FileUtils.copyDirectory(this.getTempDirectory().resolve("VoxelMods"), this.getModsDirectory().resolve
                    ("VoxelMods"));
        }

        if (savedNEICfg) {
            FileUtils.copyFile(this.getTempDirectory().resolve("NEI.cfg"), this.getConfigDirectory().resolve("NEI" +
                    ".cfg"), true);
        }

        if (savedOptionsTxt) {
            FileUtils.copyFile(this.getTempDirectory().resolve("options.txt"), this.getRootDirectory().resolve
                    ("options" + ".txt"), true);
        }

        if (savedServersDat) {
            FileUtils.copyFile(this.getTempDirectory().resolve("servers.dat"), this.getRootDirectory().resolve
                    ("servers" + ".dat"), true);
        }

        if (savedPortalGunSounds) {
            FileUtils.copyFile(this.getTempDirectory().resolve("PortalGunSounds.pak"), this.getModsDirectory()
                    .resolve("PortalGunSounds.pak"), true);
        }
    }

    private Boolean installUsingJSON() throws Exception {
        if (this.jsonVersion == null) {
            return false;
        }
        if (this.jsonVersion.hasMessages()) {
            if (this.isReinstall && this.jsonVersion.getMessages().hasUpdateMessage() && this.jsonVersion.getMessages
                    ().showUpdateMessage(this.pack) != 0) {
                LogManager.error("Instance Install Cancelled After Viewing Message!");
                cancel(true);
                return false;
            } else if (this.jsonVersion.getMessages().hasInstallMessage() && this.jsonVersion.getMessages()
                    .showInstallMessage(this.pack) != 0) {
                LogManager.error("Instance Install Cancelled After Viewing Message!");
                cancel(true);
                return false;
            }
        }

        this.jsonVersion.compileColours();

        this.allMods = sortMods((this.isServer ? this.jsonVersion.getServerInstallMods() : this.jsonVersion
                .getClientInstallMods()));

        boolean hasOptional = false;
        for (Mod mod : this.allMods) {
            if (mod.optional) {
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
            this.modsInstalled.add(mod.generateDisableableMod(this, file));
        }

        if (this.isReinstall && instance.hasCustomMods() && instance.getMinecraftVersion().equalsIgnoreCase(version
                .getMinecraftVersion().getVersion())) {
            for (DisableableMod mod : instance.getCustomDisableableMods()) {
                modsInstalled.add(mod);
            }
        }
        this.instanceIsCorrupt = true; // From this point on the instance is corrupt
        FileUtils.createDirectory(this.getTempDirectory()); // Make the temp directory
        backupSelectFiles();
        makeDirectories();
        addPercent(5);
        setMainClass();
        setExtraArguments();
        if (this.version.getMinecraftVersion().hasResources()) {
            downloadResources(); // Download Minecraft Resources
            if (isCancelled()) {
                return false;
            }
        }
        downloadLibraries(); // Download Libraries
        if (isCancelled()) {
            return false;
        }
        organiseLibraries(); // Organise the libraries
        if (isCancelled()) {
            return false;
        }
        if (this.isServer) {
            for (Path path : serverLibraries) {
                FileUtils.createDirectory(path);
                FileUtils.copyFile(FileSystem.LIBRARIES.resolve(path.getFileName()), path, true);
            }
        }
        addPercent(5);
        if (this.isServer && this.hasJarMods()) {
            fireTask(Language.INSTANCE.localize("server.extractingjar"));
            fireSubProgressUnknown();
            FileUtils.unzip(getMinecraftJar(), getTempJarDirectory());
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
            FileUtils.zip(getTempJarDirectory(), getMinecraftJar());
        }
        if (extractedTexturePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingtexturepackfiles"));
            fireSubProgressUnknown();
            if (!Files.exists(this.getTexturePacksDirectory())) {
                FileUtils.createDirectory(this.getTexturePacksDirectory());
            }
            FileUtils.zip(getTempTexturePackDirectory(), this.getTexturePacksDirectory().resolve("TexturePack.zip"));
        }

        if (extractedResourcePack) {
            fireTask(Language.INSTANCE.localize("instance.zippingresourcepackfiles"));
            fireSubProgressUnknown();
            if (!Files.exists(this.getResourcePacksDirectory())) {
                FileUtils.createDirectory(this.getResourcePacksDirectory());
            }
            FileUtils.zip(getTempResourcePackDirectory(), this.getResourcePacksDirectory().resolve("ResourcePack.zip"));
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

        // Copy over common configs if any
        if (FileSystem.COMMON.toFile().listFiles().length != 0) {
            FileUtils.copyDirectory(FileSystem.COMMON, getRootDirectory());
        }

        restoreSelectFiles();

        if (isServer) {
            File batFile = this.getRootDirectory().resolve("LaunchServer.bat").toFile();
            File shFile = this.getRootDirectory().resolve("LaunchServer.sh").toFile();

            Utils.replaceText(FileSystem.LIBRARIES.resolve("LaunchServer.bat").toFile(), batFile, "%%SERVERJAR%%",
                    getServerJar());
            Utils.replaceText(FileSystem.LIBRARIES.resolve("LaunchServer.sh").toFile(), shFile, "%%SERVERJAR%%",
                    getServerJar());

            batFile.setExecutable(true);
            shFile.setExecutable(true);
        }

        return true;
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        LogManager.info("Started install of " + this.pack.getName() + " - " + this.version);

        try {
            this.jsonVersion = this.pack.getJsonVersion(version.getVersion());
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
                    if (mod.name.equals(depends)) {
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
                    if (mod.group.equals(depends)) {
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
            if (!this.jsonVersion.getExtraArguments().hasDepends() && !this.jsonVersion.getExtraArguments()
                    .hasDependsGroup()) {
                this.extraArguments = this.jsonVersion.getExtraArguments().getArguments();
            } else if (this.jsonVersion.getExtraArguments().hasDepends()) {
                String depends = this.jsonVersion.getExtraArguments().getDepends();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (mod.name.equals(depends)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    this.extraArguments = this.jsonVersion.getExtraArguments().getArguments();
                }
            } else if (this.jsonVersion.getMainClass().hasDependsGroup()) {
                String depends = this.jsonVersion.getMainClass().getDependsGroup();
                boolean found = false;
                for (Mod mod : this.selectedMods) {
                    if (!mod.hasGroup()) {
                        continue; // No group, continue
                    }
                    if (mod.group.equals(depends)) {
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
            APIResponse response = Gsons.DEFAULT.fromJson(Utils.sendGetAPICall("pack/" + this.pack.getSafeName() + "/" +
                    version.getVersion() + "/share-code/" + code), APIResponse.class);

            if (!response.wasError()) {
                shareCodeData = response.getDataAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return shareCodeData;
    }
}
