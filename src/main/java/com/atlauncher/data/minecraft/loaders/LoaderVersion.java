/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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
package com.atlauncher.data.minecraft.loaders;

public class LoaderVersion {
    public String version;
    public String rawVersion;
    public boolean recommended;
    public Long size;
    public String hash;
    public String type;

    public LoaderVersion(String version, String rawVersion, boolean recommended, String type, Long size, String hash) {
        this.version = version;
        this.rawVersion = rawVersion;
        this.recommended = recommended;
        this.type = type;
        this.size = size;
        this.hash = hash;
    }

    public LoaderVersion(String version, String rawVersion, boolean recommended, String type) {
        this(version, rawVersion, recommended, type, null, null);
    }

    public LoaderVersion(String version, boolean recommended, String type) {
        this(version, version, recommended, type, null, null);
    }

    public LoaderVersion(String version) {
        this(version, version, false, "Dummy");
    }

    public boolean isFabric() {
        return this.type.equalsIgnoreCase("Fabric");
    }

    public String toString() {
        if (this.recommended) {
            return this.version + " (Recommended)";
        }

        return this.version;
    }
}
