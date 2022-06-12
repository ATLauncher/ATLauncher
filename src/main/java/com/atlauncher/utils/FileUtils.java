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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import com.atlauncher.App;
import com.atlauncher.utils.walker.DeleteDirVisitor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileUtils {
    private static final Logger LOG = LogManager.getLogger(FileUtils.class);

    public static boolean delete(Path path) {
        return delete(path, false);
    }

    public static boolean delete(Path path, boolean recycle) {
        if (!Files.exists(path)) {
            LOG.error("Couldn't delete {} as it doesn't exist!", path);
            return false;
        }

        if (Files.isSymbolicLink(path)) {
            LOG.error("Not deleting {} as it's a symlink!", path);
            return false;
        }

        if (recycle && App.settings.useRecycleBin) {
            return recycle(path);
        }

        if (Files.isDirectory(path)) {
            return deleteDirectory(path);
        }

        try {
            Files.delete(path);
        } catch (IOException e){
            LOG.error("Path {} couldn't be deleted:", path, e);//don't send
            return false;
        }

        return true;
    }

    private static boolean recycle(Path path) {
        if (!Files.exists(path)) {
            LOG.error("Cannot recycle " + path + " as it doesn't exist.");
            return false;
        }

        com.sun.jna.platform.FileUtils fileUtils = com.sun.jna.platform.FileUtils.getInstance();
        if (fileUtils.hasTrash()) {
            try {
                fileUtils.moveToTrash(new File[] { path.toFile() });
                return true;
            } catch (IOException e) {
                return delete(path, false);
            }
        } else {
            return delete(path, false);
        }
    }

    public static boolean deleteDirectory(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            LOG.error("Cannot delete directory {} as it doesn't exist or isn't a directory!", dir);
            return false;
        }

        try {
            Files.walkFileTree(dir, new DeleteDirVisitor());
        } catch (IOException e){
            LOG.error("Error trying to delete the directory {}", dir, e);//don't send
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
        } catch (IOException e){
            LOG.error("Error creating directory {}", directory, e);//don't send
        }

        return false;
    }

    public static boolean copyFile(Path from, Path to) {
        return copyFile(from, to, false);
    }

    public static boolean copyFile(Path from, Path to, boolean withFilename) {
        LOG.debug("Copying file from {} to {}", from, to);
        if (!Files.isRegularFile(from)) {
            LOG.error("File {} cannot be copied to {} as it isn't a file!", from, to);
            return false;
        }

        if (!Files.exists(from)) {
            LOG.error("File {} cannot be copied to {} as it doesn't exist!", from, to);
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
            LOG.error("Failed to copy file " + from + " to " + to, e);
            return false;
        }

        try {
            Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOG.error("Failed to copy file " + from + " to " + to, e);
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
                LOG.error("Couldn't delete file " + from + " while renaming to " + to, e);
            }
            return true;
        } else {
            LOG.error("Couldn't move file {} to {}", from, to);
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
