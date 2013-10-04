/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.mclauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
        if (jarMods.exists() && (instance.hasJarMods() || jarMods.listFiles().length != 0)) {
            if (instance.hasJarMods()) {
                ArrayList<String> jarmods = new ArrayList<String>(Arrays.asList(instance
                        .getJarOrder().split(",")));
                for (String mod : jarmods) {
                    File thisFile = new File(jarMods, mod);
                    if (thisFile.exists()) {
                        cpb.append(File.pathSeparator);
                        cpb.append(thisFile);
                    }
                }
                for (File file : jarMods.listFiles()) {
                    if (jarmods.contains(file.getName())) {
                        continue;
                    }
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
            } else {
                for (File file : jarMods.listFiles()) {
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
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

        if (App.settings.getMemory() < instance.getMemory()) {
            if (Utils.getMaximumRam() < instance.getMemory()) {
                arguments.add("-Xmx" + App.settings.getMemory() + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + App.settings.getMemory() + "M");
        }
        if (App.settings.getPermGen() < instance.getPermGen()) {
            arguments.add("-XX:MaxPermSize=" + instance.getPermGen() + "M");
        } else {
            arguments.add("-XX:MaxPermSize=" + App.settings.getPermGen() + "M");
        }

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                arguments.add(arg);
            }
        }

        arguments.add("-Dfml.ignorePatchDiscrepancies=true");
        arguments.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
        
        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (Utils.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:icon="
                    + new File(App.settings.getImagesDir(), "NewMinecraftIcon.png")
                            .getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");
        }

        arguments.add("-Djava.library.path=" + instance.getNativesDirectory().getAbsolutePath());
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path") + cpb.toString());

        arguments.add(instance.getMainClass());
        arguments.add("--username=" + account.getMinecraftUsername());
        arguments.add("--session=" + session);
        arguments.add("--version=" + instance.getMinecraftVersion());
        arguments.add("--gameDir=" + instance.getRootDirectory());
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
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }

}