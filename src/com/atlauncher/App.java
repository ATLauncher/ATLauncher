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

import com.atlauncher.data.Settings;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SetupDialog;
import com.atlauncher.gui.SplashScreen;

public class App {

    public static void main(String[] args) {
        // TODO Add in arguments for some stuff and handle relaunching of the
        // client under certain situations such as updates
        
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
