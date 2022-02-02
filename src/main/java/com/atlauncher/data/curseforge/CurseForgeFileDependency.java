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

public class CurseForgeFileDependency {
    // in both legacy and core api
    public int fileId;

    // renamed in core
    @SerializedName(value = "modId", alternate = { "addonId" })
    public int modId;

    @SerializedName(value = "relationType", alternate = { "type" })
    public int relationType;

    public boolean isRequired() {
        return this.relationType == 3;
    }

    public boolean isOptional() {
        return this.relationType == 2;
    }
}
