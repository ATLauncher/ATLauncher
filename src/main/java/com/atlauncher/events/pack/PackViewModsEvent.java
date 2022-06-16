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
package com.atlauncher.events.pack;

import com.atlauncher.data.Pack;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategory;

public final class PackViewModsEvent extends PackEvent{
    PackViewModsEvent(final String name, final AnalyticsCategory category){
        super(name, "", AnalyticsActions.VIEW_MODS, category);
    }

    public static PackViewModsEvent of(final String name, final AnalyticsCategory category){
        return new PackViewModsEvent(name, category);
    }

    public static PackViewModsEvent of(final Pack pack){
        return PackViewModsEvent.of(pack.name, pack);
    }
}