/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.data;

public class Version {

    private boolean isDev;
    private String version;
    private MinecraftVersion minecraftVersion;

    public Version(boolean isDev, String version, MinecraftVersion minecraftVersion) {
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

    public MinecraftVersion getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public String toString() {
        return this.version + " (Minecraft " + this.minecraftVersion.getVersion() + ")";
    }

}
