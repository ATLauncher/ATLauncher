/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher;

import java.awt.Image;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.theme.DefaultTheme;
import com.atlauncher.gui.theme.LoadableTheme;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.utils.Utils;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

public class App {
    // Using this will help spread the workload across multiple threads allowing you to do many
    // tasks at once
    // Approach with caution though
    // Dedicated 2 threads to the TASKPOOL shouldnt have any problems with that little
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);

    public static Theme THEME = new DefaultTheme().createTheme();
    private static SystemTray TRAY = null;
    public static PopupMenu TRAY_MENU = new TrayMenu();

    public static boolean wasUpdated = false;

    public static Settings settings;

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

        File config = new File(Utils.getCoreGracefully(), "Configs");
        if (!config.exists()) {
            int files = config.getParentFile().list().length;
            if (files > 2) {
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

        settings = new Settings(); // Setup the Settings and wait for it to finish

        loadTheme();

        settings.loadConsole(); // Load console AFTER L&F

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
        settings.log("Using Theme: " + THEME.getThemeName() + " by " + THEME.getAuthorsName());

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

    public static void loadTheme() {
        File themeFile = settings.getThemeFile();
        if (themeFile != null) {
            try {
                THEME = Settings.gson.fromJson(new FileReader(themeFile), LoadableTheme.class)
                        .createTheme();
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                THEME = new DefaultTheme().createTheme();
            } catch (JsonIOException e) {
                e.printStackTrace();
                THEME = new DefaultTheme().createTheme();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                THEME = new DefaultTheme().createTheme();
            }
        }

        try {
            setLAF();
            modifyLAF();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void setLAF() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase("nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
            }
        }
    }

    private static void modifyLAF() throws Exception {
        UIManager.put("control", App.THEME.getBaseColour());
        UIManager.put("text", App.THEME.getTextColour());
        UIManager.put("nimbusBase", App.THEME.getButtonColour());
        UIManager.put("nimbusFocus", App.THEME.getBaseColour());
        UIManager.put("nimbusBorder", App.THEME.getBaseColour());
        UIManager.put("nimbusLightBackground", App.THEME.getBaseColour());
        UIManager.put("info", App.THEME.getBaseColour());
        UIManager.put("nimbusSelectionBackground", App.THEME.getDropDownSelectionColour());
        UIManager
                .put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder(2, 5, 2, 5));

        ToolTipManager.sharedInstance().setDismissDelay(15000);
        ToolTipManager.sharedInstance().setInitialDelay(50);

        UIManager.getLookAndFeelDefaults().put("defaultFont", App.THEME.getDefaultFont());
        UIManager.getLookAndFeelDefaults().put("Button.font", App.THEME.getButtonFont());
        if (Utils.isMac()) {
            InputMap im = (InputMap) UIManager.get("TextField.focusInputMap");
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.copyAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.pasteAction);
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.META_DOWN_MASK),
                    DefaultEditorKit.cutAction);
        }
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