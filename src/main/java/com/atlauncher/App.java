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
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.managers.BenchmarkManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.managers.SettingsManager;
import com.atlauncher.utils.HTMLUtils;
import com.atlauncher.utils.Utils;
import io.github.asyncronous.toast.Toaster;

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

    /**
     * The instance of toaster to show popups in the bottom right.
     */
    public static final Toaster TOASTER = Toaster.instance();

    /**
     * The tray menu shown in the notification area or whatever it's called in non Windows OS.
     */
    public static TrayMenu TRAY_MENU = new TrayMenu();

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
     * This is the Settings instance which holds all the users settings and alot of methods relating to getting things
     * done.
     *
     * @TODO This should probably be switched to be less large and have less responsibility.
     */
    public static OldSettings settings;

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

        if (args != null) {
            for (String arg : args) {
                String[] parts = arg.split("=");
                if (parts[0].equalsIgnoreCase("--launch")) {
                    autoLaunch = parts[1];
                } else if (parts[0].equalsIgnoreCase("--updated")) {
                    wasUpdated = true;
                } else if (parts[0].equalsIgnoreCase("--debug")) {
                    LogManager.showDebug = true;
                    LogManager.debugLevel = 1;
                    LogManager.debug("Debug logging is enabled! Please note that this will remove any censoring of "
                            + "user data!");
                } else if (parts[0].equalsIgnoreCase("--debug-level") && parts.length == 2) {
                    int debugLevel;

                    try {
                        debugLevel = Integer.parseInt(parts[1]);
                    } catch (NumberFormatException e) {
                        LogManager.error("Error converting given debug level string to an integer. The specified " +
                                "debug level given was '" + parts[1] + "'");
                        continue;
                    }

                    if (debugLevel < 1 || debugLevel > 3) {
                        LogManager.error("Invalid debug level of '" + parts[1] + "' given!");
                        continue;
                    }

                    LogManager.debugLevel = debugLevel;
                    LogManager.debug("Debug level has been set to " + debugLevel + "!");
                } else if (parts[0].equalsIgnoreCase("--usegzip") && parts[1].equalsIgnoreCase("false")) {
                    useGzipForDownloads = false;
                    LogManager.debug("GZip has been turned off for downloads! Don't ask for support with this " +
                            "disabled!", true);
                } else if (parts[0].equalsIgnoreCase("--skip-tray-integration")) {
                    skipTrayIntegration = true;
                    LogManager.debug("Skipping tray integration!", true);
                } else if (parts[0].equalsIgnoreCase("--force-offline-mode")) {
                    forceOfflineMode = true;
                    LogManager.debug("Forcing offline mode!", true);
                }
            }
        }

        File config = FileSystem.CONFIGS.toFile();
        if (!config.exists()) {
            int files = config.getParentFile().list().length;
            if (files > 1) {
                String[] options = {"Yes It's Fine", "Whoops. I'll Change That Now"};
                int ret = JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph("I've detected that you may " +
                        "not have installed this in the right location.<br/><br/>The exe or JAR file should " +
                        "be placed in it's own folder with nothing else in it.<br/><br/>Are you 100% sure " +
                        "that's what you've done?"), "Warning", JOptionPane.DEFAULT_OPTION, JOptionPane
                        .ERROR_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    System.exit(0);
                }
            }
        }

        // Load in the settings
        SettingsManager.loadSettings();

        // Setup the Settings and wait for it to finish.
        settings = new OldSettings();

        if (settings.isUsingMacApp() && !settings.isUsingNewMacApp()) {
            String[] options = {"Download"};

            JOptionPane.showOptionDialog(null, HTMLUtils.centerParagraph("You're using an old version of the" +
                    " ATLauncher Mac OSX app.<br/><br/>Please download the new Mac OSX app from below to " +
                    "keep playing!<br/><br/>Your instances and data will be transferred once the new app " +
                    "is launcher.<br/><br/>Sorry for any inconvenience caused!"), "Error", JOptionPane
                    .DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);

            Utils.openBrowser("https://atl.pw/oldosxapp");
            System.exit(0);
        }

        final SplashScreen ss = new SplashScreen();

        // Load and show the splash screen while we load other things.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ss.setVisible(true);
            }
        });

        // Load the theme and style everything.
        loadTheme();

        // Load the console, making sure it's after the theme and L&F has been loaded otherwise bad results may occur.
        settings.loadConsole();

        if (settings.enableTrayIcon() && !skipTrayIntegration) {
            try {
                // Try to enable the tray icon.
                trySystemTrayIntegration();
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }

        LogManager.info(Constants.LAUNCHER_NAME + " Version: " + Constants.VERSION);
        LogManager.info("Operating System: " + System.getProperty("os.name"));
        LogManager.info("RAM Available: " + Utils.getMaximumRam() + "MB");

        if (settings.isUsingCustomJavaPath()) {
            LogManager.warn("Custom Java Path Set!");

            settings.checkForValidJavaPath(false);
        }

        LogManager.info("Java Version: " + Utils.getActualJavaVersion());
        LogManager.info("Java Path: " + settings.getJavaPath());
        LogManager.info("64 Bit Java: " + Utils.is64Bit());
        LogManager.info("Launcher Directory: " + FileSystem.BASE_DIR.toString());
        LogManager.info("Using Theme: " + THEME);

        // Now for some Mac specific stuff, mainly just setting the name of the application and icon.
        if (Utils.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", Constants.LAUNCHER_NAME + " " +
                    Constants.VERSION);
            try {
                Class util = Class.forName("com.apple.eawt.Application");
                Method getApplication = util.getMethod("getApplication", new Class[0]);
                Object application = getApplication.invoke(util);
                Class params[] = new Class[1];
                params[0] = Image.class;
                Method setDockIconImage = util.getMethod("setDockIconImage", params);
                setDockIconImage.invoke(application, Utils.getImage("/assets/image/Icon.png"));
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        }

        if (settings.enableConsole()) {
            // Show the console if enabled.
            settings.getConsole().setVisible(true);
        }

        LogManager.info("Showing splash screen and loading everything");
        settings.loadEverything(); // Loads everything that needs to be loaded
        LogManager.info("Launcher finished loading everything");

        if (settings.isFirstTimeRun()) {
            LogManager.warn("Launcher not setup. Loading Setup Dialog");
            new SetupDialog();
        }

        boolean open = true;

        if (autoLaunch != null && InstanceManager.isInstanceBySafeName(autoLaunch)) {
            Instance instance = InstanceManager.getInstanceBySafeName(autoLaunch);
            LogManager.info("Opening Instance " + instance.getName());
            if (instance.launch()) {
                open = false;
            } else {
                LogManager.error("Error Opening Instance  " + instance.getName());
            }
        }

        TRAY_MENU.localize();
        integrate();
        ss.close();

        if (packCodeToAdd != null) {
            if (PackManager.addSemiPublicPack(packCodeToAdd)) {
                Pack packAdded = PackManager.getSemiPublicPackByCode(packCodeToAdd);
                if (packAdded != null) {
                    LogManager.info("The pack " + packAdded.getName() + " was automatically added to the launcher!");
                } else {
                    LogManager.error("Error automatically adding semi public pack with code of " + packCodeToAdd + "!");
                }
            } else {
                LogManager.error("Error automatically adding semi public pack with code of " + packCodeToAdd + "!");
            }
        }

        BenchmarkManager.stop();

        new LauncherFrame(open); // Open the Launcher
    }

    /**
     * Loads the theme and applies the theme's settings to the look and feel.
     */
    public static void loadTheme() {
        Path themeFile = settings.getThemeFile();
        if (themeFile != null) {
            try {
                InputStream stream = null;

                ZipFile zipFile = new ZipFile(themeFile.toFile());
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    if (entry.getName().equals("theme.json")) {
                        stream = zipFile.getInputStream(entry);
                        break;
                    }
                }

                if (stream != null) {
                    THEME = Gsons.THEMES.fromJson(new InputStreamReader(stream), Theme.class);
                    stream.close();
                }

                zipFile.close();
            } catch (Exception ex) {
                THEME = Theme.DEFAULT_THEME;
            }
        }

        try {
            setLAF();
            modifyLAF();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the look and feel to be that of nimbus which is the base.
     *
     * @throws Exception
     */
    private static void setLAF() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase("nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
            }
        }
    }

    /**
     * This modifies the look and feel based upon the theme loaded.
     *
     * @throws Exception
     */
    private static void modifyLAF() throws Exception {
        THEME.apply();
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        ToolTipManager.sharedInstance().setInitialDelay(50);
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);

        if (Utils.isMac()) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
        }
    }

    /**
     * This tries to create the system tray menu.
     *
     * @throws Exception
     */
    private static void trySystemTrayIntegration() throws Exception {
        if (SystemTray.isSupported()) {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = new TrayIcon(Utils.getImage("/assets/image/Icon.png"));

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getButton() == MouseEvent.BUTTON3) {
                        TRAY_MENU.setInvoker(TRAY_MENU);
                        TRAY_MENU.setLocation(e.getX(), e.getY());
                        TRAY_MENU.setVisible(true);
                    }
                }
            });
            trayIcon.setToolTip(Constants.LAUNCHER_NAME);
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);
        }
    }

    /**
     * This creates some integration files so the launcher can work with other applications by storing some properties
     * about itself and it's location in a set location.
     */
    public static void integrate() {
        try {
            if (!Utils.getOSStorageDir().exists()) {
                Utils.getOSStorageDir().mkdirs();
            }

            File f = new File(Utils.getOSStorageDir(), "atlauncher.conf");

            if (!f.exists()) {
                f.createNewFile();
            }

            Properties props = new Properties();
            props.load(new FileInputStream(f));

            props.setProperty("java_version", Utils.getJavaVersion());
            props.setProperty("location", FileSystem.BASE_DIR.toString());
            props.setProperty("executable", new File(Update.class.getProtectionDomain().getCodeSource().getLocation()
                    .getPath()).getAbsolutePath());

            packCodeToAdd = props.getProperty("pack_code_to_add", null);
            props.remove("pack_code_to_add");

            packToInstall = props.getProperty("pack_to_install", null);
            props.remove("pack_to_install");

            packShareCodeToInstall = props.getProperty("pack_share_code_to_install", null);
            props.remove("pack_share_code_to_install");

            props.store(new FileOutputStream(f), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
