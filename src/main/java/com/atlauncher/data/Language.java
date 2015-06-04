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

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public enum Language {
    INSTANCE;

    private final Map<String, Map<String, String>> langs = new HashMap<>();
    private volatile String current;

    private Language() {
        try {
            this.load("en");
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
        }
    }

    public static String[] available() {
        File[] files = FileSystem.LANGUAGES.toFile().listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".json");
            }
        });
        String[] langs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            langs[i] = files[i].getName().substring(0, 1).toUpperCase() + files[i].getName().substring(1, files[i]
                    .getName().lastIndexOf("."));
        }
        return langs;
    }

    public static synchronized String current() {
        return INSTANCE.current;
    }

    public void load(String lang) throws IOException {
        if (!this.langs.containsKey(lang)) {
            Map<String, String> trans = new HashMap<>();
            java.lang.reflect.Type type = new TypeToken<Map<String, String>>() {
            }.getType();

            Path langFile = FileSystem.LANGUAGES.resolve(lang.toLowerCase() + ".json");

            //            if (!Files.exists(langFile)) {
            //                LogManager.error("Language file " + langFile.getFileName() + " doesn't exist!
            // Defaulting it inbuilt " +
            //                        "one!");
            //                props.load(App.class.getResourceAsStream("/assets/lang/english.json"));
            //            } else {
            //                props.load(new FileInputStream(langFile.toFile()));
            //            }

            try {
                trans.putAll((Map<String, String>) new JsonFile(langFile).convert(type));
            } catch (Exception e) {
                LogManager.logStackTrace("Error loading language file " + langFile, e);
            }

            this.langs.put(lang, trans);
            LogManager.info("Loaded Language: " + lang);
        }

        this.current = lang;
    }

    public void reload(String lang) throws IOException {
        if (this.langs.containsKey(lang)) {
            this.langs.remove(lang);
        }

        this.load(lang);
    }

    public String localize(String lang, String tag) {
        if (this.langs.containsKey(lang)) {
            Map<String, String> trans = this.langs.get(lang);
            if (trans.containsKey(tag)) {
                return trans.get(tag);
            } else {
                if (lang.equalsIgnoreCase("en")) {
                    return "Unknown language key " + tag;
                } else {
                    return this.localize("en", tag);
                }
            }
        } else {
            return this.localize("en", tag);
        }
    }

    public String localize(String tag) {
        return this.localize(this.current, tag);
    }

    public String localizeWithReplace(String tag, String replaceWith) {
        return this.localize(this.current, tag).replace("%s", replaceWith);
    }

    public String getCurrent() {
        return this.current;
    }
}