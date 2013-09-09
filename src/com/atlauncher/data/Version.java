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

public class Version {

    private boolean isDev;
    private String version;
    private String minecraftVersion;
    
    public Version(boolean isDev, String version, String minecraftVersion) {
        this.isDev = isDev;
        this.version = version;
        this.minecraftVersion = minecraftVersion;
    }
    
    public boolean isDevVersion() {
        return this.isDev;
    }
    
    public String getVersion() {
        return this.version;
    }
    
    public String getMinecraftVersion() {
        return this.minecraftVersion;
    }
    
    public String toString() {
        return this.version + " (Minecraft " + this.minecraftVersion + ")";
    }
    
}
