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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlauncher.utils.OS;

public class OperatingSystemRule {
    public String name;
    public String version;
    public String arch;

    public boolean applies() {
        if (name == null) {
            return true;
        }

        if (name.equalsIgnoreCase("osx") && !OS.isMac()) {
            return false;
        }

        if (name.equalsIgnoreCase("windows") && !OS.isWindows()) {
            return false;
        }

        if (name.equalsIgnoreCase("linux") && !OS.isLinux()) {
            return false;
        }

        if (arch != null && ((arch.equalsIgnoreCase("x86") && OS.is64Bit())
                || (arch.equalsIgnoreCase("x64") && !OS.is64Bit()))) {
            return false;
        }

        if (version == null) {
            return true;
        }

        Pattern pattern = Pattern.compile(version);
        Matcher matcher = pattern.matcher(OS.getVersion());

        return matcher.find();
    }
}
