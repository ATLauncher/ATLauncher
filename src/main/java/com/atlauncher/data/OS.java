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
package com.atlauncher.data;

import com.atlauncher.FileSystem;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    public static Path storagePath() {
        switch (getOS()) {
            case WINDOWS:
                return Paths.get(System.getenv("APPDATA")).resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
            case OSX:
                return Paths.get(System.getenv("user.home")).resolve("Library").resolve("Application Support")
                        .resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
            default:
                return Paths.get(System.getenv("user.home")).resolve("." + Constants.LAUNCHER_NAME.toLowerCase());
        }
    }

    public static boolean isUsingMacApp() {
        return OS.isMac() && Files.exists(FileSystem.BASE_DIR.getParent().resolve("MacOS"));
    }
}
