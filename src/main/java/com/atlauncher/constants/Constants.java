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
package com.atlauncher.constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.data.LauncherVersion;

public class Constants {
    static {
        String versionFromFile = new BufferedReader(
                new InputStreamReader(App.class.getResourceAsStream("/version"), StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("")).trim();
        String[] versionParts = versionFromFile.split("\\.", 4);

        String stream = "Release";

        if (versionParts[3].endsWith(".Beta")) {
            versionParts[3] = versionParts[3].replace(".Beta", "");
            stream = "Beta";
        }

        VERSION = new LauncherVersion(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]),
                Integer.parseInt(versionParts[2]), Integer.parseInt(versionParts[3]), stream);
    }

    // Launcher config
    public static final LauncherVersion VERSION;
    public static final String LAUNCHER_NAME = "ATLauncher";
    public static final String DEFAULT_THEME_CLASS = "com.atlauncher.themes.Dark";
    public static final String DISCORD_CLIENT_ID = "589393213723246592";
    public static final String GA_TRACKING_ID = "UA-88820616-7";
    public static final String CROWDIN_URL = "https://crowdin.com/project/atlauncher";
    public static final String SENTRY_DSN = "https://499c3bbc55cb434dad42a3ac670e2c91@sentry.io/1498519";

    // Launcher domains, endpoints, etc
    public static String BASE_LAUNCHER_PROTOCOL = "https://";
    public static String BASE_LAUNCHER_DOMAIN = "atlauncher.com";
    public static String API_BASE_URL = BASE_LAUNCHER_PROTOCOL + "api." + BASE_LAUNCHER_DOMAIN + "/v1/launcher/";
    public static String API_HOST = "api." + BASE_LAUNCHER_DOMAIN;
    public static String PASTE_CHECK_URL = BASE_LAUNCHER_PROTOCOL + "paste." + BASE_LAUNCHER_DOMAIN;
    public static String PASTE_HOST = "paste." + BASE_LAUNCHER_DOMAIN;
    public static String SERVERS_LIST_PACK = BASE_LAUNCHER_PROTOCOL + BASE_LAUNCHER_DOMAIN + "/servers/list/pack";
    public static String PASTE_API_URL = BASE_LAUNCHER_PROTOCOL + "paste." + BASE_LAUNCHER_DOMAIN + "/api/create";

    // CDN domains, endpoints, etc
    public static String BASE_CDN_PROTOCOL = "https://";
    public static String BASE_CDN_DOMAIN = "download.nodecdn.net";
    public static String BASE_CDN_PATH = "/containers/atl";
    public static String DOWNLOAD_SERVER = BASE_CDN_PROTOCOL + BASE_CDN_DOMAIN + BASE_CDN_PATH;
    public static String DOWNLOAD_HOST = BASE_CDN_DOMAIN;

    // CurseForge domains, endpoints, config, etc
    public static final String CURSEFORGE_API_URL = "https://addons-ecs.forgesvc.net/api/v2";
    public static final String CURSEFORGE_HOST = "addons-ecs.forgesvc.net";
    public static final int CURSEFORGE_FABRIC_CATEGORY_ID = 4780;
    public static final int CURSEFORGE_PAGINATION_SIZE = 40;
    public static final int CURSEFORGE_FABRIC_MOD_ID = 306612;
    public static final int CURSEFORGE_JUMPLOADER_MOD_ID = 361988;
    public static final int CURSEFORGE_MODS_SECTION_ID = 6;
    public static final int CURSEFORGE_MODPACKS_SECTION_ID = 4471;
    public static final int CURSEFORGE_RESOURCE_PACKS_SECTION_ID = 12;
    public static final int CURSEFORGE_WORLDS_SECTION_ID = 17;

    // Modrinth domains, endpoints, config, etc
    public static final String MODRINTH_API_URL = "https://api.modrinth.com/api/v1";
    public static final String MODRINTH_HOST = "api.modrinth.com";
    public static final String MODRINTH_FABRIC_MOD_ID = "P7dR8mSH";
    public static final int MODRINTH_PAGINATION_SIZE = 40;

    // Modpacks.ch domains, endpoints, config, etc
    public static final String MODPACKS_CH_API_URL = "https://api.modpacks.ch/public";
    public static final String MODPACKS_CH_HOST = "api.modpacks.ch";
    public static final int MODPACKS_CH_PAGINATION_SIZE = 20;

    // Forge domains, endpoints, etc
    public static final String FORGE_MAVEN = "https://maven.minecraftforge.net/net/minecraftforge/forge";
    public static final String FORGE_PROMOTIONS_FILE = "https://files.minecraftforge.net/net/minecraftforge/forge/promotions_slim.json";
    public static final String FORGE_MAVEN_BASE = "https://maven.minecraftforge.net/";
    public static final String FORGE_HOST = "maven.minecraftforge.net";

    // Fabric domains, endpoints, etc
    public static final String FABRIC_MAVEN = "https://maven.fabricmc.net/";
    public static final String FABRIC_HOST = "maven.fabricmc.net";

    // Minecraft domains, endpoints, etc
    public static final String LAUNCHER_META_MINECRAFT = "https://launchermeta.mojang.com";
    public static final String MINECRAFT_LIBRARIES = "https://libraries.minecraft.net/";
    public static final String MINECRAFT_RESOURCES = "https://resources.download.minecraft.net";
    public static final String MINECRAFT_VERSION_MANIFEST_URL = LAUNCHER_META_MINECRAFT
            + "/mc/game/version_manifest.json";
    public static final String MINECRAFT_JAVA_RUNTIME_URL = LAUNCHER_META_MINECRAFT
            + "/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    // Misc
    public static final String LEGACY_JAVA_FIXER_URL = "https://cdn.atlcdn.net/legacyjavafixer-1.0.jar";
    public static final String LEGACY_JAVA_FIXER_MD5 = "12c337cb2445b56b097e7c25a5642710";
    public static final String[] DATE_FORMATS = { "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd", "dd MMMM yyyy",
            "dd-MM-yyyy", "MM-dd-yyyy", "yyyy-MM-dd" };
    public static final String[] SCREEN_RESOLUTIONS = { "854x480", "1280x720", "1366x768", "1600x900", "1920x1080",
            "2560x1440", "3440x1440", "3840x2160" };
    public static final String DEFAULT_JAVA_PARAMETERS = "-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

    // Custom for ATLauncher Microsoft login constants
    public static final String MICROSOFT_LOGIN_CLIENT_ID = "90890812-00d1-48a8-8d3f-38465ef43b58";
    public static final int MICROSOFT_LOGIN_REDIRECT_PORT = 28562;
    public static final String MICROSOFT_LOGIN_REDIRECT_URL = "http://127.0.0.1:" + MICROSOFT_LOGIN_REDIRECT_PORT;
    public static final String MICROSOFT_LOGIN_REDIRECT_URL_ENCODED = "http%3A%2F%2F127.0.0.1%3A"
            + MICROSOFT_LOGIN_REDIRECT_PORT;
    public static final String[] MICROSOFT_LOGIN_SCOPES = { "XboxLive.signin", "XboxLive.offline_access" };

    // General Microsoft login constants
    public static final String MICROSOFT_LOGIN_URL = "https://login.live.com/oauth20_authorize.srf" + "?client_id="
            + MICROSOFT_LOGIN_CLIENT_ID
            + "&prompt=select_account&cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d&response_type=code" + "&scope="
            + String.join("%20", MICROSOFT_LOGIN_SCOPES) + "&redirect_uri=" + MICROSOFT_LOGIN_REDIRECT_URL_ENCODED;
    public static final String MICROSOFT_AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    public static final String MICROSOFT_XBL_AUTH_TOKEN_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String MICROSOFT_XSTS_AUTH_TOKEN_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MICROSOFT_MINECRAFT_LOGIN_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    public static final String MICROSOFT_MINECRAFT_STORE_URL = "https://api.minecraftservices.com/entitlements/mcstore";
    public static final String MICROSOFT_MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";

    public static void setBaseLauncherDomain(String baseLauncherDomain) {
        String host = baseLauncherDomain.replace("https://", "").replace("http://", "");

        BASE_LAUNCHER_PROTOCOL = baseLauncherDomain.startsWith("https://") ? "https://" : "http://";
        BASE_LAUNCHER_DOMAIN = host;
        API_BASE_URL = BASE_LAUNCHER_PROTOCOL + "api." + host + "/v1/launcher/";
        API_HOST = "api." + host;
        PASTE_CHECK_URL = BASE_LAUNCHER_PROTOCOL + "paste." + host;
        PASTE_HOST = "paste." + host;
        SERVERS_LIST_PACK = BASE_LAUNCHER_PROTOCOL + host + "/servers/list/pack";
        PASTE_API_URL = BASE_LAUNCHER_PROTOCOL + "paste." + host + "/api/create";
    }

    public static void setBaseCdnDomain(String baseCdnDomain) {
        String host = baseCdnDomain.replace("https://", "").replace("http://", "");

        BASE_CDN_PROTOCOL = baseCdnDomain.startsWith("https://") ? "https://" : "http://";
        BASE_CDN_DOMAIN = host;
        DOWNLOAD_SERVER = baseCdnDomain + BASE_CDN_PATH;
        DOWNLOAD_HOST = host;
    }

    public static void setBaseCdnPath(String baseCdnPath) {
        BASE_CDN_PATH = baseCdnPath;
        DOWNLOAD_SERVER = BASE_CDN_PROTOCOL + BASE_CDN_DOMAIN + baseCdnPath;
    }
}
