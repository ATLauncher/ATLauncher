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
package com.atlauncher.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.managers.LogManager;

import org.mini2Dx.gettext.GetText;
import org.mini2Dx.gettext.PoFile;

public class Language {
    public final static List<Locale> locales = new ArrayList<>();
    public final static Map<String, Locale> languages = new HashMap<>();
    public static String selected = Locale.ENGLISH.getDisplayName();

    // add in the languages we have support for
    static {
        locales.add(Locale.ENGLISH);
    }

    public static void init() throws IOException {
        for (Locale locale : locales) {
            if (App.class.getResourceAsStream("/assets/lang/" + locale.toString() + ".po") != null) {
                System.out.println(locale.toString());
                languages.put(locale.getDisplayName(), locale);
                LogManager.debug("Loaded language " + locale.getDisplayName() + " with key of " + locale);
            }
        }
    }

    public static void setLanguage(String language) {
        if (selected.equals(language)) {
            return;
        }

        Locale locale;

        if (isLanguageByName(language)) {
            LogManager.info("Language set to " + language);
            locale = languages.get(language);
            selected = language;
        } else {
            LogManager.info("Unknown language " + language + ". Defaulting to " + Locale.ENGLISH.getDisplayName());
            locale = Locale.ENGLISH;
            selected = Locale.ENGLISH.getDisplayName();
        }

        if (locale != Locale.ENGLISH) {
            try {
                GetText.add(
                        new PoFile(locale, App.class.getResourceAsStream("/assets/lang/" + locale.toString() + ".po")));
            } catch (IOException e) {
                LogManager.logStackTrace("Failed loading language po file for " + language, e);
                locale = Locale.ENGLISH;
                selected = Locale.ENGLISH.getDisplayName();
            }
        }

        GetText.setLocale(locale);
        RelocalizationManager.post();
    }

    public static boolean isLanguageByName(String language) {
        return languages.containsKey(language);
    }
}
