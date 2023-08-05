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
package com.atlauncher.utils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.jar.JarFile;

import com.atlauncher.managers.LogManager;

import me.cortex.jarscanner.Detector;

public class SecurityUtils {
    public static List<Path> scanForFractureiser(List<Path> paths) throws InterruptedException {
        Function<String, String> logOutput = outputString -> {
            LogManager.error(outputString);
            return outputString;
        };

        List<Path> infectionsFound = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (final Path path : paths) {
            executor.submit(() -> {
                LogManager.debug(String.format("Scanning %s for Fractureiser", path.toAbsolutePath().toString()));

                try (JarFile scannableJarFile = new JarFile(path.toFile())) {
                    if (Detector.scan(scannableJarFile, path, logOutput)) {
                        infectionsFound.add(path);
                    }
                } catch (Exception e) {
                    LogManager.error(
                            String.format("Failed to scan %s for Fractureiser", path.toAbsolutePath().toString()));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        return infectionsFound;
    }
}
