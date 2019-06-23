/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import com.atlauncher.LogManager;
import com.atlauncher.exceptions.InvalidMinecraftVersion;

public class PackVersion {
    private String version;
    private String minecraft;
    private String hash;
    private MinecraftVersion minecraftVersion;
    private boolean canUpdate = true;
    private boolean isRecommended = true;
    private boolean isDev;
    private boolean hasLoader = false;
    private boolean hasChoosableLoader = false;

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
            LogManager.logStackTrace(e);
        }
    }

    public MinecraftVersion getMinecraftVersion() {
        if (this.minecraftVersion == null) {
            this.setMinecraftVesion();
        }
        return this.minecraftVersion;
    }

    public String getHash() {
        if (this.hash == null || !this.isDev) {
            return null;
        }
        return this.hash;
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

    public String toString() {
        if (this.minecraft.equalsIgnoreCase(this.version)) {
            return this.version;
        }

        return this.version + " (" + this.getMinecraftVersion().getVersion() + ")";
    }

    public boolean versionMatches(String version) {
        return this.version.equalsIgnoreCase(version);
    }

    public boolean hashMatches(String hash) {
        if (this.hash == null || !this.isDev) {
            return false;
        }

        return this.hash.equalsIgnoreCase(hash);
    }

    public boolean hasLoader() {
        return this.hasLoader;
    }

    public boolean hasChoosableLoader() {
        return this.hasChoosableLoader;
    }

}
