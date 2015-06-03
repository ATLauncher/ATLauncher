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

import com.atlauncher.FileSystemData;
import com.atlauncher.annot.Json;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.ServerManager;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Utils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

@Json
public class Settings {
    // Non Gsonable fields
    private transient Server selectedServer = null;
    private transient Proxy proxy = null;

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
    private Proxy.Type proxyType; // The type of proxy (socks, http)
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

    private String lastAccount;
    private String addedPacks;

    private static final String[] VALID_FORGE_LOGGING_LEVELS = {"SEVERE", "WARNING", "INFO", "CONFIG", "FINE",
            "FINER", "FINEST"};

    public void loadDefaults() {
        this.server = "Auto";

        this.hadPasswordDialog = false;
        this.firstTimeRun = true;
        this.language = "English";
        this.forgeLoggingLevel = "INFO";
        this.initialMemory = 256;
        this.maximumMemory = Utils.getMaximumSafeRam();
        this.permGen = (Utils.is64Bit() ? 256 : 128);
        this.windowWidth = 854;
        this.windowHeight = 480;
        this.usingCustomJavaPath = false;
        this.javaParamaters = "";
        this.maximiseMinecraft = false;
        this.saveCustomMods = true;
        this.advancedBackup = false;
        this.sortPacksAlphabetically = false;
        this.keepLauncherOpen = true;
        this.enableConsole = true;
        this.enablePackTags = false;
        this.enableTrayIcon = true;
        this.enableLeaderboards = false;
        this.enableLogs = true;
        this.enableServerChecker = false;
        this.enableOpenEyeReporting = true;

        this.enableProxy = false;
        this.proxyHost = "";
        this.proxyPort = 0;
        this.proxyType = Proxy.Type.DIRECT;

        this.serverCheckerWait = 5;
        this.concurrentConnections = 8;
        this.daysOfLogsToKeep = 7;

        this.theme = Constants.LAUNCHER_NAME;

        this.dateFormat = "dd/M/yyy";

        this.lastAccount = "";

        this.addedPacks = "";

        this.autoBackup = false;
        this.notifyBackup = true;
        this.dropboxFolderLocation = "";
    }

    public void migrateProperties() {
        Properties properties = new Properties();

        try {
            properties.load(Files.newInputStream(FileSystemData.PROPERTIES));

            this.theme = properties.getProperty("theme", Constants.LAUNCHER_NAME);
            this.dateFormat = properties.getProperty("dateformat", "dd/M/yyy");
            this.enablePackTags = Boolean.parseBoolean(properties.getProperty("enablepacktags", "false"));
            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole", "true"));
            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon", "true"));

            if (!properties.containsKey("usingcustomjavapath")) {
                this.usingCustomJavaPath = false;
            } else {
                this.usingCustomJavaPath = Boolean.parseBoolean(properties.getProperty("usingcustomjavapath", "false"));
                if (this.usingCustomJavaPath) {
                    this.javaPath = properties.getProperty("javapath", Utils.getJavaHome());
                }
            }

            this.enableProxy = Boolean.parseBoolean(properties.getProperty("enableproxy", "false"));

            if (this.enableProxy) {
                this.proxyHost = properties.getProperty("proxyhost", null);

                this.proxyPort = Integer.parseInt(properties.getProperty("proxyport", "0"));

                String proxyType = properties.getProperty("proxytype", "");
                if (!proxyType.equals("SOCKS") && !proxyType.equals("HTTP") && !proxyType.equals("DIRECT")) {
                    this.enableProxy = false;
                } else {
                    this.proxyType = Proxy.Type.valueOf(proxyType);
                }
            }

            this.concurrentConnections = Integer.parseInt(properties.getProperty("concurrentconnections", "8"));
            this.daysOfLogsToKeep = Integer.parseInt(properties.getProperty("daysoflogstokeep", "7"));
            this.server = properties.getProperty("server", "Auto");

            this.firstTimeRun = Boolean.parseBoolean(properties.getProperty("firsttimerun", "true"));

            this.hadPasswordDialog = Boolean.parseBoolean(properties.getProperty("hadpassworddialog", "false"));

            this.language = properties.getProperty("language", "English");

            this.forgeLoggingLevel = properties.getProperty("forgelogginglevel", "INFO");
            this.maximumMemory = Integer.parseInt(properties.getProperty("ram", Utils.getSafeMaximumRam() + ""));
            this.initialMemory = Integer.parseInt(properties.getProperty("initialmemory", "256"));
            this.permGen = Integer.parseInt(properties.getProperty("permGen", (Utils.is64Bit() ? "256" : "128")));
            this.windowWidth = Integer.parseInt(properties.getProperty("windowwidth", "854"));
            this.windowHeight = Integer.parseInt(properties.getProperty("windowheight", "480"));

            this.javaParamaters = properties.getProperty("javaparameters", "");

            this.maximiseMinecraft = Boolean.parseBoolean(properties.getProperty("maximiseminecraft", "false"));

            this.saveCustomMods = Boolean.parseBoolean(properties.getProperty("savecustommods", "true"));

            this.advancedBackup = Boolean.parseBoolean(properties.getProperty("advancedbackup", "false"));

            this.sortPacksAlphabetically = Boolean.parseBoolean(properties.getProperty("sortpacksalphabetically",
                    "false"));

            this.keepLauncherOpen = Boolean.parseBoolean(properties.getProperty("keeplauncheropen", "true"));

            this.enableConsole = Boolean.parseBoolean(properties.getProperty("enableconsole", "true"));

            this.enableTrayIcon = Boolean.parseBoolean(properties.getProperty("enabletrayicon", "true"));

            this.enableLeaderboards = Boolean.parseBoolean(properties.getProperty("enableleaderboards", "false"));

            this.enableLogs = Boolean.parseBoolean(properties.getProperty("enablelogs", "true"));

            this.enableServerChecker = Boolean.parseBoolean(properties.getProperty("enableserverchecker", "false"));

            this.enableOpenEyeReporting = Boolean.parseBoolean(properties.getProperty("enableopeneyereporting",
                    "true"));

            this.serverCheckerWait = Integer.parseInt(properties.getProperty("servercheckerwait", "5"));

            this.lastAccount = properties.getProperty("lastaccount", "");
            this.addedPacks = properties.getProperty("addedpacks", null);

            this.autoBackup = Boolean.parseBoolean(properties.getProperty("autobackup", "false"));
            this.notifyBackup = Boolean.parseBoolean(properties.getProperty("notifybackup", "true"));
            this.dropboxFolderLocation = properties.getProperty("dropboxlocation", "");

            FileUtils.delete(FileSystemData.PROPERTIES);
        } catch (IOException e) {
            LogManager.logStackTrace("Error migrating existing settings!", e);
        }
    }

    public void validate() {
        loadLanguage();
        loadServer();
        checkForgeLoggingLevel();
        checkMemory();
        checkWindowSize();
        checkProxy();
        checkServerCheckerWait();
        checkConcurrentConnections();
        checkDaysOfLogsToKeep();
        checkDateFormat();
        checkLastAccount();
        addAddedPacks();
    }

    private void loadServer() {
        if (this.server == null || !ServerManager.isServerByName(this.server)) {
            if (this.server != null) {
                LogManager.warn("Server " + this.server + " is invalid");
            }

            this.server = "Auto";
        }

        if (ServerManager.isServerByName(this.server)) {
            this.selectedServer = ServerManager.getServerByName(this.server);
        }
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

    private void checkMemory() {
        if (this.maximumMemory > Utils.getMaximumRam()) {
            LogManager.warn("Tried to allocate " + this.maximumMemory + "MB for maximum memory but only " + Utils
                    .getMaximumRam() + "MB is available to use!");

            // User tried to allocate too much ram, set it back to half, capped at 4GB
            this.maximumMemory = Utils.getMaximumSafeRam();
        }

        if (this.initialMemory > Utils.getMaximumRam()) {
            LogManager.warn("Tried to allocate " + this.initialMemory + "MB for initial memory but only " + Utils
                    .getMaximumRam() + "MB is available to use!");

            this.initialMemory = 256; // User tried to allocate too much initial memory, set it back to 256MB
        }

        if (this.initialMemory > this.maximumMemory) {
            LogManager.warn("Tried to allocate " + this.initialMemory + "MB for initial memory but maximum ram is " +
                    this.maximumMemory + "MB which is less!");

            this.initialMemory = 256; // User tried to allocate more initial memory than maximum, set it back to 256MB
        }
    }

    private void checkWindowSize() {
        if (this.windowWidth > Utils.getMaximumWindowWidth()) {
            LogManager.warn("Tried to set window width to " + this.windowWidth + " pixels but the maximum is " +
                    Utils.getMaximumWindowWidth() + " pixels!");
            this.windowWidth = Utils.getMaximumWindowWidth(); // User tried to make screen size wider than they have
        }

        if (this.windowHeight > Utils.getMaximumWindowHeight()) {
            LogManager.warn("Tried to set window height to " + this.windowHeight + " pixels but the maximum is " +
                    Utils.getMaximumWindowHeight() + " pixels!");
            this.windowHeight = Utils.getMaximumWindowHeight(); // User tried to make screen size taller than they have
        }
    }

    private void checkProxy() {
        if (this.enableProxy) {
            if (this.proxyPort <= 0 || this.proxyPort > 65535) {
                // Proxy port is invalid so disable proxy
                LogManager.warn("Tried to set proxy port to " + this.proxyPort + " which is not a valid port! " +
                        "Proxy support disabled!");

                this.enableProxy = false;
                this.proxy = null;
            }

            this.proxy = new Proxy(this.proxyType, new InetSocketAddress(this.proxyHost, this.proxyPort));
        }
    }

    private void checkServerCheckerWait() {
        if (this.serverCheckerWait < 1 || this.serverCheckerWait > 30) {
            // Server checker wait should be between 1 and 30
            LogManager.warn("Tried to set server checker wait to " + this.serverCheckerWait + " which is not " +
                    "valid! Must be between 1 and 30. Setting back to default of 5!");

            this.serverCheckerWait = 5;
        }
    }

    private void checkConcurrentConnections() {
        if (this.concurrentConnections < 1) {
            // Concurrent connections should be more than or equal to 1
            LogManager.warn("Tried to set the number of concurrent connections to " + this.concurrentConnections + " " +
                    "which is not valid! Must be 1 or more. Setting back to default of 8!");

            this.concurrentConnections = 8;
        }
    }

    private void checkDaysOfLogsToKeep() {
        if (this.daysOfLogsToKeep < 1 || this.daysOfLogsToKeep > 30) {
            // Days of logs to keep should be 1 or more but less than 30
            LogManager.warn("Tried to set the number of days worth of logs to keep to " + this.daysOfLogsToKeep +
                    " which is not valid! Must be between 1 and 30 inclusive. Setting back to default of 7!");

            this.daysOfLogsToKeep = 7;
        }
    }

    private void checkDateFormat() {
        if (!this.dateFormat.equalsIgnoreCase("dd/M/yyy") && !this.dateFormat.equalsIgnoreCase("M/dd/yyy") &&
                !this.dateFormat.equalsIgnoreCase("yyy/M/dd")) {
            LogManager.warn("Tried to set the date format to " + this.dateFormat + " which is not valid! Setting " +
                    "back to default of dd/M/yyy!");

            this.dateFormat = "dd/M/yyy";
        }
    }

    private void checkLastAccount() {
        if (!this.lastAccount.isEmpty()) {
            // Set the account. If it's null, that's okay, it uses that to signify nobody logged in
            AccountManager.setActiveAccount(AccountManager.getAccountByName(this.lastAccount));
        }
    }

    private void addAddedPacks() {
        List<String> semiPublicPackCodes = new LinkedList<>();

        if (!this.addedPacks.isEmpty()) {
            if (this.addedPacks.contains(",")) {
                Collections.addAll(semiPublicPackCodes, this.addedPacks.split(","));
            } else {
                semiPublicPackCodes.add(this.addedPacks);
            }
        }

        PackManager.setSemiPublicPackCodes(semiPublicPackCodes);
    }
}
