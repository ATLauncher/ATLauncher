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
package com.atlauncher.themes;

@SuppressWarnings("serial")
public class Dark extends ATLauncherLaf {
    public static boolean install() {
        instance = new Dark();

        return install(instance);
    }

    @Override
    public String getName() {
        return "Dark";
    }

    @Override
    public String getDescription() {
        return "Dark theme of ATLauncher";
    }

    @Override
    public boolean isDark() {
        return true;
    }
}
