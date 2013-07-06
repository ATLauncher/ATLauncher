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
package com.atlauncher.mclauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.gui.Utils;

public class NewMCLauncher {

    public static Process launch(Account account, Instance instance, String session)
            throws IOException {
        StringBuilder cpb = new StringBuilder("");

        File jarMods = instance.getJarModsDirectory();
        if (jarMods.exists() && instance.hasJarMods()) {
            for (String mod : instance.getJarOrder().split(",")) {
                cpb.append(File.pathSeparator);
                cpb.append(new File(jarMods, mod));
            }
        }

        for (String jarFile : instance.getLibrariesNeeded().split(",")) {
            cpb.append(File.pathSeparator);
            cpb.append(new File(instance.getBinDirectory(), jarFile));
        }

        cpb.append(File.pathSeparator);
        cpb.append(instance.getMinecraftJar());

        List<String> arguments = new ArrayList<String>();

        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator
                + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        arguments.add("-Xms256M");
        arguments.add("-Xmx" + App.settings.getMemory() + "M");
        arguments.add("-XX:MaxPermSize=" + App.settings.getPermGen() + "M");

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                arguments.add(arg);
            }
        }

        arguments.add("-Djava.library.path=" + instance.getNativesDirectory().getAbsolutePath());
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path") + cpb.toString());

        arguments.add(instance.getMainClass());
        arguments.add("--username=" + account.getMinecraftUsername());
        arguments.add("--session=" + session);
        arguments.add("--version=" + instance.getMinecraftVersion());
        arguments.add("--gameDir=" + instance.getMinecraftDirectory());
        arguments.add("--assetsDir=" + App.settings.getResourcesDir());
        arguments.add("--width=" + App.settings.getWindowWidth());
        arguments.add("--height=" + App.settings.getWindowHeight());
        if (instance.hasMinecraftArguments()) {
            String args = instance.getMinecraftArguments();
            if (args.contains(" ")) {
                for (String arg : args.split(" ")) {
                    arguments.add(arg);
                }
            } else {
                arguments.add(args);
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

}