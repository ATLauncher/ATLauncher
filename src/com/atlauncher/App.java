package com.atlauncher;

import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.SplashScreen;

public class App {

    public static void main(String[] args) {
        // TODO Add in arguments for some stuff and handle relaunching of the
        // client under certain situations such as updates

        SplashScreen ss = new SplashScreen(); // Show Splash Screen
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ss.close(); // Close the Splash Screen

        new LauncherFrame(); // Open the Launcher
    }

}
