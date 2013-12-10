/**
 * Copyright 2013 by ATLauncher and Contributors
 *
 * ATLauncher is licensed under CC BY-NC-ND 3.0 which allows others you to
 * share this software with others as long as you credit us by linking to our
 * website at http://www.atlauncher.com. You also cannot modify the application
 * in any way or make commercial use of this software.
 *
 * Link to license: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */
package com.atlauncher.data.mojang;

public enum OperatingSystem {
    LINUX("linux"), WINDOWS("windows"), OSX("osx");

    private final String name;

    private OperatingSystem(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public static OperatingSystem getOS() {
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            return OperatingSystem.WINDOWS;
        } else if (osName.contains("mac")) {
            return OperatingSystem.OSX;
        } else {
            return OperatingSystem.LINUX;
        }
    }

    public static String getVersion() {
        return System.getProperty("os.version");
    }

}
