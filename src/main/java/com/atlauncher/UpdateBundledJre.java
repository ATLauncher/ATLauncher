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
package com.atlauncher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.Utils;

public class UpdateBundledJre {
    public static void main(String[] args) {
        File newBundledJrePath = new File(args[0]);
        File oldBundledJrePath = new File(args[1]);
        String launcherPath = args[2];
        String[] otherArgs = Arrays.copyOfRange(args, 3, args.length);

        int tries = 0;
        while (oldBundledJrePath.exists() && tries++ < 10) {
            try {
                FileUtils.deleteDirectoryQuietly(oldBundledJrePath.toPath());
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // ignored
            }
        }

        Utils.copyDirectory(newBundledJrePath, oldBundledJrePath);

        List<String> arguments = new ArrayList<>();

        arguments.add(Java.getPathToJavaExecutable(oldBundledJrePath.toPath()));
        arguments.add("-Djna.nosys=true");
        arguments.add("-jar");
        arguments.add(launcherPath);

        arguments.add("--updatedBundledJre=true");

        // pass in all the other arguments
        arguments.addAll(Arrays.asList(otherArgs));

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
}
