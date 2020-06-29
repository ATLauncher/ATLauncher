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

/**
 * Test LaF.
 *
 * The UI defaults are loaded from TestLaF.properties, ATLauncherLaF.properties,
 * FlatDarkLaf.properties and FlatLaf.properties
 *
 * @see src/main/resources/com/atlauncher/themes/ATLauncherLaF.properties
 * @author Ryan Dowling
 */
@SuppressWarnings("serial")
public class TestLaF extends ATLauncherLaF {
    public static boolean install() {
        instance = new TestLaF();

        return install(instance);
    }

    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public String getDescription() {
        return "Test theme of ATLauncher";
    }
}
