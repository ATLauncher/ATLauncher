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
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.TrayMenu;
import com.atlauncher.gui.dialogs.SetupDialog;
import com.atlauncher.gui.theme.Theme;
import com.atlauncher.utils.Utils;

import io.github.asyncronous.toast.Toaster;

import java.awt.Image;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.InputMap;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.text.DefaultEditorKit;

public class App {
    public static final ExecutorService TASKPOOL = Executors.newFixedThreadPool(2);
    public static final Toaster TOASTER = Toaster.instance();

    public static TrayMenu TRAY_MENU = new TrayMenu();

    public static boolean wasUpdated = false;
    public static boolean experimentalJson = false;
    public static boolean useGzipForDownloads = true;

    public static Settings settings;

    public static Theme THEME = Theme.DEFAULT_THEME;

    static {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionStrainer());
    }

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
                } else if (parts[0].equalsIgnoreCase("--json") && parts[1].equalsIgnoreCase("experimental")) {
                    experimentalJson = true;
                    LogManager.debug("Experimental JSON support enabled! Don't ask for support with this enabled!",
                            true);
                } else if (parts[0].equalsIgnoreCase("--debug")) {
                    LogManager.showDebug = true;
                    LogManager.debug("Debug logging is enabled! Please note that this will remove any censoring of "
                            + "user data!");
                } else if (parts[0].equalsIgnoreCase("--usegzip") && parts[1].equalsIgnoreCase("false")) {
                    useGzipForDownloads = false;
                    LogManager.debug("GZip has been turned off for downloads!  Don't ask for support with this " +
                            "disabled!", true);
                }
            }
        }

        File config = new File(Utils.getCoreGracefully(), "Configs");
        if (!config.exists()){
            int files = config.getParentFile().list().length;
            if(files > 1){
                String[] options = {
                        "Yes It's Fine",
                        "Whoops. I'll Change That Now"
                };
                int ret = JOptionPane.showOptionDialog(null, "<html><p align=\"center\">I've detected that you may " +
                                "not have installed this " + "in the right location.<br/><br/>The exe or jar file" + "should " +
                                "be placed in it's own folder with nothing else " + "in it<br/><br/>Are you 100% sure that's " +
                                "not have installed this " + "in the right location.<br/><br/>The exe or jar file" +
                                "should " +
                                "be placed in it's own folder with nothing else " + "in it<br/><br/>Are you 100% sure" +
                                " that's " +
                                "what you've" + "done?</p></html>", "Warning", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if(ret != 0){
                    System.exit(0);
                }
            }
        }
        settings = new Settings(); // Setup the Settings and wait for it to finish
        final SplashScreen ss = new SplashScreen();
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                ss.setVisible(true);
            }
        });
        loadTheme();
        settings.loadConsole(); // Load console AFTER L&F

        if (settings.enableTrayIcon()) {
            try {
                trySystemTrayIntegration(); // Try to enable the tray icon
            } catch (Exception e) {
                settings.logStackTrace(e);
            }
        }

        LogManager.info("ATLauncher Version: " + Constants.VERSION);
        LogManager.info("Operating System: " + System.getProperty("os.name"));
        LogManager.info("RAM Available: " + Utils.getMaximumRam() + "MB");
        if (settings.isUsingCustomJavaPath()) {
            LogManager.warn("Custom Java Path Set!");
        } else if (settings.isUsingMacApp()) {
            File oracleJava = new File("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home/bin/java");
            if (oracleJava.exists() && oracleJava.canExecute()) {
                settings.setJavaPath("/Library/Internet Plug-Ins/JavaAppletPlugin.plugin/Contents/Home");
                LogManager.warn("Launcher Forced Custom Java Path Set!");
            }
        }
        LogManager.info("Java Version: " + Utils.getActualJavaVersion());
        LogManager.info("Java Path: " + settings.getJavaPath());
        LogManager.info("64 Bit Java: " + Utils.is64Bit());
        LogManager.info("Launcher Directory: " + settings.getBaseDir());
        LogManager.info("Using Theme: " + THEME);

        if (Utils.isMac()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "ATLauncher " + Constants.VERSION);
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

        if (autoLaunch != null && settings.isInstanceBySafeName(autoLaunch)) {
            Instance instance = settings.getInstanceBySafeName(autoLaunch);
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
        new LauncherFrame(open); // Open the Launcher
    }

    public static void loadTheme() {
        File themeFile = settings.getThemeFile();
        if (themeFile != null) {
            try {
                THEME = Settings.themeGson.fromJson(new FileReader(themeFile), Theme.class);
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

    private static void setLAF() throws Exception {
        for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getName().equalsIgnoreCase("nimbus")) {
                UIManager.setLookAndFeel(info.getClassName());
            }
        }
    }

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
            trayIcon.setToolTip("ATLauncher");
            trayIcon.setImageAutoSize(true);

            tray.add(trayIcon);
        }
    }

    public static void integrate() {
        try {
            File f = new File(new File(System.getProperty("user.home")), ".atl.properties");
            if (!f.exists()) {
                f.createNewFile();
            }
            Properties props = new Properties();
            props.load(new FileInputStream(f));
            props.setProperty("atl_loc", App.settings.getBaseDir().toString());
            props.store(new FileOutputStream(f), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
