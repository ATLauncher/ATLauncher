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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.json.Java;
import com.atlauncher.data.minecraft.ArgumentRule;
import com.atlauncher.data.minecraft.Arguments;
import com.atlauncher.data.minecraft.Download;
import com.atlauncher.data.minecraft.Downloads;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingClient;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;

import org.mini2Dx.gettext.GetText;

/**
 * This class handles contains information about a single Instance in the
 * Launcher. An Instance being an installed version of a ModPack separate to
 * others by file structure.
 *
 * @deprecated
 */
public class InstanceV1 implements Cloneable {
    /**
     * The name of the Instance.
     */
    private String name;

    /**
     * The name of the Pack this instance is for.
     */
    private final String pack;

    /**
     * The username of the user who installed this if it's set to be for that user
     * only.
     */
    private String installedBy;

    /**
     * The UUID of the user who installed this if it's set to be for that user only.
     */
    private String userLock;

    /**
     * The version installed for this Instance.
     */
    private String version;

    /**
     * The hash of this instance if it's a dev version.
     */
    private String hash;

    /**
     * The version of Minecraft that this Instance uses.
     */
    private String minecraftVersion;

    /**
     * The version type that this instance uses.
     */
    private String versionType;

    /**
     * The java requirements for this instance.
     */
    private Java java;

    /**
     * If this version allows Curse mod integration.
     */
    private boolean enableCurseIntegration = false;

    /**
     * If this version allows editing mods.
     */
    private boolean enableEditingMods = true;

    private boolean assetsMapToResources = false;

    /**
     * The loader version chosen to be installed for this instance.
     */
    private LoaderVersion loaderVersion;

    /**
     * The minimum RAM/memory recommended for this Instance by the pack developer/s.
     */
    private int memory = 0;

    /**
     * The minimum PermGen/MetaSpace recommended for this Instance by the pack
     * developer/s.
     */
    private int permgen = 0;

    /**
     * Array of paths for the libraries needed to be loaded.
     */
    private List<String> libraries;

    /**
     * Array of paths for the libraries needed to be loaded.
     */
    private List<String> arguments;

    /**
     * Comma seperated list of the libraries needed by Minecraft/Forge to be added
     * to the class path when launching Minecraft.
     *
     * @deprecated
     */
    private final String librariesNeeded = null;

    /**
     * The extra arguments to be added to the command when launching Minecraft.
     * Generally involves things such as the tweakClass/s for Forge.
     */
    private String extraArguments = null;

    /**
     * The arguments required by Minecraft to be added to the command when launching
     * Minecraft. Generally involves thing such as handling of authentication,
     * assets paths etc.
     */
    private String minecraftArguments = null;

    /**
     * The main class to be run when launching Minecraft.
     */
    private String mainClass = null;

    /**
     * The version of assets used by this Minecraft instance.
     */
    private String assets = null;

    /**
     * The logging client used for Minecraft.
     */
    private LoggingClient logging = null;

    /**
     * If this instance has been converted or not from the old format.
     */
    private boolean isConverted = false;

    /**
     * If this instance uses the new format for libraries.
     */
    private boolean usesNewLibraries = false;

    /**
     * The data version.
     */
    private int dataVersion = 2;

    /**
     * The Pack object for the pack this Instance was installed from. This is not
     * stored in the instances instance.json file as Pack's can be deleted from the
     * system.
     *
     * @see com.atlauncher.data.Pack
     */
    private transient Pack realPack;

    /**
     * If this Instance was installed from a development version of the Pack.
     */
    private boolean isDev;

    /**
     * If this Instance is playable or not. It may become unplayable after a failed
     * update or if files are found corrupt.
     */
    private boolean isPlayable;

    /**
     * List of DisableableMod objects for the mods in the Instance.
     *
     * @see com.atlauncher.data.DisableableMod
     */
    private List<DisableableMod> mods;

    /**
     * List of versions of the Pack this instance comes from that the user has said
     * to not be reminded about updating to.
     */
    private List<String> ignoredUpdates;

    private InstanceSettings settings = null;

    public transient Path ROOT;

    /**
     * Instantiates a new instance.
     *
     * @param name               the name of the Instance
     * @param pack               the name of the Pack this Instance is of
     * @param realPack           the Pack object for the Pack this Instance is of
     * @param enableUserLock     if this instance is only meant to be used by the
     *                           original installer
     * @param version            the version of the Pack this Instance is of
     * @param minecraftVersion   the Minecraft version this Instance runs off
     * @param versionType        the version type this Instance runs off
     * @param memory             the minimum RAM/memory as recommended by the pack
     *                           developer/s
     * @param permgen            the minimum PermGen/Metaspace as recommended by the
     *                           pack developer/s
     * @param mods               the mods installed in this Instance
     * @param libraries          the libraries needed to launch Minecraft
     * @param extraArguments     the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass          the main class to run when launching Minecraft
     * @param assets             the assets version being used by Minecraft
     * @param isDev              if this Instance is using a dev version of the pack
     * @param isPlayable         if this instance is playable
     * @param java               the java requirements for the instance
     */
    public InstanceV1(String name, String pack, Pack realPack, boolean enableUserLock, String version,
            String minecraftVersion, String versionType, int memory, int permgen, List<DisableableMod> mods,
            List<String> libraries, String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean assetsMapToResources, LoggingClient logging, boolean isDev, boolean isPlayable, Java java,
            boolean enableCurseIntegration, boolean enableEditingMods, LoaderVersion loaderVersion) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.versionType = versionType;
        this.memory = memory;
        this.permgen = permgen;
        this.mods = mods;
        this.libraries = libraries;
        this.mainClass = mainClass;
        this.assets = assets;
        this.assetsMapToResources = assetsMapToResources;
        this.logging = logging;
        this.extraArguments = extraArguments;
        this.minecraftArguments = minecraftArguments;
        this.isDev = isDev;
        this.isPlayable = isPlayable;
        if (enableUserLock && AccountManager.getSelectedAccount().uuid != null) {
            this.userLock = AccountManager.getSelectedAccount().getUUIDNoDashes();
        } else {
            this.userLock = null;
        }
        this.isConverted = true;
        this.usesNewLibraries = true;
        this.java = java;
        this.enableCurseIntegration = enableCurseIntegration;
        this.enableEditingMods = enableEditingMods;
        this.loaderVersion = loaderVersion;
    }

    /**
     * Instantiates a new instance with it defaulting to being playable.
     *
     * @param name               the name of the Instance
     * @param pack               the name of the Pack this Instance is of
     * @param realPack           the Pack object for the Pack this Instance is of
     * @param enableUserLock     if this instance is only meant to be used by the
     *                           original installer
     * @param version            the version of the Pack this Instance is of
     * @param minecraftVersion   the Minecraft version this Instance runs off
     * @param versionType        the version type this Instance runs off
     * @param memory             the minimum RAM/memory as recommended by the pack
     *                           developer/s
     * @param permgen            the minimum PermGen/Metaspace as recommended by the
     *                           pack developer/s
     * @param mods               the mods installed in this Instance
     * @param libraries          the libraries needed to launch Minecraft
     * @param extraArguments     the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass          the main class to run when launching Minecraft
     * @param assets             the assets version being used by Minecraft
     * @param isDev              if this Instance is using a dev version of the pack
     * @param java               the java requirements for the instance
     */
    public InstanceV1(String name, String pack, Pack realPack, boolean enableUserLock, String version,
            String minecraftVersion, String versionType, int memory, int permgen, List<DisableableMod> mods,
            List<String> libraries, String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean assetsMapToResources, LoggingClient logging, boolean isDev, Java java,
            boolean enableCurseIntegration, boolean enableEditingMods, LoaderVersion loaderVersion) {
        this(name, pack, realPack, enableUserLock, version, minecraftVersion, versionType, memory, permgen, mods,
                libraries, extraArguments, minecraftArguments, mainClass, assets, assetsMapToResources, logging, isDev,
                true, java, enableCurseIntegration, enableEditingMods, loaderVersion);
    }

    /**
     * Gets this instances name.
     *
     * @return the instances name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the safe name of the Instance used in file paths. Removes all non
     * alphanumeric characters.
     *
     * @return the safe name of the Instance.
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets a List of the selected installed mods in this Instance. Mods are listed
     * as DisableableMod objects.
     *
     * @return a List of DisableableMod objects of the selected installed mods in
     *         this instance or null if none
     */
    public List<DisableableMod> getInstalledSelectedMods() {
        List<DisableableMod> mods = new ArrayList<>();

        for (DisableableMod mod : this.mods) {
            if (mod.wasSelected()) {
                mods.add(mod);
            }
        }

        return mods;
    }

    /**
     * This converts an old Instance using old Minecraft argument storage to the new
     * method of storage as well as make sure we're on the same dataVersion.
     */
    public void convert() {
        if (!this.isConverted) {
            if (this.minecraftArguments != null) {
                // Minecraft arguments are now extraArguments if found
                this.extraArguments = this.minecraftArguments;
                this.minecraftArguments = null;
            }

            this.isConverted = true;
        }

        // this ensures all existing instances have the new format for mods
        if (this.dataVersion < 1) {
            if (this.mods != null) {
                List<DisableableMod> selectedMods = this.getInstalledSelectedMods();

                if (selectedMods.size() == 0) {
                    List<DisableableMod> mods = new ArrayList<>();

                    for (DisableableMod mod : this.mods) {
                        mod.setWasSelected(true);
                        mods.add(mod);
                    }

                    this.mods = mods;
                }
            }

            this.dataVersion = 1;
            this.save(false);
        }

        // changes to the way libraries are saved and loaded from disk
        if (this.dataVersion < 2) {
            this.libraries = new ArrayList<>();

            if (this.librariesNeeded != null) {
                this.libraries.addAll(Arrays.asList(this.librariesNeeded.split(",")));
            }

            this.dataVersion = 2;
            this.save(false);
        }
    }

    public void save() {
        this.save(true);
    }

    public void save(boolean showToast) {
        Writer writer;
        try {
            writer = new FileWriter(FileSystem.INSTANCES.resolve(this.getSafeName() + "/instance.json").toFile());
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to open instance.json for writing", e);
            return;
        }

        try {
            writer.write(Gsons.DEFAULT.toJson(this));
            writer.flush();

            if (showToast) {
                // #. {0} is the name of the instance
                App.TOASTER.pop(GetText.tr("Instance {0} Saved!", this.getName()));
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to write instance.json", e);
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                LogManager.logStackTrace("Failed to close instance.json writer", e);
            }
        }
    }

    public Instance convertToNewFormat(Path path) throws Exception {
        VersionManifest versionManifest = com.atlauncher.network.Download.build().cached()
                .setUrl(String.format("%s/mc/game/version_manifest.json", Constants.LAUNCHER_META_MINECRAFT))
                .asClass(VersionManifest.class);

        VersionManifestVersion minecraftVersionManifest = versionManifest.versions.stream()
                .filter(version -> version.id.equalsIgnoreCase(minecraftVersion)).findFirst().orElse(null);

        if (minecraftVersionManifest == null) {
            throw new Exception(String.format("Failed to find Minecraft version of %s", minecraftVersion));
        }

        com.atlauncher.data.minecraft.MinecraftVersion theMinecraftVersion = com.atlauncher.network.Download.build()
                .cached().setUrl(minecraftVersionManifest.url)
                .asClass(com.atlauncher.data.minecraft.MinecraftVersion.class);

        Instance instance = new Instance(theMinecraftVersion);
        instance.ROOT = path;
        instance.libraries.addAll(0, libraries.stream().filter(l -> !l.contains("/")).map(l -> {
            Library library = new Library();
            library.name = l;

            Path file = FileSystem.LIBRARIES.resolve(l);
            Download artifact = new Download();
            artifact.path = l;
            artifact.sha1 = com.atlauncher.utils.Hashing.sha1(file).toString();
            artifact.size = file.toFile().length();

            Downloads downloads = new Downloads();
            downloads.artifact = artifact;
            library.downloads = downloads;

            return library;
        }).collect(Collectors.toList()));
        instance.mainClass = mainClass;
        instance.arguments = theMinecraftVersion.arguments;

        if (extraArguments != null && extraArguments.split(" ").length != 0) {
            List<ArgumentRule> args = Arrays.asList(extraArguments.split(" ")).stream()
                    .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList());

            if (instance.arguments == null) {
                instance.arguments = new Arguments();

                if (minecraftArguments != null) {
                    instance.arguments.game.addAll(Arrays.stream(minecraftArguments.split(" "))
                            .map(arg -> new ArgumentRule(null, arg)).collect(Collectors.toList()));
                }
            }

            instance.arguments.game.addAll(args);
        }

        InstanceLauncher instanceLauncher = new InstanceLauncher();
        instanceLauncher.name = name;
        instanceLauncher.version = version;
        instanceLauncher.java = java;
        instanceLauncher.enableCurseForgeIntegration = enableCurseIntegration;
        instanceLauncher.enableEditingMods = enableEditingMods;
        instanceLauncher.loaderVersion = loaderVersion;
        instanceLauncher.isDev = isDev;
        instanceLauncher.isPlayable = true;
        instanceLauncher.mods = mods;
        instanceLauncher.requiredMemory = memory;
        instanceLauncher.requiredPermGen = permgen;
        instanceLauncher.assetsMapToResources = this.assetsMapToResources;
        instanceLauncher.ignoredUpdates = ignoredUpdates;

        Pack thePack = Optional.ofNullable(realPack).orElseGet(() -> PackManager.getPackByName(pack));

        if (thePack != null) {
            instanceLauncher.pack = thePack.name;
            instanceLauncher.description = thePack.description;
            instanceLauncher.packId = thePack.id;
        } else {
            instanceLauncher.pack = name;
            instanceLauncher.description = "";
            instanceLauncher.packId = 0;
        }

        if (isDev) {
            instanceLauncher.hash = hash;
        }

        instance.launcher = instanceLauncher;

        return instance;
    }
}
