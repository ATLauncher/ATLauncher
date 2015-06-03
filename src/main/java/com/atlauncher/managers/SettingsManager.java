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

import com.atlauncher.FileSystemData;
import com.atlauncher.data.Settings;
import com.atlauncher.nio.JsonFile;

import java.io.FileNotFoundException;
import java.nio.file.Files;

public class SettingsManager {
    private static volatile Settings settings;

    static {
        try {
            Runtime.getRuntime().addShutdownHook(new SettingsSaver());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public static void loadSettings() {
        boolean newFile = false;

        try {
            SettingsManager.settings = new JsonFile(FileSystemData.SETTINGS).convert(Settings.class);
        } catch (FileNotFoundException ignored) {
            newFile = true;
        } catch (Exception e) {
            LogManager.logStackTrace("Error loading settings file!", e);
        } finally {
            if (SettingsManager.settings == null) {
                SettingsManager.settings = new Settings();
                SettingsManager.settings.loadDefaults();
            }
        }

        if (Files.exists(FileSystemData.PROPERTIES)) {
            SettingsManager.settings.migrateProperties();
        }

        // Validates all the settings to make sure they're valid and deals with converting (such as strings to value)
        settings.validate();

        if (newFile) {
            SettingsManager.saveSettings();
        }
    }

    public static void saveSettings() {
        try {
            if (Files.notExists(FileSystemData.SETTINGS)) {
                Files.createFile(FileSystemData.SETTINGS);
            }

            new JsonFile(FileSystemData.SETTINGS, true).write(SettingsManager.settings);
        } catch (Exception e) {
            LogManager.logStackTrace("Error saving settings file!", e);
        }
    }

    private static final class SettingsSaver extends Thread {
        @Override
        public void run() {
            SettingsManager.saveSettings();
        }
    }
}
