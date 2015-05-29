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
package com.atlauncher.data;

import com.atlauncher.managers.LogManager;
import com.atlauncher.annot.Json;
import com.atlauncher.managers.LanguageManager;

import java.io.IOException;
import java.net.Proxy;
import java.util.Arrays;

@Json
public class Settings {
    // Non Gsonable fields
    private Server selectedServer = null;
    private Proxy proxy = null;

    // Gsonable fields
    private String server;
    private String forgeLoggingLevel;
    private String language = "English";
    private int initialMemory; // Initial RAM to use when launching Minecraft
    private int maximumMemory; // Maximum RAM to use when launching Minecraft
    private int permGen; // PermGenSize to use when launching Minecraft in MB
    private int windowWidth; // Width of the Minecraft window
    private int windowHeight; // Height of the Minecraft window
    private boolean maximiseMinecraft; // If Minecraft should start maximised
    private boolean saveCustomMods; // If custom mods should be saved between updates/reinstalls
    private boolean usingCustomJavaPath; // If the user is using a custom java path
    private String javaPath; // Users path to Java
    private String javaParamaters; // Extra Java paramaters when launching Minecraft
    private boolean advancedBackup; // If advanced backup is enabled
    private boolean sortPacksAlphabetically; // If to sort packs default alphabetically
    private boolean keepLauncherOpen; // If we should close the Launcher after Minecraft has closed
    private boolean enableConsole; // If to show the console by default
    private boolean enableTrayIcon; // If to enable tray icon
    private boolean enableLeaderboards; // If to enable the leaderboards
    private boolean enableLogs; // If to enable logs
    private boolean enableOpenEyeReporting; // If to enable OpenEye reporting
    private boolean enableProxy = false; // If proxy is in use
    private boolean enablePackTags = false;
    private String proxyHost; // The proxies host
    private int proxyPort; // The proxies port
    private String proxyType; // The type of proxy (socks, http)
    private int concurrentConnections; // Number of concurrent connections to open when downloading
    private int daysOfLogsToKeep; // Number of days of logs to keep
    private String theme; // The theme to use
    private String dateFormat; // The date format to use
    private boolean enableServerChecker; // If to enable server checker
    private int serverCheckerWait; // Time to wait in minutes between checking server status

    // General backup settings
    private boolean autoBackup; // Whether backups are created on instance close
    private String lastSelectedSync; // The last service selected for syncing
    private boolean notifyBackup; // Whether to notify the user on successful backup or restore

    // Dropbox settings
    private String dropboxFolderLocation; // Location of dropbox if defined by user

    private boolean hadPasswordDialog; // If the user has seen the password dialog
    private boolean firstTimeRun; // If this is the first time the Launcher has been run
    private boolean offlineMode; // If offline mode is enabled

    private static final String[] VALID_FORGE_LOGGING_LEVELS = {"SEVERE", "WARNING", "INFO", "CONFIG", "FINE",
            "FINER", "FINEST"};

    public void loadDefaults() {
        this.hadPasswordDialog = false;
        this.firstTimeRun = true;
        this.language = "English";
        this.forgeLoggingLevel = "INFO";
    }

    public void validate() {
        loadLanguage();

        checkForgeLoggingLevel();
    }

    public void loadLanguage() {
        if (!LanguageManager.isLanguageByName(this.language)) {
            LogManager.warn("Invalid language " + this.language + ". Defaulting to English!");
            this.language = "English";
        }

        try {
            Language.INSTANCE.load(this.language);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkForgeLoggingLevel() {
        if (!Arrays.asList(VALID_FORGE_LOGGING_LEVELS).contains(this.forgeLoggingLevel)) {
            LogManager.warn("Invalid Forge logging level " + this.forgeLoggingLevel + ". Defaulting to INFO!");
            this.forgeLoggingLevel = "INFO";
        }
    }
}
