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
import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.basic.BasicLookAndFeel;

import com.atlauncher.utils.Resources;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

@SuppressWarnings("serial")
public class ATLauncherLaf extends FlatLaf {
    public static ATLauncherLaf instance;

    public String defaultFontName = "SansSerif";
    public String tabFontName = "Oswald-Regular";

    public static boolean install() {
        instance = new ATLauncherLaf();

        return install(instance);
    }

    public static ATLauncherLaf getInstance() {
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

    @Override
    public boolean isDark() {
        return true;
    }

    @Override
    public List<Class<?>> getLafClassesForDefaultsLoading() {
        List<Class<?>> classes = new ArrayList<>();

        classes.add(BasicLookAndFeel.class); // BasicLookAndFeel class
        classes.add(FlatLaf.class); // FlatLaf class

        // Add the themes base dark/light class
        if (isDark()) {
            classes.add(FlatDarkLaf.class);
        } else {
            classes.add(FlatLightLaf.class);
        }

        classes.add(ATLauncherLaf.class); // ATLauncher base class
        classes.add(getClass()); // Theme's class

        return classes;
    }
}
