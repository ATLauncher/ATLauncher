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
import java.util.HashMap;
import java.util.List;

import com.atlauncher.App;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;

public class MCLauncher {

    public static Process launch(Account account, Instance instance, AuthenticationResponse response)
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

        String path = App.settings.getJavaPath() + File.separator + "bin" + File.separator + "java";
        if (Utils.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        if (Utils.isWindows()) {
            arguments
                    .add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

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
            arguments.add("-XX:PermSize=" + instance.getPermGen() + "M");
        } else {
            arguments.add("-XX:PermSize=" + App.settings.getPermGen() + "M");
        }

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                if (!arg.isEmpty()) {
                    arguments.add(arg);
                }
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
        if (instance.hasMinecraftArguments()) {
            String[] minecraftArguments = instance.getMinecraftArguments().split(" ");
            for (String argument : minecraftArguments) {
                argument = argument.replace("${auth_player_name}", response.getSelectedProfile()
                        .getName());
                argument = argument.replace("${profile_name}", instance.getName());
                argument = argument.replace("${user_properties}", new Gson().toJson((response
                        .getUser() == null ? new HashMap() : response.getProperties())));
                argument = argument.replace("${version_name}", instance.getMinecraftVersion());
                argument = argument.replace("${game_directory}", instance.getRootDirectory()
                        .getAbsolutePath());
                argument = argument.replace("${game_assets}", instance.getAssetsDir()
                        .getAbsolutePath());
                argument = argument.replace("${assets_root}", App.settings.getResourcesDir()
                        .getAbsolutePath());
                argument = argument.replace("${assets_index_name}", instance.getAssets());
                argument = argument.replace("${auth_uuid}", response.getSelectedProfile().getId());
                argument = argument.replace("${auth_access_token}", response.getAccessToken());
                argument = argument.replace("${auth_session}", response.getSession());
                arguments.add(argument);
            }
        } else {
            arguments.add("--username=" + response.getSelectedProfile().getName());
            arguments.add("--session=" + response.getSession());

            // This is for 1.7
            arguments.add("--accessToken=" + response.getAccessToken());
            arguments.add("--uuid=" + response.getSelectedProfile().getId());
            // End of stuff for 1.7

            arguments.add("--version=" + instance.getMinecraftVersion());
            arguments.add("--gameDir=" + instance.getRootDirectory().getAbsolutePath());
            arguments.add("--assetsDir=" + App.settings.getResourcesDir().getAbsolutePath());
        }
        if (App.settings.startMinecraftMaximised()) {
            arguments.add("--width=" + Utils.getMaximumWindowWidth());
            arguments.add("--height=" + Utils.getMaximumWindowHeight());
        } else {
            arguments.add("--width=" + App.settings.getWindowWidth());
            arguments.add("--height=" + App.settings.getWindowHeight());
        }
        if (instance.hasExtraArguments()) {
            String args = instance.getExtraArguments();
            if (args.contains(" ")) {
                for (String arg : args.split(" ")) {
                    arguments.add(arg);
                }
            } else {
                arguments.add(args);
            }
        }

        App.settings.log("Launching Minecraft with the following arguments: " + arguments);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        return processBuilder.start();
    }
}