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

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;

public class JavaFinder {
    private static SoftReference<List<String>> javaPaths = new SoftReference<>(null);

    public static List<JavaInfo> findJavas() {
        PerformanceManager.start();
        List<String> javaExecs = javaPaths.get();

        if (javaExecs == null) {
            javaExecs = new ArrayList<>();

            if (OS.isWindows()) {
                if (OS.is64Bit()) {
                    javaExecs.addAll(scanWindowsRegistry(64));
                } else {
                    javaExecs.addAll(scanWindowsRegistry(32));
                }

                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/bin/java.exe");

                String[] pathsToSearch = { "Java", "Amazon Corretto", "AdoptOpenJDK" };

                for (String searchPath : pathsToSearch) {
                    List<String> foundPaths = new ArrayList<>();

                    try {
                        Files.walkFileTree(Paths.get(System.getenv("programfiles"), searchPath),
                                EnumSet.noneOf(FileVisitOption.class), 10, new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (pathMatcher.matches(path)) {
                                            foundPaths.add(path.toString());
                                        }

                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                    } catch (Exception ignored) {
                    }

                    if (foundPaths.size() != 0) {
                        javaExecs.addAll(foundPaths);
                    }
                }
            }

            if (OS.isLinux()) {
                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/bin/java");

                String[] pathsToSearch = { "/usr/java", "/usr/lib/jvm", "/usr/lib32/jvm" };

                for (String searchPath : pathsToSearch) {
                    List<String> foundPaths = new ArrayList<>();

                    try {
                        Files.walkFileTree(Paths.get(searchPath), EnumSet.noneOf(FileVisitOption.class), 10,
                                new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
                                            throws IOException {
                                        if (pathMatcher.matches(path)) {
                                            foundPaths.add(path.toString());
                                        }

                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                    } catch (Exception ignored) {
                    }

                    if (foundPaths.size() != 0) {
                        javaExecs.addAll(foundPaths);
                    }
                }
            }

            javaPaths = new SoftReference<>(javaExecs);
        }

        PerformanceManager.end();
        return javaExecs.stream().distinct().filter(java -> Files.exists(Paths.get(java))).map(JavaInfo::new)
                .collect(Collectors.toList());
    }

    // Inspired by
    // https://github.com/TechnicPack/LauncherV3/blob/a8067879fea995fbb780d3b67c4ce74a17152ea4/src/main/java/net/technicpack/launchercore/launch/java/source/os/WinRegistryJavaSource.java
    private static List<String> scanWindowsRegistry(int bitness) {
        List<String> versions = new ArrayList<>();

        String output = Utils.runProcess("reg", "query", "HKEY_LOCAL_MACHINE\\Software\\JavaSoft\\", "/f", "Home", "/t",
                "REG_SZ", "/s", "/reg:" + bitness);

        for (String line : output.split("\\r?\\n")) {
            if (line.contains("REG_SZ")) {
                String javaPath = line.substring(line.indexOf("REG_SZ") + 6).trim() + "\\bin\\java.exe";

                if (Files.exists(Paths.get(javaPath))) {
                    versions.add(javaPath);
                }
            }
        }

        return versions;
    }
}
