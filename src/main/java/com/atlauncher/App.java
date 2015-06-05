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

import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.OldSettings;
import com.atlauncher.data.Pack;
import com.atlauncher.evnt.EventModule;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.injector.Injector;
import com.atlauncher.injector.InjectorFactory;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.BenchmarkManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LanguageManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import io.github.asyncronous.toast.Toaster;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
     * This controls if GZIP is used when downloading files through the launcher. It's used as a debugging tool and is
     * enabled with the command line argument shown below.
     * <p/>
     * --usegzip=false
     */
    public static boolean useGzipForDownloads = true;

    /**
     * This allows skipping the system tray intergation so that the launcher doesn't even try to show the icon and menu
     * etc, in the users system tray. It can be skipped with the below command line argument.
     * <p/>
     * --skip-tray-integration
     */
    public static boolean skipTrayIntegration = false;

    /**
     * This forces the launcher to start in offline mode. It can be enabled with the below command line argument.
     * <p/>
     * --force-offline-mode
     */
    public static boolean forceOfflineMode = false;

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
     * For more information on themeing, please see https://atl.pw/theme
     */
    public static Theme THEME = Theme.DEFAULT_THEME;

    static {
        /**
         * Sets up where all uncaught exceptions go to.
         */
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionStrainer());
    }

    /**
     * Where the magic happens.
     *
     * @param args all the arguments passed in from the command line
     */
    public static void main(String[] args) {
        BenchmarkManager.start();
        // Set English as the default locale. CodeChickenLib(?) has some issues when not using this on some systems.
        Locale.setDefault(Locale.ENGLISH);

        // Prefer to use IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        parseCommandLineArguments(args);

        Loader loader = new Loader();

        loader.checkIfUsingOldOSXApp();

        // Load the theme and style everything.
        loader.loadTheme();

        // Load the console, making sure it's after the theme and L&F has been loaded otherwise bad results may occur.
        loader.loadConsole();

        loader.loadSystemTray();

        loader.logInformation();

        // Now for some Mac specific stuff, mainly just setting the name of the application and icon.
        loader.setupOSXSpecificStuff();

        // Loads everything that needs to be loaded
        settings = new OldSettings();
        settings.loadEverything();

        loader.checkIfSetupIsComplete();

        loader.autoLaunchInstance();

        // See write launchers location to disk and check if the launch tool has been used and act upon it
        loader.integrate();

        // Finished loading, so remove splash screen and other work
        loader.finish();

        App.frame = new LauncherFrame(); // Open the Launcher

        BenchmarkManager.stop();
    }

    private static void parseCommandLineArguments(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("launch").withRequiredArg().ofType(String.class);
        parser.accepts("updated").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug").withRequiredArg().ofType(Boolean.class);
        parser.accepts("debug-level").withRequiredArg().ofType(Integer.class);
        parser.accepts("use-gzip").withRequiredArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration").withRequiredArg().ofType(Boolean.class);
        parser.accepts("force-offline-mode").withRequiredArg().ofType(Boolean.class);

        OptionSet options = parser.parse(args);
        autoLaunch = options.has("launch") ? (String) options.valueOf("launch") : null;
        wasUpdated = options.has("updated") ? (Boolean) options.valueOf("updated") : false;

        if (options.has("debug")) {
            LogManager.showDebug = (Boolean) options.valueOf("debug");
            LogManager.debugLevel = 1;
            LogManager.debug("Debug logging is enabled! Please note that this will remove any censoring of " + "user " +
                    "data!");
        }

        if (options.has("debug-level")) {
            LogManager.debugLevel = (Integer) options.valueOf("debug-level");
            LogManager.debug("Debug level has been set to " + options.valueOf("debug-level") + "!");
        }

        useGzipForDownloads = options.has("use-gzip") ? (Boolean) options.valueOf("use-gzip") : true;
        if (!useGzipForDownloads) {
            LogManager.debug("GZip has been turned off for downloads! Don't ask for support with this " +
                    "disabled!", true);
        }

        skipTrayIntegration = options.has("skip-tray-integration") ? (Boolean) options.valueOf
                ("skip-tray-integration") : false;
        if (skipTrayIntegration) {
            LogManager.debug("Skipping tray integration!", true);
        }

        forceOfflineMode = options.has("force-offline-mode") ? (Boolean) options.valueOf("force-offline-mode") : false;
        if (forceOfflineMode) {
            LogManager.debug("Forcing offline mode!", true);
        }
    }
}
