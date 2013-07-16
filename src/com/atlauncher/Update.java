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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.atlauncher.gui.Utils;

public class Update {

    public static void main(String[] args) {
        String launcherPath = args[0];
        String temporaryUpdatePath = args[1];
        File launcher = new File(launcherPath);
        File temporaryUpdate = new File(temporaryUpdatePath);
        Utils.copyFile(temporaryUpdate, launcher.getParentFile());

        List<String> arguments = new ArrayList<String>();

        if (Utils.isMac()
                && new File(new File(System.getProperty("user.dir")).getParentFile()
                        .getParentFile(), "MacOS").exists()) {
            arguments.add("open");
            arguments.add(new File(System.getProperty("user.dir")).getParentFile().getParentFile()
                    .getParentFile().getAbsolutePath());
            
        } else {
            String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                    + "java";
            if (Utils.isWindows()) {
                path += "w";
            }
            arguments.add(path);
            arguments.add("-jar");
            arguments.add(launcherPath);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}