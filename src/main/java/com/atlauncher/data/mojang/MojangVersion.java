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
package com.atlauncher.data.mojang;

import com.atlauncher.annot.Json;

import java.util.List;

@Json
public class MojangVersion {
    private String id;
    private String minecraftArguments;
    private String type;
    private MojangAssetIndex assetIndex;
    private String assets;
    private MojangDownloads downloads;
    private List<Library> libraries;
    private List<Rule> rules;
    private String mainClass;

    public String getId() {
        return id;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public String getType() {
        return this.type;
    }

    public MojangAssetIndex getAssetIndex() {
        return this.assetIndex;
    }

    public String getAssets() {
        if (this.assets == null) {
            return "legacy";
        }

        return this.assets;
    }

    public MojangDownloads getDownloads() {
        return this.downloads;
    }

    public List<Library> getLibraries() {
        return this.libraries;
    }

    public List<Rule> getRules() {
        return this.rules;
    }

    public String getMainClass() {
        return this.mainClass;
    }
}
