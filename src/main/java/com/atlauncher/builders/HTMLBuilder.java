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
package com.atlauncher.builders;

import java.awt.Font;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.themes.ATLauncherLaf;

public final class HTMLBuilder {
    public boolean center = false;
    public String text;
    public Integer split;

    public HTMLBuilder center() {
        center = true;

        return this;
    }

    public HTMLBuilder text(String text) {
        this.text = text;

        return this;
    }

    public HTMLBuilder split(int length) {
        split = length;

        return this;
    }

    private String getText() {
        if (split == null) {
            return text;
        }

        char[] chars = text.toCharArray();
        StringBuilder sb = new StringBuilder();
        char spaceChar = ' ';
        int count = 0;
        for (char character : chars) {
            if (count >= split && character == spaceChar) {
                sb.append("<br/>");
                count = 0;
            } else {
                count++;
                sb.append(character);
            }
        }
        return sb.toString();
    }

    public String build() {
        String start = "";
        String end = "";

        if (center) {
            Font font = Optional.ofNullable(App.THEME)
                .map(ATLauncherLaf::getNormalFont)
                .orElse(new Font("Arial", Font.PLAIN, 12));
            
            start += "<p style=\"padding: 0;font-family: " + font.getFamily() + ";font-size: " + font.getSize()
                + "pt;\" align=\"center\">";
            end += "</p>";
        }

        return String.format("<html>%s%s%s</html>", start, getText(), end);
    }
}
