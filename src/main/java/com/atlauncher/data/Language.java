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
package com.atlauncher.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.mini2Dx.gettext.GetText;
import org.mini2Dx.gettext.PoFile;

import com.atlauncher.App;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.Utils;

public class Language {
    public final static List<Locale> locales = new ArrayList<>();
    public final static Map<String, Locale> languages = new LinkedHashMap<>();
    public final static List<Locale> localesWithoutFont = new ArrayList<>();
    public final static List<Locale> localesWithoutTabFont = new ArrayList<>();
    public static String selected = Locale.ENGLISH.getDisplayName();
    public static Locale selectedLocale = Locale.ENGLISH;

    // add in the languages we have support for
    static {
        locales.add(Locale.ENGLISH); // English
        locales.add(new Locale("af", "ZA")); // Afrikaans
        locales.add(new Locale("ar", "SA")); // Arabic
        locales.add(new Locale("ca", "ES")); // Catalan
        locales.add(new Locale("zh", "CN")); // Chinese Simplified
        locales.add(new Locale("zh", "TW")); // Chinese Traditional
        locales.add(new Locale("cs", "CZ")); // Czech
        locales.add(new Locale("da", "DK")); // Danish
        locales.add(new Locale("nl", "NL")); // Dutch
        locales.add(new Locale("fi", "FI")); // Finnish
        locales.add(new Locale("fr", "FR")); // French
        locales.add(new Locale("de", "DE")); // German
        locales.add(new Locale("el", "GR")); // Greek
        locales.add(new Locale("he", "IL")); // Hebrew
        locales.add(new Locale("hu", "HU")); // Hungarian
        locales.add(new Locale("it", "IT")); // Italian
        locales.add(new Locale("ja", "JP")); // Japanese
        locales.add(new Locale("ko", "KR")); // Korean
        locales.add(new Locale("no", "NO")); // Norwegian
        locales.add(new Locale("pl", "PL")); // Polish
        locales.add(new Locale("pt", "PT")); // Portuguese
        locales.add(new Locale("pt", "BR")); // Portuguese, Brazilian
        locales.add(new Locale("ro", "RO")); // Romanian
        locales.add(new Locale("ru", "RU")); // Russian
        locales.add(new Locale("sr", "SP")); // Serbian
        locales.add(new Locale("es", "ES")); // Spanish
        locales.add(new Locale("sv", "SE")); // Swedish
        locales.add(new Locale("tr", "TR")); // Turkish
        locales.add(new Locale("uk", "UA")); // Ukranian

        localesWithoutFont.add(new Locale("ar", "SA"));
        localesWithoutFont.add(new Locale("zh", "CN"));
        localesWithoutFont.add(new Locale("zh", "TW"));
        localesWithoutFont.add(new Locale("he", "IL"));
        localesWithoutFont.add(new Locale("ja", "JP"));
        localesWithoutFont.add(new Locale("ko", "KR"));

        localesWithoutTabFont.add(new Locale("ar", "SA"));
        localesWithoutTabFont.add(new Locale("zh", "CN"));
        localesWithoutTabFont.add(new Locale("zh", "TW"));
        localesWithoutTabFont.add(new Locale("he", "IL"));
        localesWithoutTabFont.add(new Locale("el", "GR"));
        localesWithoutTabFont.add(new Locale("ja", "JP"));
        localesWithoutTabFont.add(new Locale("ko", "KR"));
    }

    public static void init() throws IOException {
        for (Locale locale : locales) {
            if (Utils.getResourceInputStream(
                    "/assets/lang/" + locale.getLanguage() + "-" + locale.getCountry() + ".po") != null) {
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
                        new PoFile(locale, App.class.getResourceAsStream(
                                "/assets/lang/" + locale.getLanguage() + "-" + locale.getCountry() + ".po")));
            } catch (IOException e) {
                LogManager.logStackTrace("Failed loading language po file for " + language, e);
                locale = Locale.ENGLISH;
                selected = Locale.ENGLISH.getDisplayName();
            }
        }

        selectedLocale = locale;

        GetText.setLocale(locale);
        RelocalizationManager.post();
    }

    public static boolean isLanguageByName(String language) {
        return languages.containsKey(language);
    }
}
