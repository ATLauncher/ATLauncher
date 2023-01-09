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

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
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

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.data.Pack;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherConsole;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PackManager;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.themes.ATLauncherLaf;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.formdev.flatlaf.extras.FlatInspector;
import com.formdev.flatlaf.extras.FlatUIDefaultsInspector;

import io.github.asyncronous.toast.Toaster;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSProcess;
import oshi.software.os.OperatingSystem;

/**
 * Main entry point for the application, Java runs the main method here when the
 * application is launched.
 */
public class App {
    public static String[] PASSED_ARGS;

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
    public static TrayMenu TRAY_MENU;

    public static LauncherConsole console;

    /**
     * If the launcher was just updated and this is it's first time loading after
     * the update. This is used to check for when there are possible issues in which
     * the user may have to download the update manually.
     */
    public static boolean wasUpdated = false;

    public static boolean discordInitialized = false;

    /**
     * This allows skipping the setup dialog on first run. This is mainly used for
     * automation tests. It can be skipped with the below command line argument.
     * <p/>
     * --skip-setup-dialog
     */
    public static boolean skipSetupDialog = false;

    /**
     * This allows skipping the system tray integration so that the launcher doesn't
     * even try to show the icon and menu etc., in the users system tray. It can be
     * skipped with the below command line argument.
     * <p/>
     * --skip-tray-integration
     */
    public static boolean skipTrayIntegration = false;

    /**
     * This allows skipping the in built analytics collection. This is mainly useful
     * for development when you don't want to report analytics. For end users, this
     * can be turned off in the launcher setup or through the settings.
     * <p/>
     * --disable-analytics
     */
    public static boolean disableAnalytics = false;

    /**
     * This allows skipping the in built error reporting. This is mainly useful for
     * development when you don't want to report errors to an external third party.
     * <p/>
     * --disable-error-reporting
     */
    public static boolean disableErrorReporting = false;

    /**
     * This forces the working directory for the launcher. It can be changed with
     * the below command line argument.
     * <p/>
     * --working-dir=C:/Games/ATLauncher
     */
    public static Path workingDir = null;

    /**
     * This will tell the launcher to allow all SSL certs regardless of validity.
     * This is insecure and only intended for development purposes.
     * <p/>
     * --allow-all-ssl-certs
     */
    public static boolean allowAllSslCerts = false;

    /**
     * This forces the launcher to not check for a launcher update. It can be
     * enabled with the below command line argument.
     * <p/>
     * --no-launcher-update
     */
    public static boolean noLauncherUpdate = false;

    /**
     * This will tell the launcher to not show the console. You can open the console
     * through the tray menu or the main launcher frame.
     * <p/>
     * --no-console
     */
    public static boolean noConsole = false;

    /**
     * This will close the launcher once Minecraft is launcher. This is only
     * effective when combined with the --launch parameter.
     * <p/>
     * --close-launcher
     */
    public static boolean closeLauncher = false;

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
     * Config overrides.
     */
    public static String configOverride = null;

    /**
     * This sets a pack to auto launch on startup
     */
    public static String autoLaunch = null;

    /**
     * This is the Settings instance which holds all the users settings.
     */
    public static Settings settings;

    /**
     * This is where a majority of the UI components are held and refreshed through,
     * as well as where misc launcher actions and updater logic is held.
     */
    public static Launcher launcher;

    /**
     * This is the theme used by the launcher. For more information on themeing,
     * please see the README.
     */
    public static ATLauncherLaf THEME;

    public static TrayIcon trayIcon;

    static {
        // Sets up where all uncaught exceptions go to.
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionStrainer());
    }

    /**
     * Where the magic happens.
     *
     * @param args all the arguments passed in from the command line
     */
    public static void main(String[] args) {
        PASSED_ARGS = args;

        // Parse all the command line arguments
        parseCommandLineArguments(args);

        // Initialize the error reporting unless disabled by command line
        if (!disableErrorReporting) {
            ErrorReporting.enable();
        }

        // check the launcher has been 'installed' correctly
        checkInstalledCorrectly();

        // setup OS specific things
        setupOSSpecificThings();

        try {
            LogManager.info("Organising filesystem");
            FileSystem.organise();
        } catch (IOException e) {
            LogManager.logStackTrace("Error organising filesystem", e, false);
        }

        // Load the settings from json, convert old properties config and validate it
        loadSettings();

        try {
            Language.init();
            Language.setLanguage(settings.language);
        } catch (IOException e1) {
            LogManager.logStackTrace("Error loading language", e1);
        }

        // inject any certs into the keystore that we need (Let's Encrypt for example)
        Java.injectNeededCerts();

        // after settings have loaded, then allow all ssl certs if required
        if (allowAllSslCerts) {
            Network.allowAllSslCerts();
        }

        // check for bad install locations (OneDrive, Program Files)
        checkForBadFolderInstall();

        // Setup the Launcher and wait for it to finish.
        launcher = new Launcher();

        // Load the theme and style everything.
        loadTheme(settings.theme);

        // check for _JAVA_OPTIONS being set which breaks things
        checkForJavaOptions();

        final SplashScreen ss = new SplashScreen();

        // Load and show the splash screen while we load other things.
        SwingUtilities.invokeLater(() -> ss.setVisible(true));

        console = new LauncherConsole();
        LogManager.start();

        if (!noConsole && settings.enableConsole) {
            // Show the console if enabled.
            SwingUtilities.invokeLater(() -> console.setVisible(true));
        }

        if (settings.enableTrayMenu && !skipTrayIntegration) {
            try {
                // Try to enable the tray icon.
                trySystemTrayIntegration();
            } catch (Exception e) {
                LogManager.logStackTrace(e, false);
            }
        }

        // log out the system information to the console
        logSystemInformation(args);

        LogManager.info("Showing splash screen and loading everything");
        launcher.loadEverything(); // Loads everything that needs to be loaded
        LogManager.info("Launcher finished loading everything");

        if (settings.firstTimeRun) {
            if (skipSetupDialog) {
                App.settings.firstTimeRun = false;
                App.settings.save();
            } else {
                LogManager.warn("Launcher not setup. Loading Setup Dialog");
                new SetupDialog();
            }
        }

        boolean open = true;

        if (autoLaunch != null) {
            Optional<Instance> instance = InstanceManager.getInstances().stream().filter(
                    i -> i.getName().equalsIgnoreCase(autoLaunch) || i.getSafeName().equalsIgnoreCase(autoLaunch))
                    .findFirst();
            if (instance.isPresent()) {
                LogManager.info("Opening Instance " + instance.get().launcher.name);
                if (instance.get().launch()) {
                    open = false;
                } else {
                    LogManager.error("Error Opening Instance " + instance.get().launcher.name);
                }
            } else {
                LogManager.error("Couldn't find instance with name of " + autoLaunch + " to auto launch.");
            }
        }

        if (packCodeToAdd != null) {
            if (PackManager.addPack(packCodeToAdd)) {
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

        // Open the Launcher
        final boolean openLauncher = open;
        SwingUtilities.invokeLater(() -> {
            new LauncherFrame(openLauncher);
            ss.close();
        });
    }

    public static void ensureDiscordIsInitialized() {
        if (!OS.isArm() && !discordInitialized) {
            try {
                DiscordEventHandlers handlers = new DiscordEventHandlers.Builder().build();
                DiscordRPC.discordInitialize(Constants.DISCORD_CLIENT_ID, handlers, true);
                DiscordRPC.discordRegister(Constants.DISCORD_CLIENT_ID, "");

                discordInitialized = true;

                Runtime.getRuntime().addShutdownHook(new Thread(DiscordRPC::discordShutdown));
            } catch (Throwable e) {
                LogManager.logStackTrace("Failed to initialize Discord integration", e);
                discordInitialized = false;
            }
        }
    }

    private static void logSystemInformation(String[] args) {
        LogManager.info(Constants.LAUNCHER_NAME + " Version: " + Constants.VERSION);

        LogManager.info(String.format("App Arguments: %s", Gsons.DEFAULT_SLIM.toJson(args)));

        LogManager.info(String.format("JVM Arguments: %s",
                Gsons.DEFAULT_SLIM.toJson(ManagementFactory.getRuntimeMXBean().getInputArguments())));

        SwingUtilities.invokeLater(
                () -> Java.getInstalledJavas().forEach(version -> LogManager.debug(Gsons.DEFAULT.toJson(version))));

        LogManager.info("Java Version: "
                + String.format("Java %d (%s)", Java.getLauncherJavaVersionNumber(), Java.getLauncherJavaVersion()));

        LogManager.info("Java Path: " + settings.javaPath);

        LogManager.info("64 Bit Java: " + Java.is64Bit());

        int maxRam = OS.getMaximumRam();
        LogManager.info("RAM Available: " + (maxRam == 0 ? "Unknown" : maxRam + "MB"));

        LogManager.info("Launcher Directory: " + FileSystem.BASE_DIR);

        if (OS.isMac()) {
            LogManager.info("Using Mac App? " + (OS.isUsingMacApp() ? "Yes" : "No"));
        }

        if (OS.isArm()) {
            LogManager.info("Arm: " + OS.getArch());
        }

        if (OS.isUsingFlatpak()) {
            LogManager.info("Using Flatpak!");
        }

        try {
            SystemInfo systemInfo = OS.getSystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();

            List<GraphicsCard> cards = hal.getGraphicsCards();
            if (cards.size() != 0) {
                for (GraphicsCard card : cards) {
                    LogManager.info("GPU: " + card.getName() + " (" + card.getVendor() + ") " + card.getVersionInfo()
                            + " " + (card.getVRam() / 1048576) + "MB VRAM");
                }
            }

            CentralProcessor cpu = hal.getProcessor();
            LogManager.info(String.format("CPU: %s %d cores/%d threads", cpu.getProcessorIdentifier().getName().trim(),
                    cpu.getPhysicalProcessorCount(), cpu.getLogicalProcessorCount()));

            OperatingSystem os = systemInfo.getOperatingSystem();

            LogManager.info("Operating System: " + os.getFamily() + " (" + os.getVersionInfo() + ")");
            LogManager.info("Bitness: " + os.getBitness());
            LogManager.info("Uptime: " + os.getSystemUptime());
            LogManager.info("Manufacturer: " + os.getManufacturer());

            if (OS.isWindows() && OS.isUsingAntivirus()) {
                LogManager.warn(
                        "A running antivirus process was found on your system. If you notice any issues running Minecraft or downloading files, please whitelist ATLauncher and its folder in your antivirus program/s listed below.");

                for (OSProcess process : OS.getAntivirusProcesses()) {
                    LogManager.info(String.format("Process %s (running at %s)", process.getName(),
                            process.getPath()));
                }
            }
        } catch (Throwable t) {
            LogManager.logStackTrace(t);
        }
    }

    private static void checkInstalledCorrectly() {
        boolean matched = false;

        // user used the installer
        if (Files.exists(FileSystem.BASE_DIR.resolve("unins000.dat"))
                && Files.exists(FileSystem.BASE_DIR.resolve("unins000.exe"))) {
            return;
        }

        if (OS.isLinux() && GraphicsEnvironment.isHeadless()) {
            DialogManager.okDialog().setTitle("Using Headless Java").setContent(new HTMLBuilder().center().text(
                    "You're running ATLauncher with a headless version of Java installed on your system.<br/><br/>ATLauncher cannot run with a headless version of Java. Please uninstall it and install a non headless version of Java to continue.<br/><br/>If you're unsure how, please Google for instructions for your specific distro.")
                    .build())
                    .setType(DialogManager.ERROR).show();
            System.exit(0);
        }

        if (Files.exists(FileSystem.BASE_DIR)
                && (Files.notExists(FileSystem.CONFIGS) && Files.notExists(FileSystem.BASE_DIR.resolve("Configs")))
                && FileSystem.CONFIGS.getParent().toFile().listFiles().length > 1)

        {
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
                && FileSystem.BASE_DIR.equals(FileSystem.getUserDownloadsPath())) {
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

    private static void checkForJavaOptions() {
        try {
            String javaOptions = System.getenv("_JAVA_OPTIONS");

            if (javaOptions != null && (javaOptions.toLowerCase().contains("-xmx")
                    || javaOptions.toLowerCase().contains("-xms") || javaOptions.toLowerCase().contains("-xss"))) {
                LogManager.warn("_JAVA_OPTIONS environment variable detected: " + javaOptions);

                if (!settings.ignoreJavaOptionsWarning) {
                    int ret = DialogManager.yesNoDialog().addOption(GetText.tr("Don't remind me again"))
                            .setTitle("Warning")
                            .setContent(new HTMLBuilder().center().text(
                                    "We've detected that you have a _JAVA_OPTIONS environment variable which may cause issues installing and playing Minecraft.<br/><br/>Do you want to fix this now so you don't have any issues in the future?")
                                    .build())
                            .setType(DialogManager.ERROR).show();

                    if (ret == 0) {
                        OS.openWebBrowser("https://atl.pw/javaoptionsfromlauncher");
                        System.exit(0);
                    } else if (ret == 2) {
                        settings.ignoreJavaOptionsWarning = true;
                        settings.save();
                    }
                }
            }
        } catch (Throwable t) {
            // ignored
        }
    }

    private static void checkForBadFolderInstall() {
        if (!settings.ignoreOneDriveWarning && FileSystem.BASE_DIR.toString().contains("OneDrive")) {
            LogManager.warn("ATLauncher installed within OneDrive!");

            int ret = DialogManager.yesNoDialog().addOption(GetText.tr("Don't remind me again"))
                    .setTitle(GetText.tr("ATLauncher installed within OneDrive"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We have detected that you're running ATLauncher from within OneDrive.<br/><br/>This can cause serious issues and you should move the folder outside of OneDrive.<br/><br/>Do you want to close the launcher and do this now?"))
                            .build())
                    .setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openFileExplorer(FileSystem.BASE_DIR, true);
                System.exit(0);
            } else if (ret == 2) {
                settings.ignoreOneDriveWarning = true;
                settings.save();
            }
        }

        if (OS.isWindows() && !settings.ignoreProgramFilesWarning
                && FileSystem.BASE_DIR.toString().contains("Program Files")) {
            LogManager.warn("ATLauncher installed within Program Files!");

            int ret = DialogManager.yesNoDialog().addOption(GetText.tr("Don't remind me again"))
                    .setTitle(GetText.tr("ATLauncher installed within Program Files"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We have detected that you're running ATLauncher from within Program Files.<br/><br/>This can cause serious issues and you should move the folder outside of Program Files.<br/><br/>Do you want to close the launcher and do this now?"))
                            .build())
                    .setType(DialogManager.WARNING).show();

            if (ret == 0) {
                OS.openFileExplorer(FileSystem.BASE_DIR, true);
                System.exit(0);
            } else if (ret == 2) {
                settings.ignoreProgramFilesWarning = true;
                settings.save();
            }
        }

        File testFile = FileSystem.BASE_DIR.resolve(".test").toFile();

        try {
            if ((!testFile.exists() && !testFile.createNewFile())
                    || !FileSystem.BASE_DIR.resolve(".test").toFile().canWrite()) {
                LogManager.error("ATLauncher cannot write files!");

                DialogManager.okDialog().setTitle(GetText.tr("ATLauncher cannot write files"))
                        .setContent(new HTMLBuilder().center().text(GetText.tr(
                                "We have detected that ATLauncher cannot write files in it's current location.<br/><br/>We cannot continue to run, you must move this folder somewhere else with write access.<br/><br/>Try moving to a folder in your Desktop or another drive.<br/><br/>You can also try running ATLauncher as administrator, but this is not recommended."))
                                .build())
                        .setType(DialogManager.ERROR).show();

                OS.openFileExplorer(FileSystem.BASE_DIR, true);
                System.exit(0);
            }
        } catch (IOException e) {
            LogManager.error("ATLauncher cannot write files!");

            DialogManager.okDialog().setTitle(GetText.tr("ATLauncher cannot write files"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "We have detected that ATLauncher cannot write files in it's current location.<br/><br/>We cannot continue to run, you must move this folder somewhere else with write access.<br/><br/>Try moving to a folder in your Desktop or another drive.<br/><br/>You can also try running ATLauncher as administrator, but this is not recommended."))
                            .build())
                    .setType(DialogManager.ERROR).show();

            OS.openFileExplorer(FileSystem.BASE_DIR, true);
            System.exit(0);
        }
    }

    private static void setupOSSpecificThings() {
        // do some Mac specific stuff, setting the name of the application and icon
        // set only when using jar bundle, as if using *.app, macOS sets icon and name automatically and apple.laf.useScreenMenuBar is set using build.gradle
        if (!OS.isUsingMacApp()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("apple.awt.application.name", Constants.LAUNCHER_NAME); // setting the application name in menu bar
            try {
                if (Java.isSystemJavaNewerThanJava8()) {
                    // if Java 9 or higher
                    Class<?> util = Class.forName("java.awt.Taskbar");
                    Method getTaskbar = util.getMethod("getTaskbar");
                    Object taskbar = getTaskbar.invoke(util);
                    Method setIconImage = util.getMethod("setIconImage", Image.class);
                    setIconImage.invoke(taskbar, Utils.getImage("/assets/image/icon-osx.png"));
                } else {
                    // if Java 8 or lower
                    Class<?> util = Class.forName("com.apple.eawt.Application");
                    Method getApplication = util.getMethod("getApplication");
                    Object application = getApplication.invoke(util);
                    Method setDockIconImage = util.getMethod("setDockIconImage", Image.class);
                    setDockIconImage.invoke(application, Utils.getImage("/assets/image/icon-osx.png"));
                }
            } catch (Exception ex) {
                LogManager.logStackTrace("Failed to set dock icon", ex);
            }
        }

        // do some linux specific stuff, namely set the application title in Gnome
        if (OS.isLinux()) {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                java.lang.reflect.Field awtAppClassNameField = toolkit.getClass().getDeclaredField("awtAppClassName");
                awtAppClassNameField.setAccessible(true);
                awtAppClassNameField.set(toolkit, Constants.LAUNCHER_NAME);
            } catch (Throwable t) {
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

            // now the theme is loaded, we can intialize the toaster/tray menu
            TOASTER = Toaster.instance();
            TRAY_MENU = new TrayMenu();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void loadSettings() {
        // load the users settings or load defaults if settings file doesn't exist
        if (Files.exists(FileSystem.SETTINGS)) {
            try (FileReader fileReader = new FileReader(FileSystem.SETTINGS.toFile())) {
                settings = Gsons.DEFAULT.fromJson(fileReader, Settings.class);
            } catch (Throwable t) {
                LogManager.logStackTrace("Error loading settings, using defaults", t, false);
                settings = new Settings();
            }
        } else {
            settings = new Settings();
        }

        if (Files.exists(FileSystem.LAUNCHER_CONFIG)) {
            try (FileReader fileReader = new FileReader(FileSystem.LAUNCHER_CONFIG.toFile())) {
                Properties properties = new Properties();
                properties.load(fileReader);
                settings.convert(properties);
            } catch (Throwable t) {
                LogManager.logStackTrace("Error loading settings, using defaults", t, false);
                settings = new Settings();
            }

            try {
                Files.delete(FileSystem.LAUNCHER_CONFIG);
            } catch (IOException e) {
                LogManager.warn("Failed to delete old launcher config.");
            }
        }

        // make sure settings isn't null
        if (settings == null) {
            settings = new Settings();
        }

        // validate the settings
        settings.validate();
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
            settings.theme = theme;
        }

        // install the theme
        Class.forName(theme).getMethod("install").invoke(null);

        // then grab the instance
        THEME = (ATLauncherLaf) Class.forName(theme).getMethod("getInstance").invoke(null);

        // add in flat inspector to allow inspecting UI elements for theming purposes on
        // non release versions
        if (!Constants.VERSION.isReleaseStream()) {
            FlatInspector.install("ctrl shift alt X");
            FlatUIDefaultsInspector.install("ctrl shift alt Y");
        }

        // register the fonts so they can show within HTML
        THEME.registerFonts();
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
        UIManager.put("defaultFont", THEME.getNormalFont());
        UIManager.put("Button.font", THEME.getNormalFont());
        UIManager.put("Toaster.font", THEME.getNormalFont());
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
            trayIcon = new TrayIcon(Utils.getImage("/assets/image/icon.png"));

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

    private static void parseCommandLineArguments(String[] args) {
        // Parse all the command line arguments
        OptionParser parser = new OptionParser();
        parser.accepts("updated", "If the launcher was just updated.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-setup-dialog",
                "If the first time setup dialog should be skipped, using the defaults. Note that this will enable analytics by default.")
                .withOptionalArg().ofType(Boolean.class);
        parser.accepts("skip-tray-integration", "If the tray icon should not be enabled.").withOptionalArg()
                .ofType(Boolean.class);
        parser.accepts("disable-analytics", "If analytics should be disabled.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("disable-error-reporting", "If error reporting should be disabled.").withOptionalArg()
                .ofType(Boolean.class);
        parser.accepts("working-dir", "This forces the working directory for the launcher.").withRequiredArg()
                .ofType(String.class);
        parser.accepts("base-launcher-domain", "The base launcher domain.").withRequiredArg().ofType(String.class);
        parser.accepts("base-cdn-domain", "The base CDN domain.").withRequiredArg().ofType(String.class);
        parser.accepts("base-cdn-path", "The path on the CDN used for downloading files.").withRequiredArg()
                .ofType(String.class);
        parser.accepts("allow-all-ssl-certs",
                "This will tell the launcher to allow all SSL certs regardless of validity. This is insecure and only intended for development purposes.")
                .withOptionalArg().ofType(Boolean.class);
        parser.accepts("no-launcher-update",
                "This forces the launcher to not check for a launcher update. It can be enabled with the below command line argument.")
                .withOptionalArg().ofType(Boolean.class);
        parser.accepts("no-console", "If the console shouldn't be shown.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("close-launcher", "If the launcher should be closed after launching an instance.")
                .withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug", "If debug logging should be enabled.").withOptionalArg().ofType(Boolean.class);
        parser.accepts("debug-level", "The level of debug logging that should be logged.").withRequiredArg()
                .ofType(Integer.class);
        parser.accepts("launch",
                "The name of an instance to automatically launch. Can be the instances directory name in the file system or the full name of the instance.")
                .withRequiredArg().ofType(String.class);
        parser.accepts("proxy-type", "The type of proxy to use. Can be \"SOCKS\", \"DIRECT\" or \"HTTP\".")
                .withRequiredArg().ofType(String.class);
        parser.accepts("proxy-host", "The host of the proxy to use.").withRequiredArg().ofType(String.class);
        parser.accepts("proxy-port", "The port of the proxy to use.").withRequiredArg().ofType(Integer.class);
        parser.accepts("config-override", "A JSON string to override the launchers config.").withRequiredArg()
                .ofType(String.class);
        parser.acceptsAll(Arrays.asList("help", "?"), "Shows help for the arguments for the application.").forHelp();

        OptionSet options = parser.parse(args);
        autoLaunch = options.has("launch") ? (String) options.valueOf("launch") : null;

        if (options.has("help")) {
            try {
                parser.printHelpOn(System.out);
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            System.exit(0);
        }

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

        skipSetupDialog = options.has("skip-setup-dialog");
        if (skipSetupDialog) {
            LogManager.debug("Skipping setup dialog!");
        }

        skipTrayIntegration = options.has("skip-tray-integration");
        if (skipTrayIntegration) {
            LogManager.debug("Skipping tray integration!");
        }

        disableAnalytics = options.has("disable-analytics");
        if (disableAnalytics) {
            LogManager.debug("Disabling analytics!");
        }

        disableErrorReporting = options.has("disable-error-reporting");
        if (disableErrorReporting) {
            LogManager.debug("Disabling error reporting!");
        }

        if (options.has("working-dir")) {
            Path workingDirTemp = Paths.get(String.valueOf(options.valueOf("working-dir")));
            workingDir = workingDirTemp;
        }

        if (options.has("base-launcher-domain")) {
            String baseLauncherDomain = String.valueOf(options.valueOf("base-launcher-domain"));

            Constants.setBaseLauncherDomain(baseLauncherDomain);
            LogManager.warn("Base launcher domain set to " + baseLauncherDomain);
        }

        if (options.has("base-cdn-domain")) {
            String baseCdnDomain = String.valueOf(options.valueOf("base-cdn-domain"));

            Constants.setBaseCdnDomain(baseCdnDomain);
            LogManager.warn("Base cdn domain set to " + baseCdnDomain);
        }

        if (options.has("base-cdn-path")) {
            String baseCdnPath = String.valueOf(options.valueOf("base-cdn-path"));

            Constants.setBaseCdnPath(baseCdnPath);
            LogManager.warn("Base cdn path set to " + baseCdnPath);
        }

        allowAllSslCerts = options.has("allow-all-ssl-certs");
        if (allowAllSslCerts) {
            LogManager.warn("Allowing all ssl certs. This is insecure and should only be used for development.");
        }

        noLauncherUpdate = options.has("no-launcher-update");
        if (noLauncherUpdate) {
            LogManager.debug("Not updating the launcher!");
        }

        noConsole = options.has("no-console");
        if (noConsole) {
            LogManager.debug("Not showing console!");
        }

        closeLauncher = options.has("close-launcher");
        if (closeLauncher) {
            LogManager.debug("Closing launcher once Minecraft is launched!");
        }

        if (options.has("proxy-type") && options.has("proxy-host") && options.has("proxy-port")) {
            String proxyType = String.valueOf(options.valueOf("proxy-type"));
            String proxyHost = String.valueOf(options.valueOf("proxy-host"));
            Integer proxyPort = (Integer) options.valueOf("proxy-port");

            Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.valueOf(proxyType),
                    new InetSocketAddress(proxyHost, proxyPort));

            LogManager.warn("Proxy set to " + proxy);

            ProxySelector.setDefault(new ProxySelector() {
                @Override
                public List<java.net.Proxy> select(URI uri) {
                    return Arrays.asList(proxy);
                }

                @Override
                public void connectFailed(URI uri, SocketAddress sa, IOException e) {
                    LogManager.logStackTrace("Connection could not be established to proxy at socket [" + sa + "]", e);
                }
            });
        }

        if (options.has("config-override")) {
            configOverride = (String) options.valueOf("config-override");

            LogManager.warn("Config overridden: " + configOverride);
        }
    }
}
