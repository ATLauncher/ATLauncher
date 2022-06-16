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
package com.atlauncher.events;

import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;

public final class AddFileEvent extends AbstractAnalyticsEvent{
    AddFileEvent(final String name, final AnalyticsCategories category){
        super(name, AnalyticsActions.ADD_FILE, category);
    }

    public static AddFileEvent forMod(final String name, final AnalyticsCategories category){
        return new AddFileEvent(name, category);
    }

    public static AddFileEvent forCurseForgeMod(final String name, final String file){
        return forMod(String.format("%s - %s", name, file), AnalyticsCategories.CURSE_FORGE_MOD);
    }

    public static AddFileEvent forCurseForgeMod(final CurseForgeProject mod, final CurseForgeFile file){
        return forCurseForgeMod(mod.name, file.displayName);
    }

    public static AddFileEvent forModrinthMod(final String name, final String version){
        return forMod(String.format("%s - %s", name, version), AnalyticsCategories.MODRINTH);
    }

    public static AddFileEvent forModrinthMod(final ModrinthProject mod, final ModrinthVersion version){
        return forModrinthMod(mod.title, version.name);
    }
}