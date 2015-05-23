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

import com.atlauncher.App;
import com.atlauncher.LogManager;
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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
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

    /**
     * @deprecated use moveFile(Path, Path, boolean)
     */
    public static boolean moveFile(File from, File to, boolean withFilename) {
        return moveFile(from.toPath(), to.toPath(), withFilename);
    }

    public static boolean moveFile(Path from, Path to, boolean withFilename) {
        if (copyFile(from, to, withFilename)) {
            delete(from);
            return true;
        } else {
            LogManager.error("Couldn't move file " + from + " to " + to);
            return false;
        }
    }

    /**
     * @deprecated Use copyFile(Path, Path) instead
     */
    public static boolean copyFile(File from, File to) {
        return copyFile(from.toPath(), to.toPath(), false);
    }

    public static boolean copyFile(Path from, Path to) {
        return copyFile(from, to, false);
    }

    /**
     * @deprecated Use copyFile(Path, Path, Boolean) instead
     */
    public static boolean copyFile(File from, File to, boolean withFilename) {
        return copyFile(from.toPath(), to.toPath(), withFilename);
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

        try {
            Files.copy(from, to, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            App.settings.logStackTrace("Failed to copy file " + from + " to " + to, e);
            return false;
        }

        return true;
    }

    /**
     * Move directory.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @return true, if successful
     */
    public static boolean moveDirectory(File sourceLocation, File targetLocation) {
        if (copyDirectory(sourceLocation, targetLocation)) {
            delete(sourceLocation);
            return true;
        } else {
            LogManager.error("Couldn't move directory " + sourceLocation.getAbsolutePath() + " to " + targetLocation
                    .getAbsolutePath());
            return false;
        }
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
            App.settings.logStackTrace("Error while trying to copy files from " + from + " to " + to, e);
            return false;
        }

        return true;
    }

    public static boolean moveDirectory(Path from, Path to) {
        return copyDirectory(from, to) && deleteDirectory(from);
    }

    /**
     * @deprecated use copyDirectory(Path, Path)
     */
    public static boolean copyDirectory(File sourceLocation, File targetLocation) {
        return copyDirectory(sourceLocation.toPath(), targetLocation.toPath(), false);
    }

    /**
     * @deprecated use copyDirectory(Path, Path, boolean)
     */
    public static boolean copyDirectory(File sourceLocation, File targetLocation, boolean copyFolder) {
        if (copyFolder) {
            targetLocation = new File(targetLocation, sourceLocation.getName());
        }
        try {
            if (sourceLocation.isDirectory()) {
                if (!targetLocation.exists()) {
                    targetLocation.mkdirs();
                }

                String[] children = sourceLocation.list();
                for (int i = 0; i < children.length; i++) {
                    copyDirectory(new File(sourceLocation, children[i]), new File(targetLocation, children[i]));
                }
            } else {

                InputStream in = new FileInputStream(sourceLocation);
                OutputStream out = new FileOutputStream(targetLocation);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
            App.settings.logStackTrace("Error trying to delete the directory " + dir, e);
            return false;
        }

        return true;
    }

    /**
     * @deprecated Use delete(Path)
     */
    public static boolean delete(File file) {
        return delete(file.toPath());
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

    public static void deleteWithFilter(File file, final List<String> filesToIgnore) {
        FilenameFilter ffFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return !filesToIgnore.contains(name);
            }
        };
        for (File aFile : file.listFiles(ffFilter)) {
            delete(aFile);
        }
    }

    public static boolean deleteSpecifiedFiles(Path path, final List<String> files) {
        try {
            Files.walkFileTree(path, new DeleteSpecifiedFilesVisitor(files));
        } catch (IOException e) {
            App.settings.logStackTrace("Error while trying to delete specific files from " + path, e);
            return false;
        }

        return true;
    }

    /**
     * Delete contents.
     *
     * @param file the file
     */
    public static void deleteContents(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null) {
                // No contents in this folder so there are no files to delete
                return;
            }
            for (File c : files) {
                delete(c);
            }
        }
    }

    public static void deleteContents(Path p) {
        try {
            Files.walkFileTree(p, new ClearDirVisitor());
        } catch (IOException e) {
            App.settings.logStackTrace(e);
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
            App.settings.logStackTrace("Error creating directory " + directory, e);
        }

        return false;
    }

    /**
     * @deprecated use unzip(Path, Path, ExtractRule)
     */
    public static boolean unzip(File in, File out) {
        return unzip(in.toPath(), out.toPath(), null);
    }

    /**
     * @deprecated use unzip(Path, Path, ExtractRule)
     */
    public static boolean unzip(File in, File out, ExtractRule extractRule) {
        return unzip(in.toPath(), out.toPath(), extractRule);
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
                if (extractRule != null && extractRule.shouldExclude(entryName)) {
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
            App.settings.logStackTrace(e);
            return false;
        }

        return true;
    }

    public static boolean isSymlink(File file) {
        try {
            if (file == null) {
                throw new NullPointerException("File must not be null");
            }

            File canon;

            if (file.getParent() == null) {
                canon = file;
            } else {
                File canonDir = null;

                canonDir = file.getParentFile().getCanonicalFile();

                canon = new File(canonDir, file.getName());
            }

            return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @deprecated use zip(Path, Path)
     */
    public static void zip(File in, File out) {
        zip(in.toPath(), out.toPath());
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
            App.settings.logStackTrace(e);
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
