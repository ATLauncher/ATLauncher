/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
    public static final LauncherVersion VERSION = new LauncherVersion(3, 2, 3, 7);
    public static final String LAUNCHER_NAME = "ATLauncher";
    public static final String API_BASE_URL = "https://api.atlauncher.com/v1/launcher/";
    public static final String PASTE_CHECK_URL = "http://paste.atlauncher.com";
    public static final String PASTE_API_URL = "http://paste.atlauncher.com/api/create";
    public static final Server[] SERVERS = new Server[]{
            new Server("Auto", "download.nodecdn.net/containers/atl", true, false),
            new Server("Backup Server", "anne.nodecdn.net:8080/containers/atl", false, false),
            new Server("EU - Amsterdam 1", "bob.nodecdn.net/containers/atl", true, false),
            new Server("EU - Amsterdam 2", "emma.nodecdn.net/containers/atl", true, false),
            new Server("EU - Amsterdam 3", "lisa.nodecdn.net/containers/atl", true, false),
            new Server("US East - Ashburn 1", "anne.nodecdn.net/containers/atl", true, false),
            new Server("US East - Ashburn 2", "bruce.nodecdn.net/containers/atl", true, false),
            new Server("US East - Ashburn 3", "dave.nodecdn.net/containers/atl", true, false),
            new Server("US West - Phoenix 1", "adam.nodecdn.net/containers/atl", true, false),
            new Server("Master Server (Testing Only)", "master.atlcdn.net", false, true)
    };
}
