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

import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.FileSystemData;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.nio.JsonFile;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class LanguageManager {
    private static Language language;

    private static final DirectoryStream.Filter FILTER = new DirectoryStream.Filter<Path>() {
        @Override
        public boolean accept(Path path) throws IOException {
            return Files.isRegularFile(path) && path.toString().endsWith(".json");
        }
    };

    public static void loadLanguages() {
        try {
            java.lang.reflect.Type type = new TypeToken<List<Language>>() {
            }.getType();
            Data.LANGUAGES.clear();
            Data.LANGUAGES.addAll((List<Language>) JsonFile.of("newlanguages.json", type));
        } catch (Exception e) {
            LogManager.logStackTrace("Error loading langauges!", e);
        }
    }

    /**
     * Finds if a language is available
     *
     * @param name The name of the Language
     * @return true if found, false if not
     */
    public static boolean isLanguageByName(String name) {
        return LanguageManager.getLanguages().contains(name.toLowerCase());
    }

    public static List<String> getLanguages() {
        List<String> langs = new LinkedList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(FileSystem.LANGUAGES, LanguageManager.FILTER)) {
            for (Path file : stream) {
                String name = file.getFileName().toString();
                langs.add(name.substring(0, name.lastIndexOf(".")));
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return langs;
    }
}
