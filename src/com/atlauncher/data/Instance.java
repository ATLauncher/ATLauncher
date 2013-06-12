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
    private Version version;

    public Instance(String name, String pack, Version version) {
        this.name = name;
        this.pack = pack;
        this.version = version;
    }

    public String getName() {
        return this.name;
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
    
    public Version getVersion() {
        return version;
    }
    
    public Version getLatestVersion() {
        return version;
    }

    public String toString() {
        return "Instance Name: " + this.name + ", Pack Name: " + pack
                + ", Version: " + version;
    }

}
