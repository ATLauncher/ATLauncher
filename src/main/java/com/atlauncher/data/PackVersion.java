/**
 * Copyright 2013 and onwards by ATLauncher and Contributors
 *
 * This work is licensed under the GNU General Public License v3.0.
 * Link to license: http://www.gnu.org/licenses/gpl-3.0.txt
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
        if (this.minecraftVersion == null) {
            this.setMinecraftVesion();
        }
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
        return this.version + " (Minecraft " + this.getMinecraftVersion().getVersion() + ")";
    }

    public boolean versionMatches(String version) {
        return this.version.equalsIgnoreCase(version);
    }

}
