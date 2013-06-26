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

import java.io.File;

import javax.swing.JOptionPane;

import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;

public class App {

    public static void main(String[] args) {
        File config = new File(System.getProperty("user.dir"), "ATLauncher.conf");
        if (!config.exists()) {
            int files = config.getParentFile().list().length;
            if (files != 1) {
                String[] options = { "Yes It's Fine", "Whoops. I'll Change That Now" };
                int ret = JOptionPane.showOptionDialog(null,
                        "<html><center>I've detected that you may not have installed this "
                                + "in the right location.<br/><br/>The exe or jar file"
                                + "should be placed in it's own folder with nothing else"
                                + "in it<br/><br/>Are you 100% sure that's what you've"
                                + "done?</center></html>", "Warning", JOptionPane.DEFAULT_OPTION,
                        JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                if (ret != 0) {
                    System.exit(0);
                }
            }
        }

        Settings settings = new Settings(); // Setup the Settings and wait for it to finish

        settings.getConsole().log("Launcher started. Loading everything and showing splash screen");
        SplashScreen ss = new SplashScreen(); // Show Splash Screen
        settings.loadEverything(); // Loads everything that needs to be loaded
        ss.close(); // Close the Splash Screen
        settings.getConsole().log("Launcher finished loading everything");

        if (settings.isFirstTimeRun()) {
            settings.getConsole().log("Launcher not setup. Loading Setup Dialog");
            new SetupDialog(settings);
        }

        settings.getConsole().log("Launcher opening");
        new LauncherFrame(settings); // Open the Launcher
    }

}
