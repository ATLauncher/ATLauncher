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
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.json.ModType;
import com.atlauncher.data.version.PackVersion;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.mclauncher.LegacyMCLauncher;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.walker.SendOpenEyeReportsVisitor;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class handles contains information about a single Instance in the Launcher. An Instance being an installed
 * version of a ModPack separate to others by file structure.
 */
public class Instance implements Cloneable {
    /**
     * The name of the Instance.
     */
    private String name;

    /**
     * The name of the Pack this instance is for.
     */
    private String pack;

    /**
     * The username of the user who installed this if it's set to be for that user only.
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
     * The minimum RAM/memory recommended for this Instance by the pack developer/s.
     */
    private int memory = 0;

    /**
     * The minimum PermGen/MetaSpace recommended for this Instance by the pack developer/s.
     */
    private int permgen = 0;

    /**
     * Comma separated list of the order of Jar's to be added to the class path when launching Minecraft.
     */
    private String jarOrder;

    /**
     * Comma seperated list of the libraries needed by Minecraft/Forge to be added to the class path when launching
     * Minecraft.
     */
    private String librariesNeeded = null;

    /**
     * The extra arguments to be added to the command when launching Minecraft. Generally involves things such as the
     * tweakClass/s for Forge.
     */
    private String extraArguments = null;

    /**
     * The arguments required by Minecraft to be added to the command when launching Minecraft. Generally involves thing
     * such as handling of authentication, assets paths etc.
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
     * If this instance has been converted or not from the old format.
     */
    private boolean isConverted = false;

    /**
     * The Pack object for the pack this Instance was installed from. This is not stored in the instances instance.json
     * file as Pack's can be deleted from the system.
     *
     * @see com.atlauncher.data.Pack
     */
    private transient Pack realPack;

    /**
     * If this Instance was installed from a development version of the Pack.
     */
    private boolean isDev;

    /**
     * If this Instance is playable or not. It may become unplayable after a failed update or if files are found
     * corrupt.
     */
    private boolean isPlayable;

    /**
     * If this instance uses the MCLauncher or the LegacyMCLauncher class to load Minecraft.
     *
     * @see com.atlauncher.mclauncher.MCLauncher
     * @see com.atlauncher.mclauncher.LegacyMCLauncher
     */
    private boolean newLaunchMethod;

    /**
     * List of DisableableMod objects for the mods in the Instance.
     *
     * @see com.atlauncher.data.DisableableMod
     */
    private List<DisableableMod> mods;

    /**
     * List of versions of the Pack this instance comes from that the user has said to not be reminded about updating
     * to.
     */
    private List<String> ignoredUpdates;

    public final transient Path root;

    /**
     * Instantiates a new instance.
     *
     * @param name the name of the Instance
     * @param pack the name of the Pack this Instance is of
     * @param realPack the Pack object for the Pack this Instance is of
     * @param enableUserLock if this instance is only meant to be used by the original installer
     * @param version the version of the Pack this Instance is of
     * @param minecraftVersion the Minecraft version this Instance runs off
     * @param memory the minimum RAM/memory as recommended by the pack developer/s
     * @param permgen the minimum PermGen/Metaspace as recommended by the pack developer/s
     * @param mods the mods installed in this Instance
     * @param jarOrder the order that jar mods are loaded into the class path
     * @param librariesNeeded the libraries needed to launch Minecraft
     * @param extraArguments the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass the main class to run when launching Minecraft
     * @param assets the assets version being used by Minecraft
     * @param isDev if this Instance is using a dev version of the pack
     * @param isPlayable if this instance is playable
     * @param newLaunchMethod if this instance is using the new launch method for Minecraft
     */
    public Instance(String name, String pack, Pack realPack, boolean enableUserLock, String version, String
            minecraftVersion, int memory, int permgen, List<DisableableMod> mods, String jarOrder, String
            librariesNeeded, String extraArguments, String minecraftArguments, String mainClass, String assets,
                    boolean isDev, boolean isPlayable, boolean newLaunchMethod) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.memory = memory;
        this.permgen = permgen;
        this.mods = mods;
        this.jarOrder = jarOrder;
        this.librariesNeeded = librariesNeeded;
        this.mainClass = mainClass;
        this.assets = assets;
        this.jarOrder = jarOrder;
        this.extraArguments = extraArguments;
        this.minecraftArguments = minecraftArguments;
        this.isDev = isDev;
        this.isPlayable = isPlayable;
        this.newLaunchMethod = newLaunchMethod;

        Account account = AccountManager.getActiveAccount();
        if (enableUserLock && !account.isUUIDNull()) {
            this.userLock = account.getUUIDNoDashes();
        } else {
            this.userLock = null;
        }
        this.isConverted = true;
        this.root = FileSystem.INSTANCES.resolve(this.getSafeName());
    }

    /**
     * Instantiates a new instance with it defaulting to being playable.
     *
     * @param name the name of the Instance
     * @param pack the name of the Pack this Instance is of
     * @param realPack the Pack object for the Pack this Instance is of
     * @param enableUserLock if this instance is only meant to be used by the original installer
     * @param version the version of the Pack this Instance is of
     * @param minecraftVersion the Minecraft version this Instance runs off
     * @param memory the minimum RAM/memory as recommended by the pack developer/s
     * @param permgen the minimum PermGen/Metaspace as recommended by the pack developer/s
     * @param mods the mods installed in this Instance
     * @param jarOrder the order that jar mods are loaded into the class path
     * @param librariesNeeded the libraries needed to launch Minecraft
     * @param extraArguments the extra arguments for launching the pack
     * @param minecraftArguments the arguments needed by Minecraft to run
     * @param mainClass the main class to run when launching Minecraft
     * @param assets the assets version being used by Minecraft
     * @param isDev if this Instance is using a dev version of the pack
     * @param newLaunchMethod if this instance is using the new launch method for Minecraft
     */
    public Instance(String name, String pack, Pack realPack, boolean enableUserLock, String version, String
            minecraftVersion, int memory, int permgen, List<DisableableMod> mods, String jarOrder, String
            librariesNeeded, String extraArguments, String minecraftArguments, String mainClass, String assets,
                    boolean isDev, boolean newLaunchMethod) {
        this(name, pack, realPack, enableUserLock, version, minecraftVersion, memory, permgen, mods, jarOrder,
                librariesNeeded, extraArguments, minecraftArguments, mainClass, assets, isDev, true, newLaunchMethod);
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
     * Sets a new name for this Instance. Used primarily when renaming a cloned instance.
     *
     * @param newName the new name for this Instance
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Gets the safe name of the Instance used in file paths. Removes all non alphanumeric characters.
     *
     * @return the safe name of the Instance.
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets the name of the Pack this Instance was created from. Pack's can be deleted/removed in the future.
     *
     * @return the name of the Pack the Instance was created from.
     */
    public String getPackName() {
        return pack;
    }

    /**
     * Checks if the Instance has mods installed.
     *
     * @return true if there are mods installed in the Instance
     */
    public boolean hasInstalledMods() {
        return (this.mods == null ? false : (this.mods.size() >= 1 ? true : false));
    }

    /**
     * Gets the order to load any jar mods into the class path when launching Minecraft.
     *
     * @return comma separated list of filenames to jar mods in their correct loading order
     */
    public String getJarOrder() {
        return this.jarOrder;
    }

    /**
     * Sets the order to load the jars from the jarmods folder.
     *
     * @param jarOrder comma separated list of filenames for the order to load the mods from the jarmods folder
     */
    public void setJarOrder(String jarOrder) {
        this.jarOrder = jarOrder;
    }

    /**
     * Gets the minimum recommended RAM/memory for this Instance based off what the Pack specifies. Defaults to 0 if
     * there is none specified by the pack. Value is in MB.
     *
     * @return the minimum RAM/memory recommended for this Instance in MB
     */
    public int getMemory() {
        return this.memory;
    }

    /**
     * Sets the minimum recommended RAM/memory for this Instance in MB.
     *
     * @param memory the minimum recommended RAM/memory for this Instance in MB
     */
    public void setMemory(int memory) {
        this.memory = memory;
    }

    /**
     * Gets a List of the installed mods in this Instance. Mods are listed as DisableableMod objects.
     *
     * @return a List of DisableableMod objects of the installed mods in this instance or null if none
     */
    public List<DisableableMod> getInstalledMods() {
        return this.mods;
    }

    /**
     * Gets the minimum recommended PermGen/Metaspace size for this Instance based off what the Pack specifies. Defaults
     * to 0 if there is non specified by the pack. Value is in MB.
     *
     * @return the minimum PermGen/Metaspace recommended for this Instance in MB
     */
    public int getPermGen() {
        return this.permgen;
    }

    /**
     * Renames this instance including renaming the folder in the Instances directory to the new name provided.
     *
     * @param newName the new name of the Instance
     * @return true if the Instances folder was renamed and false if it failed
     */
    public boolean rename(String newName) {
        String oldName = this.name;
        Path oldDir = this.getRootDirectory();
        this.name = newName;

        Path newDir = this.getRootDirectory();
        if (FileUtils.moveDirectory(oldDir, newDir)) {
            return true;
        } else {
            this.name = oldName;
            return false;
        }
    }

    /**
     * Gets the name of the Pack this Instance was created from in a safe manner by removing all non alphanumeric
     * characters which is then safe for use inside file paths and URL's.
     *
     * @return the safe name of the Pack
     */
    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets a ImageIcon object for the image file of the Pack for use in displaying in the Packs and Instances tabs.
     *
     * @return ImageIcon for this Instances Pack
     */
    public ImageIcon getImage() {
        Path customImage = this.getRootDirectory().resolve("instance.png");
        Path instancesImage = FileSystem.IMAGES.resolve(getSafePackName().toLowerCase() + ".png");

        if (Files.exists(customImage)) {
            try {
                BufferedImage img = ImageIO.read(customImage.toFile());
                Image dimg = img.getScaledInstance(300, 150, Image.SCALE_SMOOTH);
                return new ImageIcon(dimg);
            } catch (IOException e) {
                LogManager.logStackTrace("Error creating scaled image from the custom image of instance " + this
                        .getName(), e);
            }
        }

        if (Files.exists(instancesImage)) {
            return Utils.getIconImage(instancesImage);

        } else {
            return Utils.getIconImage(FileSystem.IMAGES.resolve("defaultimage.png"));
        }
    }

    /**
     * Gets the description of the Pack this Instance was installed from if it's still available in the Launcher. If the
     * pack no longer exists then it simply returns "No Description".
     *
     * @return the description of the Pack this Instance was created from
     */
    public String getPackDescription() {
        if (this.realPack != null) {
            return this.realPack.getDescription();
        } else {
            return LanguageManager.localize("pack.nodescription");
        }
    }

    /**
     * Checks if this Instance has been converted or not from the old arguments storage.
     *
     * @return true if this Instance has already been converted
     */
    public boolean hasBeenConverted() {
        return this.isConverted;
    }

    /**
     * Checks to see if Leaderboards are enabled for the Pack this Instance was created from. If the pack no longer
     * exists we don't allow logging of Leaderboard statistics.
     *
     * @return true if Leaderboard are enabled and statistics can be sent
     */
    public boolean isLeaderboardsEnabled() {
        return (this.realPack != null && this.realPack.isLeaderboardsEnabled());
    }

    /**
     * Checks to see if Logging is enabled for the Pack this Instance was created from. If the pack no longer exists we
     * don't allow logging.
     *
     * @return true if Logging is enabled
     */
    public boolean isLoggingEnabled() {
        return (this.realPack != null && this.realPack.isLoggingEnabled());
    }

    /**
     * This stops the popup informing a user that this Instance has an update when they go to play this Instance. It
     * will simply deny the current version from showing up again informing the user when their Instance is not using
     * the latest version.
     */
    public void ignoreUpdate() {
        if (this.ignoredUpdates == null) {
            this.ignoredUpdates = new ArrayList<String>();
        }

        String version;

        if (this.isDev) {
            version = getLatestDevHash();
        } else {
            version = getLatestVersion();
        }

        if (!hasUpdateBeenIgnored(version)) {
            this.ignoredUpdates.add(version);
            InstanceManager.saveInstances();
        }
    }

    /**
     * Checks to see if a given version has been ignored from showing update prompts when the Instance is played.
     *
     * @param version the version to check if it's been ignored in the past
     * @return true if the user has chosen to ignore updates for the given version
     */
    public boolean hasUpdateBeenIgnored(String version) {
        if (version == null || ignoredUpdates == null || ignoredUpdates.size() == 0) {
            return false;
        }
        for (String ignoredVersion : ignoredUpdates) {
            if (ignoredVersion.equalsIgnoreCase(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This converts an old Instance using old Minecraft argument storage to the new method of storage.
     */
    public void convert() {
        if (this.minecraftArguments != null) {
            // Minecraft arguments are now extraArguments if found
            this.extraArguments = this.minecraftArguments;
            this.minecraftArguments = null;
        }
        this.isConverted = true;
    }

    /**
     * This removes a given DisableableMod object and removes it from the list of installed mods as well as deleting the
     * file.
     *
     * @param mod the DisableableMod object for the mod to remove
     */
    public void removeInstalledMod(DisableableMod mod) {
        FileUtils.delete((mod.isDisabled() ? mod.getDisabledFilePath(this) : mod.getFilePath(this)));
        this.mods.remove(mod); // Remove mod from mod List
    }

    /**
     * Gets the version of the Pack that this Instance is based off.
     *
     * @return the version of the Pack this Instance is based off
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version of the Pack this Instance was created from.
     *
     * @param version the version of the Pack this Instance was created from
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the Minecraft Version that this Instance uses.
     *
     * @return the Minecraft Version that this Instance uses
     */
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    /**
     * Sets the Minecraft version of the Pack this Instance was created from.
     *
     * @param minecraftVersion the new minecraft version
     */
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    /**
     * Gets a File object for the root directory of this Instance.
     *
     * @return File object for the root directory of this Instance
     */
    public Path getRootDirectory() {
        return FileSystem.INSTANCES.resolve(getSafeName());
    }

    /**
     * Gets a File object for the directory where the assets for this version of Minecraft are stored.
     *
     * @return File object for the assets directory used by Minecraft
     */
    public Path getAssetsDir() {
        return FileSystem.RESOURCES_VIRTUAL.resolve(getAssets());
    }

    /**
     * Gets a File object for the saves directory of this Instance.
     *
     * @return File object for the saves directory of this Instance
     */
    public Path getSavesDirectory() {
        return this.getRootDirectory().resolve("saves");
    }

    /**
     * Gets a File object for the reports directory of this Instance where OpenEye stores it's pending crash reports.
     *
     * @return File object for the reports directory of this Instance
     */
    public Path getReportsDirectory() {
        return this.getRootDirectory().resolve("reports");
    }

    /**
     * Gets a File object for the mods directory of this Instance.
     *
     * @return File object for the mods directory of this Instance
     */
    public Path getModsDirectory() {
        return this.getRootDirectory().resolve("mods");
    }

    /**
     * Gets a File object for the IC2 library directory of this Instance.
     *
     * @return File object for the IC2 library directory of this Instance
     */
    public Path getIC2LibDirectory() {
        return this.getModsDirectory().resolve("ic2");
    }

    /**
     * Gets a File object for the denlib directory of this Instance.
     *
     * @return File object for the denlib directory of this Instance
     */
    public Path getDenLibDirectory() {
        return this.getModsDirectory().resolve("denlib");
    }

    /**
     * Gets a File object for the plugins directory of this Instance.
     *
     * @return File object for the plugins directory of this Instance
     */
    public Path getPluginsDirectory() {
        return this.getRootDirectory().resolve("plugins");
    }

    /**
     * Gets a File object for the shader packs directory of this Instance.
     *
     * @return File object for the shader packs directory of this Instance
     */
    public Path getShaderPacksDirectory() {
        return this.getRootDirectory().resolve("shaderpacks");
    }

    /**
     * Gets a File object for the disabled mods directory of this Instance.
     *
     * @return File object for the disabled mods directory of this Instance
     */
    public Path getDisabledModsDirectory() {
        return this.getRootDirectory().resolve("disabledmods");
    }

    /**
     * Gets a File object for the core mods directory of this Instance.
     *
     * @return File object for the core mods directory of this Instance
     */
    public Path getCoreModsDirectory() {
        return this.getRootDirectory().resolve("coremods");
    }

    /**
     * Gets a File object for the jar mods directory of this Instance.
     *
     * @return File object for the jar mods directory of this Instance
     */
    public Path getJarModsDirectory() {
        return this.getRootDirectory().resolve("jarmods");
    }

    /**
     * Gets a File object for the texture packs directory of this Instance.
     *
     * @return File object for the texture packs directory of this Instance
     */
    public Path getTexturePacksDirectory() {
        return this.getRootDirectory().resolve("texturepacks");
    }

    /**
     * Gets a File object for the resource packs directory of this Instance.
     *
     * @return File object for the resource packs directory of this Instance
     */
    public Path getResourcePacksDirectory() {
        return this.getRootDirectory().resolve("resourcepacks");
    }

    /**
     * Gets a File object for the bin directory of this Instance.
     *
     * @return File object for the bin directory of this Instance
     */
    public Path getBinDirectory() {
        return this.getRootDirectory().resolve("bin");
    }

    /**
     * Gets a File object for the natives directory of this Instance.
     *
     * @return File object for the natives directory of this Instance
     */
    public Path getNativesDirectory() {
        return this.getBinDirectory().resolve("natives");
    }

    /**
     * Gets a File object for the minecraft.jar of this Instance.
     *
     * @return File object for the minecraft.jar of this Instance
     */
    public Path getMinecraftJar() {
        return this.getBinDirectory().resolve("minecraft.jar");
    }

    /**
     * Checks if the pack associated with this Instance can be installed.
     *
     * @return true if the Pack this Instance was made from can be installed
     * @see com.atlauncher.data.Pack#canInstall
     */
    public boolean canInstall() {
        return (this.realPack != null && this.realPack.canInstall());
    }

    /**
     * Gets the Pack object that this Instance was created from. If it doesn't exist, this will return null
     *
     * @return Pack object of the Pack this Instance was created from or null if no longer available
     */
    public Pack getRealPack() {
        return this.realPack;
    }

    /**
     * Sets the Pack object that this Instance was created from. Defaults to null when loaded.
     *
     * @param realPack the Pack object that this Instance was created from
     */
    public void setRealPack(Pack realPack) {
        this.realPack = realPack;
    }

    /**
     * Checks if this Instance has installed jar mods.
     *
     * @return true if there are jar mods
     */
    public boolean hasJarMods() {
        return this.jarOrder != null;
    }

    /**
     * Sets the minimum recommended PermGen/Metaspace size for this Instance in MB.
     *
     * @param permgen the minimum recommended PermGen/Metaspace for this Instance in MB
     */
    public void setPermgen(int permgen) {
        this.permgen = permgen;
    }

    /**
     * Sets this Instance as playable after it is marked unplayable and has been rectified.
     */
    public void setPlayable() {
        this.isPlayable = true;
    }

    /**
     * Sets this Instance as unplayable so the user cannot play the Instance. Used when installs go bad or files are
     * found that corrupts the Instance
     */
    public void setUnplayable() {
        this.isPlayable = false;
    }

    /**
     * Sets this Instance as a dev version.
     */
    public void setDevVersion() {
        this.isDev = true;
    }

    /**
     * Sets this Instance as a non dev version.
     */
    public void setNotDevVersion() {
        this.isDev = false;
        this.hash = null;
    }

    /**
     * Sets this Instances hash for dev versions.
     */
    public void setHash(String hash) {
        this.hash = hash;
    }

    /**
     * Checks if the version of the Pack this Instance was created from was a dev version.
     *
     * @return true if the version of the Pack used to create this Instance was a dev version
     */
    public boolean isDev() {
        return this.isDev;
    }

    /**
     * Checks if the Instance is playable.
     *
     * @return true if the Instance is playable
     */
    public boolean isPlayable() {
        return this.isPlayable;
    }

    /**
     * Sets the launch method used to launch this Instance.
     *
     * @param newLaunchMethod true if the new launch menthod should be used, false for the legacy launch method
     */
    public void setIsNewLaunchMethod(boolean newLaunchMethod) {
        this.newLaunchMethod = newLaunchMethod;
    }

    /**
     * Checks if this Instance uses the new launch method or not.
     *
     * @return true if this Instance uses the new launch method
     */
    public boolean isNewLaunchMethod() {
        return this.newLaunchMethod;
    }

    /**
     * Gets the libraries needed to be loaded when launching Minecraft.
     *
     * @return a comma separated list of filenames for the libraries to be loaded when Minecraft is started
     */
    public String getLibrariesNeeded() {
        return this.librariesNeeded;
    }

    /**
     * Sets the list of libraries needed to be loaded when launching Minecraft.
     *
     * @param librariesNeeded a comma separated list of filenames for the libraries to be loaded when Minecraft is
     * started
     */
    public void setLibrariesNeeded(String librariesNeeded) {
        this.librariesNeeded = librariesNeeded;
    }

    /**
     * Checks if there are extra arguments set for this Instance.
     *
     * @return true if there are set extra arguments for this Instance
     */
    public boolean hasExtraArguments() {
        return this.extraArguments != null;
    }

    /**
     * Gets the extra arguments for the Instance which is added to the command argument when launching Minecraft.
     *
     * @return the extra arguments used by the Instance when launching Minecraft
     */
    public String getExtraArguments() {
        return this.extraArguments;
    }

    /**
     * Sets the extra arguments for the Instance which is added to the command argument when launching Minecraft.
     *
     * @param extraArguments the new extra arguments used by the Instance when launching Minecraft
     */
    public void setExtraArguments(String extraArguments) {
        this.extraArguments = extraArguments;
    }

    /**
     * Checks if there are Minecraft arguments set for this Instance.
     *
     * @return true if there are set Minecraft arguments for this Instance
     */
    public boolean hasMinecraftArguments() {
        return this.minecraftArguments != null;
    }

    /**
     * Gets the Minecraft arguments for the Instance which is added to the command argument when launching Minecraft.
     * These involve things like asset directories, token input among other things.
     *
     * @return the Minecraft arguments used by the Instance when launching Minecraft
     */
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    /**
     * Sets the Minecraft arguments for the Instance which is added to the command argument when launching Minecraft.
     * These involve things like asset directories, token input among other things.
     *
     * @param minecraftArguments the new Minecraft arguments used by the Instance when launching Minecraft
     */
    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    /**
     * Gets the main class used to launch Minecraft.
     *
     * @return the main class used to launch Minecraft
     */
    public String getMainClass() {
        return this.mainClass;
    }

    /**
     * Sets the main class used to launch Minecraft.
     *
     * @param mainClass the new main class used to launch Minecraft
     */
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    /**
     * Gets the assets value which Minecraft uses to determine how to load assets in the game.
     *
     * @return the assets value
     */
    public String getAssets() {
        return (this.assets == null ? "legacy" : this.assets);
    }

    /**
     * Sets the assets value which Minecraft uses to determine how to load assets in the game.
     *
     * @param assets the new assets value
     */
    public void setAssets(String assets) {
        this.assets = assets;
    }

    /**
     * Checks if this Instance can be played. This refers only to the account and permission side of things and doesn't
     * reference if the instance is playable or as determined by the {@link com.atlauncher.data.Instance#isPlayable}
     * field.
     *
     * @return true if the user can play this Instance
     */
    public boolean canPlay() {
        Account account = AccountManager.getActiveAccount();

        // Make sure an account is selected first.
        if (account == null || !account.isReal()) {
            return false;
        }

        // Check to see if this was a private Instance belonging to a specific user only.
        if (this.userLock != null && !account.getUUIDNoDashes().equalsIgnoreCase(this.userLock)) {
            return false;
        }

        // All good, no false returns yet so allow it.
        return true;
    }

    public String getInstalledBy() {
        return this.installedBy;
    }

    public void removeInstalledBy() {
        this.installedBy = null;
    }

    public String getUserLock() {
        return this.userLock;
    }

    public void removeUserLock() {
        this.userLock = null;
    }

    public void setUserLock(String lock) {
        this.userLock = lock;
    }

    /**
     * Checks if the Pack this Instance was created from has an update.
     *
     * @return true if there is an update to the Pack this Instance was created from
     */
    public boolean hasUpdate() {
        // Check to see if there is a Pack object defined first.
        if (this.realPack != null) {
            // Then check if the Pack has any versions associated with it and were NOT running a dev
            // version, as dev versions should never be updated.
            if (this.realPack.hasVersions() && !isDev()) {
                // Lastly check if the current version we installed is different than the latest
                // version of the Pack and that the latest version of the Pack is not restricted to
                // disallow updates.
                if (!this.realPack.getLatestVersion().getVersion().equalsIgnoreCase(this.version) && !this.realPack
                        .isLatestVersionNoUpdate()) {
                    return true;
                }
            }
            if (isDev() && (this.hash != null)) {
                PackVersion devVersion = this.realPack.getDevVersionByName(this.version);
                if (devVersion != null && !devVersion.hashMatches(this.hash)) {
                    return true;
                }
            }
        }

        // If we triggered nothing then there is no update.
        return false;
    }

    /**
     * Gets the latest version of the Pack this Instance was created from. If the Pack has been removed or it has no
     * published versions then it will return null.
     *
     * @return the latest version of the Pack this Instance was created from or null if the Pack no longer exists or
     * there is no versions of the Pack
     */
    public String getLatestVersion() {
        return (this.realPack != null ? (this.realPack.getLatestVersion() == null ? null : this.realPack
                .getLatestVersion().getVersion()) : null);
    }

    public String getLatestDevHash() {
        return (this.realPack != null ? (this.realPack.getLatestDevVersion() == null ? null : this.realPack
                .getLatestDevVersion().getHash()) : null);
    }

    /**
     * Checks is a mod was installed with this Instance.
     *
     * @param name the name of the mod
     * @return true if the mod was installed with the Instance
     */
    public boolean wasModInstalled(String name) {
        if (this.mods != null) {
            for (DisableableMod mod : this.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Sets the mods installed for this Instance.
     *
     * @param mods List of {@link com.atlauncher.data.DisableableMod} objects of the mods installed with this Instance.
     */
    public void setModsInstalled(List<DisableableMod> mods) {
        this.mods = mods;
    }

    /**
     * Starts the process to launch this Instance.
     *
     * @return true if the Minecraft process was started
     */
    public boolean launch() {
        final Account account = AccountManager.getActiveAccount();
        if (account == null) {
            String[] options = {LanguageManager.localize("common.ok")};
            JOptionPane.showOptionDialog(App.frame, LanguageManager.localize("instance.noaccount"),
                    LanguageManager.localize("instance.noaccountselected"), JOptionPane.DEFAULT_OPTION, JOptionPane
                            .ERROR_MESSAGE, null, options, options[0]);
            App.settings.setMinecraftLaunched(false);
            return false;
        } else {
            if ((SettingsManager.getMaximumMemory() < this.memory) && (this.memory <= Utils.getSafeMaximumRam())) {
                String[] options = {LanguageManager.localize("common.yes"), LanguageManager.localize("common.no")};
                int ret = JOptionPane.showOptionDialog(App.frame, HTMLUtils.centerParagraph
                                (LanguageManager.localizeWithReplace("instance.insufficientram", "<b>" + this.memory
                                        + "</b> " +
                                "MB<br/><br/>")), LanguageManager.localize("instance.insufficientramtitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }
            if (SettingsManager.getPermGen() < this.permgen) {
                String[] options = {LanguageManager.localize("common.yes"), LanguageManager.localize("common.no")};
                int ret = JOptionPane.showOptionDialog(App.frame, HTMLUtils.centerParagraph
                                (LanguageManager.localizeWithReplace("instance.insufficientpermgen", "<b>" + this
                                        .permgen + "</b> " + "MB<br/><br/>")), LanguageManager.localize("instance" +
                                ".insufficientpermgentitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    LogManager.warn("Launching of instance cancelled due to user cancelling permgen warning!");
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }


            LogManager.info("Logging into Minecraft!");
            final ProgressDialog dialog = new ProgressDialog(LanguageManager.localize("account.loggingin"), 0,
                    LanguageManager.localize("account.loggingin"), "Aborted login to Minecraft!");
            dialog.addThread(new Thread() {
                public void run() {
                    dialog.setReturnValue(account.login());
                    dialog.close();
                }
            });
            dialog.start();

            final LoginResponse session = (LoginResponse) dialog.getReturnValue();

            if (session == null) {
                return false;
            }

            Thread launcher = new Thread() {
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        if (App.frame != null) {
                            App.frame.setVisible(false);
                        }
                        // Create a note of worlds for auto backup if enabled
                        HashMap<String, Long> preWorldList = new HashMap<String, Long>();
                        if (SettingsManager.isAdvancedBackupsEnabled() && SettingsManager.getAutoBackup()) {
                            if (Files.exists(Instance.this.getSavesDirectory())) {
                                File[] files = Instance.this.getSavesDirectory().toFile().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if (file.isDirectory()) {
                                            preWorldList.put(file.getName(), file.lastModified());
                                        }
                                    }
                                }
                            }
                        }

                        LogManager.info("Launching pack " + getPackName() + " " + getVersion() + " for " +
                                "Minecraft " + getMinecraftVersion());

                        Process process = null;
                        if (isNewLaunchMethod()) {
                            process = MCLauncher.launch(account, Instance.this, session);
                        } else {
                            process = LegacyMCLauncher.launch(account, Instance.this, session);
                        }

                        if (!SettingsManager.keepLauncherOpen() && !SettingsManager.enableLogs()) {
                            System.exit(0);
                        }

                        App.settings.showKillMinecraft(process);
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            if (!LogManager.showDebug) {
                                line = line.replace(account.getMinecraftUsername(), "**MINECRAFTUSERNAME**");
                                line = line.replace(account.getUsername(), "**MINECRAFTUSERNAME**");
                                if (account.hasAccessToken()) {
                                    line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
                                }
                                if (account.hasUUID()) {
                                    line = line.replace(account.getUUID(), "**UUID**");
                                }
                            }
                            LogManager.minecraft(line);
                        }
                        App.settings.hideKillMinecraft();
                        if (App.frame != null && SettingsManager.keepLauncherOpen()) {
                            App.frame.setVisible(true);
                        }
                        long end = System.currentTimeMillis();
                        if (App.settings.isInOfflineMode() && !App.forceOfflineMode) {
                            App.settings.checkOnlineStatus();
                        }
                        int exitValue = 0; // Assume we exited fine
                        try {
                            exitValue = process.exitValue(); // Try to get the real exit value
                        } catch (IllegalThreadStateException e) {
                            process.destroy(); // Kill the process
                        }
                        if (!SettingsManager.keepLauncherOpen()) {
                            App.console.setVisible(false); // Hide the console to pretend we've closed
                        }
                        if (exitValue != 0) {
                            // Submit any pending crash reports from Open Eye if need to since we
                            // exited abnormally
                            if (SettingsManager.enableLogs() && SettingsManager.enableOpenEyeReporting()) {
                                App.TASKPOOL.submit(new Runnable() {
                                    public void run() {
                                        sendOpenEyePendingReports();
                                    }
                                });
                            }
                        } else if (SettingsManager.isAdvancedBackupsEnabled() && SettingsManager.getAutoBackup()) {
                            // Begin backup
                            if (Files.exists(Instance.this.getSavesDirectory())) {
                                File[] files = Instance.this.getSavesDirectory().toFile().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if ((file.isDirectory()) && (!file.getName().equals("NEI"))) {
                                            if (preWorldList.containsKey(file.getName())) {
                                                // Only backup if file changed
                                                if (!(preWorldList.get(file.getName()) == file.lastModified())) {
                                                    SyncAbstract sync = SyncAbstract.syncList.get(SettingsManager
                                                            .getLastSelectedSync());
                                                    sync.backupWorld(file.getName() + String.valueOf(file
                                                            .lastModified()), file, Instance.this);
                                                }
                                            }
                                            // Or backup if a new file is found
                                            else {
                                                SyncAbstract sync = SyncAbstract.syncList.get(SettingsManager
                                                        .getLastSelectedSync());
                                                sync.backupWorld(file.getName() + String.valueOf(file.lastModified())
                                                        .replace(":", ""), file, Instance.this);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        App.settings.setMinecraftLaunched(false);
                        if (!App.settings.isInOfflineMode()) {
                            if (isLeaderboardsEnabled() && isLoggingEnabled() && !isDev() && SettingsManager.enableLogs
                                    ()) {
                                final int timePlayed = (int) (end - start) / 1000;
                                if (timePlayed > 0) {
                                    App.TASKPOOL.submit(new Runnable() {
                                        public void run() {
                                            addTimePlayed(timePlayed, (isDev ? "dev" : getVersion()));
                                        }
                                    });
                                }
                            }
                            if (SettingsManager.keepLauncherOpen() && App.settings.hasUpdatedFiles()) {
                                App.settings.reloadLauncherData();
                            }
                        }
                        if (!SettingsManager.keepLauncherOpen()) {
                            System.exit(0);
                        }
                    } catch (IOException e1) {
                        LogManager.logStackTrace(e1);
                    }
                }
            };
            launcher.start();
            return true;
        }

    }

    /**
     * Send open eye pending reports from the instance.
     */
    public void sendOpenEyePendingReports() {
        Path reportsDir = this.getReportsDirectory();

        if (Files.exists(reportsDir)) {
            try {
                Files.walkFileTree(reportsDir, new SendOpenEyeReportsVisitor());
            } catch (IOException e) {
                LogManager.logStackTrace("Error while sending OpenEye reports!", e);
            }
        }
    }

    public String addTimePlayed(int time, String version) {
        Map<String, Object> request = new HashMap<String, Object>();

        if (SettingsManager.enableLeaderboards()) {
            request.put("username", AccountManager.getActiveAccount().getMinecraftUsername());
        } else {
            request.put("username", null);
        }
        request.put("version", version);
        request.put("time", time);

        try {
            return Utils.sendAPICall("pack/" + getRealPack().getSafeName() + "/timeplayed/", request);
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
        return "Leaderboard Time Not Added!";
    }

    /**
     * Clones a given instance of this class.
     *
     * @return Instance The cloned instance
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            LogManager.logStackTrace(e);
        }
        return null;
    }

    public boolean hasCustomMods() {
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded()) {
                return true;
            }
        }
        return false;
    }

    public List<String> getCustomMods(ModType type) {
        List<String> customMods = new ArrayList<String>();
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded() && mod.getType() == type) {
                customMods.add(mod.getFilename());
            }
        }
        return customMods;
    }

    public List<DisableableMod> getCustomDisableableMods() {
        List<DisableableMod> customMods = new ArrayList<DisableableMod>();
        for (DisableableMod mod : this.mods) {
            if (mod.isUserAdded()) {
                customMods.add(mod);
            }
        }
        return customMods;
    }

    public void save() {
        try {
            FileWriter writer = null;
            try {
                writer = new FileWriter(this.getRootDirectory().resolve("instance.json").toFile());
                writer.write(Gsons.DEFAULT.toJson(this));
                writer.flush();
                App.TOASTER.pop("Instance " + this.getName());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    public ArrayList<String> getInstalledOptionalModNames() {
        ArrayList<String> installedOptionalMods = new ArrayList<String>();

        for (DisableableMod mod : this.getInstalledMods()) {
            if (mod.isOptional() && !mod.isUserAdded()) {
                installedOptionalMods.add(mod.getName());
            }
        }

        return installedOptionalMods;
    }

    public Map<String, Object> getShareCodeData() {
        Map<String, Object> data = new HashMap<String, Object>();
        Map<String, Object> mods = new HashMap<String, Object>();
        List<Map<String, Object>> optional = new ArrayList<Map<String, Object>>();

        for (String mod : this.getInstalledOptionalModNames()) {
            Map<String, Object> modInfo = new HashMap<String, Object>();
            modInfo.put("name", mod);
            modInfo.put("selected", true);
            optional.add(modInfo);
        }

        mods.put("optional", optional);
        data.put("mods", mods);

        return data;
    }
}
