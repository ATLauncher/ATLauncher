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

public class ModrinthVersion {
    public String id;

    @SerializedName("mod_id")
    public String modId;

    @SerializedName("author_id")
    public String authorId;

    public boolean featured;
    public String name;

    @SerializedName("version_number")
    public String versionNumber;

    public String changelog;

    @SerializedName("changelog_url")
    public String changelogUrl;

    @SerializedName("date_published")
    public String datePublished;

    public int downloads;

    @SerializedName("version_type")
    public String versionType;

    public List<ModrinthFile> files;

    public List<String> dependencies;

    @SerializedName("game_versions")
    public List<String> gameVersions;

    public List<String> loaders;

    public ModrinthFile getPrimaryFile() {
        return files.stream().filter(f -> f.primary).findFirst().orElse(files.get(0));
    }

    public String toString() {
        String releaseTypeString = this.versionType.equalsIgnoreCase("release") ? ""
                : this.versionType.equalsIgnoreCase("beta") ? " (Beta)" : " (Alpha)";
        return this.name + releaseTypeString;
    }
}
