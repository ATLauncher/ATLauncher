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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import com.atlauncher.managers.LogManager;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.zeroturnaround.zip.ZipException;
import org.zeroturnaround.zip.ZipUtil;

public class ArchiveUtils {
    public static boolean archiveContainsFile(Path archivePath, String file) {
        try {
            return ZipUtil.containsEntry(archivePath.toFile(), file);
        } catch (ZipException e) {
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
        } catch (ZipException e) {
            // allow this to fail as we can fallback to Apache Commons library
            LogManager.error("Failed to get contents of file in " + archivePath.toAbsolutePath());
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
        try {
            ZipUtil.unpack(archivePath.toFile(), extractToPath.toFile());
            return;
        } catch (ZipException e) {
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

                try {
                    outputPath = extractToPath.resolve(entry.getName());
                } catch (InvalidPathException e) {
                    LogManager.logStackTrace("InvalidPath when extracting file with name of " + entry.getName(), e);
                    outputPath = extractToPath.resolve(entry.getName().replaceAll("[:*\\?\"<>|]", ""));
                }

                File f = outputPath.toFile();
                if (entry.isDirectory()) {
                    if (!f.isDirectory() && !f.mkdirs()) {
                        throw new IOException("failed to create directory " + f);
                    }
                } else {
                    File parent = f.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("failed to create directory " + parent);
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
}
