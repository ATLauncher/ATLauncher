/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher;

import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App {
    // Using this will help spread the workload across multiple threads allowing you to do many
    // tasks at once
    // Approach with caution though
    // Dedicated 2 threads to the TASKPOOL shouldnt have any problems with that little
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);

    private static SystemTray TRAY = null;

    public static Settings settings;

    // Don't move this declaration anywheres, its important due to Java Class Loading
    private static final Color BASE_COLOR = new Color(40, 45, 50);

    static {
        // Setting the UI LAF here helps with loading the UI should improve performance
        try {
            setLAF();
            modifyLAF();

            //trySystemTrayIntegration();
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

        settings.log("ATLauncher Version: " + settings.getVersion());
        settings.log("Operating System: " + System.getProperty("os.name"));
        settings.log("RAM Available: " + Utils.getMaximumRam() + "MB");
        settings.log("Java Version: " + Utils.getJavaVersion(), LogMessageType.info, false);
        if (settings.isUsingCustomJavaPath()) {
            settings.log("Custom Java Path Set!", LogMessageType.warning, false);
        }
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
                    this.setPopupMenu(getSystemTrayMenu());
                    this.setToolTip("ATLauncher");
                }
            });
        }
    }

    // TODO: Allow detection of when Minecraft is open, console is closed etc, to update the menu
    private static PopupMenu getSystemTrayMenu() {
        PopupMenu menu = new PopupMenu();

        menu.add(new MenuItem("Kill Minecraft") {
            {
                this.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        App.settings.killMinecraft();
                    }
                });
            }
        });

        menu.add(new MenuItem("Show Console") {
            {
                this.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (!App.settings.isConsoleVisible()) {
                            App.settings.setConsoleVisible(true);
                        }
                    }
                });
            }
        });

        menu.add(new MenuItem("Hide Console") {
            {
                this.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        if (App.settings.isConsoleVisible()) {
                            App.settings.setConsoleVisible(false);
                        }
                    }
                });
            }
        });

        menu.addSeparator(); // Add Separator

        menu.add(new MenuItem("Quit") {
            {
                this.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        System.exit(0);
                    }
                });
            }
        });

        return menu;
    }
}