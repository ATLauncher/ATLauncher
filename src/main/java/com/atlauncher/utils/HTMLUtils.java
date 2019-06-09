/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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

/**
 * Creates some basic helpers for HTML displays on things.
 */
public class HTMLUtils {
    /**
     * Creates a basic centered paragraph.
     *
     * @param text the text to be centered in a paragraph
     * @return the HTML for the text that's been centered and paragraphed
     */
    public static String centerParagraph(String text) {
        return "<html><p align=\"center\">" + text + "</p></html>";
    }
}
