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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.jar.JarFile;

import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.managers.LogManager;
import com.google.common.hash.HashCode;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;

import me.cortex.jarscanner.Detector;

public class SecurityUtils {
    // List of SHA1 hashes of files scanned as clean for Fractureiser
    public static final List<String> FRACTURISER_SCANNED_HASHES = new ArrayList<>();
    public static boolean hasLoadedFractureiserScannedHashes = false;

    public static List<Path> scanForFractureiser(List<Path> paths) throws InterruptedException {
        Function<String, String> logOutput = outputString -> {
            LogManager.error(outputString);
            return outputString;
        };

        loadFractureiserScannedHashes();

        List<Path> infectionsFound = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(4);
        for (final Path path : paths) {
            executor.submit(() -> {
                HashCode fileHash = Hashing.sha1(path);
                if (FRACTURISER_SCANNED_HASHES.contains(fileHash.toString())) {
                    LogManager.debug(String.format("%s has already been scanned for Fractureiser",
                            path.toAbsolutePath().toString()));
                    return;
                }

                LogManager.debug(String.format("Scanning %s for Fractureiser", path.toAbsolutePath().toString()));

                try (JarFile scannableJarFile = new JarFile(path.toFile())) {
                    if (Detector.scan(scannableJarFile, path, logOutput)) {
                        infectionsFound.add(path);
                    } else {
                        FRACTURISER_SCANNED_HASHES.add(fileHash.toString());
                    }
                } catch (Exception e) {
                    LogManager.error(
                            String.format("Failed to scan %s for Fractureiser", path.toAbsolutePath().toString()));
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.MINUTES);

        saveFractureiserScannedHashes();

        return infectionsFound;
    }

    private static void loadFractureiserScannedHashes() {
        if (!hasLoadedFractureiserScannedHashes && Files.exists(FileSystem.FRACTURISER_SCANNED_HASHES)) {
            try (InputStreamReader fileReader = new InputStreamReader(
                Files.newInputStream(FileSystem.FRACTURISER_SCANNED_HASHES), StandardCharsets.UTF_8)) {
                Type stringListType = new TypeToken<List<String>>() {
                }.getType();
                List<String> scannedHashes = Gsons.DEFAULT.fromJson(fileReader, stringListType);

                FRACTURISER_SCANNED_HASHES.addAll(scannedHashes);

                hasLoadedFractureiserScannedHashes = true;
            } catch (Exception e) {
                LogManager.logStackTrace("Exception loading scanned Fracturiser hashes", e);
            }
        }
    }

    private static void saveFractureiserScannedHashes() {
        try (OutputStreamWriter fileWriter = new OutputStreamWriter(
            Files.newOutputStream(FileSystem.FRACTURISER_SCANNED_HASHES), StandardCharsets.UTF_8)) {
            Type stringListType = new TypeToken<List<String>>() {
            }.getType();
            Gsons.DEFAULT.toJson(FRACTURISER_SCANNED_HASHES, stringListType, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }
}
