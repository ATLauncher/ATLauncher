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
package com.atlauncher.events.theme;

import com.atlauncher.App;
import com.atlauncher.events.AnalyticsActions;
import com.atlauncher.events.AnalyticsCategories;
import com.atlauncher.events.AnalyticsEvent;
import com.atlauncher.themes.ATLauncherLaf;

public final class ThemeChangedEvent extends ThemeEvent implements AnalyticsEvent {
    ThemeChangedEvent(final ATLauncherLaf theme){
        super(theme);
    }

    @Override
    public String getLabel() {
        return this.getTheme().getName();
    }

    @Override
    public String getCategory() {
        return AnalyticsCategories.LAUNCHER.getAnalyticsCategory();
    }

    @Override
    public String getAction() {
        return AnalyticsActions.CHANGE_THEME.getAnalyticsValue();
    }

    public static ThemeChangedEvent forTheme(final ATLauncherLaf theme){
        return new ThemeChangedEvent(theme);
    }

    public static ThemeChangedEvent forCurrentTheme(){
        return forTheme(App.THEME);
    }
}