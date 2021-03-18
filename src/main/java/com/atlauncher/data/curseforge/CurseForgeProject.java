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
package com.atlauncher.data.curseforge;

import java.util.List;

import com.atlauncher.constants.Constants;
import com.atlauncher.data.json.ModType;

public class CurseForgeProject {
    public int id;
    public String name;
    public List<CurseForgeAuthor> authors;
    public List<CurseForgeAttachment> attachments;
    public String websiteUrl;
    public int gameId;
    public String summary;
    public int defaultFileId;
    public int downloadCount;
    public List<CurseForgeProjectLatestFile> latestFiles;
    public List<CurseForgeCategory> categories;
    public int status;
    public int primaryCategoryId;
    public CurseForgeCategorySection categorySection;
    public String slug;
    public List<CurseForgeGameVersionLatestFiles> gameVersionLatestFiles;
    public boolean isFeatured;
    public float popularityScore;
    public int gamePopularityRank;
    public String primaryLanguage;
    public String gameSlug;
    public String gameName;
    public String portalName;
    public String dateModified;
    public String dateCreated;
    public String dateReleased;
    public boolean isAvailable;
    public boolean isExperimental;

    public ModType getModType() {
        if (categorySection.gameCategoryId == Constants.CURSEFORGE_RESOURCE_PACKS_SECTION_ID) {
            return ModType.resourcepack;
        }

        return ModType.mods;
    }

    public boolean equals(Object object) {
        return id == ((CurseForgeProject) object).id;
    }
}
