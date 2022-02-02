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

import com.google.gson.annotations.SerializedName;

public class CurseForgeCategory {
    public String name;
    public String slug;
    public String url;
    public String dateModified;
    public int gameId;

    // added in core
    public boolean isClass = false;

    // renamed in core
    // in both legacy and core api
    @SerializedName(value = "id", alternate = { "categoryId" })
    public int id;

    @SerializedName(value = "iconUrl", alternate = { "avatarUrl" })
    public String avatarUrl;

    @SerializedName(value = "parentCategoryId", alternate = { "parentId" })
    public int parentCategoryId;

    @SerializedName(value = "classId", alternate = { "rootId" })
    public int classId;
}
