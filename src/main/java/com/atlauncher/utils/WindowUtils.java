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
package com.atlauncher.utils;

import java.awt.Dimension;
import java.awt.Window;

public class WindowUtils {

    private static final int HEIGHT_PADDING = 20;
    private static final int WIDTH_PADDING = 50;

    public static void resizeForContent(Window window) {
        resizeForContent(window, true);
    }

    public static void resizeForContent(Window window, boolean addPadding) {
        window.pack();

        if (addPadding) {
            Dimension preferredSize = window.getPreferredSize();
            window.setSize(new Dimension(preferredSize.width + WIDTH_PADDING,
                    preferredSize.height + HEIGHT_PADDING));
        } else {
            window.setSize(window.getPreferredSize());
        }
    }

}
