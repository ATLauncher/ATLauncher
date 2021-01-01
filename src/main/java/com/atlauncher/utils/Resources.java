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
package com.atlauncher.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.exceptions.ChunkyException;
import com.atlauncher.managers.LogManager;

public final class Resources {
    private static final Map<String, Object> resources = new HashMap<>();
    public static final String[] FONT_FAMILIES = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();

    private Resources() {
    }

    public static boolean isSystemFont(String name) {
        for (String str : FONT_FAMILIES) {
            if (str.equalsIgnoreCase(name)) {
                return true;
            }
        }

        return false;
    }

    public static Font makeFont(String name) {
        try {
            if (resources.containsKey(name)) {
                Object obj = resources.get(name);
                if (!(obj instanceof Font)) {
                    throw new ChunkyException("Reference for " + name + " ended up with a bad value, " + "suggested="
                            + Font.class.getName() + "; got=" + obj.getClass().getName());
                } else {
                    return (Font) obj;
                }
            } else {
                if (isSystemFont(name)) {
                    Font f = new Font(name, Font.PLAIN, 0);
                    resources.put(name, f);
                    return f;
                } else {
                    URL url = Resources.class.getResource("/assets/font/" + name + ".ttf");
                    if (url == null) {
                        LogManager.error("Cannot find font " + name);
                        return new Font("Sans-Serif", Font.PLAIN, 0);
                    } else {
                        Font f = Font.createFont(Font.TRUETYPE_FONT, url.openStream());
                        resources.put(name, f);
                        return f;
                    }
                }
            }
        } catch (Exception ex) {
            LogManager.logStackTrace("Cannot find font " + name, ex);
            return new Font("Sans-Serif", Font.PLAIN, 0);
        }
    }
}
