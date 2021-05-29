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
package com.atlauncher.data.minecraft;

import java.util.List;

import com.atlauncher.annot.Json;

@Json
public class MinecraftVersion {
    public String id;
    public int complianceLevel;
    public JavaVersion javaVersion;
    public Arguments arguments;
    public String minecraftArguments;
    public VersionManifestVersionType type;
    public String time;
    public String releaseTime;
    public String minimumLauncherVersion;
    public MojangAssetIndex assetIndex;
    public String assets;
    public MojangDownloads downloads;
    public Logging logging;
    public List<Library> libraries;
    public List<Rule> rules;
    public String mainClass;
}
