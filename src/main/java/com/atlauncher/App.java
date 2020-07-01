/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.awt.Dimension;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.themes.ATLauncherLaf;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

import io.github.asyncronous.toast.Toaster;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;

/**
 * Main entry point for the application, Java runs the main method here when the
 * application is launched.
 */
public class App {
    /**
     * The taskpool used to quickly add in tasks to do in the background.
     */
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);

    /**
     * The instance of toaster to show popups in the bottom right.
     */
    public static Toaster TOASTER;

    /**
     * The tray menu shown in the notification area or whatever it's called in non
     * Windows OS.
     */
    public static TrayMenu TRAY_MENU = new TrayMenu();

    public static LauncherConsole console;

    /**
     * If the launcher was just updated and this is it's first time loading after
     * the update. This is used to check for when there are possible issues in which
     * the user may have to download the update manually.
     */
    public static boolean wasUpdated = false;

    public static boolean discordInitialized = false;

    /**
     * This allows skipping the system tray integration so that the launcher doesn't
     * even try to show the icon and menu etc, in the users system tray. It can be
     * skipped with the below command line argument.
     * <p/>
     * --skip-tray-integration
     */
    public static boolean skipTrayIntegration = false;

    /**
     * This allows skipping the in built error reporting. This is mainly useful for
     * development when you don't want to report errors to an external third party.
     * <p/>
     * --disable-error-reporting
     */
    public static boolean disableErrorReporting = false;

    /**
     * This removes writing the launchers location to AppData/Application Support.
     * It can be enabled with the below command line argument.
     * <p/>
     * --skip-integration
     */
    public static boolean skipIntegration = false;

    /**
     * This allows skipping the hash checking when downloading files. It can be
     * skipped with the below command line argument.
     * <p/>
     * --skip-hash-checking
     */
    public static boolean skipHashChecking = false;

    /**
     * This forces the working directory for the launcher. It can be changed with
     * the below command line argument.
     * <p/>
     * --working-dir=C:/Games/ATLauncher
     */
    public static Path workingDir = null;

    /**
     * This forces the launcher to not check for a launcher update. It can be
     * enabled with the below command line argument.
     * <p/>
     * --no-launcher-update
     */
    public static boolean noLauncherUpdate = false;

    /**
     * This sets a pack code to be added to the launcher on startup.
     */
    public static String packCodeToAdd = null;

    /**
     * This sets a pack to install on startup (no share code so just prompt).
     */
    public static String packToInstall = null;

    /**
     * This sets a pack to install on startup (with share code).
     */
    public static String packShareCodeToInstall = null;

    /**
     * This sets a pack to auto launch on startup
     */
    public static String autoLaunch = null;

    /**
     * This is the Settings instance which holds all the users settings and alot of
     * methods relating to getting things done.
     *
     * @TODO This should probably be switched to be less large and have less
     *       responsibility.
     */
    public static Settings settings;

    /**
     * This is the theme used by the launcher. By default it uses the default theme
     * until the theme can be created and loaded.
     * <p/>
     * For more information on themeing, please see https://atl.pw/theme
     */
    public static ATLauncherLaf THEME;

    static {
        // Prefer to use IPv4
        System.setProperty("java.net.preferIPv4Stack", "true");

        // Sets up where all uncaught exceptions go to.
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionStrainer());
    }

    public static TrayIcon trayIcon;

    /**
     * Where the magic happens.
     *
     * @param args all the arguments passed in from the command line
     */
    public static void main(String[] args) {
        // Parse all the command line arguments
        parseCommandLineArguments(args);

        // Initialize the error reporting
        ErrorReporting.init(disableErrorReporting);

        // check the launcher has been 'installed' correctly
        checkInstalledCorrectly();

        try {
            LogManager.info("Organising filesystem");
            FileSystem.organise();
        } catch (IOException e) {
            LogManager.logStackTrace("Error organising filesystem", e, false);
        }

        // Setup the Settings and wait for it to finish.
        settings = new Settings();

        // Load the theme and style everything.
        loadTheme(settings.getTheme());

        final SplashScreen ss = new SplashScreen();

        // Load and show the splash screen while we load other things.
        SwingUtilities.invokeLater(() -> ss.setVisible(true));

        console = new LauncherConsole();
        LogManager.start();

        if (settings.enableConsole()) {
            // Show the console if enabled.
            console.setVisible(true);
        }

        try {
            Language.init();
        } catch (IOException e1) {
            LogManager.logStackTrace("Error loading language", e1);
        }

        if (settings.enableConsole()) {
            // Show the console if enabled.
            SwingUtilities.invokeLater(() -> {
                console.setVisible(true);
            });
        }

        if (settings.enableTrayIcon() && !skipTrayIntegration) {
            try {
                // Try to enable the tray icon.
                App.trySystemTrayIntegration();
            } catch (Exception e) {
                LogManager.logStackTrace(e);
            }
        }

        LogManager.info(Constants.LAUNCHER_NAME + " Version: " + Constants.VERSION);

        LogManager.info("Operating System: " + System.getProperty("os.name"));

        settings.loadJavaPathProperties();

        if (settings.isUsingCustomJavaPath()) {
            LogManager.warn("Custom Java Path Set!");

            settings.checkForValidJavaPath(false);
        } else if (OS.isUsingMacApp()) {
            // If the user is using the Mac Application, then we forcibly set the java path
            // if they have none set.

            File oracleJava = new File("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
            if (oracleJava.exists() && oracleJava.canExecute()) {
                settings.setJavaPath("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home");
                LogManager.warn("Launcher Forced Custom Java Path Set!");
            }
        }

        LogManager.info("Java Version: " + Java.getActualJavaVersion());

        SwingUtilities.invokeLater(() -> Java.getInstalledJavas().stream()
                .forEach(version -> LogManager.debug(Gsons.DEFAULT.toJson(version))));

        LogManager.info("Java Path: " + settings.getJavaPath());

        LogManager.info("64 Bit Java: " + OS.is64Bit());

        int maxRam = OS.getMaximumRam();
        LogManager.info("RAM Available: " + (maxRam == 0 ? "Unknown" : maxRam + "MB"));

        LogManager.info("Launcher Directory: " + FileSystem.BASE_DIR);

        // Now for some Mac specific stuff, mainly just setting the name of the
        // application and icon.
        if (OS.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                    Constants.LAUNCHER_NAME + " " + Constants.VERSION);
            try {
                Class<?> util = Class.forName("com.apple.eawt.Application");
                Method getApplication = util.getMethod("getApplication");
                Object application = getApplication.invoke(util);
                Method setDockIconImage = util.getMethod("setDockIconImage", Image.class);
                setDockIconImage.invoke(application, Utils.getImage("/assets/image/Icon.png"));
            } catch (Exception ex) {
                LogManager.logStackTrace("Failed to set dock icon", ex);
            }
        }

        // Check to make sure the user can load the launcher
        settings.checkIfWeCanLoad();

        LogManager.info("Showing splash screen and loading everything");
        settings.loadEverything(); // Loads everything that needs to be loaded
        LogManager.info("Launcher finished loading everything");

        if (settings.isFirstTimeRun()) {
            LogManager.warn("Launcher not setup. Loading Setup Dialog");
            new SetupDialog();
        }

        boolean open = true;

        if (autoLaunch != null) {
            if (settings.isInstanceBySafeName(autoLaunch)) {
                Instance instance = settings.getInstanceBySafeName(autoLaunch);
                LogManager.info("Opening Instance " + instance.getName());
                if (instance.launch()) {
                    open = false;
                } else {
                    LogManager.error("Error Opening Instance " + instance.getName());
                }
            } else if (settings.instancesV2.stream()
                    .anyMatch(instance -> instance.getSafeName().equalsIgnoreCase(autoLaunch))) {
                Optional<InstanceV2> instance = settings.instancesV2.stream()
                        .filter(instanceV2 -> instanceV2.getSafeName().equalsIgnoreCase(autoLaunch)).findFirst();

                if (instance.isPresent()) {
                    LogManager.info("Opening Instance " + instance.get().launcher.name);
                    if (instance.get().launch()) {
                        open = false;
                    } else {
                        LogManager.error("Error Opening Instance " + instance.get().launcher.name);
                    }
                }
            }
        }

        if (settings.enableDiscordIntegration()) {
            try {
                DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
                DiscordRPC.discordInitialize(Constants.DISCORD_CLIENT_ID, handlers, true);
                DiscordRPC.discordRegister(Constants.DISCORD_CLIENT_ID, "");

                discordInitialized = true;

                Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
            } catch (Throwable e) {
                LogManager.logStackTrace("Failed to initialize Discord integration", e);
            }
        }

        if (!skipIntegration) {
            integrate();
        }

        if (packCodeToAdd != null) {
            if (settings.addPack(packCodeToAdd)) {
                Pack packAdded = settings.getSemiPublicPackByCode(packCodeToAdd);
                if (packAdded != null) {
                    LogManager.info("The pack " + packAdded.getName() + " was automatically added to the launcher!");
                } else {
                    LogManager.error("Error automatically adding semi public pack with code of " + packCodeToAdd + "!");
                }
            } else {
                LogManager.error("Error automatically adding semi public pack with code of " + packCodeToAdd + "!");
            }
        }

        // Open the Launcher
        final boolean openLauncher = open;
        SwingUtilities.invokeLater(() -> {
            new LauncherFrame(openLauncher);
            ss.close();
        });
    }

    private static void checkInstalledCorrectly() {
        boolean matched = false;

        if ((Files.notExists(FileSystem.CONFIGS) && Files.notExists(FileSystem.BASE_DIR.resolve("Configs")))
                && FileSystem.CONFIGS.getParent().toFile().listFiles().length > 1) {
            matched = true;

            if (DialogManager.optionDialog().setTitle("Warning")
                    .setContent(new HTMLBuilder().center().text("I've detected that you may "
                            + "not have installed this in the right location.<br/><br/>The exe or jar file should "
                            + "be placed in it's own folder with nothing else in it.<br/><br/>Are you 100% sure "
                            + "that's what you've done?").build())
                    .addOption("Yes It's fine", true).addOption("Whoops. I'll change that now")
                    .setType(DialogManager.ERROR).show() != 0) {
                System.exit(0);
            }
        }

        if (!matched && (Files.notExists(FileSystem.CONFIGS) && Files.notExists(FileSystem.BASE_DIR.resolve("Configs")))
                && FileSystem.BASE_DIR.equals(FileSystem.USER_DOWNLOADS)) {
            matched = true;

            if (DialogManager.optionDialog().setTitle("Warning").setContent(new HTMLBuilder().center().text(
                    "ATLauncher shouldn't be run from the Downloads folder.<br/><br/>Please put ATLauncher in it's own folder and run the launcher from there!")
                    .build()).addOption("Yes It's fine", true).addOption("Whoops. I'll change that now")
                    .setType(DialogManager.ERROR).show() != 0) {
                System.exit(0);
            }
        }

        if (matched) {
            if (DialogManager.optionDialog().setTitle("Warning")
                    .setContent(new HTMLBuilder().center()
                            .text("Are you absolutely sure you've put ATLauncher in it's own folder?<br/><br/>If you "
                                    + "haven't and you click 'Yes, delete my files', this may delete "
                                    + FileSystem.CONFIGS.getParent().toFile().listFiles().length
                                    + " files and folders.<br/><br/>Are you 100% sure?")
                            .build())
                    .addOption("Yes, I understand", true).addOption("No, exit and I'll put it in a folder")
                    .setType(DialogManager.ERROR).show() != 0) {
                System.exit(0);
            }
        }
    }

    /**
     * Loads the theme and applies the theme's settings to the look and feel.
     */
    public static void loadTheme(String theme) {
        try {
            setLAF(theme);
            modifyLAF();

            // now the theme is loaded, we can intialize the toaster
            TOASTER = Toaster.instance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Sets the look and feel of the application.
     *
     * @throws Exception
     */
    private static void setLAF(String theme) throws Exception {
        try {
            Class.forName(theme);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            theme = Constants.DEFAULT_THEME_CLASS;
            App.settings.setTheme(theme);
        }

        // install the theme
        Class.forName(theme).getMethod("install").invoke(null);

        // then grab the instance
        THEME = (ATLauncherLaf) Class.forName(theme).getMethod("getInstance").invoke(null);
    }

    /**
     * This modifies the look and feel based upon the theme loaded.
     *
     * @throws Exception
     */
    private static void modifyLAF() throws Exception {
        ToolTipManager.sharedInstance().setDismissDelay(15000);
        ToolTipManager.sharedInstance().setInitialDelay(50);

        UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(2, 5, 2, 5));
        UIManager.put("defaultFont", App.THEME.getNormalFont());
        UIManager.put("Button.font", App.THEME.getNormalFont());
        UIManager.put("Toaster.font", App.THEME.getNormalFont());
        UIManager.put("Toaster.opacity", 0.75F);

        UIManager.put("FileChooser.readOnly", Boolean.TRUE);
        UIManager.put("ScrollBar.minimumThumbSize", new Dimension(50, 50));
        UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());

        // for Mac we setup correct copy/cut/paste shortcuts otherwise it just uses Ctrl
        if (OS.isMac()) {
            InputMap textField = (InputMap) UIManager.get("TextField.focusInputMap");
            textField.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            textField.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            textField.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            textField.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.selectAllAction);

            InputMap passwordField = (InputMap) UIManager.get("PasswordField.focusInputMap");
            passwordField.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.copyAction);
            passwordField.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.pasteAction);
            passwordField.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.cutAction);
            passwordField.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.selectAllAction);

            InputMap textArea = (InputMap) UIManager.get("TextArea.focusInputMap");
            textArea.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            textArea.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK), DefaultEditorKit.pasteAction);
            textArea.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            textArea.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.selectAllAction);

            InputMap editorPane = (InputMap) UIManager.get("EditorPane.focusInputMap");
            editorPane.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK), DefaultEditorKit.copyAction);
            editorPane.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.pasteAction);
            editorPane.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK), DefaultEditorKit.cutAction);
            editorPane.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.selectAllAction);
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
            trayIcon = new TrayIcon(Utils.getImage("/assets/image/Icon.png"));

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
     * This creates some integration files so the launcher can work with other
     * applications by storing some properties about itself and it's location in a
     * set location.
     */
    public static void integrate() {
        if (!Utils.getOSStorageDir().exists()) {
            boolean success = Utils.getOSStorageDir().mkdirs();
            if (!success) {
                LogManager.error("Failed to create OS storage directory");
                return;
            }
        }

        File f = new File(Utils.getOSStorageDir(), "atlauncher.conf");

        try {
            f.createNewFile();
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to create atlauncher.conf", e);
            return;
        }

        Properties props = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(f);
            props.load(is);
        } catch (FileNotFoundException e) {
            LogManager.logStackTrace("Failed to open atlauncher.conf for reading", e);
            return;
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to read from atlauncher.conf", e);
            return;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to close atlauncher.conf FileInputStream", e);
                }
            }
        }

        props.setProperty("java_version", Java.getLauncherJavaVersion());
        props.setProperty("location", FileSystem.BASE_DIR.toString());
        props.setProperty("executable",
                new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getAbsolutePath());

        packCodeToAdd = props.getProperty("pack_code_to_add", null);
        props.remove("pack_code_to_add");

        packToInstall = props.getProperty("pack_to_install", null);
        props.remove("pack_to_install");

        packShareCodeToInstall = props.getProperty("pack_share_code_to_install", null);
        props.remove("pack_share_code_to_install");

        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
            props.store(os, "");
        } catch (FileNotFoundException e) {
            LogManager.logStackTrace("Failed to open atlauncher.conf for writing", e);
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to write to atlauncher.conf", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to close atlauncher.conf FileOutputStream", e);
                }
            }
        }
    }

    private static void parseCommandLineArguments(String[] args) {
        // Parse all the command line arguments
        OptionParser parser = new OptionParser();
        parser.accepts("updated").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration").withOptionalArg().ofType(Boolean.class);
        parser.accepts("disable-error-reporting").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-integration").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-hash-checking").withOptionalArg().ofType(Boolean.class);
        parser.accepts("force-offline-mode").withOptionalArg().ofType(Boolean.class);
        parser.accepts("working-dir").withRequiredArg().ofType(String.class);
        parser.accepts("no-launcher-update").withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug").withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug-level").withRequiredArg().ofType(Integer.class);
        parser.accepts("launch").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);
        autoLaunch = options.has("launch") ? (String) options.valueOf("launch") : null;

        if (options.has("updated")) {
            wasUpdated = true;
        }

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
            LogManager.debug("Skipping tray integration!");
        }

        disableErrorReporting = options.has("disable-error-reporting");
        if (disableErrorReporting) {
            LogManager.debug("Disabling error reporting!");
        }

        if (options.has("working-dir")) {
            Path workingDirTemp = Paths.get(String.valueOf(options.valueOf("working-dir")));
            if (Files.exists(workingDirTemp) && Files.isDirectory(workingDirTemp)) {
                LogManager.debug("Working directory set to " + workingDirTemp + "!");
                workingDir = workingDirTemp;
            } else {
                LogManager.error("Cannot set working directory to " + workingDirTemp + " as it doesn't exist!");
            }
        }

        noLauncherUpdate = options.has("no-launcher-update");
        if (noLauncherUpdate) {
            LogManager.debug("Not updating the launcher!");
        }

        skipIntegration = options.has("skip-integration");
        if (skipIntegration) {
            LogManager.debug("Skipping integration!");
        }

        skipHashChecking = options.has("skip-hash-checking");
        if (skipHashChecking) {
            LogManager.debug("Skipping hash checking! Don't ask for support with this enabled!");
        }
    }
}
