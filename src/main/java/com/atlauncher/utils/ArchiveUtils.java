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
package com.atlauncher.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import com.atlauncher.managers.LogManager;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

public class ArchiveUtils {
    public static boolean archiveContainsFile(Path archivePath, String file) {
        try {
            return ZipUtil.containsEntry(archivePath.toFile(), file);
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to check if archive contains file in " + archivePath.toAbsolutePath());
        }

        boolean found = false;

        try (InputStream is = Files.newInputStream(archivePath);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("ZIP", is)) {
            ArchiveEntry entry = null;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }

                if (entry.getName().equals(file)) {
                    found = true;
                    break;
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return found;
    }

    public static String getFile(Path archivePath, String file) {
        try {
            return new String(ZipUtil.unpackEntry(archivePath.toFile(), file));
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.warn(
                    "Failed to get contents of file in " + archivePath.toAbsolutePath() + ". Trying fallback method");
        }

        String contents = null;

        try (InputStream is = Files.newInputStream(archivePath);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("ZIP", is)) {
            ArchiveEntry entry = null;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }

                if (entry.getName().equals(file)) {
                    contents = new String(IOUtils.toByteArray(ais));
                    break;
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return contents;
    }

    public static void extract(Path archivePath, Path extractToPath) {
        extract(archivePath, extractToPath, name -> name);
    }

    public static void extract(Path archivePath, Path extractToPath, NameMapper nameMapper) {
        try {
            ZipUtil.unpack(archivePath.toFile(), extractToPath.toFile(), nameMapper);
            return;
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to extract " + archivePath.toAbsolutePath());
        }

        try (InputStream is = Files.newInputStream(archivePath);
                ArchiveInputStream ais = new ArchiveStreamFactory().createArchiveInputStream("ZIP", is)) {
            ArchiveEntry entry = null;
            while ((entry = ais.getNextEntry()) != null) {
                if (!ais.canReadEntryData(entry)) {
                    continue;
                }

                Path outputPath;
                String fileName = nameMapper.map(entry.getName());

                if (fileName == null) {
                    continue;
                }

                try {
                    outputPath = extractToPath.resolve(fileName);
                } catch (InvalidPathException e) {
                    String newFilename = fileName.replaceAll("[:*\\?\"<>|]", "");
                    LogManager
                            .warn(String.format("InvalidPath when extracting file with name of '%s'. Renaming to '%s'",
                                    fileName, newFilename));
                    outputPath = extractToPath.resolve(newFilename);
                }

                File f = outputPath.toFile();
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("Failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (OutputStream o = Files.newOutputStream(f.toPath())) {
                        IOUtils.copy(ais, o);
                    }
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return;
    }

    public static void createZip(Path pathToCompress, Path archivePath) {
        createZip(pathToCompress, archivePath, name -> name);
    }

    public static void createZip(Path pathToCompress, Path archivePath, NameMapper nameMapper) {
        try {
            ZipUtil.pack(pathToCompress.toFile(), archivePath.toFile(), nameMapper);
            return;
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to create zip " + archivePath.toAbsolutePath() + " from "
                    + pathToCompress.toAbsolutePath());
        }

        try (OutputStream os = Files.newOutputStream(archivePath);
                ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream("ZIP", os)) {

            Files.walkFileTree(pathToCompress, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
                    // only copy files, no symbolic links or directories
                    if (attributes.isSymbolicLink() || attributes.isDirectory()) {
                        return FileVisitResult.CONTINUE;
                    }

                    // get filename
                    String fileName = nameMapper.map(pathToCompress.relativize(file).toString());

                    if (fileName == null) {
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        ArchiveEntry entry = aos.createArchiveEntry(file.toFile(), fileName);
                        aos.putArchiveEntry(entry);
                        Files.copy(file, aos);
                        aos.closeArchiveEntry();

                    } catch (IOException e) {
                        LogManager.logStackTrace(String.format("Unable to add %s to zip", file), e);
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException e) {
                    LogManager.logStackTrace(String.format("Unable to add %s to zip", file), e);
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return;
    }
}
