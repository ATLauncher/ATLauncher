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
