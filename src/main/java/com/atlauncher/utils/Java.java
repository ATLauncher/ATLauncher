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
package com.atlauncher.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.javafinder.JavaFinder;
import com.atlauncher.utils.javafinder.JavaInfo;

public class Java {
    /**
     * Get the Java version that the launcher runs on.
     *
     * @return the Java version that the launcher runs on
     */
    public static String getLauncherJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Get the Java version used to run Minecraft.
     *
     * @return the Java version used to run Minecraft
     */
    public static String getMinecraftJavaVersion() {
        if (App.settings.usingCustomJavaPath) {
            File folder = new File(App.settings.javaPath, "bin/");

            ProcessBuilder processBuilder = new ProcessBuilder(getPathToMinecraftJavaExecutable(), "-version");
            processBuilder.directory(folder.getAbsoluteFile());
            processBuilder.redirectErrorStream(true);

            String version = "Unknown";

            try {
                Process process = processBuilder.start();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    Pattern p = Pattern.compile("(java|openjdk) version \"([^\"]*)\"");

                    while ((line = br.readLine()) != null) {
                        // Extract version information
                        Matcher m = p.matcher(line);

                        if (m.find()) {
                            version = m.group(2);
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }

            if (version.equals("Unknown")) {
                LogManager.warn("Cannot get Java version from the output of \"" + getPathToSystemJavaExecutable()
                        + " -version\"");
            }

            return version;
        } else {
            return OS.getPreferredJava(Java.getInstalledJavas()).version;
        }
    }

    /**
     * Parse a Java version string and get the major version number. For example
     * "1.8.0_91" is parsed to 8.
     *
     * @param version the version string to parse
     * @return the parsed major version number
     */
    public static int parseJavaVersionNumber(String version) {
        Matcher m = Pattern.compile("(?:1\\.)?([0-9]+).*").matcher(version);

        return m.find() ? Integer.parseInt(m.group(1)) : -1;
    }

    /**
     * Parse a Java build version string and get the major version number. For
     * example "1.8.0_91" is parsed to 91, 11.0.3_7 is parsed to 7 and 11.0.3+7 is
     * parsed to 7
     *
     * @param version the version string to parse
     * @return the parsed build number
     */
    public static int parseJavaBuildVersion(String version) {
        Matcher m = Pattern.compile(".*[_\\.]([0-9]+)").matcher(version);

        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }

        return 0;
    }

    /**
     * Get the major Java version that the launcher runs on.
     *
     * @return the major Java version that the launcher runs on
     */
    public static int getLauncherJavaVersionNumber() {
        return parseJavaVersionNumber(getLauncherJavaVersion());
    }

    /**
     * Get the major Java version used to run Minecraft.
     *
     * @return the major Java version used to run Minecraft
     */
    public static int getMinecraftJavaVersionNumber() {
        return parseJavaVersionNumber(getMinecraftJavaVersion());
    }

    /**
     * Get the Java versions used by the Launcher and Minecraft as a string.
     *
     * @return the Java versions used by the Launcher and Minecraft as a string
     */
    public static String getActualJavaVersion() {
        return String.format("Launcher: Java %d (%s), Minecraft: Java %d (%s)", getLauncherJavaVersionNumber(),
                getLauncherJavaVersion(), getMinecraftJavaVersionNumber(), getMinecraftJavaVersion());
    }

    /**
     * Checks if the user is using Java 7 or above.
     *
     * @return true if the user is using Java 7 or above else false
     */
    public static boolean isJava7OrAbove(boolean checkCustomPath) {
        int version = checkCustomPath ? getMinecraftJavaVersionNumber() : getLauncherJavaVersionNumber();
        return version >= 7 || version == -1;
    }

    public static boolean isSystemJavaNewerThanJava8() {
        return getLauncherJavaVersionNumber() >= 9;
    }

    public static boolean isMinecraftJavaNewerThanJava8() {
        return getMinecraftJavaVersionNumber() >= 9;
    }

    /**
     * Checks if the user is using exactly Java 8.
     *
     * @return true if the user is using exactly Java 8
     */
    public static boolean isJava8() {
        return getMinecraftJavaVersionNumber() == 8;
    }

    /**
     * Checks if the user is using Java 8 or newer or if on Java 7 at least version
     * 151 or if on Java 8 at least version 141 or newer.
     */
    public static boolean isUsingJavaSupportingLetsEncrypt() {
        return getLauncherJavaVersionNumber() > 8
                || (getLauncherJavaVersionNumber() == 7 && parseJavaBuildVersion(getLauncherJavaVersion()) >= 151)
                || (getLauncherJavaVersionNumber() == 8 && parseJavaBuildVersion(getLauncherJavaVersion()) >= 141);
    }

    /**
     * Checks if the user is using exactly Java 9.
     *
     * @return true if the user is using exactly Java 9
     */
    public static boolean isJava9() {
        return getMinecraftJavaVersionNumber() == 9;
    }

    /**
     * Checks whether Metaspace should be used instead of PermGen. This is the case
     * for Java 8 and above.
     *
     * @return whether Metaspace should be used instead of PermGen
     */
    public static boolean useMetaspace() {
        return getMinecraftJavaVersionNumber() >= 8;
    }

    public static String getPathToSystemJavaExecutable() {
        String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";

        if (OS.isWindows()) {
            path += "w";
        }

        return path;
    }

    public static String getPathToMinecraftJavaExecutable() {
        String path = App.settings.javaPath + File.separator + "bin" + File.separator + "java";

        if (OS.isWindows()) {
            path += "w";
        }

        return path;
    }

    public static String getPathToJavaExecutable(Path root) {
        return root.resolve("bin/java" + (OS.isWindows() ? "w" : "")).toAbsolutePath().toString();
    }

    public static List<JavaInfo> getInstalledJavas() {
        PerformanceManager.start();
        List<JavaInfo> javas = JavaFinder.findJavas().stream()
                .filter(javaInfo -> javaInfo.majorVersion != null && javaInfo.minorVersion != null)
                .collect(Collectors.toList());

        JavaInfo systemJava = new JavaInfo(Java.getPathToSystemJavaExecutable());
        if (javas.size() == 0
                || javas.stream().noneMatch(java -> java.rootPath.equalsIgnoreCase(systemJava.rootPath))) {
            javas.add(systemJava);
        }

        if (Files.isDirectory(FileSystem.RUNTIMES)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(FileSystem.RUNTIMES)) {
                for (Path path : directoryStream) {
                    if (Files.exists(path.resolve("release"))) {
                        javas.add(new JavaInfo(Java.getPathToJavaExecutable(path)));
                    }
                }
            } catch (IOException e) {
                LogManager.logStackTrace(e);
            }
        }

        PerformanceManager.end();
        return javas;
    }

    public static boolean hasInstalledRuntime() {
        boolean found = false;

        if (Files.isDirectory(FileSystem.RUNTIMES)) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(FileSystem.RUNTIMES)) {
                for (Path path : directoryStream) {
                    if (Files.exists(path.resolve("release"))) {
                        found = true;
                    }
                }
            } catch (IOException e) {
            }
        }

        return found;
    }
}
