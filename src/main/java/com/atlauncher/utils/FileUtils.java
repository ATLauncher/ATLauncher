/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013 ATLauncher
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

import com.atlauncher.managers.LogManager;
import com.atlauncher.data.mojang.ExtractRule;
import com.atlauncher.utils.walker.ClearDirVisitor;
import com.atlauncher.utils.walker.CopyDirVisitor;
import com.atlauncher.utils.walker.DeleteDirVisitor;
import com.atlauncher.utils.walker.DeleteSpecifiedFilesVisitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class FileUtils {
    public static boolean moveFile(Path from, Path to) {
        return moveFile(from, to, false);
    }

    public static List<String> listFiles(Path dir) throws Exception {
        List<String> files = new LinkedList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                files.add(p.getFileName().toString());
            }
        }

        return files;
    }

    public static List<String> listFiles(Path dir, DirectoryStream.Filter<Path> filter) throws Exception {
        List<String> files = new LinkedList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            for (Path p : stream) {
                files.add(p.getFileName().toString());
            }
        }

        return files;
    }

    public static boolean moveFile(Path from, Path to, boolean withFilename) {
        if (copyFile(from, to, withFilename)) {
            try {
                // Don't delete the from file in case it's the same file such as on case insensitive file systems
                if (!Files.isSameFile(from, to)) {
                    FileUtils.delete(from);
                }
            } catch (IOException e) {
                LogManager.logStackTrace("Couldn't delete file " + from + " while renaming to " + to, e);
                LogManager.logStackTrace("Couldn't delete file " + from + " while renaming to " + to, e);
            }
            return true;
        } else {
            LogManager.error("Couldn't move file " + from + " to " + to);
            return false;
        }
    }

    public static boolean copyFile(Path from, Path to) {
        return copyFile(from, to, false);
    }

    /**
     * Copy file.
     *
     * @param from the path of the file to copy from
     * @param to the path of the file to copy to
     * @param withFilename the with filename
     * @return if the file was copied or not
     */
    public static boolean copyFile(Path from, Path to, boolean withFilename) {
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

        // If they're the same file, but different cases, then rename it using old File types
        try {
            if (Files.exists(to) && Files.isSameFile(from, to) && !from.getFileName().toString().equals(to
                    .getFileName().toString())) {
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

    public static boolean copyDirectory(Path from, Path to) {
        return copyDirectory(from, to, false);
    }

    public static boolean copyDirectory(Path from, Path to, boolean copyFolder) {
        if (copyFolder) {
            to = to.resolve(from.getParent().getFileName());
        }

        try {
            Files.walkFileTree(from, new CopyDirVisitor(from, to));
        } catch (IOException e) {
            LogManager.logStackTrace("Error while trying to copy files from " + from + " to " + to, e);
            return false;
        }

        return true;
    }

    public static boolean moveDirectory(Path from, Path to) {
        return copyDirectory(from, to) && deleteDirectory(from);
    }

    public static boolean deleteDirectory(Path dir) {
        if (!Files.exists(dir) || !Files.isDirectory(dir)) {
            LogManager.error("Cannot delete directory " + dir + " as it doesn't exist or isn't a directory!");
            return false;
        }

        try {
            Files.walkFileTree(dir, new DeleteDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace("Error trying to delete the directory " + dir, e);
            return false;
        }

        return true;
    }

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
            LogManager.error("Path " + path + " couldn't be deleted!");
            return false;
        }

        return true;
    }

    public static boolean deleteSpecifiedFiles(Path path, final List<String> files) {
        try {
            Files.walkFileTree(path, new DeleteSpecifiedFilesVisitor(files));
        } catch (IOException e) {
            LogManager.logStackTrace("Error while trying to delete specific files from " + path, e);
            return false;
        }

        return true;
    }

    public static void deleteContents(Path p) {
        try {
            Files.walkFileTree(p, new ClearDirVisitor());
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
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
            LogManager.logStackTrace("Error creating directory " + directory, e);
        }

        return false;
    }

    public static boolean unzip(Path in, Path out) {
        return unzip(in, out, null);
    }

    // TODO: Switch to NIO operations
    public static boolean unzip(Path in, Path out, ExtractRule extractRule) {
        if (!Files.exists(out)) {
            createDirectory(out);
        }

        ZipFile zipFile = null;
        try {
            zipFile = new ZipFile(in.toFile());
            Enumeration<?> e = zipFile.entries();
            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                String entryName = entry.getName();
                if (entry.getName().endsWith("aux.class")) {
                    entryName = "aux_class";
                }
                if (extractRule != null && extractRule.exclude.contains(entryName)) {
                    continue;
                }
                if (entry.isDirectory()) {
                    File folder = new File(out.toFile(), entryName);
                    folder.mkdirs();
                }
                File destinationFilePath = new File(out.toFile(), entryName);
                destinationFilePath.getParentFile().mkdirs();
                if (!entry.isDirectory() && !entry.getName().equals(".minecraft")) {
                    BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                    int b;
                    byte buffer[] = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(destinationFilePath);
                    BufferedOutputStream bos = new BufferedOutputStream(fos, 1024);
                    while ((b = bis.read(buffer, 0, 1024)) != -1) {
                        bos.write(buffer, 0, b);
                    }
                    bos.flush();
                    bos.close();
                    bis.close();
                }
            }
            zipFile.close();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            return false;
        }

        return true;
    }

    // TODO: NIO this up
    public static void zip(Path in, Path out) {
        File file = in.toFile();

        try {
            URI base = file.toURI();
            Deque<File> queue = new LinkedList<File>();
            queue.push(file);
            OutputStream stream = new FileOutputStream(out.toFile());
            Closeable res = stream;
            ZipOutputStream zout = null;
            try {
                zout = new ZipOutputStream(stream);
                res = zout;
                while (!queue.isEmpty()) {
                    file = queue.pop();
                    for (File kid : file.listFiles()) {
                        String name = base.relativize(kid.toURI()).getPath();
                        if (name.endsWith("aux_class")) {
                            name = "aux.class";
                        }
                        if (kid.isDirectory()) {
                            queue.push(kid);
                            name = name.endsWith("/") ? name : name + "/";
                            zout.putNextEntry(new ZipEntry(name));
                        } else {
                            zout.putNextEntry(new ZipEntry(name));
                            copy(kid, zout);
                            zout.closeEntry();
                        }
                    }
                }
            } finally {
                res.close();
                if (zout != null) {
                    zout.close();
                }
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    /**
     * Copy.
     *
     * @param in the in
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    /**
     * Copy.
     *
     * @param file the file
     * @param out the out
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private static void copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            copy(in, out);
        } finally {
            in.close();
        }
    }
}
