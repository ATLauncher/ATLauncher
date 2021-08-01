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
package com.atlauncher.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

public class ConfigManager {

    /**
     * Gets a config item. Use dot notation to get an item from the config
     * ("loaders.fabric.enabled").
     *
     * TODO: make this not shit. It's pretty shit
     */
    public static <T> T getConfigItem(String key, T defaultValue) {
        if (hasConfigItem(key, Data.CONFIG_OVERRIDES)) {
            return getConfigItem(key, defaultValue, Data.CONFIG_OVERRIDES);
        }

        return getConfigItem(key, defaultValue, Data.CONFIG);
    }

    private static boolean hasConfigItem(String key, Map<String, Object> source) {
        if (source == null) {
            return false;
        }

        try {
            String[] keyParts = key.split("\\.");

            if (source == null) {
                return false;
            }

            if (keyParts.length == 1) {
                return source.containsKey(keyParts[0]);
            }

            Map<String, Object> secondLevel = (Map<String, Object>) source.get(keyParts[0]);

            if (secondLevel == null) {
                return false;
            }

            if (keyParts.length == 2) {
                return secondLevel.containsKey(keyParts[1]);
            }

            Map<String, Object> thirdLevel = (Map<String, Object>) secondLevel.get(keyParts[1]);

            if (thirdLevel == null) {
                return false;
            }
            return thirdLevel.containsKey(keyParts[2]);
        } catch (Throwable t) {
            LogManager.logStackTrace(String.format("Error checking if config value for key '%s' exists", key), t);
            return false;
        }
    }

    private static <T> T getConfigItem(String key, T defaultValue, Map<String, Object> source) {
        if (source == null) {
            return defaultValue;
        }

        try {
            String[] keyParts = key.split("\\.");

            Object data = source.get(keyParts[0]);

            if (keyParts.length == 1) {
                return (T) data;
            }

            Map<String, Object> secondLevel = (Map<String, Object>) data;

            if (keyParts.length == 2) {
                return (T) secondLevel.get(keyParts[1]);
            }

            Map<String, Object> thirdLevel = (Map<String, Object>) secondLevel.get(keyParts[1]);

            return (T) thirdLevel.get(keyParts[2]);
        } catch (Throwable t) {
            LogManager.logStackTrace(String.format("Error loading config value for key '%s'", key), t);
            return defaultValue;
        }
    }

    /**
     * Loads the config for use in the Launcher
     */
    public static void loadConfig() {
        PerformanceManager.start();
        LogManager.debug("Loading config");

        java.lang.reflect.Type type = new TypeToken<Map<String, Object>>() {
        }.getType();

        try {
            File fileDir = FileSystem.JSON.resolve("config.json").toFile();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(new FileInputStream(fileDir), StandardCharsets.UTF_8));

            Data.CONFIG = Gsons.DEFAULT.fromJson(in, type);
            in.close();
        } catch (JsonIOException | JsonSyntaxException | IOException e) {
            LogManager.logStackTrace(e);
        }

        if (App.configOverride != null) {
            try {
                Data.CONFIG_OVERRIDES = Gsons.DEFAULT.fromJson(App.configOverride, type);
            } catch (JsonIOException | JsonSyntaxException e) {
                LogManager.logStackTrace("Failed to read in config overrides", e);
            }
        }

        LogManager.debug("Finished loading config");
        PerformanceManager.end();
    }
}
