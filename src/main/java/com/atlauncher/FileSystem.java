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
import com.atlauncher.utils.Utils;

public final class FileSystem {
    public static final Path USER_DOWNLOADS = Paths.get(System.getProperty("user.home"), "Downloads");
    public static final Path BASE_DIR = FileSystem.getCoreGracefully();
    public static final Path LOGS = BASE_DIR.resolve("logs");
    public static final Path BACKUPS = BASE_DIR.resolve("backups");
    public static final Path CACHE = BASE_DIR.resolve("cache");
    public static final Path LOADERS = BASE_DIR.resolve("loaders");
    public static final Path RUNTIMES = BASE_DIR.resolve("runtimes");

    public static final Path CONFIGS = BASE_DIR.resolve("configs");
    public static final Path COMMON = CONFIGS.resolve("common");
    public static final Path IMAGES = CONFIGS.resolve("images");
    public static final Path SKINS = IMAGES.resolve("skins");
    public static final Path JSON = CONFIGS.resolve("json");
    public static final Path THEMES = CONFIGS.resolve("themes");
    public static final Path TOOLS = CONFIGS.resolve("tools");

    public static final Path ASSETS = BASE_DIR.resolve("assets");
    public static final Path RESOURCES_LOG_CONFIGS = ASSETS.resolve("log_configs");
    public static final Path RESOURCES_VIRTUAL = ASSETS.resolve("virtual");
    public static final Path RESOURCES_OBJECTS = ASSETS.resolve("objects");
    public static final Path RESOURCES_INDEXES = ASSETS.resolve("indexes");

    public static final Path LIBRARIES = BASE_DIR.resolve("libraries");

    public static final Path DOWNLOADS = BASE_DIR.resolve("downloads");
    public static final Path INSTANCES = BASE_DIR.resolve("instances");
    public static final Path SERVERS = BASE_DIR.resolve("servers");
    public static final Path TEMP = BASE_DIR.resolve("temp");
    public static final Path FAILED_DOWNLOADS = BASE_DIR.resolve("faileddownloads");

    public static final Path CHECKING_SERVERS_JSON = CONFIGS.resolve("checkingservers.json");
    public static final Path USER_DATA = CONFIGS.resolve("userdata");
    public static final Path LAUNCHER_CONFIG = CONFIGS.resolve(Constants.LAUNCHER_NAME + ".conf");

    /**
     * This will organise the file system. This will remove old folders, create
     * directories needed as well as remove old directories no longer needed.
     *
     * @throws IOException
     */
    public static void organise() throws IOException {
        deleteDirectories();

        Utils.cleanTempDirectory();

        renameDirectories();

        createDirectories();
    }

    private static void deleteDirectories() throws IOException {
        if (Files.exists(CONFIGS.resolve("Jars"))) {
            FileUtils.delete(CONFIGS.resolve("Jars"));
        }

        if (Files.exists(CONFIGS.resolve("Languages"))) {
            FileUtils.delete(CONFIGS.resolve("Languages"));
        }

        if (Files.exists(CONFIGS.resolve("Libraries"))) {
            FileUtils.delete(CONFIGS.resolve("Libraries"));
        }

        if (Files.exists(CONFIGS.resolve("instancesdata"))) {
            FileUtils.delete(CONFIGS.resolve("instancesdata"));
        }

        if (Files.exists(RESOURCES_VIRTUAL.resolve("legacy"))) {
            FileUtils.delete(RESOURCES_VIRTUAL.resolve("legacy"));
        }
    }

    private static void renameDirectories() throws IOException {
        Path oldBackupsDir = BASE_DIR.resolve("Backups");
        if (Files.exists(oldBackupsDir)
                && (!Files.isSameFile(oldBackupsDir, BACKUPS) || (Files.isSameFile(oldBackupsDir, BACKUPS)
                        && BACKUPS.toRealPath().getFileName().toString().equals("Backups")))) {
            Files.move(oldBackupsDir, oldBackupsDir.resolveSibling("backupstemp"));
            Files.move(oldBackupsDir.resolveSibling("backupstemp"), BACKUPS);
        }

        Path oldInstancesDir = BASE_DIR.resolve("Instances");
        if (Files.exists(oldInstancesDir)
                && (!Files.isSameFile(oldInstancesDir, INSTANCES) || (Files.isSameFile(oldInstancesDir, INSTANCES)
                        && INSTANCES.toRealPath().getFileName().toString().equals("Instances")))) {
            Files.move(oldInstancesDir, oldInstancesDir.resolveSibling("instancestemp"));
            Files.move(oldInstancesDir.resolveSibling("instancestemp"), INSTANCES);
        }

        Path oldServersDir = BASE_DIR.resolve("Servers");
        if (Files.exists(oldServersDir)
                && (!Files.isSameFile(oldServersDir, SERVERS) || (Files.isSameFile(oldServersDir, SERVERS)
                        && SERVERS.toRealPath().getFileName().toString().equals("Servers")))) {
            Files.move(oldServersDir, oldServersDir.resolveSibling("serverstemp"));
            Files.move(oldServersDir.resolveSibling("serverstemp"), SERVERS);
        }

        Path oldTempDir = BASE_DIR.resolve("Temp");
        if (Files.exists(oldTempDir) && (!Files.isSameFile(oldTempDir, TEMP)
                || (Files.isSameFile(oldTempDir, TEMP) && TEMP.toRealPath().getFileName().toString().equals("Temp")))) {
            Files.move(oldTempDir, oldTempDir.resolveSibling("temptemp"));
            Files.move(oldTempDir.resolveSibling("temptemp"), TEMP);
        }

        Path oldDownloadsDir = BASE_DIR.resolve("Downloads");
        if (Files.exists(oldDownloadsDir)
                && (!Files.isSameFile(oldDownloadsDir, DOWNLOADS) || (Files.isSameFile(oldDownloadsDir, DOWNLOADS)
                        && DOWNLOADS.toRealPath().getFileName().toString().equals("Downloads")))) {
            Files.move(oldDownloadsDir, oldDownloadsDir.resolveSibling("downloadstemp"));
            Files.move(oldDownloadsDir.resolveSibling("downloadstemp"), DOWNLOADS);
        }

        Path oldFailedDownloadsDir = BASE_DIR.resolve("FailedDownloads");
        if (Files.exists(oldFailedDownloadsDir) && (!Files.isSameFile(oldFailedDownloadsDir, FAILED_DOWNLOADS)
                || (Files.isSameFile(oldFailedDownloadsDir, FAILED_DOWNLOADS)
                        && FAILED_DOWNLOADS.toRealPath().getFileName().toString().equals("FailedDownloads")))) {
            Files.move(oldFailedDownloadsDir, oldFailedDownloadsDir.resolveSibling("faileddownloadstemp"));
            Files.move(oldFailedDownloadsDir.resolveSibling("faileddownloadstemp"), FAILED_DOWNLOADS);
        }

        Path oldConfigsDir = BASE_DIR.resolve("Configs");
        if (Files.exists(oldConfigsDir)
                && (!Files.isSameFile(oldConfigsDir, CONFIGS) || (Files.isSameFile(oldConfigsDir, CONFIGS)
                        && CONFIGS.toRealPath().getFileName().toString().equals("Configs")))) {
            Files.move(oldConfigsDir, oldConfigsDir.resolveSibling("configstemp"));
            Files.move(oldConfigsDir.resolveSibling("configstemp"), CONFIGS);
        }

        Path oldCommonDir = CONFIGS.resolve("Common");
        if (Files.exists(oldCommonDir)
                && (!Files.isSameFile(oldCommonDir, COMMON) || (Files.isSameFile(oldCommonDir, COMMON)
                        && COMMON.toRealPath().getFileName().toString().equals("Common")))) {
            Files.move(oldCommonDir, oldCommonDir.resolveSibling("commontemp"));
            Files.move(oldCommonDir.resolveSibling("commontemp"), COMMON);
        }

        Path oldImagesDir = CONFIGS.resolve("Images");
        if (Files.exists(oldImagesDir)
                && (!Files.isSameFile(oldImagesDir, IMAGES) || (Files.isSameFile(oldImagesDir, IMAGES)
                        && IMAGES.toRealPath().getFileName().toString().equals("Images")))) {
            Files.move(oldImagesDir, oldImagesDir.resolveSibling("imagestemp"));
            Files.move(oldImagesDir.resolveSibling("imagestemp"), IMAGES);
        }

        Path oldSkinsDir = IMAGES.resolve("Skins");
        if (Files.exists(oldSkinsDir) && (!Files.isSameFile(oldSkinsDir, SKINS) || (Files.isSameFile(oldSkinsDir, SKINS)
                && SKINS.toRealPath().getFileName().toString().equals("Skins")))) {
            Files.move(oldSkinsDir, oldSkinsDir.resolveSibling("skinstemp"));
            Files.move(oldSkinsDir.resolveSibling("skinstemp"), SKINS);
        }

        Path oldJSONDir = CONFIGS.resolve("JSON");
        if (Files.exists(oldJSONDir) && (!Files.isSameFile(oldJSONDir, JSON)
                || (Files.isSameFile(oldJSONDir, JSON) && JSON.toRealPath().getFileName().toString().equals("JSON")))) {
            Files.move(oldJSONDir, oldJSONDir.resolveSibling("jsontemp"));
            Files.move(oldJSONDir.resolveSibling("jsontemp"), JSON);
        }

        Path oldThemesDir = CONFIGS.resolve("Themes");
        if (Files.exists(oldThemesDir)
                && (!Files.isSameFile(oldThemesDir, THEMES) || (Files.isSameFile(oldThemesDir, THEMES)
                        && THEMES.toRealPath().getFileName().toString().equals("Themes")))) {
            Files.move(oldThemesDir, oldThemesDir.resolveSibling("themestemp"));
            Files.move(oldThemesDir.resolveSibling("themestemp"), THEMES);
        }

        Path oldToolsDir = CONFIGS.resolve("Tools");
        if (Files.exists(oldToolsDir) && (!Files.isSameFile(oldToolsDir, TOOLS) || (Files.isSameFile(oldToolsDir, TOOLS)
                && TOOLS.toRealPath().getFileName().toString().equals("Tools")))) {
            Files.move(oldToolsDir, oldToolsDir.resolveSibling("toolstemp"));
            Files.move(oldToolsDir.resolveSibling("toolstemp"), TOOLS);
        }
    }

    private static void createDirectories() {
        FileUtils.createDirectory(BACKUPS);
        FileUtils.createDirectory(CACHE);
        FileUtils.createDirectory(INSTANCES);
        FileUtils.createDirectory(LIBRARIES);
        FileUtils.createDirectory(LOADERS);
        FileUtils.createDirectory(LOGS);
        FileUtils.createDirectory(RUNTIMES);
        FileUtils.createDirectory(SERVERS);
        FileUtils.createDirectory(TEMP);

        FileUtils.createDirectory(CONFIGS);
        FileUtils.createDirectory(COMMON);
        FileUtils.createDirectory(IMAGES);
        FileUtils.createDirectory(SKINS);
        FileUtils.createDirectory(JSON);
        FileUtils.createDirectory(THEMES);
        FileUtils.createDirectory(TOOLS);

        FileUtils.createDirectory(ASSETS);
        FileUtils.createDirectory(RESOURCES_INDEXES);
        FileUtils.createDirectory(RESOURCES_LOG_CONFIGS);
        FileUtils.createDirectory(RESOURCES_OBJECTS);
        FileUtils.createDirectory(RESOURCES_VIRTUAL);

        FileUtils.createDirectory(DOWNLOADS);
        FileUtils.createDirectory(FAILED_DOWNLOADS);
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
