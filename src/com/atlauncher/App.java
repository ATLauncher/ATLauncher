/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher;

import java.awt.Image;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import javax.swing.JOptionPane;

import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;
import com.atlauncher.gui.Utils;

public class App {

    public static Settings settings;

    public static void main(String[] args) {
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

        if (App.settings.enableConsole()) {
            App.settings.getConsole().setVisible(true);
        }

        settings.getConsole().log("Showing splash screen and loading everything");
        SplashScreen ss = new SplashScreen(); // Show Splash Screen
        settings.loadEverything(); // Loads everything that needs to be loaded
        ss.close(); // Close the Splash Screen
        settings.getConsole().log("Launcher finished loading everything");

        if (settings.isFirstTimeRun()) {
            settings.getConsole().log("Launcher not setup. Loading Setup Dialog");
            new SetupDialog(settings);
        }

        // if (autoLaunch != null) {
        // if (settings.isInstanceByName(autoLaunch)) {
        // Instance instance = settings.getInstanceByName(autoLaunch);
        // instance.launch();
        // System.exit();
        // }
        // }
        settings.getConsole().log("Launcher opening");
        settings.getConsole().log("Made By Bob*");
        settings.getConsole().log("*(Not Actually)");
        new LauncherFrame(); // Open the Launcher
    }

}
