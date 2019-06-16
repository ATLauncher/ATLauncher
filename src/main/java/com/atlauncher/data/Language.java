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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.atlauncher.App;
import com.atlauncher.LogManager;

public enum Language {
    INSTANCE, Language;

    private final Map<String, Properties> langs = new HashMap<>();
    private volatile String current;

    private Language() {
        try {
            this.load("English");
        } catch (IOException ex) {
            LogManager.logStackTrace("Failed to load language", ex);
        }
    }

    public static String[] available() {
        File[] files = App.settings.getLanguagesDir().listFiles((dir, name) -> name.endsWith(".lang"));
        String[] langs = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            langs[i] = files[i].getName().substring(0, 1).toUpperCase()
                    + files[i].getName().substring(1, files[i].getName().lastIndexOf("."));
        }
        return langs;
    }

    public static synchronized String current() {
        return INSTANCE.current;
    }

    public synchronized void load(String lang) throws IOException {
        if (!this.langs.containsKey(lang)) {
            Properties props = new Properties();
            File langFile = new File(App.settings.getLanguagesDir(), lang.toLowerCase() + ".lang");
            if (!langFile.exists()) {
                LogManager.error("Language file " + langFile.getName() + " doesn't exist! Defaulting it inbuilt one!");
                props.load(App.class.getResourceAsStream("/assets/lang/english.lang"));
            } else {
                props.load(new FileInputStream(langFile));
            }
            this.langs.put(lang, props);
            LogManager.info("Loading Language: " + lang);
        }

        this.current = lang;
    }

    public synchronized void reload(String lang) throws IOException {
        if (this.langs.containsKey(lang)) {
            this.langs.remove(lang);
        }

        this.load(lang);
    }

    public synchronized String localize(String lang, String tag) {
        if (this.langs.containsKey(lang)) {
            Properties props = this.langs.get(lang);
            if (props.containsKey(tag)) {
                return props.getProperty(tag, tag);
            } else {
                if (lang.equalsIgnoreCase("English")) {
                    return "Unknown language key " + tag;
                } else {
                    return this.localize("English", tag);
                }
            }
        } else {
            return this.localize("English", tag);
        }
    }

    public synchronized String localize(String tag) {
        return this.localize(this.current, tag);
    }

    public synchronized String localizeWithReplace(String tag, String replaceWith) {
        return this.localize(this.current, tag).replace("%s", replaceWith);
    }

    public synchronized String getCurrent() {
        return this.current;
    }
}
