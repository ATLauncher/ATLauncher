/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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
package com.atlauncher.managers;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.data.Language;
import com.atlauncher.nio.JsonFile;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LanguageManager {
    private static Language language;

    public static void loadLanguages() {
        Path file = FileSystem.JSON.resolve("newlanguages.json");

        try {
            java.lang.reflect.Type type = new TypeToken<List<Language>>() {
            }.getType();

            Data.LANGUAGES.clear();


            if (Files.exists(file)) {
                Data.LANGUAGES.addAll((List<Language>) new JsonFile(file).convert(type));
            } else {
                try (InputStreamReader isr = new InputStreamReader(App.class.getResourceAsStream
                        ("/assets/lang/newlanguages.json"))) {
                    Data.LANGUAGES.addAll((List<Language>) Gsons.DEFAULT.fromJson(isr, type));
                } catch (Exception ignored) {
                }
            }

            LanguageManager.setLanguage(SettingsManager.getLanguage());
        } catch (Exception e) {
            LogManager.logStackTrace("Error loading languages!", e);
        }

        // Load our English default language as we use it for other languages when missing a key
        Language en = LanguageManager.getLanguage("en");

        if (en != null) {
            en.load();
        }
    }

    public static Language getLanguage() {
        return LanguageManager.language;
    }

    public static Language getLanguage(String code) {
        for (Language lang : Data.LANGUAGES) {
            if (lang.getCode().equalsIgnoreCase(code)) {
                return lang;
            }
        }

        return null;
    }

    public static void setLanguage(String code) {
        LanguageManager.language = LanguageManager.getLanguage(code);

        if (LanguageManager.language == null) {
            LogManager.error("No language with the code " + code + " exists! Loading English!");
            LanguageManager.setLanguage("en");
        } else {
            LanguageManager.language.load();
            LogManager.info("Loaded language " + LanguageManager.language.getName());
        }
    }

    public static String localize(String key) {
        String ret = LanguageManager.language.getKey(key);

        if (ret == null) {
            Language en = LanguageManager.getLanguage("en");

            if (en != null) {
                ret = en.getKey(key);
            }
        }

        // If we're still null then there is no translation with the given key
        if (ret == null) {
            ret = "Unknown key " + key;
        }

        return ret;
    }

    public static String localizeWithReplace(String key, String replaceWith) {
        return LanguageManager.localize(key).replace("%s", replaceWith);
    }
}
