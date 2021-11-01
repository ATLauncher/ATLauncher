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
package com.atlauncher.data.technic;

import java.util.List;

public class TechnicModpack {
    public int id;
    public String name;
    public String displayName;
    public String user;
    public String url;
    public String platformUrl;
    public String minecraft;
    public int ratings;
    public int downloads;
    public int runs;
    public String description;
    public String tags;
    public boolean isServer;
    public boolean isOfficial;
    public String version;
    public boolean forceDir;
    public List<TechnicModpackFeed> feed;
    public TechnicModpackAsset icon;
    public TechnicModpackAsset logo;
    public TechnicModpackAsset background;
    public String solder;
    public String discordServerId;
    public String serverPackUrl;
}
