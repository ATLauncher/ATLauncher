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
package com.atlauncher.data;

import java.util.ArrayList;
import java.util.List;

import com.atlauncher.annot.Json;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.json.Java;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modpacksch.ModpacksChPackManifest;
import com.atlauncher.data.modpacksch.ModpacksChPackVersionManifest;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.google.gson.annotations.SerializedName;

@Json
public class InstanceLauncher {
    public String name;
    public String pack;
    public String description;
    public Integer packId;
    public Integer externaPackId;
    public String version;
    public String hash;

    public Java java;

    @SerializedName(value = "enableCurseForgeIntegration", alternate = { "enableCurseIntegration" })
    public boolean enableCurseForgeIntegration = false;
    public boolean enableEditingMods = true;

    public LoaderVersion loaderVersion;

    public Integer requiredMemory;
    public Integer requiredPermGen;

    public Integer initialMemory;
    public Integer maximumMemory;
    public Integer permGen;
    public String javaPath;
    public String javaArguments;
    public String account;

    public boolean isDev;
    public boolean isPlayable;
    public boolean assetsMapToResources;

    @SerializedName(value = "curseForgeManifest", alternate = { "curseManifest" })
    public CurseForgeManifest curseForgeManifest;

    public CurseForgeProject curseForgeProject;
    public CurseForgeFile curseForgeFile;
    public MultiMCManifest multiMCManifest;
    public ModpacksChPackManifest modpacksChPackManifest;
    public ModpacksChPackVersionManifest modpacksChPackVersionManifest;

    public List<DisableableMod> mods = new ArrayList<>();
    public List<String> ignoredUpdates = new ArrayList<>();
}
