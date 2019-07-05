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

import java.util.ArrayList;
import java.util.List;

import com.atlauncher.annot.Json;
import com.atlauncher.data.json.Java;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;

@Json
public class InstanceV2Launcher {
    private String launcher = Constants.LAUNCHER_NAME;
    private String launcherVersion = Constants.VERSION.toString();

    public String name;
    public String pack;
    public Integer packId;
    public String version;
    public String hash;

    public Java java;

    public boolean enableCurseIntegration = false;
    public boolean enableEditingMods = true;

    public LoaderVersion loaderVersion;

    public Integer requiredMemory;
    public Integer requiredPermGen;

    public Integer initialMemory;
    public Integer maximumMemory;
    public Integer permGen;
    public String javaPath;
    public String javaArguments;

    public boolean isDev;
    public boolean isPlayable;
    public boolean assetsMapToResources;

    public List<DisableableMod> mods = new ArrayList<>();
    public List<String> ignoredUpdates = new ArrayList<>();
}
