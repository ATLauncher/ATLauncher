/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher;

import java.awt.Color;
import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.utils.Utils;

public class App {
    // Using this will help spread the workload across multiple threads allowing you to do many
    // tasks at once
    // Approach with caution though
    // Dedicated 2 threads to the TASKPOOL shouldnt have any problems with that little
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);

    private static SystemTray TRAY = null;
    public static PopupMenu TRAY_MENU = new TrayMenu();
    public static final Logger LOGGER = LogManager.getLogger();

    public static boolean wasUpdated = false;

    public static Settings settings;

    // Don't move this declaration anywheres, its important due to Java Class Loading
    private static final Color BASE_COLOR = new Color(40, 45, 50);

    static {
        // Setting the UI LAF here helps with loading the UI should improve performance
        try {
            setLAF();
            modifyLAF();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) {
        Locale.setDefault(Locale.ENGLISH); // Set English as the default locale
        System.setProperty("java.net.preferIPv4Stack", "true");
        String autoLaunch = null;
        if (args != null) {
            for (String arg : args) {
                String[] parts = arg.split("=");
                if (parts[0].equalsIgnoreCase("--launch")) {
                    autoLaunch = parts[1];
                } else if (parts[0].equalsIgnoreCase("--updated")) {
                    wasUpdated = true;
                }
            }
        }

        File config;
        if (Utils.isLinux()) {
            try {
                config = new File(App.class.getClassLoader().getResource("").toURI());
            } catch (URISyntaxException e) {
                config = new File(System.getProperty("user.dir"), "ATLauncher");
            }
        } else {
            config = new File(System.getProperty("user.dir"));
        }
        config = new File(config, "Configs");
        if (!config.exists()) {
            int files = config.getParentFile().list().length;
            if (files != 1) {
                String[] options = { "Yes It's Fine", "Whoops. I'll Change That Now" };
                int ret = JOptionPane.showOptionDialog(null,
                        "<html><center>I've detected that you may not have installed this "
                                + "in the right location.<br/><br/>The exe or jar file"
                                + "should be placed in it's own folder with nothing else "
                                + "in it<br/><br/>Are you 100% sure that's what you've"
                                + "done?</center></html>", "Warning", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    System.exit(0);
                }
            }
        }

        settings = new Settings(); // Setup the Settings and wait for it to
                                   // finish

        if (settings.enableTrayIcon()) {
            try {
                trySystemTrayIntegration(); // Try to enable the tray icon
            } catch (Exception e) {
                settings.logStackTrace(e);
            }
        }

        settings.log("ATLauncher Version: " + settings.getVersion());
        settings.log("Operating System: " + System.getProperty("os.name"));
        settings.log("RAM Available: " + Utils.getMaximumRam() + "MB");
        if (settings.isUsingCustomJavaPath()) {
            settings.log("Custom Java Path Set!", LogMessageType.warning, false);
        } else {
            if (settings.isUsingMacApp()) {
                File oracleJava = new File(
                        "/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
                if (oracleJava.exists() && oracleJava.canExecute()) {
                    settings.setJavaPath("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home");
                    settings.log("Launcher Forced Custom Java Path Set!", LogMessageType.warning,
                            false);
                }
            }
        }
        settings.log("Java Version: " + Utils.getActualJavaVersion(), LogMessageType.info, false);
        settings.log("Java Path: " + settings.getJavaPath());
        settings.log("64 Bit Java: " + Utils.is64Bit());
        settings.log("Launcher Directory: " + settings.getBaseDir());

        if (Utils.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ATLauncher "
                    + settings.getVersion());
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
            settings.setConsoleVisible(true, false);
        }

        settings.log("Showing splash screen and loading everything");
        SplashScreen ss = new SplashScreen(); // Show Splash Screen
        settings.loadEverything(); // Loads everything that needs to be loaded
        ss.close(); // Close the Splash Screen
        settings.log("Launcher finished loading everything");

        if (settings.isFirstTimeRun()) {
            settings.log("Launcher not setup. Loading Setup Dialog", LogMessageType.warning, false);
            new SetupDialog(settings);
        }

        boolean open = true;

        if (autoLaunch != null) {
            if (settings.isInstanceBySafeName(autoLaunch)) {
                Instance instance = settings.getInstanceBySafeName(autoLaunch);
                settings.log("Opening Instance " + instance.getName());
                if (instance.launch()) {
                    open = false;
                } else {
                    settings.log("Error Opening Instance  " + instance.getName(),
                            LogMessageType.error, false);
                }
            }
        }
        ((TrayMenu) TRAY_MENU).localize();
        new LauncherFrame(open); // Open the Launcher
    }

    private static void setLAF() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase("nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
            }
        }
    }

    private static void modifyLAF() throws Exception {
        UIManager.put("control", BASE_COLOR);
        UIManager.put("text", Color.WHITE);
        UIManager.put("nimbusBase", Color.BLACK);
        UIManager.put("nimbusFocus", BASE_COLOR);
        UIManager.put("nimbusBorder", BASE_COLOR);
        UIManager.put("nimbusLightBackground", BASE_COLOR);
        UIManager.put("info", BASE_COLOR);
        UIManager.put("nimbusSelectionBackground", new Color(100, 100, 200));
        UIManager
                .put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(2, 5, 2, 5));
    }

    private static void trySystemTrayIntegration() throws Exception {
        if (SystemTray.isSupported()) {
            TRAY = SystemTray.getSystemTray();

            Image trayIconImage = Utils.getImage("/assets/image/Icon.png");
            int trayIconWidth = new TrayIcon(Utils.getImage("/assets/image/Icon.png")).getSize().width;
            TRAY.add(new TrayIcon(trayIconImage.getScaledInstance(trayIconWidth, -1,
                    Image.SCALE_SMOOTH), "tray_icon") {
                {
                    this.setPopupMenu(TRAY_MENU);
                    this.setToolTip("ATLauncher");
                }
            });
        }
    }

}