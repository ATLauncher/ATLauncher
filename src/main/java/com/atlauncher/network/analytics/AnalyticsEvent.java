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
package com.atlauncher.network.analytics;

import java.util.HashMap;
import java.util.Map;

import com.atlauncher.data.Instance;
import com.atlauncher.data.Pack;
import com.atlauncher.data.PackVersion;
import com.atlauncher.data.Server;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.ftb.FTBPackManifest;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthSearchHit;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.data.technic.TechnicModpackSlim;
import com.atlauncher.graphql.fragment.UnifiedModPackResultsFragment;

public class AnalyticsEvent {
    public String name;
    public Map<String, Object> payload = new HashMap<>();
    public long timestamp;

    public AnalyticsEvent(String name) {
        this(name, new HashMap<>());
    }

    public AnalyticsEvent(String name, Map<String, Object> payload) {
        this.name = name;
        this.payload = payload;
        this.timestamp = System.currentTimeMillis();
    }

    public static AnalyticsEvent forScreenView(String title) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", title);

        return new AnalyticsEvent("screen_view", payload);
    }

    public static AnalyticsEvent forLinkClick(String url) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("url", url);

        return new AnalyticsEvent("link_click", payload);
    }

    public static AnalyticsEvent simpleEvent(String eventName) {
        return new AnalyticsEvent(eventName);
    }

    private static Map<String, Object> getPayloadForStartingInstance(Instance instance, boolean offline,
        String reason, Integer timePlayed) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("offline", offline);
        payload.put("name", instance.launcher.pack);
        payload.put("version", instance.launcher.version);
        payload.put("platform", instance.getPlatformName());

        if (reason != null) {
            payload.put("reason", reason);
        }

        if (timePlayed != null) {
            payload.put("time_played", timePlayed);
        }

        return payload;
    }

    public static AnalyticsEvent forInstanceLaunchFailed(Instance instance, boolean offline, String reason) {
        return new AnalyticsEvent("instance_launch_failed",
            getPayloadForStartingInstance(instance, offline, reason, null));
    }

    public static AnalyticsEvent forInstanceLaunched(Instance instance, boolean offline) {
        return new AnalyticsEvent("instance_launched", getPayloadForStartingInstance(instance, offline, null, null));
    }

    public static AnalyticsEvent forInstanceLaunchCompleted(Instance instance, boolean offline, int timePlayed) {
        return new AnalyticsEvent("instance_launch_completed",
            getPayloadForStartingInstance(instance, offline, null, timePlayed));
    }

    public static AnalyticsEvent forInstanceEvent(String event, Instance instance) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", instance.launcher.pack);
        payload.put("version", instance.launcher.version);
        payload.put("platform", instance.getPlatformName());

        return new AnalyticsEvent(event, payload);
    }

    public static AnalyticsEvent forServerEvent(String event, Server server) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", server.pack);
        payload.put("version", server.version);

        return new AnalyticsEvent(event, payload);
    }

    public static AnalyticsEvent forInstanceAddLoader(Instance instance, LoaderType loaderType) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", instance.launcher.pack);
        payload.put("version", instance.launcher.version);
        payload.put("platform", instance.getPlatformName());
        payload.put("loader", loaderType.toString());

        return new AnalyticsEvent("instance_add_loader", payload);
    }

    public static AnalyticsEvent forInstanceLoaderEvent(String event, Instance instance, LoaderVersion loaderVersion) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", instance.launcher.pack);
        payload.put("version", instance.launcher.version);
        payload.put("platform", instance.getPlatformName());
        payload.put("loader", loaderVersion.getAnalyticsValue());

        return new AnalyticsEvent(event, payload);
    }

    public static AnalyticsEvent forToolRun(String name) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);

        return new AnalyticsEvent("tool_run", payload);
    }

    public static AnalyticsEvent forAccountAdd(String type) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("type", type);

        return new AnalyticsEvent("account_added", payload);
    }

    public static AnalyticsEvent forSearchEvent(String area, String query) {
        return AnalyticsEvent.forSearchEventPlatform(area, query, null, null);
    }

    public static AnalyticsEvent forSearchEvent(String area, String query, Integer page) {
        return AnalyticsEvent.forSearchEventPlatform(area, query, null, null);
    }

    public static AnalyticsEvent forSearchEventPlatform(String area, String query, Integer page, String platform) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("area", area);
        payload.put("query", query);

        if (page != null) {
            payload.put("page", page);
        }

        if (platform != null) {
            payload.put("platform", platform);
        }

        return new AnalyticsEvent("search", payload);
    }

    public static AnalyticsEvent forThemeChange(String theme) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", theme);

        return new AnalyticsEvent("theme_changed", payload);
    }

    public static AnalyticsEvent forAddMod(CurseForgeProject mod) {
        return AnalyticsEvent.forAddMod(mod.name, "CurseForge", "mod");
    }

    public static AnalyticsEvent forAddMod(ModrinthSearchHit mod) {
        return AnalyticsEvent.forAddMod(mod.title, "Modrinth", "mod");
    }

    public static AnalyticsEvent forAddMod(ModrinthProject mod) {
        return AnalyticsEvent.forAddMod(mod.title, "Modrinth", "mod");
    }

    public static AnalyticsEvent forAddPlugin(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forAddMod(plugin.title, "Modrinth", "plugin");
    }

    public static AnalyticsEvent forAddPlugin(CurseForgeProject mod) {
        return AnalyticsEvent.forAddMod(mod.name, "CurseForge", "plugin");
    }

    public static AnalyticsEvent forAddResourcePack(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forAddMod(plugin.title, "Modrinth", "resource_pack");
    }

    public static AnalyticsEvent forAddResourcePack(CurseForgeProject mod) {
        return AnalyticsEvent.forAddMod(mod.name, "CurseForge", "resource_pack");
    }

    public static AnalyticsEvent forAddShaders(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forAddMod(plugin.title, "Modrinth", "shader");
    }

    public static AnalyticsEvent forAddShaders(CurseForgeProject mod) {
        return AnalyticsEvent.forAddMod(mod.name, "CurseForge", "shader");
    }

    public static AnalyticsEvent forAddMod(String name, String platform, String type) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("platform", platform);
        payload.put("type", type);

        return new AnalyticsEvent("mod_add", payload);
    }

    public static AnalyticsEvent forRemoveMod(CurseForgeProject mod) {
        return AnalyticsEvent.forRemoveMod(mod.name, "CurseForge", "mod");
    }

    public static AnalyticsEvent forRemoveMod(ModrinthSearchHit mod) {
        return AnalyticsEvent.forRemoveMod(mod.title, "Modrinth", "mod");
    }

    public static AnalyticsEvent forRemovePlugin(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forRemoveMod(plugin.title, "Modrinth", "plugin");
    }

    public static AnalyticsEvent forRemovePlugin(CurseForgeProject mod) {
        return AnalyticsEvent.forRemoveMod(mod.name, "CurseForge", "plugin");
    }

    public static AnalyticsEvent forRemoveResourcePack(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forRemoveMod(plugin.title, "Modrinth", "resource_pack");
    }

    public static AnalyticsEvent forRemoveResourcePack(CurseForgeProject mod) {
        return AnalyticsEvent.forRemoveMod(mod.name, "CurseForge", "resource_pack");
    }

    public static AnalyticsEvent forRemoveShaders(ModrinthSearchHit plugin) {
        return AnalyticsEvent.forRemoveMod(plugin.title, "Modrinth", "shader");
    }

    public static AnalyticsEvent forRemoveShaders(CurseForgeProject mod) {
        return AnalyticsEvent.forRemoveMod(mod.name, "CurseForge", "shader");
    }

    public static AnalyticsEvent forRemoveMod(String name, String platform, String type) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("platform", platform);
        payload.put("type", type);

        return new AnalyticsEvent("mod_remove", payload);
    }

    public static AnalyticsEvent forAddedMod(CurseForgeProject mod, CurseForgeFile file) {
        return AnalyticsEvent.forAddedMod(mod.name, file.displayName, "CurseForge", "mod");
    }

    public static AnalyticsEvent forAddedMod(ModrinthProject mod, ModrinthVersion version) {
        return AnalyticsEvent.forAddedMod(mod.title, version.name, "Modrinth", "mod");
    }

    public static AnalyticsEvent forAddedPlugin(ModrinthProject plugin, ModrinthVersion version) {
        return AnalyticsEvent.forAddedMod(plugin.title, version.name, "Modrinth", "plugin");
    }

    public static AnalyticsEvent forAddedPlugin(CurseForgeProject mod, CurseForgeFile file) {
        return AnalyticsEvent.forAddedMod(mod.name, file.displayName, "CurseForge", "plugin");
    }

    public static AnalyticsEvent forAddedResourcePack(ModrinthProject plugin, ModrinthVersion version) {
        return AnalyticsEvent.forAddedMod(plugin.title, version.name, "Modrinth", "resource_pack");
    }

    public static AnalyticsEvent forAddedResourcePack(CurseForgeProject mod, CurseForgeFile file) {
        return AnalyticsEvent.forAddedMod(mod.name, file.displayName, "CurseForge", "resource_pack");
    }

    public static AnalyticsEvent forAddedShaders(ModrinthProject plugin, ModrinthVersion version) {
        return AnalyticsEvent.forAddedMod(plugin.title, version.name, "Modrinth", "shader");
    }

    public static AnalyticsEvent forAddedShaders(CurseForgeProject mod, CurseForgeFile file) {
        return AnalyticsEvent.forAddedMod(mod.name, file.displayName, "CurseForge", "shader");
    }

    public static AnalyticsEvent forAddedWorld(CurseForgeProject mod, CurseForgeFile file) {
        return AnalyticsEvent.forAddedMod(mod.name, file.displayName, "CurseForge", "world");
    }

    public static AnalyticsEvent forAddedMod(String name, String version, String platform, String type) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("version", version);
        payload.put("platform", platform);
        payload.put("type", type);

        return new AnalyticsEvent("mod_added", payload);
    }

    public static AnalyticsEvent forPackInstall(UnifiedModPackResultsFragment result) {
        return AnalyticsEvent.forPackInstall(result, false);
    }

    public static AnalyticsEvent forPackInstall(UnifiedModPackResultsFragment result, boolean server) {
        String platform;

        switch (result.platform()) {
            case ATLAUNCHER:
                platform = "ATLauncher";
                break;
            case CURSEFORGE:
                platform = "CurseForge";
                break;
            case FTB:
                platform = "FTB";
                break;
            case MODRINTH:
                platform = "Modrinth";
                break;
            case TECHNIC:
                platform = "Technic";
                break;
            default:
            case $UNKNOWN:
                platform = "Unknown";
                break;

        }

        return AnalyticsEvent.forPackInstall(result.name(), platform, server);
    }

    public static AnalyticsEvent forPackInstall(ModrinthProject project) {
        return AnalyticsEvent.forPackInstall(project.title, "Modrinth", false);
    }

    public static AnalyticsEvent forPackInstall(CurseForgeProject project) {
        return AnalyticsEvent.forPackInstall(project.name, "CurseForge", false);
    }

    public static AnalyticsEvent forPackInstall(TechnicModpackSlim pack) {
        return AnalyticsEvent.forPackInstall(pack.name, "Technic", false);
    }

    public static AnalyticsEvent forPackInstall(ModrinthSearchHit searchHit) {
        return AnalyticsEvent.forPackInstall(searchHit, false);
    }

    public static AnalyticsEvent forPackInstall(ModrinthSearchHit searchHit, boolean server) {
        return AnalyticsEvent.forPackInstall(searchHit.title, "Modrinth", server);
    }

    public static AnalyticsEvent forPackInstall(CurseForgeProject project, boolean server) {
        return AnalyticsEvent.forPackInstall(project.name, "CurseForge", server);
    }

    public static AnalyticsEvent forPackInstall(FTBPackManifest pack) {
        return AnalyticsEvent.forPackInstall(pack.name, "FTB", false);
    }

    public static AnalyticsEvent forPackInstall(Pack pack) {
        return AnalyticsEvent.forPackInstall(pack, false);
    }

    public static AnalyticsEvent forPackInstall(Pack pack, boolean server) {
        return AnalyticsEvent.forPackInstall(pack.name, "ATLauncher", server);
    }

    public static AnalyticsEvent forPackInstall(String name, String platform, boolean server) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("platform", platform);

        if (server) {
            payload.put("server", server);
        }

        return new AnalyticsEvent("pack_install", payload);
    }

    public static AnalyticsEvent forPackEvent(String event, String name) {
        return AnalyticsEvent.forPackEvent(event, name, null);
    }

    public static AnalyticsEvent forPackEvent(String event, String name, String platform) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);

        if (platform != null) {
            payload.put("platform", platform);
        }

        return new AnalyticsEvent(event, payload);
    }

    public static AnalyticsEvent forImportInstance(String method, String name) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", name);
        payload.put("method", method);

        return new AnalyticsEvent("instance_import", payload);
    }

    public static AnalyticsEvent forPackInstalled(Pack pack, PackVersion version, boolean server,
        boolean isReinstall, String platform, LoaderType loaderType) {
        final Map<String, Object> payload = new HashMap<>();
        payload.put("name", pack.name);
        payload.put("version", version.version);
        payload.put("platform", platform);

        if (server) {
            payload.put("server", server);
        }

        if (loaderType != null) {
            payload.put("loader", loaderType.toString());
        }

        return new AnalyticsEvent("pack_installed", payload);
    }
}
