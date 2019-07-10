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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.atlauncher.LogManager;
import com.atlauncher.data.Constants;

/**
 * Various utility methods for interacting with the ATLauncher API.
 */
public class ATLauncherAPIUtils {
    public static void postSystemInfo() {
        Map<String, Object> request = new HashMap<>();

        request.put("launcher_version", Constants.VERSION.toString());
        request.put("os_name", OS.getOS().toString().toLowerCase());
        request.put("os_version", System.getProperty("os.version"));
        request.put("java_version", System.getProperty("java.version"));
        request.put("ram", OS.getSystemRam());
        request.put("64_bit", OS.is64Bit());

        try {
            Utils.sendAPICall("system-info", request);
        } catch (IOException e) {
            LogManager.logStackTrace("Error sending in details of system", e);
        }
    }
}
