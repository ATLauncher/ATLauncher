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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.atlauncher.managers.LogManager;
import com.atlauncher.utils.walker.DeleteDirVisitor;

public class FileUtils {
    public static boolean delete(Path path) {
        if (!Files.exists(path)) {
            LogManager.error("Couldn't delete " + path + " as it doesn't exist!");
            return false;
        }

        if (Files.isSymbolicLink(path)) {
            LogManager.error("Not deleting " + path + " as it's a symlink!");
            return false;
        }

        if (Files.isDirectory(path)) {
            return deleteDirectory(path);
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            LogManager.logStackTrace("Path " + path + " couldn't be deleted!", e, false);
            return false;
        }

        return true;
    }

    public static boolean deleteDirectory(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            LogManager.error("Cannot delete directory " + dir + " as it doesn't exist or isn't a directory!");
            return false;
        }

        try {
            Files.walkFileTree(dir, new DeleteDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace("Error trying to delete the directory " + dir, e, false);
            return false;
        }

        return true;
    }

    public static boolean createDirectory(Path directory) {
        if (Files.exists(directory)) {
            if (Files.isDirectory(directory)) {
                return true;
            }

            // It exists but is not a directory so delete it
            delete(directory);
        }

        Path path = directory.getParent();
        if (!Files.exists(path) || !Files.isDirectory(path)) {
            if (!createDirectory(path)) {
                return false;
            }
        }

        try {
            Files.createDirectory(directory);
            return true;
        } catch (IOException e) {
            LogManager.logStackTrace("Error creating directory " + directory, e, false);
        }

        return false;
    }

    public static boolean copyFile(Path from, Path to) {
        return copyFile(from, to, false);
    }

    public static boolean copyFile(Path from, Path to, boolean withFilename) {
        LogManager.debug("Copying file from " + from + " to " + to);
        if (!Files.isRegularFile(from)) {
            LogManager.error("File " + from + " cannot be copied to " + to + " as it isn't a file!");
            return false;
        }

        if (!Files.exists(from)) {
            LogManager.error("File " + from + " cannot be copied to " + to + " as it doesn't exist!");
            return false;
        }

        if (!withFilename) {
            to = to.resolve(from.getFileName());
        }

        if (!Files.exists(to.getParent())) {
            FileUtils.createDirectory(to.getParent());
        }

        // If they're the same file, but different cases, then rename it using old File
        // types
        try {
            if (Files.exists(to) && Files.isSameFile(from, to)
                    && !from.getFileName().toString().equals(to.getFileName().toString())) {
                return from.toFile().renameTo(to.toFile());
            }
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to copy file " + from + " to " + to, e);
            return false;
        }

        try {
            Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LogManager.logStackTrace("Failed to copy file " + from + " to " + to, e);
            return false;
        }

        return true;
    }

    public static boolean moveFile(Path from, Path to) {
        return moveFile(from, to, false);
    }

    public static boolean moveFile(Path from, Path to, boolean withFilename) {
        if (copyFile(from, to, withFilename)) {
            try {
                // Don't delete the from file in case it's the same file such as on case
                // insensitive file systems
                if (!Files.isSameFile(from, to)) {
                    FileUtils.delete(from);
                }
            } catch (IOException e) {
                LogManager.logStackTrace("Couldn't delete file " + from + " while renaming to " + to, e);
            }
            return true;
        } else {
            LogManager.error("Couldn't move file " + from + " to " + to);
            return false;
        }
    }

    public static boolean directoryIsEmpty(Path path) throws IOException {
        if (Files.isDirectory(path)) {
            try (Stream<Path> entries = Files.list(path)) {
                return !entries.findFirst().isPresent();
            }
        }

        return false;
    }
}
