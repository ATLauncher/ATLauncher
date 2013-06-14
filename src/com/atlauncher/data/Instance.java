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

import javax.swing.ImageIcon;

import com.atlauncher.exceptions.InvalidPack;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.Utils;

public class Instance {

    private String name;
    private String pack;
    private String version;
    private String minecraftVersion;
    private String jarOrder;

    public Instance(String name, String pack, String version, String minecraftVersion, String jarOrder) {
        this.name = name;
        this.pack = pack;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
        this.jarOrder = jarOrder;
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
        Pack pack;
        try {
            pack = LauncherFrame.settings.getPackByName(this.pack);
        } catch (InvalidPack e) {
            // Pack doesn't exist anymore
            return "No description";
        }
        return pack.getDescription();
    }
    
    public String getVersion() {
        return version;
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

    public File getMinecraftJar() {
        return new File(getBinDirectory(), "minecraft.jar");
    }

}
