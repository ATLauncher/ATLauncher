/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data;

import com.atlauncher.App;
import com.atlauncher.exceptions.InvalidMinecraftVersion;

public class PackVersion {
    private String version;
    private String minecraft;
    private MinecraftVersion minecraftVersion;
    private boolean canUpdate = true;
    private boolean isRecommended = true;
    private boolean hasJson = false;
    private boolean isDev;

    public String getVersion() {
        return this.version;
    }

    public String getSafeVersion() {
        return this.version.replaceAll("[^A-Za-z0-9]", "");
    }

    public void setMinecraftVesion() {
        try {
            this.minecraftVersion = App.settings.getMinecraftVersion(this.minecraft);
        } catch (InvalidMinecraftVersion e) {
            this.minecraftVersion = null;
            App.settings.logStackTrace(e);
        }
    }

    public MinecraftVersion getMinecraftVersion() {
        return this.minecraftVersion;
    }

    public boolean canUpdate() {
        return this.canUpdate;
    }

    public boolean isRecommended() {
        return this.isRecommended;
    }

    public boolean isDev() {
        return this.isDev;
    }

    public boolean hasJson() {
        return this.hasJson;
    }

    public String toString() {
        return this.version + " (Minecraft " + this.minecraftVersion.getVersion() + ")";
    }

    public boolean versionMatches(String version) {
        return this.version.equalsIgnoreCase(version);
    }

}
