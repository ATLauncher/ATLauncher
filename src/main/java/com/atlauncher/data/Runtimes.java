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
package com.atlauncher.data;

import com.atlauncher.annot.Json;
import com.atlauncher.utils.OS;

@Json
public class Runtimes {
    public RuntimesOS osx;
    public RuntimesOS windows;

    public Runtime getRuntimeForOS() {
        if (OS.isLinux()) {
            return null;
        }

        RuntimesOS metaForOS = OS.isWindows() ? this.windows : this.osx;
        Runtime runtime = null;

        if (OS.is64Bit() && metaForOS.x64 != null) {
            runtime = metaForOS.x64;
        } else if (!OS.is64Bit() && metaForOS.x86 != null) {
            runtime = metaForOS.x86;
        }

        return runtime;
    }
}
