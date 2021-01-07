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
package com.atlauncher;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import com.atlauncher.constants.Constants;
import com.atlauncher.managers.LogManager;
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
    public static final Path RESOURCES_VIRTUAL_LEGACY = RESOURCES_VIRTUAL.resolve("legacy");

    public static final Path LIBRARIES = BASE_DIR.resolve("libraries");

    public static final Path DOWNLOADS = BASE_DIR.resolve("downloads");
    public static final Path INSTANCES = BASE_DIR.resolve("instances");
    public static final Path SERVERS = BASE_DIR.resolve("servers");
    public static final Path TEMP = BASE_DIR.resolve("temp");
    public static final Path FAILED_DOWNLOADS = BASE_DIR.resolve("faileddownloads");

    public static final Path CHECKING_SERVERS_JSON = CONFIGS.resolve("checkingservers.json");
    public static final Path USER_DATA = CONFIGS.resolve("userdata");
    public static final Path LAUNCHER_CONFIG = CONFIGS.resolve(Constants.LAUNCHER_NAME + ".conf");
    public static final Path SETTINGS = CONFIGS.resolve(Constants.LAUNCHER_NAME + ".json");
    public static final Path ACCOUNTS = CONFIGS.resolve("accounts.json");

    /**
     * This will organise the file system. This will remove old folders, create
     * directories needed as well as remove old directories no longer needed.
     *
     * @throws IOException
     */
    public static void organise() throws IOException {
        deleteDirectories();

        cleanTempDirectory();

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
    }

    private static void cleanTempDirectory() {
        Utils.deleteContents(TEMP.toFile());
    }

    private static void renameDirectories() throws IOException {
        renameDirectory(BASE_DIR.resolve("Backups"), BACKUPS);
        renameDirectory(BASE_DIR.resolve("Instances"), INSTANCES);
        renameDirectory(BASE_DIR.resolve("Servers"), SERVERS);
        renameDirectory(BASE_DIR.resolve("Temp"), TEMP);
        renameDirectory(BASE_DIR.resolve("Downloads"), DOWNLOADS);
        renameDirectory(BASE_DIR.resolve("FailedDownloads"), FAILED_DOWNLOADS);
        renameDirectory(BASE_DIR.resolve("Configs"), CONFIGS);
        renameDirectory(CONFIGS.resolve("Common"), COMMON);
        renameDirectory(CONFIGS.resolve("Images"), IMAGES);
        renameDirectory(CONFIGS.resolve("Skins"), SKINS);
        renameDirectory(CONFIGS.resolve("JSON"), JSON);
        renameDirectory(CONFIGS.resolve("Themes"), THEMES);
        renameDirectory(CONFIGS.resolve("Tools"), TOOLS);
    }

    private static void renameDirectory(Path from, Path to) {
        if (!Files.exists(from)) {
            return;
        }

        try {
            boolean needToMove = false;

            // case sensitive file systems
            if (!Files.exists(to) || (Files.exists(to) && !Files.isSameFile(from, to))) {
                needToMove = true;
            }

            // case insensitive file systems
            if (Files.exists(to) && Files.isSameFile(from, to)
                    && to.toRealPath().getFileName().toString().equals(from.getFileName().toString())) {
                needToMove = true;
            }

            if (needToMove) {
                // we need to use an intemediary path due to case insensitive file systems
                Path intermediaryPath = from.resolveSibling(to.getFileName().toString() + "temp");
                Files.move(from, intermediaryPath, StandardCopyOption.REPLACE_EXISTING);
                Files.move(intermediaryPath, to, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Error renaming directory", e, false);
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
        FileUtils.createDirectory(RESOURCES_VIRTUAL_LEGACY);

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
