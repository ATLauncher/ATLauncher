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

import com.atlauncher.constants.Constants;
import com.atlauncher.data.json.ModType;
import com.google.gson.annotations.SerializedName;

public class CurseForgeProject {
    // in both legacy and core api
    public int id;
    public String name;
    public List<CurseForgeAuthor> authors;
    public int gameId;
    public String summary;
    public int downloadCount;
    public List<CurseForgeFile> latestFiles;
    public List<CurseForgeCategory> categories;
    public int status;
    public int primaryCategoryId;
    public String slug;
    public boolean isFeatured;
    public String dateModified;
    public String dateCreated;
    public String dateReleased;

    // new in core
    public Map<String, String> links = new HashMap<>();
    public CurseForgeAttachment logo = null;
    public boolean allowModDistribution;

    // renamed in core
    @SerializedName(value = "screenshots", alternate = { "attachments" })
    public List<CurseForgeAttachment> screenshots;

    @SerializedName(value = "latestFilesIndexes", alternate = { "gameVersionLatestFiles" })
    public List<CurseForgeGameVersionLatestFiles> latestFilesIndexes;

    @SerializedName(value = "mainFileId", alternate = { "defaultFileId" })
    public int mainFileId;

    // removed in core
    @Deprecated
    public String websiteUrl;
    @Deprecated
    public CurseForgeCategorySection categorySection;
    @Deprecated
    public float popularityScore;
    @Deprecated
    public int gamePopularityRank;
    @Deprecated
    public String primaryLanguage;
    @Deprecated
    public String gameSlug;
    @Deprecated
    public String gameName;
    @Deprecated
    public String portalName;
    @Deprecated
    public boolean isAvailable;
    @Deprecated
    public boolean isExperimental;

    public ModType getModType() {
        if (getRootCategoryId() == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return ModType.resourcepack;
        }

        return ModType.mods;
    }

    public int getRootCategoryId() {
        if (categorySection != null) {
            return categorySection.gameCategoryId;
        }

        Optional<CurseForgeCategory> primaryCategory = categories.stream().filter(c -> c.id == primaryCategoryId)
                .findFirst();

        if (primaryCategory.isPresent()) {
            return primaryCategory.get().classId;
        }

        return Constants.CURSEFORGE_MODS_SECTION_ID;
    }

    public Optional<CurseForgeAttachment> getLogo() {
        if (logo != null) {
            return Optional.ofNullable(logo);
        }

        return screenshots.stream().filter(a -> a.isDefault).findFirst();
    }

    public boolean equals(Object object) {
        return id == ((CurseForgeProject) object).id;
    }

    public String getWebsiteUrl() {
        return Optional.ofNullable(websiteUrl).orElseGet(() -> links.get("websiteUrl"));
    }
}
