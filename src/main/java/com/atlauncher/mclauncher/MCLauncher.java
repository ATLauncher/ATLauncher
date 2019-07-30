/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2019 ATLauncher
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
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.data.Account;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceSettings;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.PropertyMapSerializer;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.network.ErrorReporting;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.OS;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.util.UUIDTypeAdapter;

public class MCLauncher {

    public static Process launch(Account account, Instance instance, LoginResponse response) throws IOException {
        StringBuilder cpb = new StringBuilder();
        boolean hasCustomJarMods = false;

        ErrorReporting.recordInstancePlay(instance.getPackName(), instance.getVersion(), instance.getLoaderVersion(),
                1);

        InstanceSettings settings = instance.getSettings();
        Integer initialMemory = settings.getInitialMemory() == null ? App.settings.getInitialMemory()
                : settings.getInitialMemory();
        Integer maximumMemory = settings.getMaximumMemory() == null ? App.settings.getMaximumMemory()
                : settings.getMaximumMemory();
        Integer permGen = settings.getPermGen() == null ? App.settings.getPermGen() : settings.getPermGen();
        String javaPath = settings.getJavaPath() == null ? App.settings.getJavaPath() : settings.getJavaPath();
        String javaArguments = settings.getJavaArguments() == null ? App.settings.getJavaParameters()
                : settings.getJavaArguments();

        File jarMods = instance.getJarModsDirectory();
        File[] jarModFiles = jarMods.listFiles();
        if (jarMods.exists() && jarModFiles != null && jarModFiles.length != 0) {
            for (File file : jarModFiles) {
                hasCustomJarMods = true;
                cpb.append(File.pathSeparator);
                cpb.append(file.getAbsolutePath());
            }
        }

        File librariesBaseDir = instance.usesNewLibraries() ? FileSystem.LIBRARIES.toFile()
                : instance.getBinDirectory();

        for (String jarFile : instance.getLibraries()) {
            cpb.append(File.pathSeparator);
            cpb.append(new File(librariesBaseDir, jarFile).getAbsolutePath());
        }

        File binFolder = instance.getBinDirectory();
        File[] libraryFiles = binFolder.listFiles();
        if (binFolder.exists() && libraryFiles != null && libraryFiles.length != 0) {
            for (File file : libraryFiles) {
                if (file.isDirectory() || file.getName().equalsIgnoreCase(instance.getMinecraftJar().getName())
                        || instance.getLibraries().contains(file.getName())) {
                    continue;
                }

                LogManager.info("Added in custom library " + file.getName());

                cpb.append(File.pathSeparator);
                cpb.append(file);
            }
        }

        if (!instance.usesNewLibraries()) {
            cpb.append(File.pathSeparator);
            cpb.append(instance.getMinecraftJar().getAbsolutePath());
        }

        List<String> arguments = new ArrayList<>();

        String path = javaPath + File.separator + "bin" + File.separator + "java";
        if (OS.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        arguments.add("-XX:-OmitStackTraceInFastThrow");

        if (javaArguments.isEmpty() && !Java.isMinecraftJavaNewerThanJava8()) {
            // Some defaults if on Java 8 or less
            javaArguments = "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy";
        }

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

        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (OS.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:icon=" + new File(instance.getAssetsDir(), "icons/minecraft.icns").getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");
        }

        ArrayList<String> negatedArgs = new ArrayList<>();

        if (!javaArguments.isEmpty()) {
            for (String arg : javaArguments.split(" ")) {
                if (!arg.isEmpty()) {
                    if (instance.hasExtraArguments()) {
                        if (instance.getExtraArguments().contains(arg)) {
                            LogManager.error("Duplicate argument " + arg + " found and not added!");
                            continue;
                        }

                        if (arg.startsWith("-XX:+")) {
                            if (instance.getExtraArguments().contains("-XX:-" + arg.substring(5))) {
                                negatedArgs.add("-XX:-" + arg.substring(5));
                                LogManager.error("Argument " + arg + " is negated by pack developer and not added!");
                                continue;
                            }
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
        arguments.add(cpb.toString());
        arguments.add(instance.getMainClass());

        String props = "[]";

        if (!response.isOffline()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer()).create();
            props = gson.toJson(response.getAuth().getUserProperties());
        }

        List<String> launchArguments = new ArrayList<>();

        if (instance.hasArguments()) {
            launchArguments.addAll(instance.getArguments());
        } else if (instance.hasMinecraftArguments()) {
            launchArguments = Arrays.asList(instance.getMinecraftArguments().split(" "));
        } else {
            VersionManifest versionManifest = com.atlauncher.network.Download.build().cached()
                    .setUrl(String.format("%s/mc/game/version_manifest.json", Constants.LAUNCHER_META_MINECRAFT))
                    .asClass(VersionManifest.class);

            VersionManifestVersion minecraftVersion = versionManifest.versions.stream()
                    .filter(version -> version.id.equalsIgnoreCase(instance.getMinecraftVersion())).findFirst()
                    .orElse(null);

            if (minecraftVersion != null) {
                MinecraftVersion version = com.atlauncher.network.Download.build().cached().setUrl(minecraftVersion.url)
                        .asClass(MinecraftVersion.class);

                if (version.arguments != null) {
                    launchArguments = version.arguments.asStringList();
                } else if (version.minecraftArguments != null) {
                    launchArguments = Arrays.asList(version.minecraftArguments.split(" "));
                }
            }
        }

        if (launchArguments.size() != 0) {
            for (String argument : launchArguments) {
                argument = argument.replace("${auth_player_name}", account.getMinecraftUsername());
                argument = argument.replace("${profile_name}", instance.getName());
                argument = argument.replace("${user_properties}", props);
                argument = argument.replace("${version_name}", instance.getMinecraftVersion());
                argument = argument.replace("${game_directory}", instance.getRootDirectory().getAbsolutePath());
                argument = argument.replace("${game_assets}", instance.getAssetsDir().getAbsolutePath());
                argument = argument.replace("${assets_root}", FileSystem.ASSETS.toAbsolutePath().toString());
                argument = argument.replace("${assets_index_name}", instance.getAssets());
                argument = argument.replace("${auth_uuid}", UUIDTypeAdapter.fromUUID(account.getRealUUID()));
                argument = argument.replace("${auth_access_token}", account.getAccessToken());
                argument = argument.replace("${auth_session}", account.getSession(response));
                argument = argument.replace("${version_type}", instance.getVersionType());
                argument = argument.replace("${launcher_name}", Constants.LAUNCHER_NAME);
                argument = argument.replace("${launcher_version}", Constants.VERSION.toString());
                argument = argument.replace("${natives_directory}", instance.getNativesDirectory().getAbsolutePath());
                argument = argument.replace("${user_type}",
                        response.isOffline() ? com.mojang.authlib.UserType.MOJANG.getName()
                                : response.getAuth().getUserType().getName());

                if (!argument.equalsIgnoreCase("-cp") && !argument.equalsIgnoreCase("${classpath}")) {
                    arguments.add(argument);
                }
            }
        } else {
            arguments.add("--username=" + account.getMinecraftUsername());
            arguments.add("--session=" + account.getSession(response));

            // This is for 1.7
            arguments.add("--accessToken=" + account.getAccessToken());
            arguments.add("--uuid=" + UUIDTypeAdapter.fromUUID(account.getRealUUID()));
            // End of stuff for 1.7

            arguments.add("--version=" + instance.getMinecraftVersion());
            arguments.add("--gameDir=" + instance.getRootDirectory().getAbsolutePath());
            arguments.add("--assetsDir=" + FileSystem.ASSETS.toAbsolutePath().toString());
        }

        if (App.settings.startMinecraftMaximised()) {
            arguments.add("--width=" + OS.getMaximumWindowWidth());
            arguments.add("--height=" + OS.getMaximumWindowHeight());
        } else {
            arguments.add("--width=" + App.settings.getWindowWidth());
            arguments.add("--height=" + App.settings.getWindowHeight());
        }

        if (instance.hasExtraArguments()) {
            String args = instance.getExtraArguments();
            if (args.contains(" ")) {
                for (String argument : args.split(" ")) {
                    if (!negatedArgs.contains(argument)) {
                        arguments.add(argument);
                    }
                }
            } else {
                if (!negatedArgs.contains(args)) {
                    arguments.add(args);
                }
            }
        }

        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            if (App.settings != null) {
                argsString = argsString.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "USERSDIR");
            }

            argsString = argsString.replace(account.getMinecraftUsername(), "REDACTED");
            argsString = argsString.replace(account.getUUID(), "REDACTED");
            argsString = argsString.replace(account.getAccessToken(), "REDACTED");
            argsString = argsString.replace(account.getSession(response), "REDACTED");
            argsString = argsString.replace(props, "REDACTED");
        }

        LogManager.info("Launching Minecraft with the following arguments " + "(user related stuff has been removed):"
                + " " + argsString);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }

    public static Process launch(Account account, InstanceV2 instance, LoginResponse response, Path nativesTempDir)
            throws IOException {
        StringBuilder cpb = new StringBuilder();
        boolean hasCustomJarMods = false;

        ErrorReporting.recordInstancePlay(instance.launcher.pack, instance.launcher.version,
                instance.launcher.loaderVersion, 2);

        Integer initialMemory = instance.launcher.initialMemory == null ? App.settings.getInitialMemory()
                : instance.launcher.initialMemory;
        Integer maximumMemory = instance.launcher.maximumMemory == null ? App.settings.getMaximumMemory()
                : instance.launcher.maximumMemory;
        Integer permGen = instance.launcher.permGen == null ? App.settings.getPermGen() : instance.launcher.permGen;
        String javaPath = instance.launcher.javaPath == null ? App.settings.getJavaPath() : instance.launcher.javaPath;
        String javaArguments = instance.launcher.javaArguments == null ? App.settings.getJavaParameters()
                : instance.launcher.javaArguments;

        // add minecraft client jar
        cpb.append(instance.getMinecraftJarLibraryPath().toAbsolutePath().toString());

        File jarMods = instance.getRoot().resolve("jarmods").toFile();
        File[] jarModFiles = jarMods.listFiles();
        if (jarMods.exists() && jarModFiles != null && jarModFiles.length != 0) {
            for (File file : jarModFiles) {
                hasCustomJarMods = true;
                cpb.append(File.pathSeparator);
                cpb.append(file.getAbsolutePath());
            }
        }

        instance.libraries.stream().filter(
                library -> library.shouldInstall() && library.downloads.artifact != null && !library.hasNativeForOS())
                .filter(library -> library.downloads.artifact != null && library.downloads.artifact.path != null)
                .forEach(library -> {
                    cpb.append(File.pathSeparator);
                    cpb.append(
                            FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile().getAbsolutePath());
                });

        instance.libraries.stream().filter(Library::hasNativeForOS).forEach(library -> {
            com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

            cpb.append(File.pathSeparator);
            cpb.append(FileSystem.LIBRARIES.resolve(download.path).toFile().getAbsolutePath());
        });

        File binFolder = instance.getRoot().resolve("bin").toFile();
        File[] libraryFiles = binFolder.listFiles();
        if (binFolder.exists() && libraryFiles != null && libraryFiles.length != 0) {
            for (File file : libraryFiles) {
                LogManager.info("Added in custom library " + file.getName());

                cpb.append(File.pathSeparator);
                cpb.append(file);
            }
        }

        List<String> arguments = new ArrayList<>();

        String path = javaPath + File.separator + "bin" + File.separator + "java";
        if (OS.isWindows()) {
            path += "w";
        }
        arguments.add(path);

        arguments.add("-XX:-OmitStackTraceInFastThrow");

        if (javaArguments.isEmpty() && !Java.isMinecraftJavaNewerThanJava8()) {
            // Some defaults if on Java 8 or less
            javaArguments = "-XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:-UseAdaptiveSizePolicy";
        }

        arguments.add("-Xms" + initialMemory + "M");

        if (OS.getMaximumRam() != 0 && maximumMemory < instance.launcher.requiredMemory) {
            if ((OS.getMaximumRam() / 2) < instance.launcher.requiredMemory) {
                arguments.add("-Xmx" + maximumMemory + "M");
            } else {
                arguments.add("-Xmx" + instance.launcher.requiredMemory + "M");
            }
        } else {
            arguments.add("-Xmx" + maximumMemory + "M");
        }
        if (OS.getMaximumRam() != 0 && permGen < instance.launcher.requiredPermGen
                && (OS.getMaximumRam() / 8) < instance.launcher.requiredPermGen) {
            if (Java.useMetaspace()) {
                arguments.add("-XX:MetaspaceSize=" + instance.launcher.requiredPermGen + "M");
            } else {
                arguments.add("-XX:PermSize=" + instance.launcher.requiredPermGen + "M");
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

        arguments.add("-Dfml.log.level=" + App.settings.getForgeLoggingLevel());

        if (OS.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            // arguments.add("-Xdock:icon=" + new File(instance.getAssetsDir(),
            // "icons/minecraft.icns").getAbsolutePath());
            arguments.add("-Xdock:name=\"" + instance.launcher.name + "\"");
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

        arguments.add("-Djava.library.path=" + nativesTempDir.toAbsolutePath().toString());
        arguments.add("-cp");
        arguments.add(cpb.toString());
        arguments.add(instance.mainClass);

        String props = "[]";

        if (!response.isOffline()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer()).create();
            props = gson.toJson(response.getAuth().getUserProperties());
        }

        for (String argument : instance.arguments.asStringList().stream().distinct().collect(Collectors.toList())) {
            argument = argument.replace("${auth_player_name}", account.getMinecraftUsername());
            argument = argument.replace("${profile_name}", instance.launcher.name);
            argument = argument.replace("${user_properties}", props);
            argument = argument.replace("${version_name}", instance.id);
            argument = argument.replace("${game_directory}", instance.getRoot().toAbsolutePath().toString());
            argument = argument.replace("${game_assets}", instance.getAssetsDir().toAbsolutePath().toString());
            argument = argument.replace("${assets_root}", FileSystem.ASSETS.toAbsolutePath().toString());
            argument = argument.replace("${assets_index_name}", instance.assets);
            argument = argument.replace("${auth_uuid}", UUIDTypeAdapter.fromUUID(account.getRealUUID()));
            argument = argument.replace("${auth_access_token}", account.getAccessToken());
            argument = argument.replace("${auth_session}", account.getSession(response));
            argument = argument.replace("${version_type}", instance.type);
            argument = argument.replace("${launcher_name}", Constants.LAUNCHER_NAME);
            argument = argument.replace("${launcher_version}", Constants.VERSION.toString());
            argument = argument.replace("${natives_directory}", nativesTempDir.toAbsolutePath().toString());
            argument = argument.replace("${user_type}",
                    response.isOffline() ? com.mojang.authlib.UserType.MOJANG.getName()
                            : response.getAuth().getUserType().getName());

            if (!argument.equalsIgnoreCase("-cp") && !argument.equalsIgnoreCase("${classpath}")) {
                arguments.add(argument);
            }
        }

        if (App.settings.startMinecraftMaximised()) {
            arguments.add("--width=" + OS.getMaximumWindowWidth());
            arguments.add("--height=" + OS.getMaximumWindowHeight());
        } else {
            arguments.add("--width=" + App.settings.getWindowWidth());
            arguments.add("--height=" + App.settings.getWindowHeight());
        }

        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            if (App.settings != null) {
                argsString = argsString.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "USERSDIR");
            }

            argsString = argsString.replace(account.getMinecraftUsername(), "REDACTED");
            argsString = argsString.replace(account.getUUID(), "REDACTED");
            argsString = argsString.replace(account.getAccessToken(), "REDACTED");
            argsString = argsString.replace(account.getSession(response), "REDACTED");
            argsString = argsString.replace(props, "REDACTED");
        }

        LogManager.info("Launching Minecraft with the following arguments " + "(user related stuff has been removed):"
                + " " + argsString);
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRoot().toAbsolutePath().toFile());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }
}
