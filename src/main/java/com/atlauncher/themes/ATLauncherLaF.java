/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.Font;

import com.atlauncher.utils.Resources;
import com.formdev.flatlaf.FlatDarkLaf;

/**
 * LaF for ATLauncher.
 *
 * The UI defaults are loaded from ATLauncherLaF.properties,
 * FlatDarkLaf.properties and FlatLaf.properties
 *
 * @author Ryan Dowling
 */
@SuppressWarnings("serial")
public class ATLauncherLaF extends FlatDarkLaf {
    public static ATLauncherLaF instance;

    public String defaultFontName = "SansSerif";
    public String tabFontName = "Oswald-Regular";

    public static boolean install() {
        instance = new ATLauncherLaF();

        return install(instance);
    }

    public static ATLauncherLaF getInstance() {
        return instance;
    }

    public Font getNormalFont() {
        return Resources.makeFont(defaultFontName);
    }

    public Font getTabFont() {
        return Resources.makeFont(tabFontName);
    }

    @Override
    public String getName() {
        return "ATLauncher";
    }

    @Override
    public String getDescription() {
        return "Default theme of ATLauncher";
    }
}
