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

import com.atlauncher.FileSystem;
import com.atlauncher.FileSystemData;
import com.atlauncher.Network;
import com.atlauncher.data.OS;
import com.atlauncher.data.Server;
import com.atlauncher.data.Settings;
import com.atlauncher.nio.JsonFile;
import com.atlauncher.utils.Timestamper;

import java.io.FileNotFoundException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

public class SettingsManager {
    private static volatile Settings settings;

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
            if (Files.notExists(FileSystem.CONFIGS)) {
                Files.createDirectory(FileSystem.CONFIGS);
            }

            if (Files.notExists(FileSystemData.SETTINGS)) {
                Files.createFile(FileSystemData.SETTINGS);
            }

            new JsonFile(FileSystemData.SETTINGS, true).write(SettingsManager.settings);
        } catch (Exception e) {
            LogManager.logStackTrace("Error saving settings file!", e);
        }
    }

    /**
     * Gets the users setting for Forge Logging Level
     *
     * @return The users setting for Forge Logging Level
     */
    public static String getForgeLoggingLevel() {
        return SettingsManager.settings.forgeLoggingLevel;
    }

    /**
     * Sets the users setting for Forge Logging Level
     */
    public static void setForgeLoggingLevel(String forgeLoggingLevel) {
        SettingsManager.settings.forgeLoggingLevel = forgeLoggingLevel;
    }

    public static int getInitialMemory() {
        return SettingsManager.settings.initialMemory;
    }

    public static void setInitialMemory(int initialMemory) {
        SettingsManager.settings.initialMemory = initialMemory;
    }

    public static int getMaximumMemory() {
        return SettingsManager.settings.maximumMemory;
    }

    public static void setMaximumMemory(int memory) {
        SettingsManager.settings.maximumMemory = memory;
    }

    public static int getPermGen() {
        return SettingsManager.settings.permGen;
    }

    public static void setPermGen(int permGen) {
        SettingsManager.settings.permGen = permGen;
    }

    public static int getWindowWidth() {
        return SettingsManager.settings.windowWidth;
    }

    public static void setWindowWidth(int windowWidth) {
        SettingsManager.settings.windowWidth = windowWidth;
    }

    public static int getWindowHeight() {
        return SettingsManager.settings.windowHeight;
    }

    public static void setWindowHeight(int windowHeight) {
        SettingsManager.settings.windowHeight = windowHeight;
    }

    public static boolean isUsingCustomJavaPath() {
        return SettingsManager.settings.usingCustomJavaPath;
    }

    public static String getJavaPath() {
        return SettingsManager.settings.javaPath;
    }

    public static void setJavaPath(String javaPath) {
        SettingsManager.settings.usingCustomJavaPath = !javaPath.equalsIgnoreCase(OS.getJavaHome());
        SettingsManager.settings.javaPath = javaPath;
    }

    public static String getJavaParameters() {
        return SettingsManager.settings.javaParamaters;
    }

    public static void setJavaParameters(String javaParamaters) {
        SettingsManager.settings.javaParamaters = javaParamaters;
    }

    /**
     * If the user has selected to start Minecraft maximised
     *
     * @return true if yes, false if not
     */
    public static boolean startMinecraftMaximised() {
        return SettingsManager.settings.maximiseMinecraft;
    }

    public static void setStartMinecraftMaximised(boolean maximiseMinecraft) {
        SettingsManager.settings.maximiseMinecraft = maximiseMinecraft;
    }

    public static boolean saveCustomMods() {
        return SettingsManager.settings.saveCustomMods;
    }

    public static void setSaveCustomMods(boolean saveCustomMods) {
        SettingsManager.settings.saveCustomMods = saveCustomMods;
    }

    /**
     * If the user has selected to enable advanced backups
     *
     * @return true if yes, false if not
     */
    public static boolean isAdvancedBackupsEnabled() {
        return SettingsManager.settings.advancedBackup;
    }

    public static void setAdvancedBackups(boolean advancedBackup) {
        SettingsManager.settings.advancedBackup = advancedBackup;
    }

    /**
     * If the user has selected to display packs alphabetically or not
     *
     * @return true if yes, false if not
     */
    public static boolean sortPacksAlphabetically() {
        return SettingsManager.settings.sortPacksAlphabetically;
    }

    public static void setSortPacksAlphabetically(boolean sortPacksAlphabetically) {
        SettingsManager.settings.sortPacksAlphabetically = sortPacksAlphabetically;
    }

    /**
     * If the user has selected to show the console always or not
     *
     * @return true if yes, false if not
     */
    public static boolean enableConsole() {
        return SettingsManager.settings.enableConsole;
    }

    /**
     * If the user has selected to keep the launcher open after Minecraft has closed
     *
     * @return true if yes, false if not
     */
    public static boolean keepLauncherOpen() {
        return SettingsManager.settings.keepLauncherOpen;
    }

    /**
     * If the user has selected to enable the tray icon
     *
     * @return true if yes, false if not
     */
    public static boolean enableTrayIcon() {
        return SettingsManager.settings.enableTrayIcon;
    }

    public static void setEnableConsole(boolean enableConsole) {
        SettingsManager.settings.enableConsole = enableConsole;
    }

    public static void setKeepLauncherOpen(boolean keepLauncherOpen) {
        SettingsManager.settings.keepLauncherOpen = keepLauncherOpen;
    }

    public static String getLastSelectedSync() {
        if (SettingsManager.settings.lastSelectedSync == null) {
            setLastSelectedSync("Dropbox");
        }
        return SettingsManager.settings.lastSelectedSync;
    }

    public static void setLastSelectedSync(String lastSelected) {
        SettingsManager.settings.lastSelectedSync = lastSelected;
    }

    public static boolean getNotifyBackup() {
        return SettingsManager.settings.notifyBackup;
    }

    public static void setNotifyBackup(boolean notify) {
        SettingsManager.settings.notifyBackup = notify;
    }

    public static String getDropboxLocation() {
        return SettingsManager.settings.dropboxFolderLocation;
    }

    public static void setDropboxLocation(String dropboxLoc) {
        SettingsManager.settings.dropboxFolderLocation = dropboxLoc;
    }

    public static boolean getAutoBackup() {
        return SettingsManager.settings.autoBackup;
    }

    public static void setAutoBackup(boolean enableBackup) {
        SettingsManager.settings.autoBackup = enableBackup;
    }

    public static void setEnableTrayIcon(boolean enableTrayIcon) {
        SettingsManager.settings.enableTrayIcon = enableTrayIcon;
    }

    public static boolean enableLeaderboards() {
        return SettingsManager.settings.enableLeaderboards;
    }

    public static void setEnableLeaderboards(boolean enableLeaderboards) {
        SettingsManager.settings.enableLeaderboards = enableLeaderboards;
    }

    public static boolean enableLogs() {
        return SettingsManager.settings.enableLogs;
    }

    public static void setEnableLogs(boolean enableLogs) {
        SettingsManager.settings.enableLogs = enableLogs;
    }

    public static boolean enableServerChecker() {
        return SettingsManager.settings.enableServerChecker;
    }

    public static void setEnableServerChecker(boolean enableServerChecker) {
        SettingsManager.settings.enableServerChecker = enableServerChecker;
    }

    public static boolean enableOpenEyeReporting() {
        return SettingsManager.settings.enableOpenEyeReporting;
    }

    public static void setEnableOpenEyeReporting(boolean enableOpenEyeReporting) {
        SettingsManager.settings.enableOpenEyeReporting = enableOpenEyeReporting;
    }

    public static boolean getEnableProxy() {
        return SettingsManager.settings.enableProxy;
    }

    public static void setEnableProxy(boolean enableProxy) {
        SettingsManager.settings.enableProxy = enableProxy;
    }

    public static String getProxyHost() {
        return SettingsManager.settings.proxyHost;
    }

    public static void setProxyHost(String proxyHost) {
        SettingsManager.settings.proxyHost = proxyHost;
    }

    public static int getProxyPort() {
        return SettingsManager.settings.proxyPort;
    }

    public static void setProxyPort(int proxyPort) {
        SettingsManager.settings.proxyPort = proxyPort;
    }

    public static Proxy.Type getProxyType() {
        return SettingsManager.settings.proxyType;
    }

    public static void setProxyType(String proxyType) {
        SettingsManager.settings.proxyType = Proxy.Type.valueOf(proxyType);
    }

    public static void setProxyType(Proxy.Type proxyType) {
        SettingsManager.settings.proxyType = proxyType;
    }

    public static int getServerCheckerWait() {
        return SettingsManager.settings.serverCheckerWait;
    }

    public static void setServerCheckerWait(int serverCheckerWait) {
        SettingsManager.settings.serverCheckerWait = serverCheckerWait;
    }

    public static int getServerCheckerWaitInMilliseconds() {
        return SettingsManager.settings.serverCheckerWait * 60 * 1000;
    }

    public static int getConcurrentConnections() {
        return SettingsManager.settings.concurrentConnections;
    }

    public static void setConcurrentConnections(int concurrentConnections) {
        SettingsManager.settings.concurrentConnections = concurrentConnections;
    }

    public static int getDaysOfLogsToKeep() {
        return SettingsManager.settings.daysOfLogsToKeep;
    }

    public static void setDaysOfLogsToKeep(int daysOfLogsToKeep) {
        SettingsManager.settings.daysOfLogsToKeep = daysOfLogsToKeep;
    }

    public static String getTheme() {
        return SettingsManager.settings.theme;
    }

    public static void setTheme(String theme) {
        SettingsManager.settings.theme = theme;
    }

    public static Path getThemeFile() {
        Path theme = FileSystem.THEMES.resolve(SettingsManager.settings.theme + ".zip");

        if (Files.exists(theme)) {
            return theme;
        } else {
            return null;
        }
    }

    public static String getDateFormat() {
        return SettingsManager.settings.dateFormat;
    }

    public static void setDateFormat(String dateFormat) {
        SettingsManager.settings.dateFormat = dateFormat;
        Timestamper.updateDateFormat();
    }

    public static Proxy getProxy() {
        return SettingsManager.settings.proxy;
    }

    public static boolean hasHadPasswordDialog() {
        return SettingsManager.settings.hadPasswordDialog;
    }

    public static boolean enabledPackTags() {
        return SettingsManager.settings.enablePackTags;
    }

    public static void setPackTags(boolean enablePackTags) {
        SettingsManager.settings.enablePackTags = enablePackTags;
    }

    /**
     * Finds out if this is the first time the Launcher has been run
     *
     * @return true if the Launcher hasn't been run and setup yet, false for otherwise
     */
    public static boolean isFirstTimeRun() {
        return SettingsManager.settings.firstTimeRun;
    }

    public static void setFirstTimeRun(boolean firstTimeRun) {
        SettingsManager.settings.firstTimeRun = firstTimeRun;
    }

    public static Server getActiveServer() {
        return SettingsManager.settings.selectedServer;
    }

    public static void setServer(Server server) {
        SettingsManager.settings.selectedServer = server;
        SettingsManager.settings.server = server.getName();
    }

    public static void setLanguage(String language) {
        SettingsManager.settings.language = language;
        LanguageManager.setLanguage(language);
    }

    public static void configureProxy() {
        Network.CLIENT.setProxy(SettingsManager.getProxy());
        Network.PROGRESS_CLIENT.setProxy(SettingsManager.getProxy());
    }

    public static String getLanguage() {
        return SettingsManager.settings.language;
    }
}
