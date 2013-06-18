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
