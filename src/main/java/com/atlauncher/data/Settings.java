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
package com.atlauncher.data;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.constants.Constants;
import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Timestamper;
import com.atlauncher.utils.Utils;

public class Settings {
    // Launcher things
    public String lastAccount;
    public boolean usingCustomJavaPath = false;
    public boolean hideOldJavaWarning = false;
    public boolean firstTimeRun = true;
    public boolean hideJava9Warning = false;
    public List<String> addedPacks = new ArrayList<>();
    public boolean ignoreOneDriveWarning = false;
    public boolean ignoreProgramFilesWarning = false;

    // Window settings
    public boolean rememberWindowSizePosition = false;
    public Dimension consoleSize = new Dimension(650, 400);
    public Point consolePosition = new Point(0, 0);
    public Dimension launcherSize = new Dimension(1200, 700);
    public Point launcherPosition = null;

    // General
    public String language = "English";
    public String theme = Constants.DEFAULT_THEME_CLASS;
    public String dateFormat = "dd/MM/yyyy";
    public int selectedTabOnStartup = 0;
    public String defaultModPlatform = "CurseForge";
    public AddModRestriction addModRestriction = AddModRestriction.STRICT;
    public boolean sortPacksAlphabetically = false;
    public boolean showPackNameAndVersion = true;
    public boolean keepLauncherOpen = true;
    public boolean enableConsole = true;
    public boolean enableTrayMenu = true;
    public boolean enableDiscordIntegration = true;
    public boolean enableFeralGamemode = OS.isLinux() && Utils.executableInPath("gamemoderun");
    private boolean disableAddModRestrictions = false;
    public boolean disableCustomFonts = false;
    public boolean useNativeFilePicker = OS.isMac();

    // Java/Minecraft
    public int initialMemory = 512;
    public int maximumMemory = 4096;
    public int metaspace = (OS.is64Bit() ? 256 : 128);
    public int windowWidth = 854;
    public int windowHeight = 480;
    public String javaPath;
    public String javaParameters = Constants.DEFAULT_JAVA_PARAMETERS;
    public boolean maximiseMinecraft = false;
    public boolean ignoreJavaOnInstanceLaunch = false;

    // Network
    public int concurrentConnections = 8;
    public int connectionTimeout = 30;
    public boolean enableProxy = false;
    public String proxyHost = "";
    public int proxyPort = 8080;
    public String proxyType = "HTTP";
    public transient Proxy proxy;

    // Logging
    public String forgeLoggingLevel = "INFO";
    public boolean enableLogs = true;
    public boolean enableAnalytics = true;
    public String analyticsClientId = UUID.randomUUID().toString();
    public boolean enableOpenEyeReporting = true;

    // Tools
    public boolean enableServerChecker = false;
    public int serverCheckerWait = 5;

    // Backups
    public boolean enableModsBackups = false;
    public boolean enableAutomaticBackupAfterLaunch = false;

    public void convert(Properties properties) {
        String importedDateFormat = properties.getProperty("dateformat");
        if (importedDateFormat != null) {
            if (importedDateFormat.equalsIgnoreCase("dd/M/yyy")) {
                importedDateFormat = "dd/MM/yyyy";
            } else if (importedDateFormat.equalsIgnoreCase("M/dd/yyy")) {
                importedDateFormat = "MM/dd/yyyy";
            } else if (importedDateFormat.equalsIgnoreCase("yyy/M/dd")) {
                importedDateFormat = "yyyy/MM/dd";
            }

            this.dateFormat = importedDateFormat;
        }

        String importedDisableAddModRestrictions = properties.getProperty("disableaddmodrestrictions");
        if (importedDisableAddModRestrictions != null) {
            disableAddModRestrictions = Boolean.parseBoolean(importedDisableAddModRestrictions);
        }

        String importedEnableConsole = properties.getProperty("enableconsole");
        if (importedEnableConsole != null) {
            enableConsole = Boolean.parseBoolean(importedEnableConsole);
        }

        String importedEnableTrayMenu = properties.getProperty("enabletrayicon");
        if (importedEnableTrayMenu != null) {
            enableTrayMenu = Boolean.parseBoolean(importedEnableTrayMenu);
        }

        String importedEnableDiscordIntegration = properties.getProperty("enablediscordintegration");
        if (importedEnableDiscordIntegration != null) {
            enableDiscordIntegration = Boolean.parseBoolean(importedEnableDiscordIntegration);
        }

        String importedEnableFeralGamemode = properties.getProperty("enableferalgamemode");
        if (importedEnableFeralGamemode != null) {
            enableFeralGamemode = Boolean.parseBoolean(importedEnableFeralGamemode);
        }

        String importedEnableProxy = properties.getProperty("enableproxy");
        if (importedEnableProxy != null) {
            enableProxy = Boolean.parseBoolean(importedEnableProxy);

            if (enableProxy) {
                proxyHost = properties.getProperty("proxyhost", "");

                proxyPort = Integer.parseInt(properties.getProperty("proxyport", "8080"));

                proxyType = properties.getProperty("proxytype", "HTTP");
            }
        }

        String importedConcurrentConnections = properties.getProperty("concurrentconnections");
        if (importedConcurrentConnections != null) {
            concurrentConnections = Integer.parseInt(importedConcurrentConnections);
        }

        String importedFirstTimeRun = properties.getProperty("firsttimerun");
        if (importedFirstTimeRun != null) {
            firstTimeRun = Boolean.parseBoolean(importedFirstTimeRun);
        }

        String importedHideOldJavaWarning = properties.getProperty("hideoldjavawarning");
        if (importedHideOldJavaWarning != null) {
            hideOldJavaWarning = Boolean.parseBoolean(importedHideOldJavaWarning);
        }

        String importedHideJava9Warning = properties.getProperty("hideJava9Warning");
        if (importedHideJava9Warning != null) {
            hideJava9Warning = Boolean.parseBoolean(importedHideJava9Warning);
        }

        String importedForgeLoggingLevel = properties.getProperty("forgelogginglevel");
        if (importedForgeLoggingLevel != null) {
            forgeLoggingLevel = importedForgeLoggingLevel;
        }

        String importedInitialMemory = properties.getProperty("initialmemory");
        if (importedInitialMemory != null) {
            initialMemory = Integer.parseInt(importedInitialMemory);
        }

        String importedMaximumMemory = properties.getProperty("ram");
        if (importedMaximumMemory != null) {
            maximumMemory = Integer.parseInt(importedMaximumMemory);
        }

        String importedMetaspace = properties.getProperty("permGen");
        if (importedMetaspace != null) {
            metaspace = Integer.parseInt(importedMetaspace);
        }

        String importedWindowWidth = properties.getProperty("windowwidth");
        if (importedWindowWidth != null) {
            windowWidth = Integer.parseInt(importedWindowWidth);
        }

        String importedWindowHeight = properties.getProperty("windowheight");
        if (importedWindowHeight != null) {
            windowHeight = Integer.parseInt(importedWindowHeight);
        }

        String importedUsingCustomJavaPath = properties.getProperty("usingcustomjavapath");
        if (importedUsingCustomJavaPath != null) {
            usingCustomJavaPath = Boolean.parseBoolean(importedUsingCustomJavaPath);
        }

        String importedJavaPath = properties.getProperty("javapath");
        if (importedJavaPath != null) {
            javaPath = importedJavaPath;
        }

        String importedJavaParameters = properties.getProperty("javaparameters");
        if (importedJavaParameters != null) {
            javaParameters = importedJavaParameters;
        }

        String importedMaximiseMinecraft = properties.getProperty("maximiseminecraft");
        if (importedMaximiseMinecraft != null) {
            maximiseMinecraft = Boolean.parseBoolean(importedMaximiseMinecraft);
        }

        String importedIgnoreJavaOnInstanceLaunch = properties.getProperty("ignorejavaoninstancelaunch");
        if (importedIgnoreJavaOnInstanceLaunch != null) {
            ignoreJavaOnInstanceLaunch = Boolean.parseBoolean(importedIgnoreJavaOnInstanceLaunch);
        }

        String importedSortPacksAlphabetically = properties.getProperty("sortpacksalphabetically");
        if (importedSortPacksAlphabetically != null) {
            sortPacksAlphabetically = Boolean.parseBoolean(importedSortPacksAlphabetically);
        }

        String importedKeepLauncherOpen = properties.getProperty("keeplauncheropen");
        if (importedKeepLauncherOpen != null) {
            keepLauncherOpen = Boolean.parseBoolean(importedKeepLauncherOpen);
        }

        String importedKeepLogs = properties.getProperty("enablelogs");
        if (importedKeepLogs != null) {
            enableLogs = Boolean.parseBoolean(importedKeepLogs);
        }

        String importedEnableAnalytics = properties.getProperty("enableanalytics");
        if (importedEnableAnalytics != null) {
            enableAnalytics = Boolean.parseBoolean(importedEnableAnalytics);
        }

        String importedEnableServerChecker = properties.getProperty("enableserverchecker");
        if (importedEnableServerChecker != null) {
            enableServerChecker = Boolean.parseBoolean(importedEnableServerChecker);
        }

        String importedEnableOpenEyeReporting = properties.getProperty("enableopeneyereporting");
        if (importedEnableOpenEyeReporting != null) {
            enableOpenEyeReporting = Boolean.parseBoolean(importedEnableOpenEyeReporting);
        }

        String importedEnableModsBackups = properties.getProperty("enablemodsbackups");
        if (importedEnableModsBackups != null) {
            enableModsBackups = Boolean.parseBoolean(importedEnableModsBackups);
        }

        String importedServerCheckerWait = properties.getProperty("servercheckerwait");
        if (importedServerCheckerWait != null) {
            serverCheckerWait = Integer.parseInt(importedServerCheckerWait);
        }

        String importedLastAccount = properties.getProperty("lastaccount");
        if (importedLastAccount != null) {
            lastAccount = importedLastAccount;
        }

        String importedAddedPacks = properties.getProperty("addedpacks");
        if (importedAddedPacks != null) {
            addedPacks = Arrays.asList(importedAddedPacks.split(","));
        }

        String importedAnalyticsClientId = properties.getProperty("analyticsclientid");
        if (importedAnalyticsClientId != null) {
            analyticsClientId = importedAnalyticsClientId;
        }

        // validate everything
        validate();

        // save
        save();
    }

    public void validate() {
        validateWindowSettings();

        validateDisableAddModRestrictions();
        validateDefaultModPlatform();

        validateJavaPath();

        validateMemory();
        validateWindowSize();

        validateProxy();

        validateServerCheckerWait();

        validateConcurrentConnections();

        validateDateFormat();
    }

    private void validateWindowSettings() {
        if (consoleSize.width < 650) {
            consoleSize.setSize(650, consoleSize.height);
        }

        if (consoleSize.height < 400) {
            consoleSize.setSize(consoleSize.width, 400);
        }

        if (launcherSize.width < 1200) {
            launcherSize.setSize(1200, launcherSize.height);
        }

        if (launcherSize.height < 700) {
            launcherSize.setSize(launcherSize.width, 700);
        }

        if (launcherPosition != null
                && !OS.getScreenVirtualBounds().contains(new Rectangle(launcherPosition, launcherSize))) {
            launcherPosition = null;
        }

        if (consolePosition != null
                && !OS.getScreenVirtualBounds().contains(new Rectangle(consolePosition, consoleSize))) {
            consolePosition = null;
        }
    }

    private void validateDisableAddModRestrictions() {
        if (disableAddModRestrictions && addModRestriction != AddModRestriction.NONE) {
            addModRestriction = AddModRestriction.NONE;
        }
    }

    private void validateDefaultModPlatform() {
        if (defaultModPlatform == null
                || !(defaultModPlatform.equals("CurseForge") || defaultModPlatform.equals("Modrinth"))) {
            defaultModPlatform = "CurseForge";
        }
    }

    public void validateJavaPath() {
        if (!usingCustomJavaPath || javaPath == null) {
            javaPath = OS.getDefaultJavaPath();
        }

        // TODO: figure out why this nonsense is here
        if (OS.isUsingMacApp()) {
            File oracleJava = new File("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
            if (oracleJava.exists() && oracleJava.canExecute()) {
                javaPath = "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home";
            }
        }

        // now validate the java path actually exists
        if (!new File(javaPath, "bin" + File.separator + "java" + (OS.isWindows() ? ".exe" : "")).exists()) {
            LogManager.warn("Custom Java Path Is Incorrect! Defaulting to valid value!");
            javaPath = OS.getDefaultJavaPath();
        }
    }

    private void validateMemory() {
        int systemMemory = OS.getMaximumRam();

        if (systemMemory != 0 && initialMemory > systemMemory) {
            LogManager.warn("Tried to allocate " + initialMemory + "MB for initial memory but only " + systemMemory
                    + "MB is available to use!");
            initialMemory = 512;
        } else if (initialMemory > maximumMemory) {
            LogManager.warn("Tried to allocate " + initialMemory + "MB for initial memory which is more than "
                    + maximumMemory + "MB set for the maximum memory!");
            initialMemory = 512;
        }

        if (systemMemory != 0 && maximumMemory > systemMemory) {
            LogManager.warn("Tried to allocate " + maximumMemory + "MB of maximum memory but only " + systemMemory
                    + "MB is available to use!");

            if (OS.is64Bit()) {
                maximumMemory = Math.min((systemMemory / 1000) * 512, 8192);
            } else {
                maximumMemory = 1024;
            }
        }
    }

    private void validateWindowSize() {
        int systemWindowWidth = OS.getMaximumWindowWidth();
        if (windowWidth > systemWindowWidth) {
            LogManager.warn("Tried to set window width to " + windowWidth + "px but the maximum is " + systemWindowWidth
                    + "px!");

            windowWidth = systemWindowWidth;
        }

        int systemWindowHeight = OS.getMaximumWindowHeight();
        if (windowHeight > systemWindowHeight) {
            LogManager.warn("Tried to set window height to " + windowHeight + "px but the maximum is "
                    + systemWindowHeight + "px!");

            windowHeight = systemWindowHeight;
        }
    }

    private void validateProxy() {
        if (enableProxy) {
            if (proxyPort <= 0 || proxyPort > 65535) {
                LogManager.warn("Tried to set proxy port to " + proxyPort
                        + " which is not a valid port! Proxy support disabled!");
                enableProxy = false;
                proxyPort = 8080;
            }

            if (!proxyType.equals("SOCKS") && !proxyType.equals("HTTP") && !proxyType.equals("DIRECT")) {
                LogManager.warn(
                        "Tried to set proxy type to " + proxyType + " which is not valid! Proxy support disabled!");
                enableProxy = false;
                proxyType = "HTTP";
            }
        }

        if (enableProxy) {
            Type type = Type.HTTP;

            switch (this.proxyType) {
                case "HTTP":
                    type = Type.HTTP;
                    break;
                case "SOCKS":
                    type = Type.SOCKS;
                    break;
                case "DIRECT":
                    type = Type.DIRECT;
                    break;
            }

            proxy = new Proxy(type, new InetSocketAddress(proxyHost, proxyPort));
        } else {
            proxy = Proxy.NO_PROXY;
        }
    }

    private void validateServerCheckerWait() {
        if (serverCheckerWait < 1 || serverCheckerWait > 30) {
            LogManager.warn("Tried to set server checker wait to " + serverCheckerWait + " which is not "
                    + "valid! Must be between 1 and 30. Setting back to default of 5!");
            serverCheckerWait = 5;
        }
    }

    private void validateConcurrentConnections() {
        if (concurrentConnections < 1) {
            LogManager.warn("Tried to set the number of concurrent connections to " + concurrentConnections
                    + " which is not valid! Must be 1 or more. Setting back to default of 8!");
            concurrentConnections = 8;
        }
    }

    private void validateDateFormat() {
        if (!Arrays.asList(Constants.DATE_FORMATS).contains(dateFormat)) {
            LogManager.warn("Tried to set the date format to " + dateFormat + " which is not valid! Setting "
                    + "back to default of dd/MM/yyyy!");
            dateFormat = "dd/MM/yyyy";
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(FileSystem.SETTINGS.toFile())) {
            Gsons.DEFAULT.toJson(this, writer);
        } catch (IOException e) {
            LogManager.logStackTrace("Error saving settings", e);
        }

        try {
            Timestamper.updateDateFormat(dateFormat);
        } catch (Exception e) {
            LogManager.logStackTrace("Error updating date format to " + dateFormat, e);
        }
    }
}
