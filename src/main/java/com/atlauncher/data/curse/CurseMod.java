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
package com.atlauncher.data.curse;

import java.util.List;

import com.atlauncher.data.Constants;
import com.atlauncher.data.json.ModType;

public class CurseMod {
    public int id;
    public String name;
    public List<CurseAuthor> authors;
    public List<CurseAttachment> attachments;
    public String websiteUrl;
    public int gameId;
    public String summary;
    public int defaultFileId;
    public int downloadCount;
    public List<CurseModLatestFile> latestFiles;
    public List<CurseModCategory> categories;
    public int status;
    public int primaryCategoryId;
    public CurseCategory categorySection;
    public String slug;
    public List<CurseGameVersionLatestFiles> gameVersionLatestFiles;
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
    public boolean isExperiemental;

    public ModType getModType() {
        if (categorySection.gameCategoryId == Constants.CURSE_RESOURCE_PACKS_SECTION_ID) {
            return ModType.resourcepack;
        }

        return ModType.mods;
    }
}
