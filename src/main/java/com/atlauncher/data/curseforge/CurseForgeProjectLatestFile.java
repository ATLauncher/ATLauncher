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

public class CurseForgeProjectLatestFile extends CurseForgeFile {
    public List<CurseForgeSortableGameVersion> sortableGameVersion;
    public String changelog;
    public boolean isCompatibleWithClient;
    public int categorySectionPackageType;
    public int restrictProjectFileAccess;
    public int projectStatus;
    public int renderCacheId;
    public int fileLegacyMappingId;
    public int projectId;
    public int parentProjectFileId;
    public int parentFileLegacyMappingId;
    public int fileTypeId; // unsure of the type of this one, as no public example
    public String exposeAsAlternative;
    public int packageFingerprintId;
    public int gameVersionMappingId;
    public int gameVersionId;
    public int gameId;
    public boolean isServerPack;
}
