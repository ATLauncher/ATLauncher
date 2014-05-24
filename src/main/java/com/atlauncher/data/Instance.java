/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import com.atlauncher.App;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.data.openmods.OpenEyeReportResponse;
import com.atlauncher.gui.ProgressDialog;
import com.atlauncher.mclauncher.LegacyMCLauncher;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.utils.Authentication;
import com.atlauncher.utils.Utils;

// TODO: Auto-generated Javadoc
/**
 * This class handles contains information about a single Instance in the Launcher. An Instance
 * being an installed version of a ModPack separate to others by file structure.
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
     * The version installed for this Instance.
     */
    private String version;

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
     * Comma separated list of the order of Jar's to be added to the class path when launching
     * Minecraft.
     */
    private String jarOrder;

    /**
     * Comma seperated list of the libraries needed by Minecraft/Forge to be added to the class path
     * when launching Minecraft.
     */
    private String librariesNeeded = null;

    /**
     * The extra arguments to be added to the command when launching Minecraft. Generally involves
     * things such as the tweakClass/s for Forge.
     */
    private String extraArguments = null;

    /**
     * The arguments required by Minecraft to be added to the command when launching Minecraft.
     * Generally involves thing such as handling of authentication, assets paths etc.
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
     * The Pack object for the pack this Instance was installed from. This is not stored in the
     * instances instance.json file as Pack's can be deleted from the system.
     * 
     * @see com.atlauncher.data.Pack
     */
    private transient Pack realPack;

    /**
     * If this Instance was installed from a development version of the Pack.
     */
    private boolean isDev;

    /**
     * If this Instance is playable or not. It may become unplayable after a failed update or if
     * files are found corrupt.
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
     * List of versions of the Pack this instance comes from that the user has said to not be
     * reminded about updating to.
     */
    private List<String> ignoredUpdates;

    /**
     * Instantiates a new instance.
     * 
     * @param name
     *            the name of the Instance
     * @param pack
     *            the name of the Pack this Instance is of
     * @param realPack
     *            the Pack object for the Pack this Instance is of
     * @param installJustForMe
     *            if this instance is only meant to be used by the original installer
     * @param version
     *            the version of the Pack this Instance is of
     * @param minecraftVersion
     *            the Minecraft version this Instance runs off
     * @param memory
     *            the minimum RAM/memory as recommended by the pack developer/s
     * @param permgen
     *            the minimum PermGen/Metaspace as recommended by the pack developer/s
     * @param mods
     *            the mods installed in this Instance
     * @param jarOrder
     *            the order that jar mods are loaded into the class path
     * @param librariesNeeded
     *            the libraries needed to launch Minecraft
     * @param extraArguments
     *            the extra arguments for launching the pack
     * @param minecraftArguments
     *            the arguments needed by Minecraft to run
     * @param mainClass
     *            the main class to run when launching Minecraft
     * @param assets
     *            the assets version being used by Minecraft
     * @param isDev
     *            if this Instance is using a dev version of the pack
     * @param isPlayable
     *            if this instance is playable
     * @param newLaunchMethod
     *            if this instance is using the new launch method for Minecraft
     */
    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int memory, int permgen,
            ArrayList<DisableableMod> mods, String jarOrder, String librariesNeeded,
            String extraArguments, String minecraftArguments, String mainClass, String assets,
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
        if (installJustForMe) {
            this.installedBy = App.settings.getAccount().getMinecraftUsername();
        } else {
            this.installedBy = null;
        }
        this.isConverted = true;
    }

    /**
     * Instantiates a new instance with it defaulting to being playable.
     * 
     * @param name
     *            the name of the Instance
     * @param pack
     *            the name of the Pack this Instance is of
     * @param realPack
     *            the Pack object for the Pack this Instance is of
     * @param installJustForMe
     *            if this instance is only meant to be used by the original installer
     * @param version
     *            the version of the Pack this Instance is of
     * @param minecraftVersion
     *            the Minecraft version this Instance runs off
     * @param memory
     *            the minimum RAM/memory as recommended by the pack developer/s
     * @param permgen
     *            the minimum PermGen/Metaspace as recommended by the pack developer/s
     * @param mods
     *            the mods installed in this Instance
     * @param jarOrder
     *            the order that jar mods are loaded into the class path
     * @param librariesNeeded
     *            the libraries needed to launch Minecraft
     * @param extraArguments
     *            the extra arguments for launching the pack
     * @param minecraftArguments
     *            the arguments needed by Minecraft to run
     * @param mainClass
     *            the main class to run when launching Minecraft
     * @param assets
     *            the assets version being used by Minecraft
     * @param isDev
     *            if this Instance is using a dev version of the pack
     * @param newLaunchMethod
     *            if this instance is using the new launch method for Minecraft
     */
    public Instance(String name, String pack, Pack realPack, boolean installJustForMe,
            String version, String minecraftVersion, int memory, int permgen,
            ArrayList<DisableableMod> mods, String jarOrder, String librariesNeeded,
            String extraArguments, String minecraftArguments, String mainClass, String assets,
            boolean isDev, boolean newLaunchMethod) {
        this(name, pack, realPack, installJustForMe, version, minecraftVersion, memory, permgen,
                mods, jarOrder, librariesNeeded, extraArguments, minecraftArguments, mainClass,
                assets, isDev, true, newLaunchMethod);
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
     * @param newName
     *            the new name for this Instance
     */
    public void setName(String newName) {
        this.name = newName;
    }

    /**
     * Gets the safe name of the Instance used in file paths. Removes all non alphanumeric
     * characters.
     * 
     * @return the safe name of the Instance.
     */
    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets the name of the Pack this Instance was created from. Pack's can be deleted/removed in
     * the future.
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
     * Gets the minimum recommended RAM/memory for this Instance based off what the Pack specifies.
     * Defaults to 0 if there is none specified by the pack. Value is in MB.
     * 
     * @return the minimum RAM/memory recommended for this Instance in MB
     */
    public int getMemory() {
        return this.memory;
    }

    /**
     * Gets a List of the installed mods in this Instance. Mods are listed as DisableableMod
     * objects.
     * 
     * @return a List of DisableableMod objects of the installed mods in this instance or null if
     *         none
     */
    public List<DisableableMod> getInstalledMods() {
        return this.mods;
    }

    /**
     * Gets the minimum recommended PermGen/Metaspace size for this Instance based off what the Pack
     * specifies. Defaults to 0 if there is non specified by the pack. Value is in MB.
     * 
     * @return the minimum PermGen/Metaspace recommended for this Instance in MB
     */
    public int getPermGen() {
        return this.permgen;
    }

    /**
     * Renames this instance including renaming the folder in the Instances directory to the new
     * name provided.
     * 
     * @param newName
     *            the new name of the Instance
     * @return true if the Instances folder was renamed and false if it failed
     */
    public boolean rename(String newName) {
        String oldName = this.name;
        File oldDir = getRootDirectory();
        this.name = newName;
        File newDir = getRootDirectory();
        if (oldDir.renameTo(newDir)) {
            return true;
        } else {
            this.name = oldName;
            return false;
        }
    }

    /**
     * Gets the name of the Pack this Instance was created from in a safe manner by removing all non
     * alphanumeric characters which is then safe for use inside file paths and URL's.
     * 
     * @return the safe name of the Pack
     */
    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Gets a ImageIcon object for the image file of the Pack for use in displaying in the Packs and
     * Instances tabs.
     * 
     * @return ImageIcon for this Instances Pack
     */
    public ImageIcon getImage() {
        File imageFile = new File(App.settings.getImagesDir(), getSafePackName().toLowerCase()
                + ".png");
        if (!imageFile.exists()) {
            imageFile = new File(App.settings.getImagesDir(), "defaultimage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    /**
     * Gets the description of the Pack this Instance was installed from if it's still available in
     * the Launcher. If the pack no longer exists then it simply returns "No Description".
     * 
     * @return the description of the Pack this Instance was created from
     */
    public String getPackDescription() {
        if (this.realPack != null) {
            return this.realPack.getDescription();
        } else {
            return "No Description!"; // TODO: Localise the No Description text
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
     * Checks to see if Leaderboards are enabled for the Pack this Instance was created from. If the
     * pack no longer exists we don't allow logging of Leaderboard statistics.
     * 
     * @return true if Leaderboard are enabled and statistics can be sent
     */
    public boolean isLeaderboardsEnabled() {
        return (this.realPack == null ? false : this.realPack.isLeaderboardsEnabled());
    }

    /**
     * Checks to see if Logging is enabled for the Pack this Instance was created from. If the pack
     * no longer exists we don't allow logging.
     * 
     * @return true if Logging is enabled
     */
    public boolean isLoggingEnabled() {
        return (this.realPack == null ? false : this.realPack.isLoggingEnabled());
    }

    /**
     * This stops the popup informing a user that this Instance has an update when they go to play
     * this Instance. It will simply deny the current version from showing up again informing the
     * user when their Instance is not using the latest version.
     */
    public void ignoreUpdate() {
        if (this.ignoredUpdates == null) {
            this.ignoredUpdates = new ArrayList<String>();
        }
        String version = getLatestVersion();
        if (!hasUpdateBeenIgnored(version)) {
            this.ignoredUpdates.add(version);
            App.settings.saveInstances();
        }
    }

    /**
     * Checks to see if a given version has been ignored from showing update prompts when the
     * Instance is played.
     * 
     * @param version
     *            the version to check if it's been ignored in the past
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
     * This converts an old Instance using old Minecraft argument storage to the new method of
     * storage.
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
     * This removes a given DisableableMod object and removes it from the list of installed mods as
     * well as deleting the file.
     * 
     * @param mod
     *            the DisableableMod object for the mod to remove
     */
    public void removeInstalledMod(DisableableMod mod) {
        Utils.delete((mod.isDisabled() ? mod.getDisabledFile(this) : mod.getFile(this)));
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
     * Gets the Minecraft Version that this Instance uses.
     * 
     * @return the Minecraft Version that this Instance uses
     */
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }

    /**
     * Gets a File object for the root directory of this Instance.
     * 
     * @return File object for the root directory of this Instance
     */
    public File getRootDirectory() {
        return new File(App.settings.getInstancesDir(), getSafeName());
    }

    /**
     * Gets a File object for the directory where the assets for this version of Minecraft are
     * stored.
     * 
     * @return File object for the assets directory used by Minecraft
     */
    public File getAssetsDir() {
        return new File(App.settings.getVirtualAssetsDir(), getAssets());
    }

    /**
     * Gets a File object for the saves directory of this Instance.
     * 
     * @return File object for the saves directory of this Instance
     */
    public File getSavesDirectory() {
        return new File(getRootDirectory(), "saves");
    }

    /**
     * Gets a File object for the reports directory of this Instance where OpenEye stores it's
     * pending crash reports.
     * 
     * @return File object for the reports directory of this Instance
     */
    public File getReportsDirectory() {
        return new File(getRootDirectory(), "reports");
    }

    /**
     * Gets a File object for the mods directory of this Instance.
     * 
     * @return File object for the mods directory of this Instance
     */
    public File getModsDirectory() {
        return new File(getRootDirectory(), "mods");
    }

    /**
     * Gets a File object for the IC2 library directory of this Instance.
     * 
     * @return File object for the IC2 library directory of this Instance
     */
    public File getIC2LibDirectory() {
        return new File(getModsDirectory(), "ic2");
    }

    /**
     * Gets a File object for the denlib directory of this Instance.
     * 
     * @return File object for the denlib directory of this Instance
     */
    public File getDenLibDirectory() {
        return new File(getModsDirectory(), "denlib");
    }

    /**
     * Gets a File object for the plugins directory of this Instance.
     * 
     * @return File object for the plugins directory of this Instance
     */
    public File getPluginsDirectory() {
        return new File(getRootDirectory(), "plugins");
    }

    /**
     * Gets a File object for the shader packs directory of this Instance.
     * 
     * @return File object for the shader packs directory of this Instance
     */
    public File getShaderPacksDirectory() {
        return new File(getRootDirectory(), "shaderpacks");
    }

    /**
     * Gets a File object for the disabled mods directory of this Instance.
     * 
     * @return File object for the disabled mods directory of this Instance
     */
    public File getDisabledModsDirectory() {
        return new File(getRootDirectory(), "disabledmods");
    }

    /**
     * Gets a File object for the core mods directory of this Instance.
     * 
     * @return File object for the core mods directory of this Instance
     */
    public File getCoreModsDirectory() {
        return new File(getRootDirectory(), "coremods");
    }

    /**
     * Gets a File object for the jar mods directory of this Instance.
     * 
     * @return File object for the jar mods directory of this Instance
     */
    public File getJarModsDirectory() {
        return new File(getRootDirectory(), "jarmods");
    }

    /**
     * Gets a File object for the texture packs directory of this Instance.
     * 
     * @return File object for the texture packs directory of this Instance
     */
    public File getTexturePacksDirectory() {
        return new File(getRootDirectory(), "texturepacks");
    }

    /**
     * Gets a File object for the resource packs directory of this Instance.
     * 
     * @return File object for the resource packs directory of this Instance
     */
    public File getResourcePacksDirectory() {
        return new File(getRootDirectory(), "resourcepacks");
    }

    /**
     * Gets a File object for the bin directory of this Instance.
     * 
     * @return File object for the bin directory of this Instance
     */
    public File getBinDirectory() {
        return new File(getRootDirectory(), "bin");
    }

    /**
     * Gets a File object for the natives directory of this Instance.
     * 
     * @return File object for the natives directory of this Instance
     */
    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    /**
     * Gets a File object for the minecraft.jar of this Instance.
     * 
     * @return File object for the minecraft.jar of this Instance
     */
    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

    /**
     * Checks if the pack associated with this Instance can be installed.
     * 
     * @return true if the Pack this Instance was made from can be installed
     * @see com.atlauncher.data.Pack#canInstall
     */
    public boolean canInstall() {
        return (this.realPack == null ? false : this.realPack.canInstall());
    }

    /**
     * Gets the Pack object that this Instance was created from. If it doesn't exist, this will
     * return null
     * 
     * @return Pack object of the Pack this Instance was created from or null if no longer available
     */
    public Pack getRealPack() {
        return this.realPack;
    }

    /**
     * Sets the Pack object that this Instance was created from. Defaults to null when loaded.
     * 
     * @param realPack
     *            the Pack object that this Instance was created from
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
     * Sets the version of the Pack this Instance was created from.
     * 
     * @param version
     *            the version of the Pack this Instance was created from
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Sets the Minecraft version of the Pack this Instance was created from.
     * 
     * @param minecraftVersion
     *            the new minecraft version
     */
    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    /**
     * Sets the order to load the jars from the jarmods folder.
     * 
     * @param jarOrder
     *            comma separated list of filenames for the order to load the mods from the jarmods
     *            folder
     */
    public void setJarOrder(String jarOrder) {
        this.jarOrder = jarOrder;
    }

    /**
     * Sets the minimum recommended RAM/memory for this Instance in MB.
     * 
     * @param memory
     *            the minimum recommended RAM/memory for this Instance in MB
     */
    public void setMemory(int memory) {
        this.memory = memory;
    }

    /**
     * Sets the minimum recommended PermGen/Metaspace size for this Instance in MB.
     * 
     * @param permgen
     *            the minimum recommended PermGen/Metaspace for this Instance in MB
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
     * Sets this Instance as unplayable so the user cannot play the Instance. Used when installs go
     * bad or files are found that corrupts the Instance
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
     * @param newLaunchMethod
     *            true if the new launch menthod should be used, false for the legacy launch method
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
     * @return a comma separated list of filenames for the libraries to be loaded when Minecraft is
     *         started
     */
    public String getLibrariesNeeded() {
        return this.librariesNeeded;
    }

    /**
     * Sets the list of libraries needed to be loaded when launching Minecraft.
     * 
     * @param librariesNeeded
     *            a comma separated list of filenames for the libraries to be loaded when Minecraft
     *            is started
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
     * Gets the extra arguments for the Instance which is added to the command argument when
     * launching Minecraft.
     * 
     * @return the extra arguments used by the Instance when launching Minecraft
     */
    public String getExtraArguments() {
        return this.extraArguments;
    }

    /**
     * Sets the extra arguments for the Instance which is added to the command argument when
     * launching Minecraft.
     * 
     * @param extraArguments
     *            the new extra arguments used by the Instance when launching Minecraft
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
     * Gets the Minecraft arguments for the Instance which is added to the command argument when
     * launching Minecraft. These involve things like asset directories, token input among other
     * things.
     * 
     * @return the Minecraft arguments used by the Instance when launching Minecraft
     */
    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    /**
     * Sets the Minecraft arguments for the Instance which is added to the command argument when
     * launching Minecraft. These involve things like asset directories, token input among other
     * things.
     * 
     * @param minecraftArguments
     *            the new Minecraft arguments used by the Instance when launching Minecraft
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
     * @param mainClass
     *            the new main class used to launch Minecraft
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
     * @param assets
     *            the new assets value
     */
    public void setAssets(String assets) {
        this.assets = assets;
    }

    /**
     * Checks if this Instance can be played. This refers only to the account and permission side of
     * things and doesn't reference if the instance is playable or as determined by the
     * {@link com.atlauncher.data.Instance#isPlayable} field.
     * 
     * @return true if the user can play this Instance
     */
    public boolean canPlay() {
        // Make sure an account is selected first.
        if (App.settings.getAccount() == null || !App.settings.getAccount().isReal()) {
            return false;
        }

        // Check to see if this was a private Instance belonging to a specific user only.
        if (this.installedBy != null
                && !App.settings.getAccount().getMinecraftUsername()
                        .equalsIgnoreCase(this.installedBy)) {
            return false;
        }

        // All good, no false returns yet so allow it.
        return true;
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
                if (!this.realPack.getLatestVersion().getVersion().equalsIgnoreCase(this.version)
                        && !this.realPack.isLatestVersionNoUpdate()) {
                    return true;
                }
            }
        }

        // If we triggered nothing then there is no update.
        return false;
    }

    /**
     * Gets the latest version of the Pack this Instance was created from. If the Pack has been
     * removed or it has no published versions then it will return null.
     * 
     * @return the latest version of the Pack this Instance was created from or null if the Pack no
     *         longer exists or there is no versions of the Pack
     */
    public String getLatestVersion() {
        return (this.realPack != null ? this.realPack.getLatestVersion().getVersion() : null);
    }

    /**
     * Checks is a mod was installed with this Instance.
     * 
     * @param name
     *            the name of the mod
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
     * @param mods
     *            List of {@link com.atlauncher.data.DisableableMod} objects of the mods installed
     *            with this Instance.
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
        final Account account = App.settings.getAccount();
        if (account == null) {
            String[] options = { App.settings.getLocalizedString("common.ok") };
            JOptionPane.showOptionDialog(App.settings.getParent(),
                    App.settings.getLocalizedString("instance.noaccount"),
                    App.settings.getLocalizedString("instance.noaccountselected"),
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                    options[0]);
            App.settings.setMinecraftLaunched(false);
            return false;
        } else {
            if ((App.settings.getMemory() < this.memory)
                    && (this.memory <= Utils.getSafeMaximumRam())) {
                String[] options = { App.settings.getLocalizedString("common.yes"),
                        App.settings.getLocalizedString("common.no") };
                int ret = JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        "<html><center>"
                                + App.settings.getLocalizedString("instance.insufficientram", "<b>"
                                        + this.memory + "</b> MB<br/><br/>") + "</center></html>",
                        App.settings.getLocalizedString("instance.insufficientramtitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                        options[0]);
                if (ret != 0) {
                    App.settings
                            .log("Launching of instance cancelled due to user cancelling memory warning!",
                                    LogMessageType.warning, false);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }
            if (App.settings.getPermGen() < this.permgen) {
                String[] options = { App.settings.getLocalizedString("common.yes"),
                        App.settings.getLocalizedString("common.no") };
                int ret = JOptionPane.showOptionDialog(
                        App.settings.getParent(),
                        "<html><center>"
                                + App.settings.getLocalizedString("instance.insufficientpermgen",
                                        "<b>" + this.permgen + "</b> MB<br/><br/>")
                                + "</center></html>", App.settings
                                .getLocalizedString("instance.insufficientpermgentitle"),
                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                        options[0]);
                if (ret != 0) {
                    App.settings
                            .log("Launching of instance cancelled due to user cancelling memory warning!",
                                    LogMessageType.warning, false);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            }
            AuthenticationResponse sess = null;
            if (!App.settings.isInOfflineMode()) {
                String password = account.getPassword();
                if (!account.isRemembered()) {
                    JPanel panel = new JPanel();
                    panel.setLayout(new BorderLayout());
                    JLabel passwordLabel = new JLabel(App.settings.getLocalizedString(
                            "instance.enterpassword", account.getMinecraftUsername()));
                    JPasswordField passwordField = new JPasswordField();
                    panel.add(passwordLabel, BorderLayout.NORTH);
                    panel.add(passwordField, BorderLayout.CENTER);
                    int ret = JOptionPane.showConfirmDialog(App.settings.getParent(), panel,
                            App.settings.getLocalizedString("instance.enterpasswordtitle"),
                            JOptionPane.OK_CANCEL_OPTION);
                    if (ret == JOptionPane.OK_OPTION) {
                        password = new String(passwordField.getPassword());
                    } else {
                        App.settings.log("Aborting login for " + account.getMinecraftUsername(),
                                LogMessageType.error, false);
                        App.settings.setMinecraftLaunched(false);
                        return false;
                    }
                }
                App.settings.log("Logging into Minecraft!");
                final String pass = password;
                final ProgressDialog dialog = new ProgressDialog(
                        App.settings.getLocalizedString("account.loggingin"), 0,
                        App.settings.getLocalizedString("account.loggingin"), "Aborting login for "
                                + account.getMinecraftUsername());
                dialog.addThread(new Thread() {
                    public void run() {
                        dialog.setReturnValue(Authentication.login(account.getUsername(), pass));
                        dialog.close();
                    };
                });
                dialog.start();
                if (dialog.getReturnValue() == null) {
                    return false;
                }
                sess = (AuthenticationResponse) dialog.getReturnValue();
                if (sess.hasError()) {
                    App.settings.log(sess.getErrorMessage(), LogMessageType.error, false);
                    String[] options = { App.settings.getLocalizedString("common.ok") };
                    JOptionPane.showOptionDialog(
                            App.settings.getParent(),
                            "<html><center>"
                                    + App.settings.getLocalizedString("instance.errorloggingin",
                                            "<br/><br/>" + sess.getErrorMessage())
                                    + "</center></html>", App.settings
                                    .getLocalizedString("instance.errorloggingintitle"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options,
                            options[0]);
                    App.settings.setMinecraftLaunched(false);
                    return false;
                }
            } else {
                sess = new AuthenticationResponse("token:0:0", false);
            }

            final AuthenticationResponse session = sess;
            Thread launcher = new Thread() {
                public void run() {
                    try {
                        long start = System.currentTimeMillis();
                        if (App.settings.getParent() != null) {
                            App.settings.getParent().setVisible(false);
                        }
                        // Create a note of worlds for auto backup if enabled
                        HashMap<String, Long> preWorldList = new HashMap<String, Long>();
                        if (App.settings.isAdvancedBackupsEnabled() && App.settings.getAutoBackup()) {
                            if (getSavesDirectory().exists()) {
                                File[] files = getSavesDirectory().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if (file.isDirectory())
                                            preWorldList.put(file.getName(), file.lastModified());
                                    }
                                }
                            }
                        }
                        Process process = null;
                        if (isNewLaunchMethod()) {
                            process = MCLauncher.launch(account, Instance.this, session);
                        } else {
                            process = LegacyMCLauncher.launch(account, Instance.this, session);
                        }
                        App.settings.showKillMinecraft(process);
                        InputStream is = process.getInputStream();
                        InputStreamReader isr = new InputStreamReader(is);
                        BufferedReader br = new BufferedReader(isr);
                        String line;
                        while ((line = br.readLine()) != null) {
                            App.settings.logMinecraft(line);
                        }
                        App.settings.hideKillMinecraft();
                        if (App.settings.getParent() != null && App.settings.keepLauncherOpen()) {
                            App.settings.getParent().setVisible(true);
                        }
                        long end = System.currentTimeMillis();
                        if (App.settings.isInOfflineMode()) {
                            App.settings.checkOnlineStatus();
                        }
                        int exitValue = 0; // Assume we exited fine
                        try {
                            exitValue = process.exitValue(); // Try to get the real exit value
                        } catch (IllegalThreadStateException e) {
                            App.settings.logStackTrace(e);
                            process.destroy(); // Kill the process
                        }
                        if (!App.settings.keepLauncherOpen()) {
                            App.settings.setConsoleVisible(false); // Hide the console to pretend
                                                                   // we've closed
                        }
                        if (exitValue != 0) {
                            // Submit any pending crash reports from Open Eye if need to since we
                            // exited abnormally
                            if (App.settings.enableLogs() && App.settings.enableOpenEyeReporting()) {
                                App.TASKPOOL.submit(new Runnable() {
                                    public void run() {
                                        sendOpenEyePendingReports();
                                    }
                                });
                            }
                        } else if (App.settings.isAdvancedBackupsEnabled()
                                && App.settings.getAutoBackup()) {
                            // Begin backup
                            if (getSavesDirectory().exists()) {
                                File[] files = getSavesDirectory().listFiles();
                                if (files != null) {
                                    for (File file : files) {
                                        if ((file.isDirectory()) && (!file.getName().equals("NEI"))) {
                                            if (preWorldList.containsKey(file.getName())) {
                                                // Only backup if file changed
                                                if (!(preWorldList.get(file.getName()) == file
                                                        .lastModified())) {
                                                    SyncAbstract sync = SyncAbstract.syncList
                                                            .get(App.settings.getLastSelectedSync());
                                                    sync.backupWorld(
                                                            file.getName()
                                                                    + String.valueOf(file
                                                                            .lastModified()), file,
                                                            Instance.this);
                                                }
                                            }
                                            // Or backup if a new file is found
                                            else {
                                                SyncAbstract sync = SyncAbstract.syncList
                                                        .get(App.settings.getLastSelectedSync());
                                                sync.backupWorld(
                                                        file.getName()
                                                                + String.valueOf(
                                                                        file.lastModified())
                                                                        .replace(":", ""), file,
                                                        Instance.this);
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        App.settings.setMinecraftLaunched(false);
                        if (!App.settings.isInOfflineMode()) {
                            if (isLeaderboardsEnabled() && isLoggingEnabled()) {
                                String username = null;
                                if (App.settings.enableLeaderboards() && App.settings.enableLogs()) {
                                    username = account.getMinecraftUsername();
                                }
                                App.settings
                                        .apiCall((username == null ? "NULL" : username),
                                                "addleaderboardtime"
                                                        + (username == null ? "" : "generic"),
                                                (getRealPack() == null ? "0" : getRealPack()
                                                        .getID() + ""),
                                                ((end - start) / 1000) + "", (isDev ? "dev"
                                                        : getVersion()));
                            }
                            if (App.settings.keepLauncherOpen() && App.settings.hasUpdatedFiles()) {
                                App.settings.reloadLauncherData();
                            }
                        }
                        if (!App.settings.keepLauncherOpen()) {
                            System.exit(1);
                        }
                    } catch (IOException e1) {
                        App.settings.logStackTrace(e1);
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
        File reportsDir = this.getReportsDirectory();
        if (reportsDir.exists()) {
            for (String filename : reportsDir.list(Utils.getOpenEyePendingReportsFileFilter())) {
                File report = new File(reportsDir, filename);
                App.LOGGER.info("OpenEye: Sending pending crash report located at '"
                        + report.getAbsolutePath() + "'");
                OpenEyeReportResponse response = Utils.sendOpenEyePendingReport(report);
                if (response == null) {
                    // Pending report was never sent due to an issue. Won't delete the file in case
                    // it's
                    // a temporary issue and can be sent again later.
                    App.LOGGER.warn("OpenEye: Couldn't send pending crash report!");
                } else {
                    // OpenEye returned a response to the report, display that to user if needed.
                    App.LOGGER
                            .info("OpenEye: Pending crash report sent! URL: " + response.getURL());
                    if (response.hasNote()) {
                        String[] options = {
                                App.settings.getLocalizedString("common.opencrashreport"),
                                App.settings.getLocalizedString("common.ok") };
                        int ret = JOptionPane.showOptionDialog(
                                App.settings.getParent(),
                                "<html><center>"
                                        + App.settings.getLocalizedString(
                                                "instance.openeyereport1", "<br/><br/>")
                                        + response.getNoteDisplay()
                                        + App.settings
                                                .getLocalizedString("instance.openeyereport2")
                                        + "</center></html>", App.settings
                                        .getLocalizedString("instance.aboutyourcrash"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null,
                                options, options[1]);
                        if (ret == 0) {
                            Utils.openBrowser(response.getURL());
                        }
                    }
                }
                Utils.delete(report); // Delete the pending report since we've sent it
            }
        }
    }

    /**
     * Uploads the contents of the console to Stikked instance to report to the pack's developer.
     */
    public void uploadCrashLog() {
        Thread thread = new Thread() {
            public void run() {
                String result = Utils.uploadPaste("ATLauncher Log", App.settings.getLog());
                if (result.contains(Constants.PASTE_CHECK_URL)) {
                    App.settings.apiCall(App.settings.getAccount().getMinecraftUsername(),
                            "reportcrash", realPack.getID() + "", getVersion(),
                            result.replace("http://paste.atlauncher.com/view/", ""));
                    App.settings.log("Log uploaded and reported to ModPack creator: " + result);
                } else {
                    App.settings
                            .log("Log failed to upload: " + result, LogMessageType.error, false);
                }
            };
        };
        thread.run();
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
            App.settings.logStackTrace(e);
        }
        return null;
    }
}
