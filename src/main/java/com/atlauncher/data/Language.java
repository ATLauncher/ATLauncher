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
package com.atlauncher.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.atlauncher.LogManager;

import org.mini2Dx.gettext.GetText;

public class Language {
    public final static Map<String, Locale> languages = new HashMap<>();
    public static String selected = "English";

    public static void init() throws IOException {
        languages.put("English", Locale.ENGLISH);
    }

    public static void setLanguage(String language) throws IOException {
        Locale locale;

        if (isLanguageByName(language)) {
            LogManager.info("Language set to " + language);
            locale = languages.get(language);
            selected = language;
        } else {
            LogManager.info("Unknown language " + language + ". Defaulting to English");
            locale = Locale.ENGLISH;
            selected = "English";
        }

        GetText.setLocale(locale);
    }

    public static boolean isLanguageByName(String language) {
        return languages.containsKey(language);
    }
}
