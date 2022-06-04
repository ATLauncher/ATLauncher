/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
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
package com.atlauncher.extension;

import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.OS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * 04 / 06 / 2022
 */
public class ExtensionUtils {
    public static boolean hasFlatpakExtension() {
        return OS.isUsingFlatpak() && new File("/app/bin/atlauncherx-flatpak.jar").exists();
    }

    /**
     * Uses another jar to handle loading files in flatpak
     */
    public static File[] selectFilesFromExtension() {
        LogManager.debug("Using separate jar");

        String command = "java -jar /app/bin/atlauncherx-flatpak.jar";
        Process child = null;
        try {
            LogManager.debug("Executing process");
            child = Runtime.getRuntime().exec(command);

            {
                BufferedReader br = new BufferedReader(
                    new InputStreamReader(child.getErrorStream())
                );

                String s;
                while ((s = br.readLine()) != null) {
                    LogManager.error(s);
                }
            }

            BufferedReader br = new BufferedReader(
                new InputStreamReader(child.getInputStream())
            );
            StringBuilder builder = new StringBuilder();
            String s;
            while ((s = br.readLine()) != null) {
                builder.append(s);
            }

            LogManager.debug("Waiting for process to finish");
            int resultCode = child.waitFor();
            LogManager.debug("Final code: " + resultCode);

            // We can be guaranteed that this will always be either empty, "[]", or "[...]", or "[...,...]"
            String result = builder.toString();
            LogManager.debug("Result string: '" + result + "'");

            String[] selectedFiles;

            if (result.length() >= 2) {
                LogManager.debug("Spitting path");
                selectedFiles = result.substring(1, result.length() - 1)
                    .split(",");
            } else {
                LogManager.debug("Too small, returning default");
                return new File[0];
            }

            return Arrays.stream(selectedFiles)
                .map(File::new).toArray(size -> new File[selectedFiles.length]);
        } catch (IOException | InterruptedException e) {
            LogManager.debug("Exception occurred");
            LogManager.logStackTrace(e);
        } finally {
            LogManager.debug("Destroying process");
            if (child != null)
                child.destroy();
        }
        return new File[0];
    }
}
