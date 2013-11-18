/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher;

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import com.atlauncher.data.Instance;
import com.atlauncher.data.LogMessageType;
import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.utils.Utils;

public class App {

    public static Settings settings;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static void main(String[] args) {
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

        settings = new Settings(); // Setup the Settings and wait for it to finish

        settings.log("ATLauncher Version: " + settings.getVersion());
        settings.log("Operating System: " + System.getProperty("os.name"));
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
                setDockIconImage.invoke(application, Utils.getImage("/resources/Icon.png"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        if (settings.enableConsole()) {
            settings.setConsoleVisible(true);
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
                instance.launch();
                open = false;
            }
        }

        new LauncherFrame(open); // Open the Launcher
    }
}