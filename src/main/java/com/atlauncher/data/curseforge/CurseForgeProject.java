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
package com.atlauncher.data.curseforge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.atlauncher.annot.ExcludeFromGsonSerialization;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.json.ModType;
import com.google.gson.annotations.SerializedName;

public class CurseForgeProject {
    public int id;
    public String name;
    public List<CurseForgeAuthor> authors;
    public int gameId;
    public String summary;
    @ExcludeFromGsonSerialization
    public int downloadCount;
    @ExcludeFromGsonSerialization
    public List<CurseForgeFile> latestFiles;
    public List<CurseForgeCategory> categories;
    public int status;
    public int primaryCategoryId;
    public String slug;
    public boolean isFeatured;
    public String dateModified;
    public String dateCreated;
    public String dateReleased;
    public Map<String, String> links = new HashMap<>();
    public CurseForgeAttachment logo = null;
    public boolean allowModDistribution;

    @SerializedName(value = "screenshots", alternate = { "attachments" })
    public List<CurseForgeAttachment> screenshots;

    @SerializedName(value = "latestFilesIndexes", alternate = { "gameVersionLatestFiles" })
    @ExcludeFromGsonSerialization
    public List<CurseForgeGameVersionLatestFiles> latestFilesIndexes;

    @SerializedName(value = "mainFileId", alternate = { "defaultFileId" })
    public int mainFileId;

    public ModType getModType() {
        if (getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return ModType.resourcepack;
        }

        return ModType.mods;
    }

    public int getRootCategoryId() {
        Optional<CurseForgeCategory> primaryCategory = categories.stream().filter(c -> c.id == primaryCategoryId)
                .findFirst();

        if (primaryCategory.isPresent()) {
            return primaryCategory.get().classId;
        }

        return Constants.CURSEFORGE_MODS_SECTION_ID;
    }

    public Optional<CurseForgeAttachment> getLogo() {
        return Optional.ofNullable(logo);
    }

    public boolean equals(Object object) {
        return id == ((CurseForgeProject) object).id;
    }

    public String getWebsiteUrl() {
        return links.get("websiteUrl");
    }

    public boolean hasWebsiteUrl() {
        return links.containsKey("websiteUrl");
    }
}
