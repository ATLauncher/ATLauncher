/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import java.io.File;
import java.io.Serializable;

import javax.swing.ImageIcon;

import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class Instance implements Serializable {

    private static final long serialVersionUID = 1925450686877381452L;
    private String name;
    private String pack;
    private String version;
    private String minecraftVersion;
    private String jarOrder;
    private String librariesNeeded = null;
    private String minecraftArguments = null;
    private String mainClass = null;
    private transient Pack realPack;
    private boolean isPlayable;
    private boolean newLaunchMethod;

    public Instance(String name, String pack, Pack realPack, String version,
            String minecraftVersion, String jarOrder, String librariesNeeded,
            String minecraftArguments, String mainClass, boolean isPlayable, boolean newLaunchMethod) {
        this.name = name;
        this.pack = pack;
        this.realPack = realPack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.jarOrder = jarOrder;
        if (newLaunchMethod) {
            this.librariesNeeded = librariesNeeded;
            this.mainClass = mainClass;
            this.jarOrder = jarOrder;
        }
        this.isPlayable = isPlayable;
        this.newLaunchMethod = newLaunchMethod;
    }

    public Instance(String name, String pack, Pack realPack, String version,
            String minecraftVersion, String jarOrder, String librariesNeeded,
            String minecraftArguments, String mainClass, boolean newLaunchMethod) {
        this(name, pack, realPack, version, minecraftVersion, jarOrder, librariesNeeded,
                minecraftArguments, mainClass, true, newLaunchMethod);
    }

    public String getName() {
        return this.name;
    }

    public String getSafeName() {
        return this.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getPackName() {
        return pack;
    }

    public String getJarOrder() {
        return this.jarOrder;
    }

    /**
     * Gets a file safe and URL safe name which simply means replacing all non alpha numerical
     * characters with nothing
     * 
     * @return File safe and URL safe name of the pack
     */
    public String getSafePackName() {
        return this.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    public ImageIcon getImage() {
        File imageFile = new File(LauncherFrame.settings.getImagesDir(), getSafePackName()
                .toLowerCase() + ".png");
        if (!imageFile.exists()) {
            imageFile = new File(LauncherFrame.settings.getImagesDir(), "defaultimage.png");
        }
        return Utils.getIconImage(imageFile);
    }

    public String getPackDescription() {
        if (this.realPack != null) {
            return this.realPack.getDescription();
        } else {
            return "No Description!";
        }
    }

    public boolean hasUpdate() {
        return false;
    }

    public String getVersion() {
        return version;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public File getRootDirectory() {
        return new File(LauncherFrame.settings.getInstancesDir(), getSafeName());
    }

    public File getMinecraftDirectory() {
        return new File(getRootDirectory(), ".minecraft");
    }

    public File getModsDirectory() {
        return new File(getMinecraftDirectory(), "mods");
    }

    public File getCoreModsDirectory() {
        return new File(getMinecraftDirectory(), "coremods");
    }

    public File getJarModsDirectory() {
        return new File(getMinecraftDirectory(), "jarmods");
    }

    public File getBinDirectory() {
        return new File(getMinecraftDirectory(), "bin");
    }

    public File getNativesDirectory() {
        return new File(getBinDirectory(), "natives");
    }

    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

    public boolean canInstall() {
        if (realPack == null) {
            return false;
        }
        return realPack.canInstall();
    }

    public Pack getRealPack() {
        return this.realPack;
    }

    public void setRealPack(Pack realPack) {
        this.realPack = realPack;
    }

    public boolean hasJarMods() {
        if (this.jarOrder == null) {
            return false;
        } else {
            return true;
        }
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setMinecraftVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public void setJarOrder(String jarOrder) {
        this.jarOrder = jarOrder;
    }

    public void setPlayable() {
        this.isPlayable = true;
    }

    public void setUnplayable() {
        this.isPlayable = false;
    }

    public boolean isPlayable() {
        return this.isPlayable;
    }

    public void setIsNewLaunchMethod(boolean newLaunchMethod) {
        this.newLaunchMethod = newLaunchMethod;
    }

    public boolean isNewLaunchMethod() {
        return this.newLaunchMethod;
    }

    public String getLibrariesNeeded() {
        return librariesNeeded;
    }

    public void setLibrariesNeeded(String librariesNeeded) {
        this.librariesNeeded = librariesNeeded;
    }

    public String getMinecraftArguments() {
        return minecraftArguments;
    }

    public void setMinecraftArguments(String minecraftArguments) {
        this.minecraftArguments = minecraftArguments;
    }

    public String getMainClass() {
        return mainClass;
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

}
