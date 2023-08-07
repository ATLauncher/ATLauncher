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
package com.atlauncher;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LWJGLVersions;
import com.atlauncher.data.News;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Server;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.modpacksch.ModpacksChPackVersion;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.technic.TechnicModpack;
import com.atlauncher.data.technic.TechnicSolderModpack;

public final class Data {
    public static final List<AbstractAccount> ACCOUNTS = new LinkedList<>();
    public static AbstractAccount SELECTED_ACCOUNT = null; // Account using the Launcher

    public static Map<String, Object> CONFIG = new HashMap<>();
    public static Map<String, Object> CONFIG_OVERRIDES = new HashMap<>();

    public static final List<News> NEWS = new LinkedList<>();

    public static final List<Pack> PACKS = new LinkedList<>();

    public static final List<Instance> INSTANCES = new LinkedList<>();

    public static final List<Server> SERVERS = new LinkedList<>();

    public static final Map<String, VersionManifestVersion> MINECRAFT = new HashMap<>();
    public static LWJGLVersions LWJGL_VERSIONS = null;
    public static JavaRuntimes JAVA_RUNTIMES = null;

    // CurseForge instance update checking
    public static final Map<Instance, CurseForgeFile> CURSEFORGE_INSTANCE_LATEST_VERSION = new HashMap<>();

    // Modpacks.ch instance update checking
    public static final Map<Instance, ModpacksChPackVersion> MODPACKS_CH_INSTANCE_LATEST_VERSION = new HashMap<>();

    // Technic Non Solder instance update checking
    public static final Map<Instance, TechnicModpack> TECHNIC_INSTANCE_LATEST_VERSION = new HashMap<>();

    // Technic Solder instance update checking
    public static final Map<Instance, TechnicSolderModpack> TECHNIC_SOLDER_INSTANCE_LATEST_VERSION = new HashMap<>();

    // Modrinth instance update checking
    public static final Map<Instance, ModrinthVersion> MODRINTH_INSTANCE_LATEST_VERSION = new HashMap<>();
}
