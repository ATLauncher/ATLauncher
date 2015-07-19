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

import com.atlauncher.FileSystem;
import com.atlauncher.Network;
import com.atlauncher.backup.BackupMethods;
import com.atlauncher.collection.DownloadPool;
import com.atlauncher.collection.ModList;
import com.atlauncher.data.DisableableMod;
import com.atlauncher.data.Downloadable;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.json.Action;
import com.atlauncher.data.json.CaseType;
import com.atlauncher.data.json.Delete;
import com.atlauncher.data.json.DownloadType;
import com.atlauncher.data.json.Library;
import com.atlauncher.data.json.Mod;
import com.atlauncher.data.json.ModType;
import com.atlauncher.data.json.Version;
import com.atlauncher.data.mojang.AssetIndex;
import com.atlauncher.data.mojang.AssetObject;
import com.atlauncher.data.mojang.MojangConstants;
import com.atlauncher.data.version.PackVersion;
import com.atlauncher.gui.dialogs.ModsChooser;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.utils.ATLauncherAPI;
import com.atlauncher.utils.CompressionUtils;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.validator.DependencyValidator;
import com.atlauncher.utils.validator.GroupValidator;
import com.atlauncher.utils.walker.CaseFileVisitor;

import javax.swing.SwingWorker;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

public class InstanceInstaller extends SwingWorker<Boolean, Void> {
    public final String name;
    public final String shareCode;
    public final boolean server;
    public final boolean reinstall;
    public final boolean showModsChooser;
    public final Path tmpDir;
    public final Path root;
    public final Path mods;
    public final Path coremods;
    public final Path resourcepacks;
    public final Path bin;
    public final Path texturepacks;
    public final Path configs;
    public final Path jarmods;
    public final Path libraries;
    public final Path natives;
    public final Path disabledmods;
    public final Path dependencies;
    public final Path shaderpacks;
    public final Path flans;
    public final Path ic2;
    public final Path denlib;
    public final Path plugins;
    public final PackVersion packVersion;
    public final Pack pack;
    public final ModList selectedMods = new ModList();
    public final List<DisableableMod> installedMods = new LinkedList<>();
    public final List<String> forgeLibraries = new LinkedList<>();

    private final List<Path> serverLibraries = new LinkedList<>();

    protected String jarOrder;
    protected int permgen;
    protected int memory;
    protected int percent;
    protected int totalBytes;
    protected int downloadedBytes;
    protected boolean extractedTexturePack;
    protected boolean corrupt = true;
    protected boolean extractedResourcePack;
    protected String mainClass;
    protected String extraArgs;
    protected String librariesNeeded;
    protected Instance instance;
    public Version version;
    public ModList allMods;

    public InstanceInstaller(String name, Pack pack, PackVersion version, boolean reinstall, String shareCode,
                             boolean server, boolean showModsChooser) {
        this.name = name;
        this.pack = pack;
        this.packVersion = version;
        this.reinstall = reinstall;
        this.shareCode = shareCode;
        this.server = server;
        this.showModsChooser = showModsChooser;

        this.tmpDir = FileSystem.TMP.resolve(this.pack.getSafeName() + "_" + this.packVersion.getSafeVersion());
        this.root = (this.server ? FileSystem.SERVERS.resolve(this.pack.getSafeName() + "_" + this.packVersion
                .getSafeVersion()) : FileSystem.INSTANCES.resolve(this.name.replaceAll("[^A-Za-z0-9]", "")));
        this.mods = root.resolve("mods");
        this.coremods = root.resolve("coremods");
        this.resourcepacks = root.resolve("resourcepacks");
        this.texturepacks = root.resolve("texturepacks");
        this.bin = root.resolve("bin");
        this.configs = root.resolve("configs");
        this.jarmods = root.resolve("jarmods");
        this.libraries = root.resolve("libraries");
        this.disabledmods = root.resolve("disabledmods");
        this.natives = this.bin.resolve("natives");
        this.dependencies = this.mods.resolve(this.packVersion.getMinecraftVersion().getVersion());
        this.shaderpacks = this.root.resolve("shaderpacks");
        this.flans = this.root.resolve("Flan");
        this.ic2 = this.mods.resolve("ic2");
        this.plugins = this.root.resolve("plugins");
        this.denlib = this.mods.resolve("denlib");
    }

    public Instance getInstance() {
        return this.instance;
    }

    public void addDownloadedBytes(int bytes) {
        this.downloadedBytes += bytes;
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
            this.fireSubProgress(100, String.format("%.2f MB", done));
        } else {
            this.fireSubProgress((int) progress, String.format("%.2f MB / %.2f MB", done, toDo));
        }
    }

    private void fireSubProgress(int percent, String paint) {
        if (percent > 100) {
            percent = 100;
        }
        String[] info = new String[2];
        info[0] = "" + percent;
        info[1] = paint;
        this.firePropertyChange("subprogress", null, info);
    }

    public void setTexturePacksExtracted() {
        this.extractedTexturePack = true;
    }

    public void setResourcePacksExtracted() {
        this.extractedResourcePack = true;
    }

    public void addToJarOrder(String file) {
        if (this.jarOrder == null) {
            this.jarOrder = file;
        } else {
            if (this.packVersion.getMinecraftVersion().isLegacy()) {
                this.jarOrder = this.jarOrder + "," + file;
            } else {
                this.jarOrder = file + "," + this.jarOrder;
            }
        }
    }

    public boolean isOnlyRecommendedInGroup(Mod mod) {
        for (Mod modd : this.allMods) {
            if (modd == mod || !modd.hasGroup()) {
                continue;
            }

            if (modd.group.equalsIgnoreCase(mod.group) && modd.recommended) {
                return false;
            }
        }

        return true;
    }

    private Path getMinecraftJar() {
        if (this.server) {
            return this.root.resolve("minecraft_server." + this.packVersion.getMinecraftVersion().getVersion() + "" +
                    ".jar");
        } else {
            return this.bin.resolve("minecraft.jar");
        }
    }

    private String getServerJar() {
        Mod forge = this.selectedMods.getByType(ModType.FORGE);
        Mod mcpc = this.selectedMods.getByType(ModType.MCPC);

        if (mcpc != null) {
            return mcpc.getFile();
        } else if (forge != null) {
            return forge.getFile();
        } else {
            return this.getMinecraftJar().getFileName().toString();
        }
    }

    public Path getTempJarDirectory() {
        return FileSystem.TMP.resolve(this.pack.getSafeName() + "_" + this.packVersion.getSafeVersion() + "_JarTemp");
    }

    public Path getTempActionsDirectory() {
        return FileSystem.TMP.resolve(this.pack.getSafeName() + "_" + this.packVersion.getSafeVersion() +
                "_ActionsTemp");
    }

    public Path getTempResourcePacksDirectory() {
        return FileSystem.TMP.resolve(this.pack.getSafeName() + "_" + this.packVersion.getSafeVersion() + "_RPTemp");
    }

    public Path getTempTexturePacksDirectory() {
        return FileSystem.TMP.resolve(this.pack.getSafeName() + "_" + this.packVersion.getSafeVersion() + "_TPTemp");
    }

    private DownloadPool getLibraries() {
        DownloadPool pool = new DownloadPool();
        List<String> libraryNamesAdded = new LinkedList<>();

        for (Library lib : this.version.getLibraries()) {
            if (lib.hasDepends()) {
                if (!lib.dependencyValidator().find(this.selectedMods)) {
                    continue;
                }
            } else if (lib.hasDependsGroup()) {
                if (!lib.groupValidator().find(this.selectedMods)) {
                    continue;
                }
            }

            if (!lib.getUrl().startsWith("http://") && !lib.getUrl().startsWith("https://")) {
                lib.setDownloadType(DownloadType.SERVER);
            }

            if (this.librariesNeeded == null) {
                this.librariesNeeded = lib.getFile();
            } else {
                this.librariesNeeded += "," + lib.getFile();
            }

            this.forgeLibraries.add(lib.getFile());

            if (this.server) {
                if (!lib.forServer()) {
                    continue;
                }

                this.serverLibraries.add(this.libraries.resolve(lib.getServer()));
            }

            Path to = FileSystem.LIBRARIES.resolve(lib.getFile());

            if (lib.shouldForce() && Files.exists(to)) {
                FileUtils.delete(to);
            }

            if (lib.getDownloadType() == DownloadType.SERVER) {
                pool.add(new Downloadable(lib.getUrl(), lib.getMD5(), to, lib.getFilesize(), true, this));
            } else if (lib.getDownloadType() == DownloadType.DIRECT) {
                pool.add(new Downloadable(lib.getUrl(), lib.getMD5(), to, -1, false, this));
            } else {
                LogManager.error("DownloadType for server library " + lib.getFile() + " is invalid with value of " +
                        lib.getDownloadType());
                this.cancel(true);
                return null;
            }

            if (lib.getFile().contains("-")) {
                libraryNamesAdded.add(lib.getFile().substring(0, lib.getFile().lastIndexOf("-")));
            } else {
                libraryNamesAdded.add(lib.getFile());
            }
        }

        if (!this.server) {
            for (com.atlauncher.data.mojang.Library lib : this.packVersion.getMinecraftVersion().getMojangVersion()
                    .getLibraries()) {
                if (lib.shouldInstall()) {
                    String name = lib.getFilePath().getFileName().toString();
                    if (libraryNamesAdded.contains(name.substring(0, name.lastIndexOf("-")))) {
                        LogManager.debug("Not adding library " + lib.getName() + " as it's been overwritten already " +
                                "by the packs libraries");
                        continue;
                    }

                    if (!lib.shouldExtract()) {
                        if (librariesNeeded == null) {
                            this.librariesNeeded = name;
                        } else {
                            this.librariesNeeded += "," + name;
                        }
                    }

                    pool.add(new Downloadable(lib.getURL(), null, lib.getFilePath(), -1, false, this));
                }
            }
        }

        if (this.server) {
            pool.add(new Downloadable(this.getServerURL(), null, FileSystem.JARS.resolve("minecraft_server." + this
                    .packVersion.getMinecraftVersion().getVersion() + ".jar"), -1, false, this));
        } else {
            pool.add(new Downloadable(this.getClientURL(), null, FileSystem.JARS.resolve(this.packVersion
                    .getMinecraftVersion().getVersion() + ".jar"), -1, false, this));
        }

        return pool;
    }

    private String getClientURL() {
        return MojangConstants.DOWNLOAD_BASE.getURL("versions/" + this.packVersion.getMinecraftVersion().getVersion() +
                "/" + this.packVersion.getMinecraftVersion().getVersion() + ".jar");
    }

    private String getServerURL() {
        return MojangConstants.DOWNLOAD_BASE.getURL("versions/" + this.packVersion.getMinecraftVersion().getVersion() +
                "/minecraft_server." + this.packVersion.getMinecraftVersion().getVersion() + ".jar");
    }

    private DownloadPool getResources() {
        DownloadPool pool = new DownloadPool();

        String assetVersion = this.packVersion.getMinecraftVersion().getMojangVersion().getAssets();
        Path virtual = FileSystem.RESOURCES_VIRTUAL.resolve(assetVersion);
        Path indexFile = FileSystem.RESOURCES_INDEXES.resolve(assetVersion + ".json");

        Downloadable dl = new Downloadable(MojangConstants.DOWNLOAD_BASE.getURL("indexes/" + assetVersion + "" +
                ".json"), Hashing.md5(indexFile).toString(), indexFile, -1, false, this);

        try {
            if (dl.needToDownload()) {
                dl.download();
            }

            AssetIndex index = new JsonFile(indexFile).convert(AssetIndex.class);
            if (!index.isVirtual() && !Files.exists(virtual)) {
                FileUtils.createDirectory(virtual);
            }

            for (Map.Entry<String, AssetObject> entry : index.getObjects().entrySet()) {
                AssetObject obj = entry.getValue();
                String filename = obj.getHash().substring(0, 2) + "/" + obj.getHash();
                Path file = FileSystem.RESOURCES_OBJECTS.resolve(filename);

                if (obj.needToDownload(file)) {
                    pool.add(new Downloadable(MojangConstants.RESOURCES_BASE.getURL(filename), obj.getHash(), file,
                            entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1), (int) obj.getSize(),
                            false, this));
                } else {
                    Path virtualFile = virtual.resolve(entry.getKey());
                    if (index.isVirtual()) {
                        FileUtils.createDirectory(virtualFile.getParent());
                        FileUtils.copyFile(file, virtualFile, true);
                    }
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Error processing resources for Minecraft", e);
        }

        return pool;
    }

    private void downloadMods(ModList mods) throws Exception {
        this.fireSubProgressUnknown();
        DownloadPool pool = mods.downloadPool(this).downsize();
        this.totalBytes = this.downloadedBytes = 0;
        this.totalBytes = pool.totalSize();
        this.fireSubProgress(0);
        pool.downloadAll();
        this.fireSubProgress(-1);
        for (Mod mod : mods) {
            if (!this.isCancelled()) {
                this.fireTask(LanguageManager.localize("common.downloading") + " " + (mod.filePattern ? mod.name :
                        mod.getFile()));
                mod.download(this);
                this.fireSubProgress(-1);
            }
        }
    }

    private void downloadResources() {
        this.fireTask(LanguageManager.localize("instance.downloadingresources"));
        this.fireSubProgressUnknown();
        DownloadPool pool = this.getResources().downsize();
        this.totalBytes = this.downloadedBytes = 0;
        Network.setupProgressClient(this);
        this.totalBytes = pool.totalSize();
        this.fireSubProgress(0);
        pool.downloadAll(this);
        this.fireSubProgress(-1);
    }

    private void downloadConfigs() {
        this.totalBytes = this.downloadedBytes = 0;
        this.fireSubProgressUnknown();

        this.fireTask(LanguageManager.localize("instance.downloadingconfigs"));

        String path = "packs/" + this.pack.getSafeName() + "/versions/" + this.packVersion.getVersion() + "/Configs" +
                ".zip";
        Downloadable dl = new Downloadable(path, null, this.tmpDir.resolve("Configs.zip"), -1, true, this);

        try {
            dl.download();
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }
    }

    private void extractConfigs() {
        Path configs = this.tmpDir.resolve("Configs.zip");
        this.fireSubProgressUnknown();
        this.fireTask(LanguageManager.localize("instance.extractingconfigs"));
        FileUtils.unzip(configs, this.root);
        FileUtils.delete(configs);
    }

    private void downloadLibraries() {
        this.fireTask(LanguageManager.localize("instance.downloadinglibraries"));
        this.fireSubProgressUnknown();
        DownloadPool pool = this.getLibraries().downsize();
        this.totalBytes = this.downloadedBytes = 0;
        this.totalBytes = pool.totalSize();
        this.fireSubProgress(0);
        pool.downloadAll(this);
        this.fireSubProgress(-1);
    }

    private void setExtraArgs() {
        if (this.version.hasExtraArguments()) {
            if (!this.version.getExtraArguments().hasDepends() && !this.version.getExtraArguments().hasDependsGroup()) {
                this.extraArgs = this.version.getExtraArguments().getArguments();
            } else if (this.version.getExtraArguments().hasDepends()) {
                if (this.version.getExtraArguments().dependencyValidator().find(this.selectedMods)) {
                    this.extraArgs = this.version.getExtraArguments().getArguments();
                }
            } else if (this.version.getExtraArguments().hasDependsGroup()) {
                if (this.version.getExtraArguments().groupValidator().find(this.selectedMods)) {
                    this.extraArgs = this.version.getExtraArguments().getArguments();
                }
            }
        }
    }

    private void setMainClass() {
        if (this.version.hasMainClass()) {
            if (!this.version.getMainClass().hasDepends() && !this.version.getMainClass().hasDependsGroup()) {
                this.mainClass = this.version.getMainClass().getMainClass();
            } else if (this.version.getMainClass().hasDepends()) {
                DependencyValidator depChecker = this.version.getMainClass().dependencyValidator();
                if (depChecker.find(this.selectedMods)) {
                    this.mainClass = this.version.getMainClass().getMainClass();
                }
            } else if (this.version.getMainClass().hasDependsGroup()) {
                GroupValidator groupValidator = this.version.getMainClass().groupValidator();
                if (groupValidator.find(this.selectedMods)) {
                    this.mainClass = this.version.getMainClass().getMainClass();
                }
            }
        }

        if (this.mainClass == null) {
            this.mainClass = this.packVersion.getMinecraftVersion().getMojangVersion().getMainClass();
        }
    }

    private void makeDirectories() {
        if (this.reinstall || this.server) {
            if (Files.exists(this.bin) && Files.isDirectory(this.bin)) {
                FileUtils.deleteDirectory(this.bin);
            }

            if (Files.exists(this.configs) && Files.isDirectory(this.configs)) {
                FileUtils.deleteDirectory(this.configs);
            }

            if (this.instance != null && this.versionMatch() && this.instance.hasCustomMods()) {
                FileUtils.deleteSpecifiedFiles(this.mods, this.instance.getCustomMods(ModType.MODS));
                if (this.packVersion.getMinecraftVersion().usesCoreMods()) {
                    FileUtils.deleteSpecifiedFiles(this.coremods, this.instance.getCustomMods(ModType.COREMODS));
                }
                if (this.reinstall) {
                    FileUtils.deleteSpecifiedFiles(this.jarmods, this.instance.getCustomMods(ModType.JAR));
                }
            } else {
                FileUtils.deleteDirectory(this.mods);
                if (this.packVersion.getMinecraftVersion().usesCoreMods()) {
                    FileUtils.deleteDirectory(this.coremods);
                }
                if (this.reinstall) {
                    FileUtils.deleteDirectory(this.jarmods);
                }
            }

            if (this.reinstall) {
                Path pack = this.texturepacks.resolve("TexturePack.zip");
                if (Files.exists(pack)) {
                    FileUtils.delete(pack);
                }

                pack = this.resourcepacks.resolve("ResourcePack.zip");
                if (Files.exists(pack)) {
                    FileUtils.delete(pack);
                }
            } else {
                if (Files.exists(this.libraries) && Files.isDirectory(this.libraries)) {
                    FileUtils.deleteDirectory(this.libraries);
                }
            }

            if (this.instance != null && this.version.hasDeletes()) {
                for (Delete del : this.version.getDeletes().getFiles()) {
                    Path file = del.getFile(this.instance);
                    if (del.isValid() && Files.exists(file)) {
                        FileUtils.delete(file);
                    }
                }

                for (Delete del : this.version.getDeletes().getFolders()) {
                    Path file = del.getFile(this.instance);
                    if (del.isValid() && Files.exists(file)) {
                        FileUtils.deleteDirectory(file);
                    }
                }
            }

            Path[] dirs;
            if (this.server) {
                dirs = new Path[]{this.root, this.mods, this.libraries};
            } else {
                dirs = new Path[]{this.root, this.mods, this.disabledmods, this.jarmods, this.bin, this.natives};
            }

            for (Path p : dirs) {
                FileUtils.createDirectory(p);
            }

            if (this.packVersion.getMinecraftVersion().usesCoreMods()) {
                FileUtils.createDirectory(this.coremods);
            }
        }
    }

    private boolean versionMatch() {
        return this.instance.getMinecraftVersion().equalsIgnoreCase(this.packVersion.getMinecraftVersion().getVersion
                ());
    }

    private void fireSubProgress(int perc) {
        if (perc > 100) {
            perc = 100;
        }

        this.firePropertyChange("subprogress", null, perc);
    }

    private void addPercent(int perc) {
        this.percent = this.percent + perc;
        if (this.percent > 100) {
            this.percent = 100;
        }
        this.fireProgress(this.percent);
    }

    private void fireProgress(int perc) {
        if (perc > 100) {
            perc = 100;
        }

        this.firePropertyChange("progress", null, perc);
    }

    public void fireSubProgressUnknown() {
        this.firePropertyChange("subprogressint", null, null);
    }

    public void fireTask(String name) {
        this.firePropertyChange("doing", null, name);
    }

    private boolean hasForge() {
        for (Mod mod : this.selectedMods) {
            if (!mod.server && this.server) {
                continue;
            }

            if (mod.type == ModType.FORGE) {
                return true;
            }
        }

        return false;
    }

    private void installMods() {
        for (Mod mod : this.selectedMods) {
            if (!this.isCancelled()) {
                this.fireTask(LanguageManager.localize("common.installing") + " " + mod.name);
                this.addPercent(this.selectedMods.size() / 40);

                try {
                    mod.install(this);
                } catch (Exception e) {
                    LogManager.logStackTrace(e);
                }
            }
        }
    }

    private void organizeLibraries() {
        List<String> libraryNamesAdded = new LinkedList<>();
        this.fireTask(LanguageManager.localize("instance.organisinglibraries"));
        this.fireSubProgressUnknown();
        if (!this.server) {
            for (String lib : this.forgeLibraries) {
                Path library = FileSystem.LIBRARIES.resolve(lib);
                if (Files.exists(library)) {
                    FileUtils.copyFile(library, this.bin);
                } else {
                    LogManager.error("Cannot install instance because the library file " + lib + " wasn't found");
                    this.cancel(true);
                    return;
                }

                libraryNamesAdded.add(library.getFileName().toString().substring(0, library.getFileName().toString()
                        .lastIndexOf("-")));
            }

            for (com.atlauncher.data.mojang.Library lib : this.packVersion.getMinecraftVersion().getMojangVersion()
                    .getLibraries()) {
                if (lib.shouldInstall()) {
                    if (libraryNamesAdded.contains(lib.getFilePath().getFileName().toString().substring(0, lib
                            .getFilePath().getFileName().toString().lastIndexOf("-")))) {
                        continue;
                    }
                    if (Files.exists(lib.getFilePath())) {
                        if (lib.shouldExtract()) {
                            FileUtils.unzip(lib.getFilePath(), this.natives, lib.getExtractRule());
                        } else {
                            FileUtils.copyFile(lib.getFilePath(), this.bin);
                        }
                    } else {
                        LogManager.error("Cannot install instance because the library file " + lib.getFilePath() + " " +
                                "wasn't found!");
                        this.cancel(true);
                        return;
                    }
                }
            }
        }

        Path from, to;
        boolean withFileName = false;
        if (this.server) {
            from = FileSystem.JARS.resolve("minecraft_server." + this.packVersion.getMinecraftVersion().getVersion()
                    + ".jar");
            to = this.root;
        } else {
            from = FileSystem.JARS.resolve(this.packVersion.getMinecraftVersion().getVersion() + ".jar");
            to = this.bin.resolve("minecraft.jar");
            withFileName = true;
        }

        if (Files.exists(from)) {
            FileUtils.copyFile(from, to, withFileName);
        } else {
            LogManager.error("Cannot install instance because the library file " + from + " wasn't found");
            this.cancel(true);
            return;
        }

        this.fireSubProgress(-1);
    }

    private void deleteMetaInf() {
        Path input = this.getMinecraftJar();
        Path output = FileSystem.TMP.resolve(this.pack.getSafeName() + "-minecraft.jar");
        try (JarInputStream jis = new JarInputStream(Files.newInputStream(input));
             JarOutputStream jos = new JarOutputStream(Files.newOutputStream(output))) {

            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (entry.getName().contains("META-INF")) {
                    continue;
                }

                jos.putNextEntry(entry);
                byte[] bits = new byte[1024];
                int len;
                while ((len = jis.read(bits, 0, 1024)) != -1) {
                    jos.write(bits, 0, len);
                }
                jos.closeEntry();
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        FileUtils.delete(input);
        FileUtils.moveFile(output, input);
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        this.version = pack.getJsonVersion(this.packVersion.getVersion());
        this.allMods = (server ? this.version.getMods().server() : this.version.getMods().client()).sort();

        if (this.version.hasMessages()) {
            if (this.reinstall && this.version.getMessages().hasUpdateMessage() && this.version.getMessages()
                    .showUpdateMessage(this.pack) != 0) {
                LogManager.error("Instance install canceled after viewing update message");
                this.cancel(true);
                return Boolean.FALSE;
            } else if (this.version.getMessages().hasInstallMessage() && this.version.getMessages()
                    .showInstallMessage(this.pack) != 0) {
                LogManager.error("Instance install canceled after viewing install message");
                this.cancel(true);
                return Boolean.FALSE;
            }
        }

        this.version.compileColours();

        boolean hasOptional = this.allMods.hasOptional();

        if (this.allMods.size() != 0 && hasOptional) {
            ModsChooser modsChooser = new ModsChooser(this);
            if (this.shareCode != null) {
                modsChooser.applyShareCode(this.shareCode);
            }

            if (this.showModsChooser) {
                modsChooser.setVisible(true);
            }

            if (modsChooser.wasClosed()) {
                this.cancel(true);
                return Boolean.FALSE;
            }

            this.selectedMods.as(modsChooser.getSelected());
        }

        if (!hasOptional) {
            this.selectedMods.as(this.allMods);
        }

        for (Mod mod : this.selectedMods) {
            String file = mod.getFile();
            if (this.version.getCaseAllFiles() == CaseType.upper) {
                file = file.substring(0, file.lastIndexOf(".")).toUpperCase() + file.substring(file.lastIndexOf("."));
            } else if (this.version.getCaseAllFiles() == CaseType.lower) {
                file = file.substring(0, file.lastIndexOf(".")).toLowerCase() + file.substring(file.lastIndexOf("."));
            }

            this.installedMods.add(mod.generateDisableableMod(this, file));
        }

        if (this.reinstall && this.instance.hasCustomMods() && this.instance.getMinecraftVersion().equalsIgnoreCase
                (this.packVersion.getMinecraftVersion().getVersion())) {
            for (DisableableMod mod : this.instance.getCustomDisableableMods()) {
                this.installedMods.add(mod);
            }
        }

        FileUtils.createDirectory(this.tmpDir);
        BackupMethods.backup(this);
        this.makeDirectories();
        this.addPercent(5);
        this.setMainClass();
        this.setExtraArgs();

        if (this.packVersion.getMinecraftVersion().hasResources()) {
            this.downloadResources();
            if (this.isCancelled()) {
                return Boolean.FALSE;
            }
        }

        this.downloadLibraries();
        if (this.isCancelled()) {
            return Boolean.FALSE;
        }

        this.organizeLibraries();
        if (this.isCancelled()) {
            return Boolean.FALSE;
        }

        if (this.server) {
            for (Path p : this.serverLibraries) {
                FileUtils.createDirectory(p);
                FileUtils.copyFile(FileSystem.LIBRARIES.resolve(p.getFileName()), p, true);
            }
        }

        this.addPercent(5);

        if (this.server && this.selectedMods.hasJarMod(this)) {
            this.fireTask(LanguageManager.localize("server.extractingjar"));
            this.fireSubProgressUnknown();
            FileUtils.unzip(this.getTempJarDirectory(), this.getMinecraftJar());
        }

        if (!this.server && this.selectedMods.hasJarMod(this) && !this.hasForge()) {
            this.deleteMetaInf();
        }

        this.addPercent(5);
        if (this.selectedMods.size() != 0) {
            this.addPercent(40);
            this.fireTask(LanguageManager.localize("instance.downloadingmods"));
            this.downloadMods(this.selectedMods);
            if (this.isCancelled()) {
                return Boolean.FALSE;
            }
            this.addPercent(40);
            this.installMods();
        } else {
            this.addPercent(80);
        }

        if (this.isCancelled()) {
            return Boolean.FALSE;
        }

        if (this.version.shouldCaseAllFiles()) {
            try {
                if (this.reinstall && this.versionMatch()) {
                    Files.walkFileTree(this.mods, new CaseFileVisitor(this.version.getCaseAllFiles(), this.instance
                            .getCustomMods(ModType.MODS)));
                } else {
                    Files.walkFileTree(this.mods, new CaseFileVisitor(this.version.getCaseAllFiles()));
                }
            } catch (Exception e) {
                LogManager.logStackTrace("Error casing files while install instance", e);
            }
        }

        if (this.server && this.selectedMods.hasJarMod(this)) {
            this.fireTask(LanguageManager.localize("server.zippingjar"));
            this.fireSubProgressUnknown();
            CompressionUtils.zip(this.getTempJarDirectory(), this.getMinecraftJar());
        }

        if (this.extractedTexturePack) {
            this.fireTask(LanguageManager.localize("instance.zippingtexturepackfiles"));
            this.fireSubProgressUnknown();
            if (!Files.exists(this.texturepacks)) {
                FileUtils.createDirectory(this.texturepacks);
            }
            CompressionUtils.zip(this.getTempTexturePacksDirectory(), this.texturepacks);
        }

        if (this.extractedResourcePack) {
            this.fireTask(LanguageManager.localize("instance.zippingresourcepackfiles"));
            this.fireSubProgressUnknown();
            if (!Files.exists(this.resourcepacks)) {
                FileUtils.createDirectory(this.resourcepacks);
            }
            CompressionUtils.zip(this.getTempResourcePacksDirectory(), this.resourcepacks);
        }

        if (this.isCancelled()) {
            return Boolean.FALSE;
        }

        if (this.version.hasActions()) {
            for (Action action : this.version.getActions()) {
                action.execute(this);
            }
        }

        if (this.isCancelled()) {
            return Boolean.FALSE;
        }

        if (!this.version.hasNoConfigs()) {
            this.downloadConfigs();
            this.extractConfigs();
        }

        if (FileSystem.COMMON.toFile().listFiles().length != 0) {
            FileUtils.copyDirectory(FileSystem.COMMON, this.root);
        }

        BackupMethods.restore(this);

        if (this.server) {
            File batFile = this.root.resolve("LaunchServer.bat").toFile();
            File shFile = this.root.resolve("LaunchServer.sh").toFile();
            FileUtils.replaceText(FileSystem.LIBRARIES.resolve("LaunchServer.bat").toFile(), batFile,
                    "%%SERVERJAR%%", getServerJar());
            FileUtils.replaceText(FileSystem.LIBRARIES.resolve("LaunchServer.sh").toFile(), shFile, "%%SERVERJAR%%",
                    getServerJar());
            batFile.setExecutable(true);
            shFile.setExecutable(true);
        }

        return Boolean.TRUE;
    }

    public String getShareCodeData(String code) {
        return ATLauncherAPI.getShareCode(this.pack, version.getVersion(), code);
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}