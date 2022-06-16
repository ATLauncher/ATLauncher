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

import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.data.modrinth.ModrinthSearchHit;

public final class AddModEvent extends AbstractAnalyticsEvent{
    private AddModEvent(final String name, final AnalyticsCategories category){
        super(name, AnalyticsActions.ADD, category);
    }

    public static AddModEvent forMod(final String name, final AnalyticsCategories category){
        return new AddModEvent(name, category);
    }

    public static AddModEvent forCurseForgeMod(final CurseForgeProject mod){
        return forMod(mod.name, AnalyticsCategories.CURSE_FORGE_MOD);
    }

    public static AddModEvent forModrinthMod(final ModrinthSearchHit mod){
        return forMod(mod.title, AnalyticsCategories.MODRINTH);
    }
}