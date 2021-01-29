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

import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
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
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.FileSystem;
import com.atlauncher.Update;
import com.atlauncher.constants.Constants;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.network.Analytics;
import com.atlauncher.utils.javafinder.JavaInfo;

import oshi.SystemInfo;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;

public enum OS {
    LINUX, WINDOWS, OSX;

    private static int memory = 0;
    private static SystemInfo systemInfo = null;
    private static Integer memoryFromTool = null;

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
        return OS.isMac() && Files.isDirectory(FileSystem.BASE_DIR.getParent().resolve("MacOS"));
    }

    /**
     * This opens the users default browser to the given url.
     */
    public static void openWebBrowser(String url) {
        try {
            Analytics.sendOutboundLink(url);
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
            if (getOS() == LINUX && Utils.executableInPath("xdg-open")) {
                Runtime.getRuntime().exec("xdg-open " + uri);
            } else if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(uri);
            }
        } catch (Exception e) {
            LogManager.logStackTrace("Error opening web browser!", e);
        }
    }

    public static void openFileExplorer(Path path) {
        openFileExplorer(path, false);
    }

    /**
     * Opens the system file explorer to the given path.
     */
    public static void openFileExplorer(Path path, boolean toFile) {
        try {
            if ((toFile || !Files.isDirectory(path)) && OS.isWindows()) {
                LogManager.info("/select," + path.toAbsolutePath());
                Runtime.getRuntime().exec("explorer /select," + path.toAbsolutePath());
            } else {
                Path pathToOpen = path;

                if (!Files.isDirectory(path)) {
                    pathToOpen = path.getParent();
                }

                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(pathToOpen.toFile());
                } else if (getOS() == LINUX && (Files.exists(Paths.get("/usr/bin/xdg-open"))
                        || Files.exists(Paths.get("/usr/local/bin/xdg-open")))) {
                    Runtime.getRuntime().exec("xdg-open " + pathToOpen.toString());
                }
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
        String preferredPath = OS.getPreferredJavaPath(Java.getInstalledJavas());

        if (preferredPath == null) {
            return OS.getJavaHome();
        }

        return preferredPath;
    }

    public static String getPreferredJavaPath(List<JavaInfo> installedJavas) {
        JavaInfo preferredJava = getPreferredJava(installedJavas);
        if (preferredJava == null) {
            return null;
        }

        return preferredJava.rootPath;
    }

    public static JavaInfo getPreferredJava(List<JavaInfo> installedJavas) {
        if (installedJavas.size() == 0) {
            return null;
        }

        List<JavaInfo> validVersions = installedJavas.stream()
                .filter(javaInfo -> javaInfo.majorVersion != null && javaInfo.minorVersion != null)
                .collect(Collectors.toList());

        if (validVersions.size() == 0) {
            return null;
        }

        // prefer the downloaded runtime if it's installed
        Optional<JavaInfo> runtimeJava = validVersions.stream().filter(javaInfo -> javaInfo.isRuntime).findFirst();
        if (runtimeJava.isPresent()) {
            return runtimeJava.get();
        }

        // get newest Java 8 64 bit if installed
        Optional<JavaInfo> java864bit = validVersions.stream()
                .sorted(Comparator.comparingInt((JavaInfo javaInfo) -> javaInfo.minorVersion).reversed())
                .filter(javaInfo -> javaInfo.majorVersion == 8 && javaInfo.is64bits).findFirst();
        if (java864bit.isPresent()) {
            return java864bit.get();
        }

        // get newest 64 bit if installed
        Optional<JavaInfo> java64bit = validVersions.stream().filter(javaInfo -> javaInfo.is64bits).findFirst();
        return java64bit.orElseGet(() -> validVersions.get(0));

        // default to the first java installed
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
     * Checks if using Arm.
     */
    public static boolean isArm() {
        return System.getProperty("os.arch").startsWith("arm");
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
        PerformanceManager.start();
        long ramm;
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
        PerformanceManager.end();
        return ram;
    }

    /**
     * Returns the amount of RAM in the users system via the oshi (or if that fails
     * the getMemory tool).
     */
    public static int getSystemRamViaTool() {
        PerformanceManager.start();

        int ram;

        try {
            SystemInfo systemInfo = getSystemInfo();
            HardwareAbstractionLayer hal = systemInfo.getHardware();
            GlobalMemory globalMemory = hal.getMemory();

            ram = (int) (globalMemory.getTotal() / 1048576);
        } catch (Throwable t) {
            LogManager.logStackTrace(t);

            // getSystemInfo returned null/wrong, so use the old getMemory binary
            ram = getMemoryFromTool();
        }

        PerformanceManager.end();

        return ram;
    }

    /**
     * Returns the amount of RAM in the users system via the getMemory tool.
     */
    public static int getMemoryFromTool() {
        if (memoryFromTool == null) {
            PerformanceManager.start();

            String binaryFile = "getMemory";

            if (OS.isArm()) {
                binaryFile += "-arm";

                if (OS.is64Bit()) {
                    binaryFile += "64";
                }
            } else {
                if (OS.is64Bit()) {
                    binaryFile += "-x64";
                }
            }

            if (OS.isWindows()) {
                binaryFile += ".exe";
            } else if (OS.isMac()) {
                binaryFile += "-osx";
            } else if (OS.isLinux()) {
                binaryFile += "-linux";
            }

            if (Files.exists(FileSystem.TOOLS.resolve(binaryFile))) {
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder(
                            FileSystem.TOOLS.resolve(binaryFile).toAbsolutePath().toString());
                    processBuilder.directory(FileSystem.TOOLS.toFile());
                    processBuilder.redirectErrorStream(true);

                    Process process = processBuilder.start();
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        memoryFromTool = (int) (Long.parseLong(br.readLine()) / 1048576);
                    }
                } catch (IOException e) {
                    LogManager.logStackTrace(e);
                }
            }
            PerformanceManager.end();
        }

        return memoryFromTool;
    }

    /**
     * Returns the system information via the getSystemInfo tool.
     */
    public static SystemInfo getSystemInfo() {
        if (systemInfo == null) {
            PerformanceManager.start();
            systemInfo = new SystemInfo();
            PerformanceManager.end();
        }

        return systemInfo;
    }

    /**
     * Returns the amount of RAM in the users system.
     */
    public static int getSystemRam() {
        // fetch the memory from the tool/bean if it's 0
        if (memory == 0) {
            if (!Java.isSystemJavaNewerThanJava8()) {
                memory = OS.getSystemRamViaBean();
            } else {
                memory = OS.getSystemRamViaTool();
            }
        }

        return memory;
    }

    /**
     * Returns the maximum RAM available to Java. If on a 64 Bit system, then its
     * all of the System RAM otherwise its limited to 1GB or less due to allocations
     * of PermGen.
     */
    public static int getMaximumRam() {
        int maxRam = getSystemRam();
        if (!is64Bit()) {
            return Math.min(maxRam, 1024);
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

    public static Rectangle getScreenVirtualBounds() {
        Rectangle bounds = new Rectangle(0, 0, 0, 0);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

        for (GraphicsDevice gd : ge.getScreenDevices()) {
            bounds.add(gd.getDefaultConfiguration().getBounds());
        }

        return bounds;
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

        arguments.add(Java.getPathToSystemJavaExecutable());
        arguments.add("-Djna.nosys=true");
        arguments.add("-cp");
        arguments.add(path);
        arguments.add("com.atlauncher.Restart");

        // we don't need to know the path to the jar if user is using osx app
        if (!OS.isUsingMacApp()) {
            arguments.add(path);
        }

        // pass in all the original arguments
        arguments.addAll(Arrays.asList(App.PASSED_ARGS));

        if (args != null) {
            arguments.addAll(args);
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(FileSystem.BASE_DIR.toFile());
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
        relaunchInDebugMode(3);
    }

    /**
     * This restarts the launcher in debug mode.
     */
    public static void relaunchInDebugMode(int level) {
        restartLauncher(new ArrayList<>(Arrays.asList("--debug", "--debug-level=" + level)));
    }

    /**
     * Copies the given text to the users clipboard.
     */
    public static void copyToClipboard(String data) {
        StringSelection text = new StringSelection(data);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(text, null);
    }

    public static Object getUserAgentString() {
        String name = "";
        String arch = "";

        switch (getOS()) {
            case WINDOWS: {
                name = "Windows NT " + getVersion();

                if (OS.is64Bit()) {
                    arch = "; Win64; x64";
                }
                break;
            }
            case OSX: {
                // M1 machines still show Intel
                name = String.format("Macintosh; Intel %s %s", getName(), getVersion().replaceAll(".", "_"));
                break;
            }
            case LINUX: {
                name = String.format("%s; Linux", getName());

                if (OS.is64Bit()) {
                    arch = "x86_64";
                }
                break;
            }
        }

        return String.format("%s%s", name, arch);
    }
}
