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
package com.atlauncher.utils;

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.LogManager;
import com.atlauncher.Update;
import com.atlauncher.data.Constants;
import com.atlauncher.utils.javafinder.JavaInfo;

public enum OS {
    LINUX, WINDOWS, OSX;

    public static OS getOS() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            return OS.WINDOWS;
        } else if (osName.contains("mac")) {
            return OS.OSX;
        } else {
            return OS.LINUX;
        }
    }

    public static String getName() {
        return System.getProperty("os.name");
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }

    public static boolean isWindows() {
        return getOS() == WINDOWS;
    }

    public static boolean isMac() {
        return getOS() == OSX;
    }

    public static boolean isLinux() {
        return getOS() == LINUX;
    }

    /**
     * This gets the storage path for the OS.
     */
    public static Path storagePath() {
        switch (getOS()) {
        case WINDOWS:
            return Paths.get(System.getenv("APPDATA")).resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
        case OSX:
            return Paths.get(System.getProperty("user.home")).resolve("Library").resolve("Application Support")
                    .resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
        default:
            return Paths.get(System.getProperty("user.home")).resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
        }
    }

    /**
     * This checks to see if the user is using the Mac application.
     */
    public static boolean isUsingMacApp() {
        return OS.isMac() && Files.exists(FileSystem.BASE_DIR.getParent().resolve("MacOS"));
    }

    /**
     * This opens the users default browser to the given url.
     */
    public static void openWebBrowser(String url) {
        try {
            OS.openWebBrowser(new URI(url));
        } catch (Exception e) {
            LogManager.logStackTrace("Error opening web browser!", e);
        }
    }

    /**
     * This opens the users default browser to the given url.
     */
    public static void openWebBrowser(URL url) {
        try {
            OS.openWebBrowser(url.toURI());
        } catch (URISyntaxException e) {
            LogManager.logStackTrace("Error opening web browser!", e);
        }
    }

    /**
     * This opens the users default browser to the given uri.
     */
    public static void openWebBrowser(URI uri) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            } else if (getOS() == LINUX && (Files.exists(Paths.get("/usr/bin/xdg-open"))
                    || Files.exists(Paths.get("/usr/local/bin/xdg-open")))) {
                Runtime.getRuntime().exec("xdg-open " + uri);
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Error opening web browser!", e);
        }
    }

    /**
     * Opens the system file explorer to the given path.
     */
    public static void openFileExplorer(Path path) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(path.toFile());
            } else if (getOS() == LINUX && (Files.exists(Paths.get("/usr/bin/xdg-open"))
                    || Files.exists(Paths.get("/usr/local/bin/xdg-open")))) {
                Runtime.getRuntime().exec("xdg-open " + path.toString());
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Error opening file explorer!", e);
        }
    }

    /**
     * Os slash.
     *
     * @return the string
     */
    public static String osSlash() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    /**
     * Os delimiter.
     *
     * @return the string
     */
    public static String osDelimiter() {
        if (isWindows()) {
            return ";";
        } else {
            return ":";
        }
    }

    /**
     * Gets the java home.
     */
    public static String getJavaHome() {
        return System.getProperty("java.home");
    }

    public static String getDefaultJavaPath() {
        if (!OS.isWindows()) {
            return OS.getJavaHome();
        }

        String prefferedPath = OS.getPrefferedJavaPath(Java.getInstalledJavas());

        if (prefferedPath == null) {

            return OS.getJavaHome();
        }

        return prefferedPath;
    }

    public static String getPrefferedJavaPath(List<JavaInfo> installedJavas) {
        if (installedJavas.size() == 0) {
            return null;
        }

        // get newest Java 8 64 bit if installed
        Optional<JavaInfo> java864bit = installedJavas.stream()
                .sorted(Comparator.comparingInt((JavaInfo javaInfo) -> javaInfo.minorVersion != null ? javaInfo.minorVersion : 0).reversed())
                .filter(javaInfo -> javaInfo.majorVersion == 8 && javaInfo.is64bits).findFirst();
        if (java864bit.isPresent()) {
            return java864bit.get().rootPath;
        }

        // get newest 64 bit if installed
        Optional<JavaInfo> java64bit = installedJavas.stream().filter(javaInfo -> javaInfo.is64bits).findFirst();
        if (java64bit.isPresent()) {
            return java64bit.get().rootPath;
        }

        // default to the first java installed
        return installedJavas.get(0).rootPath;
    }

    /**
     * Checks if the Java being used is 64 bit.
     */
    public static boolean is64Bit() {
        return System.getProperty("sun.arch.data.model").contains("64");
    }

    /**
     * Checks if Windows is 64 bit.
     */
    public static boolean isWindows64Bit() {
        return System.getenv("ProgramFiles(x86)") != null;
    }

    /**
     * Gets the architecture type of the system.
     */
    public static String getArch() {
        if (is64Bit()) {
            return "64";
        } else {
            return "32";
        }
    }

    /**
     * Returns the amount of RAM in the users system via OperatingSystemMXBean. This
     * was removed in Java 9.
     */
    public static int getSystemRamViaBean() {
        long ramm = 0;
        int ram = 0;
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        try {
            Method m = operatingSystemMXBean.getClass().getDeclaredMethod("getTotalPhysicalMemorySize");
            m.setAccessible(true);
            Object value = m.invoke(operatingSystemMXBean);
            if (value != null) {
                ramm = Long.parseLong(value.toString());
                ram = (int) (ramm / 1048576);
            } else {
                ram = 1024;
            }
        } catch (SecurityException | InvocationTargetException | IllegalAccessException | IllegalArgumentException
                | NoSuchMethodException e) {
            LogManager.logStackTrace(e);
        }
        return ram;
    }

    /**
     * Returns the amount of RAM in the users system via the getMemory tool.
     */
    public static int getSystemRamViaTool() {
        long ramm = 0;
        int ram = 0;

        String binaryFile;

        if (OS.is64Bit()) {
            binaryFile = (OS.isWindows() ? "getMemory-x64.exe"
                    : (OS.isLinux() ? "getMemory-x64-linux" : "getMemory-x64-osx"));
        } else {
            binaryFile = (OS.isWindows() ? "getMemory.exe" : (OS.isLinux() ? "getMemory-linux" : "getMemory-osx"));
        }

        ProcessBuilder processBuilder = new ProcessBuilder(App.settings.getToolsDir() + File.separator + binaryFile);
        processBuilder.directory(App.settings.getToolsDir().getAbsoluteFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                ramm = Long.parseLong(br.readLine());
                ram = (int) (ramm / 1048576);
            } finally {
                br.close();
            }
        } catch (IOException ignored) {
            return ram;
        }

        return ram;
    }

    /**
     * Returns the amount of RAM in the users system.
     */
    public static int getSystemRam() {
        if (!Java.isSystemJavaNewerThanJava8()) {
            return OS.getSystemRamViaBean();
        }

        return OS.getSystemRamViaTool();
    }

    /**
     * Returns the maximum RAM available to Java. If on a 64 Bit system, then its
     * all of the System RAM otherwise its limited to 1GB or less due to allocations
     * of PermGen.
     */
    public static int getMaximumRam() {
        int maxRam = getSystemRam();
        if (!is64Bit()) {
            if (maxRam < 1024) {
                return maxRam;
            } else {
                return 1024;
            }
        } else {
            return maxRam;
        }
    }

    /**
     * Returns the safe amount of maximum ram available to Java. This is set to half
     * of the total maximum ram available to Java in order to not allocate too much
     * and leave enough RAM for the OS and other applications.
     */
    public static int getSafeMaximumRam() {
        int maxRam = getSystemRam();
        if (!is64Bit()) {
            if (maxRam < 1024) {
                return maxRam / 2;
            } else {
                return 512;
            }
        } else {
            return maxRam / 2;
        }
    }

    /**
     * Gets the maximum window width.
     */
    public static int getMaximumWindowWidth() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.width;
    }

    /**
     * Gets the maximum window height.
     */
    public static int getMaximumWindowHeight() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getScreenSize();
        return dim.height;
    }

    /**
     * If the system is running in headless mode.
     */
    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }

    /**
     * This restarts the launcher with an option set of arguments to add.
     *
     * @param args a List of arguments to pass when starting the launcher
     */
    public static void restartLauncher(List<String> args) {
        File thisFile = new File(Update.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        String path = null;
        try {
            path = thisFile.getCanonicalPath();
            path = URLDecoder.decode(path, "UTF-8");
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }

        List<String> arguments = new ArrayList<>();

        if (isUsingMacApp()) {
            arguments.add("open");
            arguments.add("-n");
            arguments.add(FileSystem.BASE_DIR.getParent().getParent().toString());
        } else {
            arguments.add(Java.getPathToSystemJavaExecutable());
            arguments.add("-Djna.nosys=true");
            arguments.add("-jar");
            arguments.add(path);
        }

        if (args != null) {
            for (String arg : args) {
                arguments.add(arg);
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void restartLauncher() {
        OS.restartLauncher(null);
    }

    /**
     * This restarts the launcher in debug mode.
     */
    public static void relaunchInDebugMode() {
        restartLauncher(new ArrayList<>(Arrays.asList("--debug", "--debug-level 3")));
    }

    /**
     * Copies the given text to the users clipboard.
     */
    public static void copyToClipboard(String data) {
        StringSelection text = new StringSelection(data);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(text, null);
    }
}
