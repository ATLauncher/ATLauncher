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

public class Constants {
    public static final LauncherVersion VERSION = new LauncherVersion(3, 2, 9, 4);
    public static final String LAUNCHER_NAME = "ATLauncher";
    public static final String DISCORD_CLIENT_ID = "589393213723246592";
    public static final String API_BASE_URL = "https://api.atlauncher.com/v1/launcher/";
    public static final String PASTE_CHECK_URL = "https://paste.atlauncher.com";
    public static final String PASTE_API_URL = "https://paste.atlauncher.com/api/create";
    public static final String CURSE_API_URL = "https://addons-ecs.forgesvc.net/api/v2";
    public static final int CURSE_FABRIC_CATEGORY_ID = 4780;
    public static final int CURSE_PAGINATION_SIZE = 40;
    public static final int CURSE_FABRIC_MOD_ID = 306612;
    public static final String FORGE_MAVEN = "https://files.minecraftforge.net/maven/net/minecraftforge/forge/";
    public static final String FABRIC_MAVEN = "https://maven.fabricmc.net/";
    public static final String LEGACY_JAVA_FIXER_URL = "https://files.minecraftforge.net/LegacyJavaFixer/legacyjavafixer-1.0.jar";
    public static final String LEGACY_JAVA_FIXER_MD5 = "12c337cb2445b56b097e7c25a5642710";
    public static final Server[] SERVERS = new Server[] {
            new Server("Auto", "download.nodecdn.net/containers/atl", true, false, true),
            new Server("Master Server (Testing Only)", "master.atlcdn.net", false, true, true) };
}
