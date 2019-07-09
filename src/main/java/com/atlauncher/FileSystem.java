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
package com.atlauncher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.atlauncher.data.Constants;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.OS;

public final class FileSystem {
    public static final Path USER_DOWNLOADS = Paths.get(System.getProperty("user.home"), "Downloads");
    public static final Path BASE_DIR = FileSystem.getCoreGracefully();
    public static final Path LOGS = BASE_DIR.resolve("Logs");
    public static final Path BACKUPS = BASE_DIR.resolve("backups");
    public static final Path CONFIGS = BASE_DIR.resolve("Configs");
    public static final Path LOADERS = BASE_DIR.resolve("loaders");
    public static final Path RUNTIMES = BASE_DIR.resolve("runtimes");
    public static final Path THEMES = CONFIGS.resolve("Themes");
    public static final Path JSON = CONFIGS.resolve("JSON");
    public static final Path VERSIONS = CONFIGS.resolve("Versions");
    public static final Path IMAGES = CONFIGS.resolve("Images");
    public static final Path SKINS = IMAGES.resolve("Skins");
    public static final Path TOOLS = IMAGES.resolve("Tools");
    public static final Path COMMON = CONFIGS.resolve("Common");

    public static final Path ASSETS = BASE_DIR.resolve("assets");
    public static final Path RESOURCES_LOG_CONFIGS = ASSETS.resolve("log_configs");
    public static final Path RESOURCES_VIRTUAL = ASSETS.resolve("virtual");
    public static final Path RESOURCES_VIRTUAL_LEGACY = RESOURCES_VIRTUAL.resolve("legacy");
    public static final Path RESOURCES_OBJECTS = ASSETS.resolve("objects");
    public static final Path RESOURCES_INDEXES = ASSETS.resolve("indexes");

    public static final Path LIBRARIES = BASE_DIR.resolve("libraries");

    public static final Path LANGUAGES = CONFIGS.resolve("Languages");
    public static final Path DOWNLOADS = BASE_DIR.resolve("Downloads");
    public static final Path INSTANCES = BASE_DIR.resolve("Instances");
    public static final Path SERVERS = BASE_DIR.resolve("Servers");
    public static final Path TMP = BASE_DIR.resolve("Temp");
    public static final Path FAILED_DOWNLOADS = BASE_DIR.resolve("FailedDownloads");

    public static final Path INSTANCES_DATA = CONFIGS.resolve("instancesdata");
    public static final Path CHECKING_SERVERS_JSON = CONFIGS.resolve("checkingservers.json");
    public static final Path USER_DATE = CONFIGS.resolve("userdata");
    public static final Path LAUNCHER_CONFIG = CONFIGS.resolve(Constants.LAUNCHER_NAME + ".conf");

    /**
     * This will organise the file system. This will remove old folders, create
     * directories needed as well as remove old directories no longer needed.
     *
     * @throws IOException
     */
    public static void organise() throws IOException {
        if (Files.exists(CONFIGS.resolve("Jars"))) {
            FileUtils.delete(CONFIGS.resolve("Jars"));
        }

        renameDirectories();

        createDirectories();
    }

    private static void renameDirectories() throws IOException {
        Path oldBackupsDir = BASE_DIR.resolve("Backups");
        if (!Files.isSameFile(oldBackupsDir, BACKUPS) || (Files.isSameFile(oldBackupsDir, BACKUPS)
                && BACKUPS.toRealPath().getFileName().toString().equals("Backups"))) {
            Files.move(oldBackupsDir, oldBackupsDir.resolveSibling("backupstemp"));
            Files.move(oldBackupsDir.resolveSibling("backupstemp"), BACKUPS);
        }
    }

    private static void createDirectories() {
        FileUtils.createDirectory(BACKUPS);
        FileUtils.createDirectory(LIBRARIES);
        FileUtils.createDirectory(RUNTIMES);

        FileUtils.createDirectory(ASSETS);
        FileUtils.createDirectory(RESOURCES_INDEXES);
        FileUtils.createDirectory(RESOURCES_LOG_CONFIGS);
        FileUtils.createDirectory(RESOURCES_OBJECTS);
        FileUtils.createDirectory(RESOURCES_VIRTUAL);
        FileUtils.createDirectory(RESOURCES_VIRTUAL_LEGACY);
    }

    public static Path getDownloads() {
        return OS.isUsingMacApp() ? USER_DOWNLOADS : DOWNLOADS;
    }

    public static Path getCoreGracefully() {
        if (App.workingDir != null) {
            return App.workingDir;
        }

        if (OS.isLinux()) {
            try {
                return Paths.get(
                        App.class.getProtectionDomain().getCodeSource().getLocation().toURI().getSchemeSpecificPart())
                        .getParent();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                return Paths.get(System.getProperty("user.dir"), Constants.LAUNCHER_NAME);
            }
        } else {
            return Paths.get(System.getProperty("user.dir"));
        }
    }
}
