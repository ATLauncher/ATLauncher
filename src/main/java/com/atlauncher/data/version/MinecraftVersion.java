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
package com.atlauncher.data.version;

import com.atlauncher.annot.Json;
import com.atlauncher.data.mojang.MojangVersion;

@Json
public class MinecraftVersion {
    private String version;
    private boolean server;
    private boolean legacy;
    private boolean coremods;
    private boolean resources;
    private MojangVersion json;

    public boolean canCreateServer() {
        return this.server;
    }

    public String getVersion() {
        return this.version;
    }

    public MojangVersion getMojangVersion() {
        return this.json;
    }

    public boolean isLegacy() {
        return this.legacy;
    }

    public boolean usesCoreMods() {
        return this.coremods;
    }

    public boolean hasResources() {
        return this.resources;
    }

    public String toString() {
        return this.version;
    }

}