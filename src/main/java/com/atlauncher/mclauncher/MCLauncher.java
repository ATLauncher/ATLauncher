/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.data.AbstractAccount;
import com.atlauncher.data.Constants;
import com.atlauncher.data.Instance;
import com.atlauncher.data.InstanceSettings;
import com.atlauncher.data.InstanceV2;
import com.atlauncher.data.Launchable;
import com.atlauncher.data.LoginResponse;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.data.MojangAccount;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.PropertyMapSerializer;
import com.atlauncher.data.minecraft.VersionManifest;
import com.atlauncher.data.minecraft.VersionManifestVersion;
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

    // Instance
    public static Process launch(MicrosoftAccount account, Launchable instance) throws Exception {
        return launch(account, instance, instance.getNativesDirectory().toPath());
    }

    // InstanceV2
    public static Process launch(MicrosoftAccount account, Launchable instance, Path nativesTempDir)
            throws Exception {
        return launch(account, instance, null, nativesTempDir.toFile());
    }

    // Instance
    public static Process launch(MojangAccount account, Launchable instance, LoginResponse response)
            throws Exception {
        return launch(account, instance, response, instance.getNativesDirectory().toPath());
    }

    // InstanceV2
    public static Process launch(MojangAccount account, Launchable instance, LoginResponse response,
            Path nativesTempDir) throws Exception {
        String props = "[]";

        if (!response.isOffline()) {
            Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, new PropertyMapSerializer()).create();
            props = gson.toJson(response.getAuth().getUserProperties());
        }

        return launch(account, instance, props, nativesTempDir.toFile());
    }

    private static Process launch(AbstractAccount account, Launchable instance, String props, File nativesDir)
            throws Exception {
        List<String> arguments = getArguments(account, instance, props, nativesDir.getAbsolutePath());

        LogManager.info("Launching Minecraft with the following arguments (user related stuff has been removed): "
                + censorArguments(arguments, account, props));
        ProcessBuilder processBuilder = new ProcessBuilder(arguments);
        processBuilder.directory(instance.getRootDirectory());
        processBuilder.redirectErrorStream(true);
        processBuilder.environment().remove("_JAVA_OPTIONS"); // Remove any _JAVA_OPTIONS, they are a PAIN
        return processBuilder.start();
    }

    private static List<String> getArguments(AbstractAccount account, Launchable instance, String props,
            String nativesDir) {
        StringBuilder cpb = new StringBuilder();
        boolean hasCustomJarMods = false;

        ErrorReporting.recordInstancePlay(instance.getPackName(), instance.getVersion(), instance.getLoaderVersion(),
                (instance instanceof InstanceV2 ? 2 : 1));

        InstanceSettings settings = instance.getSettings();
        int initialMemory = Optional.ofNullable(settings.getInitialMemory()).orElse(App.settings.initialMemory);
        int maximumMemory = Optional.ofNullable(settings.getMaximumMemory()).orElse(App.settings.maximumMemory);
        int permGen = Optional.ofNullable(settings.getPermGen()).orElse(App.settings.metaspace);
        String javaPath = Optional.ofNullable(settings.getJavaPath()).orElse(App.settings.javaPath);
        String javaArguments = Optional.ofNullable(settings.getJavaArguments()).orElse(App.settings.javaParameters);

        if (instance instanceof InstanceV2) {
            // add minecraft client jar
            cpb.append(instance.getMinecraftJar().getAbsolutePath());
        }

        File jarMods = instance.getJarModsDirectory();
        File[] jarModFiles = jarMods.listFiles();
        if (jarMods.exists() && jarModFiles != null && jarModFiles.length != 0) {
            for (File file : jarModFiles) {
                hasCustomJarMods = true;
                cpb.append(File.pathSeparator);
                cpb.append(file.getAbsolutePath());
            }
        }

        if (instance instanceof InstanceV2) {
            ((InstanceV2) instance).libraries.stream()
                    .filter(library -> library.shouldInstall() && library.downloads.artifact != null
                            && !library.hasNativeForOS())
                    .filter(library -> library.downloads.artifact != null && library.downloads.artifact.path != null)
                    .forEach(library -> {
                        cpb.append(File.pathSeparator);
                        cpb.append(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path).toFile()
                                .getAbsolutePath());
                    });

            ((InstanceV2) instance).libraries.stream().filter(Library::hasNativeForOS).forEach(library -> {
                com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

                cpb.append(File.pathSeparator);
                cpb.append(FileSystem.LIBRARIES.resolve(download.path).toFile().getAbsolutePath());
            });
        } else {
            File librariesBaseDir = ((Instance) instance).usesNewLibraries() ? FileSystem.LIBRARIES.toFile()
                    : instance.getBinDirectory();

            for (String jarFile : ((Instance) instance).getLibraries()) {
                cpb.append(File.pathSeparator);
                cpb.append(new File(librariesBaseDir, jarFile).getAbsolutePath());
            }
        }

        File binFolder = instance.getBinDirectory();
        File[] libraryFiles = binFolder.listFiles();
        if (binFolder.exists() && libraryFiles != null && libraryFiles.length != 0) {
            for (File file : libraryFiles) {
                if (instance instanceof Instance && (file.isDirectory()
                        || file.getName().equalsIgnoreCase(instance.getMinecraftJar().getAbsolutePath())
                        || ((Instance) instance).getLibraries().contains(file.getName()))) {
                    continue;
                }

                LogManager.info("Added in custom library " + file.getName());

                cpb.append(File.pathSeparator);
                cpb.append(file);
            }
        }

        if (instance instanceof Instance && !((Instance) instance).usesNewLibraries()) {
            cpb.append(File.pathSeparator);
            cpb.append(instance.getMinecraftJar().getAbsolutePath());
        }

        List<String> arguments = new ArrayList<>();

        if (OS.isLinux() && App.settings.enableFeralGamemode && Utils.executableInPath("gamemoderun")) {
            arguments.add("gamemoderun");
        }

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

        arguments.add("-Dfml.log.level=" + App.settings.forgeLoggingLevel);

        if (OS.isMac()) {
            arguments.add("-Dapple.laf.useScreenMenuBar=true");
            arguments.add("-Xdock:name=\"" + instance.getName() + "\"");

            if (new File(instance.getAssetsDir(), "icons/minecraft.icns").exists()) {
                arguments.add(
                        "-Xdock:icon=" + new File(instance.getAssetsDir(), "icons/minecraft.icns").getAbsolutePath());
            }
        }

        ArrayList<String> negatedArgs = new ArrayList<>();

        if (!javaArguments.isEmpty()) {
            for (String arg : javaArguments.split(" ")) {
                if (!arg.isEmpty()) {
                    if (instance instanceof Instance) {
                        if (((Instance) instance).hasExtraArguments()) {
                            if (((Instance) instance).getExtraArguments().contains(arg)) {
                                LogManager.error("Duplicate argument " + arg + " found and not added!");
                                continue;
                            }

                            if (arg.startsWith("-XX:+")) {
                                if (((Instance) instance).getExtraArguments().contains("-XX:-" + arg.substring(5))) {
                                    negatedArgs.add("-XX:-" + arg.substring(5));
                                    LogManager
                                            .error("Argument " + arg + " is negated by pack developer and not added!");
                                    continue;
                                }
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

        if (instance instanceof InstanceV2) {
            for (String argument : ((InstanceV2) instance).arguments.jvmAsStringList().stream().distinct()
                    .collect(Collectors.toList())) {
                argument = replaceArgument(argument, instance, account, props, nativesDir);

                if (!argument.equalsIgnoreCase("-cp") && !argument.equalsIgnoreCase("${classpath}")) {
                    arguments.add(argument);
                }
            }
        }

        arguments.add("-Djava.library.path=" + nativesDir);
        arguments.add("-cp");
        arguments.add(cpb.toString());
        arguments.add(instance.getMainClass());

        if (instance instanceof InstanceV2) {
            for (String argument : ((InstanceV2) instance).arguments.gameAsStringList().stream().distinct()
                    .collect(Collectors.toList())) {
                argument = replaceArgument(argument, instance, account, props, nativesDir);

                if (!argument.equalsIgnoreCase("-cp") && !argument.equalsIgnoreCase("${classpath}")) {
                    arguments.add(argument);
                }
            }
        } else {
            List<String> launchArguments = new ArrayList<>();

            if (((Instance) instance).hasArguments()) {
                launchArguments.addAll(((Instance) instance).getArguments());
            } else if (((Instance) instance).hasMinecraftArguments()) {
                launchArguments = Arrays.asList(((Instance) instance).getMinecraftArguments().split(" "));
            } else {
                VersionManifest versionManifest = com.atlauncher.network.Download.build().cached()
                        .setUrl(String.format("%s/mc/game/version_manifest.json", Constants.LAUNCHER_META_MINECRAFT))
                        .asClass(VersionManifest.class);

                VersionManifestVersion minecraftVersion = versionManifest.versions.stream()
                        .filter(version -> version.id.equalsIgnoreCase(instance.getMinecraftVersion())).findFirst()
                        .orElse(null);

                if (minecraftVersion != null) {
                    MinecraftVersion version = com.atlauncher.network.Download.build().cached()
                            .setUrl(minecraftVersion.url).asClass(MinecraftVersion.class);

                    if (version.arguments != null) {
                        launchArguments = version.arguments.asStringList();
                    } else if (version.minecraftArguments != null) {
                        launchArguments = Arrays.asList(version.minecraftArguments.split(" "));
                    }
                }
            }

            if (launchArguments.size() != 0) {
                for (String argument : launchArguments) {
                    argument = replaceArgument(argument, instance, account, props, nativesDir);

                    if (!argument.equalsIgnoreCase("-cp") && !argument.equalsIgnoreCase("${classpath}")) {
                        arguments.add(argument);
                    }
                }
            } else {
                arguments.add("--username=" + account.minecraftUsername);
                arguments.add("--session=" + account.getSessionToken());

                // This is for 1.7
                arguments.add("--accessToken=" + account.getAccessToken());
                arguments.add("--uuid=" + UUIDTypeAdapter.fromUUID(account.getRealUUID()));
                // End of stuff for 1.7

                arguments.add("--version=" + instance.getMinecraftVersion());
                arguments.add("--gameDir=" + instance.getRootDirectory().getAbsolutePath());
                arguments.add("--assetsDir=" + FileSystem.ASSETS.toAbsolutePath().toString());
            }
        }

        if (App.settings.maximiseMinecraft) {
            arguments.add("--width=" + OS.getMaximumWindowWidth());
            arguments.add("--height=" + OS.getMaximumWindowHeight());
        } else {
            arguments.add("--width=" + App.settings.windowWidth);
            arguments.add("--height=" + App.settings.windowHeight);
        }

        if (instance instanceof Instance) {
            if (((Instance) instance).hasExtraArguments()) {
                String args = ((Instance) instance).getExtraArguments();
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
        }

        return arguments;
    }

    private static String replaceArgument(String incomingArgument, Launchable instance, AbstractAccount account,
            String props, String nativesDir) {
        String argument = incomingArgument;

        argument = argument.replace("${auth_player_name}", account.minecraftUsername);
        argument = argument.replace("${profile_name}", instance.getName());
        argument = argument.replace("${user_properties}", Optional.ofNullable(props).orElse(""));
        argument = argument.replace("${version_name}", instance.getMinecraftVersion());
        argument = argument.replace("${game_directory}", instance.getRootDirectory().getAbsolutePath());
        argument = argument.replace("${game_assets}", instance.getAssetsDir().getAbsolutePath());
        argument = argument.replace("${assets_root}", FileSystem.ASSETS.toAbsolutePath().toString());
        argument = argument.replace("${assets_index_name}", instance.getAssets());
        argument = argument.replace("${auth_uuid}", UUIDTypeAdapter.fromUUID(account.getRealUUID()));
        argument = argument.replace("${auth_access_token}", account.getAccessToken());
        argument = argument.replace("${version_type}", instance.getVersionType());
        argument = argument.replace("${launcher_name}", Constants.LAUNCHER_NAME);
        argument = argument.replace("${launcher_version}", Constants.VERSION.toStringForLogging());
        argument = argument.replace("${natives_directory}", nativesDir);
        argument = argument.replace("${user_type}", account.type);
        argument = argument.replace("${auth_session}", account.getSessionToken());

        return argument;
    }

    private static String censorArguments(List<String> arguments, AbstractAccount account, String props) {
        String argsString = arguments.toString();

        if (!LogManager.showDebug) {
            if (App.settings != null) {
                argsString = argsString.replace(FileSystem.BASE_DIR.toAbsolutePath().toString(), "USERSDIR");
            }

            argsString = argsString.replace(account.minecraftUsername, "REDACTED");
            argsString = argsString.replace(account.uuid, "REDACTED");
            argsString = argsString.replace(account.getAccessToken(), "REDACTED");

            if (props != null) {
                argsString = argsString.replace(props, "REDACTED");
            }
        }

        return argsString;
    }
}
