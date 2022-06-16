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

public final class NavigationEvent extends AbstractAnalyticsEvent{
    private final int page;

    NavigationEvent(final AnalyticsActions action, final AnalyticsCategory category, final int page){
        super(action, category);
        this.page = page;
    }

    public int getPage(){
        return this.page;
    }

    public static NavigationEvent nextPage(final int page, final AnalyticsCategory category){
        return new NavigationEvent(AnalyticsActions.NEXT_PAGE, category, page);
    }

    public static NavigationEvent previousPage(final int page, final AnalyticsCategory category){
        return new NavigationEvent(AnalyticsActions.PREVIOUS_PAGE, category, page);
    }
}
