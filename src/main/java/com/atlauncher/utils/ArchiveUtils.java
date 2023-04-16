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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nullable;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.zeroturnaround.zip.NameMapper;
import org.zeroturnaround.zip.ZipUtil;

import com.atlauncher.managers.LogManager;

public class ArchiveUtils {
    public static boolean archiveContainsFile(Path archivePath, String file) {
        try {
            return ZipUtil.containsEntry(archivePath.toFile(), file);
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to check if archive contains file in " + archivePath.toAbsolutePath());
        }

        boolean found = false;

        try (InputStream is = createInputStream(archivePath);
                ZipArchiveInputStream zais = new ZipArchiveInputStream(is, "UTF8", true, true)) {
            ArchiveEntry entry = null;
            while ((entry = zais.getNextEntry()) != null) {
                if (!zais.canReadEntryData(entry)) {
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

    /**
     * Creates an input stream from the provided path.
     * This will handle if the path is a URI.
     *
     * @param archivePath Path to create an input stream for.
     * @return Input stream if successful, null otherwise
     */
    public static @Nullable InputStream createInputStream(Path archivePath) {
        InputStream is = null;

        try {
            if (archivePath.toString().startsWith("file:")) {
                is = new URL(archivePath.toString()).openStream();
            } else {
                is = Files.newInputStream(archivePath);
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return is;
    }

    public static String getFile(Path archivePath, String file) {
        try {
            byte[] contents = ZipUtil.unpackEntry(createInputStream(archivePath), file);

            if (contents != null) {
                return new String(contents);
            }
        } catch (Throwable t) {
            LogManager.logStackTrace(t);
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.debug(
                    "Failed to get contents of file in " + archivePath.toAbsolutePath() + ". Trying fallback method");
        }

        String contents = null;

        try {
            InputStream is = createInputStream(archivePath);
            try (
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
        } catch (Exception e) {
            LogManager.logStackTrace(e);
        }

        return contents;
    }

    public static boolean extract(Path archivePath, Path extractToPath) {
        return extract(archivePath, extractToPath, name -> name);
    }

    public static boolean extract(Path archivePath, Path extractToPath, NameMapper nameMapper) {
        try {
            ZipUtil.unpack(archivePath.toFile(), extractToPath.toFile(), nameMapper);
            return true;
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to extract " + archivePath.toAbsolutePath());
        }

        try (InputStream is = createInputStream(archivePath);
                ZipArchiveInputStream zais = new ZipArchiveInputStream(is, "UTF8", true, true)) {
            ArchiveEntry entry = null;
            while ((entry = zais.getNextEntry()) != null) {
                if (!zais.canReadEntryData(entry)) {
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
                        IOUtils.copy(zais, o);
                    }
                }
            }
        } catch (Exception e) {
            LogManager.logStackTrace(e);
            return false;
        }

        return true;
    }

    public static boolean createZip(Path pathToCompress, Path archivePath) {
        return createZip(pathToCompress, archivePath, name -> name);
    }

    public static boolean createZip(Path pathToCompress, Path archivePath, NameMapper nameMapper) {
        try {
            ZipUtil.pack(pathToCompress.toFile(), archivePath.toFile(), nameMapper);
            return true;
        } catch (Throwable t) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to create zip " + archivePath.toAbsolutePath() + " from "
                    + pathToCompress.toAbsolutePath());
        }

        // TODO, It seems that exports currently do not use dbus for dir sel,
        // it would be optimal to be aware the below line will cause problems
        // once dbus is setup for export as well
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
            return false;
        }

        return true;
    }
}
