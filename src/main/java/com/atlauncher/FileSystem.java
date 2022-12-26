/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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

import java.io.File;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class FileSystem {

    /**
     * If the launcher should use XDG Compliant directories
     *
     * @return false if in portable more, true when on linux and not portable
     */
    private static boolean useXdg() {
        return !App.IS_PORTABLE && OS.isLinux();
    }

    private static String getHomeDir() {
        return System.getProperty("user.home");
    }

    /**
     * Enumeration for XDG Base Directories
     */
    private enum XDG {
        /**
         * User-specific configuration files
         */
        CONFIG("XDG_CONFIG_HOME", getHomeDir() + "/.config"),
        /**
         * User-specific data
         */
        DATA("XDG_DATA_HOME", getHomeDir() + "/.local/share"),
        /**
         * Non-essential user-specific data
         */
        CACHE("XDG_CACHE_HOME", getHomeDir() + "/.cache");
        /**
         * Environment key to check
         */
        final String value;

        /**
         * Directory that should be used by default
         */
        final String defaultValue;

        XDG(String value, String defaultValue) {
            this.value = value;
            this.defaultValue = defaultValue;
        }
    }

    private static Path CACHED_USER_DOWNLOADS = null;
    public static final Path BASE_DIR = FileSystem.getCoreGracefully();

    private static final Path B_LOGS = BASE_DIR.resolve("logs");
    public static final Path LOGS =
        resolveDirectory(XDG.DATA, "logs", B_LOGS);

    private static final Path B_BACKUPS = BASE_DIR.resolve("backups");
    public static final Path BACKUPS =
        resolveDirectory(XDG.DATA, "backups", B_BACKUPS);

    private static final Path B_CACHE = BASE_DIR.resolve("cache");
    public static final Path CACHE =
        resolveDirectory(XDG.CACHE, null, B_CACHE);
    public static final Path APOLLO_CACHE = CACHE.resolve("apolloCache");
    public static final Path REMOTE_IMAGE_CACHE = CACHE.resolve("remote_image");

    private static final Path B_LOADERS = BASE_DIR.resolve("loaders");
    public static final Path LOADERS =
        resolveDirectory(XDG.DATA, "loaders", B_LOADERS);

    private static final Path B_RUNTIMES = BASE_DIR.resolve("runtimes");
    public static final Path RUNTIMES =
        resolveDirectory(XDG.DATA, "runtimes", B_RUNTIMES);
    public static final Path MINECRAFT_RUNTIMES = RUNTIMES.resolve("minecraft");

    private static final Path B_CONFIGS = BASE_DIR.resolve("configs");
    public static final Path CONFIGS =
        resolveDirectory(XDG.CONFIG, null, B_CONFIGS);
    public static final Path COMMON = CONFIGS.resolve("common");
    public static final Path IMAGES = CONFIGS.resolve("images");
    public static final Path SKINS = IMAGES.resolve("skins");
    public static final Path JSON = CONFIGS.resolve("json");
    public static final Path MINECRAFT_VERSIONS_JSON = JSON.resolve("minecraft");
    public static final Path THEMES = CONFIGS.resolve("themes");

    private static final Path B_ASSETS = BASE_DIR.resolve("assets");
    public static final Path ASSETS =
        resolveDirectory(XDG.DATA, "assets", B_ASSETS);
    public static final Path RESOURCES_LOG_CONFIGS = ASSETS.resolve("log_configs");
    public static final Path RESOURCES_VIRTUAL = ASSETS.resolve("virtual");
    public static final Path RESOURCES_OBJECTS = ASSETS.resolve("objects");
    public static final Path RESOURCES_INDEXES = ASSETS.resolve("indexes");
    public static final Path RESOURCES_VIRTUAL_LEGACY = RESOURCES_VIRTUAL.resolve("legacy");

    private static final Path B_LIBRARIES = BASE_DIR.resolve("libraries");
    public static final Path LIBRARIES =
        resolveDirectory(XDG.DATA, "libraries", B_LIBRARIES);

    private static final Path B_DOWNLOADS = BASE_DIR.resolve("downloads");
    public static final Path DOWNLOADS =
        resolveDirectory(XDG.DATA, "downloads", B_DOWNLOADS);
    public static final Path TECHNIC_DOWNLOADS = DOWNLOADS.resolve("technic");

    private static final Path B_INSTANCES = BASE_DIR.resolve("instances");
    public static final Path INSTANCES =
        resolveDirectory(XDG.DATA, "instances", B_INSTANCES);

    private static final Path B_SERVERS = BASE_DIR.resolve("servers");
    public static final Path SERVERS =
        resolveDirectory(XDG.DATA, "servers", B_SERVERS);

    public static final Path TEMP = resolveTemp();

    private static final Path B_FAILEDDOWNLOADS = BASE_DIR.resolve("faileddownloads");
    public static final Path FAILED_DOWNLOADS =
        resolveDirectory(XDG.DATA, "faileddownloads", B_FAILEDDOWNLOADS);

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
        deleteOldThings();

        cleanTempDirectory();

        renameDirectories();

        createDirectories();

        copyResourcesOutJar();

        moveToXDG();
    }

    /**
     * Move directories to XDG
     */
    private static void moveToXDG() {
        if (useXdg()) {
            renameDirectory(B_LOGS, LOGS);
            renameDirectory(B_BACKUPS, BACKUPS);
            renameDirectory(B_CACHE, CACHE);
            renameDirectory(B_LOADERS, LOADERS);
            renameDirectory(B_RUNTIMES, RUNTIMES);
            renameDirectory(B_CONFIGS, CONFIGS);
            renameDirectory(B_ASSETS, ASSETS);
            renameDirectory(B_LIBRARIES, LIBRARIES);
            renameDirectory(B_DOWNLOADS, DOWNLOADS);
            renameDirectory(B_INSTANCES, INSTANCES);
            renameDirectory(B_SERVERS, SERVERS);
            renameDirectory(B_FAILEDDOWNLOADS, FAILED_DOWNLOADS);
        }
    }

    public static void copyResourcesOutJar() throws IOException {
        Path legacyLaunchLibrariesJar = FileSystem.LIBRARIES.resolve("launcher/legacy-launch.jar");

        if (!Files.exists(legacyLaunchLibrariesJar) || Files.size(legacyLaunchLibrariesJar) != 8368l) {
            if (!Files.isDirectory(FileSystem.LIBRARIES.resolve("launcher"))) {
                Files.createDirectory(FileSystem.LIBRARIES.resolve("launcher"));
            }

            Files.copy(FileSystem.class.getResourceAsStream("/legacy-launch-jar"),
                legacyLaunchLibrariesJar, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteOldThings() throws IOException {
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

        if (Files.exists(CONFIGS.resolve("tools"))) {
            FileUtils.delete(CONFIGS.resolve("tools"));
        }

        if (Files.exists(JSON.resolve("version_manifest.json"))) {
            FileUtils.delete(JSON.resolve("version_manifest.json"));
        }

        if (Files.exists(JSON.resolve("additive_versions.json"))) {
            FileUtils.delete(JSON.resolve("additive_versions.json"));
        }

        if (Files.exists(RUNTIMES.resolve("1.8.0_51"))) {
            FileUtils.delete(RUNTIMES.resolve("1.8.0_51"));
        }
    }

    public static Path getUserDownloadsPath(boolean useSetting) {
        if (useSetting && App.settings != null && App.settings.customDownloadsPath != null) {
            try {
                return Paths.get(App.settings.customDownloadsPath);
            } catch (Exception e) {
                LogManager.logStackTrace(
                    "Problem when reading custom downloads path, defaulting to user downloads folder.", e);
            }
        }

        if (CACHED_USER_DOWNLOADS != null) {
            return CACHED_USER_DOWNLOADS;
        }

        try {
            if (OS.isWindows()) {
                String output = Utils.runProcess("reg", "query",
                    "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\User Shell Folders", "/f",
                    "{374DE290-123F-4565-9164-39C4925E467B}", "/t",
                    "REG_EXPAND_SZ", "/s");

                for (String line : output.split("\\r?\\n")) {
                    if (line.contains("REG_EXPAND_SZ")) {
                        String downloadsFolderPath = line.substring(line.indexOf("REG_EXPAND_SZ") + 13).trim();

                        if (Files.exists(Paths.get(downloadsFolderPath))) {
                            CACHED_USER_DOWNLOADS = Paths.get(downloadsFolderPath);
                            return CACHED_USER_DOWNLOADS;
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Problem when reading in registry", e);
        }

        CACHED_USER_DOWNLOADS = Paths.get(getHomeDir(), "Downloads");
        return CACHED_USER_DOWNLOADS;
    }

    public static Path getUserDownloadsPath() {
        return getUserDownloadsPath(true);
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
        FileUtils.createDirectory(BASE_DIR);

        FileUtils.createDirectory(BACKUPS);
        FileUtils.createDirectory(CACHE);
        FileUtils.createDirectory(REMOTE_IMAGE_CACHE);
        FileUtils.createDirectory(INSTANCES);
        FileUtils.createDirectory(LIBRARIES);
        FileUtils.createDirectory(LOADERS);
        FileUtils.createDirectory(LOGS);
        FileUtils.createDirectory(RUNTIMES);
        FileUtils.createDirectory(MINECRAFT_RUNTIMES);
        FileUtils.createDirectory(SERVERS);
        FileUtils.createDirectory(TEMP);

        FileUtils.createDirectory(CONFIGS);
        FileUtils.createDirectory(COMMON);
        FileUtils.createDirectory(IMAGES);
        FileUtils.createDirectory(SKINS);
        FileUtils.createDirectory(JSON);
        FileUtils.createDirectory(MINECRAFT_VERSIONS_JSON);
        FileUtils.createDirectory(THEMES);

        FileUtils.createDirectory(ASSETS);
        FileUtils.createDirectory(RESOURCES_INDEXES);
        FileUtils.createDirectory(RESOURCES_LOG_CONFIGS);
        FileUtils.createDirectory(RESOURCES_OBJECTS);
        FileUtils.createDirectory(RESOURCES_VIRTUAL);
        FileUtils.createDirectory(RESOURCES_VIRTUAL_LEGACY);

        FileUtils.createDirectory(DOWNLOADS);
        FileUtils.createDirectory(TECHNIC_DOWNLOADS);
        FileUtils.createDirectory(FAILED_DOWNLOADS);
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

    /**
     * Attempt to resolve the directory
     *
     * @param xdg         Directory as specified by XDG Base Directories
     * @param subDir      If set, a subdirectory will be used
     * @param defaultPath Directory to fall back on
     * @return Resolved directory
     */
    private static @NotNull Path resolveDirectory(@NotNull XDG xdg, @Nullable String subDir, @NotNull Path defaultPath) {
        // Only resolve compliance when enabled
        if (useXdg()) {
            String envPath = System.getenv(xdg.value);

            if (envPath != null && !envPath.isEmpty()) {
                File envDir = new File(envPath, Constants.LAUNCHER_NAME);

                // Append sub dir
                if (subDir != null)
                    envDir = new File(envDir, subDir);

                return envDir.toPath();
            } else {
                // XDG is not set, attempting compliant
                File compliantDefault = new File(xdg.defaultValue, Constants.LAUNCHER_NAME);

                // Append sub dir
                if (subDir != null)
                    compliantDefault = new File(compliantDefault, subDir);

                return compliantDefault.toPath();
            }
        }
        return defaultPath;
    }

    /**
     * Checks if the launcher,
     * while in portable mode,
     * is in an empty directory.
     *
     * @return true if the directory is populated
     */
    public static boolean isPortablePopulated() {
        if (!useXdg()) {
            return Files.exists(FileSystem.BASE_DIR)
                && Files.notExists(FileSystem.CONFIGS)
                && FileSystem.BASE_DIR.toFile().listFiles().length > 2;
        }
        return false;
    }

    /**
     * Resolve the temp directory.
     * On linux, there is a dedicated temp directory that clears out.
     *
     * @return temp directory to use
     */
    private static Path resolveTemp() {
        if (useXdg()) {
            return new File("/tmp", "ATLauncher").toPath();
        }
        return BASE_DIR.resolve("temp");
    }

}
