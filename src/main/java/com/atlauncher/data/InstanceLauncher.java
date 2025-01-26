/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.annot.Json;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.curseforge.pack.CurseForgeManifest;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.data.ftb.FTBPackVersionManifest;
import com.atlauncher.data.json.Java;
import com.atlauncher.data.json.QuickPlay;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.modrinth.pack.ModrinthModpackManifest;
import com.atlauncher.data.multimc.MultiMCManifest;
import com.atlauncher.data.technic.TechnicModpack;
import com.google.gson.annotations.SerializedName;

@Json
public class InstanceLauncher {
    public String name;
    public String pack;
    public String description;
    public Integer packId;

    @SerializedName(value = "externalPackId", alternate = { "externaPackId" })
    public Integer externalPackId;

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
    public String javaRuntimeOverride = null;
    public String account;
    public Boolean enableDiscordIntegration = null;
    public QuickPlay quickPlay = QuickPlay.getDefault();
    public Boolean useJavaProvidedByMinecraft = null;
    public Boolean disableLegacyLaunching = null;
    public Boolean enableCommands = null;
    public String preLaunchCommand = null;
    public String postExitCommand = null;
    public String wrapperCommand = null;
    public Boolean useSystemGlfw = null;
    public Boolean useSystemOpenAl = null;
    public Boolean useDedicatedGpu = null;

    public boolean isDev;
    public boolean isPlayable;
    public boolean assetsMapToResources;

    @SerializedName(value = "curseForgeManifest", alternate = { "curseManifest" })
    public CurseForgeManifest curseForgeManifest;

    public CurseForgeProject curseForgeProject;
    public String curseForgeProjectDescription = null;
    public CurseForgeFile curseForgeFile;
    public MultiMCManifest multiMCManifest;
    public ModrinthProject modrinthProject;
    public ModrinthVersion modrinthVersion;
    public ModrinthModpackManifest modrinthManifest;
    @SerializedName(value = "ftbPackManifest", alternate = { "modpacksChPackManifest" })
    public FTBPackManifest ftbPackManifest;
    @SerializedName(value = "ftbPackVersionManifest", alternate = { "modpacksChPackVersionManifest" })
    public FTBPackVersionManifest ftbPackVersionManifest;
    public TechnicModpack technicModpack;
    public List<String> overridePaths = new ArrayList<>();

    public boolean checkForUpdates = true;

    public List<DisableableMod> mods = new ArrayList<>();
    public List<String> ignoredUpdates = new ArrayList<>();
    public boolean ignoreAllUpdates = false;
    public boolean vanillaInstance = false;

    public Instant lastPlayed = null;
    public Long numPlays = null;

    public String lastExportName = null;
    public String lastExportVersion = null;
    public String lastExportAuthor = null;
    public String lastExportSaveTo = null;

    public InstanceLauncher() {
        this.numPlays = 0l;
        this.lastPlayed = Instant.EPOCH;
    }

}
