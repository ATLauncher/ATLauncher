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
package com.atlauncher.data.modrinth;

import com.atlauncher.annot.ExcludeFromGsonSerialization;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModrinthProject {
    public String id;
    public String slug;

    @SerializedName("project_type")
    public ModrinthProjectType projectType = ModrinthProjectType.MOD;

    public String team;
    public String title;
    public String description;
    public String body;

    @SerializedName("body_url")
    public String bodyUrl;

    public String published;
    public String updated;
    public ModrinthProjectStatus status;

    @SerializedName("moderator_message")
    @ExcludeFromGsonSerialization
    public ModrinthModeratorMessage moderatorMessage;

    public ModrinthLicense license;

    @SerializedName("client_side")
    public ModrinthSide clientSide;

    @SerializedName("server_side")
    public ModrinthSide serverSide;

    @ExcludeFromGsonSerialization
    public int downloads;
    @ExcludeFromGsonSerialization
    public int followers;

    public List<String> categories;
    @ExcludeFromGsonSerialization
    public List<String> versions;

    @SerializedName("icon_url")
    public String iconUrl;

    @SerializedName("issues_url")
    public String issuesUrl;

    @SerializedName("source_url")
    public String sourceUrl;

    @SerializedName("wiki_url")
    public String wikiUrl;

    @SerializedName("discord_url")
    public String discordUrl;

    @SerializedName("donation_urls")
    public List<ModrinthDonationUrl> donationUrls;

    // TODO: what is this?
    public List<Object> gallery;
}
