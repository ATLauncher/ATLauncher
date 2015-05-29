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
import com.atlauncher.Gsons;
import com.atlauncher.data.Settings;
import com.atlauncher.nio.JsonFile;

public class SettingsManager {
    private static Settings settings;

    public static void loadSettings() {
        try {
            SettingsManager.settings = new JsonFile(FileSystemData.SETTINGS).convert(Gsons.SETTINGS, Settings.class);
        } catch (Exception e) {
            SettingsManager.settings = new Settings();
            SettingsManager.settings.loadDefaults();
            LogManager.logStackTrace("Error loading settings file!", e);
        }

        // Validates all the settings to make sure they're valid and deals with converting (such as strings to value)
        settings.validate();
    }

    public static void saveSettings() {
        try {
            new JsonFile(FileSystemData.SETTINGS).write(SettingsManager.settings);
        } catch (Exception e) {
            LogManager.logStackTrace("Error saving settings file!", e);
        }
    }
}
