/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2021 ATLauncher
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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Instance;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.PropertyMapSerializer;
import com.atlauncher.managers.LogManager;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

public class MCLauncher {
    public static final List<String> IGNORED_ARGUMENTS = new ArrayList<String>() {
        {
            // these seem to be tracking/telemetry things
            add("--clientId");
            add("${clientid}");
            add("--xuid");
            add("${auth_xuid}");
        }
    };

    public static Process launch(MicrosoftAccount account, Instance instance, Path nativesTempDir, String username)
            throws Exception {
        return launch(account, instance, null, nativesTempDir.toFile(), username);
    }

    public static Process launch(MojangAccount account, Instance instance, LoginResponse response, Path nativesTempDir,
            String username) throws Exception {
        String props = "[]";

        if (!response.isOffline()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer()).create();
            props = gson.toJson(response.getAuth().getUserProperties());
        }

        return launch(account, instance, props, nativesTempDir.toFile(), username);
    }

    private static Process launch(AbstractAccount account, Instance instance, String props, File nativesDir,
            String username) throws Exception {
        List<String> arguments = getArguments(account, instance, props, nativesDir.getAbsolutePath(), username);

        LogManager.info("Launching Minecraft with the following arguments (user related stuff has been removed): "
                + censorArguments(arguments, account, props, username));
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }

    private static List<String> getArguments(AbstractAccount account, Instance instance, String props,
            String nativesDir, String username) {
        StringBuilder cpb = new StringBuilder();
        boolean hasCustomJarMods = false;

        ErrorReporting.recordInstancePlay(instance.getPackName(), instance.getVersion(), instance.getLoaderVersion(),
                2);

        int initialMemory = Optional.ofNullable(instance.launcher.initialMemory).orElse(App.settings.initialMemory);
        int maximumMemory = Optional.ofNullable(instance.launcher.maximumMemory).orElse(App.settings.maximumMemory);
        int permGen = Optional.ofNullable(instance.launcher.permGen).orElse(App.settings.metaspace);
        String javaPath = Optional.ofNullable(instance.launcher.javaPath).orElse(App.settings.javaPath);
        String javaArguments = Optional.ofNullable(instance.launcher.javaArguments).orElse(App.settings.javaParameters);

        // are we using Mojangs provided runtime?
        if (instance.javaVersion != null && Optional.ofNullable(instance.launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft)) {
            Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(instance.javaVersion.component)
                    .resolve(JavaRuntimes.getSystem()).resolve(instance.javaVersion.component);

            if (OS.isMac()) {
                runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
            }

            if (Files.isDirectory(runtimeDirectory)) {
                javaPath = runtimeDirectory.toAbsolutePath().toString();
                LogManager.debug(String.format("Using Java runtime %s (major version %n) at path %s",
                        instance.javaVersion.component, instance.javaVersion.majorVersion, javaPath));
            }
        }

        File jarMods = instance.getJarModsDirectory();
        File[] jarModFiles = jarMods.listFiles();
        if (jarMods.exists() && jarModFiles != null && jarModFiles.length != 0) {
            for (File file : jarModFiles) {
                hasCustomJarMods = true;
                cpb.append(file.getAbsolutePath());
                cpb.append(File.pathSeparator);
            }
        }

        instance.libraries.stream().filter(
                library -> library.shouldInstall() && library.downloads.artifact != null && !library.hasNativeForOS())
                .filter(library -> library.downloads.artifact != null && library.downloads.artifact.path != null)
                .forEach(library -> {
                    cpb.append(
                            FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile().getAbsolutePath());
                    cpb.append(File.pathSeparator);
                });

        instance.libraries.stream().filter(Library::hasNativeForOS).forEach(library -> {
            com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

            cpb.append(FileSystem.LIBRARIES.resolve(download.path).toFile().getAbsolutePath());
            cpb.append(File.pathSeparator);
        });

        File binFolder = instance.getBinDirectory();
        File[] libraryFiles = binFolder.listFiles();
        if (binFolder.exists() && libraryFiles != null && libraryFiles.length != 0) {
            for (File file : libraryFiles) {
                LogManager.info("Added in custom library " + file.getName());

                cpb.append(file);
                cpb.append(File.pathSeparator);
            }
        }

        // add minecraft client jar last
        cpb.append(instance.getMinecraftJar().getAbsolutePath());

        List<String> arguments = new ArrayList<>();

        if (OS.isLinux() && App.settings.enableFeralGamemode && Utils.executableInPath("gamemoderun")) {
            arguments.add("gamemoderun");
        }

        String path = javaPath + File.separator + "bin" + File.separator + "java";
        if (OS.isWindows() && (Files.exists(Paths.get(path + "w")) || Files.exists(Paths.get(path + "w.exe")))) {
            path += "w";
        }
        arguments.add(path);

        arguments.add("-XX:-OmitStackTraceInFastThrow");

        arguments.add("-Xms" + initialMemory + "M");

        if (OS.getMaximumRam() != 0 && maximumMemory < instance.getMemory()) {
            if ((OS.getMaximumRam() / 2) < instance.getMemory()) {
                arguments.add("-Xmx" + maximumMemory + "M");
            } else {
                arguments.add("-Xmx" + instance.getMemory() + "M");
            }
        } else {
            arguments.add("-Xmx" + maximumMemory + "M");
        }

        if (OS.getMaximumRam() != 0 && permGen < instance.getPermGen()
                && (OS.getMaximumRam() / 8) < instance.getPermGen()) {
            if (Java.useMetaspace()) {
                arguments.add("-XX:MetaspaceSize=" + instance.getPermGen() + "M");
            } else {
                arguments.add("-XX:PermSize=" + instance.getPermGen() + "M");
            }
        } else {
            if (Java.useMetaspace()) {
                arguments.add("-XX:MetaspaceSize=" + permGen + "M");
            } else {
                arguments.add("-XX:PermSize=" + permGen + "M");
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

        arguments.add("-Dfml.log.level=" + App.settings.forgeLoggingLevel);

        if (OS.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");

            if (new File(instance.getAssetsDir(), "icons/minecraft.icns").exists()) {
                arguments.add(
                        "-Xdock:icon=" + new File(instance.getAssetsDir(), "icons/minecraft.icns").getAbsolutePath());
            }
        }

        if (!javaArguments.isEmpty()) {
            for (String arg : javaArguments.split(" ")) {
                if (!arg.isEmpty()) {
                    if (arguments.toString().contains(arg)) {
                        LogManager.error("Duplicate argument " + arg + " found and not added!");
                        continue;
                    }

                    arguments.add(arg);
                }
            }
        }

        String classpath = cpb.toString();

        for (String argument : instance.arguments.jvmAsStringList()) {
            if (IGNORED_ARGUMENTS.contains(argument)) {
                continue;
            }

            arguments.add(replaceArgument(argument, instance, account, props, nativesDir, classpath, username));
        }

        if (OS.isWindows() && !arguments
                .contains("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump")) {
            arguments.add("-XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump");
        }

        // if there's no -Djava.library.path already, then add it (for older versions)
        if (!arguments.stream().anyMatch(arg -> arg.startsWith("-Djava.library.path="))) {
            arguments.add("-Djava.library.path=" + nativesDir);
        }

        // if there's no classpath already, then add it (for older versions)
        if (!arguments.contains("-cp")) {
            arguments.add("-cp");
            arguments.add(cpb.toString());
        }

        arguments.add(instance.getMainClass());

        for (String argument : instance.arguments.gameAsStringList()) {
            if (IGNORED_ARGUMENTS.contains(argument)) {
                continue;
            }

            arguments.add(replaceArgument(argument, instance, account, props, nativesDir, classpath, username));
        }

        if (App.settings.maximiseMinecraft) {
            arguments.add("--width=" + OS.getMaximumWindowWidth());
            arguments.add("--height=" + OS.getMaximumWindowHeight());
        } else {
            arguments.add("--width=" + App.settings.windowWidth);
            arguments.add("--height=" + App.settings.windowHeight);
        }

        return arguments;
    }

    private static String replaceArgument(String incomingArgument, Instance instance, AbstractAccount account,
            String props, String nativesDir, String classpath, String username) {
        String argument = incomingArgument;

        argument = argument.replace("${auth_player_name}", username);
        argument = argument.replace("${profile_name}", instance.getName());
        argument = argument.replace("${user_properties}", Optional.ofNullable(props).orElse("[]"));
        argument = argument.replace("${version_name}", instance.getMinecraftVersion());
        argument = argument.replace("${game_directory}", instance.getRootDirectory().getAbsolutePath());
        argument = argument.replace("${game_assets}", instance.getAssetsDir().getAbsolutePath());
        argument = argument.replace("${assets_root}", FileSystem.ASSETS.toAbsolutePath().toString());
        argument = argument.replace("${assets_index_name}", instance.getAssets());
        argument = argument.replace("${auth_uuid}", UUIDTypeAdapter.fromUUID(account.getRealUUID()));
        argument = argument.replace("${auth_access_token}", account.getAccessToken());
        argument = argument.replace("${version_type}", instance.type.getValue());
        argument = argument.replace("${launcher_name}", Constants.LAUNCHER_NAME);
        argument = argument.replace("${launcher_version}", Constants.VERSION.toStringForLogging());
        argument = argument.replace("${natives_directory}", nativesDir);
        argument = argument.replace("${user_type}", account.getUserType());
        argument = argument.replace("${auth_session}", account.getSessionToken());
        argument = argument.replace("${library_directory}", FileSystem.LIBRARIES.toAbsolutePath().toString());
        argument = argument.replace("${classpath}", classpath);
        argument = argument.replace("${classpath_separator}", File.pathSeparator);

        return argument;
    }

    private static String censorArguments(List<String> arguments, AbstractAccount account, String props,
            String username) {
        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            if (App.settings != null) {
                argsString = argsString.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "USERSDIR");
            }

            argsString = argsString.replace(username, "REDACTED");
            argsString = argsString.replace(account.uuid, "REDACTED");
        }

        if (props != null) {
            argsString = argsString.replace(props, "REDACTED");
        }
        argsString = argsString.replace(account.getAccessToken(), "REDACTED");
        argsString = argsString.replace(account.getSessionToken(), "REDACTED");

        return argsString;
    }
}
