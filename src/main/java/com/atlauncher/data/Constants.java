/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.atlauncher.App;

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

    public static final LauncherVersion VERSION;
    public static final String LAUNCHER_NAME = "ATLauncher";
    public static final String DEFAULT_THEME_CLASS = "com.atlauncher.themes.Dark";
    public static final String DISCORD_CLIENT_ID = "589393213723246592";
    public static final String GA_TRACKING_ID = "UA-88820616-7";
    public static final String CROWDIN_URL = "https://crowdin.com/project/atlauncher";
    public static final String SENTRY_DSN = "https://499c3bbc55cb434dad42a3ac670e2c91@sentry.io/1498519";
    public static final String API_BASE_URL = "https://api.atlauncher.com/v1/launcher/";
    public static final String API_HOST = "api.atlauncher.com";
    public static final String PASTE_CHECK_URL = "https://paste.atlauncher.com";
    public static final String PASTE_HOST = "paste.atlauncher.com";
    public static final String SERVERS_LIST_PACK = "https://servers.atlauncher.com/list/pack";
    public static final String PASTE_API_URL = "https://paste.atlauncher.com/api/create";
    public static final String CURSE_API_URL = "https://addons-ecs.forgesvc.net/api/v2";
    public static final String CURSE_HOST = "addons-ecs.forgesvc.net";
    public static final int CURSE_FABRIC_CATEGORY_ID = 4780;
    public static final int CURSE_PAGINATION_SIZE = 40;
    public static final int CURSE_FABRIC_MOD_ID = 306612;
    public static final int CURSE_MODS_SECTION_ID = 6;
    public static final int CURSE_RESOURCE_PACKS_SECTION_ID = 12;
    public static final int CURSE_WORLDS_SECTION_ID = 17;
    public static final String FORGE_MAVEN = "https://files.minecraftforge.net/maven/net/minecraftforge/forge";
    public static final String FORGE_MAVEN_BASE = "https://files.minecraftforge.net/maven/";
    public static final String FORGE_HOST = "files.minecraftforge.net";
    public static final String FABRIC_MAVEN = "https://maven.fabricmc.net/";
    public static final String FABRIC_HOST = "maven.fabricmc.net";
    public static final String DOWNLOAD_SERVER = "https://download.nodecdn.net/containers/atl";
    public static final String DOWNLOAD_HOST = "download.nodecdn.net";
    public static final String LAUNCHER_META_MINECRAFT = "https://launchermeta.mojang.com";
    public static final String MINECRAFT_LIBRARIES = "https://libraries.minecraft.net/";
    public static final String MINECRAFT_RESOURCES = "https://resources.download.minecraft.net";
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
}
