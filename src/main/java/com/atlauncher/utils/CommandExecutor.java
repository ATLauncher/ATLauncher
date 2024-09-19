/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.data.Instance;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.exceptions.CommandException;
import com.atlauncher.managers.LogManager;

public class CommandExecutor {
    /**
     * Runs the specified {@code command} in the system command line. Substitutes
     * <br/>
     * "$INST_NAME" with the name of the specified {@code instance}, <br/>
     * "$INST_ID" with the name of the instance's root directory, <br/>
     * "$INST_DIR" with the absolute path the the instance directory "$INST_JAVA"
     * with the absolute path to the java binary used to run the instance, <br/>
     * "$INST_JAVA_ARGS" with the JVM arguments (e.g. "-Xmx3G", NOT the arguments
     * used to launch the game, containing account information), <br/>
     * in the specified command, then runs it on the command line.
     *
     * @param instance The instance to run the command for
     * @param command  The command to run on the command line
     * @throws CommandException If the process exits with a non zero value or
     *                          another error occurs when trying to run the command
     */
    public static void executeCommand(Instance instance, String command) {
        if (command == null)
            return;

        try {
            command = replaceArgumentTokensForCommand(getCommandArgumentTokensForInstance(instance), command);

            LogManager.info("Running command: \"" + command + "\"");

            Process process;

            // linux/osx needs to run through sh
            if (OS.isLinux() || OS.isMac()) {
                String[] linuxCommand = { "/bin/sh", "-c", command };
                process = Runtime.getRuntime().exec(linuxCommand, null, instance.getRootDirectory());
            } else {
                process = Runtime.getRuntime().exec(command, null, instance.getRootDirectory());
            }

            printStreamToConsole(process.getInputStream());

            process.waitFor();

            if (process.exitValue() != 0) {
                printErrorStreamToConsole(process.getErrorStream());

                throw new CommandException();
            }
        } catch (IOException | InterruptedException e) {
            LogManager.logStackTrace(e);
            throw new CommandException(e);
        }
    }

    private static void printStreamToConsole(InputStream stream) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line;

            while ((line = reader.readLine()) != null) {
                LogManager.info(line);
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            // throw new RuntimeException(e);
        }
    }

    // print the whole thing as 1 message rather than line by line
    private static void printErrorStreamToConsole(InputStream stream) {
        try {
            boolean hasGotFirstContentLine = false;
            InputStreamReader inputStreamReader = new InputStreamReader(stream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder message = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                // cut off any initial blank lines
                if (!line.isEmpty() || hasGotFirstContentLine) {
                    hasGotFirstContentLine = true;
                    message.append(line).append(System.lineSeparator());
                }
            }

            LogManager.error(message.toString());
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            // throw new RuntimeException(e);
        }
    }

    /**
     * Substitutes all tokens beginning with '$' (and in all capitals) with the
     * matching key in {@code tokens} if it exists
     *
     * @param tokens  All the keys that will be replaced with their value in
     *                {@code command}
     * @param command The string to substitute into
     * @return The string with all tokens substituted
     */
    private static String replaceArgumentTokensForCommand(Map<String, String> tokens, String command) {
        final Pattern tokenPattern = Pattern.compile("\\$([A-Z_]+)");
        final StringBuffer result = new StringBuffer();
        Matcher match = tokenPattern.matcher(command);

        while (match.find()) {
            final String key = match.group(1);

            if (tokens.containsKey(key)) {
                match.appendReplacement(result, tokens.get(key).replace("\\", "\\\\"));
            }
        }

        match.appendTail(result);

        return result.toString();
    }

    private static Map<String, String> getCommandArgumentTokensForInstance(Instance instance) {
        final Map<String, String> result = new HashMap<>();
        result.put("INST_NAME", instance.getName());
        result.put("INST_ID", instance.getRootDirectory().getName());
        result.put("INST_DIR", instance.getRootDirectory().getAbsolutePath());
        result.put("INST_MC_DIR", instance.getRootDirectory().getAbsolutePath());
        result.put("INST_JAVA", getJavaPathForInstance(instance));
        result.put("INST_JAVA_ARGS", getJavaParametersForInstance(instance));
        return result;
    }

    private static String getJavaPathForInstance(Instance instance) {
        String javaPath = Optional.ofNullable(instance.launcher.javaPath).orElse(App.settings.javaPath);

        // are we using Mojangs provided runtime?
        if (instance.javaVersion != null && App.settings.useJavaProvidedByMinecraft) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeToUse = Optional.ofNullable(instance.launcher.javaRuntimeOverride)
                    .orElse(instance.javaVersion.component);

            if (runtimesForSystem.containsKey(runtimeToUse)
                    && !runtimesForSystem.get(runtimeToUse).isEmpty()) {
                Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                        .resolve(JavaRuntimes.getSystem()).resolve(runtimeToUse);

                if (OS.isMac()) {
                    runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
                }

                if (Files.isDirectory(runtimeDirectory)) {
                    javaPath = runtimeDirectory.toAbsolutePath().toString();
                    if (instance.launcher.javaRuntimeOverride != null) {
                        LogManager.info(String.format("Using overriden Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    } else {
                        LogManager.info(String.format("Using Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    }
                }
            }
        }

        String path = javaPath + File.separator + "bin" + File.separator + "java";

        if (OS.isWindows() && (Files.exists(Paths.get(path + "w")) || Files.exists(Paths.get(path + "w.exe")))) {
            path += "w";
        }

        return path;
    }

    private static String getJavaParametersForInstance(Instance instance) {
        if (instance.launcher.javaArguments == null) {
            return App.settings.javaParameters;
        } else {
            return instance.launcher.javaArguments;
        }
    }
}
