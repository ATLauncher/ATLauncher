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
package com.atlauncher.data.modrinth;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ModrinthSearchHit {
    @SerializedName("mod_id")
    public String modId;

    public String slug;
    public String author;
    public String title;
    public String description;
    public List<String> categories;
    public List<String> versions;
    public int downloads;

    @SerializedName("page_url")
    public String pageUrl;

    @SerializedName("icon_url")
    public String iconUrl;

    @SerializedName("author_url")
    public String authorUrl;

    @SerializedName("date_created")
    public String dateCreated;

    @SerializedName("date_modified")
    public String dateModified;

    @SerializedName("latest_version")
    public String latestVersion;

    public String license;

    @SerializedName("client_side")
    public String clientSide;

    @SerializedName("server_side")
    public String serverSide;

    public String host;
}
