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
package com.atlauncher;

import com.atlauncher.utils.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class FileSystem {
    public static final Path USER_DOWNLOADS = Paths.get(System.getProperty("user.home"), "Downloads");
    public static final Path BASE_DIR = Utils.getCoreGracefully();
    public static final Path LOGS = BASE_DIR.resolve("Logs");
    public static final Path BACKUPS = BASE_DIR.resolve("Backups");
    public static final Path CONFIGS = BASE_DIR.resolve("Configs");
    public static final Path THEMES = CONFIGS.resolve("Themes");
    public static final Path JSON = CONFIGS.resolve("JSON");
    public static final Path VERSIONS = CONFIGS.resolve("Versions");
    public static final Path IMAGES = CONFIGS.resolve("Images");
    public static final Path SKINS = IMAGES.resolve("Skins");
    public static final Path JARS = CONFIGS.resolve("Jars");
    public static final Path COMMON = CONFIGS.resolve("Common");
    public static final Path RESOURCES = CONFIGS.resolve("Resources");
    public static final Path RESOURCES_VIRTUAL = RESOURCES.resolve("virtual");
    public static final Path RESOURCES_OBJECTS = RESOURCES.resolve("objects");
    public static final Path RESOURCES_INDEXES = RESOURCES.resolve("indexes");
    public static final Path RESOURCES_VIRTUAL_LEGACY = RESOURCES_VIRTUAL.resolve("legacy");
    public static final Path LIBRARIES = CONFIGS.resolve("Libraries");
    public static final Path LAUNCHER_LIBRARIES = LIBRARIES.resolve("Launcher");
    public static final Path LANGUAGES = CONFIGS.resolve("Languages");
    public static final Path DOWNLOADS = BASE_DIR.resolve("Downloads");
    public static final Path INSTANCES = BASE_DIR.resolve("Instances");
    public static final Path SERVERS = BASE_DIR.resolve("Servers");
    public static final Path TMP = BASE_DIR.resolve("Temp");
    public static final Path FAILED_DOWNLOADS = BASE_DIR.resolve("FailedDownloads");
}