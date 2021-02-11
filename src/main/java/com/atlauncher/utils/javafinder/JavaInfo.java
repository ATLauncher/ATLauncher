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
package com.atlauncher.utils.javafinder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.FileSystem;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.Utils;

public class JavaInfo {
    public String path;
    public String rootPath;
    public String version;
    public Integer majorVersion;
    public Integer minorVersion;
    public boolean is64bits;
    public boolean isRuntime;

    private static final Map<String, String> versionInfos = new HashMap<>();

    public JavaInfo(String javaPath) {
        String versionInfo = versionInfos.get(javaPath);

        if (versionInfo == null) {
            versionInfo = Utils.runProcess(javaPath, "-version");
            JavaInfo.versionInfos.put(javaPath, versionInfo);
        }

        String[] tokens = versionInfo.split("\"");

        if (tokens.length < 2) {
            this.version = "Unknown";
        } else {
            this.version = tokens[1];
            this.majorVersion = Java.parseJavaVersionNumber(this.version);
            this.minorVersion = Java.parseJavaBuildVersion(this.version);
        }

        this.is64bits = versionInfo.toUpperCase().contains("64-BIT");
        this.path = javaPath;
        this.rootPath = new File(javaPath).getParentFile().getParentFile().getAbsolutePath();

        try {
            this.isRuntime = Files.isSameFile(FileSystem.RUNTIMES, Paths.get(this.rootPath).getParent());
        } catch (Exception e) {
            this.isRuntime = false;
        }
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, Integer majorVersion, Integer minorVersion,
            boolean is64bits, boolean isRuntime) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.is64bits = is64bits;
        this.isRuntime = isRuntime;
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, Integer majorVersion, Integer minorVersion,
            boolean is64bits) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.is64bits = is64bits;
    }

    // used for testing
    public JavaInfo(String path, String rootPath, String version, boolean is64bits) {
        this.path = path;
        this.rootPath = rootPath;
        this.version = version;
        this.is64bits = is64bits;
    }

    public String toString() {
        return this.path + " (" + (this.is64bits ? "64-bit" : "32-bit") + ")";
    }
}
