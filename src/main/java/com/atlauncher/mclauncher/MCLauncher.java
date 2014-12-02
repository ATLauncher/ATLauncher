/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.mclauncher;

import com.atlauncher.App;
import com.atlauncher.Gsons;
import com.atlauncher.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.mojang.auth.AuthenticationResponse;
import com.atlauncher.data.mojang.auth.UserType;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MCLauncher {

    public static Process launch(Account account, Instance instance, LoginResponse response) throws
            IOException {
        StringBuilder cpb = new StringBuilder();
        boolean hasCustomJarMods = false;

        File jarMods = instance.getJarModsDirectory();
        File[] jarModFiles = jarMods.listFiles();
        if (jarMods.exists() && jarModFiles != null && (instance.hasJarMods() || jarModFiles.length != 0)) {
            if (instance.hasJarMods()) {
                ArrayList<String> jarmods = new ArrayList<String>(Arrays.asList(instance.getJarOrder().split(",")));
                if (jarmods.size() > 1) {
                    hasCustomJarMods = true;
                }
                for (String mod : jarmods) {
                    File thisFile = new File(jarMods, mod);
                    if (thisFile.exists()) {
                        cpb.append(File.pathSeparator);
                        cpb.append(thisFile);
                    }
                }
                for (File file : jarModFiles) {
                    if (jarmods.contains(file.getName())) {
                        continue;
                    }
                    hasCustomJarMods = true;
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
            } else {
                for (File file : jarModFiles) {
                    hasCustomJarMods = true;
                    cpb.append(File.pathSeparator);
                    cpb.append(file);
                }
            }
        }

        for (String jarFile : instance.getLibrariesNeeded().split(",")) {
            cpb.append(File.pathSeparator);
            cpb.append(new File(instance.getBinDirectory(), jarFile));
        }

        File binFolder = instance.getBinDirectory();
        File[] libraryFiles = binFolder.listFiles();
        if (binFolder.exists() && libraryFiles != null && libraryFiles.length != 0) {
            for (File file : libraryFiles) {
                if (file.isDirectory() || file.getName().equalsIgnoreCase(instance.getMinecraftJar().getName()) ||
                        instance.getLibrariesNeeded().contains(file.getName())) {
                    continue;
                }

                LogManager.info("Added in custom library " + file.getName());

                cpb.append(File.pathSeparator);
                cpb.append(file);
            }
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
            arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

        arguments.add("-XX:-OmitStackTraceInFastThrow");

        if (App.settings.getJavaParameters().isEmpty()) {
            // Mojang launcher defaults if user has no custom java arguments
            arguments.add("-XX:+UseConcMarkSweepGC");
            arguments.add("-XX:+CMSIncrementalMode");
            arguments.add("-XX:-UseAdaptiveSizePolicy");
        }

        arguments.add("-Xms" + App.settings.getInitialMemory() + "M");

        if (App.settings.getMaximumMemory() < instance.getMemory()) {
            if ((Utils.getMaximumRam() / 2) < instance.getMemory()) {
                arguments.add("-Xmx" + App.settings.getMaximumMemory() + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + App.settings.getMaximumMemory() + "M");
        }
        if (App.settings.getPermGen() < instance.getPermGen() && (Utils.getMaximumRam() / 8) < instance.getPermGen()) {
            if (Utils.isJava8()) {
                arguments.add("-XX:MetaspaceSize=" + instance.getPermGen() + "M");
            } else {
                arguments.add("-XX:PermSize=" + instance.getPermGen() + "M");
            }
        } else {
            if (Utils.isJava8()) {
                arguments.add("-XX:MetaspaceSize=" + App.settings.getPermGen() + "M");
            } else {
                arguments.add("-XX:PermSize=" + App.settings.getPermGen() + "M");
            }
        }

        arguments.add("-Duser.language=en");
        arguments.add("-Duser.country=US");

        if (hasCustomJarMods) {
            System.out.println("OH NOES! Avert your eyes!");
            arguments.add("-Dfml.ignorePatchDiscrepancies=true");
            arguments.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
            System.out.println("Okay you can look again, you saw NOTHING!");
        }

        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (Utils.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:icon=" + new File(instance.getAssetsDir(), "icons/minecraft.icns").getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");
        }

        if (!App.settings.getJavaParameters().isEmpty()) {
            for (String arg : App.settings.getJavaParameters().split(" ")) {
                if (!arg.isEmpty()) {
                    if (instance.hasExtraArguments()) {
                        if (instance.getExtraArguments().contains(arg)) {
                            LogManager.error("Duplicate argument " + arg + " found and not added!");
                            continue;
                        }
                    }

                    if (arguments.toString().contains(arg)) {
                        LogManager.error("Duplicate argument " + arg + " found and not added!");
                        continue;
                    }

                    arguments.add(arg);
                }
            }
        }

        arguments.add("-Djava.library.path=" + instance.getNativesDirectory().getAbsolutePath());
        arguments.add("-cp");
        arguments.add(System.getProperty("java.class.path") + cpb.toString());
        arguments.add(instance.getMainClass());

        String props = "";
        if(!response.isOffline()) {
            props = new Gson().toJson(response.getAuth().getUserProperties());
        }

        if (instance.hasMinecraftArguments()) {
            String[] minecraftArguments = instance.getMinecraftArguments().split(" ");
            for (String argument : minecraftArguments) {
                argument = argument.replace("${auth_player_name}", account.getMinecraftUsername());
                argument = argument.replace("${profile_name}", instance.getName());
                argument = argument.replace("${user_properties}", props);
                argument = argument.replace("${version_name}", instance.getMinecraftVersion());
                argument = argument.replace("${game_directory}", instance.getRootDirectory().getAbsolutePath());
                argument = argument.replace("${game_assets}", instance.getAssetsDir().getAbsolutePath());
                argument = argument.replace("${assets_root}", App.settings.getResourcesDir().getAbsolutePath());
                argument = argument.replace("${assets_index_name}", instance.getAssets());
                argument = argument.replace("${auth_uuid}", account.getUUID());
                argument = argument.replace("${auth_access_token}", account.getAccessToken());
                argument = argument.replace("${auth_session}", account.getSession(response));
                argument = argument.replace("${user_type}", response.isOffline() ? com.mojang.authlib.UserType
                        .MOJANG.getName() : response.getAuth().getUserType().getName());
                arguments.add(argument);
            }
        } else {
            arguments.add("--username=" + account.getMinecraftUsername());
            arguments.add("--session=" + account.getSession(response));

            // This is for 1.7
            arguments.add("--accessToken=" + account.getAccessToken());
            arguments.add("--uuid=" + account.getUUID());
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
                Collections.addAll(arguments, args.split(" "));
            } else {
                arguments.add(args);
            }
        }

        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            argsString = argsString.replace(account.getMinecraftUsername(), "REDACTED");
            argsString = argsString.replace(account.getUUID(), "REDACTED");
            argsString = argsString.replace(account.getAccessToken(), "REDACTED");
            argsString = argsString.replace(account.getSession(response), "REDACTED");
            argsString = argsString.replace(props, "REDACTED");
        }

        LogManager.info("Launching Minecraft with the following arguments " + "(user related stuff has been removed):" +
                " " + argsString);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }
}