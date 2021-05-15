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
package com.atlauncher.data.minecraft;

import java.util.List;
import java.util.Map;

import com.atlauncher.annot.Json;
import com.atlauncher.utils.OS;
import com.google.gson.annotations.SerializedName;

@Json
public class JavaRuntimes {
    public Map<String, List<JavaRuntime>> gamecore;

    public Map<String, List<JavaRuntime>> linux;
    @SerializedName("linux-i386")
    public Map<String, List<JavaRuntime>> linuxI386;

    @SerializedName("mac-os")
    public Map<String, List<JavaRuntime>> macOs;

    @SerializedName("windows-x64")
    public Map<String, List<JavaRuntime>> windowsX64;
    @SerializedName("windows-x86")
    public Map<String, List<JavaRuntime>> windowsX86;

    public Map<String, List<JavaRuntime>> getForSystem() {
        switch (OS.getOS()) {
            case WINDOWS:
                if (!OS.is64Bit()) {
                    return windowsX86;
                }

                return windowsX64;
            case OSX:
                return macOs;
            case LINUX:
                if (!OS.is64Bit()) {
                    return linuxI386;
                }

                return linux;
        }

        return null;
    }

    public static String getSystem() {
        switch (OS.getOS()) {
            case WINDOWS:
                if (!OS.is64Bit()) {
                    return "windows-x86";
                }

                return "windows-x64";
            case OSX:
                return "mac-os";
            case LINUX:
                if (!OS.is64Bit()) {
                    return "linux-i386";
                }

                return "linux";
        }

        return "unknown";
    }
}
