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
package com.atlauncher.data;

import com.atlauncher.FileSystem;
import com.atlauncher.managers.LogManager;
import com.atlauncher.nio.JsonFile;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Language {
    private final Map<String, String> store = new HashMap<>();

    private String name;
    private String code;

    public void load() {
        Path path = FileSystem.LANGUAGES.resolve(this.code + ".json");

        if (!Files.exists(path)) {
            LogManager.error("Error loading language " + this.name + " as there is no json file with translations!");
            return;
        }


        java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
        }.getType();

        try {
            this.store.clear();
            this.store.putAll((Map<String, String>) new JsonFile(path).convert(type));
        } catch (Exception e) {
            LogManager.logStackTrace("Error loading language translations for " + this.name + "!", e);
        }
    }

    public String getName() {
        return this.name;
    }

    public String getCode() {
        return this.code;
    }

    public String getKey(String key) {
        if (this.store.containsKey(key)) {
            return this.store.get(key);
        }

        return null;
    }

    @Override
    public String toString() {
        return this.name;
    }
}