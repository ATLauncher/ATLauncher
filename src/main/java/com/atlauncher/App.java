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

import com.atlauncher.data.OldSettings;
import com.atlauncher.evnt.EventModule;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.injector.Injector;
import com.atlauncher.injector.InjectorFactory;
import com.atlauncher.managers.BenchmarkManager;
import com.atlauncher.managers.LogManager;
import io.github.asyncronous.toast.Toaster;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main entry point for the application, Java runs the main method here when the application is launched.
 */
public class App {
    /**
     * The taskpool used to quickly add in tasks to do in the background.
     */
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);

    public static final Injector INJECTOR = InjectorFactory.createInjector(new EventModule());

    /**
     * The instance of toaster to show popups in the bottom right.
     */
    public static final Toaster TOASTER = Toaster.instance();

    /**
     * If the launcher was just updated and this is it's first time loading after the update. This is used to check for
     * when there are possible issues in which the user may have to download the update manually.
     */
    public static boolean wasUpdated = false;

    /**
     * This allows skipping the system tray intergation so that the launcher doesn't even try to show the icon and menu
     * etc, in the users system tray. It can be skipped with the below command line argument.
     * <p/>
     * --skip-tray-integration
     */
    public static boolean skipTrayIntegration = false;

    /**
     * This allows skipping the hash checking when downloading files. It can be skipped with the below command line
     * argument.
     * <p/>
     * --skip-hash-checking
     */
    public static boolean skipHashChecking = false;

    /**
     * This forces the launcher to start in offline mode. It can be enabled with the below command line argument.
     * <p/>
     * --force-offline-mode
     */
    public static boolean forceOfflineMode = false;

    /**
     * This forces the working directory for the launcher. It can be changed with the below command line argument.
     * <p/>
     * --working-dir=C:/Games/ATLauncher
     */
    public static Path workingDir = null;

    /**
     * This forces the launcher to not check for a launcher update. It can be enabled with the below command line
     * argument.
     * <p/>
     * --no-launcher-update
     */
    public static boolean noLauncherUpdate = false;

    /**
     * This forces the launcher to remove the Downloads/ and Config/Libraries/ folder on boot. It can be enabled with
     * the below command line argument.
     * <p/>
     * --clear-downloadable-files
     */
    public static boolean clearDownloadableFiles = false;

    /**
     * This removes writing the launchers location to AppData/Application Support. It can be enabled with the below
     * command line argument.
     * <p/>
     * --skip-integration
     */
    public static boolean skipIntegration = false;

    /**
     * This sets a pack code to be added to the launcher on startup.
     */
    public static String packCodeToAdd;

    /**
     * This sets a pack to install on startup (no share code so just prompt).
     */
    public static String packToInstall;

    /**
     * This sets a pack to install on startup (with share code).
     */
    public static String packShareCodeToInstall;

    public static String autoLaunch = null;

    /**
     * The tray menu shown in the notification area or whatever it's called in non Windows OS.
     */
    public static TrayMenu trayMenu;

    public static LauncherConsole console; // The Launcher's console

    public static LauncherFrame frame; // The Launcher's main window

    /**
     * This is the Settings instance which holds all the users settings and alot of methods relating to getting things
     * done.
     *
     * @TODO This should probably be switched to be less large and have less responsibility.
     */
    public static OldSettings settings = null;

    /**
     * This is the theme used by the launcher. By default it uses the default theme until the theme can be created and
     * loaded.
     * <p/>
     * For more information on theming, please see https://atl.pw/theme
     */
    public static Theme THEME = Theme.DEFAULT_THEME;

    static {
        // Set English as the default locale. CodeChickenLib(?) has some issues when not using this on some systems.
        Locale.setDefault(Locale.ENGLISH);

        // Prefer to use IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Sets up where all uncaught exceptions go to.
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionStrainer());
    }

    /**
     * Where the magic happens.
     *
     * @param args all the arguments passed in from the command line
     */
    public static void main(String[] args) {
        BenchmarkManager.start();

        // Parse all the command line arguments
        parseCommandLineArguments(args);

        // Start the loader
        Loader loader = new Loader();

        // Check if the user is using the old OSX app
        loader.checkIfUsingOldOSXApp();

        // Load the theme and style everything.
        loader.loadTheme();

        // Load the console, making sure it's after the theme and L&F has been loaded otherwise bad results may occur.
        loader.loadConsole();

        // Load the system tray if enabled
        loader.loadSystemTray();

        // Log some basic information about the launcher and system to the console
        loader.logInformation();

        // Now for some Mac specific stuff, mainly just setting the name of the application and icon.
        loader.setupOSXSpecificStuff();

        // Loads everything that needs to be loaded
        settings = new OldSettings();
        settings.loadEverything();

        // Checks to see if the user has completed the first run dialog
        loader.checkIfSetupIsComplete();

        // Checks if we're auto launching an instance or not
        loader.autoLaunchInstance();

        // See write launchers location to disk and check if the launch tool has been used and act upon it
        if (!skipIntegration) {
            loader.integrate();
        }

        // Finished loading, so remove splash screen and other work
        loader.finish();

        // Open the launcher
        App.frame = new LauncherFrame();

        BenchmarkManager.stop();
    }

    private static void parseCommandLineArguments(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("launch").withRequiredArg().ofType(String.class);
        parser.accepts("updated").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug").withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug-level").withRequiredArg().ofType(Integer.class);
        parser.accepts("skip-tray-integration").withOptionalArg().ofType(Boolean.class);
        parser.accepts("force-offline-mode").withOptionalArg().ofType(Boolean.class);
        parser.accepts("working-dir").withRequiredArg().ofType(String.class);
        parser.accepts("no-launcher-update").withOptionalArg().ofType(Boolean.class);
        parser.accepts("clear-downloadable-files").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-integration").withOptionalArg().ofType(Boolean.class);

        OptionSet options = parser.parse(args);
        autoLaunch = options.has("launch") ? (String) options.valueOf("launch") : null;
        wasUpdated = options.has("updated") ? (Boolean) options.valueOf("updated") : false;

        if (options.has("debug")) {
            LogManager.showDebug = true;
            LogManager.debugLevel = 1;
            LogManager.debug("Debug logging is enabled! Please note that this will remove any censoring of user data!");
        }

        if (options.has("debug-level")) {
            LogManager.debugLevel = (Integer) options.valueOf("debug-level");
            LogManager.debug("Debug level has been set to " + options.valueOf("debug-level") + "!");
        }

        skipTrayIntegration = options.has("skip-tray-integration");
        if (skipTrayIntegration) {
            LogManager.debug("Skipping tray integration!", true);
        }

        forceOfflineMode = options.has("force-offline-mode");
        if (forceOfflineMode) {
            LogManager.debug("Forcing offline mode!", true);
        }

        if (options.has("working-dir")) {
            Path workingDirTemp = Paths.get(String.valueOf(options.valueOf("working-dir")));
            if (Files.exists(workingDirTemp) && Files.isDirectory(workingDirTemp)) {
                LogManager.debug("Working directory set to " + workingDirTemp + "!", true);
                workingDir = workingDirTemp;
            } else {
                LogManager.error("Cannot set working directory to " + workingDirTemp + " as it doesn't exist!");
            }
        }

        noLauncherUpdate = options.has("no-launcher-update");
        if (noLauncherUpdate) {
            LogManager.debug("Not updating the launcher!", true);
        }

        clearDownloadableFiles = options.has("clear-downloadable-files");
        if (clearDownloadableFiles) {
            LogManager.debug("Clearing downloadable files!", true);
        }

        skipIntegration = options.has("skip-integration");
        if (skipIntegration) {
            LogManager.debug("Skipping integration!", true);
        }

        skipHashChecking = options.has("skip-hash-checking");
        if (skipHashChecking) {
            LogManager.debug("Skipping hash checking! Don't ask for support with this enabled!", true);
        }
    }
}
