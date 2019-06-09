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
package com.atlauncher.data.mojang;

import com.atlauncher.annot.Json;

import java.util.List;

@Json
public class MojangVersion {
    private String id;
    private MojangArguments arguments;
    private String minecraftArguments;
    private String type;
    private String time;
    private String releaseTime;
    private String minimumLauncherVersion;
    private MojangAssetIndex assetIndex;
    private String assets;
    private MojangDownloads downloads;
    private Logging logging;
    private List<Library> libraries;
    private List<Rule> rules;
    private String mainClass;

    public String getId() {
        return id;
    }

    public MojangArguments getArguments() {
        return this.arguments;
    }

    public Boolean hasArguments() {
        return this.arguments != null;
    }

    public String getMinecraftArguments() {
        return this.minecraftArguments;
    }

    public String getType() {
        return this.type;
    }

    public String getTime() {
        return this.time;
    }

    public String getReleaseTime() {
        return this.releaseTime;
    }

    public String getMinimumLauncherVersion() {
        return this.minimumLauncherVersion;
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

    public boolean hasLogging() {
        return this.logging != null;
    }

    public Logging getLogging() {
        return this.logging;
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
