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
package com.atlauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.atlauncher.utils.Utils;

public class Update {
    public static void main(String[] args) {
        String launcherPath = args[0];
        String temporaryUpdatePath = args[1];
        File launcher = new File(launcherPath);
        File temporaryUpdate = new File(temporaryUpdatePath);
        Utils.copyFile(temporaryUpdate, launcher.getParentFile());

        List<String> arguments = new ArrayList<String>();

        if (Utils.isMac() && new File(new File(System.getProperty("user.dir")).getParentFile().getParentFile(),
                "MacOS").exists()) {
            arguments.add("open");
            arguments.add(new File(System.getProperty("user.dir")).getParentFile().getParentFile().getParentFile()
                    .getAbsolutePath());

        } else {
            String path = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            if (Utils.isWindows()) {
                path += "w";
            }
            arguments.add(path);
            arguments.add("-jar");
            arguments.add(launcherPath);
            arguments.add("--updated=true");
        }

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(arguments);

        try {
            processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
